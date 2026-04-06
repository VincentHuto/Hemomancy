package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class BloodVolumeServerPacket {

	public static BloodVolumeServerPacket decode(final FriendlyByteBuf packetBuffer) {
		return new BloodVolumeServerPacket(packetBuffer.readBoolean(), packetBuffer.readDouble(),
				packetBuffer.readDouble(), Bloodline.deserialize(packetBuffer.readNbt()),
				packetBuffer.readBoolean(), packetBuffer.readDouble(),
				packetBuffer.readBoolean(), packetBuffer.readDouble());
	}
	public static void encode(final BloodVolumeServerPacket msg, final FriendlyByteBuf packetBuffer) {
		packetBuffer.writeBoolean(msg.active);
		packetBuffer.writeDouble(msg.max);
		packetBuffer.writeDouble(msg.volume);
		packetBuffer.writeNbt(msg.bloodline.serialize());
		packetBuffer.writeBoolean(msg.trickleEnabled);
		packetBuffer.writeDouble(msg.trickleRate);
		packetBuffer.writeBoolean(msg.autoDrawEnabled);
		packetBuffer.writeDouble(msg.autoDrawThreshold);
	}
	public static void handle(final BloodVolumeServerPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {

			if (Minecraft.getInstance().player != null) {
				IBloodVolume capa = Minecraft.getInstance().player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(NullPointerException::new);
				capa.setActive(msg.active);
				capa.setMaxBloodVolume(msg.max);
				capa.setBloodVolume(msg.volume);
				capa.setBloodLine(msg.bloodline);
				capa.setTrickleEnabled(msg.trickleEnabled);
				capa.setTrickleRate(msg.trickleRate);
				capa.setAutoDrawEnabled(msg.autoDrawEnabled);
				capa.setAutoDrawThreshold(msg.autoDrawThreshold);
			}

		});
		ctx.get().setPacketHandled(true);
	}
	private boolean active;

	private double max;

	private double volume;

	private Bloodline bloodline;

	private boolean trickleEnabled;
	private double trickleRate;
	private boolean autoDrawEnabled;
	private double autoDrawThreshold;

	public BloodVolumeServerPacket(boolean active, double maxIn, double volumeIn, Bloodline bloodline,
								   boolean trickleEnabled, double trickleRate,
								   boolean autoDrawEnabled, double autoDrawThreshold) {
		this.active = active;
		this.max = maxIn;
		this.volume = volumeIn;
		this.bloodline = bloodline;
		this.trickleEnabled = trickleEnabled;
		this.trickleRate = trickleRate;
		this.autoDrawEnabled = autoDrawEnabled;
		this.autoDrawThreshold = autoDrawThreshold;
	}

	public BloodVolumeServerPacket(IBloodVolume volume) {
		this.active = volume.isActive();
		this.max = volume.getMaxBloodVolume();
		this.volume = volume.getBloodVolume();
		this.bloodline = volume.getBloodLine();
		this.trickleEnabled = volume.isTrickleEnabled();
		this.trickleRate = volume.getTrickleRate();
		this.autoDrawEnabled = volume.isAutoDrawEnabled();
		this.autoDrawThreshold = volume.getAutoDrawThreshold();
	}
}