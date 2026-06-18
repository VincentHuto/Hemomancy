package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Static factory that produces {@link DialogueTree} variants for the Harbinger
 * Alchemist entity. Dialogue focuses on the machines, crafting stations, and
 * functional systems available to Harbinger members, with knowledge gated by
 * the player's current initiatory degree.
 */
public final class HarbingerAlchemistDialogueTrees {

	private static final ResourceLocation ALCHEMIST_ICON = Hemomancy.rloc("textures/entity/harbinger_alchemist/harbinger_alchemist.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_alchemist";
	public static final String EVENT_RED_TAXONOMY_PREFIX = "alchemist_red_taxonomy_";

	private HarbingerAlchemistDialogueTrees() {}

	/**
	 * Returns the appropriate dialogue tree for the player's progression state.
	 *
	 * @param degree       The player's current initiatory degree number (0–7).
	 * @param entityId     The entity id of the alchemist being spoken to.
	 * @param hasBloodline    Whether the player has an established bloodline.
	 * @param isNpcRecruited Whether this alchemist has already pledged to the player's bloodline.
	 */
	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, null);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			RedTaxonomySample heldRedTaxonomySample) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, heldRedTaxonomySample, false);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			RedTaxonomySample heldRedTaxonomySample, boolean redTaxonomyComplete) {
		if (degree >= 2 && heldRedTaxonomySample != null) {
			return votary(entityId, heldRedTaxonomySample);
		}
		return switch (degree) {
			case 0 -> uninitiated(entityId);
			case 1 -> neophyte(entityId);
			case 2 -> votary(entityId, heldRedTaxonomySample);
			case 3 -> initiate(entityId, redTaxonomyComplete);
			case 4 -> adept(entityId);
			case 5 -> illuminatus(entityId, hasBloodline, isNpcRecruited);
			case 6 -> sanctified(entityId, hasBloodline, isNpcRecruited);
			case 7 -> archon(entityId, hasBloodline, isNpcRecruited);
			default -> apotheos(entityId, hasBloodline, isNpcRecruited); // degree 8+
		};
	}

	public enum RedTaxonomySample {
		INFECTED_FUNGUS("infected_fungus", BlockInit.infected_fungus.get(),
				HarbingerAdvancementGranter.ADV_RED_TAXONOMY_INFECTED_FUNGUS),
		STINKHORN_FUNGUS("stinkhorn_fungus", BlockInit.stinkhorn_fungus.get(),
				HarbingerAdvancementGranter.ADV_RED_TAXONOMY_STINKHORN_FUNGUS),
		SARCODES("sarcodes", BlockInit.sarcodes.get(),
				HarbingerAdvancementGranter.ADV_RED_TAXONOMY_SARCODES),
		BLEEDING_HEART("bleeding_heart", BlockInit.bleeding_heart.get(),
				HarbingerAdvancementGranter.ADV_RED_TAXONOMY_BLEEDING_HEART),
		RAFFLESIA("rafflesia", BlockInit.rafflesia.get(),
				HarbingerAdvancementGranter.ADV_RED_TAXONOMY_RAFFLESIA),
		DEVILS_TOOTH("devils_tooth", BlockInit.devils_tooth.get(),
				HarbingerAdvancementGranter.ADV_RED_TAXONOMY_DEVILS_TOOTH),
		PUFFBALL_FUNGUS("puffball_fungus", BlockInit.puffball_fungus.get(),
				HarbingerAdvancementGranter.ADV_RED_TAXONOMY_PUFFBALL_FUNGUS);

		private final String key;
		private final Block block;
		private final ResourceLocation advancement;

		RedTaxonomySample(String key, Block block, ResourceLocation advancement) {
			this.key = key;
			this.block = block;
			this.advancement = advancement;
		}

		public String key() {
			return key;
		}

		public Block block() {
			return block;
		}

		public ResourceLocation advancement() {
			return advancement;
		}

		public boolean matches(ItemStack stack) {
			return stack.is(block.asItem());
		}

		public String eventId() {
			return EVENT_RED_TAXONOMY_PREFIX + key;
		}

		public static RedTaxonomySample fromStack(ItemStack stack) {
			for (RedTaxonomySample sample : values()) {
				if (sample.matches(stack)) {
					return sample;
				}
			}
			return null;
		}

		public static RedTaxonomySample fromEventId(String eventId) {
			if (eventId == null || !eventId.startsWith(EVENT_RED_TAXONOMY_PREFIX)) {
				return null;
			}
			String key = eventId.substring(EVENT_RED_TAXONOMY_PREFIX.length());
			for (RedTaxonomySample sample : values()) {
				if (sample.key.equals(key)) {
					return sample;
				}
			}
			return null;
		}
	}

	private static void addRecruitmentOption(List<DialogueOption> options, boolean hasBloodline,
			boolean isNpcRecruited) {
		if (!hasBloodline) {
			return;
		}
		options.add(isNpcRecruited
				? new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger")
				: new DialogueOption("hemomancy.dialogue.recruit.option.pledge_blood", "recruit_offer", null));
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
						new DialogueOption("hemomancy.dialogue.alchemist.option.alembic_leak", "alembic_leak", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("alembic_leak", List.of(
						"hemomancy.alchemist.neophyte.alembic_leak"
				), List.of(
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

	public static DialogueTree votary(int entityId, RedTaxonomySample heldRedTaxonomySample) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		if (heldRedTaxonomySample != null) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.submit_red_taxonomy_sample",
					"red_taxonomy_" + heldRedTaxonomySample.key(), heldRedTaxonomySample.eventId()));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.begin_red_taxonomy",
				"red_taxonomy_intro", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge",
				"centrifuge_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd",
				"gourd_upgrades", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_alembic",
				"alembic_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_structures",
				"blood_structure_intro", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item",
				"item_hint", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));

		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.votary.line1"
				), greetingOptions))
				.addNode(new DialogueNode("red_taxonomy_intro", List.of(
						"hemomancy.alchemist.red_taxonomy.intro.line1",
						"hemomancy.alchemist.red_taxonomy.intro.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge",
								"centrifuge_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("red_taxonomy_infected_fungus", List.of(
						"hemomancy.alchemist.red_taxonomy.infected_fungus.line1",
						"hemomancy.alchemist.red_taxonomy.infected_fungus.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("red_taxonomy_stinkhorn_fungus", List.of(
						"hemomancy.alchemist.red_taxonomy.stinkhorn_fungus.line1",
						"hemomancy.alchemist.red_taxonomy.stinkhorn_fungus.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("red_taxonomy_sarcodes", List.of(
						"hemomancy.alchemist.red_taxonomy.sarcodes.line1",
						"hemomancy.alchemist.red_taxonomy.sarcodes.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("red_taxonomy_bleeding_heart", List.of(
						"hemomancy.alchemist.red_taxonomy.bleeding_heart.line1",
						"hemomancy.alchemist.red_taxonomy.bleeding_heart.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("red_taxonomy_rafflesia", List.of(
						"hemomancy.alchemist.red_taxonomy.rafflesia.line1",
						"hemomancy.alchemist.red_taxonomy.rafflesia.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("red_taxonomy_devils_tooth", List.of(
						"hemomancy.alchemist.red_taxonomy.devils_tooth.line1",
						"hemomancy.alchemist.red_taxonomy.devils_tooth.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("red_taxonomy_puffball_fungus", List.of(
						"hemomancy.alchemist.red_taxonomy.puffball_fungus.line1",
						"hemomancy.alchemist.red_taxonomy.puffball_fungus.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("centrifuge_lore", List.of(
						"hemomancy.alchemist.votary.centrifuge_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd",
								"gourd_upgrades", null),
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
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd",
								"gourd_upgrades", null),
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
		return initiate(entityId, false);
	}

	public static DialogueTree initiate(int entityId, boolean redTaxonomyComplete) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		if (redTaxonomyComplete) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_mnemonist_work",
					"mnemonist_breadcrumb", null));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_loom", "loom_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.what_is_memory_weaving", "memory_weaving", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));

		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.initiate.line1"
				), greetingOptions))
				.addNode(new DialogueNode("mnemonist_breadcrumb", List.of(
						"hemomancy.alchemist.initiate.mnemonist_breadcrumb"
				), List.of(
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
	public static DialogueTree illuminatus(int entityId, boolean hasBloodline, boolean isNpcRecruited) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_crafting", "blood_crafting_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_morphling_incubator", "incubator_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null));
		addRecruitmentOption(greetingOptions, hasBloodline, isNpcRecruited);
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
	public static DialogueTree sanctified(int entityId, boolean hasBloodline, boolean isNpcRecruited) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.what_remains", "final_machines", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null));
		addRecruitmentOption(greetingOptions, hasBloodline, isNpcRecruited);
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
	public static DialogueTree archon(int entityId, boolean hasBloodline, boolean isNpcRecruited) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null));
		addRecruitmentOption(greetingOptions, hasBloodline, isNpcRecruited);
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
	 * Item inquiry for the Alchemist. Degree gates apply to Loom (3+),
	 * Scar Station (4+), and Morphling Incubator (5+) — these conditions are
	 * expressed as {@code min_degree} fields in the JSON entries.
	 * Unknown items receive a professional dismissal.
	 */
	public static DialogueTree itemInquiry(ItemStack item, int degree, int entityId) {
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
		return ItemInquiryRegistry.INSTANCE
				.resolve("alchemist", itemId, degree, 0f)
				.map(lines -> basicItemInquiry(entityId, lines.toArray(String[]::new)))
				.orElseGet(() -> basicItemInquiry(entityId, "hemomancy.alchemist.item_inquiry.unknown"));
	}

	private static DialogueTree basicItemInquiry(int entityId, String... lines) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(lines), List.of(
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

	/** Degree 8 — Apotheos. The alchemist witnesses something beyond their framework. */
	public static DialogueTree apotheos(int entityId, boolean hasBloodline, boolean isNpcRecruited) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.what_do_you_see", "reflection", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null));
		addRecruitmentOption(greetingOptions, hasBloodline, isNpcRecruited);
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
