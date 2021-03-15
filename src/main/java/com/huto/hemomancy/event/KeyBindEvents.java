package com.huto.hemomancy.event;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.keybind.PacketBloodCraftingKeyPress;
import com.huto.hemomancy.network.keybind.PacketBloodFormationKeyPress;
import com.huto.hemomancy.network.keybind.PacketChangeMorphKey;
import com.huto.hemomancy.network.keybind.PacketDisplayKnownManips;
import com.huto.hemomancy.network.keybind.PacketGroundBloodDraw;
import com.huto.hemomancy.recipes.BaseBloodCraftingRecipe;
import com.huto.hemomancy.recipes.ModBloodCraftingRecipes;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class KeyBindEvents {
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (ClientEventSubscriber.bloodFormation.isPressed()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketBloodFormationKeyPress());
		}

		if (ClientEventSubscriber.bloodCrafting.isPressed()) {
			for (BaseBloodCraftingRecipe pattern : ModBloodCraftingRecipes.RECIPES) {
				if (ClientEventSubscriber.getClientPlayer().getHeldItemMainhand().getItem() == pattern.getHeldItem()) {
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketBloodCraftingKeyPress(
							ClientEventSubscriber.getClientPlayer().getHeldItemMainhand()));
				}
			}
		}
		if (ClientEventSubscriber.bloodDraw.isKeyDown()) {
			PacketHandler.CHANNELBLOODVOLUME
					.sendToServer(new PacketGroundBloodDraw(ClientEventSubscriber.getPartialTicks()));
		}
		if (ClientEventSubscriber.toggleMorphlingOpenJar.isPressed()) {
			PacketHandler.HANDLER
					.sendToServer(new PacketChangeMorphKey());

		}
		if (ClientEventSubscriber.displayKnownManips.isPressed()) {
			PacketHandler.CHANNELKNOWNMANIPS.sendToServer(new PacketDisplayKnownManips());
		}


	}

}
