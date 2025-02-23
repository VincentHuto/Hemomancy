package com.vincenthuto.hemomancy.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CrimsonFlameBlock extends FireBlock {

	public CrimsonFlameBlock(BlockBehaviour.Properties builder, float fireDamageIn) {
		super(builder);
	}


}
