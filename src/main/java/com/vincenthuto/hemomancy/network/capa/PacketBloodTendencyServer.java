package com.vincenthuto.hemomancy.network.capa;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.tendency.BloodTendencyProvider;
import com.vincenthuto.hemomancy.capa.tendency.EnumBloodTendency;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketBloodTendencyServer {
	private Map<EnumBloodTendency, Float> Tendency = new HashMap<>();

	public PacketBloodTendencyServer(Map<EnumBloodTendency, Float> TendencyIn) {
		this.Tendency = TendencyIn;
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final PacketBloodTendencyServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();

			Minecraft.getInstance().player.getCapability(BloodTendencyProvider.TENDENCY_CAPA)
					.orElseThrow(IllegalStateException::new).setTendency(msg.Tendency);

		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketBloodTendencyServer msg, final FriendlyByteBuf packetBuffer) {
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

	public static PacketBloodTendencyServer decode(final FriendlyByteBuf packetBuffer) {
		Map<EnumBloodTendency, Float> devo = new HashMap<>();
		for (EnumBloodTendency key : EnumBloodTendency.values()) {
			devo.put(key, packetBuffer.readNbt().getFloat(key.toString()));
		}
		return new PacketBloodTendencyServer(devo);
	}
}