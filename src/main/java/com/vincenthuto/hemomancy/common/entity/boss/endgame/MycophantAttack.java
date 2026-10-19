package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** The Spawn Mother's moves. Phase 1 is the claim, phase 2 the joining (see MycophantControllerRules.allowed). */
public enum MycophantAttack {
	NONE,
	RUSHING_EMBRACE,
	SIPHON_FEED,
	PLUCK,
	CLAW_RAKE,
	BROOD_CALL,
	NECTAR_SURGE,
	FUSION_EMBRACE,
	WING_GUARD,
	WING_SWEEP,
	PULP_BLOOM;

	public boolean isEmbrace() {
		return this == RUSHING_EMBRACE || this == FUSION_EMBRACE;
	}

	public static MycophantAttack byOrdinal(int ordinal) {
		MycophantAttack[] values = values();
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : NONE;
	}
}
