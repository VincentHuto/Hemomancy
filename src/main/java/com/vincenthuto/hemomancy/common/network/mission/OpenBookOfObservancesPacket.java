package com.vincenthuto.hemomancy.common.network.mission;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.item.BookOfObservancesScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server snapshot used by the read-only Book of Observances ledger. */
public record OpenBookOfObservancesPacket(
		int acceptedMask,
		int claimedMask,
		int availableMask,
		int readyMask,
		float purity,
		float clarity,
		boolean clarityUnlocked) implements CustomPacketPayload {

	public static final Type<OpenBookOfObservancesPacket> TYPE =
			new Type<>(Hemomancy.rloc("open_book_of_observances"));
	public static final StreamCodec<FriendlyByteBuf, OpenBookOfObservancesPacket> STREAM_CODEC =
			StreamCodec.of(OpenBookOfObservancesPacket::encode, OpenBookOfObservancesPacket::decode);

	private static void encode(FriendlyByteBuf buf, OpenBookOfObservancesPacket msg) {
		buf.writeVarInt(msg.acceptedMask);
		buf.writeVarInt(msg.claimedMask);
		buf.writeVarInt(msg.availableMask);
		buf.writeVarInt(msg.readyMask);
		buf.writeFloat(msg.purity);
		buf.writeFloat(msg.clarity);
		buf.writeBoolean(msg.clarityUnlocked);
	}

	private static OpenBookOfObservancesPacket decode(FriendlyByteBuf buf) {
		return new OpenBookOfObservancesPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
				buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readBoolean());
	}

	public static void handle(OpenBookOfObservancesPacket msg, IPayloadContext context) {
		context.enqueueWork(() -> BookOfObservancesScreen.open(msg.acceptedMask, msg.claimedMask,
				msg.availableMask, msg.readyMask, msg.purity, msg.clarity, msg.clarityUnlocked));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
