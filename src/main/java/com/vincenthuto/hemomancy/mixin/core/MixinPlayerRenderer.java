package com.vincenthuto.hemomancy.mixin.core;

import com.vincenthuto.hemomancy.common.item.shared.armor.PlayerLayerHidingArmor;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class MixinPlayerRenderer {
	@Inject(method = "setModelProperties", at = @At("RETURN"), remap = false)
	private void hemomancy$hideSkinLayersUnderOversizedArmor(AbstractClientPlayer player, CallbackInfo ci) {
		if (!PlayerLayerHidingArmor.isWorn(player)) return;
		PlayerModel<AbstractClientPlayer> model = ((PlayerRenderer) (Object) this).getModel();
		model.hat.visible = false;
		model.jacket.visible = false;
		model.leftPants.visible = false;
		model.rightPants.visible = false;
		model.leftSleeve.visible = false;
		model.rightSleeve.visible = false;
	}
}
