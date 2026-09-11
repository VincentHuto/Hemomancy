package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinParalysisPlayer {
    @Inject(method="attack",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$paralysisAttack(Entity target, CallbackInfo ci) {
        if (Paralysis.isParalyzed((Player)(Object)this)) ci.cancel();
    }
}
