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

/**
 * AI goal that makes a mob seek out nearby log/wood blocks,
 * navigate to them, gnaw on them with breaking particles and sounds,
 * then destroy the block and drop coarse dirt — like a termite chewing wood.
 * <p>
 * Gives up on unreachable blocks (floating wood) after a timeout and
 * filters out logs that have no adjacent walkable ground.
 */
public class ChewWoodGoal extends MoveToBlockGoal {
	private final PathfinderMob mob;
	private int chewTimer;
	private int stuckTimer;
	private static final int CHEW_DURATION = 60;   // ~3 seconds at 20 tps
	private static final int GIVE_UP_TICKS = 100;  // ~5 seconds trying to reach before giving up

	public ChewWoodGoal(PathfinderMob mob, double speed, int searchRange) {
		super(mob, speed, searchRange);
		this.mob = mob;
	}

	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		if (!level.getBlockState(pos).is(BlockTags.LOGS)) {
			return false;
		}
		// Reject floating wood — require at least one adjacent/below block
		// that has solid ground the mob could stand on
		for (Direction dir : Direction.values()) {
			BlockPos neighbor = pos.relative(dir);
			BlockPos below = neighbor.below();
			// The neighbor must be non-solid (air/passable) and have solid ground beneath it
			if (!level.getBlockState(neighbor).isCollisionShapeFullBlock(level, neighbor)
					&& level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
				return true;
			}
		}
		// Also accept if the block itself is at ground level (block below is solid)
		BlockPos belowTarget = pos.below();
		if (level.getBlockState(belowTarget).isFaceSturdy(level, belowTarget, Direction.UP)) {
			return true;
		}
		return false;
	}

	@Override
	public double acceptedDistance() {
		return 1.5;
	}

	@Override
	public void start() {
		super.start();
		this.chewTimer = 0;
		this.stuckTimer = 0;
	}

	@Override
	public void stop() {
		super.stop();
		this.chewTimer = 0;
		this.stuckTimer = 0;
	}

	@Override
	public boolean canContinueToUse() {
		// Give up if stuck trying to reach the block for too long
		if (this.stuckTimer >= GIVE_UP_TICKS) {
			return false;
		}
		return super.canContinueToUse();
	}

	@Override
	public void tick() {
		super.tick();
		Level level = this.mob.level();
		BlockPos target = this.blockPos;

		// Look at the target block
		this.mob.getLookControl().setLookAt(
				target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);

		// Only chew when close enough
		if (this.isReachedTarget()) {
			// We reached it — reset stuck timer
			this.stuckTimer = 0;

			BlockState state = level.getBlockState(target);
			if (!state.is(BlockTags.LOGS)) {
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

			// Destroy the block after chewing duration and drop coarse dirt
			if (this.chewTimer >= CHEW_DURATION) {
				level.destroyBlock(target, false);

				ItemEntity drop = new ItemEntity(level,
						target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
						new ItemStack(Blocks.COARSE_DIRT.asItem(), 1));
				drop.setDefaultPickUpDelay();
				level.addFreshEntity(drop);

				// Play a final crunch sound
				level.playSound(null, target,
						SoundEvents.WOOD_BREAK, SoundSource.HOSTILE,
						1.0f, 0.8f);

				this.chewTimer = 0;
				this.nextStartTick = reducedTickDelay(100); // Cooldown before seeking next block
			}
		} else {
			// Not at target yet — increment stuck timer
			this.chewTimer = 0;
			this.stuckTimer++;

			// If navigation has completely stalled (no path), give up immediately
			if (this.mob.getNavigation().isDone() && this.stuckTimer > 20) {
				this.stuckTimer = GIVE_UP_TICKS;
			}
		}
	}
}
