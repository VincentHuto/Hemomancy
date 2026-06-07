package com.vincenthuto.hemomancy.common.item.harbinger;

public final class QliphothPomeRules {
	public static final long BASE_EMPOWERMENT_DURATION_TICKS = 3600L;
	public static final double BASE_EMPOWERMENT_COST_MULTIPLIER = 0.75D;
	public static final int MAX_POMES_PER_BLOOM = 9;

	private QliphothPomeRules() {
	}

	public static long empowermentDurationTicks(int qliphothGestationLevel) {
		return BASE_EMPOWERMENT_DURATION_TICKS + Math.max(0, qliphothGestationLevel) * 360L;
	}

	public static double empowermentCostMultiplier(int qliphothGestationLevel) {
		return Math.max(0.6D, BASE_EMPOWERMENT_COST_MULTIPLIER - Math.max(0, qliphothGestationLevel) * 0.03D);
	}

	public static boolean canRipenPome(int ripenedCount, boolean hasPendingPome) {
		return !hasPendingPome && ripenedCount >= 0 && ripenedCount < MAX_POMES_PER_BLOOM;
	}

	public static int nextRipeHuskIndex(int ripenedCount) {
		return Math.max(0, Math.min(MAX_POMES_PER_BLOOM - 1, ripenedCount));
	}

	public static boolean canPickPendingPome(boolean hasPendingPome, boolean pendingPomeClaimed) {
		return hasPendingPome && !pendingPomeClaimed;
	}

	public static boolean canConsumePickedPome(boolean boundPome, boolean hasOwnerData, boolean ownerMatchesPlayer) {
		return !boundPome || (hasOwnerData && ownerMatchesPlayer);
	}

	public static boolean canConsumePickedPome(boolean boundPome, boolean hasOwnerData, boolean ownerMatchesPlayer,
			boolean creativePlayer) {
		return creativePlayer || canConsumePickedPome(boundPome, hasOwnerData, ownerMatchesPlayer);
	}

	public static boolean shouldProtectBoundPome(boolean boundPome, boolean creativePlayer) {
		return boundPome && !creativePlayer;
	}

	public static boolean shouldResetProgressOnPrune(boolean removedBloom, int totalPomesConsumed) {
		return removedBloom && totalPomesConsumed > 0;
	}
}
