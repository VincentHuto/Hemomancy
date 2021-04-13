package com.huto.hemomancy.item.memories;

import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;

public class ItemBloodMemoryConjureClaws extends ItemBloodMemory {

	public ItemBloodMemoryConjureClaws(Properties properties) {
		super(properties);
	}

	@Override
	public BloodManipulation getManip() {
		return ManipulationInit.conjure_claw;
	}

}
