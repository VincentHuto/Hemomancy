package com.vincenthuto.hemomancy.common.network.lidless;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * A player's Lidless crossing, for the crossing player and everyone tracking them: whether it is on, which
 * doorway, which way through it, what waits at the far end, how far along, and how long the crossing is.
 */
public record LidlessPassageStatePacket(ResourceLocation dimension, int playerId, long token, boolean active,
		BlockPos door, Direction facing, boolean towardFront, LidlessPassageService.Mode mode,
		com.vincenthuto.hemomancy.common.block.shared.LidlessTheme theme, float progress,
		float length) implements CustomPacketPayload {
	public static final Type<LidlessPassageStatePacket> TYPE = new Type<>(Hemomancy.rloc("lidless_passage_state"));
	public static final StreamCodec<FriendlyByteBuf, LidlessPassageStatePacket> STREAM_CODEC = StreamCodec.of(
			(b, p) -> {
				b.writeResourceLocation(p.dimension);
				b.writeVarInt(p.playerId);
				b.writeLong(p.token);
				b.writeBoolean(p.active);
				b.writeBlockPos(p.door);
				b.writeEnum(p.facing);
				b.writeBoolean(p.towardFront);
				b.writeEnum(p.mode);
				b.writeEnum(p.theme);
				b.writeFloat(p.progress);
				b.writeFloat(p.length);
			},
			b -> new LidlessPassageStatePacket(b.readResourceLocation(), b.readVarInt(), b.readLong(), b.readBoolean(),
					b.readBlockPos(), b.readEnum(Direction.class), b.readBoolean(),
					b.readEnum(LidlessPassageService.Mode.class),
					b.readEnum(com.vincenthuto.hemomancy.common.block.shared.LidlessTheme.class), b.readFloat(), b.readFloat()));

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
