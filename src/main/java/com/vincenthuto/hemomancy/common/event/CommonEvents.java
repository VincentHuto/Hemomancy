package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.command.HemoCommand;

import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class CommonEvents {

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		HemoCommand.register(event.getDispatcher());
	}

	@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
	public static class CommonModBusEvents {
	}

}
