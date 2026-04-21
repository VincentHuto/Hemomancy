package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.volume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;

public class BloodVolumeServerPacket implements CustomPacketPayload {

	public static final Type<BloodVolumeServerPacket> TYPE = new Type<>(Hemomancy.rloc("blood_volume_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodVolumeServerPacket> STREAM_CODEC = StreamCodec.of(BloodVolumeServerPacket::encode, BloodVolumeServerPacket::decode);

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
	public static void handle(final BloodVolumeServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {

			if (Minecraft.getInstance().player != null) {
				IBloodVolume capa = HemoCapabilityAccess.getBloodVolume(Minecraft.getInstance().player)
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

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
