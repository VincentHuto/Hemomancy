package com.vincenthuto.hemomancy.common.network.capa;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.HashMap;
import java.util.Map;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public class BloodTendencyServerPacket implements CustomPacketPayload {

	public static final Type<BloodTendencyServerPacket> TYPE = new Type<>(Hemomancy.rloc("blood_tendency_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, BloodTendencyServerPacket> STREAM_CODEC = StreamCodec.of(BloodTendencyServerPacket::encode, BloodTendencyServerPacket::decode);
	public static BloodTendencyServerPacket decode(final FriendlyByteBuf packetBuffer) {
		Map<EnumBloodTendency, Float> devo = new HashMap<>();
		for (EnumBloodTendency key : EnumBloodTendency.values()) {
			devo.put(key, packetBuffer.readNbt().getFloat(key.toString()));
		}
		return new BloodTendencyServerPacket(devo);
	}

	public static void encode(final FriendlyByteBuf packetBuffer, final BloodTendencyServerPacket msg) {
		CompoundTag covenTag = new CompoundTag();
		for (EnumBloodTendency key : EnumBloodTendency.values()) {
			if (msg.Tendency.get(key) != null) {
				covenTag.putFloat(key.toString(), msg.Tendency.get(key));
				packetBuffer.writeNbt(covenTag);
			} else {
				covenTag.putFloat(key.toString(), 0);
				packetBuffer.writeNbt(covenTag);

			}
		}
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final BloodTendencyServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null) {
				return;
			}

			HemoCapabilityAccess.getBloodTendency(mc.player)
					.orElseThrow(IllegalStateException::new).setTendency(msg.Tendency);

		});
	}

	private Map<EnumBloodTendency, Float> Tendency = new HashMap<>();

	public BloodTendencyServerPacket(Map<EnumBloodTendency, Float> TendencyIn) {
		this.Tendency = TendencyIn;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
