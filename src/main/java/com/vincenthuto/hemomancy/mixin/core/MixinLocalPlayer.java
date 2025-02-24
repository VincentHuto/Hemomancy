package com.vincenthuto.hemomancy.mixin.core;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.vincenthuto.hemomancy.mixin.util.ClientMixinHooks;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
	  @Unique
	  private boolean hemomancy$flag = false;

	  @Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/client/player/LocalPlayer.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), method = "aiStep")
	  public void hemomancy$checkFlight(CallbackInfo cb) {
		  
	    this.hemomancy$flag = ClientMixinHooks.checkFlight();
	  }

	  @ModifyVariable(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/client/player/LocalPlayer.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), method = "aiStep")
	  public ItemStack hemomancy$affixEmptyStack(ItemStack stack) {
	    return this.hemomancy$flag ? stack : ItemStack.EMPTY;
	  }
	}
