package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Mushroom Fields passive mob - an elegant, tall wading-bird-like creature
 * with long delicate legs and translucent skin revealing a visible network
 * of veins. It steps carefully through mushroom fields like a heron through
 * water. Eerily beautiful — its movements are slow, deliberate, almost regal.
 * Flees when attacked.
 */
public class VenousStriderEntity extends PathfinderMob {

	public final AnimationState idleAnimationState = new AnimationState();
	public final AnimationState walkAnimationState = new AnimationState();

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 12.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.2D);
	}

	public VenousStriderEntity(EntityType<? extends VenousStriderEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D));
		this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	public static boolean canSpawnHere(EntityType<? extends VenousStriderEntity> type, LevelAccessor world,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		return world.getBlockState(below).isSolidRender(world, below)
				&& world.getRawBrightness(pos, 0) > 8;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.PARROT_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PARROT_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.PARROT_HURT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	public boolean isMovingOnLand() {
		return this.onGround() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D
				&& !this.isInWaterOrBubble();
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			if (this.isMovingOnLand()) {
				this.walkAnimationState.startIfStopped(this.tickCount);
				this.idleAnimationState.stop();
			} else {
				this.walkAnimationState.stop();
				this.idleAnimationState.startIfStopped(this.tickCount);
			}
		}
	}
}
