package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class UnstainedPathGuidance {
	public enum PlayStyle { PASSIVE_SURVIVAL, OBSERVANCE_FOCUSED, ALTAR_FOCUSED }
	private static final int[][] PURITY_MINUTES = {{45,105,180,270},{30,75,135,210},{20,55,105,180}};
	private static final int[][] CLARITY_MINUTES = {{50,115,190,280},{35,80,145,220},{25,65,120,195}};

	private UnstainedPathGuidance() {}

	public static String nextRequiredRite(boolean begun, boolean purified, boolean clarityUnlocked) {
		if (!begun) return "lethean_baptism";
		if (!purified) return "patient_purification";
		return clarityUnlocked ? "clarity_observances" : "clarity_ascension";
	}

	public static int estimatedPurityMinutes(PlayStyle style, int percent) { return estimate(PURITY_MINUTES[style.ordinal()], percent); }
	public static int estimatedClarityMinutes(PlayStyle style, int percent) { return estimate(CLARITY_MINUTES[style.ordinal()], percent); }
	private static int estimate(int[] targets, int percent) {
		if (percent <= 25) return targets[0];
		if (percent <= 50) return targets[1];
		if (percent <= 75) return targets[2];
		return targets[3];
	}
}
