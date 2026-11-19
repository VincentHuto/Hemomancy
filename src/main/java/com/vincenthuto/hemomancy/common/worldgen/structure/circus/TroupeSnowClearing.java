package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * A troupe site's yards stay clear in snow (2026-11-17). The freeze pass that lays snow runs after structures, so a
 * pavilion, camp or stage built in a cold biome came out with its side rings, paths and yards snowed under. When a new
 * chunk holding part of one finishes generating, it is queued; on the next level tick every snow layer resting on a
 * {@code #hemomancy:troupe_yard_floor} block inside the site's box comes off. Snow on the grass round it stays.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TroupeSnowClearing {
	public static final TagKey<Block> YARD_FLOOR = TagKey.create(Registries.BLOCK, Hemomancy.rloc("troupe_yard_floor"));
	/** New chunks waiting for their next level tick, per level; session state, cleared with the server. */
	private static final java.util.Map<ServerLevel, java.util.Set<ChunkPos>> QUEUED = new java.util.HashMap<>();

	private TroupeSnowClearing() {
	}

	@SubscribeEvent
	public static void onChunkLoad(ChunkEvent.Load event) {
		if (!event.isNewChunk() || !(event.getLevel() instanceof ServerLevel level)
				|| !(event.getChunk() instanceof LevelChunk chunk)) return;
		if (troupeSites(level, chunk).isEmpty()) return;
		QUEUED.computeIfAbsent(level, ignored -> new java.util.HashSet<>()).add(chunk.getPos());
	}

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel level)) return;
		java.util.Set<ChunkPos> chunks = QUEUED.remove(level);
		if (chunks == null) return;
		for (ChunkPos pos : chunks) {
			if (!level.hasChunk(pos.x, pos.z)) continue;
			LevelChunk chunk = level.getChunk(pos.x, pos.z);
			for (BoundingBox box : troupeSites(level, chunk)) {
				int x0 = Math.max(box.minX(), pos.getMinBlockX()), x1 = Math.min(box.maxX(), pos.getMaxBlockX());
				int z0 = Math.max(box.minZ(), pos.getMinBlockZ()), z1 = Math.min(box.maxZ(), pos.getMaxBlockZ());
				for (BlockPos snow : snowOnYard(x0, z0, x1, z1, box.minY(), box.maxY() + 1,
						at -> level.getBlockState(at).is(Blocks.SNOW), at -> level.getBlockState(at).is(YARD_FLOOR)))
					level.setBlock(snow, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			}
		}
	}

	/** The boxes of the troupe sites (every {@link CircusPavilionStructure}) that reach into this chunk. */
	private static List<BoundingBox> troupeSites(ServerLevel level, LevelChunk chunk) {
		List<BoundingBox> boxes = new ArrayList<>();
		for (StructureStart start : level.structureManager().startsForStructure(chunk.getPos(),
				structure -> structure instanceof CircusPavilionStructure))
			if (start.isValid()) boxes.add(start.getBoundingBox());
		return boxes;
	}

	/** Every snow layer in the columns x0..x1, z0..z1 (rows minY..maxY) that rests on a yard-floor block. */
	public static List<BlockPos> snowOnYard(int x0, int z0, int x1, int z1, int minY, int maxY, Predicate<BlockPos> snow,
			Predicate<BlockPos> yard) {
		List<BlockPos> found = new ArrayList<>();
		for (int x = x0; x <= x1; x++)
			for (int z = z0; z <= z1; z++)
				for (int y = minY + 1; y <= maxY; y++) {
					BlockPos at = new BlockPos(x, y, z);
					if (snow.test(at) && yard.test(at.below())) found.add(at);
				}
		return found;
	}

	public static void clearSessionState() {
		QUEUED.clear();
	}
}
