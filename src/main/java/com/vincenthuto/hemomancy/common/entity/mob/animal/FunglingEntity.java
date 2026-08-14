package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.MathUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.RandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class FunglingEntity extends PathfinderMob {

// Move Goal
	private class MoveTowardsTargetGoal extends Goal {
		private final PathfinderMob creature;
		private LivingEntity targetEntity;
		private double movePosX;
		private double movePosY;
		private double movePosZ;
		private final double speed;
		private final float maxTargetDistance;

		public MoveTowardsTargetGoal(PathfinderMob creature, double speedIn, float targetMaxDistance) {
			this.creature = creature;
			this.speed = speedIn;
			this.maxTargetDistance = targetMaxDistance;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canContinueToUse() {
			return !this.creature.getNavigation().isDone() && this.targetEntity.isAlive()
					&& this.targetEntity.distanceToSqr(this.creature) < this.maxTargetDistance * this.maxTargetDistance;
		}

		@Override
		public boolean canUse() {
			this.targetEntity = this.creature.getTarget();
			if (this.targetEntity == null) {
				return false;
			} else if (this.targetEntity.distanceToSqr(this.creature) > this.maxTargetDistance
					* this.maxTargetDistance) {
				return false;
			} else {
				BlockPos vector3d = RandomPos.generateRandomPosTowardDirection(this.creature, 16, level().random,
						this.targetEntity.blockPosition());
				if (vector3d == null) {
					return false;

				} else {
					this.movePosX = vector3d.getX();
					this.movePosY = vector3d.getY();
					this.movePosZ = vector3d.getZ();
					return true;
				}
			}
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		@Override
		public void start() {
			this.creature.getNavigation().moveTo(this.movePosX, this.movePosY, this.movePosZ, this.speed);
		}

		/**
		 * Reset the task's internal state. Called when this task is interrupted by
		 * another one
		 */
		@Override
		public void stop() {
			this.targetEntity = null;
		}

		@Override
		public void tick() {
			LivingEntity target = getTarget();
			if (target == null)
				return;
			double distFromTarget = distanceToSqr(target);

			getLookControl().setLookAt(target, getMaxHeadYRot(), getMaxHeadXRot());

			boolean isClose = distFromTarget < 40;

			if (getNavigation().isDone())
				getNavigation().moveTo(target, 1.2);

			if (isClose)
				yBodyRotO = (float) MathUtils.getAngle(FunglingEntity.this, target) + 90f;
		}
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	public int puffCooldown = 0;

	public FunglingEntity(EntityType<? extends FunglingEntity> type, Level worldIn) {
		super(type, worldIn);

	}


	public void attackInBox(AABB box, int disabledShieldTime) {
		List<LivingEntity> attackables = level().getEntitiesOfClass(LivingEntity.class, box,
				entity -> entity != this && !hasPassenger(entity));
		for (LivingEntity attacking : attackables) {
			doHurtTarget(attacking);
			if (disabledShieldTime > 0 && attacking instanceof Player) {
				Player player = ((Player) attacking);
				if (player.isUsingItem()) {
					player.getCooldowns().addCooldown(Items.SHIELD, disabledShieldTime);
					player.stopUsingItem();
					level().broadcastEntityEvent(player, (byte) 9);
				}
			}
		}
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_FUNGLING_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_FUNGLING_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_FUNGLING_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	protected void registerGoals() {
		// goalSelector.addGoal(2, new HeadButtGoal());
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(1, new MoveTowardsTargetGoal(this, 0.5d, 50));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));

	}

	public void sporePuff(Level world, AABB effectBounds, double x, double y, double z) {
		List<Entity> list = world.getEntities(this, effectBounds);
		for (Entity ent : list) {
			if (!(ent instanceof FunglingEntity)) {
				LivingEntity liv = (LivingEntity) ent;
				liv.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 200));
				for (int countparticles = 0; countparticles <= 10; ++countparticles) {
					world.addParticle(GlowParticleFactory.createData(new ParticleColor(0, 150, 0)),
							getX() + HLParticleUtils.inRange(-0.25, 0.25),
							getY() + HLParticleUtils.inRange(-0.25, 0.25),
							getZ() + HLParticleUtils.inRange(-0.25, 0.25), 0, 0.000, 0);
					world.addParticle(GlowParticleFactory.createData(new ParticleColor(0, 250, 0)),
							getX() + HLParticleUtils.inRange(-0.25, 0.25), getY() + HLParticleUtils.inRange(-0.1, 0.1),
							getZ() + HLParticleUtils.inRange(-0.25, 0.25), 0, 0.000, 0);
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		LivingEntity target = getTarget();
		if (target == null)
			return;
		double distFromTarget = distanceToSqr(target);

		getLookControl().setLookAt(target, getMaxHeadYRot(), getMaxHeadXRot());
		boolean isClose = distFromTarget < 5;
		if (getNavigation().isDone())
			getNavigation().moveTo(target, 1.2);
		if (isClose) {
			yRotO = (float) MathUtils.getAngle(FunglingEntity.this, target) + 90f;
		}
	}

}
