package com.vincenthuto.hemomancy.mixin.core;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.util.Pair;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicBiomes;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Predicate;

@Mixin(value = ServerLevel.class, remap = false)
public abstract class MixinPelagicBiomeSearch {
    @WrapMethod(method = "findClosestBiome3d")
    private Pair<BlockPos, Holder<Biome>> hemomancy$searchDepthLayers(Predicate<Holder<Biome>> predicate,
            BlockPos origin, int radius, int horizontalStep, int verticalStep,
            Operation<Pair<BlockPos, Holder<Biome>>> original) {
        var level = (ServerLevel)(Object)this;
        if (verticalStep > 16 && PelagicWorldgen.context(level.getChunkSource().randomState()) != null) {
            var biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
            for (var layer : new PelagicLayer[]{PelagicLayer.TWILIGHT, PelagicLayer.MIDNIGHT,
                    PelagicLayer.CARRION, PelagicLayer.HYDROTHERMAL}) {
                if (predicate.test(biomes.getHolderOrThrow(PelagicBiomes.key(layer)))) {
                    // Locate's 64-block stride can skip every sample in a roughly 20-block depth band.
                    verticalStep = 16;
                    break;
                }
            }
        }
        return original.call(predicate, origin, radius, horizontalStep, verticalStep);
    }
}
