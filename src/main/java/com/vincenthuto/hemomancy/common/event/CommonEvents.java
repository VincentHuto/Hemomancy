package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.command.HemomancyCommand;
import com.vincenthuto.hemomancy.common.command.UnstainedCommand;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEvents {

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		HemomancyCommand.register(event.getDispatcher());
		UnstainedCommand.register(event.getDispatcher());
	}

	@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
	public static class CommonModBusEvents {
	}

}
