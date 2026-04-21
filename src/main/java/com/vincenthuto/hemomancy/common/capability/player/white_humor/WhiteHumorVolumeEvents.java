package com.vincenthuto.hemomancy.common.capability.player.white_humor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.tile.IWhiteHumorTile;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class WhiteHumorVolumeEvents {
@SubscribeEvent
public static void attachCapabilitiesTile(final AttachCapabilitiesEvent<BlockEntity> event) {
if (event.getObject() instanceof IWhiteHumorTile) {
event.addCapability(Hemomancy.rloc("whitehumorvolume"), new WhiteHumorVolumeProvider());
}
}
}
