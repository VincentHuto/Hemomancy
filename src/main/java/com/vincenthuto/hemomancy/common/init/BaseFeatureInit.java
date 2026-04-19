package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.config.HyphaeConfig;
import com.vincenthuto.hemomancy.common.worldgen.config.SmallInfectedMushroomConfig;
import com.vincenthuto.hemomancy.common.worldgen.feature.BogBodyFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.EarthenVeinFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.BrownCanopyMushroomFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.CheckAbovePatchFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.HyphaeTendrilFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.FungusFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.HyphaeFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.RedCanopyMushroomFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.SilverBellsTowerFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.SmallInfectedMushroomFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.SporeNexusTowerFeature;
import com.vincenthuto.hemomancy.common.worldgen.feature.TermiteMoundFeature;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;

public class BaseFeatureInit {

	public static final DeferredRegister<Feature<?>> FEATURE_REGISTER = DeferredRegister.create(Registries.FEATURE,
			Hemomancy.MOD_ID);

	public static final Feature<DiskConfiguration> MYCELIUM_BLOB = register("mycelium_blob",
			new CheckAbovePatchFeature(DiskConfiguration.CODEC));
	public static final Feature<DiskConfiguration> INFESTED_VENOUS_STONE_BLOB = register("infested_venous_stone_blob",
			new CheckAbovePatchFeature(DiskConfiguration.CODEC));

	public static final Feature<HugeMushroomFeatureConfiguration> CANOPY_BROWN_MUSHROOM = register(
			"canopy_brown_mushroom", new BrownCanopyMushroomFeature(HugeMushroomFeatureConfiguration.CODEC));
	public static final Feature<HugeMushroomFeatureConfiguration> CANOPY_RED_MUSHROOM = register("canopy_red_mushroom",
			new RedCanopyMushroomFeature(HugeMushroomFeatureConfiguration.CODEC));

	public static final Feature<NoneFeatureConfiguration> HYPHAE_TENDRIL = register("hyphae_tendril",
			new HyphaeTendrilFeature(NoneFeatureConfiguration.CODEC));

	public static final Feature<NoneFeatureConfiguration> HUGE_FUNGUS = register("huge_fungus",
			new FungusFeature(NoneFeatureConfiguration.CODEC));

	public static final Feature<SmallInfectedMushroomConfig> SMALL_FUNGUS = register("small_fungus",
			new SmallInfectedMushroomFeature(SmallInfectedMushroomConfig.CODEC));

	public static final Feature<HyphaeConfig> PATCH_HYPHAE = register("patch_hyphae",
			new HyphaeFeature(HyphaeConfig.CODEC));

	public static final Feature<NoneFeatureConfiguration> BOG_BODY = register("bog_body",
			new BogBodyFeature(NoneFeatureConfiguration.CODEC));

	public static final Feature<NoneFeatureConfiguration> EARTHEN_VEIN = register("earthen_vein",
			new EarthenVeinFeature(NoneFeatureConfiguration.CODEC));

	public static final Feature<NoneFeatureConfiguration> TERMITE_MOUND = register("termite_mound",
			new TermiteMoundFeature(NoneFeatureConfiguration.CODEC));

	public static final Feature<NoneFeatureConfiguration> SPORE_NEXUS_TOWER = register("spore_nexus_tower",
			new SporeNexusTowerFeature(NoneFeatureConfiguration.CODEC));

	public static final Feature<NoneFeatureConfiguration> SILVER_BELLS_TOWER = register("silver_bells_tower",
			new SilverBellsTowerFeature(NoneFeatureConfiguration.CODEC));

	private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String key, F value) {
		FEATURE_REGISTER.register(key, () -> value);
		return value;
	}

	public static void setup() {
	}
}
