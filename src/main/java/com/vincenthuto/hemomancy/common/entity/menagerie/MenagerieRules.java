package com.vincenthuto.hemomancy.common.entity.menagerie;

import java.util.Locale;

/**
 * Who a Harbinger may keep, and the numbers of keeping (core-loop Track 5). One rule answers for the three ways of
 * keeping a creature: leading it home on the Tendon Line, a Specimen Jar, and a Specimen Vat. The size of the
 * vessel is a second question, asked by the jar ({@code SpecimenCaptureRules}) and the vat ({@link #vatFits}).
 */
public final class MenagerieRules {
	/** A hostile creature must be at or under this share of its health to be leashed; a passive one at any health. */
	public static final float WEAK_SHARE = 1.0F / 3.0F;
	/** A leash's upkeep is this share of the creature's maximum health in mL a second, never under one. */
	public static final double UPKEEP_PER_HEALTH = 0.25D;
	public static final double MIN_UPKEEP = 1.0D;
	/** A led creature whose keeper is online but further than this, or in another dimension, waits; after a while it breaks free. */
	public static final double FOLLOW_RANGE = 48.0D;
	public static final int ABANDON_TICKS = 600;
	/** Led creatures within this range go through a portal with their keeper. */
	public static final double CARRY_RANGE = 16.0D;
	/**
	 * A Specimen Vat is one vat or several joined into one vessel on a square of vats, one to {@link #VAT_MAX_WIDTH}
	 * across and one to {@link #VAT_MAX_LAYERS} high (a vat is two blocks), drawn as the same octagonal column grown to
	 * its size. A lone vat's column is clear for 0.78 blocks across and 1.5 high; a joined vessel's octagon sits a little
	 * inside its blocks and loses {@link #VAT_WALL_LOSS} across, and each layer above the first adds two blocks of
	 * height.
	 */
	public static final int VAT_MAX_WIDTH = 3;
	public static final int VAT_MAX_LAYERS = 2;
	public static final float VAT_SINGLE_CLEAR_WIDTH = 0.78F;
	public static final float VAT_WALL_LOSS = 0.34F;
	public static final float VAT_CLEAR_HEIGHT = 1.5F;
	/** A body fits when drawn at this share of its size it stands inside the glass. */
	public static final float VAT_MIN_DRAWN_SCALE = 0.75F;
	/** The strongest creature a vessel holds, per vat it is made of. */
	public static final float VAT_HEALTH_PER_VAT = 40.0F;

	private MenagerieRules() {
	}

	public enum Verdict {
		KEEP,
		DEAD,
		NOT_A_CREATURE,
		PLAYER,
		BOSS,
		OWNED,
		SUMMONED,
		WILL,
		PERSON,
		SAINT,
		WARDEN,
		UNKEEPABLE;

		public boolean keeps() {
			return this == KEEP;
		}

		/** The refusal's lang key, or null when nothing is said (a living creature that simply cannot be kept). */
		public String messageKey() {
			return switch (this) {
				case KEEP, DEAD, NOT_A_CREATURE -> null;
				default -> "message.hemomancy.menagerie.refused." + name().toLowerCase(Locale.ROOT);
			};
		}
	}

	/**
	 * What keeping can see of a creature. {@code mob} is a creature with a mind of its own; {@code owned} is tamed or
	 * bound by anyone (the keeper's own pets included); {@code summoned} is a puppeteer's summon; {@code person} is a
	 * villager, trader or any of the mod's people; {@code saint} is a Saint's body or one of its companions;
	 * {@code unkeepable} is any kind in {@code hemomancy:menagerie_unkeepable}.
	 */
	public record Candidate(boolean alive, boolean mob, boolean player, boolean boss, boolean owned, boolean summoned,
			boolean will, boolean person, boolean saint, boolean warden, boolean unkeepable) {
	}

	public static Verdict canKeep(Candidate candidate) {
		if (candidate.player()) return Verdict.PLAYER;
		if (!candidate.alive()) return Verdict.DEAD;
		if (!candidate.mob()) return Verdict.NOT_A_CREATURE;
		if (candidate.boss()) return Verdict.BOSS;
		if (candidate.saint()) return Verdict.SAINT;
		if (candidate.warden()) return Verdict.WARDEN;
		if (candidate.will()) return Verdict.WILL;
		if (candidate.person()) return Verdict.PERSON;
		if (candidate.unkeepable()) return Verdict.UNKEEPABLE;
		if (candidate.summoned()) return Verdict.SUMMONED;
		if (candidate.owned()) return Verdict.OWNED;
		return Verdict.KEEP;
	}

	/** Whether a creature is weak enough to be leashed: any passive one, a hostile one at a third of its health or less. */
	public static boolean weakEnough(float health, float maxHealth, boolean hostile) {
		return !hostile || maxHealth <= 0.0F || health <= maxHealth * WEAK_SHARE + 1.0E-4F;
	}

	/** How many creatures a Harbinger may lead at once (CL-13): none before D3, one at D3, two at D5, three at D7. */
	public static int maxLed(int degree) {
		if (degree >= 7) return 3;
		if (degree >= 5) return 2;
		if (degree >= 3) return 1;
		return 0;
	}

	/** A leash's upkeep in mL a second, by the creature's maximum health. */
	public static double upkeepPerSecond(float maxHealth) {
		return Math.max(MIN_UPKEEP, Math.max(0.0F, maxHealth) * UPKEEP_PER_HEALTH);
	}

	/** A vessel's clear width, in blocks, by how many vats it is across. */
	public static float vatClearWidth(int width) {
		return width <= 1 ? VAT_SINGLE_CLEAR_WIDTH : width - VAT_WALL_LOSS;
	}

	/** A vessel's clear height, in blocks, by how many vats it is high. */
	public static float vatClearHeight(int layers) {
		return VAT_CLEAR_HEIGHT + 2.0F * (Math.max(1, layers) - 1);
	}

	/** Whether a body fits a vessel {@code width} vats across and {@code layers} vats high. */
	public static boolean vatFits(float bodyWidth, float bodyHeight, float maxHealth, int width, int layers) {
		return bodyWidth * VAT_MIN_DRAWN_SCALE <= vatClearWidth(width) + 1.0E-4F
				&& bodyHeight * VAT_MIN_DRAWN_SCALE <= vatClearHeight(layers) + 1.0E-4F
				&& maxHealth <= VAT_HEALTH_PER_VAT * width * width * layers + 1.0E-4F;
	}

	/**
	 * The smallest vessel a body fits, as {width, layers}, the fewest vats first; null when even the largest vessel
	 * is too small. For telling a keeper what to build.
	 */
	public static int[] smallestVat(float bodyWidth, float bodyHeight, float maxHealth) {
		int[] best = null;
		for (int width = 1; width <= VAT_MAX_WIDTH; width++) {
			for (int layers = 1; layers <= VAT_MAX_LAYERS; layers++) {
				if (!vatFits(bodyWidth, bodyHeight, maxHealth, width, layers)) continue;
				if (best == null || width * width * layers < best[0] * best[0] * best[1]) best = new int[]{width, layers};
			}
		}
		return best;
	}
}
