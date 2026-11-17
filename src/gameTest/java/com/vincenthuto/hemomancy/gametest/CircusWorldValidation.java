package com.vincenthuto.hemomancy.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.structure.StructurePlacementChecks;
import com.vincenthuto.hemomancy.common.worldgen.structure.circus.CircusPavilionStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Chunk 15 T10 (NQ-15-2, D-43): Crimson Troupe placement on a fresh seeded noise Overworld. Every potential start chunk
 * of the pavilion, camp and stage sets within {@link #HALF} blocks of the origin is generated to structure starts; each
 * is recorded as accepted or refused, with the reason a refusal comes from (an exclusion zone from pavilions, a nearby
 * village, the biome, wet or high ground, too steep), and the nearest accepted site of each kind. The report is
 * {@code circus-report.json} in the run's game directory. The owner's aim is a pavilion within about 1,500 blocks; the
 * run reports the distance and does not fail on it.
 */
@GameTestHolder("circus_worldgen_validation")
public final class CircusWorldValidation {
	private static final int HALF = 4096;
	private static final int EXCLUSION_CHUNKS = 6;
	private static final ResourceLocation PAVILIONS = Hemomancy.rloc("circus_pavilion");
	private static final Map<ResourceLocation, int[]> SETS = new LinkedHashMap<>();

	static {
		// set id -> the footprint a refusal is judged on (x, z)
		SETS.put(PAVILIONS, new int[]{81, 65});
		SETS.put(Hemomancy.rloc("troupe_teaching_camps"), new int[]{27, 27});
		SETS.put(Hemomancy.rloc("troupe_abandoned_stages"), new int[]{17, 15});
	}

	private CircusWorldValidation() {
	}

	@GameTestGenerator
	public static Collection<TestFunction> tests() {
		if (System.getProperty("hemomancy.circus.validationSeed") == null) return List.of();
		return List.of(new TestFunction("circus_worldgen", "circus_placement", "circus_worldgen_validation:room",
				12000, 0, true, CircusWorldValidation::inspect));
	}

	private static void inspect(GameTestHelper h) {
		try {
			run(h);
		} catch (Exception e) {
			throw new RuntimeException("Circus worldgen validation failed", e);
		}
	}

	private static void run(GameTestHelper h) throws Exception {
		ServerLevel level = h.getLevel();
		long seed = level.getSeed();
		h.assertTrue(seed == Long.getLong("hemomancy.circus.validationSeed", 42), "Requested seed must be used");
		var state = level.getChunkSource().getGeneratorState();
		var sets = level.registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
		var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		var pavilionSet = sets.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE_SET, PAVILIONS));

		var report = new JsonObject();
		report.addProperty("seed", seed);
		report.addProperty("half", HALF);
		var nearest = new TreeMap<String, Double>();
		long started = System.nanoTime();
		for (var entry : SETS.entrySet()) {
			StructureSet set = sets.getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET, entry.getKey()));
			var spread = (RandomSpreadStructurePlacement) set.placement();
			Structure structure = set.structures().getFirst().structure().value();
			var reasons = new TreeMap<String, Integer>();
			var accepted = new JsonArray();
			int cells = 0;
			int min = Math.floorDiv(Math.floorDiv(-HALF, 16), spread.spacing());
			int max = Math.floorDiv(Math.floorDiv(HALF, 16), spread.spacing());
			for (int rx = min; rx <= max; rx++) for (int rz = min; rz <= max; rz++) {
				ChunkPos chunk = spread.getPotentialStructureChunk(seed, rx * spread.spacing(), rz * spread.spacing());
				if (Math.abs(chunk.getMiddleBlockX()) > HALF || Math.abs(chunk.getMiddleBlockZ()) > HALF) continue;
				cells++;
				var access = level.getChunk(chunk.x, chunk.z, ChunkStatus.STRUCTURE_STARTS, true);
				var found = access.getAllStarts().entrySet().stream()
						.filter(s -> s.getValue().isValid() && set.structures().stream().anyMatch(e -> e.structure().value() == s.getKey()))
						.findFirst();
				if (found.isPresent()) {
					BoundingBox box = found.get().getValue().getBoundingBox();
					var id = structures.getKey(found.get().getKey());
					BlockPos centre = box.getCenter();
					double distance = Math.hypot(centre.getX(), centre.getZ());
					var row = new JsonObject();
					row.addProperty("site", String.valueOf(id));
					row.addProperty("x", centre.getX());
					row.addProperty("y", box.minY());
					row.addProperty("z", centre.getZ());
					row.addProperty("distance", Math.round(distance));
					accepted.add(row);
					nearest.merge(String.valueOf(id), distance, Math::min);
					reasons.merge("accepted", 1, Integer::sum);
				} else {
					reasons.merge(refusal(level, structure, state, pavilionSet, entry, chunk), 1, Integer::sum);
				}
			}
			var row = new JsonObject();
			row.addProperty("cells", cells);
			row.add("outcomes", new GsonBuilder().create().toJsonTree(reasons));
			row.add("accepted", accepted);
			report.add(entry.getKey().getPath(), row);
			LogUtils.getLogger().info("CIRCUS_WORLDGEN seed {} set {}: {} cells, {}", seed, entry.getKey().getPath(), cells, reasons);
		}
		report.add("nearest", new GsonBuilder().create().toJsonTree(nearest));
		report.addProperty("seconds", (System.nanoTime() - started) / 1e9);
		Files.writeString(Path.of("circus-report.json"), new GsonBuilder().setPrettyPrinting().create().toJson(report));
		LogUtils.getLogger().info("CIRCUS_WORLDGEN seed {} nearest site of each kind (blocks from the origin): {}", seed, nearest);
		h.assertTrue(nearest.containsKey(PAVILIONS.toString()), "no pavilion within " + HALF + " blocks: " + nearest);
		for (String camp : List.of("air", "blades", "pageantry", "embers", "memory"))
			h.assertTrue(nearest.containsKey("hemomancy:troupe_camp_" + camp), "no " + camp + " camp within " + HALF + " blocks");
		h.assertTrue(nearest.containsKey("hemomancy:troupe_abandoned_stage"), "no abandoned stage within " + HALF + " blocks");
		h.succeed();
	}

	/** Why a potential cell holds no start, in the order the game asks. */
	private static String refusal(ServerLevel level, Structure structure,
			net.minecraft.world.level.chunk.ChunkGeneratorStructureState state,
			net.minecraft.core.Holder<StructureSet> pavilions, Map.Entry<ResourceLocation, int[]> set, ChunkPos chunk) {
		if (!set.getKey().equals(PAVILIONS) && state.hasStructureChunkInRange(pavilions, chunk.x, chunk.z, EXCLUSION_CHUNKS))
			return "exclusion_from_pavilion";
		var generator = level.getChunkSource().getGenerator();
		var context = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(),
				level.getChunkSource().randomState(), level.getStructureManager(), level.getSeed(), chunk, level,
				structure.biomes()::contains);
		if (StructurePlacementChecks.nearVillage(context, chunk, CircusPavilionStructure.VILLAGE_CHUNKS)) return "village";
		int x = chunk.getMinBlockX() + set.getValue()[0] / 2, z = chunk.getMinBlockZ() + set.getValue()[1] / 2;
		int y = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, context.randomState());
		if (!structure.biomes().contains(generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
				QuartPos.fromBlock(z), context.randomState().sampler()))) return "biome";
		BoundingBox box = new BoundingBox(chunk.getMinBlockX(), y, chunk.getMinBlockZ(),
				chunk.getMinBlockX() + set.getValue()[0] - 1, y + 10, chunk.getMinBlockZ() + set.getValue()[1] - 1);
		if (structure instanceof CircusPavilionStructure troupe) box = troupe.gateBox(box);
		return StructurePlacementChecks.troupeRefusal(context, box).orElse("other");
	}
}
