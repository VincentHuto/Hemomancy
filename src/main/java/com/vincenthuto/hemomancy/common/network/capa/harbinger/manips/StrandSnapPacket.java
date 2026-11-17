package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the attack button went down while a held Sculpting kept the channel (a strand's snap, core-loop
 * Track 12, CL-22; since 2026-11-12 any held Sculpting that takes the button). Carries nothing: the server decides from
 * its own record of the player's open session, look and blood, through {@code VerbSculpting.accent}. Paced to one per
 * five ticks.
 */
public record StrandSnapPacket() implements CustomPacketPayload {
	public static final StrandSnapPacket INSTANCE = new StrandSnapPacket();
	public static final Type<StrandSnapPacket> TYPE = new Type<>(Hemomancy.rloc("strand_snap"));
	public static final StreamCodec<FriendlyByteBuf, StrandSnapPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	private static final String RATE_KEY = "strand_snap";
	private static final int RATE_INTERVAL_TICKS = 5;

	public static void handle(StrandSnapPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !ServerboundGuards.activePlayer(player)
					|| !player.isUsingItem()
					|| !ServerboundRateLimiter.tryAcquire(player, RATE_KEY, RATE_INTERVAL_TICKS)) {
				return;
			}
			SculptingSessions.Session session = SculptingSessions.current(player);
			if (session != null) session.sculpting().accent(player);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
