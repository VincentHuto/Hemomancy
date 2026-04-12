package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

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
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

/**
 * Lush Caves / Flower Forest passive mob - a floating bioluminescent jellyfish
 * made of living blood. Hovers gracefully above the ground, pulsing with warm
 * crimson light. Hauntingly beautiful — delicate trailing filaments of
 * semi-transparent blood vessels drift behind it. Flees when attacked.
 */
public class HemojellyEntity extends PathfinderMob {

	public final AnimationState idleAnimationState = new AnimationState();

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 6.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.15D);
	}

	public HemojellyEntity(EntityType<? extends HemojellyEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.2D));
		this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.4D));
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	public static boolean canSpawnHere(EntityType<? extends HemojellyEntity> type, LevelAccessor world,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		return world.getBlockState(below).isSolidRender(world, below);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ALLAY_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ALLAY_HURT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.25f;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.idleAnimationState.startIfStopped(this.tickCount);
		}
	}
}
