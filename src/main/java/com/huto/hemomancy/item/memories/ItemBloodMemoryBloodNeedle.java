package com.huto.hemomancy.item.memories;

import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;

public class ItemBloodMemoryBloodNeedle extends ItemBloodMemory {

	public ItemBloodMemoryBloodNeedle(Properties properties) {
		super(properties);

	}

	@Override
	public BloodManipulation getManip() {
		return ManipulationInit.blood_needle;
	}

}
