package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.ErythrocoralReefTuning;
import com.vincenthuto.hemomancy.common.worldgen.terrablender.ErythrocoralReefRegion;
import com.vincenthuto.hemomancy.common.worldgen.terrablender.FungalGardensOverworldRegion;
import com.vincenthuto.hemomancy.common.worldgen.terrablender.TestSurfaceRuleData;
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
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class BiomeInit {
	public static final DeferredRegister<Biome> BIOME_REGISTER = DeferredRegister.create(Registries.BIOME,
			Hemomancy.MOD_ID);

	public static final ResourceKey<Biome> FUNGAL_GARDENS = register("fungal_gardens");
	public static final ResourceKey<Biome> FUNGAL_ISLES = register("fungal_isles");
	public static final ResourceKey<Biome> SPORECROWN_THICKET = register("sporecrown_thicket");
	public static final ResourceKey<Biome> MYCELIAL_DEPTHS = register("mycelial_depths");
	public static final ResourceKey<Biome> HEMORRHAGIC_PLATEAU = register("hemorrhagic_plateau");
	public static final ResourceKey<Biome> ERYTHROCORAL_REEF = register("erythrocoral_reef");

	private static ResourceKey<Biome> register(String name) {
		ResourceKey<Biome> key = ResourceKey.create(Registries.BIOME, Hemomancy.rloc(name));
		return key;
	}

	private static void register(BootstrapContext<Biome> context, ResourceKey<Biome> key, Biome biome) {
		context.register(key, biome);
	}

	public static void bootstrapBiomes(BootstrapContext<Biome> context) {
		HolderGetter<ConfiguredWorldCarver<?>> carverGetter = context.lookup(Registries.CONFIGURED_CARVER);
		HolderGetter<PlacedFeature> placedFeatureGetter = context.lookup(Registries.PLACED_FEATURE);
		register(context, FUNGAL_GARDENS, fungalGardens(placedFeatureGetter, carverGetter));
		register(context, FUNGAL_ISLES, fungalIsles(placedFeatureGetter, carverGetter));
		register(context, SPORECROWN_THICKET, sporecrownThicket(placedFeatureGetter, carverGetter));
		register(context, MYCELIAL_DEPTHS, mycelialDepths(placedFeatureGetter, carverGetter));
		register(context, HEMORRHAGIC_PLATEAU, hemorrhagicPlateau(placedFeatureGetter, carverGetter));
		register(context, ERYTHROCORAL_REEF, erythrocoralReef(placedFeatureGetter, carverGetter));
	}

	private static Biome fungalIsles(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		// Fungal Isles - barren, exposed, dangerous. Hostile landscape with sparse vegetation.
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		// Dangerous hostile mobs - this is the treacherous biome
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.abhorent_thought.get(), 10, 1, 2));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.thirster.get(), 10, 1, 3));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.lump_of_thought.get(), 5, 1, 1));
		spawnBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 30, 1, 2));
		// Some passive fauna
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.fungling.get(), 6, 1, 3));
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.venous_strider.get(), 4, 1, 2));

		// Biome features - sparse, barren with exposed ore
		BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatureGetter,
				carverGetter);
		biomeBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_OPEN);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, NetherPlacements.SPRING_CLOSED);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HYPHAE_TENDRIL);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HUGE_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_INFESTED_VENOUS_STONE_BLOB);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_CANOPY_MUSHROOMS_SPARSE);
		// Sparse vegetation
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.PATCH_HYPHAE);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION,
				PlacedFeatureInit.SMALL_INFECTED_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.STINK_HORNS);
		// Ore generation
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_ORES, PlacedFeatureInit.ORE_HEMATIC_IRON);

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

	private static Biome sporecrownThicket(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		// Sporecrown Thicket - dense, overgrown, dangerous. The deepest fungal infection.
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.erythromycelium_eruptus.get(), 15, 1, 3));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.chthonian.get(), 8, 1, 2));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.fargone.get(), 6, 1, 3));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.blood_drunk_puppeteer.get(), 12, 1, 1));
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
				PlacedFeatureInit.PLACED_CONSCIOUS_MASS_BLOB);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_CANOPY_MUSHROOMS_DENSE);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.BLEEDING_HEARTS);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.PATCH_HYPHAE);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION,
				PlacedFeatureInit.SMALL_INFECTED_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.STINK_HORNS);
		// Ore generation
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_ORES, PlacedFeatureInit.ORE_HEMATIC_IRON);

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

	private static Biome erythrocoralReef(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		spawnBuilder.addSpawn(MobCategory.WATER_AMBIENT,
				new MobSpawnSettings.SpawnerData(EntityInit.blood_lantern_jelly.get(),
						ErythrocoralReefTuning.BLOOD_LANTERN_JELLY_WEIGHT,
						ErythrocoralReefTuning.BLOOD_LANTERN_JELLY_MIN_COUNT,
						ErythrocoralReefTuning.BLOOD_LANTERN_JELLY_MAX_COUNT));
		spawnBuilder.addSpawn(MobCategory.WATER_AMBIENT,
				new MobSpawnSettings.SpawnerData(EntityInit.barbed_urchin.get(),
						ErythrocoralReefTuning.BARBED_URCHIN_WEIGHT,
						ErythrocoralReefTuning.BARBED_URCHIN_MIN_COUNT,
						ErythrocoralReefTuning.BARBED_URCHIN_MAX_COUNT));
		spawnBuilder.addSpawn(MobCategory.WATER_AMBIENT,
				new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH,
						ErythrocoralReefTuning.TROPICAL_FISH_WEIGHT,
						ErythrocoralReefTuning.TROPICAL_FISH_MIN_COUNT,
						ErythrocoralReefTuning.TROPICAL_FISH_MAX_COUNT));
		spawnBuilder.addSpawn(MobCategory.WATER_AMBIENT,
				new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH,
						ErythrocoralReefTuning.PUFFERFISH_WEIGHT,
						ErythrocoralReefTuning.PUFFERFISH_MIN_COUNT,
						ErythrocoralReefTuning.PUFFERFISH_MAX_COUNT));
		spawnBuilder.addSpawn(MobCategory.WATER_CREATURE,
				new MobSpawnSettings.SpawnerData(EntityType.SQUID, ErythrocoralReefTuning.SQUID_WEIGHT,
						ErythrocoralReefTuning.SQUID_MIN_COUNT, ErythrocoralReefTuning.SQUID_MAX_COUNT));
		spawnBuilder.addSpawn(MobCategory.WATER_CREATURE,
				new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, ErythrocoralReefTuning.DOLPHIN_WEIGHT,
						ErythrocoralReefTuning.DOLPHIN_MIN_COUNT, ErythrocoralReefTuning.DOLPHIN_MAX_COUNT));
		spawnBuilder.addSpawn(MobCategory.WATER_CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.mnemonic_whale.get(),
						ErythrocoralReefTuning.MNEMONIC_WHALE_WEIGHT,
						ErythrocoralReefTuning.MNEMONIC_WHALE_MIN_COUNT,
						ErythrocoralReefTuning.MNEMONIC_WHALE_MAX_COUNT));

		BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatureGetter,
				carverGetter);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.ERYTHROCORAL_REEF);

		return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.95F).downfall(0.8F)
				.specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x8A315F).waterFogColor(0x321025)
						.fogColor(0x4A1734).skyColor(0x7AA0B8)
						.ambientLoopSound(net.minecraft.core.Holder.direct(SoundEvents.AMBIENT_UNDERWATER_LOOP))
						.ambientMoodSound(new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0D))
						.ambientParticle(new AmbientParticleSettings(
								net.minecraft.core.particles.ParticleTypes.CRIMSON_SPORE, 0.006F))
						.build())
				.mobSpawnSettings(spawnBuilder.build()).generationSettings(biomeBuilder.build()).build();
	}

	/**
	 * Mycelial Depths â€” cold, dim, cave-like. A bioluminescent underworld where
	 * calcified hyphae form vast forests of skeletal pillars and sporite crystals
	 * glitter in the dark. Hemolymphopoda swarm through narrow passages.
	 */
	private static Biome mycelialDepths(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		// Deep-dwelling creatures â€” bizarre, predatory, alien
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.erythromycelium_eruptus.get(), 8, 1, 2));
		spawnBuilder.addSpawn(MobCategory.AMBIENT,
				new MobSpawnSettings.SpawnerData(EntityInit.hemolymphopoda.get(), 20, 3, 8));
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.hemojelly.get(), 6, 1, 3));

		BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatureGetter,
				carverGetter);
		biomeBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		// Dense hyphae tendrils â€” this is the hypha-forest biome
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HYPHAE_TENDRIL);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION, PlacedFeatureInit.HUGE_FUNGUS);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_CONSCIOUS_MASS_BLOB);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_CANOPY_MUSHROOMS_DENSE);
		// Glowing crystal patches and sparse vegetation
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.GHOST_PIPES);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.SPORITE_CRYSTAL_CLUSTER);
		// Rare Spore Nexus Tower â€” the signature landmark of this biome
		addFeature(biomeBuilder, GenerationStep.Decoration.SURFACE_STRUCTURES, PlacedFeatureInit.SPORE_NEXUS_TOWER);
		// Ore
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_ORES, PlacedFeatureInit.ORE_HEMATIC_IRON);

		return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.5F).downfall(0.0F)
				.specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x2E4A38).waterFogColor(0x0D1A12)
						.fogColor(0x0A1A10).skyColor(0x051008)
						.ambientLoopSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP)
						.ambientMoodSound(
								new AmbientMoodSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0D))
						.ambientAdditionsSound(
								new AmbientAdditionsSettings(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111D))
						.ambientParticle(new net.minecraft.world.level.biome.AmbientParticleSettings(
								net.minecraft.core.particles.ParticleTypes.WARPED_SPORE, 0.005F))
						.build())
				.mobSpawnSettings(spawnBuilder.build()).generationSettings(biomeBuilder.build()).build();
	}

	/**
	 * Hemorrhagic Plateau â€” hot, exposed, hostile. A blasted highland of dried
	 * hemorrhagic crust and scattered calcified growths. Rare Spore Nexus Towers
	 * erupt from the plateau floor.
	 */
	private static Biome hemorrhagicPlateau(HolderGetter<PlacedFeature> placedFeatureGetter,
			HolderGetter<ConfiguredWorldCarver<?>> carverGetter) {
		MobSpawnSettings.Builder spawnBuilder = new MobSpawnSettings.Builder();
		// Surface predators of the exposed plateau
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.abhorent_thought.get(), 6, 1, 2));
		spawnBuilder.addSpawn(MobCategory.MONSTER,
				new MobSpawnSettings.SpawnerData(EntityInit.blood_drunk_puppeteer.get(), 8, 1, 1));
		spawnBuilder.addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.GHAST, 10, 1, 1));
		// Sparse survivable life
		spawnBuilder.addSpawn(MobCategory.CREATURE,
				new MobSpawnSettings.SpawnerData(EntityInit.venous_strider.get(), 5, 1, 2));

		BiomeGenerationSettings.Builder biomeBuilder = new BiomeGenerationSettings.Builder(placedFeatureGetter,
				carverGetter);
		biomeBuilder.addCarver(GenerationStep.Carving.AIR, Carvers.NETHER_CAVE);
		// Very sparse features â€” this is a barren exposed plateau
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_INFESTED_VENOUS_STONE_BLOB);
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_DECORATION,
				PlacedFeatureInit.PLACED_CANOPY_MUSHROOMS_SPARSE);
		// Barely any vegetation â€” just stinkhorns and scattered calcified forms
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.STINK_HORNS);
		addFeature(biomeBuilder, GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.BLEEDING_HEARTS);
		// Rare Spore Nexus Tower â€” erupts dramatically from the open plateau
		addFeature(biomeBuilder, GenerationStep.Decoration.SURFACE_STRUCTURES, PlacedFeatureInit.SPORE_NEXUS_TOWER);
		// Ore
		addFeature(biomeBuilder, GenerationStep.Decoration.UNDERGROUND_ORES, PlacedFeatureInit.ORE_HEMATIC_IRON);

		return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.5F).downfall(0.0F)
				.specialEffects((new BiomeSpecialEffects.Builder()).waterColor(0x5C0000).waterFogColor(0x2A0000)
						.fogColor(0x3D0A00).skyColor(0x6B0000)
						.grassColorOverride(0x4A0808).foliageColorOverride(0x3D0505)
						.ambientLoopSound(SoundEvents.AMBIENT_NETHER_WASTES_LOOP)
						.ambientMoodSound(
								new AmbientMoodSettings(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0D))
						.ambientAdditionsSound(
								new AmbientAdditionsSettings(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111D))
						.build())
				.mobSpawnSettings(spawnBuilder.build()).generationSettings(biomeBuilder.build()).build();
	}


	@SubscribeEvent
	public static void commonSetup(final FMLCommonSetupEvent event) {

		event.enqueueWork(() -> {

			Regions.register(new ErythrocoralReefRegion(Hemomancy.rloc("erythrocoral_reef_overworld"), 1));
			if (HemoCommonConfig.enableOverworldFungalGardensRegion()) {
				Regions.register(new FungalGardensOverworldRegion(
						Hemomancy.rloc(FungalGardensOverworldRegion.REGION_PATH), 2));
			}

			// Register our surface rules
			SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, Hemomancy.MOD_ID,
					TestSurfaceRuleData.makeRules());
		});
	}
}
