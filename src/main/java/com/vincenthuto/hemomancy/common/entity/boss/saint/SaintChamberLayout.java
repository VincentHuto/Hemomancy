package com.vincenthuto.hemomancy.common.entity.boss.saint;

/**
 * The chamber-local numbers of {@code tools/worldgen/mausoleum/pieces.py} (the chamber section), which the four Saint
 * designs quote. Local x runs from the ring door (0) to the apse's back wall; local z across the hall, symmetric
 * about {@link #ZC}; local y from the hall floor's course (0). {@code SaintCombatKitGuardrailTest} pins every value
 * to the generator.
 */
public final class SaintChamberLayout {
	/** CHAMBER_L, CHAMBER_H, CHAMBER_W. */
	public static final int LENGTH = 37;
	public static final int HEIGHT = 17;
	public static final int WIDTH = 27;
	/** CHAMBER_ZC: the centre line. */
	public static final int ZC = 13;
	/** CHAMBER_ENTRY: the door's sill course. */
	public static final int ENTRY = 6;
	/** CHAMBER_PILLARS: both ranks, on z = ZC -+ RANK. */
	public static final int[] PILLARS = {9, 13, 17, 21, 25};
	public static final int RANK = 8;
	/** CHAMBER_BAYS: the bay centres between the pillars. */
	public static final int[] BAYS = {11, 15, 19, 23};
	/** CHAMBER_AISLE: each aisle's centre line, off ZC. */
	public static final int AISLE = 10;
	/** CHAMBER_CLEAR: the boss floor's x span; its z span is ZC -+ 7 ({@link #CLEAR_HALF}). */
	public static final int CLEAR_MIN_X = 9;
	public static final int CLEAR_MAX_X = 23;
	public static final int CLEAR_HALF = 7;
	/** CHAMBER_STEPS and CHAMBER_DAIS: three risers, the top step's course. */
	public static final int[] STEPS = {24, 25, 26};
	public static final int DAIS = 3;
	/** CHAMBER_BACK: the apse's back wall. */
	public static final int BACK = 35;
	/** CHAMBER_TOMB: the sarcophagus' main block, facing west (toward the door). */
	public static final int TOMB_X = 30;
	public static final int TOMB_Y = DAIS + 1;
	public static final int TOMB_Z = ZC;

	/** The course a player stands on over the boss floor: one above the floor course. */
	public static final int FLOOR_Y = 1;
	public static final int BOSS_FLOOR_MIN_Z = ZC - CLEAR_HALF;
	public static final int BOSS_FLOOR_MAX_Z = ZC + CLEAR_HALF;
	/** The pillar ranks' and aisles' z planes. */
	public static final int RANK_NORTH_Z = ZC - RANK;
	public static final int RANK_SOUTH_Z = ZC + RANK;
	public static final int AISLE_NORTH_Z = ZC - AISLE;
	public static final int AISLE_SOUTH_Z = ZC + AISLE;

	private SaintChamberLayout() {
	}
}
