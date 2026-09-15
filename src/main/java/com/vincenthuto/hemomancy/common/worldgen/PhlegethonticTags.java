package com.vincenthuto.hemomancy.common.worldgen;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class PhlegethonticTags {
    public static final TagKey<net.minecraft.world.entity.EntityType<?>> RIVER_GUARDIANS=TagKey.create(
            Registries.ENTITY_TYPE,Hemomancy.rloc("phlegethontic_river_guardians"));
    public static final TagKey<Fluid> ICHOR=TagKey.create(Registries.FLUID,Hemomancy.rloc("phlegethontic_ichor"));
    public static final TagKey<Block> SHORE=block("phlegethontic_shore_blocks");
    public static final TagKey<Block> OVERGROWTH_SUPPORT=block("escharian_overgrowth_supports");
    public static final TagKey<Block> VEIN_REPLACEABLE=block("phlegethontic_vein_replaceable");
    public static final TagKey<Block> BASIN_REPLACEABLE=block("phlegethontic_basin_terrain_replaceable");
    private static TagKey<Block> block(String path) { return TagKey.create(Registries.BLOCK,Hemomancy.rloc(path)); }
    private PhlegethonticTags() {}
}
