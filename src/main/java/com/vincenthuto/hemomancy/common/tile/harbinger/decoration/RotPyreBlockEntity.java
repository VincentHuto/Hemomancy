package com.vincenthuto.hemomancy.common.tile.harbinger.decoration;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A Rot Pyre's block entity. It holds no state (whether the pyre burns is its block's LIT); it is there so the pyre's
 * renderer can draw its Flammeus fire.
 */
public class RotPyreBlockEntity extends BlockEntity {
	public RotPyreBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.rot_pyre.get(), pos, state);
	}
}
