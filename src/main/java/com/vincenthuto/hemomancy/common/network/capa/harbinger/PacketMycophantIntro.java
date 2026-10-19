package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.client.boss.MycophantGlimpse;
import com.vincenthuto.hemomancy.client.boss.MycophantIntroClientState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Drives the player's side of being taken: the Gardens capture, the waking (or rematch), the hunt's watching pulse and
 * glimpse, the Pulp Bloom veil, or clears them. Kinds match MycophantIntroClientState; WATCH carries the hunt stage.
 */
public record PacketMycophantIntro(int kind, int seed) implements CustomPacketPayload {
	public static final int WAKING = 0;
	public static final int REMATCH = 1;
	public static final int BLOOM = 2;
	public static final int CLEAR = 3;
	public static final int CAPTURE = 4;
	public static final int WATCH = 5;
	public static final int GLIMPSE = 6;
	public static final Type<PacketMycophantIntro> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("hemomancy", "mycophant_intro"));
	public static final StreamCodec<FriendlyByteBuf, PacketMycophantIntro> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> { buffer.writeVarInt(packet.kind); buffer.writeVarInt(packet.seed); },
			buffer -> new PacketMycophantIntro(buffer.readVarInt(), buffer.readVarInt()));

	public static void handle(PacketMycophantIntro packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (packet.kind == GLIMPSE) {
				MycophantGlimpse.start(packet.seed);
				return;
			}
			if (packet.kind == CLEAR) MycophantGlimpse.clear();
			MycophantIntroClientState.INSTANCE.start(packet.kind, packet.seed);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
