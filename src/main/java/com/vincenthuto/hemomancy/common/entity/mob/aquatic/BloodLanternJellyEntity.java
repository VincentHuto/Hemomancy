package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

public class BloodLanternJellyEntity extends PathfinderMob {
	public final AnimationState idleAnimationState = new AnimationState();

	public BloodLanternJellyEntity(EntityType<? extends BloodLanternJellyEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new PanicGoal(this, 0.8D));
		this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 4.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.06D);
	}

	public static boolean canSpawnHere(EntityType<? extends BloodLanternJellyEntity> type, LevelAccessor level,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		return level.getFluidState(pos).is(FluidTags.WATER)
				&& level.getFluidState(pos.above()).is(FluidTags.WATER)
				&& pos.getY() <= level.getSeaLevel();
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (BloodLanternJellyMovementRules.canDrift(this.isInWater(), this.isAlive())) {
			double pulse = Math.sin((this.tickCount + this.getId()) * 0.12D);
			double turn = (this.tickCount + this.getId()) * 0.037D;
			boolean waterAbove = this.level().getFluidState(this.blockPosition().above()).is(FluidTags.WATER);
			boolean waterBelow = this.level().getFluidState(this.blockPosition().below()).is(FluidTags.WATER);
			Vec3 drift = new Vec3(
					BloodLanternJellyMovementRules.horizontalDrift(Math.cos(turn)),
					BloodLanternJellyMovementRules.verticalImpulse(waterAbove, waterBelow, pulse),
					BloodLanternJellyMovementRules.horizontalDrift(Math.sin(turn)));
			this.setDeltaMovement(this.getDeltaMovement().scale(BloodLanternJellyMovementRules.DAMPING).add(drift));
			this.move(MoverType.SELF, this.getDeltaMovement());
		} else {
			super.travel(travelVector);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(this.isInWater());
		if (this.level().isClientSide()) {
			this.idleAnimationState.startIfStopped(this.tickCount);
			if (this.random.nextInt(12) == 0) {
				this.level().addParticle(ParticleTypes.CRIMSON_SPORE, this.getX(), this.getY() + 0.25D, this.getZ(),
						0.0D, 0.01D, 0.0D);
			}
		}
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_BLOOD_LANTERN_JELLY_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_BLOOD_LANTERN_JELLY_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundInit.ENTITY_BLOOD_LANTERN_JELLY_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.2F;
	}
}
