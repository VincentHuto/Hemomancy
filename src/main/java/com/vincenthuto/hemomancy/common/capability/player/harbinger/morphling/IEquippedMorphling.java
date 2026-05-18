package com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling;

import net.minecraft.world.item.ItemStack;

public interface IEquippedMorphling {

	ItemStack getEquippedMorphling();

	void setEquippedMorphling(ItemStack stack);

	void clearMorphling();

	boolean hasMorphling();

}
