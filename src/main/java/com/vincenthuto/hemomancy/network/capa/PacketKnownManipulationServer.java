package com.vincenthuto.hemomancy.network.capa;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketKnownManipulationServer {
	private LinkedHashMap<BloodManipulation, ManipLevel> known = new LinkedHashMap<BloodManipulation, ManipLevel>();
	BloodManipulation selected;

	public PacketKnownManipulationServer(IKnownManipulations known) {
		this.known = known.getKnownManips();
		this.selected = known.getSelectedManip();
	}

	public PacketKnownManipulationServer(LinkedHashMap<BloodManipulation, ManipLevel> list,
			BloodManipulation selected) {
		this.known = list;
		this.selected = selected;
	}

	@SuppressWarnings("unused")
	public static void handle(final PacketKnownManipulationServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer sender = ctx.get().getSender();
			Minecraft.getInstance().player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalStateException::new).setKnownManips(msg.known);
			Minecraft.getInstance().player.getCapability(KnownManipulationProvider.MANIP_CAPA)
					.orElseThrow(IllegalStateException::new).setSelectedManip(msg.selected);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketKnownManipulationServer msg, final FriendlyByteBuf buf) {
		if (msg.selected != null) {
			buf.writeNbt(msg.selected.serialize());
		}
		buf.writeInt(msg.known.size());
		for (int i = 0; i < msg.known.size(); ++i) {
			if (msg.known.keySet().toArray()[i] != null) {
				buf.writeNbt(((BloodManipulation) msg.known.keySet().toArray()[i]).serialize());
				buf.writeNbt(((ManipLevel) msg.known.values().toArray()[i]).serialize());
			}
		}
	}

	public static PacketKnownManipulationServer decode(final FriendlyByteBuf buf) {
		BloodManipulation sel = BloodManipulation.deserialize(buf.readNbt());
		int count = buf.readInt();
		LinkedHashMap<BloodManipulation, ManipLevel> manips = new LinkedHashMap<BloodManipulation, ManipLevel>();
		for (int i = 0; i < count; ++i) {
			manips.put(BloodManipulation.deserialize(buf.readNbt()), ManipLevel.deserialize(buf.readNbt()));

		}

		return new PacketKnownManipulationServer(manips, sel);
	}
}