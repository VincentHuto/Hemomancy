package com.vincenthuto.hemomancy.common.init;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.block.vein.IEarthenVeinLoc;
import com.vincenthuto.hemomancy.common.capability.player.degree.IInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.white_humor.IWhiteHumorVolume;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.visceral.IVisceralOrgans;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.morphling.IEquippedMorphling;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScar;
import com.vincenthuto.hemomancy.common.capability.player.scar.IScarsItemHandler;
import com.vincenthuto.hemomancy.common.capability.player.vascular.IVascularSystem;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class CapabilityInit {

	@SubscribeEvent
	public static void init(RegisterCapabilitiesEvent event) {
		event.register(IBloodTendency.class);
		event.register(IVascularSystem.class);
		event.register(IBloodVolume.class);
		event.register(IWhiteHumorVolume.class);
		event.register(IKnownManipulations.class);
		event.register(IEarthenVeinLoc.class);
		event.register(IScar.class);
		event.register(IScarsItemHandler.class);
		event.register(IEquippedMorphling.class);
		event.register(IInitiatoryDegree.class);
		event.register(IUnstainedProgress.class);
		event.register(IVisceralOrgans.class);

	}

}
