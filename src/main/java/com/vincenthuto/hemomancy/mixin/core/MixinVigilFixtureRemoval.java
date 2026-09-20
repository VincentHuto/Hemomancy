package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.antecedent.VigilSites;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=LevelChunk.class,remap=false)
public abstract class MixinVigilFixtureRemoval {
    @Inject(method="setBlockState",at=@At("HEAD"))
    private void hemomancy$retireFixture(BlockPos pos,BlockState state,boolean moving,CallbackInfoReturnable<BlockState> callback) {
        var chunk=(LevelChunk)(Object)this;
        if(!chunk.getLevel().isClientSide && VigilSites.fixture(chunk.getLevel(),pos)!=null)
            VigilSites.changing(chunk.getLevel(),pos,chunk.getBlockState(pos),state);
    }
}
