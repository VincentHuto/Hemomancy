package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class BloodAbsorptionChannelRules {
	private static final double BASE_UNLOCKED_MOVEMENT_MULTIPLIER = 0.25D;
	private static final double MOVEMENT_RECOVERY_PER_LEVEL = 0.15D;
	private static final double VANILLA_USE_ITEM_MOVEMENT_MULTIPLIER = 0.2D;
	/**
	 * Ticks between living-target drain pulses. A hit leaves its target 20 ticks of hurt cooldown, during which a hit no
	 * bigger than the last is refused while more than 10 remain, so any pulse faster than 10 ticks lands less often,
	 * not more (8 ticks lands every 16, 6 and 4 every 12). The pulse stays at 10 and the cadence skills raise the amount.
	 */
	public static final int LIVING_TARGET_PULSE_INTERVAL = 10;
	/** The pulse each cadence level used to promise, kept as the measure of how much more each level drains. */
	private static final int PROMISED_PULSE_REDUCTION_PER_CADENCE_LEVEL = 2;
	private static final int MIN_PROMISED_PULSE_INTERVAL = 4;
	private static final int BASE_WEAKNESS_THRESHOLD = 100;
	private static final int BASE_NAUSEA_THRESHOLD = 180;
	private static final int BASE_BLOOD_POISONING_THRESHOLD = 260;
	private static final int STRAIN_THRESHOLD_BONUS_PER_LEVEL = 20;
	/** Strain eased per tick spent not drinking from a living creature, including ticks out of the channel. */
	private static final int STRAIN_DECAY_PER_TICK = 2;

	private BloodAbsorptionChannelRules() {
	}

	public enum StrainTier {
		NONE,
		WEAKNESS,
		NAUSEA,
		BLOOD_POISONING
	}

	public static boolean canStartChannel(boolean activeBlood) {
		return activeBlood;
	}

	/**
	 * Whether a living creature may be drawn from: active blood is enough. A full body still draws Temper; the blood
	 * past its cap is lost, or goes to a worn gourd with Sated Siphon (CQ-9, NQ-04-10).
	 */
	public static boolean canDrainLivingTarget(boolean activeBlood) {
		return activeBlood;
	}

	public static double movementMultiplier(int movementUnlockLevel, int slowdownReductionLevel,
			boolean freeMovementUnlocked) {
		if (freeMovementUnlocked) {
			return 1.0D;
		}
		if (movementUnlockLevel <= 0) {
			return 0.0D;
		}
		return Math.min(1.0D, BASE_UNLOCKED_MOVEMENT_MULTIPLIER
				+ Math.max(0, slowdownReductionLevel) * MOVEMENT_RECOVERY_PER_LEVEL);
	}

	public static double clientInputMultiplier(double desiredMovementMultiplier) {
		double desired = Math.max(0.0D, desiredMovementMultiplier);
		if (desired <= 0.0D) {
			return 0.0D;
		}
		return desired / VANILLA_USE_ITEM_MOVEMENT_MULTIPLIER;
	}

	/** Ticks between living-target drain pulses, bare or with the Staff, at every cadence level. */
	public static int livingTargetPulseInterval() {
		return LIVING_TARGET_PULSE_INTERVAL;
	}

	/**
	 * How much a pulse drains at {@code cadenceLevel} (0 to 3) against the untrained pulse: the drain per second the
	 * old faster pulses promised (10, 8, 6 and 4 ticks), now delivered at the one 10-tick pulse. 1, 1.25, 1.67, 2.5.
	 */
	public static double cadenceDrainMultiplier(int cadenceLevel) {
		int promised = Math.max(MIN_PROMISED_PULSE_INTERVAL, LIVING_TARGET_PULSE_INTERVAL
				- Math.max(0, cadenceLevel) * PROMISED_PULSE_REDUCTION_PER_CADENCE_LEVEL);
		return (double) LIVING_TARGET_PULSE_INTERVAL / promised;
	}

	/**
	 * Absorption strain: the body's answer to drinking from living creatures without a pause. It counts one per tick
	 * of drinking (a pulse that drew blood covers its whole interval) and eases {@link #STRAIN_DECAY_PER_TICK} per tick
	 * otherwise. {@code lastTick} is when it was last worked out and {@code drinkingUntil} the last tick the latest
	 * draining pulse covers.
	 */
	public record Strain(int ticks, long lastTick, long drinkingUntil) {
		public static final Strain NONE = new Strain(0, 0L, 0L);
	}

	/** {@code current} carried forward to game time {@code now}, with a draining pulse at {@code now} if one landed. */
	public static Strain nextStrain(Strain current, long now, boolean drainedLivingTarget) {
		long accounted = Math.max(current.lastTick(), current.drinkingUntil());
		long through = drainedLivingTarget ? now - 1 : now;
		long uncovered = current.ticks() <= 0 ? 0L : Math.max(0L, through - accounted);
		long eased = Math.min((long) Integer.MAX_VALUE, uncovered * STRAIN_DECAY_PER_TICK);
		int ticks = (int) Math.max(0L, current.ticks() - eased);
		long drinkingUntil = current.drinkingUntil();
		if (drainedLivingTarget) {
			ticks += LIVING_TARGET_PULSE_INTERVAL;
			drinkingUntil = now + LIVING_TARGET_PULSE_INTERVAL - 1;
		}
		return new Strain(ticks, now, drinkingUntil);
	}

	public static StrainTier strainTier(int strainTicks, int toleranceLevel) {
		int safeTicks = Math.max(0, strainTicks);
		int toleranceBonus = Math.max(0, toleranceLevel) * STRAIN_THRESHOLD_BONUS_PER_LEVEL;
		if (safeTicks >= BASE_BLOOD_POISONING_THRESHOLD + toleranceBonus) {
			return StrainTier.BLOOD_POISONING;
		}
		if (safeTicks >= BASE_NAUSEA_THRESHOLD + toleranceBonus) {
			return StrainTier.NAUSEA;
		}
		if (safeTicks >= BASE_WEAKNESS_THRESHOLD + toleranceBonus) {
			return StrainTier.WEAKNESS;
		}
		return StrainTier.NONE;
	}
}
