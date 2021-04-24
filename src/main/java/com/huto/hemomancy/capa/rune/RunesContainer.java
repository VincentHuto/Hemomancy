package com.huto.hemomancy.capa.rune;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class RunesContainer extends ItemStackHandler implements IRunesItemHandler {

	private final static int RUNE_SLOTS =8;
	private final ItemStack[] previous = new ItemStack[RUNE_SLOTS];
	private boolean[] changed = new boolean[RUNE_SLOTS];
	private boolean blockEvents = false;
	private LivingEntity holder;

	public RunesContainer(LivingEntity player) {
		super(RUNE_SLOTS);
		this.holder = player;
		Arrays.fill(previous, ItemStack.EMPTY);
	}

	@Override
	public void setSize(int size) {
		if (size != RUNE_SLOTS)
			System.out.println("Cannot resize rune container");
	}

	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		LazyOptional<IRune> opt = stack.getCapability(RunesCapabilities.ITEM_RUNE);
		if (stack.isEmpty() || !opt.isPresent())
			return false;
		IRune mindrune = opt.orElseThrow(NullPointerException::new);
		return mindrune.canEquip(holder) && mindrune.getRuneType().hasSlot(slot);
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		if (stack.isEmpty() || this.isItemValidForSlot(slot, stack)) {
			super.setStackInSlot(slot, stack);
		}
	}

	@Override
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (!this.isItemValidForSlot(slot, stack))
			return stack;
		return super.insertItem(slot, stack, simulate);
	}

	@Override
	public boolean isEventBlocked() {
		return blockEvents;
	}

	@Override
	public void setEventBlock(boolean blockEvents) {
		this.blockEvents = blockEvents;
	}

	@Override
	protected void onContentsChanged(int slot) {
		this.changed[slot] = true;
	}

	public void tick() {
		for (int i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			if (stack.getItem() != Items.AIR) {
				stack.getCapability(RunesCapabilities.ITEM_RUNE).ifPresent(b -> b.onWornTick(holder));
			}
		}
		sync();
	}

	private void sync() {
		if (!(holder instanceof ServerPlayerEntity)) {
			return;
		}

		List<PlayerEntity> receivers = null;
		for (byte i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			boolean autosync = stack.getCapability(RunesCapabilities.ITEM_RUNE).map(b -> b.willAutoSync(holder))
					.orElse(false);
			if (changed[i] || autosync && !ItemStack.areItemStacksEqual(stack, previous[i])) {
				if (receivers == null) {
					receivers = new ArrayList<>(((ServerWorld) holder.world).getPlayers());
					receivers.add((ServerPlayerEntity) holder);
				}
				RuneEntityEventHandler.syncSlot((ServerPlayerEntity) holder, i, stack, receivers);
				this.changed[i] = false;
				previous[i] = stack.copy();
			}
		}
	}

	public LivingEntity getHolder() {
		return this.holder;
	}
}
