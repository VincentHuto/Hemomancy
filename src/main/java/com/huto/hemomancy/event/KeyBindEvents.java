package com.huto.hemomancy.event;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.network.PacketBloodFormationKeyPress;
import com.huto.hemomancy.network.PacketHandler;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class KeyBindEvents {

	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (ClientEventSubscriber.bloodFormation.isPressed()) {
			PacketHandler.CHANNELBLOODVOLUME.sendToServer(new PacketBloodFormationKeyPress());
		}
	}

}
