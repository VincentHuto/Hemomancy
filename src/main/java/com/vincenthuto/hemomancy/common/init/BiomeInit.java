package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.feature.ErythrocoralReefTuning;
import com.vincenthuto.hemomancy.common.worldgen.biome.CorticalDriftSurfaceRuleData;
import com.vincenthuto.hemomancy.common.worldgen.biome.ErythrocoralReefRegion;
import com.vincenthuto.hemomancy.common.worldgen.biome.FungalGardensOverworldRegion;
import com.vincenthuto.hemomancy.common.worldgen.biome.FungalGardensSurfaceRuleData;
import com.vincenthuto.hemomancy.config.HemoCommonConfig;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.NetherPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
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

	public static Biome fungalGardens(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		// Fungal Gardens - lush, peaceful, verdant. The cultivated heart of the dimension.
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		// Peaceful creatures dominate
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.fungling.get(), 15, 3, 6));
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.chitinite.get(), 10, 2, 5));
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.venous_strider.get(), 8, 1, 3));
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.crimson_doe.get(), 6, 2, 4));
		// Light hostile presence
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.fargone.get(), 4, 1, 2));
		spawnBuilder.addSpawn(MobCategory.AMBIENT,
				new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 4, 8));

		// Biome features - lush vegetation, mushroom forests
		BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatureGetter,
				carverGetter);
		biomeBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.FUNGAL_FLOOR);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HYPHAE_TENDRIL);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HUGE_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_MYCELIUM_BLOB);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_CONSCIOUS_MASS_BLOB);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_CANOPY_MUSHROOMS_SPARSE);
		// Rich vegetation
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.BLEEDING_HEARTS);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.PATCH_HYPHAE);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION,
				PlacedFeatureInit.SMALL_INFECTED_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.LETHEAN_POPPIES);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.GHOST_PIPES);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.SARCODES);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.RAFFLESIA);
		// Ore generation
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_ORES, PlacedFeatureInit.ORE_HEMATIC_IRON);

		return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.0F).downfall(0.0F)
				.specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011)
						.fogColor(0x601F18).skyColor(0xFF00FFFF)
						.grassColorOverride(0x8B4513).foliageColorOverride(0x6B3A0F)
						.ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
						.ambientMoodSound(
								new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0D))
						.ambientAdditionsSound(
								new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111D))
						.ambientParticle(new net.minecraft.world.level.biome.AmbientParticleSettings(
								net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE, 0.01F))
						.build())
				.mobSpawnSettings(spawnBuilder.build()).generationSettings(biomeBuilder.build()).build();
	}

	private static void addFeature(BiomeGenerationSettings.Builder builder, GenerationStep.Decoration step,
			ResourceKey<PlacedFeature> feature) {
		builder.addFeature(step, feature);
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
