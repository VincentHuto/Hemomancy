package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class RedCanopyMushroomFeature extends CanopyMushroomFeature {

	public RedCanopyMushroomFeature(Codec<HugeMushroomFeatureConfiguration> featureConfigurationCodec) {
		super(featureConfigurationCodec);
	}

	@Override
	protected int getTreeHeight(RandomSource random) {
		return 11 + random.nextInt(8);
	}

	@Override
	protected int getBranches(RandomSource random) {
		return 2 + random.nextInt(5);
	}

	@Override
	protected double getLength(RandomSource random) {
		return 8.0D + random.nextDouble() * 5.0D;
	}

	@Override
	protected float getLeanChance(RandomSource random) {
		return 0.12F + random.nextFloat() * 0.12F;
	}

	@Override
	protected float getCapHorizontalStretch(RandomSource random) {
		return 0.8F + random.nextFloat() * 0.55F;
	}

	@Override
	protected float getCapVerticalStretch(RandomSource random) {
		return 0.95F + random.nextFloat() * 0.75F;
	}
}
