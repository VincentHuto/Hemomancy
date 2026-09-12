package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.damage.SchoolDamage;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinSchoolDamage {
    @ModifyVariable(method = "hurt", at = @At("HEAD"), argsOnly = true, remap = false)
    private DamageSource hemomancy$schoolSource(DamageSource source) {
        return SchoolDamage.wrap((LivingEntity)(Object)this, source);
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$commandedEffect(MobEffectInstance effect, net.minecraft.world.entity.Entity source,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (!effect.getEffect().value().isBeneficial()
                && !com.vincenthuto.hemomancy.common.manipulation.HematicCommandManager.mayAffect(source, (LivingEntity)(Object)this))
            cir.setReturnValue(false);
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("RETURN"), remap = false)
    private void hemomancy$captureRetort(MobEffectInstance effect, net.minecraft.world.entity.Entity source,
            org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && effect.is(com.vincenthuto.hemomancy.common.init.EffectInit.iron_retort)
                && !((LivingEntity)(Object)this).level().isClientSide) SchoolDamage.captureRetort((LivingEntity)(Object)this);
    }

    @Inject(method = "onEffectRemoved", at = @At("TAIL"), remap = false)
    private void hemomancy$schoolCleanup(MobEffectInstance effect, CallbackInfo ci) {
        SchoolStates.removed((LivingEntity)(Object)this, effect);
        if (effect.is(com.vincenthuto.hemomancy.common.init.EffectInit.iron_retort)) SchoolDamage.clearRetort((LivingEntity)(Object)this);
    }
}
