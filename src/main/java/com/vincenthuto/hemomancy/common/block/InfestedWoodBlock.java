package com.vincenthuto.hemomancy.common.block;

import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class InfestedWoodBlock extends Block {

	public InfestedWoodBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canSustainPlant(BlockState state, BlockGetter world, BlockPos pos, Direction facing,
			IPlantable plantable) {
		PlantType plantType = plantable.getPlantType(world, pos.relative(facing));
		if (plantType == PlantType.CAVE) {
			return true;
		}
		Block plant = plantable.getPlant(world, pos.relative(facing)).getBlock();
		if (plant instanceof InfectedFungusBlock || plant instanceof GhostPipeBlock
				|| plant instanceof SarcodesBlock) {
			return true;
		}
		return super.canSustainPlant(state, world, pos, facing, plantable);
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
