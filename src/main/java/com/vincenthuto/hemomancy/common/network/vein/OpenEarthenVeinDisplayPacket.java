package com.vincenthuto.hemomancy.common.network.vein;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.vein.EarthenVeinTravelClientState;
import com.vincenthuto.hemomancy.common.vein.EarthenVeinDestination;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record OpenEarthenVeinDisplayPacket(UUID nonce, ResourceLocation sourceDimension, BlockPos source,
		List<EarthenVeinDestination> destinations) implements CustomPacketPayload {
	public static final Type<OpenEarthenVeinDisplayPacket> TYPE =
			new Type<>(Hemomancy.rloc("open_earthen_vein_display"));
	public static final StreamCodec<FriendlyByteBuf, OpenEarthenVeinDisplayPacket> STREAM_CODEC =
			StreamCodec.of(OpenEarthenVeinDisplayPacket::encode, OpenEarthenVeinDisplayPacket::decode);

	public OpenEarthenVeinDisplayPacket {
		source = source.immutable();
		destinations = List.copyOf(destinations);
	}

	private static void encode(FriendlyByteBuf buffer, OpenEarthenVeinDisplayPacket packet) {
		buffer.writeUUID(packet.nonce);
		buffer.writeResourceLocation(packet.sourceDimension);
		buffer.writeBlockPos(packet.source);
		buffer.writeVarInt(packet.destinations.size());
		packet.destinations.forEach(destination -> destination.write(buffer));
	}

	private static OpenEarthenVeinDisplayPacket decode(FriendlyByteBuf buffer) {
		UUID nonce = buffer.readUUID();
		ResourceLocation dimension = buffer.readResourceLocation();
		BlockPos source = buffer.readBlockPos();
		int count = Math.min(512, Math.max(0, buffer.readVarInt()));
		List<EarthenVeinDestination> destinations = new ArrayList<>(count);
		for (int index = 0; index < count; index++) destinations.add(EarthenVeinDestination.read(buffer));
		return new OpenEarthenVeinDisplayPacket(nonce, dimension, source, destinations);
	}

	public static void handle(OpenEarthenVeinDisplayPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> EarthenVeinTravelClientState.open(packet));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
