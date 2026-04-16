package com.vincenthuto.hemomancy.common.capability.player.lethe;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.ILetheTile;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LetheVolumeEvents {
@SubscribeEvent
public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
if (event.getObject() instanceof ILetheTile) {
event.addCapability(Hemomancy.rloc("lethevolume"), new LetheVolumeProvider());
}
}
}
