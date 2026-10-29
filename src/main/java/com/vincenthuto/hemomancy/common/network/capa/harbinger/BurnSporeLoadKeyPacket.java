package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoadService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client to server: the Burn Spores key. Carries nothing; the service checks every gate. */
public record BurnSporeLoadKeyPacket() implements CustomPacketPayload {
	public static final Type<BurnSporeLoadKeyPacket> TYPE = new Type<>(Hemomancy.rloc("burn_spore_load_key"));
	public static final StreamCodec<FriendlyByteBuf, BurnSporeLoadKeyPacket> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> { }, buffer -> new BurnSporeLoadKeyPacket());

	public static void handle(BurnSporeLoadKeyPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			if (context.player() instanceof ServerPlayer player) SporeLoadService.burn(player);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
