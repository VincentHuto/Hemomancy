package com.vincenthuto.hemomancy.common.item.shared;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MnemonicBlueprintAppearanceTest {
	@Test
	void assignsDistinctModelVariantsToBlankRiteAndStructureBlueprints() {
		assertEquals(0, MnemonicBlueprintAppearance.customModelData(null));
		assertEquals(1, MnemonicBlueprintAppearance.customModelData(
				MnemonicBlueprintTarget.Type.CARDINAL_RITE));
		assertEquals(2, MnemonicBlueprintAppearance.customModelData(
				MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE));
	}

	@Test
	void itemModelAndTexturesExposeAllThreeVariants() throws Exception {
		Path itemModels = Path.of("src/main/resources/assets/hemomancy/models/item");
		String baseModel = Files.readString(itemModels.resolve("mnemonic_blueprint.json"));
		assertTrue(baseModel.contains("mnemonic_blueprint_blank"));
		assertTrue(baseModel.contains("\"custom_model_data\": 1"));
		assertTrue(baseModel.contains("\"custom_model_data\": 2"));
		assertTrue(Files.exists(itemModels.resolve("mnemonic_blueprint_rite.json")));
		assertTrue(Files.exists(itemModels.resolve("mnemonic_blueprint_structure.json")));

		Path textures = Path.of("src/main/resources/assets/hemomancy/textures/item");
		BufferedImage blank = ImageIO.read(textures.resolve("mnemonic_blueprint_blank.png").toFile());
		BufferedImage rite = ImageIO.read(textures.resolve("mnemonic_blueprint_rite.png").toFile());
		BufferedImage structure = ImageIO.read(textures.resolve("mnemonic_blueprint_structure.png").toFile());
		assertEquals(16, blank.getWidth());
		assertEquals(16, blank.getHeight());
		assertEquals(16, rite.getWidth());
		assertEquals(16, rite.getHeight());
		assertEquals(16, structure.getWidth());
		assertEquals(16, structure.getHeight());
		assertNotEquals(blank.getRGB(8, 8), rite.getRGB(8, 8));
		assertNotEquals(rite.getRGB(8, 8), structure.getRGB(8, 8));
	}
}
