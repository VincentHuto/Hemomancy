package com.huto.hemomancy.capabilities.manipulation;

import java.util.ArrayList;
import java.util.List;

import com.huto.hemomancy.manipulation.BloodManipulation;

public class KnownManipulations implements IKnownManipulations {

	List<BloodManipulation> knownManips;

	public KnownManipulations() {
		this.knownManips = new ArrayList<BloodManipulation>();
	}

	public List<BloodManipulation> getKnownManips() {
		return knownManips;
	}

	public void setKnownManips(List<BloodManipulation> knownManips) {
		this.knownManips = knownManips;
	}

}
