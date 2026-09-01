package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.EnumSet;

public class PrismCuttleEntity extends WaterAnimal {
	private static final EntityDataAccessor<Integer> VARIANT =
			SynchedEntityData.defineId(PrismCuttleEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> FLASH_TICKS =
			SynchedEntityData.defineId(PrismCuttleEntity.class, EntityDataSerializers.INT);

	private int flashCooldownTicks;
	private Vec3 fleeImpulse = Vec3.ZERO;

	public PrismCuttleEntity(EntityType<? extends PrismCuttleEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
		this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.08F, 0.02F, true);
		this.lookControl = new SmoothSwimmingLookControl(this, 10);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.18D)
				.add(Attributes.FOLLOW_RANGE, 12.0D);
	}

	public static boolean canSpawnHere(EntityType<? extends PrismCuttleEntity> type, LevelAccessor level,
			MobSpawnType spawnType, BlockPos pos, RandomSource random) {
		boolean waterHere = level.getFluidState(pos).is(FluidTags.WATER);
		boolean openWater = level.getFluidState(pos.above()).is(FluidTags.WATER)
				&& level.getFluidState(pos.below()).is(FluidTags.WATER);
		return PrismCuttleRules.canNaturalSpawn(waterHere, openWater, level.getSeaLevel(), pos.getY());
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(VARIANT, PrismCuttleVariant.DEEP.id());
		builder.define(FLASH_TICKS, 0);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new DriftGoal(this));
		this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
	}

	public PrismCuttleVariant getVariant() {
		return PrismCuttleVariant.byId(this.entityData.get(VARIANT));
	}

	public boolean isFlashing() {
		return this.entityData.get(FLASH_TICKS) > 0;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("PrismCuttleVariant", this.entityData.get(VARIANT));
		tag.putInt("FlashCooldown", this.flashCooldownTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.entityData.set(VARIANT, tag.getInt("PrismCuttleVariant"));
		this.flashCooldownTicks = tag.getInt("FlashCooldown");
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.isAlive() && this.isInWater()) {
			double driftTime = (this.tickCount + this.getId()) * 0.055D;
			double depthCorrection = PrismCuttleRules.verticalDepthCorrection(this.level().getSeaLevel(), this.getY());
			Vec3 drift = new Vec3(Math.cos(driftTime) * 0.006D, depthCorrection,
					Math.sin(driftTime) * 0.006D);
			this.setDeltaMovement(this.getDeltaMovement().scale(0.88D).add(drift).add(this.fleeImpulse));
			this.fleeImpulse = this.fleeImpulse.scale(0.82D);
			this.move(MoverType.SELF, this.getDeltaMovement());
		} else {
			super.travel(travelVector);
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(this.isInWater());
		int flashTicks = this.entityData.get(FLASH_TICKS);
		if (flashTicks > 0) {
			this.entityData.set(FLASH_TICKS, flashTicks - 1);
		}
		if (this.level().isClientSide()) {
			return;
		}
		if (this.flashCooldownTicks > 0) {
			this.flashCooldownTicks--;
		}
		if (this.tickCount % PrismCuttleRules.CAMOUFLAGE_SAMPLE_INTERVAL_TICKS == 0 && !this.isFlashing()) {
			this.sampleCamouflage();
		}
		Player player = this.findFlashTarget();
		if (player != null) {
			this.flashAt(player);
		}
	}

	private Player findFlashTarget() {
		AABB bounds = this.getBoundingBox().inflate(PrismCuttleRules.FLASH_RANGE_BLOCKS);
		return this.level().getEntitiesOfClass(Player.class, bounds,
						player -> PrismCuttleRules.shouldFlash(player.isAlive(), player.isCreative(), player.isSpectator(),
								this.flashCooldownTicks, this.distanceTo(player)))
				.stream()
				.min(Comparator.comparingDouble(this::distanceToSqr))
				.orElse(null);
	}

	private void flashAt(Player player) {
		this.entityData.set(FLASH_TICKS, PrismCuttleRules.FLASH_TICKS);
		this.flashCooldownTicks = PrismCuttleRules.FLASH_COOLDOWN_TICKS;
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, PrismCuttleRules.BLINDNESS_DURATION_TICKS, 0));
		player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, PrismCuttleRules.NAUSEA_DURATION_TICKS, 0));
		Vec3 away = this.position().subtract(player.position()).normalize().scale(0.18D);
		double depthCorrection = PrismCuttleRules.verticalDepthCorrection(this.level().getSeaLevel(), this.getY());
		this.fleeImpulse = new Vec3(away.x, Math.min(0.0D, depthCorrection), away.z);
		this.level().playSound(null, this.blockPosition(), SoundEvents.GLOW_SQUID_SQUIRT, SoundSource.NEUTRAL,
				0.6F, 1.35F);
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.95F, 0.1F, 0.9F), 1.2F),
					this.getX(), this.getY() + 0.25D, this.getZ(), 18, 0.4D, 0.25D, 0.4D, 0.02D);
			serverLevel.sendParticles(new DustParticleOptions(new Vector3f(0.1F, 0.9F, 1.0F), 1.2F),
					this.getX(), this.getY() + 0.25D, this.getZ(), 18, 0.4D, 0.25D, 0.4D, 0.02D);
		}
	}

	private void sampleCamouflage() {
		int red = 0;
		int green = 0;
		int blue = 0;
		int samples = 0;
		BlockPos base = this.blockPosition();
		for (BlockPos pos : BlockPos.betweenClosed(base.offset(-2, -2, -2), base.offset(2, 1, 2))) {
			BlockState state = this.level().getBlockState(pos);
			if (!state.isAir() && !state.getFluidState().is(FluidTags.WATER)) {
				int color = state.getMapColor(this.level(), pos).col;
				red += (color >> 16) & 0xFF;
				green += (color >> 8) & 0xFF;
				blue += color & 0xFF;
				samples++;
			}
		}
		if (samples > 0) {
			int color = (Mth.clamp(red / samples, 0, 255) << 16)
					| (Mth.clamp(green / samples, 0, 255) << 8)
					| Mth.clamp(blue / samples, 0, 255);
			this.entityData.set(VARIANT, PrismCuttleVariant.fromBlockColor(color).id());
		}
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected float getSoundVolume() {
		return 0.25F;
	}

	private static final class DriftGoal extends Goal {
		private final PrismCuttleEntity cuttle;

		private DriftGoal(PrismCuttleEntity cuttle) {
			this.cuttle = cuttle;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return true;
		}
	}
}
