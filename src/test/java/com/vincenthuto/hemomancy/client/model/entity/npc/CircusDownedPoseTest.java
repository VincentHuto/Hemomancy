package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CircusDownedPoseTest {
	@Test
	void torsoStaysSeatedOnTheLegsAndCarriesHeadAndShoulders() {
		assertSeated(humanoid(0.0F), 2.0F, 0.3F);
	}

	@Test
	void stiltRigFoldsAboutItsOwnRaisedHip() {
		// The Stilt Walker authors its torso 24 pixels higher, with hips at -12.
		assertSeated(humanoid(-24.0F), 2.0F, 0.0F);
	}

	@Test
	void standingAgainReleasesTheKnees() {
		HumanoidModel<LivingEntity> model = humanoid(0.0F);
		CircusDownedPose pose = new CircusDownedPose(model, 2.0F, 0.3F);
		pose.apply(true, 0.0F);
		pose.apply(false, 0.0F);
		assertEquals(0.0F, model.rightLeg.getChild("right_calf").xRot, 0.0001F);
	}

	private static void assertSeated(HumanoidModel<LivingEntity> model, float seatDepth, float kneeBend) {
		Vector3f restNeck = new Vector3f(model.body.x, model.body.y, model.body.z);
		Vector3f restShoulder = new Vector3f(model.rightArm.x, model.rightArm.y, model.rightArm.z);
		float restHip = model.rightLeg.y;
		new CircusDownedPose(model, seatDepth, kneeBend).apply(true, 40.0F);

		// The bottom of the torso meets the hip line the legs now pivot from, on the ground.
		Vector3f hip = point(model.body, 0.0F, restHip - restNeck.y, 0.0F);
		assertClose(new Vector3f(0.0F, model.rightLeg.y, 0.0F), hip);
		assertEquals(24.0F - seatDepth, model.rightLeg.y, 0.0001F);
		assertEquals(model.rightLeg.y, model.leftLeg.y, 0.0001F);
		// The head rides the posed neck, and the shoulder keeps its rest offset on the torso.
		assertClose(point(model.body, 0.0F, 0.0F, 0.0F), new Vector3f(model.head.x, model.head.y, model.head.z));
		Vector3f shoulder = point(model.body, restShoulder.x - restNeck.x, restShoulder.y - restNeck.y,
				restShoulder.z - restNeck.z);
		assertClose(shoulder, new Vector3f(model.rightArm.x, model.rightArm.y, model.rightArm.z));
		// Slumped forward, never through the legs or lying flat.
		assertTrue(model.body.xRot > 0.3F && model.body.xRot < 0.8F);
		assertTrue(model.head.z < 0.0F);
	}

	/** A point in a part's own pixel space, in model pixels. */
	private static Vector3f point(ModelPart part, float x, float y, float z) {
		PoseStack stack = new PoseStack();
		part.translateAndRotate(stack);
		return stack.last().pose().transformPosition(x / 16.0F, y / 16.0F, z / 16.0F, new Vector3f()).mul(16.0F);
	}

	private static void assertClose(Vector3f expected, Vector3f actual) {
		assertEquals(expected.x, actual.x, 0.001F);
		assertEquals(expected.y, actual.y, 0.001F);
		assertEquals(expected.z, actual.z, 0.001F);
	}

	private static HumanoidModel<LivingEntity> humanoid(float raise) {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		var root = mesh.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create().addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F),
				PartPose.offset(0.0F, raise, 0.0F));
		root.addOrReplaceChild("body", CubeListBuilder.create().addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F),
				PartPose.offset(0.0F, raise, 0.0F));
		root.addOrReplaceChild("right_arm", CubeListBuilder.create().addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
				PartPose.offset(-5.0F, 2.0F + raise, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F),
				PartPose.offset(5.0F, 2.0F + raise, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
				PartPose.offset(-1.9F, 12.0F + raise, 0.0F)).addOrReplaceChild("right_calf",
				CubeListBuilder.create().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
				PartPose.offset(1.9F, 12.0F + raise, 0.0F)).addOrReplaceChild("left_calf",
				CubeListBuilder.create().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
		return new HumanoidModel<>(LayerDefinition.create(mesh, 64, 64).bakeRoot());
	}
}
