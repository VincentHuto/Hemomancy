package com.vincenthuto.hemomancy.common.entity.summon;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Spectral Companion — a ghostly warrior summoned from the Unstained's past.
 * Follows the summoner, attacks hostile mobs, and dissipates after a set
 * duration. It is semi-transparent and emits soul-fire particles.
 */
public class SpectralCompanionEntity extends PathfinderMob implements OwnableEntity {

	private static final int DEFAULT_DURATION_TICKS = 1200; // 60 seconds
	private int remainingTicks;
	@Nullable
	private LivingEntity summoner;
	@Nullable
	private UUID summonerUUID;

	public SpectralCompanionEntity(EntityType<? extends PathfinderMob> type, Level worldIn) {
		super(type, worldIn);
		this.remainingTicks = DEFAULT_DURATION_TICKS;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 30.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 6.0D)
				.add(Attributes.FOLLOW_RANGE, 32.0D)
				.add(Attributes.ARMOR, 8.0D);
	}

	public SpectralCompanionEntity setSummoner(LivingEntity summoner, int durationTicks) {
		this.summoner = summoner;
		this.summonerUUID = summoner.getUUID();
		this.remainingTicks = durationTicks;
		return this;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
		this.goalSelector.addGoal(4, new FollowSummonerGoal(this, 1.0D, 6.0F, 2.0F));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

		this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
				e -> e != summoner));
	}

	@Override
	public void tick() {
		super.tick();

		remainingTicks--;

		// Particle effects — spectral soul-fire particles
		if (this.level().isClientSide) {
			float f = (this.random.nextFloat() - 0.5F) * 0.8F;
			float f2 = (this.random.nextFloat() - 0.5F) * 0.8F;
			this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
					this.getX() + f, this.getY() + 0.5 + this.random.nextFloat() * 0.5, this.getZ() + f2,
					0.0D, 0.02D, 0.0D);
			if (this.tickCount % 5 == 0) {
				this.level().addParticle(ParticleTypes.SOUL,
						this.getX() + f, this.getY() + 1.0, this.getZ() + f2,
						0.0D, 0.05D, 0.0D);
			}
		}

		// Dissipate when duration expires
		if (!this.level().isClientSide && remainingTicks <= 0) {
			dissipate();
		}

		// Dissipate if summoner is gone
		if (!this.level().isClientSide && summoner != null && !summoner.isAlive()) {
			dissipate();
		}
	}

	private void dissipate() {
		this.level().playSound(null, this.blockPosition(),
				SoundEvents.SOUL_ESCAPE, SoundSource.NEUTRAL, 1.0f, 1.0f);
		this.discard();
	}

	@Override
	public void onAddedToWorld() {
		super.onAddedToWorld();
		// Limit number of spectral companions per summoner to 2
		if (!this.level().isClientSide() && summoner != null) {
			List<SpectralCompanionEntity> existing = this.level().getEntitiesOfClass(
					SpectralCompanionEntity.class,
					this.getBoundingBox().inflate(32.0),
					e -> e != this && summoner.getUUID().equals(e.summonerUUID));
			existing.sort((a, b) -> b.tickCount - a.tickCount);
			int removeCount = existing.size() - 1; // keep max 2 (including this one)
			for (int i = 0; i < removeCount; i++) {
				existing.get(i).discard();
			}
		}
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.SOUL_ESCAPE;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.GLASS_BREAK;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.SOUL_ESCAPE;
	}

	@Override
	protected float getSoundVolume() {
		return 0.6f;
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		// Immune to fire and fall damage
		if (source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)
				|| source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)) {
			return false;
		}
		return super.hurt(source, amount);
	}

	@Override
	public boolean isAffectedByPotions() {
		return false;
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Nullable
	@Override
	public UUID getOwnerUUID() {
		return summonerUUID;
	}

	@Nullable
	public LivingEntity getSummoner() {
		return summoner;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("RemainingTicks", remainingTicks);
		if (summonerUUID != null) {
			tag.putUUID("SummonerUUID", summonerUUID);
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		remainingTicks = tag.getInt("RemainingTicks");
		if (tag.hasUUID("SummonerUUID")) {
			summonerUUID = tag.getUUID("SummonerUUID");
		}
	}

	/**
	 * Simple follow-summoner goal that keeps the companion near its owner.
	 */
	private static class FollowSummonerGoal extends WaterAvoidingRandomStrollGoal {
		private final SpectralCompanionEntity companion;
		private final double followSpeed;
		private final float maxDist;
		private final float minDist;

		public FollowSummonerGoal(SpectralCompanionEntity companion, double speed, float maxDist, float minDist) {
			super(companion, speed);
			this.companion = companion;
			this.followSpeed = speed;
			this.maxDist = maxDist;
			this.minDist = minDist;
		}

		@Override
		public boolean canUse() {
			LivingEntity owner = companion.summoner;
			if (owner == null || !owner.isAlive()) return false;
			double dist = companion.distanceToSqr(owner);
			return dist > (maxDist * maxDist);
		}

		@Override
		public boolean canContinueToUse() {
			LivingEntity owner = companion.summoner;
			if (owner == null || !owner.isAlive()) return false;
			double dist = companion.distanceToSqr(owner);
			return dist > (minDist * minDist);
		}

		@Override
		public void start() {
			// Navigate toward summoner
		}

		@Override
		public void tick() {
			LivingEntity owner = companion.summoner;
			if (owner != null) {
				companion.getNavigation().moveTo(owner, followSpeed);
				// Teleport if too far
				if (companion.distanceToSqr(owner) > 256) { // 16 blocks
					companion.teleportTo(
							owner.getX() + (companion.random.nextFloat() - 0.5) * 2,
							owner.getY(),
							owner.getZ() + (companion.random.nextFloat() - 0.5) * 2);
				}
			}
		}
	}
}
