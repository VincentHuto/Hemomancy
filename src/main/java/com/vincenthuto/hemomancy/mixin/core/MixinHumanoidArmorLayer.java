package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.client.morphling.MorphlingPlayerPartVisibility;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class MixinHumanoidArmorLayer {

    @Inject(method = "setPartVisibility", at = @At("RETURN"), remap = false)
    private void hemomancy$hideMorphlingReplacementArmorParts(HumanoidModel<?> model,
            EquipmentSlot slot, CallbackInfo ci) {
        MorphlingPlayerPartVisibility.applyToArmorModel(model, slot);
    }
}
