package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;

import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Client → Server: Player updates their per-player bloodline pool settings
 * (trickle donation and auto-draw configuration).
 */
public class PacketUpdatePoolSettings implements CustomPacketPayload {

	public static final Type<PacketUpdatePoolSettings> TYPE = new Type<>(Hemomancy.rloc("packet_update_pool_settings"));
	public static final StreamCodec<FriendlyByteBuf, PacketUpdatePoolSettings> STREAM_CODEC = StreamCodec.of(PacketUpdatePoolSettings::encode, PacketUpdatePoolSettings::decode);

	private final boolean trickleEnabled;
	private final double trickleRate;
	private final boolean autoDrawEnabled;
	private final double autoDrawThreshold;

	public PacketUpdatePoolSettings(boolean trickleEnabled, double trickleRate,
									boolean autoDrawEnabled, double autoDrawThreshold) {
		this.trickleEnabled = trickleEnabled;
		this.trickleRate = trickleRate;
		this.autoDrawEnabled = autoDrawEnabled;
		this.autoDrawThreshold = autoDrawThreshold;
	}

	public static void encode(PacketUpdatePoolSettings msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.trickleEnabled);
		buf.writeDouble(msg.trickleRate);
		buf.writeBoolean(msg.autoDrawEnabled);
		buf.writeDouble(msg.autoDrawThreshold);
	}

	public static PacketUpdatePoolSettings decode(FriendlyByteBuf buf) {
		return new PacketUpdatePoolSettings(
				buf.readBoolean(), buf.readDouble(),
				buf.readBoolean(), buf.readDouble());
	}

	public static void handle(final PacketUpdatePoolSettings msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ServerPlayer player = ctx.player();
			if (player == null) return;

			HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
				volume.setTrickleEnabled(msg.trickleEnabled);
				volume.setTrickleRate(Math.max(0.01, Math.min(100.0, msg.trickleRate)));
				volume.setAutoDrawEnabled(msg.autoDrawEnabled);
				volume.setAutoDrawThreshold(Math.max(0.0, Math.min(1.0, msg.autoDrawThreshold)));
				BloodVolumeEvents.syncVolume(player, volume);
			});
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
