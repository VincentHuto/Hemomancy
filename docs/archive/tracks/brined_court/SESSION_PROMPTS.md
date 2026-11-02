# Brined Court: session opening prompts

One prompt per track. Paste a prompt as the first message of a new session in the Hemomancy repo. Each prompt stands on its own.

## Start order

| Wave | Tracks | Start when |
| --- | --- | --- |
| 1 | **1** Strain table, **6** Placement rules | Now, in parallel |
| 2 | **2** Powers, **3** Visuals | After 1 lands, in parallel |
| 2 | **7** Seats, **8** Outposts | After 6 lands, in parallel |
| 3 | **4** Strain lore, **5** Discovery | 4 after 1 and 2; 5 after 7 (or earlier, against a stub Seat) |
| 4 | **11** Court life (state and rules) | After 5 |
| 4 | **9** NPCs | After 3, 7, 8 and 11 |
| 5 | **10** Marimo and Hydra | After 7 and 9 |

"Lands" means the track's status line says DONE and its proof passed. Each track marks itself DONE at the end.

---

## Track 1: Strain table and the eight new strains

```
Build Brined Court Track 1: the Morphling strain table and the eight new house strains.

Read first, in order: AGENTS.md, docs/architecture/ARCHITECTURE_STANDARDS.md, docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/01-strain-table-and-replacement.md, then the spec sections it cites in docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md.

Scope: only Track 1. Add MorphlingStrain/MorphlingStrains and make every hard-coded strain list read it. Delete the eight old strains outright (no migration, no aliases: the mod isn't live). Add the eight new items with ids, Latin, tendencies, Cradle flags, Last Rite ids and eight partner-organism polyp families. Install the icons from docs/archive/concept/output/brined-strain-icons-16x16/16x16/. Powers can be placeholders that Track 2 fills in; they must compile and register.

Rules: build straight through without separate review prompts; run inline (no subagent-driven execution) and finish with one reviewer pass over the whole change. No commits. Other sessions may be running: bump PacketHandler.PROTOCOL_VERSION from what's on disk, regenerate payload-registry.txt and the frozen goldens rather than hand-merging, and treat a mid-run NoClassDefFoundError as a build collision, not a bug. Don't edit other tracks' files.

Done when: ./gradlew.bat test and the Morphling GameTests pass, a fresh dev world shows all eight strains (creative, incubation from polyp, equip, HUD, Bestiary), HR §16.1 is updated, and the track file's status line reads "DONE <date>" with what remains for Tracks 2-4.
```

## Track 2: Strain powers

```
Build Brined Court Track 2: the eight strain power ladders and the new shared mechanics.

Start only if docs/archive/tracks/brined_court/01-strain-table-and-replacement.md says DONE. Read first: AGENTS.md, docs/architecture/ARCHITECTURE_STANDARDS.md, docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/02-strain-powers.md, and the spec's "Strain powers and appearance" section (shared rules, balance rules, all eight tables) in docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md. The spec's numbers are the build values.

Scope: only Track 2. Build the shared pieces first (home conditions, Stung, visibility, damage share, links, regrowth queue, Second Head, field entity, anchor state, Source constants, strain hooks), then the strains in the track's order: Hushbell, Marrowplume, Mermaid's Purse, Ink Cap, Holdfast, Dead Man's Rope, Kinbud, Sunburst. One strain at a time, each with its rules test and GameTests. Keep costs and cooldowns inside the balance bands (add MorphlingBalanceBandsTest). Puppet Interference stays (x1.25 upkeep) and cancels Sunburst's Sun-Fed waiver. Expose the active-state flags Track 3 needs in SyncEquippedMorphlingPacket. Player blood only through BloodAccounting; targets through ManipulationCombatHelper.

Rules: build straight through without separate review prompts; run inline and finish with one reviewer pass. No commits. Parallel sessions (Track 3 especially): bump PROTOCOL_VERSION from disk, regenerate payload-registry.txt, treat a mid-run NoClassDefFoundError as a build collision. Don't edit Track 3's renderer files; agree on the flag layout through the track files.

Done when: unit tests, the morphling_strain_validation GameTests, CombatTargetingMatrixGameTests and MultiplayerVerification pass; docs/MORPHLING_STRAINS.md has the numbers table; the track file says "DONE <date>".
```

## Track 3: Strain visuals

```
Build Brined Court Track 3: Morphling strain visuals (multi-anchor layers, generated models, motion, world effects, HUD art).

Start only if docs/archive/tracks/brined_court/01-strain-table-and-replacement.md says DONE. Read first: AGENTS.md, docs/architecture/ARCHITECTURE_STANDARDS.md, docs/MANIPULATION_VISUALS.md, docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/03-strain-visuals.md, and the spec's "Silhouette and palette", "Real-world references" and per-strain "Appearance" tables in docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md.

Scope: only Track 3. MorphlingVisualMutation holds several attachments; add tools/model_export/morphling_strains.py writing a Java model, .bbmodel and preview per anchor at 16 px per block; tag arm and leg parts as upper or lower limb for Track 9's jointed NPCs; Mermaid's Purse is the sailfish-and-red-purse hybrid; keep the Gravecap fungal head under a neutral name for the Mycophant Tendril; build world effects from WorldEffectDispatcher with the custom toolkit, no vanilla particles; redraw the HUD sprites. Read Track 2's synced flags from SyncEquippedMorphlingPacket (coordinate through the track files if Track 2 hasn't landed them).

Rules: build straight through without separate review prompts, and make visual tweaks directly; run inline and finish with one reviewer pass. No commits. Parallel sessions: regenerate goldens and payload-registry.txt rather than hand-merge; a mid-run NoClassDefFoundError is a build collision. Shader or render-type changes need a client launch to prove they compile.

Done when: morphlingStrainReviewClient is added and its captures show all eight strains at all six stages plus every active state and the Apex contact sheet; tests pass; the track file says "DONE <date>".
```

## Track 4: Strain lore and docs

```
Build Brined Court Track 4: strain text, Bestiary, books and reference docs.

Start only if Tracks 1 and 2 say DONE (docs/archive/tracks/brined_court/01-*.md, 02-*.md). Read first: AGENTS.md, docs/NPC_VOICES.md (the Alchemist's sheet), docs/LORE_REFERENCE.md, docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/04-strain-lore-and-docs.md, and the spec's house texts and "Real-world references" in docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md.

Scope: only Track 4. Item names, binomials, descriptions and maturity lines; Bestiary entries in the Order's voice, with a Court line shown only once the Court is known (read the flag Track 5 will add; if it doesn't exist yet, add a stub predicate and note it in Track 5's file); Alchemist inquiries; Liber Morphling pages and the Cradle list; Mycophant Tendril wording; HR §16 rewrite; docs/MORPHLING_STRAINS.md prose; the LORE_REFERENCE strain section; the wiki page; and reconcile the inner houses' "Morphling direction" text in the spec. Lang keys only. No God/devil framing, no "many-headed" for Dead Man's Rope.

Rules: write directly without separate review prompts; finish with one reviewer pass. No commits.

Done when: HardCodedEnglishGuardrailTest, EnglishLiteralAllowListTest and the strain text resource test pass, the old-name scan is clean, and the track file says "DONE <date>".
```

## Track 5: Court discovery

```
Build Brined Court Track 5: the "Court known" state, its three discovery routes, the Relay Chart and the Brine-Sealed Letter.

Best after Track 7 says DONE (the chart's target); otherwise build against a stub Seat candidate and leave a note in Track 7's file. Read first: AGENTS.md, docs/architecture/ARCHITECTURE_STANDARDS.md, docs/NPC_VOICES.md (Vicar, Voyager, Alchemist), docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/05-court-discovery.md, and the spec's "How the player learns of the Court" in docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md. Study CircusDiscoveryProgress, CircusProgress, CircusWaybillItem and VoyagerIntroduction first; mirror them.

Scope: only Track 5. CourtProgress attachment + PlayerStateSync row; the 20-tick structure discovery for living non-spectator players; the Vicar→Voyager route at D5 (ProgressionGates), the Votary letter (once per unknowing player, LivingDropsEvent), the Alchemist route (D5 plus a strain at Mature); RelayChartItem extends CovenantWaybillItem with an off-thread, bounded seat-candidate search (never the server thread). Votaries are drowned would-be envoys: update the LORE_REFERENCE line. Dialogue events go in the existing <Npc>DialogueEvents classes.

Rules: build straight through without separate review prompts; run inline and finish with one reviewer pass. No commits. Parallel sessions: bump PROTOCOL_VERSION from disk and regenerate payload-registry.txt and goldens; a mid-run NoClassDefFoundError is a build collision.

Done when: the court_discovery_validation GameTests, dialogue_route_validation and the chart timing test pass; HR and LORE_REFERENCE are updated; the track file says "DONE <date>".
```

## Track 6: Ocean placement rules

```
Build Brined Court Track 6: Court structure sets, the shared placement predicate, the clutter budget and its validation run.

Can start now. Read first: AGENTS.md, docs/PELAGIC_WORLDGEN.md (especially the Circus pavilion performance sections), docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/06-ocean-placement-rules.md, and the spec's "In-game presence" in docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md.

Scope: only Track 6. brined_court_seats (64/24) and brined_court_outposts (28/10, exclusion_zone vs seats) structure sets with stub structures (one marker block each) for the eight sites, so Tracks 7 and 8 replace templates, not placement; the hemomancy:brined_court tag; CourtPlacementRules (Pelagic gate, biome-source check at the house's depth band including the Midnight surface-climate split, sampler floor points, Vent Sanctum on seeded vent centres, exclusion by other sets' potential cells within 96 blocks); CourtSeatCandidates.nearest for Track 5; the court_worldgen_validation run in a new CourtWorldValidation class.

Rules: build straight through without separate review prompts; run inline and finish with one reviewer pass. No commits. Another session has uncommitted edits in StructurePlacementChecks.java, MausoleumStructure.java, MausoleumEntranceRules.java, PelagicWorldValidation.java and gradle/game-tests.gradle: build on top of them and don't revert them. Keep placement cheap: no all-column scans, no chunk loads in the predicate.

Done when: CourtPlacementRulesTest passes, court_worldgen_validation passes on seeds 42, 1337 and 8675309 (forward and reverse) with a JFR travel check, PELAGIC_WORLDGEN.md has a Court section, and the track file says "DONE <date>".
```

## Track 7: The two Seats

```
Build Brined Court Track 7: the Lighthouse Garden (with Marimo's tube-coliseum undercroft) and the Vent Sanctum.

Start only if docs/archive/tracks/brined_court/06-ocean-placement-rules.md says DONE. Read first: AGENTS.md, docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/07-seats.md, docs/archive/tracks/mausoleums/00-README.md (the set-piece quality bar), the spec's Giving Light, Kindling and "Marimo and Hydra" sections in docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md, and docs/archive/concept/Underwater faction ideas/Underwater ideas.md (the original Marimo habitat).

Scope: only Track 7. Replace Track 6's stub Seats with jigsaw structures built by Python builders in tools/worldgen/brined_court/ (add the path to AGENTS.md's version-controlled list), set-piece blocks from tools/model_export/ generators at 16 px per block, biological light only, no copper/brass/bronze, no torches or lanterns. The Vent Sanctum keeps the terrain's chimneys exactly. The undercroft descends in moss tiers, half above the waterline, with a space at the bottom for Track 10's Marimo presence. Leave data markers for Track 9's residents. Air pockets must stay dry after reload.

Rules: build straight through without separate review prompts, and make visual tweaks directly; run inline and finish with one reviewer pass. No commits. Parallel sessions: regenerate goldens rather than hand-merge; a mid-run NoClassDefFoundError is a build collision.

Done when: template validation and court_worldgen_validation pass, brinedSeatReviewClient captures both Seats along the camera path by day and night, HR and LORE_REFERENCE have the Seats, and the track file says "DONE <date>".
```

## Track 8: Outposts and signs

```
Build Brined Court Track 8: the six inner-house outposts and the sign features.

Start only if docs/archive/tracks/brined_court/06-ocean-placement-rules.md says DONE. Can run alongside Track 7. Read first: AGENTS.md, docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/08-outposts-and-signs.md, docs/archive/tracks/mausoleums/00-README.md (quality bar), and the six inner house texts in docs/archive/tracks/brined_court/design/BRINED_COURT_HOUSES_WORKING_DESIGN.md.

Scope: only Track 8. Replace Track 6's stub outposts (Tide Refuge, Wake nursery, Line Camp with joined and parted variants, Screened Cleft, The Bell, Whale Fall) with small templates from tools/worldgen/brined_court/ builders, plus set-piece blocks from model generators. Sign features (Sunburst bloom, house markers as one block with a house property, relay markers with drifting red current threads, messenger buoys, vent offerings) are set dressing only, with deterministic per-region caps. Leave data markers for Track 9's residents. Biological light, no copper family, 16 px per block. If Track 7 is also building, share tools/worldgen/brined_court/ helpers through small shared modules and don't edit Track 7's builders.

Rules: build straight through without separate review prompts, and make visual tweaks directly; run inline and finish with one reviewer pass. No commits. Regenerate goldens rather than hand-merge; a mid-run NoClassDefFoundError is a build collision.

Done when: template validation passes, sign caps hold in court_worldgen_validation, the review client captures every outpost and sign, and the track file says "DONE <date>". Note in the file that outposts don't ship until Track 9 gives them residents.
```

## Track 11: Court life (state and rules)

```
Build Brined Court Track 11: house membership, standing, the D5 cultivation lesson, transgression state, the Champion title fields and Unstained shunning, as server state and rules that Tracks 9 and 10 wire into dialogue and fights.

Start only if docs/archive/tracks/brined_court/05-court-discovery.md says DONE (it creates CourtProgress). Read first: AGENTS.md, docs/architecture/ARCHITECTURE_STANDARDS.md, docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/11-court-life-and-progression.md, docs/archive/tracks/brined_court/10-marimo-and-hydra.md (the transgression, offering and Champion rules), and the spec's "Decided October 29, 2026" list.

Scope: only Track 11. CourtProgress fields (house, switchUsed, per-house standing, Giving Light transgression record, Champion title, usurp retry time; the retry time is not a PlayerCooldowns entry); CourtMembershipRules (one free entry, one free switch, D5 plus guest standing, no strain needed); CourtStandingRules; the lesson multiplier (x1.5 to start) on feeding inside the strain's own house structure; the hemomancy:court_offering/giving_light tag (foods, c:foods); an Unstained check through ProgressionGates that Track 9 uses for shunning. Expose a small service API for Tracks 9 and 10, documented in the track file. No dialogue lines here.

Rules: build straight through without separate review prompts; run inline and finish with one reviewer pass. No commits. Parallel sessions: bump PROTOCOL_VERSION from disk if sync changes, regenerate payload-registry.txt; a mid-run NoClassDefFoundError is a build collision.

Done when: unit tests and GameTests for join/switch/refusal, standing, lesson and offering rules pass; the HR Court section covers membership and standing; the track file says "DONE <date>" and lists the service API.
```

## Track 9: Court NPCs

```
Build Brined Court Track 9: the Court NPC roles on the jointed Hemomancy rig, their spawning, and their dialogue.

Start only if Tracks 3, 7, 8 and 11 say DONE. Read first: AGENTS.md, docs/architecture/ARCHITECTURE_STANDARDS.md, docs/NPC_VOICES.md (rules, contrast map), docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/09-court-npcs.md, docs/archive/tracks/brined_court/11-court-life-and-progression.md (its service API), the spec's house texts, and tools/model_export/annetta_knowles.py (the jointed rig).

Scope: only Track 9. A shared rig helper tools/model_export/court_humanoid.py (root→hips→body; arm→forearm with the elbow 4 px below the shoulder; leg→shin with the knee halfway; inset joint fillers), never a vanilla HumanoidModel; one generator per role with _anim.java poses; the strain anchor adapter for forearm and shin; CourtMemberEntity (PathfinderMob, ProgressionDialogueNpc, ProtectedNpc) with house and role; underwater breathing and navigation; structure-bound spawning from Track 7 and 8 markers; Shared Wake schools (also natural, rare, capped, never near a Seat). Build the two Seats' roles first, then one house at a time. Dialogue: add a "Brined Court" section to NPC_VOICES.md with each sheet placed on the contrast map before writing that speaker's lines; one speaker at a time; states include stranger, referred, known, member, transgressor and Unstained (shunned: distance, closed doors, refusal lines, nothing given, never attacked). NPCs give food, rest, cultivation help, gifts, inquiries and directions; leave room for trading later.

Rules: build straight through without separate review prompts, and make visual tweaks directly; run inline and finish with one reviewer pass. No commits. Parallel sessions: regenerate goldens and payload-registry.txt; a mid-run NoClassDefFoundError is a build collision.

Done when: the role GameTests, dialogue_route_validation and CombatTargetingMatrixGameTests pass, courtNpcReviewClient captures every role (including the shallow-to-deep lineup and the moving school), and the track file says "DONE <date>".
```

## Track 10: Marimo and Hydra

```
Build Brined Court Track 10: the Marimo and Hydra presences, the Bloom Warden and the Kindled Champion.

Start only if Tracks 7 and 9 say DONE (and 11 for the state API). Read first: AGENTS.md, docs/architecture/ARCHITECTURE_STANDARDS.md, docs/MANIPULATION_VISUALS.md, docs/archive/tracks/brined_court/00-README.md, docs/archive/tracks/brined_court/10-marimo-and-hydra.md, docs/archive/tracks/brined_court/11-court-life-and-progression.md, and the spec's "Marimo and Hydra" and Kindling sections. Study an existing boss built from BossReplayClock, BossMoveTimeline, BossBarFrame and BossSceneCatalog (Hemorath or Mycophant) before starting.

Scope: only Track 10. Tier 1 first: Marimo's moss mass at the undercroft's bottom and Hydra's rising head, both untouchable (no hitbox, no damage, no boss bar, blocks refuse breaking), each with a first-sight scene. The player never fights Marimo or Hydra. Then Tier 2: the Bloom Warden wakes only on the listed transgressions; holding a food offering stops its attacks and opens reconciliation; losing means death. The Kindled Champion is a title the player may choose to usurp: losing means death and a retry after a wait held in CourtProgress; winning makes the head crown the victor, and the old Champion yields and lives. Both on the jointed rig; custom VFX to the Riposte bar; no vanilla particle clouds.

Rules: build straight through without separate review prompts, and make visual tweaks directly; run inline and finish with one reviewer pass. No commits. Parallel sessions: regenerate goldens and payload-registry.txt; a mid-run NoClassDefFoundError is a build collision.

Done when: the presence and miniboss GameTests, BossAnimationClockGuardrailTest and CombatTargetingGuardrailTest pass, courtPresenceReviewClient captures both presences and every miniboss phase, HR and LORE_REFERENCE are updated, and the track file says "DONE <date>".
```
