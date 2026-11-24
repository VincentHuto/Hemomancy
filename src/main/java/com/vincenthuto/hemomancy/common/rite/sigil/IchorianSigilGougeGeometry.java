package com.vincenthuto.hemomancy.common.rite.sigil;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import java.util.ArrayList;
import java.util.List;

/**
 * The ground strip a sigil link's gouge is drawn on: the cut down its middle, then the
 * lip, then the vein skirt. Heights run straight between the two node heights.
 */
public final class IchorianSigilGougeGeometry {
	public static final float CUT_HALF_WIDTH = 0.16F;
	public static final float STRIP_HALF_WIDTH = 0.90F;
	public static final float STEPS_PER_BLOCK = 2.0F;
	/** Synced segment heights are surface + 0.10; the strip sits at surface + 0.012. */
	public static final float SURFACE_DROP = 0.088F;

	private static final double MIN_LENGTH = 0.05D;

	private IchorianSigilGougeGeometry() {
	}

	/** Cross-sections from start to end, at least two; empty when the link is shorter than 0.05 blocks. */
	public static List<Cross> strip(double startX, double startY, double startZ,
			double endX, double endY, double endZ) {
		double dx = endX - startX;
		double dz = endZ - startZ;
		double horizontal = Math.hypot(dx, dz);
		double length = Math.sqrt(horizontal * horizontal + (endY - startY) * (endY - startY));
		// A link that only climbs has no ground direction to lay a strip across.
		if (length < MIN_LENGTH || horizontal < 1.0E-6D) {
			return List.of();
		}
		double sideX = dz / horizontal * STRIP_HALF_WIDTH;
		double sideZ = -dx / horizontal * STRIP_HALF_WIDTH;
		int steps = Math.max(1, (int) Math.ceil(length * STEPS_PER_BLOCK));
		List<Cross> crosses = new ArrayList<>(steps + 1);
		for (int step = 0; step <= steps; step++) {
			double progress = step / (double) steps;
			double x = HemoEasing.lerp(startX, endX, progress);
			double z = HemoEasing.lerp(startZ, endZ, progress);
			double y = HemoEasing.lerp(startY, endY, progress) - SURFACE_DROP;
			crosses.add(new Cross(x + sideX, z + sideZ, x - sideX, z - sideZ, y,
					(float) progress, (float) (length * progress)));
		}
		return List.copyOf(crosses);
	}

	/** One cross-section. along is 0..1 down the link; alongBlocks is the distance from the start. */
	public record Cross(double leftX, double leftZ, double rightX, double rightZ, double y,
			float along, float alongBlocks) {
	}
}
