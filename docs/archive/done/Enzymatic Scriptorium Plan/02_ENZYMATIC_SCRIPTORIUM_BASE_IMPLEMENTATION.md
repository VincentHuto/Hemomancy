# Enzymatic Scriptorium — Base Implementation

> **Scope:** D3 station behavior before Overexertion.  
> **Goal:** Bias ordinary enchantment generation using Hemomancy enzymes without bypassing vanilla legality.

## 1. Base station behavior

The D3 **Enzymatic Scriptorium** functions as an enhanced enchanting table.

Recommended inventory/menu concepts:

```text
          [ Enzyme Input ]

[ Enzyme Input ]  [ ITEM ]  [ Enzyme Input ]

               [ LAPIS ]

          [ Blood / state readout ]
```

The physical model has eight permanent reservoirs, but ordinary enchanting uses a limited active enzyme profile, recommended **up to three inserted enzyme units/items**.

The GUI and renderer route each inserted enzyme to its matching visible tube.

## 2. Legal-enchantment-first rule

The Scriptorium must never generate an enchantment merely because an enzyme favors it.

Correct resolution order:

```text
1. Determine item enchantability.
2. Determine enchanting power / bookshelf power.
3. Generate the legal enchantment candidate pool.
4. Apply normal item compatibility.
5. Respect enchantment incompatibilities.
6. Respect treasure-only / source restrictions.
7. Resolve ordinary legal level ranges.
8. Apply Scriptorium enzyme weighting to eligible candidates.
9. Roll/select the result.
10. Calculate Scriptorial Strain / curse risk.
11. Pay resources and produce the item.
```

The Scriptorium modifies **weight**, not legality.

## 3. Explicit restrictions

Base Enzymatic Bias must not:

- make Mending table-enchantable if it is not already legal;
- make treasure-only enchantments appear from nowhere;
- ignore enchantment conflicts;
- ignore item-type compatibility;
- exceed normal enchantment maximum levels at D3;
- create an otherwise invalid enchantment combination;
- guess tendency affinity for unknown modded enchantments.

## 4. Enchantment affinity data

Each enchantment may have:

- a **primary tendency**;
- an optional **secondary tendency**;
- or no Hemomancy affinity.

Candidate mappings from the design discussion:

| Enchantment | Primary | Secondary |
|---|---|---|
| Efficiency | Ductilis | Ferric |
| Unbreaking | Ferric | Animus |
| Fire Aspect | Flammeus | Mortem |
| Flame | Flammeus | Lux |
| Frost Walker | Congeatio | — |
| Respiration | Animus | Congeatio |
| Aqua Affinity | Congeatio | Ductilis |
| Quick Charge | Ductilis | Ferric |
| Channeling | Ductilis | Lux |
| Smite | Lux | Mortem |
| Swift Sneak | Tenebris | Ductilis |
| Feather Falling | Ductilis | Tenebris |
| Looting | Mortem | Animus |
| Fortune | Ferric | Lux |
| Silk Touch | Tenebris | Ferric |

These are practical balance classifications, not hard-coded metaphysical laws.

## 5. Datapack-driven affinity format

Recommended resource path:

```text
data/hemomancy/enchantment_affinity/<namespace>/<enchantment_path>.json
```

Example:

```json
{
  "enchantment": "minecraft:efficiency",
  "primary": "hemomancy:ductilis",
  "secondary": "hemomancy:ferric"
}
```

Unknown enchantments should remain safe and retain their original selection weight.

Do **not** infer affinity from registry names.

## 6. Initial weighting model

The exact balance formula remains tunable. A workable starting rule from the discussion is:

```text
effectiveWeight =
    baseWeight
    × clamp(
        1
        + 0.75 × matchingPrimaryEnzymes
        + 0.35 × matchingSecondaryEnzymes,
        1.0,
        3.5
      )
```

Example:

```text
Inserted:
2 Neurotic
1 Ferric

Efficiency:
primary Ductilis
secondary Ferric

weight multiplier:
1 + (0.75 × 2) + (0.35 × 1)
= 2.85x
```

The important contract is:

- primary tendency influence is stronger than secondary;
- duplicate matching enzymes intensify the bias;
- mixed profiles are useful;
- normal D3 bias remains probabilistic rather than deterministic.

## 7. Mixed profiles

### Ferric + Ductilis

Industrial / mechanical bias.

Likely tendencies toward:

- Efficiency;
- Unbreaking;
- Quick Charge;
- Channeling;
- compatible tool/industrial enchantments.

### Mortem + Animus

Predatory / harvest bias.

Likely tendencies toward:

- Looting;
- kill-oriented weapon enchants;
- aggressive harvest themes.

### Tenebris + Ferric

Preservation / precision bias.

Likely tendencies toward:

- Silk Touch;
- Swift Sneak;
- durable/controlled utility.

### Lux + Flammeus

Radiant aggression.

Likely tendencies toward:

- Flame;
- Fire Aspect;
- Smite;
- compatible light/fire effects.

## 8. Cost model

The Scriptorium should not replace existing enchanting costs.

Recommended D3 cost stack:

- ordinary XP requirement;
- ordinary lapis requirement;
- inserted enzyme(s);
- Hemomancy blood cost.

Starting blood targets:

| Offer tier | Suggested blood |
|---|---:|
| Low | 100 mL |
| Medium | 200 mL |
| High / level-30 style | ~300 mL |

The precise blood formula is tunable.

## 9. Commit flow

Preferred survival interaction:

```text
Insert item
↓
Insert lapis
↓
Insert 1–3 enzymes
↓
Preview tendency influence / offers
↓
Choose offer
↓
Commit blood
↓
Consume XP + lapis + enzymes
↓
Resolve enchantments
↓
Resolve Hematic Curse chance
↓
Mark output as Scriptorium-inscribed
↓
Render red enchantment glint
```

If direct Blood Projection into the station is comfortable in practice, use it for commitment.

If it makes the enchanting UI awkward, an explicit server-side blood withdrawal on confirm is acceptable for the base enchanting action. The upgrade rites remain projection-heavy regardless.

## 10. Physical reservoir behavior

The eight model tubes are not merely decoration.

Recommended representation:

- empty tube: clear/dark;
- one inserted matching enzyme: normal fill;
- duplicate matching enzymes: increased fill height / brightness / pulse intensity;
- active enchanting: fluid pulses inward toward the book.

The renderer exposes the full eightfold system without requiring eight normal GUI input slots.

## 11. GUI information

Normal player-facing UI should communicate **direction and intensity**, not necessarily exact RNG tables.

Good:

```text
Dominant Bias: Ductilis
Secondary Bias: Ferric
Bias Strength: Strong
```

Developer/debug surfaces may expose exact effective weights.

Avoid making the survival UI primarily an exact-probability spreadsheet.

## 12. Item provenance component

Recommended state:

```text
ScriptoriumInscriptionData
```

Possible fields:

```text
origin = hemomancy:enzymatic_scriptorium
scriptorialStrain = <optional persisted summary>
excessLoad = <used by Overexertion>
hematicCurse = <optional curse data>
```

The red glint should depend on inscription provenance, not curse state.

## 13. Anvil and grindstone behavior

### Anvil

- rename preserves Scriptorium provenance;
- normal repairs preserve it;
- compatible normal enchantments may be added;
- red glint remains.

### Grindstone

Recommended V1:

- removes ordinary removable enchantments normally;
- does **not** erase Scriptorium provenance by itself;
- does **not** trivially cleanse a Hematic Curse.

## 14. Enchanted books

This remains an open implementation choice.

Recommended first implementation:

- support concrete equipment first;
- defer curse-bearing books;
- add book support only after provenance and curse-transfer rules are explicit.

## 15. Modded enchantment compatibility

Unknown modded enchantments:

```text
no affinity definition
→ no Hemomancy weighting modifier
→ ordinary mod/vanilla behavior
```

Third-party datapacks should eventually be able to provide affinity definitions.

Do not mutate registry enchantment definitions globally.

## 16. Suggested implementation architecture

```text
EnzymaticScriptoriumBlock
EnzymaticScriptoriumBlockEntity
EnzymaticScriptoriumMenu
EnzymaticScriptoriumScreen

EnchantmentAffinity
EnchantmentAffinityLoader
EnchantmentBiasRules
EnchantmentBiasResolver

ScriptoriumInscriptionData
ScriptorialStrainCalculator

HematicCurseRegistry / helpers
OverexertionRules
```

Pure rule classes should be separated from registry/world access where practical.

## 17. Minimum tests

```text
no enzymes → base weights unchanged
primary affinity bias
secondary affinity bias
duplicate enzyme bias
mixed enzyme profile
weight cap
neutral enchantment unchanged
unsupported mod enchantment unchanged
treasure-only legality unaffected
incompatibility rules unaffected
red glint applied to Scriptorium output
red provenance persists through relog
anvil rename preserves provenance
enzyme/blood/XP/lapis each consumed exactly once
```
