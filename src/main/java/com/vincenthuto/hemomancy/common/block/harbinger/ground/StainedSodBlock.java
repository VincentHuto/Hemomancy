package com.vincenthuto.hemomancy.common.block.harbinger.ground;

import com.vincenthuto.hemomancy.common.world.answer.BloodSpilledLedger;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswerRules;
import com.vincenthuto.hemomancy.config.HemoConfigValues;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stained Sod (core-loop Track 10): grass under a stain that wore away in a region where much blood was spilled. It
 * spreads nowhere and grows nothing. On its random tick it turns back to grass once the region has quieted (or the
 * server switches the ground's memory off), and to dirt when something covers it, as grass does.
 */
public class StainedSodBlock extends Block {
	public StainedSodBlock(Properties properties) {
		super(properties);
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		BlockState above = level.getBlockState(pos.above());
		if (above.isSolidRender(level, pos.above())) {
			level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
			return;
		}
		boolean remembers = HemoConfigValues.get(HemoServerConfig.WORLD_ANSWER_GROUND_REMEMBERS, true);
		if (!remembers || WorldAnswerRules.sodRecovers(BloodSpilledLedger.band(level, pos), random.nextFloat())) {
			level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
		}
	}
}
