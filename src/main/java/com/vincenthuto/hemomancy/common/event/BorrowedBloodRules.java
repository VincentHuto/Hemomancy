package com.vincenthuto.hemomancy.common.event;

/**
 * Pure math for the borrowed-blood reserve — the shared handshake pool that
 * Blood Lust overkill lifesteal and morphling feed-banking deposit into, and
 * that emergency casting drains before the player's own volume runs dry.
 * Storage semantics only; the manipulation-discount spend side is a later
 * synergy phase.
 *
 * <p>No Minecraft imports on purpose — adapter is BorrowedBloodReserve.</p>
 */
public final class BorrowedBloodRules {

	public static final double DEFAULT_CAP = 500.0D;
	/** Conversion rate for wasted lifesteal: blood banked per overkill health point. */
	public static final double BLOOD_PER_OVERKILL_HEALTH = 25.0D;

	private BorrowedBloodRules() {
	}

	/** Portion of a deposit the reserve accepts, respecting the cap. */
	public static double acceptedDeposit(double current, double amount, double cap) {
		if (amount <= 0.0D || cap <= 0.0D) {
			return 0.0D;
		}
		double room = Math.max(0.0D, cap - Math.max(0.0D, current));
		return Math.min(amount, room);
	}

	/** Portion of a drain request the reserve can actually cover. */
	public static double drained(double current, double requested) {
		if (requested <= 0.0D) {
			return 0.0D;
		}
		return Math.min(Math.max(0.0D, current), requested);
	}

	public static double castDeficitToCover(boolean nonBloodGatesPassed, double currentBlood, double effectiveCost) {
		if (!nonBloodGatesPassed || currentBlood > effectiveCost) {
			return 0.0D;
		}
		return Math.max(0.0D, effectiveCost - currentBlood + 1.0D);
	}

	/**
	 * Healing beyond what the target could absorb — the overkill slice of a
	 * lifesteal heal that would otherwise vanish.
	 */
	public static float overkillHealing(float healthBefore, float maxHealth, float healAmount) {
		if (healAmount <= 0.0F) {
			return 0.0F;
		}
		float missing = Math.max(0.0F, maxHealth - healthBefore);
		return Math.max(0.0F, healAmount - missing);
	}
}
