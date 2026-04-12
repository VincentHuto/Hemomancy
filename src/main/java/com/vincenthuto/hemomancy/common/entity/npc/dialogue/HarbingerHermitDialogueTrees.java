package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

/**
 * Static factory that produces {@link DialogueTree} variants for the Harbinger
 * Hermit entity. Dialogue varies based on the player's current initiation
 * degree, providing lore and progressive hints toward hemomancy mastery.
 */
public final class HarbingerHermitDialogueTrees {

	private static final ResourceLocation HERMIT_ICON = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/harbinger_hermit/harbinger_hermit.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_hermit";

	private HarbingerHermitDialogueTrees() {}

	/**
	 * Returns the appropriate dialogue tree for the player's progression state.
	 *
	 * @param degree       The player's current initiatory degree number (0–7).
	 * @param hasBlood     Whether the player has active blood magic.
	 * @param entityId     The entity id of the hermit being spoken to.
	 */
	public static DialogueTree forDegree(int degree, boolean hasBlood, int entityId) {
		if (!hasBlood) {
			return noBlood(entityId);
		}
		return switch (degree) {
			case 0 -> uninitiated(entityId);
			case 1 -> neophyte(entityId);
			case 2 -> votary(entityId);
			case 3 -> initiate(entityId);
			case 4 -> adept(entityId);
			case 5 -> illuminatus(entityId);
			case 6 -> sanctified(entityId);
			default -> archon(entityId); // degree 7+
		};
	}

	/** Player has no blood magic active — the hermit senses nothing. */
	public static DialogueTree noBlood(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.hermit.no_blood"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 0 — uninitiated but has active blood. The hermit offers guidance. */
	public static DialogueTree uninitiated(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.hermit.uninitiated.line1",
						"hemomancy.hermit.uninitiated.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.how_do_i_begin", "guidance", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.what_is_this_place", "temple_lore", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("guidance", List.of(
						"hemomancy.hermit.uninitiated.guidance"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.accept_guidance", null, "hermit_accept_guidance"),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("temple_lore", List.of(
						"hemomancy.hermit.temple_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 1 — Neophyte. The hermit acknowledges the first step. */
	public static DialogueTree neophyte(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.hermit.neophyte.line1",
						"hemomancy.hermit.neophyte.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.what_next", "hint", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.tell_me_about_manipulations", "manip_lore", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("hint", List.of(
						"hemomancy.hermit.neophyte.hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("manip_lore", List.of(
						"hemomancy.hermit.manip_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 2 — Votary. Deeper guidance on blood rites and tendencies. */
	public static DialogueTree votary(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.hermit.votary.line1",
						"hemomancy.hermit.votary.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.what_next", "hint", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.tell_me_about_tendencies", "tendency_lore", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("hint", List.of(
						"hemomancy.hermit.votary.hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("tendency_lore", List.of(
						"hemomancy.hermit.tendency_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 3 — Initiate. Guidance toward advanced rites. */
	public static DialogueTree initiate(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.hermit.initiate.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.what_next", "hint", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("hint", List.of(
						"hemomancy.hermit.initiate.hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 4 — Adept. The hermit introduces rune crafting and its deeper meaning. */
	public static DialogueTree adept(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.hermit.adept.line1",
						"hemomancy.hermit.adept.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.tell_me_about_runes", "rune_intro", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.what_next", "hint", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("rune_intro", List.of(
						"hemomancy.hermit.adept.rune_intro"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.how_do_runes_work", "rune_lore", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("rune_lore", List.of(
						"hemomancy.hermit.adept.rune_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.rune_crafting", "rune_crafting", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("rune_crafting", List.of(
						"hemomancy.hermit.adept.rune_crafting"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("hint", List.of(
						"hemomancy.hermit.adept.hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.tell_me_about_runes", "rune_intro", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 5 — Illuminatus. The hermit reveals the existence of the Crimson Lodge. */
	public static DialogueTree illuminatus(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.hermit.illuminatus.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.what_next", "hint", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("hint", List.of(
						"hemomancy.hermit.illuminatus.hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 6 — Sanctified. The hermit speaks with reverence. */
	public static DialogueTree sanctified(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.hermit.sanctified.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.what_next", "hint", null),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.addNode(new DialogueNode("hint", List.of(
						"hemomancy.hermit.sanctified.hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 7 — Archon. The hermit kneels before a master. */
	public static DialogueTree archon(int entityId) {
		return DialogueTree.builder(SPEAKER, HERMIT_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.hermit.archon.line1",
						"hemomancy.hermit.archon.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.hermit.option.share_wisdom", null, "hermit_archon_wisdom"),
						new DialogueOption("hemomancy.dialogue.hermit.option.leave", null, null)
				)))
				.build();
	}
}
