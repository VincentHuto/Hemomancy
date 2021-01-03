package com.huto.hemomancy.network;

import java.util.function.Supplier;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class OpenNormalInvPacket {

    public OpenNormalInvPacket(PacketBuffer buf) {
    }

    public OpenNormalInvPacket() {
    }

    public void toBytes(PacketBuffer buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity playerEntity = ctx.get().getSender();
            if (playerEntity != null) {
                playerEntity.openContainer.onContainerClosed(playerEntity);
                playerEntity.openContainer = playerEntity.container;
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
