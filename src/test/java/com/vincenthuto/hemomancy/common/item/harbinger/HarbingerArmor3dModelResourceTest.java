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
				"SilentArchonArmorModel.boots.get()",
				"new SilentArchonArmorItemRenderer(");
		assertModelWiring("venous strider sabatons item",
				"com/vincenthuto/hemomancy/common/item/shared/armor/VenousStriderSabatonsItem.java",
				"implements HemoClientItemExtensionsProvider",
				"ChalybeateFortressArmorModel.boots.get()",
				"new ModelBackedArmorItemRenderer(");
		assertModelWiring("covenant mantle item",
				"com/vincenthuto/hemomancy/common/item/shared/armor/CovenantMantleArmorItem.java",
				"implements HemoClientItemExtensionsProvider",
				"CovenantLeaderArmorModel.chest.get()",
				"new ModelBackedArmorItemRenderer(");
		assertModelWiring("blood lust armor item",
				"com/vincenthuto/hemomancy/common/item/harbinger/armor/BloodLustArmorItem.java",
				"new ModelBackedArmorItemRenderer(");
		assertModelWiring("barbed armor item",
				"com/vincenthuto/hemomancy/common/item/shared/armor/BarbedArmorItem.java",
				"new ModelBackedArmorItemRenderer(");
		assertModelWiring("chitinite armor item",
				"com/vincenthuto/hemomancy/common/item/shared/armor/ChitiniteArmorItem.java",
				"new ModelBackedArmorItemRenderer(");
		assertModelWiring("unstained armor item",
				"com/vincenthuto/hemomancy/common/item/unstained/armor/UnstainedArmorItem.java",
				"new ModelBackedArmorItemRenderer(");

		String layerEvents = read(SOURCE_ROOT.resolve("com/vincenthuto/hemomancy/client/event/LayerEvents.java"));
		assertContains("silent archon layer registrations", layerEvents,
				"SilentArchonArmorModel.SILENT_ARCHON_HELMET_LAYER");
		assertContains("silent archon layer registrations", layerEvents,
				"SilentArchonArmorModel.SILENT_ARCHON_CHEST_LAYER");
		assertContains("silent archon layer registrations", layerEvents,
				"SilentArchonArmorModel.SILENT_ARCHON_LEGS_LAYER");
		assertContains("silent archon layer registrations", layerEvents,
				"SilentArchonArmorModel.SILENT_ARCHON_BOOTS_LAYER");
		assertContains("silent archon shader overlay player layer registration", layerEvents,
				"new SilentArchonArmorOverlayLayer");
		assertMethodContains("silent archon shader overlay entity layer registration", layerEvents,
				"addLayerToEntity", "new SilentArchonArmorOverlayLayer");
		assertContains("chalybeate layer registration", layerEvents,
				"ChalybeateFortressArmorModel.CHALYBEATE_FORTRESS_BOOTS_LAYER");
		assertContains("covenant layer registration", layerEvents,
				"CovenantLeaderArmorModel.COVENANT_LEADER_CHEST_LAYER");

		assertExists("chalybeate fortress armor model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/ChalybeateFortressArmorModel.java"));
		assertExists("silent archon armor model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/SilentArchonArmorModel.java"));
		assertExists("silent archon armor item renderer", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/SilentArchonArmorItemRenderer.java"));
		assertExists("silent archon armor overlay layer", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/layer/player/SilentArchonArmorOverlayLayer.java"));
		assertExists("silent archon render helper", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/SilentArchonArmorRenderHelper.java"));
		assertExists("model-backed armor render helper", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/ModelBackedArmorItemRenderHelper.java"));
		assertExists("armor item display transform helper", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/ArmorItemDisplayTransformHelper.java"));
		assertExists("armor item model pose helper", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/ArmorItemModelPoseHelper.java"));
		assertExists("model-backed armor item renderer", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/ModelBackedArmorItemRenderer.java"));
		assertExists("covenant leader armor model", SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/model/armor/CovenantLeaderArmorModel.java"));

		String modelBackedRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/ModelBackedArmorItemRenderer.java"));
		assertContains("model-backed renderer shared display transform", modelBackedRenderer,
				"ArmorItemDisplayTransformHelper.apply(displayContext, definition.slot(), poseStack)");
		assertContains("model-backed renderer neutral item pose", modelBackedRenderer,
				"ArmorItemModelPoseHelper.reset(model);");
		assertContains("model-backed renderer slot visibility", modelBackedRenderer,
				"applySlotVisibility(model, definition.slot())");
		assertContains("model-backed renderer head visibility", modelBackedRenderer, "case HEAD -> {");
		assertContains("model-backed renderer head visibility", modelBackedRenderer, "model.head.visible = true;");
		assertContains("model-backed renderer head visibility", modelBackedRenderer, "model.hat.visible = true;");
		assertContains("model-backed renderer chest visibility", modelBackedRenderer, "case CHEST -> {");
		assertContains("model-backed renderer chest visibility", modelBackedRenderer, "model.body.visible = true;");
		assertContains("model-backed renderer chest visibility", modelBackedRenderer, "model.rightArm.visible = true;");
		assertContains("model-backed renderer chest visibility", modelBackedRenderer, "model.leftArm.visible = true;");
		assertContains("model-backed renderer legs visibility", modelBackedRenderer, "case LEGS -> {");
		assertContains("model-backed renderer legs visibility", modelBackedRenderer, "model.rightLeg.visible = true;");
		assertContains("model-backed renderer legs visibility", modelBackedRenderer, "model.leftLeg.visible = true;");
		assertContains("model-backed renderer boots visibility", modelBackedRenderer, "case FEET -> {");
		assertNotContains("model-backed renderer front-facing transform", modelBackedRenderer,
				"Axis.YP.rotationDegrees(180.0F)");

		String silentArchonRenderer = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/item/SilentArchonArmorItemRenderer.java"));
		assertContains("silent archon renderer shared display transform", silentArchonRenderer,
				"ArmorItemDisplayTransformHelper.apply(displayContext, slot, poseStack)");
		assertContains("silent archon renderer neutral item pose", silentArchonRenderer,
				"ArmorItemModelPoseHelper.reset(model);");
		assertNotContains("silent archon renderer front-facing transform", silentArchonRenderer,
				"Axis.YP.rotationDegrees(180.0F)");

		String poseHelper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/ArmorItemModelPoseHelper.java"));
		assertContains("armor item pose reset helper", poseHelper, "model.head.getAllParts().forEach(ModelPart::resetPose);");
		assertContains("armor item pose reset helper", poseHelper, "model.hat.getAllParts().forEach(ModelPart::resetPose);");
		assertContains("armor item pose reset helper", poseHelper, "model.body.getAllParts().forEach(ModelPart::resetPose);");
		assertContains("armor item pose reset helper", poseHelper, "model.rightArm.getAllParts().forEach(ModelPart::resetPose);");
		assertContains("armor item pose reset helper", poseHelper, "model.leftArm.getAllParts().forEach(ModelPart::resetPose);");
		assertContains("armor item pose reset helper", poseHelper, "model.rightLeg.getAllParts().forEach(ModelPart::resetPose);");
		assertContains("armor item pose reset helper", poseHelper, "model.leftLeg.getAllParts().forEach(ModelPart::resetPose);");
		assertContains("armor item pose reset helper", poseHelper, "model.leftArmPose = HumanoidModel.ArmPose.EMPTY;");
		assertContains("armor item pose reset helper", poseHelper, "model.rightArmPose = HumanoidModel.ArmPose.EMPTY;");
		assertContains("armor item pose reset helper", poseHelper, "model.crouching = false;");
		assertContains("armor item pose reset helper", poseHelper, "model.swimAmount = 0.0F;");

		String displayTransformHelper = read(SOURCE_ROOT.resolve(
				"com/vincenthuto/hemomancy/client/render/armor/ArmorItemDisplayTransformHelper.java"));
		assertContains("armor item normalized gui offset", displayTransformHelper,
				"translateWithNormalizedSlotOffset(poseStack, slot, -0.35D, 0.0D, 1.0D);");
		assertContains("armor item helmet gui transform", displayTransformHelper,
				"applyHelmetGuiTransform(slot, poseStack);");
		assertContains("armor item helmet gui transform", displayTransformHelper,
				"private static void applyHelmetGuiTransform(EquipmentSlot slot, PoseStack poseStack)");
		assertContains("armor item helmet gui transform", displayTransformHelper,
				"if (slot == EquipmentSlot.HEAD)");
		assertContains("armor item helmet gui transform", displayTransformHelper,
				"poseStack.mulPose(Axis.YP.rotationDegrees(-24.0F));");
		assertContains("armor item helmet first-person transform", displayTransformHelper,
				"applyArmorFirstPersonTransform(context, slot, poseStack);");
		assertContains("armor item helmet first-person transform", displayTransformHelper,
				"private static void applyArmorFirstPersonTransform(ItemDisplayContext context, EquipmentSlot slot, PoseStack poseStack)");
		assertContains("armor item helmet first-person transform", displayTransformHelper,
				"context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? -54.0F : 54.0F");
		assertNotContains("armor item helmet first-person transform should not use reversed yaw", displayTransformHelper,
				"context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND ? 54.0F : -54.0F");
		assertContains("armor item helmet first-person transform", displayTransformHelper,
				"poseStack.translate(0.0D, 0.15D, 0.0D);");
		assertContains("armor item helmet first-person transform", displayTransformHelper,
				"poseStack.mulPose(Axis.YP.rotationDegrees(yaw));");
		assertContains("armor item head y normalization", displayTransformHelper, "case HEAD -> 0.55D;");
		assertContains("armor item chest y normalization", displayTransformHelper, "case CHEST -> 0.15D;");
		assertContains("armor item legs y normalization", displayTransformHelper, "case LEGS -> -0.30D;");
		assertContains("armor item boots y normalization", displayTransformHelper, "case FEET -> -0.45D;");

		assertItemModelParent("blood_lust_helm", "builtin/entity");
		assertItemModelParent("blood_lust_helm_tengu", "builtin/entity");
		assertItemModelParent("blood_lust_helm_grinning", "builtin/entity");
		assertItemModelParent("blood_lust_helm_lodestone", "builtin/entity");
		assertItemModelParent("blood_lust_helm_velorum", "builtin/entity");
		assertItemModelParent("blood_lust_chest", "builtin/entity");
		assertItemModelParent("blood_lust_legs", "builtin/entity");
		assertItemModelParent("blood_lust_boots", "builtin/entity");
		assertItemModelParent("silent_archon_helm", "builtin/entity");
		assertItemModelParent("silent_archon_chestplate", "builtin/entity");
		assertItemModelParent("silent_archon_leggings", "builtin/entity");
		assertItemModelParent("silent_archon_boots", "builtin/entity");
		assertItemModelParent("barbed_helm", "builtin/entity");
		assertItemModelParent("barbed_chestplate", "builtin/entity");
		assertItemModelParent("barbed_leggings", "builtin/entity");
		assertItemModelParent("barbed_boots", "builtin/entity");
		assertItemModelParent("chitinite_helm", "builtin/entity");
		assertItemModelParent("chitinite_chestplate", "builtin/entity");
		assertItemModelParent("chitinite_leggings", "builtin/entity");
		assertItemModelParent("chitinite_boots", "builtin/entity");
		assertItemModelParent("unstained_helm", "builtin/entity");
		assertItemModelParent("unstained_chestplate", "builtin/entity");
		assertItemModelParent("unstained_leggings", "builtin/entity");
		assertItemModelParent("unstained_boots", "builtin/entity");
		assertItemModelParent("venous_strider_sabatons", "builtin/entity");
		assertItemModelParent("covenant_mantle", "builtin/entity");
		assertItemModelParent("marrow_crown", "builtin/entity");
		assertItemModelParent("hemolymphopoda_headpiece", "builtin/entity");

		assertItemModelParent("hematic_iron_helm", "minecraft:item/generated");

		assertImageSize("venous strider sabatons worn texture",
				"assets/hemomancy/textures/models/armor/venous_strider_layer_1.png", 256, 128);
		assertImageSize("silent archon outer worn texture",
				"assets/hemomancy/textures/models/armor/silent_archon_layer_1.png", 128, 128);
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

	private static void assertItemModelParent(String item, String parent) throws IOException {
		String model = read(RESOURCE_ROOT.resolve("assets/hemomancy/models/item/" + item + ".json"));
		assertContains("model parent for " + item, model, "\"parent\": \"" + parent + "\"");
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

	private static void assertMethodContains(String label, String source, String methodName, String expected) {
		String method = source.substring(source.indexOf(methodName));
		method = method.substring(0, method.indexOf("\n\t}\n") + 4);
		assertContains(label, method, expected);
	}

	private static void assertNotContains(String label, String text, String forbidden) {
		if (text.contains(forbidden)) {
			throw new AssertionError(label + ": should not contain " + forbidden);
		}
	}
}
