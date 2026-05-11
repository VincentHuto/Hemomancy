package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class HarbingerVoyagerDialogueTrees {
	private static final ResourceLocation VOYAGER_ICON = Hemomancy.rloc(
			"textures/entity/harbinger_voyager/harbinger_voyager.png");
	private static final String SPEAKER = "entity.hemomancy.harbinger_voyager";

	private HarbingerVoyagerDialogueTrees() {
	}

	public static DialogueTree forDegree(int degree, int entityId) {
		if (degree <= 0) {
			return basic(entityId, "uninitiated",
					"hemomancy.voyager.uninitiated.line1",
					"hemomancy.voyager.uninitiated.line2");
		}
		if (degree < 5) {
			return basic(entityId, "harbinger",
					"hemomancy.voyager.harbinger.line1",
					"hemomancy.voyager.harbinger.line2");
		}
		return basic(entityId, "lodge",
				"hemomancy.voyager.lodge.line1",
				"hemomancy.voyager.lodge.line2");
	}

	public static DialogueTree purifying(int entityId) {
		return basic(entityId, "purifying",
				"hemomancy.voyager.purifying.line1",
				"hemomancy.voyager.purifying.line2");
	}

	public static DialogueTree clarity(int entityId) {
		return basic(entityId, "clarity",
				"hemomancy.voyager.clarity.line1",
				"hemomancy.voyager.clarity.line2");
	}

	public static DialogueTree itemInquiry(ItemStack item, int degree, int entityId) {
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
		return ItemInquiryRegistry.INSTANCE
				.resolve("voyager", itemId, degree, 0f)
				.map(lines -> basicItemInquiry(entityId, lines.toArray(String[]::new)))
				.orElseGet(() -> basicItemInquiry(entityId, "hemomancy.voyager.item_inquiry.unknown"));
	}

	private static DialogueTree basic(int entityId, String rootId, String... lines) {
		return DialogueTree.builder(SPEAKER, VOYAGER_ICON, entityId)
				.addNode(new DialogueNode(rootId, List.of(lines), List.of(
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_work", "work", null),
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_wrecks", "wrecks", null),
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.voyager.option.leave", null, null)
				)))
				.addNode(new DialogueNode("work", List.of(
						"hemomancy.voyager.work.line1",
						"hemomancy.voyager.work.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_wrecks", "wrecks", null),
						new DialogueOption("hemomancy.dialogue.voyager.option.leave", null, null)
				)))
				.addNode(new DialogueNode("wrecks", List.of(
						"hemomancy.voyager.wrecks.line1",
						"hemomancy.voyager.wrecks.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_work", "work", null),
						new DialogueOption("hemomancy.dialogue.voyager.option.leave", null, null)
				)))
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueTree basicItemInquiry(int entityId, String... lines) {
		return DialogueTree.builder(SPEAKER, VOYAGER_ICON, entityId)
				.addNode(new DialogueNode("root", List.of(lines), List.of(
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.voyager.option.leave", null, null)
				)))
				.addNode(itemHintNode())
				.build();
	}

	private static DialogueNode itemHintNode() {
		return new DialogueNode("item_hint", List.of(
				"hemomancy.voyager.item_hint"
		), List.of(
				new DialogueOption("hemomancy.dialogue.voyager.option.leave", null, null)
		));
	}
}
