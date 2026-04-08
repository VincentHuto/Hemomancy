package com.vincenthuto.hemomancy.common.item.morphlings;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

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
	 * Returns the primary blood tendency this morphling prefers when being fed
	 * enzymes. Feeding a preferred enzyme grants full power contribution.
	 */
	default EnumBloodTendency getPreferredTendency() {
		return EnumBloodTendency.ANIMUS;
	}

	/**
	 * Returns a secondary blood tendency this morphling benefits from.
	 * Feeding a secondary enzyme grants 75% power contribution.
	 * Non-preferred enzymes grant 50% power contribution.
	 */
	default EnumBloodTendency getSecondaryTendency() {
		return EnumBloodTendency.MORTEM;
	}

	/**
	 * Called every drain interval while this morphling is equipped on the player.
	 * Override to apply passive effects scaled by the morphling's maturity.
	 *
	 * @param player the player wearing this morphling
	 * @param stack  the morphling ItemStack (contains maturity NBT)
	 */
	default void onEquippedTick(Player player, ItemStack stack) {
	}

}
