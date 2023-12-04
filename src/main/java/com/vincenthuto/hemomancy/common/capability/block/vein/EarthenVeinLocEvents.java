package com.vincenthuto.hemomancy.common.capability.block.vein;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.EarthenVeinBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EarthenVeinLocEvents {
	@SubscribeEvent
	public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject()instanceof EarthenVeinBlockEntity te) {
			event.addCapability(Hemomancy.rloc("veinlocation"), new EarthenVeinLocProvider());

		}

	}

}