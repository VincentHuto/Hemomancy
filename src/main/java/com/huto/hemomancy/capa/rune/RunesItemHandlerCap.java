package com.huto.hemomancy.capa.rune;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;

public class RunesItemHandlerCap {

	public static class IRunesItemHandlerStorage implements Capability.IStorage<IRunesItemHandler> {

		@Nullable
		@Override
		public Tag writeNBT(Capability<IRunesItemHandler> capability, IRunesItemHandler instance, Direction side) {
			return null;
		}

		@Override
		public void readNBT(Capability<IRunesItemHandler> capability, IRunesItemHandler instance, Direction side,
				Tag nbt) {

		}
	}

	public static class IRunesItemHandlerFactory implements Callable<IRunesItemHandler> {

		@Override
		public IRunesItemHandler call() {
			return new IRunesItemHandler() {
				@Override
				public boolean isItemValidForSlot(int slot, ItemStack stack) {
					return false;
				}

				@Override
				public boolean isEventBlocked() {
					return false;
				}

				@Override
				public void setEventBlock(boolean blockEvents) {

				}

				@Override
				public void setStackInSlot(int slot, @Nonnull ItemStack stack) {

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
				public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
					return false;
				}

				@Override
				public void tick() {

				}
			};
		}
	}
}
