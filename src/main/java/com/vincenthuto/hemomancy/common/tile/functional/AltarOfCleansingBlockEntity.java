package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.IMultiBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class AltarOfCleansingBlockEntity extends BlockEntity implements IMultiBlockEntity {

	public AltarOfCleansingBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.altar_of_cleansing.get(), pos, state);
	}

	@Override
	public AABB getRenderBoundingBox() {
		return IMultiBlockEntity.computeMultiBlockAABB(this);
	}

	public void tick() {
	}
}
