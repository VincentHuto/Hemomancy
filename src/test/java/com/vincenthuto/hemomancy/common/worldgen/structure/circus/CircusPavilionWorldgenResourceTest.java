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

	public static void main(String[] args) throws Exception {
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
	void pavilionPlacesHarbingerStyleAbocipherEmitters() throws Exception {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/circus/CircusPavilionStructure.java"));
		assertTrue(source.contains("AbocipherEmitterPlacement.placeHarbingerOutpostEmitters"));
	}

	@Test
	void pavilionSkipsTheRuntimeNeighborSweep() throws Exception {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/circus/CircusPavilionStructure.java"));
		assertFalse(source.contains("updateNeighborsAt"));
	}

	private static String read(String relative) throws Exception {
		return Files.readString(DATA.resolve(relative)).replace("\r\n", "\n");
	}
}
