package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class DisplayKnownManipsPacket {

	public DisplayKnownManipsPacket() {
	}

	public static DisplayKnownManipsPacket decode(final FriendlyByteBuf buffer) {
		buffer.readByte();
		return new DisplayKnownManipsPacket();
	}

	public static void encode(final DisplayKnownManipsPacket message, final FriendlyByteBuf buffer) {
		buffer.writeByte(0);
	}

	public static void handle(final DisplayKnownManipsPacket message, final Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Player player = ctx.get().getSender();
			if (player == null)
				return;
			IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(NullPointerException::new);
			player.displayClientMessage(Component.literal("Selected: " + known.getSelectedManip().getProperName()),
					false);
			for (int i = 0; i < known.getKnownManips().size(); i++) {
				player.displayClientMessage(
						Component.literal("Manipulation " + i + ": " + known.getManipList().get(i).getProperName()),
						false);
			}
		});
		ctx.get().setPacketHandled(true);
	}
}