package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingDigRules;

import java.util.ArrayList;
import java.util.List;

/**
 * The numbers and shapes of the Staff's projection mining (core-loop Track 8), free of Minecraft types so they are
 * tested on the JVM. Lode Draw crawls an ore lode one block a pulse and breaks what it reached on release; Hematic
 * Hewing cuts a set shape out of the struck face each pulse. Both cost more blood per block than the pickaxe form's
 * {@code LivingDigRules}: they trade blood for time, and their drops are always plain.
 */
public final class StaffMiningRules {
	/** Lode Draw: 12 mL a pulse at the default cadence, one block of the lode a pulse (24 mL/s, 2 blocks a second). */
	public static final double LODE_BLOOD_PER_PULSE = 12.0D;
	public static final int LODE_CADENCE = 10;
	/**
	 * Hematic Hewing: one cut a second, priced 2 mL a hardness point for each block cut (the pickaxe's
	 * {@link LivingDigRules#bloodPerBlock}), never less than this floor (CB-20, NQ-04-14).
	 */
	public static final double HEW_MIN_BLOOD_PER_CUT = 36.0D;
	public static final int HEW_CADENCE = 20;
	/** How steeply the player must look up or down for a cut to become a stair. */
	public static final float STAIR_PITCH = 35.0F;

	/** What a cut takes out of the struck face. */
	public enum HewShape {
		/** One wide, two tall: the struck block and the one above or below it, toward the player's body. */
		TUNNEL,
		/** Three by three across the struck face (sneak while hewing). */
		FACE,
		/**
		 * A column three tall centred on the struck block: aimed low into a wall it opens the next step down (the
		 * struck block at the feet, the floor below it kept), aimed high the next step up.
		 */
		STAIR
	}

	private StaffMiningRules() {
	}

	/** What a cut through blocks of these hardnesses costs: the larger of the floor and their summed price. */
	public static double hewPrice(float[] hardnesses) {
		double sum = 0.0D;
		for (float hardness : hardnesses) sum += LivingDigRules.bloodPerBlock(hardness);
		return Math.max(HEW_MIN_BLOOD_PER_CUT, sum);
	}

	/**
	 * The lode a crawl reaches in one pulse: one block, slowed by a dry pulse's weaker work but never sped up by the
	 * Staff, so the price per block stays above the pickaxe's.
	 */
	public static double lodeBlocksPerPulse(double workScale) {
		if (!Double.isFinite(workScale)) return 1.0D;
		return Math.max(0.25D, Math.min(1.0D, workScale));
	}

	/** Which shape the cut takes: sneaking cuts a face, a steep look into a wall cuts a stair, else a tunnel. */
	public static HewShape shapeFor(boolean sneaking, float pitch, boolean horizontalFace) {
		if (sneaking) return HewShape.FACE;
		if (horizontalFace && Math.abs(pitch) >= STAIR_PITCH) return HewShape.STAIR;
		return HewShape.TUNNEL;
	}

	/**
	 * The cells a cut takes, as {x, y, z} offsets from the struck block. {@code faceX/Y/Z} is the struck face's
	 * outward normal (one of the six unit axes); {@code bodyBelow} says the player's body is lower than the struck
	 * block (a tunnel then takes the block below it, else the one above).
	 */
	public static List<int[]> cells(HewShape shape, int faceX, int faceY, int faceZ, boolean bodyBelow) {
		List<int[]> cells = new ArrayList<>();
		cells.add(new int[] { 0, 0, 0 });
		boolean vertical = faceY != 0;
		switch (shape) {
			case TUNNEL -> {
				if (vertical) {
					// A shaft: the next block on, away from the player.
					cells.add(new int[] { 0, -faceY, 0 });
				} else {
					cells.add(new int[] { 0, bodyBelow ? -1 : 1, 0 });
				}
			}
			case FACE -> {
				for (int a = -1; a <= 1; a++) {
					for (int b = -1; b <= 1; b++) {
						if (a == 0 && b == 0) continue;
						if (vertical) cells.add(new int[] { a, 0, b });
						else if (faceX != 0) cells.add(new int[] { 0, a, b });
						else cells.add(new int[] { a, b, 0 });
					}
				}
			}
			case STAIR -> {
				cells.add(new int[] { 0, 1, 0 });
				cells.add(new int[] { 0, -1, 0 });
			}
		}
		return cells;
	}
}
