package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeEvents;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client → Server: Player updates their per-player bloodline pool settings
 * (trickle donation and auto-draw configuration).
 */
public class PacketUpdatePoolSettings {

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

	public static void handle(PacketUpdatePoolSettings msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player == null) return;

			player.getCapability(BloodVolumeProvider.VOLUME_CAPA).ifPresent(volume -> {
				volume.setTrickleEnabled(msg.trickleEnabled);
				volume.setTrickleRate(Math.max(0.01, Math.min(100.0, msg.trickleRate)));
				volume.setAutoDrawEnabled(msg.autoDrawEnabled);
				volume.setAutoDrawThreshold(Math.max(0.0, Math.min(1.0, msg.autoDrawThreshold)));
				BloodVolumeEvents.syncVolume(player, volume);
			});
		});
		ctx.get().setPacketHandled(true);
	}
}
