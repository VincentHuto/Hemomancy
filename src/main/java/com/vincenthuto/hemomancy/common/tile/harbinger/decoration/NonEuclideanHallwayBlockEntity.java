package com.vincenthuto.hemomancy.common.tile.harbinger.decoration;

import com.vincenthuto.hemomancy.common.tile.HemoBlockEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NonEuclideanHallwayBlockEntity extends HemoBlockEntity {
	public NonEuclideanHallwayBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.non_euclidean_hallway.get(), pos, state);
	}

}
