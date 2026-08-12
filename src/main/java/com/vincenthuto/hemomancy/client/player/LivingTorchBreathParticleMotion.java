package com.vincenthuto.hemomancy.client.player;

import net.minecraft.world.phys.Vec3;

/** Pure motion rules for the client-side Living Torch flame stream. */
public final class LivingTorchBreathParticleMotion {
	public static final int REQUESTED_FLAME_LIFETIME_TICKS = 24;
	public static final int EFFECTIVE_FLAME_LIFETIME_TICKS = REQUESTED_FLAME_LIFETIME_TICKS / 2;
	private static final double BLOOD_CELL_FACTORY_SPEED_COMPENSATION = 0.5D;

	private LivingTorchBreathParticleMotion() { }

	public static Vec3 velocity(Vec3 look, Vec3 side, int elapsedTicks, int tongue,
			double verticalJitter) {
		double speed = 0.18D + tongue * 0.035D;
		double writhe = Math.sin((elapsedTicks + tongue * 3) * 0.62D) * 0.035D;
		return look.scale(speed).add(side.scale(writhe)).add(0.0D, verticalJitter, 0.0D);
	}

	/** BloodCellParticle doubles constructor velocity, so compensate to match the flame tongues. */
	public static Vec3 bloodCellFactoryInput(Vec3 flameVelocity) {
		return flameVelocity.scale(BLOOD_CELL_FACTORY_SPEED_COMPENSATION);
	}
}
