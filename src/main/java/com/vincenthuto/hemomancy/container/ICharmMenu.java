package com.vincenthuto.hemomancy.container;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.entity.LivingEntity;

public interface ICharmMenu {
	@Nonnull
	LivingEntity getOwner();

	@Nonnull
	ImmutableList<CharmSlotItemHandler> getCharmSlots();

	void onCharmContentsChanged(CharmSlotItemHandler slot);
}
