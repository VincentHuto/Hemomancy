package com.vincenthuto.hemomancy.common.menu.tile.crafting;

import java.util.Objects;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.item.scar.pattern.ItemScarPattern;
import com.vincenthuto.hemomancy.common.menu.slot.ScarStationSlot;
import com.vincenthuto.hemomancy.common.menu.slot.OutputSlot;
import com.vincenthuto.hemomancy.common.menu.slot.ScarPatternSlot;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.tile.crafting.ScarStationBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ScarStationMenu extends AbstractContainerMenu {
	private final ScarStationBlockEntity te;
	public int[] activatedScars;

	public ScarStationMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	public ScarStationMenu(final int windowId, final Inventory playerInv, final ScarStationBlockEntity te) {
		super(ContainerInit.scar_station.get(), windowId);
		this.te = te;
		// SLOTS
		addSlot(new ScarStationSlot(te, 3, 8, 14));
		addSlot(new Slot(te, 0, 8, 18 + 1 * 18));
		addSlot(new Slot(te, 1, 8, 22 + 2 * 18));
		addSlot(new ScarPatternSlot(te, 4, 8, 26 + 3 * 18));
		addSlot(new OutputSlot(te, 2, 145, 44));
		// INVENTORY
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x + y * 9 + 9, 8 + x * 18, 104 + y * 18));
			}
		}
		// HOTBAR
		for (int x = 0; x < 9; x++) {
			this.addSlot(new Slot(playerInv, x, 8 + x * 18, 162));
		}

	}

	private static ScarStationBlockEntity getBlockEntity(final Inventory playerInv, final FriendlyByteBuf data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final BlockEntity tileAtPos = playerInv.player.level().getBlockEntity(data.readBlockPos());
		if (tileAtPos instanceof ScarStationBlockEntity) {
			return (ScarStationBlockEntity) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return this.te.stillValid(playerIn);
	}

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
	}

	@Override
	public void broadcastChanges() {
		te.sendUpdates();
		super.broadcastChanges();
	}

	@Override
	public void setItem(int p_182407_, int p_182408_, ItemStack p_182409_) {
		te.sendUpdates();
		super.setItem(p_182407_, p_182408_, p_182409_);
	}

	private static final int PATTERN_SLOT_INDEX = 3;

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		super.clicked(slotId, dragType, clickTypeIn, player);
		checkPatternSlotCleared();
		te.sendUpdates();
	}

	/**
	 * If the pattern slot no longer contains an ItemScarPattern, clear the scar grid.
	 */
	private void checkPatternSlotCleared() {
		ItemStack patternStack = this.slots.get(PATTERN_SLOT_INDEX).getItem();
		if (patternStack.isEmpty() || !(patternStack.getItem() instanceof ItemScarPattern)) {
			te.setScarList(ScarRecipe.blank());
		}
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		// Slot indices in this container:
		// 0 = ScarStationSlot (knapper tool)
		// 1 = Ingredient 1
		// 2 = Ingredient 2
		// 3 = ScarPatternSlot
		// 4 = OutputSlot
		// 5-31 = Player inventory (27 slots)
		// 32-40 = Hotbar (9 slots)
		final int CONTAINER_START = 0;
		final int CONTAINER_END = 5; // exclusive
		final int PLAYER_INV_START = 5;
		final int PLAYER_INV_END = 32; // exclusive
		final int HOTBAR_START = 32;
		final int HOTBAR_END = 41; // exclusive

		ItemStack returnStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot == null || !slot.hasItem()) {
			return returnStack;
		}

		ItemStack slotStack = slot.getItem();
		returnStack = slotStack.copy();

		if (index >= CONTAINER_START && index < CONTAINER_END) {
			// Shift-click FROM a container slot -> move to player inventory
			if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, HOTBAR_END, true)) {
				return ItemStack.EMPTY;
			}
		} else {
			// Shift-click FROM player inventory/hotbar -> try to place in the right container slot
			// Try chisel slot first (slot 0) for knappers
			if (this.slots.get(0).mayPlace(slotStack)) {
				if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			}
			// Try Scar Pattern slot (slot 3) for patterns/binders
			else if (this.slots.get(3).mayPlace(slotStack)) {
				if (!this.moveItemStackTo(slotStack, 3, 4, false)) {
					return ItemStack.EMPTY;
				}
			}
			// Try ingredient slots (slots 1–2) for anything else
			else if (!this.moveItemStackTo(slotStack, 1, 3, false)) {
				// If ingredient slots are full, move between inventory <-> hotbar
				if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
					if (!this.moveItemStackTo(slotStack, HOTBAR_START, HOTBAR_END, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= HOTBAR_START && index < HOTBAR_END) {
					if (!this.moveItemStackTo(slotStack, PLAYER_INV_START, PLAYER_INV_END, false)) {
						return ItemStack.EMPTY;
					}
				}
				return ItemStack.EMPTY;
			}
		}

		if (slotStack.isEmpty()) {
			slot.set(ItemStack.EMPTY);
		} else {
			slot.setChanged();
		}
		checkPatternSlotCleared();
		te.sendUpdates();
		return returnStack;
	}

	public ScarStationBlockEntity getTe() {
		return this.te;
	}

}
