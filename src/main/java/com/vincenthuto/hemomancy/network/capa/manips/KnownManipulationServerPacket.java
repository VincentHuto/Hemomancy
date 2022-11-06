package com.vincenthuto.hemomancy.network.capa.manips;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.capa.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.capa.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.capa.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class KnownManipulationServerPacket {

	public static KnownManipulationServerPacket decode(final FriendlyByteBuf buf) {
		BloodManipulation sel = BloodManipulation.deserialize(buf.readNbt());
		VeinLocation selvein = VeinLocation.deserializeToLoc(buf.readNbt());
		int count = buf.readInt();
		LinkedHashMap<BloodManipulation, ManipLevel> manips = new LinkedHashMap<>();
		for (int i = 0; i < count; ++i) {
			BloodManipulation currManip = BloodManipulation.deserialize(buf.readNbt());
			manips.put(currManip, ManipLevel.deserialize(buf.readNbt()));
		}
		int veincount = buf.readInt();
		List<VeinLocation> veinList = new ArrayList<>();
		for (int i = 0; i < veincount; ++i) {
			veinList.add(VeinLocation.deserializeToLoc(buf.readNbt()));
		}
		boolean avatarActive = buf.readBoolean();

		return new KnownManipulationServerPacket(manips, sel, veinList, selvein, avatarActive);
	}
	public static void encode(final KnownManipulationServerPacket msg, final FriendlyByteBuf buf) {
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
		for (VeinLocation element : msg.veinList) {
			if (element != null) {
				buf.writeNbt(element.serializeNBT());
			}
		}
		buf.writeBoolean(msg.avatarActive);
	}
	public static void handle(final KnownManipulationServerPacket msg, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			IKnownManipulations known = Minecraft.getInstance().player
					.getCapability(KnownManipulationProvider.MANIP_CAPA).orElseThrow(IllegalStateException::new);
			known.setKnownManips(msg.known);
			known.setSelectedManip(msg.selected);
			known.setVeinList(msg.veinList);
			known.setSelectedVein(msg.selectedVein);
			known.setAvatarActive(msg.avatarActive);
		});
		ctx.get().setPacketHandled(true);
	}
	private List<VeinLocation> veinList = new ArrayList<>();
	private LinkedHashMap<BloodManipulation, ManipLevel> known = new LinkedHashMap<>();

	private BloodManipulation selected;

	private VeinLocation selectedVein;

	private boolean avatarActive;

	public KnownManipulationServerPacket(IKnownManipulations known) {
		this.known = known.getKnownManips();
		this.selected = known.getSelectedManip();
		this.veinList = known.getVeinList();
		this.selectedVein = known.getSelectedVein();
		this.avatarActive = known.isAvatarActive();
	}

	public KnownManipulationServerPacket(LinkedHashMap<BloodManipulation, ManipLevel> list, BloodManipulation selected,
			List<VeinLocation> veinList, VeinLocation selectedVein, boolean avatarActive) {
		this.known = list;
		this.selected = selected;
		this.veinList = veinList;
		this.selectedVein = selectedVein;
		this.avatarActive = avatarActive;

	}
}