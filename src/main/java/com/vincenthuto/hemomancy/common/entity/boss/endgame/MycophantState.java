package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** What the Spawn Mother is doing as a whole; the active move lives in MycophantAttack. */
public enum MycophantState {
	INTRO,
	IDLE,
	DRAG,
	ATTACK,
	STAGGER,
	SPLIT,
	YIELD;

	public static MycophantState byOrdinal(int ordinal) {
		MycophantState[] values = values();
		return ordinal >= 0 && ordinal < values.length ? values[ordinal] : IDLE;
	}
}
