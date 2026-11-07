package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintDeeds;
import net.minecraft.world.phys.Vec3;

/**
 * What each skipped Chapel lends a forced Velorum (design section 10, resolution D-4), on top of the kit's stat floor.
 * Beholding, "They watch you": every frozen or pinned figure has a gaze, and standing in it counts as standing.
 * Litany, "The Rule kept in silence": the Draw of Silence from phase 1, no opening extensions, a closing toll, and the
 * Long Night a fifth more often. Repose, "He never pauses": recoveries x0.7, no settle, a shorter decision delay and the
 * short Bow. JVM-tested.
 */
public final class VelorumDeedRules {
	public static final float REPOSE_RECOVERY = 0.7F;
	public static final int MIN_RECOVERY = 10;
	public static final int REPOSE_DELAY_CUT = 8;
	public static final int GAZE_LENGTH = 8;
	public static final double GAZE_DEGREES = 50.0D;
	/** The Litany's closing toll: everyone within 12 blocks loses their held channel to DISRUPTED for 10 ticks. */
	public static final double TOLL_RADIUS = 12.0D;
	public static final int TOLL_DISRUPTED = 10;
	public static final int THREADS = 5;
	public static final int LITANY_THREADS = 7;
	/** The Long Night's spacing and pressure: a fifth more often under the Litany, since its windows cannot stretch. */
	public static final int LONG_NIGHT_SPACING = 300;
	public static final int LITANY_LONG_NIGHT_SPACING = 240;
	public static final float LONG_NIGHT_PRESSURE = 6.0F;
	public static final float LITANY_LONG_NIGHT_PRESSURE = 7.2F;
	public static final int LONG_NIGHT_BASE = 6;
	public static final int LONG_NIGHT_CAP = 48;
	/** The opening pressure's guarantee: no more than this long without an opening, a fifth sooner under the Litany. */
	public static final int OPENING_DUE = 480;
	public static final int LITANY_OPENING_DUE = 384;

	private VelorumDeedRules() {
	}

	public static boolean beholding(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.BEHOLDING);
	}

	public static boolean litany(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.LITANY);
	}

	public static boolean repose(int mask) {
		return SaintDeeds.skipped(mask, ChapelRules.Kind.REPOSE);
	}

	/** A move's recovery: x0.7 under the Repose deed, never under 10 ticks (nor over the recovery it had). */
	public static int recovery(int recovery, boolean repose) {
		if (!repose) return recovery;
		return Math.min(recovery, Math.max(MIN_RECOVERY, Math.round(recovery * REPOSE_RECOVERY)));
	}

	public static int decisionDelay(int delay, boolean repose) {
		return repose ? Math.max(1, delay - REPOSE_DELAY_CUT) : delay;
	}

	public static int threads(boolean litany) {
		return litany ? LITANY_THREADS : THREADS;
	}

	public static int longNightSpacing(boolean litany) {
		return litany ? LITANY_LONG_NIGHT_SPACING : LONG_NIGHT_SPACING;
	}

	/** The Long Night's weight: 6, plus its pressure for every 100 ticks since the last opening ended, capped at 48. */
	public static int longNightWeight(int ticksSinceOpening, boolean litany) {
		float pressure = litany ? LITANY_LONG_NIGHT_PRESSURE : LONG_NIGHT_PRESSURE;
		return Math.min(LONG_NIGHT_CAP, LONG_NIGHT_BASE + Math.round(pressure * (Math.max(0, ticksSinceOpening) / 100)));
	}

	/** Ticks without an opening after which the Long Night is called at the next boundary, whatever the picker says. */
	public static int openingDue(boolean litany) {
		return litany ? LITANY_OPENING_DUE : OPENING_DUE;
	}

	/**
	 * Whether {@code point} stands in a figure's gaze: within 8 blocks of its face and within 25 degrees of where it
	 * looks ({@code lookX}, {@code lookZ}, a horizontal direction). Sight is the caller's.
	 */
	public static boolean inGaze(Vec3 face, double lookX, double lookZ, Vec3 point) {
		return inGaze(face, lookX, lookZ, point, GAZE_LENGTH);
	}

	/** In the dark a figure's gaze reaches 10 blocks (revision 2026-11-06, R.9). */
	public static final double DARK_GAZE_LENGTH = 10.0D;

	public static double gazeLength(boolean dark) {
		return dark ? DARK_GAZE_LENGTH : GAZE_LENGTH;
	}

	/** {@link #inGaze(Vec3, double, double, Vec3)} with the gaze's reach given. */
	public static boolean inGaze(Vec3 face, double lookX, double lookZ, Vec3 point, double length) {
		double dx = point.x - face.x, dz = point.z - face.z;
		double distance = Math.sqrt(dx * dx + dz * dz);
		if (distance > length || distance < 1.0E-4D) return distance < 1.0E-4D;
		double look = Math.sqrt(lookX * lookX + lookZ * lookZ);
		if (look < 1.0E-6D) return false;
		double cos = (dx * lookX + dz * lookZ) / (distance * look);
		return cos >= Math.cos(Math.toRadians(GAZE_DEGREES / 2.0D));
	}
}
