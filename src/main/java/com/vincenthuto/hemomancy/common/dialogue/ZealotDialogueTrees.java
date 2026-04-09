package com.vincenthuto.hemomancy.common.dialogue;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

/**
 * Static factory that produces the {@link DialogueTree} variants used by
 * the Unstained Zealot entity depending on the player's progression state.
 */
public final class ZealotDialogueTrees {

	private static final ResourceLocation ZEALOT_ICON = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/unstained_zealot/unstained_zealot.png");
	private static final String SPEAKER = "entity.hemomancy.unstained_zealot";

	private ZealotDialogueTrees() {}

	/** Player has active blood at VOTARY+ degree — the full plea with choices. */
	public static DialogueTree pleaDialogue(int entityId) {
		return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.zealot.plea.line1",
						"hemomancy.zealot.plea.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.tell_me_more", "explain", null),
						new DialogueOption("hemomancy.dialogue.zealot.option.not_interested", "reject", "zealot_reject_help")
				)))
				.addNode(new DialogueNode("explain", List.of(
						"hemomancy.zealot.plea.line3",
						"hemomancy.zealot.plea.line4"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.accept_purification", null, "zealot_accept_purification"),
						new DialogueOption("hemomancy.dialogue.zealot.option.accept_church", null, "zealot_accept_church"),
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.addNode(new DialogueNode("reject", List.of(
						"hemomancy.dialogue.zealot.reject_response"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	/** Player is already on the purification path. */
	public static DialogueTree alreadyOnPath(int entityId) {
		return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.zealot.already_on_path"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	/** Player has no blood magic active. */
	public static DialogueTree noBlood(int entityId) {
		return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.zealot.no_blood"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	/** Player is initiated but not yet VOTARY. */
	public static DialogueTree tooEarly(int entityId) {
		return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.zealot.too_early"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}

	/** Player is uninitiated (degree 0) with active blood. */
	public static DialogueTree uninitiated(int entityId) {
		return DialogueTree.builder(SPEAKER, ZEALOT_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.zealot.uninitiated"
				), List.of(
						new DialogueOption("hemomancy.dialogue.zealot.option.leave", null, null)
				)))
				.build();
	}
}
