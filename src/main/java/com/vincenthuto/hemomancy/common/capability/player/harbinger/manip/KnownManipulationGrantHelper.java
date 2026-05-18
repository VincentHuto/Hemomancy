package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.manips.KnownManipulationServerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.LinkedHashMap;
import java.util.List;

public final class KnownManipulationGrantHelper {
	private KnownManipulationGrantHelper() {
	}

	public static boolean learnAndEquipIfPossible(IKnownManipulations known, BloodManipulation manipulation,
			int maxSlots) {
		if (known == null || manipulation == null || manipulation == BloodManipulation.BLANK) return false;
		return learnAndEquipIfPossible(known.getKnownManips(), known.getEquippedManipNames(), manipulation, maxSlots);
	}

	public static boolean learnAndEquipIfPossible(LinkedHashMap<BloodManipulation, ManipLevel> knownManips,
			List<String> equippedNames, BloodManipulation manipulation, int maxSlots) {
		if (knownManips == null || equippedNames == null || manipulation == null
				|| manipulation == BloodManipulation.BLANK) {
			return false;
		}

		boolean changed = false;
		if (!containsManipName(knownManips, manipulation)) {
			knownManips.put(manipulation, ManipLevel.BLANK);
			changed = true;
		}

		if (ManipulationEquipHelper.equipNameIfPossible(equippedNames, manipulation.getName(), maxSlots)) {
			changed = true;
		}
		return changed;
	}

	public static boolean grantDegreeOneUtilities(ServerPlayer player) {
		if (player == null || HemoCapabilityAccess.getPlayerDegreeNumber(player) < 1) return false;
		return HemoCapabilityAccess.getKnownManipulations(player)
				.map(known -> {
					int maxSlots = ManipSlotHelper.getMaxSlots(player);
					boolean changed = false;
					changed |= grant(known, ManipulationInit.blood_absorption, maxSlots);
					changed |= grant(known, ManipulationInit.blood_projection, maxSlots);
					if (changed) {
						PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
					}
					return changed;
				})
				.orElse(false);
	}

	private static boolean grant(IKnownManipulations known,
			DeferredHolder<BloodManipulation, BloodManipulation> holder, int maxSlots) {
		return holder != null && holder.isBound()
				&& learnAndEquipIfPossible(known, holder.get(), maxSlots);
	}

	private static boolean containsManipName(LinkedHashMap<BloodManipulation, ManipLevel> knownManips,
			BloodManipulation manipulation) {
		for (BloodManipulation current : knownManips.keySet()) {
			if (current != null && current.getName() != null && current.getName().equals(manipulation.getName())) {
				return true;
			}
		}
		return false;
	}
}
