package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationStorage;
import com.huto.hemomancy.capa.manip.KnownManipulations;
import com.huto.hemomancy.capa.rune.IRune;
import com.huto.hemomancy.capa.rune.IRunesItemHandler;
import com.huto.hemomancy.capa.rune.RuneCap.IRuneFactory;
import com.huto.hemomancy.capa.rune.RuneCap.IRuneStorage;
import com.huto.hemomancy.capa.rune.RunesItemHandlerCap.IRunesItemHandlerFactory;
import com.huto.hemomancy.capa.rune.RunesItemHandlerCap.IRunesItemHandlerStorage;
import com.huto.hemomancy.capa.tendency.BloodTendency;
import com.huto.hemomancy.capa.tendency.BloodTendencyStorage;
import com.huto.hemomancy.capa.tendency.IBloodTendency;
import com.huto.hemomancy.capa.vascular.IVascularSystem;
import com.huto.hemomancy.capa.vascular.VascularSystem;
import com.huto.hemomancy.capa.vascular.VascularSystemStorage;
import com.huto.hemomancy.capa.volume.BloodVolume;
import com.huto.hemomancy.capa.volume.BloodVolumeStorage;
import com.huto.hemomancy.capa.volume.IBloodVolume;
import com.huto.hemomancy.model.animation.IAnimatable;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class CapabilityInit {

	public static void init() {
		CapabilityManager.INSTANCE.register(IBloodTendency.class, new BloodTendencyStorage(), BloodTendency::new);
		CapabilityManager.INSTANCE.register(IVascularSystem.class, new VascularSystemStorage(), VascularSystem::new);
		CapabilityManager.INSTANCE.register(IBloodVolume.class, new BloodVolumeStorage(), BloodVolume::new);
		CapabilityManager.INSTANCE.register(IKnownManipulations.class, new KnownManipulationStorage(),
				KnownManipulations::new);
		CapabilityManager.INSTANCE.register(IRune.class, new IRuneStorage(), new IRuneFactory());
		CapabilityManager.INSTANCE.register(IRunesItemHandler.class, new IRunesItemHandlerStorage(),
				new IRunesItemHandlerFactory());
		IAnimatable.registerCapability();

	}

}
