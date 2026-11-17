package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.IBloodTemperament;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.EnumMap;
import java.util.Map;

/**
 * Server to owner: the Temperament, with the grown alignment and what worn scars lend sent apart, so the client can
 * tell them apart (core loop Track 4). Effective alignment is their sum on both sides.
 */
public class BloodTemperamentServerPacket implements CustomPacketPayload {

	public static final Type<BloodTemperamentServerPacket> TYPE = new Type<>(Hemomancy.rloc("blood_temperament_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodTemperamentServerPacket> STREAM_CODEC = StreamCodec.of(
			BloodTemperamentServerPacket::encode, BloodTemperamentServerPacket::decode);

	private final Map<EnumBloodTemper, Float> base;
	private final Map<EnumBloodTemper, Float> scars;

	public BloodTemperamentServerPacket(Map<EnumBloodTemper, Float> base, Map<EnumBloodTemper, Float> scars) {
		this.base = copy(base);
		this.scars = copy(scars);
	}

	/** The profile as it stands: grown alignment and scar alignment apart. */
	public static BloodTemperamentServerPacket of(IBloodTemperament temperament) {
		Map<EnumBloodTemper, Float> base = new EnumMap<>(EnumBloodTemper.class);
		Map<EnumBloodTemper, Float> scars = new EnumMap<>(EnumBloodTemper.class);
		for (EnumBloodTemper school : EnumBloodTemper.values()) {
			float grown = temperament.getBaseAlignment(school);
			base.put(school, grown);
			scars.put(school, temperament.getAlignmentByTemper(school) - grown);
		}
		return new BloodTemperamentServerPacket(base, scars);
	}

	private static Map<EnumBloodTemper, Float> copy(Map<EnumBloodTemper, Float> source) {
		Map<EnumBloodTemper, Float> copy = new EnumMap<>(EnumBloodTemper.class);
		for (EnumBloodTemper school : EnumBloodTemper.values()) {
			Float value = source == null ? null : source.get(school);
			copy.put(school, value == null ? 0.0F : value);
		}
		return copy;
	}

	public Map<EnumBloodTemper, Float> base() {
		return base;
	}

	public Map<EnumBloodTemper, Float> scars() {
		return scars;
	}

	private static BloodTemperamentServerPacket decode(FriendlyByteBuf buffer) {
		Map<EnumBloodTemper, Float> base = new EnumMap<>(EnumBloodTemper.class);
		Map<EnumBloodTemper, Float> scars = new EnumMap<>(EnumBloodTemper.class);
		for (EnumBloodTemper school : EnumBloodTemper.values()) base.put(school, buffer.readFloat());
		for (EnumBloodTemper school : EnumBloodTemper.values()) scars.put(school, buffer.readFloat());
		return new BloodTemperamentServerPacket(base, scars);
	}

	private static void encode(FriendlyByteBuf buffer, BloodTemperamentServerPacket packet) {
		for (EnumBloodTemper school : EnumBloodTemper.values()) buffer.writeFloat(packet.base.get(school));
		for (EnumBloodTemper school : EnumBloodTemper.values()) buffer.writeFloat(packet.scars.get(school));
	}

	public static void handle(final BloodTemperamentServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) return;
			var temperament = HemoCapabilityAccess.getBloodTemperament(player);
			temperament.setTemper(msg.base);
			temperament.setTransientAlignments(msg.scars);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
