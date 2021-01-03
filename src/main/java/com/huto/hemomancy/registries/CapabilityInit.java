package com.huto.hemomancy.registries;

import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.capabilities.mindrunes.IRune;
import com.huto.hemomancy.capabilities.mindrunes.IRunesItemHandler;
import com.huto.hemomancy.capabilities.mindrunes.RuneCap.IRuneFactory;
import com.huto.hemomancy.capabilities.mindrunes.RuneCap.IRuneStorage;
import com.huto.hemomancy.capabilities.mindrunes.RunesItemHandlerCap.IRunesItemHandlerFactory;
import com.huto.hemomancy.capabilities.mindrunes.RunesItemHandlerCap.IRunesItemHandlerStorage;
import com.huto.hemomancy.capabilities.tendency.BloodTendency;
import com.huto.hemomancy.capabilities.tendency.BloodTendencyStorage;
import com.huto.hemomancy.capabilities.tendency.IBloodTendency;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class CapabilityInit {

	public static void init() {
		CapabilityManager.INSTANCE.register(IBloodTendency.class, new BloodTendencyStorage(), BloodTendency::new);
		CapabilityManager.INSTANCE.register(IRune.class, new IRuneStorage(), new IRuneFactory());
		CapabilityManager.INSTANCE.register(IRunesItemHandler.class, new IRunesItemHandlerStorage(),
				new IRunesItemHandlerFactory());
	}
	
}
