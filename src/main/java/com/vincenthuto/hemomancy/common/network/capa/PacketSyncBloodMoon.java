package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.worldevent.BloodMoonClientState;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Server → Client: Syncs blood moon active state so the client can check it
 * in {@link com.vincenthuto.hemomancy.common.manipulation.BloodManipulation}.
 */
public class PacketSyncBloodMoon implements CustomPacketPayload {

	public static final Type<PacketSyncBloodMoon> TYPE = new Type<>(Hemomancy.rloc("packet_sync_blood_moon"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncBloodMoon> STREAM_CODEC = StreamCodec.of(PacketSyncBloodMoon::encode, PacketSyncBloodMoon::decode);

	private final boolean active;

	public PacketSyncBloodMoon(boolean active) {
		this.active = active;
	}

	public static void encode(PacketSyncBloodMoon msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.active);
	}

	public static PacketSyncBloodMoon decode(FriendlyByteBuf buf) {
		return new PacketSyncBloodMoon(buf.readBoolean());
	}

	public static void handle(final PacketSyncBloodMoon msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> BloodMoonClientState.set(msg.active));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
