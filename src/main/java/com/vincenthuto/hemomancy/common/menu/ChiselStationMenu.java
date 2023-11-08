package com.vincenthuto.hemomancy.common.menu;

import java.util.Objects;

import com.vincenthuto.hemomancy.common.init.ContainerInit;
import com.vincenthuto.hemomancy.common.menu.slot.ChiselSlot;
import com.vincenthuto.hemomancy.common.menu.slot.OutputSlot;
import com.vincenthuto.hemomancy.common.menu.slot.RunePatternSlot;
import com.vincenthuto.hemomancy.common.tile.ChiselStationBlockEntity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChiselStationMenu extends AbstractContainerMenu {
	private final int numRows;
	private final ChiselStationBlockEntity te;
	public int[] activatedRunes;

	public ChiselStationMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	public ChiselStationMenu(final int windowId, final Inventory playerInv, final ChiselStationBlockEntity te) {
		super(ContainerInit.runic_chisel_station.get(), windowId);
		this.te = te;
		this.numRows = 4;
		// te.openInventory(player);
		// SLOTS
		addSlot(new ChiselSlot(te, 3, 8, 14));
		addSlot(new Slot(te, 0, 8, 18 + 1 * 18));
		addSlot(new Slot(te, 1, 8, 22 + 2 * 18));
		addSlot(new RunePatternSlot(te, 4, 8, 26 + 3 * 18));
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

	private static ChiselStationBlockEntity getBlockEntity(final Inventory playerInv, final FriendlyByteBuf data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final BlockEntity tileAtPos = playerInv.player.level().getBlockEntity(data.readBlockPos());
		if (tileAtPos instanceof ChiselStationBlockEntity) {
			return (ChiselStationBlockEntity) tileAtPos;
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

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		super.clicked(slotId, dragType, clickTypeIn, player);
		te.sendUpdates();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		te.sendUpdates();
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemStack = slot.getItem();
			stack = itemStack.copy();
			if (index < this.numRows * 9) {
				if (!this.moveItemStackTo(itemStack, this.numRows * 9, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(itemStack, 0, this.numRows * 9, false)) {
				return ItemStack.EMPTY;
			}
			if (itemStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return stack;
	}

	public ChiselStationBlockEntity getTe() {
		return this.te;
	}

}
