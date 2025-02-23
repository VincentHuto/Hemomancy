package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommontEvents {

	@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Bus.MOD)
	public static class CommonModBusEvents {
	}

}
