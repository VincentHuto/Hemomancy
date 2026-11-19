package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.world.phys.Vec3;

/**
 * The Stilt Walker's round of their stage (2026-11-17): a slow walk on the promenade outside the curb, one long stride
 * at a time, the true self drawn {@link #SCALE} times over so they stand about twelve blocks tall. The walk is counted in
 * strides (a whole number is both stilts planted); the body moves through the middle of each stride and settles as the
 * stilt plants, and the model swings its legs from the same count, synced on the entity.
 */
public final class OrdealStiltWalk {
	/** On the promenade, well past the curb (radius 10) and the apron: no blow from the ring reaches them. */
	public static final double RADIUS = 15.0D;
	/** How many times over the true self is drawn. */
	public static final float SCALE = 1.6F;
	/** Ticks a stride takes, and how far round the stage one carries them (about three blocks). */
	public static final int STRIDE_TICKS = 30;
	public static final double STRIDE_ANGLE = 3.2D / RADIUS;
	/** The drawn eyes above the stilts' tips, in blocks ({@code CircusTrueStiltWalkerModel}). */
	public static final double EYES = 5.75D * SCALE;

	private OrdealStiltWalk() {
	}

	/** Where the stilts stand after {@code strides} strides, round the ring's centre {@code floor} on its plane. */
	public static Vec3 position(Vec3 floor, double strides) {
		double whole = Math.floor(strides);
		double angle = (whole + HemoEasing.smoothstep(strides - whole)) * STRIDE_ANGLE;
		return new Vec3(floor.x + Math.cos(angle) * RADIUS, floor.y, floor.z + Math.sin(angle) * RADIUS);
	}

	/** The way the walk is heading at {@code strides}: the tangent, counter-clockwise seen from above. */
	public static Vec3 heading(double strides) {
		double angle = strides * STRIDE_ANGLE;
		return new Vec3(-Math.sin(angle), 0.0D, Math.cos(angle));
	}

	public static Vec3 eyes(Vec3 feet) {
		return feet.add(0.0D, EYES, 0.0D);
	}
}
