package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

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

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public BlockPos center() {
		return center;
	}

	public float centerY() {
		return centerY;
	}

	public int durationTicks() {
		return durationTicks;
	}

	public float startRadius() {
		return startRadius;
	}
}
