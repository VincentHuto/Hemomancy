package com.vincenthuto.hemomancy.common.worldgen.terrablender;


import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;

public class TestSurfaceRuleData
{
    private static final SurfaceRules.RuleSource DIRT = makeStateRule(Blocks.DIRT);
    private static final SurfaceRules.RuleSource GRASS_BLOCK = makeStateRule(Blocks.GRASS_BLOCK);
    private static final SurfaceRules.RuleSource erythrocytic_mycelium = makeStateRule(BlockInit.erythrocytic_mycelium.get());
    private static final SurfaceRules.RuleSource erythrocytic_dirt = makeStateRule(BlockInit.erythrocytic_dirt.get());
    private static final SurfaceRules.RuleSource venous_stone = makeStateRule(BlockInit.venous_stone.get());

    public static SurfaceRules.RuleSource makeRules()
    {
        SurfaceRules.ConditionSource isAtOrAboveWaterLevel = SurfaceRules.waterBlockCheck(-1, 0);
        SurfaceRules.RuleSource grassSurface = SurfaceRules.sequence(SurfaceRules.ifTrue(isAtOrAboveWaterLevel, GRASS_BLOCK), DIRT);

        return SurfaceRules.sequence(
            SurfaceRules.ifTrue(SurfaceRules.isBiome(BiomeInit.FUNGAL_GARDENS),
                SurfaceRules.sequence(
                    SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,
                        SurfaceRules.ifTrue(isAtOrAboveWaterLevel, erythrocytic_mycelium)),
                    SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, erythrocytic_dirt),
                    SurfaceRules.ifTrue(SurfaceRules.VERY_DEEP_UNDER_FLOOR, venous_stone)
                )
            ),

            // Default to a grass and dirt surface
            SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, grassSurface)
        );
    }

    private static SurfaceRules.RuleSource makeStateRule(Block block)
    {
        return SurfaceRules.state(block.defaultBlockState());
    }
}