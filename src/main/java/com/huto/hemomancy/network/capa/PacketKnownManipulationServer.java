package com.huto.hemomancy.network.capa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.huto.hemomancy.capabilities.manipulation.KnownManipulationProvider;
import com.huto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketKnownManipulationServer {
	private List<BloodManipulation> known = new ArrayList<>();

	public PacketKnownManipulationServer(List<BloodManipulation> list) {
		this.known = list;
	}

	// This code only runs on the client
	@SuppressWarnings("unused")
	public static void handle(final PacketKnownManipulationServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender();
			Minecraft.getInstance().player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalStateException::new).setKnownManips(msg.known);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketKnownManipulationServer msg, final PacketBuffer buf) {
		buf.writeInt(msg.known.size());
		for (int i = 0; i < msg.known.size(); ++i) {
			if (msg.known.get(i) != null) {
				buf.writeCompoundTag(msg.known.get(i).serialize());
			}
		}
	}

	public static PacketKnownManipulationServer decode(final PacketBuffer buf) {
		int count = buf.readInt();
		List<BloodManipulation> manips = new ArrayList<BloodManipulation>();
		for (int i = 0; i < count; ++i) {
			manips.add(BloodManipulation.deserialize(buf.readCompoundTag()));
		}
		return new PacketKnownManipulationServer(manips);
	}
}