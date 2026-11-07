package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

/**
 * The Tithe (design sections 4 and 7): every cast or vein feed a challenger makes within 24 blocks of him adds its
 * cost to their debt (x1.5 under the Litany deed). Tithe Reckoning collects it as damage, min(16, 3 + debt / 80), from
 * the most indebted challenger once they owe 150; held Projection along the chain pays 20 a tick off it. JVM-tested.
 */
public final class HemorathTitheRules {
	public static final double RANGE = 24.0D;
	public static final float LITANY = 1.5F;
	public static final float PAY_RATE = 20.0F;
	public static final float MIN_DEBT = 150.0F;
	public static final float BASE_DAMAGE = 3.0F;
	public static final float DEBT_PER_DAMAGE = 80.0F;
	public static final float MAX_DAMAGE = 16.0F;
	public static final int BASE_WEIGHT = 4;
	public static final float DEBT_PER_WEIGHT = 50.0F;
	public static final int MAX_WEIGHT = 30;
	public static final float DEBT_PER_LINK = 100.0F;
	public static final int MAX_LINKS = 10;
	/** The debt a player's HUD shows at full (the meter's per-player secondary maximum). */
	public static final float DEBT_SHOWN = 1000.0F;

	private HemorathTitheRules() {
	}

	public static float accrue(float debt, float cost, boolean litany) {
		if (cost <= 0.0F) return debt;
		return debt + (litany ? cost * LITANY : cost);
	}

	/** Damage on an unpaid commit; none at zero debt. */
	public static float damage(float debt) {
		if (debt <= 0.0F) return 0.0F;
		return Math.min(MAX_DAMAGE, BASE_DAMAGE + debt / DEBT_PER_DAMAGE);
	}

	/** The pick weight; 0 below MIN_DEBT. */
	public static int weight(float debt) {
		if (debt < MIN_DEBT) return 0;
		return Math.min(MAX_WEIGHT, BASE_WEIGHT + (int) (debt / DEBT_PER_WEIGHT));
	}

	/** Links drawn on the chain and round the target's waist. */
	public static int links(float debt) {
		return Math.max(0, Math.min(MAX_LINKS, (int) (debt / DEBT_PER_LINK)));
	}

	/** One tick of payment: what leaves the debt (never more than the debt, the rate or what was paid). */
	public static float pay(float debt, float paid) {
		return Math.max(0.0F, Math.min(debt, Math.min(PAY_RATE, paid)));
	}
}
