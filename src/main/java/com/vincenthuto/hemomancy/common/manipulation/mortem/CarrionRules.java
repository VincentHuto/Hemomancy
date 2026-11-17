package com.vincenthuto.hemomancy.common.manipulation.mortem;

/**
 * Pure numbers for Carrion Communion's Sculpting of Absorption (core-loop round two P4). The dead hold no living blood,
 * so what it draws from flesh and bone is Mortem Temper and a little health, never blood. A bone is worth what
 * dissolving one as a Mortem catalyst is worth; a skull and a bone block are worth more and take a few pulses.
 */
public final class CarrionRules {
	/** Health each consumed remain gives back. */
	public static final float HEAL_PER_REMAIN = 1.0F;

	/** What can be drawn from: two kinds lying on the ground, two standing as blocks. */
	public enum Remains {
		/** Rotten flesh on the ground. */
		FLESH(2.0F, 1),
		/** A bone on the ground: a Mortem catalyst's worth. */
		BONE(3.0F, 1),
		/** A skeleton, wither skeleton or zombie skull or head. */
		SKULL(10.0F, 2),
		/** A bone block. */
		BONE_BLOCK(9.0F, 4);

		/** Mortem Temper it gives at full strength. */
		public final float temper;
		/** Paid pulses on it before it is drawn whole (items go one or more a pulse). */
		public final int pulses;

		Remains(float temper, int pulses) {
			this.temper = temper;
			this.pulses = pulses;
		}
	}

	private CarrionRules() {
	}

	/** How many remains lying on the ground one pulse draws: one by hand, more with the Staff, never none. */
	public static int itemsPerPulse(double workScale) {
		return Math.max(1, (int) Math.floor(Math.max(0.0D, workScale) + 0.5D));
	}

	/**
	 * The Temper one remain gives: never more than its worth (CQ-16). The Staff draws more remains from the ground
	 * rather than more from each, and a block yields the same however hard it is worked. A dry pulse draws less.
	 */
	public static float temper(Remains remains, double workScale) {
		return (float) (remains.temper * Math.clamp(workScale, 0.0D, 1.0D));
	}

	/** Whether the pulse with this {@code index} (0 for the first on a block) draws the block whole. */
	public static boolean drawsWhole(Remains remains, int index) {
		return (index + 1) % Math.max(1, remains.pulses) == 0;
	}
}
