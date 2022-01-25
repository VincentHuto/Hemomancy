package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncTrackingAvatar {

	public int playerId;
	public boolean isActive;

	public PacketSyncTrackingAvatar(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.isActive = buf.readBoolean();
	}

	public PacketSyncTrackingAvatar(int playerId, boolean isActive) {
		this.playerId = playerId;
		this.isActive = isActive;
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeBoolean(this.isActive);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity p = Minecraft.getInstance().level.getEntity(playerId);
			if (p instanceof Player) {
				p.getCapability(KnownManipulationProvider.MANIP_CAPA).ifPresent(b -> {
					b.setAvatarActive(isActive);
					System.out.println("attempted to sync");
					
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
