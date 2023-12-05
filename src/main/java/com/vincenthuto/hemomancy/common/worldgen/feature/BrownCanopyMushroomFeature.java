package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public class BrownCanopyMushroomFeature extends CanopyMushroomFeature {
    public BrownCanopyMushroomFeature(Codec<HugeMushroomFeatureConfiguration> featureConfigurationCodec) {
        super(featureConfigurationCodec);
    }

    @Override
    protected int getBranches(RandomSource random) {
        return Math.max(random.nextInt(5), 2);
    }

    @Override
    protected double getLength(RandomSource random) {
        return 9 - random.nextInt(2);
    }
}
