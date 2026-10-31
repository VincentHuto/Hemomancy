package com.vincenthuto.hemomancy.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.function.Predicate;

/**
 * Client-side state for a mob that turns its body onto the wall it climbs:
 * which wall it clings to and how far the turn has blended in. The owning
 * entity ticks it on the client; its renderer reads it to swing and tip the
 * body, and its model reads {@link #headFollow} to keep the head straight.
 */
public final class WallClimbPose {
	private static final float BLEND_PER_TICK = 0.2F;

	private final Predicate<BlockState> climbable;
	private float amount;
	private float amountO;
	private Direction wall = Direction.NORTH;

	/** @param climbable which wall blocks count; the body only turns onto these */
	public WallClimbPose(Predicate<BlockState> climbable) {
		this.climbable = climbable;
	}

	/**
	 * @param climbing whether the mob is climbing or clinging this tick, by its own
	 *                 rules; the pose still needs a touched wall to turn onto
	 */
	public void tick(LivingEntity entity, boolean climbing) {
		this.amountO = this.amount;
		Direction touched = climbing ? touchedWall(entity) : null;
		if (touched != null) {
			this.wall = touched;
		}
		this.amount = Mth.approach(this.amount, touched != null ? 1.0F : 0.0F, BLEND_PER_TICK);
	}

	/** The side the body presses against, nearest the way it faces when there are several. */
	private Direction touchedWall(LivingEntity entity) {
		AABB box = entity.getBoundingBox();
		Direction best = null;
		float bestTurn = Float.MAX_VALUE;
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			AABB pushed = box.move(dir.getStepX() * 0.1D, 0.0D, dir.getStepZ() * 0.1D);
			if (entity.level().noCollision(entity, pushed) || !touchesClimbable(entity, pushed)) {
				continue;
			}
			float turn = Math.abs(Mth.wrapDegrees(dir.toYRot() - entity.yBodyRot));
			if (turn < bestTurn) {
				best = dir;
				bestTurn = turn;
			}
		}
		return best;
	}

	private boolean touchesClimbable(LivingEntity entity, AABB pushed) {
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(pushed.minX, pushed.minY, pushed.minZ),
				BlockPos.containing(pushed.maxX, pushed.maxY, pushed.maxZ))) {
			BlockState state = entity.level().getBlockState(pos);
			if (!state.getCollisionShape(entity.level(), pos).isEmpty() && this.climbable.test(state)) {
				return true;
			}
		}
		return false;
	}

	public float amount(float partialTick) {
		return Mth.lerp(partialTick, this.amountO, this.amount);
	}

	public float wallYaw() {
		return this.wall.toYRot();
	}

	/** How much head tracking to keep this frame: none while fully on a wall. */
	public float headFollow(float partialTick) {
		return 1.0F - amount(partialTick);
	}
}
