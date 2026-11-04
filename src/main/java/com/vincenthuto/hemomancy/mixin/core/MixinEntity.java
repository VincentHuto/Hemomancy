package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(Entity.class)
public class MixinEntity {
	@Inject(method = "move", at = @At("HEAD"), cancellable = true, remap = false)
	private void hemomancy$applySilentSlippingNoClip(MoverType type, Vec3 movement, CallbackInfo ci) {
		if ((Object) this instanceof Player player) {
			SilentArchonArmorAbilityHandler.applySilentSlippingNoClip(player);
			com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager.applyMovement(player);
			if (com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager.isTraveling(player)) ci.cancel();
		}
	}
}
