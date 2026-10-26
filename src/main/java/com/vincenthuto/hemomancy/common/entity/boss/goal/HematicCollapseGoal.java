package com.vincenthuto.hemomancy.common.entity.boss.goal;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEntity;
import net.minecraft.ChatFormatting;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathVisualRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HemorathEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

/**
 * Hematic Collapse — Hemorath's signature reckoning. After a visible
 * telegraph window the boss discharges all accumulated blood debt as damage
 * against the target, then zeroes the debt.
 *
 * Damage formula: {@code damage = blood_debt * 0.5} (clamped to a floor so the
 * cast always has some impact).
 *
 * Cadence: PHASE_1_COOLDOWN_TICKS in Phase 1, PHASE_2_COOLDOWN_TICKS in Phase 2.
 */
public class HematicCollapseGoal extends Goal {

	private static final int TELEGRAPH_TICKS = HemorathVisualRules.COLLAPSE_WINDUP;
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
		boss.setVisualTarget(boss.getTarget());
		boss.setVisualState(HemorathEntity.VISUAL_COLLAPSE_WINDUP, telegraphTicks * HemorathVisualRules.GOAL_TICK_INTERVAL);
		if (boss.level() instanceof ServerLevel server) {
			server.playSound(null, boss.getX(), boss.getY(), boss.getZ(),
					SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 2.0F, 0.6F);
		}
		if (boss.getTarget() instanceof Player player) {
			player.displayClientMessage(
					Component.translatable("message.hemomancy.hematic_collapse_goal.the_hollow_vessel_draws_in_your").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
					true);
		}
	}

	@Override
	public void stop() {
		firing = false;
		boss.setCollapseCharging(false);
		boss.setVisualTarget(null);
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
		boss.setVisualTarget(target);

		if (telegraphTicks > 0) {
			telegraphTicks--;

			return;
		}

		fire(target);
		firing = false;
	}

	private void fire(LivingEntity target) {
		if (!(boss.level() instanceof ServerLevel server)) return;

		IBloodVolume blood = target instanceof Player player
				? HemoCapabilityAccess.getBloodVolume(player)
				: null;
		double debt = blood == null ? 0.0 : blood.getBloodDebt();
		float damage = (float) Math.max(DAMAGE_FLOOR, debt * DAMAGE_MULTIPLIER);
		if (target.hurt(boss.damageSources().mobAttack(boss), damage) && blood != null) {
			blood.consumeDebt();
		}

		server.playSound(null, target.getX(), target.getY(), target.getZ(),
				SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 2.0F, 0.5F);
		boss.setVisualState(HemorathEntity.VISUAL_COLLAPSE_IMPACT, HemorathVisualRules.COLLAPSE_IMPACT);
		HemorathEffects.burst(server, target.position().add(0, target.getBbHeight() * .6, 0), HemorathVisualRules.DEBT_HIT, 1.5F);
	}

}
