package com.vincenthuto.hemomancy.common.menu;

import javax.annotation.Nullable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * O-3: the base of Hemomancy's menus. {@link #addPlayerInventory} adds the player's 27 main slots and the hotbar in
 * vanilla slot order; shift-click code depends on that order, so a menu calls it where its own loops stood.
 * <p>
 * A menu backed by an item the player carries (a jar, a syringe, a folio) keeps that stack by identity: it refuses any
 * click that would move the open stack ({@link #wouldMoveOpenItem}) and closes once the player no longer carries that
 * exact stack ({@link #carriesExactStack}). Checking only the item type lets a hotbar swap followed by a throw copy
 * the stack's contents into the world.
 */
public abstract class HemoContainerMenu extends AbstractContainerMenu {
	public static final int SLOT_SIZE = 18;
	public static final int HOTBAR_GAP = 58;

	protected HemoContainerMenu(@Nullable MenuType<?> type, int containerId) {
		super(type, containerId);
	}

	/** The player's main inventory or offhand holds {@code stack} itself, not a copy. */
	protected static boolean carriesExactStack(Player player, ItemStack stack) {
		if (player.getOffhandItem() == stack) return true;
		for (ItemStack carried : player.getInventory().items) {
			if (carried == stack) return true;
		}
		return false;
	}

	/**
	 * A click that would lift, swap or throw the open stack: any click on its slot, or a hotbar (button 0 to 8) or
	 * offhand (button 40) swap that names it.
	 */
	protected boolean wouldMoveOpenItem(ItemStack open, Inventory inventory, int slotId, int button, ClickType clickType) {
		if (slotId >= 0 && slotId < slots.size() && slots.get(slotId).getItem() == open) return true;
		if (clickType != ClickType.SWAP) return false;
		if (button == 40) return inventory.player.getOffhandItem() == open;
		return button >= 0 && button < 9 && inventory.getItem(button) == open;
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
