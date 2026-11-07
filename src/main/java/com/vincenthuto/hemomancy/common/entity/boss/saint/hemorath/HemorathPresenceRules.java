package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

/**
 * The drain (design section 5): full within 4 blocks of him, falling to nothing at 9, a pulse every 5 ticks; 12.5
 * blood a pulse from a blood-active player (2.5 a tick), x1.25 into the meter from an Apotheos; 1 health every 20 ticks
 * inside 4.5 blocks from an empty or inactive one, worth 25 to the meter. During the Last Thirst it is 2 a tick, flat,
 * out to 18 blocks. JVM-tested.
 */
public final class HemorathPresenceRules {
	public static final double FULL_RADIUS = 4.0D;
	public static final double EDGE_RADIUS = 9.0D;
	public static final int CADENCE = 5;
	public static final float DRAIN_PER_PULSE = 12.5F;
	public static final float APOTHEOS_INTAKE = 1.25F;
	public static final float EMPTY_DAMAGE = 1.0F;
	public static final int EMPTY_INTERVAL = 20;
	public static final double EMPTY_RADIUS = 4.5D;
	public static final float METER_PER_HEALTH = 25.0F;
	public static final double THIRST_RADIUS = 18.0D;
	public static final float THIRST_PER_TICK = 2.0F;
	public static final double VERTICAL_REACH = 4.0D;
	/** His heartbeat's interval: 40 ticks hollow, 20 full. */
	public static final int HEARTBEAT_EMPTY = 40;
	public static final int HEARTBEAT_FULL = 20;

	private HemorathPresenceRules() {
	}

	public static double radius(boolean thirst) {
		return thirst ? THIRST_RADIUS : EDGE_RADIUS;
	}

	/** 1 inside FULL_RADIUS, linear to 0 at EDGE_RADIUS; 1 everywhere during the Last Thirst. */
	public static float falloff(double distance, boolean thirst) {
		if (thirst) return distance <= THIRST_RADIUS ? 1.0F : 0.0F;
		if (distance <= FULL_RADIUS) return 1.0F;
		if (distance >= EDGE_RADIUS) return 0.0F;
		return (float) ((EDGE_RADIUS - distance) / (EDGE_RADIUS - FULL_RADIUS));
	}

	/** Blood asked of a blood-active player this pulse. */
	public static float drain(float strength, boolean thirst) {
		return thirst ? THIRST_PER_TICK * CADENCE : DRAIN_PER_PULSE * strength;
	}

	/** What the paid blood is worth to the meter, before the phase-2 intake. */
	public static float intake(float paid, boolean apotheos) {
		return apotheos ? paid * APOTHEOS_INTAKE : paid;
	}

	/** The strength at the empty-player radius: an empty player inside 4.5 blocks is hurt. */
	public static boolean hurtsEmpty(float strength, boolean thirst) {
		return thirst || strength >= falloff(EMPTY_RADIUS, false) - 1.0E-4F;
	}

	/** Whether an empty player's health tick falls on this pulse (one in every EMPTY_INTERVAL ticks). */
	public static boolean emptyTickDue(int tickCount) {
		return Math.floorMod(tickCount, EMPTY_INTERVAL) < CADENCE;
	}

	/** Nothing while he rises, while a phase card plays, or during the ending. */
	public static boolean active(boolean rising, boolean card, boolean ending) {
		return !rising && !card && !ending;
	}

	public static int heartbeat(float fraction) {
		float f = Math.max(0.0F, Math.min(1.0F, fraction));
		return Math.round(HEARTBEAT_EMPTY + (HEARTBEAT_FULL - HEARTBEAT_EMPTY) * f);
	}
}
