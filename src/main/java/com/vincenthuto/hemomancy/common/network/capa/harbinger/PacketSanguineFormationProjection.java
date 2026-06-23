package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.ActiveSanguineFormationProjectionClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSanguineFormationProjection(BlockPos pos, Direction face, float progress, int visibleTicks,
		State state, boolean attuned, long seed) implements CustomPacketPayload {
	public static final Type<PacketSanguineFormationProjection> TYPE =
			new Type<>(Hemomancy.rloc("packet_sanguine_formation_projection"));
	public static final StreamCodec<FriendlyByteBuf, PacketSanguineFormationProjection> STREAM_CODEC =
			StreamCodec.of(PacketSanguineFormationProjection::encode, PacketSanguineFormationProjection::decode);

	public static void encode(FriendlyByteBuf buf, PacketSanguineFormationProjection msg) {
		buf.writeBlockPos(msg.pos);
		buf.writeEnum(msg.face);
		buf.writeFloat(msg.progress);
		buf.writeVarInt(msg.visibleTicks);
		buf.writeEnum(msg.state);
		buf.writeBoolean(msg.attuned);
		buf.writeLong(msg.seed);
	}

	public static PacketSanguineFormationProjection decode(FriendlyByteBuf buf) {
		return new PacketSanguineFormationProjection(buf.readBlockPos(), buf.readEnum(Direction.class),
				buf.readFloat(), buf.readVarInt(), buf.readEnum(State.class), buf.readBoolean(), buf.readLong());
	}

	public static void handle(final PacketSanguineFormationProjection msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> ActiveSanguineFormationProjectionClientData.upsert(msg.pos, msg.face, msg.progress,
				msg.visibleTicks, msg.state, msg.attuned, msg.seed));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public enum State {
		FORMING,
		COMPLETE,
		FAILURE
	}
}
