package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class KnownManipulations implements IKnownManipulations, INBTSerializable<ListTag> {

	BlockPos lastVeinMineStart = BlockPos.ZERO;
	BloodManipulation selectedManip = BloodManipulation.BLANK;
	LinkedHashMap<BloodManipulation, ManipLevel> knownManips = new LinkedHashMap<>();
	List<VeinLocation> veinList = new ArrayList<>();
	VeinLocation selectedVein = VeinLocation.BLANK;
	String activeAvatarForm = "";
	List<String> equippedManipNames = new ArrayList<>();
	List<String> activePassiveNames = new ArrayList<>();
	List<ManipulationLoadout> loadouts = new ArrayList<>();
	String selectedMemoryKey = "";
	Set<String> grandfatheredFamilyForms = new LinkedHashSet<>();
	private static final int FAMILY_MEMORY_VERSION = 2;
	/** Forms moved to another family in family-memory version 2 (Sanguine Marionette: Blood Binding to Hematic Rebuke). */
	private static final Set<String> FORMS_REHOMED_IN_V2 = Set.of("sanguine_marionette");
	/** Owner-visible state changed since the last send. Never saved. */
	private transient boolean syncDirty;

	@Override
	public void markSyncDirty() {
		syncDirty = true;
	}

	@Override
	public boolean isSyncDirty() {
		return syncDirty;
	}

	@Override
	public void clearSyncDirty() {
		syncDirty = false;
	}

	@Override
	public BlockPos getLastVeinMineStart() {
		return lastVeinMineStart;
	}

	@Override
	public void setLastVeinMineStart(BlockPos newPos) {
		if (!java.util.Objects.equals(lastVeinMineStart, newPos)) markSyncDirty();
		this.lastVeinMineStart = newPos;
	}

	/*
	 * Used because saving the manip objects means its not as easy as just comparing
	 * them 1:1
	 */
	@Override
	public boolean doesListContainName(LinkedHashMap<BloodManipulation, ManipLevel> map, BloodManipulation manipIn) {
		for (BloodManipulation curr : map.keySet()) {
			if (manipIn.getName().equals(curr.getName())) {
				return true;
			}
		}
		return false;

	}

	@Override
	public LinkedHashMap<BloodManipulation, ManipLevel> getKnownManips() {
		return knownManips;
	}

	@Override
	public List<ManipLevel> getLevelList() {
		return new ArrayList<>(knownManips.values());
	}

	@Override
	public ManipLevel getManipLevel(BloodManipulation manip) {
		if (manip == null) return null;
		if (ManipulationFamilyRegistry.normalizeKnown(knownManips)) markSyncDirty();
		ManipLevel direct = knownManips.get(manip);
		if (direct != null) return direct;
		String baseline = ManipulationFamilyRegistry.baselineId(manip.getName());
		for (var entry : knownManips.entrySet()) {
			BloodManipulation known = entry.getKey();
			if (known != null && (manip.getName().equals(known.getName())
					|| baseline.equals(ManipulationFamilyRegistry.baselineId(known.getName())))) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	public List<BloodManipulation> getManipList() {
		return new ArrayList<>(knownManips.keySet());

	}

	@Override
	public BloodManipulation getSelectedManip() {
		if (selectedManip == null) {
			selectedManip = getManipList().get(0);
			markSyncDirty();
		}
		return selectedManip;

	}

	@Override
	public ManipLevel getSelectedManipLevel() {
		return knownManips.get(selectedManip);
	}

	@Override
	public VeinLocation getSelectedVein() {
		if (selectedVein == null) {
			selectedVein = getVeinList().get(0);
			markSyncDirty();
		}
		return selectedVein;
	}

	@Override
	public List<BlockPos> getVeinBlockList() {
		List<BlockPos> list = new ArrayList<>();
		for (VeinLocation loc : getVeinList()) {
			list.add(loc.getPosition());
		}
		return list;
	}

	@Override
	public List<VeinLocation> getVeinList() {
		return veinList;
	}

	@Override
	public List<String> getVeinNameList() {
		List<String> list = new ArrayList<>();
		for (VeinLocation loc : getVeinList()) {
			list.add(loc.getName());
		}
		return list;
	}

	@Override
	public void incrSelectedManipLevel(int incr) {
		ManipLevel sel = getManipLevel(selectedManip);
		if (sel == null) return;
		int currLevel = sel.getCurrentLevel();
		sel.setCurrentLevel(currLevel + incr);
		ManipulationFamilyRegistry.normalizeKnown(knownManips);
		if (incr != 0) markSyncDirty();

	}

	@Override
	public String getActiveAvatarForm() {
		return activeAvatarForm;
	}

	@Override
	public void setActiveAvatarForm(String manipulationId) {
		String form = manipulationId != null ? manipulationId : "";
		if (!form.equals(activeAvatarForm)) markSyncDirty();
		this.activeAvatarForm = form;
	}

	@Override
	public void setKnownManips(LinkedHashMap<BloodManipulation, ManipLevel> knownManips) {
		this.knownManips = knownManips != null ? knownManips : new LinkedHashMap<>();
		ManipulationFamilyRegistry.normalizeKnown(this.knownManips);
		markSyncDirty();
	}

	@Override
	public Set<String> getGrandfatheredFamilyForms() {
		return grandfatheredFamilyForms;
	}

	@Override
	public void setGrandfatheredFamilyForms(Set<String> forms) {
		Set<String> next = forms != null ? new LinkedHashSet<>(forms) : new LinkedHashSet<>();
		if (!next.equals(grandfatheredFamilyForms)) markSyncDirty();
		grandfatheredFamilyForms = next;
	}

	@Override
	public void setSelectedManip(BloodManipulation selectedManip) {
		String memoryKey = this.selectedMemoryKey;
		if (selectedManip != null && selectedManip != BloodManipulation.BLANK) {
			memoryKey = MemorySlotRef.manipulation(selectedManip.getName()).storageKey();
		}
		if (selectedManip != this.selectedManip || !java.util.Objects.equals(memoryKey, this.selectedMemoryKey)) {
			markSyncDirty();
		}
		this.selectedManip = selectedManip;
		this.selectedMemoryKey = memoryKey;
	}

	@Override
	public void setSelectedManipLevel(int level) {
		ManipLevel selectedLevel = getManipLevel(selectedManip);
		if (selectedLevel != null) {
			if (selectedLevel.getCurrentLevel() != level) markSyncDirty();
			selectedLevel.setCurrentLevel(level);
			ManipulationFamilyRegistry.normalizeKnown(knownManips);
		}
	}

	@Override
	public void setSelectedVein(VeinLocation selectedVein) {
		if (selectedVein != this.selectedVein) markSyncDirty();
		this.selectedVein = selectedVein;

	}

	@Override
	public void setVeinList(List<VeinLocation> dimPos) {
		this.veinList = dimPos;
		markSyncDirty();
	}

	@Override
	public void setCapa(IKnownManipulations old) {
		this.knownManips = old.getKnownManips();
		this.selectedManip = old.getSelectedManip();
		this.veinList = old.getVeinList();
		this.activeAvatarForm = old.getActiveAvatarForm();
		this.equippedManipNames = new ArrayList<>(old.getEquippedManipNames());
		this.activePassiveNames = new ArrayList<>(old.getEquippedManipNames().stream()
				.filter(old::isPassiveActive).toList());
		this.loadouts = new ArrayList<>(old.getLoadouts());
		this.selectedMemoryKey = old.getSelectedMemoryRef().storageKey();
		this.grandfatheredFamilyForms = new LinkedHashSet<>(old.getGrandfatheredFamilyForms());
		markSyncDirty();
	}

	// ── Equipped manipulation slots ──

	@Override
	public List<String> getEquippedManipNames() {
		if (ManipulationEquipHelper.normalizeEquippedNames(equippedManipNames)) markSyncDirty();
		return equippedManipNames;
	}

	@Override
	public void setEquippedManipNames(List<String> names) {
		List<String> before = equippedManipNames;
		List<String> passivesBefore = new ArrayList<>(activePassiveNames);
		this.equippedManipNames = names != null ? new ArrayList<>(names) : new ArrayList<>();
		ManipulationEquipHelper.normalizeEquippedNames(this.equippedManipNames);
		activePassiveNames.retainAll(this.equippedManipNames);
		if (!equippedManipNames.equals(before) || !activePassiveNames.equals(passivesBefore)) markSyncDirty();
	}

	@Override
	public List<MemorySlotRef> getEquippedMemoryRefs() {
		return getEquippedManipNames().stream().map(MemorySlotRef::fromStorageKey).toList();
	}

	@Override
	public void setEquippedMemoryRefs(List<MemorySlotRef> refs) {
		setEquippedManipNames(refs == null ? List.of()
				: refs.stream().filter(java.util.Objects::nonNull).map(MemorySlotRef::storageKey).toList());
	}

	@Override
	public MemorySlotRef getSelectedMemoryRef() {
		if (selectedMemoryKey != null && !selectedMemoryKey.isBlank()) {
			return MemorySlotRef.fromStorageKey(selectedMemoryKey);
		}
		BloodManipulation selected = getSelectedManip();
		return MemorySlotRef.manipulation(selected != null ? selected.getName() : "");
	}

	@Override
	public void setSelectedMemoryRef(MemorySlotRef ref) {
		String key = ref == null ? "" : ref.storageKey();
		if (!key.equals(selectedMemoryKey)) markSyncDirty();
		selectedMemoryKey = key;
		if (ref != null && ref.kind() == MemoryEntryKind.MANIPULATION) {
			for (BloodManipulation manipulation : getManipList()) {
				if (manipulation != null && ref.id().equals(manipulation.getName())) {
					if (selectedManip != manipulation) markSyncDirty();
					selectedManip = manipulation;
					break;
				}
			}
		}
	}

	@Override
	public boolean isMemoryEquipped(MemorySlotRef ref) {
		return ref != null && getEquippedManipNames().contains(ref.storageKey());
	}

	@Override
	public boolean equipMemory(MemorySlotRef ref, int maxSlots) {
		if (ref == null) return false;
		if (ref.muscleMemory().isPresent()) {
			MemoryEquipRules.AutoEquipResult result = MemoryEquipRules.autoEquipMuscleMemory(
					equippedManipNames, ref.muscleMemory().orElseThrow(), maxSlots);
			boolean equipped = result == MemoryEquipRules.AutoEquipResult.ADDED
					|| result == MemoryEquipRules.AutoEquipResult.REPLACED_SECTION;
			if (equipped) markSyncDirty();
			return equipped;
		}
		BloodManipulation manipulation = getManipList().stream()
				.filter(known -> known != null && ref.id().equals(known.getName())).findFirst().orElse(null);
		boolean equipped = manipulation != null && isManipulationAvailable(manipulation)
				&& ManipulationEquipHelper.equipNameIfPossible(equippedManipNames, ref.storageKey(), maxSlots);
		if (equipped) markSyncDirty();
		return equipped;
	}

	@Override
	public boolean unequipMemory(MemorySlotRef ref) {
		if (ref == null) return false;
		boolean removed = ManipulationEquipHelper.unequipNameIfAllowed(equippedManipNames, ref.storageKey());
		if (removed) {
			activePassiveNames.remove(ref.storageKey());
			markSyncDirty();
		}
		return removed;
	}

	@Override
	public boolean isManipEquipped(BloodManipulation manip) {
		if (manip == null || manip == BloodManipulation.BLANK) return false;
		if (ManipulationEquipHelper.normalizeEquippedNames(equippedManipNames)) markSyncDirty();
		return equippedManipNames.contains(manip.getName());
	}

	@Override
	public boolean equipManip(String manipName, int maxSlots) {
		BloodManipulation manipulation = getManipList().stream()
				.filter(known -> known != null && manipName.equals(known.getName())).findFirst().orElse(null);
		boolean equipped = manipulation != null && isManipulationAvailable(manipulation)
				&& ManipulationEquipHelper.equipNameIfPossible(equippedManipNames, manipName, maxSlots);
		if (equipped) markSyncDirty();
		return equipped;
	}

	@Override
	public boolean unequipManip(String manipName) {
		boolean removed = ManipulationEquipHelper.unequipNameIfAllowed(equippedManipNames, manipName);
		if (removed) {
			activePassiveNames.remove(manipName);
			markSyncDirty();
		}
		return removed;
	}

	@Override
	public boolean isPassiveActive(String manipName) {
		return manipName != null && equippedManipNames.contains(manipName) && activePassiveNames.contains(manipName);
	}

	@Override
	public boolean togglePassive(String manipName) {
		if (manipName == null || !equippedManipNames.contains(manipName)) return false;
		markSyncDirty();
		if (activePassiveNames.remove(manipName)) return false;
		activePassiveNames.add(manipName);
		return true;
	}

	@Override
	public List<ManipulationLoadout> getLoadouts() {
		return loadouts;
	}

	@Override
	public void setLoadouts(List<ManipulationLoadout> loadouts) {
		this.loadouts = loadouts != null ? new ArrayList<>(loadouts) : new ArrayList<>();
		markSyncDirty();
	}

	@Override
	public ManipulationLoadout getLoadout(int slotIndex) {
		if (slotIndex < 0 || slotIndex >= loadouts.size() || loadouts.get(slotIndex) == null) {
			return ManipulationLoadout.empty(slotIndex);
		}
		return loadouts.get(slotIndex);
	}

	@Override
	public void setLoadout(int slotIndex, ManipulationLoadout loadout) {
		if (slotIndex < 0) {
			return;
		}
		while (loadouts.size() <= slotIndex) {
			loadouts.add(ManipulationLoadout.empty(loadouts.size()));
		}
		loadouts.set(slotIndex, loadout != null ? loadout : ManipulationLoadout.empty(slotIndex));
		markSyncDirty();
	}

	@Override
	public ListTag serializeNBT(HolderLookup.Provider provider) {
		ListTag list = new ListTag();
		CompoundTag selectedManip = new CompoundTag();
		selectedManip.put("Selected", getSelectedManip().serialize());
		selectedManip.put("SelectedVein", getSelectedVein().serializeNBT());
		selectedManip.putString("SelectedMemory", getSelectedMemoryRef().storageKey());
		list.add(selectedManip);

		for (int i = 0; i < getKnownManips().size(); i++) {
			BloodManipulation manip = getManipList().get(i);
			ManipLevel level = getLevelList().get(i);
			if (manip != null && level != null) {
				CompoundTag entry = new CompoundTag();
				entry.put("Manip" + i, manip.serialize());
				entry.put("Level" + i, level.serialize());
				list.add(entry);
			}
		}

		CompoundTag veinCount = new CompoundTag();
		veinCount.putInt("veinCount", getVeinList().size());
		list.add(veinCount);
		for (int i = 0; i < getVeinList().size(); i++) {
			VeinLocation loc = getVeinList().get(i);
			if (loc != null) {
				CompoundTag entry = new CompoundTag();
				entry.put("Vein" + i, loc.serializeNBT());
				list.add(entry);
			}
		}

		CompoundTag avatarState = new CompoundTag();
		avatarState.putString("avatarForm", getActiveAvatarForm());
		avatarState.put("lastVeinMineStart", NbtUtils.writeBlockPos(getLastVeinMineStart()));
		list.add(avatarState);

		CompoundTag equippedEntry = new CompoundTag();
		ListTag equippedTag = new ListTag();
		for (String name : getEquippedManipNames()) {
			equippedTag.add(StringTag.valueOf(name));
		}
		equippedEntry.put("equippedManips", equippedTag);
		ListTag activePassives = new ListTag();
		for (String name : activePassiveNames) activePassives.add(StringTag.valueOf(name));
		equippedEntry.put("activePassives", activePassives);
		list.add(equippedEntry);

		CompoundTag loadoutsEntry = new CompoundTag();
		ListTag loadoutsTag = new ListTag();
		for (int i = 0; i < getLoadouts().size(); i++) {
			loadoutsTag.add(getLoadout(i).toTag());
		}
		loadoutsEntry.put("synapticLoadouts", loadoutsTag);
		list.add(loadoutsEntry);

		CompoundTag familyMemoryState = new CompoundTag();
		familyMemoryState.putInt("familyMemoryVersion", FAMILY_MEMORY_VERSION);
		ListTag grandfathered = new ListTag();
		for (String id : grandfatheredFamilyForms) grandfathered.add(StringTag.valueOf(id));
		familyMemoryState.put("grandfatheredFamilyForms", grandfathered);
		list.add(familyMemoryState);

		return list;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, ListTag listNbt) {
		markSyncDirty();
		LinkedHashMap<BloodManipulation, ManipLevel> map = new LinkedHashMap<>();
		List<VeinLocation> veinList = new ArrayList<>();
		int veinCount = 0;
		boolean legacyAvatarActive = false;
		String savedAvatarForm = "";
		int familyMemoryVersion = 0;
		Set<String> savedGrandfatheredForms = new LinkedHashSet<>();
		for (int i = 0; i < listNbt.size(); i++) {
			CompoundTag parsedNbt = (CompoundTag) listNbt.get(i);
			if (parsedNbt != null && !parsedNbt.isEmpty()) {
				if (parsedNbt.contains("Selected")) {
					BloodManipulation selectedManip = BloodManipulation.deserialize(parsedNbt.getCompound("Selected"));
					setSelectedManip(selectedManip);
				}
				if (parsedNbt.contains("SelectedVein")) {
					VeinLocation selectedVein = VeinLocation.deserializeToLoc(parsedNbt.getCompound("SelectedVein"));
					setSelectedVein(selectedVein);
				}
				if (parsedNbt.contains("Manip" + (i - 1))) {
					CompoundTag manipNbt = parsedNbt.getCompound("Manip" + (i - 1));
					CompoundTag levelNbt = parsedNbt.getCompound("Level" + (i - 1));
					BloodManipulation bloodManip = BloodManipulation.deserialize(manipNbt);
					ManipLevel level = ManipLevel.deserialize(levelNbt);
					map.put(bloodManip, level);
				}
				if (parsedNbt.contains("veinCount")) {
					veinCount = parsedNbt.getInt("veinCount");
				}
				for (int j = 0; j < veinCount; j++) {
					if (parsedNbt.contains("Vein" + j)) {
						VeinLocation vein = VeinLocation.deserializeToLoc(parsedNbt.getCompound("Vein" + j));
						veinList.add(vein);
					}
				}
				if (parsedNbt.contains("avatarActive")) {
					legacyAvatarActive = parsedNbt.getBoolean("avatarActive");
				}
				if (parsedNbt.contains("avatarForm", Tag.TAG_STRING)) {
					savedAvatarForm = parsedNbt.getString("avatarForm");
				}
				if (parsedNbt.contains("lastVeinMineStart")) {
					setLastVeinMineStart(NbtUtils.readBlockPos(parsedNbt, "lastVeinMineStart").orElse(BlockPos.ZERO));
				}
				if (parsedNbt.contains("equippedManips")) {
					ListTag equippedTag = parsedNbt.getList("equippedManips", Tag.TAG_STRING);
					List<String> equipped = new ArrayList<>();
					for (int j = 0; j < equippedTag.size(); j++) {
						equipped.add(equippedTag.getString(j));
					}
					setEquippedManipNames(equipped);
				}
				if (parsedNbt.contains("activePassives")) {
					ListTag activeTag = parsedNbt.getList("activePassives", Tag.TAG_STRING);
					activePassiveNames.clear();
					for (int j = 0; j < activeTag.size(); j++) {
						String name = activeTag.getString(j);
						if (equippedManipNames.contains(name)) activePassiveNames.add(name);
					}
				}
				if (parsedNbt.contains("SelectedMemory", Tag.TAG_STRING)) {
					setSelectedMemoryRef(MemorySlotRef.fromStorageKey(parsedNbt.getString("SelectedMemory")));
				}
				if (parsedNbt.contains("synapticLoadouts")) {
					ListTag loadoutsTag = parsedNbt.getList("synapticLoadouts", Tag.TAG_COMPOUND);
					List<ManipulationLoadout> parsedLoadouts = new ArrayList<>();
					for (int j = 0; j < loadoutsTag.size(); j++) {
						parsedLoadouts.add(ManipulationLoadout.fromTag(loadoutsTag.getCompound(j), j));
					}
					setLoadouts(parsedLoadouts);
				}
				if (parsedNbt.contains("familyMemoryVersion")) {
					familyMemoryVersion = parsedNbt.getInt("familyMemoryVersion");
					ListTag forms = parsedNbt.getList("grandfatheredFamilyForms", Tag.TAG_STRING);
					for (int j = 0; j < forms.size(); j++) savedGrandfatheredForms.add(forms.getString(j));
				}
			}
		}
		if (familyMemoryVersion < 2) detachRehomedForms(map, savedGrandfatheredForms);
		setKnownManips(map);
		if (familyMemoryVersion < 1) {
			for (BloodManipulation manipulation : map.keySet()) {
				if (manipulation != null && ManipulationFamilyRegistry.form(manipulation.getName()).isPresent()) {
					savedGrandfatheredForms.add(manipulation.getName());
				}
			}
		}
		setGrandfatheredFamilyForms(savedGrandfatheredForms);
		setVeinList(veinList);
		String restoredAvatar = !savedAvatarForm.isBlank() ? savedAvatarForm
				: legacyAvatarActive ? "summon_avatar" : "";
		setActiveAvatarForm(canRestoreAvatarForm(restoredAvatar) ? restoredAvatar : "");
	}

	/**
	 * A re-homed form was saved with its old family's mastery. Left alone, normalization would lend that mastery to
	 * the new family. It starts at the new family's level instead, and stays usable for whoever already learned it.
	 */
	private static void detachRehomedForms(LinkedHashMap<BloodManipulation, ManipLevel> map, Set<String> grandfathered) {
		for (var entry : map.entrySet()) {
			BloodManipulation manipulation = entry.getKey();
			if (manipulation == null || !FORMS_REHOMED_IN_V2.contains(manipulation.getName())) continue;
			entry.setValue(new ManipLevel(0, 0));
			grandfathered.add(manipulation.getName());
		}
	}

	private boolean canRestoreAvatarForm(String manipulationId) {
		if (manipulationId == null || manipulationId.isBlank()
				|| !equippedManipNames.contains(manipulationId)) return false;
		return knownManips.keySet().stream().anyMatch(manipulation -> manipulation != null
				&& manipulationId.equals(manipulation.getName()) && isManipulationAvailable(manipulation));
	}
}
