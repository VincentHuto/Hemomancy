package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

/**
 * Static factory producing {@link DialogueTree} variants for Unstained Acolyte
 * NPCs. Unlike the Zealot (who recruits), the Acolyte offers stage-aware
 * guidance, lore, and repeatable tasks for purity/clarity progress.
 */
public final class AcolyteDialogueTrees {

	private static final ResourceLocation ACOLYTE_ICON = Hemomancy.rloc("textures/entity/unstained_acolyte/unstained_acolyte.png");
	private static final String SPEAKER = "entity.hemomancy.unstained_acolyte";

	private AcolyteDialogueTrees() {}

	// ════════════════════════════════════════════════════════════
	//  Pre-Path: Player has not begun purification
	// ════════════════════════════════════════════════════════════

	/** Player has not begun purification — gentle introduction. */
	public static DialogueTree notOnPath(int entityId) {
		return DialogueTree.builder(SPEAKER, ACOLYTE_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.acolyte.not_on_path.line1",
						"hemomancy.acolyte.not_on_path.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.acolyte.option.who_are_you", "who_are_you", null),
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.addNode(new DialogueNode("who_are_you", List.of(
						"hemomancy.acolyte.who_are_you.line1",
						"hemomancy.acolyte.who_are_you.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	// ════════════════════════════════════════════════════════════
	//  Purity Stage: CORRUPTED (0-24) — Just started
	// ════════════════════════════════════════════════════════════

	/** Player has begun purification but is still Corrupted. */
	public static DialogueTree corruptedStage(int entityId) {
		return DialogueTree.builder(SPEAKER, ACOLYTE_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.acolyte.corrupted.line1",
						"hemomancy.acolyte.corrupted.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.acolyte.option.how_purify", "how_purify", null),
						new DialogueOption("hemomancy.dialogue.acolyte.option.task_gather", null, "acolyte_task_gather_ghost_pipe"),
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.addNode(new DialogueNode("how_purify", List.of(
						"hemomancy.acolyte.how_purify.line1",
						"hemomancy.acolyte.how_purify.line2",
						"hemomancy.acolyte.how_purify.line3"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	// ════════════════════════════════════════════════════════════
	//  Purity Stage: TAINTED (25-49)
	// ════════════════════════════════════════════════════════════

	/** Player has reached TAINTED stage — offers deeper lore and tasks. */
	public static DialogueTree taintedStage(int entityId) {
		return DialogueTree.builder(SPEAKER, ACOLYTE_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.acolyte.tainted.line1",
						"hemomancy.acolyte.tainted.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.acolyte.option.about_lady", "lady_lore", null),
						new DialogueOption("hemomancy.dialogue.acolyte.option.task_wreath", null, "acolyte_task_wreath"),
						new DialogueOption("hemomancy.dialogue.acolyte.option.task_hemolytic", null, "acolyte_task_hemolytic"),
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.addNode(new DialogueNode("lady_lore", List.of(
						"hemomancy.acolyte.lady_lore.line1",
						"hemomancy.acolyte.lady_lore.line2",
						"hemomancy.acolyte.lady_lore.line3"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	// ════════════════════════════════════════════════════════════
	//  Purity Stage: CLEANSING (50-74)
	// ════════════════════════════════════════════════════════════

	/** Player has reached CLEANSING stage — more advanced guidance. */
	public static DialogueTree cleansingStage(int entityId) {
		return DialogueTree.builder(SPEAKER, ACOLYTE_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.acolyte.cleansing.line1",
						"hemomancy.acolyte.cleansing.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.acolyte.option.about_silver_veil", "silver_veil", null),
						new DialogueOption("hemomancy.dialogue.acolyte.option.task_consecrate", null, "acolyte_task_consecrate"),
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.addNode(new DialogueNode("silver_veil", List.of(
						"hemomancy.acolyte.silver_veil.line1",
						"hemomancy.acolyte.silver_veil.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	// ════════════════════════════════════════════════════════════
	//  Purity Stage: ABSOLVED (75-99)
	// ════════════════════════════════════════════════════════════

	/** Player has reached ABSOLVED stage — nearing full purity. */
	public static DialogueTree absolvedStage(int entityId) {
		return DialogueTree.builder(SPEAKER, ACOLYTE_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.acolyte.absolved.line1",
						"hemomancy.acolyte.absolved.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.acolyte.option.about_clarity", "about_clarity", null),
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.addNode(new DialogueNode("about_clarity", List.of(
						"hemomancy.acolyte.about_clarity.line1",
						"hemomancy.acolyte.about_clarity.line2",
						"hemomancy.acolyte.about_clarity.line3"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	// ════════════════════════════════════════════════════════════
	//  Purity Stage: PURIFIED (100) — Before Clarity
	// ════════════════════════════════════════════════════════════

	/** Player is fully purified but hasn't unlocked Clarity yet. */
	public static DialogueTree purifiedStage(int entityId) {
		return DialogueTree.builder(SPEAKER, ACOLYTE_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.acolyte.purified.line1",
						"hemomancy.acolyte.purified.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.acolyte.option.how_clarity", "how_clarity", null),
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.addNode(new DialogueNode("how_clarity", List.of(
						"hemomancy.acolyte.how_clarity.line1",
						"hemomancy.acolyte.how_clarity.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	// ════════════════════════════════════════════════════════════
	//  Clarity Phase — Player is on the second progression track
	// ════════════════════════════════════════════════════════════

	/** Player has clarity unlocked — guidance about the clarity path. */
	public static DialogueTree clarityPhase(int entityId) {
		return DialogueTree.builder(SPEAKER, ACOLYTE_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.acolyte.clarity.line1",
						"hemomancy.acolyte.clarity.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.acolyte.option.about_verdigris", "verdigris", null),
						new DialogueOption("hemomancy.dialogue.acolyte.option.task_chalice", null, "acolyte_task_chalice"),
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.addNode(new DialogueNode("verdigris", List.of(
						"hemomancy.acolyte.verdigris.line1",
						"hemomancy.acolyte.verdigris.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	// ════════════════════════════════════════════════════════════
	//  Enlightened — Player has completed the full path
	// ════════════════════════════════════════════════════════════

	/** Player is fully Enlightened — ultimate respect. */
	public static DialogueTree enlightenedStage(int entityId) {
		return DialogueTree.builder(SPEAKER, ACOLYTE_ICON, entityId)
				.theme(DialogueTheme.UNSTAINED)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.acolyte.enlightened.line1",
						"hemomancy.acolyte.enlightened.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}
}
