package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionSequences;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the possessor skipped a possession scene. The server runs what is left of the sender's own
 * sequence at once ({@link PossessionSequences#skip}), which ends in the state the scenes would have; a sender with no
 * sequence is ignored.
 */
public record PossessionSkipPacket() implements CustomPacketPayload {
	public static final PossessionSkipPacket INSTANCE = new PossessionSkipPacket();
	public static final Type<PossessionSkipPacket> TYPE = new Type<>(Hemomancy.rloc("possession_skip"));
	public static final StreamCodec<FriendlyByteBuf, PossessionSkipPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	public static void handle(PossessionSkipPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) PossessionSequences.skip(player);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
