package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import net.minecraft.core.BlockPos;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public interface IKnownManipulations {

	public BlockPos getLastVeinMineStart();

	public void setLastVeinMineStart(BlockPos newPos);

	public void setCapa(IKnownManipulations old);

	public boolean doesListContainName(LinkedHashMap<BloodManipulation, ManipLevel> knownList, BloodManipulation manip);

	public LinkedHashMap<BloodManipulation, ManipLevel> getKnownManips();

	public List<ManipLevel> getLevelList();

	public ManipLevel getManipLevel(BloodManipulation manip);

	public List<BloodManipulation> getManipList();

	public BloodManipulation getSelectedManip();

	public ManipLevel getSelectedManipLevel();

	public VeinLocation getSelectedVein();

	public List<BlockPos> getVeinBlockList();

	public List<VeinLocation> getVeinList();

	public List<String> getVeinNameList();

	public void incrSelectedManipLevel(int incr);

	public String getActiveAvatarForm();

	public void setActiveAvatarForm(String manipulationId);

	default boolean isAvatarActive() {
		return !getActiveAvatarForm().isBlank();
	}

	default void setAvatarActive(boolean avatarActive) {
		setActiveAvatarForm(avatarActive ? "summon_avatar" : "");
	}

	public void setKnownManips(LinkedHashMap<BloodManipulation, ManipLevel> knownManips);

	public Set<String> getGrandfatheredFamilyForms();

	public void setGrandfatheredFamilyForms(Set<String> forms);

	default boolean isManipulationAvailable(BloodManipulation manipulation) {
		if (manipulation == null || !doesListContainName(getKnownManips(), manipulation)) return false;
		var form = ManipulationFamilyRegistry.form(manipulation.getName());
		if (form.isEmpty() || getGrandfatheredFamilyForms().contains(manipulation.getName())) return true;
		String baselineId = ManipulationFamilyRegistry.baselineId(manipulation.getName());
		BloodManipulation baseline = getManipList().stream()
				.filter(known -> known != null && baselineId.equals(known.getName())).findFirst().orElse(null);
		ManipLevel level = baseline == null ? null : getManipLevel(baseline);
		return level != null && level.getCurrentLevel() >= form.orElseThrow().requiredLevel();
	}

	public void setSelectedManip(BloodManipulation selectedManip);

	public void setSelectedManipLevel(int level);

	public void setSelectedVein(VeinLocation selectedVein);

	public void setVeinList(List<VeinLocation> dimPos);

	// ── Equipped manipulation slots ──

	/** Returns the list of manipulation names currently equipped in the player's limited slots. */
	public List<String> getEquippedManipNames();

	/** Overwrites the equipped manipulation name list. */
	public void setEquippedManipNames(List<String> names);

	public List<MemorySlotRef> getEquippedMemoryRefs();

	public void setEquippedMemoryRefs(List<MemorySlotRef> refs);

	public MemorySlotRef getSelectedMemoryRef();

	public void setSelectedMemoryRef(MemorySlotRef ref);

	public boolean isMemoryEquipped(MemorySlotRef ref);

	public boolean equipMemory(MemorySlotRef ref, int maxSlots);

	public boolean unequipMemory(MemorySlotRef ref);

	/** Returns {@code true} if the given manipulation is currently equipped. */
	public boolean isManipEquipped(BloodManipulation manip);

	/**
	 * Attempts to equip a manipulation by name into a free slot.
	 *
	 * @return {@code true} if the manipulation was equipped, {@code false} if no free slot
	 */
	public boolean equipManip(String manipName, int maxSlots);

	/** Removes a manipulation from the equipped slots. */
	public boolean unequipManip(String manipName);

	public boolean isPassiveActive(String manipName);

	/** Toggles an equipped passive and returns its new active state. */
	public boolean togglePassive(String manipName);

	// Synaptic memory loadouts

	public List<ManipulationLoadout> getLoadouts();

	public void setLoadouts(List<ManipulationLoadout> loadouts);

	public ManipulationLoadout getLoadout(int slotIndex);

	public void setLoadout(int slotIndex, ManipulationLoadout loadout);

}
