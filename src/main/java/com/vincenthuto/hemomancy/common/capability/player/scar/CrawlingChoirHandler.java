package com.vincenthuto.hemomancy.common.capability.player.scar;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.scar.functional.AntiphonomycesResonansItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Handles the Crawling Choir (Antiphonomyces resonans) echo-cast mechanic.
 * Called from BloodManipulation.performAction after a successful cast.
 */
public final class CrawlingChoirHandler {

	private CrawlingChoirHandler() {}

	/**
	 * If the player has Antiphonomyces resonans equipped in slot 0 and the current
	 * cast is not already an echo, rolls a chance to re-fire the manipulation action
	 * at no additional blood cost. Recursion is prevented via IS_ECHO_CAST.
	 */
	public static void tryEchoCast(Player player, Level world, ItemStack heldItem, BlockPos position,
			BloodManipulation manip) {
		if (AntiphonomycesResonansItem.IS_ECHO_CAST.get()) {
			return;
		}

		HemoCapabilityAccess.getScars(player).ifPresent(scars -> {
			ItemStack slot0 = scars.getStackInSlot(0);
			if (!(slot0.getItem() instanceof AntiphonomycesResonansItem)) {
				return;
			}

			if (world.random.nextFloat() < AntiphonomycesResonansItem.ECHO_CHANCE) {
				AntiphonomycesResonansItem.IS_ECHO_CAST.set(true);
				try {
					manip.getAction(player, world, heldItem, position);
				} finally {
					AntiphonomycesResonansItem.IS_ECHO_CAST.set(false);
				}
			}
		});
	}
}
