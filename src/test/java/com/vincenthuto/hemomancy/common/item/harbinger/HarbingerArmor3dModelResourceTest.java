package com.vincenthuto.hemomancy.common.item.harbinger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public final class HarbingerArmor3dModelResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private HarbingerArmor3dModelResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		assertModelWiring("silent archon item",
				"com/vincenthuto/hemomancy/common/item/harbinger/armor/SilentArchonArmorItem.java",
				"implements HemoClientItemExtensionsProvider",
				"SilentArchonArmorModel.helmet.get()",
				"SilentArchonArmorModel.chest.get()",
				"SilentArchonArmorModel.legs.get()",
				"SilentArchonArmorModel.boots.get()");
		assertModelWiring("chalybeate sabatons item",
				"com/vincenthuto/hemomancy/common/item/shared/armor/ChalybeateScleriteSabatonsItem.java",
				"implements HemoClientItemExtensionsProvider",
				"ChalybeateFortressArmorModel.boots.get()");
		assertModelWiring("covenant mantle item",
				"com/vincenthuto/hemomancy/common/item/shared/armor/CovenantMantleArmorItem.java",
				"implements HemoClientItemExtensionsProvider",
				"CovenantLeaderArmorModel.chest.get()");

		String layerEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		assertContains("silent archon layer registrations", layerEvents,
				"SilentArchonArmorModel.SILENT_ARCHON_HELMET_LAYER");
		assertContains("silent archon layer registrations", layerEvents,
				"SilentArchonArmorModel.SILENT_ARCHON_CHEST_LAYER");
		assertContains("silent archon layer registrations", layerEvents,
				"SilentArchonArmorModel.SILENT_ARCHON_LEGS_LAYER");
		assertContains("silent archon layer registrations", layerEvents,
				"SilentArchonArmorModel.SILENT_ARCHON_BOOTS_LAYER");
		assertContains("chalybeate layer registration", layerEvents,
				"ChalybeateFortressArmorModel.CHALYBEATE_FORTRESS_BOOTS_LAYER");
		assertContains("covenant layer registration", layerEvents,
				"CovenantLeaderArmorModel.COVENANT_LEADER_CHEST_LAYER");

		assertExists("chalybeate fortress armor model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/ChalybeateFortressArmorModel.java"));
		assertExists("silent archon armor model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/SilentArchonArmorModel.java"));
		assertExists("covenant leader armor model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/CovenantLeaderArmorModel.java"));

		assertImageSize("chalybeate fortress worn texture",
				"assets/hemomancy/textures/models/armor/chalybeate_sclerite_layer_1.png", 256, 128);
		assertImageSize("silent archon outer worn texture",
				"assets/hemomancy/textures/models/armor/silent_archon_layer_1.png", 256, 128);
		assertImageSize("silent archon inner worn texture",
				"assets/hemomancy/textures/models/armor/silent_archon_layer_2.png", 256, 128);
		assertImageSize("covenant mantle worn texture",
				"assets/hemomancy/textures/models/armor/covenant_mantle_layer_1.png", 256, 128);
	}

	private static void assertModelWiring(String label, String relativePath, String... expected) throws IOException {
		String source = read(SOURCE_ROOT.resolve(relativePath));
		for (String needle : expected) {
			assertContains(label, source, needle);
		}
	}

	private static void assertImageSize(String label, String relativePath, int width, int height) throws IOException {
		Path path = RESOURCE_ROOT.resolve(relativePath);
		assertExists(label, path);
		BufferedImage image = ImageIO.read(path.toFile());
		if (image == null) {
			throw new AssertionError(label + ": not a readable image " + path);
		}
		if (image.getWidth() != width || image.getHeight() != height) {
			throw new AssertionError(label + ": expected " + width + "x" + height + " but found "
					+ image.getWidth() + "x" + image.getHeight() + " at " + path);
		}
	}

	private static String read(Path path) throws IOException {
		assertExists("source", path);
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
