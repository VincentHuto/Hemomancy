package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class UnstainedAccessRules {
	public static final int NOVITIATE_MASK = 0b1_1111 << 9;

	private UnstainedAccessRules() {}

	public static UnstainedPhase phase(IUnstainedProgress progress) {
		if (progress.hasClarityUnlocked()) return UnstainedPhase.PLEDGED;
		if (progress.isBaselineRestored()) return UnstainedPhase.CLEANSED_UNPLEDGED;
		if (progress.hasBegunPurification()) {
			return progress.isPurified() ? UnstainedPhase.CURE_READY : UnstainedPhase.PURIFYING;
		}
		if (((progress.getAcceptedObservances() | progress.getClaimedObservances()) & NOVITIATE_MASK) != 0) {
			return UnstainedPhase.NOVITIATE;
		}
		return UnstainedPhase.OUTSIDER;
	}

	public static float baptismStartingPurity(int degree, boolean severedFounder) {
		return severedFounder ? 0f : Math.max(0f, 50f - Math.max(0, degree) * 10f);
	}

	public static boolean hasCompletedNovitiateVows(int claimedObservances) {
		return (claimedObservances & NOVITIATE_MASK) == NOVITIATE_MASK;
	}

	public static int completedNovitiateVows(int claimedObservances) {
		return Integer.bitCount(claimedObservances & NOVITIATE_MASK);
	}

	public static boolean mayGainBloodProgress(UnstainedPhase phase) {
		return phase == UnstainedPhase.OUTSIDER || phase == UnstainedPhase.NOVITIATE;
	}

	public static boolean mayUseBloodPowers(UnstainedPhase phase) {
		return phase != UnstainedPhase.CLEANSED_UNPLEDGED && phase != UnstainedPhase.PLEDGED;
	}

	public static boolean blocksHarbingerProgress(IUnstainedProgress progress) {
		UnstainedPhase phase = phase(progress);
		return phase == UnstainedPhase.PURIFYING || phase == UnstainedPhase.CURE_READY
				|| phase == UnstainedPhase.CLEANSED_UNPLEDGED || phase == UnstainedPhase.PLEDGED;
	}

	public static boolean blocksKnownBloodPowerUse(IUnstainedProgress progress) {
		return !mayUseBloodPowers(phase(progress));
	}

	public static boolean bypassesUnstainedLevelGate(String recipePath, IUnstainedProgress progress) {
		if (recipePath == null) return false;
		if (recipePath.equals("cardinal_rite/lethean_baptism")
				|| recipePath.equals("cardinal_rite/closed_vein")
				|| recipePath.equals("cardinal_rite/severed_covenant")
				|| recipePath.equals("cardinal_rite/clarity_ascension")) return true;
		int claimed = progress.getClaimedObservances();
		if (recipePath.equals("blood_structure/pallid_retort")) return (claimed & (1 << 9)) != 0;
		if (recipePath.equals("cardinal_rite/still_waters")
				|| recipePath.equals("cardinal_rite/pale_consecration")) return (claimed & (1 << 12)) != 0;
		return false;
	}
}
