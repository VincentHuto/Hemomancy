package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.block.unstained.plant.GhostPipeBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;

public class InfestedWoodBlock extends Block {

	public InfestedWoodBlock(Properties properties) {
		super(properties);
	}

	@Override
	public TriState canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing,
			BlockState plantState) {
		Block plant = plantState.getBlock();
		if (plant instanceof InfectedFungusBlock || plant instanceof GhostPipeBlock
				|| plant instanceof SarcodesBlock) {
			return TriState.TRUE;
		}
		return super.canSustainPlant(state, world, pos, facing, plantState);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (random.nextInt(25) == 0) {
			BlockPos above = pos.above();
			if (level.isEmptyBlock(above) && level.getMaxLocalRawBrightness(above) <= 7) {
				int roll = random.nextInt(10);
				BlockState plant;
				if (roll < 5) {
					plant = BlockInit.infected_fungus.get().defaultBlockState();
				} else if (roll < 8) {
					plant = BlockInit.hyphae.get().defaultBlockState();
				} else {
					plant = BlockInit.stinkhorn_fungus.get().defaultBlockState();
				}
				level.setBlockAndUpdate(above, plant);
			}
		}
	}
}
