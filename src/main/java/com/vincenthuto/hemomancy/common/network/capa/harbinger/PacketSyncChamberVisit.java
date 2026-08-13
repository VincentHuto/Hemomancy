package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.overlay.ChamberVisitOverlay;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitMode;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncChamberVisit(boolean active, ChamberVisitMode mode, int remainingTicks, int totalTicks)
		implements CustomPacketPayload {
	public static final Type<PacketSyncChamberVisit> TYPE = new Type<>(Hemomancy.rloc("sync_chamber_visit"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncChamberVisit> STREAM_CODEC =
			StreamCodec.of(PacketSyncChamberVisit::encode, PacketSyncChamberVisit::decode);

	public static PacketSyncChamberVisit inactive() {
		return new PacketSyncChamberVisit(false, ChamberVisitMode.ADMIN, 0, 0);
	}

	private static void encode(FriendlyByteBuf buffer, PacketSyncChamberVisit packet) {
		buffer.writeBoolean(packet.active);
		buffer.writeEnum(packet.mode);
		buffer.writeVarInt(packet.remainingTicks);
		buffer.writeVarInt(packet.totalTicks);
	}

	private static PacketSyncChamberVisit decode(FriendlyByteBuf buffer) {
		return new PacketSyncChamberVisit(buffer.readBoolean(), buffer.readEnum(ChamberVisitMode.class),
				buffer.readVarInt(), buffer.readVarInt());
	}

	public static void handle(PacketSyncChamberVisit packet, IPayloadContext context) {
		context.enqueueWork(() -> ChamberVisitOverlay.setState(packet.active, packet.mode,
				packet.remainingTicks, packet.totalTicks));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
