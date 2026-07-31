package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/** Removes and later restores the exact bonded staff stack used to start a rite. */
public final class CardinalRiteStaffEscrow {
	private CardinalRiteStaffEscrow() {
	}

	public static ItemStack capture(ServerPlayer player) {
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (!stack.is(ItemInit.living_staff.get())) continue;
			ItemStack captured = stack.copyWithCount(1);
			stack.shrink(1);
			player.getInventory().setChanged();
			return captured;
		}
		return ItemStack.EMPTY;
	}

	public static boolean isPlanted(ServerPlayer player) {
		for (var level : player.server.getAllLevels()) {
			ActiveCardinalRite rite = CardinalRiteSavedData.get(level).getRite(player.getUUID());
			if (rite != null && rite.hasEscrowedStaff()) return true;
		}
		return false;
	}

	public static void restore(ServerPlayer player, ActiveCardinalRite rite) {
		if (player == null || rite == null || !rite.hasEscrowedStaff()) return;
		ItemStack stack = rite.releaseEscrowedStaff(player.registryAccess());
		if (!player.getInventory().add(stack)) {
			ItemEntity dropped = player.drop(stack, false);
			if (dropped != null) dropped.setNoPickUpDelay();
		}
	}
}
