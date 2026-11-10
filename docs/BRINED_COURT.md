# Brined Court

Status (2026-11-01): all 11 tracks built, uncommitted, in the working tree since 2026-10-30; review fixes from the user's in-game pass applied 2026-10-30; full `alphaCheck` last passed at the end of Track 10's Tier 2 run (1262 GameTests) and has not been re-run since the later fixes.

## What it is

The Brined Court is the sea offshoot of the Harbinger tradition: eight houses down the ocean's depth layers, each keeping one hereditary Morphling strain, between Marimo at the sunlit top and Hydra at the vents. It practises a feeding, bodily form of hemomancy in which Morphlings are symbiotic partners, the counterpart to the Circus's mental work. Membership does not require devotion to either pole; the Court exists so the houses can stay in contact across the layers (`archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md`).

| House | Temper | Strain | Water | Presence |
| --- | --- | --- | --- | --- |
| The Giving Light | Lux | Sunburst | Erythrocoral Reef and the coast beside it | Seat: Lighthouse Garden |
| The Holding Stone | Ferric | Holdfast | Rockpool Shore | outpost: Tide Refuge |
| The Shared Wake | Animus | Mermaid's Purse | Pelagic Ocean | roaming school; rare Wake nursery |
| The Woven Current | Ductilis | Dead Man's Rope | Twilight Ocean | outpost: Line Camp |
| The Veiled Fold | Tenebris | Ink Cap | Midnight Ocean under a temperate or warm surface | outpost: Screened Cleft |
| The Still Bell | Congeatio | Hushbell | Midnight Ocean under a cold or frozen surface | outpost: The Bell |
| Last Marrow | Mortem | Marrowplume | Carrion Depths | outpost: Whale Fall |
| The Kindling | Flammeus | Kinbud | Hydrothermal Depths | Seat: Vent Sanctum |

What is built, in the order the tracks landed:

- **Strains.** One table, `MorphlingStrains`, drives every per-strain list. Each strain has a passive, a Developing, Mature, Apex and Primal ladder, a home condition (faster bonding inside the house's water or stance), and shared mechanics: damage share, links, a regrowth queue, anchoring, the Stung effect, a Morphling lock, field patches and Kinbud's Second Head as a body part. Death-saves are Ink Cap's Ink Reprieve at Apex and Hushbell's Cryptobiosis at Primal. Numbers are in `MORPHLING_STRAINS.md`; the balance bands are constants checked by `MorphlingBalanceBandsTest`. The eight wild polyp families are named for each strain's partner organism.
- **Strain visuals.** Generated multi-anchor attachments (16 anchors) that grow stage by stage, move with the synced active bits, and dress Court NPCs as well as players; world effects (Rope strands, field patches, grip strands, the dissolve, the frost veil) from `WorldEffectDispatcher` on the shared toolkit; redrawn HUD sprites.
- **Strain text.** The Order's catalogue voice always shows; the house line in the tooltip and the Bestiary's Court paragraph show only once the Court is known. Three Liber pages, eight Alchemist inquiries.
- **Discovery.** Three routes converge on one `CourtProgress` "known" state: the Vicar's D5 referral to a Voyager, a Brine-Sealed Letter dropped once by a Brined Votary killed by a player who does not yet know the Court, and the Alchemist at D5 with a Mature strain. Entering any Court structure also counts. The Relay Chart points at the nearest Lighthouse Garden and is searched off the server thread (`HEMOMANCY_REFERENCE.md` §29, "Court discovery").
- **Placement.** Two structure sets: Seats (spacing 64, separation 24) and outposts (spacing 28, separation 10, excluded round Seats), gated behind Pelagic worldgen, placed through a pure predicate on the biome source and terrain sampler with no chunk loads. Signs (Sunburst blooms, house markers, relay markers, messenger buoys, vent offerings) are one capped placed feature. Seeds 42, 1337 and 8675309 validate (`PELAGIC_WORLDGEN.md`, "Brined Court placement").
- **Seats and outposts.** Jigsaw templates built by Python generators with set-piece blocks: the Lighthouse Garden (tide rock, living Sunburst lamp, terraces, arbors, guest house, undercroft down to Marimo's pool and the petrified followers) and the Vent Sanctum (processional ring, dry halls, Hydra's body, runtime crowns and basins on the terrain's own chimneys); six outposts, each with a house marker. Every set piece the user named has a creative item in the Brined Court section, as do Marimo's moss and heart; the Warden's bloom stays itemless; none drops.
- **People.** 16 role types and 2 leads on the jointed Hemomancy rig, each with a generator, `_anim.java`, portrait, crest, voice sheet, lines and inquiries. Members are protected, breathe underwater, spawn from resident marks when a site generates, and are replaced on a one-day timer only after `/kill` or the void. The Shared Wake is a moving school of four to six, launched from a nursery or naturally (one region in three, never within 256 blocks of a Seat). Hospitality: food fed directly, rest, a one-time welcome gift per house, each house's custom, directions, the lesson and joining.
- **Marimo and Hydra.** Presences only: Marimo is the moss mass and heart laid at runtime in the undercroft, with a chorus spoken by the anchored; Hydra's largest head rises from its chimney when the Head-tender calls it. Both have a first-sight scene and no boss bar. Fights are against followers: the Bloom Warden (woken only by a wrong against the Giving Light; fights through light-fed blooms; a held food offering stops it and makes amends) and the Kindled Champion (a title the player may usurp through the Head-tender once the gate opens; the old Champion yields and lives; Hydra's head crowns the victor).
- **Court life.** Server state behind `CourtLife`: one free house entry and one free switch (D5 plus guest standing, no strain needed); per-house standing of stranger, guest, friend, raised by a visit and by the house's custom, once per act; the Lightkeeper's D5 lesson (x1.5 bonding credit when a strain is fed inside its own house's site); the Giving Light transgression record kept through death; the Champion title and its one-day retry wait (refused first, ahead of the strain and standing gates); Unstained players shunned by every house, never attacked.

## Where it lives

| Area | Path |
| --- | --- |
| Strain table, rules, powers, items | `src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/morphlings/` (`MorphlingStrains`, `MorphlingStrain`, `CourtHouse`, one `<Strain>Rules` and `<Strain>MorphlingItem` per strain, `MorphlingBalanceBands`, `MorphlingActiveState`) |
| Court player state | `common/capability/player/harbinger/court/` (`CourtProgress`, `CourtLife`, `CourtDiscoveryProgress`, `CourtKnowledge`, the `*Rules` classes) |
| Placement, templates, residents | `common/worldgen/structure/court/` (`CourtPlacementRules`, `CourtSite`, `CourtStructure`, `CourtSeatCandidates`, `CourtResidentSpawner`, `VentSanctumHeads`, `CourtPresencePlacement`); signs in `common/worldgen/feature/court/` |
| NPCs, school, presences | `common/entity/npc/court/` (`CourtMemberEntity`, `CourtRole`, `CourtHospitality`, `CourtSchools`, `CourtPresences`, `CourtTransgressions`, `CourtVacancies`, `HydraRisingHead`) |
| Follower minibosses | `common/entity/boss/court/` (`BloomWardenEntity`, `KindledChampionEntity`, their rules, `KindledChampionTrial`) |
| Client models | `client/model/entity/npc/court/`, `client/model/entity/morphling/`, `MorphlingStrainWorldRenderer` |
| Generators | `tools/model_export/morphling_strains.py`, `court_humanoid.py` plus one `court_<role>.py` and `_anim.java` per role, `court_npc_icons.py`, `court_kit.py`, `court_outpost_kit.py`, `marimo_moss.py`; templates from `tools/worldgen/brined_court_seats.py` and `brined_court_outposts.py` over `tools/worldgen/brined_court/` |
| Data | `data/hemomancy/worldgen/structure/`, `structure_set/`, `structure/brined_court/`, tags `court_offering/<house>`, `court_growth/giving_light`, structure tag `hemomancy:brined_court`, inquiries under `dialogue_inquiry/court_<role>/` |
| Review clients | `gradle/review-clients.gradle`: `morphlingStrainReviewClient`, `brinedSeatReviewClient`, `brinedOutpostReviewClient`, `courtNpcReviewClient`, `courtPresenceReviewClient` |
| GameTests | `court_worldgen_validation` (seeded, `runCourtValidationServer`), `morphling_strain_validation`, `court_discovery_validation`, `court_life_validation`, `court_npc_validation`, `court_presence_validation`; rows in `combat_targeting_validation` and `dialogue_route_validation` |
| Docs | `HEMOMANCY_REFERENCE.md` §16 and §29; `LORE_REFERENCE.md` §6.3c and the Court entries; `MORPHLING_STRAINS.md`; `NPC_VOICES.md` "The Brined Court"; `wiki/Brined-Court.md`; terrain in `PELAGIC_WORLDGEN.md` and `PELAGIC_ECOLOGY.md` |

## Rulings that bind

From the user, 2026-10-20 to 2026-10-31 (`archive/tracks/brined_court/00-README.md`, "Decisions"; the spec's "Decided" lists).

- **No migration.** The mod is not live; the eight first-attempt strains were deleted outright. No aliases, no `PlayerDataMigrations` entry, no save fixtures.
- **Marimo and Hydra are never fought.** They are presences with no hitbox, no damage and no boss bar. Every fight is against a follower in the pole's image.
- **Court NPCs use the jointed Hemomancy rig**, never a vanilla humanoid. They are protected for now.
- **No copper, brass or bronze on anything blood-side.** Bronze-age Sea Peoples imagery stays in cloth, bone, stone and lacquer. No modern diving tech. The Court's light is biological.
- **Gift economy, not prices.** NPCs give food, rest, cultivation help, gifts, inquiries and directions. The later NPC trade system keeps this: Court cards are customs that earn standing, gifts unlock by standing, never a price (`archive/tracks/npc_trade/00-README.md`, Q-1).
- **Strains:** one table; ids `morphling_sunburst`, `_holdfast`, `_mermaids_purse`, `_dead_mans_rope`, `_ink_cap`, `_hushbell`, `_marrowplume`, `_kinbud`; Mermaid's Purse keeps the red egg-case icon and grows a sailfish-and-purse hybrid; secondary tempers follow the gradient; eight wild polyp families named for the partner organism; a strain's house line shows only once the Court is known; build at first-pass numbers and tune in play inside the bands.
- **World:** no Court structures when Pelagic worldgen is off; outposts 28/10, Seats 64/24, one faction landmark per 96 blocks as starting values; the tube coliseum is the Garden's undercroft with Marimo at the bottom; Court sites may be visited before D5, residents feed and talk but do not teach; the names are Relay Chart and Brine-Sealed Letter, and Votaries are drowned would-be envoys.
- **People:** Shared Wake schools also spawn naturally, rarely, capped, never near a Seat.
- **Followers:** every listed act wakes the Bloom Warden (breaking Garden growth, taking from the undercroft, harm at residents or in the undercroft, a second gift or others' food); amends are an offering held in hand, and offerings to the Giving Light are foods; the Champion title gives recognition only for now, elders challenging the player comes later; losing either fight is death with a later retry.
- **Court life:** one free entry plus one free switch; the first lesson is faster growth when a strain is fed inside its own house's site; Hydra judges worthiness by which follower its head crowns; messengers and currents are set dressing only; no strain is needed to join, the house's strain is needed for its lesson; joining needs D5 plus guest standing; Court and Circus combine, but Puppet Interference (x1.25 upkeep) still applies and cancels Sunburst's Sun-Fed waiver; Unstained players may visit and are shunned by every house.
- **Lore:** fungal symbiosis, not mermaids; strains are fungi convergent on animals, never the animal; anchoring is reversible, true petrification is not; no God or Satan framing for Marimo or Hydra; Dead Man's Rope is never "many-headed" (`LORE_REFERENCE.md` §6.3c).
- **D-13 (2026-11-06):** keep the Wake nursery beside the natural schools; the Seat names and the eight house marks stay as built.
- **Review fixes the user asked for (2026-10-30):** the jar, polyp and strains back in the Brined Court creative section; polyp ocean spawns throttled (one try in 40, sturdy seabed, 48-block spacing); the Wake nursery widened so it generates and searches only within 8 blocks of its `/locate` chunk; creative items for every set piece; portraits and crests for every role.

## Calls made without the user

Each track's "Calls made unattended" has the full list; these are the ones that change what a player meets.

| Call | Where |
| --- | --- |
| The Lighthouse Garden raises its own tide rock and is pinned to the sea surface rather than requiring a natural coast; every outpost likewise brings its own gully, canyon face or rock stack. | `archive/tracks/brined_court/07-seats.md`, `08-outposts-and-signs.md` |
| Mermaid's Purse's +35% is a ceiling on total swim speed together with the Prismatic set; inside the full set the strain adds nothing. | `02-strain-powers.md`, third run |
| Helmets win over head growths below Primal; at Primal the replacement takes the head and the helmet renders over it, unscaled. | `03-strain-visuals.md` |
| Other players always see each other's Morphling mutations; no client setting. | `03-strain-visuals.md` |
| Each house's custom: food (Giving Light), stone (Holding Stone), raw fish (Shared Wake), string (Woven Current), ink sac or black dye (Veiled Fold), wool (Still Bell), bone or rotten flesh (Last Marrow); accepted every time, counted once. Welcome gifts are vanilla items kept outside each custom tag. | `09-court-npcs.md` |
| Food is fed directly, only when hungry; rest heals 8 once a minute per member; nothing is farmable. | `09-court-npcs.md`, Seats run |
| The Lightkeeper and Head-tender wear their strains at Apex; the Kindled elder and Rooted elder wear no strain layer. | `09-court-npcs.md` |
| Only `/kill` and the void open a replacement vacancy; a Creative blow is a deliberate removal. | `09-court-npcs.md`, Holding Stone run |
| One school per 1024x1024 region, ever, across nursery and natural starts. | `09-court-npcs.md`, Shared Wake run |
| Marimo's moss is laid at runtime by `afterPlace`, not in the template; first sight is once per player and durable. | `10-marimo-and-hydra.md`, Tier 1 |
| The Head-tender calls the head on first approach within 12 blocks, not through a dialogue option. | `10-marimo-and-hydra.md`, Tier 1 |
| Amends with the Warden is using the offering on it; "a second gift" has no hook yet (act id reserved); "raising a weapon" counts only when harm reaches a resident; reaching for unbreakable Marimo blocks counts on the first swing. | `10-marimo-and-hydra.md`, Tier 2 and review |
| Warden 160 health, Champion 220 health and armour 6; the Champion exists only during a fight and steps back onto the elder's mark afterwards; bars show only to the player in the fight. | `10-marimo-and-hydra.md`, Tier 2 |
| The lesson's x1.5 multiplies equipped bonding credit, including Sun-Fed's waived upkeep; Incubator enzyme feeding is unchanged. | `11-court-life-and-progression.md` |
| The Champion gate reads "Kindling standing" as friend; the retry wait is server game time, one in-game day, kept through death. | `11-court-life-and-progression.md` |
| A transgression drops only Giving Light standing to stranger; amends restore guest, not friend. The PROPOSED member home-bonus extra was not built. | `11-court-life-and-progression.md` |
| The Vicar's referral records nothing; only the Voyager, letter or Alchemist route and a found site record "known". The Mnemonist does not hint at the Court. | `05-court-discovery.md` |
| Relay Chart search: 6 Seat rings each way, cached per dimension and Seat cell, one search per player at a time. | `05-court-discovery.md` |
| Midnight's cold split is sea-level temperature below -0.15; the Wake nursery keeps one valid cell in 6 after the review widening. | `06-ocean-placement-rules.md`, `00-README.md` |
| The Screened Cleft is flooded throughout (no airlock exists); the Bell is the deep outpost with air. | `08-outposts-and-signs.md` |

## Open questions

- Late playthrough decisions (2026-11-02), each a failing `required = false` probe with its evidence in [PLAYTHROUGH_TESTS.md](PLAYTHROUGH_TESTS.md#open-questions) and the roll-up in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) §1.6 (PT- plus the number): D-45 [BC-1], D-46 [BC-2], D-47 [BC-3], D-48 [BC-4], D-49 [BC-5], D-50 [BC-6], D-51 [BC-7], D-52 [BC-8], D-53 [BC-9], D-54 [BC-10], D-55 [BC-12].

| Question | Source |
| --- | --- |
| Should the Lighthouse Garden require a true shoreline instead of raising its own tide rock? | `archive/tracks/brined_court/07-seats.md` |
| Is the Purse's +35% a ceiling on the total with the Prismatic set (as built) or the strain's own share? | `02-strain-powers.md` |
| Ink Cap's cap still shows above its Primal hood; Holdfast and Sunburst read small at Mature; Rope strands are faint in daylight. | `03-strain-visuals.md`, "Left for later" |
| Should Morphling visuals on other players respect a client setting? | `03-strain-visuals.md` |
| Does Sun-Fed's hunger-to-blood surplus count underwater near a daylit surface? | `02-strain-powers.md` |
| Is the threshold chimney ever usable by the player, and what happens to someone who enters Hydra? | `07-seats.md`, spec "Still open" 11 |
| Lore only: when and why a Marimo follower petrifies; the fate of Last Marrow elders when a fall is exhausted; Court customs for passage and disputes; how a house receives a migrant and whether they take its strain. | `11-court-life-and-progression.md`, spec "Still open" 3, 4, 5, 8 |
| Deferred by ruling: elders challenging a title-holding player (T3); the Tier 3 conflict between the poles; Sleeper waking as a communal event; house-specific greetings for other houses' members; Court lines rewritten in a house voice. | `10-marimo-and-hydra.md`, `09-court-npcs.md`, `04-strain-lore-and-docs.md` |
| In-play tuning of every first-pass number inside the bands (M8). | `02-strain-powers.md`, `MORPHLING_STRAINS.md` |

## Proof owed and known gaps

- **Not run:** a full `alphaCheck` since the 2026-10-30 review fixes (last full pass was Track 10's Tier 2 run); the chart timing on seed 8675309; `court_worldgen_validation` after Tracks 9 and 10 (placement unchanged, residents and presences are placed in `afterPlace`).
- **Not walked by hand:** no fresh dev world has been walked end to end. Equip, passive, incubation, discovery, joining, the lesson and both fights are proven server-side by GameTests; the HUD, Bestiary dossier, Liber pages and tooltip text were never captured in a client.
- **Multiplayer:** the strand sync was proven live with two driven clients (`build/mp-verify/strands-run*`) for Track 2 only. No two-client check of Court NPC synced poses, the Woven Current strands or the school.
- **No resident backfill:** outposts generated before Track 9's deeper-houses run stay empty; residents spawn only in `afterPlace`.
- **Not re-shot:** the Bloom Warden review framing after the camera was recentred.
- **Unfinished small pieces:** effect icons for `stung`, `pinned` and `morphling_sealed` are placeholder copies; polyp appendage geometry per family is Track 1's placeholder; first-person hand rendering of arm growths was not touched; the Head-tender's challenge event has no row in the dialogue-route fingerprints; school state is not synced, so the client shows only the raised sail.
- **Mixed-build hazard:** `CourtSeatCandidates.search` over the outpost set can predict a cell the set's `exclusion_zone` then refuses; nothing that ships uses it, but it is noted for the placement owner (`08-outposts-and-signs.md`).

## History

- Concept and powwow: `archive/concept/Underwater faction ideas/` (the original Marimo notes, the 2026-10-06 powwow, the 2026-10-04 house plates with the old strain names), superseded by the spec where they disagree.
- Working design, decisions of 2026-10-20 and 2026-10-29: `archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md`.
- Track plan, decision roll-up and the 2026-10-30 review fixes: `archive/tracks/brined_court/00-README.md`; session prompts in `SESSION_PROMPTS.md`.
- The eleven build tracks, each with its Status, handover and unattended calls: `01-strain-table-and-replacement.md`, `02-strain-powers.md`, `03-strain-visuals.md`, `04-strain-lore-and-docs.md`, `05-court-discovery.md`, `06-ocean-placement-rules.md`, `07-seats.md`, `08-outposts-and-signs.md`, `09-court-npcs.md`, `10-marimo-and-hydra.md`, `11-court-life-and-progression.md`.
- The first-attempt strains, as history only: `archive/done/CURRENT_MORPHLING_STATS_AND_ABILITIES.md`. Strain icons and HUD exporter: `archive/concept/output/brined-strain-icons-16x16/`.
- Boundary with the Circus: `archive/tracks/circus/06-underwater-boundary-and-deferred-work.md`. Trading that came after: `archive/tracks/npc_trade/`.
