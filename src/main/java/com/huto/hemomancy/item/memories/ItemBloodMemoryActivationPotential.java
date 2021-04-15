package com.huto.hemomancy.item.memories;

import com.huto.hemomancy.init.ManipulationInit;
import com.huto.hemomancy.manipulation.BloodManipulation;

public class ItemBloodMemoryActivationPotential extends ItemBloodMemory {

	public ItemBloodMemoryActivationPotential(Properties properties) {
		super(properties);

	}

	@Override
	public BloodManipulation getManip() {
		return ManipulationInit.activation_potential;
	}

}
