package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.block.vein.IEarthenVeinLoc;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRune;
import com.vincenthuto.hemomancy.common.capability.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

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
		event.register(IEarthenVeinLoc.class);
		event.register(IRune.class);
		event.register(IRunesItemHandler.class);

	}

}
