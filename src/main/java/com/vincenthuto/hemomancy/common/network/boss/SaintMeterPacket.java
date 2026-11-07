package com.vincenthuto.hemomancy.common.network.boss;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * One challenger's side of a kit Saint's meter: which Saint they are fighting ({@code saintId}, its entity id), their
 * own value and maximum (a per-player meter's, or their state beside a Saint-owned one), a secondary value, the Saint's
 * flag word (bits 0..15 are the Saint's, the rest the kit's) and a state countdown. {@link #CLEAR} in the flags tells
 * the client the fight is over for it. The only kit payload; {@code SaintMeterSync} sends it.
 */
public record SaintMeterPacket(int saintId, float value, float max, float secondary, float secondaryMax, int flags,
		int stateTicks) implements CustomPacketPayload {
	/** The kit's bit: the player left the roster, or the trial ended or reset. */
	public static final int CLEAR = 1 << 31;
	/** The Saint's own flag bits. */
	public static final int SAINT_FLAGS = 0xFFFF;

	public static final Type<SaintMeterPacket> TYPE = new Type<>(Hemomancy.rloc("saint_meter"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SaintMeterPacket> STREAM_CODEC = StreamCodec.of(
			(buf, packet) -> {
				buf.writeVarInt(packet.saintId);
				buf.writeFloat(packet.value);
				buf.writeFloat(packet.max);
				buf.writeFloat(packet.secondary);
				buf.writeFloat(packet.secondaryMax);
				buf.writeInt(packet.flags);
				buf.writeVarInt(packet.stateTicks);
			},
			buf -> new SaintMeterPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
					buf.readFloat(), buf.readInt(), buf.readVarInt()));

	public SaintMeterPacket {
		stateTicks = Math.max(0, stateTicks);
	}

	/** The packet that ends the fight on one client. */
	public static SaintMeterPacket clear(int saintId) {
		return new SaintMeterPacket(saintId, 0.0F, 1.0F, 0.0F, 1.0F, CLEAR, 0);
	}

	public boolean clears() {
		return (flags & CLEAR) != 0;
	}

	/** The Saint's own flag bits, without the kit's. */
	public int saintFlags() {
		return flags & SAINT_FLAGS;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
