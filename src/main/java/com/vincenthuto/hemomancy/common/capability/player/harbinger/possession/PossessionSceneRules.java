package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

import java.util.ArrayList;
import java.util.List;

/**
 * The tick table of possession's cinematics. A possession begins and ends as a staged sequence: two scenes split by a
 * cut, where the server carries the will across dimensions, and a settle tick where the body is handed over (control
 * swap on arrival, re-manifest on a return). The server runs its steps from this table and the client draws its
 * scenes from the same numbers, so the visuals land on the server's ticks. Pure, so both sides and the tests agree.
 */
public final class PossessionSceneRules {
	/** One half of a sequence, as the client plays it: its length in ticks. */
	public enum Scene {
		DEPARTURE(110),
		ARRIVAL(80),
		BROKEN_GARDENS(40),
		BROKEN_RITE(70),
		CLEAN_GARDENS(50),
		CLEAN_RITE(90);

		private final int length;

		Scene(int length) {
			this.length = length;
		}

		public int length() {
			return length;
		}

		/** Whether this half plays at the rite space (around the echo) rather than in the Gardens. */
		public boolean atRite() {
			return this == DEPARTURE || this == BROKEN_RITE || this == CLEAN_RITE;
		}

		public static Scene byOrdinal(int ordinal) {
			Scene[] all = values();
			return ordinal >= 0 && ordinal < all.length ? all[ordinal] : null;
		}
	}

	/** A staged server sequence: a beginning, or one of the two returns. */
	public enum Sequence {
		BEGIN(Scene.DEPARTURE, Scene.ARRIVAL, ARRIVAL_CONTROL),
		RETURN_BROKEN(Scene.BROKEN_GARDENS, Scene.BROKEN_RITE, BROKEN_REMANIFEST_END),
		RETURN_CLEAN(Scene.CLEAN_GARDENS, Scene.CLEAN_RITE, CLEAN_REMANIFEST_END);

		private final Scene first;
		private final Scene second;
		private final int settleInSecond;

		Sequence(Scene first, Scene second, int settleInSecond) {
			this.first = first;
			this.second = second;
			this.settleInSecond = settleInSecond;
		}

		public Scene first() {
			return first;
		}

		public Scene second() {
			return second;
		}

		/** The sequence tick the server carries the will across: the end of the first scene. */
		public int cut() {
			return first.length();
		}

		/** The sequence tick the body is handed over; the server's sequence is finished here. */
		public int settle() {
			return cut() + settleInSecond;
		}

		public static Sequence returning(boolean broken) {
			return broken ? RETURN_BROKEN : RETURN_CLEAN;
		}
	}

	/** What the server does at a sequence tick. */
	public enum Step {
		/** Begin: inventory, attachment and teleport into the Gardens. Return: the finish and the teleport home. */
		CROSS,
		/** Begin: the scene facsimile goes and the player takes the body. Return: the echo goes and the player stands. */
		SETTLE
	}

	// Departure, in scene ticks: the body fades into the echo, the daemon flies into the surrogate, both dissolve, the
	// floor's tendrils close over the spores and take them down, then black for the crossing.
	public static final int DEPARTURE_FADE_END = 40;
	public static final int DEPARTURE_DAEMON_START = 40;
	public static final int DEPARTURE_DAEMON_ARRIVES = 70;
	public static final int DEPARTURE_DISSOLVE_END = 84;
	public static final int DEPARTURE_TENDRILS_RISE = 72;
	public static final int DEPARTURE_TENDRILS_CLOSE = 92;
	public static final int DEPARTURE_BLACK = 100;

	// Arrival: fade in, tendrils seize the facsimile, inject, release; control at ARRIVAL_CONTROL.
	public static final int ARRIVAL_FADE_IN = 12;
	public static final int ARRIVAL_SEIZE_END = 30;
	public static final int ARRIVAL_INJECT_END = 58;
	public static final int ARRIVAL_RELEASE_END = 68;
	public static final int ARRIVAL_CONTROL = 72;

	// Broken return: the body bursts, hyphae reabsorb the spill; at the rite the floor blasts the daemon out of the
	// tendrils and into the echo, and the player is thrown back standing.
	public static final int BROKEN_BURST = 6;
	public static final int BROKEN_REABSORB_END = 34;
	public static final int BROKEN_BLACK = 30;
	public static final int BROKEN_ERUPT_END = 22;
	public static final int BROKEN_BLAST = 24;
	public static final int BROKEN_SLAM = 36;
	public static final int BROKEN_REMANIFEST_END = 48;

	// Clean return: the body stammers and collapses into goo; at the rite spores leak from the tendrils and re-form the
	// daemon, which drifts down into the echo, and the player calmly re-manifests.
	public static final int CLEAN_STAMMER_END = 20;
	public static final int CLEAN_COLLAPSE_END = 30;
	public static final int CLEAN_BLACK = 40;
	public static final int CLEAN_TENDRILS_RISE_END = 20;
	public static final int CLEAN_REFORM_END = 70;
	public static final int CLEAN_DRIFT_END = 78;
	public static final int CLEAN_REMANIFEST_END = 88;

	/** Ticks the rite-half black takes to lift after a cut. */
	public static final int FADE_IN = 10;

	private PossessionSceneRules() {
	}

	/** The sequence tick a step runs at. */
	public static int tick(Sequence sequence, Step step) {
		return step == Step.CROSS ? sequence.cut() : sequence.settle();
	}

	/**
	 * The steps whose tick falls in {@code (from, to]}, in order. A sequence starts at {@code from = -1}; a skip asks for
	 * everything left with {@code to = settle}, which runs the same steps in the same order as letting it play.
	 */
	public static List<Step> due(Sequence sequence, int from, int to) {
		List<Step> steps = new ArrayList<>(2);
		for (Step step : Step.values()) {
			int at = tick(sequence, step);
			if (at > from && at <= to) steps.add(step);
		}
		return steps;
	}

	/** Whether the sequence is finished at {@code t}: its last step has run. */
	public static boolean finished(Sequence sequence, int t) {
		return t >= sequence.settle();
	}

	/** The scene playing at sequence tick {@code t}. */
	public static Scene sceneAt(Sequence sequence, int t) {
		return t < sequence.cut() ? sequence.first() : sequence.second();
	}

	/**
	 * The echo's manifest at sequence tick {@code t}: 1 is the player's own skin, 0 the shadow. A departure fades the
	 * body into the shadow; a return leaves it dark until the rite half brings the skin back by the settle tick.
	 */
	public static float manifest(Sequence sequence, int t) {
		return switch (sequence) {
			case BEGIN -> 1.0F - HemoEasing.clamp01(t / (float) DEPARTURE_FADE_END);
			case RETURN_BROKEN -> ramp(t - sequence.cut(), BROKEN_SLAM, BROKEN_REMANIFEST_END);
			case RETURN_CLEAN -> ramp(t - sequence.cut(), CLEAN_DRIFT_END, CLEAN_REMANIFEST_END);
		};
	}

	private static float ramp(float t, float start, float end) {
		return HemoEasing.clamp01((t - start) / (end - start));
	}
}
