package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.data.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchBreathRules;
import com.vincenthuto.hemomancy.common.item.unstained.tool.AbsolutionDaggerItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.SilthmereGlaiveItem;
import com.vincenthuto.hemomancy.common.item.unstained.tool.UnstainedWarhammerItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PlayerAnimationKind;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/**
 * Every third-person player pose, in priority order. The staff planting pose, the offset reset, the casting pose and
 * the warp chair always apply; of the poses that claim the frame, the first to pose the model wins and the rest are
 * skipped. Each animation keeps its own packet, client state, tick, session reset and first-person hand renderer.
 */
public final class PlayerAnimationRegistry {
	/** One pose; returns true when it claims the frame. */
	@FunctionalInterface
	interface Pose {
		boolean apply(LivingEntity entity, HumanoidModel<?> model, float partialTick);
	}

	record Entry(String name, boolean claims, Pose pose) {
	}

	static final List<Entry> THIRD_PERSON = List.of(
			always("cardinal_staff_planting", (entity, model, partialTick) -> {
				CardinalRiteStaffPlantingClientState.applyThirdPersonPose(entity, model, partialTick);
				return false;
			}),
			always("offset_reset", PlayerAnimationRegistry::resetOffsets),
			always("casting", (entity, model, partialTick) -> {
				if (entity instanceof Player player) CastingPlayerPose.apply(player, model, partialTick);
				return false;
			}),
			claims("hematic_microscope", PlayerAnimationRegistry::microscope),
			claims("blood_vial_injection", PlayerAnimationRegistry::injection),
			claims("living_torch_breath", PlayerAnimationRegistry::torchBreath),
			claims("unstained_weapon_swing", PlayerAnimationRegistry::unstainedSwing),
			claims("living_weapon_stroke", PlayerAnimationRegistry::livingStroke),
			always("warp_chair", (entity, model, partialTick) -> {
				WarpChairPlayerPose.applyModelPose(entity, model);
				return false;
			}));

	private PlayerAnimationRegistry() {
	}

	public static void applyThirdPersonPose(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		boolean claimed = false;
		for (Entry entry : THIRD_PERSON) {
			if (entry.claims() && claimed) continue;
			if (entry.pose().apply(entity, model, partialTick) && entry.claims()) claimed = true;
		}
	}

	private static Entry always(String name, Pose pose) {
		return new Entry(name, false, pose);
	}

	private static Entry claims(String name, Pose pose) {
		return new Entry(name, true, pose);
	}

	private static boolean resetOffsets(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		if (!(entity instanceof Player)) return false;
		// Vanilla never resets these; the casting waist pivot assigns them, so clear last frame's offset first.
		model.body.z = 0;
		model.head.z = 0;
		model.hat.z = 0;
		if (model instanceof PlayerModel<?> playerModel) playerModel.jacket.z = 0;
		return false;
	}

	private static boolean microscope(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		if (!(entity instanceof Player player)) return false;
		var microscope = HematicMicroscopeClientState.animation(player);
		if (microscope == null) return false;
		HematicMicroscopePose.apply(model, microscope.poseTick(partialTick), microscope.packet.right());
		return true;
	}

	private static boolean injection(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		if (!(entity instanceof Player player)) return false;
		var injection = BloodVialInjectionClientState.animation(player);
		if (injection == null) return false;
		BloodVialInjectionPose.apply(model, injection.elapsed(partialTick), injection.packet.right());
		return true;
	}

	private static boolean torchBreath(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		if (!(entity instanceof Player player) || !PlayerAnimationClientState.isBreathing(entity)) return false;
		InteractionHand hand = PlayerAnimationClientState.hand(entity, PlayerAnimationKind.LIVING_TORCH_BREATH);
		boolean right = armFor(player, hand) == HumanoidArm.RIGHT;
		float windup = Mth.clamp(PlayerAnimationClientState.elapsed(entity, PlayerAnimationKind.LIVING_TORCH_BREATH,
				partialTick) / LivingTorchBreathRules.WINDUP_TICKS, 0.0F, 1.0F);
		LivingTorchPlayerPose.BreathPose pose = LivingTorchPlayerPose.breath(windup, right);
		model.body.xRot += pose.bodyLean();
		model.head.xRot -= pose.bodyLean() * 0.35F;
		var arm = right ? model.rightArm : model.leftArm;
		var clearArm = right ? model.leftArm : model.rightArm;
		arm.xRot = pose.armXRot();
		arm.yRot = pose.armYRot();
		arm.zRot = pose.armZRot();
		clearArm.yRot *= 0.25F;
		clearArm.zRot *= 0.25F;
		copySleeves(model);
		return true;
	}

	/** Claims the frame whenever an Unstained weapon is in the swinging hand, even between swings. */
	private static boolean unstainedSwing(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		if (!(entity instanceof Player player)) return false;
		InteractionHand swingingHand = swingingHand(player);
		var swingingItem = player.getItemInHand(swingingHand).getItem();
		if (!(swingingItem instanceof AbsolutionDaggerItem || swingingItem instanceof SilthmereGlaiveItem
				|| swingingItem instanceof UnstainedWarhammerItem)
				|| CardinalRiteStaffPlantingClientState.isAnimating(player)) return false;
		float swing = player.getAttackAnim(partialTick);
		if (swing <= 0.0F) return true;
		boolean right = armFor(player, swingingHand) == HumanoidArm.RIGHT;
		var pose = swingingItem instanceof AbsolutionDaggerItem
				? UnstainedWeaponPlayerPose.dagger(swing, right)
				: swingingItem instanceof SilthmereGlaiveItem
						? UnstainedWeaponPlayerPose.glaive(swing, right)
						: UnstainedWeaponPlayerPose.hammer(swing, right);
		UnstainedWeaponPlayerPose.applyThirdPerson(model, pose, right, player.getItemInHand(otherHand(swingingHand)).isEmpty());
		copySleeves(model);
		return true;
	}

	private static boolean livingStroke(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		if (!(entity instanceof Player player) || CardinalRiteStaffPlantingClientState.isAnimating(player)) return false;
		InteractionHand swingingHand = swingingHand(player);
		var swingingItem = player.getItemInHand(swingingHand).getItem();
		float swing = player.getAttackAnim(partialTick);
		HumanoidArm swingingArm = armFor(player, swingingHand);
		var stroke = LivingWeaponPlayerPose.stroke(swingingItem, SwingComboTracker.strike(player, swingingArm, swing));
		if (stroke == null || swing <= 0.0F) return false;
		boolean right = swingingArm == HumanoidArm.RIGHT;
		UnstainedWeaponPlayerPose.applyThirdPerson(model, LivingWeaponPlayerPose.thirdPerson(stroke, swing, right),
				right, player.getItemInHand(otherHand(swingingHand)).isEmpty());
		copySleeves(model);
		return true;
	}

	private static InteractionHand swingingHand(Player player) {
		return player.swingingArm == null ? InteractionHand.MAIN_HAND : player.swingingArm;
	}

	private static InteractionHand otherHand(InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	}

	private static HumanoidArm armFor(Player player, InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();
	}

	private static void copySleeves(HumanoidModel<?> model) {
		if (model instanceof PlayerModel<?> playerModel) {
			playerModel.rightSleeve.copyFrom(model.rightArm);
			playerModel.leftSleeve.copyFrom(model.leftArm);
		}
	}
}
