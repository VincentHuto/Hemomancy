package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CuttlefishMorphlingItem;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Minecraft-facing adapter for {@link LastRiteRules}: the shared death-save
 * gate. Every death-prevention source (Ink Mantle Reprieve, Last-Light
 * Mantle, Silent Archon refusal, future Cryptobiosis) checks {@link #canFire}
 * before firing and calls {@link #consume} on success.
 */
public final class LastRiteHelper {

	public static final String INK_MANTLE_ID = "hemomancy:ink_mantle";
	public static final String LAST_LIGHT_ID = "hemomancy:last_light";
	public static final String SILENT_REFUSAL_ID = "hemomancy:silent_refusal";

	private LastRiteHelper() {
	}

	public static boolean canFire(Player player, String sourceId) {
		if (player == null) {
			return false;
		}
		if (!HemoServerConfig.LAST_RITE_ENABLED.get()) {
			return true;
		}
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		return LastRiteRules.canFire(state.getLastRiteArmedSource(), sourceId, player.level().getGameTime(),
				state.getLastRiteCooldownUntil());
	}

	public static void consume(Player player, String sourceId) {
		if (player == null || !HemoServerConfig.LAST_RITE_ENABLED.get()) {
			return;
		}
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		if (isValidSourceId(sourceId)) {
			state.setLastRiteArmedSource(sourceId);
		}
		state.setLastRiteCooldownUntil(LastRiteRules.nextSharedCooldownUntil(player.level().getGameTime(),
				HemoServerConfig.LAST_RITE_SHARED_COOLDOWN_TICKS.get()));
	}

	public static void arm(Player player, String sourceId) {
		if (player == null || player.level().isClientSide() || !isValidSourceId(sourceId)) {
			return;
		}
		HemoCapabilityAccess.getPowerGuardrails(player).setLastRiteArmedSource(sourceId);
	}

	public static void clearIfArmed(Player player, String sourceId) {
		if (player == null || player.level().isClientSide() || !isValidSourceId(sourceId)) {
			return;
		}
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		if (state.isLastRiteArmed(sourceId)) {
			state.clearLastRiteArmedSource();
		}
	}

	public static boolean hasArmedSource(Player player) {
		if (player == null) {
			return false;
		}
		return !HemoCapabilityAccess.getPowerGuardrails(player).getLastRiteArmedSource().isEmpty();
	}

	public static String getArmedSource(Player player) {
		return player == null ? "" : HemoCapabilityAccess.getPowerGuardrails(player).getLastRiteArmedSource();
	}

	public static void armForMorphling(Player player, ItemStack stack) {
		if (player == null || player.level().isClientSide()) {
			return;
		}
		String sourceId = sourceIdForMorphling(stack);
		if (isValidSourceId(sourceId)) {
			arm(player, sourceId);
		} else {
			clearMorphlingRites(player);
		}
	}

	public static void armForMorphlingIfUnarmed(Player player, ItemStack stack) {
		if (!hasArmedSource(player)) {
			armForMorphling(player, stack);
		}
	}

	public static void clearMorphlingRites(Player player) {
		clearIfArmed(player, INK_MANTLE_ID);
		clearIfArmed(player, LAST_LIGHT_ID);
	}

	private static String sourceIdForMorphling(ItemStack stack) {
		if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof CuttlefishMorphlingItem)) {
			return "";
		}
		if (MorphlingItem.isPrimal(stack)) {
			return LAST_LIGHT_ID;
		}
		if (MorphlingItem.getMaturityLevel(stack) >= 4) {
			return INK_MANTLE_ID;
		}
		return "";
	}

	private static boolean isValidSourceId(String sourceId) {
		return sourceId != null && !sourceId.isEmpty();
	}
}
