# Myelin Borer — Cortical Drift fiber crawler

**Date:** 2026-09-22
**Status:** Approved design, pending implementation plan
**Entity id:** `hemomancy:myelin_borer`

## 1. Summary

A blind segmented crawler that lives on the Cortical Drift's nerve-bridge network rather than on its
islands. It grips the cables and skitters along them. Left alone it MENDS the network, closing gaps in
severed cables. Provoked - by the player breaking network blocks, striking one, or killing one nearby -
it turns and BORES, chewing fibers out of the bridges and biting with `neural_overload`. It calms after
~30s and the survivors repair the damage.

The result is a feedback loop: travel carefully and the network stays whole and self-healing; vandalise
it and you breed the thing that eats your bridges out from under you.

## 2. Starting point (important)

`MyelinBorerEntity` already exists but is ORPHANED - it is never registered, so it cannot exist in game:
- EXISTS: `common/entity/mob/arthropod/MyelinBorerEntity.java` (94 lines, `Monster`, 8 HP, melee +
  stroll goals, applies `EffectInit.neural_overload` on hit), `client/model/.../MyelinBorerModel.java`
  (head + segment1..3 + tail), `client/render/.../MyelinBorerRenderer.java`, sounds
  `SoundInit.ENTITY_MYELIN_BORER_{AMBIENT,HURT,DEATH}`, lang `entity.hemomancy.myelin_borer` and
  `item.hemomancy.spawn_egg_myelin_borer`.
- MISSING: `EntityInit` entity type, attribute registration, renderer + layer definition registration,
  spawn egg item, loot table, spawn entry, and all behaviour.

This spec finishes it rather than adding a second neural worm. Its body plan (segmented) and its name
(one that bores through myelin - the Drift's own `myelin_sheath` vocabulary) already fit the role.

## 3. Behaviour

### 3.1 Movement
Climbs the network the way `ChalybeateSnailEntity` climbs vent blocks:
- New block tag `hemomancy:cortical_crawlable` = `nerve_fiber`, `nerve_bundle`, `synaptic_node`.
- `onClimbable()` true while touching a tagged block; `maxUpStep` raised while climbing.
- Maths in a pure `MyelinBorerMovementRules` class (no Minecraft types) mirroring
  `ChalybeateSnailMovementRules`, so it is unit testable.
- Gravity stays ON. Leaving the cable means falling.

### 3.2 Mood
One synced entity-data int, `agitation`.
- `0` = TENDING (neutral, mends).
- `> 0` = BORING (hostile, severs). Decrements each tick; ~600 ticks (30s) from a full trigger.
- Raised TO 600 by: player breaking a `cortical_crawlable` block within 16 blocks (`BlockEvent.BreakEvent`
  handler, the only new event wiring); the Borer being hurt; another Borer dying within 16 blocks.
- Target selection only runs while agitated - a calm Borer never attacks.

### 3.3 Goals
Both follow `ChewWoodGoal`'s shape (`MoveToBlockGoal` + progress counter).
- `MendFiberGoal` (agitation == 0): valid target is a GAP - an air position flanked on opposite sides
  by `cortical_crawlable` blocks. After ~40 ticks places `nerve_fiber` with the axis matching the run.
  Because it only fills genuine gaps it can never extrude cable into open void.
- `BoreFiberGoal` (agitation > 0): valid target is a `nerve_fiber`/`nerve_bundle` that has network
  neighbours. After ~60 ticks `destroyBlock(pos, false)`.

### 3.4 Guard rail
Borers NEVER target `synaptic_node`. Anchors and hubs survive, so the network's topology always remains
recoverable - the damage is cut cables, not an erased biome. With menders repairing once calm, the
network cannot spiral into permanent ruin.

## 4. Player payload

New item `ganglion_cluster` (`ItemInit.BASEITEMS`), dropped 1-2 per kill via
`data/hemomancy/loot_table/entities/myelin_borer.json`.

Its purpose: **crafting `synaptic_node` blocks**. Since `synaptic_node` is already in the
`ductilis_conductors` tag, players can build their own junctions and cable runs and chain Ferric
Ductilis manipulations where they choose. This reuses existing systems; no parallel system is invented.

## 5. Spawning

- Added to `worldgen/biome/cortical_drift.json` monster spawners: `weight 8, minCount 1, maxCount 2`
  (Abhorent Thought sits at weight 12 there, so Borers are the less common of the two).
- `MyelinBorerEntity.canSpawnHere` (the hook already stubbed on the class) requires a
  `cortical_crawlable` block nearby, so Borers appear on bridges and around hubs, never adrift in void.
- Keep the existing 8 HP / 0.2 speed / 2 attack attributes.

## 6. Registration work required

`EntityInit` entity type (`MobCategory.MONSTER`, follow the `chitinite` entry), attribute registration,
renderer + layer definition registration on the client event bus, spawn egg item (lang entry already
exists), loot table, and the `cortical_crawlable` tag file.

## 7. Lore

Per `AGENTS.md` tone rules: the Borer is not vermin and not a servant. It is the network's own
maintenance tissue - something the Vagrant Mind grew, or that grew on it, and which kept working long
after the Mind stopped. Harbinger readings treat it as proof the organ still has reflexes; Unstained /
Lethean readings treat cutting the cables as waking something that had finally gone quiet. No deity
framing.

## 8. Testing

- Pure JVM unit tests: `MyelinBorerMovementRules` (climb maths), gap detection, agitation decay.
- Source-contract tests: entity/renderer/attribute/spawn-egg registration, loot table, spawn entry,
  tag contents.
- GameTests: build a fiber run with a gap, tick a calm Borer, assert the gap fills; agitate one and
  assert a fiber is removed; assert `synaptic_node` is never removed; assert tag-driven climbing.

## 9. Documentation contract

`docs/HEMOMANCY_REFERENCE.md` (mob, item, tag, loop), `docs/LORE_REFERENCE.md` (§7 above),
`wiki/Cortical-Drift.md` (player-facing), and a mention wherever Ferric Ductilis materials are listed.

## 10. Out of scope

- A second, opposed species (one mob with two moods was chosen deliberately).
- Any change to the Vagrant Mind clearance rule (a separate open decision).
- Breeding, taming, or a mender-only passive variant.
