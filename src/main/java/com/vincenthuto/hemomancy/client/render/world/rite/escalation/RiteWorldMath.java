package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteInstability;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Every number the rite's world layers use: when each layer arrives, how far it reaches, how strain bends it and
 * how it lets go when the rite ends. Pure, so it can be tested and tuned in one place.
 */
public final class RiteWorldMath {
	/** Fractions of the world level at which layers arrive. Air, trench, braziers and the drone come with step 1. */
	public static final float CREEP_FROM = 1.0F / 3.0F;
	public static final float WOUND_FROM = 0.5F;
	public static final float RAIN_FROM = 0.75F;
	/** How far below its threshold a layer fades in when a rite's step count is unknown. */
	public static final float ARRIVAL_SOFTNESS = 0.04F;
	public static final float FULL_WEIGHT_MARGIN = 4.0F;
	public static final float ZERO_WEIGHT_MARGIN = 32.0F;
	public static final float MIN_FAR_PLANE = 24.0F;
	public static final int MAX_RAIN_PER_TICK = 30;
	public static final double WOUND_DRAW_DISTANCE = 256.0D;
	public static final int BUILD_LIMIT_MARGIN = 8;
	/** The wound never hangs lower than this over its Focus, even when the build limit is close. */
	public static final int WOUND_MIN_CLEARANCE = 12;
	/** Ticks after a collapse begins at which its wound throws lightning. */
	public static final int[] COLLAPSE_STRIKE_TICKS = {0, 7, 15, 24};

	public enum Ending {
		NONE, COMPLETED, COLLAPSED, CANCELLED, VANISHED
	}

	private RiteWorldMath() {
	}

	private static boolean ruptured(CardinalRiteInstability strain) {
		return strain == CardinalRiteInstability.RUPTURING || strain == CardinalRiteInstability.COLLAPSED;
	}

	public static float level(int stage, int total) {
		return total <= 0 ? 0.0F : HemoEasing.clamp01(stage / (float) total);
	}

	/** The first step of a rite with {@code total} steps that reaches a threshold. */
	static int firstStep(float from, int total) {
		return (int) Math.ceil(from * total - 1.0e-4F);
	}

	/**
	 * 0 until a rite reaches the first of its steps at or past a layer's threshold, 1 from that step on. The layer
	 * fades in while the eased level climbs from the step before, so it never shows on an earlier step, whatever
	 * the rite's step count. With no step count it fades in over {@link #ARRIVAL_SOFTNESS} below the threshold.
	 */
	public static float arrival(float level, float from, int total) {
		if (total <= 0) return HemoEasing.smoothstep(from - ARRIVAL_SOFTNESS, from, level);
		int first = firstStep(from, total);
		// The edges are computed exactly as level(stage, total) is, so the step before reads exactly 0.
		return HemoEasing.smoothstep((first - 1) / (float) total, first / (float) total, level);
	}

	/** How far past {@code from} the level has gone, 0 to 1. */
	public static float ramp(float level, float from) {
		if (from >= 1.0F) return level >= 1.0F ? 1.0F : 0.0F;
		return HemoEasing.clamp01((level - from) / (1.0F - from));
	}

	/** A layer that arrives showing {@code start} of itself on its own step and grows to the whole by level 1. */
	public static float growth(float level, float from, float start, int total) {
		float arrivesAt = total <= 0 ? from : firstStep(from, total) / (float) total;
		return arrival(level, from, total) * (start + (1.0F - start) * ramp(level, arrivesAt));
	}

	public static float viewerWeight(double distanceXZ, float footprint) {
		double full = footprint + FULL_WEIGHT_MARGIN;
		double zero = footprint + ZERO_WEIGHT_MARGIN;
		if (distanceXZ <= full) return 1.0F;
		if (distanceXZ >= zero) return 0.0F;
		return (float) ((zero - distanceXZ) / (zero - full));
	}

	public static float fogMix(String profile) {
		return switch (profile == null ? "" : profile) {
			case "storm" -> 0.6F;
			case "dense" -> 0.45F;
			default -> 0.25F;
		};
	}

	public static float farPlaneAtFull(String profile) {
		return switch (profile == null ? "" : profile) {
			case "storm" -> 0.65F;
			case "dense" -> 0.75F;
			default -> 0.85F;
		};
	}

	public static float strainFarScale(CardinalRiteInstability strain) {
		return ruptured(strain) ? 0.8F : strain == CardinalRiteInstability.STRAINED ? 0.9F : 1.0F;
	}

	/** The far-plane multiplier a rite asks for at full viewer weight. */
	public static float farPlaneScale(float level, String profile, CardinalRiteInstability strain) {
		return (1.0F + (farPlaneAtFull(profile) - 1.0F) * HemoEasing.clamp01(level)) * strainFarScale(strain);
	}

	/** Applies a far-plane multiplier at a viewer weight, never pulling the fog inside {@link #MIN_FAR_PLANE}. */
	public static float applyFarPlane(float far, float scale, float weight) {
		if (far <= MIN_FAR_PLANE) return far;
		return Math.max(MIN_FAR_PLANE, far * (1.0F + (scale - 1.0F) * HemoEasing.clamp01(weight)));
	}

	public static float trenchFill(float level) {
		return HemoEasing.clamp01(level);
	}

	public static float overflow(float level, int total) {
		return growth(level, RAIN_FROM, 0.3F, total);
	}

	public static float creepReach(float footprint) {
		return Math.max(4.0F, Math.min(12.0F, 0.6F * footprint));
	}

	/** The creep's radius from the Focus centre: from the floor's edge outward, or 0 before it arrives. */
	public static float creepRadius(float floorEdge, float footprint, float level, int total,
			CardinalRiteInstability strain) {
		float grown = growth(level, CREEP_FROM, 0.25F, total);
		if (grown <= 0.0F) return 0.0F;
		return floorEdge + creepReach(footprint) * grown * (ruptured(strain) ? 0.7F : 1.0F);
	}

	public static float woundPresence(float level, int total) {
		return arrival(level, WOUND_FROM, total);
	}

	public static float woundRadius(float footprint, float level) {
		return Math.max(2.0F, footprint) * (0.8F + 0.8F * ramp(level, WOUND_FROM));
	}

	public static double woundAltitude(int focusY, float footprint, int maxBuildHeight) {
		double wanted = focusY + 30.0D + 0.6D * footprint;
		double capped = Math.min(wanted, maxBuildHeight - BUILD_LIMIT_MARGIN);
		return Math.max(focusY + WOUND_MIN_CLEARANCE, capped);
	}

	/** Ticks between a storm rite's ambient strikes from its wound: rarer early, more frequent as it deepens. */
	public static int woundStrikeInterval(float level) {
		return Math.round(80.0F - 50.0F * HemoEasing.clamp01(level));
	}

	public static float rainLevel(float level, int total) {
		return growth(level, RAIN_FROM, 0.3F, total);
	}

	public static int rainPerTick(float rainLevel, float footprint, float particleScale,
			CardinalRiteInstability strain) {
		float thick = strain == CardinalRiteInstability.STABLE ? 1.0F : 1.25F;
		int count = Math.round(HemoEasing.clamp01(rainLevel) * (2.0F + 0.5F * footprint) * thick);
		return Math.round(Math.min(MAX_RAIN_PER_TICK, count) * Math.max(0.0F, particleScale));
	}

	/** A brazier fire's height multiplier: fed taller as the rite deepens, guttered by strain. */
	public static float flameHeight(float level, CardinalRiteInstability strain, float beat, float flicker) {
		float height = 1.0F + 0.5F * HemoEasing.clamp01(level);
		if (strain == CardinalRiteInstability.STRAINED) return height * 0.75F * flicker;
		if (ruptured(strain)) return height * (0.15F + 0.85F * HemoEasing.clamp01(beat));
		return height;
	}

	public static float flameLeanDegrees(float level) {
		return 20.0F * HemoEasing.clamp01(level);
	}

	/** Braziers burn in the sanguine palette once the rite is halfway through. */
	public static boolean flameSanguine(float level) {
		return level >= WOUND_FROM;
	}

	public static float droneVolume(float level, float weight, boolean still) {
		if (still || level <= 0.0F) return 0.0F;
		return weight * (0.35F + 0.4F * HemoEasing.clamp01(level));
	}

	public static float heartbeatVolume(float level, int total, float weight, boolean still) {
		return still ? 0.0F : weight * arrival(level, CREEP_FROM, total) * 0.9F;
	}

	public static float stormVolume(float level, int total, float weight, boolean still) {
		return still ? 0.0F : weight * arrival(level, RAIN_FROM, total) * 0.6F;
	}

	public static float strainPitch(CardinalRiteInstability strain) {
		return ruptured(strain) ? 0.75F : strain == CardinalRiteInstability.STRAINED ? 0.88F : 1.0F;
	}

	public static int endingTicks(Ending ending) {
		return switch (ending) {
			case NONE -> 0;
			case COMPLETED -> 60;
			case COLLAPSED, CANCELLED -> 80;
			case VANISHED -> 16;
		};
	}

	/** How much of the creep, trench and braziers remains as a rite ends. */
	public static float groundEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case COMPLETED -> 1.0F - HemoEasing.clamp01(age / 60.0F);
			case COLLAPSED -> 1.0F - HemoEasing.clamp01(age / 20.0F);
			case CANCELLED -> 1.0F - HemoEasing.clamp01(age / 80.0F);
			case VANISHED -> 1.0F - HemoEasing.clamp01(age / 16.0F);
		};
	}

	/** How much of the air's tint remains as a rite ends. */
	public static float airEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case COMPLETED -> 1.0F - HemoEasing.clamp01(age / 60.0F);
			case COLLAPSED, CANCELLED -> 1.0F - HemoEasing.clamp01(age / 80.0F);
			case VANISHED -> 1.0F - HemoEasing.clamp01(age / 16.0F);
		};
	}

	/** A collapse pulls the fog 20% closer over its first 10 ticks, then lets it thin back over 70. */
	public static float collapseSurge(Ending ending, float age) {
		if (ending != Ending.COLLAPSED || age < 0.0F) return 1.0F;
		if (age < 10.0F) return 1.0F - 0.2F * (age / 10.0F);
		return 0.8F + 0.2F * HemoEasing.clamp01((age - 10.0F) / 70.0F);
	}

	public static float rainEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case CANCELLED -> 1.0F - HemoEasing.clamp01(age / 80.0F);
			default -> 0.0F;
		};
	}

	public static float soundEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case COMPLETED -> 1.0F - HemoEasing.clamp01(age / 40.0F);
			case COLLAPSED -> 0.0F;
			case CANCELLED -> 1.0F - HemoEasing.clamp01(age / 80.0F);
			case VANISHED -> 1.0F - HemoEasing.clamp01(age / 16.0F);
		};
	}

	/** The wound's size as a rite ends: a completion closes it behind a flash, a collapse shudders it shut. */
	public static float woundEnding(Ending ending, float age) {
		return switch (ending) {
			case NONE -> 1.0F;
			case COMPLETED -> 1.0F - HemoEasing.smoothstep(0.0F, 14.0F, age);
			case COLLAPSED -> (1.0F - HemoEasing.smoothstep(4.0F, 30.0F, age)) * (1.0F + 0.12F * (float) Math.sin(age * 2.1F));
			case CANCELLED -> 1.0F - HemoEasing.clamp01(age / 80.0F);
			case VANISHED -> 1.0F - HemoEasing.clamp01(age / 16.0F);
		};
	}

	/** A flash in the wound: a step's surge, or a completion closing it. */
	public static float woundFlash(Ending ending, float age, float surge) {
		float closing = ending == Ending.COMPLETED ? Math.max(0.0F, 1.0F - age / 8.0F) : 0.0F;
		return Math.max(HemoEasing.clamp01(surge), closing);
	}
}
