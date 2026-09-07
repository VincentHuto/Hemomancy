package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusPerformerResourceTest {
	private static final Path RESOURCES = Path.of("src/main/resources");
	private static final String CIRCUS_ASSETS = "assets/hemomancy/";
	private static final String CIRCUS_TEXTURES = CIRCUS_ASSETS
			+ "textures/entity/npc/harbinger/circus/";
	private static final String CIRCUS_MODELS = CIRCUS_ASSETS
			+ "models/entity/bbmodel/npc/harbinger/circus/";
	private static final String[] ROLES = { "fire_eater", "stilt_walker", "acrobat", "knife_thrower" };

	@Test
	void everyPerformerHasRuntimeAndEditableAssets() {
		for (String role : ROLES) {
			assertTrue(Files.isRegularFile(RESOURCES.resolve(CIRCUS_TEXTURES + role + "_0.png")),
					role + " texture 0");
			assertTrue(Files.isRegularFile(RESOURCES.resolve(CIRCUS_TEXTURES + role + "_1.png")),
					role + " texture 1");
			assertTrue(Files.isRegularFile(RESOURCES.resolve(
					"data/hemomancy/loot_table/entities/circus_" + role + ".json")), role + " loot table");
		}
		for (String model : new String[] { "CircusFireEaterModel", "CircusStiltWalkerModel",
				"CircusAcrobatModel", "CircusKnifeThrowerModel" }) {
			assertTrue(Files.isRegularFile(RESOURCES.resolve(CIRCUS_MODELS + model + ".bbmodel")), model);
		}
		assertTrue(Files.isRegularFile(RESOURCES.resolve(
				"data/hemomancy/tags/entity_type/circus_performers.json")));
	}

	@Test
	void performerArtUsesTheBloodDrunkPuppeteerScaleAndDistinctProps() throws IOException {
		String[] props = { "throat_frame", "stilt_brace", "aerial_ribbon", "knife_fan" };
		String[] models = { "CircusFireEaterModel", "CircusStiltWalkerModel",
				"CircusAcrobatModel", "CircusKnifeThrowerModel" };
		for (int i = 0; i < ROLES.length; i++) {
			var texture = ImageIO.read(RESOURCES.resolve(CIRCUS_TEXTURES + ROLES[i] + "_0.png").toFile());
			assertTrue(texture.getWidth() == 128 && texture.getHeight() == 128, ROLES[i] + " atlas size");
			String source = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/"
					+ models[i] + ".java"));
			assertTrue(source.contains("LayerDefinition.create(mesh, 128, 128)"), models[i] + " model scale");
			assertTrue(source.contains('"' + props[i] + '"'), models[i] + " profession prop");
		}
	}
}
