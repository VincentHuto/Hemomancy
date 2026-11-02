# Stage 4: Unstained zone lifecycle audit

This is a source audit of the 2026-09-25 checkout. It compares the timed zones created by Still Waters, Pale Consecration, and Lethe Covenant, including the Antiseptic Ground and Lethean Font variants that use the latter two saved records. No gameplay code or save format has changed.

## Stored records and creation

All three records are [SavedData](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/StillWatersSavedData.java) instances retrieved from the **Overworld data storage**. [UnstainedCardinalRiteEvents](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/UnstainedCardinalRiteEvents.java) writes the caster UUID, center, casting dimension string, radius, and `castingLevel.getGameTime() + duration` when a rite completes. Entries are appended; the code does not replace an earlier zone from the same caster. Each `addEntry` marks its own saved data dirty.

| Record | Stable SavedData name | Entry NBT | Creators and dimensions |
| --- | --- | --- | --- |
| [Still Waters](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/StillWatersSavedData.java) | `hemomancy_still_waters` | Root `entries`; per entry `Owner`, `Center`, `Dimension`, `BlockRadius`, `ExpiryTick` | Still Waters: radius 16 blocks, 6,000 ticks. The entry stores the casting dimension. |
| [Pale Consecration](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/PaleConsecrationSavedData.java) | `hemomancy_pale_consecration` | Same keys as Still Waters | Pale Consecration: radius 8 blocks, 12,000 ticks. Antiseptic Ground: radius 12 blocks, 18,000 ticks. Both use the same list and effect handler. |
| [Lethe Covenant](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/LetheCovenantSavedData.java) | `hemomancy_lethe_covenant` | Root `entries`; per entry `Owner`, `Center`, `Dimension`, **`ChunkRadius`**, `ExpiryTick` | Covenant: 5 chunks, 36,000 ticks. Lethean Font: 8 chunks, 72,000 ticks. The runtime query multiplies chunk radius by 16 to get its block radius. |

The three `load`/`save` loops have the same shape, and `removeExpired(currentTick)` has the same rule: remove entries when `currentTick >= expiryTick`, then call `setDirty()` only if the list changed. `getEntries()` returns the mutable list, although current gameplay call sites use it for reading. A common codec or prune helper could reduce duplication, but it must retain the three SavedData names, entry types, radius key/unit, and stored dimension. An abstraction that silently converts `ChunkRadius` to `BlockRadius` would change old worlds.

## Timing and effect routes

| Handler | Tick and storage route | Active effect and geometry |
| --- | --- | --- |
| [StillWatersEvents](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/StillWatersEvents.java) | `LevelTickEvent.Post`, Overworld only, every 100 ticks: prune Still Waters data. | On `LivingDamageEvent.Pre`, checks server-side player, `DamageTypes.MAGIC`, current dimension and tick, then scales damage by `0.70`. Saved-data `isInZone` uses X/Z axis-aligned distance within block radius; Y is ignored. The damage event can run in a different dimension while reading Overworld storage. |
| [PaleConsecrationEvents](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/PaleConsecrationEvents.java) | `LevelTickEvent.Post`, **Overworld only**: prune every 100 ticks, apply effects every 20 ticks. | For each live Overworld entry, searches a three-dimensional `AABB(center).inflate(radius, 4, radius)`, damages each living Monster by 1 magic and applies Slowness I for 60 ticks, then emits particles. Its SavedData `isInZone` is an X/Z-only check and is not called by this effect loop. |
| [LetheCovenantEvents](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/LetheCovenantEvents.java) | `LevelTickEvent.Post`, Overworld only: prune every 100 ticks; every 1,200 ticks, grant eligible players inside a domain 0.2 purity and sync Unstained progress. | Separate `MobSpawnEvent.PositionCheck` in any server dimension can deny a spawn with 50% probability; `LivingIncomingDamageEvent` in any server dimension cancels magic damage for a player with Silver Ward. `isInDomain` uses an X/Z axis-aligned radius of `chunkRadius * 16`, ignoring Y. |

All three active queries skip expired entries even before the next 100-tick prune. The saved list can therefore still contain an expired entry for up to a cleanup interval without its effect applying. The cleanup tick uses Overworld game time; creation and cross-dimension event queries use the current level's game time. A shared scheduler should use the same clock contract and should check cross-dimension behavior before assuming those values are interchangeable.

## Source-level questions before consolidation

1. **Pale Consecration outside the Overworld.** Creation stores the casting dimension, but its 20-tick effect loop returns for every non-Overworld level and then compares each entry's dimension with the Overworld ID. Source inspection therefore finds no active Pale/Antiseptic effect loop for an entry cast elsewhere. Confirm whether rites can complete there and what behavior is intended before changing the loop's dimension gate.
2. **Lethe purity outside the Overworld.** Spawn suppression and Silver Ward protection query Overworld storage from the affected level, but the purity loop runs only on the Overworld level and iterates Overworld players. A domain in another dimension can have the first two effects without periodic purity. Decide whether that difference is intentional.
3. **Spawn-type wording.** Lethe's comment says “natural” spawns, but its [PositionCheck handler](../../../src/main/java/com/vincenthuto/hemomancy/common/rite/unstained/LetheCovenantEvents.java) excludes only `MobSpawnType.SPAWNER`; it does not positively require `NATURAL`. Inspect the eligible spawn types before moving this predicate to shared zone logic.
4. **Pale query shape.** Replacing the effect loop's AABB with `PaleConsecrationSavedData.isInZone` would remove the Y bound and change the exact horizontal edge. A generic zone containment method needs an explicit geometry policy.
5. **Overlap behavior.** Still Waters and Lethe membership return on the first matching zone, so overlaps do not stack those effects. Pale iterates every matching entry; overlapping zones can process the same monster more than once in a 20-tick pass. Preserve or deliberately revise this rule outside a mechanical cleanup.

These are source findings, not live-world results. The first two depend on where rites are actually allowed to complete; the overlap and spawn-type outcomes need targeted gameplay checks before a behavior change.

## Smallest safe extraction order

1. Coordinate the three **100-tick Overworld cleanup calls** behind one `LevelTickEvent.Post` entry point, while each SavedData class retains `removeExpired` and its own key. Preserve the call order and the `currentTick >= expiryTick` boundary. Keep the Pale 20-tick effect and Lethe 1,200-tick purity work in their owners.
2. If the cleanup dispatch is useful, extract common NBT field read/write for `Owner`, `Center`, `Dimension`, and `ExpiryTick`, passing the radius key and unit explicitly. Retain each public entry record and SavedData name. A full generic `SavedData` superclass offers little value here.
3. Review zone containment only after deciding the dimension and geometry questions above. Still Waters and Lethe share an X/Z square query after radius conversion; Pale's active effect uses a three-dimensional AABB. Keep damage, spawn, purity, and particle event hooks separate.
4. For a later implementation, check save/reload across the expiry boundary, overlapping zones, an entry cast outside the Overworld if the rite allows it, Pale mobs above/below the vertical bound, and Lethe spawn types. No gameplay test suite was run for this documentation stage.
