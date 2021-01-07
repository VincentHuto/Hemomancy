package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.network.PacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class BloodVolumePacketClient {

	public BloodVolumePacketClient() {

	}

	public static void handle(final BloodVolumePacketClient msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IBloodVolume volume = sender.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
				// Send message back to the client to set the information
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> sender),
						new BloodVolumePacketServer(volume.getBloodVolume()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final BloodVolumePacketClient msg, final PacketBuffer packetBuffer) {

	}

	public static BloodVolumePacketClient decode(final PacketBuffer packetBuffer) {
		return new BloodVolumePacketClient();
	}
}