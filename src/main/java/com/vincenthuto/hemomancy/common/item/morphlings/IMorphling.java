package com.vincenthuto.hemomancy.common.item.morphlings;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IMorphling {

//	int getTier();

	int getBloodCost();

//	int getAllegianceChance();

	// public boolean canUseModule(int rarity);

	public void use(Player playerIn, InteractionHand handIn, ItemStack itemStack, Level worldIn);

	/**
	 * Called every drain interval while this morphling is equipped on the player.
	 * Override to apply passive effects, e.g. the spider morphling applies
	 * Arachnid Anastomosis here.
	 */
	default void onEquippedTick(Player player) {
	}

}
