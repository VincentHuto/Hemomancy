package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonNerveEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A provoked Borer's work: chew a cable segment out of a live run, severing the route. Synaptic nodes
 * are never targeted, so hubs and anchors survive and the network's topology stays recoverable - the
 * damage is cut cables, not an erased biome.
 */
public class BoreFiberGoal extends MoveToBlockGoal {
	private static final int BORE_DURATION = 60;

	private final MyelinBorerEntity borer;
	private int boreTimer;

	public BoreFiberGoal(MyelinBorerEntity borer, double speed, int searchRange) {
		super(borer, speed, searchRange);
		this.borer = borer;
	}

	@Override
	public boolean canUse() {
		return this.borer.isAgitated() && this.borer.getTarget() == null && super.canUse();
	}

	@Override
	public boolean canContinueToUse() {
		return this.borer.isAgitated() && this.borer.getTarget() == null
				&& isValidTarget(this.borer.level(), this.blockPos);
	}

	@Override
	public void start() {
		this.boreTimer = 0;
		this.borer.setWorkTarget(this.blockPos);
	}

	@Override
	public void stop() {
		this.borer.setWorkTarget(null);
	}

	/** synaptic_node is deliberately excluded: hubs and anchors must survive. */
	private static boolean isEdible(BlockState state) {
		return state.is(BlockInit.nerve_fiber.get()) || state.is(BlockInit.nerve_bundle.get());
	}

	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		return isEdible(level.getBlockState(pos))
				&& FiberGapRules.isGap(FiberRepair.neighbourFlags(level, pos));
	}

	@Override
	public void tick() {
		Level level = this.borer.level();
		BlockPos target = this.blockPos;
		this.borer.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);

		if (this.borer.position().distanceToSqr(target.getX() + 0.5D,
				target.getY() + 1.0D, target.getZ() + 0.5D) > 2.25D) {
			this.boreTimer = 0;
			return;
		}
		BlockState state = level.getBlockState(target);
		if (!isEdible(state) || !FiberGapRules.isGap(FiberRepair.neighbourFlags(level, target))) {
			return;
		}

		this.boreTimer++;
		if (level instanceof ServerLevel serverLevel && this.boreTimer % 6 == 0) {
			MyelinBorerWorkEffects.chewing(serverLevel, target, state);
		}
		if (this.boreTimer >= BORE_DURATION) {
			if(level.destroyBlock(target,false) && level instanceof ServerLevel serverLevel)
				NaeglerophaeonNerveEvents.announceGap(serverLevel,target,false);
			this.boreTimer = 0;
		}
	}
}
