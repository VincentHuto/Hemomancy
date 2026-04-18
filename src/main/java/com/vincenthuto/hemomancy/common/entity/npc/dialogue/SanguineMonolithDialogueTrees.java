package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

/**
 * Static factory producing {@link DialogueTree} variants for the Sanguine
 * Monolith block. Dialogue varies based on the player's current initiatory
 * degree (5 = Illuminatus through 7+ = Archon).
 */
public final class SanguineMonolithDialogueTrees {

	private static final ResourceLocation MONOLITH_ICON = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/model_sanguine_monolith.png");
	private static final String SPEAKER = "block.hemomancy.sanguine_monolith";

	/** Sentinel used in place of an entity ID for this block-based speaker. */
	public static final int BLOCK_ENTITY_ID = -1;

	private SanguineMonolithDialogueTrees() {}

	/**
	 * Returns the appropriate dialogue tree for the given degree.
	 * Assumes the player is at least degree 4 (Adept).
	 */
	public static DialogueTree forDegree(int degree) {
		return switch (degree) {
			case 4 -> adept();
			case 5 -> illuminatus();
			case 6 -> sanctified();
			default -> archon();
		};
	}

	/** Player is below the minimum degree — the monolith is silent. */
	public static DialogueTree unworthy() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.unworthy"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 4 — Adept. Guidance toward the Sainted Mausoleums. */
	public static DialogueTree adept() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.guidance.adept.line1",
						"hemomancy.monolith.guidance.adept.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.build();
	}

	/** Degree 5 — Illuminatus. Guidance toward the Crimson Lodge. */
	public static DialogueTree illuminatus() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.guidance.illuminatus.line1",
						"hemomancy.monolith.guidance.illuminatus.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.build();
	}

	/** Degree 6 — Sanctified. Guidance toward the Somatic Loom. */
	public static DialogueTree sanctified() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.guidance.sanctified.line1",
						"hemomancy.monolith.guidance.sanctified.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.understood", null, null)
				)))
				.build();
	}

	/** Degree 7+ — Archon. The monolith hints that it is beginning to crack. */
	public static DialogueTree archon() {
		return DialogueTree.builder(SPEAKER, MONOLITH_ICON, BLOCK_ENTITY_ID)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.monolith.guidance.archon.line1",
						"hemomancy.monolith.guidance.archon.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.monolith.option.leave", null, null)
				)))
				.build();
	}
}
