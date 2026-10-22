package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Where a Working craft's daemon flies. During the offering procession it leaves
 * the planted staff, swoops to each lit brazier in turn and absorbs its offering
 * there, then returns. In the culmination it dives into the built structure and
 * stays inside it while the structure darkens. Pure maths; the engine moves the
 * daemon entity along it each tick.
 */
public final class WorkingCraftDaemonPath {
	/** Ticks to swoop from one stop to the next. */
	public static final int FLY_TICKS = 26;
	/** Ticks it hangs over a brazier, drinking its offering. */
	public static final int ABSORB_TICKS = 12;
	public static final int VISIT_TICKS = FLY_TICKS + ABSORB_TICKS;
	/** Ticks into the culmination at which the daemon has dived inside the structure. */
	public static final int DIVE_TICKS = 36;
	/** How high each swoop arcs above the straight line between stops. */
	private static final double ARC = 1.1D;
	private static final double DIVE_ARC = 1.6D;

	private WorkingCraftDaemonPath() {
	}

	/** The procession tick on which offering {@code index} is absorbed: the daemon has just arrived over it. */
	public static int drawTick(int index) {
		return index * VISIT_TICKS + FLY_TICKS;
	}

	public static int drawsDue(int phaseTicks, int offerings) {
		int due = 0;
		while (due < offerings && drawTick(due) <= phaseTicks) due++;
		return due;
	}

	/** Ticks from setting out to settling back over the staff. */
	public static int processionTicks(int offerings) {
		return offerings <= 0 ? 0 : offerings * VISIT_TICKS + FLY_TICKS;
	}

	public static boolean processionFinished(int phaseTicks, int offerings) {
		return phaseTicks >= processionTicks(offerings);
	}

	/**
	 * The daemon's position: swooping from the staff ({@code hover}) to each stop, hanging
	 * there while it absorbs, and back to the staff after the last.
	 */
	public static Vec3 processionPosition(double phaseTicks, Vec3 hover, List<Vec3> stops) {
		if (stops.isEmpty() || phaseTicks <= 0.0D) return hover;
		int legs = stops.size() + 1;
		for (int leg = 0; leg < legs; leg++) {
			double start = leg * (double) VISIT_TICKS;
			Vec3 from = leg == 0 ? hover : stops.get(leg - 1);
			Vec3 to = leg == stops.size() ? hover : stops.get(leg);
			if (phaseTicks < start + FLY_TICKS) {
				double t = Math.max(0.0D, (phaseTicks - start) / FLY_TICKS);
				return swoop(from, to, easeInOut(t), ARC);
			}
			if (leg < stops.size() && phaseTicks < start + VISIT_TICKS) {
				double hang = (phaseTicks - start - FLY_TICKS) / ABSORB_TICKS;
				return to.add(0.0D, 0.08D * Math.sin(Math.PI * hang), 0.0D);
			}
		}
		return hover;
	}

	/** Diving from over the staff into the structure's heart, then staying inside it. */
	public static Vec3 divePosition(double culminationTicks, Vec3 hover, Vec3 structure) {
		double t = HemoEasing.clamp01(culminationTicks / DIVE_TICKS);
		return swoop(hover, structure, t * t * (3.0D - 2.0D * t), DIVE_ARC);
	}

	/** The daemon shrinks as it pours itself into the structure. */
	public static float diveScale(double culminationTicks, float startScale, float minScale) {
		double t = HemoEasing.clamp01(culminationTicks / DIVE_TICKS);
		double eased = t * t * (3.0D - 2.0D * t);
		return (float) (startScale + (minScale - startScale) * eased);
	}

	private static Vec3 swoop(Vec3 from, Vec3 to, double t, double arc) {
		return from.lerp(to, t).add(0.0D, arc * Math.sin(Math.PI * t), 0.0D);
	}

	private static double easeInOut(double t) {
		return t < 0.5D ? 4.0D * t * t * t : 1.0D - Math.pow(-2.0D * t + 2.0D, 3.0D) / 2.0D;
	}
}
