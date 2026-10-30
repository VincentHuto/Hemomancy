package com.vincenthuto.hemomancy.common.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.AdipocereCrustBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.world.AdipocereCrustRules.Host;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Crusts corpse wax onto the walls of vanilla monster rooms, strongholds and ancient cities. Strongholds and monster
 * rooms are code-built, so no template processor reaches them; instead each chunk that holds a site is decorated once,
 * the first time it loads, and recorded in {@link AdipocereCrustSavedData}. Loading only queues the chunk: the pass
 * runs on the next level tick, a few chunks at a time, and only touches blocks inside the chunk it decorates.
 *
 * <p>A site is a monster room (found by its spawner on a cobblestone floor; the room is a feature, not a structure)
 * or one stronghold or ancient city start whose pieces touch the chunk. The rules are {@link AdipocereCrustRules}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class AdipocereCrustDecoration {
	public static final TagKey<Block> WALLS = TagKey.create(Registries.BLOCK, Hemomancy.rloc("adipocere_crust_walls"));
	private static final int CHUNKS_PER_TICK = 4;
	private static final Map<ResourceKey<Level>, LongLinkedOpenHashSet> PENDING = new HashMap<>();

	/** One place crusts may grow, with the cells it covers. */
	public record Site(Host host, long key, List<BoundingBox> regions) {}

	private record Candidate(BlockPos pos, Direction facing) {}

	private AdipocereCrustDecoration() {
	}

	public static void clearSessionState() {
		PENDING.clear();
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event) {
		if (!(event.getLevel() instanceof ServerLevel level) || !(event.getChunk() instanceof LevelChunk chunk)) return;
		if (!mayHost(level, chunk) || AdipocereCrustSavedData.get(level).isDecorated(chunk.getPos())) return;
		PENDING.computeIfAbsent(level.dimension(), key -> new LongLinkedOpenHashSet()).add(chunk.getPos().toLong());
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		LongLinkedOpenHashSet queue = PENDING.get(level.dimension());
		if (queue == null || queue.isEmpty()) return;
		for (int i = 0; i < CHUNKS_PER_TICK && !queue.isEmpty(); i++) {
			ChunkPos pos = new ChunkPos(queue.removeFirstLong());
			LevelChunk chunk = level.getChunkSource().getChunkNow(pos.x, pos.z);
			// An unloaded chunk is queued again the next time it loads.
			if (chunk != null) decorate(level, chunk);
		}
	}

	/** Decorates a loaded chunk once; returns how many crusts it placed (0 when the chunk was already done). */
	public static int decorate(ServerLevel level, LevelChunk chunk) {
		List<Site> sites = sitesIn(level, chunk);
		if (sites.isEmpty()) return 0;
		return decorateOnce(level, chunk.getPos(), sites, HemoServerConfig.ADIPOCERE_CRUST_DENSITY.get());
	}

	/** Runs the pass over {@code sites} unless the chunk is already recorded, then records it. */
	public static int decorateOnce(ServerLevel level, ChunkPos chunk, List<Site> sites, double density) {
		AdipocereCrustSavedData data = AdipocereCrustSavedData.get(level);
		if (data.isDecorated(chunk)) return 0;
		int placed = place(level, chunk, sites, density).size();
		data.markDecorated(chunk);
		return placed;
	}

	/** Places the crusts the rules choose for {@code sites} in one chunk, unrecorded; returns where they went. */
	public static List<BlockPos> place(ServerLevel level, ChunkPos chunk, List<Site> sites, double density) {
		List<BlockPos> placed = new ArrayList<>();
		for (Site site : sites) {
			int remaining = AdipocereCrustRules.remaining(placed.size());
			if (remaining == 0) break;
			long seed = AdipocereCrustRules.seed(level.getSeed(), chunk.toLong(), site.host(), site.key());
			int crusts = Math.min(remaining, AdipocereCrustRules.crustsFor(site.host(), density, seed));
			if (crusts == 0) continue;
			List<Candidate> candidates = candidates(level, chunk, site);
			for (int index : AdipocereCrustRules.pick(candidates.size(), crusts, seed)) {
				Candidate candidate = candidates.get(index);
				BlockState crust = BlockInit.adipocere_crust.get().defaultBlockState()
						.setValue(AdipocereCrustBlock.FACING, candidate.facing());
				if (!crust.canSurvive(level, candidate.pos())) continue;
				level.setBlock(candidate.pos(), crust, Block.UPDATE_CLIENTS);
				placed.add(candidate.pos());
			}
		}
		return placed;
	}

	/** A cheap test at load time: does the chunk reference a host structure or hold a spawner? */
	private static boolean mayHost(ServerLevel level, LevelChunk chunk) {
		var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		for (Structure structure : chunk.getAllReferences().keySet()) {
			ResourceLocation id = registry.getKey(structure);
			if (id != null && AdipocereCrustRules.hostFor(id.toString()) != null) return true;
		}
		for (BlockPos pos : chunk.getBlockEntitiesPos()) {
			if (chunk.getBlockState(pos).is(Blocks.SPAWNER)) return true;
		}
		return false;
	}

	/** The sites touching a chunk, in a fixed order so the pass is the same in every copy of the world. */
	public static List<Site> sitesIn(ServerLevel level, LevelChunk chunk) {
		ChunkPos pos = chunk.getPos();
		List<Site> sites = new ArrayList<>();
		var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		for (Structure structure : chunk.getAllReferences().keySet()) {
			ResourceLocation id = registry.getKey(structure);
			Host host = id == null ? null : AdipocereCrustRules.hostFor(id.toString());
			if (host == null) continue;
			for (StructureStart start : level.structureManager().startsForStructure(pos, s -> s == structure)) {
				if (!start.isValid()) continue;
				List<BoundingBox> regions = start.getPieces().stream().map(StructurePiece::getBoundingBox)
						.filter(box -> box.intersects(pos.getMinBlockX(), pos.getMinBlockZ(), pos.getMaxBlockX(),
								pos.getMaxBlockZ()))
						.toList();
				if (!regions.isEmpty()) sites.add(new Site(host, start.getChunkPos().toLong(), regions));
			}
		}
		chunk.getBlockEntitiesPos().stream().sorted(Comparator.comparingLong(BlockPos::asLong))
				.filter(spawner -> isMonsterRoomSpawner(chunk, spawner))
				.forEach(spawner -> sites.add(monsterRoom(spawner)));
		sites.sort(Comparator.comparingInt((Site site) -> site.host().ordinal()).thenComparingLong(Site::key));
		return sites;
	}

	/** A vanilla dungeon spawner stands on its cobblestone or mossy cobblestone floor. */
	private static boolean isMonsterRoomSpawner(LevelChunk chunk, BlockPos pos) {
		if (!chunk.getBlockState(pos).is(Blocks.SPAWNER)) return false;
		BlockState floor = chunk.getBlockState(pos.below());
		return floor.is(Blocks.COBBLESTONE) || floor.is(Blocks.MOSSY_COBBLESTONE);
	}

	/** The interior a monster room can have around its spawner: up to three cells out, four cells high. */
	public static Site monsterRoom(BlockPos spawner) {
		int reach = AdipocereCrustRules.MONSTER_ROOM_REACH;
		BoundingBox room = new BoundingBox(spawner.getX() - reach, spawner.getY(), spawner.getZ() - reach,
				spawner.getX() + reach, spawner.getY() + 3, spawner.getZ() + reach);
		return new Site(Host.MONSTER_ROOM, spawner.asLong(), List.of(room));
	}

	/** Every face in the site's regions, inside this chunk, that may take a crust; in a fixed order. */
	private static List<Candidate> candidates(ServerLevel level, ChunkPos chunk, Site site) {
		Map<BlockPos, Candidate> found = new LinkedHashMap<>();
		BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
		for (BoundingBox region : site.regions()) {
			int minX = Math.max(region.minX(), chunk.getMinBlockX());
			int maxX = Math.min(region.maxX(), chunk.getMaxBlockX());
			int minZ = Math.max(region.minZ(), chunk.getMinBlockZ());
			int maxZ = Math.min(region.maxZ(), chunk.getMaxBlockZ());
			for (int y = region.minY(); y <= region.maxY(); y++) {
				for (int z = minZ; z <= maxZ; z++) {
					for (int x = minX; x <= maxX; x++) {
						cell.set(x, y, z);
						if (found.containsKey(cell) || !level.getBlockState(cell).isAir()) continue;
						Direction facing = wallFacing(level, chunk, cell);
						if (facing != null) found.put(cell.immutable(), new Candidate(cell.immutable(), facing));
					}
				}
			}
		}
		return new ArrayList<>(found.values());
	}

	/** The way a crust in {@code cell} would face out of its wall, or null when no neighbour is a wall. */
	private static Direction wallFacing(ServerLevel level, ChunkPos chunk, BlockPos cell) {
		if (!roofed(level, cell)) return null;
		for (Direction toWall : Direction.Plane.HORIZONTAL) {
			BlockPos wall = cell.relative(toWall);
			if (!inChunk(chunk, wall)) continue;
			BlockState support = level.getBlockState(wall);
			boolean isWall = support.is(WALLS) && support.isFaceSturdy(level, wall, toWall.getOpposite());
			boolean continues = level.getBlockState(wall.above()).is(WALLS) || level.getBlockState(wall.below()).is(WALLS);
			if (AdipocereCrustRules.isCandidate(true, isWall, continues, true)) return toWall.getOpposite();
		}
		return null;
	}

	private static boolean roofed(ServerLevel level, BlockPos cell) {
		BlockPos.MutableBlockPos above = cell.mutable();
		for (int i = 1; i <= AdipocereCrustRules.ROOF_REACH; i++) {
			above.move(Direction.UP);
			if (!level.getBlockState(above).isAir()) return true;
		}
		return false;
	}

	private static boolean inChunk(ChunkPos chunk, BlockPos pos) {
		return pos.getX() >> 4 == chunk.x && pos.getZ() >> 4 == chunk.z;
	}
}
