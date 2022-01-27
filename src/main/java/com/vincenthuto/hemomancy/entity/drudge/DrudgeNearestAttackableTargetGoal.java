package com.vincenthuto.hemomancy.entity.drudge;

import java.util.EnumSet;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class DrudgeNearestAttackableTargetGoal<T extends LivingEntity> extends TargetGoal {
	protected final Class<T> targetClass;
	protected final int targetChance;
	protected LivingEntity nearestTarget;
	/**
	 * This filter is applied to the Entity search. Only matching entities will be
	 * targeted.
	 */
	protected TargetingConditions targetEntitySelector;

	public DrudgeNearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean checkSight) {
		this(goalOwnerIn, targetClassIn, checkSight, false);
	}

	public DrudgeNearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, boolean checkSight,
			boolean nearbyOnlyIn) {
		this(goalOwnerIn, targetClassIn, 10, checkSight, nearbyOnlyIn, (Predicate<LivingEntity>) null);
	}

	public DrudgeNearestAttackableTargetGoal(Mob goalOwnerIn, Class<T> targetClassIn, int targetChanceIn,
			boolean checkSight, boolean nearbyOnlyIn, @Nullable Predicate<LivingEntity> targetPredicate) {
		super(goalOwnerIn, checkSight, nearbyOnlyIn);
		this.targetClass = targetClassIn;
		this.targetChance = targetChanceIn;
		this.setFlags(EnumSet.of(Goal.Flag.TARGET));
		this.targetEntitySelector = (TargetingConditions.forNonCombat()).range(this.getFollowDistance())
				.selector(targetPredicate);
	}

	/**
	 * Returns whether execution should begin. You can also read and cache any state
	 * necessary for execution in this method as well.
	 */
	@Override
	public boolean canUse() {
		if (this.mob instanceof EntityDrudge) {
			EntityDrudge drudge = (EntityDrudge) mob;
			if (drudge.getRoleTitle() == EnumDrudgeRoles.ATTACK) {
				if (this.targetChance > 0 && this.mob.getRandom().nextInt(this.targetChance) != 0) {
					return false;
				} else {
					this.findNearestTarget();
					return this.nearestTarget != null;
				}
			}
		} else {
			return false;

		}
		return false;
	}

	protected AABB getTargetableArea(double targetDistance) {
		return this.mob.getBoundingBox().inflate(targetDistance, 14.0D, targetDistance);
	}

	protected void findNearestTarget() {
		if (this.targetClass != Player.class && this.targetClass != ServerPlayer.class) {
			this.nearestTarget = this.mob.level.getNearestEntity(this.targetClass, this.targetEntitySelector, this.mob,
					this.mob.getX(), this.mob.getEyeY(), this.mob.getZ(),
					this.getTargetableArea(this.getFollowDistance()));
		} else {
			this.nearestTarget = this.mob.level.getNearestPlayer(this.targetEntitySelector, this.mob, this.mob.getX(),
					this.mob.getEyeY(), this.mob.getZ());
		}

	}

	/**
	 * Execute a one shot task or start executing a continuous task
	 */
	@Override
	public void start() {
		this.mob.setTarget(this.nearestTarget);
		super.start();
	}

	public void setNearestTarget(@Nullable LivingEntity target) {
		this.nearestTarget = target;
	}
}