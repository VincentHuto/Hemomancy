package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchBreathRules;
import net.minecraft.world.phys.Vec3;

/** Pure motion rules for the client-side Living Torch flame stream. */
public final class LivingTorchBreathParticleMotion {
	public static final int FAN_TONGUES = 7;
	public static final double FAN_HALF_ANGLE_DEGREES = LivingTorchBreathRules.HALF_ANGLE_DEGREES;
	public static final int REQUESTED_FLAME_LIFETIME_TICKS = 24;
	public static final int EFFECTIVE_FLAME_LIFETIME_TICKS = REQUESTED_FLAME_LIFETIME_TICKS;
	private static final double BLOOD_CELL_FACTORY_SPEED_COMPENSATION = 0.5D;

	private LivingTorchBreathParticleMotion() { }

	public static Vec3 velocity(Vec3 look, Vec3 side, int elapsedTicks, int tongue,
			double verticalJitter) {
		double spread = FAN_TONGUES <= 1 ? 0.0D : tongue / (double) (FAN_TONGUES - 1);
		double angle = Math.toRadians(-FAN_HALF_ANGLE_DEGREES + spread * FAN_HALF_ANGLE_DEGREES * 2.0D);
		double speed = 0.30D + Math.sin(spread * Math.PI) * 0.015D;
		double writhe = Math.sin((elapsedTicks + tongue * 3) * 0.62D) * 0.035D
				* Math.sin(spread * Math.PI);
		Vec3 direction = look.scale(Math.cos(angle)).add(side.scale(Math.sin(angle))).normalize();
		return direction.scale(speed).add(side.scale(writhe)).add(0.0D, verticalJitter, 0.0D);
	}

	/** BloodCellParticle doubles constructor velocity, so compensate to match the flame tongues. */
	public static Vec3 bloodCellFactoryInput(Vec3 flameVelocity) {
		return flameVelocity.scale(BLOOD_CELL_FACTORY_SPEED_COMPENSATION);
	}
}
