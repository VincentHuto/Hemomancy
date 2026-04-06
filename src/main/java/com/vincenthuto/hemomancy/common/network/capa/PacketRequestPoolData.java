package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/**
 * Client → Server: Request the server to send the latest bloodline pool data.
 */
public class PacketRequestPoolData {

	public PacketRequestPoolData() {
	}

	public static void encode(PacketRequestPoolData msg, FriendlyByteBuf buf) {
	}

	public static PacketRequestPoolData decode(FriendlyByteBuf buf) {
		return new PacketRequestPoolData();
	}

	public static void handle(PacketRequestPoolData msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player == null) return;

			player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
				Bloodline bloodline = volume.getBloodLine();
				if (bloodline.isValid()) {
					ServerLevel overworld = player.server.overworld();
					BloodlineSavedData savedData = BloodlineSavedData.get(overworld);
					Bloodline globalLine = savedData.getBloodline(bloodline.getBloodlineUUID());
					if (globalLine != null) {
						PacketHandler.CHANNELBLOODVOLUME.send(
								PacketDistributor.PLAYER.with(() -> player),
								new PacketSyncBloodlinePool(globalLine.getBloodVolume(),
										globalLine.getMaxBloodVolume(),
										globalLine.getPlayerUUIDS().size()));
					}
				}
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
