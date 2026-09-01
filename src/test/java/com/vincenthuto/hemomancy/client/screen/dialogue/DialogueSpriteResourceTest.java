package com.vincenthuto.hemomancy.client.screen.dialogue;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DialogueSpriteResourceTest {
	private static final Path ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/sprites/dialogue");

	@Test
	void themesProvideNineSliceFramesCardsAndButtons() throws IOException {
		for (String theme : new String[] { "blood", "blood_material", "unstained", "unstained_material",
				"fungal", "fungal_material", "still", "still_material" }) {
			assertSprite(theme + "/frame.png", 128, 128, true);
			assertSprite(theme + "/portrait_frame.png", 128, 128, true);
			for (String state : new String[] { "card", "card_selected", "card_disabled" })
				assertSprite(theme + "/" + state + ".png", 64, 64, true);
			for (String state : new String[] { "button", "button_selected", "button_disabled" })
				assertSprite(theme + "/" + state + ".png", 64, 32, true);
		}
	}

	@Test
	void categoryIconsAndRecurringNpcCrestsExist() throws IOException {
		for (String icon : new String[] { "quests", "inquiries", "lore", "conversation", "leave", "back",
				"reward", "locked", "complete", "turn_in", "unread", "active", "disabled" })
			assertSprite("icons/" + icon + ".png", 32, 32, false);
		for (String crest : new String[] { "default", "alchemist", "artificer", "cicatrix_anchorite", "hermit",
				"mnemonist", "vicar", "votary_wayfarer", "voyager", "acolyte", "guardian", "zealot" })
			assertSprite("crests/" + crest + ".png", 32, 32, false);
	}

	@Test
	void hubCategoriesHaveReferenceMatchedColoredCardBorders() throws IOException {
		for (String category : new String[] { "quests", "inquiries", "lore", "conversation" }) {
			assertSprite("categories/" + category + ".png", 64, 64, true);
			assertSprite("categories/" + category + "_selected.png", 64, 64, true);
			assertSolidCenter("categories/" + category + ".png", 8);
			assertSolidCenter("categories/" + category + "_selected.png", 8);
			assertSprite("categories_material/" + category + ".png", 64, 64, true);
			assertSprite("categories_material/" + category + "_selected.png", 64, 64, true);
			assertSolidCenter("categories_material/" + category + ".png", 8);
			assertSolidCenter("categories_material/" + category + "_selected.png", 8);
		}
	}

	@Test
	void nineSliceStretchCentersContainNoDecorativeLines() throws IOException {
		for (String theme : new String[] { "blood", "blood_material", "unstained", "unstained_material",
				"fungal", "fungal_material", "still", "still_material" }) {
			for (String state : new String[] { "card", "card_selected", "card_disabled" })
				assertSolidCenter(theme + "/" + state + ".png", 8);
			for (String state : new String[] { "button", "button_selected", "button_disabled" })
				assertSolidCenter(theme + "/" + state + ".png", 6);
		}
	}

	@Test
	void doubleResolutionNineSlicesKeepTheirOriginalLogicalGeometry() throws IOException {
		String frameMeta = Files.readString(ROOT.resolve("blood/frame.png.mcmeta"));
		String cardMeta = Files.readString(ROOT.resolve("blood/card.png.mcmeta"));
		assertTrue(frameMeta.contains("\"width\": 64"));
		assertTrue(frameMeta.contains("\"border\": 8"));
		assertTrue(cardMeta.contains("\"width\": 32"));
		assertTrue(cardMeta.contains("\"border\": 4"));
	}

	private static void assertSprite(String relative, int width, int height, boolean metadata) throws IOException {
		Path path = ROOT.resolve(relative);
		assertTrue(Files.isRegularFile(path), relative);
		BufferedImage image = ImageIO.read(path.toFile());
		assertEquals(width, image.getWidth(), relative);
		assertEquals(height, image.getHeight(), relative);
		if (metadata) assertTrue(Files.isRegularFile(path.resolveSibling(path.getFileName() + ".mcmeta")), relative);
	}

	private static void assertSolidCenter(String relative, int border) throws IOException {
		BufferedImage image = ImageIO.read(ROOT.resolve(relative).toFile());
		int expected = image.getRGB(border, border);
		for (int y = border; y < image.getHeight() - border; y++)
			for (int x = border; x < image.getWidth() - border; x++)
				assertEquals(expected, image.getRGB(x, y), relative + " stretch center at " + x + "," + y);
	}
}
