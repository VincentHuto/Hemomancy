package com.vincenthuto.hemomancy.common.entity.mob;

import java.util.EnumSet;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.init.SoundInit;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public class ErythromyceliumEruptusEntity extends Monster {
	private static final EntityDataAccessor<Integer> DATA_SWELL_DIR = SynchedEntityData
			.defineId(ErythromyceliumEruptusEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData
			.defineId(ErythromyceliumEruptusEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_ERUPTED = SynchedEntityData
			.defineId(ErythromyceliumEruptusEntity.class, EntityDataSerializers.BOOLEAN);

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	private int oldSwell;
	private int swell;
	private int maxSwell = 30;

	private int oldErupt;
	private int erupt;
	private int maxErupt = 30;


	public final AnimationState disguisedAnimationState = new AnimationState();
	public final AnimationState walkAnimationState = new AnimationState();
	public AnimationState eruptAnimationState = new AnimationState();
	public AnimationState idleAnimationState = new AnimationState();

	public ErythromyceliumEruptusEntity(EntityType<? extends ErythromyceliumEruptusEntity> type, Level worldIn) {
		super(type, worldIn);

	}

	public boolean isErupted() {
		return this.entityData.get(DATA_IS_ERUPTED);
	}


	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_SWELL_DIR, -1);
		this.entityData.define(DATA_IS_IGNITED, false);
		this.entityData.define(DATA_IS_ERUPTED, false);

	}

	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		pCompound.putShort("Fuse", (short) this.maxSwell);
		pCompound.putBoolean("ignited", this.isIgnited());
		pCompound.putBoolean("erupted", this.isErupted());

	}

	public boolean isIgnited() {
		return this.entityData.get(DATA_IS_IGNITED);
	}

	public void ignite() {
		this.entityData.set(DATA_IS_IGNITED, true);
	}

	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		if (pCompound.contains("Fuse", 99)) {
			this.maxSwell = pCompound.getShort("Fuse");
		}

	}

	public int getSwellDir() {
		return this.entityData.get(DATA_SWELL_DIR);
	}

	/**
	 * Sets the state of creeper, -1 to idle and 1 to be 'in fuse'
	 */
	public void setSwellDir(int pState) {
		this.entityData.set(DATA_SWELL_DIR, pState);
	}

	public float getSwelling(float pPartialTicks) {
		return Mth.lerp(pPartialTicks, (float) this.oldSwell, (float) this.swell) / (float) (this.maxSwell - 2);
	}
	
	public float getErupting(float pPartialTicks) {
		return Mth.lerp(pPartialTicks, (float) this.oldErupt, (float) this.swell) / (float) (this.maxErupt - 2);
	}
	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
			MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn, @Nullable CompoundTag dataTag) {
		spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
		this.populateDefaultEquipmentSlots(random, difficultyIn);

		return spawnDataIn;

	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_ABHORENT_THOUGHT_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_ABHORENT_THOUGHT_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_ABHORENT_THOUGHT_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(2, new AmbushEruptGoal(this));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(5, new EruptedRandomStrollGoal(this, 0.8D));

	}

	private boolean isMovingOnLand() {
		return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D
				&& !this.isInWaterOrBubble();
	}

	private boolean isMovingInWater() {
		return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D && this.isInWaterOrBubble();
	}

	@Override
	public void tick() {
		super.tick();

		if (this.isAlive() && !this.isErupted()) {
			this.oldSwell = this.swell;
			if (this.isIgnited()) {
				this.setSwellDir(1);
			}

			int i = this.getSwellDir();
			if (i > 0 && this.swell == 0) {
				this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
				this.gameEvent(GameEvent.PRIME_FUSE);
			}

			this.swell += i;
			if (this.swell < 0) {
				this.swell = 0;
			}

			if (this.swell >= this.maxSwell) {
				this.swell = this.maxSwell;
				this.erupt();
			}
		}

		if (this.isAlive() && this.isErupted()) {
//			this.disguisedAnimationState.stop();
//			this.idleAnimationState.stop();
//			this.walkAnimationState.stop();
			this.oldErupt = this.erupt;

			this.erupt += 1;
			if (this.erupt < 0) {
				this.erupt = 0;
			}

			if (this.erupt > this.maxErupt) {
				this.erupt = this.maxErupt;
				this.eruptAnimationState.stop();
			}
		}
		
		if (this.level().isClientSide()) {

			if (this.isErupted() && this.erupt == this.maxErupt) {
				if (this.isMovingOnLand()) {
					this.disguisedAnimationState.stop();
					this.idleAnimationState.stop();
					this.eruptAnimationState.stop();

					this.walkAnimationState.startIfStopped(this.tickCount);
				} else {
					this.disguisedAnimationState.stop();
					this.walkAnimationState.stop();
					this.eruptAnimationState.stop();
					this.idleAnimationState.startIfStopped(this.tickCount);

				}
			} else {
				if(!(this.erupt >0)) {
					this.disguisedAnimationState.startIfStopped(this.tickCount);
				}else {
					this.disguisedAnimationState.stop();
					this.idleAnimationState.stop();
					this.walkAnimationState.stop();

					this.eruptAnimationState.startIfStopped(this.tickCount);

				}
			}

		}

	}

	private void erupt() {
		this.disguisedAnimationState.stop();
		this.eruptAnimationState.startIfStopped(erupt);
		this.entityData.set(DATA_IS_ERUPTED, true);

//		 if (!this.level().isClientSide) {
//	         float f = this.isPowered() ? 2.0F : 1.0F;
//	         this.dead = true;
//	         this.level().explode(this, this.getX(), this.getY(), this.getZ(), (float)this.explosionRadius * f, Level.ExplosionInteraction.MOB);
//	         this.discard();
//	         this.spawnLingeringCloud();
//	      }		
	}
	public static class AmbushEruptGoal extends Goal {
		private final ErythromyceliumEruptusEntity eruptus;
		@Nullable
		private LivingEntity target;

		public AmbushEruptGoal(ErythromyceliumEruptusEntity pErythromyceliumEruptusEntity) {
			this.eruptus = pErythromyceliumEruptusEntity;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		/**
		 * Returns whether execution should begin. You can also read and cache any state
		 * necessary for execution in this method as well.
		 */
		public boolean canUse() {
			LivingEntity livingentity = this.eruptus.getTarget();
			return this.eruptus.getSwellDir() > 0
					|| livingentity != null && this.eruptus.distanceToSqr(livingentity) < 9.0D;
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void start() {
			this.eruptus.getNavigation().stop();
			this.target = this.eruptus.getTarget();
		}

		/**
		 * Reset the task's internal state. Called when this task is interrupted by
		 * another one
		 */
		public void stop() {
			this.target = null;
		}

		public boolean requiresUpdateEveryTick() {
			return true;
		}

		/**
		 * Keep ticking a continuous task that has already been started
		 */
		public void tick() {
			if (this.target == null) {
				this.eruptus.setSwellDir(-1);
			} else if (this.eruptus.distanceToSqr(this.target) > 49.0D) {
				this.eruptus.setSwellDir(-1);
			} else if (!this.eruptus.getSensing().hasLineOfSight(this.target)) {
				this.eruptus.setSwellDir(-1);
			} else {
				this.eruptus.setSwellDir(1);
			}
		}
	}

	public static class EruptedRandomStrollGoal extends RandomStrollGoal {
		public static final float PROBABILITY = 0.001F;
		protected final float probability;

		public EruptedRandomStrollGoal(ErythromyceliumEruptusEntity pMob, double pSpeedModifier) {
			this(pMob, pSpeedModifier, 0.001F);
		}

		public EruptedRandomStrollGoal(ErythromyceliumEruptusEntity pMob, double pSpeedModifier, float pProbability) {
			super(pMob, pSpeedModifier);
			this.probability = pProbability;
		}

		@Override
		public boolean canUse() {
			if (((ErythromyceliumEruptusEntity) mob).isErupted()) {
				return super.canUse();
			} else {
				return false;
			}
		}

		@Nullable
		protected Vec3 getPosition() {
			if (this.mob.isInWaterOrBubble()) {
				Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7);
				return vec3 == null ? super.getPosition() : vec3;
			} else {

				return this.mob.getRandom().nextFloat() >= this.probability ? LandRandomPos.getPos(this.mob, 10, 7)
						: super.getPosition();
			}
		}
	}
}
