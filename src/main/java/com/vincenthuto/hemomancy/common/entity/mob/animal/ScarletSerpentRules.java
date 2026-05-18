package com.vincenthuto.hemomancy.common.entity.mob.animal;

public final class ScarletSerpentRules {
	public static final double HOOD_FLARE_RANGE_BLOCKS = 8.0D;
	public static final double STRIKE_RANGE_BLOCKS = 5.0D;
	public static final int POISON_DURATION_TICKS = 100;
	public static final int BLOOD_BINDING_DURATION_TICKS = 60;
	public static final int STRIKE_ANIMATION_TICKS = 10;

	private ScarletSerpentRules() {
	}

	public static boolean shouldFlareHood(boolean alive, boolean creative, boolean spectator, double distance) {
		return isVulnerablePlayer(alive, creative, spectator) && distance <= HOOD_FLARE_RANGE_BLOCKS;
	}

	public static boolean shouldStrike(boolean alive, boolean creative, boolean spectator, double distance) {
		return isVulnerablePlayer(alive, creative, spectator) && distance <= STRIKE_RANGE_BLOCKS;
	}

	private static boolean isVulnerablePlayer(boolean alive, boolean creative, boolean spectator) {
		return alive && !creative && !spectator;
	}
}
