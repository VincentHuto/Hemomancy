package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

public class FargoneEntity extends Monster {
	private static final String VARIANT_TAG = "FargoneVariant";
	private static final EntityDataAccessor<Integer> VARIANT =
			SynchedEntityData.defineId(FargoneEntity.class, EntityDataSerializers.INT);

	public final AnimationState idleAnimationState = new AnimationState();
	public final AnimationState walkAnimationState = new AnimationState();

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	public FargoneEntity(EntityType<? extends FargoneEntity> type, Level worldIn) {
		super(type, worldIn);

	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(VARIANT, FargoneVariant.SCOUT.id());
	}

	public FargoneVariant getVariant() {
		return FargoneVariant.fromId(this.entityData.get(VARIANT));
	}

	public void setVariant(FargoneVariant variant) {
		this.entityData.set(VARIANT, variant == null ? FargoneVariant.SCOUT.id() : variant.id());
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType,
			SpawnGroupData spawnData) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnData);
		if (spawnType != MobSpawnType.COMMAND && spawnType != MobSpawnType.SPAWN_EGG) {
			setVariant(FargoneVariant.random(level.getRandom()));
		}
		return data;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(VARIANT_TAG, this.entityData.get(VARIANT));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.entityData.set(VARIANT, tag.contains(VARIANT_TAG) ? tag.getInt(VARIANT_TAG) : FargoneVariant.SCOUT.id());
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}


	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_FARGONE_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_FARGONE_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_FARGONE_HURT.get();
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
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));

	}

	public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> pType, ServerLevelAccessor pLevel,
			MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
		return pLevel.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(pLevel, pPos, pRandom)
				&& checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
	}

	public static boolean isDarkEnoughToSpawn(ServerLevelAccessor pLevel, BlockPos pPos, RandomSource pRandom) {
		if (pLevel.getBrightness(LightLayer.SKY, pPos) > pRandom.nextInt(32)) {
			return false;
		} else {
			DimensionType dimensiontype = pLevel.dimensionType();
			int i = dimensiontype.monsterSpawnBlockLightLimit();
			if (i < 15 && pLevel.getBrightness(LightLayer.BLOCK, pPos) > i) {
				return false;
			} else {
				int j = pLevel.getLevel().isThundering() ? pLevel.getMaxLocalRawBrightness(pPos, 10)
						: pLevel.getMaxLocalRawBrightness(pPos);
				return j <= dimensiontype.monsterSpawnLightTest().sample(pRandom);
			}
		}
	}

	public boolean isMovingOnLand() {
		return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D
				&& !this.isInWaterOrBubble();
	}

	public boolean isMovingInWater() {
		return this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D && this.isInWaterOrBubble();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			if (this.isMovingOnLand()) {
				this.walkAnimationState.startIfStopped(this.tickCount);
			} else {
				this.walkAnimationState.stop();
				this.idleAnimationState.startIfStopped(this.tickCount);

			}

		}
	}
}
