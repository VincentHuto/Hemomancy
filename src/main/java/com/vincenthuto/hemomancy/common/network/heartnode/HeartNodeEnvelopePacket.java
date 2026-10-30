package com.vincenthuto.hemomancy.common.network.heartnode;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.world.HeartNodeRelation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.UUID;

/**
 * The heart-node sanctums near a player in the Gardens, each with how the player stands to it. The whole list
 * replaces what the client held; an empty list clears it. A player's own node rides here too.
 */
public record HeartNodeEnvelopePacket(List<Envelope> envelopes) implements CustomPacketPayload {
	public static final Type<HeartNodeEnvelopePacket> TYPE = new Type<>(Hemomancy.rloc("heart_node_envelope"));
	/** More than any real neighbourhood holds; it only bounds what a client will decode. */
	private static final int MAX_ENVELOPES = 64;
	public static final StreamCodec<RegistryFriendlyByteBuf, HeartNodeEnvelopePacket> STREAM_CODEC =
			Envelope.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_ENVELOPES))
					.map(HeartNodeEnvelopePacket::new, HeartNodeEnvelopePacket::envelopes);

	public HeartNodeEnvelopePacket {
		envelopes = List.copyOf(envelopes);
	}

	/**
	 * One sanctum: its owner, node, synapses in the order raised, settings (toggle bits and intensity), the viewer's
	 * relation, when it was planted (Gardens game time) and its maturity stage.
	 */
	public record Envelope(UUID owner, BlockPos heart, List<BlockPos> synapses, int settingsBits, int intensity,
			HeartNodeRelation relation, long plantedAt, int stage) {
		private static final int MAX_SYNAPSES = 32;
		public static final StreamCodec<RegistryFriendlyByteBuf, Envelope> STREAM_CODEC = StreamCodec.of(
				Envelope::encode, Envelope::decode);

		public Envelope {
			synapses = List.copyOf(synapses);
		}

		private static void encode(RegistryFriendlyByteBuf buf, Envelope envelope) {
			UUIDUtil.STREAM_CODEC.encode(buf, envelope.owner);
			BlockPos.STREAM_CODEC.encode(buf, envelope.heart);
			BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_SYNAPSES)).encode(buf, envelope.synapses);
			buf.writeVarInt(envelope.settingsBits);
			buf.writeByte(envelope.intensity);
			HeartNodeRelation.STREAM_CODEC.encode(buf, envelope.relation);
			buf.writeVarLong(envelope.plantedAt);
			buf.writeByte(envelope.stage);
		}

		private static Envelope decode(RegistryFriendlyByteBuf buf) {
			return new Envelope(UUIDUtil.STREAM_CODEC.decode(buf), BlockPos.STREAM_CODEC.decode(buf),
					BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_SYNAPSES)).decode(buf), buf.readVarInt(),
					buf.readByte(), HeartNodeRelation.STREAM_CODEC.decode(buf), buf.readVarLong(), buf.readByte());
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
