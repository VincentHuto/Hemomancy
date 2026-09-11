package com.vincenthuto.hemomancy.common.network.vein;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.vein.EarthenVeinTravelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

public record SelectEarthenVeinDestinationPacket(UUID nonce, UUID destinationId) implements CustomPacketPayload {
	public static final Type<SelectEarthenVeinDestinationPacket> TYPE =
			new Type<>(Hemomancy.rloc("select_earthen_vein_destination"));
	public static final StreamCodec<FriendlyByteBuf, SelectEarthenVeinDestinationPacket> STREAM_CODEC =
			StreamCodec.of((buffer, packet) -> {
				buffer.writeUUID(packet.nonce);
				buffer.writeUUID(packet.destinationId);
			}, buffer -> new SelectEarthenVeinDestinationPacket(buffer.readUUID(), buffer.readUUID()));

	public static void handle(SelectEarthenVeinDestinationPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) {
				EarthenVeinTravelManager.select(player, packet.nonce, packet.destinationId);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
