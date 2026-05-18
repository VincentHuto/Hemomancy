package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.command.HemoCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class CommonEvents {

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		HemoCommand.register(event.getDispatcher());
	}


}

