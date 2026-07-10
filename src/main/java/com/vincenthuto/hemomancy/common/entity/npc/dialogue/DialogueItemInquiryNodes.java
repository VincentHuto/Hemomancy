package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.HematicMemoryItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class DialogueItemInquiryNodes {
	static final String INVENTORY_NODE_PREFIX = "item_inquiry/";

	private DialogueItemInquiryNodes() {
	}

	public static DialogueTree withInventoryItemInquiries(DialogueTree tree, Player player, String speakerKey,
			int degree, float purity) {
		List<ItemStack> inventory = new ArrayList<>(player.getInventory().getContainerSize());
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			inventory.add(player.getInventory().getItem(slot));
		}
		return withInventoryItemInquiries(tree, inventory, speakerKey, degree, purity);
	}

	static DialogueTree withInventoryItemInquiries(DialogueTree tree, Iterable<ItemStack> inventory,
			String speakerKey, int degree, float purity) {
		if (!tree.nodes().containsKey("item_hint")) {
			return tree;
		}
		Map<ResourceLocation, List<String>> resolvedItems = new LinkedHashMap<>();
		Set<ResourceLocation> seenItems = new LinkedHashSet<>();
		for (ItemStack stack : inventory) {
			if (stack == null || stack.isEmpty()) continue;
			ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
			if (!seenItems.add(itemId)) continue;
			resolveKnownLines(stack, speakerKey, degree, purity)
					.ifPresent(lines -> resolvedItems.put(itemId, lines));
		}
		return addResolvedInventoryItemInquiries(tree, resolvedItems);
	}

	static DialogueTree addResolvedInventoryItemInquiries(DialogueTree tree,
			Map<ResourceLocation, List<String>> resolvedItems) {
		if (!tree.nodes().containsKey("item_hint")) return tree;
		DialogueNode template = tree.getNode("item_hint");
		List<DialogueOption> inquiryOptions = template.options().stream()
				.filter(option -> !isLegacyLeave(option))
				.toList();
		for (Map.Entry<ResourceLocation, List<String>> entry : resolvedItems.entrySet()) {
			String nodeId = inventoryNodeId(entry.getKey());
			tree.nodes().put(nodeId, new DialogueNode(nodeId,
					entry.getValue(), inquiryOptions));
		}
		return tree;
	}

	static String inventoryNodeId(ResourceLocation itemId) {
		return INVENTORY_NODE_PREFIX + itemId.getNamespace() + "/" + itemId.getPath();
	}

	static ResourceLocation inventoryItemId(String nodeId) {
		if (!nodeId.startsWith(INVENTORY_NODE_PREFIX)) return null;
		String value = nodeId.substring(INVENTORY_NODE_PREFIX.length());
		int separator = value.indexOf('/');
		if (separator <= 0 || separator == value.length() - 1) return null;
		return ResourceLocation.tryBuild(value.substring(0, separator), value.substring(separator + 1));
	}

	private static boolean isLegacyLeave(DialogueOption option) {
		return option.nextNodeId() == null && option.eventId() == null
				&& option.text().toLowerCase(Locale.ROOT).contains("leave");
	}

	private static Optional<List<String>> resolveKnownLines(ItemStack item, String speakerKey, int degree,
			float purity) {
		if ("vicar".equals(speakerKey) && item.getItem() instanceof HematicMemoryItem) {
			return Optional.of(List.of(
					"hemomancy.vicar.item_inquiry.hematic_memory.line1",
					"hemomancy.vicar.item_inquiry.hematic_memory.line2"));
		}
		ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item.getItem());
		return registeredLines(speakerKey, itemId, degree, purity);
	}

	static Optional<List<String>> registeredLines(String speakerKey, ResourceLocation itemId, int degree,
			float purity) {
		return ItemInquiryRegistry.INSTANCE.resolve(speakerKey, itemId, degree, purity);
	}
}
