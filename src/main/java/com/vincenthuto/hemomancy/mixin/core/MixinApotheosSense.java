package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.client.render.world.ApotheosSense;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Outlines what an Apotheos senses through the Gardens' ground, on that player's client only. */
@Mixin(value = Minecraft.class, remap = false)
public abstract class MixinApotheosSense {
	@Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
	private void hemomancy$apotheosSense(Entity entity, CallbackInfoReturnable<Boolean> callback) {
		if (!callback.getReturnValueZ() && ApotheosSense.sees(entity)) callback.setReturnValue(true);
	}
}
