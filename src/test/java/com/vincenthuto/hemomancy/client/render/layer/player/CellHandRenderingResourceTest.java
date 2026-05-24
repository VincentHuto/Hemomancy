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
		String bloodCellParticle = read("src/main/java/com/vincenthuto/hemomancy/client/particle/BloodCellParticle.java");
		String absorbedBloodCellParticle = read("src/main/java/com/vincenthuto/hemomancy/client/particle/AbsorbedBloodCellParticle.java");
		String projectionItem = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodProjectionItem.java");
		String absorptionItem = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living/BloodAbsorptionItem.java");

		assertDoesNotContain("third-person particles keep the renderer hand transform", layer,
				"curMatrix.mul(inverted)");
		assertContains("third-person particles use a body-yaw hand anchor", layer,
				"calculateThirdPersonHandOrigin(living, side)");
		assertContains("third-person right side vector is not mirrored to the left shoulder", layer,
				"new Vec3(-forward.z, 0.0D, forward.x)");
		assertDoesNotContain("third-person particles do not spawn from the camera", layer,
				"gameRenderer.getMainCamera().getPosition()");
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
				"spawnFirstPersonParticlesForStack(stack, side);");
		assertContains("first-person particles use camera-local hand offsets", itemRenderer,
				"calculateFirstPersonHandAnchor(hand)");
		assertContains("first-person hand aura is raised toward the visible palm", itemRenderer,
				"-0.14D + 0.035D * pulse");
		assertContains("first-person hand aura is moved out to the visible palm", itemRenderer,
				"sideSign * (0.32D + 0.035D * wave)");
		assertContains("first-person hand aura sits closer to the rendered hand", itemRenderer,
				"0.66D - 0.03D * pulse");
		assertContains("first-person particles are anchored while alive", itemRenderer,
				"particle.setFirstPersonAnchor(localOffset);");
		assertContains("absorbed first-person particles are created directly for hand targeting", itemRenderer,
				"mc.particleEngine.createParticle(AbsrobedBloodCellParticleFactory.createData(targetColor)");
		assertContains("absorbed first-person particles follow the active hand target", itemRenderer,
				"particle.setFirstPersonTargetAnchor(anchor);");
		assertContains("third-person absorbed cells do not sag below the hand target", layer,
				"particle.setTargetYOffset(0.0D);");
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
}
