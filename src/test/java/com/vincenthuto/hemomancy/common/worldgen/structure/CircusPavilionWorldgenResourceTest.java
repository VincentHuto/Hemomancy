package com.vincenthuto.hemomancy.common.worldgen.structure;

import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

	private static String read(String relative) throws Exception {
		return Files.readString(DATA.resolve(relative)).replace("\r\n", "\n");
	}
}
