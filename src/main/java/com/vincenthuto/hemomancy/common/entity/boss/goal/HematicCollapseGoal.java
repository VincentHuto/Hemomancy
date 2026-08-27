package com.vincenthuto.hemomancy.common.entity.boss.goal;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

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
	private static final double MAX_RANGE_SQR = 32.0D * 32.0D;

	private final HemorathEntity boss;
	private int telegraphTicks;
	private int cooldownTicks;
	private boolean firing;

	public HematicCollapseGoal(HemorathEntity boss) {
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
		return target != null && target.isAlive() && boss.distanceToSqr(target) < MAX_RANGE_SQR;
	}

	@Override
	public boolean canContinueToUse() {
		LivingEntity target = boss.getTarget();
		return firing && target != null && target.isAlive() && target.level() == boss.level()
				&& boss.distanceToSqr(target) < MAX_RANGE_SQR;
	}

	@Override
	public void start() {
		telegraphTicks = boss.isInPhase2() ? TELEGRAPH_TICKS / 2 : TELEGRAPH_TICKS;
		firing = true;
		boss.setCollapseCharging(true);
		boss.setVisualState(HemorathEntity.VISUAL_COLLAPSE_WINDUP, telegraphTicks);
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
		if (boss.getVisualState() == HemorathEntity.VISUAL_COLLAPSE_WINDUP) {
			boss.setVisualState(HemorathEntity.VISUAL_NONE, 0);
		}
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
				sendDebtTether(server, target);
			}
			return;
		}

		fire(target);
		firing = false;
	}

	private void fire(LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) return;

		IBloodVolume blood = target instanceof Player player
				? HemoCapabilityAccess.getBloodVolume(player).orElse(null)
				: null;
		double debt = blood == null ? 0.0 : blood.getBloodDebt();
		float damage = (float) Math.max(DAMAGE_FLOOR, debt * DAMAGE_MULTIPLIER);
		if (target.hurt(boss.damageSources().mobAttack(boss), damage) && blood != null) {
			blood.consumeDebt();
		}

		server.playSound(null, target.getX(), target.getY(), target.getZ(),
				SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, 0.5F);
		boss.setVisualState(HemorathEntity.VISUAL_COLLAPSE_IMPACT, 14);
		server.sendParticles(ParticleTypes.SCULK_SOUL,
				target.getX(), target.getY() + 1.0, target.getZ(),
				40, 0.6, 1.0, 0.6, 0.1);
		server.sendParticles(ParticleTypes.LARGE_SMOKE,
				target.getX(), target.getY() + 1.0, target.getZ(),
				25, 0.8, 0.8, 0.8, 0.05);
	}

	private void sendDebtTether(ServerLevel server, LivingEntity target) {
		Vec3 from = boss.position().add(0.0, 1.2, 0.0);
		Vec3 to = target.position().add(0.0, target.getBbHeight() * 0.65, 0.0);
		Vec3 delta = to.subtract(from);
		for (int i = 1; i <= 10; i++) {
			Vec3 point = from.add(delta.scale(i / 11.0D));
			server.sendParticles(ParticleTypes.SCULK_SOUL,
					point.x, point.y, point.z,
					1, 0.02, 0.02, 0.02, 0.0);
		}
	}
}
