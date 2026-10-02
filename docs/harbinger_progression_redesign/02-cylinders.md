# Phase 2: ordinary wax and specialty ambergris

[Index](HARBINGER_PROGRESSION_REDESIGN.md) · Implement before the final recording/Forge lesson pass.

## Outcome and fixed behavior

Ordinary recording and basic equipment-pattern transfer do not require whale material. Preserve the existing Ambergris Cylinder and all data it can already carry.

| Medium | Routine audio | Ordinary Forge patterns | Ancient records | Create/stabilize master patterns |
|---|---|---|---|---|
| New Wax Cylinder | Yes | Yes | Not generated as ancient records | No |
| Existing Ambergris Cylinder | Yes, including legacy recordings | Yes, including legacy patterns | Preserve existing content | Yes, subject to existing D7 machine gate |

Implementation default: honeycomb + Active Befouling Ash + paper produces two blank Wax Cylinders. Keep the ambergris recipe as a specialty recipe. Honeycomb supplies the ordinary wax; do not introduce a separate beeswax production system.

Do not add longer recordings, voice recording, new audio storage formats, or new ancient programs as part of this cleanup.

## Read before editing

- [Current cylinder recipe](../../src/main/resources/data/hemomancy/recipe/ambergris_cylinder.json).
- [Forge construction recipe](../../src/main/resources/data/hemomancy/recipe/blood_structure/resonant_forge.json).
- [Clairaudiograph machine](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/functional/ClairaudiographBlockEntity.java).
- [Forge machine](../../src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/crafting/ResonantForgeBlockEntity.java).
- ItemInit, AmbergrisCylinderItem/Renderer, AncientRecordings, ResonantForgeMenu, ClinicalBloodKnowledge/Dialogue, material atlas/data, relevant menu/JEI handling and block-item displays.

## Instructions

1. Add the ordinary item and its recipe, authored item appearance, translation, tooltip, recipe discovery, and inventory/creative presentation. Use distinct native 16x16 item artwork within the existing asset style. Reuse suitable rendering/component behavior while making the two materials visually distinguishable.
2. Replace duplicated hard-coded ambergris-only ordinary acceptance checks with a small, shared distinction between supported cylinders and master-capable cylinders. Inspect all consumers before choosing tags or a helper; do not scatter another set of item-ID comparisons.
3. Update machine insertion, shift-click routing, automation handlers, start validation, completion validation, and recipe output handling together.
4. A blank cylinder must have no ancient recording, ordinary audio recording, or resonant pattern. Never overwrite any of those components by treating the stack as plain wax.
5. Ordinary recording preserves the existing blood-vial consumption/empty-vial result and preview/play semantics.
6. Change base Forge construction to accept ordinary wax; retaining ambergris as an alternative is acceptable. A whale expedition must not remain a hidden ingredient requirement.
7. Accept either medium for ordinary grinding and application at the appropriate tier. Preserve source-item components, curse behavior, costs, and ordinary cylinder consumption.
8. Restrict creation and stabilization of master patterns to Ambergris Cylinders at Masterwork tier. Explain the rejection before spending blood or changing inputs. Do not add a new conversion machine.
9. Preserve all existing Ambergris Cylinder records/patterns and their registry ID. AncientRecordings should continue generating and recognizing the legacy ancient item. Saved recordings must not need re-recording.
10. Update clinical recipe awards, item inquiries, Clairaudiograph lesson text, Forge teaching, material atlas, JEI, and documentation. The ordinary recipe must not remain locked behind discovery of ambergris.

## Acceptance

2026-10-01 focused follow-up: Both wax and ambergris paid capture/application now have fresh-block-entity serialization/resume coverage. Exact custom names, damage, enchantments and patterns, initial costs, remaining tick boundaries, ordinary consumption, single maintenance increments and no post-completion replay passed in the 20-case Forge/Scriptorium suite. The test tick helper's extra tick and same-position fixture reuse were corrected after failed runs; no production mechanic changed. This is not chunk-unload, cold-server, native interruption or complete older-world compatibility acceptance. Phase 8 records the runs.

- [ ] Fresh D3 records and replays a creature call using ordinary wax, without acquiring ambergris.
- [ ] Forge construction and ordinary capture/application work with wax.
- [ ] Audio/ancient/pattern-bearing cylinders are never accepted as blank.
- [ ] Old audio, ancient, ordinary-pattern, and master-pattern ambergris stacks survive save/load and retain use.
- [ ] Wax cannot create or stabilize a master pattern; rejection leaves inputs and blood unchanged.
- [ ] Full outputs, interrupted operations, automation, and item swapping preserve existing recovery behavior.
- [ ] Both item types render correctly in inventory, hand, world, and machine views.

Use existing Clairaudiograph/Antecedent and ResonantForge runtime tests. Resource checks alone do not prove component preservation or machine transfer.

## 2026-09-30 implementation and live follow-up

The ordinary wax route works in disposable `HarbingerWaxRoute_20260930`: inventory crafting produced two cylinders; a fresh cow sample recorded and replayed; a wax offering supported Forge construction; ordinary grinding and application transferred Efficiency III and Unbreaking II. The recording, machine blood, and equipment components survived a full client restart. No ambergris was supplied or acquired. Degree, ingredients, projection item, equipment, platform, and formation placement were assisted, so natural acquisition and pacing remain unverified.

The live run exposed a menu bug: shift-clicking the recipe's stack of two blank cylinders inserted nothing. Forge routing now checks a one-item copy for blankness and transfers from the original stack. One cylinder enters the grinder and the remainder stays in inventory; an occupied grinder does not send the remaining blank to application. Machine start checks still require one cylinder. New GameTests cover wax and ambergris stacks, occupied-grinder rejection, audio/ancient rejection, and pattern-preserving application routing. The final focused run passed 19 Forge/Scriptorium and 88 blood-injection tests. The fixed stack interaction and saved recording playback were also replayed in the client.

These checks do not close every acceptance box above. Natural survival, older-world compatibility, multiplayer, all interruption/automation cases in a client, master rejection presentation, and both media's full visual review remain open. Exact assists, costs, logs, and screenshots are in [the Phase 8 handoff](08-validation-and-handoff.md).
