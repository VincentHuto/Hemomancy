# Hemomancy Held-Item NPC Dialogue Query Guide

This guide explains how Harbinger NPCs decide what items they can answer questions about when the player right-clicks them while holding an item.

## Quick Summary

Held-item dialogue is hard-coded in the NPC dialogue tree classes:

- Vicar mappings: `HarbingerVicarDialogueTrees.itemInquiry(...)`
- Alchemist mappings: `HarbingerAlchemistDialogueTrees.itemInquiry(...)`

The visible dialogue text is stored in language keys in:

- `src/main/resources/assets/hemomancy/lang/en_us.json`

`DialogueOption.java` only defines what a clickable option contains. It does **not** decide which held items are recognized.

## Interaction Flow

1. The player right-clicks an NPC.
2. The NPC's `mobInteract(...)` method runs.
3. The NPC checks that the interaction is server-side and uses the main hand.
4. The NPC reads `player.getMainHandItem()`.
5. If the main-hand item is not empty, the NPC opens an item-inquiry dialogue tree.
6. The item-inquiry method checks the held item and returns a matching `DialogueTree`.
7. If no known item matches, the NPC returns an unknown-item fallback dialogue.

Relevant entity files:

- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/HarbingerVicarEntity.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/HarbingerAlchemistEntity.java`

Relevant dialogue files:

- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java`

## Where to Add Recognized Items

### Vicar

Add Vicar-recognized items in:

```java
HarbingerVicarDialogueTrees.itemInquiry(ItemStack item, int degree, int entityId)
```

Current pattern:

```java
public static DialogueTree itemInquiry(ItemStack item, int degree, int entityId) {
    Item it = item.getItem();
    if (it == ItemInit.rite_hint.get()) {
        return riteHintInquiry(degree, entityId);
    } else if (it == ItemInit.blood_structure_hint.get()) {
        return bloodStructureInquiry(entityId);
    } else {
        return vicarUnknownInquiry(entityId);
    }
}
```

### Alchemist

Add Alchemist-recognized items in:

```java
HarbingerAlchemistDialogueTrees.itemInquiry(ItemStack item, int degree, int entityId)
```

The Alchemist already uses a helper called `basicItemInquiry(...)` for many simple responses. Prefer that helper unless the item needs custom logic or degree-gated text.

## How to Add a New Vicar Item Query

### 1. Add the item mapping

In `HarbingerVicarDialogueTrees.itemInquiry(...)`, add a new branch before the unknown fallback:

```java
} else if (it == ItemInit.some_item.get()) {
    return someItemInquiry(entityId);
} else {
    return vicarUnknownInquiry(entityId);
}
```

For block items, compare against `.asItem()`:

```java
} else if (it == BlockInit.some_block.get().asItem()) {
    return someBlockInquiry(entityId);
}
```

For item families/classes, use `instanceof`:

```java
} else if (item.getItem() instanceof HematicMemoryItem) {
    return memoryInquiry(entityId);
}
```

### 2. Add the dialogue tree helper

Add a helper method in `HarbingerVicarDialogueTrees.java`:

```java
private static DialogueTree someItemInquiry(int entityId) {
    return DialogueTree.builder(SPEAKER, VICAR_ICON, entityId)
            .addNode(new DialogueNode("root", List.of(
                    "hemomancy.vicar.item_inquiry.some_item.line1",
                    "hemomancy.vicar.item_inquiry.some_item.line2"
            ), List.of(
                    new DialogueOption("hemomancy.dialogue.vicar.option.ask_about_item", "item_hint", null),
                    new DialogueOption("hemomancy.dialogue.vicar.option.leave", null, null)
            )))
            .addNode(itemHintNode())
            .build();
}
```

Important: if an option points to `"item_hint"`, the tree must contain an `"item_hint"` node. The Vicar file has a shared `itemHintNode()` helper for this.

### 3. Add language keys

In `src/main/resources/assets/hemomancy/lang/en_us.json`, add matching text:

```json
"hemomancy.vicar.item_inquiry.some_item.line1": "First vicar response line.",
"hemomancy.vicar.item_inquiry.some_item.line2": "Second vicar response line."
```

Keep Vicar prose in the Harbinger tone: measured, ecclesiastical, Latinate, and morally gray.

## How to Add a New Alchemist Item Query

### Simple Alchemist response

In `HarbingerAlchemistDialogueTrees.itemInquiry(...)`, add:

```java
} else if (it == ItemInit.some_reagent.get()) {
    return basicItemInquiry(entityId,
            "hemomancy.alchemist.item_inquiry.some_reagent.line1",
            "hemomancy.alchemist.item_inquiry.some_reagent.line2");
}
```

Then add language keys:

```json
"hemomancy.alchemist.item_inquiry.some_reagent.line1": "First alchemist response line.",
"hemomancy.alchemist.item_inquiry.some_reagent.line2": "Second alchemist response line."
```

### Degree-gated Alchemist response

Use the `degree` parameter when the NPC should withhold details until the player reaches a certain Harbinger degree:

```java
} else if (it == BlockInit.some_advanced_station.get().asItem()) {
    return someAdvancedStationInquiry(degree, entityId);
}
```

Helper example:

```java
private static DialogueTree someAdvancedStationInquiry(int degree, int entityId) {
    if (degree < 4) {
        return basicItemInquiry(entityId, "hemomancy.alchemist.item_inquiry.some_advanced_station.locked");
    }
    return basicItemInquiry(entityId,
            "hemomancy.alchemist.item_inquiry.some_advanced_station.line1",
            "hemomancy.alchemist.item_inquiry.some_advanced_station.line2");
}
```

## Existing Key Naming Conventions

Vicar item inquiry keys:

```text
hemomancy.vicar.item_inquiry.<topic>.line1
hemomancy.vicar.item_inquiry.<topic>.line2
```

Alchemist item inquiry keys:

```text
hemomancy.alchemist.item_inquiry.<topic>.line1
hemomancy.alchemist.item_inquiry.<topic>.line2
```

Fallback keys:

```text
hemomancy.vicar.item_inquiry.unknown
hemomancy.alchemist.item_inquiry.unknown
```

Shared hint keys:

```text
hemomancy.vicar.item_hint
hemomancy.alchemist.item_hint
```

Option keys:

```text
hemomancy.dialogue.vicar.option.ask_about_item
hemomancy.dialogue.alchemist.option.ask_about_item
hemomancy.dialogue.vicar.option.leave
hemomancy.dialogue.alchemist.option.leave
```

## Common Gotchas

- Held-item query checks the **main hand** only.
- Any non-empty main-hand item can route to item inquiry instead of normal dialogue.
- Unknown items still produce a dialogue tree, usually the unknown-item response.
- If a `DialogueOption` has `nextNodeId = "item_hint"`, the current tree must add an `"item_hint"` node.
- Missing `item_hint` nodes cause the dialogue screen to become blank because the client switches to a node that does not exist.
- Vicar logic checks some special states, such as Clarity, before item inquiry.
- Alchemist logic currently routes held items before some other state checks.
- Keep Java mappings and `en_us.json` keys in sync, or the player will see untranslated key text.

## Testing Checklist

After adding a new item query:

1. Hold the item in the main hand.
2. Right-click the intended NPC.
3. Confirm the custom response appears.
4. Click the "I have a question about something I found" option.
5. Confirm the item-hint response appears.
6. Confirm the leave option closes the dialogue.
7. Test an unrelated item to confirm the unknown fallback still works.
8. Run a compile check:

```powershell
.\gradlew.bat compileJava
```

