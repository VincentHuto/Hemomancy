package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.player.volume.IBloodVolume;

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

			if (Minecraft.getInstance().player != null) {
				IBloodVolume capa = Minecraft.getInstance().player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				capa.setActive(msg.active);
				capa.setMaxBloodVolume(msg.max);
				capa.setBloodVolume(msg.volume);
			}

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