# Track 7: Farming and water

**Goal:** blood grows things and baits water. Three farming tiers that trade area for yield, Hemomancy crops that need
blood-fed ground, and chum as a pool the player makes by projecting into water.

**Depends on:** Track 1 (the Fluid target), Track 3 (basins clot), Track 6 (gutters, stains, the bone meal gouge).

## What exists

- **No farming hook at all.** Nothing subscribes to `CropGrowEvent` or `BonemealEvent`. There is no farmland
  subclass.
- **Gourds** grow on a vanilla `StemBlock` and need no blood. Shears on a gourd have a bug (Track 0).
- **Scab stalagmites.** `BloodScabPointedDripstoneBlock extends PointedDripstoneBlock` (`BlockInit` near line 126),
  registered without `.randomTicks()`, so it never drips or grows.
- **Gourdvine Tap.** Sapling for 900 mL, dead bush for 1,800 mL. The one renewable blood source.
- **Mycelial Lantern.** 4,000 mL tank, 600 mL per enzyme. Replacing it outright touches about 49 files: the
  Working-craft roster, JEI's `EnzymeFruitingRecipeCategory`, journey GameTests, the `first_culture_complete`
  advancement, `MaterialAtlasSpec`. First Culture at D4 to D5 depends on it.
- **Dead-end blocks with no job:** `ravenous_heartwood`, Salt-Stained Log.
- **Fishing.** Nothing live. The old Blood Chum sits in `removed_assets` (`ChummedWaters*`, `BloodChumEntity`,
  `mixin/core/MixinFishingHook`: radius 6, 2,400 ticks, 3 ticks off `timeUntilLured`), cut on 2026-11-03.
- **Vital Effusion** is an R cast with no world growth.

## Target design

### One rule for fed ground

A pure `BloodFedGrowthRules` answers one question for a crop position: how well is this fed, from 0 to 3?

| Level | Source |
|---|---|
| 0 | nothing |
| 1 | a drip from above, or a wet stain on the soil's side |
| 2 | soil in or beside a planting basin that holds fresh blood, or beside a bone-meal-fed gouge |
| 3 | inside a cloche with blood in its buffer |

One subscriber, `common/event/BloodFedGrowthEvents`, reads it on `CropGrowEvent.Pre` and gives extra growth chances
by level. Level 1 covers a wide area cheaply; level 3 is one block at a real cost. That is the whole tier system,
and nothing else needs to know about crops.

Fed ground is tracked without a new farmland block: a drip or basin stamps a game time on a chunk attachment keyed by
position (the `PHLEGETHONTIC_*` pattern), read lazily. Unfed ground simply ages out.

### Tier 1: the drip

- The scab stalagmite gets its own random tick. When a block above it holds fresh blood (a basin, a gutter end, a
  living vein node), it drips: a custom drip particle and a feed stamp on the first farmland or soil within 10 blocks
  below.
- Each drip takes a little from the source. A slow, wide, low-yield ceiling farm.
- A cauldron under a drip with nothing to feed collects it and clots it: a slow passive Formation maker, which is the
  "item collection" half of the user's idea. An empty cauldron under a drip becomes a Sanguine Condenser the same way
  projection converts one today.
- Stalagmites also grow, slowly, where they are fed. That makes them farmable instead of only found.

### Tier 2: planting basins

- A shallow basin block that holds soil and one plant, fed by a gutter or by hand. Smaller area, direct feed, level 2.
- It is a basin (Track 6), so it clots: the Formations it leaves are the by-product of a tended bed.
- Fungal strains and gourds grow here at normal speed and at tier 1 only slowly.

### Tier 3: the cloche (`CL-15`)

- The Mycelial Lantern re-housed: same block id, same recipe type, same JEI category, same First Culture advancement,
  new body (a glass bell over a single planted block) and the Track 3 feed (working buffer, owner draw).
- One block, level 3, the highest cost per crop. It also keeps what the Lantern does today: enzymes from spores.
- Keeping the id avoids the 49-file replacement and keeps the D4 to D5 gate intact.

### Crops

- **Gourds need blood.** The stem only advances on fed ground (level 1 or more). This puts the gourd, which every
  Harbinger wears, at the start of the farming ladder.
- **Fungal strains** are placed crops with a substrate: `ravenous_heartwood` for the flesh-feeding strains,
  Salt-Stained Log for the Brined ones, podzol or mycelium for the rest. That gives both dead-end blocks a job.
- **Catalyst crops.** One plant per school whose harvest is a weak catalyst (Track 4): a renewable, slow, low-grade
  alternative to keeping a creature. Lux and Flammeus first, since they are the thin schools.
- **Vanilla crops** on fed ground grow faster and have a small chance of a tainted harvest (ties to tainted meat in
  Track 5). No new vanilla crop items.
- **Vital Effusion's projection Shaping** is growth on demand: bone meal's effect on the struck block for blood and
  Animus Temper, through `BonemealEvent` so other mods' crops behave.

### Chum

- **How.** Projection with nothing selected (or a dedicated Chum Shaping if plain projection into water needs to stay
  free for other things) at a water surface. The Fluid target from Track 1 picks it up.
- **The pool.** A `ChumPool` in level saved data: centre, radius, strength, expiry. Radius grows with mL projected,
  from 3 to about 12. It fades over a few minutes and expires lazily. Blood in flowing water or rain disperses
  faster. No blocks are placed.
- **Effect.** Restore `ChummedWaters*` and `MixinFishingHook` from `removed_assets`: shorter lure time inside a pool.
  A global loot modifier adds Hemomancy fish to catches inside a pool, anywhere, at a low rate that rises with
  strength. Near their own biomes (the reef) the rate is much higher.
- **Spawning.** A strong pool in deep enough water has a chance to draw a Hemomancy sea creature to it. Start with
  ambient fish; hostile draws belong to Track 10.
- **Temper.** A catalyst dissolved with the blood colours the pool: a different catch table per school. This is
  content, added a table at a time.
- **Visual.** A slow red bloom in the water using the mod's own pool and mote particles. No vanilla particles.

## What changes, file by file

| File | Change |
| --- | --- |
| New `common/world/farm/BloodFedGrowthRules` (pure), fed-ground chunk attachment | The rule and its record. |
| New `common/event/BloodFedGrowthEvents` | The one crop subscriber. |
| `BloodScabPointedDripstoneBlock`, `BlockInit` | Random tick, drip, growth. |
| New `PlantingBasinBlock` + block entity | Tier 2. |
| Mycelial Lantern block, block entity, renderer, model generator | Re-housed as the cloche. |
| Gourd stem block | Needs fed ground. |
| `animus/VitalEffusionManip` | Projection Shaping. |
| New `common/world/chum/ChumPools` (saved data), `ChumRules` (pure); restored `MixinFishingHook` | Chum. |
| New loot modifier + `data/hemomancy/loot_modifiers/` | Fish in pools. |
| `BloodProjectionItem` chain | The Fluid branch. |

## Tests

- New: `BloodFedGrowthRulesTest`, `ChumRulesTest`, a `farming_validation` GameTest batch (each tier feeds at its
  level; a gourd will not grow dry; drip into a cauldron clots; cloche still makes enzymes), a `chum_validation`
  batch (radius by mL, expiry, catch table).
- Must stay green: the First Culture journey tests, `WorkingCraftRosterDataTest`, the JEI enzyme category tests,
  `WoodSetResourceTest`, `bloodwood_validation`.

## Hazards

- **Existing worlds' gourd farms stop.** The mod is unreleased, so this is fine, but the Liber and the teacher who
  hands out the first gourd seeds must say it.
- **Mixin on the fishing hook** was removed once. Find out why before restoring it; if it was for compatibility, use
  `ItemFishedEvent` and a loot modifier only, and accept a smaller lure effect.
- **Auto-farms.** A drip over farmland with a hopper underneath is automation. It is slow and it needs a living or
  freshly fed source above, which is the intended limit. Check it against Track 6's "no base runs itself" rule.
- **Other mods' crops.** Use the events, never block-class checks.

## Open questions

`CL-15`. Also: does plain projection into water chum, or does chum need its own Shaping? My call for now: plain
projection, because it is the most discoverable version of "blood in the water".

## Acceptance

- A ceiling of fed stalagmites over a field grows it measurably faster than rain.
- A gourd grows only where blood reaches.
- The cloche does everything the Lantern did and the First Culture gate still passes.
- Projecting into the sea off a plains coast lets a player catch a reef fish, slowly.
