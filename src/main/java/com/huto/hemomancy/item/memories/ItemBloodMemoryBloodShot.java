package com.huto.hemomancy.item.memories;

import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;

public class ItemBloodMemoryBloodShot extends ItemBloodMemory {

	public ItemBloodMemoryBloodShot(Properties properties) {
		super(properties);

	}

	@Override
	public BloodManipulation getManip() {
		return ManipulationInit.blood_shot;
	}

}
