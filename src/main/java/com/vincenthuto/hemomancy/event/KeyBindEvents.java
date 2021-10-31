package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.capa.manips.PacketChangeSelectedManip;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUseContManipKey;
import com.vincenthuto.hemomancy.network.capa.manips.PacketUseQuickManipKey;
import com.vincenthuto.hemomancy.network.keybind.PacketBloodCraftingKeyPress;
import com.vincenthuto.hemomancy.network.keybind.PacketBloodFormationKeyPress;
import com.vincenthuto.hemomancy.network.morphling.PacketChangeMorphKey;
import com.vincenthuto.hemomancy.network.particle.PacketGroundBloodDraw;
import com.vincenthuto.hemomancy.recipe.RecipeBaseBloodCrafting;
import com.vincenthuto.hemomancy.recipe.BloodCraftingRecipes;
import com.vincenthuto.hutoslib.client.ClientUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD, value = Dist.CLIENT)
public class KeyBindEvents {
	public static void onClientTick(ClientTickEvent event) {

		if (ClientEventSubscriber.bloodFormation.consumeClick()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketBloodFormationKeyPress());
		}

		if (ClientEventSubscriber.bloodCrafting.consumeClick()) {
			for (RecipeBaseBloodCrafting pattern : BloodCraftingRecipes.RECIPES) {
				if (ClientUtils.getClientPlayer().getMainHandItem().getItem() == pattern.getHeldItem()) {
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(
							new PacketBloodCraftingKeyPress(ClientUtils.getClientPlayer().getMainHandItem()));
				}
			}
		}
		if (ClientEventSubscriber.bloodDraw.isDown()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketGroundBloodDraw(ClientUtils.getPartialTicks()));
		}
		if (ClientEventSubscriber.toggleMorphlingOpenJar.consumeClick()) {
			PacketHandler.CHANNELMAIN.sendToServer(new PacketChangeMorphKey());

		}
//		if (ClientEventSubscriber.displayKnownManips.isPressed()) {
//			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketDisplayKnownManips());
//		}
		if (ClientEventSubscriber.cycleSelectedManip.consumeClick()) {
			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketChangeSelectedManip(ClientUtils.getPartialTicks()));
		}
		if (ClientEventSubscriber.useQuickManip.consumeClick()) {
			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketUseQuickManipKey(ClientUtils.getPartialTicks()));
		}
		if (ClientEventSubscriber.useContManip.isDown()) {
			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketUseContManipKey(ClientUtils.getPartialTicks()));
		}
	}

}
