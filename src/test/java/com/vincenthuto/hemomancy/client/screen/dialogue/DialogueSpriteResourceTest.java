package com.vincenthuto.hemomancy.client.screen.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class DialogueSpriteResourceTest {
	private static final Path ROOT = Path.of("src/main/resources/assets/hemomancy/textures/gui/sprites/dialogue");

	@Test
	void themesProvideNineSliceFramesCardsAndButtons() throws IOException {
		for (String theme : new String[] { "blood", "unstained", "fungal" }) {
			assertSprite(theme + "/frame.png", 64, 64, true);
			assertSprite(theme + "/portrait_frame.png", 64, 64, true);
			for (String state : new String[] { "card", "card_selected", "card_disabled" })
				assertSprite(theme + "/" + state + ".png", 32, 32, true);
			for (String state : new String[] { "button", "button_selected", "button_disabled" })
				assertSprite(theme + "/" + state + ".png", 32, 16, true);
		}
	}

	@Test
	void categoryIconsAndRecurringNpcCrestsExist() throws IOException {
		for (String icon : new String[] { "quests", "inquiries", "lore", "conversation", "leave", "back",
				"reward", "locked", "complete", "turn_in", "unread", "active", "disabled" })
			assertSprite("icons/" + icon + ".png", 16, 16, false);
		for (String crest : new String[] { "default", "alchemist", "artificer", "cicatrix_anchorite", "hermit",
				"mnemonist", "vicar", "votary_wayfarer", "voyager", "acolyte", "guardian", "zealot" })
			assertSprite("crests/" + crest + ".png", 16, 16, false);
	}

	@Test
	void nineSliceStretchCentersContainNoDecorativeLines() throws IOException {
		for (String theme : new String[] { "blood", "unstained", "fungal" }) {
			for (String state : new String[] { "card", "card_selected", "card_disabled" })
				assertSolidCenter(theme + "/" + state + ".png", 4);
			for (String state : new String[] { "button", "button_selected", "button_disabled" })
				assertSolidCenter(theme + "/" + state + ".png", 3);
		}
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
