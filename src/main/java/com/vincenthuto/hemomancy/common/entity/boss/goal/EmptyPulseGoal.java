package com.vincenthuto.hemomancy.common.entity.boss.goal;

import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Empty Pulse — Hemorath releases a concussive wave that knocks back
 * nearby living entities and applies Hemophagy (healing reduction). Used as
 * spacing/zoning, not as a primary damage tool.
 */
public class EmptyPulseGoal extends Goal {

	private static final double RADIUS = HemorathVisualRules.PULSE_RADIUS;
	private static final double KNOCKBACK_STRENGTH = 1.2;
	private static final int HEMOPHAGY_DURATION_TICKS = 140;
	private static final int COOLDOWN_TICKS = 160;
	private static final int WINDUP_TICKS = HemorathVisualRules.PULSE_WINDUP;

	private final HemorathEntity boss;
	private int cooldownTicks;
	private int windupTicks;
	private boolean active;

	public EmptyPulseGoal(HemorathEntity boss) {
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
		boss.setVisualState(HemorathEntity.VISUAL_EMPTY_PULSE, WINDUP_TICKS * HemorathVisualRules.GOAL_TICK_INTERVAL);
		if (boss.level() instanceof ServerLevel server) {
			server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
					SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.5F, 0.8F);
		}
	}

	@Override
	public void stop() {
		active = false;
		if (boss.getVisualState() == HemorathEntity.VISUAL_EMPTY_PULSE) {
			boss.setVisualState(HemorathEntity.VISUAL_NONE, 0);
		}
		cooldownTicks = COOLDOWN_TICKS;
	}

	@Override
	public void tick() {
		if (windupTicks > 0) {
			windupTicks--;

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
		boss.setVisualState(HemorathEntity.VISUAL_EMPTY_PULSE_IMPACT, HemorathVisualRules.PULSE_IMPACT);
		HemorathEffects.burst(server, origin, HemorathVisualRules.PULSE_RELEASE, (float) RADIUS);

		for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class,
				boss.getBoundingBox().inflate(RADIUS))) {
			if (victim == boss || victim.distanceToSqr(boss) > RADIUS * RADIUS) continue;
			Vec3 push = victim.position().subtract(origin).normalize().scale(KNOCKBACK_STRENGTH);
			victim.push(push.x, 0.4, push.z);
			victim.hurtMarked = true;
			victim.addEffect(new MobEffectInstance(EffectInit.hemophagy,
					HEMOPHAGY_DURATION_TICKS, 0, false, false, true));
		}
	}

}
