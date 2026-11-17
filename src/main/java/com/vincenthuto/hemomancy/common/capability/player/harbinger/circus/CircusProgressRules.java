package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

public final class CircusProgressRules {
	public static final int MAX_ACCLIMATION = 1000;
	public static final int PASSIVE_POINT_TICKS = 80;

	private CircusProgressRules() {
	}

	public static int clamp(int score) {
		return Math.clamp(score, 0, MAX_ACCLIMATION);
	}

	public static Stage stage(int score) {
		int value = clamp(score);
		if (value >= 1000) return Stage.ATTUNED;
		if (value >= 500) return Stage.ACCLIMATING;
		if (value >= 150) return Stage.DISTURBED;
		return Stage.UNAWARE;
	}

	public static int passivePoints(int ticks) {
		return Math.max(0, ticks) / PASSIVE_POINT_TICKS;
	}

	public static boolean canReceivePact(int score) {
		return stage(score).ordinal() >= Stage.ACCLIMATING.ordinal();
	}

	public enum Stage {
		UNAWARE, DISTURBED, ACCLIMATING, ATTUNED;

		public boolean hasPresentation() {
			return this != UNAWARE;
		}

		public int particleIntervalTicks() {
			return switch (this) {
				case UNAWARE -> 0;
				case DISTURBED -> 12;
				case ACCLIMATING -> 10;
				case ATTUNED -> 8;
			};
		}





		public int motionEchoAlpha() {
			return switch (this) {
				case DISTURBED -> 72;
				default -> 0;
			};
		}
	}
}
