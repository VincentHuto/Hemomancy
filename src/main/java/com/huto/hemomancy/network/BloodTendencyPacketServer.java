package com.huto.hemomancy.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.tendency.BloodTendencyProvider;
import com.huto.hemomancy.capabilities.tendency.EnumBloodTendency;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class BloodTendencyPacketServer {
	private Map<EnumBloodTendency, Float> Tendency = new HashMap<>();

	public BloodTendencyPacketServer(Map<EnumBloodTendency, Float> TendencyIn) {
		this.Tendency = TendencyIn;
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final BloodTendencyPacketServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender();

			Minecraft.getInstance().player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalStateException::new).setTendency(msg.Tendency);

		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final BloodTendencyPacketServer msg, final PacketBuffer packetBuffer) {
		CompoundNBT covenTag = new CompoundNBT();
		for (EnumBloodTendency key : EnumBloodTendency.values()) {
			if (msg.Tendency.get(key) != null) {
				covenTag.putFloat(key.toString(), msg.Tendency.get(key));
				packetBuffer.writeCompoundTag(covenTag);
			} else {
				covenTag.putFloat(key.toString(), 0);
				packetBuffer.writeCompoundTag(covenTag);

			}
		}
	}

	public static BloodTendencyPacketServer decode(final PacketBuffer packetBuffer) {
		Map<EnumBloodTendency, Float> devo = new HashMap<>();
		for (EnumBloodTendency key : EnumBloodTendency.values()) {
			devo.put(key, packetBuffer.readCompoundTag().getFloat(key.toString()));
		}
		return new BloodTendencyPacketServer(devo);
	}
}