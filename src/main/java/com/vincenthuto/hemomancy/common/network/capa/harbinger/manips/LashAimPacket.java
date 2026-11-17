package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: where the caster's whip aim points this tick, while their lash is live or fading. The mouse turns
 * the whip aim in full while the view takes a capped share, so the lash can point where the look does not. The server
 * holds the aim to its own limits ({@link StrandService#aim}) and decides every hit from its copy. One a tick; the
 * service takes a few between two steps of the lash, so two that arrive together are not lost, and no more.
 *
 * <p>With a turn ({@code turnYaw}, {@code turnPitch} not both zero) it is instead the flick that throws a charging
 * lash: the turn is the way the mouse went, and {@code yaw} and {@code pitch} the look the arc is centred on
 * ({@link StrandService#flick}).
 */
public record LashAimPacket(float yaw, float pitch, float turnYaw, float turnPitch) implements CustomPacketPayload {
	/** A plain whip aim, no flick. */
	public LashAimPacket(float yaw, float pitch) {
		this(yaw, pitch, 0.0F, 0.0F);
	}

	public boolean flick() {
		return turnYaw != 0.0F || turnPitch != 0.0F;
	}

	public static final Type<LashAimPacket> TYPE = new Type<>(Hemomancy.rloc("lash_aim"));
	public static final StreamCodec<FriendlyByteBuf, LashAimPacket> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, LashAimPacket::yaw, ByteBufCodecs.FLOAT, LashAimPacket::pitch,
			ByteBufCodecs.FLOAT, LashAimPacket::turnYaw, ByteBufCodecs.FLOAT, LashAimPacket::turnPitch, LashAimPacket::new);
	private static final String FLICK_RATE_KEY = "lash_flick";
	private static final int FLICK_INTERVAL_TICKS = 2;

	public static void handle(LashAimPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (!(context.player() instanceof ServerPlayer player) || !ServerboundGuards.activePlayer(player)
					|| !player.isUsingItem() || !StrandService.isOpen(player)) {
				return;
			}
			if (!packet.flick()) StrandService.aim(player, packet.yaw(), packet.pitch());
			else if (ServerboundRateLimiter.tryAcquire(player, FLICK_RATE_KEY, FLICK_INTERVAL_TICKS))
				StrandService.flick(player, packet.yaw(), packet.pitch(), packet.turnYaw(), packet.turnPitch());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
