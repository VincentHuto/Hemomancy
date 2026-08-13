package com.vincenthuto.hemomancy.client.player;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.WarpChairBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Client-only presentation for players using a Warp Chair's vanilla sleep state. */
public final class WarpChairPlayerPose {
	private static final SeatedLegs SEATED_LEGS = new SeatedLegs(
			-1.4137167F, (float) (Math.PI / 10.0D), (float) (-Math.PI / 10.0D),
			0.07853982F, -0.07853982F);
	private static final TrancePresentation TRANCE_PRESENTATION = new TrancePresentation(
			-0.5D, -1.15F, 0.15F, -0.15F, 0.20F, -0.08F, 0.18F);

	private WarpChairPlayerPose() {
	}

	public static boolean isSeated(LivingEntity entity) {
		return entity instanceof Player && entity.hasPose(Pose.SLEEPING) && chairState(entity).isPresent();
	}

	public static boolean applyRenderRotation(LivingEntity entity, PoseStack poseStack) {
		Optional<BlockState> chair = chairState(entity);
		if (chair.isEmpty()) return false;
		Direction facing = chair.get().getValue(WarpChairBlock.FACING);
		poseStack.translate(0.0D, TRANCE_PRESENTATION.verticalOffset(), 0.0D);
		poseStack.mulPose(Axis.YP.rotationDegrees(bodyRotationDegrees(facing)));
		return true;
	}

	public static void applyModelPose(LivingEntity entity, HumanoidModel<?> model) {
		if (!isSeated(entity)) return;
		model.body.xRot = 0.0F;
		model.body.yRot = 0.0F;
		model.body.zRot = 0.0F;
		model.rightLeg.xRot = SEATED_LEGS.xRot();
		model.rightLeg.yRot = SEATED_LEGS.rightYRot();
		model.rightLeg.zRot = SEATED_LEGS.rightZRot();
		model.leftLeg.xRot = SEATED_LEGS.xRot();
		model.leftLeg.yRot = SEATED_LEGS.leftYRot();
		model.leftLeg.zRot = SEATED_LEGS.leftZRot();
		model.rightArm.xRot = TRANCE_PRESENTATION.armXRot();
		model.rightArm.yRot = 0.0F;
		model.rightArm.zRot = TRANCE_PRESENTATION.rightArmZRot();
		model.leftArm.xRot = TRANCE_PRESENTATION.armXRot();
		model.leftArm.yRot = 0.0F;
		model.leftArm.zRot = TRANCE_PRESENTATION.leftArmZRot();
		model.head.xRot = TRANCE_PRESENTATION.headXRot();
		model.head.yRot = TRANCE_PRESENTATION.headYRot();
		model.head.zRot = TRANCE_PRESENTATION.headZRot();
		model.hat.copyFrom(model.head);
	}

	public static void resetHeadRoll(HumanoidModel<?> model) {
		model.head.zRot = 0.0F;
	}

	public static float bodyRotationDegrees(Direction facing) {
		return switch (facing) {
			case SOUTH -> 180.0F;
			case EAST -> -90.0F;
			case WEST -> 90.0F;
			default -> 0.0F;
		};
	}

	public static SeatedLegs seatedLegs() {
		return SEATED_LEGS;
	}

	public static TrancePresentation trancePresentation() {
		return TRANCE_PRESENTATION;
	}

	private static Optional<BlockState> chairState(LivingEntity entity) {
		if (!(entity instanceof Player) || !entity.hasPose(Pose.SLEEPING)) return Optional.empty();
		return entity.getSleepingPos()
				.filter(entity.level()::hasChunkAt)
				.map(entity.level()::getBlockState)
				.filter(state -> state.is(BlockInit.warp_chair.get()));
	}

	public record SeatedLegs(float xRot, float rightYRot, float leftYRot,
			float rightZRot, float leftZRot) {
	}

	public record TrancePresentation(double verticalOffset, float armXRot,
			float rightArmZRot, float leftArmZRot, float headXRot,
			float headYRot, float headZRot) {
	}
}
