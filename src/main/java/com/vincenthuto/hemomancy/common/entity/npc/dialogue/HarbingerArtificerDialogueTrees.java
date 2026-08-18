package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

import static com.vincenthuto.hemomancy.common.mission.ArtificerProgressionRules.Step;

public final class HarbingerArtificerDialogueTrees {
	private static final ResourceLocation ARTIFICER_ICON = Hemomancy.rloc(
			"textures/entity/harbinger_artificer/harbinger_artificer.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_artificer";
	public static final String EVENT_CLAIM_WORN_VOW_REWARD = "artificer_claim_worn_vow_reward";
	public static final String EVENT_CLAIM_THREE_ANSWERS_REWARD = "artificer_claim_three_answers_reward";
	public static final String EVENT_CLAIM_CRIMSON_VESTMENT_REWARD = "artificer_claim_crimson_vestment_reward";
	public static final String EVENT_CLAIM_ASSUMED_LIMB_REWARD = "artificer_claim_assumed_limb_reward";
	public static final String EVENT_CLAIM_HEMATIC_IRON_FITTING = "artificer_claim_hematic_iron_fitting";
	public static final String EVENT_CLAIM_FORK_FITTING = "artificer_claim_fork_fitting";
	public static final String EVENT_CLAIM_BLOOD_LUST_FITTING = "artificer_claim_blood_lust_fitting";
	public static final String EVENT_CLAIM_D7_FITTING = "artificer_claim_d7_fitting";
	public static final String EVENT_CLAIM_LIVING_ARSENAL_FITTING = "artificer_claim_living_arsenal_fitting";
	public static final String EVENT_BRIEF_WORN_VOW = "artificer_brief_worn_vow";
	public static final String EVENT_BRIEF_THREE_ANSWERS = "artificer_brief_three_answers";
	public static final String EVENT_BRIEF_CRIMSON_VESTMENT = "artificer_brief_crimson_vestment";
	public static final String EVENT_BRIEF_ASSUMED_LIMB = "artificer_brief_assumed_limb";
	public static final String EVENT_BRIEF_WEIGHT_OF_FRAME = "artificer_brief_weight_of_frame";
	public static final String EVENT_INSPECT_THREE_ANSWERS = "artificer_inspect_three_answers";
	public static final String EVENT_INSPECT_CRIMSON_VESTMENT = "artificer_inspect_crimson_vestment";
	public static final String EVENT_INSPECT_WEIGHT_OF_FRAME = "artificer_inspect_weight_of_frame";
	public static final String EVENT_CLAIM_D7_REWARD = "artificer_claim_d7_reward";
	public static final String EVENT_RECOVER_FORK_PREFIX = "artificer_recover_fork_";
	public static final String EVENT_RECOVER_D7_PREFIX = "artificer_recover_d7_";

	private HarbingerArtificerDialogueTrees() {
	}

	public static DialogueTree forState(int entityId, ArtificerProgressSnapshot progress) {
		if (progress.purifying() || progress.clarity()) {
			return refusal(entityId);
		}
		if (!progress.activeBlood() || progress.degree() <= 0) {
			return unawakened(entityId);
		}
		if (progress.degree() == 1) {
			return neophyte(entityId);
		}
		return workshop(entityId, progress);
	}

	private static DialogueTree refusal(int entityId) {
		return DialogueTree.builder(SPEAKER, ARTIFICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.artificer.refusal.line1",
						"hemomancy.artificer.refusal.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.artificer.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)
				)))
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree unawakened(int entityId) {
		return DialogueTree.builder(SPEAKER, ARTIFICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.artificer.unawakened.line1",
						"hemomancy.artificer.unawakened.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.artificer.option.who_are_you", "identity", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)
				)))
				.addNode(identityNode())
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree neophyte(int entityId) {
		return DialogueTree.builder(SPEAKER, ARTIFICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.artificer.neophyte.line1",
						"hemomancy.artificer.neophyte.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.artificer.option.who_are_you", "identity", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.armature_hint", "armature_hint", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)
				)))
				.addNode(identityNode())
				.addNode(new DialogueNode("armature_hint", List.of(
						"hemomancy.artificer.armature_hint.line1",
						"hemomancy.artificer.armature_hint.line2"
				), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null))))
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree workshop(int entityId, ArtificerProgressSnapshot progress) {
		int degree = progress.degree();
		List<DialogueOption> options = new ArrayList<>();
		options.add(new DialogueOption("hemomancy.dialogue.artificer.option.who_are_you", "identity", null));
		if (degree >= 2) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.teach_armature", "armature", null));
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.assignments", "assignments", null));
		}
		if (degree >= 3) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.armor_path", "armor_forks", null));
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.scarlet_vanity", "scarlet_vanity", null));
		}
		if (progress.livingStaffBond()) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.living_grafts", "living_grafts", null));
		}
		if (degree >= 5) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.late_armature", "late_armature", null));
		}
		if (degree >= 7) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.monolithic_armature",
					"monolithic_armature", null));
		}
		options.add(new DialogueOption("hemomancy.dialogue.artificer.option.ask_about_item", "item_hint", null));
		options.add(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null));

		DialogueTree.Builder builder = DialogueTree.builder(SPEAKER, ARTIFICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.artificer.workshop.line1",
						"hemomancy.artificer.workshop.line2"
				), options))
				.addNode(identityNode());
		if (degree >= 2) {
			builder.addNode(armatureNode());
			builder.addNode(assignmentsNode(progress));
			addAssignmentReminderNodes(builder, progress);
		}
		if (degree >= 3) {
			builder.addNode(armorForksNode());
			builder.addNode(scarletVanityNode());
		}
		if (progress.livingStaffBond()) {
			builder.addNode(livingGraftsNode());
		}
		if (degree >= 5) {
			builder.addNode(lateArmatureNode());
		}
		if (degree >= 7) {
			builder.addNode(monolithicArmatureNode());
		}
		builder.addNode(itemHintNode());
		return builder.build();
	}

	private static DialogueNode identityNode() {
		return new DialogueNode("identity", List.of(
				"hemomancy.artificer.identity.line1",
				"hemomancy.artificer.identity.line2",
				"hemomancy.artificer.identity.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode armatureNode() {
		return new DialogueNode("armature", List.of(
				"hemomancy.artificer.armature.line1",
				"hemomancy.artificer.armature.line2",
				"hemomancy.artificer.armature.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode armorForksNode() {
		return new DialogueNode("armor_forks", List.of(
				"hemomancy.artificer.forks.line1",
				"hemomancy.artificer.forks.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode scarletVanityNode() {
		return new DialogueNode("scarlet_vanity", List.of(
				"hemomancy.artificer.scarlet_vanity.line1",
				"hemomancy.artificer.scarlet_vanity.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode livingGraftsNode() {
		return new DialogueNode("living_grafts", List.of(
				"hemomancy.artificer.grafts.line1",
				"hemomancy.artificer.grafts.line2",
				"hemomancy.artificer.grafts.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode lateArmatureNode() {
		return new DialogueNode("late_armature", List.of(
				"hemomancy.artificer.late_armature.line1",
				"hemomancy.artificer.late_armature.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode monolithicArmatureNode() {
		return new DialogueNode("monolithic_armature", List.of(
				"hemomancy.artificer.monolithic_armature.line1",
				"hemomancy.artificer.monolithic_armature.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}

	private static DialogueNode assignmentsNode(ArtificerProgressSnapshot progress) {
		List<DialogueOption> options = new ArrayList<>();
		assignmentOption(options, "worn_vow", progress.wornVow(), EVENT_BRIEF_WORN_VOW,
				EVENT_CLAIM_WORN_VOW_REWARD, EVENT_CLAIM_HEMATIC_IRON_FITTING, progress.missingWornVowFitting());
		assignmentOption(options, "three_answers", progress.threeAnswers(), EVENT_BRIEF_THREE_ANSWERS,
				EVENT_INSPECT_THREE_ANSWERS, EVENT_CLAIM_FORK_FITTING, progress.missingForkFitting());
		assignmentOption(options, "crimson_vestment", progress.crimsonVestment(), EVENT_BRIEF_CRIMSON_VESTMENT,
				EVENT_INSPECT_CRIMSON_VESTMENT, EVENT_CLAIM_BLOOD_LUST_FITTING, progress.missingCrimsonFitting());
		assignmentOption(options, "assumed_limb", progress.assumedLimb(), EVENT_BRIEF_ASSUMED_LIMB,
				EVENT_CLAIM_ASSUMED_LIMB_REWARD, EVENT_CLAIM_LIVING_ARSENAL_FITTING, progress.missingAssumedLimbFitting());
		assignmentOption(options, "weight_of_frame", progress.weightOfFrame(), EVENT_BRIEF_WEIGHT_OF_FRAME,
				EVENT_INSPECT_WEIGHT_OF_FRAME, EVENT_CLAIM_D7_FITTING, progress.missingD7Fitting());
		if (progress.weightOfFrame() == Step.MATERIAL_REWARD) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.claim_d7_reward", null,
					EVENT_CLAIM_D7_REWARD));
		}
		if (progress.needsForkRecovery()) addRecoveryOptions(options, EVENT_RECOVER_FORK_PREFIX,
				"barbed", "chitinite", "prismatic");
		if (progress.needsD7Recovery()) addRecoveryOptions(options, EVENT_RECOVER_D7_PREFIX,
				"silent_archon", "edacious", "sheolic", "phantasmal");
		options.add(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null));
		return new DialogueNode("assignments", List.of(
				"hemomancy.artificer.assignments.line1",
				"hemomancy.artificer.assignments.line2"
		), options);
	}

	private static void assignmentOption(List<DialogueOption> options, String assignment, Step step,
			String briefingEvent, String inspectionEvent, String fittingEvent, boolean missingFitting) {
		if (step == Step.LOCKED || step == Step.MATERIAL_REWARD || step == Step.RECOVER_BRANCH) return;
		String event = switch (step) {
			case BRIEFING -> briefingEvent;
			case INSPECTION -> inspectionEvent;
			case FITTING -> fittingEvent;
			case COMPLETE -> missingFitting ? fittingEvent : null;
			default -> null;
		};
		if (step == Step.COMPLETE && !missingFitting) return;
		String key = event == null ? "reminder"
				: step == Step.COMPLETE ? "replacement" : step.name().toLowerCase(java.util.Locale.ROOT);
		options.add(new DialogueOption("hemomancy.dialogue.artificer.option." + (event == null ? key : assignment + "." + key),
				event == null ? "assignment_" + assignment : null, event));
	}

	private static void addAssignmentReminderNodes(DialogueTree.Builder builder, ArtificerProgressSnapshot progress) {
		addReminder(builder, "worn_vow", progress.wornVow());
		addReminder(builder, "three_answers", progress.threeAnswers());
		addReminder(builder, "crimson_vestment", progress.crimsonVestment());
		addReminder(builder, "assumed_limb", progress.assumedLimb());
		addReminder(builder, "weight_of_frame", progress.weightOfFrame());
	}

	private static void addReminder(DialogueTree.Builder builder, String assignment, Step step) {
		if (step == Step.LOCKED || step == Step.COMPLETE || step == Step.BRIEFING || step == Step.INSPECTION
				|| step == Step.FITTING || step == Step.MATERIAL_REWARD || step == Step.RECOVER_BRANCH) return;
		builder.addNode(new DialogueNode("assignment_" + assignment, List.of(
				"hemomancy.artificer.assignment." + assignment + "." + step.name().toLowerCase(java.util.Locale.ROOT)),
				List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null))));
	}

	private static void addRecoveryOptions(List<DialogueOption> options, String prefix, String... branches) {
		for (String branch : branches) {
			options.add(new DialogueOption("hemomancy.dialogue.artificer.option.recover." + branch, null, prefix + branch));
		}
	}

	private static DialogueNode itemHintNode() {
		return new DialogueNode("item_hint", List.of(
				"hemomancy.artificer.item_hint"
		), List.of(new DialogueOption("hemomancy.dialogue.artificer.option.leave", null, null)));
	}
}
