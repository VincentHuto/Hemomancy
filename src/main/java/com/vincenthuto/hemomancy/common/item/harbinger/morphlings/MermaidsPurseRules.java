package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.List;

/** Mermaid's Purse's first-pass numbers (spec, "Mermaid's Purse - The Shared Wake"). */
public final class MermaidsPurseRules {
	public static final int PROC_TICKS = 40;
	private static final double[] SIPHON = { 1.0D, 1.5D, 2.0D, 2.5D, 3.0D, 3.5D };
	private static final double[] SWIM = { 0.0D, 0.05D, 0.10D, 0.15D, 0.20D, 0.25D };
	/** The full Prismatic set's ceiling; the strain never grants more. */
	public static final double SWIM_CAP = 0.35D;
	public static final double SPRINT_SWIM_BANK_MULTIPLIER = 1.5D;

	public static final float SCHOOL_TRIGGER = 0.3F;
	public static final double SCHOOL_RANGE = 8.0D;
	public static final double SCHOOL_COST = 200.0D;
	public static final int SCHOOL_COOLDOWN = 10 * 20;

	public static final double SAIL_ALLY_RANGE = 6.0D;
	public static final int BILL_WINDOW = 2 * 20;
	public static final int BILL_LIMIT = 8 * 20;
	public static final float BILL_DAMAGE = 4.0F;

	public static final double WAKE_COST = 450.0D;
	public static final int WAKE_TICKS = 20 * 20;
	public static final double WAKE_RANGE = 12.0D;
	public static final float WAKE_HEAL_CAP = 2.5F;
	public static final double WAKE_BANK_CAP = 35.0D;
	public static final int WAKE_COOLDOWN = 60 * 20;
	public static final int WAKE_STRAIN = 15 * 20;

	public static final List<MorphlingPower> POWERS = List.of(
			MorphlingPower.passive("purse_siphon"),
			MorphlingPower.conditional("feed_banking", 2, 0),
			MorphlingPower.reactive("schools_share", 3, SCHOOL_COOLDOWN),
			MorphlingPower.conditional("raise_the_sail", 4, BILL_LIMIT),
			MorphlingPower.primal("shared_wake", WAKE_COST, WAKE_COOLDOWN));

	private MermaidsPurseRules() {
	}

	public static double siphon(int stage) {
		return SIPHON[SunburstRules.clamp(stage)];
	}

	/** Swim speed bonus while submerged; home raises it by half, never past {@link #SWIM_CAP}. */
	public static double swimBonus(int stage, boolean home) {
		double base = SWIM[SunburstRules.clamp(stage)];
		return Math.min(SWIM_CAP, home ? base * 1.5D : base);
	}

	/**
	 * The strain's swim bonus under another multiplier already on the host (the full Prismatic or Phantasmal Bloodlust
	 * set's x1.35): the strain only tops the total up to {@code 1 + SWIM_CAP}, so the set and the strain never stack
	 * past +35% together.
	 */
	public static double swimBonus(int stage, boolean home, double otherMultiplier) {
		double headroom = (1.0D + SWIM_CAP) / Math.max(1.0D, otherMultiplier) - 1.0D;
		return Math.max(0.0D, Math.min(swimBonus(stage, home), headroom));
	}

	/** School's Share heal: 6 at Mature, 9 at Apex, 12 at Primal. */
	public static float schoolHeal(int stage) {
		return stage < 3 ? 0.0F : 6.0F + 3.0F * (Math.min(stage, 5) - 3);
	}

	/** Feed Banking deposit for a melee hit, before the reserve's own cap. */
	public static double bankDeposit(int stage, float damage, boolean sprintSwimming, boolean billStrike) {
		double deposit = MorphlingFeedBankRules.attackDeposit(stage, damage);
		if (sprintSwimming) deposit *= SPRINT_SWIM_BANK_MULTIPLIER;
		if (billStrike) deposit *= 2.0D;
		return deposit;
	}
}
