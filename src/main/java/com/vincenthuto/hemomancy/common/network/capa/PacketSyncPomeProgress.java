package com.vincenthuto.hemomancy.common.network.capa;

import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PacketSyncPomeProgress {

	private final int progress;

	public PacketSyncPomeProgress(int progress) {
		this.progress = progress;
	}

	public static void encode(PacketSyncPomeProgress msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.progress);
	}

	public static PacketSyncPomeProgress decode(FriendlyByteBuf buf) {
		return new PacketSyncPomeProgress(buf.readInt());
	}

	public static void handle(PacketSyncPomeProgress msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			if (Minecraft.getInstance().player != null) {
				Minecraft.getInstance().player.getCapability(InitiatoryDegreeProvider.DEGREE_CAPA)
						.ifPresent(degree -> degree.syncTotalPomesConsumed(msg.progress));
			}
		});
		ctx.get().setPacketHandled(true);
	}
}
