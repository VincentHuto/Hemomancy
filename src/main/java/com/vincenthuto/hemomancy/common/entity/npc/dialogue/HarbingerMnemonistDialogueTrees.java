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

	private HarbingerMnemonistDialogueTrees() {
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter) {
		return forDegree(degree, entityId, hasBloodline, isNpcRecruited, canClaimStarter, false);
	}

	public static DialogueTree forDegree(int degree, int entityId, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter, boolean wovenVesselComplete) {
		if (degree <= 0) return uninitiated(entityId);
		if (degree == 1) return neophyte(entityId, canClaimStarter);
		if (degree == 2) return votary(entityId, canClaimStarter);
		return woven(entityId, degree >= 5 && hasBloodline, isNpcRecruited, canClaimStarter,
				!wovenVesselComplete);
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
				.addNode(reliquaryNode())
				.addNode(starterChoiceNode())
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree woven(int entityId, boolean hasBloodline, boolean isNpcRecruited,
			boolean canClaimStarter, boolean canCompleteWovenVessel) {
		List<DialogueOption> options = new ArrayList<>();
		if (canCompleteWovenVessel) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.woven_vessel",
					"woven_vessel", null));
		}
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_loom", "loom", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_reliquary", "reliquary", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_crude_memories", "crude_memories", null));
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
				.addNode(wovenVesselNode())
				.addNode(starterChoiceNode())
				.addNode(recruitOfferNode())
				.addNode(itemHintNode())
				.build();
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
		if (canClaimStarter) {
			options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.choose_starter", "starter_choice", null));
		}
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_item", "item_hint", null));
		options.add(new DialogueOption("hemomancy.dialogue.mnemonist.option.leave", null, null));
		return options;
	}

	private static List<DialogueOption> votaryOptions(boolean canClaimStarter) {
		List<DialogueOption> options = neophyteOptions(canClaimStarter);
		options.add(2, new DialogueOption("hemomancy.dialogue.mnemonist.option.ask_about_reliquary", "reliquary", null));
		return options;
	}

	private static DialogueNode memoryLoreNode() {
		return new DialogueNode("memory_lore", List.of(
				"hemomancy.mnemonist.memory_lore.line1",
				"hemomancy.mnemonist.memory_lore.line2"
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
