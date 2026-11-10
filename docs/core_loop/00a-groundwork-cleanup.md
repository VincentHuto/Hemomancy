# Track 0: Groundwork and cleanup

**Goal:** fix the bugs and close the gaps the overhaul would otherwise build on top of. Nothing here changes design.
Every item stands on its own and is worth doing even if the rest of the suite is rejected.

**Depends on:** nothing.

## Why first

The later tracks add many player-caused world edits, per-player sessions and blood movements. Today several of the
paths they would extend skip the shared checks, and a few have plain bugs. Fixing them first means the new work copies
a correct pattern.

## Tasks

### World edits that skip `PlayerBlockEdits`

| Site | Problem | Fix |
| --- | --- | --- |
| `common/event/BloodInfusionManager` | Sets AIR and drops the result with no `mayAlter` or spawn-protection check. | Route through `PlayerBlockEdits`; add an in-place replace helper (see below). |
| `CardinalRiteCompletionEffects.completeHungeringEarth` | Raw `setBlock` over radius 16. | Same helper, with the rite's owner as the editor. |
| `GroundBloodDrawPacket` (ash ignition) | Raw `setBlockAndUpdate`, gameplay inline in the packet, sent every client tick while Left Alt is held, no rate limiter. | Move the body to a service, pace it with `ServerboundRateLimiter`, edit through `PlayerBlockEdits`. Track 6 later retires the payload. |
| `VeinMinerHelper.tryVeinMine` (Talaromyces Minus scar) | 250 blocks, no harvest-tier check, no `mayAlter`. | Add both. Track 8 folds it into the shared crawl. |

`PlayerBlockEdits` only has `mayBreak(player, pos)` and `place(...)`. Add, with tests:

- `breakBlock(player, pos, tool)`: break event, drops with the given tool context.
- `replace(player, pos, newState)`: in-place conversion that fires break then place checks.
- `lift(player, pos)` / `settle(player, pos, state)`: remove a block without drops and put it back later (Track 9).

`PlayerBlockEditGuardrailTest` scans only `common/manipulation` and `common/network`. Widen it to `common/event`,
`common/item`, `common/block` and `common/tile`, with the listed non-player editors as today.

### Bugs

- **Specimen Jar deletes a riding mob.** `SpecimenJarData.captureEntity` calls `entity.save()`, which returns false
  for a passenger, then `discard()`. Dismount first or refuse.
- **Specimen Jar has no capture rules.** No size, health, boss, owner or passenger check beyond the tag. Add a
  `SpecimenCaptureRules` (pure, tested). Track 5 extends it.
- **Gourd and shears.** `GourdBlock.useItemOn` with shears makes a vanilla carved pumpkin.
- **Dead tool code.** `isCorrectToolForDrops(BlockState)` overrides on the living tools never run in 1.21. Replace with
  the `Tool` data component or tags; delete the overrides.
- **`BloodTemperament.setTemper(Map.of(...))`** installs an immutable map; the next write throws. Copy into a mutable
  map.
- **`getPercentByTemper`** can divide by zero.
- **Will Temperament shifts are never synced** (+3 on absorption, -3 on banish).
- **Kill shift and absorption can double count** the same creature.
- **Opposing schools are defined twice** (`BloodTemperament.getOpposingTemper`, `TemperWeaponHelper`). Keep one.
- **Strongest-temper ties resolve by enum order.** Decide a rule (last gained wins) or document it.
- **`TemporaryIceManager`** keeps Glacial ice in memory; a restart leaves it in the world. Persist or sweep on load.
- **`HematicCommandManager`** keeps impressed mobs in static UUID maps that are lost on unload. Track 5 needs this on
  an entity attachment; do the move here.
- **Thrall reabsorb** fills the player with `playerVol.fill` and no accounting. Route through the shared path so the
  ledger sees it.
- **Scab stalagmite** is registered without `.randomTicks()` and inherits drip logic that tests for
  `Blocks.POINTED_DRIPSTONE`, so it never drips. Track 7 gives it its own tick; here, just note it as inert.

### The verb items

Found by the Track 12 audits. Each is a fault in plain Projection or Absorption today, and the second is an inference
that needs a test in game before it is fixed.

- **Absorption strain never builds.** A tick without a pulse removes 2 and a pulse adds 1, so the counter sits at 0
  or 1 and `blood_tolerance` does nothing. Decide what strain is for, then make the arithmetic match.
- **The cadence skills probably slow the drain.** A hit starts 20 ticks of hurt-invulnerability, and a repeat hit of
  the same size is refused while more than 10 remain. Pulse intervals of 8, 6 and 4 ticks should then land every 16,
  12 and 12 ticks, against 10 with no skill. Measure it; if true, pace the damage at 10 ticks and let the skills
  raise the amount instead.
- **Acclimation by draining mobs is out of reach.** The bare drain is about 2 mL per second and the thresholds start
  at 750 mL, while block absorption moves 100 mL per tick. Note it for Track 4, which changes what a creature gives.
- **Will banishment charges the full tile rate every tick** it succeeds (100 mL, or 150 and up on the Staff). Check
  that this is meant.
- **The Condenser takes the blood before the payment is checked.** If `pay` fails, the blood is not returned. Pay
  first.
- **Absorption's damage has no owner** (`generic()`): no aggro, no kill credit. This one is a design change, so it is
  `CL-24` in Track 12, not a fix here.

### Data and resources

- Blood profiles `husk`, `bogged` and `armadillo` were reported as orphans. Check whether the loader reads them and
  either wire or remove them.
- Coal has no enzyme mapping in `recipe/rendering` though it is the user's own example of a Flammeus catalyst. Add
  coal and charcoal to fervent when Track 4 lands, not before.
- `ViridSalisTrailBlock` is an older duplicate of the ash trail's wire code. Move to `removed_assets/` if nothing
  places it.
- `LivingPistolItem` is unregistered. Move to `removed_assets/`.
- Registered but unrouted: `SanguineFertilityEffect`, `potion_of_sanguine_fertility`. Track 5 gives them a job; leave
  them.

### Stale docs

Fix in `docs/HEMOMANCY_REFERENCE.md` as each section is touched by a later track, not in one sweep, so the reference
never describes a state that never existed:

- §19 names source classes that do not exist.
- §30 Hemopothecary, §21.5, §28.1.1, §38 (Saints, Tap, Drudge claims), §5.4 Hermit table, §25.2 against §10, §31.2,
  §32.2.
- "123 manipulations" in §8.2 and in `MANIPULATIONS_AND_CASTING.md`; the registry holds 108.

### Dead ends to note, not fix

These get jobs in later tracks; listing them here keeps them from being deleted in the meantime.

| Thing | Job it gets | Track |
| --- | --- | --- |
| `ravenous_heartwood`, Salt-Stained Log | Planting-basin and cloche substrates | 7 |
| Cooked foods | Blood regeneration | 10 |
| Vascular Status Gauge | Reads a container's freshness | 3 |
| Barometer, Ossuary Clock | Moon and Blood Moon forecast | 10 |
| Vitality Chalice | A hand-held basin (carries a small fresh measure) | 3 |
| Sanguine Vigil (under-exposed) | Guard post for a menagerie | 5 |
| `organic_prism`, `suspended_vivianite` | Catalysts for Lux gouges | 6 |
| `LivingStaffProgress.bloodHandled` (accumulated, never read) | Staff growth | 8 |

## Tests and guardrails

- New: `PlayerBlockEditsTest` cases for the three helpers; `SpecimenCaptureRulesTest`.
- Changed: `PlayerBlockEditGuardrailTest` scan roots and allow-list; `BloodAccountingGuardrailTest` counts if the
  Thrall path moves; `BloodTemperamentTransientAlignmentTest` for the map fix; `LignumMortisWiringTest` untouched.
- Regenerate `docs/architecture/evidence/source-text-impact-map.csv` after source-text tests move.

## Acceptance

- No player-caused block edit outside `PlayerBlockEdits` in the widened scan.
- The listed bugs each have a failing-then-passing test.
- `./gradlew.bat alphaCheck` is no worse than its baseline (seven known playthrough failures on 2026-11-05).
