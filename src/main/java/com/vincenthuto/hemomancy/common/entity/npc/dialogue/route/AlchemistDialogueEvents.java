package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryProgress;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.RedTaxonomyRewardRules;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.shared.MnemonicRecipeKnowledge;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Alchemist's dialogue events: the red taxonomy and bestiary, her commissions and lessons, First Separation,
 * the Body Answers brief and the armour research she pays for the Artificer.
 */
final class AlchemistDialogueEvents {
	private AlchemistDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		HarbingerAlchemistDialogueTrees.RedTaxonomySample redTaxonomySample =
				HarbingerAlchemistDialogueTrees.RedTaxonomySample.fromEventId(event.getEventId());
		if (redTaxonomySample != null) {
			handleAlchemistRedTaxonomy(player, redTaxonomySample);
			return true;
		}
		if (isAlchemistBestiaryEvent(event.getEventId())) {
			handleAlchemistBestiary(player, event.getEntityId(), event.getEventId());
			return true;
		}
		// The Artificer's armour research is told and paid by the Alchemist.
		if (event.getEventId().equals(HarbingerAlchemistDialogueTrees.EVENT_CLAIM_ARMOR_RESEARCH_REWARD)) {
			if (player.level().getEntity(event.getEntityId()) instanceof HarbingerAlchemistEntity) {
				ArtificerDialogueEvents.handleArtificerMaterial(player, event.getEntityId(),
						ArtificerAssignments.claimForkResearchReward(player),
						"hemomancy.dialogue.event.alchemist_armor_research_reward_granted");
			}
			return true;
		}
		switch (event.getEventId()) {
		case VagrantMindInquiryDialogue.BIOLOGY_REPORT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.reportBiology(
						player, player.level().getEntity(event.getEntityId())));
		case DeepDarkCommissionDialogue.REPORT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.alchemist.DeepDarkCommission.report(
						player, player.level().getEntity(event.getEntityId())));
		case DeepDarkCommissionDialogue.BEARING -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.alchemist.DeepDarkGuidance.tell(
						player, player.level().getEntity(event.getEntityId())));
		case PhlegethonticCommissionDialogue.REPORT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.alchemist.PhlegethonticCommission.report(
						player, player.level().getEntity(event.getEntityId())));
		case PhlegethonticCommissionDialogue.BEARING -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.alchemist.PhlegethonticBasinGuidance.tell(
						player, player.level().getEntity(event.getEntityId())));
		case MorphlingHandlingDialogue.INSPECT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.alchemist.MorphlingHandlingAssignment.inspect(
						player, player.level().getEntity(event.getEntityId())));
		case OverworldFungalSurveyDialogue.REPORT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.alchemist.OverworldFungalSurvey.report(
						player, player.level().getEntity(event.getEntityId())));
		case HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF -> {
			handleAlchemistFirstSeparationBrief(player);
		}
		case HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM -> {
			event.setRewardDelivered(handleAlchemistFirstSeparationReward(player, event.getEntityId()));
		}
		case HarbingerAlchemistDialogueTrees.EVENT_BODY_ANSWERS_BRIEF -> {
			handleAlchemistBodyAnswersBrief(player);
		}
		case FirstDrawsDialogue.ACCEPT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.alchemist.FirstDrawsAssignment.brief(
					player, player.level().getEntity(event.getEntityId())));
		case SpecimenJarLessonDialogue.TEACH -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.alchemist.SpecimenJarLesson.teach(
					player, player.level().getEntity(event.getEntityId())));
		case "alchemist_replace_concentrated_blood" -> event.setRewardDelivered(com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood.replace(player, player.level().getEntity(event.getEntityId())));
		default -> {
			return false;
		}
		}
		return true;
	}

	private static void handleAlchemistFirstSeparationBrief(ServerPlayer player) {
		if (!FirstSeparationAssignment.canBrief(player)) return;
		if (!FirstSeparationAssignment.markBriefed(player)) return;
		FirstSeparationAssignment.giveBriefingSupplies(player);
	}

	private static boolean handleAlchemistFirstSeparationReward(ServerPlayer player, int entityId) {
        if (!(player.level().getEntity(entityId) instanceof HarbingerAlchemistEntity alchemist)
                || !com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation.near(player, alchemist)) return false;
		if (!FirstSeparationAssignment.claimRewards(player)) return false;
		player.displayClientMessage(Component.translatable("hemomancy.alchemist.first_separation.next_distillation"), false);
		return true;
	}

	private static void handleAlchemistRedTaxonomy(ServerPlayer player,
			HarbingerAlchemistDialogueTrees.RedTaxonomySample sample) {
		if (!ProgressionGates.hasDegree(player, 2)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_red_taxonomy_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		ItemStack held = player.getMainHandItem();
		if (!sample.matches(held)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_red_taxonomy_missing_sample")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		if (HarbingerAdvancementGranter.hasAdvancement(player, sample.advancement())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_red_taxonomy_known",
									held.getHoverName())
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		boolean taxonomyWasComplete = HarbingerAdvancementGranter.isRedTaxonomyComplete(player);
		int countBefore = HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(player);
		if (!player.isCreative()) {
			held.shrink(1);
		}
		HarbingerAdvancementGranter.grantIfNotDone(player, sample.advancement());
		int count = HarbingerAdvancementGranter.getRedTaxonomySpecimenCount(player);
		if (RedTaxonomyRewardRules.grantsFirstFieldVial(countBefore, count > countBefore)) {
			DialogueRewards.giveOrDropAtEntity(player, player.getId(), new ItemStack(ItemInit.bloody_vial.get()));
			player.displayClientMessage(Component.translatable(
					"hemomancy.dialogue.event.alchemist_red_taxonomy_first_vial")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
		boolean taxonomyCompletedNow = count >= 4 && !taxonomyWasComplete;
		if (taxonomyCompletedNow) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_RED_TAXONOMY_COMPLETE);
			DialogueRewards.giveOrDropAtEntity(player, player.getId(), new ItemStack(BlockInit.specimen_jar.get(), 4));
			DialogueRewards.giveOrDropAtEntity(player, player.getId(), new ItemStack(ItemInit.bloody_vial.get(), 4));
		}

		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.alchemist_red_taxonomy_recorded",
								held.getHoverName(), count)
						.withStyle(ChatFormatting.DARK_RED),
				false);
		if (taxonomyCompletedNow) {
			player.displayClientMessage(Component.translatable(
					"hemomancy.dialogue.event.alchemist_red_taxonomy_reward")
					.withStyle(ChatFormatting.DARK_RED), false);
		}
	}

	private static boolean isAlchemistBestiaryEvent(String eventId) {
		return HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD.equals(eventId)
				|| HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER.equals(eventId)
				|| (eventId != null && eventId.startsWith(
						HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX));
	}

	private static void handleAlchemistBestiary(ServerPlayer player, int entityId, String eventId) {
		if (!ProgressionGates.hasDegree(player, 2)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		ItemStack held = player.getMainHandItem();
		if (!SpecimenJarData.hasSpecimen(held)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_missing_specimen")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		var specimen = SpecimenJarData.getSpecimen(held);
		ResourceLocation specimenId = SpecimenJarData.getSpecimenEntityId(specimen).orElse(null);
		Component specimenName = SpecimenJarData.getSpecimenName(specimen);
		if (!SpecimenBestiaryDefinitions.isResearchSpecimen(specimenId)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_not_research",
									specimenName)
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}

		var progress = HemoCapabilityAccess.getSpecimenBestiary(player);
		if (HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD.equals(eventId)) {
			boolean newSpecimen = progress.recordSpecimen(specimenId);
			int newLayers = 0;
			for (MorphlingPolypLayer layer : SpecimenJarData.getMorphlingLayers(specimen)) {
				if (progress.recordMorphlingLayer(layer)) {
					newLayers++;
				}
			}
			player.displayClientMessage(
					Component.translatable(newSpecimen || newLayers > 0
									? SpecimenBestiaryDefinitions.recordedDialogueKey(specimenId)
									: "hemomancy.dialogue.event.alchemist_bestiary_known",
							specimenName, progress.recordedSpecimenCount(),
							SpecimenBestiaryDefinitions.totalResearchSpecimens())
							.withStyle(newSpecimen || newLayers > 0 ? ChatFormatting.DARK_RED : ChatFormatting.GRAY),
					false);
			SpecimenBestiaryEvents.sync(player, progress);
			return;
		}

		MorphlingPolypLayer requestedLayer = morphlingLayerFromEvent(eventId);
		if (requestedLayer != null) {
			handleAlchemistPolypSurrender(player, entityId, held, specimen, specimenId, requestedLayer, progress);
			return;
		}

		progress.recordSpecimen(specimenId);
		progress.surrenderSpecimen(specimenId);
		ItemStack reward = SpecimenBestiaryDefinitions.createSurrenderReward(specimenId);
		SpecimenJarData.clearSpecimen(held);
		if (!reward.isEmpty()) {
			DialogueRewards.giveOrDropAtEntity(player, entityId, reward);
		}
		player.displayClientMessage(
				Component.translatable(SpecimenBestiaryDefinitions.surrenderedDialogueKey(specimenId),
								specimenName, reward.getHoverName())
						.withStyle(ChatFormatting.DARK_RED),
				false);
		SpecimenBestiaryEvents.sync(player, progress);
	}

	private static void handleAlchemistPolypSurrender(ServerPlayer player, int entityId, ItemStack held,
			net.minecraft.nbt.CompoundTag specimen, ResourceLocation specimenId, MorphlingPolypLayer requestedLayer,
			SpecimenBestiaryProgress progress) {
		if (!SpecimenJarData.getMorphlingLayers(specimen).contains(requestedLayer)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_layer_missing")
							.withStyle(ChatFormatting.GRAY),
					false);
			return;
		}
		ItemStack morphling = SpecimenBestiaryDefinitions.createWildBoundMorphling(requestedLayer);
		if (morphling.isEmpty()) {
			return;
		}
		progress.recordSpecimen(specimenId);
		progress.recordMorphlingLayer(requestedLayer);
		progress.surrenderSpecimen(specimenId);
		SpecimenJarData.clearSpecimen(held);
		DialogueRewards.giveOrDropAtEntity(player, entityId, morphling);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.alchemist_bestiary_polyp_surrendered",
								morphling.getHoverName())
						.withStyle(ChatFormatting.DARK_RED),
				false);
		SpecimenBestiaryEvents.sync(player, progress);
	}

	private static MorphlingPolypLayer morphlingLayerFromEvent(String eventId) {
		if (eventId == null || !eventId.startsWith(
				HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX)) {
			return null;
		}
		String layerName = eventId.substring(
				HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX.length());
		for (MorphlingPolypLayer layer : MorphlingPolypLayer.values()) {
			if (layer.serializedName().equals(layerName)) {
				return layer;
			}
		}
		return null;
	}

	private static void handleAlchemistBodyAnswersBrief(ServerPlayer player) {
		if (!BodyAnswersAssignment.canBrief(player)) return;
		if (!BodyAnswersAssignment.markBriefed(player)) return;
		BodyAnswersAssignment.giveBriefingSupplies(player);
		MnemonicRecipeKnowledge.awardStarter(player);
	}
}
