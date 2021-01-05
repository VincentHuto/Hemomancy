package com.huto.hemomancy.containers;

import java.util.Objects;

import com.huto.hemomancy.containers.slots.SlotChisel;
import com.huto.hemomancy.containers.slots.SlotOutput;
import com.huto.hemomancy.containers.slots.SlotRunePattern;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.tile.TileEntityChiselStation;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

public class ContainerChiselStation extends Container {
	private final int numRows;
	private final TileEntityChiselStation te;
	public int[] activatedRunes;

	public ContainerChiselStation(final int windowId, final PlayerInventory playerInventory, final PacketBuffer data) {
		this(windowId, playerInventory, getTileEntity(playerInventory, data));
	}

	public ContainerChiselStation(final int windowId, final PlayerInventory playerInv,
			final TileEntityChiselStation te) {
		super(ContainerInit.runic_chisel_station.get(), windowId);
		this.te = te;
		this.numRows = 4;
		// te.openInventory(player);
		// SLOTS
		addSlot(new SlotChisel(te, 3, 8, 14));
		addSlot(new Slot(te, 0, 8, 18 + 1 * 18));
		addSlot(new Slot(te, 1, 8, 22 + 2 * 18));
		addSlot(new SlotRunePattern(te, 4, 8, 26 + 3 * 18));

		addSlot(new SlotOutput((IInventory) te, 2, 145, 44));
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

	private static TileEntityChiselStation getTileEntity(final PlayerInventory playerInv, final PacketBuffer data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final TileEntity tileAtPos = playerInv.player.world.getTileEntity(data.readBlockPos());
		if (tileAtPos instanceof TileEntityChiselStation) {
			return (TileEntityChiselStation) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn) {
		return true;
	}

	@Override
	public void onContainerClosed(PlayerEntity playerIn) {
		super.onContainerClosed(playerIn);
	}

	@Override
	public void detectAndSendChanges() {
		te.sendUpdates();
		super.detectAndSendChanges();
	}

	
	@Override
	public void putStackInSlot(int slotID, ItemStack stack) {
		te.sendUpdates();
		super.putStackInSlot(slotID, stack);
	}
	
	@Override
	public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
		te.sendUpdates();
		return super.slotClick(slotId, dragType, clickTypeIn, player);
	}

	
	
	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
		ItemStack stack = ItemStack.EMPTY;
		te.sendUpdates();
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemStack = slot.getStack();
			stack = itemStack.copy();
			if (index < this.numRows * 9) {
				if (!this.mergeItemStack(itemStack, this.numRows * 9, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemStack, 0, this.numRows * 9, false)) {
				return ItemStack.EMPTY;
			}
			if (itemStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}
		return stack;
	}

	public TileEntityChiselStation getTe() {
		return this.te;
	}

}
