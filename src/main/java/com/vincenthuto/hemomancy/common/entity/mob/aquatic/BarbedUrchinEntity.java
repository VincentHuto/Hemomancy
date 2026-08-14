package com.vincenthuto.hemomancy.common.entity.mob.aquatic;


import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.worldgen.ErythrocoralReefTuning;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

public class BarbedUrchinEntity extends AbstractFish {
	public final AnimationState idleAnimationState = new AnimationState();

	private static final EntityDataAccessor<Integer> PUFF_STATE = SynchedEntityData.defineId(BarbedUrchinEntity.class,
			EntityDataSerializers.INT);
	int inflateCounter;
	int deflateTimer;
	private static final Predicate<LivingEntity> SCARY_MOB = (p_289442_) -> {
		if (p_289442_ instanceof Player && ((Player) p_289442_).isCreative()) return false;
		return p_289442_.getType() == EntityType.AXOLOTL || !p_289442_.getType().is(EntityTypeTags.AQUATIC);
	};
	static final TargetingConditions targetingConditions = TargetingConditions.forNonCombat()
			.ignoreInvisibilityTesting().ignoreLineOfSight().selector(SCARY_MOB);
	public static final int STATE_SMALL = 0;
	public static final int STATE_MID = 1;
	public static final int STATE_FULL = 2;

	public BarbedUrchinEntity(EntityType<? extends BarbedUrchinEntity> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
		this.refreshDimensions();
	}

	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(PUFF_STATE, 0);
	}

	public int getPuffState() {
		return this.entityData.get(PUFF_STATE);
	}

	public void setPuffState(int pPuffState) {
		this.entityData.set(PUFF_STATE, pPuffState);
	}

	public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
		if (PUFF_STATE.equals(pKey)) {
			this.refreshDimensions();
		}

		super.onSyncedDataUpdated(pKey);
	}

	public void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		pCompound.putInt("PuffState", this.getPuffState());
	}

	/**
	 * (abstract) Protected helper method to read subclass entity data from NBT.
	 */
	public void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		this.setPuffState(Math.min(pCompound.getInt("PuffState"), 2));
	}

	public ItemStack getBucketItemStack() {
		return new ItemStack(Items.TADPOLE_BUCKET);
	}

	protected void registerGoals() {
		this.goalSelector.addGoal(1, new BarbedUrchinEntity.BarbedUrchinEntityPuffGoal(this));
	}

	public static boolean canSpawnHere(EntityType<? extends AbstractFish> fish, LevelAccessor world,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		int seaLevel = world.getSeaLevel();
		int minY = seaLevel - ErythrocoralReefTuning.URCHIN_MAX_DEPTH_BELOW_SEA_LEVEL;
		int maxY = seaLevel - ErythrocoralReefTuning.URCHIN_MIN_DEPTH_BELOW_SEA_LEVEL;
		boolean isAllNeighborsSource = isSourceBlock(world, pos.north()) && isSourceBlock(world, pos.south())
				&& isSourceBlock(world, pos.west()) && isSourceBlock(world, pos.east());
		return isSourceBlock(world, pos) && isAllNeighborsSource && pos.getY() >= minY && pos.getY() <= maxY;
	}
    private static boolean isSourceBlock(LevelAccessor world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof LiquidBlock && world.getBlockState(pos).is(Blocks.WATER) && state.getValue(LiquidBlock.LEVEL) == 0;
    }

	@Override
	public boolean checkSpawnObstruction(LevelReader pLevel) {
		return pLevel.isUnobstructed(this);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	@Override
	public void travel(Vec3 pTravelVector) {
		if (this.isEffectiveAi() && this.isInWater()) {
			this.moveRelative(-0.01F, pTravelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
			if (this.getTarget() == null) {
				this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.025D, 0.0D));
			}
		} else {
			super.travel(pTravelVector);
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */

	public void tick() {
		if (this.level().isClientSide()) {
			this.idleAnimationState.startIfStopped(this.tickCount);

		}

		if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
			if (this.inflateCounter > 0) {
				if (this.getPuffState() == 0) {
					this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getVoicePitch());
					this.setPuffState(1);
				} else if (this.inflateCounter > 40 && this.getPuffState() == 1) {
					this.playSound(SoundEvents.PUFFER_FISH_BLOW_UP, this.getSoundVolume(), this.getVoicePitch());
					this.setPuffState(2);
				}

				++this.inflateCounter;
			} else if (this.getPuffState() != 0) {
				if (this.deflateTimer > 60 && this.getPuffState() == 2) {
					this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getVoicePitch());
					this.setPuffState(1);
				} else if (this.deflateTimer > 100 && this.getPuffState() == 1) {
					this.playSound(SoundEvents.PUFFER_FISH_BLOW_OUT, this.getSoundVolume(), this.getVoicePitch());
					this.setPuffState(0);
				}

				++this.deflateTimer;
			}
		}

		super.tick();
	}

	/**
	 * Called every tick so the entity can update its state as required. For
	 * example, zombies and skeletons use this to react to sunlight and start to
	 * burn.
	 */
	public void aiStep() {
		super.aiStep();
		if (this.isAlive() && this.getPuffState() > 0) {
			for (Mob mob : this.level().getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(0.3D),
					(p_149013_) -> {
						return targetingConditions.test(this, p_149013_);
					})) {
				if (mob.isAlive()) {
					this.touch(mob);
				}
			}
		}

	}

	private void touch(Mob pMob) {
		int i = this.getPuffState();
		if (pMob.hurt(this.damageSources().mobAttack(this), (float) (1 + i))) {
			pMob.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * i, 0), this);
			this.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
		}

	}

	/**
	 * Called by a player entity when they collide with an entity
	 */
	public void playerTouch(Player pEntity) {
		int i = this.getPuffState();
		if (pEntity instanceof ServerPlayer && i > 0
				&& pEntity.hurt(this.damageSources().mobAttack(this), (float) (1 + i))) {
			if (!this.isSilent()) {
				((ServerPlayer) pEntity).connection
						.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
			}

			pEntity.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * i, 0), this);
		}

	}

	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_BARBED_URCHIN_AMBIENT.get();
	}

	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_BARBED_URCHIN_DEATH.get();
	}

	protected SoundEvent getHurtSound(DamageSource pDamageSource) {
		return SoundInit.ENTITY_BARBED_URCHIN_HURT.get();
	}

	protected SoundEvent getFlopSound() {
		return SoundEvents.PUFFER_FISH_FLOP;
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pPose) {
		return super.getDefaultDimensions(pPose).scale(getScale(this.getPuffState()));
	}

	private static float getScale(int pPuffState) {
		switch (pPuffState) {
		case 0:
			return 0.5F;
		case 1:
			return 0.7F;
		default:
			return 1.0F;
		}
	}

	static class BarbedUrchinEntityPuffGoal extends Goal {
		private final BarbedUrchinEntity fish;

		public BarbedUrchinEntityPuffGoal(BarbedUrchinEntity pFish) {
			this.fish = pFish;
		}

		public boolean canUse() {
			List<LivingEntity> list = this.fish.level().getEntitiesOfClass(LivingEntity.class,
					this.fish.getBoundingBox().inflate(2.0D), (p_149015_) -> {
						return BarbedUrchinEntity.targetingConditions.test(this.fish, p_149015_);
					});
			return !list.isEmpty();
		}

		/**
		 * Execute a one shot task or start executing a continuous task
		 */
		public void start() {
			this.fish.inflateCounter = 1;
			this.fish.deflateTimer = 0;
		}

		/**
		 * Reset the task's internal state. Called when this task is interrupted by
		 * another one
		 */
		public void stop() {
			this.fish.inflateCounter = 0;
		}
	}
}
