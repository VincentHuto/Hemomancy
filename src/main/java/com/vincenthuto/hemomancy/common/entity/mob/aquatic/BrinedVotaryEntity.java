package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;

public class BrinedVotaryEntity extends Monster {
	public final AnimationState idleAnimationState = new AnimationState();
	public final AnimationState swimAnimationState = new AnimationState();

	public BrinedVotaryEntity(EntityType<? extends BrinedVotaryEntity> type, Level level) {
		super(type, level);
		this.setPathfindingMalus(PathType.WATER, 0.0F);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 14.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.18D)
				.add(Attributes.ATTACK_DAMAGE, BrinedVotaryBehaviorRules.ATTACK_DAMAGE)
				.add(Attributes.ARMOR, 2.0D)
				.add(Attributes.FOLLOW_RANGE, BrinedVotaryBehaviorRules.WAKE_RANGE_BLOCKS + 2.0D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, BrinedVotaryBehaviorRules.SWIM_SPEED, true));
		this.goalSelector.addGoal(6, new RandomSwimmingGoal(this, BrinedVotaryBehaviorRules.SWIM_SPEED, 30));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				target -> BrinedVotaryBehaviorRules.shouldWakeForTarget(target instanceof Player, target.isAlive(),
						this.distanceTo(target))));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			MobSpawnType reason, SpawnGroupData spawnData) {
		if (reason == MobSpawnType.STRUCTURE) {
			this.setPersistenceRequired();
		}
		return super.finalizeSpawn(level, difficulty, reason, spawnData);
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_BRINED_VOTARY_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_BRINED_VOTARY_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundInit.ENTITY_BRINED_VOTARY_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.35F;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			if (this.isInWaterOrBubble() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5D) {
				this.swimAnimationState.startIfStopped(this.tickCount);
			} else {
				this.swimAnimationState.stop();
				this.idleAnimationState.startIfStopped(this.tickCount);
			}
			if (this.random.nextInt(16) == 0
					&& this.level().getFluidState(this.blockPosition()).is(FluidTags.WATER)) {
				this.level().addParticle(ParticleTypes.BUBBLE, this.getX(), this.getY() + 1.25D, this.getZ(),
						0.0D, 0.04D, 0.0D);
			}
		}
	}
}
