# Track 11: Court life and progression

**Status:** DONE 2026-10-30 (unattended run). Court life is server state in `CourtProgress` behind the `CourtLife` service: one free house entry and one free switch (D5 plus guest standing, no strain, a third change refused with a lang line), per-house standing (stranger/guest/friend, once per act, a site visit makes a guest), the Lightkeeper's lesson (x1.5 bonding credit inside the strain's own house site, through the real upkeep tick), the Giving Light transgression record and food offerings (`#hemomancy:court_offering/giving_light`), the Kindled Champion title and usurp retry wait, and Unstained shunning through `ProgressionGates.isUnstained`. `CourtLifeRulesTest` (18), `court_life_validation` (8 GameTests), the full JUnit suite and the regression GameTest suites pass; HR §29 "Court life", LORE_REFERENCE and the wiki updated. Left: no dialogue (Track 9 calls the service), the Warden and Champion fights (Track 10), and the PROPOSED member home-bonus extra (not built). See "Service API" and "Calls made unattended".

Phase D. Answered 2026-10-29 enough to build the core. Spec sections: "Membership", "Communication between houses", "Still open" items 2–13. Powwow: the Court is the bodily specialist path around D4–D5, as the Circus is the mental one.

## Goal

What the player *does* with the Court once they've found it, so the NPCs have something to teach and the minibosses have something to gate.

## The loop

1. **Welcome (any degree, W4).** The player finds a house, or is sent to the Lighthouse Garden. They're fed, housed and talked to. Nothing is taught before D5.
2. **First lesson at the Garden (D5, SETTLED L2).** The Lightkeeper teaches cultivation as partnership: a strain grows faster when it's fed inside its own house's Seat or outpost. Mechanic: once taught, feeding a strain inside its house's structure applies a maturity multiplier to the existing feeding. The multiplier starts at ×1.5 and is tuned in play.
3. **Visiting the houses.** Each house's members recognise their own strain on the player and teach one thing about it: an inquiry, a tip or a one-time gift. Visiting all eight teaches the whole gradient. Optional; a Side thread in the assignment ledger.
4. **Standing, not currency (PROPOSED).** Each house tracks a standing (stranger, guest, friend). It's raised by visiting and by the house's own custom (bring food to the Garden, keep a promise to the Holding Stone, give an offering at a vent), never bought. Standing opens the next tier of help and the Kindled Champion challenge. Transgression drops it (the Bloom Warden, Track 10).
5. **House membership (SETTLED L1).** A player gets **one free entry** into a house and **one free switch** after it. They can join a house early and, if they later learn of one that fits them better, leave the first for it. After the switch, membership is fixed. This mirrors the spec's custom (members are born in and may move once): the player's entry stands in for being born in, and the switch is their one move.
   - State in `CourtProgress` (Track 5): `house` (nullable `CourtHouse`), `switchUsed` (boolean). Joining sets `house`. Switching sets the new `house` and `switchUsed`. A third change is refused, and the refusal is a lang key.
   - Leaving a house is a moment, not a menu. The house left behind treats departure the way its tension says (the Holding Stone takes it hard; the Veiled Fold doesn't ask why). Standing with the old house drops to guest, never to hostile.
   - Joining happens through the house's lead or senior role (the Seat leads; an outpost's Role B elder) in dialogue, once the player is D5 and has guest standing.
   - PROPOSED effect: members get a small extra on their house's home bonus at their own Seat or outpost, and the other houses greet them by their house (the middle houses lean toward their pole; Marimo's and Hydra's followers react to a member of the other end).
6. **Ends of life aren't the player's.** Anchoring, petrification and surrender are things the player witnesses and understands, not things they do. The player's own Primal anchoring (Sunburst Arbor, Holdfast, Marrowplume) is the closest they come, and it's always reversible. Holding the Kindled Champion title never means entering the threshold.

## Communication between houses (SETTLED L4: set dressing)

Messengers and blood currents are set dressing only: relay markers at layer boundaries, messenger buoys at the surface, and visible red current threads in the water near relays (Track 8's signs, plus a drifting-thread effect from the custom toolkit). There's no delivery task and no signal mechanic. NPC dialogue can mention them.

## Worthiness (SETTLED L3)

Hydra judges, shown by which follower its head crowns after the Kindled Champion fight. The Head-tender reads it aloud (Track 10).

## Answered 2026-10-29

- **L5:** no strain is needed to join a house; the house's own strain is needed for its cultivation lesson.
- **L6:** joining needs D5 and guest standing with that house.
- **L7:** the Court isn't exclusive with the Circus. The existing trade-off still applies: **Puppet Interference** (HR §16.6). An equipped Morphling multiplies its blood upkeep by 1.25 while a same-owner shaped puppet or Commandeered Will is active. The new strains keep it unchanged. Sunburst's Sun-Fed waives upkeep at home. **Interference cancels that waiver (SETTLED 2026-10-29):** while a puppet qualifies, Sunburst pays normal upkeep ×1.25 even at home, and Sun-Fed's hunger and blood restoration still runs.
- **L8:** Unstained players can visit, but every house actively shuns them as a walking biohazard. To a people who live by symbiosis, a body scrubbed clean of partners is a contagion risk: whatever cleansed them could spread to the houses' strains. Use the Unstained path check in `ProgressionGates`, not `volume.isActive()`.
  - Residents keep their distance (they step or swim a few blocks away) and close up: the Veiled Fold vanishes, the Still Bell seals its door, the Holding Stone braces the entrance shut. In dialogue they turn the player away with a shunning line per role. No food, rest, gifts, lessons, membership or standing.
  - They never attack (residents are protected and non-hostile). Visiting alone isn't a transgression, but a transgression by an Unstained player still wakes the Bloom Warden.
  - Discovery still records the Court as known; the Court simply refuses them.

## Still open (none block the core build)

| Id | Question | Recommendation |
| --- | --- | --- |
| — | Marimo's long-term relationship with a follower; when and why one petrifies. | Lore only (spec "Still open" 4). |
| — | Last Marrow elders' final fate when a fall is exhausted. | Lore only (spec "Still open" 8). |
| — | Are the vents literally Hydra's body, and what happens to someone who enters? | Lore first. Track 10 only needs "they don't come back as themselves". |
| — | Court customs: passage, hospitality, disputes, cultivation knowledge. | Write per house in the voice sheets (Track 9). |

## Build notes

- `CourtProgress` gains `house`, `switchUsed`, the per-house standing map (eight small ints), the Giving Light transgression record, the Kindled Champion title and the usurp retry time (Track 10). There's no migration (the attachment is new). The retry time is deliberately not a `PlayerCooldowns` entry, which would reset on death.
- GameTests: join, switch, refused third change, standing changes once per act, lesson multiplier applies only inside the strain's own house structure and only after the lesson; an Unstained visitor is shunned by every role (distance, closed doors, refusal lines, nothing given) and is never attacked.
- Docs: the HR Court section (membership, standing, the lesson); `docs/LORE_REFERENCE.md` (the player's place in the Court's custom).

## Service API (for Tracks 9 and 10)

Everything is in `common/capability/player/harbinger/court/`. `CourtLife` is the one entry point; it reads and writes `CourtProgress` (through `HemoCapabilityAccess`) and every write resends the owner's copy through `CourtDiscoveryProgress.sync`. Refusals come back as enums whose `translationKey()` is the lang line the service has already shown the player; Track 9 can add its own spoken line on top.

| Call | What it does |
| --- | --- |
| `CourtLife.isShunned(player)` | True on the Unstained path (`ProgressionGates.isUnstained`, stage 1+). Use it for the shunning: distance, closed doors, the per-role refusal line, nothing given. Never a reason to attack. |
| `CourtLife.house(player)`, `isMember(player, house)` | The player's house, or null. |
| `CourtLife.standing(player, house)` | `CourtStandingRules.Standing` STRANGER / GUEST / FRIEND (`atLeast(...)`). |
| `CourtLife.turnsAway(player, house)` | The Giving Light while a transgression is outstanding. |
| `CourtLife.tryJoin(player, house)` | Call from the Seat lead or an outpost's Role B elder. Returns `CourtMembershipRules.Outcome`: `JOINED`, `SWITCHED` (old house drops to guest), `ALREADY_MEMBER`, `REFUSED_SWITCH_USED`, `REFUSED_DEGREE`, `REFUSED_STANDING`, `REFUSED_SHUNNED`, `REFUSED_TRANSGRESSION`; `changed()` says whether membership moved. |
| `CourtLife.recordAct(player, house, actId, ActKind.VISIT/CUSTOM)` | Counts an act toward standing once per house (true if standing rose). VISIT lifts a stranger to guest; CUSTOM raises a step to friend. Track 9 picks the act ids for each house's custom (for example `custom_food`, `custom_promise`, `custom_vent_offering`). The site visit is already recorded by `CourtLife.onPlayerTick` under `CourtLife.VISIT_ACT`. |
| `CourtLife.teachLesson(player)` | The Lightkeeper's lesson. `CourtLessonRules.Outcome` `TAUGHT`, `ALREADY_TAUGHT`, `REFUSED_DEGREE`, `REFUSED_SHUNNED`, `REFUSED_TRANSGRESSION`. `hasLesson(player)` reads it. |
| `CourtLife.lessonMultiplier(player, strainStack)` | 1.5 inside the strain's own house site once taught, else 1. Already applied in `EquippedMorphlingEvents`; read it only to show it. |
| `CourtLife.houseAt(level, pos)` | The house whose Seat or outpost has a piece at `pos`, or null. |
| `CourtLife.recordTransgression(player, actId)` | Track 10's Warden triggers call this. Records the wrong (kept through death) and drops Giving Light standing to stranger. `hasTransgression`, `progress(player).transgressionAct()` and `transgressionCount()` read it. Waking the Warden once per act is Track 10's. |
| `CourtLife.holdsOffering(player)`, `isOffering(stack)` | A main-hand `#hemomancy:court_offering/giving_light` item (`#c:foods`): the Warden holds off and reconciliation opens. |
| `CourtLife.reconcile(player)` | With a wrong outstanding and an offering in hand: consumes one, clears the record, standing back to guest. False, with nothing changed, otherwise. |
| `CourtLife.championGate(player)` | `CourtChampionRules.Gate`: `OPEN`, or `REFUSED_UNKNOWN`, `REFUSED_SHUNNED`, `REFUSED_CHAMPION`, `REFUSED_STRAIN` (no Apex strain worn or carried), `REFUSED_STANDING` (Kindling friend needed), `REFUSED_WAIT`. |
| `CourtLife.recordUsurpLoss(player)` | Starts the one-day wait (`CourtChampionRules.USURP_RETRY_TICKS`, game time), kept through death and relog. |
| `CourtLife.grantChampion(player)`, `revokeChampion(player)`, `isChampion(player)` | The title. Granting clears the wait. `progress(player).championSince()` is stored for the deferred elder challenge. |

Saved keys in `CourtProgress` (beside Track 5's `Known`, `KnownBy`, `LetterDropped`, `ChartIssued`): `House`, `SwitchUsed`, `Standing` (house id to level), `StandingActs` (house id to act ids), `LessonTaught`, `Transgression` {`Act`, `GameTime`}, `TransgressionCount`, `Champion`, `ChampionSince`, `UsurpRetryAt`. The attachment is copied on death.

## Calls made unattended

- **Lesson multiplier target.** "Feeding" a strain is read as its equipped blood upkeep, the bonding credit that gates the next maturity (`MorphlingItem.recordBondingBlood` in `EquippedMorphlingEvents`). The x1.5 multiplies that credit, including Sun-Fed's credited-but-waived upkeep. Enzyme feeding at the Incubator jumps a whole maturity and is unchanged.
- **"Inside the house structure"** means inside a piece's bounds of that house's `#hemomancy:brined_court` structure (the same piece test as discovery), resolved from the `CourtStructure`'s `site().house()`.
- **Standing model.** Three tiers as the plan proposes. A visit makes a stranger a guest; a custom act raises one step; each act id counts once per house. The site-visit check needs the Court to be known (discovery records that on the same visit) and a living, non-spectator player. Standing chat lines are system messages ("You are a guest of the Giving Light now."), not dialogue.
- **A transgression drops Giving Light standing to stranger** and makes only the Garden turn the player away (no Giving Light standing, membership or lesson) until amends. Other houses are unaffected. Amends restore guest, not an earlier friend.
- **The lesson is refused while the Garden turns the player away**, since the Lightkeeper is the Garden's.
- **Champion gate** takes the plan's PROPOSED gate, with "Kindling standing" read as friend (the plan says standing opens the challenge as the next tier). The Apex strain may be worn or carried, matching Track 5's maturity read.
- **Retry wait** uses server game time (shared by every dimension), one in-game day.
- **Unstained check** is `ProgressionGates.isUnstained(player)`: Unstained stage 1 or beyond (begun purification). A player who has never begun purification is not shunned.
- **No protocol bump.** `PacketSyncCourtProgress` already carries the whole NBT tag, so the new keys ride the existing payload; its layout didn't change.
- **Member home-bonus extra (PROPOSED)** is not built: the plan marks it PROPOSED, and Track 9 owns how houses greet members.
- **New GameTest namespace** `court_life_validation` (alpha), with a `court_life_site` batch that writes a structure start and reference for a real Court site into the test chunk and restores the chunk's own data afterwards.
- **House and standing names** are lang keys (`court_house.hemomancy.<id>`, `court_standing.hemomancy.<tier>`); `CourtHouse.byId` and `CourtHouse.displayName()` were added.
