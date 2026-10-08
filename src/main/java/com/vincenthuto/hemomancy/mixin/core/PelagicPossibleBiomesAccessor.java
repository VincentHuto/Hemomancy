package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(value = BiomeSource.class, remap = false)
public interface PelagicPossibleBiomesAccessor {
    @Mutable @Accessor("possibleBiomes")
    void hemomancy$setPossibleBiomes(Supplier<Set<Holder<Biome>>> biomes);
}
