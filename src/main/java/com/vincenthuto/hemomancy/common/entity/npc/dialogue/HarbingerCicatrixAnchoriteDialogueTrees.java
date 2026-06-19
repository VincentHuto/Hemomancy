package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class HarbingerCicatrixAnchoriteDialogueTrees {
	private static final ResourceLocation ANCHORITE_ICON = Hemomancy.rloc(
			"textures/entity/harbinger_cicatrix_anchorite/harbinger_cicatrix_anchorite.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_cicatrix_anchorite";
	public static final String EVENT_FIRST_LESSON = "vein_mason_first_lesson";
	public static final String EVENT_CONTINUATION_REWARD = "vein_mason_continuation_reward";

	private HarbingerCicatrixAnchoriteDialogueTrees() {
	}

	public static DialogueTree forState(int entityId, int degree, boolean activeBlood, boolean purifying,
			boolean clarity, boolean veinMasonFirstLesson, boolean veinMasonFirstScarLearned,
			boolean veinMasonFirstEffigyPattern, boolean veinMasonFirstEffigyLoadout,
			boolean veinMasonRewardClaimed) {
		if (clarity || purifying) {
			return refusal(entityId);
		}
		if (!activeBlood || degree < 4) {
			return locked(entityId);
		}
		if (!veinMasonFirstLesson) {
			return firstLesson(entityId);
		}
		if (!veinMasonFirstScarLearned) {
			return carveReminder(entityId);
		}
		if (!veinMasonFirstEffigyPattern) {
			return effigyInstruction(entityId);
		}
		if (!veinMasonFirstEffigyLoadout) {
			return loadoutReminder(entityId);
		}
		if (!veinMasonRewardClaimed) {
			return continuationReward(entityId);
		}
		return completed(entityId);
	}

	private static DialogueTree firstLesson(int entityId) {
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.first_lesson.line1",
						"hemomancy.anchorite.first_lesson.line2",
						"hemomancy.anchorite.first_lesson.line3"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.accept_first_pattern",
								"first_lesson_given", EVENT_FIRST_LESSON),
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.addNode(new DialogueNode("first_lesson_given", List.of(
						"hemomancy.anchorite.first_lesson.given.line1",
						"hemomancy.anchorite.first_lesson.given.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.build();
	}

	private static DialogueTree carveReminder(int entityId) {
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.carve_reminder.line1",
						"hemomancy.anchorite.carve_reminder.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.build();
	}

	private static DialogueTree effigyInstruction(int entityId) {
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.effigy_instruction.line1",
						"hemomancy.anchorite.effigy_instruction.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.build();
	}

	private static DialogueTree loadoutReminder(int entityId) {
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.loadout_reminder.line1",
						"hemomancy.anchorite.loadout_reminder.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.build();
	}

	private static DialogueTree continuationReward(int entityId) {
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.reward.line1",
						"hemomancy.anchorite.reward.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.claim_reward",
								"reward_claimed", EVENT_CONTINUATION_REWARD),
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.addNode(new DialogueNode("reward_claimed", List.of(
						"hemomancy.anchorite.reward.claimed.line1",
						"hemomancy.anchorite.reward.claimed.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.build();
	}

	private static DialogueTree completed(int entityId) {
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.completed.line1",
						"hemomancy.anchorite.completed.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.build();
	}

	private static DialogueTree locked(int entityId) {
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.locked.line1",
						"hemomancy.anchorite.locked.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.build();
	}

	private static DialogueTree refusal(int entityId) {
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.refusal.line1",
						"hemomancy.anchorite.refusal.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null)
				)))
				.build();
	}
}
