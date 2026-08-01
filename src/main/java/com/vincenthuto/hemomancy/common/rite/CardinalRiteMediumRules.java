package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.tile.functional.CardinalFocusBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/** Exact matching rules for the single item seated in a Cardinal Focus. */
public final class CardinalRiteMediumRules {
	private CardinalRiteMediumRules() {
	}

	public static boolean matches(Ingredient required, ItemStack seated) {
		boolean requiresMedium = required != null && required != Ingredient.EMPTY && !required.isEmpty();
		ItemStack actual = seated == null ? ItemStack.EMPTY : seated;
		return requiresMedium ? required.test(actual) : actual.isEmpty();
	}

	public static boolean consume(CardinalFocusBlockEntity focus, Ingredient required) {
		if (focus == null || !matches(required, focus.getMediumForMatching())) return false;
		return focus.consumeMedium(required);
	}
}
