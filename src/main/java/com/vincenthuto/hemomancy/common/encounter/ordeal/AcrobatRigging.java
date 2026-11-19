package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * The Acrobat's place in their ordeal (2026-11-17): a trapeze in the rigging over the net apron, outside the ring and
 * out of reach. The true self hangs by the knees from its bar (the entity stands at the bar; the body is drawn hanging
 * below it, {@link #SCALE} times life size). The trapeze drifts round the ring and swings along its path; it drops out
 * of the dark at the start, and every {@link #CROSS_EVERY} ticks the Acrobat lets go, tumbles across over the ring and
 * is caught on the far side.
 */
public final class AcrobatRigging {
	/** Out past the ring (radius 9.5) over the net apron. */
	public static final double RADIUS = 12.0D;
	/** The bar's height over the plate. */
	public static final double BAR = 13.0D;
	/** How many times life size the true self is drawn. */
	public static final float SCALE = 2.5F;
	/** The drawn head's eyes under the bar, in blocks, hanging. */
	public static final double EYES = 4.25D;
	/** One drift round the ring; the swing along the path, its reach and period. */
	public static final float PERIOD = 1600.0F;
	public static final double SWAY = 1.2D;
	public static final float SWAY_PERIOD = 70.0F;
	/** The crossing: how often, how long, and how high its arc rises over the bar. */
	public static final int CROSS_EVERY = 500, CROSS_TICKS = 40;
	public static final double CROSS_ARC = 3.0D;
	/** The drop out of the dark at the start: from this high over the bar, over this many ticks. */
	public static final double ENTRANCE_DROP = 16.0D;
	public static final int ENTRANCE_TICKS = 60;

	private AcrobatRigging() {
	}

	/** Where the bar is {@code time} ticks after the true self appeared; {@code stage} is the stage origin. */
	public static Vec3 position(BlockPos stage, float time) {
		double cx = stage.getX() + 0.5D, cz = stage.getZ() + 0.5D, bar = stage.getY() + 1.0D + BAR;
		long n = (long) Math.floor(time / CROSS_EVERY);
		double into = time - n * (double) CROSS_EVERY;
		double base = Math.PI * 2.0D * time / PERIOD + Math.PI * (n - 1);
		double x, z, y = bar;
		if (n > 0 && into < CROSS_TICKS) {
			// Let go: across the ring's middle to the far trapeze, high, the arc rising and falling.
			double u = HemoEasing.smoothstep(0.0F, 1.0F, (float) (into / CROSS_TICKS));
			double fromX = Math.cos(base), fromZ = Math.sin(base);
			x = cx + RADIUS * fromX * (1.0D - 2.0D * u);
			z = cz + RADIUS * fromZ * (1.0D - 2.0D * u);
			y += CROSS_ARC * Math.sin(Math.PI * into / CROSS_TICKS);
		} else {
			double angle = n > 0 ? base + Math.PI : base;
			double sway = SWAY * Math.sin(Math.PI * 2.0D * time / SWAY_PERIOD);
			// Along the path's tangent, and a touch higher at each end of the swing (the lines are long).
			x = cx + RADIUS * Math.cos(angle) - sway * Math.sin(angle);
			z = cz + RADIUS * Math.sin(angle) + sway * Math.cos(angle);
			y += sway * sway / 16.0D;
		}
		if (time < ENTRANCE_TICKS) y += ENTRANCE_DROP * (1.0D - HemoEasing.smoothstep(0.0F, 1.0F, time / ENTRANCE_TICKS));
		return new Vec3(x, y, z);
	}

	/** Whether the Acrobat is crossing the ring at {@code time}. */
	public static boolean crossing(float time) {
		long n = (long) Math.floor(time / CROSS_EVERY);
		return n > 0 && time - n * (double) CROSS_EVERY < CROSS_TICKS;
	}

	/** Where the drawn head's eyes are, hanging under the bar. */
	public static Vec3 eyes(Vec3 bar) {
		return bar.add(0.0D, -EYES, 0.0D);
	}
}
