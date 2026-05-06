package com.vincenthuto.hemomancy.common.entity.boss.goal;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HollowVesselEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Hematic Collapse — the Hollow Vessel's signature reckoning. After a visible
 * telegraph window the boss discharges all accumulated blood debt as damage
 * against the target, then zeroes the debt.
 *
 * Damage formula: {@code damage = blood_debt * 0.5} (clamped to a floor so the
 * cast always has some impact).
 *
 * Cadence: PHASE_1_COOLDOWN_TICKS in Phase 1, PHASE_2_COOLDOWN_TICKS in Phase 2.
 */
public class HematicCollapseGoal extends Goal {

	private static final int TELEGRAPH_TICKS = 60;
	private static final int PHASE_1_COOLDOWN_TICKS = 200;
	private static final int PHASE_2_COOLDOWN_TICKS = 120;
	private static final float DAMAGE_FLOOR = 2.0F;
	private static final double DAMAGE_MULTIPLIER = 0.5D;

	private final HollowVesselEntity boss;
	private int telegraphTicks;
	private int cooldownTicks;
	private boolean firing;

	public HematicCollapseGoal(HollowVesselEntity boss) {
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
		return target != null && target.isAlive() && boss.distanceToSqr(target) < 32.0 * 32.0;
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity target = boss.getTarget();
		return firing && target != null && target.isAlive();
	}

	@Override
	public void start() {
		telegraphTicks = boss.isInPhase2() ? TELEGRAPH_TICKS / 2 : TELEGRAPH_TICKS;
		firing = true;
		boss.setCollapseCharging(true);
		if (boss.level() instanceof ServerLevel server) {
			server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
					SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.6F);
		}
		if (boss.getTarget() instanceof Player player) {
			player.displayClientMessage(
					Component.literal("The Hollow Vessel draws in your debt...")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					true);
		}
	}

	@Override
	public void stop() {
		firing = false;
		boss.setCollapseCharging(false);
		cooldownTicks = boss.isInPhase2() ? PHASE_2_COOLDOWN_TICKS : PHASE_1_COOLDOWN_TICKS;
	}

	@Override
	public void tick() {
		LivingEntity target = boss.getTarget();
		if (target == null) {
			firing = false;
			return;
		}
		boss.getLookControl().setLookAt(target, 30.0F, 30.0F);

		if (telegraphTicks > 0) {
			telegraphTicks--;
			if (boss.level() instanceof ServerLevel server && telegraphTicks % 4 == 0) {
				server.sendParticles(ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
						target.getX(), target.getY() + 1.5, target.getZ(),
						8, 0.4, 0.6, 0.4, 0.01);
				server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
						boss.getX(), boss.getY() + 1.0, boss.getZ(),
						10, 0.5, 0.8, 0.5, 0.02);
			}
			return;
		}

		fire(target);
		firing = false;
	}

	private void fire(LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) return;

		double debt = 0.0;
		if (target instanceof Player player) {
			debt = HemoCapabilityAccess.getBloodVolume(player)
					.map(v -> v.consumeDebt())
					.orElse(0.0);
		}

		float damage = (float) Math.max(DAMAGE_FLOOR, debt * DAMAGE_MULTIPLIER);
		target.hurt(boss.damageSources().mobAttack(boss), damage);

		server.playSound(null, target.getX(), target.getY(), target.getZ(),
				SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, 0.5F);
		server.sendParticles(ParticleTypes.SCULK_SOUL,
				target.getX(), target.getY() + 1.0, target.getZ(),
				40, 0.6, 1.0, 0.6, 0.1);
		server.sendParticles(ParticleTypes.LARGE_SMOKE,
				target.getX(), target.getY() + 1.0, target.getZ(),
				25, 0.8, 0.8, 0.8, 0.05);
	}
}
