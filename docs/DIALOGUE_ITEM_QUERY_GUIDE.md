# Hemomancy Held-Item NPC Dialogue Query Guide

This guide explains how NPCs decide what held items they can answer questions about when the player right-clicks them with an item in the main hand.

## Quick Summary

Held-item inquiry dialogue is now mostly data-driven.

- Item mappings live as JSON under `src/main/resources/data/hemomancy/dialogue_inquiry/<npc_id>/<item_namespace>/<item_path>.json`.
- `ItemInquiryLoader` loads those files on server start and resource reload.
- `ItemInquiryRegistry` resolves the held item's registry ID for a specific NPC.
- Dialogue text still lives in `src/main/resources/assets/hemomancy/lang/en_us.json`.
- Normal item IDs should be added with JSON, not new Java branches.

Java changes are only needed when adding a new NPC inquiry surface, adding custom non-JSON predicates, or handling item families/classes. The Vicar's `HematicMemoryItem` handling is the current example of a class-based exception that remains in Java.

## Interaction Flow

1. The player right-clicks an NPC.
2. The NPC's `mobInteract(...)` method runs server-side for `InteractionHand.MAIN_HAND`.
3. The NPC reads `player.getMainHandItem()`.
4. If the main-hand item is not empty, most current inquiry NPCs route to item inquiry.
5. The dialogue tree class gets the held item registry ID with `BuiltInRegistries.ITEM.getKey(item.getItem())`.
6. The tree class calls `ItemInquiryRegistry.INSTANCE.resolve(npcId, itemId, degree, purity)`.
7. The registry finds the JSON entry for that NPC and item, then resolves the first matching condition branch.
8. If no entry or branch matches, the NPC uses its unknown-item fallback line.

Current NPC IDs with resource folders:

```text
alchemist
guardian
mnemonist
vicar
votary_wayfarer
voyager
zealot
```

Current implementation files:

- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryLoader.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryRegistry.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryEntry.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/inquiry/ItemInquiryCondition.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVicarDialogueTrees.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerMnemonistDialogueTrees.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVoyagerDialogueTrees.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/HarbingerVotaryWayfarerDialogueTrees.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/ZealotDialogueTrees.java`
- `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/dialogue/GuardianDialogueTrees.java`

## JSON File Location

The loader scans resources with this path shape:

```text
data/<resource_namespace>/dialogue_inquiry/<npc_id>/<item_namespace>/<item_path>.json
```

In this repo, Hemomancy entries usually live under:

```text
src/main/resources/data/hemomancy/dialogue_inquiry/<npc_id>/<item_namespace>/<item_path>.json
```

The item is identified by the path, not by a field inside the JSON. For example:

```text
hemomancy:blood_crystal_shard
src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/blood_crystal_shard.json

minecraft:book
src/main/resources/data/hemomancy/dialogue_inquiry/vicar/minecraft/book.json
```

The `<item_path>` portion may contain nested folders if an item registry path ever contains slashes.

## JSON Formats

### Simple response

Use `lines` when the NPC should always say the same thing for the item.

```json
{
  "lines": [
    "hemomancy.voyager.item_inquiry.erythrocoral_fragment.line1",
    "hemomancy.voyager.item_inquiry.erythrocoral_fragment.line2"
  ]
}
```

### Conditional response

Use `conditions` when text depends on player degree or Unstained purity.

```json
{
  "conditions": [
    { "max_degree": 1, "lines": ["hemomancy.vicar.item_inquiry.rite_hint.low.line1", "hemomancy.vicar.item_inquiry.rite_hint.low.line2"] },
    { "max_degree": 4, "lines": ["hemomancy.vicar.item_inquiry.rite_hint.mid.line1", "hemomancy.vicar.item_inquiry.rite_hint.mid.line2"] },
    { "lines": ["hemomancy.vicar.item_inquiry.rite_hint.high.line1", "hemomancy.vicar.item_inquiry.rite_hint.high.line2"] }
  ]
}
```

Branches are evaluated top-to-bottom. The first branch whose constraints pass is used.

Supported condition fields:

```text
min_degree
max_degree
min_purity
max_purity
```

Rules:

- Every branch must include a non-empty `lines` array.
- `min_degree` and `max_degree` are inclusive integer bounds.
- `min_purity` and `max_purity` are inclusive numeric bounds.
- Missing condition fields are unconstrained.
- A final branch with only `lines` is the normal catch-all fallback inside that item's JSON.
- Do not mix root-level `lines` with `conditions`; if `conditions` exists, the loader uses the conditional format.

Degree and purity availability depends on the NPC:

```text
vicar, alchemist, mnemonist, voyager, votary_wayfarer: real degree, purity 0
zealot: degree 0, real purity
guardian: degree 0, purity 0
```

That means `min_degree` and `max_degree` are useful for Harbinger NPCs, while `min_purity` and `max_purity` are currently useful for Zealot entries.

## How to Add a New Item Query

### 1. Choose the NPC and item IDs

Use the NPC folder name and the held item's registry ID.

Example target:

```text
NPC: alchemist
Item: hemomancy:some_reagent
```

Create:

```text
src/main/resources/data/hemomancy/dialogue_inquiry/alchemist/hemomancy/some_reagent.json
```

### 2. Add the inquiry JSON

Simple entry:

```json
{
  "lines": [
    "hemomancy.alchemist.item_inquiry.some_reagent.line1",
    "hemomancy.alchemist.item_inquiry.some_reagent.line2"
  ]
}
```

Degree-gated entry:

```json
{
  "conditions": [
    { "min_degree": 3, "lines": ["hemomancy.alchemist.item_inquiry.some_station.line1", "hemomancy.alchemist.item_inquiry.some_station.line2"] },
    { "lines": ["hemomancy.alchemist.item_inquiry.some_station.locked"] }
  ]
}
```

Purity-gated Zealot entry:

```json
{
  "conditions": [
    { "min_purity": 75, "lines": ["hemomancy.zealot.item_inquiry.some_relic.line1", "hemomancy.zealot.item_inquiry.some_relic.line2"] },
    { "lines": ["hemomancy.zealot.item_inquiry.some_relic.not_yet"] }
  ]
}
```

### 3. Add language keys

Add matching entries to:

```text
src/main/resources/assets/hemomancy/lang/en_us.json
```

Example:

```json
"hemomancy.alchemist.item_inquiry.some_reagent.line1": "First alchemist response line.",
"hemomancy.alchemist.item_inquiry.some_reagent.line2": "Second alchemist response line."
```

Keep the NPC voice consistent:

- Harbinger NPCs use measured, ecclesiastical, scholarly, or covenant language depending on role.
- Unstained NPCs use cleaner sacramental language: Lethean water, white/silver, oxidized copper, antiseptic ritual, and blunt containment.
- Harbingers should not read as simple villains, and Unstained NPCs should not read as simple heroes.

## Existing Key Naming Conventions

Item inquiry lines usually follow:

```text
hemomancy.<npc_id>.item_inquiry.<topic>.line1
hemomancy.<npc_id>.item_inquiry.<topic>.line2
```

Locked or not-yet branches usually follow:

```text
hemomancy.<npc_id>.item_inquiry.<topic>.locked
hemomancy.<npc_id>.item_inquiry.<topic>.not_yet
```

Unknown fallback keys:

```text
hemomancy.<npc_id>.item_inquiry.unknown
```

Shared item-hint keys:

```text
hemomancy.<npc_id>.item_hint
```

Most inquiry trees include an "ask about item" option that points to an `item_hint` node. Guardian item responses are terser and currently only offer a leave option.

## When Java Still Needs to Change

Add or edit Java only when the data-driven registry cannot express the behavior:

- Adding item inquiry to a new NPC type.
- Adding class-wide or family-wide behavior, such as Vicar handling every `HematicMemoryItem` with one shared response.
- Adding conditions beyond degree and purity.
- Changing the options shown after an inquiry response.
- Changing interaction priority, such as whether held items are checked before clarity or purifying branches.

For normal one-item-to-one-response mappings, add JSON and language keys only.

## Common Gotchas

- Held-item inquiry checks the main hand only.
- The JSON file path identifies the item. There is no `"item"` field in the file.
- The loader reconstructs the item ID as `<item_namespace>:<item_path>` from the resource path.
- Branch order matters. Put narrow conditions before broad catch-all branches.
- If no condition branch matches, the NPC uses its unknown-item fallback.
- If an option points to `"item_hint"`, the tree must include an `item_hint` node.
- Vicar refuses clarity-bearing players before item inquiry and attacks instead.
- Alchemist, Mnemonist, Voyager, Votary Wayfarer, Zealot, and Guardian currently check non-empty held items before their normal empty-hand dialogue branches.
- Translation keys in JSON are not validated at load time; missing keys show up to the player as untranslated key text.
- Malformed JSON paths or invalid condition fields are logged by `ItemInquiryLoader`.

## Testing Checklist

After adding or changing an item query:

1. Confirm the JSON path matches the held item's registry ID.
2. Confirm every `lines` key exists in `en_us.json`.
3. Start or reload the server/client resources so `ItemInquiryLoader` runs.
4. Hold the item in the main hand.
5. Right-click the intended NPC.
6. Confirm the custom response appears.
7. If the tree includes an ask-about-item option, click it and confirm the item-hint response appears.
8. Test an unrelated item to confirm the unknown fallback still works.
9. If Java changed, run a compile check:

```powershell
.\gradlew.bat compileJava
```
