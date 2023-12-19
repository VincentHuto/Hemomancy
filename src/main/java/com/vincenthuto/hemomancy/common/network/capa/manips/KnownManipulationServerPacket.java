package com.vincenthuto.hemomancy.common.network.capa.manips;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.manip.KnownManipulationProvider;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class KnownManipulationServerPacket {

	
	private List<VeinLocation> veinList = new ArrayList<>();
	private LinkedHashMap<BloodManipulation, ManipLevel> known = new LinkedHashMap<>();

	private BloodManipulation selected;

	private VeinLocation selectedVein;
	BlockPos lastVeinMineStart;

	private boolean avatarActive;

	public KnownManipulationServerPacket(IKnownManipulations known) {
		this.known = known.getKnownManips();
		this.selected = known.getSelectedManip();
		this.veinList = known.getVeinList();
		this.selectedVein = known.getSelectedVein();
		this.avatarActive = known.isAvatarActive();
		this.lastVeinMineStart = known.getLastVeinMineStart();
	}

	public KnownManipulationServerPacket(LinkedHashMap<BloodManipulation, ManipLevel> list, BloodManipulation selected,
			List<VeinLocation> veinList, VeinLocation selectedVein, boolean avatarActive,BlockPos lastVeinMineStart) {

		this.known = list;
		this.selected = selected;
		this.veinList = veinList;
		this.selectedVein = selectedVein;
		this.avatarActive = avatarActive;
		this.lastVeinMineStart = lastVeinMineStart;


	}
	
	
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
			veinList.add(VeinLocation.deserializeFromBuf(buf));
		}
		boolean avatarActive = buf.readBoolean();
		BlockPos lastveinstart = buf.readBlockPos();
		return new KnownManipulationServerPacket(manips, sel, veinList, selvein, avatarActive, lastveinstart);
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
				element.serializeToBuf(buf);
			}
		}
		buf.writeBoolean(msg.avatarActive);
		buf.writeBlockPos(msg.lastVeinMineStart);

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
			known.setLastVeinMineStart(msg.lastVeinMineStart);

	
		});
		ctx.get().setPacketHandled(true);
	}

}