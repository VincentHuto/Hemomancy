package com.vincenthuto.hemomancy.common.item.unstained;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class VestmentOfTheFinalMoltResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java");
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	private VestmentOfTheFinalMoltResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/common/init/ItemInit.java"));
		String item = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/item/unstained/armor/VestmentOfTheFinalMoltArmorItem.java"));
		String model = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/VestmentOfTheFinalMoltArmorModel.java"));
		String layerEvents = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		String helper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/ModelBackedArmorItemRenderHelper.java"));
		String renderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/ModelBackedArmorItemRenderer.java"));
		String granter = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/common/event/UnstainedAdvancementGranter.java"));
		String lang = read(RESOURCE_ROOT.resolve("assets/hemomancy/lang/en_us.json"));

		assertContains("item registration", itemInit, "vestment_of_the_final_molt");
		assertContains("item registration", itemInit, "new VestmentOfTheFinalMoltArmorItem(");
		assertContains("item should count as unstained", itemInit, "EnumModArmorTiers.UNSTAINED.holder()");
		assertContains("item registration", itemInit, "ArmorItem.Type.CHESTPLATE");

		assertContains("custom item class", item, "implements HemoClientItemExtensionsProvider");
		assertContains("custom item class", item, "VestmentOfTheFinalMoltArmorModel.chest.get()");
		assertContains("custom item class", item, "new ModelBackedArmorItemRenderer(");
		assertContains("custom item class", item, "vestment_of_the_final_molt_layer_1");
		assertContains("custom item class", item, "tooltip.hemomancy.vestment_of_the_final_molt");
		assertContains("custom item class", item, "stacksTo(1)");

		assertContains("armor model layer", model, "VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER");
		assertContains("armor model supplier", model, "chest = Lazy");
		assertContains("hood up part", model, "hoodUp");
		assertContains("hood down part", model, "hoodDown");
		assertContains("helmet hides hood", model, "entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty()");
		assertContains("helmet hides hood", model, "hoodUp.visible = hoodRaised");
		assertContains("helmet shows lowered hood", model, "hoodDown.visible = !hoodRaised");
		assertContains("flowing robe animation", model, "Mth.sin");
		assertContains("moth wing panel naming", model, "leftWingPanel");
		assertContains("moth wing panel naming", model, "rightWingPanel");

		assertContains("layer registration", layerEvents,
				"VestmentOfTheFinalMoltArmorModel.VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER");

		assertContains("item render helper texture", helper, "VESTMENT_OF_THE_FINAL_MOLT_LAYER_1");
		assertContains("item render helper route", helper, "ItemInit.vestment_of_the_final_molt.get()");
		assertContains("item render helper route", helper, "VestmentOfTheFinalMoltArmorModel.chest::get");
		assertContains("vestment compact gui transform", renderer, "applyManualGuiTransforms");
		assertContains("vestment compact gui transform", renderer, "stack.is(ItemInit.vestment_of_the_final_molt.get())");
		assertContains("vestment compact gui transform", renderer, "poseStack.scale(0.6F, 0.6F, 0.6F);");

		assertContains("advancement reward", granter, "awardVestmentOfTheFinalMolt");
		assertContains("advancement reward", granter, "ADV_ENLIGHTENED_SEEKER.equals(id)");
		assertContains("advancement reward", granter, "ItemInit.vestment_of_the_final_molt.get()");
		assertContains("advancement reward", granter, "player.drop(stack, false)");

		assertContains("item name", lang,
				"\"item.hemomancy.vestment_of_the_final_molt\": \"Vestment of the Final Molt\"");
		assertContains("item tooltip", lang, "\"tooltip.hemomancy.vestment_of_the_final_molt\"");

		assertItemModelParent("vestment_of_the_final_molt", "builtin/entity");
		assertImageSize("vestment item texture",
				"assets/hemomancy/textures/item/vestment_of_the_final_molt.png", 16, 16);
		assertImageSize("vestment worn texture",
				"assets/hemomancy/textures/models/armor/vestment_of_the_final_molt_layer_1.png", 256, 128);
	}

	private static void assertItemModelParent(String item, String parent) throws IOException {
		String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + item + ".json"));
		assertContains("model parent for " + item, model, "\"parent\": \"" + parent + "\"");
	}

	private static void assertImageSize(String label, String relativePath, int width, int height) throws IOException {
		Path path = RESOURCE_ROOT.resolve(relativePath);
		assertExists(label, path);
		var image = ImageIO.read(path.toFile());
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
