package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Static factory for the Harbinger Mnemonist, the outpost guide for blood
 * manipulation memories, crude echoes, loadout slots, and memory weaving.
 */
public final class HarbingerMnemonistDialogueTrees {
	private static final ResourceLocation MNEMONIST_ICON = Hemomancy.rloc(
			"textures/entity/harbinger_mnemonist/harbinger_mnemonist.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_mnemonist";
	public static final String EVENT_WOVEN_VESSEL_TURN_IN = "mnemonist_woven_vessel_turn_in";
	public static final String EVENT_RELIQUARY_TAUGHT = "mnemonist_reliquary_taught";
	public static final String EVENT_ANCHORITE_COUNSEL = "mnemonist_anchorite_counsel";
	public static final String EVENT_CIRCUS_WAYBILL = "mnemonist_circus_waybill";

	private HarbingerMnemonistDialogueTrees() {
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, canClaimStarter, false);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter, boolean wovenVesselComplete) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, canClaimStarter,
				wovenVesselComplete, false);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter, boolean wovenVesselComplete, boolean morphlingPuppetInterference) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, canClaimStarter, wovenVesselComplete,
				morphlingPuppetInterference, false, false);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter, boolean wovenVesselComplete, boolean morphlingPuppetInterference,
			boolean anchoriteReferral, boolean anchoriteCounsel) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, canClaimStarter, wovenVesselComplete,
				morphlingPuppetInterference, anchoriteReferral, anchoriteCounsel, false, false);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter, boolean wovenVesselComplete, boolean morphlingPuppetInterference,
			boolean anchoriteReferral, boolean anchoriteCounsel, boolean circusDiscovered,
			boolean carryingCircusWaybill) {
		if (degree <= 0) return uninitiated(entityId);
		if (degree == 1) return neophyte(entityId, canClaimStarter);
		if (degree == 2) return votary(entityId, canClaimStarter);
		return woven(entityId, degree, degree >= 5 && hasBloodline, isNpcRecruited, canClaimStarter,
				!wovenVesselComplete, morphlingPuppetInterference, anchoriteReferral, anchoriteCounsel,
				circusDiscovered, carryingCircusWaybill);
	}

	public static DialogueTree purifying(int entityId) {
		return DialogueTree.builder(SPEAKER, MNEMONIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.mnemonist.purifying.line1",
						"hemomancy.mnemonist.purifying.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_crude_memories", "crude_memories", null),
						new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)
				)))
				.addNode(crudeMemoriesNode())
				.addNode(itemHintNode())
				.build();
	}

	public static DialogueTree clarity(int entityId) {
		return DialogueTree.builder(SPEAKER, MNEMONIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.mnemonist.clarity.line1",
						"hemomancy.mnemonist.clarity.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_memory", "memory_lore", null),
						new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)
				)))
				.addNode(memoryLoreNode())
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree uninitiated(int entityId) {
		return DialogueTree.builder(SPEAKER, MNEMONIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.mnemonist.uninitiated.line1",
						"hemomancy.mnemonist.uninitiated.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_memory", "memory_lore", null),
						new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)
				)))
				.addNode(memoryLoreNode())
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree neophyte(int entityId, boolean canClaimStarter) {
		return DialogueTree.builder(SPEAKER, MNEMONIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.mnemonist.neophyte.line1",
						"hemomancy.mnemonist.neophyte.line2"
				), neophyteOptions(canClaimStarter)))
				.addNode(crudeMemoriesNode())
				.addNode(slotsNode())
				.addNode(chamberNode())
				.addNode(starterChoiceNode())
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree votary(int entityId, boolean canClaimStarter) {
		return DialogueTree.builder(SPEAKER, MNEMONIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.mnemonist.votary.line1"
				), votaryOptions(canClaimStarter)))
				.addNode(crudeMemoriesNode())
				.addNode(slotsNode())
				.addNode(chamberNode())
				.addNode(starterChoiceNode())
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree woven(int entityId, int degree, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter, boolean canCompleteWovenVessel, boolean morphlingPuppetInterference,
			boolean anchoriteReferral, boolean anchoriteCounsel, boolean circusDiscovered,
			boolean carryingCircusWaybill) {
		List<DialogueOption> options = new ArrayList<>();
		if (canCompleteWovenVessel) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.woven_vessel",
					"woven_vessel", null));
		}
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_loom", "loom", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_reliquary", "reliquary", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_chamber", "chamber", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_crude_memories", "crude_memories", null));
		if (morphlingPuppetInterference) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.morphling_puppet_interference",
					"morphling_puppet_interference", null));
		}
		if (degree >= 6) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_mnemonic_doctrine",
					"mnemonic_doctrine", null));
		}
		if (anchoriteReferral && !anchoriteCounsel) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.anchorite_counsel",
					"anchorite_counsel", EVENT_ANCHORITE_COUNSEL));
		}
		if (CircusIntroductionRules.introductionFor(degree, circusDiscovered)
				!= CircusIntroductionRules.Introduction.HIDDEN) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_circus",
					"circus", null));
		}
		if (canClaimStarter) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.choose_starter", "starter_choice", null));
		}
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_item", "item_hint", null));
		addRecruitmentOption(options, hasBloodline, isNpcRecruited);
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null));

		return DialogueTree.builder(SPEAKER, MNEMONIST_ICON, entityId)
				.addNode(new DialogueNode("greeting", List.of(
						"hemomancy.mnemonist.woven.line1",
						"hemomancy.mnemonist.woven.line2"
				), options))
				.addNode(crudeMemoriesNode())
				.addNode(slotsNode())
				.addNode(reliquaryNode())
				.addNode(loomNode())
				.addNode(mnemonicDoctrineNode())
				.addNode(new DialogueNode("anchorite_counsel", List.of(
						"hemomancy.mnemonist.anchorite_counsel.line1",
						"hemomancy.mnemonist.anchorite_counsel.line2"), List.of(
						new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null))))
				.addNode(morphlingPuppetInterferenceNode())
				.addNode(circusNode(degree, circusDiscovered, carryingCircusWaybill))
				.addNode(chamberNode())
				.addNode(wovenVesselNode())
				.addNode(starterChoiceNode())
				.addNode(recruitOfferNode())
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueNode circusNode(int degree, boolean discovered, boolean carryingWaybill) {
		List<String> lines = discovered ? List.of(
				"hemomancy.mnemonist.circus.discovered.line1",
				"hemomancy.mnemonist.circus.discovered.line2") : List.of(
				"hemomancy.mnemonist.circus.undiscovered.line1",
				"hemomancy.mnemonist.circus.undiscovered.line2");
		List<DialogueOption> options = new ArrayList<>();
		if (CircusIntroductionRules.canRequestWaybill(degree, carryingWaybill)) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.take_circus_waybill",
					null, EVENT_CIRCUS_WAYBILL));
		}
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null));
		return new DialogueNode("circus", lines, options);
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

	private static List<DialogueOption> neophyteOptions(boolean canClaimStarter) {
		List<DialogueOption> options = new ArrayList<>();
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_crude_memories", "crude_memories", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_slots", "slots", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_chamber", "chamber", null));
		if (canClaimStarter) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.choose_starter", "starter_choice", null));
		}
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_item", "item_hint", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null));
		return options;
	}

	private static List<DialogueOption> votaryOptions(boolean canClaimStarter) {
		return neophyteOptions(canClaimStarter);
	}

	private static DialogueNode memoryLoreNode() {
		return new DialogueNode("memory_lore", List.of(
				"hemomancy.mnemonist.memory_lore.line1",
				"hemomancy.mnemonist.memory_lore.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)));
	}

	private static DialogueNode mnemonicDoctrineNode() {
		return new DialogueNode("mnemonic_doctrine", List.of(
				"hemomancy.mnemonist.mnemonic_doctrine.line1",
				"hemomancy.mnemonist.mnemonic_doctrine.line2",
				"hemomancy.mnemonist.mnemonic_doctrine.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)));
	}

	private static DialogueNode morphlingPuppetInterferenceNode() {
		return new DialogueNode("morphling_puppet_interference", List.of(
				"hemomancy.mnemonist.morphling_puppet_interference.line1",
				"hemomancy.mnemonist.morphling_puppet_interference.line2",
				"hemomancy.mnemonist.morphling_puppet_interference.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)));
	}

	private static DialogueNode crudeMemoriesNode() {
		return new DialogueNode("crude_memories", List.of(
				"hemomancy.mnemonist.crude_memories.line1",
				"hemomancy.mnemonist.crude_memories.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_slots", "slots", null),
				new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)));
	}

	private static DialogueNode slotsNode() {
		return new DialogueNode("slots", List.of(
				"hemomancy.mnemonist.slots.line1",
				"hemomancy.mnemonist.slots.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)));
	}

	private static DialogueNode reliquaryNode() {
		return new DialogueNode("reliquary", List.of(
				"hemomancy.mnemonist.reliquary.line1",
				"hemomancy.mnemonist.reliquary.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.reliquary_understood", null,
				EVENT_RELIQUARY_TAUGHT)));
	}

	private static DialogueNode chamberNode() {
		return new DialogueNode("chamber", List.of(
				"hemomancy.mnemonist.chamber.line1",
				"hemomancy.mnemonist.chamber.line2",
				"hemomancy.mnemonist.chamber.line3"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)));
	}

	private static DialogueNode loomNode() {
		return new DialogueNode("loom", List.of(
				"hemomancy.mnemonist.loom.line1",
				"hemomancy.mnemonist.loom.line2"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)));
	}

	private static DialogueNode wovenVesselNode() {
		return new DialogueNode("woven_vessel", List.of(
				"hemomancy.mnemonist.woven_vessel.recipe.line1",
				"hemomancy.mnemonist.woven_vessel.recipe.line2",
				"hemomancy.mnemonist.woven_vessel.archive.line1"
		), List.of(
				new DialogueOption("hemomancy.dialogue.mnemonist.option.turn_in_woven_vessel", null,
						EVENT_WOVEN_VESSEL_TURN_IN),
				new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)
		));
	}

	private static DialogueNode starterChoiceNode() {
		return new DialogueNode("starter_choice", List.of(
				"hemomancy.mnemonist.starter_choice.line1",
				"hemomancy.mnemonist.starter_choice.line2"
		), List.of(
				new DialogueOption("hemomancy.dialogue.mnemonist.option.choose_blood_shot", null,
						MnemonistStarterMemoryChoice.BLOOD_SHOT.eventId()),
				new DialogueOption("hemomancy.dialogue.mnemonist.option.choose_blood_rush", null,
						MnemonistStarterMemoryChoice.BLOOD_RUSH.eventId()),
				new DialogueOption("hemomancy.dialogue.mnemonist.option.choose_deadly_gaze", null,
						MnemonistStarterMemoryChoice.DEADLY_GAZE.eventId()),
				new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)
		));
	}

	private static DialogueNode recruitOfferNode() {
		return new DialogueNode("recruit_offer", List.of(
				"hemomancy.dialogue.recruit.mnemonist.consider",
				"hemomancy.dialogue.recruit.mnemonist.accept"
		), List.of(
				new DialogueOption("hemomancy.dialogue.recruit.option.confirm", null, "recruit_harbinger"),
				new DialogueOption("hemomancy.dialogue.recruit.option.not_yet", null, null)
		));
	}

	private static DialogueNode itemHintNode() {
		return new DialogueNode("item_hint", List.of(
				"hemomancy.mnemonist.item_hint"
		), List.of(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)));
	}

	public static DialogueTree itemInquiry(ItemStack item, int degree, int entityId) {
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
		return ItemInquiryRegistry.INSTANCE
				.resolve("mnemonist", itemId, degree, 0f)
				.map(lines -> basicItemInquiry(entityId, lines.toArray(String[]::new)))
				.orElseGet(() -> basicItemInquiry(entityId, "hemomancy.mnemonist.item_inquiry.unknown"));
	}

	private static DialogueTree basicItemInquiry(int entityId, String... lines) {
		return DialogueTree.builder(SPEAKER, MNEMONIST_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(lines), List.of(
						new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null)
				)))
				.addNode(itemHintNode())
				.build();
	}
}
