package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class HarbingerVotaryWayfarerDialogueTrees {
	private static final ResourceLocation VOTARY_ICON = Hemomancy.rloc(
			"textures/entity/harbinger_votary_wayfarer/harbinger_votary_wayfarer.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_votary_wayfarer";

	private HarbingerVotaryWayfarerDialogueTrees() {
	}

	public static DialogueTree forDegree(int degree, int entityId) {
		if (degree <= 0) {
			return basic(entityId, "uninitiated",
					"hemomancy.votary_wayfarer.uninitiated.line1",
					"hemomancy.votary_wayfarer.uninitiated.line2");
		}
		if (degree < 5) {
			return basic(entityId, "peer",
					"hemomancy.votary_wayfarer.peer.line1",
					"hemomancy.votary_wayfarer.peer.line2");
		}
		return basic(entityId, "senior",
				"hemomancy.votary_wayfarer.senior.line1",
				"hemomancy.votary_wayfarer.senior.line2");
	}

	public static DialogueTree purifying(int entityId) {
		return basic(entityId, "purifying",
				"hemomancy.votary_wayfarer.purifying.line1",
				"hemomancy.votary_wayfarer.purifying.line2");
	}

	public static DialogueTree clarity(int entityId) {
		return basic(entityId, "clarity",
				"hemomancy.votary_wayfarer.clarity.line1",
				"hemomancy.votary_wayfarer.clarity.line2");
	}

	public static DialogueTree itemInquiry(ItemStack item, int degree, int entityId) {
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
		return ItemInquiryRegistry.INSTANCE
				.resolve("votary_wayfarer", itemId, degree, 0f)
				.map(lines -> basicItemInquiry(entityId, lines.toArray(String[]::new)))
				.orElseGet(() -> basicItemInquiry(entityId, "hemomancy.votary_wayfarer.item_inquiry.unknown"));
	}

	private static DialogueTree basic(int entityId, String rootId, String... lines) {
		return DialogueTree.builder(SPEAKER, VOTARY_ICON, entityId)
				.addNode(new DialogueNode(rootId, List.of(lines), List.of(
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.ask_about_voyage", "voyage", null),
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.ask_about_fear", "fear", null),
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("voyage", List.of(
						"hemomancy.votary_wayfarer.voyage.line1",
						"hemomancy.votary_wayfarer.voyage.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.ask_about_fear", "fear", null),
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.leave", null, null)
				)))
				.addNode(new DialogueNode("fear", List.of(
						"hemomancy.votary_wayfarer.fear.line1",
						"hemomancy.votary_wayfarer.fear.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.ask_about_voyage", "voyage", null),
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.leave", null, null)
				)))
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree basicItemInquiry(int entityId, String... lines) {
		return DialogueTree.builder(SPEAKER, VOTARY_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(lines), List.of(
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.leave", null, null)
				)))
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueNode itemHintNode() {
		return new DialogueNode("item_hint", List.of(
				"hemomancy.votary_wayfarer.item_hint"
		), List.of(
				new DialogueOption("hemomancy.dialogue.votary_wayfarer.option.leave", null, null)
		));
	}
}
