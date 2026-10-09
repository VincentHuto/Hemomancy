package com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar;

import com.vincenthuto.hemomancy.common.circus.CinderBellowsRules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * How a held Marionette Crossbar works its strings. A bar with no puppet out hangs still and only breathes; with a
 * puppet out it bobs, rolls and walks its rear bar against the front one. Puppet attacks tug it, and a call or
 * recall lifts it and snaps it down. Angles are radians and {@code rise} is model pixels.
 */
public final class CrossbarMotionRules {
	public static final int TUG_TICKS = 12;
	public static final int LIFT_TICKS = 10;
	/** Puppet count before the holder's first observed tick; a Crossbar picked up mid-act is not a call. */
	public static final int UNSEEN = -1;
	private static final float TUG_PEAK = 2.0F;
	private static final float ENGAGE_STEP = 0.15F;

	private CrossbarMotionRules() {
	}

	public record Pose(float pitch, float roll, float rise, float frontRock, float rearRock,
			float threadSwing, float threadFlick, float pulse) {
	}

	public static Pose pose(float time, float engage, float tug, float lift) {
		float walk = (float) Math.sin(time * 0.16F);
		float pitch = engage * (float) Math.sin(time * 0.11F) * 0.10F - tug * 0.55F - lift * 0.30F;
		float roll = (1.0F - engage) * (float) Math.sin(time * 0.05F) * 0.02F
				+ engage * (float) Math.sin(time * 0.07F) * 0.12F;
		float rise = lift * 3.0F + tug * 1.2F;
		float threadSwing = (float) Math.sin(time * 0.05F) * 0.03F + engage * (float) Math.sin(time * 0.2F) * 0.15F;
		float threadFlick = tug * 0.7F + Math.max(0.0F, lift) * 0.3F;
		float pulse = engage * (0.55F + 0.45F * (float) Math.sin(time * 0.25F));
		return new Pose(pitch, roll, rise, engage * walk * 0.22F, -engage * walk * 0.32F, threadSwing, threadFlick,
				pulse);
	}

	/** A sharp upward tug: peaks two ticks after the attack, then eases out over {@link #TUG_TICKS}. */
	public static float tug(float age) {
		if (age <= 0.0F || age > TUG_TICKS) return 0.0F;
		if (age <= TUG_PEAK) return (float) Math.sin(age / TUG_PEAK * Math.PI * 0.5D);
		float rest = (TUG_TICKS - age) / (TUG_TICKS - TUG_PEAK);
		return rest * rest;
	}

	/** Lift over four ticks, snap past rest by tick six, settle by {@link #LIFT_TICKS}. */
	public static float lift(float age) {
		if (age <= 0.0F || age >= LIFT_TICKS) return 0.0F;
		if (age <= 4.0F) return (float) Math.sin(age / 4.0F * Math.PI * 0.5D);
		if (age <= 6.0F) return 1.0F - (age - 4.0F) / 2.0F * 1.35F;
		return -0.35F * (1.0F - (age - 6.0F) / (LIFT_TICKS - 6.0F));
	}

	public static float approachEngage(float engage, boolean active) {
		return active ? Math.min(1.0F, engage + ENGAGE_STEP) : Math.max(0.0F, engage - ENGAGE_STEP);
	}

	public static boolean isCallOrRecall(int previousCount, int count) {
		return previousCount != UNSEEN && previousCount != count;
	}

	public static boolean isNewSwing(boolean wasSwinging, boolean swinging) {
		return !wasSwinging && swinging;
	}

	/** The Cinder Bellows has no swing; its synced cycle entering the breath phase is its attack. */
	public static boolean isBreathStart(int previousCycle, int cycle) {
		return cycle > 0 && CinderBellowsRules.phase(previousCycle) != CinderBellowsRules.Phase.BREATHE
				&& CinderBellowsRules.phase(cycle) == CinderBellowsRules.Phase.BREATHE;
	}

	/** Each puppet keeps one string, in entity-id order, so its thread never jumps between bar tips. */
	public static int stringIndex(List<Integer> puppetIds, int puppetId, int strings) {
		List<Integer> sorted = new ArrayList<>(puppetIds);
		Collections.sort(sorted);
		int index = sorted.indexOf(puppetId);
		return index < 0 ? -1 : index % strings;
	}
}
