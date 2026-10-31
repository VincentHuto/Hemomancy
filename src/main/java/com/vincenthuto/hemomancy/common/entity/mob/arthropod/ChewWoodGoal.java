package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * AI goal that makes a mob seek out nearby log/wood blocks,
 * navigate to them, gnaw on them with breaking particles and sounds,
 * then destroy the block and drop coarse dirt — like a termite chewing wood.
 * <p>
 * A tree is eaten from the top down so no crown is left floating: the goal
 * finds a log it can reach from the ground, then aims for the highest log
 * joined to it. The Chthonians are spiders, so wall-climber navigation
 * carries them up the trunk; leaves that block the climb are chewed
 * through. After each log the next highest one in the same tree follows
 * without leaving the goal. A log the mob cannot reach in time is skipped
 * for a while so it does not stare at the same trunk again.
 */
public class ChewWoodGoal extends MoveToBlockGoal {
	private final PathfinderMob mob;
	private final Map<BlockPos, Long> givenUp = new HashMap<>();
	private int chewTimer;
	private int leafTimer;
	private int stuckTimer;
	private double bestDistance;
	private static final int CHEW_DURATION = 60;    // ~3 seconds at 20 tps
	private static final int LEAF_CHEW_DURATION = 10;
	private static final int GIVE_UP_TICKS = 100;   // ~5 seconds without getting closer
	private static final int GIVE_UP_MEMORY = 1200; // skip an abandoned log for a minute
	private static final int MAX_TREE_LOGS = 256;
	private static final int TREE_RADIUS = 12;
	private static final int TREE_HEIGHT = 48;

	public ChewWoodGoal(PathfinderMob mob, double speed, int searchRange) {
		super(mob, speed, searchRange);
		this.mob = mob;
	}

	private static boolean isWood(BlockState state) {
		return state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS);
	}

	private boolean isGivenUp(BlockPos pos) {
		Long until = this.givenUp.get(pos);
		return until != null && until > this.mob.level().getGameTime();
	}

	/** Search filter: wood the mob can walk up to from the ground. */
	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		if (!isWood(level.getBlockState(pos)) || isGivenUp(pos)) {
			return false;
		}
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			BlockPos side = pos.relative(dir);
			if (canStand(level, side) || canStand(level, side.below())) {
				return true;
			}
		}
		return canStand(level, pos.above());
	}

	private static boolean canStand(LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
				&& level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	/**
	 * The highest log joined to {@code seed} (nearest first on ties), or
	 * {@code seed} itself for planks or when every log is skipped.
	 */
	private BlockPos treeTop(BlockPos seed) {
		Level level = this.mob.level();
		if (!level.getBlockState(seed).is(BlockTags.LOGS)) {
			return seed;
		}
		Set<BlockPos> seen = new HashSet<>();
		ArrayDeque<BlockPos> open = new ArrayDeque<>();
		seen.add(seed);
		open.add(seed);
		BlockPos best = null;
		Vec3 from = this.mob.position();
		while (!open.isEmpty() && seen.size() < MAX_TREE_LOGS) {
			BlockPos pos = open.poll();
			if (!isGivenUp(pos) && (best == null || pos.getY() > best.getY()
					|| pos.getY() == best.getY() && pos.distToCenterSqr(from) < best.distToCenterSqr(from))) {
				best = pos;
			}
			for (BlockPos next : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
				if (Math.abs(next.getX() - seed.getX()) > TREE_RADIUS
						|| Math.abs(next.getZ() - seed.getZ()) > TREE_RADIUS
						|| next.getY() < seed.getY() - 2 || next.getY() > seed.getY() + TREE_HEIGHT) {
					continue;
				}
				if (!seen.contains(next) && level.getBlockState(next).is(BlockTags.LOGS)) {
					BlockPos found = next.immutable();
					seen.add(found);
					open.add(found);
				}
			}
		}
		return best != null ? best : seed;
	}

	@Override
	protected boolean isReachedTarget() {
		return this.mob.getBoundingBox().inflate(0.6, 0.75, 0.6).intersects(new AABB(this.blockPos));
	}

	@Override
	public double acceptedDistance() {
		return 1.0;
	}

	@Override
	protected void moveMobToBlock() {
		// Aim into the wood itself: wall-climber navigation walks as close as
		// it can, then presses against the trunk and climbs it.
		this.mob.getNavigation().moveTo(this.blockPos.getX() + 0.5, this.blockPos.getY(),
				this.blockPos.getZ() + 0.5, this.speedModifier);
	}

	private void retarget(BlockPos pos) {
		this.blockPos = pos;
		this.chewTimer = 0;
		this.leafTimer = 0;
		this.stuckTimer = 0;
		this.tryTicks = 0;
		this.bestDistance = Double.MAX_VALUE;
	}

	@Override
	public void start() {
		retarget(treeTop(this.blockPos));
		moveMobToBlock();
	}

	@Override
	public void stop() {
		if (this.stuckTimer >= GIVE_UP_TICKS) {
			long now = this.mob.level().getGameTime();
			this.givenUp.values().removeIf(until -> until <= now);
			this.givenUp.put(this.blockPos.immutable(), now + GIVE_UP_MEMORY);
			this.nextStartTick = reducedTickDelay(40);
		}
		super.stop();
		this.chewTimer = 0;
		this.leafTimer = 0;
		this.stuckTimer = 0;
	}

	@Override
	public boolean canContinueToUse() {
		return this.stuckTimer < GIVE_UP_TICKS && this.tryTicks <= 1200
				&& isWood(this.mob.level().getBlockState(this.blockPos));
	}

	@Override
	public void tick() {
		Level level = this.mob.level();
		BlockPos target = this.blockPos;
		this.tryTicks++;

		this.mob.getLookControl().setLookAt(
				target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);

		if (this.isReachedTarget()) {
			this.stuckTimer = 0;
			this.mob.getNavigation().stop();
			if (!this.mob.onGround()) {
				// Cling where we are; goals run before travel, so a zeroed
				// motion means this tick's move is zero.
				this.mob.setDeltaMovement(Vec3.ZERO);
				this.mob.resetFallDistance();
			}
			chew(level, target);
			return;
		}

		this.chewTimer = 0;
		double distance = this.mob.position().distanceTo(Vec3.atCenterOf(target));
		if (distance < this.bestDistance - 0.05) {
			this.bestDistance = distance;
			this.stuckTimer = 0;
		} else {
			this.stuckTimer++;
		}

		if (target.getY() + 1 < this.mob.getY()) {
			// Wood is below us: let go and drop rather than climb on.
			this.mob.getNavigation().stop();
			return;
		}
		if (!chewBlockingLeaves(level) && this.tryTicks % 20 == 0) {
			moveMobToBlock();
		}
	}

	/** Leaves over the mob's head stop the climb; chew a way through them. */
	private boolean chewBlockingLeaves(Level level) {
		if (this.blockPos.getY() <= this.mob.getY() + 1 || !this.mob.horizontalCollision) {
			this.leafTimer = 0;
			return false;
		}
		BlockPos above = BlockPos.containing(this.mob.getX(), this.mob.getBoundingBox().maxY + 0.1, this.mob.getZ());
		BlockState state = level.getBlockState(above);
		if (!state.is(BlockTags.LEAVES)) {
			this.leafTimer = 0;
			return false;
		}
		this.mob.setDeltaMovement(Vec3.ZERO);
		this.stuckTimer = 0;
		if (++this.leafTimer >= LEAF_CHEW_DURATION) {
			level.destroyBlock(above, false);
			this.leafTimer = 0;
		}
		return true;
	}

	private void chew(Level level, BlockPos target) {
		BlockState state = level.getBlockState(target);
		if (!isWood(state)) {
			return; // Block was already broken by something else
		}

		this.chewTimer++;

		// Chewing sounds every few ticks
		if (this.chewTimer % 8 == 0) {
			level.playSound(null, target,
					SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.HOSTILE,
					0.5f, 1.5f + level.random.nextFloat() * 0.5f);
		}

		// Breaking particles every few ticks (server-side)
		if (level instanceof ServerLevel serverLevel && this.chewTimer % 4 == 0) {
			serverLevel.sendParticles(
					new BlockParticleOption(ParticleTypes.BLOCK, state),
					target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
					6, 0.3, 0.3, 0.3, 0.05);
		}

		if (this.chewTimer < CHEW_DURATION) {
			return;
		}

		// Destroy the block and drop coarse dirt
		boolean wasLog = state.is(BlockTags.LOGS);
		level.destroyBlock(target, false);

		ItemEntity drop = new ItemEntity(level,
				target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
				new ItemStack(Blocks.COARSE_DIRT.asItem(), 1));
		drop.setDefaultPickUpDelay();
		level.addFreshEntity(drop);

		level.playSound(null, target, SoundEvents.WOOD_BREAK, SoundSource.HOSTILE, 1.0f, 0.8f);

		// Carry on down the same tree; rest once it is gone
		BlockPos next = wasLog ? nextLogOfTree(level, target) : null;
		if (next != null) {
			retarget(treeTop(next));
		} else {
			this.chewTimer = 0;
			this.nextStartTick = reducedTickDelay(100);
		}
	}

	private static BlockPos nextLogOfTree(Level level, BlockPos eaten) {
		for (BlockPos pos : BlockPos.betweenClosed(eaten.offset(-1, -1, -1), eaten.offset(1, 1, 1))) {
			if (level.getBlockState(pos).is(BlockTags.LOGS)) {
				return pos.immutable();
			}
		}
		return null;
	}
}
