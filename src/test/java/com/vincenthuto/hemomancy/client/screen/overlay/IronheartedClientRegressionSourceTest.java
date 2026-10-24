package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.common.manipulation.ferric.IronheartedRules;
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
		assertEquals(98, IronheartedRules.ironHeartY(100, 3, true, 28, 20.0F));
		assertEquals(100, IronheartedRules.ironHeartY(100, 2, true, 28, 20.0F));
		assertEquals(100, IronheartedRules.ironHeartY(100, 3, false, 28, 20.0F));
	}

	@Test
	void ironHeartUsesOneBriefPulsePerSecond() {
		assertTrue(IronheartedRules.ironHeartPulse(0));
		assertTrue(IronheartedRules.ironHeartPulse(3));
		assertFalse(IronheartedRules.ironHeartPulse(4));
		assertFalse(IronheartedRules.ironHeartPulse(19));
		assertTrue(IronheartedRules.ironHeartPulse(20));
	}

	@Test
	void formingIronHeartFadesInBeforeItFills() {
		var fading = IronheartedRules.ironHeartFormation(0.0F, 0.25F);
		assertEquals(0.5F, fading.emptyAlpha());
		assertEquals(0.0F, fading.fill());

		var filling = IronheartedRules.ironHeartFormation(0.0F, 0.75F);
		assertEquals(1.0F, filling.emptyAlpha());
		assertEquals(0.5F, filling.fill());

		var existing = IronheartedRules.ironHeartFormation(0.5F, 0.5F);
		assertEquals(1.0F, existing.emptyAlpha());
		assertEquals(0.5F, existing.fill());
	}

	@Test
	void removedIronHeartsPlayThreeCrackFrames() {
		assertEquals(2, IronheartedRules.removedIronHeartSlots(6.0F, 2.0F));
		assertEquals(0, IronheartedRules.removedIronHeartSlots(6.0F, 5.0F));
		assertEquals(0, IronheartedRules.removedIronHeartSlots(2.0F, 4.0F));

		assertEquals(0, IronheartedRules.ironHeartCrackFrame(100L, 100L));
		assertEquals(1, IronheartedRules.ironHeartCrackFrame(100L, 104L));
		assertEquals(2, IronheartedRules.ironHeartCrackFrame(100L, 108L));
		assertEquals(-1, IronheartedRules.ironHeartCrackFrame(100L, 112L));
	}

	@Test
	void chargedManipulationUsesTheSharedReleaseInputRules() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientTickEvents.java")
				+ read("src/main/java/com/vincenthuto/hemomancy/client/event/ManipulationChargeInput.java");

		assertEquals(1, occurrences(source, "handleCommonClientTickInput();"));
		assertTrue(source.contains("mc.player.hurtTime > 0 && mc.player.hurtTime == mc.player.hurtDuration"));
		assertTrue(source.contains("var memory = known.getSelectedMemoryRef();"));
		assertTrue(source.contains("ManipulationInit.getByName(memory.id())"));
		assertTrue(source.contains("ManipulationInputRules.tick(type, down, clicked"));
		assertFalse(source.contains("manipulationChargeTicks >= IronheartedRules.IRON_HEART_CHARGE_TICKS"));
		assertFalse(source.contains("manipulationChargeSent"));
	}

	@Test
	void ironHeartsCoverExistingHealthHeartsOnly() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/IronheartedOverlay.java");

		assertTrue(source.contains("int y = height - 39;"));
		assertTrue(source.contains("heart < IronheartedRules.ironHeartSlots(shownIron)"));
		assertTrue(source.contains("x + heart * 8"));
	}

	@Test
	void ironHeartArtworkIsTextureBacked() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/IronheartedOverlay.java");
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
			String directory = name.equals("empty") ? "textures/gui/sprites/manipulation/ironhearted/" : "textures/gui/manipulation/ironhearted/";
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
		String source = read("src/main/java/com/vincenthuto/hemomancy/client/screen/overlay/IronheartedOverlay.java");
		assertTrue(source.contains("IRON_HEART_CRACK_FRAMES"));
		assertTrue(source.contains("IronheartedRules.ironHeartCrackFrame"));

		var full = ImageIO.read(Path.of(
				"src/main/resources/assets/hemomancy/textures/gui/manipulation/ironhearted/iron_heart_full.png").toFile());
		for (int frame = 0; frame < 3; frame++) {
			var cracked = ImageIO.read(Path.of("src/main/resources/assets/hemomancy/textures/gui/manipulation/ironhearted/"
					+ "iron_heart_crack_" + frame + ".png").toFile());
			assertNotNull(cracked);
			assertEquals(9, cracked.getWidth());
			assertEquals(9, cracked.getHeight());
			assertFalse(java.util.Arrays.equals(full.getRGB(0, 0, 9, 9, null, 0, 9),
					cracked.getRGB(0, 0, 9, 9, null, 0, 9)));
		}
	}

	@Test
	void ironheartedUsesItsDedicatedFerricHeartArtwork() throws IOException {
		String radial = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java");
		String loadout = read("src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SynapticLoadoutScreen.java");
		String resolver = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/ManipulationIconResolver.java");

		assertTrue(resolver.contains("default -> \"memory_\" + manipulationId + \"_overlay\""));
		assertFalse(resolver.contains("case \"ironhearted\""));
		assertTrue(read("src/main/resources/assets/hemomancy/models/item/memory_ironhearted.json")
				.contains("hemomancy:item/memories/memory_ironhearted_overlay"));
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
