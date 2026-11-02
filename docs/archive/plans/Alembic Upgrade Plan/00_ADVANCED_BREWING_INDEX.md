# Advanced Potion Brewing — Implementation Packet

> **Target:** Hemomancy, Minecraft 1.21.1 / NeoForge.  
> **Status:** Ordered, implementation-oriented work packages.  
> **Canonical design:** `HEMOMANCY_ADVANCED_POTION_BREWING_DESIGN.md`. Read it before changing behavior. This packet breaks its requirements into reviewable stages; it does not replace the design.

## Documents and order

1. `01_ADVANCED_BREWING_CONCEPT.md` — player loop, degree gates, station form, scope, and shared contracts.
2. `02_ALEMBIC_TIERS_AND_RECIPE_ENGINE.md` — tier state, slots, transactional recipe routing, data representation, server menus, and existing-feature regression.
3. `03_REFINED_POTIONS_AND_PILOT_RECIPES.md` — D3 and D5 refinement, exact first three recipes, event behavior, and tooltip rules.
4. `04_COMPOUND_POTIONS_AND_VESSELS.md` — two-effect bottles, three-effect bound vessels, refill transaction, blood substitution, and consumption rules.
5. `05_ALEMBIC_UPGRADE_RITES_AND_PRESENTATION.md` — Alchemist lessons, safe D3/D5 Cardinal Rites, visual growth, escrow, recovery, and end-to-end verification.

## Dependency graph

```text
01 contracts
    ↓
02 tier state, slots, recipe engine
    ├── 03 refined outputs
    └── 04 compounds and vessels
              ↓
05 rites, teaching, presentation, full integration
```

The D3 rite can be prototyped against tier state after document 02, but complete document 05 after all gameplay operations are testable. Each document has its own evidence and acceptance checklist. Do not implement the entire packet in one undifferentiated change.

## Shared rules for every work package

- The vanilla Brewing Stand and existing Hemomancy potion/enzyme recipes still work.
- The Ghastly Alembic is the same block across three persistent tiers: base → Hematic Condenser (D3) → Sanguine Athanor (D5).
- The six original slots remain input 0, flask 1, result 2, catalyst 3, flask output 4, and tincture blood 5. D5-only Catalyst 2 is slot 6.
- Existing distillation, blood filling, gourd filling, hopper routing, and Pallid Retort recipes are regression-sensitive. `DistillationRecipeSerializer` is shared with the Retort.
- No hidden chance decides a brewing output. Only the vessel's **on-use** blood substitution is probabilistic: a server-side 20% roll may spend 1,000 mL of the user's own blood rather than one dose.
- Upgrade rites are safe utility ceremonies. They have no hostile waves, instability collapse, destructive failure, or unrefunded one-time kit. Projected blood remains spent, while offerings and the exact Living Staff are recoverable on cancellation/abort.
- Use registered potion effects and a validated component for formula/dose/provenance data. Never infer semantics from translated potion names.
- Initial compound scope is drinkable, duration-based, one-primary-effect inputs. Instant effects, splash, lingering, arrows, and arbitrary third-party potions are outside this first release.

## Current code anchors

The current machine uses `GhastlyAlembicBlockEntity`, `GhastlyAlembicRenderer`, a resource-backed block model, and the shared `DistillationRecipeSerializer`. There is no current `GhastlyAlembicModel` Java class. The activation packet lives under `common.network.capa.harbinger`. Confirm these anchors against source before changing behavior.

## Milestone gates

| Gate | Prove before moving on |
| --- | --- |
| After 02 | Original Alembic and Pallid Retort tests still pass; tier round-trip and recipe routing work with no upgraded outputs yet. |
| After 03 | Three exact pilot potions brew at proper tiers; combat/effect hooks and cleanup follow the specified numbers. |
| After 04 | Two-effect bottle, vessel binding/refill, and blood substitution survive save/reload with no duplication. |
| After 05 | NPC claims, both safe rites, model states, break/reinstall, cancellation, and recovery pass on a server. |

## Balance follow-up after playtesting

The three pilot recipes and all starting blood/tick numbers are initial targets. Record actual use frequency, blood spent versus doses saved, and whether the D5 bound base makes any potion disproportionately cheap before authoring more refinements or supporting alternate delivery forms.
