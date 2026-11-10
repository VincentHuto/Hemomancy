package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public class BloodTemperamentServerPacket implements CustomPacketPayload {

	public static final Type<BloodTemperamentServerPacket> TYPE = new Type<>(Hemomancy.rloc("blood_temperament_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodTemperamentServerPacket> STREAM_CODEC = StreamCodec.of(BloodTemperamentServerPacket::encode, BloodTemperamentServerPacket::decode);
	public static BloodTemperamentServerPacket decode(final FriendlyByteBuf packetBuffer) {
		Map<EnumBloodTemper, Float> devo = new HashMap<>();
		for (EnumBloodTemper key : EnumBloodTemper.values()) {
			devo.put(key, packetBuffer.readNbt().getFloat(key.toString()));
		}
		return new BloodTemperamentServerPacket(devo);
	}

	public static void encode(final FriendlyByteBuf packetBuffer, final BloodTemperamentServerPacket msg) {
		CompoundTag covenTag = new CompoundTag();
		for (EnumBloodTemper key : EnumBloodTemper.values()) {
			if (msg.Temper.get(key) != null) {
				covenTag.putFloat(key.toString(), msg.Temper.get(key));
				packetBuffer.writeNbt(covenTag);
			} else {
				covenTag.putFloat(key.toString(), 0);
				packetBuffer.writeNbt(covenTag);

			}
		}
	}

	public static void handle(final BloodTemperamentServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}

			HemoCapabilityAccess.getBloodTemperament(player)
					.setTemper(msg.Temper);

		});
	}

	private Map<EnumBloodTemper, Float> Temper = new HashMap<>();

	public BloodTemperamentServerPacket(Map<EnumBloodTemper, Float> TemperIn) {
		this.Temper = TemperIn;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
