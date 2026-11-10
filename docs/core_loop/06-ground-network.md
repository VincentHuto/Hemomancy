# Track 6: The ground network

**Goal:** blood leaves marks on the world, in three tiers that share one look and one set of rules: stains, gouges and
living veins. They are markers, lights, paths, item runs and short feeds. Absorption takes them back up. This track
also makes gutters and basins carry real blood.

**Depends on:** Track 1 (targets, Shapings), Track 3 (clotting, living vessels).

This is the track with the highest risk of turning blood into wiring. The order inside it is deliberate: stains and
gouges first as markers, catalysts second, anything that moves blood last and smallest.

## What exists

- **Ash trails.** `common/block/harbinger/rite/AshTrailBlock` (Smouldering, Befouling and their Active forms) is a
  cleaned copy of redstone wire: floor only, no signal, and "active" changes nothing but the dropped item. Ignited by
  `GroundBloodDrawPacket` for 25 mL (Track 0 guards it).
- **Gutters and basins.** `BloodGutterBlock` is state only (four sides, `FILL` 0 to 3). Only `HemorathArena` and
  `VenousBasinBlock.flow` set it: a search of up to 64 blocks that loses one level every four and only ever raises.
  `puzzle/BloodBasinBlockEntity` is health-based and belongs to the Saint puzzle. No mL moves anywhere.
- **Earthen Veins.** `EarthenVeinTravelManager.project` is already in the projection chain, and the D2 Rooted lesson
  teaches it.
- **Memory Thread.** An item that draws up to 96 route points as particles.
- **Renderers that already draw glowing things on block faces from per-section caches:**
  `HeartNodeSpreadRenderer`, `MausoleumWardRenderer`, through `WorldEffectDispatcher`, `MaterialQuads` and
  `SeededRenderTypes`. The infusion vein shader (`blood_infusion_veins`) is the nearest look.
- **Real light** comes from a blockstate `lightLevel` or from `effect/LanternTickLightManager`, which places vanilla
  light blocks. No Lux manipulation emits light today.

## Target design

### Tier 1: stains

- **What.** A thin multiface block (the glow lichen and sculk vein pattern): any face, any surface, no block entity,
  a baked model. Three states: wet, tacky, dry.
- **Made by.** Plain Projection onto a surface that nothing else in the chain claims. This is the new last link of
  the projection chain: blood that had nowhere to go lands on the world. A few mL per face. Also left by heavy
  bleeding and by kills, sparsely.
- **Ages.** Random ticks take it wet → tacky → dry → gone. Rain and water wash it. Nothing is stored in it.
- **Does.** Marks a path. Carries the item draw. Feeds a crop directly beneath a wet one (Track 7). Draws attention
  (Track 10). Wet stains are what a gouge or vein is drawn over.
- **Taken back.** Absorption on a wet stain returns part of the blood; on a dry one it just cleans.

### Tier 2: gouges

- **What.** A trench cut into the block face, same multiface block family, permanent until removed. This replaces the
  Memory Thread.
- **Made by.** A Shaping (the converted `HematicBeaconManip`, or a new Ferric one) projected along surfaces. Costs
  blood and Ferric Temper. Cutting respects `PlayerBlockEdits`.
- **Dormant by default.** A gouge does nothing until blood is projected into any part of it. Then a pulse runs along
  the connected run, bounded like the basin's search (64 blocks), and the run stays lit for a time that depends on
  what was dissolved into it.
- **Catalysts give it a function.** One catalyst per run, dissolved by projecting with the item in the off hand:

  | Catalyst | The lit run |
  | --- | --- |
  | none | glows faintly, shows the way it was drawn |
  | glowstone dust, incandescent enzyme, `organic_prism` | gives real light (blockstate light level) |
  | coal, fervent enzyme | is hot: items lying on it smelt slowly; it lights campfires and furnaces it touches |
  | compass, lodestone compass | points: the pulse runs toward the bound place and the far end flares |
  | packed ice, frigid enzyme | is cold: water beside it freezes, crops beside it stop growing |
  | iron, ferric enzyme | pulls dropped items along itself toward the pulse's origin |
  | bone meal, vivacious enzyme | feeds what grows beside it (Track 7) |
  | ink sac, umbral enzyme | is dark: hides the run and dims mobs' sight along it |

  The table is data (`data/hemomancy/gouge_catalyst/`), read through Track 4's catalyst table, so new functions are
  content, not code.
- **Taken back.** Absorption along a gouge fills it in.

### Tier 3: living veins

- **What.** A gouge the Harbinger has grown tissue into. This is the Earthen Vein, kept and extended, so the D2
  lesson still teaches something true.
- **Different because.** It is alive: a living vessel (Track 3), so blood in it stays fresh while it is fed. It has a
  block entity only at its nodes; the runs between are block states.
- **Does (`CL-14`).** Carries blood between two nodes: a basin and a machine, a Tap and a planting basin, two rooms.
  Small flow, short runs, one owner. It also carries everything a gouge can.
- **Limits.** Total vein length per owner by degree. Flow per vein fixed and low. It needs upkeep from its owner
  through the Suture rules; when the owner is out of range or offline it goes dormant, and unfed for long enough it
  withers back to a gouge. It moves blood; it never holds more than a few seconds of flow.
- **Travel.** `EarthenVeinTravelManager` already lets a rooted player move along one. Keep it.

### Gutters and basins

The Saint complexes built the blocks; this makes them work for the player.

- **Basin.** An inert container with a block entity and a real volume. It clots like any other (Track 3): blood
  projected into a basin runs out through its gutters for the fresh window, and what is left becomes Formations in
  the bowl. "Pour it and walk away for a few minutes" works. "Store it" does not.
- **Gutter.** Stays state only. `FILL` 0 to 3 is display, driven by the basin's level and distance, and by the
  existing `VenousBasinBlock.flow` search, made to lower as well as raise. A gutter delivers to what it ends at: a
  planting basin, a machine's working buffer, a drip point, farmland.
- **The arena keeps working.** `HemorathArena`, `SaintArena.set/schedule/revert` and
  `HemorathEntity.standsInGutter` read `FILL > 0`; a flag marks arena gutters so player basins cannot feed a Saint
  fight and the fight cannot be drained by a hopper.
- **Cost.** The 64-block flood fill runs per random tick today. Run it when the basin's level changes instead.

### The catalyst feeder

A small hopper-like block set against a gouge or vein node. It holds one stack and dissolves one item each time the
run would go dark, so a lit hallway or a hot run stays up while stocked. It feeds catalysts only, never blood.

### Lux and Tenebris

- **Lux, projection.** A Shaping that spreads a temporary gouge of light across the struck surface in a branching,
  semi-random pattern, fading over minutes. It is a light source that costs blood and Lux Temper, and it is how a
  Harbinger lights a cave without torches. Thin lines; follow the "no spaghetti" rule.
- **Tenebris, absorption.** A Shaping that draws the light out of where it is aimed. True light removal from other
  blocks is not practical. What it does instead: snuffs sources it can switch (candles, campfires, lanterns to their
  unlit states, Lux gouges, lit runs) through `PlayerBlockEdits`, and hangs a field of dark for a time (client fog
  in the zone, mob follow range cut inside it). Needs a spike before it is promised.

### The item draw

`SanguineMagnetismManip`'s absorption Shaping. Dropped items within reach slide toward the player, faster along wet
stains and iron-fed gouges, and are picked up on arrival. Works on a full body (Track 1 put the hook ahead of that
check).

## What retires

- **Memory Thread** (item). Its offering job in the Mnemonist Puppet ordeal needs a replacement (`D-21`).
- **`GroundBloodDrawPacket`** and the Left Alt path. Ash trails are lit by Projection through the chain.
- **`ViridSalisTrailBlock`** if unused (Track 0).
- Ash trails themselves stay: they are rite furniture. Their wire code is not reused; the multiface family is new.

## What changes, file by file

| File | Change |
| --- | --- |
| New `common/block/harbinger/ground/`: `BloodStainBlock`, `BloodGougeBlock`, `LivingVeinBlock`, `VeinNodeBlock` + BE, `CatalystFeederBlock` + BE | The three tiers. |
| New `common/world/ground/`: `GroundRunRules` (pure: connection, pulse, bounds), `GougeCatalysts` (data), `VeinLedger` (saved data: length per owner) | Rules and limits. |
| `BloodProjectionItem` chain | Stain as the last link. |
| `BloodAbsorptionItem` chain | Slurp and fill-in. |
| `VenousBasinBlock`, `BloodGutterBlock`, new `VenousBasinBlockEntity` | Real volume, flow on change, arena flag. |
| `EarthenVeinTravelManager` | Reads the new vein blocks. |
| New `client/render/world/GroundRunRenderer` | Lit runs, from `WorldEffectDispatcher`. |
| `BlockInit`, itemless lists, frozen files, creative tab sections | Registration. |

## Tests

- New: `GroundRunRulesTest`, `GougeCatalystDataTest`, a `ground_network_validation` GameTest batch (stain ages and
  washes; pulse bounds; each catalyst's function; vein withers without its owner; basin clots; arena gutters
  isolated).
- Retune: anything pinning ash-trail ignition, the Rooted lesson, Hemorath's arena tests.
- Guardrails: `PlayerBlockEditGuardrailTest` (cutting), `VanillaParticleRatchetTest` (no vanilla particles on runs),
  `RenderTypeCachingGuardrailTest`.

## Hazards

- **RF by another name.** A vein that moves blood from a Tap to a machine with no player involved is a pipe. The
  owner-range rule, the length cap and the absence of storage are what keep it from being one. If playtesting shows
  bases running themselves, cut blood transport and leave veins as signal and catalyst carriers (`CL-14`).
- **Block spam.** Stains are blocks. Bleeding and kills must leave few, and they must age out.
- **Light updates.** A 64-block run switching light level is 64 light updates. Stagger the pulse.
- **Griefing.** Gouges are permanent edits. Spawn protection and claims go through `PlayerBlockEdits`.
- **Looks.** Rejected twice before: thick strands and noise-contour lines. Runs are narrow, partly clear, and sit in
  the block's own pixel grid at 16 px.

## Open questions

`CL-14`. Also collides with `PT-9`, `PT-42`, `D-21`; see Track 11.

## Acceptance

- A player can mark a cave route in gouges, light it with glowstone, and find their way back along it.
- Blood poured into a basin runs down its gutters, feeds what they reach for a few minutes, and leaves Formations.
- Absorption removes any tier.
- No arrangement of basins, gutters and veins keeps a machine running with its owner away.
