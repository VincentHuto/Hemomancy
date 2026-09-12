package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.damage.SchoolAwareness;
import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinSchoolAwareness {
    @Inject(method = "hasLineOfSight", at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$awarenessSight(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Mob observer && target instanceof LivingEntity living
                && !SchoolAwareness.canPerceive(observer, living)) cir.setReturnValue(false);
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$awarenessAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof Mob observer && !SchoolAwareness.canPerceive(observer, target)) cir.setReturnValue(false);
    }

    @Inject(method = "getVisibilityPercent", at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$closeDefense(Entity observer, CallbackInfoReturnable<Double> cir) {
        LivingEntity target = (LivingEntity)(Object)this;
        if (observer instanceof Mob mob && SchoolStates.has(target, SchoolState.VEILED)
                && SchoolAwareness.canPerceive(mob, target)) cir.setReturnValue(1.0);
    }

    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), cancellable = true, remap = false)
    private void hemomancy$exposedOutline(CallbackInfoReturnable<Boolean> cir) {
        if (SchoolStates.has((LivingEntity)(Object)this, SchoolState.ILLUMINATED)) cir.setReturnValue(true);
    }
}
