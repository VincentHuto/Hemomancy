package com.vincenthuto.hemomancy.capa.block.vein;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.tile.EarthenVeinBlockEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EarthenVeinLocEvents {
	@SubscribeEvent
	public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
		if (event.getObject()instanceof EarthenVeinBlockEntity te) {
			event.addCapability(new ResourceLocation(Hemomancy.MOD_ID, "veinlocation"), new EarthenVeinLocProvider());

		}

	}

}