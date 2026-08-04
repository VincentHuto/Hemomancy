#!/usr/bin/env python3
"""Apply the approved 2026-08-04 dialogue-inquiry canon and runtime sync.

This migration is intentionally idempotent. It writes the small Java contract files,
patches the existing inquiry loader/hub, updates inquiry localisation through JSON,
and rewrites the authoring guide. It is removed after the branch passes verification.
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "src/main/java/com/vincenthuto/hemomancy"
TEST = ROOT / "src/test/java/com/vincenthuto/hemomancy"
LANG = ROOT / "src/main/resources/assets/hemomancy/lang/en_us.json"


def write(path: Path, content: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content.rstrip() + "\n", encoding="utf-8")


def replace(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    if old not in text:
        if new in text:
            return
        raise RuntimeError(f"Expected text not found in {path}: {old[:120]!r}")
    path.write_text(text.replace(old, new), encoding="utf-8")


INQUIRY = JAVA / "common/entity/npc/dialogue/inquiry"
DIALOGUE = JAVA / "common/entity/npc/dialogue"

write(INQUIRY / "ItemInquiryContext.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.world.entity.player.Player;

/** Stable player state used while resolving inventory inquiry branches. */
public record ItemInquiryContext(
        int degree,
        float purity,
        float clarity,
        boolean clarityUnlocked,
        boolean activeBlood,
        boolean purifying,
        boolean silentArchon,
        boolean apotheos
) {
    public static ItemInquiryContext legacy(int degree, float purity) {
        return new ItemInquiryContext(degree, purity, 0F, false, degree > 0, purity > 0F, false, degree >= 8);
    }

    public static ItemInquiryContext from(Player player) {
        int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
        float[] unstained = new float[2];
        boolean[] flags = new boolean[2];
        HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
            unstained[0] = progress.getPurity();
            unstained[1] = progress.getClarity();
            flags[0] = progress.hasBegunPurification();
            flags[1] = progress.hasClarityUnlocked();
        });
        boolean activeBlood = HemoCapabilityAccess.getBloodVolume(player).map(volume -> volume.isActive()).orElse(false);
        boolean silent = player.getPersistentData().getBoolean("hemomancy.silent_archon")
                || player.getPersistentData().getBoolean("silent_archon");
        return new ItemInquiryContext(degree, unstained[0], unstained[1], flags[1], activeBlood,
                flags[0], silent, degree >= 8);
    }
}
''')

write(INQUIRY / "ItemInquiryCondition.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import java.util.List;

/** One ordered conditional branch inside an {@link ItemInquiryEntry}. */
public record ItemInquiryCondition(
        int minDegree,
        int maxDegree,
        float minPurity,
        float maxPurity,
        float minClarity,
        float maxClarity,
        Boolean clarityUnlocked,
        Boolean requiresActiveBlood,
        Boolean requiresPurifying,
        List<String> lines
) {
    /** Compatibility constructor for the original degree/purity schema. */
    public ItemInquiryCondition(int minDegree, int maxDegree, float minPurity, float maxPurity,
            List<String> lines) {
        this(minDegree, maxDegree, minPurity, maxPurity, -1F, -1F, null, null, null, lines);
    }

    public static ItemInquiryCondition unconditional(List<String> lines) {
        return new ItemInquiryCondition(-1, -1, -1F, -1F, -1F, -1F, null, null, null, lines);
    }

    public boolean matches(ItemInquiryContext context) {
        if (minDegree >= 0 && context.degree() < minDegree) return false;
        if (maxDegree >= 0 && context.degree() > maxDegree) return false;
        if (minPurity >= 0 && context.purity() < minPurity) return false;
        if (maxPurity >= 0 && context.purity() > maxPurity) return false;
        if (minClarity >= 0 && context.clarity() < minClarity) return false;
        if (maxClarity >= 0 && context.clarity() > maxClarity) return false;
        if (clarityUnlocked != null && context.clarityUnlocked() != clarityUnlocked) return false;
        if (requiresActiveBlood != null && context.activeBlood() != requiresActiveBlood) return false;
        if (requiresPurifying != null && context.purifying() != requiresPurifying) return false;
        return true;
    }

    public boolean matches(int degree, float purity) {
        return matches(ItemInquiryContext.legacy(degree, purity));
    }

    public boolean isUnconstrained() {
        return minDegree < 0 && maxDegree < 0 && minPurity < 0 && maxPurity < 0
                && minClarity < 0 && maxClarity < 0 && clarityUnlocked == null
                && requiresActiveBlood == null && requiresPurifying == null;
    }
}
''')

write(INQUIRY / "ItemInquiryEntry.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import java.util.List;
import java.util.Optional;

/** Loaded representation of one item-inquiry JSON file. */
public record ItemInquiryEntry(List<ItemInquiryCondition> conditions) {
    public Optional<List<String>> resolve(ItemInquiryContext context) {
        for (ItemInquiryCondition branch : conditions) {
            if (branch.matches(context)) return Optional.of(branch.lines());
        }
        return Optional.empty();
    }

    public Optional<List<String>> resolve(int degree, float purity) {
        return resolve(ItemInquiryContext.legacy(degree, purity));
    }
}
''')

write(INQUIRY / "ItemInquiryRegistry.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Runtime store for datapack-backed inventory inquiry mappings. */
public final class ItemInquiryRegistry {
    public static final ItemInquiryRegistry INSTANCE = new ItemInquiryRegistry();
    private final Map<String, Map<ResourceLocation, ItemInquiryEntry>> data = new HashMap<>();

    private ItemInquiryRegistry() {}

    public void reload(Map<String, Map<ResourceLocation, ItemInquiryEntry>> freshData) {
        data.clear();
        data.putAll(freshData);
    }

    public Optional<List<String>> resolve(String npcId, ResourceLocation itemId, ItemInquiryContext context) {
        ItemInquiryEntry entry = data.getOrDefault(npcId, Collections.emptyMap()).get(itemId);
        return entry == null ? Optional.empty() : entry.resolve(context);
    }

    public Optional<List<String>> resolve(String npcId, ResourceLocation itemId, int degree, float purity) {
        return resolve(npcId, itemId, ItemInquiryContext.legacy(degree, purity));
    }

    public Map<String, Map<ResourceLocation, ItemInquiryEntry>> allEntries() {
        return Collections.unmodifiableMap(data);
    }
}
''')

write(INQUIRY / "InquiryAccessPolicy.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

/** Explicitly enforces the instructional limits claimed by refusal dialogue. */
public final class InquiryAccessPolicy {
    public enum Access { FULL, IDENTIFICATION_ONLY, NONE }

    private InquiryAccessPolicy() {}

    public static Access accessFor(String speakerId, ItemInquiryContext context) {
        if ("alchemist".equals(speakerId)) {
            if (context.clarityUnlocked()) return Access.NONE;
            if (context.purifying()) return Access.IDENTIFICATION_ONLY;
        }
        if ("artificer".equals(speakerId) && (context.purifying() || context.clarityUnlocked())) {
            return Access.IDENTIFICATION_ONLY;
        }
        return Access.FULL;
    }

    public static String refusalKey(String speakerId) {
        return "hemomancy." + speakerId + ".item_inquiry.refusal";
    }
}
''')

write(INQUIRY / "StackAwareInquiryProvider.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/** Resolves data-bearing stacks before ordinary registry-ID inquiry lookup. */
@FunctionalInterface
public interface StackAwareInquiryProvider {
    Optional<StackAwareInquiryRegistry.ResolvedStackInquiry> resolve(
            String speakerId, ItemStack stack, ItemInquiryContext context);
}
''')

write(INQUIRY / "StackAwareInquiryRegistry.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.util.SpecimenJarData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Ordered built-in providers for stateful inquiry items. */
public final class StackAwareInquiryRegistry {
    private static final List<StackAwareInquiryProvider> PROVIDERS = List.of(
            StackAwareInquiryRegistry::blueprint,
            StackAwareInquiryRegistry::scarPattern,
            StackAwareInquiryRegistry::specimenJar
    );

    private StackAwareInquiryRegistry() {}

    public static Optional<ResolvedStackInquiry> resolve(String speakerId, ItemStack stack,
            ItemInquiryContext context) {
        for (StackAwareInquiryProvider provider : PROVIDERS) {
            Optional<ResolvedStackInquiry> result = provider.resolve(speakerId, stack, context);
            if (result.isPresent()) return result;
        }
        return Optional.empty();
    }

    private static Optional<ResolvedStackInquiry> blueprint(String speakerId, ItemStack stack,
            ItemInquiryContext context) {
        if (!(stack.getItem() instanceof MnemonicBlueprintItem)
                || !("vicar".equals(speakerId) || "mnemonist".equals(speakerId))) return Optional.empty();
        MnemonicBlueprintTarget target = MnemonicBlueprintItem.getTarget(stack);
        String state;
        String detail;
        if (target == null) {
            state = "blank";
            detail = "Blank plan: no rite or blood structure has been impressed into it.";
        } else {
            state = target.type() == MnemonicBlueprintTarget.Type.CARDINAL_RITE ? "rite" : "structure";
            detail = proper(target.recipeId().getPath()) + " (" + target.recipeId() + ")";
        }
        return Optional.of(new ResolvedStackInquiry(
                "blueprint/" + state + "/" + Integer.toHexString(detail.hashCode()),
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                List.of("hemomancy." + speakerId + ".item_inquiry.mnemonic_blueprint." + state,
                        literal(detail))));
    }

    private static Optional<ResolvedStackInquiry> scarPattern(String speakerId, ItemStack stack,
            ItemInquiryContext context) {
        if (!(stack.getItem() instanceof ItemScarPattern)
                || !("mnemonist".equals(speakerId) || "vicar".equals(speakerId))) return Optional.empty();
        List<ResourceLocation> ids = new ArrayList<>(ItemScarPattern.getScarIds(stack));
        ids.sort(Comparator.comparing(ResourceLocation::toString));
        String state = ids.isEmpty() ? "blank" : ids.size() == 1 ? "template" : "loadout";
        String detail = ids.isEmpty() ? "No scar route is written into this motif."
                : "Written routes: " + ids.stream().map(id -> proper(id.getPath())).reduce((a, b) -> a + ", " + b).orElse("");
        String fingerprint = ids.stream().map(ResourceLocation::toString).reduce((a, b) -> a + ";" + b).orElse("blank");
        return Optional.of(new ResolvedStackInquiry(
                "scar_pattern/" + Integer.toHexString(fingerprint.hashCode()),
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                List.of("hemomancy." + speakerId + ".item_inquiry.scar_pattern." + state, literal(detail))));
    }

    private static Optional<ResolvedStackInquiry> specimenJar(String speakerId, ItemStack stack,
            ItemInquiryContext context) {
        if (!SpecimenJarData.hasSpecimen(stack)
                || !("alchemist".equals(speakerId) || "voyager".equals(speakerId)
                || "votary_wayfarer".equals(speakerId))) return Optional.empty();
        Optional<ResourceLocation> specimen = SpecimenJarData.getSpecimenEntityId(stack);
        if (specimen.isEmpty()) return Optional.empty();
        var tag = SpecimenJarData.getSpecimen(stack);
        String layers = SpecimenJarData.getMorphlingLayers(tag).stream()
                .map(layer -> proper(layer.name().toLowerCase(Locale.ROOT)))
                .reduce((a, b) -> a + ", " + b).orElse("");
        String detail = "Contained specimen: " + proper(specimen.get().getPath());
        if (!layers.isBlank()) detail += ". Recorded layers: " + layers;
        return Optional.of(new ResolvedStackInquiry(
                "specimen/" + specimen.get() + "/" + Integer.toHexString(layers.hashCode()),
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                List.of("hemomancy." + speakerId + ".item_inquiry.specimen_jar", literal(detail))));
    }

    private static String proper(String value) {
        String path = value.contains("/") ? value.substring(value.lastIndexOf('/') + 1) : value;
        String[] words = path.replace('-', '_').split("_");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!result.isEmpty()) result.append(' ');
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    private static String literal(String text) {
        return "@literal:" + text;
    }

    public record ResolvedStackInquiry(String presentationKey, ResourceLocation itemId, List<String> lines) {}
}
''')

write(DIALOGUE / "DialogueItemInquiryNodes.java", r'''
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
    static final String UNKNOWN_NODE_ID = "item_inquiry/unknown";

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
        boolean unsupported = false;
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
                unsupported = true;
                continue;
            }
            String key = itemId.toString();
            if (seen.add(key)) {
                resolved.put(key, new ResolvedInventoryInquiry(itemId.toString(), itemId,
                        restrictedLines(access, speakerKey, lines.get())));
            }
        }
        return addResolvedInventoryItemInquiries(tree, resolved.values(), unsupported, speakerKey);
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
        return addResolvedInventoryItemInquiries(tree, converted, false, "generic");
    }

    static DialogueTree addResolvedInventoryItemInquiries(DialogueTree tree,
            Iterable<ResolvedInventoryInquiry> resolvedItems, boolean hasUnsupported, String speakerKey) {
        if (!tree.nodes().containsKey("item_hint")) return tree;
        DialogueNode template = tree.getNode("item_hint");
        List<DialogueOption> inquiryOptions = template.options().stream().filter(option -> !isLegacyLeave(option)).toList();
        for (ResolvedInventoryInquiry entry : resolvedItems) {
            String nodeId = inventoryNodeId(entry.itemId(), entry.presentationKey());
            tree.nodes().put(nodeId, new DialogueNode(nodeId, entry.lines(), inquiryOptions));
        }
        if (hasUnsupported) {
            tree.nodes().put(UNKNOWN_NODE_ID, new DialogueNode(UNKNOWN_NODE_ID,
                    List.of("hemomancy." + speakerKey + ".item_inquiry.unknown"), inquiryOptions));
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
        if (!nodeId.startsWith(INVENTORY_NODE_PREFIX) || UNKNOWN_NODE_ID.equals(nodeId)) return null;
        String value = nodeId.substring(INVENTORY_NODE_PREFIX.length());
        int hash = value.indexOf('#');
        if (hash >= 0) value = value.substring(0, hash);
        int separator = value.indexOf('/');
        if (separator <= 0 || separator == value.length() - 1) return null;
        return ResourceLocation.tryBuild(value.substring(0, separator), value.substring(separator + 1));
    }

    static boolean isUnknownNode(String nodeId) {
        return UNKNOWN_NODE_ID.equals(nodeId);
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
''')

# Expand the existing loader without replacing its resource scan behaviour.
loader = INQUIRY / "ItemInquiryLoader.java"
text = loader.read_text(encoding="utf-8")
text = text.replace(
'''\t\tint minDegree   = parseIntField(obj, "min_degree", -1, src);\n\t\tint maxDegree   = parseIntField(obj, "max_degree", -1, src);\n\t\tfloat minPurity = parseFloatField(obj, "min_purity", -1f, src);\n\t\tfloat maxPurity = parseFloatField(obj, "max_purity", -1f, src);\n\n\t\treturn new ItemInquiryCondition(minDegree, maxDegree, minPurity, maxPurity, lines);''',
'''\t\tint minDegree = parseIntField(obj, "min_degree", -1, src);\n\t\tint maxDegree = parseIntField(obj, "max_degree", -1, src);\n\t\tfloat minPurity = parseFloatField(obj, "min_purity", -1f, src);\n\t\tfloat maxPurity = parseFloatField(obj, "max_purity", -1f, src);\n\t\tfloat minClarity = parseFloatField(obj, "min_clarity", -1f, src);\n\t\tfloat maxClarity = parseFloatField(obj, "max_clarity", -1f, src);\n\t\tBoolean clarityUnlocked = parseBooleanField(obj, "clarity_unlocked", src);\n\t\tBoolean requiresActiveBlood = parseBooleanField(obj, "requires_active_blood", src);\n\t\tBoolean requiresPurifying = parseBooleanField(obj, "requires_purifying", src);\n\n\t\tif (!validRange(minDegree, maxDegree) || !validRange(minPurity, maxPurity)\n\t\t\t\t|| !validRange(minClarity, maxClarity) || !validPercentage(minPurity)\n\t\t\t\t|| !validPercentage(maxPurity) || !validPercentage(minClarity)\n\t\t\t\t|| !validPercentage(maxClarity)) {\n\t\t\tLOGGER.warn("[ItemInquiryLoader] Invalid condition range in: {}", src);\n\t\t\treturn null;\n\t\t}\n\n\t\treturn new ItemInquiryCondition(minDegree, maxDegree, minPurity, maxPurity, minClarity, maxClarity,\n\t\t\t\tclarityUnlocked, requiresActiveBlood, requiresPurifying, lines);''')
needle = '''\tprivate static float parseFloatField(JsonObject obj, String field, float def, ResourceLocation src) {\n\t\tif (!obj.has(field)) return def;\n\t\ttry {\n\t\t\treturn obj.get(field).getAsFloat();\n\t\t} catch (Exception ex) {\n\t\t\tLOGGER.warn("[ItemInquiryLoader] Field '{}' is not a number in: {} — using default {}",\n\t\t\t\t\tfield, src, def);\n\t\t\treturn def;\n\t\t}\n\t}\n'''
addition = needle + '''\n\tprivate static Boolean parseBooleanField(JsonObject obj, String field, ResourceLocation src) {\n\t\tif (!obj.has(field)) return null;\n\t\ttry {\n\t\t\treturn obj.get(field).getAsBoolean();\n\t\t} catch (Exception ex) {\n\t\t\tLOGGER.warn("[ItemInquiryLoader] Field '{}' is not a boolean in: {}", field, src);\n\t\t\treturn null;\n\t\t}\n\t}\n\n\tprivate static boolean validRange(double min, double max) {\n\t\treturn min < 0 || max < 0 || min <= max;\n\t}\n\n\tprivate static boolean validPercentage(float value) {\n\t\treturn value < 0 || value <= 100F;\n\t}\n'''
if needle not in text and "parseBooleanField" not in text:
    raise RuntimeError("Could not patch ItemInquiryLoader helpers")
text = text.replace(needle, addition)
loader.write_text(text, encoding="utf-8")

# Teach the hub about the single explicit unknown topic.
hub = DIALOGUE / "DialogueHubFactory.java"
text = hub.read_text(encoding="utf-8")
old = '''\t\tfor (String nodeId : nodes.keySet()) {\n\t\t\tResourceLocation itemId = DialogueItemInquiryNodes.inventoryItemId(nodeId);\n\t\t\tif (itemId == null) continue;\n\t\t\ttopics.add(DialogueTopic.available(\n\t\t\t\t\t"inquiries/" + itemId.getNamespace() + "/" + itemId.getPath(),\n\t\t\t\t\tDialogueCategory.INQUIRIES, "hemomancy.dialogue.topic.inventory_item",\n\t\t\t\t\t"hemomancy.dialogue.topic.inventory_item.summary", nodeId).withItem(itemId));\n\t\t}\n'''
new = '''\t\tfor (String nodeId : nodes.keySet()) {\n\t\t\tif (DialogueItemInquiryNodes.isUnknownNode(nodeId)) {\n\t\t\t\ttopics.add(DialogueTopic.available("inquiries/unknown", DialogueCategory.INQUIRIES,\n\t\t\t\t\t\t"hemomancy.dialogue.topic.unknown_item",\n\t\t\t\t\t\t"hemomancy.dialogue.topic.inventory_item.summary", nodeId));\n\t\t\t\tcontinue;\n\t\t\t}\n\t\t\tResourceLocation itemId = DialogueItemInquiryNodes.inventoryItemId(nodeId);\n\t\t\tif (itemId == null) continue;\n\t\t\ttopics.add(DialogueTopic.available(\n\t\t\t\t\t"inquiries/" + nodeId.substring(DialogueItemInquiryNodes.INVENTORY_NODE_PREFIX.length()),\n\t\t\t\t\tDialogueCategory.INQUIRIES, "hemomancy.dialogue.topic.inventory_item",\n\t\t\t\t\t"hemomancy.dialogue.topic.inventory_item.summary", nodeId).withItem(itemId));\n\t\t}\n'''
if old not in text and new not in text:
    raise RuntimeError("Could not patch DialogueHubFactory inquiry loop")
text = text.replace(old, new)
hub.write_text(text, encoding="utf-8")

# Allow server-authored literal detail lines for stateful stacks; ordinary lines remain translations.
node = DIALOGUE / "DialogueNode.java"
text = node.read_text(encoding="utf-8")
text = text.replace('public record DialogueNode(String id, List<String> lines, List<DialogueOption> options) {',
'''public record DialogueNode(String id, List<String> lines, List<DialogueOption> options) {\n\tpublic static final String LITERAL_PREFIX = "@literal:";\n\n\tpublic static String literal(String value) {\n\t\treturn LITERAL_PREFIX + value;\n\t}''')
node.write_text(text, encoding="utf-8")

screen = ROOT / "src/main/java/com/vincenthuto/hemomancy/client/screen/dialogue/DialogueScreen.java"
text = screen.read_text(encoding="utf-8")
text = text.replace('gfx.renderTooltip(font, hoveredInquiryItem.getHoverName(), mouseX, mouseY);',
                    'gfx.renderTooltip(font, hoveredInquiryItem, mouseX, mouseY);')
text = text.replace('font.split(Component.translatable(key), textWidth)', 'font.split(resolveDialogueLine(key), textWidth)')
text = text.replace('font.split(Component.translatable(key), width)', 'font.split(resolveDialogueLine(key), width)')
marker = '\tprivate void renderHeader(GuiGraphics gfx) {'
helper = '''\tprivate static Component resolveDialogueLine(String value) {\n\t\treturn value.startsWith(DialogueNode.LITERAL_PREFIX)\n\t\t\t\t? Component.literal(value.substring(DialogueNode.LITERAL_PREFIX.length()))\n\t\t\t\t: Component.translatable(value);\n\t}\n\n'''
if helper not in text:
    text = text.replace(marker, helper + marker)
screen.write_text(text, encoding="utf-8")

# Main runtime language corrections.
data = json.loads(LANG.read_text(encoding="utf-8"))
updates = {
    # Missing shared keys
    "hemomancy.alchemist.item_inquiry.minecraft_reagent.line1": "Common apparatus, but still worth reading properly. Shape, heat tolerance, and residue tell me what work it can survive.",
    "hemomancy.alchemist.item_inquiry.minecraft_reagent.line2": "Bring it to a bench when a formula calls for it. Do not invent a hemomantic use merely because the vessel is familiar.",
    "hemomancy.guardian.item_inquiry.minecraft_arms.line1": "A serviceable field implement. Keep it clean, keep your reach, and do not open blood without cause.",
    "hemomancy.guardian.item_inquiry.minecraft_arms.line2": "Against active infection, distance and restraint matter more than ornament.",
    "hemomancy.vicar.item_inquiry.minecraft_relic.line1": "An old instrument of measure, direction, or return. The Order respects such things without pretending every relic belongs to us.",
    "hemomancy.vicar.item_inquiry.minecraft_relic.line2": "Its meaning comes from the work placed around it, not from age alone.",
    "hemomancy.votary_wayfarer.item_inquiry.minecraft_fieldkit.line1": "Ordinary field gear. That is praise, not dismissal; most voyages fail through neglected ordinary things.",
    "hemomancy.votary_wayfarer.item_inquiry.minecraft_fieldkit.line2": "I mark mine before departure. The sea is very good at making identical tools belong to nobody.",
    "hemomancy.voyager.item_inquiry.minecraft_salvage.line1": "Survey salvage. Record where it lay before deciding what it means.",
    "hemomancy.voyager.item_inquiry.minecraft_salvage.line2": "A wreck turns every commonplace object into evidence, but not every piece of evidence is a revelation.",
    "hemomancy.zealot.item_inquiry.minecraft_ritegoods.line1": "A plain material suited to patient work. Purification is built from such things more often than from miracles.",
    "hemomancy.zealot.item_inquiry.minecraft_ritegoods.line2": "Use it where the rite asks. Simplicity does not make an offering lesser.",

    # Hub and refusal contract
    "hemomancy.dialogue.topic.unknown_item": "Another object",
    "hemomancy.alchemist.item_inquiry.refusal": "I recognise the object. I will not teach blood-work to a vessel deliberately washing it away.",
    "hemomancy.artificer.item_inquiry.refusal": "I know the work. Your blood has refused my frame, so the procedure ends there.",
    "hemomancy.alchemist.item_hint": "Show me what you carry. I will name what belongs to my bench.",
    "hemomancy.artificer.item_hint": "Set out what you carry. I will tell you which pieces belong to my frame.",
    "hemomancy.guardian.item_hint": "Choose the object from your pack. I will tell you whether it belongs in the field.",
    "hemomancy.mnemonist.item_hint": "Open what you carry. I will tell you which objects remember, and which merely pretend.",
    "hemomancy.vicar.item_hint": "Show me the objects in your keeping. I will speak where doctrine or history gives me warrant.",
    "hemomancy.zealot.item_hint": "Show me what you carry. I will tell you how the Church understands it.",
    "hemomancy.voyager.item_hint": "Lay out the samples you carry. Evidence is more honest when one can point to it.",
    "hemomancy.votary_wayfarer.item_hint": "Show me what is in your pack. I know less than the captain-scholar, but I may notice something useful.",
    "hemomancy.monolith.item_hint": "Present what you carry. I will answer only where it touches the threshold.",

    # Stateful inquiry templates
    "hemomancy.vicar.item_inquiry.mnemonic_blueprint.blank": "A blank mnemonic blueprint: disciplined emptiness, awaiting a rite or blood structure you have legitimately learned.",
    "hemomancy.vicar.item_inquiry.mnemonic_blueprint.rite": "This blueprint preserves the spatial memory of a Cardinal Rite. It guides construction; it does not confer the rite's authority.",
    "hemomancy.vicar.item_inquiry.mnemonic_blueprint.structure": "This blueprint remembers a blood structure. The plan may show the form, but Blood Projection must still make the form answer.",
    "hemomancy.mnemonist.item_inquiry.mnemonic_blueprint.blank": "Blank, but not ignorant. A blueprint waits for spatial memory rather than a manipulation lesson.",
    "hemomancy.mnemonist.item_inquiry.mnemonic_blueprint.rite": "A rite has been impressed here as remembered distance, order, and relation. It is a map of memory, not memory's power.",
    "hemomancy.mnemonist.item_inquiry.mnemonic_blueprint.structure": "A blood structure has been fixed into portable recollection. It can remind the hand; it cannot replace understanding.",
    "hemomancy.mnemonist.item_inquiry.scar_pattern.blank": "An unwritten motif. The Effigy has not yet persuaded blood to leave a route upon it.",
    "hemomancy.mnemonist.item_inquiry.scar_pattern.template": "A single scar route is written here: a carving lesson for the Cerebral Scarring Station.",
    "hemomancy.mnemonist.item_inquiry.scar_pattern.loadout": "Several known routes share this motif. Burn it in an empty, lit Iron Brazier to commit the whole arrangement.",
    "hemomancy.vicar.item_inquiry.scar_pattern.blank": "Unwritten motif paper. The Vein-Mason would call it a promise not yet made.",
    "hemomancy.vicar.item_inquiry.scar_pattern.template": "A scar template: instruction for carving one route before that route is burned into memory.",
    "hemomancy.vicar.item_inquiry.scar_pattern.loadout": "A prepared scar loadout. The Mason's Effigy arranged known routes; the Iron Brazier will make the arrangement active.",
    "hemomancy.alchemist.item_inquiry.specimen_jar": "A sealed living specimen. I can identify what is stored without pretending the jar has made it harmless.",
    "hemomancy.voyager.item_inquiry.specimen_jar": "A field specimen with its context partly preserved. Record the place, depth, and behaviour before opening it.",
    "hemomancy.votary_wayfarer.item_inquiry.specimen_jar": "Something in the jar has already taught the glass how to feel too small. I wrote that down before deciding it was foolish.",

    # Alchemist canon sync
    "hemomancy.alchemist.neophyte.alembic_lore": "The Ghastly Alembic is Votary work. Begin with the Vial Centrifuge: learn to sample blood, balance the rack, and separate one answer cleanly before you attempt distillation.",
    "hemomancy.alchemist.machines_overview": "The Centrifuge separates sampled blood. The Alembic distils reagents. The Loom weaves prepared memories. Learn the chain in that order; each machine assumes you survived the previous lesson.",
    "hemomancy.alchemist.votary.centrifuge_lore": "The Vial Centrifuge is the first separation: sampled vials enter in balance and emerge as one of eight enzyme expressions. At your degree, the Ghastly Alembic also opens the wider work of reagent distillation.",
    "hemomancy.alchemist.votary.tendency_lore": "Eight tendencies answer through the enzymes: Vivacious, Fervent, Neurotic, Incandescent, Ruinous, Frigid, Ferric, and Umbral. Neurotic carries impulse and motion; it is not merely softness given a grand name.",
    "hemomancy.alchemist.item_inquiry.blood_vial.line1": "A Blood Vial belongs in the Vial Rack. Draw the sample with a Living Syringe; the Alembic does not fill these for you.",
    "hemomancy.alchemist.item_inquiry.blood_vial.line2": "Balance sampled vials in the Centrifuge and recover the separated enzyme. That is the clean loop.",
    "hemomancy.alchemist.item_inquiry.bloody_flask.line1": "A Bloody Flask is a portable reserve, not part of the syringe-and-rack assay.",
    "hemomancy.alchemist.item_inquiry.bloody_flask.line2": "Use it when blood must travel. Do not feed it to a machine merely because both contain red fluid.",
    "hemomancy.alchemist.item_inquiry.living_syringe.line1": "The Living Syringe draws a controlled specimen into an empty vial held by your rack.",
    "hemomancy.alchemist.item_inquiry.living_syringe.line2": "It is a sampling instrument first. Any violence done with it is misuse, not its governing principle.",
    "hemomancy.alchemist.item_inquiry.vial_centrifuge.line1": "The Vial Centrifuge separates properly sampled blood into one of the eight enzyme expressions.",
    "hemomancy.alchemist.item_inquiry.vial_centrifuge.line2": "Opposed slots must balance. A machine that spins blood without balance teaches the room instead of the practitioner.",
    "hemomancy.alchemist.item_inquiry.recycled_enzyme.line1": "Recycled Enzyme is salvage-grade reagent, weaker and less specific than a fresh separation.",
    "hemomancy.alchemist.item_inquiry.recycled_enzyme.line2": "Do not dismantle learned memories expecting to recover it. The modern Loom does not return what it has woven.",
    "hemomancy.alchemist.item_inquiry.iron_ingot.line1": "Ordinary iron has no hematic obedience. Hematic Iron is taught through Blood Projection and recovered through the Order's salvage practices, not by standing an ingot near a dramatic furnace.",
    "hemomancy.alchemist.item_inquiry.iron_ingot.line2": "Use the blood-structure plan or bring damaged iron through the proven salvage route. Heat alone cannot teach the metal your pulse.",
    "hemomancy.alchemist.item_inquiry.mnemonic_reliquary.line1": "The Mnemonic Reliquary arranges manipulations you already know into the active lessons nearest your pulse.",
    "hemomancy.alchemist.item_inquiry.mnemonic_reliquary.line2": "It teaches nothing. The memory item teaches; the Reliquary only chooses readiness.",
    "hemomancy.alchemist.item_inquiry.scar_blank.line1": "A blank Scar item is material for the Cerebral Scarring Station. Pattern and catalyst determine the route carved into it.",
    "hemomancy.alchemist.item_inquiry.scar_blank.line2": "After carving, the Scar item is burned in an empty, lit Iron Brazier so the route becomes known. It is not worn as a plate.",
    "hemomancy.alchemist.item_inquiry.scar_station.line1": "The Cerebral Scarring Station carves a registered route into a blank Scar item from a dynamic template and its catalyst.",
    "hemomancy.alchemist.item_inquiry.scar_station.line2": "The station makes the instruction. The Vein-Mason's rite, Effigy, and Brazier decide how that instruction enters practice.",
    "hemomancy.alchemist.item_inquiry.somatic_loom.line1": "The Somatic Loom takes a blank Hematic Memory, the exact catalyst, stored enzyme units, and projected blood.",
    "hemomancy.alchemist.item_inquiry.somatic_loom.line2": "When the weave begins, coloured memory-orbs scatter. Draw every required orb home with the Living Staff or the lesson does not crystallise.",
    "hemomancy.alchemist.initiate.loom_lore": "The Somatic Loom is a physical memory rite: prepare a blank vessel and catalyst, store the required enzyme colours, project blood, then draw the scattered memory-orbs home with the Living Staff.",
    "hemomancy.alchemist.initiate.memory_weaving": "Memory weaving does not equip a crystal at the Reliquary. The completed memory teaches its manipulation; the Reliquary later arranges which known lessons remain active.",
    "hemomancy.alchemist.item_inquiry.morphling_incubator.line1": "The Morphling Incubator stabilises cultivated fungal symbionts. They are living dependants, not obedient constructs.",
    "hemomancy.alchemist.item_inquiry.morphling_incubator.line2": "Strain, enzyme, and husbandry shape what develops. The Incubator supplies conditions; it does not manufacture a servant from nothing.",
    "hemomancy.alchemist.illuminatus.incubator_lore": "The Morphling Incubator raises cultivated fungal symbionts through controlled conditions. Wild polyps carry possibilities; incubation makes one strain stable enough to live with you.",
    "hemomancy.alchemist.item_inquiry.spore_sac.line1": "A concentrated fungal specimen. Keep it sealed; dried does not mean inert.",
    "hemomancy.alchemist.item_inquiry.spore_sac.line2": "Its use belongs to current spore and fungal recipes. Do not mistake it for the retired implantation-pylon workflow.",
    "hemomancy.alchemist.item_inquiry.living_weapon_graft.line1": "Prepared biological instruction for a Living Staff. I can judge its material stability; the Redwright owns the rite.",
    "hemomancy.alchemist.item_inquiry.living_weapon_graft.line2": "Ask the Artificer how the Iron Brazier and Blood Absorption make the staff accept the limb-pattern.",
    "hemomancy.alchemist.item_inquiry.enzyme_neurotic.line1": "Neurotic Enzyme — Ductilis tendency. Nerve impulse, acceleration, transmission, and motion made chemically legible.",
    "hemomancy.alchemist.item_inquiry.enzyme_neurotic.line2": "It belongs wherever a recipe asks blood to carry a signal rather than merely hold a shape.",
    "hemomancy.alchemist.item_inquiry.slime_ball.line1": "Slime is adhesive and elastic, but that resemblance does not make it a canonical Ductilis assay by itself.",
    "hemomancy.alchemist.item_inquiry.slime_ball.line2": "Use it only where a live recipe calls for it. Analogy is not a centrifuge result.",

    # Vicar canon sync
    "hemomancy.vicar.neophyte.line2": "Neophyte of the Crimson Veil. You have crossed the first threshold. The path before you is long, and the Order will ask for discipline before revelation.",
    "hemomancy.vicar.votary.tendency_lore": "The Covenant recognises eight blood tendencies: Animus, Flammeus, Ductilis, Lux, Mortem, Congeatio, Ferric, and Tenebris. Their common enzyme names are Vivacious, Fervent, Neurotic, Incandescent, Ruinous, Frigid, Ferric, and Umbral.",
    "hemomancy.vicar.item_inquiry.blood_structure.line1": "A blood-structure plan: blocks arranged so Blood Projection can persuade the completed form to become useful.",
    "hemomancy.vicar.item_inquiry.blood_structure.line2": "Build precisely and project at the proper point. There is no separate blood key; the outward manipulation is the act of activation.",
    "hemomancy.vicar.item_inquiry.mortal_display.line1": "A Mortal Display is a Hermit's offered heart, sustained at the centre of the temple they built for their final vigil.",
    "hemomancy.vicar.item_inquiry.mortal_display.line2": "Claiming that heart is a personal invitation. When the initiating rite is completed, the giver's vigil ends; this is not storage for organ echoes.",
    "hemomancy.vicar.item_inquiry.hematic_memory.line1": "A Hematic Memory carries one deliberate manipulation lesson. Use it, and the pattern becomes part of your own known practice.",
    "hemomancy.vicar.item_inquiry.hematic_memory.line2": "The Mnemonic Reliquary later arranges active lessons. Neither the crystal nor the Reliquary integrates a personal manipulation into a bloodline.",
    "hemomancy.vicar.item_inquiry.mnemonic_reliquary.line1": "The Mnemonic Reliquary arranges which known manipulations wait closest to the pulse.",
    "hemomancy.vicar.item_inquiry.mnemonic_reliquary.line2": "It changes readiness, not ownership. Learning belongs to the memory; covenant belongs to the bloodline.",
    "hemomancy.vicar.item_inquiry.scar_binder.line1": "An obsolete Scar Binder. The Order once treated scars as plates to be stored and worn; that model has been abandoned.",
    "hemomancy.vicar.item_inquiry.scar_binder.line2": "Known scars now remain in the practitioner. The Mason's Effigy prepares a loadout, and the Iron Brazier commits it.",
    "hemomancy.vicar.item_inquiry.scar_blank.line1": "A blank Scar item, awaiting a registered route at the Cerebral Scarring Station.",
    "hemomancy.vicar.item_inquiry.scar_blank.line2": "The carved item is burned into memory. Later arrangements are copied through the Mason's Effigy rather than worn from inventory.",
    "hemomancy.vicar.item_inquiry.qliphoth.line1": "A Qliphoth Pome belongs to the Archon's private revelation, not to ordinary Order practice.",
    "hemomancy.vicar.item_inquiry.qliphoth.line2": "Nine husks from one bloom complete Communion and force the Fungal Spine into being. Those below the threshold should not be taught to call this custom.",
    "hemomancy.vicar.item_inquiry.hallowed_residuum.line1": "Hallowed Residuum is a Saint's concentrated legacy. Alignment may permit peaceful extraction; rejection awakens the Saint and makes victory the harsher route.",
    "hemomancy.vicar.item_inquiry.hallowed_residuum.line2": "A peaceful sample becomes Residuum through the Vial Centrifuge. A defeated Saint yields it directly. Both routes enter Canon Memory work at the Loom.",
    "hemomancy.vicar.item_inquiry.dragon_egg.line1": "A Dragon Egg. Some Lodge naturalists treat its compressed vitality as evidence for broader theories of living potential.",
    "hemomancy.vicar.item_inquiry.dragon_egg.line2": "That is learned speculation, not settled Covenant cosmology, and no current degree rite requires it.",
    "hemomancy.vicar.item_inquiry.heart_of_the_sea.line1": "The Heart of the Sea carries a deep-water resonance the Order finds suggestive.",
    "hemomancy.vicar.item_inquiry.heart_of_the_sea.line2": "Some read kinship between water and blood; others call that liturgical metaphor. The object proves neither view by itself.",

    # Adjacent Monolith correction
    "hemomancy.monolith.guidance.sanctified": "Sanctified of the Covenant, few have walked this far. Enter the Chamber of Will by your own rite and prepare for the judgment that makes an Archon.",
    "hemomancy.monolith.guidance.sanctified.line2": "Sanctified of the Covenant. The Loom has taught deliberate memory; now learn the inward room where memory, scar, and will become architecture.",

    # Guardian canon and voice
    "hemomancy.guardian.item_inquiry.pale_silver_ingot.line1": "Pale Silver is refined from Consecrated Copper. There is no ore to mine and no iron shortcut.",
    "hemomancy.guardian.item_inquiry.pale_silver_ingot.line2": "Guard it. Every ingot represents completed ritual work.",
    "hemomancy.guardian.item_inquiry.sporitic_thurible.line1": "Harbinger work. Fungal support smoke, paid for in blood.",
    "hemomancy.guardian.item_inquiry.sporitic_thurible.line2": "Do not bring it into a clean line and call the fumes ours.",
    "hemomancy.guardian.item_inquiry.tome_of_the_unstained.line1": "The Tome records the path every Guardian is sworn to understand, and many of us have walked ourselves.",
    "hemomancy.guardian.item_inquiry.tome_of_the_unstained.line2": "Read it before you stand between an infected person and the Church.",
    "hemomancy.guardian.item_inquiry.warhammer.line1": "A hammer keeps the wound closed. That is why it is first in our hands.",
    "hemomancy.guardian.item_inquiry.warhammer.line2": "Strike to stop. Do not spill what you came to contain.",
    "hemomancy.guardian.item_inquiry.iron_sword.line1": "A blade is not forbidden. It is simply dangerous at arm's length around infectious blood.",
    "hemomancy.guardian.item_inquiry.iron_sword.line2": "Keep distance, keep discipline, and use a sanctioned treatment where the weapon actually accepts one.",
    "hemomancy.guardian.item_inquiry.silthmere_glaive.line1": "The Silthmere Glaive is sanctioned for its reach. The edge matters less than keeping infectious blood beyond the hands.",
    "hemomancy.guardian.item_inquiry.silthmere_glaive.line2": "Hold the line. Do not crowd the wound.",
    "hemomancy.guardian.item_inquiry.absolution_dagger.line1": "Every initiated Unstained receives an Absolution Dagger at Baptism. It is last resort, mercy, and self-defence.",
    "hemomancy.guardian.item_inquiry.absolution_dagger.line2": "Its hemolytic treatment is inherent. Do not waste a temporary coating on it.",
    "hemomancy.guardian.item_inquiry.hemolytic_solution.line1": "Hemolytic solution breaks blood-matrix corruption. Carry it for treatment and for equipment whose live recipe supports application.",
    "hemomancy.guardian.item_inquiry.hemolytic_solution.line2": "The Absolution Dagger needs none; its treatment is permanent.",
    "hemomancy.guardian.item_inquiry.hemolytic_vial.line1": "Hemolytic solution. Use it as the Church instructs, not as a universal varnish for every weapon in the rack.",
    "hemomancy.guardian.item_inquiry.hemolytic_vial.line2": "Know the equipment before you spend the dose.",
    "hemomancy.guardian.item_inquiry.bow.line1": "A bow keeps blood at distance. That alone makes it useful.",
    "hemomancy.guardian.item_inquiry.bow.line2": "Use the ammunition you actually possess; do not rely on pale-silver arrows that have not been made.",
    "hemomancy.guardian.item_inquiry.crossbow.line1": "A crossbow buys distance with force and time with a slow reload.",
    "hemomancy.guardian.item_inquiry.crossbow.line2": "Choose the shot before the line closes.",
    "hemomancy.guardian.item_inquiry.shield.line1": "A shield stops force. This one carries no special anti-blood promise.",
    "hemomancy.guardian.item_inquiry.shield.line2": "Keep it between the wound and your face.",
    "hemomancy.guardian.item_inquiry.unstained_shield.line1": "Unstained guardwork, built for a clean defensive line.",
    "hemomancy.guardian.item_inquiry.unstained_shield.line2": "Use the protection the item actually grants. Do not invent projectile rites for it.",
    "hemomancy.guardian.item_inquiry.lethean_dew.line1": "Lethean Dew is prepared through the Church's current condenser and Ghost Pipe work, not simply gathered because dawn looked holy.",
    "hemomancy.guardian.item_inquiry.lethean_dew.line2": "Carry it sealed. Field medicine begins with not contaminating the medicine.",
    "hemomancy.guardian.item_inquiry.iron_chestplate.line1": "Plain iron. It stops ordinary blows and makes no claim against infection.",
    "hemomancy.guardian.item_inquiry.iron_chestplate.line2": "Maintain it until proper Unstained armour is ready.",
    "hemomancy.guardian.item_inquiry.unstained_armor.line1": "Unstained armour is fieldwear for those who stand close to active corruption.",
    "hemomancy.guardian.item_inquiry.unstained_armor.line2": "Keep every seal sound. Clean equipment is a discipline, not a colour.",
    "hemomancy.guardian.item_inquiry.pallid_icon.line1": "A consecrated copy of the river Icon. It gives the line a direction when fear makes every road look alike.",
    "hemomancy.guardian.item_inquiry.pallid_icon.line2": "Carry it as witness, not as a substitute for vigilance.",
    "hemomancy.guardian.item_inquiry.pale_silver_bell.line1": "A Pale Silver Bell marks warning, retreat, and cleared ground.",
    "hemomancy.guardian.item_inquiry.pale_silver_bell.line2": "Ring once with purpose. Panic makes poor signals.",

    # Zealot canon and voice
    "hemomancy.zealot.item_inquiry.altar_of_cleansing.line1": "The Altar is a later place of blessing and offering. It does not begin the Unstained path.",
    "hemomancy.zealot.item_inquiry.altar_of_cleansing.line2": "First suppress the infection at the Podium; then Lethean Baptism admits you to Purity. Bring later offerings here when the path has truly begun.",
    "hemomancy.zealot.item_inquiry.hemolytic_solution.line1": "Hemolytic solution suppresses active infection at the Unstained Podium. It prepares the body; it does not by itself make a practitioner Unstained.",
    "hemomancy.zealot.item_inquiry.hemolytic_solution.line2": "After suppression, perform the Rite of Lethean Baptism. That is where Purity begins.",
    "hemomancy.zealot.item_inquiry.hemolytic_vial.line1": "Hemolytic solution quiets the blood-memory at the Podium so the candidate can survive Baptism.",
    "hemomancy.zealot.item_inquiry.hemolytic_vial.line2": "Preparation is not initiation. The rite must still be chosen.",
    "hemomancy.zealot.craft_hemolytic.line1": "Hemolytic solution begins with Lethean Dew and Ghost Pipe, prepared without crimson catalyst.",
    "hemomancy.zealot.craft_hemolytic.line2": "Bring the finished solution to the Podium to suppress the infection. Lethean Baptism follows when you are ready to enter the path.",
    "hemomancy.zealot.craft_hemolytic.line3": "The first treatment hurts because the old current is being interrupted, not because pain proves devotion.",
    "hemomancy.zealot.item_inquiry.pallid_infusion.line1": "Pallid Infusion is a battlefield restorative distilled from Ghost Pipe and White Humor.",
    "hemomancy.zealot.item_inquiry.pallid_infusion.line2": "It clears Blood Loss and grants brief regeneration. Clarity comes through Consecrated Copper at the Podium and the Rite of Clarity Ascension, not through this draught.",
    "hemomancy.zealot.item_inquiry.pallid_infusion.not_yet": "Pallid Infusion is restorative, not a threshold. You may use it whenever you have need and the item in hand.",
    "hemomancy.zealot.item_inquiry.pale_humor_flask.line1": "A Pale Humor Flask carries purified White Humor: lymph rendered clear of hematic corruption, not blood made holy.",
    "hemomancy.zealot.item_inquiry.pale_humor_flask.line2": "Keep the vessel sealed. This humor belongs to preservation and cleansing work.",
    "hemomancy.zealot.item_inquiry.tears_of_silthmere.line1": "Tears of Silthmere are a crafted distillation named for Silthmere, a liturgical title of Our Lady.",
    "hemomancy.zealot.item_inquiry.tears_of_silthmere.line2": "They do not condense from mortal grief in wild water. Their potency comes from disciplined preparation and consecration.",
    "hemomancy.zealot.item_inquiry.pale_silver_ingot.line1": "Pale Silver is the next ritual state of Consecrated Copper. It is refined through pale work; it is never mined.",
    "hemomancy.zealot.item_inquiry.pale_silver_ingot.line2": "The metal's stillness is earned, as ours is.",
    "hemomancy.zealot.item_inquiry.hallowed_residuum.line1": "Hallowed Residuum is a Saint's concentrated legacy. An aligned Harbinger may draw a peaceful sample; rejection may wake the Saint to violence.",
    "hemomancy.zealot.item_inquiry.hallowed_residuum.line2": "Either route shows the same danger: immense blood-power can persist long after wisdom fails.",
    "hemomancy.zealot.item_inquiry.lethean.line1": "Lethean Dew is prepared through still water and Ghost Pipe work; further distillation yields stronger ritual material.",
    "hemomancy.zealot.item_inquiry.lethean.line2": "Use each form where the rite asks. Poetry may guide the hand, but it must not replace the recipe.",
    "hemomancy.zealot.item_inquiry.lethean_poppy_wreath.line1": "A wreath of Lethean Poppies: patient labour offered without bargaining with blood.",
    "hemomancy.zealot.item_inquiry.lethean_poppy_wreath.line2": "The Altar receives it as a modest Purity offering. The exact measure belongs to the progress record, not to prayer.",
    "hemomancy.zealot.item_inquiry.silver_chalice.line1": "The Silver Chalice belongs to Clarity work after the second path has opened.",
    "hemomancy.zealot.item_inquiry.silver_chalice.line2": "Offer it when the rite and your progress call for it. The mirror keeps the number; the Church keeps the meaning.",
    "hemomancy.zealot.item_inquiry.consecrated_copper_ingot.line1": "Consecrated Copper has survived change and been readied for deeper Unstained work.",
    "hemomancy.zealot.item_inquiry.consecrated_copper_ingot.line2": "At full Purity, bring it to the Podium to prepare Clarity; the Rite of Clarity Ascension performs the transition.",
    "hemomancy.unstained.purification_begun": "The hemolytic solution suppresses the old current. Purity has not begun; the Rite of Lethean Baptism still awaits.",
}

data.update(updates)
LANG.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

# Pallid Infusion is restorative and must not be purity-gated.
pallid = ROOT / "src/main/resources/data/hemomancy/dialogue_inquiry/zealot/hemomancy/pallid_infusion.json"
write(pallid, '''{
  "lines": [
    "hemomancy.zealot.item_inquiry.pallid_infusion.line1",
    "hemomancy.zealot.item_inquiry.pallid_infusion.line2"
  ]
}''')

# Resource and canon regression tests.
write(TEST / "common/entity/npc/dialogue/ItemInquiryResourceValidationTest.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemInquiryResourceValidationTest {
    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path INQUIRIES = ROOT.resolve("src/main/resources/data/hemomancy/dialogue_inquiry");
    private static final Path LANGUAGE = ROOT.resolve("src/main/resources/assets/hemomancy/lang/en_us.json");

    @Test
    void everyInquiryLineExistsInRuntimeLanguageFile() throws IOException {
        Set<String> languageKeys;
        try (Reader reader = Files.newBufferedReader(LANGUAGE)) {
            languageKeys = JsonParser.parseReader(reader).getAsJsonObject().keySet();
        }
        Map<Path, List<String>> failures = new TreeMap<>();
        try (Stream<Path> files = Files.walk(INQUIRIES)) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                try {
                    JsonObject root = JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
                    List<String> keys = new ArrayList<>();
                    boolean hasLines = root.has("lines");
                    boolean hasConditions = root.has("conditions");
                    if (hasLines == hasConditions) failures.computeIfAbsent(path, ignored -> new ArrayList<>())
                            .add("must contain exactly one of lines or conditions");
                    if (hasLines) collect(root.getAsJsonArray("lines"), keys);
                    if (hasConditions) {
                        JsonArray conditions = root.getAsJsonArray("conditions");
                        for (JsonElement element : conditions) {
                            JsonObject branch = element.getAsJsonObject();
                            if (!branch.has("lines")) failures.computeIfAbsent(path, ignored -> new ArrayList<>())
                                    .add("condition missing lines");
                            else collect(branch.getAsJsonArray("lines"), keys);
                        }
                        if (!conditions.isEmpty() && constrained(conditions.get(conditions.size() - 1).getAsJsonObject())) {
                            failures.computeIfAbsent(path, ignored -> new ArrayList<>()).add("final condition must be catch-all");
                        }
                    }
                    for (String key : keys) if (!languageKeys.contains(key)) {
                        failures.computeIfAbsent(path, ignored -> new ArrayList<>()).add("missing localisation: " + key);
                    }
                } catch (Exception exception) {
                    failures.computeIfAbsent(path, ignored -> new ArrayList<>()).add(exception.getMessage());
                }
            });
        }
        assertTrue(failures.isEmpty(), () -> failures.entrySet().stream()
                .map(entry -> entry.getKey() + " -> " + String.join(", ", entry.getValue()))
                .reduce((a, b) -> a + "\n" + b).orElse(""));
    }

    private static void collect(JsonArray array, List<String> keys) {
        for (JsonElement element : array) if (!element.getAsString().isBlank()) keys.add(element.getAsString());
    }

    private static boolean constrained(JsonObject branch) {
        return branch.entrySet().stream().map(Map.Entry::getKey).anyMatch(key -> !"lines".equals(key));
    }
}
''')

write(TEST / "common/entity/npc/dialogue/inquiry/ItemInquiryConditionTest.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemInquiryConditionTest {
    @Test
    void clarityGateRequiresUnlockedClarityAndMinimumValue() {
        ItemInquiryCondition condition = new ItemInquiryCondition(-1, -1, -1F, -1F, 50F, -1F,
                true, null, null, List.of("key"));
        assertFalse(condition.matches(new ItemInquiryContext(0, 100, 49, true, false, true, false, false)));
        assertFalse(condition.matches(new ItemInquiryContext(0, 100, 75, false, false, true, false, false)));
        assertTrue(condition.matches(new ItemInquiryContext(0, 100, 75, true, false, true, false, false)));
    }

    @Test
    void pathAndBloodFlagsAreIndependent() {
        ItemInquiryCondition condition = new ItemInquiryCondition(-1, -1, -1F, -1F, -1F, -1F,
                null, false, true, List.of("key"));
        assertTrue(condition.matches(new ItemInquiryContext(4, 25, 0, false, false, true, false, false)));
        assertFalse(condition.matches(new ItemInquiryContext(4, 25, 0, false, true, true, false, false)));
    }
}
''')

write(TEST / "common/entity/npc/dialogue/DialogueInquiryCanonTest.java", r'''
package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;

class DialogueInquiryCanonTest {
    private static final Path LANGUAGE = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");

    @Test
    void currentInquiryProseDoesNotRetainSettledStaleClaims() throws IOException {
        JsonObject language = JsonParser.parseString(Files.readString(LANGUAGE)).getAsJsonObject();
        StringBuilder inquiry = new StringBuilder();
        for (var entry : language.entrySet()) {
            if (entry.getKey().contains(".item_inquiry.")) inquiry.append(entry.getValue().getAsString()).append('\n');
        }
        String text = inquiry.toString().toLowerCase(Locale.ROOT);
        for (String forbidden : List.of(
                "smelt the ore directly",
                "wear the scar",
                "integrate it into your bloodline",
                "a blood key activates",
                "pallid infusion initiates",
                "guardians are not expected to walk",
                "guardian detachments use it",
                "blood that has been purified to near-transparency",
                "five points of purity",
                "five points of clarity")) {
            assertFalse(text.contains(forbidden), () -> "Stale inquiry phrase remains: " + forbidden);
        }
    }
}
''')

# Rewrite the guide around the actual inventory-hub contract.
write(ROOT / "docs/DIALOGUE_ITEM_QUERY_GUIDE.md", r'''
# Hemomancy Inventory Inquiry Dialogue Guide

Inventory inquiry lets the player ask an NPC to examine objects already carried in the player's inventory. It is a topic-hub system, not a main-hand-only interaction.

## Runtime flow

1. The NPC builds its ordinary progression dialogue.
2. `DialogueItemInquiryNodes` scans the player's inventory in slot order.
3. Stateful providers inspect dynamic stacks first.
4. Ordinary stacks resolve through `ItemInquiryRegistry` by speaker ID and item registry ID.
5. Refusal policy may suppress inquiry or replace procedure with a terse identification response.
6. One inquiry topic is added for every distinct supported item state.
7. If unsupported objects are present, one **Another object** topic reaches the speaker's characterful unknown response.

The main runtime localisation source is:

```text
src/main/resources/assets/hemomancy/lang/en_us.json
```

Generated or editor copies must not mask missing keys in that file.

## Supported speakers

```text
alchemist
artificer
guardian
mnemonist
monolith
vicar
votary_wayfarer
voyager
zealot
```

## Datapack location

```text
data/<resource_namespace>/dialogue_inquiry/<speaker_id>/<item_namespace>/<item_path>.json
```

The path determines the held item's registry ID. A simple entry is:

```json
{
  "lines": [
    "hemomancy.voyager.item_inquiry.erythrocoral_fragment.line1",
    "hemomancy.voyager.item_inquiry.erythrocoral_fragment.line2"
  ]
}
```

Conditional branches are evaluated top-to-bottom; the first match wins. The final branch must be unconstrained.

```json
{
  "conditions": [
    {
      "min_degree": 3,
      "lines": ["hemomancy.mnemonist.item_inquiry.somatic_loom.line1"]
    },
    {
      "lines": ["hemomancy.mnemonist.item_inquiry.somatic_loom.locked"]
    }
  ]
}
```

Supported stable condition fields:

```text
min_degree / max_degree
min_purity / max_purity
min_clarity / max_clarity
clarity_unlocked
requires_active_blood
requires_purifying
```

Use Java stack-aware providers for data components, owner attunement, quest flags, or other highly specific state.

## Stateful provider priority

Before exact JSON lookup, the built-in provider registry examines:

1. Mnemonic Blueprints — blank, Cardinal Rite, or blood-structure plan.
2. Dynamic Scar Patterns — blank, single-route template, or prepared multi-scar loadout.
3. Specimen Jars — contained entity and Morphling layer state.

Distinct states of the same item ID receive distinct inquiry nodes. Their dynamic details are shown as literal evidence lines beneath the speaker's localised interpretation.

## Refusal behaviour

- A Clarity-state Alchemist exposes no Harbinger operational inquiries.
- A purifying Alchemist identifies known work but refuses procedure.
- A purifying or Clarity-state Artificer identifies known work but refuses procedure.
- Other speakers follow their own progression and faction rules.

Do not add `item_hint` to a dialogue state that should expose no inquiry at all.

## Expertise ownership

| Speaker | Owns | Defers |
|---|---|---|
| Alchemist | processing, reagents, specimens, machine preparation | armour rites, memory ownership, doctrine |
| Mnemonist | memory learning, loadouts, Loom meaning | material processing, armour construction |
| Artificer | Armature, armour, Living Staff grafts, fittings | alchemy, general memory metaphysics |
| Vicar | doctrine, history, degrees, institutions | exact machine operation |
| Guardian | containment, weapon handling, field safety | sacramental metaphysics and Harbinger machines |
| Zealot | Purity, Clarity, rites, Our Lady, sacred materials | tactical optimisation |
| Voyager | established field ecology and survey evidence | universal metaphysical certainty |
| Wayfarer | junior observation and admitted uncertainty | authoritative conclusions |
| Monolith | high-degree thresholds and implication | ordinary tutorials |

Two speakers may map the same item only when they answer different questions.

## Required validation

`ItemInquiryResourceValidationTest` parses every inquiry JSON and fails when:

- an entry has neither or both of `lines` and `conditions`;
- a condition lacks lines;
- a conditional entry lacks a final catch-all branch;
- any referenced key is absent from the main runtime language file.

`DialogueInquiryCanonTest` guards settled high-risk stale claims. Add a focused assertion when a future migration retires another player-facing workflow.
''')

print("Dialogue inquiry canonical sync applied.")
