package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintCompanion;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintEnrageRules;

/**
 * The frozen congregation's rules (design section 9): waking a figure with held Projection (40 fed ticks at 3 blood,
 * a 4-tick grace, then a half-speed bleed), pinning one with hyphae in a forced fight (30 at 2), the doubled cost and
 * lockout after a recall, whose side a woken figure takes (the awakening decides), the caps, the rises at each floor,
 * and what an awake devotee does. JVM-tested.
 */
public final class VelorumCongregationRules {
	public static final int WAKE_TICKS = 40;
	public static final float WAKE_RATE = 3.0F;
	public static final int PIN_TICKS = 30;
	public static final float PIN_RATE = 2.0F;
	public static final int GRACE_TICKS = 4;
	/** After the grace a figure loses fed ticks at half the speed it gained them. */
	public static final float BLEED = 0.5F;
	public static final int RECALL_LOCKOUT = 300;
	public static final int RECALL_COST_FACTOR = 2;
	public static final int BASE_CAP = 4;
	public static final int RISES_PER_FLOOR = 2;
	public static final int CALL_RISES = 2;
	public static final float HEALTH = 30.0F;
	public static final double ARMOR = 2.0D;
	public static final float BLOW = 4.0F;
	public static final int BLOW_GAP = 30;
	public static final double GRIP_RANGE = 1.5D;
	public static final int GRIP_COUNT = 2;
	public static final double SHELTER_RANGE = 3.0D;
	public static final int HOSTILE_GRIP_TICKS = 40;
	public static final int THAW_TICKS = 30;
	public static final int THAW_PROSTRATE_TICKS = 40;
	public static final int STILL_TICKS = 30;
	/** A figure's pose index as the block stores it: kneeling, bowed, reaching, prostrate, striding. */
	public static final int PROSTRATE = 3;
	/** Recall reaches an ally within this distance in his sight. */
	public static final double RECALL_RANGE = 14.0D;

	/** What a figure is now. */
	public enum State { FROZEN, PINNED, AWAKE, STILLED, HOME }

	private VelorumCongregationRules() {
	}

	/** A forced awakening's congregation turns on the challengers; a proper one or none fights beside them. */
	public static SaintCompanion.Allegiance allegiance(String awakening) {
		return SaintEnrageRules.enraged(awakening) ? SaintCompanion.Allegiance.SAINT : SaintCompanion.Allegiance.CHALLENGERS;
	}

	/** At most four awake on either side, plus one per challenger after the first. */
	public static int cap(int challengers) {
		return BASE_CAP + Math.max(0, challengers - 1);
	}

	/** Fed ticks a figure needs: 40 to wake, doubled after a recall. */
	public static int wakeTicks(boolean recalled) {
		return recalled ? WAKE_TICKS * RECALL_COST_FACTOR : WAKE_TICKS;
	}

	/**
	 * One tick of a figure's feeding: +1 while fed; after {@code GRACE_TICKS} unfed it bleeds back at half speed. The
	 * result is never negative. {@code idle} is how many ticks it has gone unfed.
	 */
	public static float feed(float fed, boolean feeding, int idle) {
		if (feeding) return fed + 1.0F;
		if (idle <= GRACE_TICKS) return fed;
		return Math.max(0.0F, fed - BLEED);
	}

	/** Whether a figure may be woken now: frozen or stilled, and past any recall lockout. */
	public static boolean wakeable(State state, long now, long lockoutUntil) {
		return (state == State.FROZEN || state == State.STILLED) && now >= lockoutUntil;
	}

	public static int thawTicks(int pose) {
		return pose == PROSTRATE ? THAW_PROSTRATE_TICKS : THAW_TICKS;
	}

	// ── the congregation as his source (revision 2026-11-06, R.8) ────

	/** Frozen and stilled figures feed his veins; pinned figures (hyphae shut them) and the awake do not. */
	public static boolean feedsVeins(State state) {
		return state == State.FROZEN || state == State.STILLED;
	}

	/** A woken ally's veins run bright for 6 ticks as the rime sheds: warm blood into frozen cruor. */
	public static final int WARM_VEINS = 6;
	/** Veins of Frost draws on at most this many figures. */
	public static final int MAX_VEINS = 4;
	/** Hostile devotees move x1.25 in the dark, and their grip fills +2.0 a tick there. */
	public static final double DARK_SPEED = 1.25D;
	public static final float DARK_GRIP_FILL = 2.0F;

	public static float gripFill(boolean dark) {
		return dark ? DARK_GRIP_FILL : VelorumStillnessRules.GRIP_FILL;
	}
}
