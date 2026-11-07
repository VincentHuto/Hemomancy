package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumCongregationRules.State;

/**
 * The Eclipse's pure rules (design revision 2026-11-06, R.7): the lantern goes out, the room goes dark but for the
 * cruor's glow, and the congregation stands up out of its figures as torn umbral shapes that reach. Started by state,
 * never a cooldown: phase 3, after the first Endless Hour, 400 ticks clear of an Hour and 600 of the last Eclipse, once
 * the dark covers 35% of the boss floor or his health is at 15% or less. Held Projection relights it (20 fed ticks, 30
 * under the Litany) into a 60-tick Guttering Lantern; unbroken it preserves everyone at 60 or more, hurts the rest and
 * gutters at half length. JVM-tested.
 */
public final class VelorumEclipseRules {
	public static final int HOUR_GAP = 400;
	public static final int ECLIPSE_GAP = 600;
	public static final double COVERAGE_GATE = 0.35D;
	public static final float HEALTH_GATE = 0.15F;

	/** Relighting: fed ticks at the lesser of 3 and the item's rate, with the waking's grace and bleed. */
	public static final int RELIGHT_TICKS = 20;
	public static final int LITANY_RELIGHT_TICKS = 30;
	public static final float KINDLE_RATE = 3.0F;
	/** Relit, the Gutter opens at 60 whatever the phase. */
	public static final int RELIT_GUTTER = 60;
	/** Unbroken: everyone at 60 or more is preserved, the rest take 6. */
	public static final float PRESERVES_AT = 60.0F;
	public static final float UNBROKEN_DAMAGE = 6.0F;

	/** The shapes: a reach within 4 blocks (5 under the Beholding), a 16-tick tell, a grip of at most 30, a rest of 60. */
	public static final double SHAPE_REACH = 4.0D;
	public static final double BEHOLDING_REACH = 5.0D;
	/** A shape takes 20 ticks to stand up out of its figure before it can reach. */
	public static final int SHAPE_RISE = 20;
	public static final int REACH_TELL = 16;
	public static final int GRIP_TICKS = 30;
	public static final int SHAPE_REST = 60;
	/** Walk this far from the figure to tear free of its grip. */
	public static final double GRIP_RANGE = 6.0D;
	public static final float GRIP_DAMAGE = 4.0F;
	public static final float GRIP_FILL = 1.2F;

	/** The congregation reaches together this far into the hold, its arms revealed 20 ticks before. */
	public static final int[] TOGETHER = {40, 90, 140};
	public static final int TOGETHER_REVEAL = 20;
	public static final int TOGETHER_SHAPES = 3;
	public static final double ARM_LENGTH = 9.0D;
	public static final double ARM_HALF_WIDTH = 0.6D;
	public static final double ARM_SPEED = 0.9D;
	/** The arm lies along the floor this long after it is thrown out, then fades. */
	public static final int ARM_HOLD = 10;
	public static final int ARM_ROOT = 15;
	public static final float ARM_DAMAGE = 4.0F;
	public static final float ARM_STILLNESS = 15.0F;

	/** What a figure's shape does in the Eclipse. */
	public enum Shape { NONE, REACHES, HELD }

	private VelorumEclipseRules() {
	}

	/**
	 * Whether the Eclipse may start: phase 3, the first Hour has ended, clear of the last Hour's end by 400 and of the
	 * last Eclipse's by 600 ({@code Long.MAX_VALUE} for none yet), and the dark at 35% of the floor or he at 15% health.
	 */
	public static boolean gate(int phase, boolean hourEnded, long sinceHour, long sinceEclipse, double coverage,
			float health) {
		if (phase < 3 || !hourEnded) return false;
		if (sinceHour < HOUR_GAP || sinceEclipse < ECLIPSE_GAP) return false;
		return coverage >= COVERAGE_GATE - 1.0E-9D || health <= HEALTH_GATE + 1.0E-6F;
	}

	/** D-7's mirror: no Endless Hour within 400 ticks of an Eclipse's end. */
	public static boolean hourAllowed(long sinceEclipse) {
		return sinceEclipse >= HOUR_GAP;
	}

	public static int relightTicks(boolean litany) {
		return litany ? LITANY_RELIGHT_TICKS : RELIGHT_TICKS;
	}

	public static boolean relit(float fed, boolean litany) {
		return fed >= relightTicks(litany);
	}

	/** The Gutter after the Eclipse: 60 relit, half the phase's length unbroken. */
	public static int gutterTicks(int phase, boolean relit) {
		return relit ? RELIT_GUTTER : VelorumOpeningRules.gutterTicks(phase, true);
	}

	/** Frozen and stilled figures become shapes that reach; pinned figures' shapes rise but the hyphae hold their arms. */
	public static Shape shape(State state) {
		return switch (state) {
			case FROZEN, STILLED -> Shape.REACHES;
			case PINNED -> Shape.HELD;
			case AWAKE, HOME -> Shape.NONE;
		};
	}

	public static double reach(boolean beholding) {
		return beholding ? BEHOLDING_REACH : SHAPE_REACH;
	}

	/** The index of the together-reach revealed {@code holdAge} ticks into the hold, or -1. */
	public static int revealAt(int holdAge) {
		for (int i = 0; i < TOGETHER.length; i++) if (TOGETHER[i] - TOGETHER_REVEAL == holdAge) return i;
		return -1;
	}

	/** A shape's grip holds while its quarry is inside the tether's range and its 30 ticks last. */
	public static boolean gripHolds(double distance, int gripTicks) {
		return distance <= GRIP_RANGE && gripTicks < GRIP_TICKS;
	}
}
