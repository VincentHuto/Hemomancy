package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.dimension.DimensionType;

import javax.annotation.Nullable;
import com.vincenthuto.hemomancy.common.entity.animation.AnimationCycleManager;

public class AbhorentThoughtEntity extends Monster {

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	public int puffCooldown = 0;
	public final AnimationState idleAnimationState = new AnimationState();
	public final AnimationState walkAnimationState = new AnimationState();

	// Animation cycle completion manager
	private final AnimationCycleManager animationManager = new AnimationCycleManager();

	public AbhorentThoughtEntity(EntityType<? extends AbhorentThoughtEntity> type, Level worldIn) {
		super(type, worldIn);

	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn,
			MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {
		spawnDataIn = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
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

	/**
	 * Static predicate for determining whether a monster can spawn at the provided
	 * location, incorporating a check of the current light level at the location.
	 */
	public static boolean checkMonsterSpawnRules(EntityType<? extends Monster> pType, ServerLevelAccessor pLevel,
			MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
		return pLevel.getDifficulty() != Difficulty.PEACEFUL && isDarkEnoughToSpawn(pLevel, pPos, pRandom)
				&& checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));

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
		if (this.level().isClientSide()) {
			if (this.isMovingOnLand()) {
				// Cancel any pending finish-walk countdown — we're moving again
				animationManager.resetAnimationFinish("walk");
				animationManager.startAnimation(walkAnimationState, "walk", 20, this.tickCount);
				this.idleAnimationState.stop();
			} else if (animationManager.isAnimationFinishing("walk")) {
				// Waiting for the current walk cycle to complete before switching to idle
				animationManager.updateAnimationFinish("walk");
				if (!animationManager.isAnimationFinishing("walk")) {
					this.walkAnimationState.stop();
					this.idleAnimationState.startIfStopped(this.tickCount);
				}
			} else if (this.walkAnimationState.isStarted()) {
				// Entity just stopped moving — initiate finish sequence
				animationManager.initiateAnimationFinish("walk", 20, this.tickCount);
			} else {
				this.idleAnimationState.startIfStopped(this.tickCount);
			}
		}
	}
}
