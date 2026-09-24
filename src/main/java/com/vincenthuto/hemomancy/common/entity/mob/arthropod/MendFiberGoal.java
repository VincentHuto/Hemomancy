package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;

/**
 * A calm Borer's work: find a gap where a cable was severed and extrude new fiber to close it. Only
 * genuine gaps qualify - air flanked by network blocks on opposing sides - so a Borer can never spin
 * cable out into open void.
 */
public class MendFiberGoal extends MoveToBlockGoal {
	private static final int MEND_DURATION = 40;

	private final MyelinBorerEntity borer;
	private int mendTimer;

	public MendFiberGoal(MyelinBorerEntity borer, double speed, int searchRange) {
		super(borer, speed, searchRange);
		this.borer = borer;
	}

	@Override
	public boolean canUse() {
		return !this.borer.isAgitated() && super.canUse();
	}

	@Override
	public boolean canContinueToUse() {
		return !this.borer.isAgitated() && isValidTarget(this.borer.level(), this.blockPos);
	}

	@Override
	public void start() {
		this.mendTimer = 0;
		this.borer.setWorkTarget(this.blockPos);
	}

	@Override
	public void stop() {
		this.borer.setWorkTarget(null);
	}

	@Override
	protected boolean isValidTarget(LevelReader level, BlockPos pos) {
		return FiberRepair.gapAxis(level,pos)>=0;
	}

	@Override
	public void tick() {
		Level level = this.borer.level();
		BlockPos target = this.blockPos;
		this.borer.getLookControl().setLookAt(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);

		if (this.borer.position().distanceToSqr(target.getX() + 0.5D,
				target.getY() + 1.0D, target.getZ() + 0.5D) > 2.25D) {
			this.mendTimer = 0;
			return;
		}
		if (!level.getBlockState(target).isAir()) {
			return;
		}
		if (FiberRepair.gapAxis(level,target)<0) {
			return;
		}

		this.mendTimer++;

		if (level instanceof ServerLevel serverLevel && this.mendTimer % 8 == 0) {
			MyelinBorerWorkEffects.repairing(serverLevel, target);
		}
		if (this.mendTimer >= MEND_DURATION) {
			if(level instanceof ServerLevel serverLevel) FiberRepair.repair(serverLevel,target);
			this.mendTimer = 0;
		}
	}
}
