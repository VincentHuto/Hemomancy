package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates;
import com.vincenthuto.hemomancy.common.manipulation.family.ManipulationFamilyRegistry;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.KnownManipulationServerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.LinkedHashMap;
import java.util.List;

public final class KnownManipulationGrantHelper {
	private KnownManipulationGrantHelper() {
	}

	public enum MemoryGrantStatus {
		GRANTED_EQUIPPED,
		GRANTED,
		ALREADY_KNOWN,
		NO_ACTIVE_BLOOD,
		RANK_TOO_LOW,
		RETIRED,
		INVALID
	}

	public record MemoryGrantResult(MemoryGrantStatus status, BloodManipulation manipulation, int requiredDegree) {
		public boolean success() {
			return status == MemoryGrantStatus.GRANTED_EQUIPPED || status == MemoryGrantStatus.GRANTED;
		}
	}

	public static MemoryGrantResult checkMemoryGrant(ServerPlayer player, BloodManipulation manipulation) {
		return checkMemoryGrant(player, manipulation, null);
	}

	public static MemoryGrantResult checkMemoryGrant(ServerPlayer player, BloodManipulation manipulation,
			Item memoryItem) {
		if (player == null || manipulation == null || manipulation == BloodManipulation.BLANK) {
			return new MemoryGrantResult(MemoryGrantStatus.INVALID, manipulation, 0);
		}
		if (HemoCapabilityAccess.getUnstainedProgress(player)
				.map(UnstainedAccessRules::blocksHarbingerProgress).orElse(false)) {
			return new MemoryGrantResult(MemoryGrantStatus.NO_ACTIVE_BLOOD, manipulation, 0);
		}
		if (ManipulationRetirementRules.isRetiredManipulation(manipulation)
				|| (memoryItem != null && ManipulationRetirementRules.isRetiredMemoryItem(memoryItem, manipulation))) {
			return new MemoryGrantResult(MemoryGrantStatus.RETIRED, manipulation, 0);
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive()) {
			return new MemoryGrantResult(MemoryGrantStatus.NO_ACTIVE_BLOOD, manipulation, 0);
		}
		int requiredDegree = ManipulationRankGates.minDegreeForRank(manipulation.getRank());
		if (!ManipulationRankGates.playerMeetsRank(HemoCapabilityAccess.getPlayerDegreeNumber(player),
				manipulation.getRank())) {
			return new MemoryGrantResult(MemoryGrantStatus.RANK_TOO_LOW, manipulation, requiredDegree);
		}
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null) {
			return new MemoryGrantResult(MemoryGrantStatus.INVALID, manipulation, requiredDegree);
		}
		if (known.doesListContainName(known.getKnownManips(), manipulation)) {
			return new MemoryGrantResult(MemoryGrantStatus.ALREADY_KNOWN, manipulation, requiredDegree);
		}
		return new MemoryGrantResult(MemoryGrantStatus.GRANTED, manipulation, requiredDegree);
	}

	public static MemoryGrantResult grantMemory(ServerPlayer player, BloodManipulation manipulation) {
		return grantMemory(player, manipulation, null);
	}

	public static MemoryGrantResult grantMemory(ServerPlayer player, BloodManipulation manipulation, Item memoryItem) {
		MemoryGrantResult checked = checkMemoryGrant(player, manipulation, memoryItem);
		if (!checked.success() || player == null) {
			return checked;
		}
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player).orElse(null);
		if (known == null) {
			return new MemoryGrantResult(MemoryGrantStatus.INVALID, manipulation, checked.requiredDegree());
		}
		known.getKnownManips().put(manipulation, new ManipLevel(0, 0));
		ManipulationFamilyRegistry.unlockEligibleForms(known.getKnownManips());
		boolean equipped = ManipulationEquipHelper.equipNameIfPossible(known.getEquippedManipNames(),
				manipulation.getName(), ManipSlotHelper.getMaxSlots(player));
		MnemonicReliquaryProgression.onCapacityChanged(player, known);
		PacketHandler.sendToPlayer(player, new KnownManipulationServerPacket(known));
		return new MemoryGrantResult(equipped ? MemoryGrantStatus.GRANTED_EQUIPPED : MemoryGrantStatus.GRANTED,
				manipulation, checked.requiredDegree());
	}

	public static boolean learnAndEquipIfPossible(IKnownManipulations known, BloodManipulation manipulation,
			int maxSlots) {
		if (known == null || manipulation == null || manipulation == BloodManipulation.BLANK
				|| ManipulationRetirementRules.isRetiredManipulation(manipulation)) return false;
		return learnAndEquipIfPossible(known.getKnownManips(), known.getEquippedManipNames(), manipulation, maxSlots);
	}

	public static boolean learnAndEquipIfPossible(LinkedHashMap<BloodManipulation, ManipLevel> knownManips,
			List<String> equippedNames, BloodManipulation manipulation, int maxSlots) {
		if (knownManips == null || equippedNames == null || manipulation == null
				|| manipulation == BloodManipulation.BLANK
				|| ManipulationRetirementRules.isRetiredManipulation(manipulation)) {
			return false;
		}

		boolean changed = false;
		if (!containsManipName(knownManips, manipulation)) {
			knownManips.put(manipulation, new ManipLevel(0, 0));
			ManipulationFamilyRegistry.normalizeKnown(knownManips);
			changed = true;
		}

		if (ManipulationEquipHelper.equipNameIfPossible(equippedNames, manipulation.getName(), maxSlots)) {
			changed = true;
		}
		return changed;
	}

	public static boolean grantDegreeOneUtilities(ServerPlayer player) {
		if (player == null || HemoCapabilityAccess.getPlayerDegreeNumber(player) < 1) return false;
		if (HemoCapabilityAccess.getUnstainedProgress(player)
				.map(UnstainedAccessRules::blocksHarbingerProgress).orElse(false)) return false;
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
