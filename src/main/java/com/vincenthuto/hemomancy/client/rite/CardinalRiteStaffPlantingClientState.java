package com.vincenthuto.hemomancy.client.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRitePlantingSequence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/** Client-side animation instances keyed by the planting player's entity id. */
public final class CardinalRiteStaffPlantingClientState {
	private static final Map<Integer, Animation> ACTIVE = new HashMap<>();
	private static ClientLevel activeLevel;

	private CardinalRiteStaffPlantingClientState() {
	}

	public static void start(int casterEntityId, BlockPos focus, ItemStack staff) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;
		if (activeLevel != minecraft.level) ACTIVE.clear();
		activeLevel = minecraft.level;
		ACTIVE.put(casterEntityId, new Animation(
				focus.immutable(), staff.copyWithCount(1), minecraft.level.getGameTime()));
	}

	public static void tick() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) {
			ACTIVE.clear();
			activeLevel = null;
			return;
		}
		if (activeLevel != minecraft.level) {
			ACTIVE.clear();
			activeLevel = minecraft.level;
			return;
		}
		long gameTime = minecraft.level.getGameTime();
		ACTIVE.entrySet().removeIf(entry ->
				gameTime - entry.getValue().startGameTime >= CardinalRitePlantingSequence.DURATION_TICKS);
	}

	public static boolean isAnimating(LivingEntity entity) {
		return entity != null && ACTIVE.containsKey(entity.getId());
	}

	public static Animation animation(LivingEntity entity) {
		return entity == null ? null : ACTIVE.get(entity.getId());
	}

	public static float elapsed(LivingEntity entity, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		Animation animation = animation(entity);
		if (animation == null || minecraft.level == null) return CardinalRitePlantingSequence.DURATION_TICKS;
		return minecraft.level.getGameTime() - animation.startGameTime + partialTick;
	}

	public static void applyThirdPersonPose(LivingEntity entity, HumanoidModel<?> model, float partialTick) {
		if (!(entity instanceof Player) || !isAnimating(entity)) return;
		float elapsed = elapsed(entity, partialTick);
		float windup = CardinalRitePlantingSequence.windupProgress(elapsed);
		float strike = CardinalRitePlantingSequence.strikeProgress(elapsed);
		float recovery = CardinalRitePlantingSequence.recoveryProgress(elapsed);
		float plantedHold = strike * (1.0F - recovery * 0.35F);

		model.body.xRot = 0.12F + plantedHold * 0.42F;
		model.head.xRot -= windup * 0.12F;
		model.rightArm.xRot = -1.05F - windup * 1.35F + plantedHold * 2.05F;
		model.leftArm.xRot = model.rightArm.xRot;
		model.rightArm.yRot = -0.32F;
		model.leftArm.yRot = 0.32F;
		model.rightArm.zRot = 0.16F;
		model.leftArm.zRot = -0.16F;
		if (model instanceof PlayerModel<?> playerModel) {
			playerModel.rightSleeve.copyFrom(model.rightArm);
			playerModel.leftSleeve.copyFrom(model.leftArm);
		}
	}

	public record Animation(BlockPos focus, ItemStack staff, long startGameTime) {
		public Animation {
			staff = staff.copyWithCount(1);
		}
	}
}
