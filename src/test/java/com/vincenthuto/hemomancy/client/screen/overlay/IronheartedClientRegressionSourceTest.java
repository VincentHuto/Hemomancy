package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.manipulation.BodyIdiomRules;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IronheartedClientRegressionSourceTest {
	@Test
	void regeneratingIronHeartFollowsVanillaHeartBounce() {
		assertEquals(98, BodyIdiomRules.ironHeartY(100, 3, true, 28, 20.0F));
		assertEquals(100, BodyIdiomRules.ironHeartY(100, 2, true, 28, 20.0F));
		assertEquals(100, BodyIdiomRules.ironHeartY(100, 3, false, 28, 20.0F));
	}

	@Test
	void ironHeartUsesOneBriefPulsePerSecond() {
		assertTrue(BodyIdiomRules.ironHeartPulse(0));
		assertTrue(BodyIdiomRules.ironHeartPulse(3));
		assertFalse(BodyIdiomRules.ironHeartPulse(4));
		assertFalse(BodyIdiomRules.ironHeartPulse(19));
		assertTrue(BodyIdiomRules.ironHeartPulse(20));
	}

	@Test
	void formingIronHeartFadesInBeforeItFills() {
		var fading = BodyIdiomRules.ironHeartFormation(0.0F, 0.25F);
		assertEquals(0.5F, fading.emptyAlpha());
		assertEquals(0.0F, fading.fill());

		var filling = BodyIdiomRules.ironHeartFormation(0.0F, 0.75F);
		assertEquals(1.0F, filling.emptyAlpha());
		assertEquals(0.5F, filling.fill());

		var existing = BodyIdiomRules.ironHeartFormation(0.5F, 0.5F);
		assertEquals(1.0F, existing.emptyAlpha());
		assertEquals(0.5F, existing.fill());
	}

	@Test
	void removedIronHeartsPlayThreeCrackFrames() {
		assertEquals(2, BodyIdiomRules.removedIronHeartSlots(6.0F, 2.0F));
		assertEquals(0, BodyIdiomRules.removedIronHeartSlots(6.0F, 5.0F));
		assertEquals(0, BodyIdiomRules.removedIronHeartSlots(2.0F, 4.0F));

		assertEquals(0, BodyIdiomRules.ironHeartCrackFrame(100L, 100L));
		assertEquals(1, BodyIdiomRules.ironHeartCrackFrame(100L, 104L));
		assertEquals(2, BodyIdiomRules.ironHeartCrackFrame(100L, 108L));
		assertEquals(-1, BodyIdiomRules.ironHeartCrackFrame(100L, 112L));
	}

	@Test
	void chargedManipulationUsesTheSharedReleaseInputRules() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");

		assertEquals(1, occurrences(source, "handleCommonClientTickInput();"));
		assertTrue(source.contains("mc.player.hurtTime > 0 && mc.player.hurtTime == mc.player.hurtDuration"));
		assertTrue(source.contains("ManipulationInit.getByName(known.getSelectedManip().getName())"));
		assertTrue(source.contains("ManipulationInputRules.tick(selected.getType(), down, clicked"));
		assertFalse(source.contains("manipulationChargeTicks >= BodyIdiomRules.IRON_HEART_CHARGE_TICKS"));
		assertFalse(source.contains("manipulationChargeSent"));
	}

	@Test
	void ironHeartsCoverExistingHealthHeartsOnly() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/BodyIdiomOverlay.java");

		assertTrue(source.contains("int y = height - 39;"));
		assertTrue(source.contains("heart < BodyIdiomRules.ironHeartSlots(shownIron)"));
		assertTrue(source.contains("x + heart * 8"));
	}

	@Test
	void ironHeartArtworkIsTextureBacked() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/BodyIdiomOverlay.java");
		List<String> vanillaHeartMask = List.of(
				"..##.##..",
				".#######.",
				"#########",
				"#########",
				"#########",
				".#######.",
				"..#####..",
				"...###...",
				"....#....");

		assertFalse(source.contains("String[] HEART"));
		assertFalse(source.contains("RenderSystem.setShaderColor"));
		assertTrue(source.contains("minecraft.getGuiSprites().getSprite(IRON_HEART_EMPTY)"));
		assertTrue(source.contains("graphics.blit(x, y, 0, 9, 9, emptyHeart, 1.0F, 1.0F, 1.0F, emptyAlpha)"));
		assertTrue(source.contains("graphics.blit(pulse ? IRON_HEART_PULSE : IRON_HEART_FULL"));
		for (String name : new String[] {"empty", "full", "pulse"}) {
			String directory = name.equals("empty") ? "textures/gui/sprites/body_idiom/" : "textures/gui/body_idiom/";
			var image = ImageIO.read(Path.of("src/main/resources/assets/hemomancy/" + directory
					+ "iron_heart_" + name + ".png").toFile());
			assertNotNull(image);
			assertEquals(9, image.getWidth());
			assertEquals(9, image.getHeight());
			assertEquals(vanillaHeartMask, alphaMask(image));
		}
	}

	@Test
	void removedIronHeartUsesEditableCrackFrames() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/BodyIdiomOverlay.java");
		assertTrue(source.contains("IRON_HEART_CRACK_FRAMES"));
		assertTrue(source.contains("BodyIdiomRules.ironHeartCrackFrame"));

		var full = ImageIO.read(Path.of(
				"src/main/resources/assets/hemomancy/textures/gui/body_idiom/iron_heart_full.png").toFile());
		for (int frame = 0; frame < 3; frame++) {
			var cracked = ImageIO.read(Path.of("src/main/resources/assets/hemomancy/textures/gui/body_idiom/"
					+ "iron_heart_crack_" + frame + ".png").toFile());
			assertNotNull(cracked);
			assertEquals(9, cracked.getWidth());
			assertEquals(9, cracked.getHeight());
			assertFalse(java.util.Arrays.equals(full.getRGB(0, 0, 9, 9, null, 0, 9),
					cracked.getRGB(0, 0, 9, 9, null, 0, 9)));
		}
	}

	@Test
	void ironheartedUsesItsExistingFerricMemoryArtwork() throws IOException {
		String radial = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java");
		String loadout = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SynapticLoadoutScreen.java");
		String resolver = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/ManipulationIconResolver.java");

		assertTrue(resolver.contains("case \"ironhearted\" -> \"memory_iron_retort_overlay\";"));
		assertTrue(radial.contains("ManipulationIconResolver.overlay(manipulation.getName())"));
		assertTrue(loadout.contains("ManipulationIconResolver.overlay(ref.id())"));
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path));
	}

	private static int occurrences(String source, String needle) {
		return (source.length() - source.replace(needle, "").length()) / needle.length();
	}

	private static List<String> alphaMask(java.awt.image.BufferedImage image) {
		List<String> rows = new ArrayList<>();
		for (int y = 0; y < image.getHeight(); y++) {
			StringBuilder row = new StringBuilder();
			for (int x = 0; x < image.getWidth(); x++) {
				row.append((image.getRGB(x, y) >>> 24) == 0 ? '.' : '#');
			}
			rows.add(row.toString());
		}
		return rows;
	}
}
