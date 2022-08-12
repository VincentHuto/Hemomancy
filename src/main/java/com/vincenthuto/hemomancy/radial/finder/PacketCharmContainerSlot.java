package com.vincenthuto.hemomancy.radial.finder;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketCharmContainerSlot
{
    public PacketCharmContainerSlot()
    {
    }

    public PacketCharmContainerSlot(FriendlyByteBuf buf)
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
