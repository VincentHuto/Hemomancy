package com.huto.hemomancy.item.memories;

import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;

public class ItemBloodMemorySanguineWard extends ItemBloodMemory {

	public ItemBloodMemorySanguineWard(Properties properties) {
		super(properties);

	}

	@Override
	public BloodManipulation getManip() {
		return ManipulationInit.sanguine_ward;
	}

}
