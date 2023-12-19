package com.vincenthuto.hemomancy.common.network.capa;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class BloodTendencyServerPacket {
	public static BloodTendencyServerPacket decode(final FriendlyByteBuf packetBuffer) {
		Map<EnumBloodTendency, Float> devo = new HashMap<>();
		for (EnumBloodTendency key : EnumBloodTendency.values()) {
			devo.put(key, packetBuffer.readNbt().getFloat(key.toString()));
		}
		return new BloodTendencyServerPacket(devo);
	}

	public static void encode(final BloodTendencyServerPacket msg, final FriendlyByteBuf packetBuffer) {
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
	public static void handle(final BloodTendencyServerPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();

			Minecraft.getInstance().player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalStateException::new).setTendency(msg.Tendency);

		});
		ctx.get().setPacketHandled(true);
	}

	private Map<EnumBloodTendency, Float> Tendency = new HashMap<>();

	public BloodTendencyServerPacket(Map<EnumBloodTendency, Float> TendencyIn) {
		this.Tendency = TendencyIn;
	}
}