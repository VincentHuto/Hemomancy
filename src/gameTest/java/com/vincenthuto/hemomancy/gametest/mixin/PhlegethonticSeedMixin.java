package com.vincenthuto.hemomancy.gametest.mixin;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.world.level.levelgen.WorldOptions;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The vanilla test server otherwise hardcodes seed zero and disables structures. Dev source set only. */
@Mixin(GameTestServer.class)
public class PhlegethonticSeedMixin {
    @Shadow @Final @Mutable private static WorldOptions WORLD_OPTIONS;
    @Inject(method="<clinit>",at=@At("TAIL"))
    private static void phlegethontic$seed(CallbackInfo ci) {
        WORLD_OPTIONS=new WorldOptions(Long.getLong("hemomancy.phlegethontic.validationSeed",42L),true,false);
    }
}
