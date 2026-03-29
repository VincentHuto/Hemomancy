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
	public static final ResourceKey<Biome> SPORECROWN_THICKET = register("sporecrown_thicket");

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
		register(context, SPORECROWN_THICKET, sporecrownThicket(placedFeatureGetter, carverGetter));
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

	private static Biome sporecrownThicket(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		// Mob spawns - dense and hostile
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.erythromycelium_eruptus.get(), 15, 1, 3));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.chthonian.get(), 8, 1, 2));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.fargone.get(), 6, 1, 3));
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.fungling.get(), 12, 2, 5));
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.chitinite.get(), 8, 2, 4));
		spawnBuilder.addSpawn(MobCategory.AMBIENT,
				new MobSpawnSettings.SpawnerData(EntityType.BAT, 10, 4, 8));

		// Biome features - dense fungal overgrowth
		BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatureGetter,
				carverGetter);
		biomeBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HYPHAE_TENDRIL);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HUGE_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_INFESTED_VENOUS_STONE_BLOB);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_MYCELIUM_BLOB);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_CANOPY_MUSHROOMS_DENSE);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION,
				PlacedFeatureInit.SMALL_INFECTED_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.PATCH_HYPHAE);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.BLEEDING_HEARTS);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.STINK_HORNS);

		return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(1.2F).downfall(0.9F)
				.specialEffects((new BiomeSpecialEffects.Builder()).waterColor(10980608).waterFogColor(10980608)
						.fogColor(0x330000).skyColor(0x260000).grassColorOverride(0x6B5B0F).foliageColorOverride(0x6B5B0F)
						.ambientLoopSound(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP)
						.ambientMoodSound(
								new AmbientMoodSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0D))
						.ambientAdditionsSound(
								new AmbientAdditionsSettings(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111D))
						.ambientParticle(new net.minecraft.world.level.biome.AmbientParticleSettings(
								net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE, 0.025F))
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
