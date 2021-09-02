package com.vincenthuto.hemomancy.capa.manip;

import java.util.List;

import com.vincenthuto.hemomancy.manipulation.BloodManipulation;

public interface IKnownManipulations {
	public List<BloodManipulation> getKnownManips();

	public void setKnownManips(List<BloodManipulation> knownManips);

	public BloodManipulation getSelectedManip();

	public void setSelectedManip(BloodManipulation selectedManip);

	public boolean doesListContainName(List<BloodManipulation> knownList, BloodManipulation manip);

}
