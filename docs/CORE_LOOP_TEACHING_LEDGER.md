# Track 11: Progression, teaching, tests and docs

**Goal:** the overhaul changes how the player pays, stores and aims. This track lists everything downstream of that:
the degree gates that break, the text that teaches the old way, the tests and frozen files that pin it, and the open
questions it answers or collides with. It runs beside every other track, not after them.

**Depends on:** nothing. Each row names the track that triggers it.

Counts in this file come from read-only audits on 2026-11-08 and 2026-11-09. Re-check each before acting on it.

Note (2026-11-14): eight lesson lines this ledger lists are false today, and the Mnemonist's `strands.stain` branch
teaches a form cut on 2026-11-12. The fixes are tracked as 16 T5 in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md); rulings
are in [CORE_LOOP.md](CORE_LOOP.md).

## Degree gates

| Gate | Proof today | Verdict | Fix | Track |
| --- | --- | --- | --- | --- |
| D1 → D2 | 500 mL absorbed (`FirstBloodcraftAssignment.recordAbsorption`), usually from an Alembic filled to 2,000 | Breaks if the fill and the draw are far apart | Keep the proof; teach "fill, then draw while it is fresh"; the playthrough already does it in one sitting | 3 |
| D1 → D2 | Cauldron Formation | Retune | Clotting is now the main source; the lesson becomes the first place the player sees it | 3 |
| D2 → D3 | Sampling and Centrifuge | Retune | Sampling costs health (5); Centrifuge stops minting 250 mL | 3, 5 |
| D2 → D3 | Alembic tinctures (2,500 / 5,000) | Breaks if the tank shrinks | Tank size is kept; fill and fire | 3 |
| D2 → D3 | Concentrated Blood and sleep | Breaks if sleep rules change carelessly | Sleep bonus is additive; the gate's check stays | 10 |
| D2 | Rooted / Earthen Vein lesson | Collides with living veins | The Earthen Vein is tier 3 of the ground network; rewrite the lesson to say so (done, round two P8) | 6 |
| D3 → D4 | Loom and anchors | Retune | The Loom loses its unused tank; anchors count as living during a rite | 3 |
| D3 → D4 | Pallid Vessel (blood in a White Gourd) | Safe | Gourds are living | 3 |
| D4 → D5 | Scar pattern by strongest temper | Retune | Alignment is unchanged; scars gain the Temper role | 4 |
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
| Thelemic Memories | Temperament shift | Exempt from Temper |
| Heartwood, Bloodwood | Feeding core | A living vessel candidate; check with Track 7 |
| Blood Moon | Track 10 | |
| Combat casts (Track 12) | A strand hit opens a `SchoolDamage.cast` scope, as an R cast does | It stops a Marionette channel, counts toward the D5 strain gate at a per-second rate, and falls under the D8 power lock |
| Vesper, Wills, Drudges (Track 12) | Use the canned entity cast, never a strand | Every new strand manipulation still needs a `VesperManipulationCoverage` row |

## Teaching surface

| Surface | Size | What changes |
| --- | --- | --- |
| `en_us.json` | about 177 lines name projection or absorption; 79 name temper | Verb toggle, Shaping, Temper, fresh and clotted |
| NPC lessons | Vicar, Alchemist, Mnemonist, Anchorite, Artificer | One speaker at a time, to the sheet in `docs/NPC_VOICES.md` |
| Dialogue inquiry files | 379 | Items that retire (Tendon Line, Memory Thread) and items that change (Flask, Jug, Lantern, Microscope) |
| Liber | 114 pages; 15 on the verbs, 19 on temper, none on the Lantern | A "Shaping" line on every converted manipulation; new pages for Temper, living blood, the ground network |
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
| Temper and scars | 30 | 4 |
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

_Snapshot from when this track was written. Every question here has since been answered or is carried in `docs/OPEN_QUESTIONS.md`, which is the only live list._

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

## Ledger of landed changes

Each track adds its rows here as it lands: what a player can now see or do differently, and where it is taught.

| Track | Surface | Change |
| --- | --- | --- |
| 0 | Skills (`skill_quickened_draw`, `skill_hungry_pulse`, `skill_arterial_cadence` descriptions) | The Blood Absorption pulse on a living creature stays at 10 ticks (the hurt cooldown); the three cadence skills now raise the drain per pulse (x1.25, x1.67, x2.5) instead of shortening the pulse, which had made them land less often |
| 0 | Absorption strain (`skill_blood_tolerance`) | Strain now builds: one per tick of drinking from a creature, easing two per tick otherwise, so Weakness arrives after 5 s of continuous drinking and Blood Tolerance has something to delay. Session state, forgotten on relog |
| 0 | Specimen Jar (`message.hemomancy.specimen_jar.refused.*`) | Refuses bosses, creatures owned by someone else, creatures carrying riders, bodies over 2 blocks and over 60 health, with an action-bar line; lifts a riding creature off instead of deleting it |
| 0 | World edits (`message.hemomancy.player_block_edits.refused`) | Blood infusion and Blood Structure crafts check the player's right to break every block they would consume before taking blood; item and event edits (Foul Paste, Engram Stamp, scratch engraving, Blood-Stained Stone, Pale Humor Flask, Terrestrial Speculum, Consecration, copper brushing, cap bridges, the cauldron Condenser) follow claim and spawn protection |
| 0 | Living Staff ground draw (Left Alt) | Lighting an ash trail needs the full 25 mL; an empty player no longer lights it for free |
| 0 | Gourd | Shears no longer carve a gourd into a vanilla carved pumpkin |
| 0 | Living tools | Drops now follow the `hemomancy:living_tool_mineable` tag (cobweb) and the Living Axe's axe tag through the item's tool component |
| 0 | Guardrails | `PlayerBlockEditGuardrailTest` widened to `common/event`, `item`, `block` and `tile`, with its allow-list in `src/test/resources/architecture/player-block-edit-allow-list.txt` |
| 1 | Verb key (`key.hemomancy.bloodverb.desc`, default Y), D1 controls line (`hemomancy.tutorial.current_controls`) | Blood Projection and Blood Absorption leave the manipulation wheel and the cycle key. The verb key conjures the active verb's hand into an empty hand, turns a held hand to the other verb, and with a sneak lets the hand go (same refund as before). Taught in the Vicar's D1 controls line, the structure hint (`screen.hemomancy.blood_structure_hint.instructions_harbinger`), the new Liber page "The Two Verbs" (`manipulations/pages/the_two_verbs`) and the wiki |
| 1 | Living Staff (`hemomancy.artificer.living_staff.use`, its dialogue brief, wiki) | The Staff channels the active verb whatever is selected; a selection no longer decides project or absorb. A tap of 20 ticks or less that moved no blood calls the Primal Morphling (it used to fire on any release with no utility selected). Right-clicking a blood block with the Staff now always channels instead of using the block |
| 1 | Reach (`skill.hemomancy.skill_sanguine_reach.desc`) | One reach for both verbs: 5.5 blocks bare (bare Absorption was 5.0), the Staff's focus range on the Staff for both verbs (Staff Projection was 5.5), Sanguine Reach multiplies either. Rites, ring doors, founder initiation and block endpoints use the same reach |
| 1 | Channel HUD (`verb.hemomancy.*`, `hud.hemomancy.verb_channel.shaping`) | While a verb is channelled a small label beside the crosshair names it; with a Shaping selected, its icon, name and base mL/s and four reticle ticks that brighten when it claims. No Shaping exists yet, so players see only the verb label until Track 2 |
| 1 | Mnemonist's Distributor line (`hemomancy.mnemonist.distributor.line1`) | Says the verbs are not memories you arrange, rather than "fixed" entries |
| 1 | Messages (`message.hemomancy.verb.switched`, `message.hemomancy.verb.unknown`), blood source `blood_source.hemomancy.shaping` | The verb switch and an unknown verb say so on the action bar; a Shaping's pulses show in the blood-flow diagnostics as Shaping |
| 1 | Taught (R2 P10) | The Hermit's `verbs` lesson (blood awake, Degrees 0 to 4) and the Vicar's `verbs` lesson (from the first Degree: the verb key, the radial's inner ring, who claims a held verb) teach the verbs; the Mnemonist's `shapings` lesson names the Shaping (see `CoreLoopLessons`) |
| 2 | Memory tooltips (`tooltip.hemomancy.blood_memory.shaping`, `hemomancy.manipulation.<id>.shaping.<verb>`) | A memory that shapes a verb shows a "Shaping, Projection:" (or Absorption) line saying what it does to what you aim at. Present on Lignum Mortis, Canopy Mortis, Worked Lignum, Pyretic Forge, Blood Binding and its three forms, and the Tendon Line (both verbs) |
| 2 | Liber pages (`manipulations/pages/lignum_mortis`, `pyretic_forge`, `tendon_line`, `blood_binding`, `brazier_transmutation`) | One page per conversion with its "Shaping," line in the same place, and a page on the brazier's transmutations |
| 2 | Lignum Mortis (`hemomancy.manipulation.lignum_mortis.desc`) | Hold Projection on a log or mushroom stem: the rot crawls while blood flows (16 mL/s) and the wood falls when you let go, or at once when the crawl has reached everything. R still works (marked for removal) |
| 2 | Pyretic Forge (`hemomancy.manipulation.pyretic_forge.desc`, JEI Blood Infusion rows) | Hold Projection on items on the ground to smelt them where they lie, with the furnace's experience, or on sand, stone, clay, a wet sponge or a raw ore block to fire it in place (40 mL/s). Taught by JEI (the memory shows as the catalyst) and the Liber |
| 2 | Tendon Line (`hemomancy.manipulation.tendon_line*`, `message.hemomancy.tendon_line.*`, memory `memory_tendon_line`, Somatic Loom recipe) | The Tendon Line is a memory now, not an item: Projection throws and roots it at a solid face up to three times your reach, Absorption hauls you along it, it snaps if you stray a third further. R says it has nothing to cast. Woven from Sinew Cord with Animus and Ductilis enzymes |
| 2 | Sinew Cord (`item.hemomancy.sinew_cord`, Materials atlas, Mnemonist Distributor line `hemomancy.mnemonist.distributor.line4`) | Replaces the Tendon Line as an offering of the Dendritic Distributor, Sanguine Vigil, Armature Consecration and the Memory Thread recipe; crafted by the old line's recipe, two at a time |
| 2 | Blood Binding and its forms | Hold Projection on a creature: held while blood flows (20 to 44 mL/s by form), then constricted for a short tail. The Constrictor Cord item is gone; its slow is that tail |
| 2 | Iron Brazier (`message.hemomancy.brazier_transmutation.*`, JEI "Brazier Transmutation", blood source `blood_source.hemomancy.brazier_transmutation`) | An offering no other rite wants, in a lit brazier, is drawn out by held Absorption into something else chosen by the selected memory (twelve recipes; some want a catalyst in the off hand). The scar, memory and graft rites are unchanged |
| 2 | Taught (R2 P10) | The Mnemonist's `shapings` lesson (claim order, Lignum Mortis, Pyretic Forge, the Brazier) teaches the converted memories; the Artificer's `climb` lesson teaches the Tendon Line and the Vicar's `keeping` lesson the Blood Binding leash. Advancements are untouched |
| 3 | Machine tanks (`message.hemomancy.living_blood.*`, bar tooltip `screen.hemomancy.blood_volume_bar_widget.clot`, Liber `the_hematic_order/pages/living_blood`, wiki Blood-Systems) | Blood in a machine stays fresh five minutes after it was last fed, then clots (a tenth of what is left each minute; none fresh after an hour). Clot is not fuel and fills the tank. Station bars show fresh blood over a band of clot, dulling as it ages; the tooltip names the clot and the state |
| 3 | Clot out as Formations | Blood Absorption on a tank drinks the fresh blood first, then scrapes the clot out, one Sanguine Formation per 400 mL. The Condenser also condenses clot, at its usual better yield |
| 3 | Suture owner draw | A sutured machine drinks from its owner (Degree 3, within 16 blocks; Degree 5 in their Fane) as upkeep and stays fresh; with the owner away, offline, dead or below Degree 3 it stalls and clots. Taught by the Liber page and the Gauge reading |
| 3 | Vascular Status Gauge (`tooltip.hemomancy.vascular_status_gauge.reads_machines`, `message.hemomancy.living_blood.gauge.*`, `.state.*`, `.feed.*`) | Used on a machine it prints fresh and clotted blood, the state, and whether its suture is feeding it or why it has stalled, instead of opening the machine's screen |
| 3 | Ownership (`message.hemomancy.living_blood.not_yours`, server config `bloodline.foreignReservoirDraw`) | Only a machine's placer and their bloodline may absorb from it, scrape it, collect its Condenser formations or send a courier to it; others are told why. Server owners can open this |
| 3 | Bloody Flask and Jug (`tooltip.hemomancy.living_blood.*`, `message.hemomancy.living_blood.flask_clotted`, `material.hemomancy.bloody_flask.desc` and `bloody_jug.desc` corrected to 2,500 and 5,000 mL, Alchemist `item_inquiry.bloody_flask.line2`) | A flask clots from when it was bottled or first carried; its tooltip shows how much has set. Drinking gives the fresh share as blood and the set share as Formations; pouring puts the set share into the machine as clot. Blood Rock does not clot |
| 3 | Sizes | Working buffers: Semi-Sentient Construct, Sanguine Vigil, Morphling Incubator, Enzymatic Scriptorium and Vial Centrifuge 600 mL; Mycelial Lantern 1,200 mL; Gourdvine Tap 2,000 mL and living (never clots). The Somatic Loom and Covenant Throne keep no tank |
| 3 | No free blood | The Vial Centrifuge no longer adds 250 mL per spin and the Alembic no longer adds 100 mL per free distillation; the Alembic's "Tank full" status is gone (`container.hemomancy.alembic.status.tank_full` removed) |
| 3 | Blood Formation key (`key.hemomancy.bloodformation.desc`) | Costs 300 mL for exactly one Formation (was 100 mL for 0 to 3), twice the Sanguine Cauldron's price |
| 3 | Suture Needle (`item.hemomancy.hematic_suture_needle.status`) | The needle's line now says a sutured machine drinks from you while you are near and stays fresh |
| 3 | Taught (R2 P10) | The Alchemist's `living_blood` lesson (the five-minute window, the suture, clot as Formations, the gauge, who may draw) teaches it; the Vein-Mason's `ground_marks` lesson covers the basin's bowl. No advancement for scraping clot |
| 4 | Temper pool and adaptive blood HUD (layer `hemomancy:bloodvolume`, Liber `temperament/pages/temper`, wiki Blood-Systems) | A living binding grows around the enabled vessel, equipped reserve, Morphling jar and eight pouring Temper vials (`HudAssembly`, `HudBinding`); the vials need the creative-only Titration Bandolier in its Scarlet Vanity slot (CQ-63). Independent client display toggles reshape it without changing eligibility or mechanics. Seen-school vials, healed sockets, the trickle, dry vials and real-cursor tooltips persist; JVM tests pin the geometry, binding, heartbeat and vial art. `runTemperRackReviewClient` captures every toggle combination, corners, GUI scales 2–4, bright terrain, each reserve on both sides, degrees 0/3/5/7/Apotheos, low blood, the jar modes and tooltips. |
| 4 | Dry and catalyst notices (`message.hemomancy.temper.dry_cast`, `.dry_shaping`, `.catalyst`) | The action bar says "Dry of <school>: the cast takes more blood and lands softer" or names the catalyst that dissolved, at most every two seconds |
| 4 | Cost ledger (`message.hemomancy.manipulation_cost_ledger.temper` and `.temper.tempered` / `.catalyst` / `.dry`) | The Scrying podium's cost tooltip carries a Temper ("Carried") line; only a dry one changes the price (x1.25 cast, x1.5 Shaping) |
| 4 | Scrying Temper tab (`screen.hemomancy.scrying_diagnostics.temperament_and_temper`, `.temper_held`; `.temperament_profile` removed) | Each school shows grown alignment, what scars lend (+n) and Temper held against its cap |
| 4 | Vicar's First Bloodcraft kit (`hemomancy.vicar.neophyte.first_bloodcraft_reward.granted`) | The kit gains one vivacious enzyme and the Vicar says to carry it in the off hand for when the blood has no Animus left |
| 4 | Rite of Sanguine Attunement (`hemomancy.rite.sanguine_attunement.desc`) | Says the Temper cap falls with the reset alignment |
| 4 | Blood profiles | Creeper Ductilis and Flammeus, armadillo Ferric, bogged Mortem. Coal and charcoal render into the Fervent enzyme (25%) |
| 4 | Taught (R2 P10) | The Alchemist's `temper` lesson (pool against Temperament, filling, dry, catalysts at 10, 5 and 3) and the Vein-Mason's `walls` lesson (a scar cuts the burn a quarter a tier) teach it. No advancement for a first catalyst or a first dry cast |
| 5 | Blood Binding leash (`message.hemomancy.menagerie.*`, blood source `menagerie_leash`; Liber `the_hematic_order/pages/menagerie`, the Blood Binding page's last sentence) | Held for three seconds on a passive creature or one at a third of its health, the binding leashes it: it follows, never despawns, goes through portals with you, costs a quarter of its maximum health in mL a second, and breaks free unpaid. One leash at D3, two at D5, three at D7. Sneak-use with an empty hand lets it go. Refusals name why |
| 5 | Specimen Vat (`block.hemomancy.specimen_vat`, `tooltip.hemomancy.specimen_vat.lead_in`, recipe `specimen_vat`, Alchemist inquiry `hemomancy.alchemist.item_inquiry.specimen_vat.*`) | A two-block glass column for a creature up to two blocks high and 40 health. Lead it in and use the vat with an empty hand. Its creature lives on Vigor fed from an 800 mL living buffer; held Absorption draws Temper in its schools and a little blood; neglect leaves a husk (bone meal and a ruinous enzyme); a strong hostile one can break out |
| 5 | Specimen Jar (`tooltip.hemomancy.specimen_jar.vigor`, `.husk`) | A placed jar keeps its creature on Vigor like a vat (200 mL buffer). A filled jar no longer stacks. A jar nobody placed is a preserved specimen. An empty hand reads it, an empty vial samples it |
| 5 | Vessel readout (`message.hemomancy.menagerie.readout.*`, `.vigor.*`) | An empty hand on an occupied vessel prints the creature, its Vigor, its buffer, whether a Vigil tends it, and its blood as far as you know it |
| 5 | Sanguine Vigil (`block.hemomancy.sanguine_vigil.catalyst_in`, `.catalyst_full`, `.no_catalyst`, `.tending`, `.not_tending`) | A Temper catalyst used on the Vigil goes into its slot; while stocked and you stand within 16 blocks it keeps kept creatures within 8 blocks from fading, one catalyst per five minutes. Its ward skips led creatures |
| 5 | Sampling (`message.hemomancy.blood_sampling.success` rewritten, `.recently_drawn`; damage type `hemomancy:blood_draw`, `death.attack.blood_draw*`; Liber `the_hematic_order/pages/unread_humours`, wiki Clinical-Blood-Tools) | A vial takes 2 health, a syringe 1, and the creature needs 30 seconds before another draw. "Sampled without harming it" is gone |
| 5 | Unread humours (`item.hemomancy.bloody_vial.unknown` and `.examine` rewritten) | A vial's blood is unlabelled until you have examined its kind yourself; a traded sample stays unread. Three individuals of a kind studied give a vessel's draw yield |
| 5 | Animal dose and taint (`message.hemomancy.menagerie.feral`, `.dose_short`, blood source `animal_dose`; item `tainted_meat` and its tooltip, blood source `tainted_meat`; Alchemist inquiry `hemomancy.alchemist.item_inquiry.tainted_meat.*`) | Plain Projection into a passive animal puts it in season for 20 mL; it is tainted: tainted meat, a growing chance to go feral, young that may carry your strongest school |
| 5 | Taught (R2 P10) | The Vicar's `keeping` lesson (the leash, Vigor and feeding, the brood) and the Alchemist's `humours` lesson (vial and syringe, examining, the Bestiary, dosing) teach it |
| 6 | Stains (`block.hemomancy.blood_stain`, blood source `ground_stain`, `stain_reabsorb`) | Plain Projection that nothing claims leaves a wet stain on the face (4 mL, once while it stays wet); stains age wet, tacky, dry, gone and wash in rain or beside water; Absorption takes 2 mL back from a wet one and cleans an old one. Deaths and heavy bleeding leave a few |
| 6 | Gouges and catalysts (`block.hemomancy.blood_gouge`, `ground_function.hemomancy.*`, `message.hemomancy.ground.pulse`, `.too_little_blood`, blood source `ground_pulse`; Liber `manipulations/pages/the_ground_network`) | A gouge run pulses for 15 mL when blood is projected into it; an off-hand catalyst gives the lit run light, heat, a bearing, cold, pull, feeding or dark (data `gouge_catalyst/`). Absorption fills a gouge in. The action bar names the function and the cells reached |
| 6 | Catalyst Feeder (`block.hemomancy.catalyst_feeder`, recipe `catalyst_feeder`) | Keeps a lit run of its stack's function lit; never lights a dark run |
| 6 | Living veins (`block.hemomancy.living_vein`, `block.hemomancy.vein_node`, `message.hemomancy.ground.node_not_yours`, `.vein_needs_degree`, `.vein_allowance`, `.node_no_run`, blood sources `vein_growth`, `vein_upkeep`; Liber `manipulations/pages/living_veins_and_basins`) | D3 gate: vein length 16 at D3, 24, 32, 48, 64 from D7. A vein moves a little blood between two tanks only while its owner is near; untended a day it withers |
| 6 | Basins and gutters (same Liber page; reference "The ground network") | A placed Venous Basin is a 2,000 mL tank that pours down placed gutters to machines and farmland while fresh, then clots in the bowl. Structure gutters and basins are untouched |
| 6 | Four Shapings (`hemomancy.manipulation.vascular_dowsing.shaping.project`, `hematic_beacon.shaping.project`, `blood_eclipse.shaping.absorb`, `sanguine_magnetism.shaping.absorb`; Liber `manipulations/pages/ground_shapings`; block `lux_trace`) | Tooltips and the Liber say what each memory makes of a verb: cut gouges, spread fading light, put lights out, draw dropped items |
| 6 | Retired (`key.hemomancy.drawtest.desc`, `item.hemomancy.memory_thread.clear/start/stop/tooltip`) | The Left Alt Blood Draw key is gone (Projection lights ash trails); the Memory Thread is a plain material, its route recording replaced by gouges |
| 6 | Taught (R2 P10) | The Artificer's `hot_runs` lesson (cut, catalysts, hot runs, the feeder) and the Vein-Mason's `ground_marks` and `carried` lessons (gouges, living veins, basins, the carried crossing) teach it. No advancement for a first gouge or vein |
| 7 | Fed ground (Liber `the_hematic_order/pages/fed_ground`; reference "Farming and water") | Crops grow faster on blood-fed ground: a drip from a fed scab spike or a wet stain beside the soil (level 1), a planting basin of fresh blood (level 2), the cloche (level 3) |
| 7 | Gourds need blood (`item.hemomancy.gourd_seeds.tooltip`, new Alchemist item inquiry `hemomancy.alchemist.item_inquiry.gourd_seeds.line1` / `.line2`) | The gourd vine does not grow, and bone meal does not wake it, on unfed ground. A wet stain beside the stem grows it slowly; a planting basin grows it properly. Existing dev-world gourd farms stop |
| 7 | Planting Basin (`block.hemomancy.planting_basin`, recipe `planting_basin`) | A bed of soil over a 500 mL tank, filled by hand or a worked gutter; feeds its plant at level 2 while fresh, then clots |
| 7 | The cloche (`material.hemomancy.mycelial_lantern.desc`) | The Mycelial Lantern's culture slot takes seeds as well as spores; the crop grows under the bell for 25 mL a stage. First Culture, the recipe, JEI and the menu are unchanged |
| 7 | Vital Effusion's Shaping (`hemomancy.manipulation.vital_effusion.shaping.project`) | Tooltip and Liber: hold Projection on a plant to soak its soil and grow it as bone meal would |
| 7 | Chum (`message.hemomancy.chum.coloured`, blood source `chum`; Liber `the_hematic_order/pages/chum`) | Projecting into water with nothing selected pours a pool that lures fish faster and sometimes gives its own catch; an off-hand catalyst colours it |
| 7 | Taught (R2 P10) | The Alchemist's `fed` lesson (drip, basin, Vital Effusion and the Lantern) and `beds` lesson (strains, timber, glowroot and emberpod, Tainted Harvest) and the Vicar's `answers` lesson (chum and its hunters) teach it |
| 12 | Strands (`hemomancy.manipulation.<id>.shaping.project` for `searing_lash`, `hematic_flare`, `blood_eclipse`, `glacial_grasp`, `hemorrhage`, `blood_lash`; Liber `manipulations/pages/strands`, `strand_forms`; wiki Blood-Systems "Strands") | Five combat memories put out a strand on held Projection: a lash (Searing Lash), a jet (Hematic Flare), a spray (Blood Eclipse), a plant (Glacial Grasp), a brand (Hemorrhage). The strand holds the channel until release and costs 40 to 60 mL/s; how it is moved decides the damage. Each memory's R cast is unchanged |
| 12 | Blood Lash (`hemomancy.manipulation.blood_lash*`, `item.hemomancy.memory_blood_lash`, `message.hemomancy.blood_lash.verbs_only`, Somatic Loom recipe `memory_weaving/memory_blood_lash`, Mnemonist inquiry `hemomancy.mnemonist.item_inquiry.memory_blood_lash.*`) | A new Animus Humilis memory (CL-23): nothing to cast on R; a weak lash (15 mL/s) that teaches the stroke and the crack. Woven on the Somatic Loom from leather and two vivacious enzymes |
| 12 | The snap (left click while a strand is out; blood source `blood_source.hemomancy.strand_snap`) | Forces the strand's accent for 20 to 40 mL: a crack along the aim, the jet's pressure spent at once, a drench, an early eruption, a brand set at once. A reversal crack is free |
| 12 | Pace | A strand replaces vanilla's use-item crawl with its own pace: spray 1.0, lash 0.8, plant and brand 0.6, jet 0.5. Plain Projection still crawls at vanilla's pace |
| 12 | Strand HUD | A jet's pressure, a plant's sunk blood or a brand's hold shows as a thin arc round the crosshair in the verb channel layer |
| 12 | Absorption's owner (`death.attack.blood_absorption`, `.player`; Liber `manipulations/pages/the_draw_and_the_mark`) | Drained creatures now turn on the drawer, and a kill by draw is credited (experience, loot, milestones) but pays no kill blood and no Temperament kill shift. The draw no longer knocks back, as before |
| 12 | Absorption riders (same Liber page, wiki) | The draw reads the school state on the creature: Pressure pulls and gives more back, Searing draws harder and gives less, Rime holds, Lodestone strips armour, Necrosis spreads on a kill, Disrupted arcs, Illuminated reaches further, Obscured drops the creature's target and veils the drawer. Crimson Mastery now scales the draw. The drawer's branded creatures are drawn first, from twice the reach |
| 12 | Taught (R2 P10) | The Mnemonist's `strands` lesson (the five forms, the stain run, brands) and the Artificer's `strand_line` lesson (the six skills and the twin) teach it. No advancement for a first strand, crack or brand |
| 8 | Growth rings (`message.hemomancy.living_staff.ring_laid`, Staff tooltip `item.hemomancy.living_staff.rings` / `.next_ring`; Liber `manipulations/pages/the_staff_grows`; Artificer `hemomancy.artificer.grafts.rings` in the bonded player's grafts lesson) | The Staff lays down a bone ring at 5,000, 25,000, 100,000, 300,000 and 750,000 mL handled: more reach through the Staff (+0.5, +1, +1), a 10% faster channel at ring 3, a harder dig at rings 2 and 4, 25 mL off a form swap at ring 5. A closing ring is announced in chat; the rings show on the shaft |
| 8 | The pickaxe form (`item.hemomancy.living_pickaxe`, `hemomancy.manipulation.conjure_pickaxe*`, tooltip `tooltip.hemomancy.living_tool.dig_tier.*` / `.dig_cost`, blood source `blood_source.hemomancy.living_dig`; Artificer `hemomancy.artificer.grafts.pickaxe`) | Learned with the bond (and at login for bonded players who lack it). Takes iron ore on the first day, diamonds from ring 2, obsidian from ring 4, never netherite. 2 mL per point of hardness per block; dry, it digs like a hand |
| 8 | Living Axe | Now digs by the same ring tier (it was netherite-level from the first minute) and pays blood per block like the pickaxe. Say this in the design, not as a patch note (track doc hazard) |
| 8 | Enchantments and durability | No Living form takes an enchantment (table, anvil or book) and none has durability; the axe and crossbow left the vanilla enchantable tags. A living tool with no Staff to recoil into crumbles when its wielder runs dry |
| 8 | Lode Draw and Hematic Hewing (`hemomancy.manipulation.lode_draw*`, `hematic_hewing*`, `item.hemomancy.memory_lode_draw` / `memory_hematic_hewing`, `message.hemomancy.lode_draw.*`, `message.hemomancy.hematic_hewing.verbs_only`, loom recipes `memory_weaving/memory_lode_draw` / `memory_hematic_hewing`, Mnemonist inquiries; Liber `manipulations/pages/mining_shapings`) | Two Ferric memories with nothing to cast: held Projection crawls an ore lode and breaks it on release (24 mL/s), or cuts a tunnel, a sneaking 3x3 face or a stair's column each second (36 mL/s), at the Staff's ring tier with plain drops |
| 8 | Talaromyces Minus | Its sneaking vein mine follows the lode through the same crawl as Lode Draw (64 blocks within 8, the lode's `c:ores/<kind>` tags) and breaks through the player edit checks; it was a 250-block same-block scan of an 8-block box |
| 8 | Taught (R2 P10) | The Artificer's `staff` lesson (ring thresholds and gifts, Lode Draw and Hewing, the pickaxe form) teaches it. No advancement for a first lode |
| 9 | Living Land (`hemomancy.manipulation.living_land*`, `item.hemomancy.memory_living_land`, `message.hemomancy.living_land.verbs_only` / `.refused.*`, `entity.hemomancy.living_land_strike`, loom recipe `memory_weaving/memory_living_land`, Mnemonist inquiry; Liber `manipulations/pages/living_land`) | A Mediocritas Animus memory with nothing to cast: held Projection wakes a floor, wall or ceiling patch that sends columns of its own blocks into the nearest enemy in front of it (30 mL/s), or rises into a step, ledge or spur when none is near. Every block goes back; it refuses wards, rite spaces, Saint chambers, contents and protected ground with an action-bar reason before any blood is paid |
| 9 | Venous Cling (`hemomancy.manipulation.venous_cling*`, `item.hemomancy.memory_venous_cling`, `message.hemomancy.venous_cling.*`, `blood_source.hemomancy.venous_cling`, loom recipe `memory_weaving/memory_venous_cling`, Mnemonist inquiry; Liber `manipulations/pages/venous_cling`) | A Humilis Animus memory with nothing to cast: Projection into a wall or ceiling the player touches roots them (6 mL), then 4 mL/s on a wall and 6 under a ceiling. Walking crawls along it; jumping, crawling onto the ground, a broken face or short blood lets go. A new third-person pose and roots at hands and feet |
| 9 | Hematic Cleats (`item.hemomancy.hematic_cleats`, `tooltip.hemomancy.hematic_cleats`, recipe `hematic_cleats`) | Boots that halve Venous Cling's upkeep (CB-21, 2026-11-13; was a quarter) and bring a wall to near walking pace; plain hematic iron boots otherwise |
| 9 | Holdfast morphling | Its Cling now holds only a sturdy face the body touches (the shared cling rules), not any collision |
| 9 | Taught (R2 P10) | The Mnemonist's `land` lesson (Living Land, Blood Eclipse's field of dark, Hematic Beacon) and the Artificer's `climb` lesson (Venous Cling, the cleats, the Tendon Line) teach it. No advancement for a first raise or cling |
| 10 | Food and regeneration (`effect.hemomancy.well_rested`, `message.hemomancy.daily_loop.meal` / `.meal_temper` / `.rested`, blood source `blood_source.hemomancy.meal`; Liber `the_hematic_order/pages/the_body_and_the_day`; wiki Blood-Systems "The body and the world's answer") | Regeneration follows the food bar: none starving, half hungry, x1.5 with a full bar and 10+ saturation; a full night's sleep gives Well Rested (+1x base for 5 minutes). Meat pays blood on eating by nutrition (48 mL a steak); the Order's meats pay more and carry a little Temper; tainted meat pays 60 (was 40, paid by the item itself; `blood_source.hemomancy.tainted_meat` removed) |
| 10 | The world's answer (`world_answer.hemomancy.band.*`, `block.hemomancy.humoral_barometer.readout` rewritten, `block.hemomancy.ossuary_clock.readout_with_omen` / `.omen.*`, `block.hemomancy.suspended_vivianite.ward`, `block.hemomancy.stained_sod`, `material.hemomancy.suspended_vivianite.desc` rewritten; Liber `the_hematic_order/pages/the_world_answers`; Alchemist inquiries `humoral_barometer`, `suspended_vivianite`, Vicar inquiry `ossuary_clock`) | Blood left on the ground is counted per region and fades over two days; it raises the Blood Moon's chance (now by moon phase: none at the new moon) and the Wills', draws vampire bats to wet stains and drowned to strong chum, and turns grass under old stains to Stained Sod in a heavy region. The barometer's needle and the clock's lunette show it. Suspended Vivianite (new recipe) halves it. Blood Moon frequency changes from a flat 1 in 7 to about 1 in 14 nights in a quiet world |
| 10 | Taught (R2 P10) | The Vicar's `day` and `answers` lessons and the Hermit's `answer` lesson (the count, the Blood Moon, the bats) teach it; no bestiary page per answer |
| 11 | Liber pages reachable (`LiberEntryDefinitions.CORE_LOOP_PAGES`, `LiberKnowledgeHelper.unlockForDegree`, `LiberDiscoveryEvents`) | The 24 Liber pages Tracks 1 to 12 wrote were never registered, so `MemoBookFilter` hid them from every player; now they open by Degree (D1: The Two Verbs, Living Blood, The Body and the Day, The Draw and the Mark, The Ground Network, Fed Ground, Chum; D2: Temper, Unread Humours, The World Answers; D3: The Menagerie, Living Veins and Basins), by picking up the item each teaches, and by its core-loop first |
| 11 | Core-loop firsts (`advancements.hemomancy.shaped`, `set_through`, `meat_is_blood`, `carried`, `kept`, `cut_and_lit`, `first_crack`, `clinging`, `first_ring`) | Nine advancements, each granted where the act happens and each opening its Liber page: a first paid Shaping, scraped clot, a paying meal, a dissolved catalyst, a leash, a lit gouge run, a lash crack, a cling root, the Staff's first ring |
| 11 | Wills fill Temper (Liber `temperament/pages/temper`, wiki Blood-Systems) | Taking a faltering Will whole by Absorption leaves 10 Temper per tier in its school, beside the +3 alignment it always gave |
| 11 | NPC lessons (`hemomancy.vicar.neophyte.absorption_hint.fresh`, `hemomancy.mnemonist.slots.shaping`, `hemomancy.alchemist.enzyme_uses.temper`, `hemomancy.anchorite.inquiry.pattern.temper`, `hemomancy.artificer.item_inquiry.hematic_cleats.*`; `hemomancy.anchorite.inquiry.brazier.line1` and the D1 absorption hint rewritten) | The Vicar teaches drawing a reservoir while it is fresh; the Mnemonist what a Shaping is; the Alchemist catalyst worth and order; the Vein-Mason that an active scar steadies its temper; the Artificer answers a Hematic Cleats inquiry. One speaker at a time, to `docs/NPC_VOICES.md` |
| 11 | Verb descriptions (`hemomancy.manipulation.blood_projection.desc`, `.blood_absorption.desc`) | No longer "readies the staff"; they name the verbs and the verb key |
| 11 | One word for each (`screen.hemomancy.harbinger_assignment_ledger.first_projection.title` "The First Crossing", `.blood_beneath.revelation`, `.step.fungal_survey_visit.desc`, `message.hemomancy.possession.elsewhere`, `message.hemomancy.cardinal_rite_completion_effects.apotheosis_has_not_been_chosen_witness`, `hemomancy.mnemonist.chamber.guided.unavailable`, `hemomancy.alchemist.fungal_survey.visit`; wiki The-Qliphoth, Harbinger-Path) | Projection is the verb; the Fungal Spine's trip is a crossing in what players read |
| 11 | Dev fixtures (`/hemo temperpool carried|dry|restore`, `/hemo tank clot|clear`, `/hemo vat fill|clear`, `/hemo gouge light|clear`; round two P11) | Dev runs only; documented in `docs/TESTING.md` |
| R2 P4 | Second-wave Shapings (`hemomancy.manipulation.<id>.shaping.project` for `sanguine_mending`, `crimson_flame_conjuration`, `furnace_veins`, `glacial_grasp` (rewritten for both halves), `glacial_rampart`, `ferric_rampart`; `.shaping.absorb` for `glacial_grasp`, `carrion_communion`; `message.hemomancy.furnace_veins.nothing_to_burn`, `message.hemomancy.ferric_rampart.too_many`; Liber `manipulations/pages/second_shapings` "Mending, Fire and the Dead" and `second_shapings_walls` "Ice and Iron Walls", opened by the seven memory items; wiki Blood-Systems) | Seven memories shape a verb and keep their R casts: mending dropped tools, golems and anvils; kindling candles and campfires, priming TNT; blood as furnace fuel only while held; open water frozen into a melting path ahead of Glacial Grasp's plant, and ice thawed by Absorption; ice and iron walls drawn along a sweep; Mortem Temper and health, never blood, drawn from flesh, bone and skulls |
| R2 P4 | Taught (R2 P10) | The Mnemonist's `shapings` lesson (mending, kindling, Furnace Veins, Carrion Communion, the thaw) and the Vein-Mason's `walls` lesson (ice and iron ramparts) teach them. No advancement for any of the seven |
| R2 P5 | Second-wave strands (`hemomancy.manipulation.<id>.shaping.project` for `gloam_laceration`, `cruor_javelin`, `blood_needle_lance`, `blood_needle_fan`, `synaptic_jolt`, `prismatic_reproof`, `ferric_spikes`, `conductive_mark`, `insatiable_hunger`, `grave_debt`; `crimson_flame_conjuration` and `glacial_grasp` rewritten for the plant and the stain run; Liber `manipulations/pages/second_strands` "More Strands", opened by the eleven memory items; `strand_forms` points to it; wiki Blood-Systems "Strands") | Eleven memories put out a strand on held Projection and keep their R casts; Blood Binding brands what it holds; a plant sunk into a wet stain runs along it and dries it |
| R2 P5 | Strand skills (`skill.hemomancy.skill_long_strand`, `skill_taut_line`, `skill_free_hand`, `skill_whipcrack`, `skill_deep_brand`, `skill_second_strand` and their `.desc`; the same Liber page) | Long Strand, Taut Line and Free Hand on the surface at D2 and D3; Whipcrack and Deep Brand at D5 and Second Strand at D6 on the deep layer. Free Hand now lifts a strand's pace (CL-25) |
| R2 P5 | Taught (R2 P10) | The Artificer's `strand_line` lesson names the skill line and the twin; the Mnemonist's `strands` lesson the second-wave strands, the stain run and the binding brand |
| R2 P6 | The vessel's screen (`container.hemomancy.specimen_vessel`, `screen.hemomancy.specimen_vessel.*`, `message.hemomancy.menagerie.quirk.*`) | An empty hand on a kept creature opens a screen instead of printing chat lines: the creature in its glass, Vigor, keeper, buffer and feed price, its blood (hollow unread phials, then its Tempers, then a draw's yield and its quirks once studied) and its brood. A hand that holds something keeps its own use |
| R2 P6 | Sampling in glass (`message.hemomancy.menagerie.syringe_sample`; `item.hemomancy.living_syringe.restricted_samples` rewritten) | A vial on a thick-hided kept kind is refused at no cost; the Living Syringe draws any kept creature for 3 Vigor (a vial 5), under the keeper's rights |
| R2 P6 | The Living Bestiary (`bestiary.hemomancy.section.creatures`, `bestiary.hemomancy.status.met`, `.read`, `.studied`, `bestiary.hemomancy.creature.*`, `screen.hemomancy.bestiary_tab_view.creature_counts`) | Every kind with a blood profile the player has sampled, kept, jarred, bred or seen through glass is listed under Met Creatures, its blood unread until examined, its yield and quirks shown once studied; roster specimens gain the same blood block. Unmet kinds are never named |
| R2 P6 | The brood (`message.hemomancy.menagerie.brood.*`; advancement `hemomancy/first_brood` "Of Your Own Keeping"; Liber `the_hematic_order/pages/the_brood` "The Brood", opened at the Third Degree, by the Specimen Vat item and by the advancement; `unread_humours` amended) | Two kept creatures of one kind side by side, one keeper, both above 75 Vigor, bear a young into an empty vessel beside them after the keeper presses Let them breed: 30 mL a health point (at least 150) over 120 fed seconds from their buffers. The core loop now has 28 registered Liber pages and 10 firsts |
| R2 P6 | Taught (R2 P10) | The Vicar's `keeping` lesson teaches the brood and the vessel's screen; the Alchemist's `humours` lesson teaches sampling in glass and the wider Bestiary. The Alchemist's vat inquiry is unchanged |
| R2 P7 | Strain beds (`block.hemomancy.<strain>_strain` x8, `block.hemomancy.salt_stained_timber`, `item.hemomancy.spores.substrate.flesh` / `.brined` / `.common` on every spore, `item.hemomancy.salt_stained_voyager_log.tooltip.culture`, `material.hemomancy.ravenous_heartwood.desc` rewritten, `material.hemomancy.salt_stained_timber.desc`; advancement `hemomancy/first_strain_bed` "A Bed of Strains"; Liber `the_hematic_order/pages/strain_beds` "Strain Beds", opened at the Third Degree, by the eight spores, Ravenous Heartwood, the timber, the two crops and Tainted Harvest, and by the advancement; `fed_ground` amended) | A spore plants as a strain on its own wood and grows only where blood reaches; ripe it returns its spore and often a second. Ravenous Heartwood and the new Salt-Stained Timber (from the voyager's log) are the flesh and Brined substrates. The core loop now has 29 registered Liber pages and 11 firsts |
| R2 P7 | Catalyst crops and Tainted Harvest (`block.`/`item.hemomancy.glowroot`, `.emberpod`, `item.hemomancy.tainted_harvest` and `.tooltip`, `material.hemomancy.glowroot.desc`, `.emberpod.desc`, `.tainted_harvest.desc`) | Glowroot and emberpod grow on fed ground and are weak Lux and Flammeus catalysts; a ripe crop on over-fed ground can drop Tainted Harvest |
| R2 P7 | Floor spikes and chum hunters (Liber `strain_beds`, `chum` and `the_world_answers` amended) | A drip raises a scab spike from bare floor that never meets the tip; a strong pool draws a Brined Votary in home waters, a Bog Revenant in a swamp, a drowned elsewhere |
| R2 P7 | Taught (R2 P10) | The Alchemist's `beds` lesson teaches strains, the timber and the crops, and their new item inquiries (timber, glowroot, emberpod, Tainted Harvest) answer for them; the Vicar's `answers` lesson teaches the hunters. The voyager log's five inquiry lines are unchanged |
| R2 P9 | Sixteen core-loop firsts (`advancements.hemomancy.<id>.title` / `.description` for `first_drip`, `basin_harvest`, `tainted_harvest`, `chum_catch`, `in_glass`, `studied`, `living_land_strike`, `full_night`, `moon_brought_on`, `carrion_drawn`, `twin_strand`, `stain_run`, `bound_and_branded`, `field_of_dark`, `carried_crossing`, `stoked`), each opening its Liber page; Brought On and Carried by the Vein wear the goal frame (CN-6, 2026-11-13; Along the Red waits on NQ-03-6), pinned by `CoreLoopTeachingGuardrailTest` | The firsts the rest of the core loop and round two never had; 27 in all |
| R2 P9 | The forty older Liber pages (CQ-1): thirty Liber Sanguinium pages (the overview, the eight school pages of the Manipulations and Temperament chapters, memory weaving, Canon and Thelemic memories, Blood Temperament, both Resonant Forge pages, bloodwork stations, the Sanguine Condenser, the three morphling pages, shared mob materials, Annetta Knowles) and ten Liber Immaculatus pages, registered with Degree floors (purity and clarity stages for Liber Immaculatus) and openers. Rewritten: the eight Manipulations school pages, the overview, Blood Temperament, the Sanguine Condenser, Recording a Pattern; "-tended" became "-tempered" on two Temperament pages | Pages written before the core loop are readable at last, and say what the overhaul made true: every memory by school with its Shaping or strand and no mL price (CQ-47: Axonal Transduction's "150 mL per entry" went 2026-11-13, pinned by `LiberPageRegistrationGuardrailTest`), Temper beside Temperament, clot in the Condenser and the Forge's basin |
| R2 P10 | NPC lesson trees (`CoreLoopLessons`, `CoreLoopLessonDialogue`, route `CoreLoopLessonRoutes`; lang `hemomancy.<speaker>.craft.*` and `hemomancy.dialogue.<speaker>.option.craft.*`; briefs in each speaker's brief file) | Twenty-one lessons, each an unread lore card in the speaker's Conversation box: the Hermit 2 (blood awake, Degrees 0 to 4), the Vicar 4, the Alchemist 5, the Mnemonist 3, the Artificer 4, the Vein-Mason 3 (from the fourth Degree). Asking any branch opens the lesson's Liber pages (source DIALOGUE, now registered on every core-loop page) and nothing else. Six new item inquiries: Salt-Stained Timber, glowroot, emberpod, Tainted Harvest (Alchemist), the Catalyst Feeder (Artificer), the Venous Basin (Vein-Mason) |


## Walk of the tables against what was built (2026-11-10)

Track 11 closed the run by checking every row above against the build log and the code. "Built" names where; "Held"
means the old behaviour still wins and a test says so; "Open" is in `docs/OPEN_QUESTIONS.md` §1.8 or the build log's
closing section.

### Degree gates

| Gate | Verdict as built |
| --- | --- |
| D1 → D2, 500 mL absorbed | Proof kept. The Vicar's D1 hint now has a second line: draw a reservoir while fresh; set blood comes out as Formations the ledger does not count (Track 11). Playthrough proof unchanged. |
| D1 → D2, Cauldron Formation | Unchanged proof; the Condenser now also condenses clot (Track 3). The Vicar's lesson already names the cauldron; the Living Blood page (D1) teaches clotting. |
| D2 → D3, sampling and Centrifuge | Sampling costs 2 health (vial) or 1 (syringe) and a 30 s cooldown (Track 5, not the doc's 5); the Centrifuge no longer mints blood (Track 3). Proofs pass in the playthrough suite. |
| D2 → D3, Alembic tinctures | Tank size kept (Track 3). |
| D2 → D3, Concentrated Blood and sleep | Well Rested is additive; the gate's check is untouched (Track 10). |
| D2, Rooted / Earthen Vein lesson | Done (round two P8): a living vein joining two Earthen Veins carries travel for 125 mL, and the Vicar's `rooted_vein.recovery` line now says the Earthen Vein is the first living vein and what tissue grown from the Third Degree adds; his Earthen Vein inquiry and the material atlas line no longer say veins carry workshop blood. |
| D3 → D4, Loom and anchors | The Loom keeps no tank (Track 3); no rite anchor holds block blood, so none needed a hold. |
| D3 → D4, Pallid Vessel | Safe: gourds are living. |
| D4 → D5, scar pattern | Alignment unchanged; scars gain the Temper role (Track 4) and the Vein-Mason now says so (Track 11). |
| D5, strain from casts | Built: every 80 ticks of paid Shaping or strand work counts as one ordinary use, strain included (Track 1). |
| D4 → D5, First Culture | Held: the Lantern is re-housed with the same id, recipe and advancement (Track 7). |
| D5 → D6, Quintessence, Bloodwell | Checked: Quintessence comes from rites and Saints (items; escrow never clots); the Bloodwell mirrors the bloodline pool, living kin, not a block tank. Neither clots. |
| D7 → D8, Gardens "projection" | Built: "crossing" for the Spine's trip in everything a player reads (Track 11). |
| D7 → D8, Apotheos rite repair | Held: rites claim first in both chains (`ShapingGuardrailTest`, `shaping_validation`). |
| D8, power lock and hunger pin | Built: `core_loop_validation.theNinthPomeLockHoldsAShapingAndSpendsNoTemper` (no blood, no Temper, no work, no first); `BloodRegenRulesTest.theApotheosHungerPinRegeneratesAtTheBaseRate` (food 17, saturation 0 is plain). A strand uses the same gate. |

### Systems tangled with the verbs

| System | Verdict as built |
| --- | --- |
| Cardinal Rites | Held: claim before any Shaping; a sigil-wins GameTest for every Shaping (`FirstConversionsGameTests`). |
| Blood Structures, Working crafts | Held: same chain order; escrow is items and NBT and never clots. |
| Saints | Held by order (`ShapingGuardrailTest`, `SaintCombatKitGuardrailTest`); no Saint GameTest (no cheap fixture; Tracks 1 and 12 deferred it). Living Land and Venous Cling refuse in a ward (GameTests, Track 9). |
| Vesper | Unchanged. |
| Wills | Held (a faltering Will takes the projection first, GameTest). Will absorption now fills Temper (Track 11). |
| Drudges, SSC | The SSC buffer is 600 mL (Track 3). A Drudge as a living vessel is not built; E-4 stays open. |
| Bloodline pool | Unchanged. |
| Morphlings, Brined Court | Unchanged. |
| Circus (Crossbar, Spindle) | Checked: the Marionette Crossbar is its own use channel, not a verb; strands end a Marionette like any cast (Track 12). |
| Chamber visits | Left out on purpose: the active verb is a preference and Temper is an attachment, not inventory, so a visit neither snapshots nor touches them. |
| Unstained | Not built: no cleansing answer to stains, gouges or vats; it waits for the Unstained ritual system (D-11). |
| Thelemic Memories | Exempt from Temper (Track 4). |
| Heartwood, Bloodwood | Unchanged; the feeding core was not made a living vessel. |
| Blood Moon | Built (Track 10). |
| Combat casts (Track 12) | Built: a strand hit opens the cast scope, ends a Marionette, counts toward the D5 strain and falls under the D8 lock. |
| Vesper, Wills, Drudges (Track 12) | Built: every new memory has a `VesperManipulationCoverage` row; no entity cast reaches a Shaping (`ShapingGuardrailTest.noEntityIsRoutedThroughAShaping`). |

### Teaching surface

| Surface | Verdict as built |
| --- | --- |
| `en_us.json` | Verb, Shaping, Temper, fresh and clotted are keys; the lines that taught selection were rewritten (Track 1); the verb descriptions and the Spine's crossing were fixed (Track 11). |
| NPC lessons | One line or inquiry each for the Vicar, Alchemist, Mnemonist, Vein-Mason and Artificer (Track 11), beside the item inquiries Tracks 3 to 10 wrote. Round two P10 added a lesson tree per system: 21 lessons across the Hermit, Vicar, Alchemist, Mnemonist, Artificer and Vein-Mason (`CoreLoopLessons`), each a lore card whose branches open its Liber pages; every core-loop page is taught by at least one. |
| Dialogue inquiry files | Retired items have none left; the Flask, Lantern, gourd seeds, vat, tainted meat, barometer, vivianite, clock, memories and cleats have inquiries. |
| Liber | 24 new pages, all now registered and openable (Track 11); a "Shaping," line on every converted memory's page; `CoreLoopTeachingGuardrailTest` holds both. The thirty older Sanguinium pages and ten Immaculatus pages were registered in round two P9 and the drifted ones rewritten; `LiberPageRegistrationGuardrailTest` holds every page of both books registered. |
| Advancements | No existing advancement describes a retired mechanic (checked). Nine core-loop firsts added (Track 11), two in round two P6 and P7, sixteen in P9: 27 in all. |
| Skills | The cadence skills were reworded (Track 0) and Sanguine Reach widened (Track 1); the projection strand skill line was built in round two P5 and the Artificer teaches it (R2 P10). |
| Wiki | Blood-Systems, Harbinger-Path, Clinical-Blood-Tools and The-Qliphoth updated per track. |
| Route fingerprints | Regenerated where dialogue changed: Track 1 (the verbs left `equippedManips`), Track 7 (the "nearby" normaliser, golden unchanged), Track 11 (the guided-chamber line's "crossing" only). |

### Tests and frozen files

| Item | Verdict as built |
| --- | --- |
| Playthrough suite | Run after every track: 8 required failures throughout, the same set as the pre-run baseline (Track 1 recorded it). |
| Source-text tests | Updated with each move; the impact map regenerated each track. |
| Frozen files | `registry-ids.txt`, `creative-tab-order.txt`, `payload-registry.txt` (`PROTOCOL_VERSION` 47), route and command fingerprints, allow-lists: each change recorded as intended in its track. Track 11 changed only the route fingerprint's two guided-chamber lines. |
| Guardrails to add | `ShapingGuardrailTest` (1), the living-vessel walk (3, `everyReservoirClotsOrIsLivingOrKeepsNoTank`), `TemperGuardrailTest` (4), the block census (9, `living_land_validation`), and `CoreLoopTeachingGuardrailTest` (11). |
| Guardrails to extend | `PlayerBlockEditGuardrailTest` (0), `BloodAccountingGuardrailTest` (3, 5, 7), `CombatTargetingGuardrailTest` (12), `RetiredNamesGuardrailTest` and `PackageLayoutGuardrailTest` (11). |
| `/hemo` test fixtures | Built first as `HemoTestScenario`s in `gametest/CoreLoopTestScenarios`; round two P11 moved them into four dev-only `<Name>Subcommand` classes (`temperpool`, `tank`, `vat`, `gouge`) over `common/command/CoreLoopFixtures`, with the command tree and fingerprint goldens grown on purpose. |

### Docs

| Item | Verdict as built |
| --- | --- |
| CL rows in `OPEN_QUESTIONS.md`, suite linked from `docs/README.md` | Done (§1.8; the README's topic table). |
| Reference, topic docs, station upgrades | Each track wrote its dated top section; Track 11 fixed the stale §8.2 count, §19 source contracts, §21.5 rows, §32.2, the Tap in §38 and §9.1. §5.4, §25.2, §30, §31.2 and the Saints line in §38 are older drift with no core-loop tie and were left. |
| `LORE_REFERENCE.md` | §4.1: why blood dies outside a body, why a creature's blood carries a school, why scars hold Temper steady. |
| `AGENTS.md` | Not edited (the owner's instruction file); the lines are in the build log's closing section (CQ-13). |
| `CORE_LOOP.md` | Written. The track files stay in `docs/archive/tracks/core_loop/` until the owner's review (CQ-14). |
