package com.vincenthuto.hemomancy.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.capa.volume.IBloodVolume;
import com.vincenthuto.hemomancy.network.PacketHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class BloodVolumeClientPacket {

	public BloodVolumeClientPacket() {

	}

	public static void handle(final BloodVolumeClientPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet
			if (sender != null) {
				IBloodVolume volume = sender.getCapability(BloodVolumeProvider.VOLUME_CAPA)
						.orElseThrow(IllegalStateException::new);
				// Send message back to the client to set the information
				PacketHandler.CHANNELBLOODVOLUME.send(PacketDistributor.PLAYER.with(() -> sender),
						new BloodVolumeServerPacket(volume));
			}
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final BloodVolumeClientPacket msg, final FriendlyByteBuf packetBuffer) {

	}

	public static BloodVolumeClientPacket decode(final FriendlyByteBuf packetBuffer) {
		return new BloodVolumeClientPacket();
	}
}