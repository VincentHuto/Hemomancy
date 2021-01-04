
package com.huto.hemomancy.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public abstract class TileSimpleInventory extends TileMod {
	public TileSimpleInventory(TileEntityType<?> tileEntityTypeIn) {
		super(tileEntityTypeIn);
	}

	public SimpleItemStackHandler itemHandler = createItemHandler();

	@Override
	public void readPacketNBT(CompoundNBT par1CompoundNBT) {
		itemHandler = createItemHandler();
		itemHandler.deserializeNBT(par1CompoundNBT);
	}

	@Override
	public void writePacketNBT(CompoundNBT par1CompoundNBT) {
		par1CompoundNBT.merge(itemHandler.serializeNBT());

	}

	public boolean addItem(@Nullable PlayerEntity player, ItemStack stack, @Nullable Hand hand) {
		boolean did = false;
		for (int i = 0; i < getSizeInventory(); i++)
			if (itemHandler.getStackInSlot(i).isEmpty()) {
				did = true;
				ItemStack stackToAdd = stack.copy();
				stackToAdd.setCount(1);
				itemHandler.setStackInSlot(i, stackToAdd);
				if (player == null || !player.abilities.isCreativeMode) {
					stack.shrink(1);
				}
				break;
			}
		if (did) {
			return true;
		} else {
			return false;
		}
	}

	public abstract int getSizeInventory();

	protected SimpleItemStackHandler createItemHandler() {
		return new SimpleItemStackHandler(this, true);
	}

	public IItemHandlerModifiable getItemHandler() {
		return itemHandler;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		/*
		 * if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return
		 * CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
		 */
		return super.getCapability(cap, side);
	}

	public BlockState getState() {
		return world.getBlockState(pos);
	}

	public void setBlockToUpdate() {
		sendUpdates();
	}

	public void sendUpdates() {
		world.markBlockRangeForRenderUpdate(pos, getState(), getState());
		world.notifyBlockUpdate(pos, getState(), getState(), 3);
		markDirty();
	}
	public boolean canHopperExtract() {
		return false;
	}
	/*
	 * Extension of ItemStackHandler that uses our own slot array, allows for
	 * control of writing, allows control over stack limits, and allows for
	 * itemstack-slot validation
	 */
	public static class SimpleItemStackHandler extends ItemStackHandler {

		private final boolean allowWrite;
		private final TileSimpleInventory tile;

		public SimpleItemStackHandler(TileSimpleInventory inv, boolean allowWrite) {
			super(inv.getSizeInventory());
			this.allowWrite = allowWrite;
			tile = inv;
		}

		@Nonnull
		@Override
		public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
			if (allowWrite) {
				return super.insertItem(slot, stack, simulate);
			} else
				return stack;
		}

		@Nonnull
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			if (allowWrite) {

				return super.extractItem(slot, amount, simulate);
			} else
				return ItemStack.EMPTY;
		}

		@Override
		public void onContentsChanged(int slot) {
			tile.markDirty();
		}
	}
}
