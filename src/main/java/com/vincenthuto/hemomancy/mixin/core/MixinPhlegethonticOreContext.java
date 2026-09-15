package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticOreReservations;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({OreFeature.class,ScatteredOreFeature.class})
public class MixinPhlegethonticOreContext {
    @Inject(method="place",at=@At("HEAD"),remap=false)
    private void hemomancy$beginOre(FeaturePlaceContext<OreConfiguration> context,CallbackInfoReturnable<Boolean> cir) {
        PhlegethonticOreReservations.begin(context.level());
    }
    @Inject(method="place",at=@At("RETURN"),remap=false)
    private void hemomancy$endOre(FeaturePlaceContext<OreConfiguration> context,CallbackInfoReturnable<Boolean> cir) {
        PhlegethonticOreReservations.end();
    }
}
