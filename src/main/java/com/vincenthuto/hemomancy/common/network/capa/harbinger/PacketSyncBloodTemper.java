package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server to owner: the Temper pool per school and the schools the HUD shows. */
public record PacketSyncBloodTemper(float[] amounts, int seen) implements CustomPacketPayload {
	private static final int SCHOOLS = EnumBloodTemper.values().length;
	public static final Type<PacketSyncBloodTemper> TYPE = new Type<>(Hemomancy.rloc("sync_blood_temper"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncBloodTemper> STREAM_CODEC = StreamCodec.of(
			PacketSyncBloodTemper::encode, PacketSyncBloodTemper::decode);

	private static void encode(FriendlyByteBuf buffer, PacketSyncBloodTemper packet) {
		for (int i = 0; i < SCHOOLS; i++) buffer.writeFloat(i < packet.amounts().length ? packet.amounts()[i] : 0.0F);
		buffer.writeVarInt(packet.seen());
	}

	private static PacketSyncBloodTemper decode(FriendlyByteBuf buffer) {
		float[] amounts = new float[SCHOOLS];
		for (int i = 0; i < SCHOOLS; i++) amounts[i] = buffer.readFloat();
		return new PacketSyncBloodTemper(amounts, buffer.readVarInt());
	}

	public static void handle(PacketSyncBloodTemper packet, IPayloadContext context) {
		context.enqueueWork(() -> HemoCapabilityAccess.getBloodTemper(context.player())
				.setFromSync(packet.amounts(), packet.seen()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
