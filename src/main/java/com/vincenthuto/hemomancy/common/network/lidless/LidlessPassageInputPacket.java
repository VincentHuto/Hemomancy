package com.vincenthuto.hemomancy.common.network.lidless;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The crossing player's keys this tick: forward (1), back (-1) or neither, and whether they sprint or sneak. */
public record LidlessPassageInputPacket(long token, int direction, boolean sprinting, boolean sneaking)
		implements CustomPacketPayload {
	public static final Type<LidlessPassageInputPacket> TYPE = new Type<>(Hemomancy.rloc("lidless_passage_input"));
	public static final StreamCodec<FriendlyByteBuf, LidlessPassageInputPacket> STREAM_CODEC = StreamCodec.of(
			(b, p) -> {
				b.writeLong(p.token);
				b.writeByte(p.direction);
				b.writeBoolean(p.sprinting);
				b.writeBoolean(p.sneaking);
			},
			b -> new LidlessPassageInputPacket(b.readLong(), b.readByte(), b.readBoolean(), b.readBoolean()));

	public static void handle(LidlessPassageInputPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player
					&& ServerboundRateLimiter.tryAcquire(player, "lidless_input", 1)) {
				LidlessPassageService.input(player, packet.token, packet.direction, packet.sprinting, packet.sneaking);
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
