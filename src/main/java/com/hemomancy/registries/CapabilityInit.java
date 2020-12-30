package com.hemomancy.registries;

import com.hemomancy.Hemomancy;
import com.hemomancy.capabilities.tendency.BloodTendency;
import com.hemomancy.capabilities.tendency.BloodTendencyStorage;
import com.hemomancy.capabilities.tendency.IBloodTendency;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
public class CapabilityInit {

	public static void init() {
		CapabilityManager.INSTANCE.register(IBloodTendency.class, new BloodTendencyStorage(), BloodTendency::new);

	}
	
}
