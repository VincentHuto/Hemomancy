package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;

public class ScarletSerpentEntity extends PathfinderMob {
	private static final EntityDataAccessor<Boolean> HOOD_FLARED = SynchedEntityData.defineId(
			ScarletSerpentEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> STRIKE_TICKS = SynchedEntityData.defineId(
			ScarletSerpentEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(
			ScarletSerpentEntity.class, EntityDataSerializers.INT);

	public ScarletSerpentEntity(EntityType<? extends ScarletSerpentEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 12.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.30D)
				.add(Attributes.ATTACK_DAMAGE, 3.0D)
				.add(Attributes.FOLLOW_RANGE, 16.0D);
	}

	public static boolean canSpawnHere(EntityType<? extends ScarletSerpentEntity> type, ServerLevelAccessor level,
			MobSpawnType spawnType, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		return level.getDifficulty() != Difficulty.PEACEFUL
				&& level.getBlockState(below).isSolidRender(level, below)
				&& level.getRawBrightness(pos, 0) > 8;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(HOOD_FLARED, false);
		builder.define(STRIKE_TICKS, 0);
		builder.define(VARIANT, ScarletSerpentVariant.DEFAULT.id());
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, true));
		this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.8D));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
	}

	public boolean isHoodFlared() {
		return this.entityData.get(HOOD_FLARED);
	}

	public int getStrikeTicks() {
		return this.entityData.get(STRIKE_TICKS);
	}

	public float getStrikeProgress(float partialTick) {
		int ticks = this.getStrikeTicks();
		if (ticks <= 0) {
			return 0.0F;
		}
		return Math.min(1.0F, (ticks - partialTick) / (float) ScarletSerpentRules.STRIKE_ANIMATION_TICKS);
	}

	public ScarletSerpentVariant getVariant() {
		return ScarletSerpentVariant.byId(this.entityData.get(VARIANT));
	}

	public void setVariant(ScarletSerpentVariant variant) {
		this.entityData.set(VARIANT, variant.id());
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			MobSpawnType reason, SpawnGroupData spawnData) {
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
		this.setVariant(variantForCurrentBiome());
		return data;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putInt("ScarletSerpentVariant", this.getVariant().id());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.entityData.set(VARIANT, compound.getInt("ScarletSerpentVariant"));
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		boolean hurt = super.doHurtTarget(target);
		this.entityData.set(STRIKE_TICKS, ScarletSerpentRules.STRIKE_ANIMATION_TICKS);
		if (hurt && target instanceof LivingEntity living && this.level().getDifficulty() != Difficulty.PEACEFUL) {
			living.addEffect(new MobEffectInstance(MobEffects.POISON, ScarletSerpentRules.POISON_DURATION_TICKS, 0));
			living.addEffect(new MobEffectInstance(EffectInit.blood_binding,
					ScarletSerpentRules.BLOOD_BINDING_DURATION_TICKS, 0));
		}
		return hurt;
	}

	@Override
	protected float getSoundVolume() {
		return 0.25F;
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	public void tick() {
		super.tick();
		int strikeTicks = this.getStrikeTicks();
		if (strikeTicks > 0) {
			this.entityData.set(STRIKE_TICKS, strikeTicks - 1);
		}

		if (!this.level().isClientSide()) {
			updateTerritorialTargeting();
		}
	}

	private void updateTerritorialTargeting() {
		if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
			this.setTarget(null);
			this.entityData.set(HOOD_FLARED, false);
			return;
		}

		Player warningPlayer = findNearestVulnerablePlayer(ScarletSerpentRules.HOOD_FLARE_RANGE_BLOCKS);
		boolean shouldFlare = warningPlayer != null && ScarletSerpentRules.shouldFlareHood(warningPlayer.isAlive(),
				warningPlayer.isCreative(), warningPlayer.isSpectator(), this.distanceTo(warningPlayer));
		this.entityData.set(HOOD_FLARED, shouldFlare);

		Player strikePlayer = findNearestVulnerablePlayer(ScarletSerpentRules.STRIKE_RANGE_BLOCKS);
		if (strikePlayer != null && ScarletSerpentRules.shouldStrike(strikePlayer.isAlive(), strikePlayer.isCreative(),
				strikePlayer.isSpectator(), this.distanceTo(strikePlayer))) {
			this.setTarget(strikePlayer);
		} else if (this.getTarget() instanceof Player) {
			this.setTarget(null);
		}
	}

	private Player findNearestVulnerablePlayer(double range) {
		AABB bounds = this.getBoundingBox().inflate(range);
		return this.level().getEntitiesOfClass(Player.class, bounds, player -> player.isAlive()
						&& !player.isCreative()
						&& !player.isSpectator())
				.stream()
				.min(Comparator.comparingDouble(this::distanceToSqr))
				.orElse(null);
	}

	private ScarletSerpentVariant variantForCurrentBiome() {
		String biomePath = this.level().getBiome(this.blockPosition())
				.unwrapKey()
				.map(key -> key.location().getPath())
				.orElse("");
		return ScarletSerpentVariant.fromBiomePath(biomePath);
	}

	@Override
	protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
		super.actuallyHurt(damageSource, damageAmount);
		if (!this.level().isClientSide() && this.level().getDifficulty() != Difficulty.PEACEFUL) {
			this.entityData.set(HOOD_FLARED, true);
		}
	}
}
