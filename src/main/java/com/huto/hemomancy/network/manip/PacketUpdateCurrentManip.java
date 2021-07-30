package com.huto.hemomancy.network.manip;

import java.util.List;
import java.util.function.Supplier;

import com.huto.hemomancy.capa.manip.IKnownManipulations;
import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;

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
				if (!player.world.isRemote) {
					IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
							.orElseThrow(NullPointerException::new);
					List<BloodManipulation> manips = known.getKnownManips();
					if (manips.get(msg.selected) != null) {
						known.setSelectedManip(manips.get(msg.selected));
						player.sendStatusMessage(new StringTextComponent(
								"Selected:" + known.getKnownManips().get(msg.selected).getProperName()), true);
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}
}