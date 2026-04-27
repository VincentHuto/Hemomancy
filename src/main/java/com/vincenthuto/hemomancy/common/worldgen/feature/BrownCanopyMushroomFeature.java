package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class BrownCanopyMushroomFeature extends CanopyMushroomFeature {
    public BrownCanopyMushroomFeature(Codec<HugeMushroomFeatureConfiguration> featureConfigurationCodec) {
        super(featureConfigurationCodec);
    }

    @Override
    protected int getTreeHeight(RandomSource random) {
        return 7 + random.nextInt(6);
    }

    @Override
    protected int getBranches(RandomSource random) {
        return 3 + random.nextInt(5);
    }

    @Override
    protected double getLength(RandomSource random) {
        return 6.5D + random.nextDouble() * 5.0D;
    }

    @Override
    protected float getLeanChance(RandomSource random) {
        return 0.22F + random.nextFloat() * 0.18F;
    }

    @Override
    protected float getCapHorizontalStretch(RandomSource random) {
        return 1.15F + random.nextFloat() * 0.8F;
    }

    @Override
    protected float getCapVerticalStretch(RandomSource random) {
        return 0.75F + random.nextFloat() * 0.45F;
    }
}
