package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.List;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealProfessor.Intervention;
import net.minecraft.core.BlockPos;

/**
 * The pure rules of a Chamber ordeal (2026-11-17): how far the fight has gone by the puppet's health, which
 * intervention the professor makes next, where a player's stage stands in the Chamber dimension, and what each way an
 * ordeal can end does to the rite.
 */
public final class OrdealRules {
	/** Stages stand on their own grid, far east of the Chamber rooms (which sit at x 0), one row per chamber id. */
	public static final int ORDEAL_X = 8192;
	public static final int SPACING = 256;
	public static final int FLOOR_Y = 2;
	/** An ordeal no one finishes ends here, in ticks: five minutes. */
	public static final int TIME_LIMIT = 6000;

	private OrdealRules() {
	}

	/** How far the fight has gone: the professor's interventions open up and quicken as the puppet weakens. */
	public enum Ramp {
		OPENING, PRESSED, DESPERATE
	}

	/** How an ordeal ended. Only a win completes the rite; every other end fails it. */
	public enum Outcome {
		/** The puppet was beaten. */
		WON,
		/** A killing blow was caught and the player thrown out of the professor's mind. */
		CAUGHT,
		/** The player logged out or otherwise left. */
		LEFT,
		/** The time limit ran out. */
		TIMED_OUT,
		/** The rite's floor or Focus was broken while the player was away. */
		FLOOR_LOST
	}

	public static Ramp ramp(float puppetHealthFraction) {
		if (puppetHealthFraction > 2.0F / 3.0F) return Ramp.OPENING;
		if (puppetHealthFraction > 1.0F / 3.0F) return Ramp.PRESSED;
		return Ramp.DESPERATE;
	}

	/** How many interventions may be under way at once. */
	public static int concurrent(Ramp ramp) {
		return ramp.ordinal() + 1;
	}

	/** The share of each intervention's cooldown that applies: they come round quicker as the fight goes on. */
	public static float cadence(Ramp ramp) {
		return switch (ramp) {
			case OPENING -> 1.0F;
			case PRESSED -> 0.85F;
			case DESPERATE -> 0.7F;
		};
	}

	/** The next intervention: a seeded turn through those open at this ramp, so no two in a row are alike. */
	public static Intervention next(OrdealProfessor professor, Ramp ramp, long tick, long seed) {
		List<Intervention> open = professor.interventions().stream()
				.filter(intervention -> intervention.from().ordinal() <= ramp.ordinal()).toList();
		int start = (int) Math.floorMod(seed * 31L + tick, (long) open.size());
		return open.get(start);
	}

	/** Where a chamber id's stage stands. */
	public static BlockPos stageOrigin(int chamberId) {
		return new BlockPos(ORDEAL_X, FLOOR_Y, SPACING * chamberId);
	}

	public static boolean completesRite(Outcome outcome) {
		return outcome == Outcome.WON;
	}

	/** Whether the professor gives another key: once per in-game day ({@code getDayTime() / 24000}). */
	public static boolean keyRegivenToday(long lastGiftDay, long today) {
		return today > lastGiftDay;
	}
}
