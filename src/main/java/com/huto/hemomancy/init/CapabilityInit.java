package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.IRunesItemHandler;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.capa.vascular.IVascularSystem;
import com.huto.hemomancy.capa.volume.IBloodVolume;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class CapabilityInit {

	public static void init() {
		CapabilityManager.INSTANCE.register(IBloodTendency.class);
		CapabilityManager.INSTANCE.register(IVascularSystem.class);
		CapabilityManager.INSTANCE.register(IBloodVolume.class);
		CapabilityManager.INSTANCE.register(IKnownManipulations.class);
		CapabilityManager.INSTANCE.register(IRune.class);
		CapabilityManager.INSTANCE.register(IRunesItemHandler.class);

	}

}
