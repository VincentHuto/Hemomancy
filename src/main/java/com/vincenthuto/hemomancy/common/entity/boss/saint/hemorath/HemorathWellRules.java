package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

/**
 * The four wells (design section 9): fight values kept on the Saint, 1000 each, 300 at the start. Held Absorption takes
 * 30 a tick out, held Projection puts 25 a tick in (never more than the held item moves, resolution B-4), Wells Answer
 * drains a pair over 40 ticks and heals him 1 per 50 drawn, the Litany deed refills 2 a tick, and a draining tide sends
 * half of what it held back, a quarter to each. The spoke and pit gutter fills read from here. JVM-tested.
 */
public final class HemorathWellRules {
	public static final int WELLS = 4;
	public static final float CAPACITY = 1000.0F;
	public static final float START = 300.0F;
	public static final float ABSORB = 30.0F;
	public static final float PROJECT = 25.0F;
	public static final int DRAW_TICKS = 40;
	public static final float DRAWN_PER_HEALTH = 50.0F;
	public static final float LITANY_REFILL = 2.0F;
	/** A pair is worth drawing from this much; he walks to the pit for it when hungry or a pair holds HUNGRY_PAIR. */
	public static final float MIN_PAIR = 150.0F;
	public static final float HUNGRY_PAIR = 1200.0F;
	/** Ticks before the pair just drawn may be drawn again. */
	public static final int PAIR_COOLDOWN = 400;
	public static final float TIDE_RETURN = 0.5F;
	/** Wells Answer ends Gorged when it drew this share of the meter's maximum. */
	public static final float GORGED_DRAW = 0.12F;
	/** The pit ring: he drinks only standing within this of its centre. */
	public static final double PIT_RADIUS = 5.0D;

	private HemorathWellRules() {
	}

	/** What one tick of held Absorption takes from a well: its rate, the well's content and the player's room. */
	public static float absorb(float content, double itemRate, double room) {
		return (float) Math.max(0.0D, Math.min(Math.min(content, ABSORB), Math.min(itemRate, room)));
	}

	/** What one tick of held Projection may put into a well: its rate, the well's room and what the player holds. */
	public static float project(float content, double itemRate, double held) {
		return (float) Math.max(0.0D, Math.min(Math.min(CAPACITY - content, PROJECT), Math.min(itemRate, held)));
	}

	/** Each tick of a draw takes this much from a well that held {@code atStart} when the draw began. */
	public static float drawPerTick(float atStart) {
		return Math.max(0.0F, atStart) / DRAW_TICKS;
	}

	public static float heal(float drawn) {
		return Math.max(0.0F, drawn) / DRAWN_PER_HEALTH;
	}

	/** Denial: a player absorbing a drawn well takes their share first; he gets what is left of the tick's flow. */
	public static float afterDenial(float flow, float absorbed) {
		return Math.max(0.0F, flow - absorbed);
	}

	public static float litany(float content) {
		return Math.min(CAPACITY, content + LITANY_REFILL);
	}

	/** Each of the four wells' share of a drained tide. */
	public static float tideReturnEach(float tideHeld) {
		return Math.max(0.0F, tideHeld) * TIDE_RETURN / WELLS;
	}

	/** A spoke shows its well's content: 0 below 200, 1 below 500, 2 below 800, 3 above. */
	public static int spokeFill(float content) {
		if (content < 200.0F) return 0;
		if (content < 500.0F) return 1;
		if (content < 800.0F) return 2;
		return 3;
	}

	/** The pit ring shows his meter: 0 below 35%, 1 to 70%, 2 to 90%, 3 above. */
	public static int pitFill(float fraction) {
		if (fraction < HemorathMeterRules.FILLING_FROM) return 0;
		if (fraction < HemorathMeterRules.BRIMMING_FROM) return 1;
		if (fraction < 0.90F) return 2;
		return 3;
	}

	/** One well's content, packed 0..255 of capacity into a byte of the synced word. */
	public static int pack(float[] wells) {
		int word = 0;
		for (int i = 0; i < Math.min(WELLS, wells.length); i++) {
			int level = Math.max(0, Math.min(255, Math.round(wells[i] / CAPACITY * 255.0F)));
			word |= level << (i * 8);
		}
		return word;
	}

	public static float unpack(int word, int well) {
		return ((word >>> (well * 8)) & 0xFF) / 255.0F * CAPACITY;
	}
}
