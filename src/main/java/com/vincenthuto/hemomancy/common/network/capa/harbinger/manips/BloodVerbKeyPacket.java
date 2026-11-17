package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSelection;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client to server: the verb key was pressed. Paced to one press per four ticks; the press is {@link VerbSelection#press}. */
public record BloodVerbKeyPacket() implements CustomPacketPayload {
	public static final BloodVerbKeyPacket INSTANCE = new BloodVerbKeyPacket();
	public static final Type<BloodVerbKeyPacket> TYPE = new Type<>(Hemomancy.rloc("blood_verb_key"));
	public static final StreamCodec<FriendlyByteBuf, BloodVerbKeyPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	private static final String RATE_KEY = "blood_verb_key";
	private static final int RATE_INTERVAL_TICKS = 4;

	public static void handle(BloodVerbKeyPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !ServerboundGuards.activePlayer(player)
					|| !ServerboundRateLimiter.tryAcquire(player, RATE_KEY, RATE_INTERVAL_TICKS)) {
				return;
			}
			VerbSelection.press(player);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
