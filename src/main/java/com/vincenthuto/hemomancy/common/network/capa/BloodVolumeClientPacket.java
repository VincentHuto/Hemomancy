package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodVolumeClientPacket {

	public static BloodVolumeClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new BloodVolumeClientPacket();
	}

	public static void encode(final BloodVolumeClientPacket msg, final FriendlyByteBuf packetBuffer) {

	}

	public static void handle(final BloodVolumeClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IBloodVolume volume = sender.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(IllegalStateException::new);
				// Send message back to the client to set the information
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> sender),
						new BloodVolumeServerPacket(volume));
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> sender),
						new PacketSyncPomeProgress(computePomeProgress(sender)));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	private static int computePomeProgress(ServerPlayer player) {
		return player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
				.map(degree -> degree.isQliphothCommunionDone() ? 9 : degree.getTotalPomesConsumed())
				.orElse(0);
	}

	public BloodVolumeClientPacket() {

	}
}
