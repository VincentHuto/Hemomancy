package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.worldgen.ErythrocoralReefTuning;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class MnemonicWhaleEntity extends WaterAnimal {
	private static final int SAMPLE_COOLDOWN_TICKS = 6000;
	private static final int AMBIENT_SHEDDING_CHECK_INTERVAL = 4800;

	private int sampleCooldownTicks;

	public MnemonicWhaleEntity(EntityType<? extends MnemonicWhaleEntity> type, Level level) {
		super(type, level);
		this.moveControl = new SmoothSwimmingMoveControl(this, MnemonicWhaleTuning.SMOOTH_SWIM_MAX_TURN_X,
				MnemonicWhaleTuning.SMOOTH_SWIM_MAX_TURN_Y,
				MnemonicWhaleTuning.SMOOTH_SWIM_IN_WATER_SPEED_MODIFIER,
				MnemonicWhaleTuning.SMOOTH_SWIM_OUT_OF_WATER_SPEED_MODIFIER, true);
		this.lookControl = new SmoothSwimmingLookControl(this, MnemonicWhaleTuning.SMOOTH_SWIM_MAX_TURN_Y);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 38.0D)
				.add(Attributes.MOVEMENT_SPEED, MnemonicWhaleTuning.MOVEMENT_SPEED)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new WaterBoundPathNavigation(this, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new MnemonicWhaleCruiseGoal(this));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	public static boolean canSpawnHere(EntityType<? extends MnemonicWhaleEntity> type, LevelAccessor level,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		return level.getFluidState(pos).is(FluidTags.WATER)
				&& level.getFluidState(pos.above()).is(FluidTags.WATER)
				&& level.getFluidState(pos.below()).is(FluidTags.WATER)
				&& pos.getY() <= level.getSeaLevel() - ErythrocoralReefTuning.WHALE_MIN_DEPTH_BELOW_SEA_LEVEL;
	}

	@Override
	public void tick() {
		super.tick();
		if (!this.level().isClientSide()) {
			if (this.sampleCooldownTicks > 0) {
				this.sampleCooldownTicks--;
			} else if (this.isInWater() && this.random.nextInt(AMBIENT_SHEDDING_CHECK_INTERVAL) == 0) {
				this.spawnAtLocation(ItemInit.mnemonic_ambergris.get());
				this.sampleCooldownTicks = SAMPLE_COOLDOWN_TICKS;
			}
		}

		if (this.isInWater()) {
			BlockPos below = this.blockPosition().below();
			Vec3 current = this.getDeltaMovement();
			boolean tooCloseToSurface = this.getY() > this.level().getSeaLevel()
					- ErythrocoralReefTuning.WHALE_SHALLOW_WATER_PUSH_DEPTH;
			boolean hasWaterBelow = this.level().getFluidState(below).is(FluidTags.WATER);
			double yMotion = MnemonicWhaleMovementRules.adjustVerticalMotion(current.y, tooCloseToSurface,
					hasWaterBelow);
			if (yMotion != current.y) {
				this.setDeltaMovement(current.x, yMotion, current.z);
			}
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (!stack.is(Items.GLASS_BOTTLE)) {
			return super.mobInteract(player, hand);
		}
		if (this.sampleCooldownTicks > 0) {
			this.level().playSound(null, this.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.NEUTRAL, 0.45F, 0.8F);
			return InteractionResult.sidedSuccess(this.level().isClientSide());
		}

		if (!this.level().isClientSide()) {
			if (!player.getAbilities().instabuild) {
				stack.shrink(1);
			}
			ItemStack sample = new ItemStack(ItemInit.mnemonic_ambergris.get());
			if (!player.addItem(sample)) {
				player.drop(sample, false);
			}
			this.level().playSound(null, this.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 0.7F, 0.9F);
			this.sampleCooldownTicks = SAMPLE_COOLDOWN_TICKS;
		}
		return InteractionResult.sidedSuccess(this.level().isClientSide());
	}

	@Override
	public int getMaxAirSupply() {
		return 6000;
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_MNEMONIC_WHALE_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_MNEMONIC_WHALE_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundInit.ENTITY_MNEMONIC_WHALE_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.45F;
	}

	@Override
	public float getVoicePitch() {
		return 0.65F + this.random.nextFloat() * 0.1F;
	}

	private static final class MnemonicWhaleCruiseGoal extends Goal {
		private final MnemonicWhaleEntity whale;
		private Vec3 target;
		private int cruiseTicks;

		private MnemonicWhaleCruiseGoal(MnemonicWhaleEntity whale) {
			this.whale = whale;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (!this.whale.isInWater() || this.whale.hasControllingPassenger()) {
				return false;
			}
			if (!this.whale.getNavigation().isDone() && this.whale.getRandom().nextInt(4) != 0) {
				return false;
			}
			if (this.whale.getRandom().nextInt(reducedTickDelay(MnemonicWhaleTuning.CRUISE_INTERVAL_TICKS)) != 0) {
				return false;
			}
			this.target = this.findCruiseTarget();
			return this.target != null;
		}

		@Override
		public boolean canContinueToUse() {
			return this.whale.isInWater() && !this.whale.getNavigation().isDone()
					&& this.cruiseTicks < MnemonicWhaleTuning.CRUISE_MAX_TICKS;
		}

		@Override
		public void start() {
			this.cruiseTicks = 0;
			if (this.target != null) {
				this.whale.getNavigation().moveTo(this.target.x, this.target.y, this.target.z,
						MnemonicWhaleTuning.CRUISE_SPEED_MODIFIER);
			}
		}

		@Override
		public void tick() {
			this.cruiseTicks++;
			if (this.target == null) {
				return;
			}
			this.whale.getLookControl().setLookAt(this.target.x, this.target.y, this.target.z, 4.0F, 4.0F);
			if (this.whale.distanceToSqr(this.target) < 9.0D) {
				this.whale.getNavigation().stop();
			}
		}

		@Override
		public void stop() {
			this.whale.getNavigation().stop();
			this.target = null;
		}

		private Vec3 findCruiseTarget() {
			Level level = this.whale.level();
			int seaLevel = level.getSeaLevel();
			for (int attempt = 0; attempt < MnemonicWhaleTuning.CRUISE_TARGET_ATTEMPTS; attempt++) {
				double angle = this.whale.getRandom().nextDouble() * Math.PI * 2.0D;
				int distance = MnemonicWhaleTuning.CRUISE_MIN_HORIZONTAL_DISTANCE
						+ this.whale.getRandom().nextInt(MnemonicWhaleTuning.CRUISE_HORIZONTAL_VARIANCE);
				int x = this.whale.blockPosition().getX() + (int) Math.round(Math.cos(angle) * distance);
				int z = this.whale.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * distance);
				int floorY = this.findOceanFloorY(level, x, z, seaLevel);
				int y = MnemonicWhaleMovementRules.cruiseTargetY(seaLevel, floorY,
						this.whale.getRandom().nextInt(MnemonicWhaleTuning.CRUISE_RANDOM_DIVE_DEPTH + 1));
				BlockPos targetPos = new BlockPos(x, y, z);
				if (this.isOpenWater(level, targetPos)) {
					return Vec3.atCenterOf(targetPos);
				}
			}
			return null;
		}

		private int findOceanFloorY(Level level, int x, int z, int seaLevel) {
			BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
			int startY = Math.min(seaLevel, level.getMaxBuildHeight() - 2);
			for (int y = startY; y > level.getMinBuildHeight() + 1; y--) {
				mutable.set(x, y, z);
				if (level.getFluidState(mutable).is(FluidTags.WATER)
						&& !level.getFluidState(mutable.below()).is(FluidTags.WATER)) {
					return y - 1;
				}
			}
			return level.getMinBuildHeight();
		}

		private boolean isOpenWater(Level level, BlockPos pos) {
			return level.getFluidState(pos).is(FluidTags.WATER)
					&& level.getFluidState(pos.above()).is(FluidTags.WATER)
					&& level.getFluidState(pos.below()).is(FluidTags.WATER);
		}
	}
}
