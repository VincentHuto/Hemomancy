package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.function.Supplier;

@Mixin(value = NoiseBasedChunkGenerator.class, remap = false)
public interface TroupeNoiseGeneratorAccessor {
    @Accessor("globalFluidPicker") Supplier<Aquifer.FluidPicker> hemomancy$fluidPicker();
}
