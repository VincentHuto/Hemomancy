package com.vincenthuto.hemomancy.entity.mob;

import java.util.List;

import javax.annotation.Nullable;

import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.math.MathUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
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
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

public class EntityChitinite extends PathfinderMob{

//	private Animation animation = NO_ANIMATION;
//	public static final Animation ROLLUP_ANIMATION = new Animation(128);

	public int puffCooldown = 0;

	private int animationTick;

	public EntityChitinite(EntityType<? extends EntityChitinite> type, Level worldIn) {
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
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
			MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.populateDefaultEquipmentSlots(difficultyIn);

		return spawnDataIn;

	}

	public void sporePuff(Level world, AABB effectBounds, double x, double y, double z) {
		List<Entity> list = world.getEntities(this, effectBounds);
		for (Entity ent : list) {
			if (!(ent instanceof EntityChitinite)) {
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

	@SuppressWarnings("unused")
	@Override
	public void aiStep() {
		super.aiStep();
		//Animation animation = getAnimation();
		//int animTick = getAnimationTick();

		if (puffCooldown > 0) {
			--puffCooldown;
		}

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
	//	updateAnimations();
		LivingEntity target = getTarget();
		if (target == null)
			return;
		double distFromTarget = distanceToSqr(target);

		getLookControl().setLookAt(target, getMaxHeadYRot(), getMaxHeadXRot());
		boolean isClose = distFromTarget < 5;
		if (getNavigation().isDone())
			getNavigation().moveTo(target, 1.2);
		if (isClose) {
			yHeadRot = (float) MathUtils.getAngle(EntityChitinite.this, target) + 90f;
		}
//		if (noActiveAnimation()) {
//			if (isClose && Mth.degreesDifferenceAbs((float) MathUtils.getAngle(EntityChitinite.this, target) + 90,
//					yRot) < 30) {
//				AnimationPacket.send(EntityChitinite.this, ROLLUP_ANIMATION);
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
		//this.goalSelector.addGoal(1, new BreakBlockGoal(Blocks.OAK_WOOD, this, 1.5d, 10));
	//	this.goalSelector.addGoal(2, new RollupGoal(this, 1.0f));
	//	this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 1.15D).add(Attributes.ATTACK_DAMAGE, 1.0D);
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
//		return new Animation[] { ROLLUP_ANIMATION };
//	}
//
//	public boolean isVulnerable() {
//		return this.getHealth() <= this.getMaxHealth() / 4.0F;
//	}
//
//	// Roll Goal
//	private class RollupGoal extends Goal {
//		protected final PathfinderMob creature;
//		protected boolean running;
//
//		public RollupGoal(PathfinderMob creature, double speedIn) {
//			this.creature = creature;
//			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
//		}
//
//		@Override
//		public void tick() {
//			creature.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 100, false, false));
//			if (noActiveAnimation()) {
//				AnimationPacket.send(EntityChitinite.this, ROLLUP_ANIMATION);
//			}
//		}
//
//		/**
//		 * Returns whether execution should begin. You can also read and cache any state
//		 * necessary for execution in this method as well.
//		 */
//		@Override
//		public boolean canUse() {
//			if (this.creature.getLastHurtByMob() == null && !this.creature.isOnFire()) {
//				return false;
//			} else {
//				if (this.creature.isOnFire()) {
//					BlockPos blockpos = this.getRandPos(this.creature.level, this.creature, 5, 4);
//					if (blockpos != null) {
//						return true;
//					}
//				}
//
//				return this.findRandomPosition();
//			}
//		}
//
//		protected boolean findRandomPosition() {
//			Vector3d vector3d = RandomPositionGenerator.getPos(this.creature, 5, 4);
//			if (vector3d == null) {
//				return false;
//			} else {
//				return true;
//			}
//		}
//
//		@SuppressWarnings("unused")
//		public boolean isRunning() {
//			return this.running;
//		}
//
//		/**
//		 * Execute a one shot task or start executing a continuous task
//		 */
//		@Override
//		public void start() {
//			// this.creature.getNavigator().tryMoveToXYZ(this.randPosX, this.randPosY,
//			// this.randPosZ, this.speed);
//			this.running = true;
//		}
//
//		/**
//		 * Reset the task's internal state. Called when this task is interrupted by
//		 * another one
//		 */
//		@Override
//		public void stop() {
//			this.running = false;
//		}
//
//		/**
//		 * Returns whether an in-progress EntityAIBase should continue executing
//		 */
//		@Override
//		public boolean canContinueToUse() {
//			return !this.creature.getNavigation().isDone();
//		}
//
//		@Nullable
//		protected BlockPos getRandPos(BlockGetter worldIn, Entity entityIn, int horizontalRange, int verticalRange) {
//			BlockPos blockpos = entityIn.blockPosition();
//			int i = blockpos.getX();
//			int j = blockpos.getY();
//			int k = blockpos.getZ();
//			float f = horizontalRange * horizontalRange * verticalRange * 2;
//			BlockPos blockpos1 = null;
//			BlockPos. blockpos$mutable = new BlockPos.Mutable();
//
//			for (int l = i - horizontalRange; l <= i + horizontalRange; ++l) {
//				for (int i1 = j - verticalRange; i1 <= j + verticalRange; ++i1) {
//					for (int j1 = k - horizontalRange; j1 <= k + horizontalRange; ++j1) {
//						blockpos$mutable.set(l, i1, j1);
//						if (worldIn.getFluidState(blockpos$mutable).is(FluidTags.WATER)) {
//							float f1 = (l - i) * (l - i) + (i1 - j) * (i1 - j) + (j1 - k) * (j1 - k);
//							if (f1 < f) {
//								f = f1;
//								blockpos1 = new BlockPos(blockpos$mutable);
//
//							}
//						}
//					}
//				}
//			}
//
//			return blockpos1;
//		}
//	}

}
