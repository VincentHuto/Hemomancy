package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CorticalDriftBlockPaletteSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void bulkTerrainBlocksAreRegistered() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		for (String id : new String[] { "neural_tissue", "axonal_slate", "myelin_sheath",
				"ganglion_matter", "cortical_folds" }) {
			assertTrue(blockInit.contains("BASEBLOCKS.register(\"" + id + "\""),
					"BlockInit should register " + id);
		}
	}

	@Test
	void ganglionMatterEmitsLight() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		int index = blockInit.indexOf("BASEBLOCKS.register(\"ganglion_matter\"");
		assertTrue(index > 0, "ganglion_matter must be registered");
		String declaration = blockInit.substring(index, index + 400);
		assertTrue(declaration.contains("lightLevel"), "ganglion_matter should emit light");
	}

	@Test
	void fiberBlocksAreAxisAligned() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		for (String id : new String[] { "nerve_fiber", "nerve_bundle" }) {
			int index = blockInit.indexOf("BASEBLOCKS.register(\"" + id + "\"");
			assertTrue(index > 0, "BlockInit should register " + id);
			assertTrue(blockInit.substring(index, index + 300).contains("RotatedPillarBlock"),
					id + " must be a RotatedPillarBlock so fibers can run on any axis");
		}
	}

	@Test
	void synapticNodeIsBrightAndTagged() throws IOException {
		String blockInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
		int index = blockInit.indexOf("BASEBLOCKS.register(\"synaptic_node\"");
		assertTrue(index > 0, "synaptic_node must be registered");
		assertTrue(blockInit.substring(index, index + 400).contains("lightLevel(state -> 10)"),
				"synaptic_node should be the brightest block in the palette");

		String conductors = read("src/main/resources/data/hemomancy/tags/block/ductilis_conductors.json");
		assertTrue(conductors.contains("hemomancy:synaptic_node"),
				"synaptic_node must conduct so Ferric Ductilis chains along the bridges");
	}

	@Test
	void duraMembraneModelIsTranslucent() throws IOException {
		String model = read("src/generated/resources/assets/hemomancy/models/block/dura_membrane.json");
		assertTrue(model.contains("\"render_type\": \"minecraft:translucent\""),
				"dura_membrane is a membrane stretched over cavity openings; without the translucent "
						+ "render type it renders as an opaque wall and defeats its purpose");
	}
}
