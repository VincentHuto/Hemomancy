package com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipSlotHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationEquipHelper;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.LivingStaffProgressServerPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
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
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		if (progress != null && progress.unlockLivingStaffBond()) {
			changed = true;
			player.displayClientMessage(Component.translatable("hemomancy.living_staff.bond_unlocked")
					.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
			syncProgress(player);
		}

		if (ManipulationInit.conjure_staff.isBound()) {
			IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
			if (known != null && KnownManipulationGrantHelper.learnAndEquipIfPossible(known,
					ManipulationInit.conjure_staff.get(), ManipSlotHelper.getMaxSlots(player))) {
				changed = true;
				PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
			}
		}
		return changed;
	}

	public static boolean ensureConjureStaffKnown(ServerPlayer player) {
		if (player == null || !ManipulationInit.conjure_staff.isBound()) {
			return false;
		}
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		if (progress == null || !progress.hasLivingStaffBond()) {
			return false;
		}
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null) {
			return false;
		}
		if (!KnownManipulationGrantHelper.learnAndEquipIfPossible(known,
				ManipulationInit.conjure_staff.get(), ManipSlotHelper.getMaxSlots(player))) {
			return false;
		}
		PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
		return true;
	}

	public static boolean ensureVesperSickleKnown(ServerPlayer player) {
		if (player == null || !ManipulationInit.conjure_sickle.isBound()) return false;
		ILivingStaffProgress progress = HemoCapabilityAccess.getLivingStaffProgress(player).orElse(null);
		if (progress == null || !progress.hasLivingStaffBond() || !progress.isVesperMemoryAwakened()) return false;
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null) return false;
		boolean changed = KnownManipulationGrantHelper.learnAndEquipIfPossible(known,
				ManipulationInit.conjure_sickle.get(), ManipSlotHelper.getMaxSlots(player));
		boolean learned = known.doesListContainName(known.getKnownManips(), ManipulationInit.conjure_sickle.get())
				&& known.getEquippedManipNames().contains(ManipulationEquipHelper.CONJURE_SICKLE);
		if (changed) PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
		return learned;
	}

	public static void syncProgress(ServerPlayer player) {
		if (player == null) {
			return;
		}
		HemoCapabilityAccess.getLivingStaffProgress(player).ifPresent(progress ->
				PacketHandler.sendToPlayer(player, new LivingStaffProgressServerPacket(progress)));
	}
}
