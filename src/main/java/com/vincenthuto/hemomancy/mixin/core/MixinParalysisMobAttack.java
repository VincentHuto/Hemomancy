package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({LivingEntity.class,Mob.class})
public abstract class MixinParalysisMobAttack {
    @Inject(method="doHurtTarget",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$paralysisAttack(Entity target,CallbackInfoReturnable<Boolean> cir) {
        if (Paralysis.blocksActions((LivingEntity)(Object)this)) cir.setReturnValue(false);
    }
}
