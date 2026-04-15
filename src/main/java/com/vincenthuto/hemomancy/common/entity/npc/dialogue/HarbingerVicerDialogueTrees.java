package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.resources.ResourceLocation;

/**
 * Static factory that produces {@link DialogueTree} variants for the Harbinger
 * Vicer entity. Dialogue focuses on lore, faction history, and progression-based
 * hints toward higher initiatory degrees, with depth of knowledge gated by the
 * player's current degree.
 */
public final class HarbingerVicerDialogueTrees {

	private static final ResourceLocation VICER_ICON = new ResourceLocation(Hemomancy.MOD_ID,
			"textures/entity/harbinger_vicer/harbinger_vicer.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_vicer";

	private HarbingerVicerDialogueTrees() {}

	/**
	 * Returns the appropriate dialogue tree for the player's progression state.
	 *
	 * @param degree   The player's current initiatory degree number (0–7).
	 * @param entityId The entity id of the vicer being spoken to.
	 */
	public static DialogueTree forDegree(int degree, int entityId) {
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

	/**
	 * Dialogue for a player who has begun purification — straying from the blood
	 * path. The Vicar delivers a stern warning and laments the loss of their power.
	 */
	public static DialogueTree purifying(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.purifying.line1",
						"hemomancy.vicer.purifying.line2",
						"hemomancy.vicer.purifying.line3"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.purifying.i_know", "power_warning", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.purifying.i_chose_this", "chosen_path", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("power_warning", List.of(
						"hemomancy.vicer.purifying.power_warning"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("chosen_path", List.of(
						"hemomancy.vicer.purifying.chosen_path"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 0 — uninitiated. The vicer acknowledges the newcomer with measured curiosity. */
	public static DialogueTree uninitiated(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.uninitiated.line1",
						"hemomancy.vicer.uninitiated.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.who_are_harbingers", "harbinger_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.what_is_outpost", "outpost_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("harbinger_lore", List.of(
						"hemomancy.vicer.uninitiated.harbinger_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.what_is_outpost", "outpost_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("outpost_lore", List.of(
						"hemomancy.vicer.uninitiated.outpost_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 1 — Neophyte. The vicer introduces the Hematic Covenant and the degree path. */
	public static DialogueTree neophyte(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.neophyte.line1",
						"hemomancy.vicer.neophyte.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.tell_me_about_covenant", "covenant_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("covenant_lore", List.of(
						"hemomancy.vicer.covenant_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicer.neophyte.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 2 — Votary. The vicer shares lore about blood tendencies and faction history. */
	public static DialogueTree votary(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.votary.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.tell_me_about_tendencies", "tendency_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("tendency_lore", List.of(
						"hemomancy.vicer.votary.tendency_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicer.votary.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 3 — Initiate. The vicer reveals the history of the Scarlet Sanctum. */
	public static DialogueTree initiate(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.initiate.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.tell_me_about_sanctum", "sanctum_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("sanctum_lore", List.of(
						"hemomancy.vicer.initiate.sanctum_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicer.initiate.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 4 — Adept. The vicer speaks of the Sanguine Brotherhood and blood bonds. */
	public static DialogueTree adept(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.adept.line1",
						"hemomancy.vicer.adept.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.tell_me_about_brotherhood", "brotherhood_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("brotherhood_lore", List.of(
						"hemomancy.vicer.adept.brotherhood_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicer.adept.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 5 — Illuminatus. The vicer reveals the legend of the Crimson Lodge. */
	public static DialogueTree illuminatus(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.illuminatus.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.tell_me_about_crimson_lodge", "lodge_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("lodge_lore", List.of(
						"hemomancy.vicer.illuminatus.lodge_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicer.illuminatus.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 6 — Sanctified. The vicer shares ancient doctrine about the Hematic Order. */
	public static DialogueTree sanctified(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.sanctified.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.tell_me_about_hematic_order", "hematic_order_lore", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("hematic_order_lore", List.of(
						"hemomancy.vicer.sanctified.hematic_order_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicer.sanctified.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 7 — Archon. The vicer bows and offers a final piece of hidden lore. */
	public static DialogueTree archon(int entityId) {
		return DialogueTree.builder(SPEAKER, VICER_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicer.archon.line1",
						"hemomancy.vicer.archon.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.tell_me_the_final_truth", "final_truth", null),
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("final_truth", List.of(
						"hemomancy.vicer.archon.final_truth"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicer.option.leave", null, null)
				)))
				.build();
	}
}
