package com.vincenthuto.hemomancy.common.tile.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.PulsePylonBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The head of a pulse pylon. It holds no saved state: the charge is the block's {@link PulsePylonBlock#CHARGE}. On the
 * client it remembers when the charge last changed and from what, so the renderer can ease the ring between steps.
 */
public class PulsePylonBlockEntity extends BlockEntity {
	private int previousCharge;
	private long changedAt = Long.MIN_VALUE;

	public PulsePylonBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.pulse_pylon.get(), pos, state);
	}

	public int charge() {
		return getBlockState().getValue(PulsePylonBlock.CHARGE);
	}

	public int previousCharge() {
		return previousCharge;
	}

	/** Game time of the last charge change, or {@link Long#MIN_VALUE} when none has been seen. */
	public long changedAt() {
		return changedAt;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void setBlockState(BlockState state) {
		int before = charge();
		super.setBlockState(state);
		if (state.getValue(PulsePylonBlock.CHARGE) != before && level != null) {
			previousCharge = before;
			changedAt = level.getGameTime();
		}
	}
}
