package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.util.RandomSource;

public final class VerdigrisMothRules {
	public static final int MAX_NATURAL_SPAWN_BRIGHTNESS = 7;
	public static final int NATURAL_SHED_ROLL_BOUND = 1200;
	public static final int MIN_NATURAL_SHED_COOLDOWN_TICKS = 1800;
	public static final int NATURAL_SHED_COOLDOWN_RANDOM_TICKS = 2400;
	public static final int BRUSH_SHED_COOLDOWN_TICKS = 6000;
	public static final double MAX_FLIGHT_SPEED = 0.16D;
	public static final double FLIGHT_ACCELERATION = 0.018D;
	public static final double FLIGHT_DAMPING = 0.88D;

	private VerdigrisMothRules() {
	}

	public static boolean isNight(long dayTime) {
		long timeOfDay = Math.floorMod(dayTime, 24000L);
		return timeOfDay >= 13000L && timeOfDay <= 23000L;
	}

	public static boolean canNaturalSpawn(boolean night, boolean solidBlockBelow,
			boolean openAirAtSpawn, int rawBrightness) {
		return night && solidBlockBelow && openAirAtSpawn && rawBrightness <= MAX_NATURAL_SPAWN_BRIGHTNESS;
	}

	public static boolean canShedNaturally(boolean clientSide, boolean alive, boolean onGround,
			boolean inWaterOrBubble, int cooldownTicks) {
		return !clientSide && alive && !onGround && !inWaterOrBubble && cooldownTicks <= 0;
	}

	public static boolean canBrushShed(boolean clientSide, boolean alive, int cooldownTicks) {
		return !clientSide && alive && cooldownTicks <= 0;
	}

	public static int nextNaturalShedCooldown(RandomSource random) {
		return MIN_NATURAL_SHED_COOLDOWN_TICKS + random.nextInt(NATURAL_SHED_COOLDOWN_RANDOM_TICKS + 1);
	}
}
