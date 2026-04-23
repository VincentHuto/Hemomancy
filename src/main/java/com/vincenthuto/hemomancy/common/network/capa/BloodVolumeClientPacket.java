package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public class BloodVolumeClientPacket implements CustomPacketPayload {

	public static final Type<BloodVolumeClientPacket> TYPE = new Type<>(Hemomancy.rloc("blood_volume_client_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodVolumeClientPacket> STREAM_CODEC = StreamCodec.of(BloodVolumeClientPacket::encode, BloodVolumeClientPacket::decode);

	public static BloodVolumeClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new BloodVolumeClientPacket();
	}

	public static void encode(final FriendlyByteBuf packetBuffer, final BloodVolumeClientPacket msg) {

	}

	public static void handle(final BloodVolumeClientPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer sender = ctx.player(); // the client that sent this packet
			if (sender != null) {
				IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(sender)
						.orElseThrow(IllegalStateException::new);
				// Send message back to the client to set the information
				PacketHandler.sendToPlayer(sender, new BloodVolumeServerPacket(volume));
			}
		});
	}

	public BloodVolumeClientPacket() {

	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
