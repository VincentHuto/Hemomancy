package com.vincenthuto.hemomancy.network.charm;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CharmContainerSlotPacket
{
    public CharmContainerSlotPacket()
    {
    }

    public CharmContainerSlotPacket(FriendlyByteBuf buf)
    {
    }

    public void encode(FriendlyByteBuf buf)
    {
    }

	public boolean handle(Supplier<NetworkEvent.Context> context) {
        context.get().getSender().containerMenu.sendAllDataToRemote();
        return true;
    }
}
