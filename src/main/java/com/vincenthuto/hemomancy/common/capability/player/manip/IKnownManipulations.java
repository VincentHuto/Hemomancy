package com.vincenthuto.hemomancy.common.capability.player.manip;

import java.util.LinkedHashMap;
import java.util.List;

import com.vincenthuto.hemomancy.common.capability.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;

import net.minecraft.core.BlockPos;

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

	public boolean isAvatarActive();

	public void setAvatarActive(boolean avatarActive);

	public void setKnownManips(LinkedHashMap<BloodManipulation, ManipLevel> knownManips);

	public void setSelectedManip(BloodManipulation selectedManip);

	public void setSelectedManipLevel(int level);

	public void setSelectedVein(VeinLocation selectedVein);

	public void setVeinList(List<VeinLocation> dimPos);

	// ── Equipped manipulation slots ──

	/** Returns the list of manipulation names currently equipped in the player's limited slots. */
	public List<String> getEquippedManipNames();

	/** Overwrites the equipped manipulation name list. */
	public void setEquippedManipNames(List<String> names);

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

}
