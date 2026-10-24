package com.vincenthuto.hemomancy.compat.mna.item;

import com.mna.items.sorcery.Grimoire;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Dormant with MnA compat. When it is re-enabled, register its {@code SpellBookRenderer}
 * ({@code item/special/grimoire_harbinger_open} / {@code _closed}) in {@code client/item/ItemClientExtensions}.
 */
public class HarbingerGrimoireItem extends Grimoire {

	public HarbingerGrimoireItem(Properties properties, ResourceLocation factionHarbingersId,
			ResourceLocation open_model, ResourceLocation closed_model, boolean b) {
		super(properties, factionHarbingersId, open_model, closed_model, b);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 1;
	}

}
