package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import com.vincenthuto.hemomancy.common.item.memories.HematicMemoryItem;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Static factory that produces {@link DialogueTree} variants for the Harbinger
 * Vicar entity. Dialogue focuses on lore, faction history, and progression-based
 * hints toward higher initiatory degrees, with depth of knowledge gated by the
 * player's current degree.
 */
public final class HarbingerVicarDialogueTrees {

	private static final ResourceLocation VICAR_ICON = Hemomancy.rloc("textures/entity/harbinger_vicar/harbinger_vicar.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_vicar";

	private HarbingerVicarDialogueTrees() {}

	/**
	 * Returns the appropriate dialogue tree for the player's progression state.
	 *
	 * @param degree       The player's current initiatory degree number (0–7).
	 * @param entityId     The entity id of the vicar being spoken to.
	 * @param hasBloodline Whether the player has an established bloodline. Recruit
	 *                     and expel options are only shown when this is true.
	 */
	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline) {
		return switch (degree) {
			case 0 -> uninitiated(entityId);
			case 1 -> neophyte(entityId);
			case 2 -> votary(entityId);
			case 3 -> initiate(entityId);
			case 4 -> adept(entityId);
			case 5 -> illuminatus(entityId, hasBloodline);
			case 6 -> sanctified(entityId, hasBloodline);
			case 7 -> archon(entityId, hasBloodline);
			default -> apotheos(entityId, hasBloodline); // degree 8+
		};
	}

	/**
	 * Dialogue for a player who has begun purification — straying from the blood
	 * path. The Vicar delivers a stern warning and laments the loss of their power.
	 */
	public static DialogueTree purifying(int entityId) {
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.purifying.line1",
						"hemomancy.vicar.purifying.line2",
						"hemomancy.vicar.purifying.line3"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.purifying.i_know", "power_warning", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.purifying.i_chose_this", "chosen_path", null),
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("power_warning", List.of(
						"hemomancy.vicar.purifying.power_warning"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("chosen_path", List.of(
						"hemomancy.vicar.purifying.chosen_path"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 0 — uninitiated. The vicar acknowledges the newcomer with measured curiosity. */
	public static DialogueTree uninitiated(int entityId) {
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.uninitiated.line1",
						"hemomancy.vicar.uninitiated.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.who_are_harbingers", "harbinger_lore", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.what_is_outpost", "outpost_lore", null),
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("harbinger_lore", List.of(
						"hemomancy.vicar.uninitiated.harbinger_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.what_is_outpost", "outpost_lore", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("outpost_lore", List.of(
						"hemomancy.vicar.uninitiated.outpost_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 1 — Neophyte. The vicar introduces the Hematic Covenant and the degree path. */
	public static DialogueTree neophyte(int entityId) {
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.neophyte.line1",
						"hemomancy.vicar.neophyte.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_covenant", "covenant_lore", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null),
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("covenant_lore", List.of(
						"hemomancy.vicar.covenant_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicar.neophyte.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 2 — Votary. The vicar shares lore about blood tendencies and faction history. */
	public static DialogueTree votary(int entityId) {
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.votary.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_tendencies", "tendency_lore", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null),
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("tendency_lore", List.of(
						"hemomancy.vicar.votary.tendency_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicar.votary.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 3 — Initiate. The vicar reveals the history of the Scarlet Sanctum and directs the player toward the Saints. */
	public static DialogueTree initiate(int entityId) {
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.initiate.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_sanctum", "sanctum_lore", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_the_saints", "saints_lore", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null),
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("sanctum_lore", List.of(
						"hemomancy.vicar.initiate.sanctum_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_the_saints", "saints_lore", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("saints_lore", List.of(
						"hemomancy.vicar.initiate.saints_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicar.initiate.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 4 — Adept. The vicar speaks of the Sanguine Brotherhood and blood bonds. */
	public static DialogueTree adept(int entityId) {
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.adept.line1",
						"hemomancy.vicar.adept.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_brotherhood", "brotherhood_lore", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null),
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("brotherhood_lore", List.of(
						"hemomancy.vicar.adept.brotherhood_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicar.adept.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/**
	 * Degree 5 — Illuminatus. The vicar reveals the legend of the Crimson Lodge,
	 * shares vague rumors of an ancient rite harbingers of old used to ascend further
	 * (handing the player a parchment fragment), explains the Founding Sanctum and
	 * Quintessence, and offers the degree-advancement hint toward Sanctified.
	 */
	public static DialogueTree illuminatus(int entityId, boolean hasBloodline) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_crimson_lodge", "lodge_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_founding_sanctum", "founding_sanctum_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.about_the_monolith", "monolith_rumor", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null));
		if (hasBloodline) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger"));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.illuminatus.line1"
				), greetingOptions))
				.addNode(new DialogueNode("lodge_lore", List.of(
						"hemomancy.vicar.illuminatus.lodge_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_founding_sanctum", "founding_sanctum_lore", null),
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("founding_sanctum_lore", List.of(
						"hemomancy.vicar.illuminatus.founding_sanctum_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("monolith_rumor", List.of(
						"hemomancy.vicar.illuminatus.monolith_rumor.line1",
						"hemomancy.vicar.illuminatus.monolith_rumor.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.take_the_scrap", "scrap_given", "give_blood_structure_hint"),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("scrap_given", List.of(
						"hemomancy.vicar.illuminatus.scrap_given"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicar.illuminatus.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("recruit_offer", List.of(
						"hemomancy.dialogue.recruit.vicar.consider",
						"hemomancy.dialogue.recruit.vicar.accept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
						new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/**
	 * Degree 6 — Sanctified. The vicar shares ancient doctrine about the Hematic Order.
	 * The Monolith guides the player from here; the Vicar offers the final degree hint
	 * toward Archon as the last piece of active counsel they can give.
	 */
	public static DialogueTree sanctified(int entityId, boolean hasBloodline) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_about_hematic_order", "hematic_order_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null));
		if (hasBloodline) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger"));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.sanctified.line1"
				), greetingOptions))
				.addNode(new DialogueNode("hematic_order_lore", List.of(
						"hemomancy.vicar.sanctified.hematic_order_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.what_degree_next", "degree_hint", null),
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("degree_hint", List.of(
						"hemomancy.vicar.sanctified.degree_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("recruit_offer", List.of(
						"hemomancy.dialogue.recruit.vicar.consider",
						"hemomancy.dialogue.recruit.vicar.accept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
						new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/**
	 * Degree 7 — Archon. The vicar bows and offers a humble admission: the Monolith
	 * carried the player beyond what the Vicar's doctrine could reach.
	 */
	public static DialogueTree archon(int entityId, boolean hasBloodline) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.tell_me_the_final_truth", "final_truth", null));
		if (hasBloodline) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger"));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.archon.line1",
						"hemomancy.vicar.archon.line2"
				), greetingOptions))
				.addNode(new DialogueNode("final_truth", List.of(
						"hemomancy.vicar.archon.final_truth"
				), List.of(
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("recruit_offer", List.of(
						"hemomancy.dialogue.recruit.vicar.consider",
						"hemomancy.dialogue.recruit.vicar.accept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
						new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/**
	 * Special Archon dialogue triggered when the player has an active Qliphoth
	 * Pome empowerment. The Vicar is briefly, visibly unsettled — the only time
	 * the player will ever see him like this.
	 */
	public static DialogueTree archonPomeEmpowered(int entityId) {
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.archon.pome_empowered.line1",
						"hemomancy.vicar.archon.pome_empowered.line2"
				), List.of(
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/**
	 * Item inquiry — responds to items held by the player. The registry
	 * ({@code data/.../dialogue_inquiry/vicar/}) is consulted first; items
	 * not present there fall back to {@link #vicarUnknownInquiry}.
	 * <p>
	 * {@link HematicMemoryItem} instances share a single response regardless of
	 * which manipulation they encode, so they are resolved here in code rather
	 * than requiring one JSON file per memory item.
	 *
	 * @param item     The ItemStack the player is holding.
	 * @param degree   The player's current initiatory degree.
	 * @param entityId The entity id of the vicar.
	 */
	public static DialogueTree itemInquiry(ItemStack item, int degree, int entityId) {
		// HematicMemoryItem: class-based check — all memory items share one response
		if (item.getItem() instanceof HematicMemoryItem) {
			return memoryInquiry(entityId);
		}
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
		return ItemInquiryRegistry.INSTANCE
				.resolve("vicar", itemId, degree, 0f)
				.map(lines -> basicItemInquiry(entityId, lines.toArray(String[]::new)))
				.orElseGet(() -> vicarUnknownInquiry(entityId));
	}

	private static DialogueTree memoryInquiry(int entityId) {
		return basicItemInquiry(entityId,
				"hemomancy.vicar.item_inquiry.hematic_memory.line1",
				"hemomancy.vicar.item_inquiry.hematic_memory.line2");
	}

	private static DialogueTree vicarUnknownInquiry(int entityId) {
		return basicItemInquiry(entityId,
				"hemomancy.vicar.item_inquiry.unknown");
	}

	private static DialogueTree basicItemInquiry(int entityId, String... lines) {
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(lines), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}

	/**
	 * Degree 8 — Apotheos. The Vicar has no doctrine left to offer. They bear witness
	 * to what the player has become and can only reflect on what the Covenant was always
	 * pointing toward — without understanding it fully themselves.
	 */
	public static DialogueTree apotheos(int entityId, boolean hasBloodline) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.what_do_you_make_of_this", "reflection", null));
		if (hasBloodline) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger"));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.vicar.apotheos.line1",
						"hemomancy.vicar.apotheos.line2"
				), greetingOptions))
				.addNode(new DialogueNode("reflection", List.of(
						"hemomancy.vicar.apotheos.reflection"
				), List.of(
      new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.addNode(new DialogueNode("recruit_offer", List.of(
						"hemomancy.dialogue.recruit.vicar.consider",
						"hemomancy.dialogue.recruit.vicar.accept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
						new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.vicar.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
				)))
				.build();
	}
}
