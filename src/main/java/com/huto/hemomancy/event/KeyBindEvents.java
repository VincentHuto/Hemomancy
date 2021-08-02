package com.huto.hemomancy.event;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.keybind.PacketBloodCraftingKeyPress;
import com.huto.hemomancy.network.keybind.PacketBloodFormationKeyPress;
import com.huto.hemomancy.network.keybind.PacketChangeMorphKey;
import com.huto.hemomancy.network.manip.PacketChangeSelectedManip;
import com.huto.hemomancy.network.manip.PacketUseContManipKey;
import com.huto.hemomancy.network.manip.PacketUseQuickManipKey;
import com.huto.hemomancy.network.particle.PacketGroundBloodDraw;
import com.huto.hemomancy.recipe.BaseBloodCraftingRecipe;
import com.huto.hemomancy.recipe.ModBloodCraftingRecipes;
import com.hutoslib.client.ClientUtils;

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
			for (BaseBloodCraftingRecipe pattern : ModBloodCraftingRecipes.RECIPES) {
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
