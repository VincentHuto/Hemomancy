package com.vincenthuto.hemomancy.common.network.vein;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.world.vein.EarthenVeinTravelPhase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record EarthenVeinTravelVisualPacket(ResourceLocation dimension, BlockPos vein, int travelerEntityId,
		EarthenVeinTravelPhase phase, float progress) implements CustomPacketPayload {
	public static final Type<EarthenVeinTravelVisualPacket> TYPE =
			new Type<>(Hemomancy.rloc("earthen_vein_travel_visual"));
	public static final StreamCodec<FriendlyByteBuf, EarthenVeinTravelVisualPacket> STREAM_CODEC =
			StreamCodec.of((buffer, packet) -> {
				buffer.writeResourceLocation(packet.dimension);
				buffer.writeBlockPos(packet.vein);
				buffer.writeVarInt(packet.travelerEntityId);
				buffer.writeEnum(packet.phase);
				buffer.writeFloat(packet.progress);
			}, buffer -> new EarthenVeinTravelVisualPacket(buffer.readResourceLocation(), buffer.readBlockPos(),
					buffer.readVarInt(), buffer.readEnum(EarthenVeinTravelPhase.class), buffer.readFloat()));

	@Override
	public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
