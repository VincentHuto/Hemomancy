package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;

public final class MycophantEncounterRules {
	public enum HuntStage {
		QUIET,
		WHISPERS,
		VIGNETTE,
		HALLUCINATION,
		CLAIM
	}

	private MycophantEncounterRules() {
	}

	public static boolean shouldAccumulate(int degree, EnumArchonPath archonPath, boolean defeated,
			boolean inGardens, boolean alive, boolean creative, boolean spectator, boolean encounterActive) {
		return degree >= 8 && archonPath == EnumArchonPath.APOTHEOS && !defeated && inGardens && alive
				&& !creative && !spectator && !encounterActive;
	}

	public static HuntStage huntStage(int exposureTicks, int durationTicks) {
		if (exposureTicks >= durationTicks) {
			return HuntStage.CLAIM;
		}
		if (exposureTicks * 5L >= durationTicks * 11L / 3L) {
			return HuntStage.HALLUCINATION;
		}
		if (exposureTicks * 5L >= durationTicks * 7L / 3L) {
			return HuntStage.VIGNETTE;
		}
		if (exposureTicks * 5L >= durationTicks) {
			return HuntStage.WHISPERS;
		}
		return HuntStage.QUIET;
	}

	public static int averageCueIntervalSeconds(HuntStage stage) {
		return switch (stage) {
			case QUIET, CLAIM -> Integer.MAX_VALUE;
			case WHISPERS -> 90;
			case VIGNETTE -> 60;
			case HALLUCINATION -> 35;
		};
	}

	public static boolean canUseLure(int degree, EnumArchonPath archonPath, boolean defeated,
			int retryCooldownTicks, boolean encounterActive, boolean inGardens, boolean alive) {
		return degree >= 8 && archonPath == EnumArchonPath.APOTHEOS && defeated && retryCooldownTicks <= 0
				&& !encounterActive && inGardens && alive;
	}
}
