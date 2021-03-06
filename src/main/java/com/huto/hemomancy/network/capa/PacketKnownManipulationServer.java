package com.huto.hemomancy.network.capa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.huto.hemomancy.capa.manip.KnownManipulationProvider;
import com.huto.hemomancy.manipulation.BloodManipulation;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class PacketKnownManipulationServer {
	private List<BloodManipulation> known = new ArrayList<>();
	BloodManipulation selected;

	public PacketKnownManipulationServer(List<BloodManipulation> list, BloodManipulation selected) {
		this.known = list;
		this.selected = selected;
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketKnownManipulationServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayerEntity sender = ctx.get().getSender();
			Minecraft.getInstance().player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalStateException::new).setKnownManips(msg.known);
			Minecraft.getInstance().player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalStateException::new).setSelectedManip(msg.selected);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketKnownManipulationServer msg, final PacketBuffer buf) {
		if (msg.selected != null) {
			buf.writeCompoundTag(msg.selected.serialize());
		}
		buf.writeInt(msg.known.size());
		for (int i = 0; i < msg.known.size(); ++i) {
			if (msg.known.get(i) != null) {
				buf.writeCompoundTag(msg.known.get(i).serialize());
			}
		}
	}

	public static PacketKnownManipulationServer decode(final PacketBuffer buf) {
		BloodManipulation sel = BloodManipulation.deserialize(buf.readCompoundTag());
		int count = buf.readInt();
		List<BloodManipulation> manips = new ArrayList<BloodManipulation>();
		for (int i = 0; i < count; ++i) {
			manips.add(BloodManipulation.deserialize(buf.readCompoundTag()));
		}

		return new PacketKnownManipulationServer(manips, sel);
	}
}