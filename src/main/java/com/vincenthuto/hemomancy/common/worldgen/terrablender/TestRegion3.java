package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import java.util.function.Consumer;

import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.TerraBiomeInit;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import terrablender.api.Region;
import terrablender.api.RegionType;

public class TestRegion3 extends Region
{
    public TestRegion3(ResourceLocation name, int weight)
    {
        super(name, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper)
    {
        this.addModifiedVanillaOverworldBiomes(mapper, builder -> {
            // Simple example:
            // Replace the Vanilla desert with our hot_red biome
            builder.replaceBiome(Biomes.JUNGLE, BiomeInit.FUNGAL_GARDENS);
            builder.replaceBiome(Biomes.MUSHROOM_FIELDS, BiomeInit.FUNGAL_GARDENS);
            builder.replaceBiome(Biomes.MANGROVE_SWAMP, BiomeInit.FUNGAL_GARDENS);
            builder.replaceBiome(Biomes.SWAMP, BiomeInit.FUNGAL_GARDENS);
        });
    }
}