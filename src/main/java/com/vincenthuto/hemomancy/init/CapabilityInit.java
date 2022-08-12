package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.block.vein.IEarthenVeinLoc;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.radial.finder.CharmExtensionSlot;
import com.vincenthuto.hemomancy.radial.finder.CharmSlotCapability;
import com.vincenthuto.hemomancy.radial.finder.ICharmSlotItem;

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
		event.register(ICharmSlotItem.class);
		event.register(CharmExtensionSlot.class);
		CharmSlotCapability.register(event);

	}

}
