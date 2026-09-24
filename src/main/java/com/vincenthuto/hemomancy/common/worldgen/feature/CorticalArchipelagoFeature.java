package com.vincenthuto.hemomancy.common.worldgen.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.worldgen.CorticalArchipelagoPlan;
import com.vincenthuto.hemomancy.common.worldgen.CorticalArchipelagoPlan.Kind;
import com.vincenthuto.hemomancy.common.worldgen.CorticalArchipelagoPlan.Node;
import com.vincenthuto.hemomancy.common.worldgen.CorticalAxis;
import com.vincenthuto.hemomancy.common.worldgen.CorticalIslandShape;
import com.vincenthuto.hemomancy.common.worldgen.CorticalNetworkPlan;
import com.vincenthuto.hemomancy.common.worldgen.CorticalNetworkPlan.Cable;
import com.vincenthuto.hemomancy.common.worldgen.CorticalNetworkPlan.Island;

import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/**
 * Builds every Cortical Drift island, hub and nerve cable from {@link CorticalArchipelagoPlan}.
 *
 * <p>Each archipelago is owned by an actual Vagrant Mind candidate. Chunks inspect the nearby
 * structure-placement cells, rebuild the same two orbital rings around each qualifying Mind, and
 * write only their own blocks. A Cortical Drift therefore cannot produce the old free-floating
 * scatter with its Mind somewhere unrelated in the biome.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class CorticalArchipelagoFeature extends Feature<NoneFeatureConfiguration> {
	private static final int FOOTPRINT_PAD = 6;
	private static final int ORBIT_REACH = 320;
	private static final int HUB_RADIUS = 2;
	private static final int ANCHOR_RADIUS = 2;
	private static final double MIN_SPAN = 2.0D;
	private static final ResourceKey<StructureSet> VAGRANT_MIND_SET = ResourceKey
			.create(Registries.STRUCTURE_SET, Hemomancy.rloc("vagrant_mind"));

	private record Link(Node from, Node to, boolean bundle) {}
	private record MindCandidate(int x, int z) {}

	private record RegionPlan(List<Node> nodes, List<Link> links) {}

	/**
	 * Finished orbital plans keyed by world/dimension seed plus Mind centre. Every intersecting
	 * chunk reuses the same plan; the size check sits outside computeIfAbsent, the
	 * computation never touches this map, and the whole map is dropped when the server stops.
	 */
	private static final Map<Long, RegionPlan> REGION_CACHE = new ConcurrentHashMap<>();
	private static final int REGION_CACHE_LIMIT = 256;

	public CorticalArchipelagoFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@SubscribeEvent
	public static void clearRegionCache(ServerStoppedEvent event) {
		REGION_CACHE.clear();
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		ChunkPos chunk = new ChunkPos(context.origin());
		ChunkGenerator generator = context.chunkGenerator();
		RandomState randomState = level.getLevel().getChunkSource().randomState();
		int placed = 0;
		BiomeSource biomes = generator.getBiomeSource();
		Climate.Sampler sampler = randomState.sampler();
		for (MindCandidate mind : mindCandidates(level, biomes, sampler, chunk)) {
			long orbitSeed = orbitSeed(level, mind.x(), mind.z());
			if (!REGION_CACHE.containsKey(orbitSeed) && REGION_CACHE.size() > REGION_CACHE_LIMIT) {
				REGION_CACHE.clear();
			}
			RegionPlan plan = REGION_CACHE.computeIfAbsent(orbitSeed,
					seed -> planOrbit(level, generator, randomState, seed, mind));
			for (Node node : plan.nodes()) {
				placed += node.kind() == Kind.ISLAND ? drawIsland(level, chunk, node) : drawHub(level, chunk, node);
			}
			for (Link link : plan.links()) {
				placed += drawCable(level, chunk, link);
			}
			// Anchors last so each rim keeps its synaptic node instead of a cable segment.
			for (Link link : plan.links()) {
				double[][] ends = CorticalArchipelagoPlan.cableEndpoints(link.from(), link.to());
				if (link.from().kind() == Kind.ISLAND) {
					placed += drawAnchor(level, chunk, ends[0]);
				}
				if (link.to().kind() == Kind.ISLAND) {
					placed += drawAnchor(level, chunk, ends[1]);
				}
			}
		}
		return placed > 0;
	}

	private static long orbitSeed(WorldGenLevel level, int mindX, int mindZ) {
		long dimension = level.getLevel().dimension().location().hashCode() * 0x9E3779B97F4A7C15L;
		return level.getSeed() ^ dimension ^ (mindX * 341873128712L + mindZ * 132897987541L);
	}

	// ---- planning (biome source + density function only; never generated chunk state) ----

	private static RegionPlan planOrbit(WorldGenLevel level, ChunkGenerator generator, RandomState randomState,
			long orbitSeed, MindCandidate mind) {
		List<Node> nodes = CorticalArchipelagoPlan.plan(orbitSeed, mind.x(), mind.z(), (x, z, radius) -> {
			int reach = Mth.ceil(radius * 1.4D) + FOOTPRINT_PAD;
			// Edge registration also covers the barrens band at vanilla continent rims; keep our islands
			// in open void. getBaseHeight samples the density function, so this stays chunk-state-free.
			if (isSolid(level, generator, randomState, x, z) || isSolid(level, generator, randomState, x + reach, z)
					|| isSolid(level, generator, randomState, x - reach, z)
					|| isSolid(level, generator, randomState, x, z + reach)
					|| isSolid(level, generator, randomState, x, z - reach)) {
				return false;
			}
			return true;
		});

		Map<Island, Node> byCentre = new HashMap<>();
		List<Island> centres = new ArrayList<>();
		for (Node node : nodes) {
			Island centre = new Island(node.x(), node.y(), node.z());
			if (byCentre.putIfAbsent(centre, node) == null) {
				centres.add(centre);
			}
		}
		List<Link> links = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		for (Cable cable : CorticalNetworkPlan.connect(centres, orbitSeed)) {
			Node from = byCentre.get(cable.from());
			Node to = byCentre.get(cable.to());
			if (from == null || to == null || from == to) {
				continue;
			}
			double[][] ends = CorticalArchipelagoPlan.cableEndpoints(from, to);
			if (length(ends[0], ends[1]) < MIN_SPAN) {
				continue;
			}
			if (seen.add(unorderedKey(cable.from(), cable.to()))) {
				links.add(new Link(from, to, cable.bundle()));
			}
		}
		return new RegionPlan(List.copyOf(nodes), List.copyOf(links));
	}

	/**
	 * Actual Vagrant Mind placement candidates whose orbital systems could intersect this chunk.
	 * Uses the world seed exactly as random-spread structure placement does.
	 */
	private static List<MindCandidate> mindCandidates(WorldGenLevel level, BiomeSource biomes,
			Climate.Sampler sampler, ChunkPos chunk) {
		List<MindCandidate> result = new ArrayList<>();
		var set = level.registryAccess().registry(Registries.STRUCTURE_SET)
				.flatMap(registry -> registry.getOptional(VAGRANT_MIND_SET));
		if (set.isEmpty() || !(set.get().placement() instanceof RandomSpreadStructurePlacement placement)) {
			return result;
		}
		int spacing = placement.spacing();
		int minChunkX = Math.floorDiv(chunk.getMinBlockX() - ORBIT_REACH, 16);
		int maxChunkX = Math.floorDiv(chunk.getMaxBlockX() + ORBIT_REACH, 16);
		int minChunkZ = Math.floorDiv(chunk.getMinBlockZ() - ORBIT_REACH, 16);
		int maxChunkZ = Math.floorDiv(chunk.getMaxBlockZ() + ORBIT_REACH, 16);
		long worldSeed = level.getSeed();
		for (int gx = Math.floorDiv(minChunkX, spacing); gx <= Math.floorDiv(maxChunkX, spacing); gx++) {
			for (int gz = Math.floorDiv(minChunkZ, spacing); gz <= Math.floorDiv(maxChunkZ, spacing); gz++) {
				ChunkPos candidate = placement.getPotentialStructureChunk(worldSeed, gx * spacing, gz * spacing);
				int x = candidate.getMiddleBlockX();
				int z = candidate.getMiddleBlockZ();
				if (x >= chunk.getMinBlockX() - ORBIT_REACH && x <= chunk.getMaxBlockX() + ORBIT_REACH
						&& z >= chunk.getMinBlockZ() - ORBIT_REACH && z <= chunk.getMaxBlockZ() + ORBIT_REACH
						&& isDrift(biomes, sampler, x, z)) {
					result.add(new MindCandidate(x, z));
				}
			}
		}
		return result;
	}

	private static boolean isSolid(WorldGenLevel level, ChunkGenerator generator, RandomState randomState, int x,
			int z) {
		return generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState) > level
				.getMinBuildHeight();
	}

	private static boolean isDrift(BiomeSource biomes, Climate.Sampler sampler, int x, int z) {
		return biomes.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(64), QuartPos.fromBlock(z), sampler)
				.is(BiomeInit.CORTICAL_DRIFT);
	}

	private static String unorderedKey(Island a, Island b) {
		String first = a.x() + ":" + a.y() + ":" + a.z();
		String second = b.x() + ":" + b.y() + ":" + b.z();
		return first.compareTo(second) <= 0 ? first + "|" + second : second + "|" + first;
	}

	private static double length(double[] a, double[] b) {
		double dx = b[0] - a[0];
		double dy = b[1] - a[1];
		double dz = b[2] - a[2];
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	// ---- drawing (current chunk only) ----

	private int drawIsland(WorldGenLevel level, ChunkPos chunk, Node island) {
		int reach = CorticalIslandShape.maxReach(island);
		int minX = Math.max(island.x() - reach, chunk.getMinBlockX());
		int maxX = Math.min(island.x() + reach, chunk.getMaxBlockX());
		int minZ = Math.max(island.z() - reach, chunk.getMinBlockZ());
		int maxZ = Math.min(island.z() + reach, chunk.getMaxBlockZ());
		if (minX > maxX || minZ > maxZ) {
			return 0;
		}
		int depth = CorticalIslandShape.maxDepth(island);
		int placed = 0;
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int dy = -depth; dy <= 3; dy++) {
					CorticalIslandShape.Layer layer = CorticalIslandShape.sample(island, x - island.x(), dy,
							z - island.z());
					if (layer == null) {
						continue;
					}
					pos.set(x, island.y() + dy, z);
					if (level.isOutsideBuildHeight(pos)) {
						continue;
					}
					level.setBlock(pos, layerState(layer), 2);
					placed++;
				}
			}
		}
		return placed;
	}

	private static BlockState layerState(CorticalIslandShape.Layer layer) {
		return switch (layer) {
			case MYELIN -> BlockInit.myelin_sheath.get().defaultBlockState();
			case SLATE -> BlockInit.axonal_slate.get().defaultBlockState();
			case TISSUE -> BlockInit.neural_tissue.get().defaultBlockState();
			case GANGLION -> BlockInit.ganglion_matter.get().defaultBlockState();
		};
	}

	private int drawHub(WorldGenLevel level, ChunkPos chunk, Node hub) {
		int placed = 0;
		for (int dx = -HUB_RADIUS; dx <= HUB_RADIUS; dx++) {
			for (int dy = -HUB_RADIUS; dy <= HUB_RADIUS; dy++) {
				for (int dz = -HUB_RADIUS; dz <= HUB_RADIUS; dz++) {
					int d2 = dx * dx + dy * dy + dz * dz;
					if (d2 > HUB_RADIUS * HUB_RADIUS) {
						continue;
					}
					BlockPos pos = new BlockPos(hub.x() + dx, hub.y() + dy, hub.z() + dz);
					if (!inChunk(chunk, pos) || level.isOutsideBuildHeight(pos)) {
						continue;
					}
					BlockState state = d2 <= 1 ? BlockInit.synaptic_node.get().defaultBlockState()
							: BlockInit.ganglion_matter.get().defaultBlockState();
					level.setBlock(pos, state, 2);
					placed++;
				}
			}
		}
		return placed;
	}

	private int drawCable(WorldGenLevel level, ChunkPos chunk, Link link) {
		double[][] ends = CorticalArchipelagoPlan.cableEndpoints(link.from(), link.to());
		double[] start = ends[0];
		double[] end = ends[1];
		int pad = 3;
		if (Math.max(start[0], end[0]) + pad < chunk.getMinBlockX() || Math.min(start[0], end[0]) - pad > chunk.getMaxBlockX()
				|| Math.max(start[2], end[2]) + pad < chunk.getMinBlockZ()
				|| Math.min(start[2], end[2]) - pad > chunk.getMaxBlockZ()) {
			return 0;
		}
		// Horizontal perpendicular for the bundle's side strand.
		double hx = end[0] - start[0];
		double hz = end[2] - start[2];
		double horizontal = Math.sqrt(hx * hx + hz * hz);
		double px = horizontal < 1.0E-6D ? 1.0D : -hz / horizontal;
		double pz = horizontal < 1.0E-6D ? 0.0D : hx / horizontal;
		double[][] offsets = link.bundle()
				? new double[][] { { 0, 0, 0 }, { px, 0, pz }, { 0, 1, 0 } }
				: new double[][] { { 0, 0, 0 } };
		BlockState base = (link.bundle() ? BlockInit.nerve_bundle.get() : BlockInit.nerve_fiber.get())
				.defaultBlockState();

		int steps = Mth.ceil(length(start, end) * 2.0D) + 2;
		int placed = 0;
		double[] previous = CorticalArchipelagoPlan.sag(start, end, 0.0D);
		for (int step = 0; step <= steps; step++) {
			double[] point = CorticalArchipelagoPlan.sag(start, end, step / (double) steps);
			BlockState state = base.setValue(RotatedPillarBlock.AXIS, CorticalAxis.between(previous, point));
			for (double[] offset : offsets) {
				BlockPos pos = BlockPos.containing(point[0] + offset[0], point[1] + offset[1], point[2] + offset[2]);
				if (inChunk(chunk, pos)) {
					placed += setIfOpen(level, pos, state);
				}
			}
			previous = point;
		}
		return placed;
	}

	/** Neural mass rooting a cable at an island rim, capped by a synaptic node at the exact endpoint. */
	private int drawAnchor(WorldGenLevel level, ChunkPos chunk, double[] endpoint) {
		BlockPos centre = BlockPos.containing(endpoint[0], endpoint[1], endpoint[2]);
		int placed = 0;
		for (int dx = -ANCHOR_RADIUS; dx <= ANCHOR_RADIUS; dx++) {
			for (int dy = -ANCHOR_RADIUS; dy <= ANCHOR_RADIUS; dy++) {
				for (int dz = -ANCHOR_RADIUS; dz <= ANCHOR_RADIUS; dz++) {
					if (dx * dx + dy * dy + dz * dz > ANCHOR_RADIUS * ANCHOR_RADIUS) {
						continue;
					}
					BlockPos pos = centre.offset(dx, dy, dz);
					if (inChunk(chunk, pos)) {
						placed += setIfOpen(level, pos, anchorState(pos, dy));
					}
				}
			}
		}
		if (inChunk(chunk, centre) && !level.isOutsideBuildHeight(centre)) {
			level.setBlock(centre, BlockInit.synaptic_node.get().defaultBlockState(), 2);
			placed++;
		}
		return placed;
	}

	private static BlockState anchorState(BlockPos pos, int dy) {
		if (Math.floorMod(pos.getX() + pos.getY() + pos.getZ(), 5) == 0) {
			return BlockInit.ganglion_matter.get().defaultBlockState();
		}
		if (dy < 0) {
			return BlockInit.axonal_slate.get().defaultBlockState();
		}
		return BlockInit.neural_tissue.get().defaultBlockState();
	}

	private static boolean inChunk(ChunkPos chunk, BlockPos pos) {
		return pos.getX() >= chunk.getMinBlockX() && pos.getX() <= chunk.getMaxBlockX()
				&& pos.getZ() >= chunk.getMinBlockZ() && pos.getZ() <= chunk.getMaxBlockZ();
	}

	private static int setIfOpen(WorldGenLevel level, BlockPos pos, BlockState state) {
		if (level.isOutsideBuildHeight(pos) || !level.getBlockState(pos).canBeReplaced()) {
			return 0;
		}
		level.setBlock(pos, state, 2);
		return 1;
	}
}
