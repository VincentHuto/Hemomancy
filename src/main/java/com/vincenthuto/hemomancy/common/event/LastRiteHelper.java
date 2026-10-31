package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Minecraft-facing adapter for {@link LastRiteRules}: the shared death-save
 * gate. Every death-prevention source (Ink Cap's Ink Reprieve, Hushbell's
 * Cryptobiosis, Silent Archon refusal) checks {@link #canFire} before firing
 * and calls {@link #consume} on success. Which Morphling arms which source,
 * and at what stage, is a column of {@link MorphlingStrains}.
 */
public final class LastRiteHelper {

	public static final String INK_MANTLE_ID = "hemomancy:ink_mantle";
	public static final String SILENT_REFUSAL_ID = "hemomancy:silent_refusal";
	public static final String CRYPTOBIOSIS_ID = "hemomancy:cryptobiosis";
	public static final String PHOENIX_DEBT_ID = "hemomancy:phoenix_debt";

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
		clearIfArmed(player, CRYPTOBIOSIS_ID);
	}

	private static String sourceIdForMorphling(ItemStack stack) {
		return MorphlingStrains.lastRiteFor(stack);
	}

	private static boolean isValidSourceId(String sourceId) {
		return sourceId != null && !sourceId.isEmpty();
	}
}
