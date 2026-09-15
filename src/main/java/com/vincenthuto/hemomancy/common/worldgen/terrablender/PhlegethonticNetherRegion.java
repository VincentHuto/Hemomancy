package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.*;
import terrablender.api.*;
import java.util.function.Consumer;

public final class PhlegethonticNetherRegion extends Region {
    public PhlegethonticNetherRegion(ResourceLocation name,int weight) { super(name,RegionType.NETHER,weight); }
    @Override public void addBiomes(Registry<Biome> registry,Consumer<Pair<Climate.ParameterPoint,ResourceKey<Biome>>> mapper) {
        point(mapper,0,0,0,DEFERRED_PLACEHOLDER);
        point(mapper,0,-.5F,0,DEFERRED_PLACEHOLDER);
        point(mapper,.4F,0,0,DEFERRED_PLACEHOLDER);
        point(mapper,0,.5F,.375F,DEFERRED_PLACEHOLDER);
        point(mapper,-.5F,0,.175F,BiomeInit.PHLEGETHONTIC_BASIN);
    }
    private void point(Consumer<Pair<Climate.ParameterPoint,ResourceKey<Biome>>> mapper,float temperature,float humidity,
                       float offset,ResourceKey<Biome> biome) {
        addBiome(mapper,Climate.parameters(temperature,humidity,0,0,0,0,offset),biome);
    }
}
