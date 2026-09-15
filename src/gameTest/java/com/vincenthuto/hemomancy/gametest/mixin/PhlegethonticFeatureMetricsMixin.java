package com.vincenthuto.hemomancy.gametest.mixin;

import com.vincenthuto.hemomancy.common.worldgen.feature.PhlegethonticWorldgenFeature;
import com.vincenthuto.hemomancy.gametest.PhlegethonticWorldValidation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=PhlegethonticWorldgenFeature.class,remap=false)
public class PhlegethonticFeatureMetricsMixin {
    @Inject(method="place",at=@At("HEAD"))
    private void phlegethontic$begin(FeaturePlaceContext<NoneFeatureConfiguration> context,CallbackInfoReturnable<Boolean> cir) {
        PhlegethonticWorldValidation.begin(new ChunkPos(context.origin()));
    }
    @Inject(method="place",at=@At("RETURN"))
    private void phlegethontic$end(FeaturePlaceContext<NoneFeatureConfiguration> context,CallbackInfoReturnable<Boolean> cir) {
        PhlegethonticWorldValidation.end(cir.getReturnValue());
    }
    @Inject(method="finishVein",at=@At("HEAD"))
    private static void phlegethontic$veinBegin(net.minecraft.world.level.WorldGenLevel level,
            net.minecraft.world.level.chunk.ChunkAccess chunk,CallbackInfoReturnable<Integer> cir) {
        PhlegethonticWorldValidation.begin(chunk.getPos());
    }
    @Inject(method="finishVein",at=@At("RETURN"))
    private static void phlegethontic$veinEnd(net.minecraft.world.level.WorldGenLevel level,
            net.minecraft.world.level.chunk.ChunkAccess chunk,CallbackInfoReturnable<Integer> cir) {
        PhlegethonticWorldValidation.end(cir.getReturnValue()>0);
    }
}
