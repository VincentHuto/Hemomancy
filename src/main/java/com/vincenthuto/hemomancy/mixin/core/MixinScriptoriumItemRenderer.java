package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class MixinScriptoriumItemRenderer {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, remap = false)
    private MultiBufferSource hemomancy$crimsonScriptoriumFoil(MultiBufferSource original, ItemStack stack) {
        if (!stack.has(DataComponentInit.SCRIPTORIUM_PROVENANCE.get())) return original;
        return type -> original.getBuffer(RenderTypeInit.scriptoriumGlint(type));
    }

}
