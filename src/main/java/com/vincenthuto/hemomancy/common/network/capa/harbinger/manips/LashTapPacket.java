package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: the attack button went down with a lash selected on a held Projection tool and nothing being
 * channelled. Carries nothing: the server decides from its own record of the player's tool, selection, look, blood and
 * tap cooldown, through {@link StrandService#tap}. Paced to one every four ticks.
 */
public record LashTapPacket() implements CustomPacketPayload {
	public static final LashTapPacket INSTANCE = new LashTapPacket();
	public static final Type<LashTapPacket> TYPE = new Type<>(Hemomancy.rloc("lash_tap"));
	public static final StreamCodec<FriendlyByteBuf, LashTapPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);
	private static final String RATE_KEY = "lash_tap";
	private static final int RATE_INTERVAL_TICKS = 4;

	public static void handle(LashTapPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !ServerboundGuards.activePlayer(player)
					|| player.isUsingItem() || !ServerboundRateLimiter.tryAcquire(player, RATE_KEY, RATE_INTERVAL_TICKS)) {
				return;
			}
			StrandService.tap(player);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
