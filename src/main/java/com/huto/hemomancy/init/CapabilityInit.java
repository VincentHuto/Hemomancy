package com.huto.hemomancy.init;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capabilities.bloodvolume.BloodVolume;
import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeStorage;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.capabilities.manipulation.IKnownManipulations;
import com.huto.hemomancy.capabilities.manipulation.KnownManipulationStorage;
import com.huto.hemomancy.capabilities.manipulation.KnownManipulations;
import com.huto.hemomancy.capabilities.mindrune.IRune;
import com.huto.hemomancy.capabilities.mindrune.IRunesItemHandler;
import com.huto.hemomancy.capabilities.mindrune.RuneCap.IRuneFactory;
import com.huto.hemomancy.capabilities.mindrune.RuneCap.IRuneStorage;
import com.huto.hemomancy.capabilities.mindrune.RunesItemHandlerCap.IRunesItemHandlerFactory;
import com.huto.hemomancy.capabilities.mindrune.RunesItemHandlerCap.IRunesItemHandlerStorage;
import com.huto.hemomancy.capabilities.tendency.BloodTendency;
import com.huto.hemomancy.capabilities.tendency.BloodTendencyStorage;
import com.huto.hemomancy.capabilities.tendency.IBloodTendency;
import com.huto.hemomancy.capabilities.vascularsystem.IVascularSystem;
import com.huto.hemomancy.capabilities.vascularsystem.VascularSystem;
import com.huto.hemomancy.capabilities.vascularsystem.VascularSystemStorage;
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
