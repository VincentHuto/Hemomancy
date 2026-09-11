package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Slime.class)
public abstract class MixinParalysisSlimeAttack {
    @Inject(method="dealDamage",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$paralysisContact(LivingEntity victim,CallbackInfo ci) {
        if (Paralysis.isParalyzed((LivingEntity)(Object)this)) ci.cancel();
    }
}
