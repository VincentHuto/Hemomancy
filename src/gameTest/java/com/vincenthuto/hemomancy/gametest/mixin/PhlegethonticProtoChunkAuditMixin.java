package com.vincenthuto.hemomancy.gametest.mixin;

import com.vincenthuto.hemomancy.gametest.PhlegethonticWorldValidation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ProtoChunk.class)
public class PhlegethonticProtoChunkAuditMixin {
    @Inject(method="setBlockState",at=@At("HEAD"))
    private void phlegethontic$write(BlockPos pos,BlockState state,boolean moving,CallbackInfoReturnable<BlockState> cir) {
        PhlegethonticWorldValidation.checkWrite(pos);
    }
}
