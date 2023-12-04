package com.vincenthuto.hemomancy.common.entity.mob;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

public class BloodDrunkPuppeteerEntity extends Monster {

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 7.0D).add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	public BloodDrunkPuppeteerEntity(EntityType<? extends BloodDrunkPuppeteerEntity> type, Level worldIn) {
		super(type, worldIn);

	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();

	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
			MobSpawnType pReason, SpawnGroupData pSpawnData, CompoundTag pDataTag) {
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.WOLF_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.WOLF_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.WOLF_HURT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	public void playerTouch(Player entityIn) {
		super.playerTouch(entityIn);
		// entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), 1.5f);

	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0D, true));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));

		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));

	}

	@Override
	public void onAddedToWorld() {
		super.onAddedToWorld();
		EnthralledDollEntity[] needles = new EnthralledDollEntity[4];
		for (int i = 0; i < needles.length; i++) {
			needles[i] = new EnthralledDollEntity(level(), this);
			needles[i].setPos(position().add(1, 1, 1));
			needles[i].setOwnerUUID(this.getUUID());
			level().addFreshEntity(needles[i]);
		}
	}

	public List<EnthralledDollEntity> getPuppeteer() {
		List<EnthralledDollEntity> owners = level().getNearbyEntities(EnthralledDollEntity.class,
				TargetingConditions.DEFAULT, this, getBoundingBox().inflate(12.0D, 6.0D, 12.0D));
		List<EnthralledDollEntity> dolls = owners.stream()
				.filter(o -> o.getPuppeteer().getUUID().equals(this.getUUID())).toList();
		if (!dolls.isEmpty()) {
			return dolls;
		}
		return null;
	}

	@Override
	public void tick() {
		super.tick();

//		if (level().dayTime() % 10 == 0) {
//
//			EnthralledDollEntity[] needles = new EnthralledDollEntity[1];
//			for (int i = 0; i < needles.length; i++) {
//				needles[i] = new EnthralledDollEntity(level(), this);
//				needles[i].setPos(position());
//				needles[i].setOwnerUUID(this.getUUID());
//				level().addFreshEntity(needles[i]);
//			}
//		}

	}
}