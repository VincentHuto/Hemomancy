package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinParalysisLocalPlayer {
    @Inject(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/client/player/Input;tick(ZF)V",shift=At.Shift.AFTER),remap=false)
    private void hemomancy$paralysisControls(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        if (!Paralysis.isParalyzed(player)) return;
        player.input.leftImpulse=0; player.input.forwardImpulse=0;
        player.input.up=false; player.input.down=false;
        player.input.left=false; player.input.right=false;
        player.input.jumping=false; player.input.shiftKeyDown=false;
    }
}
