package com.vincenthuto.hemomancy.common.worldgen;

import java.util.function.BiPredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

public interface SimpleBlockPredicate extends BiPredicate<WorldGenLevel, BlockPos>
{
    // Wrapper
    default boolean matches(WorldGenLevel world, BlockPos pos) {
        return this.test(world, pos);
    }
}