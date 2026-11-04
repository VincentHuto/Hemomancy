package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class HarbingerVoyagerDialogueTrees {
	private static final ResourceLocation VOYAGER_ICON = Hemomancy.rloc("textures/entity/npc/harbinger/harbinger_voyager/harbinger_voyager.png");
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

	private static DialogueTree basic(int entityId, String rootId, String... lines) {
		return DialogueTree.builder(SPEAKER, VOYAGER_ICON, entityId)
				.addNode(new DialogueNode(rootId, List.of(lines), List.of(
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_work", "work", null).withLore(),
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_wrecks", "wrecks", null).withLore(),
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_item", "item_hint", null),
						new DialogueOption("hemomancy.dialogue.voyager.option.leave", null, null)
				)))
				.addNode(new DialogueNode("work", List.of(
						"hemomancy.voyager.work.line1",
						"hemomancy.voyager.work.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_wrecks", "wrecks", null).withLore(),
						new DialogueOption("hemomancy.dialogue.voyager.option.leave", null, null)
				)))
				.addNode(new DialogueNode("wrecks", List.of(
						"hemomancy.voyager.wrecks.line1",
						"hemomancy.voyager.wrecks.line2"
				), List.of(
						new DialogueOption("hemomancy.dialogue.voyager.option.ask_about_work", "work", null).withLore(),
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
