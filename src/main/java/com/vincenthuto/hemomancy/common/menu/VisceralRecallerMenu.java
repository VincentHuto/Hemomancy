package com.vincenthuto.hemomancy.common.menu;

import java.util.Objects;

import com.vincenthuto.hemomancy.common.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.common.item.memories.HematicMemoryItem;
import com.vincenthuto.hemomancy.common.menu.slot.OutputSlot;
import com.vincenthuto.hemomancy.common.registry.ContainerInit;
import com.vincenthuto.hemomancy.common.tile.VisceralRecallerBlockEntity;
import com.vincenthuto.hutoslib.common.container.SlotSelectiveType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class VisceralRecallerMenu extends AbstractContainerMenu {
	private static VisceralRecallerBlockEntity getBlockEntity(final Inventory playerInv, final FriendlyByteBuf data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final BlockEntity tileAtPos = playerInv.player.level().getBlockEntity(data.readBlockPos());
		if (tileAtPos instanceof VisceralRecallerBlockEntity) {
			return (VisceralRecallerBlockEntity) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}
	private final int numRows;
	private final VisceralRecallerBlockEntity te;

	public int[] activatedRunes;

	public VisceralRecallerMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	public VisceralRecallerMenu(final int windowId, final Inventory playerInv, final VisceralRecallerBlockEntity te) {
		super(ContainerInit.visceral_recaller.get(), windowId);
		this.te = te;
		this.numRows = 4;
		// SLOTS
		addSlot(new SlotSelectiveType(te, HematicMemoryItem.class, 0, 1, 8, 14));
		addSlot(new Slot(te, 1, 26, 14));
		addSlot(new SlotSelectiveType(te, BloodyFlaskItem.class, 2, 16, 26, 80));
		addSlot(new OutputSlot(te, 3, 152, 14));
		// output
		addSlot(new OutputSlot(te, 4, 152, 80));
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

	@Override
	public void broadcastChanges() {
		te.sendUpdates();
		super.broadcastChanges();

	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		super.clicked(slotId, dragType, clickTypeIn, player);
		te.sendUpdates();

	}

	public VisceralRecallerBlockEntity getTe() {
		return this.te;
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

	@Override
	public void removed(Player playerIn) {
		super.removed(playerIn);
	}

	@Override
	public void setItem(int p_182407_, int p_182408_, ItemStack p_182409_) {
		super.setItem(p_182407_, p_182408_, p_182409_);
		te.sendUpdates();
	}

	@Override
	public boolean stillValid(Player playerIn) {
		return this.te.stillValid(playerIn);
	}

}
