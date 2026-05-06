package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Static factory that produces {@link DialogueTree} variants for the
 * Rite of Ancestral Communion — a degree-7 grand rite that opens a channel
 * to the ancient fungal consciousness. Each invocation produces a different
 * variant from a pool, providing unique lore revelations.
 * <p>
 * The speaker is the same anonymous entity as the fungal whispers ("???").
 */
public final class AncestralCommunionDialogueTrees {

	private static final ResourceLocation MYSTERY_ICON = Hemomancy.rloc("textures/gui/mystery_speaker.png");
	private static final String SPEAKER = "hemomancy.communion.speaker_name";

	/** Number of unique communion dialogue variants. */
	public static final int VARIANT_COUNT = 5;

	private AncestralCommunionDialogueTrees() {}

	/**
	 * Returns a dialogue tree for the given variant index (0–4).
	 * Each variant reveals different lore about the fungal origins of hemomancy.
	 */
	public static DialogueTree forVariant(int variant) {
		return switch (variant % VARIANT_COUNT) {
			case 0 -> communionVariant0();
			case 1 -> communionVariant1();
			case 2 -> communionVariant2();
			case 3 -> communionVariant3();
			default -> communionVariant4();
		};
	}

	// ── Variant 0: The Origin ──

	private static DialogueTree communionVariant0() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.communion.v0.line1",
						"hemomancy.communion.v0.line2",
						"hemomancy.communion.v0.line3"
				), List.of(
						new DialogueOption("hemomancy.communion.option.tell_me_more", "origin", null),
						new DialogueOption("hemomancy.communion.option.enough", null, "communion_end")
				)))
				.addNode(new DialogueNode("origin", List.of(
						"hemomancy.communion.v0.origin1",
						"hemomancy.communion.v0.origin2"
				), List.of(
						new DialogueOption("hemomancy.communion.option.i_understand", null, "communion_lore_origin")
				)))
				.build();
	}

	// ── Variant 1: The Schism ──

	private static DialogueTree communionVariant1() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.communion.v1.line1",
						"hemomancy.communion.v1.line2"
				), List.of(
						new DialogueOption("hemomancy.communion.option.the_schism", "schism", null),
						new DialogueOption("hemomancy.communion.option.enough", null, "communion_end")
				)))
				.addNode(new DialogueNode("schism", List.of(
						"hemomancy.communion.v1.schism1",
						"hemomancy.communion.v1.schism2",
						"hemomancy.communion.v1.schism3"
				), List.of(
						new DialogueOption("hemomancy.communion.option.i_understand", null, "communion_lore_schism")
				)))
				.build();
	}

	// ── Variant 2: The Infection ──

	private static DialogueTree communionVariant2() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.communion.v2.line1",
						"hemomancy.communion.v2.line2"
				), List.of(
						new DialogueOption("hemomancy.communion.option.tell_me_more", "infection", null),
						new DialogueOption("hemomancy.communion.option.enough", null, "communion_end")
				)))
				.addNode(new DialogueNode("infection", List.of(
						"hemomancy.communion.v2.infection1",
						"hemomancy.communion.v2.infection2"
				), List.of(
						new DialogueOption("hemomancy.communion.option.what_does_it_want", "purpose", null),
						new DialogueOption("hemomancy.communion.option.i_understand", null, "communion_lore_infection")
				)))
				.addNode(new DialogueNode("purpose", List.of(
						"hemomancy.communion.v2.purpose1"
				), List.of(
						new DialogueOption("hemomancy.communion.option.i_understand", null, "communion_lore_infection")
				)))
				.build();
	}

	// ── Variant 3: The Harbingers ──

	private static DialogueTree communionVariant3() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.communion.v3.line1",
						"hemomancy.communion.v3.line2",
						"hemomancy.communion.v3.line3"
				), List.of(
						new DialogueOption("hemomancy.communion.option.tell_me_more", "harbingers", null),
						new DialogueOption("hemomancy.communion.option.enough", null, "communion_end")
				)))
				.addNode(new DialogueNode("harbingers", List.of(
						"hemomancy.communion.v3.harbingers1",
						"hemomancy.communion.v3.harbingers2"
				), List.of(
						new DialogueOption("hemomancy.communion.option.i_understand", null, "communion_lore_harbingers")
				)))
				.build();
	}

	// ── Variant 4: The True Name ──

	private static DialogueTree communionVariant4() {
		return DialogueTree.builder(SPEAKER, MYSTERY_ICON, 0)
				.theme(DialogueTheme.FUNGAL)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.communion.v4.line1",
						"hemomancy.communion.v4.line2"
				), List.of(
						new DialogueOption("hemomancy.communion.option.speak_the_name", "name", null),
						new DialogueOption("hemomancy.communion.option.enough", null, "communion_end")
				)))
				.addNode(new DialogueNode("name", List.of(
						"hemomancy.communion.v4.name1",
						"hemomancy.communion.v4.name2",
						"hemomancy.communion.v4.name3"
				), List.of(
						new DialogueOption("hemomancy.communion.option.i_understand", null, "communion_lore_true_name")
				)))
				.build();
	}
}
