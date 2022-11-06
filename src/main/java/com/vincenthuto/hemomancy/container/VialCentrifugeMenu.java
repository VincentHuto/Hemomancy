package com.vincenthuto.hemomancy.container;

import java.util.Objects;

import com.vincenthuto.hemomancy.containers.slot.OutputSlot;
import com.vincenthuto.hemomancy.init.ContainerInit;
import com.vincenthuto.hemomancy.item.BloodyFlaskItem;
import com.vincenthuto.hemomancy.tile.VialCentrifugeBlockEntity;
import com.vincenthuto.hutoslib.common.container.SlotSelectiveType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class VialCentrifugeMenu extends AbstractContainerMenu {
	private static VialCentrifugeBlockEntity getBlockEntity(final Inventory playerInv, final FriendlyByteBuf data) {
		Objects.requireNonNull(playerInv, "playerInventory cannot be null");
		Objects.requireNonNull(data, "data cannot be null");
		final BlockEntity tileAtPos = playerInv.player.level.getBlockEntity(data.readBlockPos());
		if (tileAtPos instanceof VialCentrifugeBlockEntity) {
			return (VialCentrifugeBlockEntity) tileAtPos;
		}
		throw new IllegalStateException("Tile entity is not correct! " + tileAtPos);
	}
	private final int numRows;
	private final VialCentrifugeBlockEntity te;

	private final ContainerData data;

	public VialCentrifugeMenu(final int windowId, final Inventory playerInventory, final FriendlyByteBuf data) {
		this(windowId, playerInventory, getBlockEntity(playerInventory, data));
	}

	public VialCentrifugeMenu(int windowIdIn, Inventory playerInventoryIn,
			VialCentrifugeBlockEntity blockEntityJuicinator) {
		this(windowIdIn, playerInventoryIn, blockEntityJuicinator, new SimpleContainerData(2));
	}

	public VialCentrifugeMenu(final int windowId, final Inventory playerInv, final VialCentrifugeBlockEntity te,
			final ContainerData containerData) {
		super(ContainerInit.vial_centrifuge.get(), windowId);
		this.te = te;
		this.numRows = 4;
		this.data = containerData;

		// SLOTS
		// Input side
		addSlot(new Slot(te, 0, 8, 8));
		// Blood
		addSlot(new SlotSelectiveType(te, BloodyFlaskItem.class, 1, 16, 8, 81));
		// vials
		// Clockwise
		addSlot(new Slot(te, 2, 62, 8));
		addSlot(new Slot(te, 3, 80, 26));
		addSlot(new Slot(te, 4, 98, 44));
		addSlot(new Slot(te, 5, 80, 62));

		addSlot(new Slot(te, 6, 62, 80));
		addSlot(new Slot(te, 7, 44, 62));
		addSlot(new Slot(te, 8, 26, 44));
		addSlot(new Slot(te, 9, 44, 26));

		//Main Outputs
		addSlot(new OutputSlot(te, 10, 134, 8));
		addSlot(new OutputSlot(te, 11, 152, 8));
		addSlot(new OutputSlot(te, 12, 134, 26));
		addSlot(new OutputSlot(te, 13, 154, 26));
		addSlot(new OutputSlot(te, 14, 134, 44));
		addSlot(new OutputSlot(te, 15, 154,44));
		addSlot(new OutputSlot(te, 16, 134, 62));
		addSlot(new OutputSlot(te, 17, 154,62));

		//Aux Output
		addSlot(new OutputSlot(te, 18, 144, 80));


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

	public int getSpinProgress() {
		int i = getTe().dataAccess.get(0);
		int j = 200;
		int output = j != 0 && i != 0 ? i * 24 / j : 0;
		output =24-output;
		return output;
	}

	public VialCentrifugeBlockEntity getTe() {
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
