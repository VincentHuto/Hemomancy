package com.vincenthuto.hemomancy.common.menu;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

/**
 * O-3: the base of Hemomancy's menus. {@link #addPlayerInventory} adds the player's 27 main slots and the hotbar in
 * vanilla slot order; shift-click code depends on that order, so a menu calls it where its own loops stood.
 */
public abstract class HemoContainerMenu extends AbstractContainerMenu {
	public static final int SLOT_SIZE = 18;
	public static final int HOTBAR_GAP = 58;

	protected HemoContainerMenu(@Nullable MenuType<?> type, int containerId) {
		super(type, containerId);
	}

	/** The main inventory as three rows from ({@code left}, {@code top}), then the hotbar 58 px below them. */
	protected void addPlayerInventory(Inventory inventory, int left, int top) {
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				addSlot(new Slot(inventory, col + row * 9 + 9, left + col * SLOT_SIZE, top + row * SLOT_SIZE));
			}
		}
		for (int col = 0; col < 9; col++) {
			addSlot(new Slot(inventory, col, left + col * SLOT_SIZE, top + HOTBAR_GAP));
		}
	}
}
