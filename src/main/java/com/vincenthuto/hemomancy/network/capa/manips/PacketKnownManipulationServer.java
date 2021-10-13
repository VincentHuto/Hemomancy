package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.util.VeinLocation;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

public class PacketKnownManipulationServer {

	private List<VeinLocation> veinList = new ArrayList<VeinLocation>();
	private LinkedHashMap<BloodManipulation, ManipLevel> known = new LinkedHashMap<BloodManipulation, ManipLevel>();
	private BloodManipulation selected;
	private VeinLocation selectedVein;

	public PacketKnownManipulationServer(IKnownManipulations known) {
		this.known = known.getKnownManips();
		this.selected = known.getSelectedManip();
		this.veinList = known.getVeinList();
		this.selectedVein = known.getSelectedVein();
	}

	public PacketKnownManipulationServer(LinkedHashMap<BloodManipulation, ManipLevel> list, BloodManipulation selected,
			List<VeinLocation> veinList, VeinLocation selectedVein) {
		this.known = list;
		this.selected = selected;
		this.veinList = veinList;
		this.selectedVein = selectedVein;
	}

	public static void handle(final PacketKnownManipulationServer msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			IKnownManipulations known = Minecraft.getInstance().player
					.getCapability(KnownManipulationProvider.MANIP_CAPA).orElseThrow(IllegalStateException::new);
			known.setKnownManips(msg.known);
			known.setSelectedManip(msg.selected);
			known.setVeinList(msg.veinList);
			known.setSelectedVein(msg.selectedVein);
		});
		ctx.get().setPacketHandled(true);
	}

	public static void encode(final PacketKnownManipulationServer msg, final FriendlyByteBuf buf) {
		if (msg.selected != null) {
			buf.writeNbt(msg.selected.serialize());
		}
		if (msg.selectedVein != null) {
			buf.writeNbt(msg.selectedVein.serializeNBT());
		}

		buf.writeInt(msg.known.size());
		for (int i = 0; i < msg.known.size(); ++i) {
			if (msg.known.keySet().toArray()[i] != null) {
				buf.writeNbt(((BloodManipulation) msg.known.keySet().toArray()[i]).serialize());
				buf.writeNbt(((ManipLevel) msg.known.values().toArray()[i]).serialize());
			}
		}
		buf.writeInt(msg.veinList.size());
		for (int i = 0; i < msg.veinList.size(); ++i) {
			if (msg.veinList.get(i) != null) {
				buf.writeNbt(msg.veinList.get(i).serializeNBT());
			}
		}
	}

	public static PacketKnownManipulationServer decode(final FriendlyByteBuf buf) {
		BloodManipulation sel = BloodManipulation.deserialize(buf.readNbt());
		VeinLocation selvein = VeinLocation.deserializeToLoc(buf.readNbt());
		int count = buf.readInt();
		LinkedHashMap<BloodManipulation, ManipLevel> manips = new LinkedHashMap<BloodManipulation, ManipLevel>();
		for (int i = 0; i < count; ++i) {
			BloodManipulation currManip = BloodManipulation.deserialize(buf.readNbt());
			manips.put(currManip, ManipLevel.deserialize(buf.readNbt()));
		}
		int veincount = buf.readInt();
		List<VeinLocation> veinList = new ArrayList<VeinLocation>();
		for (int i = 0; i < veincount; ++i) {
			veinList.add(VeinLocation.deserializeToLoc(buf.readNbt()));
		}
		return new PacketKnownManipulationServer(manips, sel, veinList, selvein);
	}
}