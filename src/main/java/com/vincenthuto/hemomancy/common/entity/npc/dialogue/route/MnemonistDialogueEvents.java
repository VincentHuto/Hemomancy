package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.HemomancyDiscoverySource;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.mission.shared.MnemonicRecipeKnowledge;
import com.vincenthuto.hemomancy.common.util.ProgressionGates;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Mnemonist's dialogue events: starter memories, the Vagrant Mind and Chamber errands, the Woven Vessel,
 * the Reliquary, the blank-memory recipe, the Scriptorium lesson and the Vein Mason counsel.
 */
final class MnemonistDialogueEvents {
	private MnemonistDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		var mnemonistChoice = MnemonistStarterMemoryChoice.fromEventId(event.getEventId());
		if (mnemonistChoice.isPresent()) {
			event.setRewardDelivered(handleMnemonistStarterMemory(player, event.getEntityId(), mnemonistChoice.get()));
			return true;
		}
		switch (event.getEventId()) {
		case VagrantMindInquiryDialogue.MEMORY_REPORT -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry.reportMemory(
						player, player.level().getEntity(event.getEntityId())));
		case VagrantMindInquiryDialogue.BEARING -> event.setRewardDelivered(
				com.vincenthuto.hemomancy.common.mission.shared.VagrantMindGuidance.tell(
						player, player.level().getEntity(event.getEntityId())));
		case HarbingerMnemonistDialogueTrees.EVENT_GUIDED_CHAMBER -> {
			Entity teacher = player.level().getEntity(event.getEntityId());
			if (teacher instanceof HarbingerMnemonistEntity && player.distanceToSqr(teacher) <= 64) {
				boolean started = com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService.beginGuidedVisit(player);
				event.setRewardDelivered(started);
				player.displayClientMessage(Component.translatable(started
						? "hemomancy.mnemonist.chamber.guided.started"
						: "hemomancy.mnemonist.chamber.guided.unavailable"), true);
			}
		}
		case HarbingerMnemonistDialogueTrees.EVENT_CIRCUS_WAYBILL ->
				handleMnemonistCircusWaybill(player, event.getEntityId());
		case HarbingerMnemonistDialogueTrees.EVENT_RELIQUARY_TAUGHT ->
				MnemonicReliquaryProgression.teach(player);
		case HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL ->
				VeinMasonAssignments.onCounsel(player);
		case HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN ->
				event.setRewardDelivered(handleMnemonistWovenVessel(player, event.getEntityId()));
		case HarbingerMnemonistDialogueTrees.EVENT_BLANK_MEMORY_RECIPE -> {
			Entity teacher = player.level().getEntity(event.getEntityId());
			if (teacher instanceof HarbingerMnemonistEntity
					&& player.distanceToSqr(teacher) <= 64
					&& ProgressionGates.hasDegree(player, 3)
					&& (!HemoCapabilityAccess.getUnstainedProgress(player).hasBegunPurification() && !HemoCapabilityAccess.getUnstainedProgress(player).hasClarityUnlocked())) {
				event.setRewardDelivered(MnemonicRecipeKnowledge.awardBlankMemory(player) > 0);
			}
		}
		case HarbingerMnemonistDialogueTrees.EVENT_SCRIPTORIUM_LESSON -> {
			Entity teacher = player.level().getEntity(event.getEntityId());
			if (teacher instanceof HarbingerMnemonistEntity && teacher.isAlive()
					&& player.distanceToSqr(teacher) <= 64
					&& ProgressionGates.hasDegree(player, 3)
					&& (!HemoCapabilityAccess.getUnstainedProgress(player).hasBegunPurification() && !HemoCapabilityAccess.getUnstainedProgress(player).hasClarityUnlocked())) {
				event.setRewardDelivered(LiberKnowledgeHelper.unlockEntry(player,
						LiberEntryDefinitions.SCRIPTORIUM, HemomancyDiscoverySource.DIALOGUE));
			}
		}
		default -> {
			return false;
		}
		}
		return true;
	}

	private static void handleMnemonistCircusWaybill(ServerPlayer player, int entityId) {
		if (!(player.level().getEntity(entityId) instanceof HarbingerMnemonistEntity)
				|| HemoCapabilityAccess.getPlayerDegreeNumber(player) < CircusIntroductionRules.MINIMUM_DEGREE
				|| player.getInventory().contains(new ItemStack(ItemInit.circus_waybill.get()))) return;
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.circus_waybill.get()));
		player.displayClientMessage(Component.translatable(
				"hemomancy.dialogue.event.mnemonist_circus_waybill").withStyle(ChatFormatting.DARK_RED), false);
	}

	private static boolean handleMnemonistStarterMemory(ServerPlayer player, int entityId, MnemonistStarterMemoryChoice choice) {
		boolean purifying = HemoCapabilityAccess.getUnstainedProgress(player).hasBegunPurification();
		boolean clarity = HemoCapabilityAccess.getUnstainedProgress(player).hasClarityUnlocked();
		boolean claimed = player.getPersistentData().getBoolean(MnemonistStarterMemoryChoice.CLAIM_KEY);
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);

		if (!MnemonistStarterMemoryChoice.canClaim(degree, purifying, clarity, claimed)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_starter_unavailable")
							.withStyle(ChatFormatting.DARK_RED),
					false);
			return false;
		}
		if (knowsStarterManipulation(player, choice.manipulationName())) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_starter_already_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}

		ItemStack stack = starterStack(choice);
		if (stack.isEmpty()) {
			return false;
		}
		DialogueRewards.giveOrDropAtEntity(player, entityId, stack);
		player.getPersistentData().putBoolean(MnemonistStarterMemoryChoice.CLAIM_KEY, true);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.mnemonist_starter_granted")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		return true;
	}

	private static boolean knowsStarterManipulation(ServerPlayer player, String manipulationName) {
		return HemoCapabilityAccess.getKnownManipulations(player).getKnownManips().keySet().stream()
						.anyMatch(manip -> manip != null && manipulationName.equals(manip.getName()));
	}

	private static ItemStack starterStack(MnemonistStarterMemoryChoice choice) {
		return switch (choice) {
			case BLOOD_SHOT -> new ItemStack(ItemInit.crude_memory_blood_shot.get());
			case BLOOD_RUSH -> new ItemStack(ItemInit.crude_memory_blood_rush.get());
			case DEADLY_GAZE -> new ItemStack(ItemInit.crude_memory_deadly_gaze.get());
		};
	}

	private static boolean handleMnemonistWovenVessel(ServerPlayer player, int entityId) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		boolean purifying = HemoCapabilityAccess.getUnstainedProgress(player).hasBegunPurification();
		boolean clarity = HemoCapabilityAccess.getUnstainedProgress(player).hasClarityUnlocked();

		if (degree < 3 || purifying || clarity) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}
		if (HarbingerAdvancementGranter.isMnemonistWovenVesselComplete(player)) {
			MnemonicRecipeKnowledge.awardCatalogue(player);
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}
		if (!DialogueRewards.hasItemCount(player, ItemInit.hematic_memory.get(), 1)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_missing_memory")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}
		if (!DialogueRewards.hasItemCount(player, Items.BOOK, 1)
				|| !DialogueRewards.hasItemCount(player, Items.INK_SAC, 1)
				|| !DialogueRewards.hasItemCount(player, Items.PAPER, 3)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_missing_archive")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}

		if (!player.isCreative()) {
			DialogueRewards.consumeItemCount(player, Items.BOOK, 1);
			DialogueRewards.consumeItemCount(player, Items.INK_SAC, 1);
			DialogueRewards.consumeItemCount(player, Items.PAPER, 3);
		}
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.bleeding_bulb.get()));
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.vivacious_enzyme.get(), 3));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE);
		MnemonicReliquaryProgression.teach(player);
		MnemonicRecipeKnowledge.awardCatalogue(player);
		if (HarbingerAdvancementGranter.isMnemonistFirstWeaveComplete(player)) {
			HarbingerAdvancementGranter.grantIfNotDone(player,
					HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_FINISHED);
		}
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.mnemonist_woven_vessel_complete")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		return true;
	}
}
