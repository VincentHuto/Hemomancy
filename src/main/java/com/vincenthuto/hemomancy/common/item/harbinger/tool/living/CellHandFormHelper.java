package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class CellHandFormHelper {
	private CellHandFormHelper() {
	}

	/**
	 * Turns a cell hand held in the main hand into {@code verb}'s hand. Anything else in the hand is left alone.
	 *
	 * @return whether the hand changed
	 */
	public static boolean applyVerb(Player player, Verb verb) {
		ItemStack held = player.getMainHandItem();
		if (!isCellHandForm(held)) {
			return false;
		}
		Item targetItem = itemFor(verb);
		if (held.is(targetItem)) {
			return false;
		}
		if (player.isUsingItem() && player.getUsedItemHand() == InteractionHand.MAIN_HAND) {
			player.stopUsingItem();
		}
		ItemStack turned = new ItemStack(targetItem);
		// The hand keeps what its conjure paid, so a dispel after a turn still returns its share.
		Double paid = held.get(DataComponentInit.CONJURE_PAID.get());
		if (paid != null) turned.set(DataComponentInit.CONJURE_PAID.get(), paid);
		player.setItemInHand(InteractionHand.MAIN_HAND, turned);
		return true;
	}

	public static boolean isCellHandForm(ItemStack stack) {
		return !stack.isEmpty()
				&& (stack.getItem() instanceof BloodAbsorptionItem
				|| stack.getItem() instanceof BloodProjectionItem);
	}

	public static Item itemFor(Verb verb) {
		return verb == Verb.PROJECT ? ItemInit.blood_projection.get() : ItemInit.blood_absorption.get();
	}
}
