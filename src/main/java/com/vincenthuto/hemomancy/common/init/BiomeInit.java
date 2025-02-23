package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.terrablender.TestRegion1;
import com.vincenthuto.hemomancy.common.worldgen.terrablender.TestRegion2;
import com.vincenthuto.hemomancy.common.worldgen.terrablender.TestRegion3;
import com.vincenthuto.hemomancy.common.worldgen.terrablender.TestSurfaceRuleData;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.NetherPlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class BiomeInit {
	public static final DeferredRegister<Biome> BIOME_REGISTER = DeferredRegister.create(Registries.BIOME,
			Hemomancy.MOD_ID);

	public static final ResourceKey<Biome> FUNGAL_GARDENS = register("fungal_gardens");
	public static final ResourceKey<Biome> FUNGAL_ISLES = register("fungal_isles");

	private static ResourceKey<Biome> register(String name) {
		ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, new ResourceLocation(Hemomancy.MOD_ID, name));
		return key;
	}

	private static void register(BootstapContext<Biome> context, ResourceKey<Biome> key, Biome biome) {
		context.register(key, biome);
	}

	public static void bootstrapBiomes(BootstapContext<Biome> context) {
		HolderGetter<ConfiguredWorldCarver<?>> carverGetter = context.lookup(Registries.CONFIGURED_CARVER);
		HolderGetter<PlacedFeature> placedFeatureGetter = context.lookup(Registries.PLACED_FEATURE);
		register(context, FUNGAL_GARDENS, fungalGardens(placedFeatureGetter, carverGetter));

		register(context, FUNGAL_ISLES, fungalIsles(placedFeatureGetter, carverGetter));

	}

	private static Biome fungalIsles(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		// Mob spawns
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		spawnBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4));
		spawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2));

		// Biome features
		BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatureGetter,
				carverGetter);
		biomeBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HYPHAE_TENDRIL);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HUGE_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.SMALL_INFECTED_FUNGUS);

		return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.0F).downfall(0.0F)
				.specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011)
						.fogColor(0x601F18).skyColor(0xFF00FFFF)
						.ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
						.ambientMoodSound(
								new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0D))
						.ambientAdditionsSound(
								new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111D))
						.build())
				.mobSpawnSettings(spawnBuilder.build()).generationSettings(biomeBuilder.build()).build();
	}

	public static Biome fungalGardens(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		// Mob spawns
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		spawnBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 50, 4, 4));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityType.ZOMBIFIED_PIGLIN, 100, 4, 4));
		spawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.STRIDER, 60, 1, 2));

		// Biome features
		BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatureGetter,
				carverGetter);
		biomeBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, OrePlacements.ORE_MAGMA);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HYPHAE_TENDRIL);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HUGE_FUNGUS);

		return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.0F).downfall(0.0F)
				.specialEffects((new BiomeSpecialEffects.Builder()).waterColor(4159204).waterFogColor(329011)
						.fogColor(0x601F18).skyColor(0xFF00FFFF)
						.ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
						.ambientMoodSound(
								new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0D))
						.ambientAdditionsSound(
								new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111D))
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

			Regions.register(new TestRegion1(Hemomancy.rloc("overworld_1"), 2));
			Regions.register(new TestRegion2(Hemomancy.rloc("overworld_2"), 2));
			Regions.register(new TestRegion3(Hemomancy.rloc("overworld_3"), 2));

			// Register our surface rules
			SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, Hemomancy.MOD_ID,
					TestSurfaceRuleData.makeRules());
		});
	}
}
