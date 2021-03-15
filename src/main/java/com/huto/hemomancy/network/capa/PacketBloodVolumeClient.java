package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;
import com.huto.hemomancy.capabilities.bloodvolume.IBloodVolume;
import com.huto.hemomancy.network.PacketHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

public class PacketBloodVolumeClient {

	public PacketBloodVolumeClient() {

	}

	public static void handle(final PacketBloodVolumeClient msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IBloodVolume volume = sender.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElseThrow(IllegalStateException::new);
				// Send message back to the client to set the information
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> sender),
						new PacketBloodVolumeServer(volume.getBloodVolume()));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketBloodVolumeClient msg, final PacketBuffer packetBuffer) {

	}

	public static PacketBloodVolumeClient decode(final PacketBuffer packetBuffer) {
		return new PacketBloodVolumeClient();
	}
}