package com.vincenthuto.hemomancy.mixin.core;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.*;
import net.minecraft.core.*;
import net.minecraft.world.level.biome.*;
import org.spongepowered.asm.mixin.*;

@Mixin(value = MultiNoiseBiomeSource.class, remap = false)
public abstract class MixinPelagicBiomeSource implements PelagicBiomeSourceAccess {
    @Unique private PelagicContext hemomancy$context;
    @Override public void hemomancy$setPelagic(PelagicContext context) { hemomancy$context = context; }
    @WrapMethod(method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;")
    private Holder<Biome> hemomancy$layers(int x, int y, int z, Climate.Sampler sampler, Operation<Holder<Biome>> original) {
        var biome = original.call(x, y, z, sampler);
        return hemomancy$context == null ? biome : hemomancy$context.biome(QuartPos.toBlock(x), QuartPos.toBlock(y), QuartPos.toBlock(z), biome);
    }
}
