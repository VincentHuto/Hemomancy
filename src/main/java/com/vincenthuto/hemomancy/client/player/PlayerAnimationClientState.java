package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchBreathRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PlayerAnimationKind;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Client animation instances keyed by player id and animation kind. */
public final class PlayerAnimationClientState {
	private static final Map<Key, Animation> ACTIVE = new HashMap<>();
	private static ClientLevel activeLevel;

	private PlayerAnimationClientState() { }

	public static void set(int entityId, PlayerAnimationKind kind, boolean active, InteractionHand hand) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;
		if (activeLevel != minecraft.level) ACTIVE.clear();
		activeLevel = minecraft.level;
		Key key = new Key(entityId, kind);
		if (active) ACTIVE.put(key, new Animation(hand, minecraft.level.getGameTime()));
		else ACTIVE.remove(key);
	}

	public static boolean isActive(LivingEntity entity, PlayerAnimationKind kind) {
		return entity != null && ACTIVE.containsKey(new Key(entity.getId(), kind));
	}

	public static InteractionHand hand(LivingEntity entity, PlayerAnimationKind kind) {
		Animation animation = entity == null ? null : ACTIVE.get(new Key(entity.getId(), kind));
		return animation == null ? InteractionHand.MAIN_HAND : animation.hand;
	}

	public static float elapsed(LivingEntity entity, PlayerAnimationKind kind, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		Animation animation = entity == null ? null : ACTIVE.get(new Key(entity.getId(), kind));
		return animation == null || minecraft.level == null ? 0.0F
				: minecraft.level.getGameTime() - animation.startGameTime + partialTick;
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null || activeLevel != minecraft.level) {
			ACTIVE.clear();
			activeLevel = minecraft.level;
			return;
		}
		ACTIVE.entrySet().removeIf(entry -> {
			var entity = minecraft.level.getEntity(entry.getKey().entityId);
			if (!(entity instanceof LivingEntity living) || !living.isAlive() || living.isRemoved()) return true;
			if (entry.getKey().kind == PlayerAnimationKind.LIVING_TORCH_BREATH
					&& minecraft.level.getGameTime() - entry.getValue().startGameTime > 3L) {
				return !living.isUsingItem() || !(living.getUseItem().getItem() instanceof LivingTorchItem);
			}
			return false;
		});
	}

	public static void applyThirdPersonPose(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		if (!(entity instanceof Player player)) return;
		if (isBreathing(entity)) {
			InteractionHand hand = hand(entity, PlayerAnimationKind.LIVING_TORCH_BREATH);
			boolean right = armFor(player, hand) == HumanoidArm.RIGHT;
			float windup = Mth.clamp(elapsed(entity, PlayerAnimationKind.LIVING_TORCH_BREATH, partialTick)
					/ LivingTorchBreathRules.WINDUP_TICKS, 0.0F, 1.0F);
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
			return;
		}
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		if (!(main.getItem() instanceof LivingTorchItem) && !(off.getItem() instanceof LivingTorchItem)) return;
		float swing = player.getAttackAnim(partialTick);
		if (swing <= 0.0F) return;
		boolean right = armFor(player, main.getItem() instanceof LivingTorchItem
				? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND) == HumanoidArm.RIGHT;
		LivingTorchPlayerPose.ArmPose pose = LivingTorchPlayerPose.jabArm(swing, right);
		var arm = right ? model.rightArm : model.leftArm;
		arm.xRot = pose.xRot();
		arm.yRot = pose.yRot();
		arm.zRot = pose.zRot();
		model.body.yRot += (right ? -1.0F : 1.0F) * 0.08F * pose.weight();
		copySleeves(model);
	}

	public static boolean isBreathing(LivingEntity entity) {
		return shouldApplyTorchPresentation(
				CardinalRiteStaffPlantingClientState.isAnimating(entity),
				isActive(entity, PlayerAnimationKind.LIVING_TORCH_BREATH));
	}

	public static boolean shouldApplyTorchPresentation(boolean cardinalPlanting, boolean torchActive) {
		return torchActive && !cardinalPlanting;
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

	private record Key(int entityId, PlayerAnimationKind kind) { }
	private record Animation(InteractionHand hand, long startGameTime) { }
}
