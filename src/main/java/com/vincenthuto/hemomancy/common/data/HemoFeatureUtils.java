package com.vincenthuto.hemomancy.common.data;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.ConfiguredFeatureInit;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class HemoFeatureUtils {
    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context)
    {
    	ConfiguredFeatureInit.bootstrap(context);
    }
    public static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name)
    {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Hemomancy.MOD_ID, name));
    }

}
