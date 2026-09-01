package com.vincenthuto.hemomancy.common.item.armor;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FinalBloodLustArmorModelResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private FinalBloodLustArmorModelResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String layers = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/ModelBackedArmorItemRenderHelper.java"));

		assertFinalSet("edacious", "EdaciousBloodLustArmorModel", "EDACIOUS_BLOOD_LUST", layers,
				helper, 256, 256);
		assertFinalSet("sheolic", "SheolicBloodLustArmorModel", "SHEOLIC_BLOOD_LUST", layers,
				helper, 128, 128);
		assertFinalSet("phantasmal", "PhantasmalBloodLustArmorModel", "PHANTASMAL_BLOOD_LUST",
				layers, helper, 128, 128);
	}

	private static void assertFinalSet(String prefix, String modelClass, String constantPrefix,
			String layers, String helper, int expectedWidth, int expectedHeight) throws IOException {
		String model = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/" + modelClass + ".java"));
		String itemClass = Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1) + "BloodLustArmorItem.java";
		String bloodLustItem = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/harbinger/armor/" + itemClass))
				+ read(SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/common/item/harbinger/armor/AbstractSpecialBloodLustArmorItem.java"))
				+ read(SOURCE_ROOT.resolve(
						"com/vincenthuto/hemomancy/client/item/SpecialBloodLustClientExtensions.java"));
		assertContains(prefix + " model should define helmet layer", model, constantPrefix + "_HELMET_LAYER");
		assertContains(prefix + " model should define chest layer", model, constantPrefix + "_CHEST_LAYER");
		assertContains(prefix + " model should define legs layer", model, constantPrefix + "_LEGS_LAYER");
		assertContains(prefix + " model should define feet layer", model, constantPrefix + "_FEET_LAYER");
		assertContains(prefix + " model should expose helmet supplier", model, "helmet = Lazy");
		assertContains(prefix + " model should expose chest supplier", model, "chest = Lazy");
		assertContains(prefix + " model should expose legs supplier", model, "legs = Lazy");
		assertContains(prefix + " model should expose boots supplier", model, "boots = Lazy");

		assertContains(prefix + " worn helmet should use copied model", bloodLustItem, modelClass + ".helmet.get()");
		assertContains(prefix + " worn chest should use copied model", bloodLustItem, modelClass + ".chest.get()");
		assertContains(prefix + " worn legs should use copied model", bloodLustItem, modelClass + ".legs.get()");
		assertContains(prefix + " worn boots should use copied model", bloodLustItem, modelClass + ".boots.get()");
		assertContains(prefix + " worn texture should use the final atlas key", bloodLustItem,
				"super(materialIn, slot, \"" + prefix + "_blood_lust\")");

		assertContains(prefix + " layer should register helmet", layers, modelClass + "." + constantPrefix + "_HELMET_LAYER");
		assertContains(prefix + " layer should register chest", layers, modelClass + "." + constantPrefix + "_CHEST_LAYER");
		assertContains(prefix + " layer should register legs", layers, modelClass + "." + constantPrefix + "_LEGS_LAYER");
		assertContains(prefix + " layer should register feet", layers, modelClass + "." + constantPrefix + "_FEET_LAYER");

		assertContains(prefix + " helper should define layer 1 texture", helper, constantPrefix + "_LAYER_1");
		assertContains(prefix + " helper should define layer 2 texture", helper, constantPrefix + "_LAYER_2");
		assertContains(prefix + " helper should route stacks to copied model", helper, modelClass + ".helmet::get");
		assertContains(prefix + " helper should route stack textures to final atlas", helper,
				prefix.toUpperCase() + "_BLOOD_LUST_LAYER_1");

		for (String piece : new String[] {"helm", "chest", "legs", "boots"}) {
			String itemModel = read(RESOURCE_ROOT.resolve(
					"assets/hemomancy/models/item/" + prefix + "_blood_lust_" + piece + ".json"));
			assertContains(prefix + " " + piece + " should use the custom stack renderer", itemModel,
					"\"parent\": \"builtin/entity\"");
		}

		assertTextureSize(prefix + " layer 1 should be a copied readable armor atlas",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/models/armor/" + prefix + "_blood_lust_layer_1.png"),
				expectedWidth, expectedHeight);
		assertTextureSize(prefix + " layer 2 should be a copied readable armor atlas",
				RESOURCE_ROOT.resolve("assets/hemomancy/textures/models/armor/" + prefix + "_blood_lust_layer_2.png"),
				expectedWidth, expectedHeight);
	}

	private static String read(Path path) throws IOException {
		if (!Files.exists(path)) {
			throw new AssertionError("missing " + path);
		}
		return Files.readString(path).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}

	private static void assertTextureSize(String label, Path path, int expectedWidth, int expectedHeight) throws IOException {
		var image = ImageIO.read(path.toFile());
		if (image == null) {
			throw new AssertionError(label + ": not a readable image at " + path);
		}
		if (image.getWidth() != expectedWidth || image.getHeight() != expectedHeight) {
			throw new AssertionError(label + ": expected " + expectedWidth + "x" + expectedHeight + " but got "
					+ image.getWidth() + "x" + image.getHeight());
		}
	}
}
