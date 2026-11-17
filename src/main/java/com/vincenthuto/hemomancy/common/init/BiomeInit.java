package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.feature.ErythrocoralReefTuning;
import com.vincenthuto.hemomancy.common.worldgen.biome.CorticalDriftSurfaceRuleData;
import com.vincenthuto.hemomancy.common.worldgen.biome.ErythrocoralReefRegion;
import com.vincenthuto.hemomancy.common.worldgen.biome.FungalGardensOverworldRegion;
import com.vincenthuto.hemomancy.common.worldgen.biome.FungalGardensSurfaceRuleData;
import com.vincenthuto.hemomancy.config.HemoCommonConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import terrablender.api.EndBiomeRegistry;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class BiomeInit {
	public static final DeferredRegister<Biome> BIOME_REGISTER = DeferredRegister.create(Registries.BIOME,
			Hemomancy.MOD_ID);

	public static final ResourceKey<Biome> FUNGAL_GARDENS = register("fungal_gardens");
	public static final ResourceKey<Biome> PHLEGETHONTIC_BASIN = register("phlegethontic_basin");
	public static final ResourceKey<Biome> FUNGAL_ISLES = register("fungal_isles");
	public static final ResourceKey<Biome> SPORECROWN_THICKET = register("sporecrown_thicket");
	public static final ResourceKey<Biome> MYCELIAL_DEPTHS = register("mycelial_depths");
	// Fungal Gardens dimension lowlands; fungal_gardens itself is the Overworld region biome.
	public static final ResourceKey<Biome> MYCELIAL_MEADOWS = register("mycelial_meadows");
	public static final ResourceKey<Biome> HEMORRHAGIC_PLATEAU = register("hemorrhagic_plateau");
	public static final ResourceKey<Biome> ERYTHROCORAL_REEF = register("erythrocoral_reef");
	public static final ResourceKey<Biome> CORTICAL_DRIFT = register("cortical_drift");

	private static ResourceKey<Biome> register(String name) {
		ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME,
				Hemomancy.rloc(name));
		return key;
	}

	@SubscribeEvent
	public static void commonSetup(final FMLCommonSetupEvent event) {

		event.enqueueWork(() -> {

            if (HemoCommonConfig.enablePelagicWorldgen()) {
                Regions.register(new com.vincenthuto.hemomancy.common.worldgen.biome.PelagicOverworldRegion(
                        Hemomancy.rloc("pelagic_overworld"), HemoCommonConfig.PELAGIC_REGION_WEIGHT.get()));
            } else {
                Regions.register(new ErythrocoralReefRegion(Hemomancy.rloc("erythrocoral_reef_overworld"), 1));
            }
			if(HemoCommonConfig.ENABLE_PHLEGETHONTIC_NETHER_REGION.get()) {
				Regions.register(new com.vincenthuto.hemomancy.common.worldgen.biome.PhlegethonticNetherRegion(
						Hemomancy.rloc("phlegethontic_nether"),HemoCommonConfig.PHLEGETHONTIC_NETHER_REGION_WEIGHT.get()));
			}
			SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.NETHER,Hemomancy.MOD_ID,
					com.vincenthuto.hemomancy.common.worldgen.biome.PhlegethonticSurfaceRuleData.makeRules());
			if (HemoCommonConfig.enableOverworldFungalGardensRegion()) {
				Regions.register(new FungalGardensOverworldRegion(
						Hemomancy.rloc(FungalGardensOverworldRegion.REGION_PATH), 2));
			}

			// Register our surface rules
			SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, Hemomancy.MOD_ID,
                    net.minecraft.world.level.levelgen.SurfaceRules.sequence(
                            com.vincenthuto.hemomancy.common.worldgen.biome.PelagicSurfaceRules.makeRules(),
                            FungalGardensSurfaceRuleData.makeRules()));

			if (HemoCommonConfig.ENABLE_CORTICAL_DRIFT_END_REGION.get()) {
				int endWeight = HemoCommonConfig.CORTICAL_DRIFT_END_REGION_WEIGHT.get();
				// TerraBlender 4.1.0.8 builds its islandsArea from the EDGE list (MixinTheEndBiomeSource
				// passes the edge list twice), so the End's void zones only ever draw edge-registered
				// biomes. Register as edge so the Drift actually generates, and as island so it keeps
				// working if TerraBlender fixes that. CorticalArchipelagoFeature keeps its islands out of
				// any vanilla terrain the edge band overlaps.
				EndBiomeRegistry.registerEdgeBiome(BiomeInit.CORTICAL_DRIFT, endWeight);
				EndBiomeRegistry.registerIslandBiome(BiomeInit.CORTICAL_DRIFT, endWeight);
			}
			SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.END, Hemomancy.MOD_ID,
					CorticalDriftSurfaceRuleData.makeRules());
		});
	}
}
