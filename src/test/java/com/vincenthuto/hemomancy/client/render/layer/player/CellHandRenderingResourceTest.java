package com.vincenthuto.hemomancy.client.render.layer.player;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CellHandRenderingResourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private CellHandRenderingResourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String layer = read("src/main/java/com/vincenthuto/hemomancy/client/render/layer/player/CellHandLayer.java");
		String model = read("src/main/java/com/vincenthuto/hemomancy/client/model/item/BloodArmModel.java");
		String itemRenderer = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/CellHandItemRenderer.java");
		String particleEffects = read("src/main/java/com/vincenthuto/hemomancy/client/render/item/hematic/CellHandParticleEffects.java");
		String bloodCellParticle = read("src/main/java/com/vincenthuto/hemomancy/client/particle/BloodCellParticle.java");
		String absorbedBloodCellParticle = read("src/main/java/com/vincenthuto/hemomancy/client/particle/AbsorbedBloodCellParticle.java");
		String willAbsorptionGlowParticle = read("src/main/java/com/vincenthuto/hemomancy/client/particle/WillAbsorptionGlowParticle.java");
		String willAbsorptionGlowFactory = read("src/main/java/com/vincenthuto/hemomancy/client/particle/factory/WillAbsorptionGlowParticleFactory.java");
		String particleInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/ParticleInit.java");
		String projectionItem = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");
		String absorptionItem = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java");

		assertDoesNotContain("third-person particles do not use a fixed body-yaw approximation", layer,
				"double bodyYaw = Math.toRadians(living.yBodyRot)");
		assertContains("third-person particles use the rendered parent hand transform", layer,
				"getParentModel().translateToHand(side, poseStack)");
		assertContains("third-person particles convert the rendered palm to world space", layer,
				"CellHandRenderOrigin.fromPose");
		assertContains("third-person particles share the cell-hand particle helper", layer,
				"CellHandParticleEffects.spawnThirdPersonParticlesFromOrigin");
		assertContains("third-person blood hand presentation includes living staff utility use", layer,
				"LivingStaffItem.isLivingStaffUtilityUse");
		assertDoesNotContain("third-person particles do not spawn from the camera", layer,
				"living.position()");
		assertContains("third-person blood arm copies the active parent pose", layer,
				"copyBloodArmPose(this.getParentModel())");
		assertContains("third-person blood arm hides untouched body parts", layer,
				"model.setAllVisible(false)");

		assertContains("blood arm overlay expands far enough past the vanilla arm to avoid depth overlap", model,
				"new CubeDeformation(0.28F)");
		assertContains("blood arm overlay uses vanilla player-skin UV dimensions", model,
				"LayerDefinition.create(meshdefinition, 64, 64)");
		assertContains("blood arm overlay keeps its configured tint", model,
				"rightArm.render(poseStack, buffer, packedLight, packedOverlay, packedColor)");
		assertContains("projection uses a vanilla channeling pose", projectionItem, "return UseAnim.BOW;");
		assertContains("absorption uses a vanilla channeling pose", absorptionItem, "return UseAnim.BOW;");
		assertContains("projection starts item use successfully", projectionItem,
				"return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);");
		assertContains("absorption starts item use successfully", absorptionItem,
				"return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);");
		assertContains("first-person blood hand has a casting motion", itemRenderer,
				"applyCastingArmTransform(matrixStackIn, side);");
		assertContains("first-person casting motion follows the active hand", itemRenderer,
				"if (activeArm != side)");
		assertContains("first-person particles are emitted from the active casting hand", itemRenderer,
				"spawnFirstPersonParticlesForStack(stack, side, matrixStackIn);");
		assertContains("first-person particles only emit while the camera is first person", particleEffects,
				"!mc.options.getCameraType().isFirstPerson()");
		assertContains("first-person particles receive a pose-derived origin", itemRenderer,
				"CellHandRenderOrigin.fromPose");
		assertContains("first-person palm origin includes the rendered model-part transform", itemRenderer,
				"arm.translateAndRotate(poseStack)");
		assertContains("first-person palm origin uses the arm endpoint", itemRenderer,
				"CellHandRenderOrigin.PALM_OFFSET_Y");
		assertContains("first-person particles are anchored while alive", particleEffects,
				"particle.setFirstPersonAnchor(localOffset);");
		assertContains("absorbed first-person particles are created directly for hand targeting", particleEffects,
				"AbsorbedBloodCellParticleFactory.createData(targetColor)");
		assertContains("absorbed first-person particles follow the active hand target", particleEffects,
				"particle.setFirstPersonTargetAnchor(anchor);");
		assertContains("projection particles use an explicit source-to-surface path", particleEffects,
				"particle.setProjectionPath(origin, trace.getLocation()");
		assertDoesNotContain("projection particles do not compensate above the block surface", particleEffects,
				"trace.getLocation().add(0.0D, 1.05D, 0.0D)");
		assertContains("third-person absorbed cells do not sag below the hand target", particleEffects,
				"particle.setTargetYOffset(0.0D);");
		assertContains("absorption particles can target blood-bearing blocks", particleEffects,
				"BlockBloodInteractions.findLookedAtBloodBlockSource");
		assertContains("block absorption particles use blood color", particleEffects,
				"AbsorbedBloodCellParticleFactory.createData(ParticleColor.BLOOD)");
		assertContains("block absorption particles share the hand-targeted absorbed particle path", particleEffects,
				"spawnAbsorbedBloodParticle");
		assertBefore("block absorption visual takes priority before entity absorption targets", particleEffects,
				"findLookedAtBloodBlockSource", "getAbsorptionParticleTargets");
		assertContains("faltering Will absorption particles use the dedicated dark glow mote", particleEffects,
				"WillAbsorptionGlowParticleFactory.createData(WILL_ABSORPTION_GLOW)");
		assertContains("faltering Will absorption uses black glow color", particleEffects,
				"WILL_ABSORPTION_GLOW = ParticleColor.BLACK");
		assertContains("Will absorption adds a school-colored spiral layer", particleEffects,
				"spawnWillAbsorptionTendencySpiralParticle");
		assertContains("Will absorption spiral uses the aligned tendency color", particleEffects,
				"will.getSchool().getColor()");
		assertContains("Will absorption spiral reuses the Will glow particle path", particleEffects,
				"WillAbsorptionGlowParticleFactory.createData(will.getSchool().getColor())");
		assertContains("Will absorption spiral rotates around the Will source", particleEffects,
				"double spiralAngle");
		assertContains("Will absorption spiral targets the active hand or staff anchor", particleEffects,
				"particle.setFirstPersonTargetAnchor(firstPersonTargetAnchor.get())");
		assertBefore("faltering Will absorption visual takes priority before normal entity absorption particles",
				particleEffects, "getFalteringWillAbsorptionParticleTarget", "getAbsorptionParticleTargets");
		assertContains("Will absorption particles continue during the absorbing channel", particleEffects,
				"WillEntity::canBloodAbsorptionDrawParticles");
		assertContains("Will absorption glow is registered as its own particle type", particleInit,
				"will_absorption_glow");
		assertContains("Will absorption glow uses its own factory", particleInit,
				"WillAbsorptionGlowParticleFactory::new");
		assertContains("Will absorption glow factory creates its registered data", willAbsorptionGlowFactory,
				"ParticleInit.will_absorption_glow.get()");
		assertContains("Will absorption glow factory creates pulse data with same particle type", willAbsorptionGlowFactory,
				"createPulseData");
		assertContains("Will absorption glow factory passes pulse mode into particle", willAbsorptionGlowFactory,
				"data.outwardPulse");
		assertContains("Will absorption glow uses dark particle compositing", willAbsorptionGlowParticle,
				"HLRenderTypeInit.DARK_GLOW_RENDER");
		assertContains("Will absorption glow has outward pulse motion", willAbsorptionGlowParticle,
				"outwardPulse");
		assertContains("Will absorption glow pulse mode moves away from origin", willAbsorptionGlowParticle,
				"this.move(this.xd, this.yd, this.zd);");
		assertContains("Will absorption glow can retarget to the moving first-person hand", willAbsorptionGlowParticle,
				"this.setPosFromFirstPersonTarget();");
		assertContains("Will absorption glow target anchors use camera basis vectors", willAbsorptionGlowParticle,
				"camera.getLookVector()");
		assertContains("Will absorption glow stops terminal downward sag when hand-targeted", willAbsorptionGlowParticle,
				"this.targetYOffset = 0.0D;");
		assertContains("first-person particle anchors follow camera rotation", bloodCellParticle,
				"this.setPosFromCameraAnchor();");
		assertContains("camera anchored motes use camera basis vectors", bloodCellParticle,
				"camera.getLookVector()");
		assertContains("absorbed first-person particles retarget to the moving hand", absorbedBloodCellParticle,
				"this.setPosFromFirstPersonTarget();");
		assertContains("absorbed target anchors use camera basis vectors", absorbedBloodCellParticle,
				"camera.getLookVector()");
		assertContains("hand-targeted absorbed particles stop terminal downward sag", absorbedBloodCellParticle,
				"this.targetYOffset = 0.0D;");
		assertDoesNotContain("third-person blood arm does not add extra animation over the parent arm pose", layer,
				"applyThirdPersonCastingArmMotion");
		assertContains("third-person blood arm renders the hardened skin texture", layer,
				"RenderType.entityCutoutNoCull(skinTexture)");
		assertContains("third-person blood arm keeps texture colors", layer, "OverlayTexture.NO_OVERLAY, -1");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + " (missing '" + expected + "')");
		}
	}

	private static void assertDoesNotContain(String label, String text, String unexpected) {
		if (text.contains(unexpected)) {
			throw new AssertionError(label + " (still contains '" + unexpected + "')");
		}
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0 || secondIndex < 0 || firstIndex > secondIndex) {
			throw new AssertionError(label + " (expected '" + first + "' before '" + second + "')");
		}
	}
}
