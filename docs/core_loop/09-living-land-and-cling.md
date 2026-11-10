# Track 9: Living Land and wall cling

**Goal:** two terrain Shapings. Living Land wakes the ground and throws it at enemies. Cling roots the Harbinger to a
wall or ceiling. Both are projection into blocks, both are self-contained, and neither depends on the economy tracks.

**Depends on:** Track 1. `PlayerBlockEdits.lift` / `settle` from Track 0.

## Living Land

### The source

MnAGnosis (`C:\Users\Vince\Desktop\My MC Mods\CURRENT\MnAGnosis`, Forge 1.20.1):

- `ComponentLivingLand` spawns a `LivingLandControllerEntity`: at most two per owner, up to 600 ticks, rescanning
  every 15.
- `LivingLandTerrain.scan` and `selectMode` choose from what is around the target: `CEILING_CRUSH`, `WALL_LANCES`,
  `FLOOR_TEETH`.
- It lifts one or two pillars of three to five real blocks, at most four strikes, through
  `ConservedBlockService.remove`.
- `LivingLandStrikeEntity` carries the blocks along six control points, deals `4 + 2 × magnitude`, and puts them back
  with `payload.settleAt`. `emergencySettle` drops the blocks as items when it cannot, which is a free Silk Touch.

### The port

- **Take about ten to twelve classes:** the controller, the strike, the terrain scan, the mode selector, the payload.
  Leave the conservation package (about 33 files) behind.
- **Replace** its block handling with `PlayerBlockEdits.lift(player, pos)` and `settle(player, pos, state)`, which
  Track 0 adds: game mode, spawn protection, the NeoForge break and place events, and a refusal for block entities
  and anything in a `rite_bound` tag or an active rite space.
- **Replace** its targeting with `ManipulationCombatHelper.canHarm`, its damage with `SchoolDamage`, its visuals
  with `BloodFlowVisuals` and the mod's vein and tendril effects. The lifted blocks should be visibly held by blood:
  a thin vascular net over the mass, at the Riposte bar.
- **1.20.1 to 1.21.1:** `defineSynchedData(Builder)`, `CommonHooks.fireBlockBreak`, `EventHooks.onBlockPlace`,
  `NbtUtils.readBlockPos` returning `Optional`.
- **No loss, no gain.** Lifted states are saved on the strike entity. If it unloads, dies or cannot settle, the
  blocks go back to their origin positions; if those are taken, to the nearest free space. They never drop as items.
  A GameTest counts blocks before and after.

### As a Shaping

- Projection Shaping, Animus primary and Ferric secondary (`CL-17`): hold Projection on ground, wall or ceiling near
  an enemy. A short charge wakes the patch; while blood flows it keeps striking; release and everything settles.
- The session is the controller's lifetime. Blood per second, Animus Temper per strike. Dry (Track 4) means fewer,
  smaller pillars.
- One patch at a time at first rank, two at a later one. Nothing to keep from the 600-tick timer: the channel is the
  timer.
- **Where it refuses:** inside mausoleum wards (`MausoleumEvents`), rite spaces, arena bounds, and on anything with a
  block entity. It should say so with the channel layer's refusal mark, not fail silently.
- **Utility use.** The same Shaping aimed with no enemy near raises a temporary step or a bridge of lifted ground that
  settles on release. Cheap to add once lift and settle exist, and it is the kind of non-combat use the overhaul is
  for.

## Cling

### What exists

- `HoldfastMorphlingItem.clings` zeroes downward motion from `MorphlingPowerEvents.onPlayerTick`.
- Spider possession has a client-side climb.
- `WallClimbPose` is mob-only. There is no `onClimbable` mixin.

### Target

- **Root.** Projection (plain, or an Animus Humilis Shaping if plain projection at a wall must stay free for stains)
  into a wall the player is touching roots them to it: veins from hands and feet into the face. While rooted the
  player does not fall, can crawl along the surface at reduced speed, and can cast with the free hand.
- **Ceiling.** The same on a ceiling is a hang: the player holds position under it and can shuffle. The player model
  is not inverted. True upside-down walking means camera and collision work the mod does not need.
- **Cost.** Blood upkeep per second through `BloodAccounting.payUpkeep`. It ends when the blood runs low, the surface
  is broken, or the player jumps off.
- **Cleats.** Hematic iron cleats (boots slot, a higher-tier craft) cut the upkeep sharply and let the player move at
  near walking speed on walls. They do nothing without blood. Hematic iron and bone; no copper.
- **How it is built.** One pure `ClingRules` (which faces hold, speed, cost) used by the Holdfast morphling and the
  new Shaping alike, with the state on the player and a `PlayerStateSync` entry. Movement is applied the way the
  Holdfast path does it, by controlling motion while clinging.
- **What not to do.** Do not fake a climbable block under the player. It would let Scalding Updraft and anything else
  that checks `onClimbable` launch from walls, and it would fight ladders and vines.
- **Pose and visuals.** A third-person pose entry in `PlayerAnimationRegistry.THIRD_PERSON` at its own priority, and
  the rooting veins drawn with the infusion vein material. Thin, anchored at the four contact points.

## What changes, file by file

| File | Change |
| --- | --- |
| New `common/entity/manipulation/` `LivingLandControllerEntity`, `LivingLandStrikeEntity`; `common/manipulation/animus/LivingLand*` | The port. |
| `PlayerBlockEdits` | `lift`, `settle` (Track 0). |
| `EntityInit`, `EntityRendererRegistrar`, `ManipulationInit`, `ManipulationTreeInit` | Registration. |
| New `ClingRules` (pure), cling state attachment, `PlayerStateSync` entry | Cling. |
| `HoldfastMorphlingItem`, `MorphlingPowerEvents` | Read `ClingRules`. |
| New cleats item; `ItemInit`, creative tab section, recipe | Cleats. |
| `PlayerAnimationRegistry` | The pose. |

## Tests

- New: `LivingLandTerrainRulesTest`, `ClingRulesTest`, a `living_land_validation` GameTest batch (each mode; block
  count conserved through unload, death and an occupied origin; refused in a ward), a `cling_validation` batch
  (root, crawl, hang, upkeep ends it, surface broken ends it).
- Guardrails: `PlayerBlockEditGuardrailTest`, `CombatTargetingGuardrailTest`, `HeldTravellerGuardrailTest`,
  `PlayerAnimationGuardrailTest`, `HealAndFlightGuardrailTest` (cling is not flight and must not touch
  `FlightArbiter`).

## Hazards

- **Griefing and claims.** Living Land moves real blocks. Every lift goes through the place and break events, so
  claim mods can refuse it. A config switch turns block lifting off and uses conjured stone instead.
- **Dupes.** The single worst risk in this track. The conservation test is not optional.
- **Server anti-cheat and other mods.** Cling holds a player in the air. Reset fall distance on release, and keep the
  vanilla "flying is not enabled" kick in mind: the server must own the cling state.
- **Licence.** MnAGnosis is the user's own mod, so the port is theirs to make. Confirm before copying if anyone else
  wrote parts of it.

## Open questions

`CL-17`.

## Acceptance

- Living Land lifts, strikes and returns terrain with no block gained or lost in any test.
- A Harbinger can root to a cliff face, cross it, and hang under an overhang, for blood.
- Neither works where wards and rites say no.
