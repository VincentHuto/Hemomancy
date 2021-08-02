package com.huto.hemomancy.network.capa;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.huto.hemomancy.capa.vascular.EnumVeinSections;
import com.huto.hemomancy.capa.vascular.VascularSystemProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketVascularSystemServer {
	private Map<EnumVeinSections, Float> vascularSystem = new HashMap<>();

	public PacketVascularSystemServer(Map<EnumVeinSections, Float> vascularSystemIn) {
		this.vascularSystem = vascularSystemIn;
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final PacketVascularSystemServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();

			Minecraft.getInstance().player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
					.orElseThrow(IllegalStateException::new).setVascularSystem(msg.vascularSystem);

		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketVascularSystemServer msg, final FriendlyByteBuf packetBuffer) {
		CompoundTag covenTag = new CompoundTag();
		for (EnumVeinSections key : EnumVeinSections.values()) {
			if (msg.vascularSystem.get(key) != null) {
				covenTag.putFloat(key.toString(), msg.vascularSystem.get(key));
				packetBuffer.writeNbt(covenTag);
			} else {
				covenTag.putFloat(key.toString(), 100f);
				packetBuffer.writeNbt(covenTag);

			}
		}
	}

	public static PacketVascularSystemServer decode(final FriendlyByteBuf packetBuffer) {
		Map<EnumVeinSections, Float> devo = new HashMap<>();
		for (EnumVeinSections key : EnumVeinSections.values()) {
			devo.put(key, packetBuffer.readNbt().getFloat(key.toString()));
		}
		return new PacketVascularSystemServer(devo);
	}
}