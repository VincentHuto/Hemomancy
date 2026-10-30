package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAbilities;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.PossessionAction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client to server: a possessing will chose an entry on the possession wheel. The server checks the claim (possessing,
 * offered by the form, cooled) in {@link PossessionAbilities#act}. An unknown action ordinal does nothing.
 */
public record PossessionActionPacket(int action) implements CustomPacketPayload {
	public static final Type<PossessionActionPacket> TYPE = new Type<>(Hemomancy.rloc("possession_action"));
	public static final StreamCodec<FriendlyByteBuf, PossessionActionPacket> STREAM_CODEC = StreamCodec.of(
			(buffer, packet) -> buffer.writeVarInt(packet.action()),
			buffer -> new PossessionActionPacket(buffer.readVarInt()));

	public static PossessionActionPacket of(PossessionAction action) {
		return new PossessionActionPacket(action.ordinal());
	}

	public static void handle(PossessionActionPacket packet, IPayloadContext context) {
		context.enqueueWork(() -> {
			PossessionAction action = PossessionAction.byOrdinal(packet.action());
			if (action != null && context.player() instanceof ServerPlayer player) PossessionAbilities.act(player, action);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
