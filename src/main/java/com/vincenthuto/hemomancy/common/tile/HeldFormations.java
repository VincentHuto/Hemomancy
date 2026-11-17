package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.function.BooleanSupplier;

/**
 * Sanguine Formations set from clotted blood and waiting to be taken out: the one path clot leaves a block by (core-loop
 * Track 3). The Sanguine Condenser holds its formations here; any other reservoir hands scraped clot straight to the
 * player through {@link #give}. Hoppers draw through {@link #extraction}.
 */
public final class HeldFormations {
	private int count;

	public int count() {
		return count;
	}

	public void set(int count) {
		this.count = Math.max(0, count);
	}

	public void add(int amount) {
		set(count + amount);
	}

	/** Moves what fits into {@code player}'s pack; returns how many moved. */
	public int collect(ServerPlayer player) {
		int moved = give(player, count);
		count -= moved;
		return moved;
	}

	/**
	 * Puts up to {@code offered} Formations in {@code player}'s pack and returns how many actually went in. Nothing that
	 * does not fit is dropped: the caller keeps it, and the player is told why.
	 */
	public static int give(ServerPlayer player, int offered) {
		if (offered <= 0) return 0;
		Inventory inventory = player.getInventory();
		ItemStack template = new ItemStack(ItemInit.sanguine_formation.get());
		int fits = Math.min(offered, roomFor(inventory, template));
		if (fits <= 0) {
			player.displayClientMessage(Component.translatable("message.hemomancy.sanguine_condenser.no_room"), true);
			return 0;
		}
		ItemStack stack = template.copyWithCount(fits);
		inventory.add(stack);
		return Math.max(0, fits - stack.getCount());
	}

	public void drop(Level level, BlockPos pos) {
		if (count <= 0) return;
		Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
				new ItemStack(ItemInit.sanguine_formation.get(), count));
		count = 0;
	}

	/** Hoppers and pipes may draw finished formations; nothing can be put in. */
	public IItemHandler extraction(BooleanSupplier locked, Runnable changed) {
		return new IItemHandler() {
			@Override public int getSlots() { return 1; }

			@Override
			public ItemStack getStackInSlot(int slot) {
				return count <= 0 ? ItemStack.EMPTY : new ItemStack(ItemInit.sanguine_formation.get(), count);
			}

			@Override public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) { return stack; }

			@Override
			public ItemStack extractItem(int slot, int amount, boolean simulate) {
				int taken = locked.getAsBoolean() ? 0 : Math.min(amount, Math.min(count, 64));
				if (taken <= 0) return ItemStack.EMPTY;
				if (!simulate) {
					count -= taken;
					changed.run();
				}
				return new ItemStack(ItemInit.sanguine_formation.get(), taken);
			}

			@Override public int getSlotLimit(int slot) { return 64; }
			@Override public boolean isItemValid(int slot, ItemStack stack) { return false; }
		};
	}

	private static int roomFor(Inventory inventory, ItemStack template) {
		int room = 0;
		for (ItemStack slot : inventory.items) {
			if (slot.isEmpty()) room += template.getMaxStackSize();
			else if (ItemStack.isSameItemSameComponents(slot, template))
				room += Math.max(0, slot.getMaxStackSize() - slot.getCount());
		}
		return room;
	}
}
