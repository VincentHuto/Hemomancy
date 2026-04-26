package com.vincenthuto.hemomancy.common.entity.boss.goal;

import java.util.EnumSet;

import com.vincenthuto.hemomancy.common.entity.boss.hemorath.HollowVesselEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/**
 * Empty Pulse — the Hollow Vessel releases a concussive wave that knocks back
 * nearby living entities and applies Hemophagy (healing reduction). Used as
 * spacing/zoning, not as a primary damage tool.
 */
public class EmptyPulseGoal extends Goal {

	private static final double RADIUS = 6.0;
	private static final double KNOCKBACK_STRENGTH = 1.2;
	private static final int HEMOPHAGY_DURATION_TICKS = 140;
	private static final int COOLDOWN_TICKS = 160;
	private static final int WINDUP_TICKS = 25;

	private final HollowVesselEntity boss;
	private int cooldownTicks;
	private int windupTicks;
	private boolean active;

	public EmptyPulseGoal(HollowVesselEntity boss) {
		this.boss = boss;
		this.setFlags(EnumSet.of(Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		if (cooldownTicks > 0) {
			cooldownTicks--;
			return false;
		}
		LivingEntity target = boss.getTarget();
		return target != null && target.isAlive() && boss.distanceToSqr(target) < RADIUS * RADIUS + 4.0;
	}

	@Override
	public boolean canContinueToUse() {
		return active;
	}

	@Override
	public void start() {
		windupTicks = WINDUP_TICKS;
		active = true;
		if (boss.level() instanceof ServerLevel server) {
			server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
					SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.5F, 0.8F);
		}
	}

	@Override
	public void stop() {
		active = false;
		cooldownTicks = COOLDOWN_TICKS;
	}

	@Override
	public void tick() {
		if (windupTicks > 0) {
			windupTicks--;
			if (boss.level() instanceof ServerLevel server && windupTicks % 3 == 0) {
				server.sendParticles(ParticleTypes.REVERSE_PORTAL,
						boss.getX(), boss.getY() + 1.0, boss.getZ(),
						12, 0.8, 0.8, 0.8, 0.05);
			}
			return;
		}

		fire();
		active = false;
	}

	private void fire() {
		if (!(boss.level() instanceof ServerLevel server)) return;

		Vec3 origin = boss.position();
		server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
				SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0F, 1.0F);
		server.sendParticles(ParticleTypes.SONIC_BOOM,
				origin.x, origin.y + 1.0, origin.z,
				1, 0.0, 0.0, 0.0, 0.0);
		server.sendParticles(ParticleTypes.EXPLOSION,
				origin.x, origin.y + 1.0, origin.z,
				6, RADIUS * 0.5, 0.5, RADIUS * 0.5, 0.0);

		for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
				boss.getBoundingBox().inflate(RADIUS))) {
			if (victim == boss) continue;
			Vec3 push = victim.position().subtract(origin).normalize().scale(KNOCKBACK_STRENGTH);
			victim.push(push.x, 0.4, push.z);
			victim.hurtMarked = true;
			victim.addEffect(new MobEffectInstance(EffectInit.hemophagy,
					HEMOPHAGY_DURATION_TICKS, 0, false, true));
		}
	}
}
