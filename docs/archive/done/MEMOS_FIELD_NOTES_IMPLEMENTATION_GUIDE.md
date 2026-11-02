# Memos And Field Notes Implementation Guide

> Design and implementation guide for making Blood Echoes, Fungal Whispers, Pale Lady notes, recipe hints, and lore fragments capturable as player-written notes.
>
> Date: 2026-04-27
> Scope: Liber Sanguinium refactor, Liber Immaculatus parity, Field Notes item loop, Memo discovery definitions, Dictation Table, and non-blocking dialogue behavior.
> Related docs: `GAMEPLAY_DISCOVERY_ROADMAP_MERGED.md`, `LORE_REFERENCE.md`, `HEMOMANCY_REFERENCE.md`

> Implementation status: first vertical slice added on 2026-04-27. `FieldNotesItem`, `MemoHelper`, `MemoDefinitions`, `MemoBookFilter`, `DictationTableBlock`, and `DictationTableBlockEntity` now support stack-local memo capture via `memo_capture:<resource_id>` dialogue events, dictation into Liber stack custom data (`KnownMemos`, `UnlockedLiberEntries`), and HutosLib page filtering for dictated entries. One Hermit Neophyte field note unlocks the first gated Liber page. Remaining work is data-driven memo JSON and broader whisper / Blood Echo content.

---

## 1. Core Idea

After initiation, the player should begin seeing and hearing things they could not perceive before:

- Blood Echoes left in structures, rites, mobs, and old places.
- Fungal Whispers that are fleeting unless recorded.
- recipe hints and ritual clues delivered through in-world discoveries.
- Unstained / Pale Lady notes for players on the purification path.
- conflicting field observations that later become part of the player's permanent books.

The Liber Sanguinium should become the player's growing knowledge repertoire rather than a static guidebook. The Liber Immaculatus should do the same for the Unstained path.

The player does not automatically know everything they hear. They must carry Field Notes, spend ink, and later dictate those notes into the correct book at a Dictation Table.

Important architecture rule:

```text
Memo definitions handle discovery, visibility, capture, and trigger logic.
Existing Liber / Liber Immaculatus book entries remain the permanent readable content.
```

In other words, memos should not become a second book format. A memo points to an existing-style `liber_entry`, and dictation unlocks that entry inside the current book data structure.

This turns the book loop into:

```text
Witness message -> make field note -> spend note durability -> return to table -> dictate into book -> unlock permanent entry
```

---

## 2. Design Goals

### 2.1 Player Fantasy

The player is not reading a manual. They are becoming a field researcher of their own blood.

For Harbingers, the fantasy is:

> "I heard something in the vein, wrote it down before it passed, and later made it part of the Liber."

For the Unstained, the fantasy is:

> "I received a still-water warning, recorded it before clarity faded, and preserved it in the Liber Immaculatus."

### 2.2 Gameplay Goals

- Make the Liber Sanguinium feel alive and personal.
- Give Blood Echoes a concrete mechanical purpose.
- Make recipe hints discoverable without turning JEI into the only guide.
- Let lore be fleeting, repeatable, and worth capturing.
- Make ink a useful recurring resource without creating grind.
- Keep important revelations dramatic by allowing only some dialogue to stop player movement.
- Give Harbinger and Unstained progression parallel but tonally distinct note systems.

---

## 3. System Overview

### 3.1 Main Components

| Component | Role |
|---|---|
| Memo Definition | Data-driven record describing a capturable message or field note. |
| Fleeting Dialogue Tag | Marks a dialogue node/line as repeatable until written down. |
| Field Notes | Portable scratch item that lets the player save a memo from dialogue. A small brown leather notebook with a quill. |
| Ink | Refills Field Notes durability; about 15 memo captures per craft. |
| Dictation Table | Holds the player's Liber like a lectern/table and converts captured field notes into permanent book entries. |
| Liber Sanguinium | Permanent Harbinger knowledge repertoire. |
| Liber Immaculatus | Permanent Unstained knowledge repertoire. |
| Memo Knowledge Capability | Player state tracking seen, captured, dictated, and hidden memos. |

### 3.2 Two-Stage Knowledge Flow

| Stage | Item / System | Description |
|---|---|---|
| Scratch Capture | Field Notes | The player writes down a fleeting memo during dialogue or after witnessing an echo. |
| Permanent Dictation | Liber + Dictation Table | The player transcribes the captured memo into the appropriate permanent book. |

Field Notes are temporary and practical. The Liber is permanent and interpretive. Field Notes capture memo ids; the Liber only receives the final existing-format book entry unlock.

---

## 4. Unlock Timing

### 4.1 Harbinger Path

The player should not see or capture Blood Echoes before initiation.

Recommended unlock:

1. Player finds the Harbinger Hermit.
2. Player performs the first rite / Sanguine Initiation.
3. Blood Volume becomes active and Degree 1 is granted.
4. The player can now perceive Blood Echoes.
5. The Hermit, Liber, or first Blood Echo teaches the Field Notes loop.

### 4.2 Unstained Path

The Unstained equivalent should unlock once the player begins purification or gains clarity.

Recommended unlock:

| Player State | Access |
|---|---|
| Not on Unstained path | Cannot capture Pale Lady notes. |
| Begun purification | Can capture Unstained field notes and Acolyte instructions. |
| Clarity unlocked | Can capture deeper Pale Lady notes and high-clarity warnings. |
| Enlightened | Can capture severe / autoimmune-style warnings. |

---

## 5. Items

### 5.1 Field Notes

Working registry id:

```text
field_notes
```

Field Notes are the portable scratch medium. They are not meant to be a full readable guide on their own. Their job is to hold captured memos until the player returns to a Dictation Table.

Visual identity:

- small brown leather notebook.
- attached or tucked quill.
- practical field-research object rather than a grand magical tome.
- should look like something a Harbinger or Unstained practitioner can carry without ceremony.

#### Behavior

- Must be in the player's inventory to unlock the dialogue option:

```text
Make note of message
```

- The option appears only on memo-tagged dialogue or echo messages.
- Each captured memo consumes 1 Field Notes durability.
- The item has about 15 memo uses per ink craft.
- When durability reaches 0, the option appears disabled / greyed out.
- Field Notes can hold captured memo ids in item NBT or push them immediately into player capability state.

#### Recommended Item State

| State | Meaning |
|---|---|
| `remaining_notes` | Number of captures left. |
| `max_notes` | Default 15. |
| `captured_memos` | Optional item-level list of memo ids not yet dictated. |
| `owner_uuid` | Optional. Use only if item-level storage creates multiplayer exploits. |

### 5.2 Ink

Working registry id:

```text
hematic_field_ink
```

Recipe:

```text
dicentra_sap + hematic_iron_powder + water_bottle + squid_ink
```

#### Behavior

- Combine with Field Notes to restore note capacity.
- One craft restores about 15 memo captures.
- If Field Notes use vanilla durability, ink repairs the damage value.
- If Field Notes use custom NBT, ink resets `remaining_notes` to `max_notes`.

#### Tone

The ink is not ordinary writing fluid. It is a medium that can hold blood-memory impressions without immediately clotting, rotting, or washing away.

### 5.3 Liber Sanguinium

The Liber becomes the permanent Harbinger knowledge book.

It should contain:

- dictated Blood Echo records.
- rite blueprint pages.
- machine notes.
- manipulation / memory notes.
- scar notes.
- Saint epitaphs.
- Fungal Addenda at high degree.

### 5.4 Liber Immaculatus

The Unstained book should mirror the Liber Sanguinium structurally while feeling different in tone.

It should contain:

- Pale Lady notes.
- purification task records.
- Lethean material notes.
- copper / silver / verdigris teachings.
- anti-blood rite clues.
- clarity warnings.
- pruning observations.

### 5.5 Field Notes Naming Note

Earlier brainstorming used "Field Guide" and "Field Notes" loosely. The intended design is **one item**:

```text
Field Notes = the small brown leather notebook + quill item.
```

Do not create a separate Field Guide unless a later tutorial item becomes necessary.

---

## 6. Dictation Table

Working registry id:

```text
dictation_table
```

The Dictation Table is the bridge between ephemeral notes and permanent books. It should behave more like a ritual lectern/table than a normal crafting station.

### 6.1 Core Function

Physical interaction:

1. The player places their copy of the Liber Sanguinium or Liber Immaculatus onto the table.
2. The book remains visibly displayed on the table, similar in feel to a lectern.
3. The player right-clicks the table with Field Notes.
4. The table offers valid captured memos that can be dictated into the placed book.
5. Dictation triggers a small spectacle and blood/rite cost as the notes become written in blood.

Inputs:

- Placed Liber Sanguinium or Liber Immaculatus.
- Field Notes with captured memo ids, held or used on the table.
- Optional ink for corrections / transcription cost.
- Blood cost paid by the player for Harbinger dictation.
- Optional purity/clarity or hemolytic cost for Unstained dictation, if desired later.

Output:

- Permanent existing-format book entry unlocks.
- Field Notes captured list is cleared or marked as dictated.
- Player capability updates memo state.

### 6.2 Suggested UI

Suggested physical/table state:

| State | Item |
|---|---|
| Displayed book | Liber Sanguinium or Liber Immaculatus placed onto table |
| Held item | Field Notes used by player |
| Optional internal/input slot | Ink for correction, stabilization, or batch dictation |

Panel:

- list of captured memos.
- each memo shows title, source, faction, and whether it can be dictated into the inserted book.
- "Dictate" button for one memo.
- "Dictate All Valid" button if convenient.
- current blood cost and projected blood cost for batch dictation.

### 6.3 Validity Rules

| Memo Type | Valid Book |
|---|---|
| Harbinger | Liber Sanguinium |
| Fungal | Liber Sanguinium, unless the memo is specifically Unstained interpretation of fungus |
| Unstained | Liber Immaculatus |
| Neutral / World | Either book, but resulting text may differ |
| Contradiction | Both books can unlock different versions |

### 6.4 Why A Table

The table makes knowledge consolidation a deliberate return-to-base moment. The player does not instantly understand everything in the field. They collect fragments, return, and impose order.

The table also gives the moment ceremony. The player places the Liber down, opens their Field Notes, and spends blood to make fleeting observations permanent.

### 6.5 Dictation Spectacle And Scaling Cost

Dictation should feel magical and slightly costly, not like moving a note between inventories.

#### Spectacle

When a memo is dictated:

- the placed Liber opens or lifts slightly.
- blood-red glyphs or thin vein particles crawl from the Field Notes into the Liber.
- the Field Notes page briefly darkens or empties.
- the Liber page flashes with fresh red script.
- optional scratching/quill sound layered with heartbeat or wet ink sound.

For Unstained dictation:

- use pale/silver-blue script or droplets.
- still preserve the idea of inscription, but make it feel colder and cleaner.
- if using blood would violate the Unstained fantasy, call it "written in stillwater" or "fixed in pale ink" for the Liber Immaculatus.

#### Cost Scaling

The cost should increase as the number of memos being dictated increases.

Recommended formula for Harbinger dictation:

```text
total_cost = base_cost + (memo_count * per_memo_cost) + batch_surcharge
batch_surcharge = per_memo_cost * max(0, memo_count - 1)
```

Example tuning:

| Memo Count | Example Cost |
|---|---|
| 1 memo | 100 blood |
| 2 memos | 250 blood |
| 3 memos | 400 blood |
| 5 memos | 700 blood |
| 10 memos | 1450 blood |

Alternative softer formula:

```text
total_cost = base_cost + floor(per_memo_cost * memo_count ^ 1.25)
```

Recommended first implementation: use the simple linear + surcharge formula so the cost is easy to explain and tune.

#### Cost Rules

- Cost is shown before dictation.
- Player cannot dictate if they cannot pay.
- Cost is paid once per dictation action, not once per opened UI.
- Single-memo dictation should be cheap enough to encourage regular use.
- Large batch dictation should be convenient but visibly more expensive.
- Later/high-tier memos can define a `cost_multiplier` in their memo definition.

---

## 7. Memo Data Model

### 7.1 Memo Definition

Recommended data-driven path:

```text
data/hemomancy/memos/<memo_id>.json
```

Example:

```json
{
  "id": "hemomancy:first_clotted_witness",
  "category": "blood_echo",
  "faction": "harbinger",
  "theme": "blood",
  "min_degree": 1,
  "min_purity": 0,
  "repeat_until_captured": true,
  "important": false,
  "source": "blood_temple",
  "title_key": "memo.hemomancy.first_clotted_witness.title",
  "field_text_key": "memo.hemomancy.first_clotted_witness.field",
  "liber_entry": "hemomancy:book/sanctumsanguinium/blood_echoes/first_clotted_witness",
  "dictation_cost_multiplier": 1.0,
  "rewards": {
    "recipe_hints": ["hemomancy:sanguine_initiation"],
    "advancement": "hemomancy:first_trace"
  }
}
```

### 7.2 Required Fields

| Field | Purpose |
|---|---|
| `id` | Stable memo id. |
| `category` | `blood_echo`, `recipe_hint`, `fungal_whisper`, `pale_note`, `saint_epitaph`, etc. |
| `faction` | `harbinger`, `unstained`, `fungal`, `neutral`. |
| `theme` | Dialogue/book visual theme. |
| `repeat_until_captured` | Whether this memo keeps appearing until saved. |
| `important` | Whether the message should stop movement / demand focus. |
| `title_key` | Localized title. |
| `field_text_key` | Localized scratch-note text. |
| `liber_entry` | Permanent book entry unlocked by dictation. |

`liber_entry` should point into the existing book-entry system. The memo definition controls how the entry is discovered. It should not define the full permanent page content unless the existing book system already expects content inline.

### 7.3 Optional Fields

| Field | Purpose |
|---|---|
| `min_degree` | Harbinger degree gate. |
| `max_degree` | Optional upper bound. |
| `min_purity` | Unstained purity gate. |
| `min_clarity` | Unstained clarity gate. |
| `tendency` | Associated tendency. |
| `source` | Structure, mob, item, rite, or biome source. |
| `expires_after_ticks` | For non-repeatable timed echoes if needed later. |
| `book_variant` | Allows Harbinger and Unstained books to interpret the same memo differently. |
| `rewards` | Recipe hints, advancements, flags, or unlock callbacks. |
| `dictation_cost_multiplier` | Optional multiplier for unusually potent or dangerous memos. |

---

## 8. Dialogue Integration

The current dialogue system is built around:

- `DialogueTree`
- `DialogueNode`
- `DialogueOption`
- `OpenDialoguePacket`
- `DialogueOptionPacket`
- `DialogueScreen`

The memo system should extend these records rather than building a completely separate screen.

### 8.1 Node-Level Memo Metadata

Add memo metadata to dialogue nodes:

```java
public record DialogueNode(
    String id,
    List<String> lines,
    List<DialogueOption> options,
    @Nullable ResourceLocation memoId,
    boolean important,
    boolean blocksMovement
) {}
```

Alternative: introduce a wrapper record `DialogueNodeMetadata` if the record becomes too crowded.

### 8.2 Option-Level Enabled State

The dialogue option needs to support disabled options for "Make note of message."

Recommended extension:

```java
public record DialogueOption(
    String text,
    String nextNodeId,
    String eventId,
    boolean enabled,
    @Nullable String disabledReason
) {}
```

The client greys disabled options. The server still validates the event so players cannot spoof memo capture.

### 8.3 Memo Capture Option

When a node has `memoId`, the dialogue builder should append a standard option:

```text
Make note of message
```

Enabled if:

- player has Field Notes in inventory.
- Field Notes has remaining note capacity.
- player has not already captured or dictated this memo.
- path and progression gates allow this memo.

Disabled if:

| Missing Condition | Disabled Text |
|---|---|
| No Field Notes | "Requires Field Notes" |
| No remaining notes | "Field Notes are full" or "No ink remains" |
| Already captured | "Already noted" |
| Wrong path | "The words will not settle" |
| Too low degree / clarity | "You cannot hold this thought yet" |

### 8.4 Repeatable Until Written Down

Fleeting memo nodes should repeat until captured or dictated.

State logic:

| Player Memo State | Behavior |
|---|---|
| Unseen | Memo can appear. |
| Seen but not captured | Memo can repeat. |
| Captured in Field Notes | Memo stops repeating in the field but is not yet permanent. |
| Dictated into Liber | Memo is permanently readable. |
| Suppressed / obsolete | Memo no longer appears because later state supersedes it. |

### 8.5 Important Dialogue And Movement

Most dialogue should not stop movement.

Recommended rules:

| Dialogue Type | Stops Movement? | Why |
|---|---|---|
| Normal NPC guidance | No | Player should not feel trapped by routine dialogue. |
| Field memo / recipe hint | No | It is a note-worthy observation, not a cutscene. |
| Minor fungal whisper | No | It should feel intrusive but not fully paralyzing. |
| Major revelation | Yes | The moment is too important or frightening to ignore. |
| Archon truth / Qliphoth communion | Yes | Cosmic-horror threshold moment. |
| Pale Lady high-clarity warning | Usually yes | Stillness becomes command-like at high clarity. |

Implementation options:

- For non-blocking dialogue, render the panel as an overlay while leaving normal controls active.
- For important dialogue, keep the current Screen-style focus and optionally vignette.
- Use `important` and `blocksMovement` as separate flags so a message can look important without freezing the player.

---

## 9. Blood Echo Integration

### 9.1 Visibility Gate

Blood Echoes should become visible after initiation.

Recommended capability check:

```text
IBloodVolume.active == true
and IInitiatoryDegree >= 1
```

Optional later gates:

- tendency score reveals certain echo categories.
- degree reveals deeper echo layers.
- Archon reveals fungal reinterpretations.
- Unstained clarity reveals Pale Lady counter-readings.

### 9.2 Echo Interaction

When the player interacts with an echo:

1. Server checks whether the player can perceive it.
2. Server opens a memo-tagged dialogue or sends a memo overlay.
3. Player can read/hear the fleeting text.
4. If Field Notes are available, "Make note of message" can capture it.
5. If not captured, the echo can repeat later.

### 9.3 Echo Categories

| Category | Example Source | Book Destination |
|---|---|---|
| Blood Witness | Blood Temple, old battle site | Liber Sanguinium |
| Recipe Hint | machines, rite failures, structures | Liber Sanguinium or Liber Immaculatus |
| Saint Epitaph | Saint chamber | both, with different readings |
| Fungal Whisper | degree 4+, fungal areas, Qliphoth | Liber Sanguinium |
| Pale Note | Unstained church, clarity events | Liber Immaculatus |
| Contradiction | shared historical event | both books, different interpretation |

---

## 10. Book Refactor

### 10.1 Current Book Direction

The existing `BloodyBookItem` opens the HutosLib book titled:

```text
hemomancy:sanctumsanguinium
```

The implementation guide should preserve the existing book framework where possible instead of replacing it.

### 10.2 Refactor Goal

The books should read from:

- static base chapters.
- player-unlocked memo entries.
- dictated field notes.
- late reinterpretation overlays.

### 10.3 Unlock Model

Recommended player capability:

```text
IMemoKnowledge
```

Tracks:

| Field | Purpose |
|---|---|
| `seenMemos` | Player has encountered the message. |
| `capturedMemos` | Player wrote it into Field Notes. |
| `dictatedMemos` | Player made it permanent in a Liber. |
| `suppressedMemos` | Message should no longer appear. |
| `unlockedBookEntries` | Permanent book entry ids. |

### 10.4 Permanent Book Entries

When a memo is dictated:

1. validate the Dictation Table has the correct placed book.
2. validate the player is using Field Notes that contain the memo.
3. calculate and display blood / dictation cost.
4. validate the player can pay.
5. play dictation spectacle.
6. mark memo as dictated.
7. unlock the corresponding existing-format book entry.
8. clear memo from Field Notes.
9. optionally grant advancement / recipe hint / toast.

### 10.5 Reinterpretation Entries

Some dictated entries should gain addenda later.

Example:

| Original Entry | Later Addendum |
|---|---|
| "The Hermit gave his heart as sacred inheritance." | Archon addendum: "The heart was also a prepared spore-vessel." |
| "The Saint mastered Ferric permanence." | Fungal addendum: "Permanence was the failed wish of a fruiting body." |
| "The Pale Lady cleanses corruption." | Clarity addendum: "Cleansing can become refusal of life." |

---

## 11. Networking And Persistence

### 11.1 Packets

Likely packets:

| Packet | Direction | Purpose |
|---|---|---|
| `OpenMemoDialoguePacket` or extended `OpenDialoguePacket` | server -> client | Opens memo-tagged dialogue/overlay. |
| `CaptureMemoPacket` | client -> server | Player chose "Make note of message." |
| `SyncMemoKnowledgePacket` | server -> client | Syncs unlocked memo/book state. |
| `DictateMemoPacket` | client -> server | Player transcribes selected memos at Dictation Table. |
| `SyncDictationTablePacket` | server -> client | Syncs placed book, available memos, and calculated cost. |

If extending `DialogueOptionPacket`, memo capture can be represented as an event id:

```text
memo_capture:<memo_id>
```

Server-side validation must still check inventory, capacity, path, and memo state.

### 11.2 Persistence

Store permanent knowledge on the player, not just in the item.

Field Notes item can store scratch state, but dictated knowledge should persist through:

- death if normal player capability does.
- item loss.
- book replacement.
- multiplayer server reload.

### 11.3 Item Loss Rule

Recommended:

- Captured but not dictated memos live in Field Notes item NBT.
- Dictated memos live in player capability.
- If the player loses Field Notes before dictating, they lose those scratch notes.

This makes the scratch item matter without making permanent knowledge fragile.

### 11.4 Dictation Cost Validation

All costs must be calculated server-side.

Server validates:

- placed book is present and is the correct book type.
- Field Notes item contains requested memo ids.
- memo ids are valid and still capturable/dictatable.
- player has enough blood or path-appropriate resource.
- memo has not already been dictated.
- memo unlocks an existing valid `liber_entry`.

---

## 12. Recipes

### 12.1 Field Notes

Suggested ingredients:

- book or writable book.
- paper.
- string or leather.
- hematic iron nugget / powder.

Tone: a small bound scratchbook prepared to hold blood-impressions.

### 12.2 Hematic Field Ink

User-proposed recipe:

```text
dicentra_sap + hematic_iron_powder + water_bottle + squid_ink
```

Result:

```text
hematic_field_ink
```

Effect:

- restores Field Notes to 15 available memo captures.

### 12.3 Dictation Table

Suggested ingredients:

- lectern.
- hematic iron.
- ink.
- polished venous stone or similar Harbinger writing surface.
- optional copper/pale variant support later for Unstained.

---

## 13. Example Player Flow

### 13.1 First Harbinger Memo

1. Player finds Hermit.
2. Player performs Sanguine Initiation.
3. Player receives or crafts Field Notes.
4. A faint Blood Echo appears near the Blood Temple.
5. Echo opens a non-blocking blood-themed memo dialogue.
6. Player chooses "Make note of message."
7. Field Notes durability decreases from 15 to 14.
8. Player returns to Dictation Table.
9. Player places their Liber Sanguinium onto the table.
10. Player right-clicks the table with Field Notes.
11. Table shows "Clotted Witness" and the blood cost.
12. Player pays the cost; the notes become written in blood.
13. "Clotted Witness" entry unlocks in the existing Liber book structure.
14. Entry hints at a utility rite or next exploration clue.

### 13.2 First Fungal Whisper Memo

1. Player reaches degree 4+.
2. Minor Fungal Whisper occurs as non-blocking overlay.
3. It repeats occasionally until captured.
4. Player writes it down.
5. Dictation adds a Fungal Addendum page.
6. At degree 7, the same entry gains a harsher reinterpretation.

### 13.3 First Unstained Memo

1. Player begins purification.
2. Acolyte or Pale Lady note appears near an altar / cold water / Lethean Poppy.
3. Player captures it in Field Notes.
4. Dictation Table accepts Liber Immaculatus instead of Liber Sanguinium.
5. Entry hints at purity task, copper vigil, or Lethean recipe.

---

## 14. Implementation Phases

### Phase 1: Data And Capability

- Add `MemoDefinition` data model.
- Add memo reload listener or registry.
- Add `IMemoKnowledge` player capability.
- Add basic memo states: seen, captured, dictated.

### Phase 2: Field Notes And Ink

- Add Field Notes item.
- Add Hematic Field Ink item and recipe.
- Add durability/NBT capture count.
- Add inventory helper for finding writable Field Notes.

### Phase 3: Dialogue Extension

- Extend `DialogueNode` with memo id and importance metadata.
- Extend `DialogueOption` with enabled/disabled state.
- Add automatic "Make note of message" option.
- Add server-side capture validation.
- Add non-blocking dialogue mode for non-important messages.

### Phase 4: Dictation Table

- Add block, block entity, menu, and screen.
- Add placed-book state so the Liber sits on the table like a lectern/table.
- Add right-click-with-Field-Notes interaction.
- Add Field Notes + Liber transcription flow.
- Add blood cost calculation and validation.
- Add dictation particles/sound/spectacle.
- Unlock permanent book entries from dictated memos.

### Phase 5: Book Integration

- Surface existing book JSON content.
- Add memo-driven dynamic entries.
- Add Harbinger and Unstained variants.
- Add late addenda / reinterpretation entries.

### Phase 6: First Vertical Slice

Implement "The First Trace":

- one Blood Echo after initiation.
- one memo-tagged dialogue.
- one Field Notes capture.
- one Dictation Table unlock.
- one Liber page hinting at the next rite or recipe.

### Phase 7: Expand Content

- Fungal Whispers.
- recipe hints.
- Saint epitaphs.
- Pale Lady notes.
- Blood Moon echoes.
- Qliphoth addenda.

---

## 15. Design Guardrails

- Do not show Blood Echoes before initiation.
- Do not make Field Notes a tedious tax on every normal conversation.
- Do not require note capture for critical progression unless the player is clearly taught the mechanic.
- Do not permanently lose major progression if Field Notes are lost.
- Do not let disabled dialogue options be client-only; validate on server.
- Do not make every whisper freeze the player.
- Do not let the Liber become a static wiki again.
- Do not flatten Harbinger and Unstained note tone into the same voice.

---

## 16. Naming Suggestions

### Items

| Working Name | Tone |
|---|---|
| Field Notes | Practical, readable, player-facing. |
| Hematic Field Ink | Harbinger-leaning but usable as base ink. |
| Stillwater Ink | Optional Unstained variant. |
| Dictation Table | Clear and functional. |
| Clotted Witness | First Blood Echo memo. |
| Pale Marginalia | Unstained note category. |

### Memo Categories

| Category | Use |
|---|---|
| Blood Echo | General Harbinger discovery. |
| Fungal Addendum | Fungal reinterpretation. |
| Rite Fragment | Ritual clue. |
| Memory Plate | Manipulation / Somatic Loom note. |
| Saint Epitaph | Saint trial clue. |
| Pale Note | Unstained / Pale Lady note. |
| Verdigris Margin | High-clarity Unstained addendum. |

---

## 17. Final Recommendation

Make memos the player-facing form of Blood Echoes.

The first implementation should not try to convert every book page at once. Build one complete slice:

```text
Initiation -> perceive Blood Echo -> write memo -> spend Field Notes use -> dictate at table -> unlock Liber entry
```

Once that loop works, it becomes the pattern for almost everything Hemomancy wants to do:

- recipe discovery.
- Saint clues.
- Fungal Whispers.
- Unstained clarity warnings.
- Blood Moon secrets.
- Qliphoth reinterpretations.
- exploration cleanup.

The memo system is the missing middle layer between world events and permanent knowledge. It gives Hemomancy a reason to be discovered slowly, written down carefully, and understood too late.
