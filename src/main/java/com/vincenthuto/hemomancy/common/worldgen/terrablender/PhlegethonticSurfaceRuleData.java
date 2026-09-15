package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import com.vincenthuto.hemomancy.common.init.BiomeInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.SurfaceRules;

public final class PhlegethonticSurfaceRuleData {
    private PhlegethonticSurfaceRuleData() {}
    public static SurfaceRules.RuleSource makeRules() {
        return SurfaceRules.ifTrue(SurfaceRules.isBiome(BiomeInit.PHLEGETHONTIC_BASIN),SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.ON_CEILING,SurfaceRules.state(Blocks.BASALT.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR,SurfaceRules.state(BlockInit.venous_stone.get().defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR,SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.noiseCondition(Noises.SURFACE,-.2,.2),
                                SurfaceRules.state(BlockInit.venous_stone.get().defaultBlockState())),
                        SurfaceRules.state(Blocks.BLACKSTONE.defaultBlockState())))));
    }
}
