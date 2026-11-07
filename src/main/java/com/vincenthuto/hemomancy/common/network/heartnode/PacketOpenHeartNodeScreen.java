package com.vincenthuto.hemomancy.common.network.heartnode;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.MoltingRules;
import com.vincenthuto.hemomancy.common.world.HeartNodeSettings;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.UUID;

/**
 * Server to client: opens the Heart-node screen on the node at {@code pos} with the state only the server knows. The
 * owner gets it editable; a creative operator looking at someone else's node gets it {@code readOnly}. {@code molt}
 * is what the Molting screen starts from.
 */
public record PacketOpenHeartNodeScreen(BlockPos pos, UUID owner, String ownerName, List<Synapse> synapses,
		HeartNodeSettings settings, long plantedAt, boolean respawnHere, List<BlockPos> caches, boolean readOnly,
		Molt molt) implements CustomPacketPayload {
	public static final Type<PacketOpenHeartNodeScreen> TYPE = new Type<>(Hemomancy.rloc("open_heart_node_screen"));
	private static final int MAX_LIST = 64;
	public static final StreamCodec<RegistryFriendlyByteBuf, PacketOpenHeartNodeScreen> STREAM_CODEC = StreamCodec.of(
			PacketOpenHeartNodeScreen::encode, PacketOpenHeartNodeScreen::decode);

	public PacketOpenHeartNodeScreen {
		synapses = List.copyOf(synapses);
		caches = List.copyOf(caches);
	}

	/**
	 * One synapse in the owner's record: where, the facing of its block ({@link #UNKNOWN_FACING} when its chunk is not
	 * loaded), and whether it still chains back to the heart.
	 */
	public record Synapse(BlockPos pos, int facing, boolean connected) {
		public static final int UNKNOWN_FACING = -1;
	}

	/**
	 * The owner's grown alignment per tendency (in {@code EnumBloodTendency} order, scars left out), their spore load
	 * and how long their skin stays soft from the last molt.
	 */
	public record Molt(List<Float> base, float spores, long softTicks) {
		public static final Molt NONE = new Molt(List.of(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f), 0f, 0L);

		public Molt {
			if (base.size() != MoltingRules.TENDENCIES) throw new IllegalArgumentException("one value per tendency");
			base = List.copyOf(base);
		}

		public float[] baseArray() {
			float[] out = new float[base.size()];
			for (int i = 0; i < out.length; i++) out[i] = base.get(i);
			return out;
		}
	}

	private static void encode(RegistryFriendlyByteBuf buf, PacketOpenHeartNodeScreen packet) {
		BlockPos.STREAM_CODEC.encode(buf, packet.pos);
		UUIDUtil.STREAM_CODEC.encode(buf, packet.owner);
		buf.writeUtf(packet.ownerName, 64);
		buf.writeVarInt(packet.synapses.size());
		for (Synapse synapse : packet.synapses) {
			BlockPos.STREAM_CODEC.encode(buf, synapse.pos);
			buf.writeByte(synapse.facing);
			buf.writeBoolean(synapse.connected);
		}
		buf.writeVarInt(packet.settings.bits());
		buf.writeByte(packet.settings.intensity());
		buf.writeVarLong(packet.plantedAt);
		buf.writeBoolean(packet.respawnHere);
		BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LIST)).encode(buf, packet.caches);
		buf.writeBoolean(packet.readOnly);
		for (float value : packet.molt.base) buf.writeFloat(value);
		buf.writeFloat(packet.molt.spores);
		buf.writeVarLong(packet.molt.softTicks);
	}

	private static PacketOpenHeartNodeScreen decode(RegistryFriendlyByteBuf buf) {
		BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
		UUID owner = UUIDUtil.STREAM_CODEC.decode(buf);
		String name = buf.readUtf(64);
		int count = buf.readVarInt();
		if (count < 0 || count > MAX_LIST) throw new DecoderException("Too many synapses: " + count);
		Synapse[] synapses = new Synapse[count];
		for (int i = 0; i < count; i++) {
			synapses[i] = new Synapse(BlockPos.STREAM_CODEC.decode(buf), buf.readByte(), buf.readBoolean());
		}
		HeartNodeSettings settings = new HeartNodeSettings(buf.readVarInt(), buf.readByte());
		long plantedAt = buf.readVarLong();
		boolean respawnHere = buf.readBoolean();
		List<BlockPos> caches = BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LIST)).decode(buf);
		boolean readOnly = buf.readBoolean();
		Float[] base = new Float[MoltingRules.TENDENCIES];
		for (int i = 0; i < base.length; i++) base[i] = buf.readFloat();
		Molt molt = new Molt(List.of(base), buf.readFloat(), buf.readVarLong());
		return new PacketOpenHeartNodeScreen(pos, owner, name, List.of(synapses), settings, plantedAt, respawnHere,
				caches, readOnly, molt);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
