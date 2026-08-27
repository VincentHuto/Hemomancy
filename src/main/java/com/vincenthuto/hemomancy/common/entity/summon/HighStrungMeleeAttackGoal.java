package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

public final class HighStrungMeleeAttackGoal extends MeleeAttackGoal {
	private int attackCooldown;

	public HighStrungMeleeAttackGoal(PathfinderMob mob, double speedModifier, boolean followWithoutSight) {
		super(mob, speedModifier, followWithoutSight);
	}

	@Override
	public void tick() {
		attackCooldown = Math.max(0, attackCooldown - 1);
		super.tick();
	}

	@Override
	protected boolean isTimeToAttack() {
		return attackCooldown <= 0;
	}

	@Override
	protected void resetAttackCooldown() {
		attackCooldown = PuppeteerSummonRules.highStrungAttackInterval(highStrungLevel());
	}

	private int highStrungLevel() {
		if (!(mob instanceof BoundPuppeteerSummon bound) || bound.hemomancy$isTrialSummon()) return 0;
		return BoundSummonBehavior.ownerFor(mob, bound)
				.filter(ServerPlayer.class::isInstance)
				.map(ServerPlayer.class::cast)
				.map(SkillPointHelper::getHighStrungLevel)
				.orElse(0);
	}
}
