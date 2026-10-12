package com.vincenthuto.hemomancy.common.manipulation;

import java.util.OptionalDouble;

import net.minecraft.util.Mth;

public final class ManipulationCastingRules {
	/** A charged manipulation released below this fraction of its charge fizzles before payment. */
	public static final float MIN_CHARGE_FRACTION = 0.25F;
	/** Partial charges below this fraction do not count toward the family's mastery uses. */
	public static final float MASTERY_USE_CHARGE_FRACTION = 0.5F;
	/** Minimum server tolerance, in ticks, between the observed key-down and the client's reported charge. */
	public static final int CHARGE_CLOCK_SLACK_TICKS = 2;
	/** Proportional tolerance so honest full holds survive ordinary TPS loss and packet jitter. */
	public static final double CHARGE_CLOCK_SLACK_FRACTION = 0.15D;
	/** Grace beyond twice the required charge before an observed key-down is too old to credit. */
	public static final int CHARGE_CLOCK_STALE_GRACE_TICKS = 20;

	private ManipulationCastingRules() {
	}

	public static float chargeFraction(float chargeTicks, int requiredChargeTicks) {
		return requiredChargeTicks <= 0 || !Float.isFinite(chargeTicks) ? 0.0F
				: Mth.clamp(chargeTicks / requiredChargeTicks, 0.0F, 1.0F);
	}

	public static boolean meetsChargeFloor(float chargeTicks, int requiredChargeTicks) {
		return chargeFraction(chargeTicks, requiredChargeTicks) >= MIN_CHARGE_FRACTION;
	}

	/** Charge-scaled cost, or empty when the charge is below {@link #MIN_CHARGE_FRACTION} and the cast is refused. */
	public static OptionalDouble chargedCost(double modifiedCost, float chargeTicks, int requiredChargeTicks) {
		if (!meetsChargeFloor(chargeTicks, requiredChargeTicks)) return OptionalDouble.empty();
		return OptionalDouble.of(modifiedCost * chargeFraction(chargeTicks, requiredChargeTicks));
	}

	/** Scale applied to alignment, skill points and mastery for a successful cast; only CHARGED casts scale. */
	public static float rewardScale(EnumManipulationType type, float chargeTicks, int requiredChargeTicks) {
		return type == EnumManipulationType.CHARGED ? chargeFraction(chargeTicks, requiredChargeTicks) : 1.0F;
	}

	public static boolean earnsMasteryUse(float rewardScale) {
		return rewardScale >= MASTERY_USE_CHARGE_FRACTION;
	}

	public static int scaledSkillPoints(int points, float rewardScale) {
		return Math.round(points * Mth.clamp(rewardScale, 0.0F, 1.0F));
	}

	/**
	 * Clamps client-reported charge to what the server watched: the ticks since the observed key-down plus a
	 * tolerance of {@code max(CHARGE_CLOCK_SLACK_TICKS, ceil(CHARGE_CLOCK_SLACK_FRACTION × elapsed))}. With no
	 * observed key-down the charge is zero.
	 */
	public static float clampReportedCharge(float clientTicks, long startedAt, long now) {
		if (startedAt < 0 || !Float.isFinite(clientTicks)) return 0.0F;
		return Math.max(0.0F, Math.min(clientTicks, allowedChargeTicks(Math.max(0L, now - startedAt))));
	}

	/**
	 * {@link #clampReportedCharge(float, long, long)} for a manipulation needing {@code requiredChargeTicks}; a stale
	 * key-down (see {@link #chargeStartStale}) counts as none, so it credits nothing.
	 */
	public static float clampReportedCharge(float clientTicks, long startedAt, long now, int requiredChargeTicks) {
		if (chargeStartStale(startedAt, now, requiredChargeTicks)) return 0.0F;
		return clampReportedCharge(clientTicks, startedAt, now);
	}

	/**
	 * Whether an observed key-down is too old to belong to the cast being reported: more than
	 * {@code 2 × requiredChargeTicks + CHARGE_CLOCK_STALE_GRACE_TICKS} ticks ago. Without this a key-down that a
	 * refused cast left behind could credit every tick since it to a much later cast.
	 */
	public static boolean chargeStartStale(long startedAt, long now, int requiredChargeTicks) {
		return startedAt >= 0
				&& now - startedAt > 2L * Math.max(0, requiredChargeTicks) + CHARGE_CLOCK_STALE_GRACE_TICKS;
	}

	public static long allowedChargeTicks(long elapsed) {
		return elapsed + Math.max(CHARGE_CLOCK_SLACK_TICKS, (long) Math.ceil(CHARGE_CLOCK_SLACK_FRACTION * elapsed));
	}
}
