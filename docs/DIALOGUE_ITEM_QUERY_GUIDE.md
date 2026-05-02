# DIALOGUE_ITEM_QUERY_GUIDE.md

## Overview

When a player right-clicks an NPC while **holding an item in their main hand**, the NPC opens a specialized *item inquiry* dialogue instead of their normal conversation tree. This system is **fully data-driven**: you can add new item/block responses for any NPC without touching Java code.

---

## Which NPCs Support Item Inquiry?

| NPC Entity | NPC ID string | Context passed to JSON |
|---|---|---|
| Harbinger Vicar | `vicar` | `degree` (0–8+) |
| Harbinger Alchemist | `alchemist` | `degree` (0–8+) |
| Unstained Zealot | `zealot` | `purity` (0–100 float) |
| Unstained Guardian | `guardian` | *(none — simple lookup)* |

> **HematicMemoryItem exception:** All `HematicMemoryItem` instances share one vicar response that is still hard-coded in Java (there is one response for all memory types). You do not need a JSON file for individual memory items.

---

## File Location

```
data/<namespace>/dialogue_inquiry/<npc_id>/<item_namespace>/<item_path>.json
```

- `<namespace>` — your mod's namespace (or `hemomancy` for base-mod entries). Can be any loaded datapack namespace.
- `<npc_id>` — one of: `vicar`, `alchemist`, `zealot`, `guardian`
- `<item_namespace>/<item_path>` — the item's full registry ID, split on the colon.

**Examples:**
```
data/hemomancy/dialogue_inquiry/vicar/hemomancy/rite_hint.json
data/hemomancy/dialogue_inquiry/alchemist/hemomancy/somatic_loom.json
data/hemomancy/dialogue_inquiry/zealot/hemomancy/pallid_infusion.json
data/mymod/dialogue_inquiry/vicar/mymod/my_custom_item.json
```

> **Blocks used as items** (picked up from inventory) use their registry block path, e.g. `sanguine_monolith.json` for the Sanguine Monolith block.

---

## JSON Formats

### Simple (unconditional)

Use this when the NPC always gives the same response regardless of degree or purity.

```json
{
  "lines": [
    "translation.key.line1",
    "translation.key.line2"
  ]
}
```

- `lines` — an array of translation keys displayed as dialogue text.
- You can have 1 or more lines; 1–2 is typical.

### Conditional

Use this when the response should differ based on the player's state (degree or purity). Branches are evaluated **top-to-bottom**; the first branch whose conditions all pass is used. The last branch typically has no conditions, making it the default fallback.

```json
{
  "conditions": [
    {
      "min_degree": 3,
      "lines": ["translation.key.unlocked.line1", "translation.key.unlocked.line2"]
    },
    {
      "lines": ["translation.key.locked"]
    }
  ]
}
```

#### Supported condition fields

| Field | Type | Description |
|---|---|---|
| `min_degree` | int | Minimum initiatory degree (inclusive). Only applies when NPC is `vicar` or `alchemist`. |
| `max_degree` | int | Maximum initiatory degree (inclusive). |
| `min_purity` | float | Minimum Unstained purity (0–100). Only applies when NPC is `zealot`. |
| `max_purity` | float | Maximum Unstained purity (0–100). |

All fields are optional. Omitting a field means that direction is unconstrained.

---

## Examples

### Vicar — Degree-tiered response (rite_hint.json)

```json
{
  "conditions": [
    { "max_degree": 1, "lines": ["hemomancy.vicar.item_inquiry.rite_hint.low.line1", "hemomancy.vicar.item_inquiry.rite_hint.low.line2"] },
    { "max_degree": 4, "lines": ["hemomancy.vicar.item_inquiry.rite_hint.mid.line1", "hemomancy.vicar.item_inquiry.rite_hint.mid.line2"] },
    {                   "lines": ["hemomancy.vicar.item_inquiry.rite_hint.high.line1", "hemomancy.vicar.item_inquiry.rite_hint.high.line2"] }
  ]
}
```

### Alchemist — Machine locked behind degree (somatic_loom.json)

```json
{
  "conditions": [
    { "min_degree": 3, "lines": ["hemomancy.alchemist.item_inquiry.somatic_loom.line1", "hemomancy.alchemist.item_inquiry.somatic_loom.line2"] },
    {                   "lines": ["hemomancy.alchemist.item_inquiry.somatic_loom.locked"] }
  ]
}
```

### Zealot — Purity-gated response (pallid_infusion.json)

```json
{
  "conditions": [
    { "min_purity": 75, "lines": ["hemomancy.zealot.item_inquiry.pallid_infusion.line1", "hemomancy.zealot.item_inquiry.pallid_infusion.line2"] },
    {                    "lines": ["hemomancy.zealot.item_inquiry.pallid_infusion.not_yet"] }
  ]
}
```

### Guardian — Simple weapon response (unstained_warhammer.json)

```json
{
  "lines": [
    "hemomancy.guardian.item_inquiry.warhammer.line1",
    "hemomancy.guardian.item_inquiry.warhammer.line2"
  ]
}
```

---

## Adding a New Response (Step by Step)

1. **Find the item's registry ID** — it's the `ResourceLocation` key, e.g. `mymod:my_widget`. Split on `:` to get namespace and path.
2. **Choose the NPC** — which NPC should know about this item?
3. **Create the JSON file** at the correct path under `data/<namespace>/dialogue_inquiry/<npc_id>/<item_namespace>/<item_path>.json`.
4. **Add translation keys** to your `lang/en_us.json` (or whichever languages you support).
5. **Reload** — entries are loaded as a server-side datapack reload listener, so `/reload` in-game applies changes without restarting.

---

## For Other Mods / Datapack Authors

Because the path uses your mod namespace (e.g. `data/mymod/dialogue_inquiry/vicar/mymod/my_sword.json`), you can add item responses for your own items without any dependency on other mods' JSON files. You can also **override** Hemomancy's default responses by placing a file at `data/mymod/dialogue_inquiry/vicar/hemomancy/rite_hint.json` — the last loaded file with that path wins (standard datapack priority).

You can even register responses for vanilla Minecraft items:
```
data/mymod/dialogue_inquiry/vicar/minecraft/diamond.json
```

---

## Fallback Behavior

If no JSON file is found for the held item, the NPC gives a generic "I don't recognize this" response:
- Vicar: `hemomancy.vicar.item_inquiry.unknown`
- Alchemist: `hemomancy.alchemist.item_inquiry.unknown`
- Zealot: `hemomancy.zealot.item_inquiry.unknown`
- Guardian: `hemomancy.guardian.item_inquiry.unknown`

These are translation keys you can override in your resource pack if needed.

---

## Technical Notes

- **Loader class:** `com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryLoader`
- **Registry class:** `com.vincenthuto.hemomancy.common.entity.npc.dialogue.inquiry.ItemInquiryRegistry`
- Data is loaded via NeoForge's `AddReloadListenerEvent` — server-side only, reloads on `/reload`.
- The item's registry key (`BuiltInRegistries.ITEM.getKey(item.getItem())`) is used for lookup; NBT/components are not considered.
