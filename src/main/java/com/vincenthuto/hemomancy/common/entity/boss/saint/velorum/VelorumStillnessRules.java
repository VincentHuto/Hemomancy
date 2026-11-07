package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

/**
 * The stillness meter's pure rules (design section 4). Every challenger carries one: it climbs while they stand,
 * sneak, use an item or are slowed, and only travel brings it down. At 40 healing halves, at 70 it stops and the player
 * is slowed (which feeds the meter), at 100 they are preserved for 30 ticks. JVM-tested.
 */
public final class VelorumStillnessRules {
	public static final float MAX = 100.0F;
	public static final float COLD = 40.0F;
	public static final float STILLING = 70.0F;
	/** Under 0.08 blocks of horizontal travel a tick counts as standing, on the ground or in the air. */
	public static final double STILL_TRAVEL = 0.08D;

	public static final float STAND_FILL = 0.70F;
	public static final float ITEM_FILL = 0.30F;
	public static final float SLOW_FILL = 0.10F;
	public static final float RIME_FILL = 0.05F;
	public static final float WALK_DRAIN = 0.55F;
	public static final float SPRINT_DRAIN = 0.90F;
	public static final float PRESENCE_FILL = 1.5F;
	public static final float PRESENCE_DRAIN = 0.8F;
	public static final float SHELTER_FILL = 0.5F;
	public static final float HOUR_FILL = 2.0F;
	public static final float HOUR_DRAIN = 0.5F;
	/** During the Bow every meter drains this much more a tick, moving or not. */
	public static final float BOW_DRAIN = 0.8F;
	/** Held Blood Absorption on the Bow's seam drains every meter this much more again. */
	public static final float SEAM_DRAIN = 1.0F;
	/** Bid to Kneel's gaze: each tick the cone holds a challenger. */
	public static final float GAZE_FILL = 1.2F;
	public static final float GAZE_KNEEL = 60.0F;
	/** A hostile devotee's grip. */
	public static final float GRIP_FILL = 1.5F;

	public static final int PRESERVED_TICKS = 30;
	public static final float PRESERVED_DAMAGE = 6.0F;
	/** Velorum's moves deal this much more to a preserved player. */
	public static final float PRESERVED_TAKEN = 1.5F;
	/** The ice breaks early after this much damage taken inside it. */
	public static final float BREAK_DAMAGE = 14.0F;
	/** ... or after this many blows on it from another challenger. */
	public static final int BREAK_HITS = 2;
	/** ... or after this many fed ticks of another challenger's held Projection. */
	public static final int WARM_TICKS = 15;
	public static final float WARM_RATE = 2.0F;
	public static final float RELEASE_VALUE = 50.0F;
	public static final int THAW_TICKS = 60;
	/** After a preservation the meter cannot pass 95 for 200 ticks, so it never locks a player. */
	public static final float CAPPED = 95.0F;
	public static final int CAP_TICKS = 200;

	public static final float COLD_HEALING = 0.5F;
	public static final float STILLING_HEALING = 0.0F;

	/** Flag bits carried in {@code SaintMeter.PlayerState.flags} and the meter packet (bits 0..15). */
	public static final int FLAG_PRESERVED = 1;
	public static final int FLAG_HALTED = 1 << 1;
	public static final int FLAG_SHELTERED = 1 << 2;
	public static final int FLAG_WATCHED = 1 << 3;
	public static final int FLAG_THAWED = 1 << 4;
	public static final int FLAG_RECEDING = 1 << 5;
	public static final int FLAG_KNEELING = 1 << 6;
	/** In the liquid dark (revision 2026-11-06, R.3): the HUD's gloam, fog and muffle, and the meter's x1.4. */
	public static final int FLAG_DARK = 1 << 7;
	/** Gloamed by the Long Night, a Gloam Cut or the Shadow Step's arrival: the dark's sight and sound, not its fill. */
	public static final int FLAG_GLOAMED = 1 << 8;
	/** Marked by Arrested Pulse: the cruor heart glyph left of the vessel. */
	public static final int FLAG_MARKED = 1 << 9;
	/** Inside an Arrested Pulse beat's 12-tick tell. */
	public static final int FLAG_PULSE_TELL = 1 << 10;
	/** Held in an Eclipse shape's grip. */
	public static final int FLAG_GRIPPED = 1 << 11;

	/**
	 * One challenger's tick, as the meter reads it.
	 *
	 * @param travel    horizontal distance since the last tick
	 * @param slowness  Slowness levels (amplifier + 1), 0 with none
	 * @param rime      levels of the RIME school state
	 * @param presence  in his presence with line of sight
	 * @param sheltered within 3 blocks of an awake ally devotee
	 * @param watched   inside a watching figure's gaze (the Beholding deed)
	 * @param halted    an opening is open: every fill is zero, the drain still runs
	 * @param bow       the Bow is open
	 * @param seam      someone is drawing the cold blood out of the Bow's seam
	 * @param hour      the Endless Hour's hold
	 * @param thawed    released from preservation in the last 60 ticks
	 * @param dark      in the liquid dark ({@link VelorumDarkRules#inDark})
	 */
	public record Inputs(double travel, boolean sneaking, boolean sprinting, boolean usingItem, int slowness, int rime,
			boolean presence, boolean sheltered, boolean watched, boolean halted, boolean bow, boolean seam,
			boolean hour, boolean thawed, boolean dark) {
		/** A tick out of the dark. */
		public Inputs(double travel, boolean sneaking, boolean sprinting, boolean usingItem, int slowness, int rime,
				boolean presence, boolean sheltered, boolean watched, boolean halted, boolean bow, boolean seam,
				boolean hour, boolean thawed) {
			this(travel, sneaking, sprinting, usingItem, slowness, rime, presence, sheltered, watched, halted, bow, seam,
					hour, thawed, false);
		}

		public static Inputs standing() {
			return new Inputs(0.0D, false, false, false, 0, 0, false, false, false, false, false, false, false, false);
		}

		public static Inputs walking() {
			return new Inputs(0.2D, false, false, false, 0, 0, false, false, false, false, false, false, false, false);
		}

		public Inputs inDark() {
			return new Inputs(travel, sneaking, sprinting, usingItem, slowness, rime, presence, sheltered, watched, halted,
					bow, seam, hour, thawed, true);
		}
	}

	private VelorumStillnessRules() {
	}

	/** Standing, sneaking (moving or not), or held in a watching gaze without sprinting. */
	public static boolean still(Inputs in) {
		return in.sneaking() || in.travel() < STILL_TRAVEL || in.watched() && !in.sprinting();
	}

	/**
	 * Every fill term summed, then the presence, the dark, the shelter and the Hour multipliers in that order; zero
	 * while halted. The dark multiplies only the standing, sneaking and item-use terms, not the Slowness and Rime ones.
	 */
	public static float fill(Inputs in) {
		if (in.halted() || in.thawed()) return 0.0F;
		float held = still(in) ? STAND_FILL : 0.0F;
		if (in.usingItem()) held += ITEM_FILL;
		float slowed = SLOW_FILL * Math.max(0, in.slowness()) + RIME_FILL * Math.max(0, in.rime());
		if (in.presence()) {
			held *= PRESENCE_FILL;
			slowed *= PRESENCE_FILL;
		}
		float fill = held * VelorumDarkRules.fillFactor(in.dark()) + slowed;
		if (in.sheltered()) fill *= SHELTER_FILL;
		if (in.hour()) fill *= HOUR_FILL;
		return fill;
	}

	/** Only travel lowers it, plus the Bow and the seam; the presence, the dark and the Hour slow it in that order. */
	public static float drain(Inputs in) {
		float drain = still(in) ? 0.0F : in.sprinting() ? SPRINT_DRAIN : WALK_DRAIN;
		if (in.presence()) drain *= PRESENCE_DRAIN;
		drain *= VelorumDarkRules.drainFactor(in.dark());
		if (in.hour()) drain *= HOUR_DRAIN;
		if (in.bow()) drain += BOW_DRAIN;
		if (in.seam()) drain += SEAM_DRAIN;
		return drain;
	}

	public static float drift(Inputs in) {
		return fill(in) - drain(in);
	}

	/** The meter's ceiling this tick: 95 while the post-preservation cap holds. */
	public static float ceiling(boolean capped) {
		return capped ? CAPPED : MAX;
	}

	/** The next value from {@code value}, clamped to 0..ceiling. */
	public static float next(float value, Inputs in, boolean capped) {
		return Math.max(0.0F, Math.min(ceiling(capped), value + drift(in)));
	}

	public static boolean cold(float value) {
		return value >= COLD;
	}

	public static boolean stilling(float value) {
		return value >= STILLING;
	}

	public static boolean preserves(float value) {
		return value >= MAX;
	}

	/** The heal multiplier the meter holds on its player: 1, then 0.5 at Cold, 0 at Stilling. */
	public static float healing(float value) {
		if (stilling(value)) return STILLING_HEALING;
		return cold(value) ? COLD_HEALING : 1.0F;
	}

	/** Whether the ice breaks early: enough damage taken inside it, two blows, or a warming finished. */
	public static boolean iceBreaks(float damageTaken, int blows, int warmedTicks) {
		return damageTaken >= BREAK_DAMAGE || blows >= BREAK_HITS || warmedTicks >= WARM_TICKS;
	}

	/** Ticks to go from {@code from} to {@code to} at a constant drift; -1 when it never arrives. */
	public static int ticksBetween(float from, float to, Inputs in) {
		float drift = drift(in);
		if (drift == 0.0F || Math.signum(to - from) != Math.signum(drift)) return -1;
		return (int) Math.ceil((to - from) / drift - 1.0E-4F);
	}
}
