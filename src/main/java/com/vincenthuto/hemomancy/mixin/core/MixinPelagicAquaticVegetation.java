package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicWorldgen;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

/** Vanilla vegetation chooses its floor inside the feature, after its biome placement filter. */
@Mixin(value = {KelpFeature.class, SeagrassFeature.class, SeaPickleFeature.class}, remap = false)
public abstract class MixinPelagicAquaticVegetation {
    @WrapOperation(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/WorldGenLevel;getHeight(Lnet/minecraft/world/level/levelgen/Heightmap$Types;II)I"))
    private int hemomancy$photicShelf(WorldGenLevel level, Heightmap.Types type, int x, int z, Operation<Integer> original) {
        int floor = original.call(level, type, x, z);
        var pelagic = PelagicWorldgen.context(level.getLevel().getChunkSource().randomState());
        if (pelagic != null && floor < 13 && pelagic.sample(x, z).column().influence() > .99)
            return level.getMaxBuildHeight(); // No water/support there: vanilla safely declines this individual plant.
        return floor;
    }
}
