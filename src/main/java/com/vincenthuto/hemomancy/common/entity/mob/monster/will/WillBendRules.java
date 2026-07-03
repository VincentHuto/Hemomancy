package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

public final class WillBendRules {
	public static final int MIN_BEND_DEGREE = 7;
	public static final int REDIRECT_BLOOD_COST = 400;
	public static final int COMMANDEER_THREAD_COST = 56;
	public static final int BACKFIRE_HIVE_ATTENTION = 2;
	private static final int SILENT_ARCHON_BONUS_CAP = 1;

	private WillBendRules() {
	}

	public static BendOutcome resolve(WillOrigin origin, WillPhase phase, int playerDegree, HeldItemKind heldItem,
			boolean sneaking, boolean activeCapAvailable, boolean silentArchon) {
		if (origin != WillOrigin.BROKEN || phase != WillPhase.FALTERING || playerDegree < MIN_BEND_DEGREE) {
			return backfire();
		}
		if (heldItem == HeldItemKind.MARIONETTE_CROSSBAR) {
			if (!activeCapAvailable) return backfire();
			int threadCost = silentArchon ? COMMANDEER_THREAD_COST / 2 : COMMANDEER_THREAD_COST;
			return new BendOutcome(BendVerb.COMMANDEER, 0, threadCost, false, 0);
		}
		if (sneaking) {
			return new BendOutcome(BendVerb.REDIRECT, REDIRECT_BLOOD_COST, 0, false, 0);
		}
		return new BendOutcome(BendVerb.ABSORB, 0, 0, false, 0);
	}

	public static int silentArchonBonusCap() {
		return SILENT_ARCHON_BONUS_CAP;
	}

	private static BendOutcome backfire() {
		return new BendOutcome(BendVerb.BACKFIRE, 0, 0, true, BACKFIRE_HIVE_ATTENTION);
	}

	public enum BendVerb {
		ABSORB,
		REDIRECT,
		COMMANDEER,
		BACKFIRE
	}

	public enum HeldItemKind {
		EMPTY_OR_STAFF,
		MARIONETTE_CROSSBAR,
		OTHER
	}

	public record BendOutcome(BendVerb verb, int bloodCost, int threadCost, boolean backfire, int hiveAttentionGain) {
	}
}
