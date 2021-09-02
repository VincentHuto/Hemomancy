package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.rune.IRune;
import com.vincenthuto.hemomancy.capa.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.capa.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.capa.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class CapabilityInit {

	@SubscribeEvent
	public static void init(RegisterCapabilitiesEvent event) {
		event.register(IBloodTendency.class);
		event.register(IVascularSystem.class);
		event.register(IBloodVolume.class);
		event.register(IKnownManipulations.class);
		event.register(IRune.class);
		event.register(IRunesItemHandler.class);

	}

}
