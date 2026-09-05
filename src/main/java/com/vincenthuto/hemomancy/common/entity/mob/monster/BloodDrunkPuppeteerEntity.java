package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import java.util.List;

public class BloodDrunkPuppeteerEntity extends Monster {
	private static final EntityDataAccessor<Integer> CAROUSEL_HORSE = SynchedEntityData.defineId(
			BloodDrunkPuppeteerEntity.class, EntityDataSerializers.INT);

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
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(CAROUSEL_HORSE, -1);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
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
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class,
				BloodDrunkPuppeteerTuning.PUPPETEER_AVOID_PLAYER_DISTANCE, 1.0D, 1.2D));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("SpawnedDolls", spawnedDolls);
		if (getCarouselHorse() >= 0) tag.putInt("CarouselHorse", getCarouselHorse());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		spawnedDolls = tag.getBoolean("SpawnedDolls");
		if (tag.contains("CarouselHorse")) bindToCarousel(tag.getInt("CarouselHorse"));
	}

	public void bindToCarousel(int horse) {
		entityData.set(CAROUSEL_HORSE, Math.floorMod(horse, 3));
		spawnedDolls = true;
		setNoAi(true);
		setSilent(true);
		setInvulnerable(true);
		setPersistenceRequired();
	}

	public int getCarouselHorse() {
		return entityData.get(CAROUSEL_HORSE);
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (hand == InteractionHand.MAIN_HAND && getVehicle() instanceof CircusCarouselEntity carousel) {
			if (!level().isClientSide && !carousel.severCaptive(player, getCarouselHorse())) return InteractionResult.PASS;
			return InteractionResult.sidedSuccess(level().isClientSide);
		}
		return super.mobInteract(player, hand);
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return getCarouselHorse() < 0;
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
