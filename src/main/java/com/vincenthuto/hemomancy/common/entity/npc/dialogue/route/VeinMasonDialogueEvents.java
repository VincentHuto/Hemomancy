package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerCicatrixAnchoriteEntity;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularDiagnosisRules;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.VascularViewMenuProvider;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.mission.shared.OwedRewards;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Vein Mason (Cicatrix Anchorite) dialogue events: diagnosis, lessons, rewards, referral and pattern
 * replacement.
 */
final class VeinMasonDialogueEvents {
	private VeinMasonDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		if (CoreLoopLessonRoutes.teach(event, CoreLoopLessons.VEIN_MASON, HarbingerCicatrixAnchoriteEntity.class)) return true;
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

		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.issue(player, VeinMasonScarLesson.Slot.FIRST);
		List<ItemStack> kit = new ArrayList<>(List.of(lesson.patternStack(), new ItemStack(ItemInit.scar_blank.get()),
				new ItemStack(lesson.catalyst())));
		if (!hasAnyKnapper(player)) kit.add(new ItemStack(ItemInit.hematic_iron_knapper.get()));
		OwedRewards.owe(player, kit);
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vein_mason_first_lesson")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		OwedRewards.deliver(player);
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

		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.issue(player, VeinMasonScarLesson.Slot.CONTINUATION);
		OwedRewards.owe(player, List.of(lesson.patternStack(), new ItemStack(ItemInit.scar_blank.get()),
				new ItemStack(lesson.catalyst()), new ItemStack(ItemInit.runic_motif_paper.get(), 4)));
		HarbingerAdvancementGranter.grantIfNotDone(player,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_REWARD_CLAIMED);
		player.displayClientMessage(
				Component.translatable("hemomancy.dialogue.event.vein_mason_reward_claimed")
						.withStyle(ChatFormatting.DARK_RED),
				false);
		OwedRewards.deliver(player);
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
				Component.translatable("vein_section.hemomancy." + diagnosis.worstSection().name().toLowerCase(Locale.ROOT)),
				Component.translatable("blood_flow_state.hemomancy." + diagnosis.flow().name().toLowerCase(Locale.ROOT)),
				Math.round(diagnosis.health()), Component.translatable(diagnosis.treatment().translationKey()))
				.withStyle(ChatFormatting.DARK_RED), false);
		player.openMenu(new VascularViewMenuProvider());
	}

	private static void handleAnchoriteReward(ServerPlayer player, int entityId, int tier) {
		ResourceLocation ready = tier == 2 ? VeinMasonAssignments.D5_READY : VeinMasonAssignments.D6_READY;
		ResourceLocation claimed = tier == 2 ? VeinMasonAssignments.D5_REWARD : VeinMasonAssignments.D6_REWARD;
		if (!VeinMasonAssignments.has(player, ready)) {
			refuse(player, "hemomancy.dialogue.event.anchorite_reward_unready");
			return;
		}
		if (VeinMasonAssignments.has(player, claimed)) {
			refuse(player, "hemomancy.dialogue.event.anchorite_reward_known");
			return;
		}
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.issue(player,
				tier == 2 ? VeinMasonScarLesson.Slot.D5 : VeinMasonScarLesson.Slot.D6);
		OwedRewards.owe(player, List.of(lesson.patternStack(), new ItemStack(ItemInit.scar_blank.get()),
				new ItemStack(lesson.catalyst()), new ItemStack(ItemInit.runic_motif_paper.get(), tier == 2 ? 4 : 8)));
		VeinMasonAssignments.grant(player, claimed);
		player.displayClientMessage(Component.translatable(tier == 2
				? "hemomancy.dialogue.event.anchorite_d5_reward" : "hemomancy.dialogue.event.anchorite_d6_reward")
				.withStyle(ChatFormatting.DARK_RED), false);
		OwedRewards.deliver(player);
	}

	private static void replaceAnchoritePattern(ServerPlayer player, int entityId, int tier) {
		// A pattern is replaced only for the tier the player was actually given, as the replacement option is.
		boolean earned = switch (tier) {
			case 3 -> VeinMasonAssignments.has(player, VeinMasonAssignments.D6_REWARD);
			case 2 -> VeinMasonAssignments.has(player, VeinMasonAssignments.D5_REWARD);
			default -> HarbingerAdvancementGranter.isVeinMasonFirstLesson(player);
		};
		if (!earned) {
			refuse(player, "hemomancy.dialogue.event.anchorite_replace_unearned");
			return;
		}
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.issued(player, switch (tier) {
			case 3 -> VeinMasonScarLesson.Slot.D6;
			case 2 -> VeinMasonScarLesson.Slot.D5;
			default -> HarbingerAdvancementGranter.isVeinMasonRewardClaimed(player)
					? VeinMasonScarLesson.Slot.CONTINUATION : VeinMasonScarLesson.Slot.FIRST;
		});
		if (!VeinMasonScarLesson.needsReplacement(player, lesson) || OwedRewards.carriesOrOwed(player,
				stack -> ItemScarPattern.getScarIds(stack).contains(lesson.patternScarId()))) {
			refuse(player, "hemomancy.dialogue.event.anchorite_replace_unneeded");
			return;
		}
		OwedRewards.give(player, List.of(lesson.patternStack()));
	}

	/** PT-28: a reward the Mason will not give says so. */
	private static void refuse(ServerPlayer player, String key) {
		player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.GRAY), false);
	}
}
