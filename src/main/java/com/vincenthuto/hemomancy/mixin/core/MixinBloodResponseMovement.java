package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinBloodResponseMovement {
    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$cryoprotection(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof LivingEntity entity && entity.hasEffect(EffectInit.cryoprotection))
            cir.setReturnValue(false);
    }
    @ModifyVariable(method = "makeStuckInBlock", at = @At("HEAD"), argsOnly = true, remap = false)
    private Vec3 hemomancy$bloodMobility(Vec3 original, BlockState state, Vec3 multiplier) {
        if ((Object)this instanceof LivingEntity entity) {
            if (state.is(Blocks.COBWEB) && entity.hasEffect(EffectInit.web_mobility))
                return new Vec3(0.8, 0.8, 0.8);
            if (state.is(Blocks.POWDER_SNOW) && entity.hasEffect(EffectInit.cryoprotection))
                return new Vec3(1, 1, 1);
        }
        return original;
    }
}

