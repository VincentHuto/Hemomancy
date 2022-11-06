package com.vincenthuto.hemomancy.capa.player.rune;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import net.minecraft.world.item.ItemStack;

public class RunesItemHandlerCap {


	public static class IRunesItemHandlerFactory implements Callable<IRunesItemHandler> {

		@Override
		public IRunesItemHandler call() {
			return new IRunesItemHandler() {
				@Nonnull
				@Override
				public ItemStack extractItem(int slot, int amount, boolean simulate) {
					return null;
				}

				@Override
				public int getSlotLimit(int slot) {
					return 0;
				}

				@Override
				public int getSlots() {
					return 0;
				}

				@Nonnull
				@Override
				public ItemStack getStackInSlot(int slot) {
					return null;
				}

				@Nonnull
				@Override
				public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
					return null;
				}

				@Override
				public boolean isEventBlocked() {
					return false;
				}

				@Override
				public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
					return false;
				}

				@Override
				public boolean isItemValidForSlot(int slot, ItemStack stack) {
					return false;
				}

				@Override
				public void setEventBlock(boolean blockEvents) {

				}

				@Override
				public void setStackInSlot(int slot, @Nonnull ItemStack stack) {

				}

				@Override
				public void tick() {

				}
			};
		}
	}
}
