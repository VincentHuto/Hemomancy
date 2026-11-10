# Endgame Paths: Apotheos and Silentium

Status (2026-11-06): built and committed (`2ff58cc50`, `185aa3502`, `ea7bd732d`); the heart-node and possession reworks replaced the first overnight builds; nothing has had a full user review in a client yet. The 2026-11-06 rulings (D-3, D-4, D-5, D-7, D-8, D-9) are built uncommitted: Mind Broken's costs, the Morphic Tonic's Gardens gate and Morphic Mending, Molting at the heart-node and the dormant Spine. Chimerization, the Silentium's own D8 powers and the Saint gifts are still open.

## What it is

Degree 7 is Archon for everyone, a full stage. After the ninth Pome tears the Fungal Spine free and the first forced projection returns the player, the eighth Degree splits into two equal titles. Nobody, not even the Vicar, truly knows what follows Archon: Silentium and Apotheosis are legends to him and NPC lines about them are hearsay (`docs/LORE_REFERENCE.md` line 277, `vicar.archon.final_truth`).

| | Apotheos | Silentium |
| --- | --- | --- |
| How | Rite of Apotheosis (`DegreeProgression.advance(player, 8)` keeps meaning Apotheos) | Rite of Pruning (the Severance), both Vesper phases, then absorbing the Memory of Vesper in a lit Iron Brazier through the Living Staff (`LivingWeaponGraftRite` calls `DegreeProgression.advance(player, 8, SILENTIUM)`) |
| Path state (`EnumArchonPath`) | `APOTHEOS_PENDING`, `APOTHEOS` | `SILENT_PENDING` (only "chose silence"; no power or title), `SILENTIUM` |
| The hive | Keeps hearing it; it names them Apotheos | Hears nothing: whispers stop, pending whispers go unheard, Ancestral Communion finds nothing (`HarbingerPathPermissions.hearsTheHive`) |
| Gardens | The Spine makes only the first crossing (a projection); after it, travels in the body by the Rite of Visitation or a Fungal Podium | Only by possessing a surrogate's facsimile |
| Home | The Fungal Gardens (a personal Fane there) | The Saint mausoleums (the ring door answers them, hallowed ground tolerates them) |
| Title | `hemomancy.degree.8.apotheos` | `hemomancy.degree.8.silentium` (`DegreeTitles.titleKey`) |

Severance is the player's own act: cutting their Qliphoth tree in the Rite of Pruning, an ego death and a cut with the hive. It is not a place. The Gardens dimension is a 3D projection of the higher-dimensional being into the space above the world. Sculk and the Antecedent were the hive's first failed attempt and touch neither D7 nor D8. Unstained, unaligned and sub-D7 players never enter the dimension; the Overworld `fungal_gardens` biome is a different place anyone can walk.

**Apotheos body** (`docs/HEMOMANCY_REFERENCE.md` §5.6a): never eats, hunger pinned at 17 (sprint, no natural regen); every `FOOD` item refused, plus potions and milk inside the Gardens; the Morphic Tonic (Bloody Jug thrown into morphic nectar) is the one remedy. Only an Apotheos drinks it, and only in the Gardens (the dimension or the Overworld biome): it purges harmful effects and gives Morphic Mending for 10 seconds, 1 health every 25 ticks and 2.5 mL of blood a tick (8 health and 500 mL in all; `MorphicTonicRules`). Spore load (`spore_load` attachment, 0 to 100, not kept through death) gathers 2 per second in the dimension and 0.5 in the Overworld biome, replaces the food bar, burns 25 for 6 health on key U with a 100-tick cooldown; crouching still on `fungal_network_ground` for 2 seconds regrows 2 health per second.

**The Gardens answer the body** (§5.6b, §5.6c): a non-Apotheos walking on network ground alarms the network every 8 blocks (responders within 24 turn on them, hunter fungi within 64 converge); an Apotheos sees everyone on the network outlined within 32 blocks; puffballs feed an Apotheos (+3 load) and poison anyone else; immune suspicion (casts, high blood) summons 2 to 4 Antibodies at 12 points; caps grow bridges under an Apotheos's empty hand; gills bite strangers and shut; rendering pools break an Apotheos's thrown items into enzymes by `hemomancy:rendering` recipes. Eight facsimiles (bat, enderman, blaze, golem, horse, donkey, wolf, spider) roam the dimension as its fauna, drop only spore sacs and refuse all interaction.

**Heart-node sanctum** (§5.6d): the Apotheos's personal Fane in the Gardens, the stand-in for the Consecrated Bloodwell at the heart of a Soft Envelope. Planted by an Apotheos only, on network ground in the dimension, one per player; it sets a forced respawn. Heart lobe radius 12; up to 6 Mycelial Synapses (Blood Structure, D8, 120 blood: a Sporite Crystal against a hematic iron block on any face) each add a radius-8 lobe that must chain to the heart and may not touch another owner's envelope. Only the owner benefits (Regeneration I and spore load tapering from +4 at the node to +2 at the edge); strangers cannot break or place inside, blasts and pistons stop at the envelope; a death inside the owner's envelope leaves a fruiting-body cache that never despawns. The node matures over 3 minutes in 4 stages; right-clicking it opens `HeartNodeScreen` (map, state, seven visual toggles and a Growth slider; settings never change rules) with a Molt button.

**Molting** (D-5, built 2026-11-06): from the heart-node screen an Apotheos opens `MoltingScreen` and re-places their temper like a stat respec. Every point of grown alignment is added up (scar alignment is borrowed and left out), a tenth is shed, and the rest is placed by hand across the eight tempers in whole points (shift moves ten); every point must be placed. A molt needs the owner at their own node in the Gardens, a full spore load of 100 (spent) and a hardened skin: the new one stays soft for a day (24,000 ticks, `PlayerCooldowns.MOLTING`). The server checks everything again (`MoltRequestPacket`, `MoltingService`, `MoltingRules`). The envelope shows as a ground-creeping mycelial shader (`heart_node_spread`), not a bubble. It coexists with the Founding Fane and shares nothing with it (`HeartNodeFaneSeparationGuardrailTest`).

**Silentium possession** (§5.6e, §5.6f): the Rite of Possession is a bond rite marked special on the Qliphoth grand floor with a Sanguine Quintessence medium; the caster must be a Silentium, not Mind Broken, not possessing, and a vanilla surrogate (bat, enderman, blaze, iron golem, horse, donkey, wolf, spider or armor stand) held by the caster's own Blood Binding must stand in the rite space. Brazier offerings tune it: Mnemonic Ambergris (regen), Hematic Iron Block (3-hit shell), Totem of Undying (one refill), Adipocere (+3 max Concentration). A shadow echo stays at the rite; the caster lands in the Gardens drawn as the form's facsimile with its hitbox and animation, and the manipulation wheel becomes the possession wheel (Return, Still, the form's gifts). Concentration is the only meter (bat 3 to golem 14; 1 per hit, 2 at 6+ damage); a clean Return costs nothing; Concentration 0, death or leaving ejects the caster Mind Broken (6000 ticks, no cure, -2 attack, Will ambush x2, rite refused; since 2026-11-06 also manipulations cost 15% more, passive blood regen runs 15% slower and the Silent Archon vestments go quiet: no incorporeality, Silent Slipping, Severance or death refusal; `MindBrokenRules`). An Abhorrent Thought's gaze drains 1 per second within 10 blocks and its grab ejects at once with Mind Broken 12000. Departure, arrival and both returns are staged cinematics through the boss scene director. Adipocere is wax tier 2 (crusts in dungeons, strongholds and ancient cities; bog bodies; bog revenants; Alembic from honeycomb). The Rite of Visitation carries an Apotheos onto their heart-node from outside the Gardens.

Hallowed ground, the ring door, Chapels and the Saint fights are the mausoleum side of the split; see §5.6g and §5.8 of the mechanics reference.

## Where it lives

| Area | Code (`src/main/java/com/vincenthuto/hemomancy/`) |
| --- | --- |
| Path, titles, ascension | `common/capability/player/harbinger/degree/HarbingerPathPermissions`, `SilentiumAscensionRules`, `DegreeTitles`, `EnumArchonPath` |
| Travel gate | `common/encounter/FungalProjectionRules.travelKind` (`DORMANT` for the Spine after the first crossing); `VisitationRules`, `VisitationService` |
| Molting | `common/capability/player/harbinger/temperament/MoltingRules`, `MoltingService`; `common/network/heartnode/MoltRequestPacket`; `client/screen/tile/functional/MoltingScreen` |
| Apotheos body | `common/capability/player/harbinger/spore/SporeLoad`, `SporeLoadRules`, `SporeLoadService`, `ApotheosBodyRules`; `common/event/ApotheosBodyEvents`; `common/effect/MorphicTonicRules`, `MorphicMendingEffect`; `client/screen/overlay/SporeLoadOverlay` |
| Network, terrain, pools | `common/world/GardensNetwork`, `GardensNetworkRules`, `RenderingPoolRules`; `common/event/GardensNetworkEvents`, `RenderingPoolEvents`; `common/entity/mob/monster/AntibodyEntity`, `HunterFungusEntity`, `HunterFungusRules` |
| Facsimiles | `common/entity/mob/facsimile/Facsimile*Entity`; `client/render/entity/mob/facsimile/`, `client/model/entity/mob/facsimile/FacsimileDeformationRules` |
| Heart-node | `common/world/HeartNodeRules`, `HeartNodeFootprint`, `HeartNodeEntry`, `HeartNodeSavedData`, `HeartNodeService`, `HeartNodeSettings`, `LobeEnvelope`; `common/block/harbinger/functional/HeartNodeBlock`, `MycelialSynapseBlock`, `FruitingBodyCacheBlock`; `common/event/HeartNodeEvents`, `HeartNodeEnvelopeSync`; `common/network/heartnode/`; `client/render/world/HeartNodeSpreadRenderer`, `HeartNodeSpreadGeometry`, `HeartNodeMomentRenderer`; `client/screen/tile/functional/HeartNodeScreen`; shader `assets/hemomancy/shaders/core/world/heart_node_spread.*` |
| Possession | `common/capability/player/harbinger/possession/Possession`, `PossessionService`, `PossessionRules`, `PossessionWheelRules`, `PossessionBodyRules`, `PossessionSceneRules`, `PossessionSequences`, `SurrogateForm`, `SurrogateTethers`, `SurrogateTetherRules`; `common/event/PossessionEvents`; `common/effect/MindBrokenEffect`, `MindBrokenRules`; `common/entity/PossessionEchoEntity`; `common/entity/mob/monster/AbhorrentGazeRules`; `client/screen/manips/PossessionRadialMenuItem`, `client/render/cinematic/scene/PossessionScene`, `client/render/world/PossessionSceneRenderer`, `PossessionTendrils` |
| Adipocere | `common/block/harbinger/plant/AdipocereCrustBlock`; `common/world/AdipocereCrustRules`, `AdipocereCrustDecoration`, `AdipocereCrustSavedData` |
| Data | `recipe/cardinal_rite/possession_rite.json`, `visitation_rite.json`; `recipe/blood_structure/mycelial_synapse.json`; `recipe/rendering/`; `recipe/morphic_nectar/bloody_jug_to_morphic_tonic.json` |
| Worldgen | `tools/worldgen/fungal_gardens_worldgen.py` (dimension biomes, `BIOME_SPAWNS` for facsimiles and hunter fungi, pools and gill curtains) |
| Tests | `MoltingRulesTest`, `MorphicTonicRulesTest`, `MindBrokenRulesTest`, `FungalProjectionRulesTest`, `SporeLoadRulesTest`, `GardensNetworkRulesTest`, `RenderingPoolRulesTest`, `HeartNode*Test` (rules, saved data, footprint, settings, spread geometry, set-piece resources, fane separation), `MycelialSynapseGameTests`, `HeartNodeScreenGameTests`, `Possession*RulesTest`, `PossessionRiteDataTest`, `AbhorrentGazeRulesTest`, `AdipocereCrustGameTests` (`adipocere_crust_validation`) |
| Review clients (`gradle/review-clients.gradle`) | `heartNodeReviewClient` (`HeartNodeClientReview` with `SynapseScenes`, `HeartNodeScreenScenes`, `HeartNodeSpreadScenes`, `HeartNodeSetPieceScenes`), `possessionReviewClient` (`PossessionClientReview`: all nine forms, wheels, bodies, every scene), `adipocereCrustReviewClient` (`AdipocereCrustClientReview`) |
| Docs | `docs/HEMOMANCY_REFERENCE.md` §5.6 to §5.6h, §5.9, §5.10; `docs/LORE_REFERENCE.md` lines 34 to 41, §3 degree table, 383 to 390, 426 to 430, 1140 to 1152; `wiki/Harbinger-Path.md` Degree 8; `wiki/The-Qliphoth.md` |

## Rulings that bind

| Date | Ruling | Source |
| --- | --- | --- |
| 2026-10-27 | D7 is Archon for all; D8 is two equal titles, Apotheos and Silentium; Silentium is reached by absorbing the Memory of Vesper, not by the fight | `docs/archive/plans/endgame/Endgame Path Asymmetry Fungal Gardens  Saint Mausoleums_V2.md` |
| 2026-10-27 | No player-data migration or alias for the `SILENT_ARCHON` rename or the `abhorrent_thought` fix: the mod is not live | same; `docs/superpowers/specs/2026-10-27-endgame-foundations-design.md` D1, D9 |
| 2026-10-27 | Severance is the player cutting their Qliphoth tree (ego death plus cutting ties with the hive), not a place; the Gardens are a 3D projection of the higher-dimensional being; sculk and the Antecedent were the hive's first failed attempt, unrelated to D7/D8 | `docs/archive/plans/endgame/Fungal Gardens Purpose & Mausoleum Structure_V2.md` |
| 2026-10-27 | Only Apotheos travel physically; Silentium by possession only; Unstained and unaligned never; possession casts the surrogate's traits into a warped Gardens facsimile | Asymmetry V2 |
| 2026-10-27 | Apotheos never eat: hunger pinned at 17, morphic tonic the only exception; the "food tastes of ash" layer is gone | Asymmetry V2 |
| 2026-10-27 | Apotheos get a personal Fane-like heart-node sanctum with respawn and a bonus; the Covenant Throne stays D6 | Asymmetry V2 |
| 2026-10-27 | The ring door is two steps: nodes light as Chapels complete, then the Silentium projects to connect them; "Focus" is renamed Concentration; beeswax is replaced by honeycomb | Gardens Purpose V2, Asymmetry V2 |
| 2026-10-29 | The heart-node is the Apotheos's PERSONAL Fane in the Gardens, stand-in for the bloodwell heart; Mycelial Synapses are a Blood Structure (hematic iron block plus Sporite Crystal) that work like stakes; only the owner benefits; a screen holds visual toggles; the visual is a ground-creeping mycelium shader, not a bubble; it coexists with the Founding Fane; the ribs go | `docs/archive/tracks/heart_node/00-README.md` |
| 2026-10-29 | Possession tendrils are thin, branching and numerous; the rite requires the caster's Blood Binding on the surrogate; a binding cast on a formed `qliphoth_grand` floor lasts until the floor stops forming or a rite on that Focus ends | `docs/archive/tracks/possession/00-README.md`; `SurrogateTetherRules` |
| 2026-10-31 | Nobody, not even the Vicar, truly knows what follows Archon; Silentium and Apotheosis are legends, NPC lines hearsay | `docs/LORE_REFERENCE.md` line 277 |
| 2026-10-31 | There is no D7 Silent Archon. All Silent Archon power (vestments, set bonus, claimed-Will bonus, death refusal) is D8 Silentium only; `SILENT_PENDING` is only the "chose silence" state; a D8 Silentium gets nothing from the hivemind (`HarbingerPathPermissions.hearsTheHive`) | `docs/LORE_REFERENCE.md` lines 386, 426 |
| 2026-11-05 | D-76 [D7-5] answered: the D7 Monolith's counsel names pressing further and the Qliphoth Seed | [the spec](superpowers/specs/2026-11-05-monolith-dialogue-hub-design.md) |
| 2026-11-06 | D-3 answered: Mind Broken makes manipulations cost 15% more, slows passive blood regen (base regen and Sanguine Surge) by 15%, and disables every Silent Archon set bonus (incorporeality, Silent Slipping, the Severance, the death refusal) while it lasts | owner, OPEN_QUESTIONS round of 2026-11-06 |
| 2026-11-06 | D-4 answered: the Morphic Tonic gives both health and blood regen (Morphic Mending), and only an Apotheos can drink it, only in the Gardens dimension or the Overworld Gardens biome | same |
| 2026-11-06 | D-5 answered: Molting is a point respec of the blood temper (the total is added up and redistributed); Apotheos only, in the Gardens, started at their heart-node; it costs a full spore load, a one-day cooldown and a tenth of the total | same, with follow-up answers in session |
| 2026-11-06 | D-7 answered: there is no Core. The Mycophant is fought and found in the Nursery, and `mycophant_heir` is the record written when she/they are beaten (`MycophantEncounterManager` already writes it on the win; nothing needs to read it) | same |
| 2026-11-06 | D-8 answered: the Fungal Spine makes only the first crossing. Afterwards it lies dormant and hurts to hold, and the Apotheos crosses by the Rite of Visitation (2 spore sacs) instead. The Spine still brings its holder home from inside the Gardens | same |
| 2026-11-06 | D-9 answered: keep the block; there is no PvP while possessing | same |

## Calls made without the user

The overnight run (2026-10-28) ruled on every number in its specs. Where the 2026-10-29 rework tracks replaced those, the track stands; the rest still stand unreviewed.

| Call | Stands? | Source |
| --- | --- | --- |
| Spore load numbers: 2.0 / 0.5 per second, burn 25 for 6 health, 100-tick cooldown, sink after 40 ticks for 2 health per 20; tonic from a Bloody Jug, Regeneration I 200 ticks; spore bar as `fill` pixel art | Stands, except the tonic: since D-4 (2026-11-06) it gives Morphic Mending, Apotheos in the Gardens only | `docs/superpowers/specs/2026-10-28-apotheos-body-design.md` |
| Facsimile roster of eight, spawn tables per biome, spore-sac-only drops, UUID-seeded part deformation on vanilla models | Stands; the fungal mannequin (armor-stand body) is still deferred | `2026-10-28-gardens-facsimiles-design.md` |
| Network alarm at 8 blocks, suspicion 12 summons 2 to 4 antibodies, puffball Nausea and Weakness, cap bridge 16 blocks withering at 600 ticks, gill bite 3, hunter fungus stats and the nine rendering recipes | Stands | `2026-10-28-gardens-network-design.md`, `2026-10-28-gardens-terrain-design.md` |
| Heart-node: radius 8 footprint, eight calcified ribs, "crowds another node" rule, bonus +4 load every 40 ticks flat | Replaced by the rework: radius-12 heart lobe, 6 radius-8 synapse lobes, envelope overlap, tapering benefit, no ribs | `2026-10-28-heart-node-sanctum-design.md` vs `docs/archive/tracks/heart_node/01-personal-fane-model.md` |
| Heart-node recipe (bleeding heart, 4 spore sacs, conscious mass), Apotheos-only planting on network ground in the dimension, forced respawn, fruiting-body cache at LOWEST `LivingDropsEvent` priority | Stands (kept by the rework) | `docs/archive/tracks/heart_node/00-README.md` "What stays" |
| Cut-off synapses keep working (no pruning); the synapse has a block item, placeable under the formation's refusals (user ruling 2026-11-02); no support rule; `withered_heart` not built; budget counted against pending feeds | Stands, flagged "review these" | `docs/archive/tracks/heart_node/02-mycelial-synapse.md` |
| Screen: vanilla buttons labelled `Name: ON/OFF`, slider notches Hint / Lace / Felt / Thick / Carpet, Strangers off by default, no operator read-only GameTest | Stands, flagged | `docs/archive/tracks/heart_node/03-heart-node-screen.md` |
| Spread: carpet lifts the face rather than skipping it, signed per-corner thread distance, strangers see the pulse ring but no flare or threads, a node broken outright vanishes with no recede | Stands, flagged | `docs/archive/tracks/heart_node/04-mycelial-spread-shader.md` |
| Set pieces: beat paces locked to the spread clock, inner glow as raised block light, owner-broken nodes drop their item, cache opening has its own moment | Stands, flagged | `docs/archive/tracks/heart_node/05-heart-node-set-pieces.md` |
| Possession offerings as loose items, Thread of Will item, `communion_lesser` floor, no cinematics, frozen never-ticked bodies | Replaced by the rework: brazier offerings on the Qliphoth grand floor with a Sanguine Quintessence medium, the possession wheel, four staged scenes, ticked bodies with form hitboxes | `2026-10-28-silentium-possession-design.md` vs `docs/archive/tracks/possession/00-README.md` |
| Rite blood cost 0 (the spec's 400 dropped: `CardinalRiteProgressionResourceTest` forbids a lump cost); the rite in the recipe map's Ancestral family; a possessed will cannot attack players; Concentration per form (bat 3 to golem 14), hit cost 1 or 2, shell 3, Mind Broken 6000 ticks | Stands | `docs/superpowers/OVERNIGHT_REPORT_2026-10-28.md` §4a |
| Gaze 1 per 20 ticks within 10 blocks, grab = Mind Broken 12000; adipocere from bog bodies (axe), 35% revenant drop, Alembic honeycomb + Ruinous Enzyme (bleeding bulb dropped), +3 max Concentration; Visitation takes 2 spore sacs in its braziers instead of the Spine's 500 blood | Stands; D-8 (2026-11-06) kept the sacs and made Visitation the Apotheos's way in after the Spine's first crossing | `OVERNIGHT_REPORT_2026-10-28.md` §4b |
| Adipocere crust block item, placement like a wall torch, rates raised about fivefold (monster room 2, stronghold chunk 0.35, ancient city chunk 0.3) | Stands | `docs/archive/tracks/possession/05-adipocere-worldgen.md` follow-up |
| Mind Broken sends no fungal whisper (the first build fired one on application) because a Silentium hears nothing | Stands; follows the 2026-10-31 ruling | `docs/HEMOMANCY_REFERENCE.md` §5.6e |

## Open questions

- Late playthrough decisions (2026-11-02), each a failing `required = false` probe with its evidence in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) and the roll-up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6 (PT- plus the number): D-64 [D6-1], D-65 [D6-2], D-70 [D6-8], D-72 [D7-1], D-73 [D7-2], D-74 [D7-3], D-75 [D7-4], D-77 [D7-6], D-78 [D7-8], D-79 [D7-9], D-80 [D8-1], D-82 [D8-3], D-89 [CHAIN-2], D-90 [CHAIN-3].

From the user's own lists (`docs/archive/plans/endgame/*_V2.md`, "Open questions"):

- Chimerization: the offering item's name and recipe. Not built; still to be decided (owner, 2026-11-06).
- Surrogate stats: exact Concentration pools, speeds and ability tuning. Built with the overnight numbers.
- Silentium D8 powers: what a Silentium gains over a D7 Archon beyond the Silent Archon gear (what Mind Broken takes was ruled on 2026-11-06).
- Spore load feeding manipulations and refilling blood, and draining colonized facsimiles. Not built (§5.6a).
- Heart-node sanctum bonus and footprint (settled by the rework) and how it shares code with the Founding Fane (settled: `LobeEnvelope`, nothing else shared).
- Hallowed Residuum fragments: how many make a whole one.
- Unstained invasion rite: far future, parked.

Found by the build:

- Should the possession rite stay in the Ancestral recipe-map family (overnight §4a)?
- Molting numbers (2026-11-06, builder's calls): whole points only, so fractions of a point go with the shed tenth; a negative temper counts against the total; the one-day cooldown resets on death like every `PlayerCooldowns` entry (a full spore load is still needed, and spores are lost at death). Morphic Mending's 8 health and 500 mL over 10 seconds are builder's numbers too.
- The Fungal Podium still carries an Apotheos bodily after the first crossing (it shares the Spine's travel code but D-8 named only the Spine). Should it go dormant too, leaving Visitation as the only way in?
- Should cut-off synapses be pruned, or keep their lobe as stakes do (heart_node track 2)?
- LORE_REFERENCE §3.3 "Transcendence (Late/DLC)" overlaps the Apotheos identity; keep, merge or retire (overnight "Canon conflicts").
- "Severance" is also used by the Unstained founder cure route and the Silent Archon armour ability "Silent Severance"; rename or accept (overnight "Canon conflicts").

## Proof owed and known gaps

- Nothing in this area has been reviewed by the user in a client. The review clients exist and `heartNodeReviewClient` passed 18 checks on 2026-10-29 (`docs/archive/tracks/heart_node/02-mycelial-synapse.md`); the screen, spread and set-piece scenes and the possession scenes were added after that and need a launch to confirm the shader compiles on a GPU.
- The GameTest server builds only the Overworld, so Apotheos and Gardens-dimension gates are proved by the review clients, not GameTests (`MycelialSynapseGameTests`).
- Heart-node deferred minors (overnight §3): Creative overflow lost on opening a cache; an ownerless cache if the block entity is missing; `/setblock` places ownerless nodes anywhere; privacy covers player break and place only; `cacheSpot` accepts air only; a reset player keeps a forced Gardens respawn; ribs from the first build stay as ordinary blocks in old worlds.
- Possession deferred minors (overnight §4a, §4b): armor-stand gear left behind is what is worn at return, not the stand's original gear; dying or `removeAllEffects` clears Mind Broken; totem uses vanilla totem particles; `PossessionRules.mayBegin` is tested but `refusal()` does not call it; `FlightArbiter` revoke ignores other flight sources; the gaze drains creative and spectator possessors; Visitation does not refuse during a Chamber visit; the bog body scrape uses vanilla wax-off particles and an axe in hand always scrapes instead of harvesting vivianite.
- Not built: the fungal mannequin body, chimerization, the Gardens digesting left gear, an echo shader, a JEI category for rendering, hunter convergence on a possessed facsimile, dedicated Silentium vessel HUD art.
- Molting, the dormant Spine, Morphic Mending and Mind Broken's costs (2026-11-06) are covered by JVM rules tests and the updated `apotheosBody` playthrough GameTest; the Molting screen and its payload have not been opened in a client.
- There is no Apotheos section in the Liber; the heart-node, synapse and screen are documented in HR §5.6d and `wiki/World-Content.md` only (heart_node tracks 2 and 3).
- Canon left for the user (overnight "Canon conflicts"): the Saints still called "post-release WIP" in several docs; the sarcophagus has no D8 gate of its own; Velorum keeps internal `MARTYR` names.

## History

- 2026-10-27: the user ruled on the two V2 design docs, now at `archive/plans/endgame/` (`Endgame Path Asymmetry Fungal Gardens  Saint Mausoleums_V2.md`, `Fungal Gardens Purpose & Mausoleum Structure_V2.md`; the V1 files are kept beside them). Section 0 (foundations) spec: `superpowers/specs/2026-10-27-endgame-foundations-design.md`.
- 2026-10-28: the overnight run built Sections 1 to 4b and the mausoleum sections from `superpowers/specs/2026-10-28-*-design.md`; its decisions and deferred minors are in `superpowers/OVERNIGHT_REPORT_2026-10-28.md`. Committed as `2ff58cc50` (2026-10-29).
- 2026-10-29: the heart-node rework (`archive/tracks/heart_node/`, tracks 1 to 5) and the possession rework (`archive/tracks/possession/`, tracks 1 to 5) replaced the first builds. Committed as `185aa3502`.
- 2026-10-31: lore rulings: no D7 Silent Archon, all Silent Archon power at D8 Silentium, `hearsTheHive`, Silentium and Apotheosis as legend to the Vicar. Committed as `ea7bd732d` (2026-11-01).
- 2026-11-01: the work-track folders were archived and this doc written to replace reading them.
