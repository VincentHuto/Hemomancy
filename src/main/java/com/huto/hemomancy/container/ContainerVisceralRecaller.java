package com.huto.hemomancy.container;

import java.util.Objects;

import com.huto.hemomancy.containers.slot.SlotOutput;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.item.ItemBloodyFlask;
import com.huto.hemomancy.item.ItemEnzyme;
import com.huto.hemomancy.item.memories.ItemHematicMemory;
import com.huto.hemomancy.tile.TileEntityVisceralRecaller;
import com.hutoslib.common.container.SlotSelectiveType;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tileentity.TileEntity;

public class ContainerVisceralRecaller extends AbstractContainerMenu {
	private final int numRows;
	private final TileEntityVisceralRecaller te;
	public int[] activatedRunes;

	public ContainerVisceralRecaller(final int windowId, final Inventory playerInventory,
			final FriendlyByteBuf data) {
		this(windowId, playerInventory, getTileEntity(playerInventory, data));
	}

	public ContainerVisceralRecaller(final int windowId, final Inventory playerInv,
			final TileEntityVisceralRecaller te) {
		super(ContainerInit.visceral_recaller.get(), windowId);
		this.te = te;
		this.numRows = 4;
		// te.openInventory(player);
		// SLOTS
		addSlot(new SlotSelectiveType(te, ItemHematicMemory.class, 0, 64, 8, 14));
		addSlot(new SlotSelectiveType(te, ItemEnzyme.class, 1, 64, 26, 14));
		addSlot(new SlotSelectiveType(te, ItemBloodyFlask.class, 2, 16, 26, 80));
		addSlot(new SlotOutput(te, 3, 152, 14));

		// output
		addSlot(new SlotOutput(te, 4, 152, 80));

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

	private static TileEntityVisceralRecaller getTileEntity(final PlayerInventory playerInv, final PacketBuffer data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final TileEntity tileAtPos = playerInv.player.world.getTileEntity(data.readBlockPos());
		if (tileAtPos instanceof TileEntityVisceralRecaller) {
			return (TileEntityVisceralRecaller) tileAtPos;
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

	public TileEntityVisceralRecaller getTe() {
		return this.te;
	}

}
