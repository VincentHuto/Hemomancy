package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server to owner: the Apotheos's spore load, for the bar that replaces the food bar. */
public record PacketSyncSporeLoad(float load) implements CustomPacketPayload {
	public static final Type<PacketSyncSporeLoad> TYPE = new Type<>(Hemomancy.rloc("sync_spore_load"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncSporeLoad> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> buffer.writeFloat(packet.load()), buffer -> new PacketSyncSporeLoad(buffer.readFloat()));

	public static void handle(PacketSyncSporeLoad packet, IPayloadContext context) {
		context.enqueueWork(() -> HemoCapabilityAccess.getSporeLoad(context.player()).setFromSync(packet.load()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
