
# Hemomancy Inventory Inquiry Dialogue Guide

Inventory inquiry lets the player ask an NPC to examine objects already carried in the player's inventory. It is a topic-hub system, not a main-hand-only interaction.

## Runtime flow

1. The NPC builds its ordinary progression dialogue.
2. `DialogueItemInquiryNodes` scans the player's inventory in slot order.
3. Stateful providers inspect dynamic stacks first.
4. Ordinary stacks resolve through `ItemInquiryRegistry` by speaker ID and item registry ID.
5. Refusal policy may suppress inquiry or replace procedure with a terse identification response.
6. One inquiry topic is added for every distinct supported item state.
7. Unsupported objects are skipped; only items with a resolved inquiry appear in the topic grid.

The main runtime localization source is:

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

Distinct states of the same item ID receive distinct inquiry nodes. Their dynamic details are shown as literal evidence lines beneath the speaker's localized interpretation.

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
