package com.vincenthuto.hemomancy.common.capability.player.silthmere;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.ISilthmereTile;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SilthmereVolumeEvents {
@SubscribeEvent
public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
if (event.getObject() instanceof ISilthmereTile) {
event.addCapability(Hemomancy.rloc("silthmerevolume"), new SilthmereVolumeProvider());
}
}
}
