package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import com.vincenthuto.hemomancy.common.event.BorrowedBloodRules;
import com.vincenthuto.hemomancy.config.HemoServerConfig;

import net.minecraft.world.entity.player.Player;

/**
 * Minecraft-facing adapter for {@link BorrowedBloodRules}: a small per-player
 * reserve of blood lent by the world rather than owned by the body. Current
 * writers: Blood Lust overkill lifesteal (ArmorSetBonusHandler) and — once the
 * morphling reframe lands — Deadman's Purse feed-banking. Current reader:
 * emergency cover when a manipulation cast would otherwise fail for lack of
 * blood (BloodManipulation).
 *
 * <p>Stored on the player's persistent data, matching the tag pattern of the
 * other guardrail helpers.</p>
 */
public final class BorrowedBloodReserve {

	private static final String BORROWED_TAG = "hemomancy:borrowed_blood";

	private BorrowedBloodReserve() {
	}

	public static double get(Player player) {
		return player == null ? 0.0D : player.getPersistentData().getDouble(BORROWED_TAG);
	}

	/** Deposit into the reserve; returns the amount actually accepted. */
	public static double deposit(Player player, double amount) {
		if (player == null || player.level().isClientSide()) {
			return 0.0D;
		}
		if (!HemoServerConfig.BORROWED_BLOOD_ENABLED.get()) {
			return 0.0D;
		}
		double current = get(player);
		double accepted = BorrowedBloodRules.acceptedDeposit(current, amount,
				HemoServerConfig.BORROWED_BLOOD_CAP.get());
		if (accepted > 0.0D) {
			player.getPersistentData().putDouble(BORROWED_TAG, current + accepted);
		}
		return accepted;
	}

	/** Drain up to the requested amount; returns what the reserve covered. */
	public static double drainToCover(Player player, double requested) {
		if (player == null || player.level().isClientSide()) {
			return 0.0D;
		}
		double current = get(player);
		double drained = BorrowedBloodRules.drained(current, requested);
		if (drained > 0.0D) {
			player.getPersistentData().putDouble(BORROWED_TAG, current - drained);
		}
		return drained;
	}
}
