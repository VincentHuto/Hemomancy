package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketSyncPomeProgress implements CustomPacketPayload {

	public static final Type<PacketSyncPomeProgress> TYPE =
			new Type<>(Hemomancy.rloc("sync_pome_progress"));
	public static final StreamCodec<FriendlyByteBuf, PacketSyncPomeProgress> STREAM_CODEC =
			StreamCodec.of(PacketSyncPomeProgress::encode, PacketSyncPomeProgress::decode);

	private final int progress;

	public PacketSyncPomeProgress(int progress) {
		this.progress = progress;
	}

	public static PacketSyncPomeProgress decode(FriendlyByteBuf buf) {
		return new PacketSyncPomeProgress(buf.readInt());
	}

	public static void encode(FriendlyByteBuf buf, PacketSyncPomeProgress msg) {
		buf.writeInt(msg.progress);
	}

	public static void handle(PacketSyncPomeProgress msg, IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			if (Minecraft.getInstance().player != null) {
				HemoCapabilityAccess.getInitiatoryDegree(Minecraft.getInstance().player)
						.ifPresent(degree -> degree.syncTotalPomesConsumed(msg.progress));
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
