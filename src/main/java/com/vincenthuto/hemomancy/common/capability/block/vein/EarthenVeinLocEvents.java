package com.vincenthuto.hemomancy.common.capability.block.vein;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.functional.EarthenVeinBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class EarthenVeinLocEvents {
	@SubscribeEvent
	public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject()instanceof EarthenVeinBlockEntity te) {
			event.addCapability(Hemomancy.rloc("veinlocation"), new EarthenVeinLocProvider());

		}

	}

}