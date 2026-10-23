package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;

/**
 * How far a Harbinger rite has pushed the world around it. Each milestone counts once, the first time
 * it is reached: a ring of anchors filled, a station circuit completed, the seal (or a puppeteer trial
 * manifesting), the offering procession and culmination. The stage is the sum of those marks, so it
 * never falls when instability drains an anchor. Succession steps with its five cues instead.
 */
public final class CardinalRiteEscalation {
	public static final int FLAG_OPENING = 1;
	public static final int FLAG_PROCESSION = 2;
	public static final int FLAG_CULMINATION = 4;
	public static final int SUCCESSION_CUE_TICKS = 200;
	public static final int SUCCESSION_STEPS = 5;

	private CardinalRiteEscalation() {
	}

	/** What a rite can reach. */
	public record Plan(int rings, int circuits, boolean procession, boolean succession) {
		public int total() {
			if (succession) return SUCCESSION_STEPS;
			return Math.max(0, rings) + Math.max(0, circuits) + 1 + (procession ? 1 : 0) + 1;
		}
	}

	/** Where the rite stands this tick, read from its live state. */
	public record Reached(int rings, int circuits, boolean opening, boolean procession, boolean culmination,
			int successionTicks) {
	}

	/** Milestones already counted; every part only rises. */
	public record Marks(int rings, int circuits, int flags) {
		public static final Marks NONE = new Marks(0, 0, 0);

		public Marks raise(Reached reached, Plan plan) {
			int raised = flags;
			if (reached.opening()) raised |= FLAG_OPENING;
			// Reaching culmination means the procession has passed, even inside one tick.
			if (plan.procession() && (reached.procession() || reached.culmination())) raised |= FLAG_PROCESSION;
			if (reached.culmination()) raised |= FLAG_CULMINATION;
			return new Marks(Math.max(rings, Math.min(reached.rings(), plan.rings())),
					Math.max(circuits, Math.min(reached.circuits(), plan.circuits())), raised);
		}
	}

	public static int stage(Plan plan, Marks marks, int successionTicks) {
		if (plan.succession()) {
			return Math.min(SUCCESSION_STEPS, Math.max(0, successionTicks) / SUCCESSION_CUE_TICKS);
		}
		return marks.rings() + marks.circuits()
				+ Integer.bitCount(marks.flags() & (FLAG_OPENING | FLAG_PROCESSION | FLAG_CULMINATION));
	}

	public static Plan plan(ActiveCardinalRite rite) {
		return new Plan(rite.totalAnchorRings(), StationUpgradeRites.circuits(rite.getRecipeId()),
				!rite.getOfferingItinerary().isEmpty(), rite.succession().isPresent());
	}

	public static Reached reached(ActiveCardinalRite rite) {
		CardinalRitePhase phase = rite.getPhase();
		return new Reached(rite.completedRings(), rite.upgrade().stage(),
				rite.isAltarSealed() || rite.puppeteerTrial().isManifested(),
				phase == CardinalRitePhase.OFFERING_PROCESSION, phase == CardinalRitePhase.CULMINATION,
				rite.succession().ticks());
	}

	/**
	 * Raises the rite's marks from its live state.
	 *
	 * @return true when the stage rose, so the caller announces the step
	 */
	public static boolean update(ActiveCardinalRite rite) {
		if (rite.getPhase() == CardinalRitePhase.LEGACY) return false;
		Plan plan = plan(rite);
		Marks marks = rite.getEscalationMarks().raise(reached(rite), plan);
		rite.setEscalationMarks(marks);
		int stage = stage(plan, marks, rite.succession().ticks());
		if (stage <= rite.getEscalationStage()) return false;
		rite.setEscalationStage(stage);
		return true;
	}

	/** Rebuilds the marks of a rite saved before escalation existed, without announcing anything. */
	public static void rebuild(ActiveCardinalRite rite) {
		if (rite.getPhase() == CardinalRitePhase.LEGACY) return;
		Plan plan = plan(rite);
		Marks marks = Marks.NONE.raise(reached(rite), plan);
		rite.setEscalationMarks(marks);
		rite.setEscalationStage(stage(plan, marks, rite.succession().ticks()));
	}
}
