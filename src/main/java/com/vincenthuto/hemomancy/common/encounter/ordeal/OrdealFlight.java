package com.vincenthuto.hemomancy.common.encounter.ordeal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * The professor's path round their stage: a colossus wading slowly round the ring through the dark past the apron, seen
 * from the waist up. The true self is a bust whose waist is its root at the entity's feet, drawn {@link #SCALE} times
 * over; the entity is sunk so the waist sits just under the apron's top and the client fades it into the Chamber's dark,
 * so only the torso, arms and head rise over the yard. The entity itself stays its own small size: nothing in the ring
 * can reach or strike it.
 */
public final class OrdealFlight {
	/** Wide of the apron (radius 13.5), so the shoulders never lean over the ring; his hands reach to its edge. */
	public static final double RADIUS = 20.5D;
	/** How many times over the true self is drawn while it circles. */
	public static final float SCALE = 3.0F;
	/** The bust's eyes above its waist, in model blocks ({@code CircusTrueStrongmanModel}). */
	public static final double EYES = 2.9D;
	/** How far under the apron's top the waist sits, and the slow rise and fall of his breathing. */
	public static final double SUNK = 0.3D, BREATH = 0.25D;
	/** Ticks a full turn takes: one minute. */
	public static final float PERIOD = 1200.0F;

	private OrdealFlight() {
	}

	/** The true self's waist: just under the apron, whose top is the stage origin's own row. */
	public static Vec3 position(BlockPos stage, float time) {
		double angle = time / PERIOD * Math.PI * 2.0D;
		return new Vec3(stage.getX() + 0.5D + Math.cos(angle) * RADIUS,
				stage.getY() - SUNK + Math.sin(time * 0.05D) * BREATH,
				stage.getZ() + 0.5D + Math.sin(angle) * RADIUS);
	}

	/** Where the drawn head's eyes are, for turning the head toward the fighter. */
	public static Vec3 eyes(Vec3 waist) {
		return waist.add(0.0D, SCALE * EYES, 0.0D);
	}
}
