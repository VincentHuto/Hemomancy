# Crimson Troupe (the Circus)

Status (2026-11-01): built and uncommitted in parts; pavilion, camps, school, two endings, Puppeteer trial rite kind and the Trade box are in code; live-client, fresh-worldgen, multiplayer and cold-restart acceptance are still owed. Source tracks are archived under `docs/archive/tracks/circus/`; this page replaces reading them.

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

A Threadkeeper inspects the Crossbar for An Empty Ring and records reports; the Ringmaster keeps the outcome-specific conductor lesson. Side assignments: An Empty Ring, seven practicals, The Wrong Audience (two accounts), The Missing Understudy (four records plus testimony), daily guest acts paying four Enthralling Filaments with exact saved overflow (`03-puppetry-and-circus-mechanics.md`, "Crimson Troupe school expansion").

Perception is layered. The physical site is a rough occult pavilion (dark cloth, blackened frames, Crossbars, Spindles, bones, masks, thread). Acclimation is a persisted per-player score 0-1000 that does not decay: Unaware 0-149, Disturbed 150-499, Acclimating 500-999, Attuned 1000. Passive exposure gives one point per 80 ticks inside a generated pavilion; milestones are one-time (100 for An Empty Ring, 100 per practical, 150 per investigation). From 500 private geometry (galleries, stairs) supports the player; at 1000 the upper gallery and canopy are privately solid. The server owns the score; client cues are per viewer and never decide damage, loot or route state (`02-location-worldgen-and-perception.md`, `03-...` "Tangible pavilion manifestations"). `circusDistortion` disables client refraction without hiding collision.

Routes: at 500 the Ringmaster becomes coherent and offers a neutral exit plus warning-and-confirm Succession or Liberation. One pre-completion repair is allowed, then removed. Route state is per player; one site-global owner runs an act or finale at a time and the first completed outcome settles that pavilion. Succession needs 1000 acclimation and all five challenge bits (the peacock-spider attention act, troupe-and-doll pressure, knife/displacement, carousel coordination), downs the troupe nonlethally and grants the Ringmaster Pattern (D4 low-damage Conductor: one body slot, +1 shaped-body cap, relays mode/focus once within 32 blocks, never chains). Liberation makes the troupe lethal, uses Thread Ripper (30 ticks, 24 blocks, 35 blood, 12 s cooldown) to sever the three failed initiates bound on the Impaled Procession's horses, exposing three anchors; breaking all three ruins the carousel (air-only cobweb ruin) and opens the lethal descent. Both grant the cosmetic fire-resistant Ringmaster's Topper (`04-creatures-encounters-and-rewards.md`). The Ringmaster uses the real troupe, knife pressure and a Vampire-Bat aggregate, never Vesper's catalogue.

Fauna: Peacock Jumping Spiders (attention act), Vampire Bats (manifested aggregate), Luminal Cicada (non-hostile flash on a 5 s cooldown, Specimen Jar capture) are animals, not puppets. Scarlet Serpents and Prism Cuttles are natural Hemomancy-reactive animals; two jarred Cuttles in the pavilion are the Chromatist reference (`04-...`).

Trade: the Ringmaster and the eight performer speakers (`circus_acrobat`, `circus_fire_eater`, `circus_knife_thrower`, `circus_stilt_walker`, `circus_strongman`, `circus_beast_tamer`, `circus_threadkeeper`, `circus_understudy`) are the `CIRCUS` side in `NpcTradeSides`. They take curiosities (`#hemomancy:trade/curiosity`, graded by `rarity`) and act-themed goods, never blood or money, and the Ringmaster takes a banked spectacle (`NpcTradeWant.Act`, recorded by `CircusPerformanceController` when a living non-spectator player finishes a pavilion challenge, at most four per pavilion). Trading needs no Degree and no active blood; no trade counts as a lesson (`docs/archive/tracks/npc_trade/05-npc-catalogues.md`, `06-rapport-rotation-and-demand.md`).

## Where it lives

| Concern | Path |
| --- | --- |
| Performers, faculty, Ringmaster, carousel, finale and site rules | `src/main/java/com/vincenthuto/hemomancy/common/entity/npc/circus/` (`CircusRingmasterEntity`, `CircusPerformerEntity`, `CircusFacultyEntity`, `CircusCarouselEntity`, `CircusPerformanceController`, `CircusPavilionSavedData`, `CircusFinaleRules`) |
| Player state | `common/capability/player/harbinger/circus/` (`CircusProgress` attachment: discovery, acclimation, route, challenges, milestones; `CircusDiscoveryProgress`, `CircusApprenticeshipProgress`, `CircusSchoolKnowledge`, `CircusProgressRules`, `CircusRouteRules`); read through `HemoCapabilityAccess.circusProgress(player)` |
| Dialogue | `common/entity/npc/dialogue/CircusReferralDialogue`, `CircusIntroductionRules`, `CircusSchoolDialogue`, `HarbingerMnemonistDialogueTrees` (D4 introduction, Scarlet Waybill) |
| Worldgen | `common/worldgen/structure/circus/` (`CircusPavilionStructure`, `CircusPavilionPlacementRules`, `TroupeFootprintScan`, `TroupeTerrainColumns`); pavilion template datagen `common/data/gen/CircusPavilionStructureProvider` + `CircusPavilionTemplate` (the one structure provider `runData` runs) |
| Rite kind | `common/rite/harbinger/RiteKind.PUPPETEER_TRIAL`, state in `common/rite/PuppeteerTrialState` (a summoned puppet must be beaten inside the boundary before the ceremony closes) |
| Puppet Interference | `common/item/harbinger/morphlings/MorphlingUpkeepRules.withPuppetInterference` (`PUPPET_INTERFERENCE_MULTIPLIER = 1.25`), `IMorphling.waivesUpkeep(..., puppetInterference)`, `SunburstMorphlingItem` |
| Client | `client/model/entity/npc/Circus*Model`, `CircusFacultyModel`, `CircusDownedPose`; `client/render/entity/npc/Circus*Renderer`; `client/screen/overlay/CircusPerceptionOverlay`; `client/render/world/PuppeteerThreadRenderer` (performer right-hand threads) |
| Trade data | `src/main/resources/data/hemomancy/npc_trade/circus_ringmaster/` (`curiosity`, `ring_planks`, `bell`, `spectacle`) and one folder per performer |
| Evidence blocks | `common/block/harbinger/decoration/CircusEvidenceBlock` (stage, loft, quarters, carousel records) |
| Asset tooling | `tools/circus/build_troupe_assets.py` (never rewrites the legacy pavilion), per-body `build_*.py`, `build_teaching_camps.py`, `build_faculty.py`, `preview_*.py`, `write_troupe_language.py`, `verify_troupe_assets.py` |
| GameTests | `src/gameTest/.../gametest/CircusRingmasterGameTests`, `CrimsonTroupeGameTests`, `CrimsonTroupePracticalGameTests`, `CrimsonTroupeStructureGameTests`, `worldgen/structure/circus/TroupeFootprintGameTests`, journeys in `gametest/journey/CircusJourney*`; `/hemo test circus run`, `succession|liberation start|run` in `HemoTestCommands` |
| Review client | none Circus-specific; `npcTradeReviewClient` captures the Ringmaster's hub and Trade box (`gradle/review-clients.gradle`) |
| References | `docs/HEMOMANCY_REFERENCE.md` 17.1, `docs/LORE_REFERENCE.md` 6.3b, `wiki/Crimson-Troupe.md`, `docs/PELAGIC_WORLDGEN.md` (placement evidence) |

Worldgen numbers: legacy pavilion 33x12x33 inside a 49x49 sample, `beard_thin`, spacing 48 / separation 16, biome tag covers level plains, forests, taigas, savannas, sparse jungle and badlands, five-block max surface variation. New pavilions are a 65x65 compound around the preserved arena; five 25x9x25 camps (air, blades, pageantry, embers, memory) at spacing 24 / separation 8, three-block max variation over the full rotated footprint (`02-...`, `03-...`).

## Rulings that bind

- Pavilion observation and passive acclimation require living, non-spectator players. Preserve earlier discovery and allow ordinary D0/inactive-blood visits; the D4 referral is optional Side work, not a discovery or Main gate (`AGENTS.md` project patterns; `docs/HEMOMANCY_REFERENCE.md` 17.1).
- Circus and underwater content are separate developments (C-01, C-03): the Circus is mental (puppetry, attention, decoys, displacement, light, sound), the Brined Court is bodily (Morphlings). Morphlings stay the only bodily-symbiosis system; no sea-only attachment framework (`06-underwater-boundary-and-deferred-work.md`, `docs/archive/tracks/brined_court/00-README.md`).
- The Court and the Circus can be combined, but Puppet Interference (x1.25 Morphling upkeep while a puppet is active) still applies and cancels Sunburst's Sun-Fed upkeep waiver (2026-10-29, Brined Court L7).
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

## Open questions

- Late playthrough decisions (2026-11-02), each a failing `required = false` probe with its evidence in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) and the roll-up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6 (PT- plus the number): D-57 [CT-1], D-58 [CT-2], D-59 [CT-3], D-60 [CT-5], D-61 [CT-6], D-62 [CT-7], D-63 [CT-8].
- No Circus theme or crest on the trade box; adding one is a separate art job (`npc_trade/04-trade-screen.md`).
- Snake: STRONG CANDIDATE only; a snake boss or puppet needs a role the Scarlet Serpent and Mummer do not cover (`04-...`).
- C-13: Circus encounters as the missing midgame mini-bosses leading to the Ringmaster is a STRONG CANDIDATE, not built beyond the finale (`README.md`).
- The proposed Ringmaster staff form stays gated on the hostile-route design (`03-...` "Professions through existing equipment").
- Brined Court 05: should the Mnemonist also hint at the Court? PROPOSED no, not confirmed (`brined_court/05-court-discovery.md`).
- The circus suite's gates were phrased as OPEN GATE labels, not numbered Q ids; none remain unanswered in the archived files.

## Proof owed and known gaps

- Live-client presentation of every stage, shader-disabled readability, two clients at different stages, reconnect and dimension change (`02-...`, `05-...` verification matrix).
- Fresh natural chunk placement, density and visual fit of the 65x65 pavilion and the five camps; existing sites keep their legacy blocks (`08-...`).
- A full natural-Survival apprenticeship and both endings, multiplayer isolation and contention, real disconnect/rejoin and a cold server restart (`08-...` "Acceptance still required").
- Combat feel and arena bounds of the Ringmaster finale and carousel; multiplayer target sync of horse arcs (`05-...` WP-8, WP-9).
- Luminal Cicada clinging, flash, spawning and jar fit in a live world (`04-...`).
- Native appearance of the rebuilt Bellows, Juggler, Hulk, faculty models, camps and performer threads (`08-...`).
- The implementation report records unrelated full-run failures (8 JVM, 4 server) at the time; the resource-log gate was blocked by them (`08-...`).
- Automated evidence: 92 focused JVM tests and 15 Circus GameTests passed on 2026-10-07; 30 thread suites on 2026-10-09 (`08-...`). JVM tests are not proof of visuals, worldgen fit or restart persistence.

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
