# Track 5: Court discovery

**Status:** DONE 2026-10-30 (unattended run). The `CourtProgress` attachment and its `PlayerStateSync` row (protocol 27), the 20-tick structure discovery for living non-spectator players, the Vicar→Voyager D5 route, the once-per-unknowing-player Votary letter (`LivingDropsEvent`), the Alchemist route (D5 plus a Mature strain), letter inquiries for all three NPCs, the Relay Chart with its off-thread bounded Seat search, and `CourtKnowledge` backed by real state; `court_discovery_validation` (10), `dialogue_route_validation`, `registration_validation`, `court_worldgen_validation` seed 42 (chart search 88 ms on a worker thread, discovery lookup in five site kinds) and the full JUnit suite (4,771) pass, with two `relay_chart/` shots in `brinedSeatReviewClient`. HR §1 and §29, LORE_REFERENCE and the wiki updated. Reviewed 2026-10-30: `court_discovery_validation` (10), `dialogue_route_validation`, `registration_validation` and the architecture/unit tests re-run green, and `court_worldgen_validation` seed 1337 passed (chart search 24 ms on Worker-Main-22, discovery lookup in all seven site kinds). Left: the chart timing on seed 8675309 (not run). See "Calls made unattended" and "Handover".

Phase B. Needs Track 7's Lighthouse Garden as the chart's target; build against a stub until it lands. Spec section: "How the player learns of the Court", "Locating".

## Goal

Three routes, any of which can come first, converge on one server-authoritative "Court known" state, mirroring the Circus. Whichever route arrives second acknowledges the first instead of repeating it. The Court is visitable before D5 without being presented as a lesson (SETTLED). Finding a Court structure by sailing past it also counts.

## Player state (PROPOSED)

Mirror the Circus:

| Circus today | Court |
| --- | --- |
| `CircusProgress.discovered()` / `setDiscovered()` via `HemoCapabilityAccess.circusProgress(player)` | `CourtProgress` attachment via `HemoCapabilityAccess.courtProgress(player)` in `common/capability/player/harbinger/court/` |
| `CircusDiscoveryProgress` (`hasDiscovered` / `markDiscovered`, 20-tick structure check against `circus_pavilion`) | `CourtDiscoveryProgress`: the same 20-tick check against a `hemomancy:brined_court` structure tag (both Seats and every outpost) |
| `CircusPlayerProgress` sync | a `PlayerStateSync` row for `CourtProgress` |
| `CircusWaybillItem extends CovenantWaybillItem` | `RelayChartItem extends CovenantWaybillItem` |
| `CircusIntroductionRules` | `CourtIntroductionRules` |

`CourtProgress` fields: `known`, `knownBy` (route enum: `VOYAGER`, `LETTER`, `ALCHEMIST`, `FOUND`), `letterDropped` (the once-per-player Votary letter flag), `chartIssued`. Track 11 may add house membership later; leave room, don't add it now.

Structure discovery requires a living, non-spectator player inside the structure's bounds, like Circus observation (AGENTS.md project patterns).

## The three routes

### 1. The Vicar and the Voyagers

- Builds on The Reef Watch (D3 Side assignment). Today: `common/mission/vicar/VoyagerIntroduction` stores `Observed` / `Reported` under `hemomancy:voyager_introduction`; `HarbingerVicarEntity` (≈239) calls `VoyagerIntroductionDialogue.withVicarReport`; `VoyagerVesselGuidance` points to vessels.
- At D5 (`ProgressionGates`, never a literal), the Vicar refers the player to a Voyager. The Voyager (dialogue in `HarbingerVoyagerDialogueTrees`, events in `route/VoyagerDialogueEvents`) explains the Court at a cordial distance and issues the reissuable Relay Chart. Talking to the Voyager about it marks `known` (`VOYAGER`).
- If the player hasn't done The Reef Watch, the Vicar gives the Voyager bearing first (existing `tellBearing`).

### 2. A Brined Votary's letter

- `BrinedVotaryEntity` (`common/entity/mob/aquatic/`) guards Voyager wrecks in the Erythrocoral Reef, the Lighthouse Garden's layer.
- A Votary killed by a player who doesn't know the Court, and hasn't had the letter before, drops one Brine-Sealed Letter (`letterDropped` set on drop, not on pickup; a lost letter can be re-explained by any of the three NPCs below). Drop it from a `LivingDropsEvent` handler with the player check, not a loot table, so it can't be farmed.
- Item inquiries with the Vicar, Alchemist and Voyager (`data/hemomancy/dialogue_inquiry/<npc>/hemomancy/brine_sealed_letter.json`) explain it and record `known` (`LETTER`). The Voyager then offers the Relay Chart.
- SETTLED 2026-10-29 (W5): wrecks were failed attempts to reach the Court, and Votaries are drowned would-be envoys. Update the `docs/LORE_REFERENCE.md` line "establishes no new doctrine for the Brined Votary" to say so. "Brined" is what Voyagers call anything that came back from the Court's waters.

### 3. The Alchemist

- Once the player is D5 and holds any strain at Mature or higher, the Alchemist says the best cultivators of these lines aren't in the Order and points to the Voyagers or the coast. Records `known` (`ALCHEMIST`); the chart still comes from a Voyager.

### Found by sailing

Entering any Court structure records `known` (`FOUND`). Residents greet a stranger who found them without a referral (Track 9's dialogue).

## The Relay Chart

- Extends `CovenantWaybillItem` like the Scarlet Waybill. Points to the nearest Lighthouse Garden. Reissuable from any Voyager.
- **Never stall the server.** Vanilla `/locate` of the pavilion once took 209 s. PROPOSED: compute candidates from the Seat structure set's `random_spread` placement directly (the cell math is pure), test each candidate cell with the cheap biome-and-depth predicate from Track 6 against the biome source only (no chunk loads), nearest first, bounded to a fixed number of rings. Run it off the main thread and hand the result back on the server thread. Cache per world. A chart that finds nothing within its bound says so in its tooltip rather than pointing anywhere.
- Track 6 owns the predicate and exposes `CourtSeatCandidates.nearest(ServerLevel, BlockPos, maxRings)`.

## Dialogue

All new lines through the speaker sheets: Vicar, Voyager and Alchemist exist in `docs/NPC_VOICES.md`. Add their events to their existing `<Npc>DialogueEvents` classes; no new `DialogueEvent` subscriber. The second route to arrive acknowledges the first ("You've already had a letter from them, I see").

## Tests and proof

- Unit: `CourtIntroductionRulesTest` (D5 gate, strain maturity gate, route precedence, letter once).
- GameTests: a `court_discovery_validation` batch: each route alone, each pair in both orders, letter not dropping for a knowing player or a second time, structure discovery by a living player only, chart reissue.
- `dialogue_route_validation` fingerprints updated. `player_migration_validation` unaffected (new attachment, no migration).
- Chart search timing: a loaded test that asserts the search finishes within a fixed budget on a fresh seed and never runs on the server thread.
- Client: the Relay Chart pointing and the letter's tooltip in Track 7's review client.

## Docs

`docs/HEMOMANCY_REFERENCE.md`: the D5 referral, a new Brined Court section's discovery part. `docs/LORE_REFERENCE.md`: the Votary line (Votaries as drowned envoys), the Voyagers' cordial distance.

## Open questions

- Not blocking: should the Mnemonist (the Circus's referrer) also hint at the Court? The powwow named the Alchemist and Voyager as Court leads, so PROPOSED: no.

## Note from Track 4 (2026-10-30)

Track 4 shows a strain's house line only once the Court is known (decision M7): the Morphling tooltip's Court line (`morphling.hemomancy.<id>.court`) and the Living Bestiary's Court paragraph (`bestiary.hemomancy.morphling.<family>.court`). Both read `CourtKnowledge.isKnown(Player)` in `common/capability/player/harbinger/court/CourtKnowledge.java`, which is a **stub that always returns false** because this track hasn't added the Court-known state yet. When `CourtProgress` lands, replace that method's body with a read of the attachment through `HemoCapabilityAccess` and keep the signature. Both callers run on the client (an item tooltip through `ClientBridge.get().localPlayer()`, and `BestiaryTabView`), so the known flag must be in the owner's `PlayerStateSync` row. Add a check that a known player sees the line, for example a GameTest or a unit test on the predicate.

Done in Track 5: `CourtKnowledge.isKnown` now reads `CourtProgress.known()` through `HemoCapabilityAccess`, with the same signature; the `COURT_PROGRESS` row of `PlayerStateSync` carries it to the owner's client. `court_discovery_validation` (`knowingTheCourtSyncsToTheOwner`) checks that a known player passes the predicate and that the owner's payload decodes to the same state.

## Calls made unattended

- **Where the rules live.** `CourtIntroductionRules` sits beside the state in `common/capability/player/harbinger/court/`, not in `entity/npc/dialogue/` where `CircusIntroductionRules` lives, because the drop handler and the item use it as well as dialogue.
- **Discovery service name.** `CourtDiscoveryProgress` holds every write (the three routes, the drop, the chart issue, the 20-tick check); there is no separate `CourtPlayerProgress` because there is nothing else to mirror yet.
- **One owner payload, not a dirty flag.** Every write goes through `CourtDiscoveryProgress`, which sends `PacketSyncCourtProgress` itself; the table row resends on login, respawn and dimension change. Protocol bumped 26 → 27.
- **Option and topic wording.** The Vicar's topic is "Who else keeps to the sea?", the Voyager's "Who else works these waters?" (with "How would I find the Lighthouse Garden?" to record and take the chart), the Alchemist's "Where do these strains come from?" / "Where should I look?". All three are LORE topics in the hub (their keys contain "about").
- **The Vicar's referral records nothing.** The plan says talking to the Voyager marks `known`; the Vicar's D5 topic only refers (with the Reef Watch vessel bearing first when that assignment's observation and report are both missing and the player is referral-eligible).
- **The Voyager speaks of the Court before D5 once it is known** (found by sailing, or a letter), and reissues the chart then too. Its explanation route (`voyager_court_learn`) still needs D5.
- **Every Voyager Court answer ends with the chart** when the player knows the Court and carries none: the explanation, the letter, and the plain reissue. The Vicar and Alchemist never hand out a chart.
- **"A lost letter can be re-explained".** Read as: once a letter has dropped, while the Court is unknown and the player no longer carries it, each of the three NPCs offers "I lost a sealed letter a drowned Votary was carrying." in their Court topic, which records `letter`. Reading a letter doesn't consume it.
- **Letter inquiry wiring.** Item inquiries are passive lines, so the letter's inquiry node gets an extra event option ("Who was this letter meant for?") after `DialogueItemInquiryNodes` and before the hub decoration; once known, the node adds the speaker's `court.known.<route>` acknowledgment instead.
- **Acknowledgment lines.** One `hemomancy.<npc>.court.known.<route>` line per speaker and route (12 lines), reused by the topic and by the letter inquiry.
- **The killer is `source.getEntity()`.** A player's own projectile kill counts (the source's entity is the shooter). A tamed wolf's kill does not, because its damage source names the wolf; an unowned or environmental kill never drops the letter and never sets `LetterDropped`. (Corrected in review: the original note claimed wolf kills counted.)
- **Mature counts worn or carried.** `highestStrainMaturity` reads the equipped Morphling and every Morphling in the main inventory (not containers).
- **Chart search bound and cache.** 6 Seat rings each way (about 6,700 blocks at spacing 64), run on `Util.backgroundExecutor()`, cached per dimension and Seat cell of the asking position (so two askers in the same cell share one answer, which may be a different Garden than the strict nearest from a far corner of the cell). One search per player at a time; both maps clear through `TransientServerStateEvents`.
- **Chart timing proof.** The fresh-seed timing check went into `court_worldgen_validation` (`checkChartSearch`, 2 s budget from the origin), because only that suite has a seeded Pelagic world; it awaits the off-thread stage only, since the test itself blocks the server thread. `court_discovery_validation` proves on the flat floor that the search ran off the server thread and that an empty search shows the tooltip line. The same suite also got `checkDiscoveryBounds`: the Court tag lookup sees a point inside one generated site of each kind.
- **Structure discovery in the flat GameTest world** is proven through the tick's own gate (`mayDiscover`) with the lookup answered yes, plus a check that the tick records nothing outside a Court; the real lookup is proven in `court_worldgen_validation`.
- **Item art.** `relay_chart` and `brine_sealed_letter` are flat 16x16 item sprites (parchment, teal sea, red route; parchment with a Sunburst-crust seal), drawn by a throwaway script outside the repo; models are `item/generated`. No copper tones.
- **Creative filing.** Both items sit in the Brined Court section after the Salt-Stained Voyager Log.
- **Review client.** Track 7's `brinedSeatReviewClient` gained two `relay_chart/` shots (facing the Garden with the lodestone vignette, facing away), with `-PbrinedSeatChartOnly=true` to shoot only those. The letter's tooltip is checked in a GameTest, not a screenshot.
- **Mnemonist.** Took the plan's recommendation: the Mnemonist does not hint at the Court.

## Handover

- **Track 9:** residents who greet a stranger who found them without a referral can read `CourtDiscoveryProgress.knownBy(player) == CourtProgress.Route.FOUND`.
- **Track 11:** house membership and standing join `CourtProgress` under new NBT keys; leave `Known`, `KnownBy`, `LetterDropped`, `ChartIssued` as they are.
