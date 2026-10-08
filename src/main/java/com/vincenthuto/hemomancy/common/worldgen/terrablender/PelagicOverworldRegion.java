package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.*;
import net.minecraft.world.level.biome.*;
import terrablender.api.*;
import java.util.function.Consumer;

public final class PelagicOverworldRegion extends Region {
    public PelagicOverworldRegion(ResourceLocation name, int weight) { super(name, RegionType.OVERWORLD, weight); }
    @Override public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        var builder = new ModifiedVanillaOverworldBuilder();
        for (var key : registry.registryKeySet()) builder.replaceBiome(key, DEFERRED_PLACEHOLDER);
        builder.replaceBiome(Biomes.BEACH, PelagicBiomes.key(PelagicLayer.SHORE));
        builder.replaceBiome(Biomes.STONY_SHORE, PelagicBiomes.key(PelagicLayer.SHORE));
        builder.replaceBiome(Biomes.LUKEWARM_OCEAN, PelagicBiomes.key(PelagicLayer.REEF));
        builder.replaceBiome(Biomes.WARM_OCEAN, PelagicBiomes.key(PelagicLayer.REEF));
        for (var ocean : java.util.List.of(Biomes.DEEP_OCEAN, Biomes.DEEP_COLD_OCEAN, Biomes.DEEP_LUKEWARM_OCEAN))
            builder.replaceBiome(ocean, PelagicBiomes.key(PelagicLayer.OPEN));
        builder.build().forEach(mapper);
    }
}
