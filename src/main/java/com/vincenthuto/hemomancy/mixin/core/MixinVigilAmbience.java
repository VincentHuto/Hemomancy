package com.vincenthuto.hemomancy.mixin.core;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=AbstractSoundInstance.class,remap=false)
public abstract class MixinVigilAmbience {
    @Inject(method="getVolume",at=@At("RETURN"),cancellable=true)
    private void hemomancy$environment(CallbackInfoReturnable<Float> callback) {
        if(((AbstractSoundInstance)(Object)this).getSource()==SoundSource.AMBIENT)
            callback.setReturnValue(callback.getReturnValue()*com.vincenthuto.hemomancy.client.sound.AntecedentClientEffects.ambience());
    }
}
