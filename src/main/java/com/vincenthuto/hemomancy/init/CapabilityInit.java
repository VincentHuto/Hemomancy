package com.vincenthuto.hemomancy.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.capa.block.vein.IEarthenVeinLoc;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.rune.IRune;
import com.vincenthuto.hemomancy.capa.player.rune.IRunesItemHandler;
import com.vincenthuto.hemomancy.capa.player.tendency.IBloodTendency;
import com.vincenthuto.hemomancy.capa.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.model.anim.IAnimatable;

import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class CapabilityInit {

	@SubscribeEvent
	public static void init(RegisterCapabilitiesEvent event) {
		event.register(IAnimatable.class);
		event.register(IBloodTendency.class);
		event.register(IVascularSystem.class);
		event.register(IBloodVolume.class);
		event.register(IKnownManipulations.class);
		event.register(IRune.class);
		event.register(IRunesItemHandler.class);
		event.register(IEarthenVeinLoc.class);

	}

}
