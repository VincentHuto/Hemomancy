package com.vincenthuto.hemomancy.capa.player.manip;

import java.util.LinkedHashMap;
import java.util.List;

import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.util.VeinLocation;

public interface IKnownManipulations {

	public LinkedHashMap<BloodManipulation, ManipLevel> getKnownManips();

	public void setKnownManips(LinkedHashMap<BloodManipulation, ManipLevel> knownManips);

	public BloodManipulation getSelectedManip();

	public void setSelectedManip(BloodManipulation selectedManip);

	public boolean doesListContainName(LinkedHashMap<BloodManipulation, ManipLevel> knownList, BloodManipulation manip);

	public List<BloodManipulation> getManipList();

	public ManipLevel getManipLevel(BloodManipulation manip);

	public List<ManipLevel> getLevelList();

	public ManipLevel getSelectedManipLevel();

	public void setSelectedManipLevel(int level);

	public void incrSelectedManipLevel(int incr);

	public List<VeinLocation> getVeinList();

	public void setVeinList(List<VeinLocation> dimPos);
	
}
