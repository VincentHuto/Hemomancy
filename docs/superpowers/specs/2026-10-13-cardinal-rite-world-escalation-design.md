# Cardinal Rite world escalation: design (2026-10-13)

## Goal

A Harbinger Cardinal Rite should change the world around it as it advances. Each milestone (a ring of anchors filled, the seal, the procession, culmination) leaves a lasting change for the rest of the rite, so the site visibly fills with shed blood and escalates. The world, not a panel at the top of the screen, tells the player where the rite stands.

## Agreed decisions

1. **Server-held stage.** The rite keeps an escalation stage that only rises, is saved with the rite and is synced to clients. Client layers read an eased world level; none of them look at rings or phases.
2. **Visual and sound only.** Nothing in the world is edited and no gameplay changes. Gameplay pressure stays with the existing instability rules.
3. **Top panel off by default.** A client config option restores today's panel. Instructions move to one-line action-bar messages and in-world markers.
4. **Rings opening one per phase comes later.** It will feed the same milestones, so the world layers will not change.
5. **Harbinger interactive rites only.** Legacy and Unstained rites get no world layers.

## 1. Escalation stage (server)

### Milestones

| Milestone | Counted when |
|---|---|
| Ring | An anchor ring is complete for the first time (high-water mark of `completedRings()`). |
| Circuit | A station-upgrade circuit is completed (high-water mark of the upgrade `Stage`). |
| Opening | The altar is sealed, or a puppeteer trial manifests. |
| Procession | The rite reaches `OFFERING_PROCESSION`. Only rites that captured offerings have this milestone. |
| Culmination | The rite reaches `CULMINATION`. |

Total steps = rings + circuits + 1 + (1 if the rite has offerings) + 1.

| Rite | Steps |
|---|---|
| One ring, offerings | 4 |
| Two rings, offerings | 5 |
| Three rings, offerings | 6 |
| Apotheos (eight rings) | 11 |
| Puppeteer trial (no anchors, no offerings) | 2 |
| Station upgrade, one ring, N circuits | N + 3, or N + 4 with offerings |

**Succession** starts directly in `CULMINATION`, so it steps with its five succession cues instead (`ticks / 200`, capped at 5). Its total is 5.

### Rules

- The stage is the sum of per-milestone high-water marks: rings counted, circuits counted, and a bitmask for opening, procession and culmination. It never falls. A drained anchor lowers `completedRings()` but not the rings already counted.
- Several milestones reached in the same tick raise the stage once and send one beat.
- `CardinalRiteEscalation.update(rite)` runs once per tick in the `HarbingerCardinalRiteEvents` rite loop, after the ordeal tick and phase-entry handling. A seal that moves straight to the finale from the interaction handler is picked up on the next tick.
- **Saved data:** `ActiveCardinalRite` saves `EscalationRings`, `EscalationCircuits` and `EscalationFlags`. A rite from an older save rebuilds them on load from its current state: full rings, the upgrade stage, `altarSealed` or a manifested trial, and the phase it has reached.
- **Sync:** `PacketSyncActiveRites` and `ActiveRiteClientData.RiteEntry` carry `escalation` and `escalationTotal`.
- **Beat:** a new `CardinalRiteEventKind.ESCALATION`, appended last, with `index` = the new stage. It is sent through `CardinalRiteBeats.send`.

## 2. World layers (client)

### Shared state: `RiteWorldState`

For each rite, by Focus position, it tracks:
- **Level:** `escalation / escalationTotal`, eased over about 2 seconds, with a short overshoot when a step lands.
- **Step transitions:** the age of the latest `ESCALATION` beat.
- **Ending:** the ending kind and age, from `CardinalRiteEventClientState.ghosts()`.
- **Viewer weight:** 1 within the footprint radius + 4 blocks, falling linearly to 0 at the footprint radius + 32. Layers that affect the camera (air, sound) scale by it.
- **Strain:** the instability band, from the synced instability. Strained is 40 or more, rupturing 70 or more.
- **Palette:** from the consecrated floor at the Focus (`CardinalFocusBlockEntity` `FormedFloor`), the same way the floor renderer reads it. It falls back to the default Harbinger palette.

Layers come in at fixed fractions of the level:

| From | Layers |
|---|---|
| Step 1 | Air, trench, braziers, sound drone |
| Level ≥ 1/3 | Terrain creep, heartbeat in the sound bed |
| Level ≥ 1/2 | Sky wound forms |
| Level ≥ 3/4 | Blood rain, wind and rain in the sound bed |
| `CULMINATION` | The sky wound opens; everything beats together |

The 2-step puppeteer trial therefore gets air through sky wound at manifestation, and rain plus the opening at culmination.

### Layers

All numbers below are starting values for in-game tuning.

**Air (`RiteAirLayer`).** It handles `ViewportEvent.ComputeFogColor` and `ViewportEvent.RenderFog`.
- **Colour:** the fog colour mixes toward the palette's vein colour, darkened. The mix reaches 0.25 for faint atmosphere, 0.45 for dense and 0.6 for storm, multiplied by level and viewer weight.
- **Distance:** the far plane is multiplied down to 0.85 (faint), 0.75 (dense) or 0.65 (storm) at full level. Strain multiplies it again by 0.9 when strained and 0.8 when rupturing.
- **Limit:** it never pulls the far plane below 24 blocks, and it is skipped when the far plane is already closer.

**Trench (`FloorRiteResponse` and the membrane shader).** A new uniform, `RiteBlood` (vec4):
- `x`: trench level, 0 to 1. The pooled blood in the brazier trench widens and rises toward the lip one notch per step.
- `y`: overflow, 0 to 1. From level 3/4, blood runs over the lip as runnels into the ground beyond.
- `z`, `w`: the creep radius and strength, read by the creep below.

Under strain the pooled blood darkens and clots.

**Terrain creep (`RiteTerrainCreep` with a separate `CreepMesh`).**
- **Mesh:** a cached mesh of the top faces of solid blocks with open air above. It runs from the floor's edge out to the maximum reach and follows the terrain height, the way the floor mesh's ground cells do. It is rebuilt when the stage changes, and its blocks are re-checked every few seconds like `FloorMesh`.
- **Drawing:** the floor membrane shader's ground mode, in a persistent variant. Vessels cover everything inside the creep radius, fade near its rim, and a step's surge band runs out across it.
- **Reach:** `clamp(0.6 × footprint radius, 4, 12)` blocks past the floor edge. The radius grows from 0 at level 1/3 to the full reach at level 1.
- **Strain:** strained bruises the creep darker and toward purple; rupturing pulls it back to 70% of its radius.

**Sky wound (`RiteSkyWound`, new core shader `cardinal_rite_sky_wound`).**
- **Shape:** a slowly turning disc of cloud above the Focus. It sits at the Focus height + 30 + 0.6 × footprint radius, kept 8 blocks under the build limit. Its radius grows from 0.8× the footprint radius when it forms to 1.6× at full level.
- **Growth:** later steps darken it, add veins, and light a glowing core in the palette colours.
- **Lightning:** storm rites throw lightning from it, using the existing rite lightning palette (`CardinalRiteFogLightning`), more often as the level rises.
- **At culmination:** the core opens into a red aperture over the Focus.
- **Range:** drawn to 256 blocks, so the rite is a landmark from a distance.
- **Strain:** the core's glow stutters.

**Braziers (`BrazierFlames` hook).** For a brazier within the footprint radius + 2 of an escalated rite:
- Flame height grows by up to 50% at full level.
- The colour leans toward the palette's glow red.
- The flames lean toward the Focus by up to 20°.
- Strained: they flicker and burn 25% shorter. Rupturing: they gutter to embers between heartbeats.

**Blood rain (`RiteBloodRain`).**
- **Particles:** small `HemoParticleData.bloodCell` particles falling with a downward velocity: a rain of blood cells, in keeping with the rest of the rite. Splashes are spawned separately: small bursts of blood cells at random points on the ground under the rain, at the surface height of each point, at a rate that follows the rain density. Individual drops are not tracked to the ground. No new particle type and no vanilla particles.
- **Where:** they spawn 12–16 blocks above the floor, inside the boundary radius.
- **How many:** `round(rain level × (2 + 0.5 × footprint radius))` per tick, capped at 30. The rain level runs from 0 at level 3/4 to 1 at level 1. The vanilla particle setting scales it: 1 for all, 0.5 for decreased, 0 for minimal.
- **Strain:** rain thickens by 25%.

**Sound bed (`RiteSoundBed`).** Tickable sounds for the nearest escalated rite, with volume by viewer weight:
- **Drone:** a loop from step 1.
- **Heartbeat:** a one-shot on each floor heartbeat (`CardinalRiteMath.heartbeat`), from level 1/3, so it stays in time with the floor.
- **Wind and rain:** a loop from level 3/4.
- **Strain:** lowers the pitch of every layer.
- **Still intervals:** the bed goes silent.

New sound events, mapped to existing sounds for now so custom audio can replace them later:

| Event | Starting sound |
|---|---|
| `rite.ambient.drone` | `minecraft:ambient.crimson_forest.loop` |
| `rite.ambient.heartbeat` | `minecraft:entity.warden.heartbeat` |
| `rite.ambient.storm` | `minecraft:ambient.basalt_deltas.loop` |
| `rite.escalate` | `minecraft:entity.warden.sonic_charge` |

### Step transition

On each `ESCALATION` beat:
- A shock ring runs out across the floor and the creep, reusing the staff-shock channel of `RiteRings`.
- The sky wound flashes.
- `rite.escalate` plays at the Focus.
- The level overshoots briefly, so the step's new element surges in instead of fading in.

### Endings

Endings are driven by the ghost of the ended rite. The ghost lasts 80 ticks.

| Ending | Choreography |
|---|---|
| Completed | The wound closes with a flash. Over about 3 s the fog clears outward and the creep sinks into the ground. The rain stops at once and the sound bed fades over 2 s. |
| Collapsed or broken | The creep tears back to the floor in 1 s. The wound collapses with 3–5 lightning strikes. The fog surges 20% closer, then thins over 4 s. The sound bed cuts out under the backlash sound. |
| Cancelled | Everything fades over 4 s. |

### Fallbacks

- A rite whose recipe or ceremony is missing on the client gets air and sky only, with default values.
- A rite with no formed floor at its Focus skips the trench and creep.
- Unstained and legacy rites get no layers.

## 3. Replacing the top panel

**Config.** `HemoClientConfig.CARDINAL_RITE_HUD` is an enum, `OFF` (default) or `FULL`. `CardinalRiteOverlay` draws only when it is `FULL`, and `FULL` is unchanged from today's panel.

**Guidance (`RiteGuidance`, client).** Shown only to the rite's owner, in the action bar.
- **On each phase change:** one localized line for about 4 s. The ordeal keeps its existing wave cues, and succession sends its cue key whenever it changes.

| Key | Line |
|---|---|
| `hemomancy.rite.guide.consecration` | The sores hunger. Project blood into them. |
| `hemomancy.rite.guide.inscription` | The circle is fed. Trace its sigils, then seal it at the Focus. |
| `hemomancy.rite.guide.station_projection` | Feed the station's circuits through the Focus. |
| `hemomancy.rite.guide.puppet_trial` | Break the manifested puppet. |
| `hemomancy.rite.guide.still_interval` | Be still. The daemon gathers. |
| `hemomancy.rite.guide.offering_procession` | The floor draws your offerings in. |
| `hemomancy.rite.guide.culmination` | The rite culminates. |

- **Problems:** the server sends a short list of structured problems, each a translation key plus string arguments, next to the checklist. The client no longer recognises problems by matching English text. The first new or changed problem is shown in the action bar.

| Key | Line |
|---|---|
| `hemomancy.rite.problem.sigil_required` | A required sigil sleeps: %s |
| `hemomancy.rite.problem.allies` | The rite needs %s attendants; %s stand ready. |
| `hemomancy.rite.problem.medium` | The Focus holds the wrong medium. |
| `hemomancy.rite.problem.anchor_damaged` | A broken sore must be refilled. |
| `hemomancy.rite.problem.sacrifice` | The circle wants %s mL more from a named creature. |

**Socket markers.** During `INSCRIPTION`, the server syncs each required support socket that has not been awakened. Each entry is a world position, resolved the same way the inscription handler resolves sockets, plus the sigil id. `RiteSocketMarkerRenderer` draws a thin, pulsing blood thread at each one, using the anchor-jet geometry at 0.4 scale and 1.4 blocks tall. Everyone can see the markers.

**Dropped from the default view.** The exact anchor mL totals, ichor, ally count and pool readout. `FULL` still shows them.

## 4. Out of scope

- Rings opening one per phase (the next pass; it feeds the same milestones).
- Per-rite world overrides in the ceremony JSON.
- An aftermath that lingers after the rite ends.
- Custom audio files.
- A world response for Unstained rites.

## 5. Testing

**JVM**
- `CardinalRiteEscalationTest`:
  - totals for 0-, 1-, 2-, 3- and 8-ring rites, with and without offerings, with circuits, for trials and for succession
  - the stage holds when anchors are drained
  - several milestones in one tick give a single step
  - older saves rebuild the stage
  - NBT round trip
- `RiteWorldThresholdsTest`: layer activation for 2-, 4-, 6- and 11-step rites; viewer falloff; strain adjustments; the 24-block fog limit.
- `RiteGuidanceTest`: picking the phase line, detecting problem changes, owner-only display.
- Round trips for the new `RiteEntry` fields and the problem and socket-marker lists, where packet tests exist.
- Source tests: the overlay is gated by the config, and the new layer classes contain no vanilla `ParticleTypes`.

**GameTests (main suite)**
- The stage rises at ring completion, seal, procession and culmination, and an `ESCALATION` beat is sent each time.
- The stage holds when an anchor is drained by strain.
- The stage survives `serialize` and `deserialize`, and a tag from an older save rebuilds it.

**Shaders and visuals**
- `glslangValidator` on the changed membrane shader and the new sky wound shader.
- Offline preview renders of the creep and the sky wound for tuning.
- Final tuning in game.

## 6. Documentation

- `docs/HEMOMANCY_REFERENCE.md`, Cardinal Rite section: the world stage, the HUD default and config option, the guidance lines.
- `wiki/Harbinger-Path.md`: what the world shows during a rite, and the config option.
- `docs/CARDINAL_RITE_AUDIT.md`: status.
