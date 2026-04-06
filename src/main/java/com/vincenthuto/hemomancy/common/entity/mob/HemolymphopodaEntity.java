package com.vincenthuto.hemomancy.common.entity.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class HemolymphopodaEntity extends PathfinderMob {

	public final AnimationState idleAnimationState = new AnimationState();

	public HemolymphopodaEntity(EntityType<? extends HemolymphopodaEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.12D);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public MobType getMobType() {
		return MobType.WATER;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new PanicGoal(this, 1.4D));
		this.goalSelector.addGoal(1, new RandomStrollGoal(this, 0.5D));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	public static boolean canSpawnHere(EntityType<? extends HemolymphopodaEntity> type, LevelAccessor world,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		return world.getBlockState(below).isSolidRender(world, below)
				&& pos.getY() <= world.getSeaLevel() + 16;
	}

	@Override
	protected float getStandingEyeHeight(Pose poseIn, EntityDimensions sizeIn) {
		return sizeIn.height * 0.5F;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENDERMITE_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENDERMITE_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENDERMITE_HURT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.3F;
	}

	@Override
	public void tick() {
		if (this.level().isClientSide()) {
			this.idleAnimationState.startIfStopped(this.tickCount);
		}
		super.tick();
	}

	@Override
	public int getMaxAirSupply() {
		return 4800;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}
}
