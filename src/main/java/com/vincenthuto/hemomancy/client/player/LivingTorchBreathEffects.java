package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Authored client-local flame stream emitted from the rendered torch tip. */
public final class LivingTorchBreathEffects {
	private static final ParticleColor CRIMSON = new ParticleColor(224, 0, 18);
	private static final ParticleColor ORANGE = new ParticleColor(255, 74, 12);
	private static final ParticleColor CORE = new ParticleColor(72, 0, 10);
	private static final LivingTorchEmissionGate EMISSION_GATE = new LivingTorchEmissionGate();
	private static ClientLevel activeLevel;

	private LivingTorchBreathEffects() { }

	public static void emitFromTip(ClientLevel level, LivingEntity caster, Vec3 tip, int elapsedTicks) {
		if (activeLevel != level) {
			EMISSION_GATE.clear();
			activeLevel = level;
		}
		long gameTime = level.getGameTime();
		if (!EMISSION_GATE.tryAcquire(caster.getId(), gameTime)) return;
		Vec3 look = caster.getLookAngle().normalize();
		Vec3 side = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
		if (side.lengthSqr() < 0.01D) side = new Vec3(1.0D, 0.0D, 0.0D);
		side = side.normalize();
		RandomSource random = level.random;
		for (int tongue = 0; tongue < 5; tongue++) {
			Vec3 velocity = LivingTorchBreathParticleMotion.velocity(look, side, elapsedTicks, tongue,
					(random.nextDouble() - 0.5D) * 0.024D);
			level.addParticle(EmberParticleFactory.createData(tongue % 2 == 0 ? CRIMSON : ORANGE,
					0.11F + tongue * 0.012F, 0.9F,
					LivingTorchBreathParticleMotion.REQUESTED_FLAME_LIFETIME_TICKS), tip.x, tip.y, tip.z,
					velocity.x, velocity.y, velocity.z);
			level.addParticle(EmberParticleFactory.createData(tongue == 0 ? CORE : CRIMSON,
					0.075F + tongue * 0.008F, 0.78F,
					LivingTorchBreathParticleMotion.REQUESTED_FLAME_LIFETIME_TICKS), tip.x, tip.y, tip.z,
					velocity.x * 0.82D, velocity.y * 0.82D, velocity.z * 0.82D);
			if (tongue % 2 == 0) {
				Vec3 bloodVelocity = LivingTorchBreathParticleMotion.bloodCellFactoryInput(velocity);
				level.addParticle(BloodCellParticleFactory.createData(CRIMSON),
						tip.x, tip.y, tip.z, bloodVelocity.x, bloodVelocity.y, bloodVelocity.z);
			}
		}
	}
}
