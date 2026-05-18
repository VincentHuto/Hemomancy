package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DisplayKnownManipsPacket implements CustomPacketPayload {

	public static final Type<DisplayKnownManipsPacket> TYPE = new Type<>(Hemomancy.rloc("display_known_manips_packet"));
	public static final StreamCodec<FriendlyByteBuf, DisplayKnownManipsPacket> STREAM_CODEC = StreamCodec.of(DisplayKnownManipsPacket::encode, DisplayKnownManipsPacket::decode);

	public static DisplayKnownManipsPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new DisplayKnownManipsPacket();
	}

	public static void encode(final FriendlyByteBuf buffer, final DisplayKnownManipsPacket message) {
		buffer.writeByte(0);
	}

	public static void handle(final DisplayKnownManipsPacket message, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null)
				return;
			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player)
					.orElseThrow(NullPointerException::new);
			player.displayClientMessage(Component.literal("Selected: " + known.getSelectedManip().getProperName()),
					false);
			java.util.List<String> equipped = known.getEquippedManipNames();
			for (int i = 0; i < known.getKnownManips().size(); i++) {
				String name = known.getManipList().get(i).getName();
				String tag = equipped.contains(name) ? " [EQUIPPED]" : "";
				player.displayClientMessage(
						Component.literal("Manipulation " + i + ": " + known.getManipList().get(i).getProperName() + tag),
						false);
			}
		});
	}

	public DisplayKnownManipsPacket() {
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
