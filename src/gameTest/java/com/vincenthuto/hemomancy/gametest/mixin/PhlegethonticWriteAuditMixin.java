package com.vincenthuto.hemomancy.gametest.mixin;

import com.vincenthuto.hemomancy.gametest.PhlegethonticWorldValidation;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public class PhlegethonticWriteAuditMixin {
    @Inject(method="setBlock",at=@At("HEAD"))
    private void phlegethontic$write(BlockPos pos,BlockState state,int flags,int depth,CallbackInfoReturnable<Boolean> cir) {
        PhlegethonticWorldValidation.checkWrite(pos);
    }
}
