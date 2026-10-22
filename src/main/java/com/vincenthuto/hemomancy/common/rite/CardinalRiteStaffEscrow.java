package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationDiagnosticsSync;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.MemorySlotRef;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.CellHandFormHelper;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
			ItemStack captured = stack.split(1);
			// Put a fresh stack object in the slot: in creative, vanilla restores the count of the stack
			// that was used on the block after useOn returns, which would otherwise revive the escrowed staff.
			player.getInventory().setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack.copy());
			player.getInventory().setChanged();
			player.containerMenu.broadcastChanges();
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

	/**
	 * Once the staff is planted, the hand that held it becomes a Blood Projection hand with projection
	 * selected, so the caster can feed anchors and segments without opening the manipulation menu.
	 * An occupied hand is left alone; only the selection changes.
	 */
	public static void armProjectionHand(ServerPlayer player) {
		if (!HemoCapabilityAccess.getBloodVolume(player).isActive()
				|| UnstainedAccessRules.blocksKnownBloodPowerUse(HemoCapabilityAccess.getUnstainedProgress(player))) return;
		var known = HemoCapabilityAccess.getKnownManipulations(player);
		if (known == null
				|| !known.isMemoryEquipped(MemorySlotRef.manipulation(ManipulationEquipHelper.BLOOD_PROJECTION))) return;
		BloodManipulation projection = known.getManipList().stream()
				.filter(m -> m != null && ManipulationEquipHelper.BLOOD_PROJECTION.equals(m.getName()))
				.findFirst().orElse(null);
		if (projection == null) return;
		known.setSelectedManip(projection);
		ItemStack held = player.getMainHandItem();
		if (held.isEmpty() || CellHandFormHelper.isCellHandForm(held)) {
			if (!held.is(ItemInit.blood_projection.get()))
				player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_projection.get()));
		}
		KnownManipulationEvents.syncPlayerEvent(player);
		ManipulationDiagnosticsSync.sync(player);
	}

	public static void restore(ServerPlayer player, ActiveCardinalRite rite) {
		if (player == null || rite == null || !rite.hasEscrowedStaff()) return;
		ItemStack stack = rite.releaseEscrowedStaff(player.registryAccess());
		// The conjured projection hand was only standing in for the staff: hand the staff straight back.
		if (player.isAlive() && CellHandFormHelper.isCellHandForm(player.getMainHandItem())) {
			if (player.isUsingItem()) player.stopUsingItem();
			player.setItemInHand(InteractionHand.MAIN_HAND, stack);
			return;
		}
		if (!player.getInventory().add(stack)) {
			ItemEntity dropped = player.drop(stack, false);
			if (dropped != null) dropped.setNoPickUpDelay();
		}
	}
}
