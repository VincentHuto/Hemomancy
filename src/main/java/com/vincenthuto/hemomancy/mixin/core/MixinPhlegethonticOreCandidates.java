package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticOreReservations;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Function;

@Mixin(OreFeature.class)
public class MixinPhlegethonticOreCandidates {
    @Inject(method="canPlaceOre",at=@At("HEAD"),remap=false)
    private static void hemomancy$reserveOre(BlockState existing,Function<BlockPos,BlockState> lookup,RandomSource random,
            OreConfiguration config,OreConfiguration.TargetBlockState target,BlockPos.MutableBlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {
        PhlegethonticOreReservations.reserve(pos,target.state);
    }
}
