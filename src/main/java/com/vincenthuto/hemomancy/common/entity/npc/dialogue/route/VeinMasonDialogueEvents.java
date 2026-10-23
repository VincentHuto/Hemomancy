package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularDiagnosisRules;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.VascularViewMenuProvider;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The Vein Mason (Cicatrix Anchorite) dialogue events: diagnosis, lessons, rewards, referral and pattern
 * replacement.
 */
final class VeinMasonDialogueEvents {
	private VeinMasonDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS -> handleAnchoriteDiagnosis(player);
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD -> handleAnchoriteReward(player, event.getEntityId(), 2);
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL ->
				VeinMasonAssignments.onReferral(player);
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD -> handleAnchoriteReward(player, event.getEntityId(), 3);
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D4 -> replaceAnchoritePattern(player, event.getEntityId(), 1);
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D5 -> replaceAnchoritePattern(player, event.getEntityId(), 2);
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D6 -> replaceAnchoritePattern(player, event.getEntityId(), 3);
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON -> {
			event.setRewardDelivered(handleVeinMasonFirstLesson(player, event.getEntityId()));
		}
		case HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD -> {
			event.setRewardDelivered(handleVeinMasonContinuationReward(player, event.getEntityId()));
		}
		default -> {
			return false;
		}
		}
		return true;
	}

	private static boolean handleVeinMasonFirstLesson(ServerPlayer player, int entityId) {
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player).isActive();
		boolean purifying = HemoCapabilityAccess.getUnstainedProgress(player).hasBegunPurification();
		boolean clarity = HemoCapabilityAccess.getUnstainedProgress(player).hasClarityUnlocked();
		if (degree < 4 || !activeBlood || purifying || clarity) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vein_mason_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}
		if (HarbingerAdvancementGranter.isVeinMasonFirstLesson(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vein_mason_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}

		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(player);
		DialogueRewards.giveOrDropAtEntity(player, entityId, lesson.patternStack());
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.scar_blank.get()));
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(lesson.catalyst()));
		if (!hasAnyKnapper(player)) {
			DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.hematic_iron_knapper.get()));
		}
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vein_mason_first_lesson")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		return true;
	}

	private static boolean handleVeinMasonContinuationReward(ServerPlayer player, int entityId) {
		if (!HarbingerAdvancementGranter.isVeinMasonFirstEffigyLoadout(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vein_mason_reward_unready")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}
		if (HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player)) {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.vein_mason_reward_known")
							.withStyle(ChatFormatting.GRAY),
					false);
			return false;
		}

		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.continuationForPlayer(player);
		DialogueRewards.giveOrDropAtEntity(player, entityId, lesson.patternStack());
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.scar_blank.get()));
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(lesson.catalyst()));
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.runic_motif_paper.get(), 4));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_REWARD_CLAIMED);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vein_mason_reward_claimed")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		return true;
	}

	private static boolean hasAnyKnapper(ServerPlayer player) {
		for (ItemStack stack : player.getInventory().items) {
			if (stack.getItem() instanceof com.vincenthuto.hutoslib.common.item.ItemKnapper) {
				return true;
			}
		}
		return false;
	}

	private static void handleAnchoriteDiagnosis(ServerPlayer player) {
		var diagnosis = VascularDiagnosisRules.diagnosePlayer(player);
		VeinMasonAssignments.onDiagnosis(player);
		player.displayClientMessage(Component.translatable("hemomancy.dialogue.event.anchorite_diagnosis",
				diagnosis.worstSection().name(), diagnosis.flow().name(), Math.round(diagnosis.health()),
				diagnosis.treatment().name()).withStyle(ChatFormatting.DARK_RED), false);
		player.openMenu(new VascularViewMenuProvider());
	}

	private static void handleAnchoriteReward(ServerPlayer player, int entityId, int tier) {
		ResourceLocation ready = tier == 2 ? VeinMasonAssignments.D5_READY : VeinMasonAssignments.D6_READY;
		ResourceLocation claimed = tier == 2 ? VeinMasonAssignments.D5_REWARD : VeinMasonAssignments.D6_REWARD;
		if (!VeinMasonAssignments.has(player, ready) || VeinMasonAssignments.has(player, claimed)) return;
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.strongestForPlayer(player, tier);
		DialogueRewards.giveOrDropAtEntity(player, entityId, lesson.patternStack());
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.scar_blank.get()));
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(lesson.catalyst()));
		DialogueRewards.giveOrDropAtEntity(player, entityId, new ItemStack(ItemInit.runic_motif_paper.get(), tier == 2 ? 4 : 8));
		VeinMasonAssignments.grant(player, claimed);
		player.displayClientMessage(Component.translatable(tier == 2
				? "hemomancy.dialogue.event.anchorite_d5_reward" : "hemomancy.dialogue.event.anchorite_d6_reward")
				.withStyle(ChatFormatting.DARK_RED), false);
	}

	private static void replaceAnchoritePattern(ServerPlayer player, int entityId, int tier) {
		VeinMasonScarLesson.Lesson lesson = tier == 1
				? (HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player)
						? VeinMasonScarLesson.continuationForPlayer(player) : VeinMasonScarLesson.forPlayer(player))
				: VeinMasonScarLesson.strongestForPlayer(player, tier);
		if (VeinMasonScarLesson.needsReplacement(player, lesson)) {
			DialogueRewards.giveOrDropAtEntity(player, entityId, lesson.patternStack());
		}
	}
}
