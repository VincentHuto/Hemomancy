package com.vincenthuto.hemomancy.common.rite;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * What stands between the caster and the rite's next step, as translatable lines. The client shows the
 * first new one in the action bar instead of a panel of English text.
 */
public final class CardinalRiteProblems {
	public static final String SIGIL_REQUIRED = "hemomancy.rite.problem.sigil_required";
	public static final String ALLIES = "hemomancy.rite.problem.allies";
	public static final String MEDIUM = "hemomancy.rite.problem.medium";
	public static final String ANCHOR_DAMAGED = "hemomancy.rite.problem.anchor_damaged";
	public static final String SACRIFICE = "hemomancy.rite.problem.sacrifice";
	public static final String FINALE_HELD = "hemomancy.rite.problem.finale_held";
	public static final String RING_HUNGERS = "hemomancy.rite.problem.ring_hungers";
	public static final int MAX_PROBLEMS = 6;

	private CardinalRiteProblems() {
	}

	public record RiteProblem(String key, List<String> args) {
		public RiteProblem {
			key = key == null ? "" : key;
			args = args == null ? List.of() : List.copyOf(args);
		}
	}

	/** The live facts the problems are read from, gathered by the server. */
	public record Facts(CardinalRitePhase phase, List<String> sleepingRequiredSigils, int requiredAllies,
			int availableAllies, boolean mediumReady, int damagedAnchors, int sacrificeOwedMl,
			boolean heldBeforeFinale, int hungryRingAnchors) {
		public Facts {
			sleepingRequiredSigils = sleepingRequiredSigils == null ? List.of() : List.copyOf(sleepingRequiredSigils);
		}

		public Facts(CardinalRitePhase phase, List<String> sleepingRequiredSigils, int requiredAllies,
				int availableAllies, boolean mediumReady, int damagedAnchors, int sacrificeOwedMl,
				boolean heldBeforeFinale) {
			this(phase, sleepingRequiredSigils, requiredAllies, availableAllies, mediumReady, damagedAnchors,
					sacrificeOwedMl, heldBeforeFinale, 0);
		}
	}

	public static List<RiteProblem> of(Facts facts) {
		List<RiteProblem> problems = new ArrayList<>();
		CardinalRitePhase phase = facts.phase();
		boolean beforeSeal = phase == CardinalRitePhase.CONSECRATION || phase == CardinalRitePhase.INSCRIPTION;
		if (beforeSeal && facts.sacrificeOwedMl() > 0) {
			problems.add(new RiteProblem(SACRIFICE, List.of(Integer.toString(facts.sacrificeOwedMl()))));
		}
		if (phase == CardinalRitePhase.INSCRIPTION) {
			for (String sigil : facts.sleepingRequiredSigils()) {
				problems.add(new RiteProblem(SIGIL_REQUIRED, List.of(sigil)));
			}
			if (facts.availableAllies() < facts.requiredAllies()) {
				problems.add(new RiteProblem(ALLIES, List.of(Integer.toString(facts.requiredAllies()),
						Integer.toString(facts.availableAllies()))));
			}
			if (!facts.mediumReady()) problems.add(new RiteProblem(MEDIUM, List.of()));
		}
		boolean afterConsecration = phase == CardinalRitePhase.INSCRIPTION || phase == CardinalRitePhase.STATION_PROJECTION
				|| phase == CardinalRitePhase.ORDEAL || phase == CardinalRitePhase.STILL_INTERVAL
				|| phase == CardinalRitePhase.OFFERING_PROCESSION;
		if (afterConsecration && facts.hungryRingAnchors() > 0) {
			problems.add(new RiteProblem(RING_HUNGERS, List.of(Integer.toString(facts.hungryRingAnchors()))));
		}
		boolean feeding = phase == CardinalRitePhase.CONSECRATION || phase == CardinalRitePhase.ORDEAL
				|| phase == CardinalRitePhase.STILL_INTERVAL;
		if (feeding && facts.damagedAnchors() > 0) problems.add(new RiteProblem(ANCHOR_DAMAGED, List.of()));
		if (facts.heldBeforeFinale()) problems.add(new RiteProblem(FINALE_HELD, List.of()));
		return List.copyOf(problems.size() > MAX_PROBLEMS ? problems.subList(0, MAX_PROBLEMS) : problems);
	}

	/** {@code hematic_lattice} reads as "Hematic Lattice". */
	public static String readable(String id) {
		if (id == null || id.isBlank()) return "";
		StringBuilder result = new StringBuilder();
		for (String word : id.toLowerCase(Locale.ROOT).split("_")) {
			if (word.isEmpty()) continue;
			if (!result.isEmpty()) result.append(' ');
			result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
		}
		return result.toString();
	}
}
