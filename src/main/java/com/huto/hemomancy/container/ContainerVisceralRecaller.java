package com.huto.hemomancy.container;

import java.util.Objects;

import com.huto.hemomancy.containers.slot.SlotOutput;
import com.huto.hemomancy.init.ContainerInit;
import com.huto.hemomancy.item.ItemBloodyFlask;
import com.huto.hemomancy.item.ItemEnzyme;
import com.huto.hemomancy.item.memories.ItemHematicMemory;
import com.huto.hemomancy.tile.BlockEntityVisceralRecaller;
import com.hutoslib.common.container.SlotSelectiveType;

import net.minecraft.entity.player.Player;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tileentity.BlockEntity;
import net.minecraft.world.entity.player.getInventory();
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerVisceralRecaller extends AbstractContainerMenu {
	private final int numRows;
	private final BlockEntityVisceralRecaller te;
	public int[] activatedRunes;

	public ContainerVisceralRecaller(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	public ContainerVisceralRecaller(final int windowId, final Inventory playerInv,
			final BlockEntityVisceralRecaller te) {
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

	private static BlockEntityVisceralRecaller getBlockEntity(final PlayerInventory playerInv, final FriendlyByteBuf data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final BlockEntity tileAtPos = playerInv.player.level.getBlockEntity(data.readBlockPos());
		if (tileAtPos instanceof BlockEntityVisceralRecaller) {
			return (BlockEntityVisceralRecaller) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return true;
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
	public void setItem(int slotID, ItemStack stack) {
		te.sendUpdates();
		super.setItem(slotID, stack);
	}

	@Override
	public ItemStack clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		te.sendUpdates();
		return super.clicked(slotId, dragType, clickTypeIn, player);

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

	public BlockEntityVisceralRecaller getTe() {
		return this.te;
	}

}
