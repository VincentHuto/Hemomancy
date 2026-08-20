package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class BloodAbsorptionChannelRules {
	private static final double BASE_UNLOCKED_MOVEMENT_MULTIPLIER = 0.25D;
	private static final double MOVEMENT_RECOVERY_PER_LEVEL = 0.15D;
	private static final double VANILLA_USE_ITEM_MOVEMENT_MULTIPLIER = 0.2D;
	private static final int BASE_LIVING_TARGET_PULSE_INTERVAL = 10;
	private static final int PULSE_REDUCTION_PER_CADENCE_LEVEL = 2;
	private static final int MIN_LIVING_TARGET_PULSE_INTERVAL = 4;
	private static final int BASE_WEAKNESS_THRESHOLD = 100;
	private static final int BASE_NAUSEA_THRESHOLD = 180;
	private static final int BASE_BLOOD_POISONING_THRESHOLD = 260;
	private static final int STRAIN_THRESHOLD_BONUS_PER_LEVEL = 20;
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

	public static boolean canDrainLivingTarget(boolean activeBlood, boolean bloodFull,
			boolean satedSiphon) {
		return activeBlood && (!bloodFull || satedSiphon);
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

	public static int livingTargetPulseInterval(boolean usingLivingStaff, int cadenceLevel) {
		int interval = BASE_LIVING_TARGET_PULSE_INTERVAL
				- Math.max(0, cadenceLevel) * PULSE_REDUCTION_PER_CADENCE_LEVEL;
		return Math.max(MIN_LIVING_TARGET_PULSE_INTERVAL, interval);
	}

	public static int nextStrainTicks(int currentTicks, boolean drainedLivingTarget) {
		if (drainedLivingTarget) {
			return Math.max(0, currentTicks) + 1;
		}
		return Math.max(0, currentTicks - STRAIN_DECAY_PER_TICK);
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
