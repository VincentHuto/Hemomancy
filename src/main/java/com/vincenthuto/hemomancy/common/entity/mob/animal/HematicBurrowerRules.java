package com.vincenthuto.hemomancy.common.entity.mob.animal;

public final class HematicBurrowerRules {
	public static final double FLEE_RANGE_BLOCKS = 8.0D;
	public static final double FLEE_NAVIGATION_SPEED = 1.18D;
	public static final int BURROW_AWAY_AFTER_FLEE_TICKS = 95;
	public static final int DIG_PARTICLE_TICKS = 24;
	public static final boolean BREAKS_BLOCKS_ON_ESCAPE = false;
	public static final int MAX_LOCAL_BRIGHTNESS = 7;
	public static final int MIN_DEPTH_BELOW_SURFACE = 4;
	public static final int MAX_DEPTH_BELOW_SURFACE = 28;
	public static final int MIN_SPAWN_Y = 32;

	private static final EscapeDrop[] ESCAPE_DROPS = {
			new EscapeDrop("minecraft:coal", 7),
			new EscapeDrop("minecraft:raw_copper", 4),
			new EscapeDrop("minecraft:raw_iron", 2)
	};

	private HematicBurrowerRules() {
	}

	public static boolean canNaturalSpawn(boolean solidBelow, boolean openAir, boolean exposedToSky,
			int localBrightness, int y, int surfaceY) {
		int depth = surfaceY - y;
		return solidBelow
				&& openAir
				&& !exposedToSky
				&& localBrightness <= MAX_LOCAL_BRIGHTNESS
				&& y >= MIN_SPAWN_Y
				&& depth >= MIN_DEPTH_BELOW_SURFACE
				&& depth <= MAX_DEPTH_BELOW_SURFACE;
	}

	public static boolean shouldFlee(boolean alive, boolean creative, boolean spectator, double distance) {
		return alive && !creative && !spectator && distance <= FLEE_RANGE_BLOCKS;
	}

	public static boolean shouldBurrowAway(int fleeTicks) {
		return fleeTicks >= BURROW_AWAY_AFTER_FLEE_TICKS;
	}

	public static int totalEscapeDropWeight() {
		int total = 0;
		for (EscapeDrop drop : ESCAPE_DROPS) {
			total += drop.weight();
		}
		return total;
	}

	public static int escapeDropWeight(String itemId) {
		for (EscapeDrop drop : ESCAPE_DROPS) {
			if (drop.itemId().equals(itemId)) {
				return drop.weight();
			}
		}
		return 0;
	}

	public static String escapeDropByRoll(int roll) {
		int cursor = 0;
		for (EscapeDrop drop : ESCAPE_DROPS) {
			cursor += drop.weight();
			if (roll < cursor) {
				return drop.itemId();
			}
		}
		return "minecraft:coal";
	}

	private record EscapeDrop(String itemId, int weight) {
	}
}
