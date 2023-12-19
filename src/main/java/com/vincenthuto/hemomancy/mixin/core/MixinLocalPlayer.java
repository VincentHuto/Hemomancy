package com.vincenthuto.hemomancy.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {

	@SuppressWarnings("resource")
	@Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/client/player/LocalPlayer.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), method = "aiStep")
	public void hemomancy$checkFlight(CallbackInfo cb) {
		LocalPlayer playerEntity = Minecraft.getInstance().player;
		boolean canFly;
		if (!playerEntity.onGround() && !playerEntity.isFallFlying() && !playerEntity.isInWater()
				&& !playerEntity.hasEffect(MobEffects.LEVITATION) && AttributeInit.canFly(playerEntity)) {
			playerEntity.startFallFlying();
			canFly = true;
		} else {
			canFly = false;
		}
		if (playerEntity != null && canFly) {
			PacketHandler.sendClientElytraPacket();
		}
	}

	@ModifyVariable(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/client/player/LocalPlayer.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), method = "aiStep")
	public ItemStack hemomancy$affixEmptyStack(ItemStack stack) {
		return ItemStack.EMPTY;
	}
}
