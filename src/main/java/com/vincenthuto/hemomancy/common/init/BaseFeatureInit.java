package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.config.SmallInfectedMushroomConfig;
import com.vincenthuto.hemomancy.common.worldgen.feature.FleshTendonFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.FungusFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.SmallInfectedMushroomFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;

public class BaseFeatureInit {

	public static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(Registries.FEATURE,
			Hemomancy.MOD_ID);

	public static final Feature<NoneFeatureConfiguration> FLESH_TENDON = register("flesh_tendon",
			new FleshTendonFeature(NoneFeatureConfiguration.CODEC));

	public static final Feature<NoneFeatureConfiguration> HUGE_FUNGUS = register("huge_fungus",
			new FungusFeature(NoneFeatureConfiguration.CODEC));

	public static final Feature<SmallInfectedMushroomConfig> SMALL_FUNGUS = register("small_fungus",
			new SmallInfectedMushroomFeature(SmallInfectedMushroomConfig.CODEC));

	
	private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String key, F value) {
		FEATURE_REGISTER.register(key, () -> value);
		return value;
	}

	public static void setup() {
	}
}