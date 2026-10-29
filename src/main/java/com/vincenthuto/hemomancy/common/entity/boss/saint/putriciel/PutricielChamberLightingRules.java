package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import javax.annotation.Nullable;

/**
 * Which lights Putriciel's fight drives, and how each of his phases sets them. In his chamber the fight takes every
 * Rot Pyre and Spore Shelf inside the chamber's own box, measured from the tomb along the way its foot faces; with no
 * tomb, those within {@link #FALLBACK_RADIUS} of where the fight began. Burning, the pyres are lit and the shelves dim;
 * in the absolution window (rot) the pyres gutter out and the shelves bloom.
 */
public final class PutricielChamberLightingRules {
	/**
	 * The chamber from its tomb (tools/worldgen/mausoleum/pieces.py: CHAMBER 37 x 17 x 27, CHAMBER_TOMB (30, 4, 13)
	 * with its foot toward the door at x 0): 30 ahead, 6 behind, 13 to either side, 4 down and 12 up.
	 */
	public static final int AHEAD = 30, BEHIND = 6, ACROSS = 13, BELOW = 4, ABOVE = 12;
	/** With no tomb: how far round the fight's start, across the floor, a light can be and still answer. */
	public static final int FALLBACK_RADIUS = 16;
	/** With no tomb: how far above or below the fight's start a light can be. */
	public static final int FALLBACK_VERTICAL = 8;

	/** His two phases: burning (rot novas) and rot (the absolution window). */
	public enum Phase {
		BURNING, ROT;

		public static Phase of(boolean absolved) {
			return absolved ? ROT : BURNING;
		}
	}

	/** The two lights: a Rot Pyre ({@code lit}) and a Spore Shelf ({@code bloom}). */
	public enum Light {
		PYRE, SHELF
	}

	private PutricielChamberLightingRules() {
	}

	/** The value {@code light}'s switch ({@code lit} for a pyre, {@code bloom} for a shelf) takes in {@code phase}. */
	public static boolean on(Light light, Phase phase) {
		return light == Light.PYRE ? phase == Phase.BURNING : phase == Phase.ROT;
	}

	/** The chamber's box round a tomb whose foot faces {@code facing} (horizontal). */
	public static BoundingBox chamber(BlockPos tomb, Direction facing) {
		BlockPos ahead = tomb.relative(facing, AHEAD).relative(facing.getClockWise(), ACROSS).above(ABOVE);
		BlockPos behind = tomb.relative(facing, -BEHIND).relative(facing.getClockWise(), -ACROSS).below(BELOW);
		return BoundingBox.fromCorners(ahead, behind);
	}

	/** The box to look through for the fight's lights: the chamber's, or the fallback circle's bounds. */
	public static BoundingBox scanBox(@Nullable BlockPos tomb, @Nullable Direction facing, BlockPos home) {
		if (hasChamber(tomb, facing)) return chamber(tomb, facing);
		return new BoundingBox(home.getX() - FALLBACK_RADIUS, home.getY() - FALLBACK_VERTICAL, home.getZ() - FALLBACK_RADIUS,
				home.getX() + FALLBACK_RADIUS, home.getY() + FALLBACK_VERTICAL, home.getZ() + FALLBACK_RADIUS);
	}

	/** True when a light at {@code pos} belongs to the fight: inside the chamber, or, with no tomb, the circle round {@code home}. */
	public static boolean inArea(@Nullable BlockPos tomb, @Nullable Direction facing, BlockPos home, BlockPos pos) {
		if (hasChamber(tomb, facing)) return chamber(tomb, facing).isInside(pos);
		int dx = pos.getX() - home.getX(), dz = pos.getZ() - home.getZ();
		return dx * dx + dz * dz <= FALLBACK_RADIUS * FALLBACK_RADIUS && Math.abs(pos.getY() - home.getY()) <= FALLBACK_VERTICAL;
	}

	private static boolean hasChamber(@Nullable BlockPos tomb, @Nullable Direction facing) {
		return tomb != null && facing != null && facing.getAxis().isHorizontal();
	}
}
