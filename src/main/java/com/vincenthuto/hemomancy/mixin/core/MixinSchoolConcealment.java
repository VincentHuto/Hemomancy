package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.damage.SchoolState;
import com.vincenthuto.hemomancy.common.damage.SchoolStates;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinSchoolConcealment {
    @Inject(method = "isInvisible", at = @At("RETURN"), cancellable = true, remap = false)
    private void hemomancy$concealment(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof LivingEntity living) {
            if (SchoolStates.has(living, SchoolState.ILLUMINATED)) cir.setReturnValue(false);
            else if (SchoolStates.has(living, SchoolState.VEILED)) cir.setReturnValue(true);
        }
    }
}

