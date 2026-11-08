# Crimson Troupe (the Circus)

Status (2026-11-07): everything up to the 2026-11-06 audit is committed on `neo-1.21.1` (Circus code last touched in `d5a7ddbfa`, 2026-11-03); the audit's round-1 fixes (2026-11-07: off-thread Waybill search, stuck-state and telegraph work, `PendingDelivery` and `PlayerStateSync` moves, pavilion lighting, dead-code deletion) are in the working tree, uncommitted. Pavilion, camps, abandoned stage, school, two endings, the Puppeteer trial rite kind and the Trade box are in code. The private galleries and canopy are not built (see "Proof owed and known gaps"). Live-client, fresh-worldgen, multiplayer and cold-restart acceptance are still owed. The audit is [audits/2026-11-06-circus/](audits/2026-11-06-circus/README.md); source tracks are archived under `docs/archive/tracks/circus/`, and this page replaces reading them.

## Ringmaster finale presentation (2026-11-02)

The Ringmaster (`common/entity/npc/circus/CircusRingmasterEntity`) syncs the finale phase to the client and poses
for it (`CircusRingmasterModel`: crouched on the beam in RAFTERS, presenting on the hub in CAROUSEL, walking and
striking in DESCENT); every thrown knife whips the free arm through an entity event. It holds a `BossBarBinding`
with the `circus_ringmaster` frame (striped big-top canvas, hanging knives), plays the `circus_ringmaster` opening
scene when RAFTERS begins (the bar shows after `INTRO_TICKS`), and a phase card at CAROUSEL and DESCENT. None of it
has been watched in a client yet (cleanup audit A-10).

## Troupe telegraphs and lost-Ringmaster recovery (2026-11-07)

Every troupe attack is a `BossMoveTimeline` on the Saint kit's floors (`CircusPerformerRules.MELEE_MOVE` 12 ticks,
`AREA_MOVE` 20; audit B-2): the performer's model coils through the wind-up from an entity event
(`CircusPerformerEntity.windupState`, read by `CircusWindupPose`), the server draws the footprint with
`BossTelegraphs` in the pavilion's crimson and bone (`CircusEffects`), and the blow lands on the commit tick. The
Acrobat marks its vault spot behind the target, the Fire Eater locks and draws its 5-block, 55-degree cone, the Stilt
Walker rings its 3.5-block slam, the Dagger Thrower draws its throw line (three on a fan), and faculty raise an arm
over a small wedge. Damage, ranges and attack rates are unchanged (each cooldown is shortened by the move's length).
The Ringmaster's RAFTERS throw starts its animation and a thread cue 12 ticks before the knife leaves
(`KNIFE_THROW`), and his DESCENT staff blow is a wound-up move, not `MeleeAttackGoal`'s instant swing
(`STAFF_STRIKE`). Circus combat files no longer name a vanilla particle except the Stilt Walker's block debris (B-9).

Stuck states (B-4): on Succession, `die()` refuses every death but `/kill`, as a Saint does, and an owner's blow in
DESCENT yields him. Any real loss of the Ringmaster mid-finale (death, discard, chunk unload) releases the site once
with the `hemomancy.circus.finale.ringmaster_lost` line; acts are left to their controller. CAROUSEL counts only the
finale performers still standing, so a troupe emptied by a Liberation run reads as downed and the owner is told
(`hemomancy.circus.finale.empty_troupe`).

## What it is

The Circus is a specialist Harbinger sect built on puppetry, attention, spectacle and collective mental manifestation. It is a practice and a social identity inside the Hematic Order, not a class, a second degree ladder, a reputation bar or an enemy faction by default (`docs/archive/tracks/circus/01-faction-and-progression.md`, `docs/LORE_REFERENCE.md` 6.3b). Saints keep singular unstable powers; the Circus refines systems other Harbingers already know (C-08).

It is the main Puppeteering school (`docs/HEMOMANCY_REFERENCE.md` 17.1). Seven professors each teach one ordinary artificial body before its existing Cardinal Ordeal; instruction reveals the ordeal recipe, the ordeal unlocks the body, a practical proves mastery (`wiki/Crimson-Troupe.md`):

| Professor | Shape | Degree |
| --- | --- | ---: |
| Acrobat | Veinwing Vulture | 3 |
| Dagger Thrower | Marrow Juggler (`marrow_spitter` ids kept) | 3 |
| Stilt Walker | Scarlet Mummer | 4 |
| Strongman | Gorebound Hulk | 4 |
| Beast Tamer | Sanguine Hound | 4 |
| Fire Breather | Cinder Bellows | 4 |
| Retired Understudy | Mnemonist Puppet | 5 |

A Threadkeeper inspects the Crossbar for An Empty Ring and records reports; the Ringmaster keeps the outcome-specific conductor lesson. Side assignments: An Empty Ring, seven practicals, The Wrong Audience (two accounts), The Missing Understudy (four records plus testimony), guest acts paying four Enthralling Filaments with exact saved overflow, collected from a Threadkeeper (`03-puppetry-and-circus-mechanics.md`, "Crimson Troupe school expansion"). A guest act pays once per in-game day across the whole school, not per professor: one `guest.day` key, read from `getDayTime() / 24000`, so sleeping or `/time set` moves it (`CircusSchoolQuests`; the ledger's `ledger.guest.available` line reads as per professor; D-45).

Perception is layered. The physical site is a rough occult pavilion (dark cloth, blackened frames, Crossbars, Spindles, bones, masks, thread). Acclimation is a persisted per-player score 0-1000 that does not decay: Unaware 0-149, Disturbed 150-499, Acclimating 500-999, Attuned 1000. Every source, each one-time except the first:

| Source | Points | Code |
| --- | ---: | --- |
| Passive exposure inside a generated pavilion (living, non-spectator) | 1 per 80 ticks | `CircusDiscoveryProgress` |
| First talk with each of the eight performer roles | 50 each | `CircusPerformerEntity` (milestone `performer.<role>`) |
| An Empty Ring | 100 | `CircusSchoolQuests` |
| Each faculty practical (seven) | 100 each | `CircusApprenticeshipProgress` |
| The Wrong Audience, The Missing Understudy | 150 each | `CircusSchoolQuests` |
| Attention act (challenge bit 0) | 150 | `CircusPerformanceController` |
| Route choice (bit 1) | 50 | `CircusRingmasterEntity.handleChoice` |
| Acts 2, 3 and 4 (bits 2, 3, 4) | 100 each | `CircusPerformanceController` |

The perception overlay draws flat silhouettes, cloth and diamonds that grow calmer with the stage (`CircusPerceptionOverlay`). It is presentation only: there is no private geometry, no per-viewer collision and no `circusDistortion` setting, although the archived track 03 designed galleries and stairs at 500 and a solid upper gallery and canopy at 1000 (see "Proof owed and known gaps"). The server owns the score; client cues are per viewer and never decide damage, loot or route state (`02-location-worldgen-and-perception.md`).

Routes: at 500 the Ringmaster becomes coherent. The attention act (bit 0) can be held from a neutral route; once it is done he offers a neutral exit plus warning-and-confirm Succession or Liberation, and the choice is bit 1. One pre-completion repair is allowed, then removed. Route state is per player; one site-global owner runs an act or finale at a time and the first completed outcome settles that pavilion. Succession runs the remaining acts in order: bit 2 holds the ring against the dolls, bit 3 reads the Dagger Thrower's knife act, bit 4 holds the ring through the carousel pattern (`hemomancy.circus.challenge.begin.<bit>`). Its finale needs 1000 acclimation and all five bits (`CircusRouteRules.ALL_CHALLENGES`); it downs the troupe nonlethally and grants the Ringmaster Pattern (D4 low-damage Conductor: one body slot, +1 shaped-body cap, relays mode/focus once within 32 blocks, never chains). Liberation runs no further acts and may start its finale at once. It makes the troupe lethal; the three failed initiates bound on the Impaled Procession's horses are cut by a plain main-hand right-click from the site owner (`BloodDrunkPuppeteerEntity.mobInteract` calls `CircusCarouselEntity.severCaptive`), which kills the captive and exposes its anchor. A player who already knows Thread Ripper can cut one by casting it at the carousel, the same `severCaptive` call. Breaking all three anchors ruins the carousel (six cobwebs placed in air) and opens the lethal descent. Thread Ripper (30 ticks, 24 blocks, 35 blood, 12 s cooldown) is Liberation's prize, not its tool: it is a Summa memory, so most Liberators cannot learn it yet and receive the memory item instead. Whether cutting should need it is an open owner call ([OPEN_QUESTIONS.md](OPEN_QUESTIONS.md#17-crimson-troupe-audit-calls-2026-11-06) D-35). Both endings grant the cosmetic fire-resistant Ringmaster's Topper (`04-creatures-encounters-and-rewards.md`) through the `PendingDelivery` claim `circus_finale_rewards` (`CircusPlayerProgress.FINALE_REWARDS`), which also owes the Thread Ripper memory when Liberation cannot teach it; the owed count lives in `CircusProgress`. Neither ending checks Degree or school work: the Ringmaster Pattern is granted at any Degree (D-30). The Ringmaster uses the real troupe, knife pressure and a Vampire-Bat aggregate, never Vesper's catalogue.

Fauna: Peacock Jumping Spiders (attention act), Vampire Bats (manifested aggregate), Luminal Cicada (non-hostile flash on a 5 s cooldown, Specimen Jar capture) are animals, not puppets. Scarlet Serpents and Prism Cuttles are natural Hemomancy-reactive animals; two jarred Cuttles in the pavilion are the Chromatist reference (`04-...`).

Trade: the Ringmaster and the eight performer speakers (`circus_acrobat`, `circus_fire_eater`, `circus_knife_thrower`, `circus_stilt_walker`, `circus_strongman`, `circus_beast_tamer`, `circus_threadkeeper`, `circus_understudy`) are the `CIRCUS` side in `NpcTradeSides`. Only the Ringmaster takes curiosities (`npc_trade/circus_ringmaster/curiosity.json`, `#hemomancy:trade/curiosity`, graded by `rarity`); each performer has one act-themed items card (`npc_trade/circus_acrobat/rigging_line.json` and siblings). None takes blood or money. The Ringmaster also takes a banked spectacle (`NpcTradeWant.Act`, recorded through `NpcTradeActs.witnessed` by `CircusPerformanceController` when a living non-spectator player finishes an act). Challenge bits are per player and never reset, so a player banks at most four spectacles ever: four on Succession, one (the attention act) on Liberation. Trading needs no Degree and no active blood; no trade counts as a lesson (`docs/archive/tracks/npc_trade/05-npc-catalogues.md`, `06-rapport-rotation-and-demand.md`).

## Where it lives

| Concern | Path |
| --- | --- |
Java paths are under `src/main/java/com/vincenthuto/hemomancy/`, GameTests under `src/gameTest/java/com/vincenthuto/hemomancy/`.

| Concern | Path |
| --- | --- |
| Performers, faculty, Ringmaster, carousel, finale and site rules | `common/entity/npc/circus/` (`CircusRingmasterEntity`, `CircusPerformerEntity` and the four performer subclasses, `CircusFacultyEntity`, `CircusCarouselEntity` (registry id `impaled_procession`), `CircusCarouselRules`, `CircusCarouselEncounterRules`, `CircusPerformanceController` (acts), `CircusPerformerRules` (move timelines), `CircusPavilionSavedData`, `CircusPavilionStateRules`, `CircusFinaleRules`, `CircusEffects` (the Circus palette over the shared toolkit)) |
| Captives, knives, fauna | `common/entity/mob/monster/BloodDrunkPuppeteerEntity` (the carousel captives; `EnthralledDollEntity` the dolls a performer raises), `common/entity/projectile/CircusKnifeProjectileEntity`, `common/entity/mob/animal/PeacockSpiderEntity` (attention act), `LuminalCicadaEntity` + `LuminalCicadaRules`, the Ringmaster's bat aggregate in `VampireBatEntity` (`CircusSwarmOwner`) |
| Player state | `common/capability/player/harbinger/circus/` (`CircusProgress` attachment: discovery, acclimation, route, challenges, milestones, owed finale rewards, the act in progress (cleared on death); synced to the owner by the `CIRCUS_PROGRESS` row of `PlayerStateSync` with a perception dirty flag; `CircusPlayerProgress` (the `FINALE_REWARDS` claim), `CircusDiscoveryProgress`, `CircusApprenticeshipProgress`, `CircusSchoolKnowledge`, `CircusProgressRules`, `CircusRouteRules`); read through `HemoCapabilityAccess.circusProgress(player)` |
| School | `common/mission/circus/` (`CircusCurriculum` (each `Lesson` names its camp), `CircusPracticalController`, `CircusSchoolQuests` (investigations, guest acts), `CircusSchoolRules`, `CircusDemonstrations` (stage bodies)) |
| Rewards | Ringmaster Pattern: `common/entity/summon/RingmasterPatternEntity`, `common/entity/summon/puppeteer/RingmasterConductorRules`, granted through `KnownSummonEvents.grantSummon`; Thread Ripper: `common/manipulation/ductilis/ThreadRipperManip` + `ThreadRipperRules`; Topper: `ringmaster_topper` in `ItemInit` |
| Dialogue | `common/entity/npc/dialogue/CircusReferralDialogue`, `CircusIntroductionRules`, `CircusSchoolDialogue`, `HarbingerMnemonistDialogueTrees` (D4 introduction, Scarlet Waybill); route class `dialogue/route/CircusRingmasterDialogueEvents` |
| Networking | `common/network/circus/` (`PacketSyncCircusPerception`, `PacketSyncCircusSchool`; both clientbound) |
| Scarlet Waybill | `common/item/harbinger/CircusWaybillItem`; search in `common/worldgen/structure/circus/CircusWaybillSearch` over `common/worldgen/structure/StructureSitePrediction`, run off-thread by `common/util/OffThreadSearch` (structure tags `circus_waybill_targets`, `troupe_teacher_<camp>`) |
| Worldgen | `common/worldgen/structure/circus/` (`CircusPavilionStructure`, `TroupeFootprintScan`, `TroupeTerrainColumns`); the one terrain gate is in `common/worldgen/structure/StructurePlacementChecks`; templates are authored NBT under `src/main/resources/data/hemomancy/structure/` (no datagen provider) |
| Rite kind | `common/rite/harbinger/RiteKind.PUPPETEER_TRIAL`, state in `common/rite/PuppeteerTrialState` (a summoned puppet must be beaten inside the boundary before the ceremony closes) |
| Puppet Interference | Morphling side: `common/item/harbinger/morphlings/MorphlingUpkeepRules.withPuppetInterference` (`PUPPET_INTERFERENCE_MULTIPLIER = 1.25`), `IMorphling.waivesUpkeep(..., puppetInterference)`, `SunburstMorphlingItem`; puppet side: `common/entity/summon/puppeteer/PuppeteerSummonRules` (`MORPHLING_TETHER_RANGE_MULTIPLIER = 0.75`, `MORPHLING_THREAD_UPKEEP_MULTIPLIER = 1.5`) applied in `BoundSummonBehavior` |
| Client | `client/model/entity/npc/Circus*Model` (eleven, including `CircusCarouselModel` and `CircusFacultyModel`), `CircusDownedPose`, `CircusWindupPose`; `client/model/entity/summon/RingmasterPatternModel`; `client/render/entity/npc/CircusPerformerRenderer`, `CircusRingmasterRenderer`, `CircusCarouselRenderer`; `client/render/entity/projectile/CircusKnifeProjectileRenderer`; `client/screen/overlay/CircusPerceptionOverlay`; `client/screen/overlay/boss/frame/CircusRingmasterFrame`; `client/render/cinematic/scene/CircusRingmasterScene`; `client/render/world/PuppeteerThreadRenderer` (performer right-hand threads); the ledger's Circus tab `client/screen/item/CircusSchoolLedgerState` in `HarbingerAssignmentLedgerScreen` |
| Trade | `common/entity/npc/trade/NpcTradeActs` (spectacle banking); data `src/main/resources/data/hemomancy/npc_trade/circus_ringmaster/` (`curiosity`, `ring_planks`, `bell`, `spectacle`) and one folder per performer |
| Evidence blocks | `common/block/harbinger/decoration/CircusEvidenceBlock` (stage, loft, quarters, carousel records) |
| Asset tooling | `tools/circus/` (git-tracked `*.py`): per-body `build_*.py` with `puppet_rig.py`, `build_teaching_camps.py`, `build_faculty.py`, `preview_*.py`, `write_troupe_language.py`, `verify_troupe_assets.py`; `build_troupe_assets.py` reads the deleted `src/generated/.../circus_pavilion.nbt` and cannot run (audit W-3); `light_pavilion.py` (with `nbt_io.py`) edits the shipped pavilion NBT in place, hanging Hematic Lanterns until no roofed standable cell has block light 0, and checks the five camps (W-10) |
| GameTests | `gametest/CircusRingmasterGameTests`, `CrimsonTroupeGameTests`, `CrimsonTroupePracticalGameTests`, `CrimsonTroupeStructureGameTests` (`crimson_troupe_validation`), `common/worldgen/structure/circus/TroupeFootprintGameTests`; the CT band `gametest/playthrough/PlaythroughCrimsonTroupeGameTests` (`playthrough_validation`, opt-in); journeys in `gametest/journey/CircusJourney*`; Circus rows in the trade, dialogue-route, rite-kind, puppeteering and clinical batches; `/hemo test journey circus run`, `/hemo test journey circus succession|liberation start|run`, `next`, `status`, `reset` in `gametest/HemoTestCommands` ([TESTING.md](TESTING.md)) |
| Review client | none Circus-specific; `npcTradeReviewClient` captures the Ringmaster's hub and Trade box, and `hudLayerReviewClient` has a Circus perception scene (`HudLayerReview`) (`gradle/review-clients.gradle`) |
| References | `docs/HEMOMANCY_REFERENCE.md` 17 and 17.1, `docs/LORE_REFERENCE.md` 6.3b, `wiki/Crimson-Troupe.md`, `docs/PELAGIC_WORLDGEN.md` (placement evidence), the 2026-11-06 audit |

Worldgen numbers. Three kinds of template ship, all of structure type `hemomancy:circus_pavilion`, all `beard_thin`, all on the biome tag `#hemomancy:has_structure/circus_pavilion` (plains, sunflower plains, the forests, taigas, savannas, sparse jungle, badlands and wooded badlands):

| Structure | Template | Size | Structure set | Spacing / separation |
| --- | --- | --- | --- | --- |
| `circus_pavilion` | `crimson_troupe_pavilion.nbt` | 65x14x65 | `circus_pavilion` | 48 / 16 |
| `troupe_camp_air`, `_blades`, `_pageantry`, `_embers`, `_memory` | `troupe_camp_<camp>.nbt` | 25x9x25 | `troupe_teaching_camps` (weight 1 each) | 24 / 8 |
| `troupe_abandoned_stage` | `troupe_abandoned_stage.nbt` (no actors) | 25x9x25 | `troupe_abandoned_stages` | 32 / 10 |

The pavilion is the old arena inside a 65x65 shell of six rooms. It holds 20 Hematic Lanterns since 2026-11-07, 18 of them hung by `light_pavilion.py`. The only terrain gate is `StructurePlacementChecks`: dry ground, at most 3 blocks of surface range over every column of the rotated footprint, the base within 3 of each column and clear air above it. Only the 65-wide start gets the Harbinger Outpost's Abocipher emitters (`CircusPavilionStructure.afterPlace`).

## Rulings that bind

- Pavilion observation and passive acclimation require living, non-spectator players. Preserve earlier discovery and allow ordinary D0/inactive-blood visits; the D4 referral is optional Side work, not a discovery or Main gate (`AGENTS.md` project patterns; `docs/HEMOMANCY_REFERENCE.md` 17.1).
- Circus and underwater content are separate developments (C-01, C-03): the Circus is mental (puppetry, attention, decoys, displacement, light, sound), the Brined Court is bodily (Morphlings). Morphlings stay the only bodily-symbiosis system; no sea-only attachment framework (`06-underwater-boundary-and-deferred-work.md`, `docs/archive/tracks/brined_court/00-README.md`).
- The Court and the Circus can be combined, but Puppet Interference still applies on both sides (2026-10-29, Brined Court L7). The Morphling pays x1.25 blood upkeep while any qualifying puppet or Commandeered Will is out, and Sunburst's Sun-Fed waiver is cancelled ([HEMOMANCY_REFERENCE.md](HEMOMANCY_REFERENCE.md) §16.6). Each qualifying puppet works at 75% of its tether range and pays `ceil(upkeep x 1.5)` thread while a Morphling is equipped, and a paid surcharge refreshes the Morphling's fed timer (§17 "Morphling interference"). Whether to keep both penalties is D-40.
- The Ringmaster is excluded from Vesper's puppet roster (2026-10-16); the Ringmaster Pattern is not his (`VesperPuppetRoster`).
- The Circus got a Trade box in the first NPC-trade pass (2026-10-31, npc_trade Q-4). The Ringmaster's trial, accept/reject, finale and repair options stay in Quest Work; newcomer "ask the Vicar" guidance is skipped for the `CIRCUS` side (`docs/archive/tracks/npc_trade/01-conversation-merge.md`).
- The Circus must not add a second summon registry, ownership capability, upkeep currency, command surface, radial or progression ladder (`03-...`, 17.1).
- Illusion, decoys, teleport, light and spectacle over physical pricking (C-02); shared perception can become local reality (C-09); acclimation makes the site calmer and more solid, not noisier (C-10).
- A client illusion never decides damage, loot, route state or progression; anything that attacks, blocks or persists is an ordinary server entity. No permanent victim transformation in this pass (`02-...` "Reality boundary").
- Hostile-route reward is never a puppet; neither route grants a Saint power or Vesper's catalogue (C-11, `01-...`).
- The Mnemonist introduces the Circus at Degree 4 and acknowledges early discovery instead of repeating it (C-06, `01-...`).
- Pavilions are world-generated only, never a runtime event in player terrain (C-05).
- Enumclaw / Mr. Hands naming, explicit anatomy and a random one-percent attack chance are rejected (C-15).
- Saints are the 1.1 expansion; the sea faction follows (C-04).

## Calls made without the user

- C-14: the Impaled Procession is the central arena and the Liberation mechanism (three bound failed initiates expose three anchors); the huddle left this open and the implementation chose it (`README.md` decision table).
- Thread Ripper numbers (30 ticks, 24 blocks, 35 blood, 12 s cooldown, 25% max-health disruption above 30%, unravel at or below) and the Ringmaster Pattern's 32-block single relay were set in implementation (`04-...`).
- Acclimation thresholds, the 1/80-tick rate, milestone values and the 200-loaded-tick absent-owner grace were implementation selections (`02-...`, `03-...`).
- Worldgen spacing, footprint and biome tag values were "implemented initial selection", to be revisited only on fresh-chunk evidence (`02-...`).
- Luminal Cicada was chosen as the one candidate creature (WP-6) over snake and other candidates (`04-...`, `05-...`).
- Trade catalogue: the Ringmaster's "finale's leavings" card was dropped (the topper is never sold); a D4 bell-for-thread card is the carrot; performers got one act-themed card each instead of a tendency-graded curiosity; spectacle became a new `act` want kind rather than a Circus reading of Court `custom` (`npc_trade/05-...`, `06-...`).
- The Circus trade box draws in the theme the Ringmaster's tree already uses and the default crest; no Circus `DialogueTheme` or crest exists (`npc_trade/04-trade-screen.md`).
- Demonstration stage bodies now sync their teacher as owner (and so no longer despawn in Peaceful); performer threads are anchored from the rendered right hand (`08-crimson-troupe-implementation-report.md`).
- Faculty, Juggler, Hulk, Bellows and camp visual rebuilds (2026-10-08/09) were authored from previews only; native appearance was not reviewed (`08-...`).
- 2026-11-07 (audit B-4, D-62 family): a lost Ringmaster releases the site rather than settling it, and an emptied troupe counts as downed rather than being respawned; dead performers still never come back and a released site has no Ringmaster to restart it, so CT-7's "settled or returned" probe stays open. CAROUSEL counts only finale performers that still exist, so killed ones neither block it nor come back. Troupe wind-up poses and telegraph looks were authored without a client.
- 2026-11-07 (audit A1): the Waybill's off-thread search is bounded to `CircusWaybillSearch.MAX_RINGS = 32` spacing cells each way (a spacing cell is 768 blocks for pavilions and 384 for camps; the old server-thread search was vanilla locate with a 160-chunk radius), and a finished nearest pavilion gives way to the nearest unfinished one within `UNFINISHED_RADIUS = 2560` blocks. Concentric-ring sets are not searched; per-cell predictions are cached for the session.
- 2026-11-07 (audit A9, W-10): the 18 lanterns were placed by `light_pavilion.py` itself (hung from roofs, canvas and iron beams wherever a roofed standable cell was dark), not by hand; nobody has looked at them in a client. The camps already passed the check and were not changed.

## Open questions

- The 2026-11-06 audit's owner calls (reach, gating, the fight, powers, sites, guest pay, the test gate) are rows D-29 to D-46 in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md#17-crimson-troupe-audit-calls-2026-11-06) §1.7; the evidence and options are in [the audit](audits/2026-11-06-circus/README.md) section C.
- Late playthrough decisions (2026-11-02), each a failing `required = false` probe with its evidence in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) and the roll-up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6 (PT- plus the number): D-57 [CT-1], D-58 [CT-2], D-59 [CT-3], D-60 [CT-5], D-61 [CT-6], D-62 [CT-7], D-63 [CT-8].
- No Circus theme or crest on the trade box; adding one is a separate art job (`npc_trade/04-trade-screen.md`).
- Snake: STRONG CANDIDATE only; a snake boss or puppet needs a role the Scarlet Serpent and Mummer do not cover (`04-...`).
- C-13: Circus encounters as the missing midgame mini-bosses leading to the Ringmaster is a STRONG CANDIDATE, not built beyond the finale (`README.md`; OPEN_QUESTIONS D-18 and D-33).
- The proposed Ringmaster staff form stays gated on the hostile-route design (`03-...` "Professions through existing equipment").
- Brined Court 05: should the Mnemonist also hint at the Court? PROPOSED no, not confirmed (`brined_court/05-court-discovery.md`).
- The circus suite's gates were phrased as OPEN GATE labels, not numbered Q ids; none remain unanswered in the archived files.

## Proof owed and known gaps

- Not built: the private per-viewer galleries and stairs at 500 acclimation, the privately solid upper gallery and canopy at 1000, and the `circusDistortion` setting that the archived track 03 ("Tangible pavilion manifestations") describes. Nothing in `src` has a gallery, canopy, per-viewer collision or that config key. Because of it the idle Ringmaster's perch cannot be reached from the floor in survival (PT-57 [CT-1]). Build it, or make the galleries real for everyone, is D-29.
- Not built: any mental verb the player fires. Of attention, illusion, decoys, displacement, flash and sound, only the Scarlet Mummer's automatic taunt exists (audit P-1, D-37).
- Live-client presentation of every stage, shader-disabled readability, two clients at different stages, reconnect and dimension change (`02-...`, `05-...` verification matrix).
- Fresh natural chunk placement, density and visual fit of the 65x65 pavilion, the five camps and the abandoned stage (`08-...`). The mod is not live, so no older generated site needs to be kept.
- A full natural-Survival apprenticeship and both endings, multiplayer isolation and contention, real disconnect/rejoin and a cold server restart (`08-...` "Acceptance still required").
- Combat feel and arena bounds of the Ringmaster finale and carousel; multiplayer target sync of horse arcs (`05-...` WP-8, WP-9).
- Luminal Cicada clinging, flash, spawning and jar fit in a live world (`04-...`).
- Native appearance of the rebuilt Bellows, Juggler, Hulk, faculty models, camps and performer threads (`08-...`).
- The implementation report records unrelated full-run failures (8 JVM, 4 server) at the time; the resource-log gate was blocked by them (`08-...`).
- Automated evidence (dated): 92 focused JVM tests and 15 Circus GameTests passed on 2026-10-07; 30 thread suites on 2026-10-09 (`08-...`). On 2026-11-06 the gated `crimson_troupe_validation` batch held 16 tests, with 11 Ringmaster tests and the CT band outside the `alphaCheck` gate (audit T-1, T-2); the 2026-11-07 round deleted the ten JVM tests that checked the old pavilion template and gated `CrimsonTroupePavilionGameTests` (both finales on the real pavilion, living/non-spectator observation, the finale-reward claim, the lost-Ringmaster release) and the 11 Ringmaster tests: `runCrimsonTroupeGameTestServer` 32/32, JVM 5571/5571, combined gate 1443/1443 on 2026-11-07. `playthrough_validation` still has eight required failures, none in the Circus band; the Circus decision probes CT-1, 2, 5, 7 and 8 still fail (CT-3 passed, unexplained). JVM tests are not proof of visuals, worldgen fit or restart persistence.

## History

- 2026-09-01: huddle; decisions C-01..C-15 recorded (`archive/tracks/circus/README.md`). WP-1 canon sync completed the same day.
- September 2026: WP-2..WP-9 built: pavilion worldgen, D4 Mnemonist introduction and Scarlet Waybill, acclimation and presentation, Scarlet Mummer slice, Luminal Cicada, routes, Ringmaster finale, carousel (`archive/tracks/circus/05-implementation-workpacks.md`).
- 2026-10-07: Crimson Troupe school expansion (professors, camps, 65x65 pavilion, practicals, guest acts, Cinder Bellows) per `superpowers/plans/2026-10-07-crimson-troupe-expansion.md`; report in `archive/tracks/circus/08-crimson-troupe-implementation-report.md`.
- 2026-10-08/09: Bellows, Juggler, Hulk, camp and faculty visual revisions; performer control threads (`08-...`).
- 2026-10-16: Ringmaster excluded from Vesper's puppet roster.
- 2026-10-24: circus package split by function (`entity/npc/circus`, `capability/player/harbinger/circus`, `worldgen/structure/circus`) under the architecture consolidation (`AGENTS.md`).
- 2026-10-29: Brined Court rulings: Court and Circus combinable, Puppet Interference applies and cancels Sun-Fed (`archive/tracks/brined_court/00-README.md`).
- 2026-10-31: NPC trade pass: Circus joins the dialogue hub, Trade box and catalogues, spectacle want; `npcTradeReviewClient` captured the Ringmaster's box (`archive/tracks/npc_trade/`).
- 2026-11-01: track folders archived; this page written.
- 2026-11-01: playthrough band CT (`PlaythroughCrimsonTroupeGameTests`, `playthrough_validation`) fixes: a guest payment paid in full says `hemomancy.circus.school.reward.delivered` (LF-7); the Threadkeeper's claim card is a Turn In and Urgent only while a payment is owed, report is Urgent only when a report would complete something, inspect is a Notice until a Crossbar is on record (LF-8, `CircusSchoolDialogue.hub`); an act owner who turns spectator fails the act like a dead one (LF-9); acts 2-4 arm only performers that take part in the finale, so faculty keep teaching (LF-10); the lesson, the practical refusals (no Crossbar, below Degree, professor busy), an empty claim, route choice and route repair, and an inspect below Degree 3 now each say a `message.hemomancy.circus.*` line (LF-11). Decision probes CT-1, CT-2, CT-3, CT-5, CT-7 and CT-8 are optional tests in that class, awaiting the user.
- 2026-11-06: five-part audit of the Circus ([audits/2026-11-06-circus/](audits/2026-11-06-circus/README.md)); owner calls rolled into OPEN_QUESTIONS §1.7.
- 2026-11-07: audit round 1 (uncommitted). The Waybill searches off the server thread (`CircusWaybillSearch` over `StructureSitePrediction` through `common/util/OffThreadSearch`, shared with the Court's `CourtSeatSearch`); holding an unaimed Waybill starts at most one such search per player until the stack records its dimension, instead of a server-thread search every tick. Troupe and Ringmaster attacks wind up 12 ticks (melee, thrown) or 20 (area) on `BossMoveTimeline` with `BossTelegraphs` and `CircusEffects` from the custom toolkit. A lost Ringmaster mid-finale releases the site (`releaseUnsettledSite`), Succession's `die()` refuses every death but `/kill`, and CAROUSEL ends when every remaining finale performer is downed. The Topper and the Thread Ripper memory are the `PendingDelivery` claim `CircusPlayerProgress.FINALE_REWARDS`. Perception sync is the `PlayerStateSync` row `CIRCUS_PROGRESS`, and the act in progress moved into the `CircusProgress` attachment. Degree checks use `ProgressionGates`, summon ids use `PuppeteerSummonDefinitions` constants, and each `CircusCurriculum.Lesson` names its camp. Deleted: `CircusPavilionTemplate` and its tests, `CircusPavilionPlacementRules`, the legacy migrations (`foldLegacyRootKeys`, `convertLegacyRingmaster`) and `ThreadRipperRules.Outcome.SEVER_CAPTIVE`. The pavilion got 18 hanging Hematic Lanterns (`tools/circus/light_pavilion.py`, `nbt_io.py`). The Summons tab's offering reads the ordeal recipe. Hematic iron replaces brass on the Crossbar and Juggler. The docs were corrected against the code (audit D-1 to D-13).
