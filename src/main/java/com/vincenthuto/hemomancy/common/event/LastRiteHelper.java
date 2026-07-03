package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Minecraft-facing adapter for {@link LastRiteRules}: the shared death-save
 * gate. Every death-prevention source (Ink Mantle Reprieve, Last-Light
 * Mantle, Silent Archon refusal, future Cryptobiosis) checks {@link #canFire}
 * before firing and calls {@link #consume} on success — so the blood may
 * refuse the return only once per shared cooldown, no matter which rite does
 * the refusing.
 *
 * <p>Arm-on-use v1: the consuming source id is recorded for tooling and
 * tooltips; a most-recently-equipped arming pass can layer on later without
 * changing call sites (see the guardrails plan Task 2).</p>
 */
public final class LastRiteHelper {

	public static final String INK_MANTLE_ID = "hemomancy:ink_mantle";
	public static final String LAST_LIGHT_ID = "hemomancy:last_light";
	public static final String SILENT_REFUSAL_ID = "hemomancy:silent_refusal";

	private static final String UNTIL_TAG = "hemomancy:last_rite_until";
	private static final String ARMED_TAG = "hemomancy:last_rite_armed";

	private LastRiteHelper() {
	}

	public static boolean canFire(Player player, String sourceId) {
		if (player == null) {
			return false;
		}
		if (!HemoServerConfig.LAST_RITE_ENABLED.get()) {
			return true;
		}
		CompoundTag data = player.getPersistentData();
		return LastRiteRules.canFire(data.getString(ARMED_TAG), sourceId, player.level().getGameTime(),
				data.getLong(UNTIL_TAG));
	}

	public static void consume(Player player, String sourceId) {
		if (player == null || !HemoServerConfig.LAST_RITE_ENABLED.get()) {
			return;
		}
		CompoundTag data = player.getPersistentData();
		data.putString(ARMED_TAG, sourceId);
		data.putLong(UNTIL_TAG, LastRiteRules.nextSharedCooldownUntil(player.level().getGameTime(),
				HemoServerConfig.LAST_RITE_SHARED_COOLDOWN_TICKS.get()));
	}
}
