# Phlebotomist’s Field Case

A portable leather-and-Hematic-Iron specimen case. It holds **nine distinct specimen identities**, **16 identical full vials per identity**, for **144 vials total**. Each cell displays a representative vial; loose vials remain unstackable.

## Crafting

| Leather | Hematic Iron Scrap | Leather |
|---|---|---|
| Leather | Phlebotomist’s Cabinet | Leather |
| Leather | Hematic Iron Scrap | Leather |

The recipe consumes six Leather, two Hematic Iron Scraps and one cabinet. Glazed cabinets also work; their pane is consumed with the cabinet. No rank or research is required.

## Using the case

Place it, then right-click to unlatch the lid and open its nine-cell menu. It cannot be opened in your inventory or used in the air. Hoppers and other automation cannot access its specimen contents.

A cell requires an exact item-and-component match, including names, source and identification. Separately examined otherwise identical specimens merge. Identified and unidentified vials remain separate. Once an identity reaches sixteen vials, it cannot occupy another cell.

Click a held matching vial into its cell to deposit one. Either mouse button withdraws one onto an empty cursor. Shift-click deposits into a matching or empty cell, or withdraws as many individual vials as fit. Cell shortcuts cannot swap, throw, clone, split or collect the virtual quantity. Only full sample vials fit; cases, racks and empty containers do not.

## Packing and breaking

After everyone leaves the menu and the lid has fully closed, **sneak-right-click with an empty hand** to pack the case. It enters your inventory, or drops nearby if full. If a safe delivery cannot be completed, the placed case stays.

**Breaking packs it too.** It closes active viewers, finishes the current transfer, and drops one case with the remaining specimens. A vial already on a viewer’s cursor returns through normal inventory cleanup; it is not also stored in the packed case.

Creative placement consumes filled cases and creative breaking returns them, so these actions move the collection rather than copy it. Creative pick-block creates an empty case.

A placed occupied case resists ordinary explosions and mob destruction. Pistons cannot move it. Once dropped, the case is an ordinary item entity and is subject to despawning and environmental damage. Virtual contents never spill as hundreds of loose vial entities. The transfer guarantee covers normal server actions, not recovery from crashes between independently saved world/player files or forced removal by commands/mods.

## Validation

Verified on 2026-09-15:

- Compilation and `assemble` passed. Runtime classes, recipe, and models are in the release jar; GameTests and editable Blockbench sources are excluded according to the existing build policy. The editable source remains in the checkout.
- `runBloodInjectionGameTestServer`: all **62 required tests passed**, including **16 field-case tests** and the cabinet regressions.
- Full JVM suite: **2,042 passed, 2 failed**. The existing unrelated failures remain `LegacyMainTestSuiteContractTest` (legacy-test discovery count) and `MorphlingLumenlaceRenameResourceTest` (non-UTF-8 worldgen README). Neither was changed for the field case.
- Eight field-case resource/model files passed JSON, mesh-bound, explicit-UV, and embedded-texture checks. Re-exporting the model produces identical files.

Automated tests cover shared cabinet behavior, exact identity and capacity, component/network serialization, survival and creative placement, canceled placement, pickup, closing locks, multiple viewers and cursors, deferred breaking, refused delivery, stale source rollback, re-entry, and destruction protection.

Live checks still required: all four facings, held/dropped closed-model appearance, lid motion and closing delay, vial placement, small GUI scales, press/release under network latency, real multiplayer packing, disconnect/reconnect, and client mining prediction. Use a disposable test world rather than a saved collection.

Each occupied compartment contains a modeled glass blood vial with an iron stopper. These are baked parts of the placed block model: adding the first sample fills that compartment visually, and removing the last clears it. One modeled vial represents the compartment, not its exact stored quantity.
