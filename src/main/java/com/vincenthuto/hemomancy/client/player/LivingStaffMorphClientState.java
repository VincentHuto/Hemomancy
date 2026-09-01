package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffMorphSequence;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLivingStaffMorph;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public final class LivingStaffMorphClientState {
	private static final Map<Integer, Animation> ACTIVE = new HashMap<>();
	private static ClientLevel activeLevel;

	private LivingStaffMorphClientState() {
	}

	public static void start(PacketLivingStaffMorph packet) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.level == null) return;
		if (activeLevel != minecraft.level) ACTIVE.clear();
		activeLevel = minecraft.level;
		ACTIVE.put(packet.casterEntityId(), new Animation(packet.beforeMain(), packet.afterMain(),
				packet.beforeOff(), packet.afterOff(), minecraft.level.getGameTime()));
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
		ACTIVE.entrySet().removeIf(entry -> gameTime - entry.getValue().startGameTime()
				>= entry.getValue().durationTicks());
	}

	public static Animation animation(LivingEntity entity) {
		return entity == null ? null : ACTIVE.get(entity.getId());
	}

	public static boolean isAnimating(LivingEntity entity) {
		return animation(entity) != null;
	}

	public static boolean affectsHand(LivingEntity entity, InteractionHand hand) {
		Animation animation = animation(entity);
		return animation != null && animation.affects(hand);
	}

	public static boolean affectsArm(LivingEntity entity, HumanoidArm arm) {
		if (entity == null) return false;
		InteractionHand hand = arm == entity.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
		return affectsHand(entity, hand);
	}

	public static float elapsed(LivingEntity entity, float partialTick) {
		Minecraft minecraft = Minecraft.getInstance();
		Animation animation = animation(entity);
		if (animation == null || minecraft.level == null) return Float.MAX_VALUE;
		return minecraft.level.getGameTime() - animation.startGameTime() + partialTick;
	}

	public record Animation(ItemStack beforeMain, ItemStack afterMain, ItemStack beforeOff, ItemStack afterOff,
			long startGameTime) {
		public Animation {
			beforeMain = beforeMain.copy();
			afterMain = afterMain.copy();
			beforeOff = beforeOff.copy();
			afterOff = afterOff.copy();
		}

		public boolean affects(InteractionHand hand) {
			return !ItemStack.isSameItemSameComponents(before(hand), after(hand));
		}

		public ItemStack before(InteractionHand hand) {
			return hand == InteractionHand.MAIN_HAND ? beforeMain : beforeOff;
		}

		public ItemStack after(InteractionHand hand) {
			return hand == InteractionHand.MAIN_HAND ? afterMain : afterOff;
		}

		public boolean hasOutgoing() {
			return LivingStaffMorphSequence.hasChangedStack(affects(InteractionHand.MAIN_HAND),
					!beforeMain.isEmpty(), affects(InteractionHand.OFF_HAND), !beforeOff.isEmpty());
		}

		public boolean hasIncoming() {
			return LivingStaffMorphSequence.hasChangedStack(affects(InteractionHand.MAIN_HAND),
					!afterMain.isEmpty(), affects(InteractionHand.OFF_HAND), !afterOff.isEmpty());
		}

		public int durationTicks() {
			return LivingStaffMorphSequence.durationTicks(hasOutgoing(), hasIncoming());
		}
	}
}
