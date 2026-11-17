package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BodyFormationDraw;
import com.vincenthuto.hemomancy.common.network.ServerboundGuards;
import com.vincenthuto.hemomancy.common.util.ServerboundRateLimiter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** The Blood Formation key; the work is {@link BodyFormationDraw}. */
public class BloodFormationKeyPressPacket implements CustomPacketPayload {
	public static final Type<BloodFormationKeyPressPacket> TYPE = new Type<>(Hemomancy.rloc("blood_formation_key_press_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodFormationKeyPressPacket> STREAM_CODEC = StreamCodec.of(BloodFormationKeyPressPacket::encode, BloodFormationKeyPressPacket::decode);

	public static BloodFormationKeyPressPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new BloodFormationKeyPressPacket();
	}

	public static void encode(final FriendlyByteBuf buffer, final BloodFormationKeyPressPacket message) {
		buffer.writeByte(0);
	}

	public static void handle(final BloodFormationKeyPressPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (ctx.player() instanceof ServerPlayer player && ServerboundGuards.activePlayer(player)
					&& ServerboundRateLimiter.tryAcquire(player, "blood_formation", 10))
				BodyFormationDraw.draw(player);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
