package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

public final class VenomRibCentipedeRules {
	public static final double MAX_HEALTH = 34.0D;
	public static final double MOVEMENT_SPEED = 0.28D;
	public static final double ATTACK_DAMAGE = 6.0D;
	public static final double FOLLOW_RANGE = 20.0D;
	public static final int NATURAL_SPAWN_WEIGHT = 3;
	public static final int MIN_GROUP_SIZE = 1;
	public static final int MAX_GROUP_SIZE = 1;
	public static final double WARNING_RANGE_BLOCKS = 10.0D;
	public static final double STRIKE_RANGE_BLOCKS = 6.0D;
	public static final int POISON_DURATION_TICKS = 160;
	public static final int BLOOD_BINDING_DURATION_TICKS = 50;
	public static final int STRIKE_ANIMATION_TICKS = 12;

	private VenomRibCentipedeRules() {
	}

	public static boolean canNaturalSpawn(boolean solidBelow, boolean openAir, int localBrightness) {
		return solidBelow && openAir && localBrightness <= 8;
	}

	public static boolean shouldWarn(boolean alive, boolean creative, boolean spectator, double distance) {
		return alive && !creative && !spectator && distance <= WARNING_RANGE_BLOCKS;
	}

	public static boolean shouldStrike(boolean alive, boolean creative, boolean spectator, double distance) {
		return alive && !creative && !spectator && distance <= STRIKE_RANGE_BLOCKS;
	}
}
