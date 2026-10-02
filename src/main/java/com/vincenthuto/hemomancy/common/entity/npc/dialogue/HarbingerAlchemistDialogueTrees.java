package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bestiary.SpecimenBestiaryDefinitions;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily;
import static com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.Step;

/**
 * Static factory that produces {@link DialogueTree} variants for the Harbinger
 * Alchemist entity. Dialogue focuses on the machines, crafting stations, and
 * functional systems available to Harbinger members, with knowledge gated by
 * the player's current initiatory degree.
 */
public final class HarbingerAlchemistDialogueTrees {

	private static final ResourceLocation ALCHEMIST_ICON = ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID,
			"textures/entity/npc/harbinger/harbinger_alchemist/harbinger_alchemist.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_alchemist";
	public static final String EVENT_RED_TAXONOMY_PREFIX = "alchemist_red_taxonomy_";
	public static final String EVENT_BESTIARY_RECORD = "alchemist_bestiary_record";
	public static final String EVENT_BESTIARY_SURRENDER = "alchemist_bestiary_surrender";
	public static final String EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX = "alchemist_bestiary_surrender_morphling_";
	public static final String EVENT_FIRST_SEPARATION_BRIEF = "alchemist_first_separation_brief";
	public static final String EVENT_FIRST_SEPARATION_CLAIM = "alchemist_first_separation_claim";
	public static final String EVENT_BODY_ANSWERS_BRIEF = "alchemist_body_answers_brief";
	public static final String EVENT_CLAIM_ARMOR_RESEARCH_REWARD = "alchemist_claim_armor_research_reward";

	private HarbingerAlchemistDialogueTrees() {}

	public static DialogueTree withArtificerCorrespondence(DialogueTree tree, ArtificerProgressSnapshot progress,
			int forkResearchRecorded, boolean forkResearchClaimed) {
		if (progress.purifying() || progress.clarity()) return tree;
		List<DialogueOption> correspondence = new ArrayList<>();
		if (progress.threeAnswers() == Step.CORRESPONDENCE) {
			correspondence.add(new DialogueOption("hemomancy.dialogue.alchemist.option.artificer_three_answers",
					null, HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD,
					DialogueOptionPresentation.prompt("hemomancy.alchemist.artificer_three_answers.prompt")));
		}
		if (progress.crimsonVestment() == Step.CORRESPONDENCE) {
			correspondence.add(new DialogueOption("hemomancy.dialogue.alchemist.option.artificer_crimson_vestment",
					null, HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD,
					DialogueOptionPresentation.prompt("hemomancy.alchemist.artificer_crimson_vestment.prompt")));
		}
		String researchNodeId = null;
		if (hasCompletedForkCorrespondence(progress.threeAnswers())
				&& progress.forkFamily() != ForkFamily.NONE
				&& !forkResearchClaimed) {
			if (forkResearchRecorded >= 3) {
				correspondence.add(new DialogueOption("hemomancy.dialogue.alchemist.option.claim_armor_research",
						null, EVENT_CLAIM_ARMOR_RESEARCH_REWARD,
						DialogueOptionPresentation.prompt("hemomancy.alchemist.armor_research.claim.prompt")));
			} else {
				researchNodeId = "armor_research_" + progress.forkFamily().serializedName();
				correspondence.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_armor_research",
						researchNodeId, null));
			}
		}
		if (correspondence.isEmpty()) return tree;
		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		if (researchNodeId != null) {
			String family = progress.forkFamily().serializedName();
			nodes.put(researchNodeId, new DialogueNode(researchNodeId, List.of(
					"hemomancy.alchemist.armor_research." + family + ".line1",
					"hemomancy.alchemist.armor_research." + family + ".line2"
			), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))));
		}
		DialogueNode start = tree.getStartNode();
		List<DialogueOption> options = new ArrayList<>(start.options());
		options.addAll(Math.max(0, options.size() - 1), correspondence);
		nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), List.copyOf(options)));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	private static boolean hasCompletedForkCorrespondence(Step step) {
		return step == Step.FULL_SET || step == Step.DEMONSTRATION || step == Step.FITTING || step == Step.COMPLETE;
	}

	/**
	 * Returns the appropriate dialogue tree for the player's progression state.
	 *
	 * @param degree       The player's current initiatory degree number (0–7).
	 * @param entityId     The entity id of the alchemist being spoken to.
	 * @param hasBloodline    Whether the player has an established bloodline.
	 * @param isNpcRecruited Whether this alchemist has already pledged to the player's bloodline.
	 */
	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, null, null);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			RedTaxonomySample heldRedTaxonomySample) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, heldRedTaxonomySample, null);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			RedTaxonomySample heldRedTaxonomySample, HeldSpecimenJar heldSpecimenJar) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, heldRedTaxonomySample, heldSpecimenJar,
				false, false, false, false);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			RedTaxonomySample heldRedTaxonomySample, HeldSpecimenJar heldSpecimenJar,
			boolean canBriefFirstSeparation, boolean canClaimFirstSeparation, boolean canBriefBodyAnswers,
			boolean canDiscussMuscleMemories) {
		DialogueTree tree = switch (degree) {
			case 0 -> uninitiated(entityId);
			case 1 -> neophyte(entityId);
			case 2 -> votary(entityId, heldRedTaxonomySample, heldSpecimenJar, canBriefFirstSeparation,
					canClaimFirstSeparation, canBriefBodyAnswers, canDiscussMuscleMemories);
			case 3 -> initiate(entityId);
			case 4 -> adept(entityId);
			case 5 -> illuminatus(entityId, hasBloodline, isNpcRecruited);
			case 6 -> sanctified(entityId, hasBloodline, isNpcRecruited);
			case 7 -> archon(entityId, hasBloodline, isNpcRecruited);
			default -> apotheos(entityId, hasBloodline, isNpcRecruited); // degree 8+
		};
		boolean heldResearch = degree >= 2 && (heldRedTaxonomySample != null || heldSpecimenJar != null);
		if (degree < 1 || degree == 2 || !(heldResearch || canBriefFirstSeparation || canClaimFirstSeparation
				|| canBriefBodyAnswers || canDiscussMuscleMemories)) return tree;
		DialogueTree lessons = votary(entityId, heldResearch ? heldRedTaxonomySample : null,
				heldResearch ? heldSpecimenJar : null, canBriefFirstSeparation,
				canClaimFirstSeparation, canBriefBodyAnswers, canDiscussMuscleMemories);
		var nodes = new java.util.LinkedHashMap<>(tree.nodes());
		lessons.nodes().forEach(nodes::putIfAbsent);
		List<DialogueOption> options = new ArrayList<>();
		for (DialogueOption option : lessons.getStartNode().options()) {
			boolean researchOption = heldResearch && ("living_bestiary_intro".equals(option.nextNodeId())
					|| "red_taxonomy_intro".equals(option.nextNodeId())
					|| EVENT_BESTIARY_RECORD.equals(option.eventId())
					|| EVENT_BESTIARY_SURRENDER.equals(option.eventId())
					|| (option.eventId() != null && option.eventId().startsWith(EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX))
					|| (heldRedTaxonomySample != null && heldRedTaxonomySample.eventId().equals(option.eventId())));
			if (researchOption || "first_separation_offer".equals(option.nextNodeId())
					|| EVENT_FIRST_SEPARATION_BRIEF.equals(option.eventId())
					|| EVENT_FIRST_SEPARATION_CLAIM.equals(option.eventId())
					|| EVENT_BODY_ANSWERS_BRIEF.equals(option.eventId())
					|| "muscle_memory_catalogue".equals(option.nextNodeId())) options.add(option);
		}
		options.addAll(tree.getStartNode().options());
		nodes.put(tree.startNodeId(), new DialogueNode(tree.startNodeId(), tree.getStartNode().lines(), options));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	private static List<DialogueOption> vesselUseOptions() {
		return List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.gourd_use", "gourd_use", null),
				new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
	}

	private static DialogueNode gourdUseLesson() {
		return new DialogueNode("gourd_use", List.of("hemomancy.alchemist.gourd_use.fill",
				"hemomancy.alchemist.gourd_use.withdraw"),
				List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)));
	}

	static DialogueTree withCurrentVesselLesson(DialogueTree tree, int degree, int entityId) {
		DialogueTree lesson = switch (degree) {
			case 3 -> initiate(entityId);
			case 4 -> adept(entityId);
			case 5 -> illuminatus(entityId, false, false);
			case 6 -> sanctified(entityId, false, false);
			default -> null;
		};
		if (lesson == null) return tree;
		String nodeId = switch (degree) {
			case 3 -> "pallid_vessel";
			case 4 -> "crimson_vessel";
			case 5 -> "ashen_vessel";
			default -> "curved_horn";
		};
		Map<String, DialogueNode> nodes = new LinkedHashMap<>(tree.nodes());
		nodes.put(nodeId, lesson.getNode(nodeId));
		nodes.put("gourd_use", lesson.getNode("gourd_use"));
		DialogueNode start = tree.getStartNode();
		List<DialogueOption> options = new ArrayList<>(start.options());
		lesson.getStartNode().options().stream()
				.filter(option -> nodeId.equals(option.nextNodeId()))
				.findFirst().ifPresent(option -> options.add(Math.max(0, options.size() - 1), option));
		nodes.put(start.id(), new DialogueNode(start.id(), start.lines(), List.copyOf(options)));
		return new DialogueTree(tree.speakerName(), tree.speakerIcon(), tree.startNodeId(), nodes,
				tree.entityId(), tree.theme(), tree.presentation());
	}

	public record HeldSpecimenJar(ResourceLocation specimenId, List<MorphlingPolypLayer> morphlingLayers) {
		public boolean isResearchSpecimen() {
			return SpecimenBestiaryDefinitions.isResearchSpecimen(specimenId);
		}
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
				? new DialogueOption("hemomancy.dialogue.recruit.option.release_blood", null, "expel_harbinger",
						DialogueOptionPresentation.prompt("hemomancy.dialogue.recruit.release.prompt"))
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

	/** Degree 1 — Neophyte. Introduces the Vial Centrifuge and basic sampling. */
	public static DialogueTree neophyte(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.neophyte.line1",
						"hemomancy.alchemist.neophyte.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge", "centrifuge_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_gourds", "blood_gourd_basics",
								null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_machines", "machines_overview", null),
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("centrifuge_lore", List.of(
						"hemomancy.alchemist.neophyte.centrifuge_lore"
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
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge", "centrifuge_lore", null),
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
		return votary(entityId, heldRedTaxonomySample, null);
	}

	public static DialogueTree votary(int entityId, RedTaxonomySample heldRedTaxonomySample,
			HeldSpecimenJar heldSpecimenJar) {
		return votary(entityId, heldRedTaxonomySample, heldSpecimenJar, false, false, false);
	}

	public static DialogueTree votary(int entityId, RedTaxonomySample heldRedTaxonomySample,
			HeldSpecimenJar heldSpecimenJar, boolean canBriefFirstSeparation, boolean canClaimFirstSeparation,
			boolean canBriefBodyAnswers) {
		return votary(entityId, heldRedTaxonomySample, heldSpecimenJar, canBriefFirstSeparation,
				canClaimFirstSeparation, canBriefBodyAnswers, false);
	}

	public static DialogueTree votary(int entityId, RedTaxonomySample heldRedTaxonomySample,
			HeldSpecimenJar heldSpecimenJar, boolean canBriefFirstSeparation, boolean canClaimFirstSeparation,
			boolean canBriefBodyAnswers, boolean canDiscussMuscleMemories) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		if (heldSpecimenJar != null && heldSpecimenJar.isResearchSpecimen()) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.record_living_specimen",
					null, EVENT_BESTIARY_RECORD,
					DialogueOptionPresentation.prompt("hemomancy.alchemist.living_bestiary.record.prompt")));
			if (heldSpecimenJar.morphlingLayers().isEmpty()) {
				greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.surrender_living_specimen",
						null, EVENT_BESTIARY_SURRENDER,
						DialogueOptionPresentation.prompt("hemomancy.alchemist.living_bestiary.surrender.prompt")));
			} else {
				for (MorphlingPolypLayer layer : heldSpecimenJar.morphlingLayers()) {
					greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.surrender_polyp_"
							+ layer.serializedName(), null,
							EVENT_BESTIARY_SURRENDER_MORPHLING_PREFIX + layer.serializedName(),
							DialogueOptionPresentation.prompt(
									"hemomancy.alchemist.living_bestiary.surrender_polyp.prompt")));
				}
			}
		}
		if (heldRedTaxonomySample != null) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.submit_red_taxonomy_sample",
					"red_taxonomy_" + heldRedTaxonomySample.key(), heldRedTaxonomySample.eventId(),
					DialogueOptionPresentation.prompt("hemomancy.alchemist.red_taxonomy.submit.prompt")));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.begin_living_bestiary",
				"living_bestiary_intro", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.begin_red_taxonomy",
				"red_taxonomy_intro", null));
		if (canBriefFirstSeparation) {
			greetingOptions.add(new DialogueOption(
					"hemomancy.dialogue.alchemist.option.first_separation_assignment",
					"first_separation_offer", null));
		}
		if (canClaimFirstSeparation) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.complete_first_separation",
					"first_separation_complete", EVENT_FIRST_SEPARATION_CLAIM,
					DialogueOptionPresentation.prompt("hemomancy.alchemist.first_separation.claim.prompt")));
		}
		if (canBriefBodyAnswers) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.accept_body_answers",
					"body_answers_briefing", EVENT_BODY_ANSWERS_BRIEF,
					DialogueOptionPresentation.prompt("hemomancy.alchemist.body_answers.prompt")));
		}
		if (canDiscussMuscleMemories) {
			greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.muscle_memory_catalogue",
					"muscle_memory_catalogue", null));
		}
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge",
				"centrifuge_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.enzyme_uses",
				"enzyme_uses", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd",
				"gourd_upgrades", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_alembic",
				"alembic_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.first_distillation_assignment",
				"first_distillation_assignment", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item",
				"item_hint", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));

		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.votary.line1"
				), greetingOptions))
				.addNode(new DialogueNode("red_taxonomy_intro", List.of(
						"hemomancy.alchemist.red_taxonomy.intro.line1",
						"hemomancy.alchemist.red_taxonomy.intro.line2",
						"hemomancy.alchemist.red_taxonomy.intro.line3",
						"hemomancy.alchemist.red_taxonomy.intro.line4"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.bloodwood_lesson",
								"bloodwood_lesson", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge",
								"centrifuge_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("bloodwood_lesson", List.of(
					"hemomancy.alchemist.bloodwood_lesson.line1",
					"hemomancy.alchemist.bloodwood_lesson.line2"
				), List.of(
					new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("living_bestiary_intro", List.of(
						"hemomancy.alchemist.living_bestiary.intro.line1",
						"hemomancy.alchemist.living_bestiary.intro.line2",
						"hemomancy.alchemist.living_bestiary.intro.line3"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_morphlings",
								"living_bestiary_morphlings", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("living_bestiary_morphlings", List.of(
						"hemomancy.alchemist.living_bestiary.morphlings.line1",
						"hemomancy.alchemist.living_bestiary.morphlings.line2"
				), List.of(
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
				.addNode(new DialogueNode("first_separation_offer", List.of(
						"hemomancy.alchemist.first_separation.offer.line1",
						"hemomancy.alchemist.first_separation.offer.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.accept_first_separation",
								"first_separation_briefing", EVENT_FIRST_SEPARATION_BRIEF),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("first_separation_briefing", List.of(
						"hemomancy.alchemist.first_separation.briefing"
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))) )
				.addNode(new DialogueNode("first_separation_complete", List.of(
						"hemomancy.alchemist.first_separation.complete"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.enzyme_uses", "enzyme_uses", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)) )
				.addNode(new DialogueNode("body_answers_briefing", List.of(
						"hemomancy.alchemist.body_answers.line1",
						"hemomancy.alchemist.body_answers.line2",
						"hemomancy.alchemist.body_answers.line3"
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.addNode(new DialogueNode("muscle_memory_catalogue", List.of(
						"hemomancy.alchemist.muscle_memory_catalogue.line1",
						"hemomancy.alchemist.muscle_memory_catalogue.line2",
						"hemomancy.alchemist.muscle_memory_catalogue.line3"
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.addNode(new DialogueNode("enzyme_uses", List.of(
						"hemomancy.alchemist.enzyme_uses.brewing",
						"hemomancy.alchemist.enzyme_uses.culture",
						"hemomancy.alchemist.enzyme_uses.workshop"
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))) )
				.addNode(new DialogueNode("gourd_upgrades", List.of(
						"hemomancy.alchemist.votary.gourd_upgrades"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("alembic_lore", List.of(
						"hemomancy.alchemist.votary.alembic_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd",
								"gourd_upgrades", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("first_distillation_assignment", List.of(
						"hemomancy.alchemist.votary.first_distillation.assignment"
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 2 — Votary. Explains the Vial Centrifuge and blood tendency separation. */
	public static DialogueTree votary(int entityId) {
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.votary.line1"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge", "centrifuge_lore", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.enzyme_uses", "enzyme_uses", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd", "gourd_upgrades", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge", "centrifuge_lore", null),
      new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("centrifuge_lore", List.of(
						"hemomancy.alchemist.votary.centrifuge_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd", "gourd_upgrades", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("enzyme_uses", List.of(
						"hemomancy.alchemist.enzyme_uses.brewing",
						"hemomancy.alchemist.enzyme_uses.culture",
						"hemomancy.alchemist.enzyme_uses.workshop"
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))) )
				.addNode(new DialogueNode("gourd_upgrades", List.of(
						"hemomancy.alchemist.votary.gourd_upgrades"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("alembic_lore", List.of(
						"hemomancy.alchemist.votary.alembic_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.how_do_i_upgrade_my_gourd", "gourd_upgrades", null),
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
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_mnemonist_work",
				"mnemonist_breadcrumb", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_loom", "loom_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.pallid_vessel", "pallid_vessel", null));
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
				.addNode(new DialogueNode("pallid_vessel", List.of(
						"hemomancy.alchemist.initiate.pallid_vessel.rite"
				), vesselUseOptions()))
				.addNode(gourdUseLesson())
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
						new DialogueOption("hemomancy.dialogue.alchemist.option.lantern_cultivation", "lantern_cultivation", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.wild_morphlings", "wild_morphlings", null),
						new DialogueOption("hemomancy.dialogue.alchemist.option.crimson_vessel", "crimson_vessel", null),
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
				.addNode(new DialogueNode("lantern_cultivation", List.of(
						"hemomancy.alchemist.adept.lantern.line1",
						"hemomancy.alchemist.adept.lantern.line2"
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.addNode(new DialogueNode("wild_morphlings", List.of(
						"hemomancy.alchemist.adept.morphlings.line1",
						"hemomancy.alchemist.adept.morphlings.line2"
				), List.of(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null))))
				.addNode(new DialogueNode("crimson_vessel", List.of(
						"hemomancy.alchemist.adept.crimson_vessel"
				), vesselUseOptions()))
				.addNode(gourdUseLesson())
				.addNode(new DialogueNode("item_hint", List.of(
						"hemomancy.alchemist.item_hint"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.build();
	}

	/** Degree 5 — Illuminatus. Speaks of advanced biological processing and cardinal rite machines. */
	public static DialogueTree illuminatus(int entityId, boolean hasBloodline, boolean isNpcRecruited) {
		List<DialogueOption> greetingOptions = new ArrayList<>();
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_morphling_incubator", "incubator_lore", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ashen_vessel", "ashen_vessel", null));
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.ask_about_item", "item_hint", null));
		addRecruitmentOption(greetingOptions, hasBloodline, isNpcRecruited);
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null));
		return DialogueTree.builder(SPEAKER, ALCHEMIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.alchemist.illuminatus.line1"
				), greetingOptions))
				.addNode(new DialogueNode("incubator_lore", List.of(
						"hemomancy.alchemist.illuminatus.incubator_lore"
				), List.of(
						new DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
				)))
				.addNode(new DialogueNode("ashen_vessel", List.of(
						"hemomancy.alchemist.illuminatus.ashen_vessel"
				), vesselUseOptions()))
				.addNode(gourdUseLesson())
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
		greetingOptions.add(new DialogueOption("hemomancy.dialogue.alchemist.option.curved_horn", "curved_horn", null));
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
				.addNode(new DialogueNode("curved_horn", List.of(
						"hemomancy.alchemist.sanctified.curved_horn"
				), vesselUseOptions()))
				.addNode(gourdUseLesson())
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
