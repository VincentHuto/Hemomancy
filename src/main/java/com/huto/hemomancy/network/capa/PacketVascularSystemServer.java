package com.huto.hemomancy.network.capa;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.vascularsystem.EnumVeinSections;
import com.huto.hemomancy.capabilities.vascularsystem.VascularSystemProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketVascularSystemServer {
	private Map<EnumVeinSections, Float> vascularSystem = new HashMap<>();

	public PacketVascularSystemServer(Map<EnumVeinSections, Float> vascularSystemIn) {
		this.vascularSystem = vascularSystemIn;
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final PacketVascularSystemServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender();

			Minecraft.getInstance().player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
					.orElseThrow(IllegalStateException::new).setVascularSystem(msg.vascularSystem);

		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketVascularSystemServer msg, final PacketBuffer packetBuffer) {
		CompoundNBT covenTag = new CompoundNBT();
		for (EnumVeinSections key : EnumVeinSections.values()) {
			if (msg.vascularSystem.get(key) != null) {
				covenTag.putFloat(key.toString(), msg.vascularSystem.get(key));
				packetBuffer.writeCompoundTag(covenTag);
			} else {
				covenTag.putFloat(key.toString(), 100f);
				packetBuffer.writeCompoundTag(covenTag);

			}
		}
	}

	public static PacketVascularSystemServer decode(final PacketBuffer packetBuffer) {
		Map<EnumVeinSections, Float> devo = new HashMap<>();
		for (EnumVeinSections key : EnumVeinSections.values()) {
			devo.put(key, packetBuffer.readCompoundTag().getFloat(key.toString()));
		}
		return new PacketVascularSystemServer(devo);
	}
}