package com.huto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.huto.hemomancy.capa.volume.BloodVolumeProvider;
import com.huto.hemomancy.capa.volume.IBloodVolume;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketBloodVolumeServer {
	private boolean active;
	private float max;
	private float volume;

	public PacketBloodVolumeServer(IBloodVolume volume) {
		this.active = volume.isActive();
		this.max = volume.getMaxBloodVolume();
		this.volume = volume.getBloodVolume();
	}

	public PacketBloodVolumeServer(boolean active, float maxIn, float volumeIn) {
		this.active = active;
		this.max = maxIn;
		this.volume = volumeIn;
	}

	public static void handle(final PacketBloodVolumeServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			Minecraft.getInstance().player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new).setActive(msg.active);
			Minecraft.getInstance().player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new).setMaxBloodVolume(msg.max);
			Minecraft.getInstance().player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
					.orElseThrow(IllegalStateException::new).setBloodVolume(msg.volume);

		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketBloodVolumeServer msg, final FriendlyByteBuf packetBuffer) {
		packetBuffer.writeBoolean(msg.active);
		packetBuffer.writeFloat(msg.max);
		packetBuffer.writeFloat(msg.volume);
	}

	public static PacketBloodVolumeServer decode(final FriendlyByteBuf packetBuffer) {
		return new PacketBloodVolumeServer(packetBuffer.readBoolean(), packetBuffer.readFloat(),
				packetBuffer.readFloat());
	}
}