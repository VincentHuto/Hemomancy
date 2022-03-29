package com.vincenthuto.hemomancy.entity.mob;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.MathUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
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
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

public class EntityFungling extends PathfinderMob {

//	private Animation animation = NO_ANIMATION;
//	public static final Animation HEADBUTT_ANIMATION = new Animation(17);
//	public static final Animation SPOREPUFF_ANIMATION = new Animation(17);
	public int puffCooldown = 0;
//	private int animationTick;

	public EntityFungling(EntityType<? extends EntityFungling> type, Level worldIn) {
		super(type, worldIn);

	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
			MobSpawnType pReason, SpawnGroupData pSpawnData, CompoundTag pDataTag) {
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	public void sporePuff(Level world, AABB effectBounds, double x, double y, double z) {
		List<Entity> list = world.getEntities(this, effectBounds);
		for (Entity ent : list) {
			if (!(ent instanceof EntityFungling)) {
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
	public void aiStep() {
		super.aiStep();
//		Animation animation = getAnimation();
//		int animTick = getAnimationTick();
//
//		if (puffCooldown > 0) {
//			--puffCooldown;
//		}
//
//		if (animation == SPOREPUFF_ANIMATION) {
//			puffCooldown += 6;
//			if (animTick == 10)
////				playSound(SoundHandler.ENTITY_DARK_YOUNG_HIT, .25F, 1f);
//				if (!level.isClientSide && animTick >= 10) {
//					LivingEntity target = getTarget();
//					if (target != null) {
//						if (animTick % 30 == 0) {
//							sporePuff(level, new AABB(this.position().add(-3, -3, -3), this.position().add(3, 3, 3)),
//									this.position().x() + 0.5, this.position().y(), this.position().z() + 0.5);
//						}
//					}
//				}
//
//		} else if (animation == HEADBUTT_ANIMATION) {
//			if (animTick == 0) {
//				// playSound(SoundHandler.ENTITY_DARK_YOUNG_HIT, .25F, 1f);
//			} else if (animTick == 6)
//				attackInBox(getBoundingBox()
//						.move(Vector3d.directionFromRotation(isInWater() ? xRot : 0, yHeadRot).scale(1f)).inflate(0.85),
//						40);
//		}
	}

	public void attackInBox(AABB box, int disabledShieldTime) {
		List<LivingEntity> attackables = level.getEntitiesOfClass(LivingEntity.class, box,
				entity -> entity != this && !hasPassenger(entity));
		for (LivingEntity attacking : attackables) {
			doHurtTarget(attacking);
			if (disabledShieldTime > 0 && attacking instanceof Player) {
				Player player = ((Player) attacking);
				if (player.isUsingItem()) {
					player.getCooldowns().addCooldown(Items.SHIELD, disabledShieldTime);
					player.stopUsingItem();
					level.broadcastEntityEvent(player, (byte) 9);
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		// updateAnimations();
		LivingEntity target = getTarget();
		if (target == null)
			return;
		double distFromTarget = distanceToSqr(target);

		getLookControl().setLookAt(target, getMaxHeadYRot(), getMaxHeadXRot());
		boolean isClose = distFromTarget < 5;
		if (getNavigation().isDone())
			getNavigation().moveTo(target, 1.2);
		if (isClose) {
			yRotO = (float) MathUtils.getAngle(EntityFungling.this, target) + 90f;
		}
//		if (noActiveAnimation()) {
//			if (distFromTarget > 15 && distFromTarget < 30) {
//				AnimationPacket.send(EntityFungling.this, SPOREPUFF_ANIMATION);
//
//				if (!level.isClientSide) {
//					HLParticleUtils.spawnPoof((ServerLevel) level, new BlockPos(Vector3.fromEntityCenter(this).x,
//							Vector3.fromEntityCenter(this).y, Vector3.fromEntityCenter(this).z));
//					sporePuff(level, new AABB(this.position().add(-2, -2, -2), this.position().add(2, 2, 2)),
//							this.position().x() + 0.5, this.position().y(), this.position().z() + 0.5);
//				}
//
//			} else if (isClose && Mth.degreesDifferenceAbs((float) MathUtils.getAngle(EntityFungling.this, target) + 90,
//					yRot) < 30) {
//				AnimationPacket.send(EntityFungling.this, HEADBUTT_ANIMATION);
//			}
//		}
	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);

	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
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

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WOLF_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WOLF_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WOLF_HURT;
	}

//	@Override
//	public int getAnimationTick() {
//		return animationTick;
//	}
//
//	@Override
//	public void setAnimationTick(int tick) {
//		animationTick = tick;
//
//	}
//
//	@Override
//	public Animation getAnimation() {
//		return animation;
//	}
//
//	@Override
//	public void setAnimation(Animation animation) {
//		if (animation == null)
//			animation = NO_ANIMATION;
//		setAnimationTick(0);
//		this.animation = animation;
//	}
//
//	@Override
//	public Animation[] getAnimations() {
//		return new Animation[] { HEADBUTT_ANIMATION, SPOREPUFF_ANIMATION };
//	}
//
//	// Bite Goal
//	private class HeadButtGoal extends Goal {
//		public HeadButtGoal() {
//			setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
//		}
//
//		@Override
//		public boolean canUse() {
//			return !isControlledByLocalInstance() && getTarget() != null;
//		}
//
//		@Override
//		public void tick() {
//			LivingEntity target = getTarget();
//			if (target == null)
//				return;
//			double distFromTarget = distanceToSqr(target);
//
//			getLookControl().setLookAt(target, getMaxHeadYRot(), getMaxHeadXRot());
//
//			boolean isClose = distFromTarget < 5;
//
//			if (getNavigation().isDone())
//				getNavigation().moveTo(target, 1);
//
//			if (isClose)
//				yRot = (float) MathUtils.getAngle(EntityFungling.this, target) + 90f;
//			if (noActiveAnimation()) {
//				if (isClose && Mth.degreesDifferenceAbs((float) MathUtils.getAngle(EntityFungling.this, target) + 90,
//						yRot) < 30)
//					AnimationPacket.send(EntityFungling.this, HEADBUTT_ANIMATION);
//			}
//
//		}
//	}

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
				yBodyRotO = (float) MathUtils.getAngle(EntityFungling.this, target) + 90f;
//
//			if (noActiveAnimation()) {
//				if (distFromTarget > 40) {
//					AnimationPacket.send(EntityFungling.this, NO_ANIMATION);
//				}
//			}
		}

		/**
		 * Returns whether execution should begin. You can also read and cache any state
		 * necessary for execution in this method as well.
		 */
		@Override
		public boolean canUse() {
			this.targetEntity = this.creature.getTarget();
			if (this.targetEntity == null) {
				return false;
			} else if (this.targetEntity.distanceToSqr(this.creature) > this.maxTargetDistance
					* this.maxTargetDistance) {
				return false;
			} else {
				BlockPos vector3d = RandomPos.generateRandomPosTowardDirection(this.creature, 16, level.random,
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
		 * Returns whether an in-progress EntityAIBase should continue executing
		 */
		@Override
		public boolean canContinueToUse() {
			return !this.creature.getNavigation().isDone() && this.targetEntity.isAlive()
					&& this.targetEntity.distanceToSqr(this.creature) < this.maxTargetDistance * this.maxTargetDistance;
		}

		/**
		 * Reset the task's internal state. Called when this task is interrupted by
		 * another one
		 */
		@Override
		public void stop() {
			this.targetEntity = null;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		@Override
		public void start() {
			this.creature.getNavigation().moveTo(this.movePosX, this.movePosY, this.movePosZ, this.speed);
		}
	}

}
