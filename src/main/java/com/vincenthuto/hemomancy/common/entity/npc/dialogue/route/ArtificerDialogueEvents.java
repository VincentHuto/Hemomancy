package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.armor.ArmorSetHelper;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.D7Lineage;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Artificer's dialogue events: briefs, inspections, rewards, fittings, the consecration kit and the legacy
 * branch recovery. Two correspondence claims are spoken by the Alchemist.
 */
public final class ArtificerDialogueEvents {
	private ArtificerDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		if (!event.getEventId().startsWith("artificer_")) return false;
		// Every artificer_ choice must come from the Artificer, or from the Alchemist for its two correspondence claims.
		if (!isValidArtificerDialogueSource(event)) return true;
		if (handleArtificerLegacyBranchChoice(player, event.getEventId())) return true;
		switch (event.getEventId()) {
		case HarbingerArtificerDialogueTrees.EVENT_CONSECRATION_KIT ->
				event.setRewardDelivered(handleArtificerConsecrationKit(player, event.getEntityId()));
		case HarbingerArtificerDialogueTrees.EVENT_BRIEF_WORN_VOW ->
				handleArtificerBrief(player, 2, ArtificerAssignments.WORN_VOW_BRIEFED, false);
		case HarbingerArtificerDialogueTrees.EVENT_BRIEF_THREE_ANSWERS ->
				handleArtificerBrief(player, 3, ArtificerAssignments.THREE_ANSWERS_BRIEFED, false);
		case HarbingerArtificerDialogueTrees.EVENT_BRIEF_CRIMSON_VESTMENT ->
				handleArtificerBrief(player, 5, ArtificerAssignments.CRIMSON_VESTMENT_BRIEFED, false);
		case HarbingerArtificerDialogueTrees.EVENT_BRIEF_ASSUMED_LIMB ->
				handleArtificerBrief(player, 5, ArtificerAssignments.ASSUMED_LIMB_BRIEFED, true);
		case HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME ->
				handleArtificerBrief(player, 7, ArtificerAssignments.WEIGHT_OF_FRAME_BRIEFED, false);
		case HarbingerArtificerDialogueTrees.EVENT_INSPECT_THREE_ANSWERS ->
				handleArtificerInspection(player, ArtificerAssignments.inspectThreeAnswers(player),
					"hemomancy.dialogue.event.artificer_three_answers_inspected",
					ArtificerAssignments.hasFullForkFamily(player,
							ArtificerAssignments.firstForkFamily(player)));
		case HarbingerArtificerDialogueTrees.EVENT_INSPECT_CRIMSON_VESTMENT ->
				handleArtificerInspection(player, ArtificerAssignments.inspectCrimsonVestment(player),
					"hemomancy.dialogue.event.artificer_crimson_vestment_inspected",
					ArmorSetHelper.hasFullBloodLustSet(player));
		case HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME ->
				handleArtificerD7Inspection(player, event.getEntityId());
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_REWARD ->
				handleArtificerMaterial(player, event.getEntityId(),
					ArtificerAssignments.claimD7Material(player),
					"hemomancy.dialogue.event.artificer_d7_reward_granted");
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_WORN_VOW_REWARD -> {
			handleArtificerMaterial(player, event.getEntityId(),
					ArtificerAssignments.claimWornVowInspection(player),
					"hemomancy.dialogue.event.artificer_worn_vow_reward_granted");
		}
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD -> {
			handleArtificerMaterial(player, event.getEntityId(),
					ArtificerAssignments.counselThreeAnswers(player),
					"hemomancy.dialogue.event.artificer_three_answers_reward_granted");
		}
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD -> {
			handleArtificerMaterial(player, event.getEntityId(),
					ArtificerAssignments.counselCrimsonVestment(player),
					"hemomancy.dialogue.event.artificer_crimson_vestment_reward_granted");
		}
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_ASSUMED_LIMB_REWARD -> {
			handleArtificerMaterial(player, event.getEntityId(),
					ArtificerAssignments.claimAssumedLimbInspection(player),
					"hemomancy.dialogue.event.artificer_assumed_limb_reward_granted");
		}
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_HEMATIC_IRON_FITTING -> {
			handleArtificerFittingClaim(player, event.getEntityId(),
					claimant -> ArtificerAssignments.earnedHematicIronFitting(claimant),
					claimant -> ArtificerAssignments.tryGrantHematicIronFitting(claimant),
					"hemomancy.dialogue.event.artificer_hematic_iron_fitting_unready",
					"hemomancy.dialogue.event.artificer_hematic_iron_fitting_known",
					"hemomancy.dialogue.event.artificer_hematic_iron_fitting_granted",
					"hemomancy.dialogue.event.artificer_hematic_iron_fitting_reissued");
		}
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_FORK_FITTING -> {
			handleArtificerFittingClaim(player, event.getEntityId(),
					claimant -> ArtificerAssignments.earnedForkFitting(claimant),
					claimant -> ArtificerAssignments.tryGrantForkFitting(claimant),
					"hemomancy.dialogue.event.artificer_fork_fitting_unready",
					"hemomancy.dialogue.event.artificer_fork_fitting_known",
					"hemomancy.dialogue.event.artificer_fork_fitting_granted",
					"hemomancy.dialogue.event.artificer_fork_fitting_reissued");
		}
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_BLOOD_LUST_FITTING -> {
			handleArtificerFittingClaim(player, event.getEntityId(),
					claimant -> ArtificerAssignments.earnedBloodLustFitting(claimant),
					claimant -> ArtificerAssignments.tryGrantBloodLustFitting(claimant),
					"hemomancy.dialogue.event.artificer_blood_lust_fitting_unready",
					"hemomancy.dialogue.event.artificer_blood_lust_fitting_known",
					"hemomancy.dialogue.event.artificer_blood_lust_fitting_granted",
					"hemomancy.dialogue.event.artificer_blood_lust_fitting_reissued");
		}
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING -> {
			handleArtificerFittingClaim(player, event.getEntityId(),
					claimant -> ArtificerAssignments.earnedD7Fitting(claimant),
					claimant -> ArtificerAssignments.tryGrantD7Fitting(claimant),
					"hemomancy.dialogue.event.artificer_d7_fitting_unready",
					"hemomancy.dialogue.event.artificer_d7_fitting_known",
					"hemomancy.dialogue.event.artificer_d7_fitting_granted",
					"hemomancy.dialogue.event.artificer_d7_fitting_reissued");
		}
		case HarbingerArtificerDialogueTrees.EVENT_CLAIM_LIVING_ARSENAL_FITTING -> {
			handleArtificerFittingClaim(player, event.getEntityId(),
					claimant -> ArtificerAssignments.earnedLivingArsenalFitting(claimant),
					claimant -> ArtificerAssignments.tryGrantLivingArsenalFitting(claimant),
					"hemomancy.dialogue.event.artificer_living_arsenal_fitting_unready",
					"hemomancy.dialogue.event.artificer_living_arsenal_fitting_known",
					"hemomancy.dialogue.event.artificer_living_arsenal_fitting_granted",
					"hemomancy.dialogue.event.artificer_living_arsenal_fitting_reissued");
		}
		default -> {
			return false;
		}
		}
		return true;
	}

	public static boolean hasClaimedConsecrationKit(ServerPlayer player) {
		return HemoCapabilityAccess.stationUpgrades(player).hasClaimed(UpgradeStation.ARMATURE, 1);
	}

	private static boolean handleArtificerConsecrationKit(ServerPlayer player, int entityId) {
		Entity teacher = player.level().getEntity(entityId);
		if (!(teacher instanceof HarbingerArtificerEntity) || player.distanceToSqr(teacher) > 64) return false;
		return claimArmatureTier(player, 1, "hemomancy.dialogue.event.artificer_consecration_kit");
	}

	/** The Armature keeps its own Artificer and Monolith scenes; both claim through the shared station progress. */
	static boolean claimArmatureTier(ServerPlayer player, int tierNumber, String messagePrefix) {
		var tier = StationUpgradeCatalog.get(UpgradeStation.ARMATURE, tierNumber);
		var progress = HemoCapabilityAccess.stationUpgrades(player);
		var state = progress.claimState(player, tier);
		if (state != StationUpgradeRules.ClaimState.READY) {
			player.displayClientMessage(Component.translatable(state == StationUpgradeRules.ClaimState.CLAIMED
					? messagePrefix + "_known" : messagePrefix + "_unready").withStyle(ChatFormatting.GRAY), false);
			return false;
		}
		progress.claim(player, tier);
		player.displayClientMessage(Component.translatable(messagePrefix + "_granted")
				.withStyle(ChatFormatting.DARK_RED), false);
		return true;
	}

	private static void handleArtificerBrief(ServerPlayer player, int minimumDegree, ResourceLocation briefing,
			boolean requiresLivingStaff) {
		ArtificerProgressSnapshot progress = ArtificerProgressSnapshot.from(player);
		if (!progress.activeBlood() || progress.purifying() || progress.clarity()
				|| progress.degree() < minimumDegree || requiresLivingStaff && !progress.livingStaffBond()) return;
		ArtificerAssignments.brief(player, briefing);
	}

	private static void handleArtificerInspection(ServerPlayer player, boolean inspected, String messageKey,
			boolean fullSetEquipped) {
		if (!inspected) {
			player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.artificer_inspection_unready")
					.withStyle(ChatFormatting.GRAY), false);
			return;
		}
		ArtificerProgressSnapshot progress = ArtificerProgressSnapshot.from(player);
		player.displayClientMessage(Component.translatable(messageKey,
				progress.forkFamily().serializedName(), progress.d7Lineage().serializedName(),
				Component.translatable(fullSetEquipped
						? "hemomancy.dialogue.event.artificer_equipment_full"
						: "hemomancy.dialogue.event.artificer_equipment_incomplete"))
				.withStyle(ChatFormatting.DARK_RED), false);
	}

	private static void handleArtificerD7Inspection(ServerPlayer player, int entityId) {
		ItemStack reward = ArtificerAssignments.inspectWeightOfFrame(player);
		if (reward.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.artificer_inspection_unready")
					.withStyle(ChatFormatting.GRAY), false);
			return;
		}
		DialogueRewards.giveOrDropAtEntity(player, entityId, reward);
		ArtificerProgressSnapshot progress = ArtificerProgressSnapshot.from(player);
		player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.artificer_weight_of_frame_inspected",
				progress.forkFamily().serializedName(), progress.d7Lineage().serializedName(),
				Component.translatable(ArtificerAssignments.hasFullD7Lineage(player, progress.d7Lineage())
						? "hemomancy.dialogue.event.artificer_equipment_full"
						: "hemomancy.dialogue.event.artificer_equipment_incomplete"))
				.withStyle(ChatFormatting.DARK_RED), false);
	}

	static void handleArtificerMaterial(ServerPlayer player, int entityId, ItemStack reward, String messageKey) {
		if (reward.isEmpty()) {
			player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.artificer_reward_unready")
					.withStyle(ChatFormatting.GRAY), false);
			return;
		}
		DialogueRewards.giveOrDropAtEntity(player, entityId, reward);
		player.displayClientMessage(Component.translatable(messageKey).withStyle(ChatFormatting.DARK_RED), false);
	}

	private static boolean handleArtificerLegacyBranchChoice(ServerPlayer player, String eventId) {
		if (eventId.startsWith(HarbingerArtificerDialogueTrees.EVENT_RECOVER_FORK_PREFIX)) {
			if (!ArtificerProgressSnapshot.from(player).needsForkRecovery()) return true;
			ForkFamily family = ForkFamily.fromSerializedName(
					eventId.substring(HarbingerArtificerDialogueTrees.EVENT_RECOVER_FORK_PREFIX.length()));
			ArtificerAssignments.recordForkFamily(player, family);
			return true;
		}
		if (eventId.startsWith(HarbingerArtificerDialogueTrees.EVENT_RECOVER_D7_PREFIX)) {
			if (!ArtificerProgressSnapshot.from(player).needsD7Recovery()) return true;
			D7Lineage lineage = D7Lineage.fromSerializedName(
					eventId.substring(HarbingerArtificerDialogueTrees.EVENT_RECOVER_D7_PREFIX.length()));
			ArtificerAssignments.recordD7Lineage(player, lineage);
			return true;
		}
		return false;
	}

	private static boolean isValidArtificerDialogueSource(DialogueEvent event) {
		Entity entity = event.getPlayer().level().getEntity(event.getEntityId());
		boolean correspondence = event.getEventId().equals(HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD)
				|| event.getEventId().equals(HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD);
		return correspondence ? entity instanceof HarbingerAlchemistEntity : entity instanceof HarbingerArtificerEntity;
	}

	private static void handleArtificerFittingClaim(ServerPlayer player, int entityId,
			ArtificerFittingStackProvider earned, ArtificerFittingStackProvider grant,
			String unreadyKey, String knownKey, String grantedKey, String reissuedKey) {
		ItemStack fitting = earned.stackFor(player);
		if (!fitting.isEmpty()) {
			if (!playerHasFitting(player, fitting)) {
				DialogueRewards.giveOrDropAtEntity(player, entityId, fitting.copy());
				player.displayClientMessage(
						Component.translatable(reissuedKey).withStyle(ChatFormatting.DARK_RED),
						false);
				return;
			}
			player.displayClientMessage(
					Component.translatable(knownKey).withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		fitting = grant.stackFor(player);
		if (fitting.isEmpty()) {
			player.displayClientMessage(
					Component.translatable(unreadyKey).withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		DialogueRewards.giveOrDropAtEntity(player, entityId, fitting.copy());
		player.displayClientMessage(
				Component.translatable(grantedKey).withStyle(ChatFormatting.DARK_RED),
				false);
	}

	private static boolean playerHasFitting(ServerPlayer player, ItemStack fitting) {
		if (fitting.isEmpty()) {
			return false;
		}
		for (ItemStack stack : player.getInventory().items) {
			if (stack.is(fitting.getItem())) {
				return true;
			}
		}
		for (ItemStack stack : player.getInventory().armor) {
			if (stack.is(fitting.getItem())) {
				return true;
			}
		}
		for (ItemStack stack : player.getInventory().offhand) {
			if (stack.is(fitting.getItem())) {
				return true;
			}
		}
		var equipment = HemoCapabilityAccess.getEquipment(player);
		for (int slot = 0; slot < equipment.getSlots(); slot++) {
			if (equipment.getStackInSlot(slot).is(fitting.getItem())) {
				return true;
			}
		}
		return false;
	}

	@FunctionalInterface
	private interface ArtificerFittingStackProvider {
		ItemStack stackFor(ServerPlayer player);
	}
}
