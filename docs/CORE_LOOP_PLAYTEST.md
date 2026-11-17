# Core loop play-test script

Date: 2026-11-11 (core-loop round two, P12). For the owner's first played pass over the core loop overhaul, both rounds.
Every visual was already proven by a review client and every rule by a GameTest; what neither can tell is how it feels.
This script turns the feel questions from both build logs, the open CQ rows and the owner answer sheet into fourteen
sittings, each about 20 to 30 minutes.

## How to use it

- **One sitting per section**, in the order below (later sections lean on earlier ones). Write findings under each
  question as you go, then put rulings in `docs/OPEN_QUESTIONS.md`, the one list of open questions since 2026-11-14.
- **Status (2026-11-14).** The owner answered the question sheet on 2026-11-13 and an overnight build followed. A
  question below whose id is under "Rulings that bind" in `docs/CORE_LOOP.md` is answered; only its feel is left to
  judge. Lines that say "Ruled" or "since 2026-11-14" were updated on 2026-11-14. Sitting 5 was not (see its note).
- **Two ways to look.**
  - *Stills*: each section's review client places a scene, takes its shots into `<dir>/screenshots`, logs
    `<NAME>_REVIEW captured N` and stops itself. It is for looking, not playing. Start one with
    `./gradlew.bat run<Row>` (the row name with its first letter capitalised, for example `runVerbReviewClient`). When a
    new `"kind":"startup"` line appears in `<dir>/operator/journal.jsonl`, drop one op file into `<dir>/operator/inbox/`:
    `{"op":"create","name":"<user>","seed":"1"}` on a fresh directory, `{"op":"open","name":"<user>"}` on a reused one.
    Only one Gradle process at a time on this machine, review clients included.
  - *Play*: `./gradlew.bat runClient`, a new world with cheats on. Play in Survival (blood is only paid and Temper only
    burned outside Creative; Creative also reads every creature's blood). Use the `/hemo` commands below to skip the
    early Degrees; they are operator-only, and the four fixture branches exist only in development runs.
- **Keys.** Verb key `Y` (turn the cell hand to the other verb; sneak to dispel it), manipulation wheel `V`, cast `R`,
  use (right mouse) to hold a verb, left click while a strand is out to snap it.

### Common setup for a play world

Run these once, as yourself, at the start of a sitting (each takes an optional player at the end):

```text
/gamemode survival
/hemo blood activate
/hemo degree set 6
/hemo blood setmax 12000
/hemo blood fill
/hemo skills setpoints 60
/hemo temperpool carried
```

Then learn what the sitting needs with `/hemo manipulations add <id>` (the ids are listed in each section) and spend
skill points in the skills tab of the Field Notes (`B`). `/hemo manipulations level <0-4>` sets mastery; `/hemo temper
<temper> <value>` sets Temperament alignment (the permanent profile), which is not the Temper pool. Give yourself items
from the creative inventory before switching to Survival, or with `/give @s hemomancy:<id>`. The Temper vials show on
the HUD only while a `hemomancy:titration_bandolier` is worn in its Scarlet Vanity slot (ruled 2026-11-13; the item is
creative-only until CQ-63 is answered), so give yourself one.

For a pass that feels the early game, skip the shortcut: `/hemo degree set 1` and play up.

### Suggested order

| # | Sitting | Needs |
| --- | --- | --- |
| 1 | Verbs and the radial | nothing |
| 2 | Living blood | 1 |
| 3 | Temper | 1 |
| 4 | Shapings, first and second wave | 1, 3 |
| 5 | Lashes, held memories and the skill line | 1, 3 |
| 6 | The ground network | 1, 2 |
| 7 | Farming and water | 2, 6 |
| 8 | The menagerie | 3, 4 |
| 9 | The Living Staff and mining | 1 |
| 10 | Living Land and Venous Cling | 1 |
| 11 | The day and the world's answer | 6, 7 |
| 12 | Teaching: firsts, the Liber, lessons | all |
| 13 | Two clients | 1, 5, 10 |
| 14 | The round-two visual fixes | 4, 9, 10 |

---

## 1. Verbs and the radial

### Stills

`verbReviewClient` (dir `build/verb-client-review`, user `VerbReview`, 7 shots): plain Projection, a Shaping claiming
stone and passing dirt, an Absorption Shaping on items, the verb key turning a held hand, a claimed Shaping in third
person, and the Staff drawing from a pig on the active verb.

### Fixture

No fixture. Common setup, then `/hemo manipulations add lignum_mortis` and `add pyretic_forge`, and give yourself a
`hemomancy:living_staff`.

### Try, in order

1. With an empty main hand press `Y`: the active verb's cell hand is conjured (its own price and cooldown). Press `Y`
   again: the hand turns to the other verb. Sneak and press `Y`: it is dispelled with a refund.
2. Open the wheel (`V`). Projection and Absorption sit on the inner ring beside Conjure Staff. Pick Absorption: the
   verb changes, the selected memory does not, and with an empty main hand the Absorption hand appears.
3. Select Lignum Mortis on the wheel, hold Projection on a log: the label by the crosshair names the Shaping and its
   price, and four ticks brighten while it claims. Look at dirt: the ticks dim and the plain verb runs.
4. Press the cycle key: it steps your equipped memories and skips the verbs (round two P3, task J).
5. With the Staff in hand, tap use briefly (20 ticks or less, no blood moved): your Primal Morphling is called, if one
   is equipped. Hold it: the Staff channels the active verb whatever is selected.
6. Adventure mode (`/gamemode adventure`) and hold Lignum Mortis on a log: the reticle shows the barred bone mark and
   "Refused", the action bar says why, and no blood or Temper is taken.

### Feel and balance questions

- Does `Y` doing three jobs (conjure, turn, dispel) read, or would you rather the wheel's inner ring be the only verb
  switch? (CL-2 ruled; this is about the key; `VerbSelection.press`.)
- Is the idle reticle faint enough not to clutter, and the claimed one bold enough to notice? (`VerbChannelOverlay`)
- Is 5.5 blocks of bare reach right against the Staff's 6 plus 1.5 per Living Conduit? (`VerbReach.of`;
  `SanguineProjectionTargeting`)
- The Staff's 20-tick tap for the Morphling: too short, too long? (`LivingStaffItem.MORPHLING_TAP_TICKS` 20)
- Ruled 2026-11-13 (CN-1): the cycle key keeps skipping the verbs. (`ManipulationEquipHelper.isVerb`)

### Known limits

- Other players' streams and selections are only right while they channel (round two P11, CQ-56); see sitting 13.
- The radial has no Shaping preview; the HUD label is the only one.

---

## 2. Living blood

### Stills

`livingBloodReviewClient` (dir `build/living-blood-client-review`, user `LivingBlood`): a Scriptorium's blood bar
just fed, turning, clotting and clotted through, each with its tooltip; the Vascular Status Gauge on a sutured, stalled
machine; a clotting flask's tooltip. `condenserReviewClient` (dir `build/condenser-client-review`, user
`CondenserReview`) shows the Sanguine Condenser. `visualFixesReviewClient` (sitting 14) shows the stall pip.

### Fixture

`/hemo tank clot` stands an Enzymatic Scriptorium two blocks ahead, fed 600 mL a full clot ago (all clot, no fresh
blood); `/hemo tank clear` removes it.

### Try, in order

1. `/hemo tank clot`. Read its blood bar and hover it. Hold Absorption on it: the clot comes out as Sanguine
   Formations (one per 400 mL), a smear under 400 is wiped.
2. Place a fresh Scriptorium, project into it, and watch the bar over five minutes: bright, then dulling, then a clot
   band rising (the clot reads the game time, which neither `/time add` nor sleeping moves, so wait it out).
3. Suture the machine to yourself (blood routing opens at the third Degree; you are at the sixth) and walk 20 blocks away:
   the bar shows a 7 by 7 broken-stitch pip with a pulsing gold halo on its top edge; hover it for the reason ("more than sixteen blocks away").
   Use the Vascular Status Gauge on it: it names the same reason.
4. Fill a Bloody Flask at the Alembic, carry it for ten minutes, drink it: fresh share as blood, clot as Formations.
5. Hang a scab spike (`pointed_blood_scorched_scab`) under a filled tank over a cauldron: the drip seats a Sanguine
   Condenser owned by the tank's owner; scrape it empty and it goes back to a cauldron.
6. Press the Formation key: 300 mL for exactly one Formation.

### Feel and balance questions

- Is five minutes a working window or a timer that nags? (CL-5; `CoagulationRules` fresh window 6,000 ticks)
- Does the colour drain read in small bars, and is the clot band visible on a short bar? (`BloodVolumeBarWidget`)
- Built 2026-11-15 (CQ-6, LP-3, NQ-01-2): while a tank holds clot, half of the blood projected in wakes it, 2 mL of
  clot for each mL spent; a top-up only blends the tank's age. Does reviving feel worth it next to scraping? Try
  `/hemo tank clot`, then project into it and read the Gauge. (`CoagulationRules.pour`, `living_blood` server config)
- Built 2026-11-15 (LP-4, NQ-01-7): a sutured buffer clots and its suture wakes the clot from you at 4 mL per mL, about
  15 mL a minute for an idle 600 mL buffer. Is that upkeep noticeable but fair? (`BloodRoutingHelper`)
- Since 2026-11-15 (UN-55) the stall pip is 7 by 7 with a pulsing gold halo. Does a stall now read without a hover?
  (`BloodVolumeBarWidget`)
- The two free Formation sources (the crafting recipe, blood-stained stone at the Alembic) are closed since 2026-11-14
  (LP-1), and a bleeding mob sheds a Formation on 0.02% of its ticks (LP-8). Are Formations too scarce at D1 to D3,
  before the first Condenser?
- The 300 mL Formation key: right against the Condenser's 150 / 125 / 100 per Formation? (`BodyFormationDraw`;
  `SanguineCondenserRules`)

### Known limits

- No hopper extraction of clot outside the Condenser; the Vitality Chalice is unbuilt; a bloodline member does not
  stand in for an offline owner (CN-9, ruled 2026-11-13, not built yet).
- The recipes that spend Formations were never rebalanced for living blood (CB-28, ruled 2026-11-13, not built yet).
- A scab spike drips from any tank above it, with no draw-rights check on the spike (CQ-3, ruled 2026-11-13, not built
  yet).
- Only the worn gourd grows or feeds its wearer since 2026-11-14 (FX-1); a gourd in the pack or hand does neither.

---

## 3. Temper

### Stills

`temperReviewClient` (dir `build/temper-client-review`, user `TemperReview`, 6 shots): the phials under the blood bar
at mixed fills, only the schools seen, a dry Blood Shot with "Dry of Animus", a cast paid by a catalyst, and the
Scrying Temper tab.

### Fixture

`/hemo temperpool carried` (every school at the largest cap), `/hemo temperpool dry` (every school empty),
`/hemo temperpool restore` (puts back the pool the first of them replaced). `/hemo temper get` reads Temperament.

### Try, in order

1. `/hemo temperpool dry`, `/hemo manipulations add blood_shot`, cast it on R at a pig: "Dry of Animus", 1.25 times
   the blood, 0.9 times the hit. Nothing is refused.
2. Put a `vivacious_enzyme` in the off hand and cast again: the enzyme dissolves into Animus and the cast is tempered.
3. Hold a Shaping dry (Lignum Mortis on a log): 1.5 times the blood, three quarters of the work.
4. Draw from a cow with plain Absorption: Animus fills, 1 Temper per mL drawn, up to the cap.
5. Use the Scrying podium's Temper tab with a catalyst picked up and dropped: the preview follows the pool within a
   second (round two P3).
6. Die: the new body keeps half of each school.
7. `/hemo temperpool restore`.

### Feel and balance questions

- Is the dry penalty felt or ignored? A new player casts dry from the first cast. (CL-10; `TemperRules` Kind, dry
  1.25 / 0.9 and 1.5 / 0.75)
- Ruled 2026-11-13 (LP-14): Temper burn will follow price, 0.5 to 2 a second for held forms and 2 to 8 for R casts.
  Not built yet: today every Shaping and lash burns 1 a second whatever its price. (`TemperRules`, 0.5 per 10 ticks of
  cadence)
- Do scar tiers 2 and 3 mean a school never runs dry? The refill is 0.1 x tier a second since 2026-11-14 (LP-13; it
  was 0.25 x tier). (`TemperRules` scar refill, floor 5 x tier)
- Ruled 2026-11-13 (CQ-9): a full body will still draw Temper from a creature, and the blood is lost. Not built yet.
  (`BloodAbsorptionChannelRules` full-body stop)
- Coal and charcoal are worth 1 Temper each since 2026-11-14 (LP-15; they were 3), so one coal covers two Pyretic
  Forge pulses. Keep or tune? (`data/hemomancy/temper_catalyst/flammeus.json`)
- Catalysts are picked by best fit since 2026-11-14 (CB-19): the smallest that covers the shortfall, the off hand
  winning a tie. (`TemperCatalysts`)
- Are the phials legible at a glance, and is the dry notice too frequent? (`TemperMarksOverlay`; the notice at most every
  2 s)

### Known limits

- No JEI or tooltip line for a catalyst's worth; the Liber's Temper page and the school pages list them.
- Dry strength reaches school damage and Shaping work only.

---

## 4. Shapings, first and second wave

### Stills

`shapingsReviewClient` (dir `build/shapings-client-review`, user `ShapingsReview`, 10 shots): the rot crawling and
felling, raw iron smelting on the ground, sand heating to glass, a log drawn from a brazier, the Tendon Line thrown and
hauled. `secondShapingsReviewClient` (dir `build/second-shapings-client-review`, user `Shapings2Review`, 19 shots):
mending a pickaxe, a golem and an anvil; kindling a campfire and candles; a furnace bank on one stream; a frozen path
and a thaw; swept ice and iron walls; Carrion Communion drawing flesh and a bone block.

### Fixture

No fixture. Learn: `lignum_mortis`, `pyretic_forge`, `tendon_line`, `blood_binding`, `sanguine_mending`,
`crimson_flame_conjuration`, `furnace_veins`, `glacial_grasp`, `glacial_rampart`, `ferric_rampart`,
`carrion_communion`, `vital_effusion`.

### Try, in order

1. Lignum Mortis on a tree: the rot crawls while blood flows (16 mL/s); let go and it falls.
2. Pyretic Forge on dropped raw iron, then on sand: it smelts in place; note the small forge sunk under the items and the
   heat tendril (round two P11).
3. Tendon Line: Projection on a high wall throws it, Absorption hauls you up. Stray past a third further than the throw
   and it snaps.
4. Sanguine Mending on a dropped damaged tool, an injured iron golem, a chipped anvil (eight pulses beat it back a stage).
5. Crimson Flame Conjuration near unlit candles and a campfire: two lights a pulse within 2 blocks. Aim at TNT.
6. Furnace Veins on a furnace with ore in it and two more beside: one stream burns the bank. Let go: the fires die.
7. Glacial Grasp on open water: a path of Frozen Cruor that melts back to water. On a creature it closes a hand of ice
   (rebuilt 2026-11-12; it no longer plants a strand on dry ground). Hold Absorption on the ice to thaw it.
8. Glacial Rampart and Ferric Rampart: sweep Projection along the ground; the 13th iron segment is refused unpaid.
9. Carrion Communion: hold Absorption toward rotten flesh, bones and a bone block: Mortem Temper and a little health,
   no blood.
10. One dry pass of any of them (`/hemo temperpool dry`), and one in Adventure mode to see the refusal before any price.

### Feel and balance questions

- Shapings undercut their own R channels 6 to 12 times. Ruled 2026-11-13 (LP-11): they may, and the channels are
  repriced. Not built yet. (prices in each `*Shaping` class, table in the round-two closing section)
- Furnace Veins' bank: ruled 2026-11-13 (CQ-15) to be priced per furnace fed. Not built yet: today one price feeds the
  whole bank. (`FurnaceVeinsShaping`, radius 2, at most 8, 15 ticks of burn a pulse)
- Carrion's yields: a bone block gives 9 Mortem Temper since 2026-11-14 (CQ-16; it was 24). (`CarrionRules`,
  `HEAL_PER_REMAIN` 1)
- Should Sanguine Mending heal by `canHarm` and include the mod's own constructs? (CQ-17;
  `#hemomancy:mendable_constructs`)
- Glacial Grasp's R cast leaves water like the Shaping since 2026-11-14 (CQ-19). (`TemporaryIceManager`)
- Is the quarter floor on a dry wall's standing time right? (P4 verifier; `ShapingRules.standingTicks`)
- Brazier yields above vanilla, and free smelting at the brazier with Pyretic Forge selected. Ruled 2026-11-13 (CB-18):
  the ice chain goes to 4:1 and the rest stays. Not built yet. (`data/hemomancy/recipe/brazier_transmutation/`)
- A held session asks protection again on every pulse since 2026-11-14 (CN-13). Hewing checks only the struck block,
  kept as built (CN-14). (`VerbShaping.admits`)
- Ruled 2026-11-13 (CQ-5): the bone-meal and flint brazier recipes get a Shaping, so a held Absorption cannot eat a
  rite's bone. Not built yet. (`data/hemomancy/recipe/brazier_transmutation/`)
- The rot's crawl speed and felling, the haul's overshoot and the cord read end-on. (round one, Track 2; `LignumMortisRules`, `TendonLineRules`)

### Known limits

- Vascular Dowsing's veins toward ore and "Lode Draw follows the veins" are not built (they need an owner-only cue).
- The converted memories keep their R casts through the first play pass (CL-21, ruled 2026-11-13). You decide after it.

---

## 5. Lashes, held memories and the skill line

Rewritten 2026-11-15 (UN-59) for the lash and the held memories as they stand after 2026-11-12, and the skill line
after CQ-20 B and CQ-22 A.

### Stills

`lashTuningClient` (dir `build/lash-tuning-client`, user `LashTuning`) is where the three lashes are felt and their
numbers read. `strandReviewClient` (dir `build/strand-client-review`, user `StrandReview`) still captures its round-two
shot list, which predates the rebuild; use it for the arm poses only.

### Fixture

No fixture. Learn the three lashes `blood_lash`, `searing_lash`, `gloam_laceration`; the eight held memories
`hematic_flare`, `blood_eclipse`, `glacial_grasp`, `cruor_javelin`, `blood_needle_lance`, `synaptic_jolt`,
`crimson_flame_conjuration`, `ferric_spikes`; and `blood_binding`. Spawn husks or zombies in a fenced pen. Unlock the
six strand skills in the Living Staff branch (Long Strand at D2; Taut Line and Free Hand at D3; Whipcrack and Deep
Brand at D5; Second Strand at D6).

### Try, in order

1. The tap. Select Blood Lash and click with nothing held: one quick strike straight ahead.
2. The charged whip. Hold Projection: the lash gathers in the hand, stronger the longer you wait. Flick your aim: it is
   thrown along the stroke, cracks on the first thing it meets and lays the school's state for a long while. Then it
   follows your hand while your view turns only a little. A still lash runs back into the hand; a stroke tires after a
   quarter turn. Click while it is out to snap it down your aim.
3. The three lashes, each for what sets it apart. Blood Lash, the tether: a light, forgiving rope; a charged crack
   holds what it strikes in place, and a landed sweep gives a little blood back. Searing Lash, the heavy whip: a long
   rope that lags and overshoots; a charge widens the crack and sets it alight, and each crack in a row hits harder
   until the whip rests. Gloam Laceration, the razor: three tails land three quick cuts, harder from behind, in the dark
   or on the Obscured; a charged throw cuts through its whole arc, the third tap in a row is a small crack, and other
   players barely see it.
4. The eight held memories, one each, with the attack button where it has a use. Hematic Flare: a searchlight beam that
   Illuminates and outlines; rest it on one body and click to flash. Blood Eclipse: a dome of dark that grows round you
   and moves with you; click to end it in a flash. Glacial Grasp: a hand of ice forms round a creature over a second
   and holds it; click to squeeze. Cruor Javelin: drawn through three stages and thrown on release; it sticks in a
   block, and Absorption takes half its blood back. Blood Needle Lance: needles gather round you, up to six; let go to
   loose them, or click to loose one. Synaptic Jolt: it keeps a run of water, iron, copper or your own Ferric
   constructs live, or jolts a creature each pulse. Crimson Flame Conjuration: it kindles unlit lights near the aim
   first, then paints a line of flame where you look; click to flare the line. Ferric Spikes: a run of blades goes
   through the ground toward your aim; click and it stands up as blades that block the way.
5. Blood Binding on a zombie: it is held and carries your Animus brand, and your Absorption reaches it from twice as far.
6. Unlock the skills one by one. Long Strand: a lash is longer and every held memory reaches further (a tenth a level).
   Taut Line: a lash's tip follows the hand sooner. Free Hand: you walk faster with a lash or a held memory out (a tenth
   a level, never past walking; Synaptic Jolt sets no pace of its own). Whipcrack: a lash's crack and a held memory's
   click hit 15% harder and lay their state 20% longer a level. Deep Brand: a Blood Binding's brand lasts 45, then 60
   seconds. Second Strand with an empty off hand: a second lash trails the first at half damage.
7. One dry pass: `/hemo temperpool dry` and swing a lash.
8. Projection into a machine with a lash or a held memory selected: the machine fills at the plain pace, and nothing
   held opens.

### Feel and balance questions

- Is the lash fun: the tap, the charge, the flick, the capped view, the snap? Tune it in `lashTuningClient`.
  (`LashProfile`, `LashStyle`, `LashTuning`)
- Do the three lashes feel like three weapons, not one recoloured? (`Sculptings` lash rows)
- The held memories' own paces: Hematic Flare 0.85, Blood Eclipse and Crimson Flame 0.8, Blood Needle Lance 0.75, Cruor
  Javelin and Ferric Spikes 0.7, Glacial Grasp 0.6, Synaptic Jolt none (RV-58: they stand; tune them here).
- Should strands out-value their own R casts per mL? (LP-10; prices in `Sculptings`, R costs in `ManipulationInit`)
- Base regeneration (1 mL/s) against held prices of 15 to 60 mL/s. (CB-1; `bloodRegenRate`; `BloodRegenRules`)
- Six skills: does each earn its point now that three of them reach the held memories? (CQ-20 B; `HeldSkills`,
  `StrandSkills`, `LivingStaffSkillBranch` ids 74 to 79)
- The twin: does the trailing lash land at all? Its half-damage sweeps mostly fall inside the lead lash's hurt cooldown.
  (CQ-22 A; `StrandSkills.TWIN_SWEEP` 0.5, `TWIN_COST` 1.5)
- Blood Binding branding every creature it holds. (CQ-24; `StrandService.brand`)
- Six memories are R-only now: Blood Needle Fan, Prismatic Reproof, Hemorrhage, Conductive Mark, Insatiable Hunger and
  Grave Debt (CQ-23 A). Does any of them feel missing a held form?
- A red lash on red floors; the HUD arc. (`StrandRenderer`, `VerbChannelOverlay`)

### Known limits

- A lash is the only strand a memory holds. The jet, spray and plant kits and the stain run stay in code that only
  tests reach, while blood on the ground is explored later (CQ-25).
- In first person an empty off hand draws no arm, so a twin from an empty off hand leaves the lower screen edge.
- Infusion inputs and Blood Structure frames are still claimed by a lash (CL-4).
- A watcher's client reads the caster's unsynced skills for reach and claims (CQ-56); see sitting 13.
- Server cost of many players lashing at once is unmeasured.

---

## 6. The ground network

### Stills

`groundNetworkReviewClient` (dir `build/ground-network-client-review`, user `GroundReview`, 22 shots): stains at three
ages, a dormant and a lit route, run functions, Lux traces, a living vein, a pouring basin, the feeder, Blood Eclipse
snuffing, a hot run burning furnaces, the field of dark from outside and inside, two Earthen Veins joined by a living
vein with "125 mB", and a gouge holding water. `jeiReviewClient -PjeiReviewOnly=gouge_catalyst -PjeiReviewPages=6`
(dir `build/jei-client-review`, user `JeiReview`) shows the Gouge Catalysts pages.

### Fixture

`/hemo gouge light` cuts a five-cell gouge run two blocks ahead and pulses it with light; `/hemo gouge clear`.

### Try, in order

1. Plain Projection at the floor with nothing selected: a wet stain (4 mL). Watch it dry; pour water beside it.
2. Learn `vascular_dowsing`; sweep Projection along a floor and up a wall: a gouge. Project into it (15 mL): it lights.
3. Pulse it with a catalyst in the off hand: glowstone (light), coal (hot: a furnace beside it burns while it holds),
   a compass (point), packed ice (cold), an iron ingot (pull), bone meal (feed), an ink sac (dark). Check JEI's Gouge
   Catalysts category.
4. Put a Catalyst Feeder with coal beside a hot run and walk away: it relights for whoever lit it. Note that the run
   then runs on coal alone.
5. Learn `blood_eclipse`; hold Absorption at candles: they go out and hang a field of dark. Stand in it near a zombie
   five blocks away; then step out.
6. Place two Earthen Veins (stented), join them with a gouge, grow a living vein along it from a Vein Node (Degree 3):
   travel between them for 125 mL.
7. Wall a water source so its only way out is a gouge: the water stops at the gouge.
8. Two Venous Basins pouring into one gutter line: the gutters show the fuller one's stream.
9. In Adventure mode light a hot run beside a campfire: nothing is lit.

### Feel and balance questions

- Stains: clutter or answer? (`GroundRunRules` stain numbers; kill stain 35%, `KILL_STAIN_CHANCE`)
- The pulse's price and shimmer; trace density and life. (`GroundRunRules.PULSE_ML` 15; `LuxTraceShaping`)
- A catalyst feeder keeps a run working with no blood after the first pulse. Ruled 2026-11-13 (CQ-7): it will spend
  blood each time it feeds. Not built yet. (`CatalystFeederBlock`)
- Furnaces per hot run: no cap today. With a coal feeder that is a free furnace bank while you are merely in the level.
  Ruled 2026-11-13 (CQ-43): at most 4 furnaces a run. Not built yet. (`GroundRunRules.HOT_FURNACE_BURN` 15,
  `GroundNetwork.heat`)
- The hung field of dark hides only its caster since 2026-11-14 (CQ-41), from anything more than 4 blocks off. Is that
  still too much stealth for a cheap Shaping? (`DarkFieldRules`, `SchoolAwareness.canPerceive`)
- The field's size and time: 20 ticks per light level, radius 3 to 8, at most 2,400 ticks. (`DarkFieldRules`)
- The carried crossing at a flat 125 mL against 500 and up. (CQ-42; `EarthenVeinTravelRules.LIVING_VEIN_COST`)
- Gouges stopping flowing water like glow lichen. Ruled 2026-11-13 (CQ-44): water will break gouges and not living
  veins. Not built yet. (`BloodGougeBlock` waterlogging)
- Feed runs grow every crop beside them a stage every 30 s since 2026-11-14 (CB-24; it was 6 s), the cloche's own
  pace. Is the cloche still worth building beside one? (`GroundRunRules` feed beat)
- Vein flow (20 mL/s, owner within 32 blocks) against the five-minute window. Ruled 2026-11-13 (CL-14): veins will
  move blood only toward an end that is drawing. Not built yet. (`GroundRunRules`)

### Known limits

- The JEI mirror and the gouge catalyst sync are unproven on a dedicated server (an integrated client skips the
  channel).
- The field's fog was never seen inside a Saint fight or a rite; from outside the field is only a floor wash.
- Two players whose fields share an exact centre share one client cue.

---

## 7. Farming and water

### Stills

`farmingReviewClient` (dir `build/farming-client-review`, user `FarmReview`, 16 shots): a drip ceiling over a field,
planting basins dry and fed, the cloche, Vital Effusion, chum blooming; round two adds the eight strain beds on their
woods, a close view of the flesh and Brined strains, glowroot and emberpod (one under a cloche), floor spikes under
drips, and a Brined Votary rising in a chum pool.

### Fixture

No fixture for crops. For the tank a drip falls from, `/hemo tank clot` gives a clotted tank (no drip); place a fresh
tank or Venous Basin instead.

### Try, in order

1. Gourd seeds on plain soil: no growth, and bone meal refuses. Put a wet stain beside the stem: it grows slowly.
2. A `planting_basin` filled by Projection with wheat in it, and one dry beside it: compare.
3. Scab spikes under a filled basin over farmland: drops feed the soil; over bare stone, floor spikes rise.
4. A `mycelial_lantern` (the cloche) with seeds in its culture slot.
5. Vital Effusion's Shaping on a sapling.
6. Plant each spore on its wood: Vivacious, Fervent and Ruinous on `ravenous_heartwood`; Neurotic, Frigid and Ferric on
   `salt_stained_timber` (craft it from a Salt-Stained Voyager Log and eight logs); Incandescent and Umbral on podzol or
   mycelium. Feed them with a basin beside the wood. Cut one ripe.
7. Craft glowroot (carrot and glowstone dust) and emberpod (nether wart and blaze powder) and grow them in a basin.
8. Harvest a lot of basin-fed wheat: about one in twelve gives a Tainted Harvest. Eat one as a Harbinger.
9. Chum: Projection into water with nothing selected; fish in it. At night, a strong pool in a swamp, in the reef or
   elsewhere draws a hunter.

### Feel and balance questions

- Is a drip ceiling worth building, and do gourds needing blood make the early game a grind? (`BloodFedGrowthRules`
  drip 4 mL, 6,000 ticks)
- Planting basin pace and look; the cloche's speed and price (25 mL a stage, 600 ticks). (`BloodFedGrowthRules`)
- Vital Effusion's price, 15 mL/s. (`VitalEffusionShaping`)
- The substrate split, and Ferric on timber or flesh? (CQ-35; `FungalStrains`)
- Salt-Stained Timber as the reading of the doc's "Salt-Stained Log", and its recipes too cheap? (CQ-36;
  `recipe/salt_stained_timber*.json`)
- Spores are now renewable from one crafted spore. Acceptable? (CQ-39; `FungalStrainRules.BONUS_SPORE_CHANCE` 0.5)
- Tainted harvest at 8%, 12 mL a nutrition point, gouge feed 1,200 ticks. (CQ-37; `BloodFedGrowthRules.TAINT_CHANCE`,
  `loot_modifiers/tainted_harvest.json`, `blood_food/tainted_harvest.json`)
- Catalyst crops for the other six schools, or let the strains stand for them? (CQ-38; `CatalystCropBlock`, `data/hemomancy/temper_catalyst/`)
- Floor spikes: should they do anything? (CQ-40; `BloodFedGrowthRules.FLOOR_GROW_CHANCE` 0.1, `MAX_FLOOR_SPIKE` 3)
- The Brined Votary as the reef's chum hunter. (CQ-34; `WorldAnswerRules.hunterWaters`)
- The chum tint, and how often hunters come. (`ChumRules`; `WorldAnswerRules.HUNTER_POOL_STRENGTH` 0.75)

### Known limits

- The swamp and home-waters biome reading for hunters is proven only by unit rules; a Bog Revenant spawns in the water
  when no bank is within 3 blocks.
- The cloche's crop growing has no JEI page.
- A torchflower crop at age 1 could taint (it reads as ripe).

---

## 8. The menagerie

### Stills

`menagerieReviewClient` (dir `build/menagerie-client-review`, user `MenagerieReview`): vats of a blaze, a cow and a
husk beside a jar, a leashed pig, Absorption on the blaze's vat, a vial's tooltip unread and read.
`vesselReviewClient` (dir `build/vessel-client-review`, user `VesselReview`, 18 shots): a blaze vat's screen unread,
read and studied; a husk; a preserved jar; a cow pair gestating; a refused button; the brood's thread and a birth; the
Living Bestiary's Met Creatures; a Living Syringe drawing a vat.

### Fixture

`/hemo vat fill` stands a Specimen Vat you own, holding a cow at 60 Vigor over 400 mL; `/hemo vat clear`.

### Try, in order

1. `/hemo vat fill`; use it with an empty hand: the screen. Hold Absorption on it: Temper and a little blood.
2. Lead a cow home: throw the Tendon Line into it (Degree 3); hold Absorption on it to reel it in. Put it in a second vat
   beside the first.
3. Feed both buffers by Projection. Press "Let them breed" on one screen; watch the thread arc between the lids; wait
   the two fed minutes for a calf in an empty vat beside them.
4. Sample a kept creature with a vial (5 Vigor), then with a `living_syringe` (3 Vigor). Try a vial on a vatted blaze:
   refused at no cost.
5. Examine samples under the Hematic Microscope until a kind is studied (three individuals or three Vigor levels): the
   screen shows the yield and quirks; the Bestiary lists it under Met Creatures.
6. Leave a vat unfed for a long while: Vigor falls; at zero it is a husk.
7. Dose a passive animal with plain Projection: it goes into season, tainted.

### Feel and balance questions

- The vat economy: about 0.3 Temper per mL fed; how fast unfed vessels die; the rattle. (`VatVigorRules`)
- The leash's hold time and upkeep (a quarter of max health in mL a second). (`MenagerieRules`)
- The brood's price and pace: 30 mL per parent health point (cow 300, blaze 600) over 120 fed seconds; parents at 75
  Vigor; rest 6,000 ticks. (CQ-27; `BroodRules`)
- CQ-27 to CQ-33 are ruled, kept as built (2026-11-13). What is left on those rows is the feel.
- A Sanguine Vigil and an in-range suture can finish a brood with the keeper merely near. (CQ-28, kept;
  `SpecimenBrood.second`)
- Hostile kinds breed (a blaze pair works), on purpose. (CQ-29, kept; `#hemomancy:menagerie_no_brood`)
- When is a kind "met"? (CQ-30; `BloodResearch.recordMet`)
- Quirks as notes, not mechanics. (CQ-31; `KeptQuirks`)
- Syringe 3 Vigor and vial 5. (CQ-32; `VatVigorRules.SYRINGE_SAMPLE_COST`, `SAMPLE_COST`)
- Hollow phials show the humour count before it is read. (CQ-33; `TemperPhials`)
- Ruled 2026-11-13 (CQ-4): only the owner and bloodline may empty, pick up or fill a vessel. Not built yet.
  (`SpecimenVessels`)
- The dose and how fast taint turns a farm feral. (`AnimalTaint`)

### Known limits

- No syringe draw animation on a vessel (the payload names an entity).
- The Living Bestiary's creature preview shows at GUI scale 3 since 2026-11-14 (FX-20); the text column beside it is
  narrow there (108 px).
- A husk or preserved specimen still shows a disabled brood button with its refusal.

---

## 9. The Living Staff and mining

### Stills

`staffMiningReviewClient` (dir `build/staff-mining-client-review`, user `StaffMining`, 15 shots): staffs in frames
from no rings to five, the held Staff young and old, the pickaxe form digging and from the side, the morph into it,
Lode Draw held and released, and Hematic Hewing's tunnel and sneaking face round a chest.

### Fixture

No fixture. Give a `living_staff`, learn `lode_draw` and `hematic_hewing`, find ore.

### Try, in order

1. Conjure the pickaxe form; dig stone (3 mL), ore (6 mL), deepslate ore (9 mL). Run your blood low: it digs like a bare
   hand.
2. Lode Draw held on an ore face: the lode crawls; release breaks what it reached.
3. Hematic Hewing: a tunnel each second; sneak for a three-by-three face; aim steeply for a stair.
4. Spend blood through the Staff, or draw from creatures with it, to watch a ring close. Filling and draining a tank
   grows no ring since 2026-11-14 (CQ-8).
5. Plant the Staff in a running rite: its bands show (round two P11).

### Feel and balance questions

- Ring bands as rings or stripes; the planted Staff's bands. (`LivingStaffRingModel`, `PLANTED_BAND_LIFT` 3 px)
- The pickaxe's feel and price; stone tier at the bond. (CL-16; `LivingStaffGrowthRules.digTier`, `LivingDigRules`)
- Lode Draw's pace; Hewing's stance controls; Hewing at 4 mL a block whatever the hardness. Ruled 2026-11-13 (CB-20):
  2 mL per hardness point. Not built yet. (`StaffMiningRules`)
- Staff rings count only blood spent or drawn from creatures since 2026-11-14 (CQ-8). The Talaromyces vein is ruled
  to pay 6 to 12 mL per extra ore. Not built yet. (`ScarsEntityEventHandler`, `LivingStaffGrowth.record`)
- A Staff weapon form swap burns 1 Temper since 2026-11-14 (CB-25). (`BloodManipulation.chargesTemper`)

### Known limits

- Vascular Dowsing does not pair with Lode Draw yet.
- The Living Crossbow keeps durability; `lastVeinMineStart` still guards veins.

---

## 10. Living Land and Venous Cling

### Stills

`livingLandReviewClient` (dir `build/living-land-client-review`, user `LivingLand`, 31 shots): strikes from floor, wall
and ceiling, raises, the settle, the cling on a wall and under a ceiling, and the cleats.

### Fixture

No fixture. Learn `living_land` and `venous_cling`; give `hematic_cleats` and hematic iron boots, and fit them at an
Armature (a bowl, the boots worn, 250 mL in the tank).

### Try, in order

1. Living Land held on a floor near zombies: columns strike; with nothing near, the floor rises into a step. Let go:
   everything settles. Try it on a wall and under a ceiling.
2. Venous Cling: hold Projection on a wall you touch; crawl up and down; under a ceiling, shuffle. Jump to let go.
3. Cross a wall with the cleats fitted, then turn them off on the radial and cross it again.
4. Tendon Line from the ceiling while clinging.
5. Living Land in a rite space or on ground you may not alter: it says why before any blood.

### Feel and balance questions

- The strike's charge, speed and damage at 30 mL/s; the socket pool and vein net on a busy floor; the raise as a step or
  bridge. (`LivingLandTerrainRules`)
- The cling's crawl and price (4 mL/s on a wall, 6 under a ceiling); the root patches' boldness. (`ClingRules`)
- The cleats' discount, x0.5 (ruled 2026-11-13; was x0.25). (CB-21; `ClingRules`)
- The cleats' worn look. Since 2026-11-15 (CN-10) they are an Armature fitting on the boots you wear, with spurs
  drawn over the boots' own model, and a slice on the radial's inner ring turns them off. (`HematicCleatsModel`,
  `HematicCleatsLayer`)
- Ruled 2026-11-13 (CN-4): Living Land stays a power of its own, not a plant strand. (`LivingLandShaping`)

### Known limits

- The cling's crawl speed is set by the client; the cling HUD shows the root price; there is no restart GameTest.

---

## 11. The day and the world's answer

### Stills

`dailyLoopReviewClient` (dir `build/daily-loop-client-review`, user `DailyLoopReview`, 11 shots): the barometer and
clock quiet, seething and under a Blood Moon; stained sod; a vampire bat drinking stains; a steak's meal line; Well
Rested.

### Fixture

`/hemo bloodmoon summon` and `/hemo bloodmoon cancel`; `/hemo will ambush ...` for a Will. No fixture for regional
pressure: spill blood (stains, kills) in one place.

### Try, in order

1. Eat a steak: +48 mL. Starve: no regeneration. Sleep a full night: Well Rested.
2. Make a mess in one region (many stains and kills) and read a `humoral_barometer` and an `ossuary_clock` there.
3. Hang `suspended_vivianite` and watch the count fall faster.
4. Wait for night near wet stains underground: a vampire bat comes to drink.
5. `/hemo bloodmoon summon` to see the moon; cancel it.

### Feel and balance questions

- Meal values (steak 48, tainted meat 60). Ruled 2026-11-13 (CB-3): tainted meat 10 mL per nutrition and raw fish 4.
  Not built yet. (`data/hemomancy/blood_food/`)
- No regeneration while starving; the Well Rested window. (CL-18; `BloodRegenRules`)
- The regeneration skills were cut on 2026-11-14 (CB-2): Sanguine Surge 1 to 3 mL/s, the Mnemonic Candle 5 mL/s, the
  Rib gourd halved. Do food and sleep matter beside them now? (`SkillPointHelper`)
- How fast a region gets heavy; Blood Moons about one night in fourteen in a quiet world; a mob farm holding a region at
  seething. Ruled 2026-11-13 (CB-26): kills will supply only a capped share of a region's pressure. Not built yet.
  (`WorldAnswerRules`)
- The Blood Moon's 25% discount for every caster everywhere, and its drain. Ruled 2026-11-13 (CB-11): the discount
  goes only to Harbingers in the moon's region. Not built yet. (`ManipulationCostLedger` blood moon 0.75,
  `BloodMoonEvents`)
- Brought On credited to every awake player on counted ground. (CQ-48; `WorldAnswerRules.broughtOn`)
- How often bats and drowned arrive; the bat's feeding pose; the lunette read across a room. (`WorldAnswerRules`
  scavengers)

### Known limits

- Wills read region pressure, not the player's own recent spend; no Tenebris or Lux answer; no spreading sod; no
  bestiary world pages; the Parish (CL-19) is not built.

---

## 12. Teaching: firsts, the Liber, lessons

### Stills

`coreLoopTeachingReviewClient` (dir `build/core-loop-teaching-review`, user `CoreLoopTeach`): a first's toast, every
core-loop Liber page at its first and last leaf, the advancement tab. `liberPagesReviewClient` (dir
`build/liber-pages-review`, user `LiberPages`, 92 shots): the forty older pages of both books and all 27 firsts.
`lessonReviewClient` (dir `build/lesson-review`, user `LessonReview`; a fresh dir first needs one
`{"op":"click","x":213,"y":224}` op on the accessibility screen): every lesson card and node for the six speakers.

### Fixture

No fixture. Start a fresh world for this one, so pages and firsts are unread: `/hemo blood activate`, then
`/hemo degree set 1` and raise it a Degree at a time.

### Try, in order

1. Open the Liber Sanguinium at each Degree: which pages appear.
2. Do one act per first (a first Shaping, scraped clot, a meal, a dissolved catalyst, a leash, a lit run, a crack, a
   cling, a ring, a drip, a basin harvest, a chum catch, In Glass, a study, a strike, a full night, Carrion, a twin, a
   stain run, a binding brand, a field of dark, a carried crossing, a stoked furnace). Watch the toasts and the
   advancement tab. The stain run (Along the Red) cannot be earned since 2026-11-12: no memory plants a strand.
3. Learn a memory with `/hemo manipulations add`: its school's pages open in both chapters.
4. Visit a Hermit's temple and a Harbinger Outpost: talk to the Hermit, Vicar, Alchemist, Mnemonist and Artificer; ask a
   lesson's question; its Liber pages open. From the fourth Degree visit the Vein-Mason at Mason's Respite.

### Feel and balance questions

- The pace of the Liber's reveal, and the older pages' Degree floors. (CQ-46, kept 2026-11-13;
  `LiberKnowledgeHelper.unlockOlderForDegree`)
- 27 firsts in one tab: too many, wrong parents? Brought On and Carried by the Vein wear the goal frame since
  2026-11-14 (CN-6). Along the Red cannot be earned (CQ-45 is open). (`data/hemomancy/advancement/hemomancy/<id>.json`)
- The school pages without prices. (CQ-47, kept 2026-11-13;
  `data/hemomancy/books/libersanguinium/manipulations/pages/`)
- Carried by the Vein granted on payment, not arrival. (CQ-49; `EarthenVeinTravelManager.project`)
- Who teaches what, and lessons as lore cards lighting each speaker's NOTICE sigil until read. (CQ-50, CQ-51;
  `CoreLoopLessons`)
- The Hermit stopping at the fourth Degree; the Vein-Mason starting there. (CQ-52, CQ-53; Degree ranges in `CoreLoopLessons`)
- A lesson opens pages and nothing else. (CQ-54; `CoreLoopLessonRoutes.teach`)
- Successors telling lessons in the original voice. (CQ-55; `en_us.json` `hemomancy.<npcKey>.craft.*`)
- The Artificer's `hot_runs` and the Vein-Mason's `ground_marks.cut` both teach cutting a gouge in near-twin sentences:
  drop one? Two lesson sentences repeat existing text word for word. (P10 verifier; `en_us.json`
  `hemomancy.artificer.craft.*`, `hemomancy.anchorite.craft.*`)
- "Crossing" for the Spine's trip. (round one, Track 11; `en_us.json` `screen.hemomancy.harbinger_assignment_ledger.first_projection.title`)

### Known limits

- Six core-loop pages have no Degree floor (Lignum Mortis, Pyretic Forge, Tendon Line, Brazier Transmutation, Mining by
  Projection, Ice and Iron Walls); learning their memory opens them.
- Advancements earned before P9 do not open their new pages on old dev worlds (no migration).
- Liber Immaculatus pages are registered but their text was not edited; Unstained speakers teach no lessons.
- Every lesson card shows the hub's shared Conversation summary.

---

## 13. Two clients

### Stills

The onlooker review: `pwsh -NoProfile -File tools/review/review-onlooker.ps1` runs one Gradle call, then starts the
host (`onlookerReviewClient`, dir `build/onlooker-host`, user `OnlookHost`, port 25581) and the observer
(`onlookerObserverClient`, dir `build/onlooker-observer`, user `OnlookWatch`) as plain java. The observer logs
`ONLOOKER_REVIEW captured 26`: another player's bare and Staff streams landing at their reach, a selected Shaping's
stream, the lash, jet with its twin, spray and brand with skills, the cling and cleats, the Tendon Line thrown and
hauled, and the field of dark from outside. `-Only cling` reruns the cling scenes.

### Fixture

No fixture. For play, `./gradlew.bat runClient` and a second client on the LAN world (or `runServer` and two clients),
one Gradle process at a time.

### Try, in order

1. One player channels each verb with and without the Staff and a Shaping selected; the other watches where the stream
   lands.
2. Strands with Long Strand and Taut Line on the caster: does the onlooker see the same length and motion?
3. Cling, Tendon Line, a brood thread, a field of dark from outside and inside.

### Feel and balance questions

- Does another player's lash look like a lash (the frames caught it straight)? (`StrandClientState.follow`)
- A remote Shaping's reach scale and claims read the caster's unsynced skills on the onlooker's client, so a strand may
  land a little off. Worth a sync? (CQ-56; `VerbPresence`, `VerbShaping.reachScale`)
- Is the presence resend (every 10 ticks on change) enough on a laggy server? (`VerbPresence.REFRESH_TICKS`)

### Known limits

- No played multiplayer session, latency test or dedicated-server run yet.
- A fast-hauled player leaves a short streak of the hand's ambient cells; the field of dark is only a floor wash from
  outside.

---

## 14. The round-two visual fixes

### Stills

`visualFixesReviewClient` (dir `build/visual-fixes-client-review`, user `VisualFixes`, 20 shots): stains as splashes in
three ages on floor and wall; the cleats beside plain boots and worn; the planted Staff with five rings and none; the
stalled Scriptorium's pip and hover line; the refused and claimed reticle; Pyretic Forge on raw iron and ingots; the
Tendon Line cord from the side and in first person, slack and hauled.

### Fixture

`/hemo tank clot` for a stalled machine to hover. Otherwise as sittings 2, 4, 9 and 10.

### Try, in order

1. Leave stains to dry on floor and wall.
2. Wear the cleats; look at yourself in third person.
3. Plant the Staff in a rite.
4. Walk away from a sutured machine and hover its bar.
5. Hold a Shaping somewhere it is refused.
6. Pyretic Forge on items and on a block; Tendon Line thrown and hauled.

### Feel and balance questions

- The refusal mark: pale bone, a little clear, for the cadence plus 4 ticks. (CQ-57; `ShapingDispatcher.REFUSAL_MARK_GRACE_TICKS`)
- Pyretic Forge without the outpouring, a forge at 0.45 sunk under the items. (CQ-58; `PyreticForgeShaping`)
- The FORGE cue now shrinks below radius 1 for every sender: a Will's attached forge (0.7) and the brazier bowl's (0.8)
  were never seen in a review client. Do they still read? (P11 verifier; `ThermalGeometry`, `EntityCasts`)
- The stain splashes and their ages. (CQ-59; `tools/model_export/ground_network.py` `STAIN_ALPHA`)
- The worn cleats' look. (CQ-60; `HematicCleatsModel`, `living_land_sprites.py`)
- The planted Staff's bands and the stall reason. (CQ-61; `LivingStaffRingModel.withRings`, `ReservoirReadout.Link`)
- The Tendon cord's width and sag. (`TendonLineRenderer`)

### Known limits

- Pyretic Forge has no casting arm in third person, and from behind the player hides the heap.

---

## Checklist

| # | Sitting | Sat (date) | Findings written | Rulings filed |
| --- | --- | --- | --- | --- |
| 1 | Verbs and the radial | | | |
| 2 | Living blood | | | |
| 3 | Temper | | | |
| 4 | Shapings | | | |
| 5 | Strands and the skill line | | | |
| 6 | The ground network | | | |
| 7 | Farming and water | | | |
| 8 | The menagerie | | | |
| 9 | The Living Staff and mining | | | |
| 10 | Living Land and Venous Cling | | | |
| 11 | The day and the world's answer | | | |
| 12 | Teaching | | | |
| 13 | Two clients | | | |
| 14 | Visual fixes | | | |

Where rulings go: `docs/OPEN_QUESTIONS.md`, the one list of open questions since 2026-11-14 (the answer sheet was
answered on 2026-11-13 and is archived). Every number above lives in the code or data path named beside
it; the full table, with what the 2026-11-11 tuning sheet lacks, is in the closing section of
`docs/archive/tracks/core_loop/BUILD_LOG_ROUND2.md`. A number an NPC or the Liber says aloud must be changed in the lang file too.
