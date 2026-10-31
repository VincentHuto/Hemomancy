package com.vincenthuto.hemomancy.common.entity.boss.court;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The Bloom Warden's pure rules (Brined Court Track 10, Tier 2): which wrongs against the Giving Light wake it, how one
 * act is told from the next, its rooted and uprooted cycle, its moves, how its blooms shield and heal it, and when it
 * re-anchors. The Warden is a consequence, never loot: nothing here rewards the player.
 */
public final class BloomWardenRules {
	/** Breaking the Garden's growth, beds, arbor growth or moss. */
	public static final String GROWTH = "growth";
	/** Taking from the undercroft: Marimo's moss, the petrified, anything set there as his. */
	public static final String UNDERCROFT = "undercroft";
	/** Raising harm at a resident of the Garden; residents are protected, so the attempt counts. */
	public static final String HARM = "harm";
	/** Taking a gift a second time or food meant for others; no such path exists until a shared table does. */
	public static final String SECOND_GIFT = "second_gift";
	public static final List<String> ACTS = List.of(GROWTH, UNDERCROFT, HARM, SECOND_GIFT);

	/** The same wrong repeated within this many ticks is one act (a sweep across three residents, a held break). */
	public static final int ACT_DEBOUNCE = 40;

	public static final float MAX_HEALTH = 160.0F;
	/** How long the opening scene holds the Warden passive and untouchable. */
	public static final int INTRO = 60;
	/** Ticks rooted among its blooms before it uproots to move. */
	public static final int ROOTED_TICKS = 160;
	/** Ticks it walks uprooted before it roots again and sows. */
	public static final int UPROOTED_TICKS = 100;
	/** Ticks it takes to sink back into the tier when the fight ends. */
	public static final int REANCHOR_TICKS = 40;
	/** A target this long missing (logged off, too far, unloaded) ends the fight without an outcome. */
	public static final int TARGET_GRACE = 100;
	public static final double LEASH = 32.0D;

	/** Most blooms one Warden keeps at once, and how far from it they spread. */
	public static final int MAX_BLOOMS = 12;
	public static final int BLOOM_RADIUS = 9;
	public static final int SOW_COUNT = 3;
	/** Health one fed bloom gives back each second, and the most all of them may give. */
	public static final float BLOOM_HEAL = 0.75F;
	public static final float MAX_HEAL_PER_SECOND = 4.0F;
	/** What a hit is worth while the Warden is rooted behind fed blooms. */
	public static final float ROOTED_SHIELD = 0.25F;
	/** Seconds a bloom lasts once its light is cut off. */
	public static final int WITHER_SECONDS = 5;
	/** How far up a bloom looks for open sky or a lamp: the light shaft that feeds it. */
	public static final int SHAFT_HEIGHT = 32;
	public static final int SHAFT_LIGHT = 10;

	/** Root lash: a ring of roots bursting up round its feet. */
	public static final BossMoveTimeline ROOT_LASH = new BossMoveTimeline(20, 4, 16);
	public static final double ROOT_LASH_RADIUS = 4.5D;
	public static final float ROOT_LASH_DAMAGE = 8.0F;
	/** Frond spear: a shaft of gathered light driven along the ground to where the player stood. */
	public static final BossMoveTimeline FROND_SPEAR = new BossMoveTimeline(24, 8, 16);
	public static final double FROND_SPEAR_RANGE = 14.0D;
	public static final double FROND_SPEAR_WIDTH = 1.4D;
	public static final float FROND_SPEAR_DAMAGE = 7.0F;
	/** Sowing: blooms thrown out over the tier floor. */
	public static final BossMoveTimeline SOW = new BossMoveTimeline(16, 4, 12);

	private BloomWardenRules() {
	}

	/** The rooted cycle, then walking uprooted, then sinking back when it is over. */
	public enum Phase {
		ROOTED, UPROOTED, REANCHORING;

		public static Phase byId(int id) {
			Phase[] values = values();
			return id >= 0 && id < values.length ? values[id] : ROOTED;
		}
	}

	/** What the Warden is doing now; NONE between moves. */
	public enum Move {
		NONE(null), ROOT_LASH(BloomWardenRules.ROOT_LASH), FROND_SPEAR(BloomWardenRules.FROND_SPEAR), SOW(BloomWardenRules.SOW);

		@Nullable
		private final BossMoveTimeline timeline;

		Move(@Nullable BossMoveTimeline timeline) {
			this.timeline = timeline;
		}

		@Nullable
		public BossMoveTimeline timeline() {
			return timeline;
		}

		public static Move byId(int id) {
			Move[] values = values();
			return id >= 0 && id < values.length ? values[id] : NONE;
		}
	}

	/** How a Warden's fight ended. Only a won fight and amends are spoken of by the chorus by name. */
	public enum Outcome {
		/** The player wore it down: it re-anchors, Marimo pulses, the chorus names the wrong. */
		REANCHORED,
		/** The player made amends with an offering. */
		RECONCILED,
		/** The player died to it. */
		PLAYER_DIED,
		/** The player left. */
		GONE
	}

	/**
	 * Whether {@code act} at {@code now} is a new wrong, given the outstanding one: anything but a repeat of the same act
	 * inside {@link #ACT_DEBOUNCE}.
	 */
	public static boolean isNewAct(@Nullable String previousAct, long previousTime, String act, long now) {
		return previousAct == null || !previousAct.equals(act) || now - previousTime > ACT_DEBOUNCE || now < previousTime;
	}

	/** Who can wake the Warden: never the shunned (they are never attacked), never Creative or a spectator. */
	public static boolean canWake(boolean shunned, boolean creative, boolean spectator, boolean alive) {
		return alive && !shunned && !creative && !spectator;
	}

	/** A hit's worth: a quarter while rooted behind fed blooms, whole once it uproots or its blooms are cut off. */
	public static float damageTaken(float amount, Phase phase, int fedBlooms) {
		return phase == Phase.ROOTED && fedBlooms > 0 ? amount * ROOTED_SHIELD : amount;
	}

	/** Health the fed blooms give back this second. */
	public static float healPerSecond(int fedBlooms) {
		return Math.min(MAX_HEAL_PER_SECOND, fedBlooms * BLOOM_HEAL);
	}

	/** Whether the next hit would bring it down; it re-anchors instead of dying. */
	public static boolean reanchorsAt(float health, float damage) {
		return health - damage <= 1.0F;
	}

	/** The move it starts now, or NONE: never while the player holds an offering, lash when close, spear at range. */
	public static Move choose(Phase phase, boolean holdsOffering, double distance, int blooms, long seed) {
		if (holdsOffering || phase == Phase.REANCHORING) return Move.NONE;
		if (phase == Phase.ROOTED && blooms < MAX_BLOOMS && Math.floorMod(seed, 3) == 0) return Move.SOW;
		if (distance <= ROOT_LASH_RADIUS) return Move.ROOT_LASH;
		if (distance <= FROND_SPEAR_RANGE) return Move.FROND_SPEAR;
		return phase == Phase.ROOTED && blooms < MAX_BLOOMS ? Move.SOW : Move.NONE;
	}

	/** The chorus's line naming {@code act}. */
	public static String namingKey(@Nullable String act) {
		return "hemomancy.court.warden.named." + (act != null && ACTS.contains(act) ? act : GROWTH);
	}
}
