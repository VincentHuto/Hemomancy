package com.vincenthuto.hemomancy.gametest.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Dev-only: exercise the real noise pipeline instead of the test server's flat world. */
@Mixin(value = GameTestServer.class, remap = false)
public class PelagicTestWorldMixin {
    @Shadow @Final @Mutable private static WorldOptions WORLD_OPTIONS;
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void pelagic$seed(CallbackInfo ci) {
        WORLD_OPTIONS = new WorldOptions(Long.getLong("hemomancy.pelagic.validationSeed", 42L), true, false);
    }
    @ModifyExpressionValue(method = "*", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/level/levelgen/presets/WorldPresets;FLAT:Lnet/minecraft/resources/ResourceKey;"), require = 1)
    private static ResourceKey<WorldPreset> pelagic$preset(ResourceKey<WorldPreset> original) {
        return switch (System.getProperty("hemomancy.pelagic.validationPreset", "normal")) {
            case "large_biomes" -> WorldPresets.LARGE_BIOMES;
            case "amplified" -> WorldPresets.AMPLIFIED;
            default -> WorldPresets.NORMAL;
        };
    }
}
