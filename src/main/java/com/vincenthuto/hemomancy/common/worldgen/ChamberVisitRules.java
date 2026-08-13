package com.vincenthuto.hemomancy.common.worldgen;

public final class ChamberVisitRules {
	private ChamberVisitRules() {
	}

	public static double dreamChance(int priorFailedAttempts, boolean firstDreamSeen) {
		if (firstDreamSeen) return 0.25D;
		return switch (Math.max(0, priorFailedAttempts)) {
			case 0 -> 0.35D;
			case 1 -> 0.65D;
			default -> 1.0D;
		};
	}

	public static int durationTicks(int degree, ChamberVisitMode mode, boolean attuned) {
		if (attuned || mode == ChamberVisitMode.ATTUNED || mode == ChamberVisitMode.ADMIN) return 0;
		if (mode == ChamberVisitMode.DREAM) return Math.max(1, Math.min(2, degree)) * 1_200;
		return switch (Math.max(3, degree)) {
			case 3 -> 6_000;
			case 4 -> 12_000;
			default -> 24_000;
		};
	}

	public static int radiusForDegree(int degree) {
		return Math.max(3, Math.min(10, degree + 2));
	}

	public static boolean ordinaryBedDreamEligible(int degree, boolean activeVisit, boolean activeProjection) {
		return degree >= 1 && degree <= 2 && !activeVisit && !activeProjection;
	}

	public static boolean canUseArbor(ChamberVisitMode mode) {
		return true;
	}

	public static boolean canBuild(ChamberVisitMode mode) {
		return mode != ChamberVisitMode.DREAM;
	}

	public static boolean canMoveItems(ChamberVisitMode mode) {
		return mode != ChamberVisitMode.DREAM;
	}

	public static boolean isProtected(ChamberVisitMode mode) {
		return mode == ChamberVisitMode.DREAM || mode == ChamberVisitMode.TIMED_CHAIR;
	}
}
