package com.huto.hemomancy.capabilities.manipulation;

import java.util.List;

import com.huto.hemomancy.manipulation.BloodManipulation;

public interface IKnownManipulations {
	public List<BloodManipulation> getKnownManips();

	public void setKnownManips(List<BloodManipulation> knownManips);

	public BloodManipulation getSelectedManip();

	public void setSelectedManip(BloodManipulation selectedManip);

}
