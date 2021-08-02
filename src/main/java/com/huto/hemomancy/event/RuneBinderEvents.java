package com.huto.hemomancy.event;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.container.ContainerRuneBinder;
import com.huto.hemomancy.item.rune.ItemRuneBinder;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.binder.PacketBinderTogglePickup;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class RuneBinderEvents {

	public static void pickupEvent(EntityItemPickupEvent event) {
		if (event.getPlayer().openContainer instanceof ContainerRuneBinder || event.getPlayer().isSneaking()
				|| event.getItem().getItem().getItem() instanceof ItemRuneBinder)
			return;
		PlayerInventory playerInv = event.getPlayer().inventory;
		for (int i = 0; i <= 8; i++) {
			ItemStack stack = playerInv.getStackInSlot(i);
			if (stack.getItem() instanceof ItemRuneBinder
					&& ((ItemRuneBinder) stack.getItem()).pickupEvent(event, stack)) {
				event.setResult(Event.Result.ALLOW);
				return;
			}
		}
	}

	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (ClientEventSubscriber.keyBinds.get(0).isPressed())
			PacketHandler.CHANNELRUNEBINDER.sendToServer(new PacketBinderTogglePickup());
	}

}
