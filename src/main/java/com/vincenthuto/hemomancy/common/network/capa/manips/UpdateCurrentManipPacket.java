package com.vincenthuto.hemomancy.common.network.capa.manips;

import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class UpdateCurrentManipPacket {
	public static class Handler {
		public static void handle(final UpdateCurrentManipPacket msg, Supplier<NetworkEvent.Context> ctx) {
			ctx.get().enqueueWork(() -> {
				Player player = ctx.get().getSender();
				if (player == null)
					return;
				if (!player.level().isClientSide) {
					IKnownManipulations known = player.getCapability(KnownManipulationProvider.MANIP_CAPA)
							.orElseThrow(NullPointerException::new);
					List<BloodManipulation> manips = known.getManipList();
					System.out.println(msg.selected);
					if (manips.get(msg.selected) != null) {
						known.setSelectedManip(manips.get(msg.selected));
						player.displayClientMessage(
								Component.literal("Selected:" + known.getManipList().get(msg.selected).getProperName()),
								true);
						PacketHandler.CHANNELKNOWNMANIPS.send(
								PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
								new KnownManipulationServerPacket(known));
					}
				}
			});
			ctx.get().setPacketHandled(true);
		}
	}

	public static UpdateCurrentManipPacket decode(FriendlyByteBuf buf) {
		return new UpdateCurrentManipPacket(buf.readInt());
	}

	public static void encode(UpdateCurrentManipPacket msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.selected);
	}

	private int selected;

	public UpdateCurrentManipPacket(int selectedIn) {
		this.selected = selectedIn;

	}
}