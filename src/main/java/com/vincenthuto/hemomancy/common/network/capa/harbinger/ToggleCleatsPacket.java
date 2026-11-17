package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.CleatsFitting;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the inner-radial cleats slice was clicked. Carries nothing; {@link CleatsFitting#toggle} turns the
 * cleats in the sender's own worn boots on or off. Paced to one every ten ticks.
 */
public record ToggleCleatsPacket() implements CustomPacketPayload {
	public static final ToggleCleatsPacket INSTANCE = new ToggleCleatsPacket();
	public static final Type<ToggleCleatsPacket> TYPE = new Type<>(Hemomancy.rloc("toggle_cleats"));
	public static final StreamCodec<FriendlyByteBuf, ToggleCleatsPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	private static final String RATE_KEY = "toggle_cleats";
	private static final int RATE_INTERVAL_TICKS = 10;

	public static void handle(ToggleCleatsPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player && ServerboundGuards.activePlayer(player)
					&& ServerboundRateLimiter.tryAcquire(player, RATE_KEY, RATE_INTERVAL_TICKS)) {
				CleatsFitting.toggle(player);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
