package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.List;

public class BloodDrunkPuppeteerEntity extends Monster {

	private boolean spawnedDolls = false;

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 18.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.ATTACK_DAMAGE, 2.0D)
				.add(Attributes.ARMOR, 2.0D);
	}

	public BloodDrunkPuppeteerEntity(EntityType<? extends BloodDrunkPuppeteerEntity> type, Level worldIn) {
		super(type, worldIn);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
	}

	@Override
	protected void doPush(Entity entityIn) {
		super.doPush(entityIn);
	}

	@Override
	@SuppressWarnings("deprecation")
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty,
			MobSpawnType pReason, SpawnGroupData pSpawnData) {
		return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_BLOOD_DRUNK_PUPPETEER_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_BLOOD_DRUNK_PUPPETEER_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_BLOOD_DRUNK_PUPPETEER_HURT.get();
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
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("SpawnedDolls", spawnedDolls);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		spawnedDolls = tag.getBoolean("SpawnedDolls");
	}

	public static boolean canSpawnHere(EntityType<? extends Monster> pType, ServerLevelAccessor pLevel,
			MobSpawnType pSpawnType, BlockPos pPos, RandomSource pRandom) {
		return pLevel.getDifficulty() != Difficulty.PEACEFUL
				&& checkMobSpawnRules(pType, pLevel, pSpawnType, pPos, pRandom);
	}

	public List<EnthralledDollEntity> getPuppets() {
		return level().getEntitiesOfClass(EnthralledDollEntity.class,
				getBoundingBox().inflate(BloodDrunkPuppeteerTuning.DOLL_OWNER_SEARCH_RANGE),
				doll -> this.getUUID().equals(doll.getOwnerUUID()));
	}

	@Override
	public void tick() {
		super.tick();
		if (!level().isClientSide && !spawnedDolls) {
			spawnedDolls = true;
			summonInitialDolls();
		}
	}

	private void summonInitialDolls() {
		for (int i = 0; i < BloodDrunkPuppeteerTuning.DOLL_COUNT; i++) {
			EnthralledDollEntity doll = new EnthralledDollEntity(level(), this);
			double[] offset = BloodDrunkPuppeteerTuning.dollSpawnOffset(i);
			doll.setPos(getX() + offset[0], getY() + offset[1], getZ() + offset[2]);
			doll.setOwnerUUID(this.getUUID());
			doll.setSummonedByPuppeteer(true);
			doll.setTarget(getTarget());
			level().addFreshEntity(doll);
		}
	}
}
