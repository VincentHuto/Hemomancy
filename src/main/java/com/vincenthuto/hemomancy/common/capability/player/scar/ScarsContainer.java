package com.vincenthuto.hemomancy.common.capability.player.scar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityKeys;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ScarsContainer extends ItemStackHandler implements IScarsItemHandler, INBTSerializable<CompoundTag> {

	private final static int SCAR_SLOTS = 8;
	private final ItemStack[] previous = new ItemStack[SCAR_SLOTS];
	private boolean[] changed = new boolean[SCAR_SLOTS];
	private boolean blockEvents = false;
	private boolean ScarsUnlocked = false;
	private LivingEntity holder;

	public ScarsContainer() {
		super(SCAR_SLOTS);
		this.holder = null;
		Arrays.fill(previous, ItemStack.EMPTY);
	}

	public ScarsContainer(LivingEntity player) {
		super(SCAR_SLOTS);
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
		IScar mindscar = stack.getCapability(HemoCapabilityKeys.ITEM_SCAR);
		if (stack.isEmpty() || mindscar == null)
			return false;
		if (mindscar.getScarType() == ScarType.SCAR && !ScarsUnlocked)
			return false;
		return mindscar.canEquip(holder) && mindscar.getScarType().hasSlot(slot);
	}

	@Override
	public boolean isScarsUnlocked() {
		return ScarsUnlocked;
	}

	@Override
	public void setScarsUnlocked(boolean unlocked) {
		this.ScarsUnlocked = unlocked;
	}

	@Override
	protected void onContentsChanged(int slot) {
		// Mark for network sync
		this.changed[slot] = true;

		// Make equip/unequip airtight: drive side effects from the capability container,
		// not from UI slots, so shift-click/drag/hotkey swaps can't bypass cleanup.
		if (!blockEvents && holder != null) {
			ItemStack prev = previous[slot];
			ItemStack now = getStackInSlot(slot);

			if (!ItemStack.isSameItemSameTags(prev, now)) {
				if (!prev.isEmpty()) {
					IScar prevScar = prev.getCapability(HemoCapabilityKeys.ITEM_SCAR);
					if (prevScar != null) prevScar.onUnequipped(holder);
				}
				if (!now.isEmpty()) {
					IScar nowScar = now.getCapability(HemoCapabilityKeys.ITEM_SCAR);
					if (nowScar != null) nowScar.onEquipped(holder);
				}
			}
		}
	}

	@Override
	public void setEventBlock(boolean blockEvents) {
		this.blockEvents = blockEvents;
	}

	@Override
	public void setSize(int size) {
		if (size != SCAR_SLOTS)
			System.out.println("Cannot resize scar container");
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = super.serializeNBT(provider);
		nbt.putBoolean("ScarsUnlocked", ScarsUnlocked);
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		super.deserializeNBT(provider, nbt);
		this.ScarsUnlocked = nbt.getBoolean("ScarsUnlocked");
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		// No validation here: setStackInSlot is used for authoritative operations
		// (network sync, menu sync, deserialization). Player-initiated inserts are
		// already validated by insertItem() and slot mayPlace() checks.
		super.setStackInSlot(slot, stack);
	}

	private void sync() {
		if (!(holder instanceof ServerPlayer)) {
			return;
		}

		List<Player> receivers = null;
		for (byte i = 0; i < getSlots(); i++) {
			ItemStack stack = getStackInSlot(i);
			IScar scar = stack.getCapability(HemoCapabilityKeys.ITEM_SCAR);
			boolean autosync = scar != null && scar.willAutoSync(holder);
			if (changed[i] || autosync && !ItemStack.isSameItemSameTags(stack, previous[i])) {
				if (receivers == null) {
					receivers = new ArrayList<>(((ServerLevel) holder.level()).players());
					receivers.add((ServerPlayer) holder);
				}
				ScarEntityEventHandler.syncSlot((ServerPlayer) holder, i, stack, receivers);
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
				IScar scar = stack.getCapability(HemoCapabilityKeys.ITEM_SCAR);
				if (scar != null) scar.onWornTick(holder);
			}
		}
		sync();
	}
}
