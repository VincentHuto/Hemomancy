package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.PlacedFeatureInit;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public final class PelagicBiomes {
    private PelagicBiomes() {}
    public static ResourceKey<Biome> key(PelagicLayer layer) { return ResourceKey.create(Registries.BIOME, Hemomancy.rloc(layer.id)); }

    public static void bootstrap(BootstrapContext<Biome> context, HolderGetter<PlacedFeature> placed,
                                 HolderGetter<ConfiguredWorldCarver<?>> carvers) {
        for (var layer : PelagicLayer.values()) {
            if (layer == PelagicLayer.REEF) continue;
            var generation = new BiomeGenerationSettings.Builder(placed, carvers);
            generation.addFeature(GenerationStep.Decoration.LOCAL_MODIFICATIONS, PlacedFeatureInit.PELAGIC_SEABED);
            generation.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, PlacedFeatureInit.PELAGIC_HABITAT);
            int water = switch (layer) {
                case SHORE -> 0x386D76; case OPEN -> 0x28557A; case TWILIGHT -> 0x294552;
                case MIDNIGHT -> 0x192E42; case CARRION -> 0x303A3E; default -> 0x263C3B;
            };
            int fog = switch (layer) {
                case SHORE -> 0x153A40; case OPEN -> 0x102B46; case TWILIGHT -> 0x0C202B;
                case MIDNIGHT -> 0x070E1B; case CARRION -> 0x11191C; default -> 0x101F1C;
            };
            var spawns = new MobSpawnSettings.Builder();
            for (var entry : PelagicPopulation.entries(layer)) {
                var type = net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(net.minecraft.resources.ResourceLocation.parse(entry.entity()));
                var category = java.util.Arrays.stream(net.minecraft.world.entity.MobCategory.values())
                        .filter(c -> c.getName().equals(entry.category())).findFirst().orElseThrow();
                spawns.addSpawn(category,
                        new MobSpawnSettings.SpawnerData(type, entry.weight(), entry.min(), entry.max()));
            }
            context.register(key(layer), new Biome.BiomeBuilder().hasPrecipitation(true).temperature(.65F).downfall(.7F)
                    .specialEffects(new BiomeSpecialEffects.Builder().waterColor(water).waterFogColor(fog)
                            .fogColor(0xC0D8FF).skyColor(0x83A1C4)
                            .ambientLoopSound(Holder.direct(SoundEvents.AMBIENT_UNDERWATER_LOOP)).build())
                    .mobSpawnSettings(spawns.build()).generationSettings(generation.build()).build());
        }
    }
}
