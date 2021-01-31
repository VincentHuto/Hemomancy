package com.huto.hemomancy.event;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.containers.ContainerMorphlingJar;
import com.huto.hemomancy.item.tool.ItemMorphlingJar;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.jar.PacketJarTogglePickup;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class MorphlingJarEvents {

	public static void pickupEvent(EntityItemPickupEvent event) {
		if (event.getPlayer().openContainer instanceof ContainerMorphlingJar || event.getPlayer().isSneaking()
				|| event.getItem().getItem().getItem() instanceof ItemMorphlingJar)
			return;
		PlayerInventory playerInv = event.getPlayer().inventory;
		for (int i = 0; i <= 8; i++) {
			ItemStack stack = playerInv.getStackInSlot(i);
			if (stack.getItem() instanceof ItemMorphlingJar
					&& ((ItemMorphlingJar) stack.getItem()).pickupEvent(event, stack)) {
				event.setResult(Event.Result.ALLOW);
				return;
			}
		}
	}

	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (ClientEventSubscriber.keyBinds.get(4).isPressed())
			PacketHandler.MORPHLINGJAR.sendToServer(new PacketJarTogglePickup());
	}
	
	

}
