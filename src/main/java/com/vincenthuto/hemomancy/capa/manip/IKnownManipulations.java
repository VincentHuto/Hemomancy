package com.vincenthuto.hemomancy.capa.manip;

import java.util.HashMap;
import java.util.List;

import com.vincenthuto.hemomancy.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.manipulation.ManipLevel;

public interface IKnownManipulations {
	
	public HashMap<BloodManipulation,ManipLevel> skillsMap = new HashMap<BloodManipulation,ManipLevel>();
	
	public List<BloodManipulation> getKnownManips();

	public void setKnownManips(List<BloodManipulation> knownManips);

	public BloodManipulation getSelectedManip();

	public void setSelectedManip(BloodManipulation selectedManip);

	public boolean doesListContainName(List<BloodManipulation> knownList, BloodManipulation manip);

}
