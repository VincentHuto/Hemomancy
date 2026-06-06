package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Minimal Hematic Stake block entity. It stores no data; its sole purpose is
 * rendering the jagged sovereign-will shard in place of a static block model.
 */
public class HematicStakeBlockEntity extends BlockEntity {
	public HematicStakeBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.hematic_stake.get(), pos, state);
	}

	public AABB getRenderBoundingBox() {
		BlockPos pos = getBlockPos();
		return new AABB(pos.getX(), pos.getY(), pos.getZ(),
				pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);
	}
}
