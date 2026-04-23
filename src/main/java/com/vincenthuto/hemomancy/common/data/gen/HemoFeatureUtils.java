package com.vincenthuto.hemomancy.common.data.gen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ConfiguredFeatureInit;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class HemoFeatureUtils {
    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context)
    {
    	ConfiguredFeatureInit.bootstrap(context);
    }
    public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name)
    {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Hemomancy.rloc(name));
    }

}
