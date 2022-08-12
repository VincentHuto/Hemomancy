package com.vincenthuto.hemomancy.network.capa;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.capa.player.vascular.VascularSystemProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class VascularSystemServerPacket {
	private Map<EnumVeinSections, Float> vascularSystem = new HashMap<>();

	public VascularSystemServerPacket(Map<EnumVeinSections, Float> vascularSystemIn) {
		this.vascularSystem = vascularSystemIn;
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final VascularSystemServerPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();

			Minecraft.getInstance().player.getCapability(VascularSystemProvider.VASCULAR_CAPA)
					.orElseThrow(IllegalStateException::new).setVascularSystem(msg.vascularSystem);

		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final VascularSystemServerPacket msg, final FriendlyByteBuf packetBuffer) {
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

	public static VascularSystemServerPacket decode(final FriendlyByteBuf packetBuffer) {
		Map<EnumVeinSections, Float> devo = new HashMap<>();
		for (EnumVeinSections key : EnumVeinSections.values()) {
			devo.put(key, packetBuffer.readNbt().getFloat(key.toString()));
		}
		return new VascularSystemServerPacket(devo);
	}
}