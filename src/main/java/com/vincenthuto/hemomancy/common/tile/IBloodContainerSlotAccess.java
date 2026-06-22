package com.vincenthuto.hemomancy.common.tile;

import net.minecraft.world.Container;

public interface IBloodContainerSlotAccess {
	int getBloodContainerInputSlot();

	int getEmptyBloodContainerOutputSlot();

	default boolean acceptsBloodGourdInput() {
		return false;
	}

	default double getBloodGourdInputTransferRate() {
		return 10.0D;
	}

	default boolean processBloodContainerInputSlot(Container container, IBloodReservoir reservoir) {
		return BloodContainerTransfer.fillReservoirFromSlot(container, reservoir, this);
	}
}
