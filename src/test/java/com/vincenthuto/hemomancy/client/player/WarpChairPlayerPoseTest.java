package com.vincenthuto.hemomancy.client.player;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class WarpChairPlayerPoseTest {
	@Test
	void seatedBodyRotationTracksTheChairFacing() {
		assertEquals(0.0F, WarpChairPlayerPose.bodyRotationDegrees(Direction.NORTH));
		assertEquals(180.0F, WarpChairPlayerPose.bodyRotationDegrees(Direction.SOUTH));
		assertEquals(-90.0F, WarpChairPlayerPose.bodyRotationDegrees(Direction.EAST));
		assertEquals(90.0F, WarpChairPlayerPose.bodyRotationDegrees(Direction.WEST));
	}

	@Test
	void seatedLegsUseTheVanillaRidingBend() {
		WarpChairPlayerPose.SeatedLegs legs = WarpChairPlayerPose.seatedLegs();
		assertEquals(-1.4137167F, legs.xRot());
		assertEquals((float) (Math.PI / 10.0D), legs.rightYRot());
		assertEquals((float) (-Math.PI / 10.0D), legs.leftYRot());
		assertEquals(0.07853982F, legs.rightZRot());
		assertEquals(-0.07853982F, legs.leftZRot());
	}

	@Test
	void trancePresentationSinksIntoTheSeatAndRestsTheUpperBody() {
		WarpChairPlayerPose.TrancePresentation pose = WarpChairPlayerPose.trancePresentation();
		assertEquals(-0.5D, pose.verticalOffset());
		assertEquals(-1.15F, pose.armXRot());
		assertEquals(0.15F, pose.rightArmZRot());
		assertEquals(-0.15F, pose.leftArmZRot());
		assertEquals(0.20F, pose.headXRot());
		assertEquals(-0.08F, pose.headYRot());
		assertEquals(0.18F, pose.headZRot());
	}

	@Test
	void modelSetupClearsStaleChairHeadRollWithoutErasingPitchOrYaw() {
		ModelPart root = LayerDefinition.create(
				HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F), 64, 64).bakeRoot();
		HumanoidModel<LivingEntity> model = new HumanoidModel<>(root);
		model.head.xRot = 0.31F;
		model.head.yRot = -0.27F;
		model.head.zRot = WarpChairPlayerPose.trancePresentation().headZRot();

		WarpChairPlayerPose.resetHeadRoll(model);

		assertEquals(0.31F, model.head.xRot);
		assertEquals(-0.27F, model.head.yRot);
		assertEquals(0.0F, model.head.zRot);
	}
}
