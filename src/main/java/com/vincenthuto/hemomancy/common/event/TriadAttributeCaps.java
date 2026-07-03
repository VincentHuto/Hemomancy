package com.vincenthuto.hemomancy.common.event;

/**
 * Shared budget caps for stacking triad attribute contributions (armor set
 * bonuses + morphling passives + scar synergy). Not a generic attribute
 * interceptor: known grant sites pre-clamp their own contribution against
 * what the other layers already granted, so a single layer is never affected
 * and only stacks are disciplined.
 *
 * <p>No Minecraft imports on purpose. Current wired site: the Chitinite
 * toughness modifier in ArmorSetBonusHandler; morphling passives and scar
 * synergy join on the dev-machine pass (see the guardrails plan Task 4).</p>
 */
public final class TriadAttributeCaps {

	public static final double MAX_TRIAD_TOUGHNESS_BONUS = 4.0D;
	public static final double MAX_TRIAD_MOVE_SPEED_BONUS = 0.30D;

	private TriadAttributeCaps() {
	}

	/**
	 * Clamp a layer's requested contribution so the triad total stays within
	 * the cap. Never negative; a lone layer under the cap passes through
	 * unchanged.
	 */
	public static double clampContribution(double othersContribution, double requested, double cap) {
		if (requested <= 0.0D) {
			return 0.0D;
		}
		double room = Math.max(0.0D, cap - Math.max(0.0D, othersContribution));
		return Math.min(requested, room);
	}

	public static double clampToughness(double othersContribution, double requested) {
		return clampContribution(othersContribution, requested, MAX_TRIAD_TOUGHNESS_BONUS);
	}

	public static double clampMoveSpeed(double othersContribution, double requested) {
		return clampContribution(othersContribution, requested, MAX_TRIAD_MOVE_SPEED_BONUS);
	}
}
