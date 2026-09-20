package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.antecedent.AntecedentEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=SculkSensorBlock.class,remap=false)
public abstract class MixinListeningScar {
    @Inject(method="activate",at=@At("TAIL"))
    private void hemomancy$hearing(Entity source,Level level,BlockPos pos,BlockState state,int power,int frequency,CallbackInfo callback) {
        if(level instanceof ServerLevel server) AntecedentEvents.accepted(server,pos);
    }
}
