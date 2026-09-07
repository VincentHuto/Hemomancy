package com.vincenthuto.hemomancy.common.worldgen.structure;

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
		String pool = read("worldgen/template_pool/circus_pavilion/start_pool.json");
		String biomes = read("tags/worldgen/biome/has_structure/circus_pavilion.json");

		assert structure.contains("\"type\": \"hemomancy:circus_pavilion\"");
		assert structure.contains("\"project_start_to_heightmap\": \"WORLD_SURFACE_WG\"");
		assert structureSet.contains("\"spacing\": 48");
		assert structureSet.contains("\"separation\": 16");
		assert pool.contains("\"location\": \"hemomancy:circus_pavilion\"");
		assert biomes.contains("\"minecraft:plains\"");
		assert biomes.contains("\"minecraft:savanna\"");
		assert !biomes.contains("ocean");
		assert !biomes.contains("swamp");

		byte[] template = Files.readAllBytes(Path.of(
				"src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt"));
		assert template.length > 1_000;
		assert (template[0] & 0xff) == 0x1f && (template[1] & 0xff) == 0x8b : "template is not compressed NBT";
	}

	@Test
	void generatedPavilionCarriesTwoCapturedPrismCuttles() throws Exception {
		var root = NbtIo.readCompressed(Path.of(
				"src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt"), NbtAccounter.unlimitedHeap());
		var blocks = root.getList("blocks", Tag.TAG_COMPOUND);
		int jars = 0;
		for (int i = 0; i < blocks.size(); i++) {
			if ("hemomancy:prism_cuttle".equals(
					blocks.getCompound(i).getCompound("nbt").getCompound("Specimen").getString("id"))) jars++;
		}
		assertEquals(2, jars);
	}

	@Test
	void generatedPavilionContainsTheCurtainWall() throws Exception {
		var root = NbtIo.readCompressed(Path.of(
				"src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt"), NbtAccounter.unlimitedHeap());
		var palette = root.getList("palette", Tag.TAG_COMPOUND);
		Set<Integer> curtainStates = new HashSet<>();
		for (int i = 0; i < palette.size(); i++) {
			if ("hemomancy:circus_curtain".equals(palette.getCompound(i).getString("Name"))) curtainStates.add(i);
		}

		var blocks = root.getList("blocks", Tag.TAG_COMPOUND);
		int curtains = 0;
		for (int i = 0; i < blocks.size(); i++) {
			if (curtainStates.contains(blocks.getCompound(i).getInt("state"))) curtains++;
		}
		assertEquals(660, curtains);
	}

	@Test
	void pavilionPlacesHarbingerStyleAbocipherEmitters() throws Exception {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/CircusPavilionStructure.java"));
		assertTrue(source.contains("AbocipherEmitterPlacement.placeHarbingerOutpostEmitters"));
	}

	@Test
	void pavilionSkipsTheRuntimeNeighborSweep() throws Exception {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/CircusPavilionStructure.java"));
		assertFalse(source.contains("updateNeighborsAt"));
	}

	@Test
	void generatedCurtainStatesContainTheirConnections() throws Exception {
		var root = NbtIo.readCompressed(Path.of(
				"src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt"), NbtAccounter.unlimitedHeap());
		var palette = root.getList("palette", Tag.TAG_COMPOUND);
		var blocks = root.getList("blocks", Tag.TAG_COMPOUND);
		Map<String, Integer> curtains = new HashMap<>();
		Set<Integer> curtainStates = new HashSet<>();
		for (int i = 0; i < palette.size(); i++) {
			if ("hemomancy:circus_curtain".equals(palette.getCompound(i).getString("Name"))) curtainStates.add(i);
		}
		for (int i = 0; i < blocks.size(); i++) {
			var block = blocks.getCompound(i);
			if (!curtainStates.contains(block.getInt("state"))) continue;
			var pos = block.getList("pos", Tag.TAG_INT);
			curtains.put(pos.getInt(0) + ":" + pos.getInt(1) + ":" + pos.getInt(2), block.getInt("state"));
		}
		for (var curtain : curtains.entrySet()) {
			String[] coordinates = curtain.getKey().split(":");
			int x = Integer.parseInt(coordinates[0]);
			int y = Integer.parseInt(coordinates[1]);
			int z = Integer.parseInt(coordinates[2]);
			var properties = palette.getCompound(curtain.getValue()).getCompound("Properties");
			assertEquals(Boolean.toString(curtains.containsKey(x + ":" + y + ":" + (z - 1))), properties.getString("north"));
			assertEquals(Boolean.toString(curtains.containsKey((x + 1) + ":" + y + ":" + z)), properties.getString("east"));
			assertEquals(Boolean.toString(curtains.containsKey(x + ":" + y + ":" + (z + 1))), properties.getString("south"));
			assertEquals(Boolean.toString(curtains.containsKey((x - 1) + ":" + y + ":" + z)), properties.getString("west"));
		}
	}

	@Test
	void generatedLanternChainsCarryTheirPlacedStates() throws Exception {
		var root = NbtIo.readCompressed(Path.of(
				"src/generated/resources/data/hemomancy/structure/circus_pavilion.nbt"), NbtAccounter.unlimitedHeap());
		var palette = root.getList("palette", Tag.TAG_COMPOUND);
		boolean verticalChain = false;
		boolean hangingLantern = false;
		for (int i = 0; i < palette.size(); i++) {
			var state = palette.getCompound(i);
			var name = state.getString("Name");
			var properties = state.getCompound("Properties");
			if ("hemomancy:hematic_iron_chain".equals(name)) verticalChain |= "y".equals(properties.getString("axis"));
			if ("hemomancy:hematic_lantern".equals(name)) hangingLantern |= "true".equals(properties.getString("hanging"));
			assertFalse(name.endsWith(":lantern") && !"hemomancy:hematic_lantern".equals(name));
		}
		assertTrue(verticalChain);
		assertTrue(hangingLantern);
	}

	private static String read(String relative) throws Exception {
		return Files.readString(DATA.resolve(relative)).replace("\r\n", "\n");
	}
}
