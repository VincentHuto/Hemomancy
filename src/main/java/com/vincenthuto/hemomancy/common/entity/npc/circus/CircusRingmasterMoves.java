package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;
import com.vincenthuto.hemomancy.common.entity.boss.BossFootprint;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * The Ringmaster's finale moves (NQ-15-4 A): the Circus's mental tools on the boss kit. His knives and staff blow stay;
 * the five mental moves are the Spotlight, Sleight, the Cue, the Knife Board and Strings Cut. Each move is a
 * {@link BossMoveTimeline} on the kit's floors (12 ticks for a melee or thrown strike, 20 for an area move) and strikes a
 * {@link BossFootprint}, which the server tests and the telegraph draws. Pure, so it is JVM-tested.
 */
public final class CircusRingmasterMoves {
	public static final int MELEE_FLOOR = 12;
	public static final int AREA_FLOOR = 20;
	/** Ticks between his moves while he is perched (RAFTERS) or presenting (CAROUSEL). */
	public static final int RAFTERS_BEAT = 35;
	public static final int CAROUSEL_BEAT = 50;
	/** Ticks he waits after a move in DESCENT before choosing the next. */
	public static final int DESCENT_COOLDOWN = 16;
	public static final float STAFF_DAMAGE = 7.0F;
	public static final float BOARD_DAMAGE = 6.0F;
	public static final float SLEIGHT_DAMAGE = 7.0F;
	public static final float LENT_DAMAGE = 6.0F;
	/** The Spotlight roots a player who looks away from him for this long. */
	public static final int SPOTLIGHT_ROOT_TICKS = 30;
	/** Looking more than this far from him is looking away. */
	public static final double LOOK_AWAY_DEGREES = 60.0D;
	/** How far round the player Sleight sets him and his two doubles. */
	public static final double SLEIGHT_RING = 2.4D;
	/** The doubles stand this long after Sleight begins, then come apart into thread. */
	public static final int DOUBLES_TICKS = 80;
	/** On Sleight's wind-up tick he swaps places with one double. */
	public static final int SLEIGHT_SWAP_TICK = 10;
	public static final int FLAME_TICKS = 60;

	private static final BossFootprint STAFF = new BossFootprint.Cone(0.0D, 2.6D, 100.0D, 0.0D,
			BossFootprint.Height.STANDING);
	private static final BossFootprint SPOTLIGHT = new BossFootprint.Disc(BossFootprint.Anchor.AIM, 2.5D,
			BossFootprint.Height.STANDING);
	private static final BossFootprint BOARD = new BossFootprint.Disc(BossFootprint.Anchor.AIM, 1.8D,
			BossFootprint.Height.STANDING);
	private static final BossFootprint THRUST = new BossFootprint.Cone(0.0D, 3.2D, 70.0D, 0.0D,
			BossFootprint.Height.STANDING);

	private CircusRingmasterMoves() {
	}

	public enum Kind { MELEE, AREA }

	public enum Move {
		NONE(null, Kind.MELEE),
		/** A knife thrown from the hand; the ranged answer to a player he cannot reach. */
		KNIFE_THROW(new BossMoveTimeline(12, 1, 5), Kind.MELEE),
		/** The staff brought down in a wedge in front of him. */
		STAFF_STRIKE(new BossMoveTimeline(12, 1, 6), Kind.MELEE),
		/** A pool of attention on the player; whoever looks away from him in it is rooted. */
		SPOTLIGHT(new BossMoveTimeline(20, 40, 8), Kind.AREA),
		/** He and two doubles stand round the player and swap; only he is real, only his thrust lands. */
		SLEIGHT(new BossMoveTimeline(20, 1, 12), Kind.AREA),
		/** Every standing performer strikes on the one beat he calls. */
		CUE(new BossMoveTimeline(20, 1, 8), Kind.AREA),
		/** Knives outline the player; a dash out of the outline before they close breaks it. */
		KNIFE_BOARD(new BossMoveTimeline(20, 1, 8), Kind.AREA),
		/** He pulls the thread of a downed performer and performs its act himself. */
		STRINGS_CUT(new BossMoveTimeline(20, 1, 10), Kind.AREA);

		@Nullable
		private final BossMoveTimeline timeline;
		private final Kind kind;

		Move(@Nullable BossMoveTimeline timeline, Kind kind) {
			this.timeline = timeline;
			this.kind = kind;
		}

		@Nullable
		public BossMoveTimeline timeline() {
			return timeline;
		}

		public Kind kind() {
			return kind;
		}

		/** The kit's wind-up floor for this kind of move. */
		public int floor() {
			return kind == Kind.MELEE ? MELEE_FLOOR : AREA_FLOOR;
		}

		public static Move byId(int id) {
			Move[] values = values();
			return id >= 0 && id < values.length ? values[id] : NONE;
		}
	}

	/** The act a downed performer lends him under Strings Cut, by the performer who lends it. */
	public enum LentAct {
		/** The Acrobat's vault: he lands on the player. */
		VAULT(new BossFootprint.Disc(BossFootprint.Anchor.AIM, 2.5D, BossFootprint.Height.STANDING)),
		/** The Knife Thrower's fan of three knives down a narrow lane. */
		KNIVES(new BossFootprint.Cone(0.0D, 12.0D, 20.0D, 0.0D, BossFootprint.Height.STANDING)),
		/** The Fire Eater's breath. */
		FLAME(new BossFootprint.Cone(0.0D, 5.0D, 55.0D, 0.0D, BossFootprint.Height.STANDING)),
		/** The Stilt Walker's slam where the player stands. */
		STILTS(new BossFootprint.Disc(BossFootprint.Anchor.AIM, 3.0D, BossFootprint.Height.STANDING));

		private final BossFootprint footprint;

		LentAct(BossFootprint footprint) {
			this.footprint = footprint;
		}

		public BossFootprint footprint() {
			return footprint;
		}

		@Nullable
		public static LentAct of(CircusPerformerEntity performer) {
			if (performer instanceof CircusAcrobatEntity) return VAULT;
			if (performer instanceof CircusKnifeThrowerEntity) return KNIVES;
			if (performer instanceof CircusFireEaterEntity) return FLAME;
			if (performer instanceof CircusStiltWalkerEntity) return STILTS;
			return null;
		}

		@Nullable
		public static LentAct byName(String name) {
			for (LentAct act : values()) if (act.name().equals(name)) return act;
			return null;
		}
	}

	/** The ground a move strikes, or null for a move whose hit is not a footprint of his (knives, the Cue). */
	@Nullable
	public static BossFootprint footprint(Move move, @Nullable LentAct lent) {
		return switch (move) {
			case STAFF_STRIKE -> STAFF;
			case SPOTLIGHT -> SPOTLIGHT;
			case KNIFE_BOARD -> BOARD;
			case SLEIGHT -> THRUST;
			case STRINGS_CUT -> lent == null ? null : lent.footprint();
			default -> null;
		};
	}

	/** Whether a player looking along {@code look} is looking away from him, seen along {@code toRingmaster}. */
	public static boolean looksAway(Vec3 look, Vec3 toRingmaster) {
		double lengths = look.length() * toRingmaster.length();
		if (lengths < 1.0E-6D) return false;
		double cosine = Math.max(-1.0D, Math.min(1.0D, look.dot(toRingmaster) / lengths));
		return Math.toDegrees(Math.acos(cosine)) > LOOK_AWAY_DEGREES;
	}

	/**
	 * Perched in the rafters he throws on every beat, and every third beat is a mental move: the Spotlight, the Knife
	 * Board, then the Cue while any performer stands.
	 */
	public static Move rafters(int beat, int standing) {
		if (Math.floorMod(beat, 3) != 2) return Move.KNIFE_THROW;
		return switch (Math.floorMod(beat / 3, 3)) {
			case 0 -> Move.SPOTLIGHT;
			case 1 -> Move.KNIFE_BOARD;
			default -> standing > 0 ? Move.CUE : Move.SPOTLIGHT;
		};
	}

	/** Presenting on the carousel he calls the Cue while the troupe stands, and keeps the player's eye between. */
	public static Move carousel(int beat, int standing) {
		if (standing > 0 && Math.floorMod(beat, 2) == 0) return Move.CUE;
		return Math.floorMod(beat, 4) == 1 ? Move.KNIFE_BOARD : Move.SPOTLIGHT;
	}

	/**
	 * On foot in the ring. A player he cannot reach gets knives. While a downed performer has an act to lend he reaches
	 * for it two times in six. Otherwise the band chooses: up close the staff, Sleight or the Board; at middle range
	 * the Board, the Spotlight, Sleight or a knife; far off knives and the Spotlight.
	 */
	public static Move descent(BossBands.Band band, boolean reachable, int lent, long seed) {
		if (!reachable) return Move.KNIFE_THROW;
		int roll = (int) Math.floorMod(seed, 6);
		if (lent > 0 && band != BossBands.Band.FAR && roll < 2) return Move.STRINGS_CUT;
		return switch (band) {
			case HUG, NEAR -> roll < 3 ? Move.STAFF_STRIKE : roll < 5 ? Move.SLEIGHT : Move.KNIFE_BOARD;
			case MID -> roll < 2 ? Move.KNIFE_BOARD : roll < 3 ? Move.SPOTLIGHT : roll < 5 ? Move.SLEIGHT : Move.KNIFE_THROW;
			case FAR -> roll < 4 ? Move.KNIFE_THROW : Move.SPOTLIGHT;
		};
	}
}
