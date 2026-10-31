package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

/**
 * The two damage-redirection rule sets behind {@code MorphlingDamageShare}, as plain arithmetic.
 *
 * <ul>
 * <li>Holdfast's Breakwater moves a share of an ally's damage to the anchored host, who pays blood for it, and stops
 * once the host would fall below {@link MorphlingBalanceBands#BREAKWATER_GIVER_FLOOR}.</li>
 * <li>Dead Man's Rope's Colony spreads a share of a linked creature's damage evenly across the other linked
 * creatures, none of them taken below {@link MorphlingBalanceBands#COLONY_GIVER_FLOOR}.</li>
 * </ul>
 */
public final class MorphlingDamageShareRules {
	private MorphlingDamageShareRules() {
	}

	/**
	 * Damage Breakwater moves from an ally to the host: {@code share} of it, no more than the host can take above the
	 * floor, and no more than {@code bloodAvailable} pays for.
	 */
	public static float breakwater(float allyDamage, float share, float hostHealth, float hostMaxHealth,
			double bloodAvailable, double bloodPerHp) {
		if (allyDamage <= 0.0F) return 0.0F;
		float wanted = allyDamage * share;
		float moved = MorphlingBalanceBands.givable(hostHealth, hostMaxHealth,
				MorphlingBalanceBands.BREAKWATER_GIVER_FLOOR, wanted);
		if (bloodPerHp > 0.0D) moved = (float) Math.min(moved, Math.max(0.0D, bloodAvailable) / bloodPerHp);
		return Math.max(0.0F, moved);
	}

	/**
	 * How Colony spreads {@code share} of a linked creature's {@code damage} over the others. {@code health} and
	 * {@code maxHealth} describe the other linked creatures; each takes an even part, capped at what lies above its
	 * floor. Returns each partner's part; the victim keeps {@code damage} minus their sum.
	 */
	public static float[] colony(float damage, float share, float[] health, float[] maxHealth) {
		float[] parts = new float[health.length];
		if (damage <= 0.0F || health.length == 0) return parts;
		float even = damage * share / health.length;
		for (int i = 0; i < health.length; i++) {
			parts[i] = MorphlingBalanceBands.givable(health[i], maxHealth[i], MorphlingBalanceBands.COLONY_GIVER_FLOOR, even);
		}
		return parts;
	}

	public static float sum(float[] parts) {
		float total = 0.0F;
		for (float part : parts) total += part;
		return total;
	}
}
