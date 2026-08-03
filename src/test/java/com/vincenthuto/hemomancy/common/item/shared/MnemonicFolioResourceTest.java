package com.vincenthuto.hemomancy.common.item.shared;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MnemonicFolioResourceTest {
	@Test
	void clearingRecipeTurnsCanonicalBlueprintIntoComponentFreeBlank() throws Exception {
		String recipe = Files.readString(Path.of("src/main/resources/data/hemomancy/recipe/clear_mnemonic_blueprint.json"));
		assertTrue(recipe.contains("\"item\": \"hemomancy:mnemonic_blueprint\""));
		assertTrue(recipe.contains("\"id\": \"hemomancy:mnemonic_blueprint\""));
	}

	@Test
	void folioHasModelAndCraftingRecipe() throws Exception {
		Path model = Path.of("src/main/resources/assets/hemomancy/models/item/mnemonic_folio.json");
		Path texture = Path.of("src/main/resources/assets/hemomancy/textures/item/mnemonic_folio.png");
		assertTrue(Files.readString(model).contains("hemomancy:item/mnemonic_folio"));
		assertTrue(Files.exists(texture));
		BufferedImage image = ImageIO.read(texture.toFile());
		assertEquals(16, image.getWidth());
		assertEquals(16, image.getHeight());
		assertTrue(Files.exists(Path.of("src/main/resources/data/hemomancy/recipe/mnemonic_folio.json")));
	}
}
