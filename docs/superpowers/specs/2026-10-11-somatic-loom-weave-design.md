# Somatic Loom: the witnessed weave — design

Date: 2026-10-11. Status: approved in conversation, awaiting written-spec review.

## Intent

Weaving a memory should feel powerful and magical, not like a chore. The player is binding another Harbinger's knowledge into a vessel, and that memory resists. The ritual should:

- show what is being learned (a witnessed echo of a past Harbinger casting it);
- make the player prove their worth by surviving and subduing the memory's frayed threads;
- end in a climax rather than an item popping out;
- grow with the player through the shared station-upgrade tiers, like the Alembic, Forge and Scriptorium.

The ritual must never dead-end. A paid weave can always be finished (see the 2026-10-11 enzyme fix: no drift surcharge, never short mid-weave).

## What stays

- Setup: blank Hematic Memory, catalyst, stored enzymes, empty-hand output selection, and blood paid through Blood Projection or a Living Staff.
- The number of threads comes from the recipe's enzyme requirement (one per enzyme unit, at most 8 per tendency; median 2, maximum 12).
- Spent blood and enzymes are lost on a voluntary crouch-use cancel, as today.
- The loom's on-model state display (warp shares, veins, ganglion, woven bands) stays and extends.

## Ritual flow

1. **Payment spins the enzymes.** Each pirn gets a small vial beneath it (see Model). The vials hold the stored enzyme as liquid. When the blood is paid, the required amount is visibly drawn up out of the vials as filament and wound onto the pirns: the liquid drops and the cops grow.
2. **The echo forms.** A colour-shifted Harbinger echo appears above the warp. Its scale and colour depend on the loom tier. It casts the memory's manipulation on loop, using that manipulation's own visuals scaled to the echo.
3. **Fraying.** At the end of each loop, one thread rips loose from the echo. At most two or three threads are loose at a time. The echo visibly loses the part each thread tore from, and knits back as threads are woven home.
4. **Fighting.** Each loose thread hunts the player who paid and attacks with its tendency's kit (see Threads). Each attack it makes adds strain, and each staff hit adds more. At full strain the thread goes slack: it dims, sinks and stops attacking.
5. **Weaving.** Hook a slack thread with the staff and it snaps home along its tether. The reed beats, a band lands at the fell, and that tendency's pirn unwinds its share. A Ganglionic loom catches nearby slack threads automatically.
6. **Climax.** When the last thread is woven, the echo becomes whole. It turns to face the weaver, casts once more at full scale, and is drawn down into the vessel. The memory crystallizes at the fell.
7. **Pausing, never failing.**
   - If the weaver dies, logs out or leaves the ritual area, loose threads retreat into the echo and the weave pauses. Woven bands and paid blood are kept, and the weave resumes when the weaver returns.
   - HUMILIS and MEDIOCRITAS weaves cannot kill. At a low-health floor the weaver blacks out (a brief knockback and darkness) and the weave pauses.
   - SUMMA and above are lethal.

## Threads

Each loose thread is a hostile animated strand: a glowing knot with a frayed tail, tethered to the echo. It keeps its tendency's preferred distance and makes a telegraphed attack every few seconds. Strain needed to go slack and attack strength scale with manipulation rank. Other players may strike threads to help but are never targeted.

| Tendency | Movement | Attack | Effect on hit |
|---|---|---|---|
| Animus | Circles at mid range | Blood-shot bolts | Damage and blood-volume drain |
| Flammeus | Darting dives | Ember spit | Fire, and burning patches on the ground |
| Congeatio | Slow and heavy | Frost-shard fan | Slowness and freeze ticks |
| Mortem | Hangs low | Miasma cloud | Nausea and poison |
| Tenebris | Fades in and out | Darkness pulse | Blindness or darkness; briefly untargetable |
| Ferric | Keeps to the ground | Spikes erupt under the weaver | Damage and a launch |
| Lux | Hovers high | Flash, then light lances | Glare and damage |
| Ductilis | Jumps between points | Nerve-lightning jolt | Short stun and a pull toward the thread |

**Fairness:**
- Every attack has a windup tell.
- At most two or three threads are loose at once.
- Threads stay inside the ritual area.
- All effects use Hemomancy/HutosLib visuals (claws, slashes, cells, lightning, glows) per `docs/MANIPULATION_VISUALS.md`, never vanilla particle clouds.

## Tiers

The loom becomes the sixth upgradeable station under `docs/STATION_UPGRADES.md`:

- the shared `StationUpgradeCatalog` / `StationUpgradeProgress` / `StationUpgradeRules` / `StationUpgradeRites`;
- `StationTierProperty` with stages 0–2;
- in-place upgrades that keep contents, stored enzymes, reservoir, facing and any paused weave.

| | Base (stage 0) | Ganglionic (stage 1) | Cortical (stage 2) |
|---|---|---|---|
| Degree gate | Base station, D3 | D5 | D6 |
| Memory ranks woven | HUMILIS, MEDIOCRITAS, SUMMA (includes Saint Canon) | Adds MAGISTER | Adds PERFECTUS |
| Echo | Small, monochrome, flickering | Coloured, steadier | Life-size, full colour |
| Perk | — | Ganglion feelers auto-catch slack threads within about 4 blocks; longer staff reach | Weaves raise a dome of the weaver's own Chamber of Will sky theme around the loom; the echo stands inside it |
| Model | Current body plus vials | Grown ganglion with nerve feelers down the uprights; vials on nerve cradles | Cortical crown over the castle; the warp takes a pale myelinated sheen |
| Upgrade item (working name) | — | `ganglionic_harness` | `cortical_crown` |
| Free-kit teacher | — | Mnemonist | Mnemonist |
| Personal practice | — | Complete any weave | Complete a MAGISTER weave on a Ganglionic loom |
| Rite circuits | — | 2 × 50 mL | 3 × 50 mL |
| Five companion offerings (proposed) | — | `neurotic_enzyme`, `nerve_fiber`, `puppeteering_thread`, `sanguine_formation`, `blood_crystal_shard` | `nerve_bundle`, `synaptic_node`, `mnemonic_ambergris`, `blood_stained_stone`, `sanguine_glass` |

- **Rank gates match the learning gates.** `ManipulationRankGates`: HUMILIS D0, MEDIOCRITAS D1, SUMMA D3, MAGISTER D5, PERFECTUS D6. No player is locked out of a memory they could already learn, except that they must upgrade their own loom (the same as Armature recipe tiers).
- **Out-of-tier recipes** stay visible when cycling outputs, but are refused with a message naming the needed tier.
- **Existing looms**, including Harbinger Outpost placements, load as stage 0. Creative direct-kit use follows the contract.
- **Offerings** are all registered items. Check how `synaptic_node` is obtained at D6 during planning, and swap it if it is not reachable.
- **Copper:** none on loom upgrades (blood-side materials only).

## Echo vision

- **Client-only and purely visual.** The echo never affects gameplay.
- **Caster.** A small client-only humanoid caster above the warp, scaled and tinted by tier, plays the existing casting animations. `CastingAnimationClientState` is keyed to a player today; extend it to accept a non-player.
- **Every memory gets its real cast.** Each manipulation has an echo entry in a registry. The entry type depends on how the manipulation draws today:

| How it draws today | Count (approx.) | Echo entry |
|---|---|---|
| The `ManipulationVisuals` cue system (visual-only by design) | 50 | A short timeline of locally built `ManipulationVisualPacket`s fed to `ManipulationVisualRenderer.accept()`. Use entityId -1 for bursts, or the echo's client entity id for attached and charge cues. |
| Particles spawned directly in cast code | 19 | A hand-written client replay using Hemomancy particles |
| Projectile or summon entities | 8 | A hand-written client replay with client-only dummy visuals |
| Passives and managers with no cast visual | 17 | A short staged scene. Example: Axonal Transduction shows the echo vanishing into a nerve and reappearing |

- **Coverage.** A contract test lists every registered manipulation and fails for any without an echo entry.
- **Delivery.** Echo entries ship in batches by school. Until a memory's entry lands, it uses a school-gesture fallback. Nothing else in this design waits on echo coverage.
- **Saint Canon Memories** may use the Saint as the echo.

## Model

`tools/model_export/somatic_loom.py` gains:

- **Vials.** A small glass vial under each pirn, in a hematic iron cradle beneath the bone shelf. A tendency-coloured filament rises from the liquid, through the shelf, to the pirn.
  - The vial liquid is a renderer-scaled part showing stored stock.
  - The pirn cop shows enzyme spun for the current weave; it is no longer the stored stock.
- **Two stage bodies** (Ganglionic and Cortical) and their part models.
- Generated `SomaticLoomGeometry` anchors for the vials, stage parts and echo stand.
- The usual guarantees: zero coplanar fights, deterministic export, and a body/glass layer split for the vials.

## Architecture

- **`SomaticWeaveRules`** (pure, JVM-tested) owns:
  - the fray cadence;
  - strain per attack and per staff hit;
  - the slack threshold and rank scaling;
  - the concurrent-thread cap;
  - the non-lethal floor;
  - the pause and resume rules;
  - the tier/rank gate;
  - the auto-catch radius.
- **`SomaticLoomBlockEntity`** runs a server state machine: idle → awaiting blood → fraying/fighting → climax → idle. It persists paused progress. The fixed-distance staff drag (`dragSelectedOrb`, `ORB_*` wander code) is retired.
- **`FrayedThreadEntity`** is a new server entity carrying tendency, owner UUID, loom position, strain, rank and tether. It performs the attacks and renders with the existing knot-and-fray look. Staff hits add strain. A staff hook on a slack thread completes it.
- **The echo** lives in `client/render/...`: a client entity or renderer, an `EchoCast` registry and replay helpers. The loom renderer draws the echo stand, the vials and the spin-up.
- **Cortical dome.** A short spike first: can a `ChamberThemeEffects` theme render into a bounded sphere around a point instead of the skybox? If not, ship a simpler themed dome and record why.
- **Networking** uses `CustomPacketPayload` per `PacketHandler`, only where state isn't already synced through the block entity or the entity.
- **The Living Staff** stops scanning a 21×11×21 block volume every tick for looms. Thread hooking goes through the targeted thread entity instead.

## Teaching and docs

- **Mnemonist** dialogue teaches the witnessed weave, the tiers and the kits.
- **Liber** pages, advancements and `STATION_UPGRADES.md` are updated, along with `HEMOMANCY_REFERENCE.md` (the loom row, §25.7 ritual flow) and `wiki/Harbinger-Path.md`.
- **Tone:** fungal blood-memory; the echo is a past Harbinger's memory, not a god or spirit.

## Testing

- **JVM:** `SomaticWeaveRules` and the existing `SomaticLoomEffectRules`.
- **GameTests** (`somatic_loom_validation`):
  - the full lifecycle: pay → threads fray → strain by attack and by hit → slack → hook → climax → memory delivered;
  - death and leaving range pause the weave and keep bands;
  - the non-lethal floor below SUMMA, and lethal at SUMMA and above;
  - out-of-tier recipe refusal;
  - upgrade rites at each stage: eligibility, kit/offerings, contents and paused weave preserved, personal-practice proofs;
  - the existing no-dead-end enzyme test.
- **Source contracts:**
  - echo coverage for every manipulation;
  - no `ParticleTypes.` in loom or thread code;
  - the stage blockstates and part models are registered.
- **Native client review:** each tier idle and weaving, a mid-fight weave with several tendencies, the climax, and the Cortical dome, by day and night.

## Out of scope

- Changing recipe contents, enzyme costs or blood costs.
- Moving weaving into the actual Chamber of Will dimension. A bonus for weaving there may come later.
- Rebalancing manipulations themselves.
