package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class FungalFloorFeature extends Feature<NoneFeatureConfiguration> {
	public FungalFloorFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		ChunkPos chunk = new ChunkPos(context.origin());
		long seed = level.getSeed();
		int y = level.getMinBuildHeight() + 2;
		int placed = 0;

		for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
			for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
				BlockPos base = new BlockPos(x, y, z);
				if (!level.getBlockState(base.below()).is(BlockInit.hemorrhagic_crust.get())
						|| !level.getBlockState(base).isAir()) {
					continue;
				}

				int height = FungalFloorPattern.heightAt(seed, x, z);
				while (height > 1 && !level.getBlockState(base.above(height - 1)).isAir()) height--;
				BlockState top = switch (FungalFloorPattern.surfaceAt(seed, x, z)) {
					case 0 -> BlockInit.calcified_hyphae.get().defaultBlockState();
					case 1 -> BlockInit.mycelium_erythrocytic_dirt.get().defaultBlockState();
					default -> BlockInit.erythrocytic_mycelium.get().defaultBlockState();
				};

				for (int offset = 0; offset < height; offset++) {
					BlockPos target = base.above(offset);
					if (!level.getBlockState(target).isAir()) break;
					level.setBlock(target, offset == height - 1
							? top : BlockInit.erythrocytic_dirt.get().defaultBlockState(), 2);
					placed++;
				}
			}
		}

		return placed > 0;
	}

}
