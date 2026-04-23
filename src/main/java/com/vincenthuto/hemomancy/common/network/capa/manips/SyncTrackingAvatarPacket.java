package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class SyncTrackingAvatarPacket implements CustomPacketPayload {

	public static void encode(FriendlyByteBuf buf, SyncTrackingAvatarPacket msg) {
		msg.toBytes(buf);
	}

	public static SyncTrackingAvatarPacket decode(FriendlyByteBuf buf) {
		return new SyncTrackingAvatarPacket(buf);
	}

	public static final Type<SyncTrackingAvatarPacket> TYPE = new Type<>(Hemomancy.rloc("sync_tracking_avatar_packet"));
	public static final StreamCodec<FriendlyByteBuf, SyncTrackingAvatarPacket> STREAM_CODEC = StreamCodec.of(SyncTrackingAvatarPacket::encode, SyncTrackingAvatarPacket::decode);

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


	public static void handle(final SyncTrackingAvatarPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Entity p = Minecraft.getInstance().level.getEntity(msg.playerId);
			if (p instanceof Player) {
				HemoCapabilityAccess.getKnownManipulations(p).ifPresent(b -> {
					b.setAvatarActive(msg.isActive);
				});
			}
		});
	}

	public void toBytes(FriendlyByteBuf buf) {
		buf.writeInt(this.playerId);
		buf.writeBoolean(this.isActive);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
