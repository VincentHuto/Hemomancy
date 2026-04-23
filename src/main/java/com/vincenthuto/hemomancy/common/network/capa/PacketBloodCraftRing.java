package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.client.data.ActiveBloodCraftClientData;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server → Client: Tells the client to spawn a collapsing bloody ring
 * animation at the given position when a blood structure recipe is crafted.
 */
public class PacketBloodCraftRing implements CustomPacketPayload {

	public static final Type<PacketBloodCraftRing> TYPE = new Type<>(Hemomancy.rloc("packet_blood_craft_ring"));
	public static final StreamCodec<FriendlyByteBuf, PacketBloodCraftRing> STREAM_CODEC = StreamCodec.of(PacketBloodCraftRing::encode, PacketBloodCraftRing::decode);

	private final BlockPos center;
	private final float startRadius;
	private final float centerY;
	private final int durationTicks;

	public PacketBloodCraftRing(BlockPos center, float startRadius, float centerY, int durationTicks) {
		this.center = center;
		this.startRadius = startRadius;
		this.centerY = centerY;
		this.durationTicks = durationTicks;
	}

	public static void encode(FriendlyByteBuf buf, PacketBloodCraftRing msg) {
		buf.writeBlockPos(msg.center);
		buf.writeFloat(msg.startRadius);
		buf.writeFloat(msg.centerY);
		buf.writeInt(msg.durationTicks);
	}

	public static PacketBloodCraftRing decode(FriendlyByteBuf buf) {
		BlockPos center = buf.readBlockPos();
		float startRadius = buf.readFloat();
		float centerY = buf.readFloat();
		int durationTicks = buf.readInt();
		return new PacketBloodCraftRing(center, startRadius, centerY, durationTicks);
	}

	public static void handle(final PacketBloodCraftRing msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			ActiveBloodCraftClientData.addRing(new ActiveBloodCraftClientData.CraftRingEntry(
					msg.center, msg.startRadius, msg.centerY, msg.durationTicks));
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
