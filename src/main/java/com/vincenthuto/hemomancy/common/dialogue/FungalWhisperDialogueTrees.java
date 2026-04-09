package com.vincenthuto.hemomancy.common.dialogue;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

/**
 * Static factory that produces {@link DialogueTree} variants for the
 * mysterious "fungal whisper" events that plague players at high initiation
 * tiers (5–7). Each tier receives progressively more disturbing revelations
 * about the true fungal origins of hemomancy.
 * <p>
 * The speaker is anonymous ("???") with a mystery question-mark icon.
 */
public final class FungalWhisperDialogueTrees {

	private static final ResourceLocation MYSTERY_ICON = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/gui/mystery_speaker.png");
	private static final String SPEAKER = "hemomancy.whisper.speaker_name";

	private FungalWhisperDialogueTrees() {}

	/**
	 * Returns a dialogue tree for the given degree. Only tiers 5–7 have
	 * whisper content; lower tiers should never call this.
	 *
	 * @param degree   The player's current initiatory degree (5, 6, or 7).
	 * @param variant  A variant index (0–2) for message variety within a tier.
	 */
	public static DialogueTree forDegree(int degree, int variant) {
		// entityId of 0 means no entity — this is a disembodied voice
		return switch (degree) {
			case 5 -> illuminatusWhisper(variant);
			case 6 -> sanctifiedWhisper(variant);
			default -> archonWhisper(variant); // degree 7+
		};
	}

	// ── Degree 5 — Illuminatus whispers: first hints of something fungal ──

	private static DialogueTree illuminatusWhisper(int variant) {
		return switch (variant) {
			case 0 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.illuminatus.v0.line1",
							"hemomancy.whisper.illuminatus.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.what_was_that", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, "whisper_dismiss")
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.illuminatus.v0.follow"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			case 1 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.illuminatus.v1.line1",
							"hemomancy.whisper.illuminatus.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			default -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.illuminatus.v2.line1"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.who_are_you", "reveal", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("reveal", List.of(
							"hemomancy.whisper.illuminatus.v2.reveal"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── Degree 6 — Sanctified whispers: clearer fungal revelations ──

	private static DialogueTree sanctifiedWhisper(int variant) {
		return switch (variant) {
			case 0 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.sanctified.v0.line1",
							"hemomancy.whisper.sanctified.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.what_was_that", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, "whisper_dismiss")
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.sanctified.v0.follow"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			case 1 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.sanctified.v1.line1",
							"hemomancy.whisper.sanctified.v1.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.who_are_you", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.sanctified.v1.follow"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			default -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.sanctified.v2.line1",
							"hemomancy.whisper.sanctified.v2.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
		};
	}

	// ── Degree 7 — Archon whispers: the full truth emerges ──

	private static DialogueTree archonWhisper(int variant) {
		return switch (variant) {
			case 0 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.archon.v0.line1",
							"hemomancy.whisper.archon.v0.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.what_was_that", "follow", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("follow", List.of(
							"hemomancy.whisper.archon.v0.follow"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			case 1 -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.archon.v1.line1",
							"hemomancy.whisper.archon.v1.line2",
							"hemomancy.whisper.archon.v1.line3"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.build();
			default -> DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
					.addNode(new DialogueNode("root", List.of(
							"hemomancy.whisper.archon.v2.line1",
							"hemomancy.whisper.archon.v2.line2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.who_are_you", "truth", null),
							new DialogueOption("hemomancy.whisper.option.dismiss", null, null)
					)))
					.addNode(new DialogueNode("truth", List.of(
							"hemomancy.whisper.archon.v2.truth1",
							"hemomancy.whisper.archon.v2.truth2"
					), List.of(
							new DialogueOption("hemomancy.whisper.option.dismiss", null, "whisper_truth_acknowledged")
					)))
					.build();
		};
	}
}
