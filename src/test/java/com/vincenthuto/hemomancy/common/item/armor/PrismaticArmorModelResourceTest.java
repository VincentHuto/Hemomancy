package com.vincenthuto.hemomancy.common.item.armor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PrismaticArmorModelResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private PrismaticArmorModelResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String model = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/PrismaticArmorModel.java"));
		String item = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/shared/armor/PrismaticArmorItem.java"));
		String layers = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/ModelBackedArmorItemRenderHelper.java"));

		assertContains("model should define helmet layer", model, "PRISMATIC_HELMET_LAYER");
		assertContains("model should define chest layer", model, "PRISMATIC_CHEST_LAYER");
		assertContains("model should define legs layer", model, "PRISMATIC_LEGS_LAYER");
		assertContains("model should define feet layer", model, "PRISMATIC_FEET_LAYER");
		assertContains("model should expose helmet supplier", model, "helmet = Lazy");
		assertContains("model should expose chest supplier", model, "chest = Lazy");
		assertContains("model should expose legs supplier", model, "legs = Lazy");
		assertContains("model should expose boots supplier", model, "boots = Lazy");

		assertContains("item should provide client extensions", item, "implements HemoClientItemExtensionsProvider");
		assertContains("item should provide model-backed stack renderer", item, "new ModelBackedArmorItemRenderer");
		assertContains("item should route helmet worn model", item, "PrismaticArmorModel.helmet.get()");
		assertContains("item should route chest worn model", item, "PrismaticArmorModel.chest.get()");
		assertContains("item should route legs worn model", item, "PrismaticArmorModel.legs.get()");
		assertContains("item should route boots worn model", item, "PrismaticArmorModel.boots.get()");

		assertContains("layers should register helmet", layers, "PrismaticArmorModel.PRISMATIC_HELMET_LAYER");
		assertContains("layers should register chest", layers, "PrismaticArmorModel.PRISMATIC_CHEST_LAYER");
		assertContains("layers should register legs", layers, "PrismaticArmorModel.PRISMATIC_LEGS_LAYER");
		assertContains("layers should register feet", layers, "PrismaticArmorModel.PRISMATIC_FEET_LAYER");

		assertContains("item renderer should know prismatic texture", helper, "PRISMATIC_LAYER_1");
		assertContains("item renderer should route helmet stack", helper, "ItemInit.prismatic_helm.get()");
		assertContains("item renderer should route chest stack", helper, "ItemInit.prismatic_chestplate.get()");
		assertContains("item renderer should route legs stack", helper, "ItemInit.prismatic_leggings.get()");
		assertContains("item renderer should route boots stack", helper, "ItemInit.prismatic_boots.get()");

		for (String piece : new String[] {"helm", "chestplate", "leggings", "boots"}) {
			String itemModel = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/prismatic_" + piece + ".json"));
			assertContains("prismatic " + piece + " should use the custom stack renderer", itemModel,
					"\"parent\": \"builtin/entity\"");
		}
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
}
