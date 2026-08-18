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
	public static final String EVENT_DIAGNOSIS = "vein_mason_diagnosis";
	public static final String EVENT_D5_REWARD = "vein_mason_d5_reward";
	public static final String EVENT_D6_REFERRAL = "vein_mason_d6_referral";
	public static final String EVENT_D6_REWARD = "vein_mason_d6_reward";
	public static final String EVENT_REPLACE_D4 = "vein_mason_replace_d4";
	public static final String EVENT_REPLACE_D5 = "vein_mason_replace_d5";
	public static final String EVENT_REPLACE_D6 = "vein_mason_replace_d6";

	private HarbingerCicatrixAnchoriteDialogueTrees() {
	}

	public static DialogueTree forState(int entityId, AnchoriteProgressSnapshot progress) {
		if (progress.clarity() || progress.purifying()) {
			return refusal(entityId);
		}
		if (!progress.activeBlood() || progress.degree() < 4) {
			return locked(entityId);
		}
		if (!progress.firstLesson()) {
			return firstLesson(entityId);
		}
		if (!progress.firstScarLearned()) {
			return carveReminder(entityId, progress);
		}
		if (!progress.firstEffigyPattern()) {
			return effigyInstruction(entityId);
		}
		if (!progress.firstEffigyLoadout()) {
			return loadoutReminder(entityId);
		}
		if (!progress.d4Reward()) {
			return continuationReward(entityId);
		}
		if (progress.degree() >= 5 && !progress.d5Reward()) return d5(entityId, progress);
		if (progress.degree() >= 6 && !progress.d6Reward()) return d6(entityId, progress);
		return completed(entityId, progress);
	}

	private static DialogueTree d5(int entityId, AnchoriteProgressSnapshot p) {
		String line = !p.d5Varicose() ? "hemomancy.anchorite.d5.cause_strain"
				: !p.d5Diagnosed() ? "hemomancy.anchorite.d5.return_diagnosis"
				: !p.d5Treated() ? "hemomancy.anchorite.d5.recover"
				: !p.fortified() ? "hemomancy.anchorite.d5.fortify" : "hemomancy.anchorite.d5.complete";
		String event = p.d5Varicose() && !p.d5Diagnosed() ? EVENT_DIAGNOSIS : p.d5Ready() ? EVENT_D5_REWARD : null;
		String option = event == null ? "hemomancy.dialogue.anchorite.option.leave"
				: p.d5Ready() ? "hemomancy.dialogue.anchorite.option.claim_d5" : "hemomancy.dialogue.anchorite.option.diagnose";
		List<DialogueOption> options = new java.util.ArrayList<>();
		if (event != null) options.add(new DialogueOption(option, null, event));
		if (p.replacementTier() > 0) options.add(new DialogueOption(
				"hemomancy.dialogue.anchorite.option.replace_pattern", null,
				p.replacementTier() == 2 ? EVENT_REPLACE_D5 : EVENT_REPLACE_D4));
		options.add(new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(line), options))
				.build();
	}

	private static DialogueTree d6(int entityId, AnchoriteProgressSnapshot p) {
		String line = !p.d6Referral() ? "hemomancy.anchorite.d6.referral"
				: !p.d6Counsel() ? "hemomancy.anchorite.d6.seek_counsel"
				: !p.d6FirstRoute() ? "hemomancy.anchorite.d6.first_route"
				: !p.d6Loadout() ? "hemomancy.anchorite.d6.change_loadout"
				: !p.d6SecondRoute() ? "hemomancy.anchorite.d6.second_route" : "hemomancy.anchorite.d6.complete";
		String event = !p.d6Referral() ? EVENT_D6_REFERRAL : p.d6Ready() ? EVENT_D6_REWARD : null;
		String option = event == null ? "hemomancy.dialogue.anchorite.option.leave"
				: p.d6Ready() ? "hemomancy.dialogue.anchorite.option.claim_d6" : "hemomancy.dialogue.anchorite.option.accept_referral";
		List<DialogueOption> options = new java.util.ArrayList<>();
		if (event != null) options.add(new DialogueOption(option, null, event));
		if (p.replacementTier() > 0) options.add(new DialogueOption(
				"hemomancy.dialogue.anchorite.option.replace_pattern", null,
				p.replacementTier() == 3 ? EVENT_REPLACE_D6 : p.replacementTier() == 2 ? EVENT_REPLACE_D5 : EVENT_REPLACE_D4));
		options.add(new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(line), options))
				.build();
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

	private static DialogueTree carveReminder(int entityId, AnchoriteProgressSnapshot progress) {
		List<DialogueOption> options = new java.util.ArrayList<>();
		if (progress.replacementTier() > 0) options.add(new DialogueOption(
				"hemomancy.dialogue.anchorite.option.replace_pattern", null, EVENT_REPLACE_D4));
		options.add(new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.carve_reminder.line1",
						"hemomancy.anchorite.carve_reminder.line2"
				), options))
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

	private static DialogueTree completed(int entityId, AnchoriteProgressSnapshot progress) {
		String replacement = progress.d6Reward() ? EVENT_REPLACE_D6 : progress.d5Reward() ? EVENT_REPLACE_D5 : EVENT_REPLACE_D4;
		List<DialogueOption> options = new java.util.ArrayList<>();
		if (progress.replacementTier() > 0) options.add(new DialogueOption(
				"hemomancy.dialogue.anchorite.option.replace_pattern", null, replacement));
		options.add(new DialogueOption("hemomancy.dialogue.anchorite.option.about_alchemist", "alchemist", null));
		options.add(new DialogueOption("hemomancy.dialogue.anchorite.option.about_artificer", "artificer", null));
		options.add(new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ANCHORITE_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.anchorite.completed.line1",
						"hemomancy.anchorite.completed.line2"
				), options))
				.addNode(new DialogueNode("alchemist", List.of(
						"hemomancy.anchorite.opinion.alchemist.line1",
						"hemomancy.anchorite.opinion.alchemist.line2"), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null))))
				.addNode(new DialogueNode("artificer", List.of(
						"hemomancy.anchorite.opinion.artificer.line1",
						"hemomancy.anchorite.opinion.artificer.line2"), List.of(
						new DialogueOption("hemomancy.dialogue.anchorite.option.leave", null, null))))
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
