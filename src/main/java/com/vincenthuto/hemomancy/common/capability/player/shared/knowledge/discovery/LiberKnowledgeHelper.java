package com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hutoslib.common.book.knowledge.BookEntryRegistry;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgress;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents;
import com.vincenthuto.hutoslib.common.book.knowledge.IDiscoverySource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.LinkedHashSet;
import java.util.Set;

public final class LiberKnowledgeHelper {
	private LiberKnowledgeHelper() {
	}

	public static boolean unlockEntry(ServerPlayer player, ResourceLocation entryId, IDiscoverySource source) {
		var knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
		boolean changed = knowledge.unlockEntry(entryId, source);
		if (changed) {
			LiberKnowledgeEvents.sync(player, entryId != null ? Set.of(entryId) : Set.of());
		}
		return changed;
	}

	public static boolean unlockEntries(ServerPlayer player, Iterable<ResourceLocation> entryIds, IDiscoverySource source) {
		var knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
		boolean changed = false;
		Set<ResourceLocation> markUnreadEntries = new LinkedHashSet<>();
		for (ResourceLocation entryId : entryIds) {
			if (knowledge.unlockEntry(entryId, source)) {
				changed = true;
				markUnreadEntries.add(entryId);
			}
		}
		if (changed) {
			LiberKnowledgeEvents.sync(player, markUnreadEntries);
		}
		return changed;
	}

	public static boolean unlockForRite(ServerPlayer player, String ritePath) {
		return unlockEntries(player, LiberEntryDefinitions.forRite(ritePath), HemomancyDiscoverySource.RITE);
	}

	public static boolean unlockForDegree(ServerPlayer player, int degree) {
		boolean changed = false;
		if (degree >= 1) {
			changed |= unlockEntry(player, LiberEntryDefinitions.HEMOMANCY, HemomancyDiscoverySource.DEGREE);
			changed |= unlockEntry(player, LiberEntryDefinitions.THE_HARBINGERS, HemomancyDiscoverySource.DEGREE);
		}
		if (degree >= 2) changed |= unlockEntry(player, LiberEntryDefinitions.DEGREES, HemomancyDiscoverySource.DEGREE);
		if (degree >= 3) changed |= unlockEntry(player, LiberEntryDefinitions.ORDER_BELIEFS, HemomancyDiscoverySource.DEGREE);
		if (degree >= 4) changed |= unlockEntry(player, LiberEntryDefinitions.HISTORICAL_RECORD, HemomancyDiscoverySource.DEGREE);
		if (degree >= 5) changed |= unlockEntry(player, LiberEntryDefinitions.HERMITS, HemomancyDiscoverySource.DEGREE);
		if (degree >= 7) changed |= unlockEntry(player, LiberEntryDefinitions.ENTITY, HemomancyDiscoverySource.DEGREE);
		if (degree >= 8) changed |= unlockEntry(player, LiberEntryDefinitions.TRUTH, HemomancyDiscoverySource.DEGREE);
		// The core loop's ground floor (core-loop Track 11): what every Harbinger does from the first Degree, then
		// what the second and third Degrees open. The rest unlock by the item or the first that teaches them.
		if (degree >= 1) {
			for (ResourceLocation page : java.util.List.of(LiberEntryDefinitions.THE_TWO_VERBS, LiberEntryDefinitions.LIVING_BLOOD,
					LiberEntryDefinitions.THE_BODY_AND_THE_DAY, LiberEntryDefinitions.THE_DRAW_AND_THE_MARK,
					LiberEntryDefinitions.THE_GROUND_NETWORK, LiberEntryDefinitions.FED_GROUND, LiberEntryDefinitions.CHUM)) {
				changed |= unlockEntry(player, page, HemomancyDiscoverySource.DEGREE);
			}
		}
		if (degree >= 2) {
			changed |= unlockEntry(player, LiberEntryDefinitions.TEMPER, HemomancyDiscoverySource.DEGREE);
			changed |= unlockEntry(player, LiberEntryDefinitions.UNREAD_HUMOURS, HemomancyDiscoverySource.DEGREE);
			changed |= unlockEntry(player, LiberEntryDefinitions.THE_WORLD_ANSWERS, HemomancyDiscoverySource.DEGREE);
		}
		if (degree >= 3) {
			changed |= unlockEntry(player, LiberEntryDefinitions.MENAGERIE, HemomancyDiscoverySource.DEGREE);
			changed |= unlockEntry(player, LiberEntryDefinitions.THE_BROOD, HemomancyDiscoverySource.DEGREE);
			changed |= unlockEntry(player, LiberEntryDefinitions.STRAIN_BEDS, HemomancyDiscoverySource.DEGREE);
			changed |= unlockEntry(player, LiberEntryDefinitions.LIVING_VEINS_AND_BASINS, HemomancyDiscoverySource.DEGREE);
		}
		// The older pages (CQ-1, round two P9): each opens at the Degree its system does, and earlier by what teaches it.
		changed |= unlockOlderForDegree(player, degree);
		// DS-9: the Armature from the second Degree, when the Artificer lets a Harbinger use it, and each craft at its own.
		java.util.List<ResourceLocation> stations = new java.util.ArrayList<>();
		if (degree >= 2) stations.add(LiberEntryDefinitions.ARMATURE_RACK);
		for (LiberEntryDefinitions.CraftPage craft : LiberEntryDefinitions.CRAFT_PAGES) {
			if (degree >= craft.degree()) stations.add(craft.page());
		}
		if (!stations.isEmpty()) changed |= unlockEntries(player, stations, HemomancyDiscoverySource.DEGREE);
		return changed;
	}

	/** The older Liber Sanguinium pages' Degree floors. */
	private static boolean unlockOlderForDegree(ServerPlayer player, int degree) {
		java.util.List<ResourceLocation> open = new java.util.ArrayList<>();
		if (degree >= 1) {
			open.addAll(java.util.List.of(LiberEntryDefinitions.MANIPULATIONS_OVERVIEW, LiberEntryDefinitions.TEMPERAMENT,
					LiberEntryDefinitions.SANGUINE_CONDENSER));
		}
		if (degree >= 2) {
			open.addAll(java.util.List.of(LiberEntryDefinitions.ANIMUS_MANIPULATIONS, LiberEntryDefinitions.FLAMMEUS_MANIPULATIONS,
					LiberEntryDefinitions.DUCTILIS_MANIPULATIONS, LiberEntryDefinitions.FERRIC_MANIPULATIONS,
					LiberEntryDefinitions.LUX_MANIPULATIONS, LiberEntryDefinitions.MORTEM_MANIPULATIONS,
					LiberEntryDefinitions.CONGEATIO_MANIPULATIONS, LiberEntryDefinitions.TENEBRIS_MANIPULATIONS,
					LiberEntryDefinitions.ANIMUS_TEMPERAMENT, LiberEntryDefinitions.FLAMMEUS_TEMPERAMENT,
					LiberEntryDefinitions.DUCTILIS_TEMPERAMENT, LiberEntryDefinitions.FERRIC_TEMPERAMENT,
					LiberEntryDefinitions.LUX_TEMPERAMENT, LiberEntryDefinitions.MORTEM_TEMPERAMENT,
					LiberEntryDefinitions.CONGEATIO_TEMPERAMENT, LiberEntryDefinitions.TENEBRIS_TEMPERAMENT,
					LiberEntryDefinitions.THELEMIC_MEMORIES, LiberEntryDefinitions.MORPHLING_CULTIVATION,
					LiberEntryDefinitions.MORPHLING_STRAINS, LiberEntryDefinitions.SHARED_MOB_MATERIALS));
		}
		if (degree >= 3) {
			open.addAll(java.util.List.of(LiberEntryDefinitions.MEMORY_WEAVING, LiberEntryDefinitions.FORGE_RECORDING,
					LiberEntryDefinitions.FORGE_MAINTENANCE, LiberEntryDefinitions.BLOODWORK_STATIONS));
		}
		if (degree >= 5) open.addAll(java.util.List.of(LiberEntryDefinitions.MORPHLING_CRADLE_AND_NECTAR,
				LiberEntryDefinitions.PHLEGETHOSOMA));
		if (degree >= 6) open.add(LiberEntryDefinitions.ANNETTA_KNOWLES);
		if (degree >= 7) open.add(LiberEntryDefinitions.SAINT_MEMORIES);
		return !open.isEmpty() && unlockEntries(player, open, HemomancyDiscoverySource.DEGREE);
	}

	/**
	 * A memory just learned opens the manipulation overview and its school's pages in both chapters (round two P9), so a
	 * Harbinger reads about a school when they first take one of its memories, ahead of the second Degree.
	 */
	public static boolean unlockForMemory(ServerPlayer player, BloodManipulation manipulation) {
		if (manipulation == null || manipulation == BloodManipulation.BLANK || manipulation.getTend() == null) return false;
		java.util.List<ResourceLocation> open = new java.util.ArrayList<>(java.util.List.of(LiberEntryDefinitions.MANIPULATIONS_OVERVIEW,
				LiberEntryDefinitions.manipulationsPage(manipulation.getTend()),
				LiberEntryDefinitions.temperamentPage(manipulation.getTend())));
		// ... and whatever its memory item opens when picked up (its Sculpting's own page), which a learned memory may
		// never have been as a dropped item.
		open.addAll(BookEntryRegistry.entriesForItem(Hemomancy.rloc("memory_" + manipulation.getName())));
		return unlockEntries(player, open, HemomancyDiscoverySource.PRACTICE);
	}

	/**
	 * Liber Immaculatus's floor, the Unstained counterpart of a Degree (round two P9): each older page opens at the
	 * purity or clarity stage it describes. Called from the Unstained milestone check, only once purification has begun.
	 */
	public static boolean unlockForUnstained(ServerPlayer player, UnstainedProgress progress) {
		if (!progress.hasBegunPurification()) return false;
		java.util.List<ResourceLocation> open = new java.util.ArrayList<>(java.util.List.of(
				LiberEntryDefinitions.IMMACULATUS_STAGES_OF_PURITY, LiberEntryDefinitions.IMMACULATUS_LIVING_THE_LIFE,
				LiberEntryDefinitions.IMMACULATUS_GHOST_PIPE));
		int purity = EnumPurityStage.byPurity(progress.getPurity()).getLevel();
		// Clarity is reached only through full purity, so a player on the clarity stages has passed every purity stage.
		boolean purified = progress.isPurified() || progress.hasClarityUnlocked();
		if (purified || purity >= EnumPurityStage.TAINTED.getLevel()) {
			open.add(LiberEntryDefinitions.IMMACULATUS_WHAT_PURITY_GRANTS);
			open.add(LiberEntryDefinitions.IMMACULATUS_VIRID_SALIS);
		}
		if (purified || purity >= EnumPurityStage.CLEANSING.getLevel()) {
			open.add(LiberEntryDefinitions.IMMACULATUS_LETHEAN_GIFTS);
			open.add(LiberEntryDefinitions.IMMACULATUS_WHITE_HUMOR);
		}
		if (purified) open.add(LiberEntryDefinitions.IMMACULATUS_STAGES_OF_CLARITY);
		if (progress.hasClarityUnlocked()) {
			open.add(LiberEntryDefinitions.IMMACULATUS_STILL_ARTS);
			if (EnumClarityStage.byClarity(progress.getClarity()).getLevel() >= EnumClarityStage.RESOLUTE.getLevel()) {
				open.add(LiberEntryDefinitions.IMMACULATUS_PALE_INTERCESSION);
			}
		}
		return unlockEntries(player, open, HemomancyDiscoverySource.PRACTICE);
	}

	/** DS-9: a boss's page opens for the player its fight begins against (Annetta's keeps its own source). */
	public static boolean unlockForEncounter(ServerPlayer player, ResourceLocation entryId) {
		return unlockEntry(player, entryId, HemomancyDiscoverySource.ENCOUNTER);
	}

	public static boolean handleDialogueEvent(ServerPlayer player, String eventId) {
		return unlockEntries(player, LiberEntryDefinitions.forDialogueEvent(eventId), HemomancyDiscoverySource.DIALOGUE);
	}

	public static boolean unlockMemo(ServerPlayer player, ResourceLocation memoId, ResourceLocation entryId) {
		var knowledge = HemoCapabilityAccess.getLiberKnowledge(player);
		boolean changed = knowledge.unlockMemo(memoId, entryId);
		if (changed) {
			LiberKnowledgeEvents.sync(player, entryId != null ? Set.of(entryId) : Set.of());
		}
		return changed;
	}

	public static boolean hasEntry(Player player, ResourceLocation entryId) {
		return HemoCapabilityAccess.getLiberKnowledge(player).hasEntry(entryId);
	}

	public static boolean knowsMemo(Player player, ResourceLocation memoId) {
		return HemoCapabilityAccess.getLiberKnowledge(player).knowsMemo(memoId);
	}
}
