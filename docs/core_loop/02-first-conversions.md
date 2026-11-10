# Track 2: First conversions

**Goal:** prove the framework with four Shapings that each use a different target kind, then list the second wave.
After this track the player can feel the idea, and it is the right point to stop and play.

**Depends on:** Track 1.

## The four

### Lignum Mortis: a projection beam (target: block)

- **Today.** `mortem/LignumMortisManip` (with canopy and worked variants) is CONTINUOUS on R. `LignumMortisRules`
  finds an origin at 12 × reach, selects by tag (`organic_structure_blocks`, `worked_lignum_blocks`), crawls 26
  neighbours at 2 to 8 blocks a second, caps at 128 blocks and a radius of
  `6 + 2 × mastery + (mortem + animus/2)/20` (max 24), and breaks only on release through `mayAlter`,
  `VeinMinerHelper.hasBreakPermission` and `dropResources`.
- **Becomes.** The projection Shaping of the same manipulation. Hold Projection on a log: the rot crawls from the
  struck block while blood flows; release fells what was reached. The session, budget and dismantle code move
  unchanged behind `begin` / `pulse` / `end`.
- **Rewire.** The crawl is the first user of a shared `CrawlSession` (selector, budget, dismantle) that Track 8 reuses
  for ore. Extract it here rather than copying later. The static session map registers with
  `TransientServerStateEvents`. Breaks go through `PlayerBlockEdits.breakBlock`.
- **Keep.** R still triggers it for players who have not switched (remove after one review pass).
  `LignumMortisWiringTest` is rewritten to the Shaping API.

### Pyretic Forge: projection smelting (targets: items on the ground, block, brazier)

- **Today.** `flammeus/PyreticForgeManip` is a QUICK cast that smelts the main-hand stack, 8 or more at a time, with
  no world targeting.
- **Becomes.** Hold Projection with it selected:
  - at **items on the ground**: smelts the stack in place, a few items per pulse;
  - at a **block** with a smelting result (sand, cobblestone, ore, raw-ore blocks, wet sponge): converts it in place;
  - at a lit **Iron Brazier** holding an offering: smelts or transforms the offering (see below).
- **Rewire.** Block conversion goes through the infusion recipe type, which already converts block to block
  (`{"type":"hemomancy:blood_infusion","input","blood_cost","result"}`, six recipes). Add two optional keys:
  `"shaping": "hemomancy:pyretic_forge"` and `"in_place": true`. Plain infusion keeps working with neither key.
  Vanilla smelting recipes are read for the item case, so no recipe is authored twice.
- **Temper.** This is the user's own example for Track 4: Flammeus Temper, or coal in the off hand, or it runs dry.
  Until Track 4 lands it costs blood only.

### The brazier as the general transformation block

- **Today.** `BrazierRites.absorb` tries three `BrazierRite<P>` implementations in order (Memory, Scar,
  LivingWeaponGraft), each `accepts` → `preflight` → `complete` over 60 ticks. Ordinary insertion, Projection to
  light, held Absorption to complete, sneak to retrieve.
- **Becomes.** A fourth, recipe-driven rite appended after Scar: `BrazierTransmutationRite`, reading a new recipe type
  `hemomancy:brazier_transmutation` (`input`, optional `shaping`, optional `catalyst`, `blood_cost`, `result`). The
  selected Shaping is part of the match, so the same offering can become different things under Pyretic Forge,
  Glacial Grasp or Vital Effusion.
- **Must hold.** The scar-teaching contract in `AGENTS.md`: insertion, Projection ignition, held Absorption through
  completion, sneak retrieves, and learning, Effigy preparation and loadout commit stay separate. The new rite only
  accepts offerings no earlier rite accepts.
- **Content.** Start with a dozen recipes: charcoal and glass by Pyretic Forge; packed ice and blue ice by a Congeatio
  Shaping; rooted dirt and moss by Vital Effusion; hematic iron scrap to ingot; bone to bone meal with a ruinous
  by-product. Each gets a JEI row.

### Tendon Line: project to set, absorb to haul (targets: block, air)

- **Today.** An item (`common/item/shared/TendonLineItem`, `TendonLineRules`, `TendonLineRenderer`). Anchor within 18
  blocks stored in `CUSTOM_DATA`, pull 0.34 toward the face plus 0.35 of existing motion, detach beyond 24, arrive
  inside 1.35.
- **Becomes.** A manipulation with both Shapings. Projection throws the line and sets the anchor. Absorption reels:
  the player is hauled toward the anchor while it is held. Projection again moves the anchor. This is the cleanest
  showing of "two verbs, one manipulation".
- **Rewire.** The anchor moves from the item to the player: a transient attachment with a `PlayerStateSync` entry so
  the renderer has it (no manipulation keeps a world anchor between two uses today). `TendonLineRules` stays as the
  pure maths. The haul is a Shaping session. Anchor range uses `VerbReach` × 3, so reach investment pays.
- **Retire.** The item goes to `removed_assets/`. It is an offering of the Dendritic Distributor and Sanguine Vigil
  crafts (`D-21`); pick replacement offerings first (a length of sinew cord is the obvious one).
- **Tests.** `TendonLineBehaviorSourceTest`, `TendonLineRulesTest`, `TendonLineRendererSourceTest`,
  `PhaseFourTendonLineResourceTest`; frozen registry and creative-tab files.
- **Hazard.** Flight and fall: a haul is not flight, so `FlightArbiter` is not involved, but fall damage on arrival
  needs the same reset the item gives.

### Blood Binding: project into a creature (target: creature)

- **Today.** `animus/BloodBindingManip` and its lingering, chain and lattice variants use `aimedTarget(8, 0.975)` on
  R; `BloodBindingEffect` only zeroes velocity. Constrictor Cord is a separate thing with the same intent.
- **Becomes.** The projection Shaping: hold Projection on a creature to bind it; the bind lasts while blood flows and
  for a short tail after. `ManipulationCombatHelper.canHarm(player, target)` decides who can be bound, as for any
  cast.
- **Why here.** It is the entity-target proof and the first half of Track 5, which turns a held bind on a weakened
  creature into leading it home.
- **Fold.** Constrictor Cord becomes a variant of the same Shaping rather than a second system.

## Second wave

Not built in this track. Listed so the first four are designed with them in mind. Each names its verb and target.

| Manipulation | Verb, target | What it does as a Shaping |
| --- | --- | --- |
| `ferric/SanguineMendingManip` | project, block or creature or item | Mends a damaged block entity, a golem, or a dropped tool. |
| `flammeus/CrimsonFlameConjurationManip` | project, block | Lights campfires, candles, furnaces, braziers, TNT. |
| `flammeus/FurnaceVeinsManip` | project, block | Blood as furnace fuel for as long as it is held. No stored burn time beyond one item. |
| `animus/VitalEffusionManip` | project, block | Grows what it touches (Track 7 owns the rules). |
| `congeatio/GlacialGraspManip` | project, fluid; absorb, block | Freezes water under the stream; Absorption thaws its own ice. |
| `congeatio/GlacialRampartManip`, `ferric/FerricBarrierManip` | project, block or air | Draws the wall along the path the player sweeps. |
| `ferric/VascularDowsingManip` | project, block | Veins run from the struck face toward ore within reach (not aimed today). |
| `ferric/SanguineMagnetismManip` | absorb, items | The item draw: pulls dropped items along stains to the hand (Track 6). |
| `mortem/CarrionCommunionManip` | absorb, block or items | Draws from bone blocks, skulls and rotten flesh. |
| `HematicBeaconManip` | project, block | Marks the struck block; the mark is a gouge tier (Track 6). |
| A Lux manipulation | project, block | Glowing veins spread across the surface (Track 6). |
| A Tenebris manipulation | absorb, air | Draws the light out of an area (Track 6). |
| Chum | project, fluid | Track 7. |
| Living Land | project, block | Track 9. |

Weak fits that should stay R casts: `flammeus/ScaldingUpdraftManip`, `RimeboundSentenceManip`, and anything whose
point is timing in a fight.

## Cleanup this track enables

- Memory Thread (item, 96 route points, particles only) is not converted here. Its replacement is the gouge in
  Track 6; it is an offering of the Mnemonist Puppet ordeal (`D-21`).
- About 70 manipulations are never named in the Liber. Each conversion writes its page as it lands, with a "Shaping"
  line in a fixed place so the player learns to look for it.

## Tests

- One GameTest per Shaping per target kind, each with the "a rite anchor still wins" case.
- A balance pin per Shaping in the style of `ManipulationBalancePass2CastGameTests`: blood per second, work per
  second.
- `entity-cast-fingerprints.txt` is unaffected (Shapings are player-only); say so in the guardrail so nobody routes
  an entity through the dispatcher.

## Acceptance

- Each of the four works by holding a verb at a target, pays per pulse, and stops when the button is released.
- A review client stop for each (`-PcoreLoopShapings`), checked by eye against `MANIPULATION_VISUALS.md`.
- The four old paths are either removed or marked for removal after review.
