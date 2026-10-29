package com.vincenthuto.hemomancy.common.world;

/**
 * How the Gardens notice a body: footsteps on the network, autonomy and blood that read as foreign, and what an
 * Apotheos feels through the ground. An Apotheos is the organism's own; anyone else is a host.
 */
public final class GardensNetworkRules {
	public static final double ALARM_DISTANCE = 8.0;
	public static final long ALARM_COOLDOWN_TICKS = 100;
	public static final double ALERT_RADIUS = 24.0;
	public static final double SENSE_RADIUS = 32.0;
	public static final double STILL_MOVE_SQR = 0.0004;
	/** More than a block in one tick is a teleport, a pearl or a fall, not a footstep. */
	public static final double MAX_STEP_SQR = 1.0;
	public static final float SUSPICION_THRESHOLD = 12f;
	public static final float DECAY_PER_10S = 1f;
	public static final long SUMMON_COOLDOWN_TICKS = 600;
	public static final int MAX_ANTIBODIES = 4;

	public enum PuffballOutcome { NONE, ABSORB, BURST }

	private GardensNetworkRules() {}

	/** Distance a step adds to the network's count: sneaking is silent, and only the network's ground carries it. */
	public static double footfall(boolean sneaking, boolean onFungalGround, double moveSqr) {
		if (sneaking || !onFungalGround || moveSqr < STILL_MOVE_SQR || moveSqr > MAX_STEP_SQR) return 0.0;
		return Math.sqrt(moveSqr);
	}

	public static boolean shouldAlarm(double walked, long ticksSinceAlarm) {
		return walked >= ALARM_DISTANCE && ticksSinceAlarm >= ALARM_COOLDOWN_TICKS;
	}

	/** A cast is autonomy; an Apotheos's casts read as the organism's own, until there are too many. */
	public static float castSuspicion(boolean apotheos) {
		return apotheos ? 1f : 2f;
	}

	/** A host swollen with blood reads as foreign; the Apotheos's blood is the hive's. */
	public static float bloodSuspicionPerSecond(boolean apotheos, double volume, double maxVolume) {
		if (apotheos || maxVolume <= 0) return 0f;
		return volume / maxVolume > 0.75 ? 0.25f : 0f;
	}

	public static int antibodiesFor(float suspicion) {
		if (suspicion < SUSPICION_THRESHOLD) return 0;
		return Math.min(MAX_ANTIBODIES, 2 + (int) ((suspicion - SUSPICION_THRESHOLD) / 6f));
	}

	public static boolean canSummon(long ticksSinceSummon) {
		return ticksSinceSummon >= SUMMON_COOLDOWN_TICKS;
	}

	public static boolean senses(boolean apotheos, boolean inDimension, boolean onFungalGround, double distanceSqr) {
		return apotheos && inDimension && onFungalGround && distanceSqr <= SENSE_RADIUS * SENSE_RADIUS;
	}

	/** Puffballs answer a body only in the Gardens dimension; elsewhere, the Overworld biome included, they are plants. */
	public static PuffballOutcome puffball(boolean apotheos, boolean inDimension) {
		if (!inDimension) return PuffballOutcome.NONE;
		return apotheos ? PuffballOutcome.ABSORB : PuffballOutcome.BURST;
	}
}
