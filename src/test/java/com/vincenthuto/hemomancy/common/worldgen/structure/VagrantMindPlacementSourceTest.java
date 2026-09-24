package com.vincenthuto.hemomancy.common.worldgen.structure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class VagrantMindPlacementSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void structureTypeIsRegistered() throws IOException {
		String structureInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/StructureInit.java");
		assertTrue(structureInit.contains("register(\"vagrant_mind\""),
				"the structure type must be registered");
		assertTrue(structureInit.contains("VagrantMindStructure.CODEC"),
				"registration must bind our codec");
	}

	@Test
	void placementSuitsFloatingEndIslands() throws IOException {
		String json = read("src/main/resources/data/hemomancy/worldgen/structure/vagrant_mind.json");
		assertTrue(json.contains("\"hemomancy:vagrant_mind\""), "structure type must be ours");
		assertTrue(json.contains("\"terrain_adaptation\": \"none\""),
				"End islands float — beardifier adaptation would smear terrain into the void");
		assertFalse(json.contains("project_start_to_heightmap"),
				"there is no surface to project onto in the End");
		assertTrue(json.contains("#hemomancy:has_structure/vagrant_mind"),
				"placement must be gated on the biome tag");
	}

	@Test
	void biomeTagTargetsTheDrift() throws IOException {
		assertTrue(read("src/main/resources/data/hemomancy/tags/worldgen/biome/has_structure/vagrant_mind.json")
				.contains("hemomancy:cortical_drift"), "the Mind only belongs in its own biome");
	}

	@Test
	void structureSetIsSparse() throws IOException {
		String json = read("src/main/resources/data/hemomancy/worldgen/structure_set/vagrant_mind.json");
		assertTrue(json.contains("\"spacing\""), "spacing must be declared");
		assertTrue(json.contains("random_spread"), "use random spread placement");
		assertTrue(json.contains("\"spacing\": 40") && json.contains("\"separation\": 20"),
				"spacing 40 / separation 20 so Minds are findable (archipelago addendum)");
	}

	@Test
	void structureSearchDoesNotRejectCandidatesByScanningTerrain() throws IOException {
		String source = read(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/VagrantMindStructure.java");
		assertFalse(source.contains("getBaseHeight") || source.contains("hasClearFootprint"),
				"terrain shaping belongs in piece placement; scanning and rejecting candidates makes Minds rare "
						+ "and causes /locate to stall");
	}

	@Test
	void pieceFitsNaturalTerrainToTheOrganicBrainSilhouette() throws IOException {
		String source = read(
				"src/main/java/com/vincenthuto/hemomancy/common/worldgen/structure/VagrantMindPiece.java");
		int carve = source.indexOf("carveNaturalTerrain(level, lobes");
		int shell = source.indexOf("VagrantMindGeometry.surfaceMaterial(");
		assertTrue(carve >= 0, "piece generation must shape intersecting terrain around the Mind");
		assertTrue(shell > carve, "natural terrain must be opened before the shell is placed");
		assertTrue(source.contains("VagrantMindGeometry.clearsTerrain("),
				"terrain shaping must follow the expanded lobe field instead of a box or sphere");
		assertTrue(source.contains("level.getBlockState(cursor).is(Blocks.END_STONE)"),
				"the carver must not erase arbitrary authored or structure blocks");
	}
}
