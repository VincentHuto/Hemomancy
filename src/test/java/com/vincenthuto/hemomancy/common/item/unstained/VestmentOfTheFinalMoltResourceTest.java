package com.vincenthuto.hemomancy.common.item.unstained;

import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.support.ResourceReader;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.readMainJava;

class VestmentOfTheFinalMoltResourceTest {
	private static final Path RESOURCE_ROOT = Path.of("src/main/resources");

	@Test
	void vestmentOfTheFinalMoltIsWired() throws IOException {
		String itemInit = readMainJava("common/init/ItemInit.java");
		String item = readMainJava("common/item/unstained/armor/VestmentOfTheFinalMoltArmorItem.java")
				+ readMainJava("client/item/ItemClientExtensions.java");
		String model = readMainJava("client/model/armor/VestmentOfTheFinalMoltArmorModel.java");
		String layerEvents = readMainJava("client/event/LayerEvents.java");
		String helper = readMainJava("client/render/armor/ModelBackedArmorItemRenderHelper.java");
		String renderer = readMainJava("client/render/item/ModelBackedArmorItemRenderer.java");
		String granter = readMainJava("common/event/UnstainedAdvancementGranter.java");
		String lang = ResourceReader.text("assets/hemomancy/lang/en_us.json");

		assertContains("item registration", itemInit, "vestment_of_the_final_molt");
		assertContains("item registration", itemInit, "new VestmentOfTheFinalMoltArmorItem(");
		assertContains("item should count as unstained", itemInit, "EnumModArmorTiers.UNSTAINED.holder()");
		assertContains("item registration", itemInit, "ArmorItem.Type.CHESTPLATE");

		assertContains("custom item class", item, "Map.entry(VestmentOfTheFinalMoltArmorItem.class");
		assertContains("custom item class", item, "VestmentOfTheFinalMoltArmorModel.chest.get()");
		assertContains("custom item class", item, "new ModelBackedArmorItemRenderer(");
		assertContains("custom item class", item, "vestment_of_the_final_molt_layer_1");
		assertContains("custom item class", item, "tooltip.hemomancy.vestment_of_the_final_molt");
		assertContains("custom item class", item, "stacksTo(1)");

		assertContains("armor model layer", model, "VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER");
		assertContains("armor model supplier", model, "chest = Lazy");
		assertContains("hood up part", model, "hoodUp");
		assertContains("hood down part", model, "hoodDown");
		assertContains("helmet hides hood", item,
				"model.setHoodRaised(entityLiving.getItemBySlot(EquipmentSlot.HEAD).isEmpty())");
		assertContains("helmet hides hood", model, "hoodUp.visible = hoodRaised");
		assertContains("helmet shows lowered hood", model, "hoodDown.visible = !hoodRaised");
		assertContains("flowing robe animation", model, "Mth.sin");
		assertContains("moth wing panel naming", model, "leftForewing");
		assertContains("moth wing panel naming", model, "rightForewing");

		assertContains("layer registration", layerEvents,
				"VestmentOfTheFinalMoltArmorModel.VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER");

		assertContains("item render helper texture", helper, "VESTMENT_OF_THE_FINAL_MOLT_LAYER_1");
		assertContains("item render helper route", helper, "ItemInit.vestment_of_the_final_molt.get()");
		assertContains("item render helper route", helper, "VestmentOfTheFinalMoltArmorModel.chest::get");
		assertContains("vestment compact gui transform", renderer, "applyManualGuiTransforms");
		assertContains("vestment compact gui transform", renderer, "stack.is(ItemInit.vestment_of_the_final_molt.get())");
		assertContains("vestment compact gui transform", renderer, "poseStack.scale(0.52F, 0.52F, 0.52F);");

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
				"assets/hemomancy/textures/models/armor/vestment_of_the_final_molt_layer_1.png", 128, 256);
	}

	private static void assertItemModelParent(String item, String parent) throws IOException {
		String model = ResourceReader.text("assets/hemomancy/models/item/" + item + ".json");
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

	private static void assertExists(String label, Path path) {
		if (!Files.exists(path)) {
			throw new AssertionError(label + ": missing " + path);
		}
	}
}
