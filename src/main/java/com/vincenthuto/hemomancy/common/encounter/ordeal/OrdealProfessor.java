package com.vincenthuto.hemomancy.common.encounter.ordeal;

import java.util.List;
import java.util.Optional;

import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules.Ramp;

/**
 * A Crimson Troupe professor whose puppet-body ordeal runs on a stage in their Chamber of Will: the faculty role, the
 * body their ordeal summons, their stage template, the interventions they make from outside the ring (each opening at
 * a ramp of the fight), their {@link OrdealChoreography} (where their true self keeps watch and what each move does)
 * and their puppet's own {@link OrdealPuppetMoves}. One row per professor in {@link #ALL}.
 */
public record OrdealProfessor(String role, String summon, String stage, List<Intervention> interventions,
		OrdealChoreography choreography, OrdealPuppetMoves puppetMoves) {
	/** One intervention: its id, the ramp it opens at, its wind-up and how long before it comes round again. */
	public record Intervention(String id, Ramp from, int windupTicks, int cooldownTicks) {
	}

	public static final OrdealProfessor STRONGMAN = new OrdealProfessor("circus_strongman", "gorebound_hulk",
			"ordeal/strongman", List.of(
					new Intervention("dropped_weight", Ramp.OPENING, 20, 100),
					new Intervention("ground_heave", Ramp.OPENING, 20, 120),
					new Intervention("bar_sweep", Ramp.PRESSED, 20, 140),
					new Intervention("hold_the_line", Ramp.DESPERATE, 20, 200)),
			StrongmanChoreography.INSTANCE, OrdealHulkMoves.MOVES);

	/**
	 * Every professor with an ordeal, in the curriculum's order. Each row is its professor's own file's constant, so the
	 * list is built on first use in its own holder: a choreography whose class loads first (creating its row, and so this
	 * class) would otherwise read its own row as null here.
	 */
	private static final class Rows {
	private static final List<OrdealProfessor> ALL = List.of(STRONGMAN, KnifeThrowerChoreography.PROFESSOR, UnderstudyChoreography.PROFESSOR, AcrobatChoreography.PROFESSOR, StiltWalkerChoreography.PROFESSOR, BeastTamerChoreography.PROFESSOR, FireEaterChoreography.PROFESSOR);
	}

	public static List<OrdealProfessor> all() {
		return Rows.ALL;
	}


	public static Optional<OrdealProfessor> forSummon(String summon) {
		return Rows.ALL.stream().filter(professor -> professor.summon().equals(summon)).findFirst();
	}

	public static Optional<OrdealProfessor> forRole(String role) {
		return Rows.ALL.stream().filter(professor -> professor.role().equals(role)).findFirst();
	}
}
