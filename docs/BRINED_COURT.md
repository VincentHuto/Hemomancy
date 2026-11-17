# Brined Court

Status (2026-11-15): all 11 tracks built and committed (`62f5b38da`, 2026-10-30); the changes since 2026-11-13 are in the working tree, uncommitted; review fixes from the user's in-game pass applied 2026-10-30. The owner's answers of 2026-11-13 and 2026-11-14 are recorded below. The overnight build of 2026-11-14 (package P14) built the rule fixes, and package B26 (2026-11-15) built the Court line pass, the last Champion logout gap and the school's sail flush. Left to build: the strain look fixes and three effect icons. Left for the owner: one fresh-world session and the tuning pass.

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
- **Discovery.** Three routes converge on one `CourtProgress` "known" state: the Vicar's D5 referral to a Voyager, a Brine-Sealed Letter dropped once by a Brined Votary killed by a player who does not yet know the Court, and the Alchemist at D5 with a Mature strain. Entering any Court structure also counts. The Relay Chart points at the nearest Lighthouse Garden and is searched off the server thread; a Voyager reissues it from the letter's inquiry too. With Pelagic worldgen off no route points seaward and no letter drops (`HEMOMANCY_REFERENCE.md` §29, "Learning of the Court").
- **Placement.** Two structure sets: Seats (spacing 64, separation 24) and outposts (spacing 28, separation 10, excluded round Seats), gated behind Pelagic worldgen, placed through a pure predicate on the biome source and terrain sampler with no chunk loads. Signs (Sunburst blooms, house markers, relay markers, messenger buoys, vent offerings) are one capped placed feature. Seeds 42, 1337 and 8675309 validate (`PELAGIC_WORLDGEN.md`, "Brined Court placement").
- **Seats and outposts.** Jigsaw templates built by Python generators with set-piece blocks: the Lighthouse Garden (tide rock, living Sunburst lamp, terraces, arbors, guest house, undercroft down to Marimo's pool and the petrified followers) and the Vent Sanctum (processional ring, dry halls, Hydra's body, runtime crowns and basins on the terrain's own chimneys); six outposts, each with a house marker. Every set piece the user named has a creative item in the Brined Court section, as do Marimo's moss and heart; the Warden's bloom stays itemless; none drops.
- **People.** 16 role types and 2 leads on the jointed Hemomancy rig, each with a generator, `_anim.java`, portrait, crest, voice sheet, lines and inquiries. Members are protected, breathe underwater, spawn from resident marks when a site generates, and are replaced on a one-day timer only after `/kill` or the void. The Shared Wake is a moving school of four to six, launched from a nursery or naturally (one region in three, never within 256 blocks of a Seat). Hospitality: food fed directly, rest, a one-time welcome gift per house, each house's custom, directions, the lesson and joining. Directions name each house's site, the Giving Light's name the Vent Sanctum, and three speakers point to the Wake nursery. From the sixth Degree the Gardener, Lightkeeper and Head-tender greet the player's late road; nine speakers answer a worn Primal strain, and the Lightkeeper and Head-tender explain the Primal step to a player with an Apex or Primal strain.
- **Marimo and Hydra.** Presences only: Marimo is the moss mass and heart laid at runtime in the undercroft, with a chorus spoken by the anchored; Hydra's largest head rises from its chimney when the Head-tender calls it. Both have a first-sight scene and no boss bar. Fights are against followers: the Bloom Warden (woken only by a wrong against the Giving Light; fights through light-fed blooms; a held food offering stops it and makes amends) and the Kindled Champion (a title the player may usurp through the Head-tender once the gate opens; the old Champion yields and lives; Hydra's head crowns the victor).
- **Court life.** Server state behind `CourtLife`: one free house entry and one free switch (D5 plus guest standing, no strain needed; the switch sits behind a warning that it spends the one move); per-house standing of stranger, guest, friend, raised by a visit and by the house's custom, once per act, so that a custom and a visit make a friend in either order; the Lightkeeper's D5 lesson (x1.5 bonding credit for a strain worn inside its own house's site; every line that teaches it says "wear", not "feed", PT-51); the Giving Light transgression record kept through death; the Champion title (D5, behind the Head-tender's warning) and its one-day retry wait after a death, a logout or leaving the fight, wherever the challenger is when they log out (refused first, ahead of the strain and standing gates); Unstained players shunned by every house, never attacked. The custom pays the same rapport by dialogue as by its trade card. Court members can't be leashed.

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
- **CR-20 (2026-11-13):** the Kindled Champion regrows 2, 3 and 3 health a second by crown stage (`KindledChampionRules.REGROWTH`; it was 4.5 at the last stage).
- **CR-21 (2026-11-13):** the Bloom Warden out-healing a D0 to D2 player is intended; the answer is amends (a Giving Light offering ends the fight).
- **D-13 (2026-11-06):** keep the Wake nursery beside the natural schools; the Seat names and the eight house marks stay as built.
- **PT-45 (2026-11-13):** standing does not depend on the order of acts; a custom and a visit make a friend either way, and two visit-kind acts alone stop at guest.
- **PT-46 (2026-11-13):** escaping a usurpation (a logout, a dropped connection, or leaving the fight) costs the same one-day wait as dying.
- **PT-47 (2026-11-13):** a confirm step that names the cost comes before the free switch is spent.
- **PT-48 (2026-11-13):** a confirm step comes before the Champion steps out; the Head-tender's warning names falling and leaving.
- **PT-49 (2026-11-13):** another resident points the way to the Shared Wake's nursery. The pointer is spoken lines, not a chart mode (NQ-14-7). Built 2026-11-15: the Gardener's and Lightkeeper's `directions.line1` and the Edge-swimmer's `directions.line2`.
- **PT-50 (2026-11-13):** residents name the places: wherever a directions line names a house it names that house's site, and the Giving Light speakers name the Vent Sanctum. Built 2026-11-15 (`CourtMemberDialogueTest.directionsNameEachHousesSite`).
- **PT-51 (2026-11-13):** the Lightkeeper's feeding line is reworded to the real bonus: a strain worn inside its own house's site bonds x1.5. The same wording goes for the system line and the twelve house inquiries that repeat it. Built 2026-11-15.
- **PT-52 (2026-11-13):** the Kindled Champion challenge needs the fifth Degree.
- **PT-53 (2026-11-13):** with Pelagic worldgen off, the Vicar, Voyager and Alchemist hide their Court pointers and no Votary drops a letter.
- **PT-54 (2026-11-13):** Court speakers get lines past the fifth Degree and about the Primal step. Built 2026-11-15: `degree.late` from the sixth Degree at three speakers, `strain.primal` at nine, and a Primal topic at the Lightkeeper and the Head-tender.
- **NQ-14-6 (2026-11-14):** the Court reaches Primal by a lifetime in home water and knows the land road (Morphic Nectar, after the eighth Degree) only by report. The Court is not the keeper of the Nectar.
- **NQ-14-2 (2026-11-14):** a Marimo follower takes true petrification as a chosen, witnessed step at the end of a long anchoring. Until that step every one of them can still walk away.
- **NQ-14-3 (2026-11-14):** when a whale fall is exhausted, each of Last Marrow's rooted elders chooses: uproot and move with the house, or stay and feed the ones after.
- **NQ-14-4 (2026-11-14):** a house's custom buys passage through its water, and in a dispute the wronged house judges, as the Giving Light does with its Warden and amends.
- **NQ-14-5 (2026-11-14):** a house takes in a migrant as built: no strain is needed to join, the house's strain is needed only for its lesson, and the house left keeps the player as a guest.
- **NQ-14-7 (2026-11-14):** the nursery pointer is spoken lines only, with no Relay Chart mode; the PR-6 walk judges whether it is enough.
- **PT-55 (2026-11-13):** Court members cannot be leashed.
- **PT-56 (2026-11-13):** the custom pays the same rapport by trade card and by dialogue, every time it is given.
- **CD-1 (2026-11-13):** the Brine-Sealed Letter's inquiry at a Voyager offers the Relay Chart again once the Court is known and no chart is carried.
- **CD-4 (2026-11-13):** the Braced elder's Holdfast inquiry line no longer says "In time".
- **CD-13 (2026-11-13):** the Wayfarer keeps naming "the Court" at D5 and above.
- **HN-1 (2026-11-13):** the Lighthouse Garden keeps raising its own tide rock.
- **HN-2 (2026-11-13):** Mermaid's Purse's +35% is the strain's own share and no longer shrinks inside the Prismatic or Phantasmal Bloodlust set; the two multiply (x1.82 for a Primal Purse at home inside the set, for PR-30).
- **HN-3 (2026-11-13):** Sun-Fed's surplus counts underwater within 5 blocks below a sky-lit surface in daytime, as built.
- **HN-4 (2026-11-13, built 2026-11-16, B36):** the strain looks are fixed in `tools/model_export/morphling_strains.py`. Ink Cap's cap and crown scales stop at Apex (a part may now end at a stage, `MorphlingPartSpec.until`), so only the hood shows at Primal. Holdfast's Mature root band carries five stone knots (`forearm_knots`). Sunburst's Mature back crust is one unit thick and larger (8 by 7.5), and its frond buds stand 4.75 tall instead of 2.75. Dead Man's Rope ribbons are deeper and less see-through (alpha 215, was 175), and every rope strand and the Primal train have a dark ink thread under their glow (`STRAND_INK` 0.6), so they read against a day sky. The owner judges the before and after shots.
- **HN-8 (2026-11-13):** the Mnemonist does not hint at the Court.
- **RV-7 (2026-11-13):** keep the Court's built numbers: rest heals 8 once a minute, food only when hungry, one school per 1024 region, Head-tender range 12, Champion 220 health and armour 6, Warden 160, polyp ocean odds 1 in 40.
- **RV-41 (2026-11-13):** keep school followers folding into a line along a shelf.
- **RV-78 (2026-11-13):** keep Midnight's cold split, the nursery rate, the flooded Screened Cleft and the Bell with air.
- **RV-79 (2026-11-13):** keep: a wrong drops only Giving Light standing, amends restore guest, and the Champion gate needs Kindling friendship.
- **D-22 (2026-11-13):** the threshold chimney is not enterable, and other players always see each other's Morphling mutations. The old client toggle `renderMorphlingMutationLayer` only hides the layer on the viewer's own screen and stays.
- **FX-19 (2026-11-13):** fix the three Court minors together. The Head-tender's challenge has its route fingerprints and school state is synced (built 2026-11-14). The school's flush is built (2026-11-15, FX-19a): a second emissive pass over the sail while the `SHARED_WAKE` bit is on, for a schooling Edge-swimmer and for a player holding the Wake open. The three effect icons are not built yet. The three effect icons are their own 18 px art (built 2026-11-16, B36, `tools/model_export/effect_icons.py`): a barbed stinger in skin for `stung`, an iron stake through brine roots for `pinned`, a vivianite jar under a blood-wax seal for `morphling_sealed`.
- **PT-46 gap (14 T1, built 2026-11-15):** `KindledChampionTrial.facing` searches every loaded level at any distance, so a challenger who logs out more than 64 blocks from the Champion inside the grace is still charged the day and hears the forfeit.
- **PR-6 (2026-11-13):** one owner session covers the fresh-world walk, the two-client check and the full test run.
- **PR-30 (2026-11-13):** the first-pass numbers are tuned in play after PR-6.
- **D-12 (2026-11-13):** elder challenges and fuller Court trading stay parked; the Champion title gives recognition only.
- **HN-6 (2026-11-13):** still deferred: elders challenging a title-holder, the Tier 3 conflict between the poles, Sleeper waking, house greetings, Court lines in a house voice.
- **NQ-16-3 A (2026-11-14, built 2026-11-16, B34):** the Court has a Liber chapter, `brined_court` (The Brined Court), with one house page (The Eight Houses) and a page each for the Bloom Warden and the Kindled Champion. The house page opens when the player comes to know the Court by any route (`CourtDiscoveryProgress.markKnown`; a later route repairs a missed page). Each boss page opens for the player when that fight begins. `CourtDiscoveryGameTests.theCourtChapterOpensAsTheCourtIsMet` pins it.
- **HN-7 (2026-11-13):** Court rapport is derived from standing after alpha; until then standing and trade rapport stay two ladders.
- **NQ-14-1 (2026-11-14, accepted default):** the client toggle `renderMorphlingMutationLayer` stays. It only changes the viewer's own screen, so D-22 (nobody hides their mutations from others) holds.
- **PT-47a (2026-11-14, accepted default):** the house switch warning stays one shared narration line, `hemomancy.dialogue.court.confirm.switch`, not one per lead.
- **PT-53a (2026-11-14, accepted default):** with Pelagic worldgen off, the Wayfarer's D5 line and the two Circus lines still name the Court. A world made with Pelagic on and then switched off keeps its sites and hides the pointers.
- **Review fixes the user asked for (2026-10-30):** the jar, polyp and strains back in the Brined Court creative section; polyp ocean spawns throttled (one try in 40, sturdy seabed, 48-block spacing); the Wake nursery widened so it generates and searches only within 8 blocks of its `/locate` chunk; creative items for every set piece; portraits and crests for every role.

## Calls made without the user

Each track's "Calls made unattended" has the full list; these are the ones that change what a player meets.

| Call | Where |
| --- | --- |
| Helmets win over head growths below Primal; at Primal the replacement takes the head and the helmet renders over it, unscaled. | `03-strain-visuals.md` |
| Each house's custom: food (Giving Light), stone (Holding Stone), raw fish (Shared Wake), string (Woven Current), ink sac or black dye (Veiled Fold), wool (Still Bell), bone or rotten flesh (Last Marrow); accepted every time, counted once. Welcome gifts are vanilla items kept outside each custom tag. | `09-court-npcs.md` |
| The Lightkeeper and Head-tender wear their strains at Apex; the Kindled elder and Rooted elder wear no strain layer. | `09-court-npcs.md` |
| Only `/kill` and the void open a replacement vacancy; a Creative blow is a deliberate removal. | `09-court-npcs.md`, Holding Stone run |
| Marimo's moss is laid at runtime by `afterPlace`, not in the template; first sight is once per player and durable. | `10-marimo-and-hydra.md`, Tier 1 |
| The Head-tender calls the head on first approach within 12 blocks, not through a dialogue option. | `10-marimo-and-hydra.md`, Tier 1 |
| Amends with the Warden is using the offering on it; "a second gift" has no hook yet (act id reserved); "raising a weapon" counts only when harm reaches a resident; reaching for unbreakable Marimo blocks counts on the first swing. | `10-marimo-and-hydra.md`, Tier 2 and review |
| The lesson's x1.5 multiplies equipped bonding credit, including Sun-Fed's waived upkeep; Incubator enzyme feeding is unchanged. | `11-court-life-and-progression.md` |
| The PROPOSED member home-bonus extra was not built. | `11-court-life-and-progression.md` |
| The Vicar's referral records nothing; only the Voyager, letter or Alchemist route and a found site record "known". | `05-court-discovery.md` |
| Relay Chart search: 6 Seat rings each way, cached per dimension and Seat cell, one search per player at a time. | `05-court-discovery.md` |
| The warning before a switch is one shared narration line (`hemomancy.dialogue.court.confirm.switch`), not a line from each of the eight leads. | P14, 2026-11-14 |
| The challenge's confirm step shows the Head-tender's `challenge.line2`. The reply after it reads only `challenge.line1`, reworded on 2026-11-15 to fit a Champion already called ("Then it is asked, petitioner, and the Champion rises..."). | P14, 2026-11-14; B26 |
| Leaving a usurpation covers a logout, a dropped connection, swimming out of range and changing dimension. A server stop mid-fight most likely costs the day too (not tested). | P14, 2026-11-14 |
| An act's kind is read from its id: an id that starts with `custom` is a custom, anything else is a visit. All eight houses' custom ids do. | P14, 2026-11-14 |
| The dialogue custom pays the card's rapport (2 today) every time it is given, as the card does. | P14, 2026-11-14 |
| The Voyager's letter inquiry offers the chart through a new option ("Could you draw me a Relay Chart to the Court?"). The Voyager's own second line was left as it was. | P14, 2026-11-14 |
| With Pelagic worldgen off, the Wayfarer's D5 line and the two Circus lines still name the Court. A world made with Pelagic on and then switched off keeps its sites and hides the pointers (not tested). | P14, 2026-11-14 |
| School state is synced as entity data and a schooling Edge-swimmer carries the `SHARED_WAKE` bit. `MorphlingStrainModel` draws it as a second emissive pass over the sail at 0.55 alpha, swelling by 0.3 on a slow sine, for the owner to judge. | P14, 2026-11-14; B26 |
| The late greeting starts at the sixth Degree (`CourtMemberDialogue.LATE_DEGREE`). A Primal strain replaces the own-strain or other-strain line at the nine roles that have a `strain.primal` line; the other roles keep theirs. The Primal topic shows only to a welcome player with an Apex or Primal strain. | B26, 2026-11-15 |
| Strain passive icons are hidden from the effect row while the Morphling jar shows (`MorphlingEffectClientExtensions`), so the row shows only effects such as Stung, Pinned and Sealed. | living HUD, 2026-11-13 |

## Open questions

Open questions live in [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md). None belongs to this doc alone now. The shared rule for a lost unique NPC, which decides whether a Court lead struck down in Creative returns (PT-10, NQ-10-4), is tracked there with early and mid progression.

## Proof owed and known gaps

- **Run on 2026-11-14 (P14):** the unit suite (6,027 tests, no failure), the combined GameTest run (1,912 tests; it holds the four Court suites, `morphling_strain_validation` and `dialogue_route_validation`; only the three baseline failures, none a Court fault), the playthrough suite (all 430 required passed) and `court_worldgen_validation` (`runCourtValidationServer`, fresh `-PcourtRun=p14` worlds) on seeds 42, 1337 and 8675309, chart timing included. Seed 42 found no Tide Refuge or Bell in its search, and 8675309 no Tide Refuge.
- **Run on 2026-11-15 (B26):** one full `alphaCheck` (green: the unit suite and all 1,990 combined GameTests), the playthrough suite (all 440 required passed) and `courtNpcReviewClient` with two new night shots of the school.
- **Not run:** the other four Court review clients (`morphlingStrainReviewClient`, `brinedSeatReviewClient`, `brinedOutpostReviewClient`, `courtPresenceReviewClient` with the Bloom Warden re-shot after the camera was recentred), and the HUD, Bestiary dossier and tooltip text, which no Court harness captures yet. The three Court Liber pages were shot by `liberPagesReviewClient` on 2026-11-16 (B34, `build/build2/B34-shots/`). They wait on the strain look fixes and the icons.
- **Owner session owed (PR-6):** no fresh dev world has been walked end to end. Equip, passive, incubation, discovery, joining, the lesson and both fights are proven server-side by GameTests only. The walk should look for a Tide Refuge, a Bell and a Wake nursery, which the seed runs rarely found, and judge whether spoken directions are enough to find the nursery (PT-49).
- **Multiplayer (PR-6):** the strand sync was proven live with two driven clients (`build/mp-verify/strands-run*`) for Track 2 only. No two-client check of Court NPC synced poses, the Woven Current strands or the school.
- **Tuning owed (PR-30):** every first-pass strain number inside the bands, after PR-6. It includes the swim total of x1.82 for a Primal Purse at home inside the full Prismatic set (x1.62 at Apex away).
- **Lines to read:** the eight lines P14 added or changed (the switch warning, the Head-tender's `challenge.line2`, `refused_wait` and `refused_degree`, the two system lines `champion.refused_degree` and `champion.forfeit`, the Voyager's chart option, the Braced elder's Holdfast line), and B26's line pass (44 changed keys and 17 new ones). Both are listed in the build logs.
- **Built 2026-11-16 (B36), for the owner's eyes:** the strain look fixes (HN-4) with before and after shots from `morphlingStrainReviewClient` (`build/build2/B36-review/strains-before-after.png`, `rope-before-after.png`), and the three new effect icons from `effectIconReviewClient`.
- **Not tested (PT-46):** no test fires a real network disconnect or a server stop; the logout probes fire the logout event.
- **Silent fights:** the Bloom Warden and the Kindled Champion play no sounds (C-9). The work waits on the sound pass.
- **Unfinished small pieces, never on the question sheet:** polyp appendage geometry per family is Track 1's placeholder; first-person hand rendering of arm growths was not touched.
- **No resident backfill:** outposts generated before Track 9's deeper-houses run stay empty; residents spawn only in `afterPlace`. This touches old dev worlds only, since the mod is not released.
- **Mixed-build hazard:** `CourtSeatCandidates.search` over the outpost set can predict a cell the set's `exclusion_zone` then refuses; nothing that ships uses it, but it is noted for the placement owner (`08-outposts-and-signs.md`).

## History

- Concept and powwow: `archive/concept/Underwater faction ideas/` (the original Marimo notes, the 2026-10-06 powwow, the 2026-10-04 house plates with the old strain names), superseded by the spec where they disagree.
- Working design, decisions of 2026-10-20 and 2026-10-29: `archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md`.
- Track plan, decision roll-up and the 2026-10-30 review fixes: `archive/tracks/brined_court/00-README.md`; session prompts in `SESSION_PROMPTS.md`.
- The eleven build tracks, each with its Status, handover and unattended calls: `01-strain-table-and-replacement.md`, `02-strain-powers.md`, `03-strain-visuals.md`, `04-strain-lore-and-docs.md`, `05-court-discovery.md`, `06-ocean-placement-rules.md`, `07-seats.md`, `08-outposts-and-signs.md`, `09-court-npcs.md`, `10-marimo-and-hydra.md`, `11-court-life-and-progression.md`.
- The first-attempt strains, as history only: `archive/done/CURRENT_MORPHLING_STATS_AND_ABILITIES.md`. Strain icons and HUD exporter: `archive/concept/output/brined-strain-icons-16x16/`.
- Boundary with the Circus: `archive/tracks/circus/06-underwater-boundary-and-deferred-work.md`. Trading that came after: `archive/tracks/npc_trade/`.
- 2026-11-13: the owner answered the question sheet. The Court's rows, their verdicts and the plan (T1 to T12) are in `owner_answers/14-brined-court.md`.
- 2026-11-14: the overnight build, package P14, built T1 to T6, T8, part of T10 and the automated runs of T12, and recorded the rulings here: `owner_answers/BUILD_LOG_2026-11-14.md` ("P14. The Brined Court"). The same day's docs pass moved the open questions to `OPEN_QUESTIONS.md`.
- 2026-11-16: package B34 built NQ-16-3 A, the Court's Liber chapter: `owner_answers/BUILD_LOG_2.md` ("B34. Liber pages").
