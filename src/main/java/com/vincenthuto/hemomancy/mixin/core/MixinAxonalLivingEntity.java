package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinAxonalLivingEntity {
    @Inject(method={"isPickable", "isPushable", "canBeSeenAsEnemy"}, at=@At("HEAD"), cancellable=true, remap=false)
    private void hemomancy$signalHasNoPhysicalBody(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Player player && AxonalTransductionManager.isTraveling(player))
            cir.setReturnValue(false);
    }
}
