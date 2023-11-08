package com.vincenthuto.hemomancy.common.worldgen.feature;

import org.jetbrains.annotations.NotNull;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.worldgen.config.SmallInfectedMushroomConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * This Feature is essentially the same as vanilla's
 * {@link net.minecraft.world.level.levelgen.feature.SimpleBlockFeature}
 */
public class SmallInfectedMushroomFeature extends Feature<SmallInfectedMushroomConfig> {
	public SmallInfectedMushroomFeature(Codec<SmallInfectedMushroomConfig> codec) {
		super(codec);
	}

	@Override
	public boolean place(@NotNull FeaturePlaceContext<SmallInfectedMushroomConfig> ctx) {
		SmallInfectedMushroomConfig config = ctx.config();
		WorldGenLevel level = ctx.level();
		BlockPos pos = ctx.origin();
		BlockState state = config.getToPlace().getState(ctx.random(), pos);
		if (state.canSurvive(level, pos)) {
			level.setBlock(pos, state, Block.UPDATE_CLIENTS);

			return true;
		} else {
			return false;
		}
	}
}
