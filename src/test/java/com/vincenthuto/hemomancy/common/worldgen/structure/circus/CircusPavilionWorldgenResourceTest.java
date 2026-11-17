package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CircusPavilionWorldgenResourceTest {
	private static final Path DATA = Path.of("src/main/resources/data/hemomancy");

	@Test
	void allChecksPass() throws Exception {
		String structure = read("worldgen/structure/circus_pavilion.json");
		String structureSet = read("worldgen/structure_set/circus_pavilion.json");
		String pool = read("worldgen/template_pool/crimson_troupe_pavilion/start_pool.json");
		String biomes = read("tags/worldgen/biome/has_structure/circus_pavilion.json");

		assert structure.contains("\"type\": \"hemomancy:circus_pavilion\"");
		assert structure.contains("\"project_start_to_heightmap\": \"WORLD_SURFACE_WG\"");
		assert structureSet.contains("\"spacing\": 48");
		assert structureSet.contains("\"separation\": 16");
		assert pool.contains("\"location\": \"hemomancy:crimson_troupe_pavilion\"");
		assert biomes.contains("\"minecraft:plains\"");
		assert biomes.contains("\"minecraft:savanna\"");
		assert !biomes.contains("ocean");
		assert !biomes.contains("swamp");
	}

	@Test
	void pavilionSkipsTheRuntimeNeighborSweep() throws Exception {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/circus/CircusPavilionStructure.java"));
		assertFalse(source.contains("updateNeighborsAt"));
	}

	/** NQ-15-2 and D-43 (chunk 15 T10): the owner's spacings, the exclusion from pavilions and the sites' own ground. */
	@Test
	void troupeSitesKeepTheirSpacingsAndBringTheirOwnGround() throws Exception {
		Map<String, int[]> spacing = Map.of("circus_pavilion", new int[]{48, 16}, "troupe_teaching_camps", new int[]{24, 8},
				"troupe_abandoned_stages", new int[]{32, 10});
		for (var entry : spacing.entrySet()) {
			String set = read("worldgen/structure_set/" + entry.getKey() + ".json");
			assertTrue(set.contains("\"spacing\": " + entry.getValue()[0]), entry.getKey());
			assertTrue(set.contains("\"separation\": " + entry.getValue()[1]), entry.getKey());
			assertEquals(!entry.getKey().equals("circus_pavilion"), set.contains("\"other_set\": \"hemomancy:circus_pavilion\""),
					entry.getKey() + " keeps its exclusion zone from pavilions");
		}
		for (String site : java.util.List.of("circus_pavilion", "troupe_abandoned_stage", "troupe_camp_air", "troupe_camp_blades",
				"troupe_camp_pageantry", "troupe_camp_embers", "troupe_camp_memory")) {
			String structure = read("worldgen/structure/" + site + ".json");
			assertTrue(structure.contains("\"terrain_adaptation\": \"beard_box\""), site + " brings its own ground");
			// The camps and the stage carry a foundation skirt in row 0, so row 1 lands on the top ground block.
			assertTrue(structure.contains("\"absolute\": " + (site.equals("circus_pavilion") ? "0" : "-1")), site);
		}
	}

	private static String read(String relative) throws Exception {
		return Files.readString(DATA.resolve(relative)).replace("\r\n", "\n");
	}
}
