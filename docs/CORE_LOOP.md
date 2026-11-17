# The Core Loop

Status (2026-11-14): the 2026-11-09 core loop overhaul is built, all of it in the working tree and uncommitted. Round
one built the twelve tracks (Tracks 0 to 12; the lash spike 12a was a plan and was not built). Round two (packages P1
to P12) made the GameTest suite deterministic, fixed the old-save kick and the first round's known defects, and built
what round one deferred: the second-wave Sculptings, the strand skill line and second-wave strands, the vessel screen and
the brood, fungal strain beds, the field of dark and carried crossings, 16 more firsts, the 40 hidden Liber pages, 21
NPC lessons, seven visual fixes and the onlooker sync. On 2026-11-12 the lash was rebuilt as a tap and a charged whip,
eight held manipulations were rebuilt with mechanics of their own and six lost their held form. On 2026-11-12 and
2026-11-13 the living HUD, the Titration Bandolier, the Sanguine Vigil's model and joined vats were built.
`PROTOCOL_VERSION` is 51 (the cleats toggle, 2026-11-15). The owner answered the question sheet on 2026-11-13, and the overnight build of 2026-11-14
(packages P03, P04 and P05) built the core-loop answers that waited on nothing. The clear rulings for CL-1 to CL-28 and
CQ-1 to CQ-63 are under "Rulings that bind" below. What still needs an answer, what is ruled but not built and what is
owed in play are in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md). The track files stay in [core_loop/](archive/tracks/core_loop/00-README.md)
until the `AGENTS.md` lines are pasted (CQ-13, CQ-14); the two build logs there hold what each track and package built,
deferred and learned. Start a play session from [CORE_LOOP_PLAYTEST.md](CORE_LOOP_PLAYTEST.md).

## What it is

The overhaul turns blood from a mana bar into a material that lives, moves and answers. Three rules carry it:

1. **Two verbs.** Projection pushes blood into the world and Absorption pulls it back. They are set by the verb key,
   not selected. A selected memory can be a **Sculpting**: it changes what the held verb does to the thing aimed at.
   Rites, Saints and Wills claim first, then the Sculpting, then the plain verb. Since 2026-11-12 three combat memories
   put out a **strand**, and every strand is a lash (Blood Lash, Searing Lash, Gloam Laceration): a left-click tap, or
   a held orb thrown by a mouse flick. Eight more hold a mechanic of their own on held Projection (Blood Eclipse,
   Glacial Grasp, Cruor Javelin, Ferric Spikes, Hematic Flare, Synaptic Jolt, Blood Needle Lance, Crimson Flame
   Conjuration). Six lost their held form and cast on R only. R still casts.
2. **Living blood.** Blood is fuel only while something alive holds it. A machine's tank stays fresh for five minutes
   after it was last fed, then clots; a top-up only blends the tank's age. Clot takes room and comes out as Sanguine
   Formations, or Projection and a suture's draw wake it: half of the pour goes to the clot. Gourds, kept creatures,
   living veins and the Gourdvine Tap never clot. A sutured machine drinks from its owner while they are near.
3. **Temper is carried.** Beside Temperament (the permanent profile) each player carries **Temper**, a small pool per
   school, burned by casts and Sculptings, filled by drawing from creatures, kept creatures, Wills, meals and dissolved
   catalysts. Running dry is never a refusal: it costs more blood and lands softer. Scars slow the burn.

Around them: the ground network (stains, gouges, living veins, basins and gutters, catalyst runs), farming and water
(fed ground, scab drips, planting basins, the cloche, chum), the menagerie (leash, Specimen Vat and joined vats,
sampling costs, unread humours, animal dose, the vessel's screen, the wider Bestiary and the brood), the growing Living
Staff and its mining, Living Land and Venous Cling, the body and the day (food and sleep feed regeneration, meat is
blood) and the world's answer (spilled blood counted per region, raising the Blood Moon, Wills and scavengers). The
living HUD shows it: the vessel, the reserve, the Morphling's jar and, with a Titration Bandolier worn, eight Temper
vials. Six outpost speakers teach every one of these systems as a lesson in their Conversation box (round two P10).

## Where it lives

| Area | Code | Data and assets | Tests and review |
| --- | --- | --- | --- |
| Verbs and Sculptings | `common/manipulation/verb/` (`SculptingDispatcher`, `SculptingSessions`, `VerbTargeting`, `VerbReach`, `Sculptings`, `CrawlSession`); `BloodManipulation.tryPerformVerbPulse` | `hemomancy.manipulation.<id>.sculpting.<verb>` lang | `SculptingGuardrailTest`, `sculpting_validation`, `verbReviewClient`, `sculptingsReviewClient` |
| Strands | `common/manipulation/verb/strand/` (`StrandService`, `StrandSkills`, `LashProfile`, `LashStyle`, `LashTuning`, the five rule classes; since 2026-11-12 only the lash kit is on a memory and the jet, spray and plant kits are reached by tests alone); `client/render/world/StrandRenderer`, `StrandSchoolGeometry` | `manipulations/pages/strand_forms`, `second_strands` | `strand_validation`, `strandReviewClient`, `lashTuningClient` |
| Living blood | `common/tile/` (`BlockBloodVolume`, `ReservoirBlood`, `CoagulationRules`, `HeldFormations`, `BloodDrawRights`) | `hemomancy:bottled_at` | `living_blood_validation`, `livingBloodReviewClient` |
| Temper | `common/capability/player/harbinger/temperament/` (`BloodTemper`, `TemperRules`, `TemperCharge`, `TemperCatalysts`) | `data/hemomancy/temper_catalyst/` | `TemperGuardrailTest`, `temper_validation`, `temperReviewClient` |
| Menagerie | `common/entity/menagerie/`, `common/tile/harbinger/menagerie/` (`KeptSpecimen`, `SpecimenVessels`, `VatVigorRules`, joined vats `VatAssembly`; round two: `VesselView`, `BroodRules`, `SpecimenBrood`, `KeptQuirks`), the vessel screen `common/menu/tile/functional/SpecimenVesselMenu` and `client/screen/tile/functional/SpecimenVesselScreen`, `BloodResearch.met` / `recordMet` | tags `entity_type/menagerie_no_brood`, `menagerie_unkeepable`, `menagerie_people`, `menagerie_wills`; Liber `the_brood` | `menagerie_validation`, `BroodRulesTest`, `KeptQuirksTest`, `MenagerieKeepingGuardrailTest`, `menagerieReviewClient`, `vesselReviewClient` |
| Ground network | `common/world/ground/` (with `FurnaceHeat`, `DarkFields`, `DarkFieldRules`), `common/block/harbinger/ground/`; `client/event/UmbralFieldFog`; `compat/jei/GougeCatalystRecipeCategory` | `data/hemomancy/gouge_catalyst/` | `ground_network_validation`, `earthen_vein_travel_validation`, `DarkFieldRulesTest`, `groundNetworkReviewClient`, `jeiReviewClient` |
| Farming and water | `common/world/farm/`, `common/world/chum/`, `BloodFedGrowthEvents`; strain beds in `common/block/harbinger/plant/` (`FungalStrains`, `FungalStrainBlock`, `CatalystCropBlock`), `TaintedHarvestModifier`, hunters in `WorldAnswer.drawHunter` | `loot_modifiers/chum_catch.json`, `tainted_harvest.json`; tags `strain_substrates/*`, `scab_floor`, `chum_hunters`; generator `tools/model_export/fungal_strains.py` | `farming_validation`, `chum_validation`, `daily_loop_validation`, `FungalStrainsTest`, `farmingReviewClient` |
| Staff and mining | `common/item/harbinger/tool/living/`, `common/manipulation/ferric/` | | `staff_mining_validation`, `staffMiningReviewClient` |
| Living Land and cling | `common/manipulation/animus/`, `common/world/livingland/`, `capability/player/harbinger/cling/` | | `living_land_validation`, `cling_validation`, `livingLandReviewClient` |
| Body and the world's answer | `DailyLoopEvents`, `common/world/answer/` | `data/hemomancy/blood_food/` | `daily_loop_validation`, `dailyLoopReviewClient` |
| Second-wave Sculptings | `common/manipulation/{ferric,flammeus,congeatio,mortem}/*Sculpting`, `verb/SweepTrail`, `world/ground/SwitchableLights`, `mixin/core/FurnaceFuelAccessor` | tags `mendable_constructs`, `carrion_remains`, `carrion_blocks` | `SecondSculptingsGameTests` (`sculpting_validation`), `secondSculptingsReviewClient` |
| Teaching | `LiberEntryDefinitions.CORE_LOOP_PAGES` and `OLDER_PAGES`, the "Core loop firsts" in `HarbingerAdvancementGranter`, `LiberKnowledgeHelper.unlockForMemory` / `unlockForUnstained` | 29 core-loop Liber pages plus the 40 older pages registered in round two P9, 27 advancements | `CoreLoopTeachingGuardrailTest`, `LiberPageRegistrationGuardrailTest`, `core_loop_validation`, `coreLoopTeachingReviewClient`, `liberPagesReviewClient` |
| NPC lessons | `common/entity/npc/dialogue/CoreLoopLessons` (the table), `CoreLoopLessonDialogue` (the cards), `route/CoreLoopLessonRoutes` (the event's guard) | `hemomancy.<npcKey>.craft.*` lang and briefs; six round-two item inquiries | `CoreLoopLessonsTest`, `CoreLoopLessonGameTests` (`core_loop_validation`), 63 rows in `route-fingerprints.txt`, `lessonReviewClient` |
| Onlookers | `common/manipulation/verb/VerbPresence` sending `PacketSyncVerbPresence`; `client/data/OnlookerVerbState`; `ClientBridge.onlookerReach`, `onlookerSelection`, `onlookerVerb`; `VerbRefusalPacket` and `client/data/VerbRefusalClientState` for the refused-claim mark | `payload-registry.txt` (protocol 49 when this was built, 51 today) | `OnlookerPayloadsTest`, `onlookerReviewClient` and `onlookerObserverClient` through `tools/review/review-onlooker.ps1` |
| Living HUD | `client/screen/overlay/bloodhud/`, `client/screen/overlay/BloodVolumeOverlay`, `BloodHudLayout`, `HudVisibilityRules`; `common/item/harbinger/tool/TitrationBandolierItem` | `textures/gui/blood_hud/`; generators `tools/gui/living_hud_art.py` and `tools/gui/vessel_frame_seats.py`; the Gradle task `exportHudLayouts` | `HudAssemblyTest`, `HudBindingTest`, `HudHeartbeatTest`, `BloodHudLayoutTest`, `HudVisibilityRulesTest`, `temperRackReviewClient` |
| Visual fixes | `TendonLineRenderer`, `PyreticForgeSculpting`, `HematicCleatsModel` with `HematicCleatsLayer`, `LivingStaffRingModel.withRings`, `ReservoirReadout.Link` with `BloodVolumeBarWidget`, `VerbChannelOverlay` | stain variants from `tools/model_export/ground_network.py`; cleats texture from `living_land_sprites.py` | `visualFixesReviewClient` |
| Dev fixtures | `common/command/CoreLoopFixtures` with `TemperPoolSubcommand`, `TankSubcommand`, `VatSubcommand`, `GougeSubcommand` (`/hemo temperpool`, `tank`, `vat`, `gouge`) | `commands.hemomancy.<branch>.*` lang | `HemoCommandTreeTest`, `hemo_command_validation`, `CoreLoopClosingGameTests` |
| Deterministic GameTests | `src/gameTest/java/.../gametest/GameTestIsolation` and the four `gametest/mixin/GameTest*Mixin`, loaded by `src/gameTest/resources/gametest-isolation.mixins.json` through `gradle/game-tests.gradle` | the all-air template `hemomancy:open_room` | [TESTING.md](TESTING.md) "Deterministic GameTests" |

Mechanics and numbers: the dated core-loop sections at the top of [HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md).
Lore: [LORE_REFERENCE.md](LORE_REFERENCE.md) §4.1. Rules and guardrails: [ARCHITECTURE_STANDARDS.md](architecture/ARCHITECTURE_STANDARDS.md).
Ledger of every player-facing change and where it is taught: [CORE_LOOP_TEACHING_LEDGER.md](CORE_LOOP_TEACHING_LEDGER.md).

## Rulings that bind

A line that ends "Not built yet." is ruled and waits in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) section 3. A line with
no such ending is how the code stands today. The owner's answers for the core loop are in the chunk files
`owner_answers/01-living-blood-and-containers.md` to `owner_answers/05-verbs-sculpting-and-strands.md`.

- **CL-28 (2026-11-09):** the eight kinds are Tempers, a player's permanent profile is their Temperament, and the
  spendable pool is Temper. Pool and profile stay distinct in code and prose.
- **CL-2 (2026-11-10):** the verb is player state set by the verb key (Y), and Projection and Absorption are also verb
  picks on the radial's inner ring, drawn with their hand item icons. A pick sends `BloodVerbChoicePacket` and
  `VerbSelection.choose` sets the active verb, puts the selected memory down (the plain verb) and conjures the hand if the main hand
  is empty. The verbs are never equipped, so the cycle key steps past them (round two P3, task J).
- **Held manipulations (2026-11-12):** a held manipulation is not bound to the five strand forms, and none may be a
  recolour of another. Each has its own mechanic. The same day the owner ruled the lash a left-click tap and a charged,
  flicked whip with a capped view, and approved the eight rebuilds. The detail is in
  [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md). (This ruling has no CL id yet; CL-29 is planned for it,
  UN-60.)
- **CL-9, CL-10, CL-11 (2026-11-13):** the pool is Temper; one Temper rule for R casts and Sculptings, never refused;
  death keeps half. Kept as built. The Temper numbers ruled the same day (LP-13, CB-19, CB-25, CQ-16) are in
  [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md).
- **RV-28 (2026-11-13):** round one's prices stay as first values and are tuned in play.
- **Verbs, Sculptings and strands (2026-11-13):**
  - **CL-1:** R stays for combat casts. A memory with a Sculpting keeps its R cast.
  - **CN-1:** the cycle key keeps skipping the two verbs.
  - **CL-3:** "Shaping" is renamed "Sculpting", mod-wide. Built 2026-11-14 (B01): class names, ids, lang keys, the
    recipe field, the blood source id, the GameTest batch, the Gradle rows, the Liber page ids and the lesson ids all
    follow. No registry id or payload changed. Dated logs, audits, the archive, track files and plans keep the old word.
  - **NQ-05-1, NQ-05-2 (2026-11-13):** the noun is "a Sculpting" (plural "Sculptings"), the verb "sculpts", and the
    first-pulse advancement is "Sculpted" (`hemomancy:hemomancy/sculpted`). "Shape" stays where it means geometry or a
    summon's body.
  - **NQ-05-3 to NQ-05-6 (accepted 2026-11-14):** code names follow (`VerbSculpting`, `SculptingDispatcher`,
    `SculptingGuardrailTest` and the rest); "narrow", "main", "claiming" and "held" keep their words; every id follows;
    only live docs are rewritten.
  - **CL-4:** a claiming Sculpting runs ahead of machines and reservoirs. A block that takes blood is never claimed by a
    lash or the dome.
  - **CL-20:** held Projection stays a weapon.
  - **CL-21:** converted memories keep their R cast through the first play pass. The owner decides after that.
  - **CL-22:** left click snaps a strand, as built. (Since 2026-11-12 the same button is also the lash's tap and the
    accent of a held manipulation; recording the ruling in that wider form is UN-30.)
  - **CL-23:** Blood Lash is the first strand, the teaching lash.
  - **CL-24:** Absorption damage names the drawer. A kill by draw pays no kill blood and no bounty.
  - **CL-26:** one Absorption rider table keyed by school state (`AbsorptionRiders`).
  - **CL-27:** the off-hand Second Strand stays. What it does is an open question (CQ-22).
  - **CQ-21:** Whipcrack and Deep Brand stay at D5 and Second Strand at D6.
  - **CQ-24:** Blood Binding brands every creature it holds.
  - **CN-13:** protection is asked again on every pulse of a held Sculpting. Built 2026-11-14.
  - **CN-14:** Hematic Hewing checks only the struck block in `admits`; later cells are checked as they break.
  - **CQ-19:** Glacial Grasp's R cast leaves water when its Frozen Cruor melts. Built 2026-11-14 for the player's cast.
  - **CQ-41:** a hung field of dark hides only its caster. Built 2026-11-14.
  - **FX-9:** other players see the Tendon Line cord (built in round two P11).
  - **The kept rows** RV-29 to RV-36, RV-40, RV-49 to RV-56, RV-68, RV-69 and RV-75 (the verb key, one reach, the
    Staff tap, the Sculpting homes, the second-wave Sculptings' numbers, the field of dark's numbers) stay as built. They
    are recorded one by one in [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md).
- **Staff, Living Land and cling (2026-11-13):**
  - **CL-16:** the Living Staff digs at stone tier at the bond, iron at ring 2, diamond at ring 4.
  - **CL-17:** Living Land is Animus first, Ferric second.
  - **CN-4:** Living Land is a power of its own, a held channel, not a plant strand.
  - **CQ-8:** the Talaromyces vein pays 6 to 12 mL per extra ore, and Staff rings count only blood spent or drawn from
    creatures. The ring half is built (a tank round trip grows no ring). The vein price is not built yet.
  - **CB-20:** Hematic Hewing costs 2 mL per hardness point. Not built yet.
  - **CB-21:** Hematic Cleats halve the cling upkeep: a wall 2 mL a second, a ceiling 3. Built 2026-11-14.
  - **CN-10:** Hematic Cleats become a fitting the Hematic Armature puts on any Hemomancy boots the player wears,
    toggled from the radial. Built 2026-11-15 (`CleatsFitting`, `ToggleCleatsPacket`, `HematicCleatsLayer`).
  - **NQ-05-7 A (2026-11-14):** the player puts a Hematic Cleats item in an Armature bowl. It is crafted from 2 scrap,
    4 bone and a Sinew Cord without boots, and is no longer wearable. Built 2026-11-15.
  - **NQ-05-11 A (2026-11-14):** fitting needs only the base Armature, no new Degree gate, and the blood of a hematic
    iron upgrade (250 mL). It gives no armor-upgrade practice toward the Consecration kits. Built 2026-11-15.
  - **NQ-05-8 (2026-11-14):** the fitting is the `hemomancy:cleats` data component on the boots. It survives repair, is
    carried across the Armature's lineage upgrades and cannot be removed. Built 2026-11-15.
  - **NQ-05-9 (2026-11-14):** the item tag `hemomancy:takes_cleats` names the nine blood-side boots, Silent Archon's
    among them. Unstained boots are left out. Built 2026-11-15.
  - **NQ-05-10 (2026-11-14):** off means no discount, no faster crawl and no spurs drawn. The state is saved on the
    boots. One new serverbound payload, `ToggleCleatsPacket` (protocol 50 to 51). Built 2026-11-15.
  - **NQ-05-12 (2026-11-14):** the worn look is spurs and studs drawn over each boot's own model, in the cleats' iron,
    sinew and bone. No copper. The whole-boot model is gone. Built 2026-11-15.
- **Living blood (2026-11-13):**
  - **CL-5:** a tank stays fresh 5 minutes after it was fed, then 10% of what is fresh clots each minute, and none is
    fresh an hour after feeding. Kept as built.
  - **CQ-6, LP-3:** clot takes room in a tank, and while a tank holds clot 50% (a config value) of blood projected in
    goes to reviving the clot (built 2026-11-15, see NQ-01-2).
  - **LP-4:** sutured buffers follow the same clot-revive rule, with a cheaper revive so suturing is worth it (built
    2026-11-15, see NQ-01-7).
  - **CL-6:** Flask and Jug clot and Blood Rock does not; all three give less and less when several are used quickly
    (built 2026-11-15, see NQ-01-9).
  - **CL-7:** with its owner offline, dead or out of reach, a sutured machine stalls and its buffer clots. Kept as
    built.
  - **CN-9:** a bloodline member may stand in for an offline owner at a sutured machine (built 2026-11-15, see
    NQ-01-16).
  - **CL-8:** only the owner and their bloodline may draw from a placed machine or vat; others only behind
    `bloodline.foreignReservoirDraw` (off). Kept as built. A backlash on a stranger's draw is a possible later feature,
    not now.
  - **CQ-3:** a scab spike drips only from tanks its placer may draw (built 2026-11-15, see NQ-01-15).
  - **CQ-4:** only the owner and bloodline may empty, pick up or fill a specimen vessel (built 2026-11-15).
  - **RV-47:** a Condenser made by a drip takes the tank owner's crystallization while that owner is online, and 0
    otherwise. Kept as built.
  - **LP-1:** no recipe makes a Sanguine Formation. The shapeless craft and the Blood Stained Stone distillation are
    removed (built 2026-11-14). The gourd fill recipes were left as they are.
  - **LP-5:** Hematic Iron Scrap distils to 1 powder (built 2026-11-14).
  - **LP-8:** a bleeding mob sheds a Formation on 0.02% of its ticks (built 2026-11-14).
  - **LP-15:** coal and charcoal give 1 Flammeus Temper each (built 2026-11-14).
  - **FX-1, CN-12:** only the worn gourd grows, feeds its wearer or gives regeneration; a carried gourd does neither
    (built 2026-11-14).
  - **FX-2:** every Flask and Jug is stamped `bottled_at` when it is made: loot, gifts and the machine slot (built
    2026-11-14).
  - **FX-3:** nothing to do. The Mycelial Lantern spends its own tank, not a player's blood.
  - **CQ-5:** the bone meal and flint brazier recipes need a Sculpting (built 2026-11-15, see NQ-01-12).
  - **CB-18:** the brazier's ice chain is 4:1; the other brazier yields and Pyretic Forge's 15 mL smelting stay (built
    2026-11-15, see NQ-01-13).
  - **CB-13:** a Lantern enzyme costs 150 mL (answered 2026-11-14, built 2026-11-15).
  - **CB-14:** rendering stays free: 25% for blaze powder, scrap, packed ice and spider eye; 10% for bone, charcoal,
    coal, glowstone dust, ink sac and rotten flesh (answered 2026-11-14, built 2026-11-15).
  - **CB-16:** a tincture jug costs 4,000 mL for 6 doses (built 2026-11-14).
  - **CB-17:** `memory_blood_rush` costs 100 mL and 2 enzymes (built 2026-11-15, see NQ-01-18).
  - **CB-28:** the recipes that spend Formations are rebalanced now that none are free (built 2026-11-15). Six crafts
    changed: a blank Hematic Memory takes four Blood Stained Stones and a Neurotic Enzyme (8 Formations in all to 4);
    the Charm of Vascularium, Morphling Jar, Sanguine Conduit and Self-Reflection Mirror take 2 Formations (from 4);
    the Vitality Chalice takes 2 (from 3). Tinctures, gourd fills, grafts, Blood Structures, rites and Working crafts
    and the other crafts keep their counts. The sheet lists each recipe's old and new count
    (`owner_answers/01-T11-formation-price-sheet.csv`).
  - **NQ-01-20:** one Formation is worth 150 mL at D1 to D2 and 100 mL from D3, the Condenser's own prices (answered
    2026-11-14, built 2026-11-15).
  - **NQ-01-1, LP-2:** the gourd fill stays at 4 Formations for 200 mL, a loss on purpose (accepted 2026-11-14, kept by
    the Formation pass 2026-11-15).
- **Living blood (2026-11-14 answers and defaults, built 2026-11-15):**
  - **NQ-01-9:** repeated Rock, Flask and Jug drinks give 100, 80, 60, 40, then 25%, read from the Blood Drunkenness
    level the drinker already has. Three minutes without a drink resets it. The lost share is gone; a clotted share
    still breaks out as whole Formations.
  - **NQ-01-10:** Blood Drunkenness is that counter and keeps its +15% cast cost a level.
  - **NQ-01-11:** a suture draws only from the worn gourd. A carried gourd still pours in a machine slot, fills at the
    Gourdvine Tap and takes the crafting refill.
  - **NQ-01-12:** bone meal needs Vital Effusion selected, flint Pyretic Forge. Every brazier transmutation now names a
    Sculpting.
  - **NQ-01-13:** packed ice takes the bowl's ice and 3 more ice in the off hand, blue ice the bowl's packed ice and 3
    more packed ice; all are spent on completion and the blood stays 30 and 60 mL. A recipe field `catalyst_count` says
    how many.
  - **NQ-01-14:** Morphic Nectar turns 1 Hematic Iron Scrap into 1 powder.
  - **NQ-01-15:** a hanging spike's placer is kept in a chunk attachment keyed by the spike's root. A spike with no
    placer drips only from tanks nobody owns. A block added below a spike keeps its first placer.
  - **NQ-01-16:** the stand-in works only while the owner is offline: kin within 16 blocks of the machine, alive, D3 or
    higher, blood awake, paying from their own blood above the 50% floor. The Gauge and the bar say kin is feeding it.
  - **NQ-01-18:** `memory_blood_rush` costs Animus 1 and Flammeus 1.
  - **NQ-01-19:** breaking a stranger's specimen vessel stays possible.
  - **NQ-01-2 (answered 2026-11-14, built 2026-11-15):** while a tank holds clot, half of each pour goes to reviving
    it. Each mL of that half is used up and wakes 2 mL of clot. Server config `living_blood.clotReviveShare` (0.5) and
    `living_blood.clotReviveYield` (2.0). What does not fit as fresh blood also goes to the clot, so a pour is never
    charged for blood the tank cannot use.
  - **NQ-01-3:** a top-up no longer makes the whole tank fresh. The tank's age blends by volume.
  - **NQ-01-4:** only Projection and the suture's draw revive clot. Flask and gourd pours, gutters and couriers add
    fresh blood into free room. The Condenser never revives.
  - **NQ-01-5:** a Blood Thrall's delivery keeps the age of the tank it came from and does not revive. The thrall saves
    that age with its load.
  - **NQ-01-6:** a station held by an upgrade rite pauses its clot clock. It comes out as old as it went in.
  - **NQ-01-7 (answered 2026-11-14, built 2026-11-15):** a sutured buffer clots like any tank; the free keep-fresh near
    the owner is gone. The suture revives the clot from the owner's blood at 4 mL of clot per mL, server config
    `living_blood.suturedClotReviveYield` (4.0). An idle 600 mL buffer costs its owner about 15 mL a minute. The
    working reserve counts clot, so a buffer whose clot fills its room still asks.
  - **NQ-01-8:** a tank with no free room takes a pour and spends all of it on reviving.
  - **FX-2 (stacking, ingredient):** the `bottled_at` stamp is rounded down to the minute. The Alembic distils a Flask
    into vials by its fresh share (2 vials fresh, 1 half clotted, none clotted). Covenant Mantle and Morphic Tonic take a
    Flask or Jug as built.
- **Feed runs (CB-24, 2026-11-13):** a feeding run grows each crop beside it one stage every 30 s, once per crop however
  many feeding cells touch it.
- **Ground, farming and the daily loop (2026-11-13):**
  - **CQ-7 (built 2026-11-15):** a Catalyst Feeder spends blood each time it keeps a run lit.
    - **NQ-02-2 (answered 2026-11-14, built 2026-11-15):** 15 mL for each catalyst dissolved
      (`GroundRunRules.FEEDER_ML`). Feed runs stay at 30 s a stage with no beat charge.
    - **NQ-02-1 (accepted 2026-11-14, built 2026-11-15):** the player who lit the run pays, reserve protected (source
      `ground_feeder`). If that player is away, dead, out of the level or short, the feeder does not feed and the run
      goes dark on time. The feeder has no tank.
  - **CQ-43 (built 2026-11-15):** one hot run burns at most 4 furnaces (`GroundRunRules.HOT_FURNACE_MAX`). Its pulse
    picks the four nearest it, each once, and saves the sides on each lit cell. A furnace set down later joins at the
    next pulse or feeder top-up. Campfires and candles are not capped (02 T2 default, accepted 2026-11-14).
  - **CL-14 (built 2026-11-15):** living veins move blood only toward an end that is drawing; a vein with nothing
    drawing does not siphon and costs no upkeep (the owner's design).
    - **NQ-02-3 A (answered 2026-11-14, built 2026-11-15):** a machine under its 600 mL working reserve pulls down a
      vein; stores (the Venous Basin, the Gourdvine Tap, vats) never pull by their level.
    - **02 T5 default (accepted 2026-11-14, built 2026-11-15):** "drawing" means blood left that end's tank in the last
      5 seconds (`GroundRunRules.VEIN_DRAW_WINDOW_TICKS` 100) by anything but the vein or clotting. The stamp is
      `LastDrawn` on the tank. With both ends drawing, the fuller levels into the emptier as before.
  - **CQ-44 (built 2026-11-15):** water breaks gouges and does not break living veins; veins are the dearer, better
    tier (the owner's design).
    - **NQ-02-5 (answered 2026-11-14, built 2026-11-15):** only water that flows or is poured into a gouge's cell
      breaks it. Rain and water standing beside it do not.
    - **NQ-02-4 (answered 2026-11-14, built 2026-11-15):** a custom water state. A living vein holds any water, a source
      or flowing water of any level (`LivingVeinBlock.WATER`), so water flows over a vein instead of being dammed. A
      mixin on `FlowingFluid.tick` keeps the vein when the water over it changes or drains off.
    - **02 T6 default (accepted 2026-11-14, built 2026-11-15):** a wet vein that withers back to gouge is washed out
      and leaves its water. A new vein starts dry.
  - **CQ-15 (built 2026-11-15):** Furnace Veins is priced per furnace fed.
    - **NQ-02-9 (answered 2026-11-14, built 2026-11-15):** 6 mL a pulse for each furnace fed, so a bank of 8 is 48 mL a
      pulse. Only furnaces with something to cook count. The client readout shows the one-furnace price (02 T4
      default, accepted 2026-11-14).
  - **CB-26 (built 2026-11-15):** kills supply only a capped share of a region's pressure.
    - **NQ-02-7 (answered 2026-11-14, built 2026-11-15):** kills stop counting at 600 mL, the uneasy floor
      (`WorldAnswerRules.KILL_SHARE_CAP_ML`). Heavy and seething then need spilled blood. Stains and chum are not capped.
  - **CB-3 (built 2026-11-15):** tainted meat gives 10 mL per nutrition and raw fish 4.
    - **NQ-02-8 (answered 2026-11-14, built 2026-11-15):** cooked fish 5, tainted harvest stays 12.
  - **CB-11:** the Blood Moon cast discount goes only to Harbingers in the moon's region. Not built yet.
  - **CQ-42:** a carried crossing between two stented Earthen Veins on one living run stays a flat 125 mL.
  - **RV-45:** a run with no owner present still smelts and holds crops back, and lights and freezes nothing.
  - **RV-46:** a Catalyst Feeder keeps a run for whoever lit it.
  - **CL-15:** the cloche is the Mycelial Lantern re-housed (done). Cloche tiers for specialty fungus are only an idea,
    parked until farming is played.
  - **CQ-34:** the Brined Votary is the chum hunter in the Court's home waters.
  - **CQ-35:** the substrate split stays: Vivacious, Fervent and Ruinous on flesh; Neurotic, Frigid and Ferric on
    Salt-Stained Timber; Incandescent and Umbral on common ground.
  - **CQ-36:** Salt-Stained Timber and its two recipes stay.
  - **CQ-37:** the tainted harvest rules stay (8%, level-2 ground only).
  - **CQ-38:** no catalyst crops for the other six schools; the strains stand for them.
  - **CQ-39:** farmable spore income is accepted.
  - **CQ-40:** floor spikes stay cosmetic.
  - **RV-63:** strains are bush blocks the cloche does not grow: 1 in 6, no light, bone meal +1 on fed ground.
  - **RV-64:** the strain-bed first comes from cutting a ripe strain, Harbingers only.
  - **RV-65:** catalyst crops are worth 2; ripe glowroot gives light 4, the Incandescent strain 3.
  - **RV-66:** chum hunters spawn as events, despawn normally, one at a time, with the elder guardian curse sound at
    low volume (until a sound pass replaces it).
  - **RV-67:** the Materials atlas placement of timber, glowroot, emberpod and tainted harvest stays.
  - **RV-70:** ground runs draw before translucent blocks; the snuff stream uses the Mortem flow material.
  - **RV-72:** the JEI gouge pages stay in function order.
  - **CL-18:** food and sleep feed regeneration, as built.
  - **CQ-48:** Brought On credits every awake player on counted ground.
  - **CQ-49:** Carried by the Vein is granted when the opening is fed in full.
- **Menagerie, research and teaching (2026-11-13):**
  - **CL-12:** an unread kind's blood is weaker to that player until they examine it; the body and mind do not yet
    understand it. Built 2026-11-15.
  - **NQ-03-1 A, NQ-03-3 A (2026-11-14):** an unread draw loses Temper only, from the first draw at every Degree; the
    early blood economy stays as tuned and the Microscope pays off at Degree 2. Built 2026-11-15.
  - **NQ-03-2 (2026-11-14):** a draw from an unread kind gives half its Temper (`BloodResearch.UNREAD_SHARE` 0.5).
    Built 2026-11-15.
  - **NQ-03-4 (2026-11-14):** the weak draw applies to wild Absorption and to draws from a kept vessel; an injected
    sample of an unread kind is unchanged. Built 2026-11-15.
  - **CL-12, its shape (2026-11-14):** the cut keys on read (`BloodResearch.knows`), never on met, and a Creative player
    reads everything. The hurt to the creature, the Vigor a pull costs and the acclimation thresholds do not change.
    The hollow phials stay with a "half its Temper" line under them, and a weak draw says so on the action bar. Built
    2026-11-15.
  - **CR-26:** keeping reads entity type tags, never class names: `hemomancy:menagerie_unkeepable` holds
    `menagerie_people`, `menagerie_wills` and `#hemomancy:bosses`. Who is refused did not change (built 2026-11-14).
  - **CR-26, the Will tag (2026-11-14):** `hemomancy:menagerie_wills` holds only `will` and `will_anchor`;
    `#hemomancy:wills` is not included, so the Blood-Drunk Puppeteer stays keepable. Built 2026-11-14 (P05).
  - **NQ-03-5 (2026-11-14):** all kinds can go in vessels; no kind is added to `hemomancy:menagerie_unkeepable`, so the
    `UNKEEPABLE` verdict still reaches none. The Mnemonic Whale (4.2 blocks wide) fits no vessel, so leading one home
    leads nowhere.
  - **CQ-47 (2026-11-13, built 2026-11-15):** the school pages carry no mL price and teach only the held forms that are
    built; `LiberPageRegistrationGuardrailTest` pins both (a price inside the type brackets, a rate, "costs N mL", and
    no "held it sprays", jet, "plants a strand" or "brands it").
  - **CQ-23 A on the pages (2026-11-14):** the six memories that lost their held form read R-only on their school pages.
  - **The Temper page's phial line (2026-11-15):** "The phials under your blood bar show what you hold" stays: CQ-63 A
    gives the Titration Bandolier a survival giver, built 2026-11-15, so it is true.
  - **Joined vats:** empty vats set together join into one square vessel, up to 3 across and 2 high, at most 40 health
    a vat.
  - **CQ-27:** the brood's numbers stay: 30 mL a health point (at least 150), 120 fed seconds, 75 Vigor to breed, a
    6,000-tick rest, the young at 40 Vigor and a baby (`BroodRules`).
  - **CQ-28:** a Sanguine Vigil and an in-range suture may finish a brood; hand Projection is not required.
  - **CQ-29:** vat breeding is not limited to ageable mobs, and hostile kinds breed, on purpose: it gives the keeper a
    hemomancy way to make more of any living kind. Only the undead and `hemomancy:menagerie_no_brood` are refused.
  - **CQ-30:** a kind is met by sampling, leading in, jarring, breeding or opening its vessel's screen, profiled kinds
    only; seeing one does not count.
  - **CQ-31:** quirks stay notes read from existing rules, as built for now. Real quirks are a later pass.
  - **CQ-32:** a Living Syringe draw from glass costs 3 Vigor, a vial 5, and a vial is refused free on a syringe-only
    kind.
  - **CQ-33:** hollow phials keep showing how many humours an unread kind has.
  - **CL-13:** no vat cap; one led creature at D3, two at D5, three at D7. As built for now, revisited after play.
  - **CN-15:** the blaze stays bloodless to Absorption; the syringe and the vat are its route.
  - **CN-8:** no vessel form on the syringe draw packet, as built.
  - **RV-60:** the vessel screen stays a slotless menu with no payload, 300 by 236, Met Creatures first.
  - **RV-61:** the brood design stays: side by side, one keeper, parting loses what was paid, the inheritance odds,
    hostile kinds allowed.
  - **RV-62:** the brood thread arcs over the lids and the birth bloom comes from a block event.
  - **CQ-46:** the older Liber pages keep their Degree floors.
  - **CQ-47:** the school pages stay free of mL prices; the tooltip and HUD show the live price.
  - **CN-6:** Brought On and Carried by the Vein wear the goal frame (built 2026-11-14). Along the Red's frame waits
    with CQ-45.
  - **RV-73:** What the Dead Give is granted on every draw, even at the cap; Stoked is not gated on the owner's blood.
  - **RV-74:** a learned memory opens its school's pages; Liber Immaculatus floors are purity and clarity stages;
    Annetta's page opens when her route starts.
  - **RV-37:** "crossing" for the Spine's trip, and the first nine firsts with their parents, stay.
- **Living Bestiary preview (FX-20, 2026-11-13):** the creature preview shows at GUI scale 3.
- **Living HUD (2026-11-13, approved after follow-ups):** a thin binding in the vessel frame's material grows around
  whatever shows; eight Temper vials pour only into the vessel; the red darkens on a slow heart, 25 bpm at full blood,
  and never glows; textures are never flipped, only the layout mirrors; the Morphling sits in a jar above the reserve
  and its effect icon leaves vanilla's row while the jar shows; the HUD draws at whole screen pixels.
- **Titration Bandolier (2026-11-13):** the Temper vials show only while it is worn in its own Scarlet Vanity slot; the
  Vascularium Charm shows the vessel, reserve and jar. Since 2026-11-15 the Alchemist gives one with her D2 Temper lesson and a lost one is crafted (CQ-63 A).
- **CQ-63 A (2026-11-14):** the Alchemist gives the Titration Bandolier once, with her D2 Temper lesson, against no
  further proof, as a one-time owed reward (`TitrationBandolierGift`, `OwedRewards`); a plain recipe replaces a lost
  one (two leather, a Sinew Cord, three glass bottles). Owning it proves nothing. Built 2026-11-15.
- **Project instructions (2026-11-13):** **CQ-13:** the core-loop `AGENTS.md` lines are pasted after the CL rulings.
  Built 2026-11-15, each line checked against the code first: the six Part A replacements, the Part B bullets in
  Sculpting words, a rewritten lash bullet, a new bullet for the 2026-11-12 held-manipulation ruling, the dome on
  `DarkFields` and a HUD line. **CQ-14:** the track files moved to `docs/archive/tracks/core_loop/` on 2026-11-15.
  **NQ-05-14:** the play script and the Track 11 ledger stay live at `docs/` root as
  [CORE_LOOP_PLAYTEST.md](CORE_LOOP_PLAYTEST.md) and [CORE_LOOP_TEACHING_LEDGER.md](CORE_LOOP_TEACHING_LEDGER.md).
- **Parked (2026-11-13):** **CN-7**, Track 10's leftovers (Wills reading recent spend, a Tenebris or Lux answer,
  spreading sod, Bestiary world pages), stay deferred until the CL rulings land. Three hunters already answer chum (the
  Drowned, the Bog Revenant and the Brined Votary), so "only drowned" no longer holds. **CL-19**, the Parish, stays
  deferred to its own design. **CN-3**, Vascular Dowsing's veins toward ore, waits for a later visuals pass.
- **The strand skills and the held paces (2026-11-14):** CQ-20 B with RV-57, CQ-22 A, CQ-23 A and RV-58 with CL-25 are
  recorded in [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) "Strands and lashes".
- **Not recorded here, because the 2026-11-12 work overtook the answer:** CQ-25, CQ-45, VX-18 and LP-10 (with CB-1,
  base regeneration, which was accepted only if the strand prices stay). CQ-25, CQ-45 and VX-18 are held by the owner.
- RV-38, CQ-62 (2026-11-14, accepted default): the core-loop fixtures stay the four dev-only `/hemo` branches
  (`temperpool`, `tank`, `vat`, `gouge`). RV-38's "keep them as `/hemo test` scenarios" is overtaken.
- UN-37 (2026-11-14, accepted default): the Vial Centrifuge sentence in `LORE_REFERENCE.md` stays as corrected. The
  Centrifuge makes no blood.
- From `AGENTS.md`, unchanged by the overhaul: Harbinger and Unstained stay exclusive with their reset rules;
  `volume.isActive()` is the opt-in gate; every `PendingDelivery` claim keeps its contract; personal eligibility for
  station upgrades stays mandatory; no proof is granted by ownership, dialogue or a gift.

## Calls made without the user

Round one: each track's "My own calls" in [archive/tracks/core_loop/BUILD_LOG.md](archive/tracks/core_loop/BUILD_LOG.md). Round two: every
package's "My calls", gathered in one list (with counts per package) in the closing section of
[archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md). The ones that still change play most:

- **Prices and paces.** Every price and pace is a first value, kept to be tuned in play (RV-28): the plain Sculptings 4 to 48 mL/s (rechecked from code 2026-11-16),
  the 300 mL Formation key, 2 mL per point of hardness to dig. The three lashes cost 15 mL/s (Blood Lash, 8 mL a tap),
  40 mL/s (Gloam Laceration, 12 mL a tap) and 60 mL/s (Searing Lash, 24 mL a tap). The eight rebuilt held
  manipulations cost 35 to 60 mL/s.
- **The world's answer.** Blood Moons come about one night in fourteen in a quiet world.
- **The strand skills.** Ruled 2026-11-14 (CQ-20 B, CQ-22 A); the numbers stay first values to tune in play.
- **The firsts.** The 27 core-loop firsts and their parents (CQ-45). Along the Red cannot be earned since 2026-11-12:
  no memory plants a strand.
- **The lessons.** Which speaker teaches which system, and that a lesson opens Liber pages and nothing else (CQ-50 to
  CQ-55).
- **The stain look (CQ-59).** Three splashes per age, one outline kept as it dries, alpha 228, 200 and 160.
- **The lash rework and the rebuilt held manipulations (2026-11-12, UN-23 to UN-29).** Every `LashTuning`,
  `LashProfile` and `LashStyle` number, the prices and paces of the eight rebuilt held manipulations, and the small
  calls inside them (the javelin breaks on a body, the dome hides everyone inside, the whip returns to the crosshair).
  They are listed in [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) and in
  [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) section 4.
- **2026-11-13, built without a design round.** The Sanguine Vigil's model and lit state (UN-34). The joined-vat
  details: only squares join, a kept vessel never grows, breaking any vat frees the creature, 800 mL of buffer a vat
  (UN-35). The vivianite Morphling jar and the polished vials, made after the HUD was approved (UN-33).
- **Overnight build, P03 (2026-11-14).** For the Staff ring rule (CQ-8), "a tank" was read as anything that keeps the
  blood to be drawn back: stations, basins, jars, vats, the Bloodwell and an unlit Brazier. Blood a block uses up still
  grows rings. The other P03 calls are in [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md).
- **Overnight build, P04 (2026-11-14).** The new gourd tooltip ("Works only when worn in the gourd slot") and the two changed Alchemist lines. A crop touching two
  feeding cells is grown by the one that comes first by position.
- **Overnight build, P05 (2026-11-14).** The Living Bestiary's list narrows from 160 to 130 px and the text column's
  floor is 108 px when the panel is too narrow for the preview (FX-20). `hemomancy:menagerie_wills` holds only the Will
  and its anchor, so the Blood-Drunk Puppeteer stays keepable. A new refusal line, "%s cannot be kept.", reaches no
  kind yet.
- **Build 2, B06 (2026-11-15).** A distilled Flask's vials round to the nearest whole vial by its fresh share, and its
  clot is lost in the Alembic (no Formations come out). A kin stand-in draws at the plain owner rate (8 mL a tick, no
  suture skill) and never through a gourd or the bloodline pool; Blood Thralls drawing on that suture reach the same
  stand-in. The Lantern's 1,200 mL buffer is kept, so it now holds eight enzymes' worth.

Now rulings, and no longer calls: the brood (CQ-27 to CQ-29), the older pages' floors (CQ-46), "crossing" (RV-37), the
substrate split and Salt-Stained Timber (CQ-35, CQ-36), the flat 125 mL carried crossing (CQ-42), stone-tier harvest at
the bond (CL-16), no kill blood for a kill by draw (CL-24), the scar's Temper refill (LP-13) and who the hung field of
dark hides (CQ-41).

## Open questions

Open questions live in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md), section 1. The ids that belong to this doc: living
blood none; ground, farming and the day CB-11; menagerie and the firsts CQ-45; Staff and mining NQ-04-5, NQ-04-14;
the HUD none. The core-loop ids about the lashes and Temper are also in the pointer of
[MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md): LP-10, NQ-04-8, NQ-04-10, NQ-04-12, CQ-25, VX-18.

## Proof owed and known gaps

- **Feel, by a human:** nothing of the core loop has been played by the owner. [CORE_LOOP_PLAYTEST.md](CORE_LOOP_PLAYTEST.md)
  is the script: one sitting per system, with its review client, its `/hemo` fixture and what to try in order. The play
  rows are in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) section 4: the verbs (PR-13), the first conversions (PR-14), living
  blood (PR-15, owed after the clot-revive rule is built), Temper (PR-16), the menagerie (PR-17), the ground network
  (PR-18), farming (PR-19: judge the 30 s feed run against the cloche), the lashes and held manipulations (PR-20), the
  Staff and mining (PR-21), Living Land and the cling (PR-22), the daily loop (PR-23) and the firsts and the Liber's
  pace (PR-24).
- **The lash:** its feel is owed in `lashTuningClient` (UN-41). Sitting 5 of the play script was rewritten for the
  tap, the charged whip, the three lashes and the eight held manipulations on 2026-11-15 (UN-59). The three lash profiles and the eight rebuilt held manipulations are first values and
  unplayed (UN-24, UN-28, UN-29), and eight single-player cases were never seen in a client (UN-42).
- **Two clients:** round two P11's onlooker review (`tools/review/review-onlooker.ps1`) shows another player's
  streams landing where the caster's do (reach and selection synced), the cling with cleats, the Tendon Line and the
  field of dark. Its strand scenes predate the 2026-11-12 work, and nothing of that work has been shown to a second
  client (UN-43). A played multiplayer session (PR-25), latency and a dedicated server are still unseen.
- **Unplayed since the overnight build:** the Formation supply at D1 to D3 with no free recipes and the 0.02% mob drop
  (LP-1, LP-8); coal at 1 Temper (LP-15); the jug tincture, now cheaper per dose than a flask (CB-16); the new gourd
  and Alchemist lines (FX-1); the scale 3 Bestiary shots (FX-20); the stains (CQ-59).
- **The HUD and 2026-11-13 builds:** the vivianite jar and polished vials (UN-33); vial hover and an F3+T reload in a
  real session (UN-46); the owner's own frame texture edits (UN-45); the Sanguine Vigil in a client (UN-34); joined
  vats in play (UN-35). Since 2026-11-15 `menagerieReviewClient` stages a dormant and a lit Vigil and a joined 2x2 and 2x2x2 vessel.
- **Known test faults:** three frozen snapshot GameTests (registry ids, creative tab order, one dialogue
  fingerprint) were never refreshed for the Titration Bandolier and the tenth equipment slot, so the combined run and
  `alphaCheck` fail until they are.
- **Text a player reads that is false today:** eight lesson lines are false and the Mnemonist's `strands.stain`
  branch teaches a cut form (16 T5). The Liber Temper page's phial line is true since the CQ-63 build of 2026-11-15.
- **Deferred:** the first build log's closing section lists what round one left out, by track; the round-two closing
  section lists everything still deferred after both rounds, by system, with each round-one item marked closed or open.
- **Project instructions:** `AGENTS.md` was not edited by either round or by the overnight build;
  [archive/tracks/core_loop/AGENTS_PATCH.md](archive/tracks/core_loop/AGENTS_PATCH.md) holds the lines to paste (CQ-13). Its strand bullet teaches
  five strand forms and must be rewritten before it is pasted (UN-58), and its header says protocol 49 while the code
  says 50.

## History

| Date | Event | Source |
| --- | --- | --- |
| 2026-11-09 | Design conversation; fifteen track files and nine design plates written; CL-1 to CL-28 asked | [archive/tracks/core_loop/00-README.md](archive/tracks/core_loop/00-README.md) |
| 2026-11-09 | CL-28 ruled and the Temper rename carried out | `c2323f8af` |
| 2026-11-09 to 11-10 | Tracks 0 to 12 built in order 0, 1, 2, 3, 4, 6, 7, 5, 12, 8, 9, 10, 11 | [archive/tracks/core_loop/BUILD_LOG.md](archive/tracks/core_loop/BUILD_LOG.md) |
| 2026-11-10 | CL-2 ruled: Projection and Absorption back on the radial's inner ring as verb picks (`BloodVerbChoicePacket`, protocol 48) | [archive/tracks/core_loop/BUILD_LOG.md](archive/tracks/core_loop/BUILD_LOG.md) "After the run" |
| 2026-11-11 | Round two P1: the GameTest isolation harness (64-block grid, two margin chunks, chunk-aligned origin) and per-test root-cause fixes; the old-save kick (CQ-11) fixed | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P2: `alphaCheck` green and the playthrough suite's required probes all passing; four mod bugs fixed (the stack-aware inquiry id, the met-Vicar flag, the caster's own rite ground, a stain in a Saint's tomb) | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P3: the first round's known defects: `admits` on five older Sculptings (CQ-2), run owners for hot and cold runs, the gouge catalyst mirror, shared gutters, the Temper preview refresh, drip Condenser owners, strand pace only while out (CQ-10), `PlayerBlockEditGuardrailTest` per site, fillers reading their main tank | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P4: the second wave of Sculptings (Sanguine Mending, Crimson Flame Conjuration, Furnace Veins, Glacial Grasp freeze and thaw, Glacial and Ferric Ramparts, Carrion Communion) and narrow Sculptings ahead of a main one | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P5: the six strand skills (Long Strand to Second Strand, ids 74 to 79), eleven more strand memories, Blood Binding as the Animus brand, the stain run, first-person strand poses | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P6: the vessel screen, syringe sampling in glass, the met tier of the Living Bestiary, quirks, and the brood | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P7: the eight fungal strains as beds on their substrates, Salt-Stained Timber, glowroot and emberpod, tainted harvest, floor spikes, chum hunters by water | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P8: Blood Eclipse's field of dark, hot runs burning furnaces, living veins carrying Earthen Vein travel, gouges holding water, the JEI Gouge Catalysts category | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P9: sixteen more core-loop firsts (27 in all) and the forty older Liber pages registered with Degree floors and openers, the school pages rewritten | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P10: NPC lesson trees, 21 lessons across the Hermit, Vicar, Alchemist, Mnemonist, Artificer and Vein-Mason, each opening its Liber pages; six new item inquiries | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P11: the Tendon Line cord, Pyretic Forge's own look, stains, worn cleats, planted Staff rings, a stalled machine's screen and the refused-claim mark fixed; verb presence and the Tendon anchor synced to onlookers (protocol 49); the core-loop fixtures became four dev `/hemo` branches | [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-11 | Round two P12: docs made true for both rounds, the `AGENTS.md` patch and the play-test script written, the round's closing section, and the final proof | [archive/tracks/core_loop/AGENTS_PATCH.md](archive/tracks/core_loop/AGENTS_PATCH.md), [CORE_LOOP_PLAYTEST.md](CORE_LOOP_PLAYTEST.md), [archive/tracks/core_loop/BUILD_LOG_ROUND2.md](archive/tracks/core_loop/BUILD_LOG_ROUND2.md) |
| 2026-11-12 | Owner ruling that held manipulations are not bound to the five strand forms. The lash rebuilt as a tap and a charged whip (`LashTuning`, `lashTuningClient`); eight held manipulations rebuilt with their own mechanics; six memories lost their held form | [MANIPULATIONS_AND_CASTING.md](MANIPULATIONS_AND_CASTING.md) "History", [superpowers/specs/2026-11-12-lash-rework-design.md](superpowers/specs/2026-11-12-lash-rework-design.md) |
| 2026-11-12 to 11-13 | The living HUD built and approved, the Titration Bandolier added (tenth equipment slot), the Sanguine Vigil modelled, vats learned to join | `.superpowers/sdd/2026-11-12-living-hematic-hud/progress.md` |
| 2026-11-13 | The owner answered the question sheet. The core-loop answers (chunks 01 to 05) are recorded under "Rulings that bind" | `owner_answers/01-living-blood-and-containers.md` to `owner_answers/05-verbs-sculpting-and-strands.md` |
| 2026-11-14 | Overnight build. P03: the Temper and cast numbers, the tank half of the Staff ring rule (CQ-8), the hung field of dark hiding only its caster (CQ-41). P04: LP-1, LP-5, LP-8, LP-15, CB-16, FX-1, FX-2 and CB-24 built, the chunk 01 and 02 kept rows recorded. P05: the goal frames, the scale 3 Bestiary preview, the keeping tags, the last school-page price, CB-21, CN-13 and CQ-19 built, the chunk 03 kept rows recorded | [owner_answers/BUILD_LOG_2026-11-14.md](owner_answers/BUILD_LOG_2026-11-14.md) |
| 2026-11-14 | Open questions consolidated into one list; this doc brought up to date with the chunk 01 to 05 rulings | [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) |
