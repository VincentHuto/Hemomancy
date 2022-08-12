package com.vincenthuto.hemomancy.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.container.MorphlingJarMenu;
import com.vincenthuto.hemomancy.init.KeyBindInit;
import com.vincenthuto.hemomancy.item.morphlings.ItemMorphlingJar;
import com.vincenthuto.hemomancy.network.PacketHandler;
import com.vincenthuto.hemomancy.network.morphling.JarTogglePickupPacket;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class MorphlingJarEvents {

	public static void pickupEvent(EntityItemPickupEvent event) {
		if (event.getPlayer().containerMenu instanceof MorphlingJarMenu || event.getPlayer().isShiftKeyDown()
				|| event.getItem().getItem().getItem() instanceof ItemMorphlingJar)
			return;
		Inventory playerInv = event.getPlayer().getInventory();
		for (int i = 0; i <= 8; i++) {
			ItemStack stack = playerInv.getItem(i);
			if (stack.getItem() instanceof ItemMorphlingJar
					&& ((ItemMorphlingJar) stack.getItem()).pickupEvent(event, stack)) {
				event.setResult(Event.Result.ALLOW);
				return;
			}
		}
	}

	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (KeyBindInit.toggleMorphlingJarPickup.consumeClick())
			PacketHandler.CHANNELMORPHLINGJAR.sendToServer(new JarTogglePickupPacket());
	}

}
