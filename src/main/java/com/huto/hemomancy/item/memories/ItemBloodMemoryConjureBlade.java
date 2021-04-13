package com.huto.hemomancy.item.memories;

import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;

public class ItemBloodMemoryConjureBlade extends ItemBloodMemory {

	public ItemBloodMemoryConjureBlade(Properties properties) {
		super(properties);

	}

	@Override
	public BloodManipulation getManip() {
		return ManipulationInit.conjure_blade;
	}

}
