package com.huto.hemomancy.event;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.crafting.PacketBloodCraftingKeyPress;
import com.huto.hemomancy.network.crafting.PacketBloodFormationKeyPress;
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
					PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketBloodCraftingKeyPress(pattern));
				}
			}
		}

	}

}
