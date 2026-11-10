# Track 11: Progression, teaching, tests and docs

**Goal:** the overhaul changes how the player pays, stores and aims. This track lists everything downstream of that:
the degree gates that break, the text that teaches the old way, the tests and frozen files that pin it, and the open
questions it answers or collides with. It runs beside every other track, not after them.

**Depends on:** nothing. Each row names the track that triggers it.

Counts in this file come from read-only audits on 2026-11-08 and 2026-11-09. Re-check each before acting on it.

## Degree gates

| Gate | Proof today | Verdict | Fix | Track |
| --- | --- | --- | --- | --- |
| D1 → D2 | 500 mL absorbed (`FirstBloodcraftAssignment.recordAbsorption`), usually from an Alembic filled to 2,000 | Breaks if the fill and the draw are far apart | Keep the proof; teach "fill, then draw while it is fresh"; the playthrough already does it in one sitting | 3 |
| D1 → D2 | Cauldron Formation | Retune | Clotting is now the main source; the lesson becomes the first place the player sees it | 3 |
| D2 → D3 | Sampling and Centrifuge | Retune | Sampling costs health (5); Centrifuge stops minting 250 mL | 3, 5 |
| D2 → D3 | Alembic tinctures (2,500 / 5,000) | Breaks if the tank shrinks | Tank size is kept; fill and fire | 3 |
| D2 → D3 | Concentrated Blood and sleep | Breaks if sleep rules change carelessly | Sleep bonus is additive; the gate's check stays | 10 |
| D2 | Rooted / Earthen Vein lesson | Collides with living veins | The Earthen Vein is tier 3 of the ground network; rewrite the lesson to say so | 6 |
| D3 → D4 | Loom and anchors | Retune | The Loom loses its unused tank; anchors count as living during a rite | 3 |
| D3 → D4 | Pallid Vessel (blood in a White Gourd) | Safe | Gourds are living | 3 |
| D4 → D5 | Scar pattern by strongest tendency | Retune | Alignment is unchanged; scars gain the Temper role | 4 |
| D5 | Strain from casts | Breaks if casts become verbs | Shaping pulses add strain at a per-second rate set to match | 1 |
| D4 → D5 | First Culture (Lantern enzyme) | Breaks if the Lantern is replaced | Re-house, keep the id (`CL-15`) | 7 |
| D5 → D6 | Quintessence, Bloodwell | Retune | Check each against clotting and owner draw | 3 |
| D7 → D8 | Gardens "projection" (name collision with Blood Projection) | Wording | Pick one word for each in the lang pass | 11 |
| D7 → D8 | Apotheos rite repair | Retune | Rites still claim first in the chain | 1 |
| D8 | Power lock after the ninth pome; Apotheos hunger pin | Breaks | The lock must cover Shapings and Temper; the hunger pin must survive `BloodRegenRules` | 1, 4, 10 |

**Rules that hold through all of it** (from `AGENTS.md`): Harbinger and Unstained stay mutually exclusive with the
documented reset rules; `volume.isActive()` stays the opt-in gate; every `PendingDelivery` claim keeps its contract;
personal eligibility for station upgrades stays mandatory; no proof is granted by ownership, dialogue or a gift.

## Systems tangled with the verbs

Each needs a line in its track's acceptance, and a GameTest that the old behaviour still wins where it should.

| System | Tie | Rule |
| --- | --- | --- |
| Cardinal Rites | Anchors fed by Projection, recalled by Absorption; `PlaythroughClock.rite` holds Projection | Rites claim before any Shaping |
| Blood Structures, Working crafts | `SUBJECT_FEEDING` by Projection; `RiteOfferingEscrow` | Same; escrow never clots |
| Saints | `SaintChannelRouter`; ring door 1,000 mL | Saints claim first |
| Vesper | Absorption branch at the top of the chain | Unchanged |
| Wills | Banish by Projection, take by Absorption | Unchanged; Will absorption also fills Temper |
| Drudges, SSC | 500 mL per scan from a 30,000 tank; craft withheld (`E-4`) | Decide with `E-4`: a Drudge is a living vessel with a small body, fed by owner draw |
| Bloodline pool | Fane fallback | Living kin; unchanged |
| Morphlings, Brined Court | Cradle upkeep | Already the model |
| Circus (Crossbar, Spindle) | Use the channel | Check after Track 1 |
| Chamber visits | `ChamberVisitService` snapshots inventory | Snapshot Temper and the verb toggle too, or leave them out on purpose and say so |
| Unstained | No blood magic | Stains, gouges and vats need an Unstained answer (cleansing); note it, build it with their ritual system later |
| Thelemic Memories | Tendency shift | Exempt from Temper |
| Heartwood, Bloodwood | Feeding core | A living vessel candidate; check with Track 7 |
| Blood Moon | Track 10 | |
| Combat casts (Track 12) | A strand hit opens a `SchoolDamage.cast` scope, as an R cast does | It stops a Marionette channel, counts toward the D5 strain gate at a per-second rate, and falls under the D8 power lock |
| Vesper, Wills, Drudges (Track 12) | Use the canned entity cast, never a strand | Every new strand manipulation still needs a `VesperManipulationCoverage` row |

## Teaching surface

| Surface | Size | What changes |
| --- | --- | --- |
| `en_us.json` | about 177 lines name projection or absorption; 79 name tendency | Verb toggle, Shaping, Temper, fresh and clotted |
| NPC lessons | Vicar, Alchemist, Mnemonist, Anchorite, Artificer | One speaker at a time, to the sheet in `docs/NPC_VOICES.md` |
| Dialogue inquiry files | 379 | Items that retire (Tendon Line, Memory Thread) and items that change (Flask, Jug, Lantern, Microscope) |
| Liber | 114 pages; 15 on the verbs, 19 on tendency, none on the Lantern | A "Shaping" line on every converted manipulation; new pages for Temper, living blood, the ground network |
| Advancements | 192; about 19 affected | Rewrite triggers, keep ids where the meaning holds |
| Skills | 13 absorption and projection skills | Reach, rate and efficiency skills gain value; check each still does something. Track 12 adds a projection combat line and makes `crimson_mastery` reach the verbs |
| Wiki | 23 pages | With the references |
| Route fingerprints | 630 | Regenerate with `runDialogueRouteGameTestServer` after dialogue changes |

About 70 manipulations are never named in the Liber today. Each conversion writes its page as it lands (Track 2).

New player-facing words need keys and a ruling: **Shaping** (`CL-3`), **Temper** (`CL-9`), **fresh / clotted**,
**Vigor**, **stain / gouge / living vein**, **growth ring**. "Saturation" is taken. "Working" and "Reserve" are taken.

## Tests and frozen files

| Area | JVM tests | Triggered by |
| --- | --- | --- |
| Casting | 53 | 1, 4 |
| Absorption and projection | 24 | 1 |
| Folded items | 9 | 2, 6 |
| Containers and routing | 29 | 3 |
| Tendency and scars | 30 | 4 |
| Sampling | 29 | 5 |
| Ground, farm, brazier | 33 | 2, 6, 7 |
| Staff | 29 | 8 |
| Regen | 5 | 10 |
| Dialogue | 78 | 11 |

- **Playthrough suite:** 426 tests, 315 required. It is the truth for "can a player still reach D8". Run it at the
  end of every stage, one Gradle build at a time. Known baseline failures exist (6 to 7); record the baseline before
  Stage A starts.
- **Source-text tests:** 239 `*SourceTest` files read source as text and will fail on moves and renames. Regenerate
  `docs/architecture/evidence/source-text-impact-map.csv` with `python gradle/source-text-impact-map.py` before each
  track and use it to find them.
- **Frozen files to rewrite as tracks land:** `registry-ids.txt`, `creative-tab-order.txt` (folded items at lines 42,
  85, 385, 569), `payload-registry.txt` with a `PacketHandler.PROTOCOL_VERSION` bump per payload change,
  `route-fingerprints.txt`, `hemo-command-fingerprints.txt`, `hemo-tree.txt`, `entity-cast-fingerprints.txt`, the
  allow-lists under `src/test/resources/architecture/`.
- **Guardrails to add:** `ShapingGuardrailTest` (Track 1), a living-vessel walk over every `IBloodReservoir`
  (Track 3), a Temper charge-point test (Track 4), a block-conservation test (Track 9).
- **Guardrails to extend:** `PlayerBlockEditGuardrailTest`, `BloodAccountingGuardrailTest`,
  `CombatTargetingGuardrailTest`, `RetiredNamesGuardrailTest` (Tendon Line, Memory Thread, `GroundBloodDrawPacket`),
  `PackageLayoutGuardrailTest` (no `core_loop` folder in source; everything files by function).
- **`/hemo` test fixtures:** add fixture branches for a filled vat, a clotted tank, a Temper state and a lit gouge
  run, each as its own `<Name>Subcommand` class.

## Open questions this touches

From `docs/OPEN_QUESTIONS.md`. Confirm each against the roll-up before moving it.

- **Looks answered or moot once the matching track lands:** `PT-1`, `PT-1b`, `PT-2`, `PT-12`, `PT-13`, `PT-14`,
  `PT-25`, `PT-39`, `PT-88`, `F-12`, `F-13`, `F-15`. Possibly `D-26`.
- **Collide, and need a ruling before the track that touches them:** `PT-9`, `PT-11`, `PT-22`, `PT-26`, `PT-29`,
  `PT-42`, `PT-55`, `PT-72`, `PT-75`, `D-20`, `D-21`, `D-25`, `D-28`, `D-37`, `D-40`, `F-16`, `F-17`, `F-19`,
  `G-2b`, `G-2c`, `G-7b`, `S-5b`, `E-4`.
- **New:** `CL-1` to `CL-27`, in `00-README.md` (`CL-20` to `CL-27` are Track 12's).

`D-21` blocks two retirements (Tendon Line and Memory Thread are both craft offerings). `G-2b` / `F-16` (moving
targeting sites to `Reach.STRICT`) should be ruled before Shapings add new targeting sites.

## Docs

- When a track is approved: add its `CL-` rows to `docs/OPEN_QUESTIONS.md` and link the suite from `docs/README.md`.
  Neither is done yet, because nothing is approved.
- When a track lands: update `docs/HEMOMANCY_REFERENCE.md` (the stale sections Track 0 lists first),
  `docs/MANIPULATIONS_AND_CASTING.md`, `docs/STATION_UPGRADES.md`, `docs/LORE_REFERENCE.md` where clotting and
  Temper need a sentence of lore, and `AGENTS.md` for each new rule and guardrail.
- When the suite is done: lift it into one topic doc at `docs/` root (`CORE_LOOP.md`) with the standard headings and
  move the track files to `docs/archive/tracks/core_loop/`.
- Lore to settle in words before the lang pass: why blood dies outside a body (it is the fungal memory that is
  alive, not the fluid); why a creature's blood carries a school; why scars hold Temper steady.

## Acceptance

- The playthrough suite passes at the end of each stage with no more failures than the recorded baseline.
- No teacher, Liber page or advancement describes a mechanic that no longer exists.
- Every `CL-` id is either ruled and moved to "Rulings that bind" or still listed as open, in both places.
