package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class LivingArsenalInventoryGuard {
	private LivingArsenalInventoryGuard() {
	}

	public static boolean isLivingArsenalItem(ItemStack stack) {
		return !stack.isEmpty() && isLivingArsenalItem(stack.getItem());
	}

	private static boolean isLivingArsenalItem(Item item) {
		return item == ItemInit.living_staff.get()
				|| item == ItemInit.living_blade.get()
				|| item == ItemInit.living_axe.get()
				|| item == ItemInit.living_spear.get()
				|| item == ItemInit.living_baghnakh.get()
				|| item == ItemInit.living_crossbow.get()
				|| item == ItemInit.living_torch.get()
				|| item == ItemInit.living_flail.get()
				|| item == ItemInit.living_sickle.get();
	}

	public static boolean summonOrRecoverStaff(Player player, ItemStack heldItemMainhand) {
		if (!heldItemMainhand.isEmpty()) {
			return false;
		}
		ItemStack recoveredStaff = findRecoverableStaff(player);
		removeAllLivingArsenal(player);
		player.setItemInHand(InteractionHand.MAIN_HAND,
				recoveredStaff.isEmpty() ? new ItemStack(ItemInit.living_staff.get()) : recoveredStaff);
		sanitizePlayerInventory(player);
		return true;
	}

	public static void sanitizePlayerInventory(Player player) {
		if (player.level().isClientSide) {
			return;
		}
		Inventory inventory = player.getInventory();
		int keepSlot = findPreferredLivingArsenalSlot(player);
		if (keepSlot < 0) {
			return;
		}
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!isLivingArsenalItem(stack)) {
				continue;
			}
			if (slot == keepSlot) {
				stack.setCount(1);
				if (slot != inventory.selected && LivingSicklePruning.isTemporarySickle(stack)) {
					stack = LivingSicklePruning.restoredWeaponStack(stack, player.registryAccess());
					inventory.setItem(slot, stack);
				}
				if (slot != inventory.selected && LivingStaffWeaponFormHelper.isTransformedStaffWeapon(stack)) {
					inventory.setItem(slot, LivingStaffWeaponFormHelper.restoredStaffStack(stack, player.registryAccess()));
				}
			} else {
				inventory.setItem(slot, ItemStack.EMPTY);
			}
		}
	}

	public static void restoreLivingArsenalFromNonPlayerSlots(Player player, AbstractContainerMenu menu) {
		if (player.level().isClientSide || menu == null) {
			return;
		}
		boolean changed = false;
		for (Slot slot : menu.slots) {
			if (slot.container == player.getInventory()) {
				continue;
			}
			ItemStack stack = slot.getItem();
			if (!isLivingArsenalItem(stack)) {
				continue;
			}
			ItemStack returning = normalizeForPlayerInventory(stack, player.registryAccess());
			slot.set(ItemStack.EMPTY);
			slot.setChanged();
			changed = true;
			if (!playerHasLivingArsenal(player)) {
				giveBackToPlayer(player, returning);
			}
		}
		if (changed) {
			menu.broadcastChanges();
			player.displayClientMessage(Component.literal("Living weapons will not rest in dead storage.")
					.withStyle(ChatFormatting.DARK_RED), true);
		}
		sanitizePlayerInventory(player);
	}

	private static int findPreferredLivingArsenalSlot(Player player) {
		Inventory inventory = player.getInventory();
		if (isLivingArsenalItem(inventory.getItem(inventory.selected))) {
			return inventory.selected;
		}
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(ItemInit.living_staff.get())) {
				return slot;
			}
		}
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (isLivingArsenalItem(inventory.getItem(slot))) {
				return slot;
			}
		}
		return -1;
	}

	private static ItemStack findRecoverableStaff(Player player) {
		Inventory inventory = player.getInventory();
		ItemStack fallback = ItemStack.EMPTY;
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (!isLivingArsenalItem(stack)) {
				continue;
			}
			if (LivingStaffWeaponFormHelper.isTransformedStaffWeapon(stack)) {
				return LivingStaffWeaponFormHelper.restoredStaffStack(stack, player.registryAccess());
			}
			if (stack.is(ItemInit.living_staff.get())) {
				ItemStack staff = stack.copy();
				staff.setCount(1);
				return staff;
			}
			if (fallback.isEmpty()) {
				fallback = new ItemStack(ItemInit.living_staff.get());
			}
		}
		return fallback;
	}

	private static boolean playerHasLivingArsenal(Player player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (isLivingArsenalItem(inventory.getItem(slot))) {
				return true;
			}
		}
		return false;
	}

	private static void removeAllLivingArsenal(Player player) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (isLivingArsenalItem(inventory.getItem(slot))) {
				inventory.setItem(slot, ItemStack.EMPTY);
			}
		}
	}

	private static ItemStack normalizeForPlayerInventory(ItemStack stack, HolderLookup.Provider registryAccess) {
		if (LivingSicklePruning.isTemporarySickle(stack)) {
			ItemStack restored = LivingSicklePruning.restoredWeaponStack(stack, registryAccess);
			return LivingStaffWeaponFormHelper.isTransformedStaffWeapon(restored)
					? LivingStaffWeaponFormHelper.restoredStaffStack(restored, registryAccess)
					: restored;
		}
		if (LivingStaffWeaponFormHelper.isTransformedStaffWeapon(stack)) {
			return LivingStaffWeaponFormHelper.restoredStaffStack(stack, registryAccess);
		}
		ItemStack copy = stack.copy();
		copy.setCount(1);
		return copy;
	}

	private static void giveBackToPlayer(Player player, ItemStack stack) {
		if (stack.isEmpty()) {
			return;
		}
		if (player.getMainHandItem().isEmpty()) {
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			return;
		}
		if (!player.getInventory().add(stack)) {
			player.displayClientMessage(Component.literal("The living weapon tears free, leaving no room to settle.")
					.withStyle(ChatFormatting.DARK_RED), true);
		}
	}
}
