package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.HematicMemoryItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class DialogueItemInquiryNodes {
	private DialogueItemInquiryNodes() {
	}

	public static DialogueTree withHeldItemInquiry(DialogueTree tree, ItemStack item, String speakerKey,
			String unknownLine, int degree, float purity) {
		if (item.isEmpty() || !tree.nodes().containsKey("item_hint")) {
			return tree;
		}
		DialogueNode current = tree.getNode("item_hint");
		tree.nodes().put("item_hint", new DialogueNode("item_hint",
				resolveLines(item, speakerKey, unknownLine, degree, purity),
				current.options()));
		return tree;
	}

	private static List<String> resolveLines(ItemStack item, String speakerKey, String unknownLine, int degree,
			float purity) {
		if ("vicar".equals(speakerKey) && item.getItem() instanceof HematicMemoryItem) {
			return List.of(
					"hemomancy.vicar.item_inquiry.hematic_memory.line1",
					"hemomancy.vicar.item_inquiry.hematic_memory.line2");
		}
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
		return ItemInquiryRegistry.INSTANCE
				.resolve(speakerKey, itemId, degree, purity)
				.orElseGet(() -> List.of(unknownLine));
	}
}
