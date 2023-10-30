package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.feature.FleshTendonFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;

public class BaseFeatureInit {
	
	
    public static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(Registries.FEATURE, Hemomancy.MOD_ID);
	public static final Feature<NoneFeatureConfiguration> FLESH_TENDON = register("flesh_tendon",
			new FleshTendonFeature(NoneFeatureConfiguration.CODEC));
	
    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String key, F value)
    {
        FEATURE_REGISTER.register(key, () -> value);
        return value;
    }

    public static void setup() {}
}
