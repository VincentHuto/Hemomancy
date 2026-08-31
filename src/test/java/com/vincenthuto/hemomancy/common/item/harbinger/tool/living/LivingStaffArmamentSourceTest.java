package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class LivingStaffArmamentSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private LivingStaffArmamentSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String itemInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String manipInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		String treeInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationTreeInit.java");
		String rules = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffWeaponFormRules.java");
		String helper = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingStaffWeaponFormHelper.java");
		String guard = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingArsenalInventoryGuard.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

		assertContains("torch item registration", itemInit, "living_torch = SPECIALITEMS.register(\"living_torch\"");
		assertContains("flail item registration", itemInit, "living_flail = SPECIALITEMS.register(\"living_flail\"");
		assertContains("torch memory registration", itemInit, "memory_living_torch = BASEITEMS.register(\"memory_living_torch\"");
		assertContains("flail memory registration", itemInit, "memory_living_flail = BASEITEMS.register(\"memory_living_flail\"");
		assertContains("torch uses item class", itemInit, "new LivingTorchItem");
		assertContains("flail uses item class", itemInit, "new LivingFlailItem");
		assertContains("living staff defaults to iron sword attack attributes", compact(itemInit),
				"newLivingStaffItem(newItem.Properties().stacksTo(1)"
						+ ".attributes(SwordItem.createAttributes(Tiers.IRON,3.0F,-2.4F)))");

		assertContains("torch manipulation registration", manipInit, "MANIPS.register(\"conjure_torch\"");
		assertContains("flail manipulation registration", manipInit, "MANIPS.register(\"conjure_flail\"");
		assertContains("torch is flammeus", manipInit,
				"new StaffWeaponFormManip(\"conjure_torch\", ItemInit.living_torch, 0, 0,\n"
						+ "\t\t\t\t\tEnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.FLAMMEUS");
		assertContains("flail is congeatio", manipInit,
				"new StaffWeaponFormManip(\"conjure_flail\", ItemInit.living_flail, 0, 0,\n"
						+ "\t\t\t\t\tEnumManipulationRank.MEDIOCRITAS, EnumBloodTendency.CONGEATIO");

		assertContains("torch rule constant", rules, "CONJURE_TORCH = \"conjure_torch\"");
		assertContains("flail rule constant", rules, "CONJURE_FLAIL = \"conjure_flail\"");
		assertContains("torch recognized as staff form", rules, "CONJURE_TORCH");
		assertContains("flail recognized as staff form", rules, "CONJURE_FLAIL");
		assertContains("helper maps torch form", helper, "case LivingStaffWeaponFormRules.CONJURE_TORCH -> ItemInit.living_torch.get();");
		assertContains("helper maps flail form", helper, "case LivingStaffWeaponFormRules.CONJURE_FLAIL -> ItemInit.living_flail.get();");
		assertContains("guard knows living torch", guard, "ItemInit.living_torch");
		assertContains("guard knows living flail", guard, "ItemInit.living_flail");

		assertContains("torch tree entry", treeInit, "register(\"conjure_torch\"");
		assertContains("torch branches from flame conjuration", treeInit, "\"crimson_flame_conjuration\")\n"
				+ "\t\t\t\t.setSoftParents(\"conjure_staff\")");
		assertContains("flail tree entry", treeInit, "register(\"conjure_flail\"");
		assertContains("flail branches from glacial rampart", treeInit, "\"glacial_rampart\")\n"
				+ "\t\t\t\t.setSoftParents(\"conjure_staff\")");

		assertContains("torch lang", lang, "\"item.hemomancy.living_torch\": \"Living Torch\"");
		assertContains("flail lang", lang, "\"item.hemomancy.living_flail\": \"Living Flail\"");
		assertContains("torch memory lang", lang, "\"item.hemomancy.memory_living_torch\": \"Memory Living Torch\"");
		assertContains("flail memory lang", lang, "\"item.hemomancy.memory_living_flail\": \"Memory Living Flail\"");

		assertResource("torch item model", "src/main/resources/assets/hemomancy/models/item/living_torch.json");
		assertResource("flail item model", "src/main/resources/assets/hemomancy/models/item/living_flail.json");
		assertResource("torch memory model", "src/main/resources/assets/hemomancy/models/item/memory_living_torch.json");
		assertResource("flail memory model", "src/main/resources/assets/hemomancy/models/item/memory_living_flail.json");
		assertResource("torch renderer texture", "src/main/resources/assets/hemomancy/textures/entity/model_living_torch.png");
		assertResource("flail renderer texture", "src/main/resources/assets/hemomancy/textures/entity/model_living_flail.png");
		assertResource("torch memory overlay", "src/main/resources/assets/hemomancy/textures/item/memories/memory_living_torch_overlay.png");
		assertResource("flail memory overlay", "src/main/resources/assets/hemomancy/textures/item/memories/memory_living_flail_overlay.png");

		String torchModel = read("src/main/resources/assets/hemomancy/models/item/living_torch.json");
		String flailModel = read("src/main/resources/assets/hemomancy/models/item/living_flail.json");
		assertContains("torch item model uses custom renderer", torchModel, "\"parent\": \"builtin/entity\"");
		assertContains("flail item model uses custom renderer", flailModel, "\"parent\": \"builtin/entity\"");

		String flailItem = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingFlailItem.java");
		String flailRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/LivingFlailItemRenderer.java");
		String flailHelper = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/LivingFlailRenderHelper.java");
		String flailModelClass = read("src/main/java/com/vincenthuto/hemomancy/client/model/item/LivingFlailModel.java");
		String layerEvents = read("src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java");
		assertContains("flail item routes custom renderer", flailItem, "RenderPropLivingFlail");
		assertContains("flail renderer uses physics helper", flailRenderer, "LivingFlailRenderHelper.renderHeld");
		assertContains("flail helper has damped physics", flailHelper, "private static final class PhysicsState");
		assertContains("flail model has chain link", flailModelClass, "renderChainLink");
		assertContains("flail model layer registered", layerEvents, "LivingFlailModel.LAYER_LOCATION");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static String compact(String text) {
		return text.replaceAll("\\s+", "");
	}

	private static void assertResource(String label, String path) {
		if (!Files.exists(ROOT.resolve(path))) {
			throw new AssertionError(label + " missing at " + path);
		}
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}
}
