package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueItemInquiryNodes.ResolvedInventoryInquiry;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryCondition;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryEntry;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry;

import net.minecraft.resources.ResourceLocation;

class DialogueItemInquiryNodesTest {
    @Test
    void inventoryItemsBecomeDistinctItemSpecificNodesInSlotOrder() {
        DialogueTree tree = inquiryTree();
        Map<ResourceLocation, List<String>> resolved = new LinkedHashMap<>();
        resolved.put(ResourceLocation.fromNamespaceAndPath("minecraft", "stone"), List.of("stone.line"));
        resolved.put(ResourceLocation.fromNamespaceAndPath("minecraft", "dirt"), List.of("dirt.line"));
        DialogueItemInquiryNodes.addResolvedInventoryItemInquiries(tree, resolved);

        assertTrue(tree.nodes().containsKey("item_inquiry/minecraft/stone"));
        assertTrue(tree.nodes().containsKey("item_inquiry/minecraft/dirt"));
        assertEquals(2, tree.nodes().keySet().stream().filter(key -> key.startsWith("item_inquiry/")).count());
        assertEquals(List.of("stone.line"), tree.getNode("item_inquiry/minecraft/stone").lines());
        assertEquals(List.of("ask_follow_up"), tree.getNode("item_inquiry/minecraft/stone").options()
                .stream().map(DialogueOption::text).toList());
    }

    @Test
    void sameItemIdWithDifferentStateGetsDistinctNodes() {
        DialogueTree tree = inquiryTree();
        ResourceLocation blueprint = id("mnemonic_blueprint");
        List<ResolvedInventoryInquiry> resolved = List.of(
                new ResolvedInventoryInquiry("blueprint/rite/a", blueprint, List.of("a.line")),
                new ResolvedInventoryInquiry("blueprint/rite/b", blueprint, List.of("b.line")));

        DialogueItemInquiryNodes.addResolvedInventoryItemInquiries(tree, resolved);

        List<String> nodes = tree.nodes().keySet().stream()
                .filter(key -> key.startsWith("item_inquiry/hemomancy/mnemonic_blueprint#"))
                .toList();
        assertEquals(2, nodes.size());
        assertFalse(nodes.get(0).equals(nodes.get(1)));
    }

    @Test
    void unsupportedInventoryDoesNotCreateAnInquiryNode() {
        DialogueTree tree = inquiryTree();
        DialogueItemInquiryNodes.addResolvedInventoryItemInquiries(tree, List.of());

        assertFalse(tree.nodes().containsKey("item_inquiry/unknown"));
    }

    @Test
    void onlyRegisteredAndCurrentlyEligibleItemsResolveForTheGrid() {
        ResourceLocation known = ResourceLocation.fromNamespaceAndPath("minecraft", "stone");
        ResourceLocation gated = ResourceLocation.fromNamespaceAndPath("minecraft", "diamond");
        ResourceLocation unknown = ResourceLocation.fromNamespaceAndPath("minecraft", "dirt");
        Map<ResourceLocation, ItemInquiryEntry> entries = new LinkedHashMap<>();
        entries.put(known, new ItemInquiryEntry(List.of(ItemInquiryCondition.unconditional(List.of("known.line")))));
        entries.put(gated, new ItemInquiryEntry(List.of(
                new ItemInquiryCondition(3, -1, -1F, -1F, List.of("gated.line")))));
        ItemInquiryRegistry.INSTANCE.reload(Map.of("alchemist", entries));
        try {
            assertTrue(DialogueItemInquiryNodes.registeredLines("alchemist", known, 0, 0F).isPresent());
            assertFalse(DialogueItemInquiryNodes.registeredLines("alchemist", gated, 0, 0F).isPresent());
            assertFalse(DialogueItemInquiryNodes.registeredLines("alchemist", unknown, 0, 0F).isPresent());
        } finally {
            ItemInquiryRegistry.INSTANCE.reload(Map.of());
        }
    }

    private static DialogueTree inquiryTree() {
        return DialogueTree.builder("speaker", id("portrait"), 7)
                .addNode(new DialogueNode("greeting", List.of("hello"), List.of()))
                .addNode(new DialogueNode("item_hint", List.of("hint"), List.of(
                        new DialogueOption("ask_follow_up", "details", null),
                        new DialogueOption("hemomancy.dialogue.option.leave", null, null))))
                .addNode(new DialogueNode("details", List.of("details"), List.of()))
                .build();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
    }
}
