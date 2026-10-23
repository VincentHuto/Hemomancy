package com.vincenthuto.hemomancy.common.menu;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

/** O-3: a menu with no slots that only opens a screen (tendency, vascular, scrying, reliquary, structure views). */
public abstract class ViewOnlyMenu extends HemoContainerMenu {
	protected ViewOnlyMenu(@Nullable MenuType<?> type, int containerId) {
		super(type, containerId);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}
}
