package com.vincenthuto.hemomancy.radial;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.LivingEntity;

public interface ICharmContainer {
	@Nonnull
	LivingEntity getOwner();

	@Nonnull
	ImmutableList<CharmSlotItemHandler> getSlots();

	void onContentsChanged(CharmSlotItemHandler slot);
}
