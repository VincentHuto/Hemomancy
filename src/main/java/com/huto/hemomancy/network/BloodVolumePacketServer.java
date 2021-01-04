package com.huto.hemomancy.network;

import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.bloodvolume.BloodVolumeProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class BloodVolumePacketServer {
	private float volume;

	public BloodVolumePacketServer(float volumeIn) {
		this.volume = volumeIn;
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final BloodVolumePacketServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
			Minecraft.getInstance().player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new).setBloodVolume(msg.volume);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final BloodVolumePacketServer msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeFloat(msg.volume);
	}

	public static BloodVolumePacketServer decode(final PacketBuffer packetBuffer) {
		return new BloodVolumePacketServer(packetBuffer.readFloat());
	}
}