package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.armor.ability.SilentArchonArmorAbilityHandler;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingBaghnakhItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchBreathRules;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingTorchItem;
import com.vincenthuto.hemomancy.mixin.util.ClientMixinHooks;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer {
	  @Unique
	  private boolean hemomancy$flag = false;

	  @ModifyVariable(method = "swing", at = @At("HEAD"), argsOnly = true, remap = false)
	  private InteractionHand hemomancy$alternatePairedClaws(InteractionHand hand) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		if (hand != InteractionHand.MAIN_HAND
				|| !(player.getMainHandItem().getItem() instanceof LivingBaghnakhItem)
				|| !(player.getOffhandItem().getItem() instanceof LivingBaghnakhItem)) {
			return hand;
		}
		// Follow the last accepted swing; vanilla sends this hand to the server and observers.
		return player.swingingArm == InteractionHand.MAIN_HAND
				? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
	  }

	  @Inject(method = "aiStep", at = @At("HEAD"), remap = false)
	  private void hemomancy$applySilentSlippingNoClipBeforePushOut(CallbackInfo cb) {
		SilentArchonArmorAbilityHandler.applySilentSlippingNoClip((LocalPlayer) (Object) this);
	  }

	  @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), method = "aiStep", remap = false)
	  public void hemomancy$checkFlight(CallbackInfo cb) {
		  
	    this.hemomancy$flag = ClientMixinHooks.checkFlight();
	  }

	  @Redirect(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"),
			method = "aiStep",
			remap = false)
	  public ItemStack hemomancy$affixEmptyStack(LocalPlayer player, EquipmentSlot slot) {
		ItemStack stack = player.getItemBySlot(slot);
		return this.hemomancy$flag ? stack : ItemStack.EMPTY;
	  }

	  @Redirect(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V"),
			method = "aiStep",
			remap = false)
	  private void hemomancy$useLivingTorchMovementMultiplier(Input input, boolean slowDown,
			float slowdownMultiplier) {
		LocalPlayer player = (LocalPlayer) (Object) this;
		float effectiveMultiplier = player.isUsingItem()
				&& player.getUseItem().getItem() instanceof LivingTorchItem
				? LivingTorchBreathRules.MOVEMENT_MULTIPLIER : slowdownMultiplier;
		input.tick(slowDown, effectiveMultiplier);
	  }
	}
