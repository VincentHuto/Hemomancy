package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinScriptoriumItemStack {
    @Inject(method = "hasFoil", at = @At("HEAD"), cancellable = true, remap = false)
    private void hemomancy$scriptoriumFoil(CallbackInfoReturnable<Boolean> result) {
        if (((ItemStack) (Object) this).has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())) result.setReturnValue(true);
    }
}
