# Track 2: the trade data model

**Status (2026-10-31): built, uncommitted.** What later tracks code against (all in `common/entity/npc/trade/`):
- `NpcTradeRegistry`: `offers(speaker)` (sorted by `order` then id), `byId(id)`, `all()`, `errors()` (files that failed to parse plus cards the cross-file rules dropped; Track 7's GameTest reads it). Server-side only; nothing syncs, since cards ride in the hub presentation (Track 3).
- `NpcTradeOffer(id, speaker, side, title, line, order, requirements, want, give, giveFine, rapport, stock, showLocked, opens)`. `opens` is an optional lang key, only on `custom` cards: what the standing leads to, since a Court card gives no goods.
- `NpcTradeWant` (sealed): `Items(match, count, appraisal)`, `Blood(amount, source)` (only source today is `Blood.TITHE = "npc_tithe"`; Track 3 holds the `BloodAccounting.Source` constant for it), `Bundle(parts)` (2-3 `Items`), `Custom(act)`. `itemParts()` lists the `Items` a card takes stacks for; `exclusiveClaim()` is `blood` / `custom:<act>`.
- `NpcTradeMatch(items, tags)`: an ingredient held as ids. `matches(stack)` and `toIngredient()` (for display) resolve tags when called.
- `NpcTradeRequirements(path, degreeAtLeast, stageAtLeast, rapportAtLeast, advancement, bloodActive)`. `firstFailure(ServerPlayer, rapport)` returns the first lock key (`LOCKED_PATH` / `_DEGREE` / `_STAGE` / `_BLOOD` / `_ADVANCEMENT` / `_RAPPORT`, all in `en_us.json`), checked in that order through `ProgressionGates`. A blood price sets `bloodActive`.
- `NpcTradeStock(uses, restockDays)`: `UNLIMITED = 0`, `limited()`, `windowTicks()` (`TICKS_PER_DAY = 24000`).
- `NpcTradeGive(item, count, components)`: `toStack(HolderLookup.Provider)` builds the stack. The components are held as JSON text, and the live loader decodes each give once at load to check it.
- `NpcTradeAppraisals`: `Grade {POOR, FAIR, FINE}` with `key()` (`hemomancy.trade.grade.*`, in `en_us.json`) and `min()`, `grade(stack, appraisal)`, and `spec(id)` returning `Spec(grader, params, canGradeFine)`. It ships `none`, `durability` (<25% left is POOR, >=75% is FINE) and `rarity`. Track 5 adds a grader as one more `GRADERS` row, with the `appraisal_*` params it reads. The loader rejects an unknown grader, and a param the grader doesn't list.
- `NpcTradeSides.refuses(speaker, player)` (Unstained speaker and `ProgressionGates.hasDegree(player, 1)`) and `refusalKey(speaker)` = `hemomancy.trade.<speaker>.refuse_harbinger`. Track 5 writes those lines.
- `NpcTradeGiveRules`: `NEVER_SOLD` (`hemomancy:trade/never_sold`, a new tag seeded with `fungal_spine` and `memory_of_vesper`), `isManipulationMemory`, `isStationUpgrade` (read from `StationUpgradeCatalog`), `isBannedById`. Track 7's guardrail can call these.
- `NpcTradeLoader` (registered in `Hemomancy.onAddReloadListeners` as `new NpcTradeLoader(event.getRegistryAccess())`), with `NpcTradeLoader.parse(id, json, Environment)` for tests. `NpcTradeTagEvents` re-runs the cross-file rules on `TagsUpdatedEvent` (`SERVER_DATA_LOAD`).

Deviations from the plan, with reasons:
- The loader extends the repo's shared `JsonResourceReloadListener` rather than `SimpleJsonResourceReloadListener`, because R-6 and `ReloadListenerGuardrailTest` require it. A file that isn't valid JSON is logged and skipped by the base class but not counted in `errors()`. Only parse and validation failures are counted.
- Tags aren't bound while data loads, so the one-want-per-card rule runs twice. On install, a tag clashes only with the same tag (or the same id). On `TagsUpdatedEvent`, tags are expanded to their items and the `never_sold` tag is checked. When two cards clash, the lexically first id stays and the other is dropped and counted.
- The rule also covers blood and customs: one `blood` card per speaker and one card per custom. Within a bundle, no two parts may want the same thing.
- The `memory_*` ban has one exception, `memory_thread`, which is a weaving material that Track 5's Mnemonist catalogue gives. `crude_memory_*` is banned too. In the live loader, any `BloodMemoryItem` or `HematicMemoryItem` is also banned by class.
- Extra checks the loader makes: an Unstained card can't set `degree_at_least` above 0 (same reason as `path: harbinger`). A `custom` act must be the speaker's own house custom (`CourtHospitality.customAct(role.house())`), and a custom card can't have gives, `give_fine` or stock. `give_fine` needs a grader that can actually grade FINE. Unknown fields fail the file. `title`/`line`/`opens` must be keys (no whitespace, at least one dot). An ingredient takes only `item`/`tag` entries (no `type`). A give count can't pass the item's stack size. A Court card must be a `custom` (Q-1: no prices; added in the final review).
- `rarity` ships now (beside `none` and `durability`), because it reads only the vanilla `Rarity` and has no component guesswork.
- No offers are shipped (that's Track 5). `NpcTradeLangKeysTest` already walks `data/hemomancy/npc_trade/` and checks every title, line, `opens`, lock and grade key.

**Goal:** an NPC's offers are datapack JSON, loaded by a reload listener next to `ItemInquiryLoader`, and parsed into immutable records that Track 3's service and Track 4's view both read.

## Where the files go
`data/hemomancy/npc_trade/<speaker>/<offer>.json`. `<speaker>` is the same id `dialogue_inquiry/` uses (`alchemist`, `artificer`, `cicatrix_anchorite`, `mnemonist`, `vicar`, `voyager`, `votary_wayfarer`, `zealot`, `guardian`, the `court_*` roles, and so on), plus the Circus ids from Track 1 (`circus_ringmaster`, `circus_<performer>`). The offer id is `hemomancy:<speaker>/<offer>`. Singular `npc_trade`, matching the repo's 1.21 style.

## Sides
`NpcTradeSides` maps every speaker id to `HARBINGER`, `UNSTAINED`, `COURT` or `CIRCUS` (README). It's a Java table, not data: a datapack can add offers, not change who trades with whom.
- `UNSTAINED`: the box refuses a player for whom `ProgressionGates.hasDegree(player, 1)` holds (Q-2). Checked when cards are built and again in the service, so no card is ever sent to a Harbinger.
- `blood` wants are legal only on `HARBINGER` speakers (Q-3), `custom` only on `COURT` (Q-1). The loader fails a file that breaks either rule.
- A speaker with a folder but no table row fails the loader.

## The schema
```json
{
  "title": "hemomancy.trade.alchemist.ferric_draw.title",
  "line": "hemomancy.trade.alchemist.ferric_draw.line",
  "order": 20,
  "requires": { "path": "harbinger", "degree_at_least": 2, "rapport_at_least": 0 },
  "want": {
    "kind": "items",
    "ingredient": { "item": "hemomancy:ferric_enzyme" },
    "count": 2,
    "appraisal": "none"
  },
  "give": [
    { "item": "minecraft:redstone", "count": 4 }
  ],
  "rapport": 2,
  "stock": { "uses": 4, "restock_days": 3 }
}
```

### `requires` (all optional)
- `path`: `harbinger`, `unstained`, `any` (default). Read through `ProgressionGates`. The side's own rule comes first: an Unstained card can't set `harbinger`.
- `degree_at_least` / `stage_at_least`: through `ProgressionGates`, never a literal comparison in code.
- `rapport_at_least`: Track 3's ledger.
- `advancement`: a resource location the player must have.
- `blood_active`: true means `volume.isActive()` must hold. Implied by any blood price.

A card that fails `requires` is **hidden** unless it sets `"show_locked": true`. Then it draws greyed with the first failed reason as a key (`hemomancy.trade.locked.degree`, `.rapport`, `.advancement`). Use `show_locked` for one or two carrots per NPC, not the whole list.

### `want`: one of four kinds
| Kind | Fields | Paid how |
| --- | --- | --- |
| `items` | `ingredient` (vanilla `Ingredient` JSON: item or tag), `count`, `appraisal` | Taken from the slots the player chose (Track 3). |
| `blood` | `amount` (mB), `source` (a `BloodAccounting.Source` id) | `BloodAccounting.pay`. `HARBINGER` speakers only (Q-3). |
| `bundle` | `parts`: a list of `items` wants, two or three at most | Every part must be met; taken together. |
| `custom` | `act` (a `CourtHospitality` custom id) | `COURT` only: the house custom, recorded through `CourtLife.recordAct`. No price, no stock (Q-1). |

**Every card on one NPC wants something different.** The loader rejects two offers on one speaker whose `items` wants match the same item (tags resolved at load). This is the "each box expects different items" rule, enforced, not just intended.

### `appraisal`
A named grader, not free-form code. Each grader turns a stack into a grade, `POOR`, `FAIR` or `FINE`, from its components:
- `none`: always `FAIR`.
- `tendency`: a blood-bearing stack graded by how far its dominant tendency matches `appraisal_tendency`.
- `freshness`: a specimen graded by its age.
- `rank`: a memory or engram graded by rank, with `appraisal_min_rank`.
- `durability`: worn gear graded by remaining durability.
- `rarity`: a Circus curiosity graded by the item's `Rarity` (common `FAIR`, uncommon and up `FINE`; nothing is `POOR`).

Graders live in `NpcTradeAppraisals` (one static map of id → `Grader`). Track 2 ships `none` and `durability`. The tendency, freshness and rank graders land with the catalogue rows that need them (Track 5), each after reading which data components those items actually carry. Don't guess component names.

`POOR` stacks are refused (the NPC says why, `hemomancy.trade.<speaker>.appraise.poor`). `FINE` pays the `give_fine` list instead of `give` when one is present, otherwise one extra rapport.

### `give`
A list of item stacks (`item`, `count`, optional `components`), plus an optional `give_fine`. No loot tables in the first pass: a fixed result reads plainly on the card. Banned outright (the loader fails the file, and Track 7 adds a guardrail):
- any `memory_*` manipulation memory, or `hematic_memory`;
- any station upgrade kit (station kits keep their own gates, `docs/STATION_UPGRADES.md`);
- any item tagged `hemomancy:trade/never_sold` (new tag, for rank items, proofs and unique bosses' drops).

### `rapport` and `stock`
- `rapport`: what a completed trade adds (default 1, a `custom` 2).
- `stock.uses`: completed trades per restock window, per player (default unlimited). `stock.restock_days`: in-game days (24000 ticks of `level.getDayTime()`), default 1.
- `rotation` (Track 6) is reserved; the loader warns and ignores it until then.

## Code
- `common/entity/npc/trade/NpcTradeOffer` (record: id, speaker, title, line, order, requirements, want, give, giveFine, rapport, stock, showLocked).
- `NpcTradeWant` (sealed: `Items`, `Blood`, `Bundle`, `Custom`), `NpcTradeRequirements`, `NpcTradeStock`.
- `NpcTradeLoader extends SimpleJsonResourceReloadListener`, registered in `Hemomancy.java` beside `new ItemInquiryLoader()`. It logs and skips a bad file rather than failing the reload, and keeps an error count for the GameTest in Track 7.
- `NpcTradeRegistry`: `offers(speaker)` sorted by `order` then id, `byId(id)`.
- Records hold lang keys, never English.

## Tests
- `NpcTradeLoaderTest`: parses every kind, rejects a banned give, rejects two cards on one speaker that want the same item, rejects a `blood` want off the Harbinger side and a `custom` off the Court, rejects an unknown speaker, and keeps going past a bad file.
- A resource test that every `title`, `line` and lock reason key used by a shipped offer exists in `en_us.json`.
