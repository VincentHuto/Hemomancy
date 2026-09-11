package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.manipulation.ductilis.Paralysis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinParalysisLivingEntity {
    @Inject(method="isImmobile",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$paralysisInput(CallbackInfoReturnable<Boolean> cir) {
        if (Paralysis.isParalyzed((LivingEntity)(Object)this)) cir.setReturnValue(true);
    }
    @ModifyVariable(method="travel",at=@At("HEAD"),argsOnly=true,remap=false)
    private Vec3 hemomancy$paralysisTravel(Vec3 input) {
        return Paralysis.isParalyzed((LivingEntity)(Object)this) ? Vec3.ZERO : input;
    }
    @Inject(method="jumpFromGround",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$paralysisJump(CallbackInfo ci) {
        if (Paralysis.isParalyzed((LivingEntity)(Object)this)) ci.cancel();
    }
    @Inject(method="startUsingItem",at=@At("HEAD"),cancellable=true,remap=false)
    private void hemomancy$paralysisUse(InteractionHand hand,CallbackInfo ci) {
        if (Paralysis.isParalyzed((LivingEntity)(Object)this)) ci.cancel();
    }
    @Inject(method="onEffectRemoved",at=@At("TAIL"),remap=false)
    private void hemomancy$paralysisRecovery(MobEffectInstance effect,CallbackInfo ci) {
        Paralysis.removed((LivingEntity)(Object)this,effect);
    }
}
