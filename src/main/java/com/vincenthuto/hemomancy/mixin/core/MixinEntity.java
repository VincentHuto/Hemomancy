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
	@Inject(method = "move", at = @At("HEAD"), remap = false)
	private void hemomancy$applySilentSlippingNoClip(MoverType type, Vec3 movement, CallbackInfo ci) {
		System.out.println("MixinEntity.move called for entity: " + ((Entity) (Object) this).getName().getString() + " of type: " + ((Entity) (Object) this).getType().getDescriptionId());
		if ((Object) this instanceof Player player) {
			System.out.println("Checking if we should apply Silent Slipping No Clip for player: " + player.getName().getString());
			SilentArchonArmorAbilityHandler.applySilentSlippingNoClip(player);
		}
	}
}
