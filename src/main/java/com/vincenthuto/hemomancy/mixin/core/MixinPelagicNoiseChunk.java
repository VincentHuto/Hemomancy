package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NoiseChunk.class, remap = false)
public abstract class MixinPelagicNoiseChunk {
    @Shadow @Final @Mutable private Aquifer aquifer;
    @Inject(method = "<init>", at = @At("RETURN"))
    private void hemomancy$oceanWater(int cells, RandomState state, int x, int z, NoiseSettings noise,
            DensityFunctions.BeardifierOrMarker beardifier, NoiseGeneratorSettings settings, Aquifer.FluidPicker picker,
            Blender blender, CallbackInfo ci) {
        var context = PelagicWorldgen.context(state);
        if (context != null) aquifer = new PelagicAquifer(aquifer, context);
    }
}
