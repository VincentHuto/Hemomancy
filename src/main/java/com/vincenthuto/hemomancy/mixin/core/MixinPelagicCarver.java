package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicAquifer;
import net.minecraft.core.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.*;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.*;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.function.Function;

@Mixin(value = WorldCarver.class, remap = false)
public abstract class MixinPelagicCarver {
    @Inject(method = "carveBlock", at = @At("HEAD"), cancellable = true)
    private void hemomancy$sealFloor(CarvingContext context, CarverConfiguration config, ChunkAccess chunk,
            Function<BlockPos, Holder<Biome>> biome, CarvingMask mask, BlockPos.MutableBlockPos pos,
            BlockPos.MutableBlockPos check, Aquifer aquifer, MutableBoolean reached, CallbackInfoReturnable<Boolean> cir) {
        if (aquifer instanceof PelagicAquifer ocean && ocean.protectsSeabed(pos.getX(), pos.getY(), pos.getZ())) cir.setReturnValue(false);
    }
}
