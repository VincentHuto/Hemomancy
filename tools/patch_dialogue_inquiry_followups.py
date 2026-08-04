#!/usr/bin/env python3
"""Apply focused follow-up corrections after the main inquiry migration.

The script is idempotent and intentionally limited to the remaining D1/D2
Alchemist boundary and pure regression tests.
"""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ALCHEMIST = ROOT / "src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java"
LANG = ROOT / "src/main/resources/assets/hemomancy/lang/en_us.json"
SCOPE_TEST = ROOT / "src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/AlchemistDialogueScopeTest.java"
NODE_TEST = ROOT / "src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/DialogueItemInquiryNodesTest.java"
ACCESS_TEST = ROOT / "src/test/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/InquiryAccessPolicyTest.java"


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if old in text:
        return text.replace(old, new, 1)
    if new in text:
        return text
    raise RuntimeError(f"Could not locate {label}")


text = ALCHEMIST.read_text(encoding="utf-8")
text = replace_once(
    text,
    "/** Degree 1 — Neophyte. Introduces the Ghastly Alembic and basic blood processing. */",
    "/** Degree 1 — Neophyte. Introduces the Vial Centrifuge and basic sampling. */",
    "Neophyte comment",
)
text = replace_once(
    text,
    'new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_alembic", "alembic_lore", null),',
    'new DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge", "centrifuge_lore", null),',
    "Neophyte centrifuge option",
)
old_node = '''\t\t\t\t.addNode(new DialogueNode("alembic_lore", List.of(
\t\t\t\t\t\t"hemomancy.alchemist.neophyte.alembic_lore"
\t\t\t\t), List.of(
\t\t\t\t\t\tnew DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_gourds", "blood_gourd_basics",
\t\t\t\t\t\t\t\tnull),
\t\t\t\t\t\tnew DialogueOption("hemomancy.dialogue.alchemist.option.alembic_leak", "alembic_leak", null),
\t\t\t\t\t\tnew DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
\t\t\t\t)))
\t\t\t\t.addNode(new DialogueNode("alembic_leak", List.of(
\t\t\t\t\t\t"hemomancy.alchemist.neophyte.alembic_leak"
\t\t\t\t), List.of(
\t\t\t\t\t\tnew DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
\t\t\t\t)))'''
new_node = '''\t\t\t\t.addNode(new DialogueNode("centrifuge_lore", List.of(
\t\t\t\t\t\t"hemomancy.alchemist.neophyte.centrifuge_lore"
\t\t\t\t), List.of(
\t\t\t\t\t\tnew DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_blood_gourds", "blood_gourd_basics",
\t\t\t\t\t\t\t\tnull),
\t\t\t\t\t\tnew DialogueOption("hemomancy.dialogue.alchemist.option.leave", null, null)
\t\t\t\t)))'''
text = replace_once(text, old_node, new_node, "Neophyte machine node")
old_overview = '''\t\t\t\t.addNode(new DialogueNode("machines_overview", List.of(
\t\t\t\t\t\t"hemomancy.alchemist.machines_overview"
\t\t\t\t), List.of(
\t\t\t\t\t\tnew DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_alembic", "alembic_lore", null),'''
new_overview = '''\t\t\t\t.addNode(new DialogueNode("machines_overview", List.of(
\t\t\t\t\t\t"hemomancy.alchemist.machines_overview"
\t\t\t\t), List.of(
\t\t\t\t\t\tnew DialogueOption("hemomancy.dialogue.alchemist.option.tell_me_about_centrifuge", "centrifuge_lore", null),'''
text = replace_once(text, old_overview, new_overview, "Neophyte machine overview")
# The next remaining use of the old shared Alembic line belongs to Votary.
text = replace_once(
    text,
    '''\t\t\t\t.addNode(new DialogueNode("alembic_lore", List.of(
\t\t\t\t\t\t"hemomancy.alchemist.neophyte.alembic_lore"''',
    '''\t\t\t\t.addNode(new DialogueNode("alembic_lore", List.of(
\t\t\t\t\t\t"hemomancy.alchemist.votary.alembic_lore"''',
    "Votary Alembic localisation",
)
ALCHEMIST.write_text(text, encoding="utf-8")

lang = json.loads(LANG.read_text(encoding="utf-8"))
lang.update({
    "hemomancy.alchemist.neophyte.centrifuge_lore": (
        "Begin with the Vial Centrifuge. Draw a specimen with the Living Syringe, keep the vials in a rack, "
        "and set opposing samples in balance before you start the spin."
    ),
    "hemomancy.alchemist.votary.alembic_lore": (
        "Now the Ghastly Alembic is yours to study. It distils plants, fungi, and creature matter into defined "
        "reagents; fire, vessel state, and recipe determine whether the batch answers cleanly."
    ),
})
LANG.write_text(json.dumps(lang, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

SCOPE_TEST.write_text('''package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class AlchemistDialogueScopeTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void alchemistDoesNotTeachArtificerOrVicarSubjects() throws IOException {
        String alchemist = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java");
        String artificer = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerArtificerDialogueTrees.java");
        String vicar = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java");
        String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");

        assertFalse(alchemist.contains("tell_me_about_armature"));
        assertFalse(alchemist.contains("blood_structure_intro"));
        assertFalse(alchemist.contains("blood_crafting_lore"));
        assertFalse(lang.contains("hemomancy.alchemist.votary.armature_lore"));
        assertFalse(lang.contains("hemomancy.alchemist.votary.blood_structure_intro"));
        assertFalse(lang.contains("hemomancy.alchemist.illuminatus.blood_crafting_lore"));
        assertTrue(artificer.contains("teach_armature"));
        assertTrue(vicar.contains("ask_about_blood_crafting"));
    }

    @Test
    void neophyteTeachesCentrifugeAndVotaryOwnsAlembic() throws IOException {
        String source = read("src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java");
        String neophyte = between(source, "public static DialogueTree neophyte", "public static DialogueTree votary");
        String votary = between(source, "public static DialogueTree votary", "public static DialogueTree initiate");

        assertTrue(neophyte.contains("tell_me_about_centrifuge"));
        assertTrue(neophyte.contains("neophyte.centrifuge_lore"));
        assertFalse(neophyte.contains("tell_me_about_alembic"));
        assertFalse(neophyte.contains("alembic_leak"));
        assertTrue(votary.contains("tell_me_about_alembic"));
        assertTrue(votary.contains("votary.alembic_lore"));
    }

    private static String between(String source, String start, String end) {
        int from = source.indexOf(start);
        int to = source.indexOf(end, from + start.length());
        assertTrue(from >= 0 && to > from, "Dialogue source boundary not found");
        return source.substring(from, to);
    }

    private static String read(String path) throws IOException {
        return Files.readString(ROOT.resolve(path)).replace("\\r\\n", "\\n");
    }
}
''', encoding="utf-8")

NODE_TEST.write_text('''package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

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

        DialogueItemInquiryNodes.addResolvedInventoryItemInquiries(tree, resolved, false, "vicar");

        List<String> nodes = tree.nodes().keySet().stream()
                .filter(key -> key.startsWith("item_inquiry/hemomancy/mnemonic_blueprint#"))
                .toList();
        assertEquals(2, nodes.size());
        assertFalse(nodes.get(0).equals(nodes.get(1)));
    }

    @Test
    void unsupportedInventoryCreatesOneUnknownTopic() {
        DialogueTree tree = inquiryTree();
        DialogueItemInquiryNodes.addResolvedInventoryItemInquiries(tree, List.of(), true, "guardian");

        assertTrue(tree.nodes().containsKey(DialogueItemInquiryNodes.UNKNOWN_NODE_ID));
        assertEquals(List.of("hemomancy.guardian.item_inquiry.unknown"),
                tree.getNode(DialogueItemInquiryNodes.UNKNOWN_NODE_ID).lines());
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
''', encoding="utf-8")

ACCESS_TEST.parent.mkdir(parents=True, exist_ok=True)
ACCESS_TEST.write_text('''package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InquiryAccessPolicyTest {
    @Test
    void clarityAlchemistHasNoOperationalInquiry() {
        ItemInquiryContext context = new ItemInquiryContext(0, 100, 25, true, false, true, false, false);
        assertEquals(InquiryAccessPolicy.Access.NONE, InquiryAccessPolicy.accessFor("alchemist", context));
    }

    @Test
    void purifyingAlchemistAndArtificerOnlyIdentify() {
        ItemInquiryContext context = new ItemInquiryContext(4, 50, 0, false, true, true, false, false);
        assertEquals(InquiryAccessPolicy.Access.IDENTIFICATION_ONLY,
                InquiryAccessPolicy.accessFor("alchemist", context));
        assertEquals(InquiryAccessPolicy.Access.IDENTIFICATION_ONLY,
                InquiryAccessPolicy.accessFor("artificer", context));
    }

    @Test
    void normalPractitionerRetainsFullInquiry() {
        ItemInquiryContext context = new ItemInquiryContext(4, 0, 0, false, true, false, false, false);
        assertEquals(InquiryAccessPolicy.Access.FULL, InquiryAccessPolicy.accessFor("alchemist", context));
        assertEquals(InquiryAccessPolicy.Access.FULL, InquiryAccessPolicy.accessFor("artificer", context));
    }
}
''', encoding="utf-8")

print("Applied dialogue inquiry follow-up corrections.")
