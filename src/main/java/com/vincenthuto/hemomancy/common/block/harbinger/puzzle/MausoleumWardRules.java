package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The ward over a Saint's mausoleum past its threshold: the outer halls are open to every Harbinger, but only the
 * eighth degree crosses the ward line. Anyone else who touches it, or is found beyond it, is set back a block before
 * the line. Every refused blow strikes the ward; blows that come quickly brighten the strike, never the push.
 */
public final class MausoleumWardRules {
	public static final int WARD_DEGREE = 8;
	/** How far from the viewer the ward layer is drawn, in blocks. */
	public static final int WARD_RADIUS = 12;
	/** Strikes this close together in time count as one escalating blow. */
	public static final int STRIKE_WINDOW_TICKS = 20;
	/** How long a strike's ring and flash last on the client. */
	public static final int STRIKE_LIFE_TICKS = 30;
	/** How far before the ward line a turned-back visitor is set down. */
	public static final double PUSH_DISTANCE = 1.0;
	/** A visitor's body stays this far from the corridor's side walls when set back. */
	public static final double SIDE_MARGIN = 0.35;
	private static final float FLASH_STEP = 0.35f;
	private static final float FLASH_MAX = 2.4f;

	private MausoleumWardRules() {}

	/**
	 * Where a visitor touching or past the {@code threshold} slab is set down: {@link #PUSH_DISTANCE} before its outer
	 * face (against {@code inward}), on its floor, kept across the corridor where they stood.
	 */
	public static Vec3 pushBack(Vec3 playerPos, AABB threshold, Direction inward) {
		Direction.Axis along = inward.getAxis();
		double outerFace = inward.getAxisDirection() == Direction.AxisDirection.POSITIVE
				? threshold.min(along) - PUSH_DISTANCE
				: threshold.max(along) + PUSH_DISTANCE;
		Direction.Axis across = along == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		double low = threshold.min(across) + SIDE_MARGIN;
		double high = threshold.max(across) - SIDE_MARGIN;
		double side = low > high ? (threshold.min(across) + threshold.max(across)) / 2.0
				: Math.max(low, Math.min(high, playerPos.get(across)));
		double x = along == Direction.Axis.X ? outerFace : side;
		double z = along == Direction.Axis.Z ? outerFace : side;
		return new Vec3(x, threshold.minY, z);
	}

	/** The flash of a strike that is the {@code strikesInWindow}-th within {@link #STRIKE_WINDOW_TICKS}. */
	public static float flashStrength(int strikesInWindow) {
		if (strikesInWindow <= 1) return 1.0f;
		return Math.min(FLASH_MAX, 1.0f + (strikesInWindow - 1) * FLASH_STEP);
	}

	/** The ward's colour over each Saint's stone (0xRRGGBB). */
	public static int wardColor(EnumSaintType saint) {
		return switch (saint) {
			case HEMORATH -> 0xD8623E;
			case SERAPHAE -> 0xF3DDA2;
			case PUTRICIEL -> 0xA8BC6A;
			case VELORUM -> 0x9ECBEA;
		};
	}
}
