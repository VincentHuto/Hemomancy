
package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.InquiryAccessPolicy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryContext;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.StackAwareInquiryRegistry;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.HematicMemoryItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public final class DialogueItemInquiryNodes {
    static final String INVENTORY_NODE_PREFIX = "item_inquiry/";

    private DialogueItemInquiryNodes() {}

    public static DialogueTree withInventoryItemInquiries(DialogueTree tree, Player player, String speakerKey,
            int degree, float purity) {
        List<ItemStack> inventory = new ArrayList<>(player.getInventory().getContainerSize());
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            inventory.add(player.getInventory().getItem(slot));
        }
        return withInventoryItemInquiries(tree, inventory, speakerKey, ItemInquiryContext.from(player));
    }

    static DialogueTree withInventoryItemInquiries(DialogueTree tree, Iterable<ItemStack> inventory,
            String speakerKey, int degree, float purity) {
        return withInventoryItemInquiries(tree, inventory, speakerKey, ItemInquiryContext.legacy(degree, purity));
    }

    static DialogueTree withInventoryItemInquiries(DialogueTree tree, Iterable<ItemStack> inventory,
            String speakerKey, ItemInquiryContext context) {
        if (!tree.nodes().containsKey("item_hint")) return tree;
        InquiryAccessPolicy.Access access = InquiryAccessPolicy.accessFor(speakerKey, context);
        if (access == InquiryAccessPolicy.Access.NONE) return tree;

        Map<String, ResolvedInventoryInquiry> resolved = new LinkedHashMap<>();
        Set<String> seen = new LinkedHashSet<>();
        for (ItemStack stack : inventory) {
            if (stack == null || stack.isEmpty()) continue;
            Optional<StackAwareInquiryRegistry.ResolvedStackInquiry> dynamic =
                    StackAwareInquiryRegistry.resolve(speakerKey, stack, context);
            if (dynamic.isPresent()) {
                var value = dynamic.get();
                String key = value.itemId() + "#" + value.presentationKey();
                if (seen.add(key)) {
                    resolved.put(key, new ResolvedInventoryInquiry(value.presentationKey(), value.itemId(),
                            restrictedLines(access, speakerKey, value.lines())));
                }
                continue;
            }
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            Optional<List<String>> lines = resolveKnownLines(stack, speakerKey, context);
            if (lines.isEmpty()) {
                continue;
            }
            String key = itemId.toString();
            if (seen.add(key)) {
                resolved.put(key, new ResolvedInventoryInquiry(itemId.toString(), itemId,
                        restrictedLines(access, speakerKey, lines.get())));
            }
        }
        return addResolvedInventoryItemInquiries(tree, resolved.values());
    }

    private static List<String> restrictedLines(InquiryAccessPolicy.Access access, String speakerKey,
            List<String> normal) {
        return access == InquiryAccessPolicy.Access.IDENTIFICATION_ONLY
                ? List.of(InquiryAccessPolicy.refusalKey(speakerKey)) : normal;
    }

    static DialogueTree addResolvedInventoryItemInquiries(DialogueTree tree,
            Map<ResourceLocation, List<String>> resolvedItems) {
        List<ResolvedInventoryInquiry> converted = resolvedItems.entrySet().stream()
                .map(entry -> new ResolvedInventoryInquiry(entry.getKey().toString(), entry.getKey(), entry.getValue()))
                .toList();
        return addResolvedInventoryItemInquiries(tree, converted);
    }

    static DialogueTree addResolvedInventoryItemInquiries(DialogueTree tree,
            Iterable<ResolvedInventoryInquiry> resolvedItems) {
        if (!tree.nodes().containsKey("item_hint")) return tree;
        DialogueNode template = tree.getNode("item_hint");
        List<DialogueOption> inquiryOptions = template.options().stream().filter(option -> !isLegacyLeave(option)).toList();
        for (ResolvedInventoryInquiry entry : resolvedItems) {
            String nodeId = inventoryNodeId(entry.itemId(), entry.presentationKey());
            tree.nodes().put(nodeId, new DialogueNode(nodeId, entry.lines(), inquiryOptions));
        }
        return tree;
    }

    static String inventoryNodeId(ResourceLocation itemId) {
        return INVENTORY_NODE_PREFIX + itemId.getNamespace() + "/" + itemId.getPath();
    }

    static String inventoryNodeId(ResourceLocation itemId, String presentationKey) {
        String base = inventoryNodeId(itemId);
        return itemId.toString().equals(presentationKey) ? base
                : base + "#" + Integer.toHexString(presentationKey.hashCode());
    }

    static ResourceLocation inventoryItemId(String nodeId) {
        if (!nodeId.startsWith(INVENTORY_NODE_PREFIX)) return null;
        String value = nodeId.substring(INVENTORY_NODE_PREFIX.length());
        int hash = value.indexOf('#');
        if (hash >= 0) value = value.substring(0, hash);
        int separator = value.indexOf('/');
        if (separator <= 0 || separator == value.length() - 1) return null;
        return ResourceLocation.tryBuild(value.substring(0, separator), value.substring(separator + 1));
    }

    private static boolean isLegacyLeave(DialogueOption option) {
        return option.nextNodeId() == null && option.eventId() == null
                && option.text().toLowerCase(Locale.ROOT).contains("leave");
    }

    private static Optional<List<String>> resolveKnownLines(ItemStack item, String speakerKey,
            ItemInquiryContext context) {
        if ("vicar".equals(speakerKey) && item.getItem() instanceof HematicMemoryItem) {
            return Optional.of(List.of("hemomancy.vicar.item_inquiry.hematic_memory.line1",
                    "hemomancy.vicar.item_inquiry.hematic_memory.line2"));
        }
        return registeredLines(speakerKey, BuiltInRegistries.ITEM.getKey(item.getItem()), context);
    }

    static Optional<List<String>> registeredLines(String speakerKey, ResourceLocation itemId,
            ItemInquiryContext context) {
        return ItemInquiryRegistry.INSTANCE.resolve(speakerKey, itemId, context);
    }

    static Optional<List<String>> registeredLines(String speakerKey, ResourceLocation itemId, int degree,
            float purity) {
        return registeredLines(speakerKey, itemId, ItemInquiryContext.legacy(degree, purity));
    }

    record ResolvedInventoryInquiry(String presentationKey, ResourceLocation itemId, List<String> lines) {}
}
