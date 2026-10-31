package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The owner's Brined Court progress, mirrored for the strain tooltips and the Bestiary. */
public record PacketSyncCourtProgress(CompoundTag data) implements CustomPacketPayload {
	public static final Type<PacketSyncCourtProgress> TYPE = new Type<>(Hemomancy.rloc("court_progress"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncCourtProgress> STREAM_CODEC = StreamCodec.of(
			(buf, packet) -> buf.writeNbt(packet.data), buf -> new PacketSyncCourtProgress(buf.readNbt()));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public static void handle(PacketSyncCourtProgress packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (packet.data != null) HemoCapabilityAccess.courtProgress(context.player())
					.deserializeNBT(context.player().registryAccess(), packet.data);
		});
	}
}
