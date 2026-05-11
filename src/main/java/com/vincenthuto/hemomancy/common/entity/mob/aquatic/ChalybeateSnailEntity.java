package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class ChalybeateSnailEntity extends WaterAnimal {
	private static final EntityDataAccessor<Boolean> RETRACTED = SynchedEntityData.defineId(ChalybeateSnailEntity.class,
			EntityDataSerializers.BOOLEAN);
	private static final TagKey<Block> CLIMBABLE_VENT_BLOCKS = BlockTags.create(
			Hemomancy.rloc("chalybeate_snail_climbable"));
	private static final int HARVEST_COOLDOWN_TICKS = 6000;

	public final AnimationState crawlAnimationState = new AnimationState();
	public final AnimationState retractAnimationState = new AnimationState();

	private int retractTicks;
	private int harvestCooldownTicks;

	public ChalybeateSnailEntity(EntityType<? extends ChalybeateSnailEntity> type, Level level) {
		super(type, level);
		this.refreshDimensions();
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 12.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.08D)
				.add(Attributes.ARMOR, 8.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.45D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new RetractWhenThreatenedGoal(this));
		this.goalSelector.addGoal(1, new SlowFloorCrawlGoal(this));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 5.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(RETRACTED, false);
	}

	public boolean isRetracted() {
		return ChalybeateSnailRetractionRules.shouldRenderRetracted(this.entityData.get(RETRACTED), this.retractTicks);
	}

	private void setRetracted(boolean retracted) {
		if (this.entityData.get(RETRACTED) != retracted) {
			this.entityData.set(RETRACTED, retracted);
			this.refreshDimensions();
		}
	}

	public int getHarvestCooldownTicks() {
		return this.harvestCooldownTicks;
	}

	private void retractFor(int ticks) {
		boolean wasRetracted = this.isRetracted();
		this.retractTicks = Math.max(this.retractTicks, ticks);
		if (!wasRetracted) {
			this.playSound(SoundEvents.TURTLE_EGG_CRACK, 0.5F, 0.65F);
		}
		this.setRetracted(true);
		this.getNavigation().stop();
		this.setDeltaMovement(Vec3.ZERO);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putBoolean("Retracted", this.isRetracted());
		compound.putInt("RetractTicks", this.retractTicks);
		compound.putInt("HarvestCooldownTicks", this.harvestCooldownTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		this.retractTicks = compound.getInt("RetractTicks");
		if (compound.getBoolean("Retracted") && this.retractTicks <= 0) {
			this.retractTicks = ChalybeateSnailRetractionRules.THREAT_HOLD_TICKS;
		}
		this.setRetracted(this.retractTicks > 0);
		this.harvestCooldownTicks = compound.getInt("HarvestCooldownTicks");
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(stack.getItem() instanceof ItemKnapper)) {
			return super.mobInteract(player, hand);
		}

		if (!this.level().isClientSide()) {
			if (!this.isRetracted()) {
				this.retractFor(ChalybeateSnailRetractionRules.INTERACTION_TUCK_TICKS);
				return InteractionResult.SUCCESS;
			}
			if (this.harvestCooldownTicks > 0) {
				this.level().playSound(null, this.blockPosition(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL,
						0.4F, 0.75F);
				return InteractionResult.SUCCESS;
			}
			this.harvestSclerites(player, hand, stack);
		}
		return InteractionResult.sidedSuccess(this.level().isClientSide());
	}

	private void harvestSclerites(Player player, InteractionHand hand, ItemStack tool) {
		int count = 1 + (this.random.nextFloat() < 0.18F ? 1 : 0);
		ItemStack drop = new ItemStack(ItemInit.chalybeate_sclerite.get(), count);
		if (!player.addItem(drop)) {
			player.drop(drop, false);
		}

		if (!player.isCreative()) {
			EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
			tool.hurtAndBreak(1, player, slot);
		}

		this.harvestCooldownTicks = HARVEST_COOLDOWN_TICKS;
		this.retractFor(ChalybeateSnailRetractionRules.HARVEST_TUCK_TICKS);
		this.level().playSound(null, this.blockPosition(), SoundEvents.AXE_SCRAPE, SoundSource.NEUTRAL,
				0.85F, 0.55F + this.random.nextFloat() * 0.2F);
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SCRAPE,
					this.getX(), this.getY() + 0.25D, this.getZ(),
					10, 0.35D, 0.15D, 0.35D, 0.02D);
			serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE,
					this.getX(), this.getY() + 0.2D, this.getZ(),
					5, 0.25D, 0.1D, 0.25D, 0.01D);
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!this.level().isClientSide()) {
			this.retractFor(ChalybeateSnailRetractionRules.HURT_TUCK_TICKS);
		}
		return super.hurt(source, this.isRetracted() ? amount * 0.35F : amount);
	}

	@Override
	public void playerTouch(Player player) {
		if (this.isRetracted() && !player.isCreative() && !player.isSpectator() && this.tickCount % 20 == 0) {
			player.hurt(this.damageSources().mobAttack(this), 1.0F);
		}
		super.playerTouch(player);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.crawlAnimationState.startIfStopped(this.tickCount);
			if (this.isRetracted()) {
				this.retractAnimationState.startIfStopped(this.tickCount);
			} else {
				this.retractAnimationState.stop();
			}
		}

		if (!this.level().isClientSide()) {
			if (this.isAlive() && this.isEffectiveAi()) {
				this.retractTicks = ChalybeateSnailRetractionRules.refreshForThreat(this.retractTicks,
						this.hasNearbyThreat());
			}
			if (this.harvestCooldownTicks > 0) {
				this.harvestCooldownTicks--;
			}
			if (this.retractTicks > 0) {
				this.retractTicks = ChalybeateSnailRetractionRules.tickDown(this.retractTicks);
			}
			this.setRetracted(this.retractTicks > 0);
		}
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (RETRACTED.equals(key)) {
			this.refreshDimensions();
		}
		super.onSyncedDataUpdated(key);
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.isEffectiveAi() && this.isInWater()) {
			float speed = this.isRetracted() ? 0.003F : 0.022F;
			this.moveRelative(speed, travelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			Vec3 delta = this.getDeltaMovement().multiply(0.72D, 0.45D, 0.72D).add(0.0D, -0.018D, 0.0D);
			if (this.horizontalCollision && this.canUseVentClimb()) {
				delta = new Vec3(delta.x, ChalybeateSnailMovementRules.climbAdjustedY(delta.y, true), delta.z);
			}
			this.setDeltaMovement(delta);
		} else {
			super.travel(travelVector);
		}
	}

	@Override
	public boolean onClimbable() {
		return this.canUseVentClimb();
	}

	@Override
	public float maxUpStep() {
		return this.canUseVentClimb() ? ChalybeateSnailMovementRules.VENT_STEP_HEIGHT : super.maxUpStep();
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@Override
	public int getMaxAirSupply() {
		return 4800;
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return this.isRetracted() ? super.getDefaultDimensions(pose).scale(0.82F, 0.72F) : super.getDefaultDimensions(pose);
	}

	private boolean hasNearbyThreat() {
		return !this.level().getEntitiesOfClass(LivingEntity.class,
				this.getBoundingBox().inflate(ChalybeateSnailRetractionRules.THREAT_DETECTION_RADIUS),
				entity -> entity != this && entity.isAlive() && isThreat(entity)).isEmpty();
	}

	private boolean canUseVentClimb() {
		return ChalybeateSnailMovementRules.canUseVentClimb(this.isInWater(), this.isRetracted(),
				this.isTouchingClimbableVentBlock());
	}

	private boolean isTouchingClimbableVentBlock() {
		BlockPos base = this.blockPosition();
		for (int y = 0; y <= 1; y++) {
			BlockPos at = base.above(y);
			for (Direction direction : Direction.Plane.HORIZONTAL) {
				if (this.level().getBlockState(at.relative(direction)).is(CLIMBABLE_VENT_BLOCKS)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isThreat(LivingEntity entity) {
		boolean creativePlayer = entity instanceof Player player && (player.isCreative() || player.isSpectator());
		return ChalybeateSnailRetractionRules.isThreat(creativePlayer,
				entity.getType().is(EntityTypeTags.AQUATIC),
				entity instanceof WaterAnimal);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_CHALYBEATE_SNAIL_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_CHALYBEATE_SNAIL_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundInit.ENTITY_CHALYBEATE_SNAIL_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.28F;
	}

	@Override
	public float getVoicePitch() {
		return 0.55F + this.random.nextFloat() * 0.15F;
	}

	private static class RetractWhenThreatenedGoal extends Goal {
		private final ChalybeateSnailEntity snail;

		private RetractWhenThreatenedGoal(ChalybeateSnailEntity snail) {
			this.snail = snail;
		}

		@Override
		public boolean canUse() {
			return !this.snail.isRetracted() && this.snail.hasNearbyThreat();
		}

		@Override
		public void start() {
			this.snail.retractFor(ChalybeateSnailRetractionRules.THREAT_HOLD_TICKS
					+ this.snail.random.nextInt(120));
		}
	}

	private static class SlowFloorCrawlGoal extends Goal {
		private final ChalybeateSnailEntity snail;
		private Vec3 motion = Vec3.ZERO;
		private int crawlTicks;

		private SlowFloorCrawlGoal(ChalybeateSnailEntity snail) {
			this.snail = snail;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return !this.snail.isRetracted() && this.snail.isInWater()
					&& this.snail.random.nextInt(ChalybeateSnailMovementRules.CRAWL_START_CHANCE) == 0;
		}

		@Override
		public boolean canContinueToUse() {
			return this.crawlTicks > 0 && !this.snail.isRetracted() && this.snail.isInWater();
		}

		@Override
		public void start() {
			float angle = this.snail.random.nextFloat() * Mth.TWO_PI;
			this.motion = new Vec3(Mth.cos(angle) * ChalybeateSnailMovementRules.CRAWL_HORIZONTAL_SPEED, 0.0D,
					Mth.sin(angle) * ChalybeateSnailMovementRules.CRAWL_HORIZONTAL_SPEED);
			this.crawlTicks = ChalybeateSnailMovementRules.CRAWL_BASE_TICKS
					+ this.snail.random.nextInt(ChalybeateSnailMovementRules.CRAWL_RANDOM_TICKS);
			this.snail.setYRot((float) (Mth.atan2(this.motion.z, this.motion.x) * Mth.RAD_TO_DEG) - 90.0F);
		}

		@Override
		public void tick() {
			this.snail.setDeltaMovement(this.snail.getDeltaMovement().add(this.motion.x,
					ChalybeateSnailMovementRules.CRAWL_SINK_SPEED, this.motion.z));
			this.crawlTicks--;
		}
	}
}
