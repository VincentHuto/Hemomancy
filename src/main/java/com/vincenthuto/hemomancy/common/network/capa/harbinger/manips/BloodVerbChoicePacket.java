package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSelection;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client to server: a verb was picked on the inner wheel. Paced like the verb key; the pick is {@link VerbSelection#choose}. */
public record BloodVerbChoicePacket(Verb verb) implements CustomPacketPayload {
	public static final Type<BloodVerbChoicePacket> TYPE = new Type<>(Hemomancy.rloc("blood_verb_choice"));
	public static final StreamCodec<FriendlyByteBuf, BloodVerbChoicePacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.BYTE.map(Verb::byId, verb -> (byte) verb.ordinal()), BloodVerbChoicePacket::verb,
			BloodVerbChoicePacket::new);
	private static final String RATE_KEY = "blood_verb_choice";
	private static final int RATE_INTERVAL_TICKS = 4;

	public static void handle(BloodVerbChoicePacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !ServerboundGuards.activePlayer(player)
					|| !ServerboundRateLimiter.tryAcquire(player, RATE_KEY, RATE_INTERVAL_TICKS)) {
				return;
			}
			VerbSelection.choose(player, packet.verb());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
