package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.rite.CardinalRiteStaffPlantingClientState;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PlayerAnimationKind;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Client animation instances keyed by player id and animation kind (the Living Torch breath). The poses that read them
 * apply in {@link PlayerAnimationRegistry}'s order.
 */
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

	public static boolean isBreathing(LivingEntity entity) {
		return shouldApplyTorchPresentation(
				CardinalRiteStaffPlantingClientState.isAnimating(entity),
				isActive(entity, PlayerAnimationKind.LIVING_TORCH_BREATH));
	}

	public static boolean shouldApplyTorchPresentation(boolean cardinalPlanting, boolean torchActive) {
		return torchActive && !cardinalPlanting;
	}

	private record Key(int entityId, PlayerAnimationKind kind) { }
	private record Animation(InteractionHand hand, long startGameTime) { }
}
