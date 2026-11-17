package com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.LivingStaffProgressServerPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class LivingStaffBondHelper {
	private LivingStaffBondHelper() {
	}

	public static boolean unlockFromBloodStructureCraft(ServerPlayer player, ItemStack result) {
		if (player == null || result == null || !result.is(ItemInit.living_staff.get())) {
			return false;
		}
		boolean changed = false;
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		if (progress != null && progress.unlockLivingStaffBond()) {
			changed = true;
			player.displayClientMessage(Component.translatable("hemomancy.living_staff.bond_unlocked")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			syncProgress(player);
		}

		if (ManipulationInit.conjure_staff.isBound()) {
			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
			if (known != null && KnownManipulationGrantHelper.learnAndEquipIfPossible(known,
					ManipulationInit.conjure_staff.get(), ManipSlotHelper.getMaxSlots(player))) {
				changed = true;
				KnownManipulationEvents.syncPlayerEvent(player);
			}
		}
		// The pickaxe form comes with the bond, so a new Harbinger mines iron with the Staff on the first day (Track 8).
		changed |= ensureConjurePickaxeKnown(player);
		return changed;
	}

	/**
	 * Teaches a bonded player the Staff's pickaxe form if they do not know it, equipping it when a slot is free. A
	 * player who already knows it is left alone, so unequipping it sticks across logins.
	 */
	public static boolean ensureConjurePickaxeKnown(ServerPlayer player) {
		if (player == null || !ManipulationInit.conjure_pickaxe.isBound()) return false;
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		if (progress == null || !progress.hasLivingStaffBond()) return false;
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		if (known == null || known.doesListContainName(known.getKnownManips(), ManipulationInit.conjure_pickaxe.get())) {
			return false;
		}
		boolean changed = KnownManipulationGrantHelper.learnAndEquipIfPossible(known,
				ManipulationInit.conjure_pickaxe.get(), ManipSlotHelper.getMaxSlots(player));
		if (changed) KnownManipulationEvents.syncPlayerEvent(player);
		return changed;
	}

	public static boolean ensureConjureStaffKnown(ServerPlayer player) {
		if (player == null || !ManipulationInit.conjure_staff.isBound()) {
			return false;
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		if (progress == null || !progress.hasLivingStaffBond()) {
			return false;
		}
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		if (known == null) {
			return false;
		}
		if (!KnownManipulationGrantHelper.learnAndEquipIfPossible(known,
				ManipulationInit.conjure_staff.get(), ManipSlotHelper.getMaxSlots(player))) {
			return false;
		}
		KnownManipulationEvents.syncPlayerEvent(player);
		return true;
	}

	public static boolean ensureVesperSickleKnown(ServerPlayer player) {
		if (player == null || !ManipulationInit.conjure_sickle.isBound()) return false;
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player);
		if (progress == null || !progress.hasLivingStaffBond() || !progress.isVesperMemoryAwakened()) return false;
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		if (known == null) return false;
		if (known.doesListContainName(known.getKnownManips(), ManipulationInit.conjure_sickle.get())) return true;
		boolean changed = KnownManipulationGrantHelper.learnAndEquipIfPossible(known,
				ManipulationInit.conjure_sickle.get(), ManipSlotHelper.getMaxSlots(player));
		boolean learned = known.doesListContainName(known.getKnownManips(), ManipulationInit.conjure_sickle.get());
		if (changed) KnownManipulationEvents.syncPlayerEvent(player);
		return learned;
	}

	public static void syncProgress(ServerPlayer player) {
		if (player == null) {
			return;
		}
		PacketHandler.sendToPlayer(player, new LivingStaffProgressServerPacket(HemoCapabilityAccess.getLivingStaffProgress(player)));
	}
}
