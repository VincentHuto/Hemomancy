# Phase 2: ordinary wax and specialty ambergris

[Index](../HARBINGER_PROGRESSION_REDESIGN.md) · Implement before the final recording/Forge lesson pass.

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

- [ ] Fresh D3 records and replays a creature call using ordinary wax, without acquiring ambergris.
- [ ] Forge construction and ordinary capture/application work with wax.
- [ ] Audio/ancient/pattern-bearing cylinders are never accepted as blank.
- [ ] Old audio, ancient, ordinary-pattern, and master-pattern ambergris stacks survive save/load and retain use.
- [ ] Wax cannot create or stabilize a master pattern; rejection leaves inputs and blood unchanged.
- [ ] Full outputs, interrupted operations, automation, and item swapping preserve existing recovery behavior.
- [ ] Both item types render correctly in inventory, hand, world, and machine views.

Use existing Clairaudiograph/Antecedent and ResonantForge runtime tests. Resource checks alone do not prove component preservation or machine transfer.
