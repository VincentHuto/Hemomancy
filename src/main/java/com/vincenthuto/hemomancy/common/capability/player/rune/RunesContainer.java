package com.vincenthuto.hemomancy.common.capability.player.rune;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class RunesContainer extends ItemStackHandler implements IRunesItemHandler {

	private final static int RUNE_SLOTS = 8;
	private final ItemStack[] previous = new ItemStack[RUNE_SLOTS];
	private boolean[] changed = new boolean[RUNE_SLOTS];
	private boolean blockEvents = false;
	private LivingEntity holder;

	public RunesContainer(LivingEntity player) {
		super(RUNE_SLOTS);
		this.holder = player;
		Arrays.fill(previous, ItemStack.EMPTY);
	}

	public LivingEntity getHolder() {
		return this.holder;
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
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		LazyOptional<IRune> opt = stack.getCapability(RunesCapabilities.ITEM_RUNE);
		if (stack.isEmpty() || !opt.isPresent())
			return false;
		IRune mindrune = opt.orElseThrow(NullPointerException::new);
		return mindrune.canEquip(holder) && mindrune.getRuneType().hasSlot(slot);
	}

	@Override
	protected void onContentsChanged(int slot) {
		this.changed[slot] = true;
	}

	@Override
	public void setEventBlock(boolean blockEvents) {
		this.blockEvents = blockEvents;
	}

	@Override
	public void setSize(int size) {
		if (size != RUNE_SLOTS)
			System.out.println("Cannot resize rune container");
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		if (stack.isEmpty() || this.isItemValidForSlot(slot, stack)) {
			super.setStackInSlot(slot, stack);
		}
	}

	private void sync() {
		if (!(holder instanceof ServerPlayer)) {
			return;
		}

		List<Player> receivers = null;
		for (byte i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			boolean autosync = stack.getCapability(RunesCapabilities.ITEM_RUNE).map(b -> b.willAutoSync(holder))
					.orElse(false);
			if (changed[i] || autosync && !ItemStack.isSameItemSameTags(stack, previous[i])) {
				if (receivers == null) {
					receivers = new ArrayList<>(((ServerLevel) holder.level()).players());
					receivers.add((ServerPlayer) holder);
				}
				RuneEntityEventHandler.syncSlot((ServerPlayer) holder, i, stack, receivers);
				this.changed[i] = false;
				previous[i] = stack.copy();
			}
		}
	}

	@Override
	public void tick() {
		for (int i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			if (stack.getItem() != Items.AIR) {
				stack.getCapability(RunesCapabilities.ITEM_RUNE).ifPresent(b -> b.onWornTick(holder));
			}
		}
		sync();
	}
}