package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

import javax.annotation.Nullable;
import java.util.Locale;

/**
 * The numbers of the brood (core-loop round two, P6): two kept creatures of one kind in vessels side by side, fed past
 * a Vigor mark, bear one young into an empty vessel beside them. The keeper asks for it on one vessel's screen; that
 * vessel is the bearer. Each fed second the bearer's brood takes a share of its cost from the bearer's living buffer,
 * or from its mate's, so the young is paid for in blood the keeper projected or sutured in. Nothing is stored for it:
 * parting the pair, breaking a vessel or letting a parent fall to a husk loses what was paid.
 *
 * Pure: no Minecraft types. {@link SpecimenBrood} runs it in the world.
 */
public final class BroodRules {
	/** Both parents must hold at least this much Vigor for the brood to take its second's share. */
	public static final double BROOD_VIGOR = 75.0D;
	/** The young costs this much buffer blood per point of a parent's maximum health, and never less than the floor. */
	public static final double ML_PER_HEALTH = 30.0D;
	public static final double MIN_COST = 150.0D;
	/** Fed seconds a brood takes from the press to term. */
	public static final int BROOD_SECONDS = 120;
	/** Every this many paid seconds, each parent pays this much Vigor on top of its own upkeep. */
	public static final int VIGOR_DRAIN_EVERY = 10;
	public static final double VIGOR_DRAIN = 1.0D;
	/** At the birth each parent loses this much Vigor, and both rest this long before they may pair again. */
	public static final double BIRTH_VIGOR = 20.0D;
	public static final long REST_TICKS = 6000L;
	/** The young starts weak, and as a baby where its kind has one. */
	public static final double YOUNG_VIGOR = 40.0D;
	public static final int YOUNG_AGE = -24000;
	/** When only one parent carries an inherited school, the young carries it this often. */
	public static final double INHERIT_CHANCE = 0.5D;
	/** A menu sends shorts: the largest brood cost a screen is told. */
	public static final int MAX_SHOWN_COST = 32000;

	private BroodRules() {
	}

	/** What a young of a creature with {@code maxHealth} costs in buffer blood: a cow 300 mL, a blaze 600. */
	public static double broodCost(float maxHealth) {
		return Math.max(MIN_COST, ML_PER_HEALTH * Math.max(0.0F, maxHealth));
	}

	/** One fed second's share of {@code cost}. */
	public static double perSecond(double cost) {
		return cost / BROOD_SECONDS;
	}

	/** Whether both parents are fed well enough for the brood to take its share this second. */
	public static boolean gestates(double vigorA, double vigorB) {
		return vigorA >= BROOD_VIGOR && vigorB >= BROOD_VIGOR;
	}

	/** How far along a brood is, 0 to 1. */
	public static double progress(double paid, double cost) {
		if (cost <= 0.0D) return 1.0D;
		return Math.max(0.0D, Math.min(1.0D, paid / cost));
	}

	/** Whether {@code paid} covers the whole cost, allowing for rounding in the shares. */
	public static boolean atTerm(double paid, double cost) {
		return paid >= cost - 1.0E-6D;
	}

	/** This second's share: the rest of the cost when less than a full share is left. */
	public static double share(double paid, double cost) {
		return Math.max(0.0D, Math.min(perSecond(cost), cost - paid));
	}

	/** Whole seconds paid so far, counted the way the shares are taken. */
	public static int paidSeconds(double paid, double cost) {
		double each = perSecond(cost);
		return each <= 0.0D ? 0 : (int) Math.floor(paid / each + 1.0E-6D);
	}

	/** Whether the second that brought the paid seconds to {@code paidSeconds} also costs each parent Vigor. */
	public static boolean drainsVigor(int paidSeconds) {
		return paidSeconds > 0 && paidSeconds % VIGOR_DRAIN_EVERY == 0;
	}

	/**
	 * The school the young carries: the shared one when both parents carry it; when only one parent does, that one
	 * when {@code roll} is under {@link #INHERIT_CHANCE}; when they carry different ones, the bearer's on a roll under
	 * one half and the mate's otherwise.
	 */
	@Nullable
	public static EnumBloodTemper inherits(@Nullable EnumBloodTemper bearer, @Nullable EnumBloodTemper mate, double roll) {
		if (bearer == null && mate == null) return null;
		if (bearer != null && bearer == mate) return bearer;
		if (bearer != null && mate != null) return roll < 0.5D ? bearer : mate;
		return roll < INHERIT_CHANCE ? (bearer != null ? bearer : mate) : null;
	}

	/** Where a vessel's brood stands. */
	public enum State {
		NONE, GESTATING, PAUSED, AT_TERM, RESTING;

		public String key() {
			return "message.hemomancy.menagerie.brood.state." + name().toLowerCase(Locale.ROOT);
		}
	}

	/** A paired vessel gestates while fed and waits at term once paid; an unpaired one rests or stands idle. */
	public static State state(boolean paired, double paid, double cost, boolean fed, long now, long restUntil) {
		if (!paired) return now < restUntil ? State.RESTING : State.NONE;
		if (atTerm(paid, cost)) return State.AT_TERM;
		return fed ? State.GESTATING : State.PAUSED;
	}

	/** Why a press of "Let them breed" did nothing; {@link #NONE} when the pair was made. */
	public enum Refusal {
		NONE, NOT_YOURS, PRESERVED, HUSK, UNDEAD, CANNOT_BREED, RESTING, NO_MATE, OTHER_KEEPER, MATE_HUSK, MATE_PAIRED,
		MATE_RESTING;

		public String key() {
			return "message.hemomancy.menagerie.brood.refused." + name().toLowerCase(Locale.ROOT);
		}

		public static Refusal byOrdinal(int ordinal) {
			Refusal[] values = values();
			return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NONE;
		}
	}

	/**
	 * The pressing vessel's own answer: it must be kept, the presser must be its keeper or of their bloodline, its
	 * creature must live, be of a kind that breeds, and not be resting.
	 */
	public static Refusal ownSide(boolean owned, boolean mayDraw, boolean living, boolean undead, boolean noBrood,
			boolean resting) {
		if (!owned) return Refusal.PRESERVED;
		if (!mayDraw) return Refusal.NOT_YOURS;
		if (!living) return Refusal.HUSK;
		if (undead) return Refusal.UNDEAD;
		if (noBrood) return Refusal.CANNOT_BREED;
		if (resting) return Refusal.RESTING;
		return Refusal.NONE;
	}

	/** A neighbour of the same kind as a mate: the same keeper, alive, unpaired and not resting. */
	public static Refusal mateSide(boolean sameKeeper, boolean living, boolean paired, boolean resting) {
		if (!sameKeeper) return Refusal.OTHER_KEEPER;
		if (!living) return Refusal.MATE_HUSK;
		if (paired) return Refusal.MATE_PAIRED;
		if (resting) return Refusal.MATE_RESTING;
		return Refusal.NONE;
	}
}
