package com.vincenthuto.hemomancy.common.network.capa.harbinger.manips;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationLoadout;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationRetirementRules;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KnownManipulationServerPacket implements CustomPacketPayload {

	public static final Type<KnownManipulationServerPacket> TYPE = new Type<>(Hemomancy.rloc("known_manipulation_server_packet"));
	public static final StreamCodec<FriendlyByteBuf, KnownManipulationServerPacket> STREAM_CODEC = StreamCodec.of(KnownManipulationServerPacket::encode, KnownManipulationServerPacket::decode);

	
	private List<VeinLocation> veinList = new ArrayList<>();
	private LinkedHashMap<BloodManipulation, ManipLevel> known = new LinkedHashMap<>();

	private BloodManipulation selected;
	private String selectedMemoryKey = "";

	private VeinLocation selectedVein;
	BlockPos lastVeinMineStart;

	private String avatarForm = "";

	private List<String> equippedManipNames = new ArrayList<>();
	private List<ManipulationLoadout> loadouts = new ArrayList<>();
	private Set<String> grandfatheredFamilyForms = new LinkedHashSet<>();

	public KnownManipulationServerPacket(IKnownManipulations known) {
		ManipulationRetirementRules.sanitizeKnownManipulations(known);
		this.known = snapshot(known.getKnownManips());
		this.selected = known.getSelectedManip();
		this.selectedMemoryKey = known.getSelectedMemoryRef().storageKey();
		this.veinList = new ArrayList<>(known.getVeinList());
		this.selectedVein = known.getSelectedVein();
		this.avatarForm = known.getActiveAvatarForm();
		this.lastVeinMineStart = known.getLastVeinMineStart();
		this.equippedManipNames = new ArrayList<>(known.getEquippedManipNames());
		this.loadouts = new ArrayList<>(known.getLoadouts());
		this.grandfatheredFamilyForms = new LinkedHashSet<>(known.getGrandfatheredFamilyForms());
	}

	public KnownManipulationServerPacket(LinkedHashMap<BloodManipulation, ManipLevel> list, BloodManipulation selected,
			List<VeinLocation> veinList, VeinLocation selectedVein, boolean avatarActive, BlockPos lastVeinMineStart,
			List<String> equippedManipNames, List<ManipulationLoadout> loadouts) {
		this(list, selected, veinList, selectedVein, avatarActive ? "summon_avatar" : "", lastVeinMineStart,
				equippedManipNames, loadouts,
				selected != null ? selected.getName() : "");
	}

	public KnownManipulationServerPacket(LinkedHashMap<BloodManipulation, ManipLevel> list, BloodManipulation selected,
			List<VeinLocation> veinList, VeinLocation selectedVein, boolean avatarActive, BlockPos lastVeinMineStart,
			List<String> equippedManipNames, List<ManipulationLoadout> loadouts, String selectedMemoryKey) {
		this(list, selected, veinList, selectedVein, avatarActive ? "summon_avatar" : "", lastVeinMineStart,
				equippedManipNames, loadouts, selectedMemoryKey);
	}

	public KnownManipulationServerPacket(LinkedHashMap<BloodManipulation, ManipLevel> list, BloodManipulation selected,
			List<VeinLocation> veinList, VeinLocation selectedVein, String avatarForm, BlockPos lastVeinMineStart,
			List<String> equippedManipNames, List<ManipulationLoadout> loadouts) {
		this(list, selected, veinList, selectedVein, avatarForm, lastVeinMineStart, equippedManipNames, loadouts,
				selected != null ? selected.getName() : "");
	}

	public KnownManipulationServerPacket(LinkedHashMap<BloodManipulation, ManipLevel> list, BloodManipulation selected,
			List<VeinLocation> veinList, VeinLocation selectedVein, String avatarForm, BlockPos lastVeinMineStart,
			List<String> equippedManipNames, List<ManipulationLoadout> loadouts, String selectedMemoryKey) {

		this.known = snapshot(list);
		this.selected = selected;
		this.veinList = new ArrayList<>(veinList);
		this.selectedVein = selectedVein;
		this.avatarForm = avatarForm != null ? avatarForm : "";
		this.lastVeinMineStart = lastVeinMineStart;
		this.equippedManipNames = equippedManipNames != null ? new ArrayList<>(equippedManipNames) : new ArrayList<>();
		this.loadouts = loadouts != null ? new ArrayList<>(loadouts) : new ArrayList<>();
		this.selectedMemoryKey = selectedMemoryKey != null ? selectedMemoryKey : "";
	}
	
	
    // Capture on the server thread before asynchronous network encoding.
    private static LinkedHashMap<BloodManipulation,ManipLevel> snapshot(LinkedHashMap<BloodManipulation,ManipLevel> source) {
        var snapshot=new LinkedHashMap<BloodManipulation,ManipLevel>();
        source.forEach((manip,level)->snapshot.put(manip,level==null?null:new ManipLevel(level.getCurrentLevel(),level.getXp())));
        return snapshot;
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
		String avatarForm = buf.readUtf();
		BlockPos lastveinstart = buf.readBlockPos();
		int equippedCount = buf.readInt();
		List<String> equippedManipNames = new ArrayList<>();
		for (int i = 0; i < equippedCount; ++i) {
			equippedManipNames.add(buf.readUtf());
		}
		int loadoutCount = buf.readInt();
		List<ManipulationLoadout> loadouts = new ArrayList<>();
		for (int i = 0; i < loadoutCount; ++i) {
			loadouts.add(ManipulationLoadout.readFromBuf(buf, i));
		}
		String selectedMemoryKey = buf.readUtf();
		KnownManipulationServerPacket packet = new KnownManipulationServerPacket(manips, sel, veinList, selvein,
				avatarForm, lastveinstart, equippedManipNames, loadouts, selectedMemoryKey);
		int grandfatheredCount = buf.readInt();
		for (int i = 0; i < grandfatheredCount; i++) packet.grandfatheredFamilyForms.add(buf.readUtf());
		return packet;
	}
	public static void encode(final FriendlyByteBuf buf, final KnownManipulationServerPacket msg) {
		if (msg.selected != null) {
			buf.writeNbt(msg.selected.serialize());
		}
		if (msg.selectedVein != null) {
			buf.writeNbt(msg.selectedVein.serializeNBT());
		}

		buf.writeInt(msg.known.size());
        for(var entry:msg.known.entrySet()) {
            if(entry.getKey()!=null) {
                buf.writeNbt(entry.getKey().serialize());
                buf.writeNbt(entry.getValue().serialize());
            }
        }
		buf.writeInt(msg.veinList.size());
		for (VeinLocation element : msg.veinList) {
			if (element != null) {
				element.serializeToBuf(buf);
			}
		}
		buf.writeUtf(msg.avatarForm);
		buf.writeBlockPos(msg.lastVeinMineStart);
		buf.writeInt(msg.equippedManipNames.size());
		for (String name : msg.equippedManipNames) {
			buf.writeUtf(name);
		}
		buf.writeInt(msg.loadouts.size());
		for (ManipulationLoadout loadout : msg.loadouts) {
			loadout.writeToBuf(buf);
		}
		buf.writeUtf(msg.selectedMemoryKey);
		buf.writeInt(msg.grandfatheredFamilyForms.size());
		for (String id : msg.grandfatheredFamilyForms) buf.writeUtf(id);

	}
	public static void handle(final KnownManipulationServerPacket msg, final IPayloadContext ctx) {
		ctx.enqueueWork(() -> {
			Player player = ctx.player();
			if (player == null) {
				return;
			}
			IKnownManipulations known = HemoCapabilityAccess.requireKnownManipulations(player);
			known.setKnownManips(msg.known);
			known.setGrandfatheredFamilyForms(msg.grandfatheredFamilyForms);
			known.setSelectedManip(msg.selected);
			known.setVeinList(msg.veinList);
			known.setSelectedVein(msg.selectedVein);
			known.setActiveAvatarForm(msg.avatarForm);
			known.setLastVeinMineStart(msg.lastVeinMineStart);
			known.setEquippedManipNames(msg.equippedManipNames);
			known.setLoadouts(msg.loadouts);
			known.setSelectedMemoryRef(com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef
					.fromStorageKey(msg.selectedMemoryKey));
			ManipulationRetirementRules.sanitizeKnownManipulations(known);

	
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
