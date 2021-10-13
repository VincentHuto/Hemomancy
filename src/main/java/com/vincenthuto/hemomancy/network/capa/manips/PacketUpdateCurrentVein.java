package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.util.VeinLocation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketUpdateCurrentVein {
	private int selected;

	public PacketUpdateCurrentVein(int selectedIn) {
		this.selected = selectedIn;

	}

	public static void encode(PacketUpdateCurrentVein msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.selected);
	}

	public static PacketUpdateCurrentVein decode(FriendlyByteBuf buf) {
		return new PacketUpdateCurrentVein(buf.readInt());
	}

	public static class Handler {
		public static void handle(final PacketUpdateCurrentVein msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Player player = ctx.get().getSender();
				if (player == null)
					return;
				if (!player.level.isClientSide) {
					IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
							.orElseThrow(NullPointerException::new);
					List<VeinLocation> manips = known.getVeinList();
					if (manips.get(msg.selected) != null) {
						known.setSelectedVein(manips.get(msg.selected));
						player.displayClientMessage(
								new TextComponent("Selected:" + known.getVeinList().get(msg.selected).getName()), true);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}