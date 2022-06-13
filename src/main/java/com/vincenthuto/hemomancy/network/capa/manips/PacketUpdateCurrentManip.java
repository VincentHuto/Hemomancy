package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class PacketUpdateCurrentManip {
	private int selected;

	public PacketUpdateCurrentManip(int selectedIn) {
		this.selected = selectedIn;

	}

	public static void encode(PacketUpdateCurrentManip msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.selected);
	}

	public static PacketUpdateCurrentManip decode(FriendlyByteBuf buf) {
		return new PacketUpdateCurrentManip(buf.readInt());
	}

	public static class Handler {
		public static void handle(final PacketUpdateCurrentManip msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Player player = ctx.get().getSender();
				if (player == null)
					return;
				if (!player.level.isClientSide) {
					IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
							.orElseThrow(NullPointerException::new);
					List<BloodManipulation> manips = known.getManipList();
					if (manips.get(msg.selected) != null) {
						known.setSelectedManip(manips.get(msg.selected));
						player.displayClientMessage(
								 Component.translatable("Selected:" + known.getManipList().get(msg.selected).getProperName()),
								true);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}