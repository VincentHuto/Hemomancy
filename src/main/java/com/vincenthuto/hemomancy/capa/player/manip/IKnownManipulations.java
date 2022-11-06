package com.vincenthuto.hemomancy.capa.player.manip;

import java.util.LinkedHashMap;
import java.util.List;

import com.vincenthuto.hemomancy.capa.block.vein.VeinLocation;
import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;

import net.minecraft.core.BlockPos;

public interface IKnownManipulations {

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

}
