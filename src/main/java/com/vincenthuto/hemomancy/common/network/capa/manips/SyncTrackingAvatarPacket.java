package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.function.Supplier;


import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.NetworkEvent;

public class SyncTrackingAvatarPacket {

	public int playerId;
	public boolean isActive;

	public SyncTrackingAvatarPacket(FriendlyByteBuf buf) {
		this.playerId = buf.readInt();
		this.isActive = buf.readBoolean();
	}

	public SyncTrackingAvatarPacket(int playerId, boolean isActive) {
		this.playerId = playerId;
		this.isActive = isActive;
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Entity p = Minecraft.getInstance().level.getEntity(playerId);
			if (p instanceof Player) {
				HemoCapabilityAccess.getKnownManipulations(p).ifPresent(b -> {
					b.setAvatarActive(isActive);
				});
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeBoolean(this.isActive);
	}
}
