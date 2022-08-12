package com.vincenthuto.hemomancy.container;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.vincenthuto.hemomancy.capa.player.charm.CharmSlotItemHandler;

import net.minecraft.world.entity.LivingEntity;

public interface ICharmMenu {
	@Nonnull
	LivingEntity getOwner();

	@Nonnull
	ImmutableList<CharmSlotItemHandler> getSlots();

	void onContentsChanged(CharmSlotItemHandler slot);
}
