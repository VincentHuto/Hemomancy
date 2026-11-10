# Core loop overhaul: work paths

Date: 2026-11-09. Status: **proposal, nothing approved, no product code written.** This suite turns the 2026-11-09
design conversation into staged tracks. It says what each idea needs, what the code already gives it, and what has to be
changed, added or rewired. Line numbers and counts come from seven read-only audits run the same day; re-check them
before relying on one.

## The problem

Blood is a mana bar. A Harbinger presses R, a number goes down, an effect happens. Projection and Absorption, the two
things a Harbinger physically does, are mostly used to fill machines and feed rites. Manipulations read as a list of
unrelated powers. The mod has almost no tie to farming, husbandry, fishing, mining or villages, and the world does not
answer blood use. Much of that is baggage from the first implementation.

## The three rules

Every track serves one of these. A feature that breaks one is out of scope.

1. **Two verbs.** Projection pushes blood into the world and Absorption pulls it back. They are the mechanical powers.
   A selected manipulation can be a **Shaping**: it changes what the verb does to the thing the player is aiming at
   (a block, water, a mob, an item on the ground, open air). Rites, Saints and Wills keep first claim on a channel;
   then the Shaping; then the plain verb, which behaves as it does today.
2. **Living blood.** Blood is fuel only while something alive holds it: the player, a worn gourd, a bound creature, a
   living plant or vein. In an inert container it stays fresh for a short window after the player last fed it, then
   clots. Clotted blood is not fuel; it is Sanguine Formations, a crafting material. There is no blood battery and no
   blood-as-RF.
3. **Temper is carried, not just earned.** Temperament, the permanent alignment, stays as identity (gates, affinity, lessons). Beside
   it sits **Temper**: a small spendable pool per school, filled by absorbing from living creatures or dissolving a
   catalyst, burned by Shapings and casts. Running dry is never a refusal; it costs more blood and weakens the effect.
   Scars slow the burn.

"Shaping" and "Temper" are working names (`CL-3`, `CL-9`). "Working" is taken by Working crafts and "Reserve" by four
existing things.

## Tracks

| # | Track | File | Depends on |
| --- | --- | --- | --- |
| 0 | Groundwork: bugs, unguarded edits, dead code the overhaul would otherwise build on | [00a-groundwork-cleanup.md](00a-groundwork-cleanup.md) | none |
| 1 | Verb framework: targeting, reach, Shaping hooks, payment, verb selection | [01-verb-framework.md](01-verb-framework.md) | 0 |
| 2 | First conversions: Lignum Mortis, Pyretic Forge and the brazier, Tendon Line, Blood Binding | [02-first-conversions.md](02-first-conversions.md) | 1 |
| 3 | Living blood: clotting, small buffers, owner draw, Formation economy | [03-living-blood.md](03-living-blood.md) | 0 |
| 4 | Temper: the spendable pool beside Temperament, catalysts, scars | [04-temper-and-catalysts.md](04-temper-and-catalysts.md) | 1 (charge point), 3 helps |
| 5 | Menagerie and research: binding, vats, sampling cost, the Microscope's job | [05-menagerie-and-research.md](05-menagerie-and-research.md) | 2 (Binding), 4 |
| 6 | Ground network: stains, gouges, living veins, gutters and basins, catalyst feed, Lux and Tenebris | [06-ground-network.md](06-ground-network.md) | 1, 3 |
| 7 | Farming and water: drip, planting basins, the cloche, crops, chum | [07-farming-and-water.md](07-farming-and-water.md) | 1, 3, 6 (basins) |
| 8 | Living Staff and mining: pickaxe form, staff growth, crawl mining | [08-living-staff-and-mining.md](08-living-staff-and-mining.md) | 1, 2 (Lignum crawl) |
| 9 | Living Land and wall cling | [09-living-land-and-cling.md](09-living-land-and-cling.md) | 1 |
| 10 | Daily loop and the world's answer: food, sleep, moon, stains that draw attention, villages | [10-daily-loop-and-world-answer.md](10-daily-loop-and-world-answer.md) | 3, 6 for the stain parts |
| 11 | Progression, teaching, tests and docs: the ledger every other track writes into | [11-progression-teaching-tests.md](11-progression-teaching-tests.md) | runs beside all |
| 12 | Strand combat: held Projection as a swung weapon (lash, jet, spray, plant, brand), Absorption riders | [12-strand-combat.md](12-strand-combat.md) | 1; 4 and 6 optional |
| 12a | The lash spike: a throwaway build that tests whether a swung lash is fun. Plan only, not built | [12a-lash-spike.md](12a-lash-spike.md) | none |

Tracks 12 and 12a were added later on 2026-11-09 as review drafts, after the owner asked how projection-based attacks
could work. They change one earlier recommendation: see `CL-20` against `CL-1`.

Nine design plates in [images/](images/00-visual-language.md) illustrate the rules and loops. They are authored
diagrams, not in-game art, and their numbers are this proposal's values.

## Suggested order

- **Stage A, the spine:** 0, then 1, then 2. After Stage A the two verbs do four new things and the player can feel
  the idea. This is the point to stop and play before committing to the rest.
- **Stage B, the economy:** 3, then 4. These change what blood and temper are worth. They break the most progression
  proofs, so Track 11's gate table is worked in the same sessions.
- **Stage C, the world:** 6, then 7, with 5 once 4 is in. This is where the mod meets farming, animals and water.
- **Stage D, tools and spectacle:** 8 and 9, any time after Stage A.
- **Stage E:** 10. The food and sleep half has no dependency and can ship early as a cheap win.
- **Combat, beside the stages:** the spike in 12a can run at any time, before Stage A if wanted, because it does not
  use Track 1. Track 12 itself comes after Stage A and only if the spike says the lash is worth it. Its Jet, Spray,
  Plant and Brand forms do not depend on that verdict.

One Gradle build at a time (parallel sessions share build dirs), so tracks run one after another even where the table
says they are independent.

## What stays

- `BloodAccounting.pay` / `payUpkeep` with named `Source` constants as the only way a player pays blood, ending in
  `BloodVolumeEvents.syncVolume`.
- R casting for combat (recommended; see `CL-1`). `BloodManipulation.tryPerformAction` and its gates are not rewritten.
  Track 12 proposes that some combat manipulations also get a held strand on Projection (`CL-20`); under that proposal
  R still keeps every burst cast and every converted manipulation keeps its R cast (`CL-21`).
- The order of early claims in both channel chains: initiation, Saint router, Wills, Cardinal Rite activation and
  cancellation, `WorkingCraftRites.hintAt`, block endpoints for rites and structures.
- `ProjectedBloodSink` and `IBloodReservoir` as the way projected blood reaches a block entity.
- The Suture routing data (`BloodRoutingSavedData`, `DirectBloodLinkData`) and its Degree 3 and Degree 5 steps.
- Temperament (permanent alignment), its gates (`Humilis`, `Mediocritas`, avatar), scars' transient alignment, and the
  Brazier offering lifecycle for scar teaching.
- First Draws, First Separation and every `PendingDelivery` claim in `AGENTS.md`.
- Hemorath's arena and the Saint complexes' use of gutters and basins (`SaintArena.set/schedule/revert`,
  `HemorathEntity.standsInGutter`).

## What goes or changes shape

- Tendon Line and Memory Thread as items (Track 2, Track 6). Their offering jobs need replacements first (`D-21`).
- Selection deciding project versus absorb on the Living Staff (`LivingStaffUtilitySelectionRules`) (Track 1).
- `GroundBloodDrawPacket` and the Left Alt ash-ignition path (Track 0 guards it, Track 6 retires it).
- Large inert tanks as storage: the 30,000 mL SSC, 10,000 mL Tap, Flask and Jug as carried bulk (Track 3).
- Blood made by machines from nothing: Centrifuge +250 per spin, Alembic +100 per distillation (Track 3).
- `BloodFormationKeyPressPacket` as the cheap Formation source (Track 3).
- Free, unlimited sampling (Track 5).
- The Mycelial Lantern's current housing and feed model (Track 7; the block id and recipe type are kept).
- Anything deleted moves to untracked `removed_assets/`.

## Shared rules for every track

- Read `AGENTS.md` and `docs/architecture/ARCHITECTURE_STANDARDS.md` first. Function-first packages; lang keys only;
  particles through `HemoParticleData`; render types cached; world drawing from `WorldEffectDispatcher`.
- The mod is not released: no migrations, no aliases. Old state may be lost in dev worlds.
- Every world edit a player causes goes through `common/util/PlayerBlockEdits`. Track 0 widens that helper and its
  guardrail before any track adds edits.
- A new payload bumps `PacketHandler.PROTOCOL_VERSION` (40 on 2026-11-09) and rewrites `payload-registry.txt`. Prefer
  no new serverbound payloads: the verbs already ride vanilla use-item. Track 12's snap is the one proposed exception
  (`CL-22`).
- A static per-player map registers with `TransientServerStateEvents`. State that must survive a chunk unload goes on
  an entity or block attachment or in saved data, never in a static map.
- Visuals follow `docs/MANIPULATION_VISUALS.md`. Overlays on the ground stay thin and partly clear (no thick strands);
  focal moments are bold.
- "Nothing Shaping-like selected" must behave exactly as today. The playthrough suite holds the hand tools without
  selecting anything and is the main behaviour pin.
- Each track updates its teaching surface (lang, Liber, NPC lessons, advancements, wiki) and logs it in Track 11.
- No commits; leave work in the tree.

## Owner decisions

Asked here once; each track repeats the ones it needs. My recommendation is in brackets.

| Id | Question | Track |
| --- | --- | --- |
| CL-1 | Does R stay for combat casts, or does every manipulation end up going through the verbs? [R stays; only world-facing manipulations become Shapings] | 1 |
| CL-2 | How does the player choose the verb once selection is the Shaping? [A verb toggle held in player state; the hand tools and Staff read it; Projection and Absorption leave the radial] | 1 |
| CL-3 | Name for a manipulation used as a verb mode. [Shaping] | 1 |
| CL-4 | Does a Shaping run ahead of machines and reservoirs when it claims the target, or only when nothing else took the blood? [Ahead, when it claims the target] | 1 |
| CL-5 | Fresh window and clot rate for inert containers. [5 minutes fresh, then 10% of the remainder per minute] | 3 |
| CL-6 | Flask and Jug: clot like any container, or cut? [Clot; they become the way to carry blood to a job or to make Formations] | 3 |
| CL-7 | A machine whose owner is offline or out of range. [It stalls and its buffer clots; that is the rule working] | 3 |
| CL-8 | May a player absorb from another player's machines and vats? [Yes outside a bloodline only if a config flag allows it; default owner and bloodline only] | 3, 5 |
| CL-9 | Name for the spendable temper pool. [Temper] | 4 |
| CL-10 | Does Temper apply to R combat casts or only to Shapings? [Both, one rule: never refused, dry costs more and hits softer; combat multipliers start mild] | 4 |
| CL-11 | What death does to Temper. [Lose half] | 4 |
| CL-12 | Is an unexamined creature's blood weaker, or only unlabelled? [Only unlabelled; research is knowledge, not a gate] | 5 |
| CL-13 | Cap on bound creatures and vats. [No hard vat cap, upkeep is the limit; one led creature at D3, two at D5, three at D7] | 5 |
| CL-14 | Do living veins move blood, or only signals and catalysts? [Blood, short runs, small flow, dormant when the owner is away] | 6 |
| CL-15 | The cloche: a new block, or the Mycelial Lantern re-housed? [Re-house the Lantern: same id and recipes, new body and feed] | 7 |
| CL-16 | Pickaxe form tier and whether Staff forms take enchantments. [Iron-equal at bond, diamond-equal by growth, never netherite; no enchantments] | 8 |
| CL-17 | Living Land's school. [Animus primary, Ferric secondary] | 9 |
| CL-18 | Do food and sleep feed blood regeneration? [Yes] | 10 |
| CL-19 | Villages: is the Parish idea in scope for this overhaul? [Defer; write it as its own design after Stage C] | 10 |
| CL-20 | Does held Projection become a weapon? This softens `CL-1` to "R keeps bursts; manipulations about motion also get a strand". [Yes, if the lash spike is fun] | 12 |
| CL-21 | Does a converted manipulation keep its R cast as a quick flick? [Keep it through the first wave; decide after play] | 12 |
| CL-22 | Left click while a strand is out as a snap (the suite's one new serverbound payload). [Yes; a mouse reversal stays as the free version] | 12 |
| CL-23 | The first strand a Harbinger meets: a new weak Animus Humilis lash, or nothing until Searing Lash? [A new Animus lash that teaches the motion] | 12 |
| CL-24 | Plain Absorption's damage gets an owner (aggro, kill credit). Does a kill by draw also pay the kill bounty? [Owner yes; no bounty on a draw kill] | 12 |
| CL-25 | Movement speed while a strand is out. [Spray 1.0, Lash 0.8, Plant and Brand 0.6, Jet 0.5; a skill lifts them] | 12 |
| CL-26 | Absorption specialises by the school state on the target (one rider table), not by per-manipulation hooks. [Yes] | 12 |
| CL-27 | A second strand from the off hand as a late skill. [Defer] | 12 |
| CL-28 | Rename "tendency". **Ruled 2026-11-09 and carried out:** the eight kinds are **Tempers**, a player's permanent profile is their **Temperament**, and the spendable pool of `CL-9` keeps the name **Temper**. Code, data keys, lang, the Liber chapter and live docs were renamed in one sweep (`EnumBloodTemper`, `BloodTemperament`, `/hemo temper`). Dated records under `docs/archive`, `docs/superpowers` and `docs/audits` keep the old word. | all |

Existing open questions this overhaul answers, makes moot or collides with are listed by id in Track 11.

## Where the evidence is

The seven audit reports, and the two later ones behind Track 12, live in the session transcript of 2026-11-09, not in
the repo. Each track's "What the code already gives" or "What exists" section carries the facts a builder needs from
them.
