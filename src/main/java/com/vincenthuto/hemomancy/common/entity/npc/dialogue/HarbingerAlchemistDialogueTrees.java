package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Static factory that produces {@link DialogueTree} variants for the Harbinger
 * Alchemist entity. Dialogue focuses on the machines, crafting stations, and
 * functional systems available to Harbinger members, with knowledge gated by
 * the player's current initiatory degree.
 */
public final class HarbingerAlchemistDialogueTrees {

	private static final ResourceLocation ALCHEMIST_ICON = Hemomancy.rloc("textures/entity/harbinger_alchemist/harbinger_alchemist.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_alchemist";

	private HarbingerAlchemistDialogueTrees() {}

	/**
	 * Returns the appropriate dialogue tree for the player's progression state.
	 *
	 * @param degree       The player's current initiatory degree number (0–7).
	 * @param entityId     The entity id of the alchemist being spoken to.
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
	 * Dialogue for a player who has begun purification — abandoning the blood path.
	 * The Alchemist dismisses them: no time for someone who won't use the knowledge.
	 */
	public static DialogueTree purifying(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.purifying.line1",
						"hemomancy.alchemist.purifying.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.purifying.i_can_explain", "explain", null),
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("explain", List.of(
						"hemomancy.alchemist.purifying.explain"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/**
	 * Dialogue for a player who has attained Clarity — fully committed to the Unstained
	 * path. The Alchemist gives them the cold shoulder: no engagement, no teaching.
	 */
	public static DialogueTree clarity(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.clarity.line1"
				), List.of(
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 0 — uninitiated. The alchemist politely explains that machines require initiation. */
	public static DialogueTree uninitiated(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.uninitiated.line1",
						"hemomancy.alchemist.uninitiated.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.what_machines", "machines_locked", null),
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("machines_locked", List.of(
						"hemomancy.alchemist.uninitiated.machines_locked"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 1 — Neophyte. Introduces the Ghastly Alembic and basic blood processing. */
	public static DialogueTree neophyte(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.neophyte.line1",
						"hemomancy.alchemist.neophyte.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_alembic", "alembic_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_gourds", "blood_gourd_basics",
								null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_machines", "machines_overview", null),
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("alembic_lore", List.of(
						"hemomancy.alchemist.neophyte.alembic_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_gourds", "blood_gourd_basics",
								null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("blood_gourd_basics", List.of(
						"hemomancy.alchemist.neophyte.blood_gourd_basics"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_machines", "machines_overview", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("machines_overview", List.of(
						"hemomancy.alchemist.machines_overview"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_alembic", "alembic_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_gourds", "blood_gourd_basics",
								null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 2 — Votary. Explains the Vial Centrifuge, blood tendency separation, and introduces blood structure crafting. */
	public static DialogueTree votary(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.votary.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge", "centrifuge_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd", "gourd_upgrades", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_alembic", "alembic_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_structures", "blood_structure_intro", null),
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("centrifuge_lore", List.of(
						"hemomancy.alchemist.votary.centrifuge_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd", "gourd_upgrades", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("gourd_upgrades", List.of(
						"hemomancy.alchemist.votary.gourd_upgrades"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("alembic_lore", List.of(
						"hemomancy.alchemist.neophyte.alembic_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd", "gourd_upgrades", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("blood_structure_intro", List.of(
						"hemomancy.alchemist.votary.blood_structure_intro"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 3 — Initiate. Reveals the Somatic Loom and memory weaving. */
	public static DialogueTree initiate(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.initiate.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_loom", "loom_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.what_is_memory_weaving", "memory_weaving", null),
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("loom_lore", List.of(
						"hemomancy.alchemist.initiate.loom_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.what_is_memory_weaving", "memory_weaving", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("memory_weaving", List.of(
						"hemomancy.alchemist.initiate.memory_weaving"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 4 — Adept. Introduces the Cerebral Scarring Station and the Chisel Station. */
	public static DialogueTree adept(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.adept.line1",
						"hemomancy.alchemist.adept.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_scar_station", "scar_station_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_chisel_station", "chisel_lore", null),
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("scar_station_lore", List.of(
						"hemomancy.alchemist.adept.scar_station_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_chisel_station", "chisel_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("chisel_lore", List.of(
						"hemomancy.alchemist.adept.chisel_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 5 — Illuminatus. Speaks of advanced blood crafting and cardinal rite machines. */
	public static DialogueTree illuminatus(int entityId, boolean hasBloodline) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_crafting", "blood_crafting_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_morphling_incubator", "incubator_lore", null));
		if (hasBloodline) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger"));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.illuminatus.line1"
				), greetingOptions))
				.addNode(new DialogueNode("blood_crafting_lore", List.of(
						"hemomancy.alchemist.illuminatus.blood_crafting_lore"
				), List.of(
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("incubator_lore", List.of(
						"hemomancy.alchemist.illuminatus.incubator_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("recruit_offer", List.of(
						"hemomancy.dialogue.recruit.alchemist.consider",
						"hemomancy.dialogue.recruit.alchemist.accept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
						new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 6 — Sanctified. The alchemist speaks of the pinnacle of Harbinger engineering. */
	public static DialogueTree sanctified(int entityId, boolean hasBloodline) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.what_remains", "final_machines", null));
		if (hasBloodline) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger"));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.sanctified.line1"
				), greetingOptions))
				.addNode(new DialogueNode("final_machines", List.of(
						"hemomancy.alchemist.sanctified.final_machines"
				), List.of(
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("recruit_offer", List.of(
						"hemomancy.dialogue.recruit.alchemist.consider",
						"hemomancy.dialogue.recruit.alchemist.accept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
						new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 7 — Archon. The alchemist defers to the player's mastery. */
	public static DialogueTree archon(int entityId, boolean hasBloodline) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		if (hasBloodline) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger"));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.archon.line1",
						"hemomancy.alchemist.archon.line2"
				), greetingOptions))
				.addNode(new DialogueNode("recruit_offer", List.of(
						"hemomancy.dialogue.recruit.alchemist.consider",
						"hemomancy.dialogue.recruit.alchemist.accept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
						new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
				)))
.addNode(new DialogueNode("item_hint", List.of(
		"hemomancy.alchemist.item_hint"
), List.of(
		new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
)))
				.build();
	}

	/**
	 * Item inquiry for the Alchemist. Responds to Harbinger crafting items and machines.
	 * Hemolytic vial and cleansing hemolymph get clinical chemistry responses with no
	 * mention of Unstained NPCs. Degree gates apply to Loom (3+), Scar Station (4+),
	 * and Morphling Incubator (5+). Unknown/Unstained items dismissed professionally.
	 */
	public static DialogueTree itemInquiry(ItemStack item, int degree, int entityId) {
		Item it = item.getItem();
		if (it == ItemInit.vivacious_enzyme.get()) return enzymeInquiry("vivacious", entityId);
		if (it == ItemInit.fervent_enzyme.get()) return enzymeInquiry("fervent", entityId);
		if (it == ItemInit.neurotic_enzyme.get()) return enzymeInquiry("neurotic", entityId);
		if (it == ItemInit.incandescent_enzyme.get()) return enzymeInquiry("incandescent", entityId);
		if (it == ItemInit.ruinous_enzyme.get()) return enzymeInquiry("ruinous", entityId);
		if (it == ItemInit.frigid_enzyme.get()) return enzymeInquiry("frigid", entityId);
		if (it == ItemInit.ferric_enzyme.get()) return enzymeInquiry("ferric", entityId);
		if (it == ItemInit.umbral_enzyme.get()) return enzymeInquiry("umbral", entityId);
		if (it == ItemInit.recycled_enzyme.get()) return recycledEnzymeInquiry(entityId);
		if (it == ItemInit.bloody_vial.get()) return bloodVialInquiry(entityId);
		if (it == ItemInit.blood_gourd_white.get()
				|| it == ItemInit.blood_gourd_red.get()
				|| it == ItemInit.blood_gourd_black.get()) return bloodGourdInquiry(entityId);
		if (it == ItemInit.foul_paste.get()) return foulPasteInquiry(entityId);
		if (it == BlockInit.ghastly_alembic.get().asItem()) return alembicItemInquiry(entityId);
		if (it == BlockInit.vial_centrifuge.get().asItem()) return centrifugeItemInquiry(entityId);
		if (it == BlockInit.somatic_loom.get().asItem()) return loomItemInquiry(degree, entityId);
		if (it == BlockInit.scar_station.get().asItem()) return scarStationItemInquiry(degree, entityId);
		if (it == BlockInit.morphling_incubator.get().asItem()) return incubatorItemInquiry(degree, entityId);
		if (it == ItemInit.hemolytic_vial.get()) return alchemistHemolyticInquiry(entityId);
		if (it == ItemInit.cleansing_hemolymph.get()) return cleansingHemolymphInquiry(entityId);
		return alchemistUnknownInquiry(entityId);
	}

	private static DialogueTree enzymeInquiry(String tendency, int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.enzyme_" + tendency + ".line1",
						"hemomancy.alchemist.item_inquiry.enzyme_" + tendency + ".line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	private static DialogueTree recycledEnzymeInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.recycled_enzyme.line1",
						"hemomancy.alchemist.item_inquiry.recycled_enzyme.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree bloodVialInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.blood_vial.line1",
						"hemomancy.alchemist.item_inquiry.blood_vial.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree bloodGourdInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.blood_gourd.line1",
						"hemomancy.alchemist.item_inquiry.blood_gourd.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree foulPasteInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.foul_paste.line1",
						"hemomancy.alchemist.item_inquiry.foul_paste.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree alembicItemInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.ghastly_alembic.line1",
						"hemomancy.alchemist.item_inquiry.ghastly_alembic.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree centrifugeItemInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.vial_centrifuge.line1",
						"hemomancy.alchemist.item_inquiry.vial_centrifuge.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree loomItemInquiry(int degree, int entityId) {
		if (degree < 3) {
			return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
					.addNode(new DialogueNode("root", List.of("hemomancy.alchemist.item_inquiry.somatic_loom.locked"),
       new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
							List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
					.build();
		}
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.somatic_loom.line1",
						"hemomancy.alchemist.item_inquiry.somatic_loom.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree scarStationItemInquiry(int degree, int entityId) {
		if (degree < 4) {
			return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
					.addNode(new DialogueNode("root", List.of("hemomancy.alchemist.item_inquiry.scar_station.locked"),
       new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
							List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
					.build();
		}
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.scar_station.line1",
						"hemomancy.alchemist.item_inquiry.scar_station.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree incubatorItemInquiry(int degree, int entityId) {
		if (degree < 5) {
			return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
					.addNode(new DialogueNode("root", List.of("hemomancy.alchemist.item_inquiry.morphling_incubator.locked"),
       new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
							List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
					.build();
		}
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.morphling_incubator.line1",
						"hemomancy.alchemist.item_inquiry.morphling_incubator.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree alchemistHemolyticInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.hemolytic_vial.line1",
						"hemomancy.alchemist.item_inquiry.hemolytic_vial.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree cleansingHemolymphInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.cleansing_hemolymph.line1",
						"hemomancy.alchemist.item_inquiry.cleansing_hemolymph.line2"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	private static DialogueTree alchemistUnknownInquiry(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(
						"hemomancy.alchemist.item_inquiry.unknown"
    new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.build();
	}

	/** Degree 8 — Apotheos. The alchemist witnesses something beyond their framework. */
	public static DialogueTree apotheos(int entityId, boolean hasBloodline) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.what_do_you_see", "reflection", null));
		if (hasBloodline) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger"));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.apotheos.line1",
						"hemomancy.alchemist.apotheos.line2"
				), greetingOptions))
				.addNode(new DialogueNode("reflection", List.of(
						"hemomancy.alchemist.apotheos.reflection"
				), List.of(
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("recruit_offer", List.of(
						"hemomancy.dialogue.recruit.alchemist.consider",
						"hemomancy.dialogue.recruit.alchemist.accept"
				), List.of(
						new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
						new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
				)))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}
}
