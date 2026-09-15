package com.vincenthuto.hemomancy.common.worldgen.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record EscharianOvergrowthConfiguration(int minPatchBlocks, int maxPatchBlocks,
        int ichorRadius, int anchorSearchBudget, int groundPileChance) implements FeatureConfiguration {
    public static final EscharianOvergrowthConfiguration DEFAULT = new EscharianOvergrowthConfiguration(10, 30, 10, 256, 8);
    public static final Codec<EscharianOvergrowthConfiguration> CODEC = RecordCodecBuilder.<EscharianOvergrowthConfiguration>create(instance -> instance.group(
            Codec.intRange(10,30).optionalFieldOf("min_patch_blocks",10).forGetter(EscharianOvergrowthConfiguration::minPatchBlocks),
            Codec.intRange(10,30).optionalFieldOf("max_patch_blocks",30).forGetter(EscharianOvergrowthConfiguration::maxPatchBlocks),
            Codec.intRange(1,10).optionalFieldOf("ichor_radius",10).forGetter(EscharianOvergrowthConfiguration::ichorRadius),
            Codec.intRange(1,1024).optionalFieldOf("anchor_search_budget",256).forGetter(EscharianOvergrowthConfiguration::anchorSearchBudget),
            Codec.intRange(1,64).optionalFieldOf("ground_pile_chance",8).forGetter(EscharianOvergrowthConfiguration::groundPileChance)
    ).apply(instance, EscharianOvergrowthConfiguration::new)).validate(config -> config.minPatchBlocks <= config.maxPatchBlocks
            ? com.mojang.serialization.DataResult.success(config)
            : com.mojang.serialization.DataResult.error(() -> "min_patch_blocks must not exceed max_patch_blocks"));
}
