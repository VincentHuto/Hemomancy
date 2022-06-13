package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.Bloodline;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketBloodVolumeServer {

	private boolean active;
	private double max;
	private double volume;
	private Bloodline bloodline;

	public PacketBloodVolumeServer(IBloodVolume volume) {
		this.active = volume.isActive();
		this.max = volume.getMaxBloodVolume();
		this.volume = volume.getBloodVolume();
		this.bloodline = volume.getBloodLine();
	}

	public PacketBloodVolumeServer(boolean active, double maxIn, double volumeIn, Bloodline bloodline) {
		this.active = active;
		this.max = maxIn;
		this.volume = volumeIn;
		this.bloodline = bloodline;
	}

	public static void handle(final PacketBloodVolumeServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {

			if (Minecraft.getInstance().player != null) {
				IBloodVolume capa = Minecraft.getInstance().player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				capa.setActive(msg.active);
				capa.setMaxBloodVolume(msg.max);
				capa.setBloodVolume(msg.volume);
				capa.setBloodLine(msg.bloodline);
			}

		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketBloodVolumeServer msg, final FriendlyByteBuf packetBuffer) {
		packetBuffer.writeBoolean(msg.active);
		packetBuffer.writeDouble(msg.max);
		packetBuffer.writeDouble(msg.volume);
		packetBuffer.writeNbt(msg.bloodline.serialize());
	}

	public static PacketBloodVolumeServer decode(final FriendlyByteBuf packetBuffer) {
		return new PacketBloodVolumeServer(packetBuffer.readBoolean(), packetBuffer.readDouble(),
				packetBuffer.readDouble(), Bloodline.deserialize(packetBuffer.readNbt()));
	}
}