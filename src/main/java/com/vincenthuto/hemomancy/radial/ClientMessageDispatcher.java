package com.vincenthuto.hemomancy.radial;

import com.vincenthuto.hemomancy.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.network.NetworkDirection;

public class ClientMessageDispatcher {
	public static void sendRadialInventorySlotChange(int slot, boolean offhand) {
		PacketHandler.CHANNELKNOWNMANIPS.sendTo(new RadialInventorySlotChangeMessage(slot, offhand),
				Minecraft.getInstance().getConnection().getConnection(), NetworkDirection.PLAY_TO_SERVER);
	}

}
