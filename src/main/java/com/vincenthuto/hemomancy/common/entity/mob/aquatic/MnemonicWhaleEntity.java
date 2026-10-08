package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.worldgen.ErythrocoralReefTuning;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
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
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;

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
				.add(Attributes.ATTACK_DAMAGE, MnemonicWhaleTuning.BITE_DAMAGE)
				.add(Attributes.FOLLOW_RANGE, MnemonicWhaleTuning.HUNT_RANGE)
				.add(Attributes.MOVEMENT_SPEED, MnemonicWhaleTuning.MOVEMENT_SPEED)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
	}

	@Override
	protected PathNavigation createNavigation(Level level) {
		return new MnemonicWhaleNavigation(this, level);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new MnemonicWhaleHuntGoal(this));
		this.goalSelector.addGoal(1, new MnemonicWhaleCruiseGoal(this));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
	}

	public static boolean canSpawnHere(EntityType<? extends MnemonicWhaleEntity> type, LevelAccessor level,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		if (PelagicHabitat.layer(level, pos) != null) return PelagicHabitat.suitable(level, pos, Species.WHALE);
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
			boolean tooCloseToSurface = this.getTarget() == null && this.getY() > this.level().getSeaLevel()
					- ErythrocoralReefTuning.WHALE_SHALLOW_WATER_PUSH_DEPTH;
			boolean hasWaterBelow = this.level().getFluidState(below).is(FluidTags.WATER);
			double yMotion = MnemonicWhaleMovementRules.adjustVerticalMotion(current.y, tooCloseToSurface,
					hasWaterBelow);
			if (yMotion != current.y) {
				this.setDeltaMovement(current.x, yMotion, current.z);
			}
		}
	}

	static boolean isFeedingPrey(LivingEntity prey) {
		return (prey instanceof Squid || prey instanceof PrismCuttleEntity)
				&& prey.isAlive() && !prey.isRemoved() && prey.isInWater();
	}

	boolean canBite(LivingEntity prey) {
		return this.isAlive() && this.isInWater() && isFeedingPrey(prey)
				&& this.getBoundingBox().inflate(MnemonicWhaleTuning.BITE_REACH).intersects(prey.getBoundingBox())
				&& this.hasLineOfSight(prey);
	}

	@Override
	public boolean doHurtTarget(Entity target) {
		if (this.level().isClientSide() || !(target instanceof LivingEntity prey) || !this.canBite(prey)) {
			return false;
		}
		boolean hurt = super.doHurtTarget(prey);
		if (hurt && !prey.isAlive()) {
			this.spawnAtLocation(new ItemStack(ItemInit.mnemonic_ambergris.get()), this.getBbHeight() + 0.1F);
		}
		return hurt;
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

	private static final class MnemonicWhaleHuntGoal extends Goal {
		private final MnemonicWhaleEntity whale;
		private LivingEntity prey;
		private Path path;
		private int nextSearchTick;
		private int repathTicks;
		private int biteTicks;
		private int stalledTicks;
		private double closestDistance;
		private boolean unreachable;

		private MnemonicWhaleHuntGoal(MnemonicWhaleEntity whale) {
			this.whale = whale;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			if (!this.whale.isInWater() || this.whale.hasControllingPassenger()
					|| this.whale.tickCount < this.nextSearchTick) {
				return false;
			}
			this.nextSearchTick = this.whale.tickCount + MnemonicWhaleTuning.HUNT_SEARCH_INTERVAL_TICKS;
			var candidates = this.whale.level().getEntitiesOfClass(LivingEntity.class,
					this.whale.getBoundingBox().inflate(MnemonicWhaleTuning.HUNT_RANGE),
					candidate -> isFeedingPrey(candidate) && this.inRange(candidate));
			candidates.sort(Comparator.comparingDouble(this.whale::distanceToSqr));
			for (LivingEntity candidate : candidates) {
				Path candidatePath = this.pathTo(candidate);
				if (this.whale.canBite(candidate) || candidatePath != null && candidatePath.canReach()) {
					this.prey = candidate;
					this.path = candidatePath;
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean canContinueToUse() {
			return this.prey != null && isFeedingPrey(this.prey) && this.inRange(this.prey)
					&& this.whale.isInWater() && !this.whale.hasControllingPassenger()
					&& !this.unreachable;
		}

		@Override
		public void start() {
			this.whale.setTarget(this.prey);
			this.repathTicks = 0;
			this.biteTicks = 0;
			this.stalledTicks = 0;
			this.closestDistance = this.whale.distanceToSqr(this.prey);
			this.unreachable = false;
			if (this.path != null) {
				this.whale.getNavigation().moveTo(this.path, MnemonicWhaleTuning.HUNT_SPEED_MODIFIER);
			}
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			this.whale.getLookControl().setLookAt(this.prey, 12.0F, 12.0F);
			if (this.biteTicks > 0) {
				this.biteTicks--;
			}
			if (this.whale.canBite(this.prey)) {
				this.whale.getNavigation().stop();
				this.stalledTicks = 0;
				if (this.biteTicks == 0) {
					this.whale.doHurtTarget(this.prey);
					this.biteTicks = MnemonicWhaleTuning.BITE_INTERVAL_TICKS;
				}
				return;
			}
			if (--this.repathTicks <= 0) {
				this.repathTicks = MnemonicWhaleTuning.HUNT_REPATH_INTERVAL_TICKS;
				this.path = this.pathTo(this.prey);
				if (this.path == null || !this.path.canReach()) {
					this.unreachable = true;
					return;
				}
				this.whale.getNavigation().stop();
				this.whale.getNavigation().moveTo(this.path, MnemonicWhaleTuning.HUNT_SPEED_MODIFIER);
			}
			double distance = this.whale.distanceToSqr(this.prey);
			if (distance < this.closestDistance - 0.25D) {
				this.closestDistance = distance;
				this.stalledTicks = 0;
			} else if (++this.stalledTicks >= MnemonicWhaleTuning.HUNT_STALL_TIMEOUT_TICKS) {
				this.unreachable = true;
			}
		}

		@Override
		public void stop() {
			this.whale.getNavigation().stop();
			this.whale.setTarget(null);
			if (this.unreachable) {
				this.nextSearchTick = this.whale.tickCount + MnemonicWhaleTuning.HUNT_STALL_TIMEOUT_TICKS;
			}
			this.prey = null;
			this.path = null;
		}

		private boolean inRange(LivingEntity candidate) {
			return this.whale.distanceToSqr(candidate) <= MnemonicWhaleTuning.HUNT_RANGE * MnemonicWhaleTuning.HUNT_RANGE;
		}

		private Path pathTo(LivingEntity candidate) {
			// Path nodes anchor the whale's wide footprint at its corner, not its centre.
			double centreOffset = (int) (this.whale.getBbWidth() + 1.0F) * 0.5D;
			BlockPos approach = BlockPos.containing(candidate.getX() - centreOffset + 0.5D,
					candidate.getY(), candidate.getZ() - centreOffset + 0.5D);
			var approaches = new HashSet<BlockPos>();
			for (BlockPos node : BlockPos.betweenClosed(approach.offset(-2, -2, -2), approach.offset(2, 1, 2))) {
				Vec3 centre = new Vec3(node.getX() + centreOffset, node.getY(), node.getZ() + centreOffset);
				AABB biteBox = this.whale.getBoundingBox().move(centre.subtract(this.whale.position()))
						.inflate(MnemonicWhaleTuning.BITE_REACH - 0.3D);
				if (biteBox.intersects(candidate.getBoundingBox()) && this.whale.level().clip(new ClipContext(
						centre.add(0, this.whale.getEyeHeight(), 0), candidate.getEyePosition(),
						ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.whale)).getType() == HitResult.Type.MISS) {
					approaches.add(node.immutable());
				}
			}
			return approaches.isEmpty() ? null : this.whale.getNavigation().createPath(approaches, 0);
		}
	}

	private static final class MnemonicWhaleNavigation extends WaterBoundPathNavigation {
		private MnemonicWhaleNavigation(MnemonicWhaleEntity whale, Level level) {
			super(whale, level);
		}

		@Override
		protected void followThePath() {
			if (this.mob.getTarget() == null) {
				super.followThePath();
				return;
			}
			// The normal 2.1-block waypoint tolerance stops this wide mob outside biting range.
			Vec3 position = this.getTempMobPos();
			Vec3 next = this.path.getNextEntityPos(this.mob);
			if (Math.abs(position.x - next.x) < 0.25D && Math.abs(position.z - next.z) < 0.25D
					&& Math.abs(position.y - next.y) < 1.0D) {
				this.path.advance();
			}
			this.doStuckDetection(position);
		}
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
				if (!PelagicHabitat.loaded(level, new BlockPos(x, seaLevel - 1, z))) continue;
				int floorY = this.findOceanFloorY(level, x, z, seaLevel);
				int y = MnemonicWhaleMovementRules.cruiseTargetY(seaLevel, floorY,
						this.whale.getRandom().nextInt(MnemonicWhaleTuning.CRUISE_RANDOM_DIVE_DEPTH + 1));
				var layer = PelagicHabitat.layer(level, this.whale.blockPosition());
				if (layer != null && layer != com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.REEF)
					y = Math.max(floorY + MnemonicWhaleTuning.CRUISE_MIN_FLOOR_CLEARANCE, 34 + this.whale.getRandom().nextInt(21));
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
			return PelagicHabitat.waterClearance(level, pos, 2, 2, 2);
		}
	}
}
