package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.antecedent.VigilSites;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=SculkShriekerBlockEntity.class,remap=false)
public abstract class MixinVigilShrieker {
    @Inject(method="tryShriek",at=@At("HEAD"),cancellable=true)
    private void hemomancy$failedReflex(ServerLevel level,ServerPlayer player,CallbackInfo callback) {
        var site=VigilSites.fixture(level,((SculkShriekerBlockEntity)(Object)this).getBlockPos());
        if(site!=null) site.failShriek();
        if(VigilSites.authored(level,((SculkShriekerBlockEntity)(Object)this).getBlockPos()))callback.cancel();
    }
    @Inject(method="tryRespond",at=@At("HEAD"),cancellable=true)
    private void hemomancy$noAnswer(ServerLevel level,CallbackInfo callback) {
        if(VigilSites.authored(level,((SculkShriekerBlockEntity)(Object)this).getBlockPos())) callback.cancel();
    }
}
