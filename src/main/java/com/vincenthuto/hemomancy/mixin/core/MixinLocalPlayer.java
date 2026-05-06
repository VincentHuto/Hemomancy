package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.mixin.util.ClientMixinHooks;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
	  @Unique
	  private boolean hemomancy$flag = false;

	  @Inject(at = @At(value = "INVOKE_ASSIGN", target = "net/minecraft/world/entity/LivingEntity.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), method = "aiStep", remap = false)
	  public void hemomancy$checkFlight(CallbackInfo cb) {
		  
	    this.hemomancy$flag = ClientMixinHooks.checkFlight();
	  }

	  @Redirect(
			at = @At(value = "INVOKE", target = "net/minecraft/world/entity/LivingEntity.getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"),
			method = "aiStep",
			remap = false)
	  public ItemStack hemomancy$affixEmptyStack(LivingEntity entity, EquipmentSlot slot) {
		ItemStack stack = entity.getItemBySlot(slot);
		return this.hemomancy$flag ? stack : ItemStack.EMPTY;
	  }
	}
