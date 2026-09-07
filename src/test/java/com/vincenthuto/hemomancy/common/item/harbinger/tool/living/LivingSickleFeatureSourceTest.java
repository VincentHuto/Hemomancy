package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingSickleFeatureSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void sickleIsAContextualPrunerAndVesperUnlockedInnerRingForm() throws IOException {
		String items = read("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");
		String manipulations = read("src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java");
		String forms = read("src/main/java/com/vincenthuto/hemomancy/common/item/component/LivingWeaponForm.java");
		String radial = read("src/main/java/com/vincenthuto/hemomancy/client/screen/manips/RadialChooseManipScreen.java");
		String equip = read("src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationEquipHelper.java");
		String vesperRite = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRite.java");
		String bloom = read("src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/QliphothBloomBlock.java");

		assertTrue(items.contains("living_sickle"));
		assertTrue(manipulations.contains("conjure_sickle"));
		assertFalse(forms.contains("SICKLE("), "the seven graft forms must remain unchanged");
		assertTrue(radial.contains("ManipulationEquipHelper.CONJURE_SICKLE"));
		assertTrue(equip.contains("CONJURE_SICKLE"));
		assertTrue(vesperRite.contains("ensureVesperSickleKnown"));
		assertTrue(bloom.contains("LivingSicklePruning"));
	}

	@Test
	void oldPruningRiteIsRetiredAndSickleModelExists() throws IOException {
		assertFalse(Files.exists(ROOT.resolve(
				"src/main/resources/data/hemomancy/recipe/cardinal_rite/pruning_of_qliphoth.json")));
		Path model = ROOT.resolve("src/main/resources/assets/hemomancy/models/item/living_sickle.json");
		assertTrue(Files.exists(model));
		String json = Files.readString(model);
		assertTrue(json.contains("\"parent\": \"builtin/entity\""));
	}

	@Test
	void sickleUsesTheCurvedBlockbenchModelAndSharedTexture() throws IOException {
		String javaModel = read("src/main/java/com/vincenthuto/hemomancy/client/model/item/LivingSickleModel.java");
		String itemRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/harbinger/LivingSickleItemRenderer.java");
		String vesperLayer = read("src/main/java/com/vincenthuto/hemomancy/client/render/layer/mob/endgame/VesperLivingWeaponLayer.java");
		Path bbmodel = ROOT.resolve("src/main/resources/assets/hemomancy/models/item/bbmodel/LivingSickleModel.bbmodel");
		Path texture = ROOT.resolve("src/main/resources/assets/hemomancy/textures/entity/model_living_sickle.png");

		assertTrue(javaModel.contains("blade_root"));
		assertTrue(javaModel.contains("blade_tip"));
		String blockbenchModel = Files.readString(bbmodel);
		assertTrue(blockbenchModel.contains("model_living_sickle.png"));
		assertTrue(blockbenchModel.contains("data:image/png;base64,"), "the Blockbench project must carry its texture");
		assertTrue(Files.size(texture) > 0L, "the living sickle texture must be present");
		assertTrue(javaModel.contains("entityCutoutNoCull"), "opaque pixels must render crisply on both wielders");
		assertTrue(itemRenderer.contains("model_living_sickle.png"));
		assertTrue(vesperLayer.contains("renderStatic(weapon"), "Vesper must keep using the shared item renderer");
	}

	@Test
	void ordinarySickleHasTwoActiveModesAndARealHookProjectile() throws IOException {
		String item = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/LivingSickleItem.java");
		String hook = read("src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/LivingSickleHookEntity.java");
		String renderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/entity/projectile/LivingSickleHookRenderer.java");
		String entities = read("src/main/java/com/vincenthuto/hemomancy/common/init/EntityInit.java");
		String client = read("src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");

		assertTrue(item.contains("player.isShiftKeyDown()"));
		assertTrue(item.contains("LivingSickleMode.BLOOD_HOOK"));
		assertTrue(item.contains("performSpin"));
		assertTrue(item.contains("LivingSickleHookEntity"));
		assertTrue(item.contains("DataComponents.ATTRIBUTE_MODIFIERS"));
		assertTrue(hook.contains("TendrilEffectSpawner"));
		assertTrue(hook.contains("LivingSickleCombatRules.pullStrength"));
		assertTrue(hook.contains("protected boolean canHitEntity"));
		assertTrue(hook.contains("discard()"));
		assertTrue(renderer.contains("LivingSickleItemRenderer.renderModel"));
		assertTrue(entities.contains("living_sickle_hook"));
		assertTrue(client.contains("LivingSickleHookRenderer::new"));
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}
}
