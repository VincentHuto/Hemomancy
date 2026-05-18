package com.vincenthuto.hemomancy.common.network.capa.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class KnownManipulationServerPacket implements CustomPacketPayload {

	public static final Type<KnownManipulationServerPacket> TYPE = new Type<>(Hemomancy.rloc("known_manipulation_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, KnownManipulationServerPacket> STREAM_CODEC = StreamCodec.of(KnownManipulationServerPacket::encode, KnownManipulationServerPacket::decode);

	
	private List<VeinLocation> veinList = new ArrayList<>();
	private LinkedHashMap<BloodManipulation, ManipLevel> known = new LinkedHashMap<>();

	private BloodManipulation selected;

	private VeinLocation selectedVein;
	BlockPos lastVeinMineStart;

	private boolean avatarActive;

	private List<String> equippedManipNames = new ArrayList<>();

	public KnownManipulationServerPacket(IKnownManipulations known) {
		this.known = known.getKnownManips();
		this.selected = known.getSelectedManip();
		this.veinList = known.getVeinList();
		this.selectedVein = known.getSelectedVein();
		this.avatarActive = known.isAvatarActive();
		this.lastVeinMineStart = known.getLastVeinMineStart();
		this.equippedManipNames = new ArrayList<>(known.getEquippedManipNames());
	}

	public KnownManipulationServerPacket(LinkedHashMap<BloodManipulation, ManipLevel> list, BloodManipulation selected,
			List<VeinLocation> veinList, VeinLocation selectedVein, boolean avatarActive, BlockPos lastVeinMineStart,
			List<String> equippedManipNames) {

		this.known = list;
		this.selected = selected;
		this.veinList = veinList;
		this.selectedVein = selectedVein;
		this.avatarActive = avatarActive;
		this.lastVeinMineStart = lastVeinMineStart;
		this.equippedManipNames = equippedManipNames != null ? equippedManipNames : new ArrayList<>();
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
		int equippedCount = buf.readInt();
		List<String> equippedManipNames = new ArrayList<>();
		for (int i = 0; i < equippedCount; ++i) {
			equippedManipNames.add(buf.readUtf());
		}
		return new KnownManipulationServerPacket(manips, sel, veinList, selvein, avatarActive, lastveinstart, equippedManipNames);
	}
	public static void encode(final FriendlyByteBuf buf, final KnownManipulationServerPacket msg) {
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
		buf.writeInt(msg.equippedManipNames.size());
		for (String name : msg.equippedManipNames) {
			buf.writeUtf(name);
		}

	}
	public static void handle(final KnownManipulationServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}
			IKnownManipulations known = HemoCapabilityAccess.requireKnownManipulations(player);
			known.setKnownManips(msg.known);
			known.setSelectedManip(msg.selected);
			known.setVeinList(msg.veinList);
			known.setSelectedVein(msg.selectedVein);
			known.setAvatarActive(msg.avatarActive);
			known.setLastVeinMineStart(msg.lastVeinMineStart);
			known.setEquippedManipNames(msg.equippedManipNames);

	
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
