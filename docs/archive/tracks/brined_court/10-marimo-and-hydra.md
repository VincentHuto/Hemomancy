# Track 10: Marimo and Hydra

**Status:** DONE 2026-10-30. Tier 1 (the presences) and Tier 2 (the Bloom Warden and the Kindled Champion) are built; see both handovers below. Open: the dialogue-route fingerprints have no row for the Head-tender's challenge yet, and the Warden review shots need a re-run to confirm the recentred framing (see the review note at the end).

Phase C, last build track. Depends on Track 7 (the undercroft and the Sanctum) and Track 9 (residents who speak for them). Spec sections: "Marimo and Hydra", "Anchoring and true petrification", the Kindling's surrender and worthiness. Powwow: the Court's routes may end in a conflict between the poles (tentative).

## Rule (SETTLED 2026-10-29)

**The player never fights Marimo or Hydra directly.** They're presences the player visits, hears and witnesses. They never take damage, never get a boss bar, and are never a target. Every fight in this track is against a **follower** built in its pole's image: the "NPC version" of Marimo or Hydra from the original request. Hydra's heads and Marimo's moss count as the poles themselves, so the player doesn't fight them either.

## Tier 1: presences (build first)

Non-combat set pieces at the Seats.

### Marimo in the undercroft (SETTLED location)

- **Where:** the bottom of the Lighthouse Garden's undercroft, the tube coliseum (Track 7). It's half above the waterline and descends through moss-covered tiers. The deeper the tier, the more moss than person the anchored members are.
- **What:** a great moss mass filling the lowest chamber, threaded up through the walls of the tiers above, with petrified followers embedded in it. A multiblock set piece with a block entity driving its pulse and glow. It has no mob and no hitbox to attack. Breaking its blocks is refused, like the rite-space and sanctum privacy rules.
- **Voice:** Marimo doesn't speak directly. The anchored members of the lowest tiers speak for him in a shared "we", and the moss carries messages between them (your moss people "communicating with the walls"). One speaker sheet: Marimo's chorus.
- **What it does:** acknowledges the player and offers what Marimo offers: food, belonging, and the long road to becoming part of him. It's the one place where true petrification is shown and explained. It gives nothing permanent in Tier 1.

### Hydra at the Vent Sanctum

- **Where:** the Sanctum's chimneys (the heads) and the body below (Track 7).
- **What:** the heads are set pieces on the chimneys. When the Head-tender calls it, the largest can **rise** from its chimney: a scripted animated entity with no AI, no damage and no hitbox. It lowers its polyp crown over the player and then withdraws. The body is a set piece.
- **Voice:** Hydra doesn't speak in words. The Head-tender and Kindled elders interpret it. One speaker sheet: the Head-tender.
- **What it does:** the Kindling's devotion made visible, and the first sight of what a head kept by the worthy looks like.

Each presence gets a short first-sight cinematic through the boss-scene director (`client/render/cinematic/BossSceneDirector`, a `scene/*Scene.java`), with no boss bar.

## Tier 2: follower minibosses (SETTLED 2026-10-29, P5)

Ship Tier 1 first; these two follow. Each opponent is a follower who has grown furthest toward its pole, and the two fights mirror their poles: Marimo's follower answers a wrong done to the many, and Hydra's champion is the strongest one, open to challenge.

### The Bloom Warden (Marimo's image): a response to transgression

- **Who:** an Arbor elder of the undercroft's middle tiers, more moss than person but not yet petrified, who can draw on Marimo's blooms.
- **When:** never offered. The Warden wakes when a player steals from or transgresses against the Giving Light. Transgressions (SETTLED 2026-10-29, T1: all of these):
  - breaking the Garden's growth, beds, arbor growth or moss;
  - taking from the undercroft (the moss, the petrified, anything set there as Marimo's);
  - raising a weapon or casting harm in the undercroft or at a resident (residents are protected, so the attempt itself counts);
  - taking a gift a second time or taking food meant for others, if Track 11 gives the Garden a shared table.
- **Fight:** in the undercroft. The Warden fights through the room rather than its body. Bloom patches spread across the tier floors and heal it while light shafts from above feed them. The player breaks the bloom network by cutting off light and clearing blooms, then faces the Warden when it uproots to move. This tests positioning and area control. Petrified followers in the walls are never damaged; they're people.
- **End:** if the player wins, the Warden re-anchors, Marimo's moss pulses once through the walls, and the chorus names the wrong. Either way the player loses Giving Light standing (Track 11) and is turned away until amends are made. Losing kills the player (T4). The transgression stays recorded through death. There's no reward for beating the Warden: it's a consequence, not loot.
- **Amends (SETTLED 2026-10-29, T2): bring an offering.** A transgressor holding an offering item in their main hand isn't attacked by the Warden, and holding it opens reconciliation dialogue with the Warden or the Lightkeeper. Reconciling consumes the offering, clears the recorded transgression and restores standing to guest. Holding an offering protects only the act of making amends: a player who commits a new transgression while holding one still wakes the Warden. Offerings are foods (SETTLED 2026-10-29, T5): the tag `hemomancy:court_offering/giving_light`, which includes `c:foods`.

### The Kindled Champion (Hydra's image): a rite of passage

- **Who:** the Sanctum's Kindled Champion is a title, not one fixed NPC. The current holder is a Kindled elder in the mature Hydra form who hasn't surrendered yet: the strongest of the Kindling, still a person.
- **Fiction:** the Kindling believe in survival of the fittest. A worthy challenger may **usurp** the Champion. For the Kindling it's an honour, not an insult, and a rite of passage. The player can choose to attempt it once they meet Track 11's gate (Court known, a strain at Apex, Kindling standing; PROPOSED). The Head-tender witnesses it.
- **Fight:** in the vent water on the mound, with the chimneys venting heat as arena hazards. The Champion has neck sweeps, a stinging crown (Stung, from Track 2), and crown grabs. It also regrows damage (Kinbud's regrowth at miniboss scale), so the player has to burst it past its regrowth. Its phases follow its preparation: at each threshold it buds more of its crown.
- **If the player wins:** the old Champion yields and lives, and steps back into the Kindled elders. Hydra's largest head rises and lowers its crown over the **victor** (the Tier 1 presence; this is the worthiness judgement, L3), and the Head-tender names the player Champion. For now the title gives only standing and recognition at every Kindling site (SETTLED 2026-10-29, T3), never a strain item and never surrender. The player is never asked to enter the threshold.
- **If the player loses:** they die (SETTLED 2026-10-29, T4) and can try again after a wait. The Champion keeps the title. The wait is stored in `CourtProgress` (Track 11), not `PlayerCooldowns`, because those reset on death and death is how this fight is lost. PROPOSED wait: one in-game day.
- **After (DEFERRED, T3):** later, a Kindled elder will be able to challenge the title-holding player in turn. Store the title in `CourtProgress` now so that can be added without a data change.

### Shared boss plumbing

Both minibosses compose the existing helpers; there's no shared abstract boss entity:

- `BossReplayClock` for synced animation time and `BossMoveTimeline` for each move's windup, active and recovery windows (`BossAnimationClockGuardrailTest`).
- A `BossBarFrame` each (`client/screen/overlay/boss/frame/`): flame-crown tentacles for the Champion, gold fronds for the Warden.
- An opening scene each in `BossSceneCatalog`.
- `BossTelegraphs` for readable attacks; custom VFX to the Riposte standard (`docs/MANIPULATION_VISUALS.md`); no vanilla particle clouds.
- Models from `tools/model_export/` generators with `_anim.java` files, like `hemorath.py` / `hemorath_anim.java`. The Champion shares Track 9's Kindled elder body; the Warden builds on the Arbor role, on Track 9's jointed rig, with the Sunburst Primal attachments.

## Tier 3: the Court conflict (DEFERRED)

The powwow's tentative destination is a conflict between the poles, with the middle houses leaning toward their ends. Under the rule above, the player fights followers in that conflict and never Marimo or Hydra. The Tier 2 encounters are written so they can be reused as conflict beats (a Champion who comes as an enemy, a Warden defending the Garden). Don't build this until Track 11's membership and standing are settled.

## Review client

`courtPresenceReviewClient`: both presences with their first-sight scenes, the Hydra head rising, then each miniboss's phases against a test player (the operator-inbox pattern used by `bossPresentationReviewClient`).

## Tests

- GameTests: presences have no hitbox and can't be damaged by any source (melee, projectiles, manipulations, explosions); moss and head blocks refuse breaking; the head-rise entity can't be targeted.
- GameTests: each transgression wakes the Warden exactly once per act, and nothing else does (ordinary visiting, eating offered food, sneaking past); a held offering stops the Warden's attacks and opens reconciliation, while a new transgression made while holding one still wakes it; reconciliation consumes one offering and restores guest standing; the retry wait survives death and relog; the usurpation offer respects its gate; a yielded Champion survives and returns to the elders; the title moves to the victor; standing changes apply once; arena state (blooms, venting) restores afterwards.
- Add both presences to `CombatTargetingMatrixGameTests` as never-targetable, and both minibosses as normal targets during their fight only.
- `BossAnimationClockGuardrailTest` and `CombatTargetingGuardrailTest` stay green.

## Docs

HR Court section: the minibosses, their gates and rewards, and the rule that the poles are never fought. `docs/LORE_REFERENCE.md`: Marimo and Hydra entries (no God or Satan framing; Marimo grey and gentle but compelling, Hydra the cultivation of one).

## Open questions

None. All of this track's questions were answered 2026-10-29.

## Note from Track 11 (2026-10-30)

The state this track needs is built behind `CourtLife` (`common/capability/player/harbinger/court/`); see [11-court-life-and-progression.md](11-court-life-and-progression.md), "Service API". Warden triggers call `recordTransgression(player, actId)`; `holdsOffering` and `reconcile` handle amends; `championGate`, `recordUsurpLoss`, `grantChampion` and `revokeChampion` handle the title and the one-day retry wait (stored in `CourtProgress`, copied on death).

## Handover (Tier 1 run, 2026-10-30)

### What landed

- **Blocks:** `marimo_moss` (`MarimoMossBlock`) and `marimo_heart` (`MarimoHeartBlock` + `MarimoHeartBlockEntity`, `MarimoHeartRenderer`), both strength -1 / blast-proof / push-refusing / no loot, with items in the Brined Court creative section. Textures, models and blockstates from `tools/model_export/marimo_moss.py` (4-frame breathing textures at 16 px; grey-green moss with the Giving Light's gold filaments; the heart's filaments gather into pale sun-cored nodes).
- **Placement:** `CourtPresencePlacement` (called from `CourtStructure.afterPlace` for the Garden) reads the `court_mark/marimo_presence` mark and lays a lumped dome of moss on the island, the heart on its crown, three whole-moss petrified followers in its flanks, six runners over the pool floor and six threads up the shaft wall to the top gallery. It replaces only wall/floor moss or stone bricks.
- **Hydra:** `HydraRisingHead` (entity `hydra_rising_head`, MISC, no AI, no hitbox, never hurt, never saved; synced age replayed through `BossReplayClock`; pure `pose(t, reachTo)`), drawn by `HydraRisingHeadRenderer` from Kindled Tissue neck segments and a scaled Hydra Crown that turns over the player.
- **Service:** `CourtPresences` (`common/entity/npc/court/`): Marimo's first sight and chorus, the Head-tender's call (`onPlayerTick`, `callHead`, `tallestCrown`), and the break/explosion refusals (`refusesBreak`).
- **State:** `CourtProgress` gains `PresencesSeen` (a sorted string list; new key, no migration), read and written through `CourtLife.hasSeenPresence` / `markPresenceSeen`.
- **Scenes:** `BossPresentationIds.SCENE_MARIMO_PRESENCE` / `SCENE_HYDRA_RISE`, `MarimoPresenceScene` (no entity: `BossScenes.playTo(player, scene)` sends id -1) and `HydraRiseScene`, registered in `BossSceneCatalog`. Cards `boss.hemomancy.marimo` / `boss.hemomancy.hydra`.
- **Text:** chorus lines `hemomancy.court.presence.marimo.*`, the Head-tender's call, refusals; a Marimo's chorus sheet and contrast row in `docs/NPC_VOICES.md`.
- **Tests:** `court_presence_validation` (`CourtPresenceGameTests`, 6), `CombatTargetingMatrixGameTests.courtPresenceIsNeverATarget`, `CourtSeatValidation` asserts one heart and the moss mass, `CourtPresenceStateTest`. Goldens refreshed (registry ids, creative order).
- **Review:** `courtPresenceReviewClient` (`CourtPresenceClientReview`, `-PcourtPresenceSeed`, default 42) in `build/court-presence-review/screenshots/{marimo,hydra}`.
- **Docs:** HR §29 "The presences: Marimo and Hydra", `LORE_REFERENCE.md` "Marimo" and "Hydra", `wiki/Brined-Court.md` "Marimo and Hydra".

### Calls made unattended

- **Marimo is placed at runtime, not in the template.** The undercroft template only carries the mark; `afterPlace` lays the mass, as the Sanctum's heads are laid. No template regeneration, so Track 7's NBT and `CourtSeatTemplateTest` are untouched.
- **First sight is once per player, durable** (`CourtProgress.PresencesSeen`), not per session.
- **The Head-tender calls the head on first approach** (within 12 blocks), not through a dialogue option. A repeat viewing on request (a Head-tender option) is left for Track 11 or Tier 2, where the head also crowns the Champion.
- **The heart's scene has no camera shot**: a shot needs an entity, and the spec gives Marimo none. It uses the player's own view with letterbox, a dip toward the island and the card.
- **Protected set pieces:** Marimo's moss refuses breaking everywhere outside Creative; petrified followers, Hydra Crowns and Kindled Tissue refuse only inside a Court site (`CourtLife.houseAt`), since they also have decor items. Creative always may break, for building and testing.
- **Hydra's head is built from the set-piece block models** (Kindled Tissue neck, Hydra Crown at x2.2) instead of a generated entity model, so it reads as the chimney heads come alive and needs no new atlas. A bespoke `tools/model_export/` head is a Tier 2 follow-up if the Champion's body wants to share it.
- **Pulse visual** uses the vanilla `eyes` (additive) render type over the heart's own sprite plus a ring of `HemoParticleData.glow` motes; no vanilla particles.

### What remains (Tier 2, not started)

- **Bloom Warden:** `CourtPresences`-style hooks calling `CourtLife.recordTransgression` for each listed act (Garden growth, the undercroft's moss and petrified, harm in the undercroft or at a resident, a second gift); the Warden entity on the jointed rig with Sunburst Primal attachments (`tools/model_export/` generator + `_anim.java`), bloom patches fed by light shafts, uprooting, `BossBarFrame` (gold fronds), opening scene, offering stand-down and reconciliation dialogue (`CourtLife.holdsOffering` / `reconcile`), re-anchoring and the chorus naming the wrong, death on loss.
- **Kindled Champion:** the usurpation offer through the Head-tender (`CourtLife.championGate`), the Champion entity on Track 9's Kindled elder body with neck sweeps, Stung crown, crown grabs, regrowth and budding phases, vent heat hazards, `BossBarFrame` (flame-crown tentacles), opening scene; on a win the old Champion yields to the elders, `HydraRisingHead` crowns the victor and `CourtLife.grantChampion`; on a loss `recordUsurpLoss`.
- The Tier 2 GameTests listed under "Tests", the matrix rows for both minibosses, and their review-client phases.
- Visual follow-ups seen in the review: the Hydra rise shot sits in vent water and reads dark; the overhead crown's tentacles fill the frame when the camera is under them.

## Handover (Tier 2 run, 2026-10-30)

### What landed

- **Rules:** `BloomWardenRules` and `KindledChampionRules` (`common/entity/boss/court/`): acts, debounce, phases, moves as `BossMoveTimeline`s, shield and heal, bud stages, regrowth, vents, yield.
- **Transgressions:** `CourtTransgressions` (`common/entity/npc/court/`): breaking `hemomancy:court_growth/giving_light` inside the Garden, reaching for a petrified follower or Marimo's moss there (refused or not), and harm reaching a Giving Light resident (`CourtMemberEntity.hurt` now reports the attempt). Each calls `CourtLife.recordTransgression` and `BloomWardenEntity.wake`.
- **Bloom Warden:** `BloomWardenEntity` (entity `bloom_warden`, on the Arbor role so it wears Sunburst at Primal through `CourtStrainLayer`), `WardenBloomBlock` (`warden_bloom`, itemless, drawn with the Sunburst bloom pad), bar frame `BloomWardenFrame`, scene `BloomWardenScene`, model `CourtBloomWardenModel` from `tools/model_export/court_bloom_warden.py` + `court_bloom_warden_anim.java` (rooted kneel, uprooted stoop, root lash, frond spear, sowing, re-anchoring).
- **Kindled Champion:** `KindledChampionEntity` (entity `kindled_champion`, on the Kindled elder role), `KindledChampionTrial` (the Head-tender's challenge and the death hook), bar frame `KindledChampionFrame`, scene `KindledChampionScene`, model `CourtKindledChampionModel` from `tools/model_export/court_kindled_champion.py` + `court_kindled_champion_anim.java` (the elder's gait plus two rings of buds, rearing, neck sweep, crown sting, crown grab, yielding).
- **Dialogue:** the Head-tender's "challenge" option (`CourtMemberDialogue.CHALLENGE`, built only for the Head-tender from `CourtLife.championGate`), routed in `CourtDialogueEvents` to `KindledChampionTrial.challenge`. Lines in the Head-tender's voice; the chorus names each wrong.
- **Follow-ups fixed:** the Hydra rise shot starts 3.5 blocks up and further out (out of the vent water); the overhead pitch eases from -38/-32 to -24/-20 so the crown's tentacles no longer fill the frame; a Head-tender that finds no crown waits `CourtPresences.CROWN_RESCAN_TICKS` (600) before searching again.
- **Tests:** `CourtFollowerGameTests` (9, in `court_presence_validation`), `CombatTargetingMatrixGameTests.courtFollowersAreTargetsOnlyInTheirFight`, `BossPresentationBindingTest` now counts 14 bars, `RegistrationGameTests` lists `warden_bloom` as itemless, registry-ids golden refreshed.
- **Review:** `courtPresenceReviewClient` adds `warden/` and `champion/` folders after the presences.
- **Docs:** HR section 29 "The followers who fight", `LORE_REFERENCE.md` Marimo and Hydra entries, `wiki/Brined-Court.md` "The Bloom Warden and the Kindled Champion".

### Calls made unattended

- **The Warden is its own entity type**, a `CourtMemberEntity` subclass on the Arbor role, spawned beside the nearest Arbor (or four blocks in front of the player when none is near). The Arbor it uproots beside stays where it is.
- **Amends with the Warden is using the offering on it**, not a dialogue tree: it calls `CourtLife.reconcile` and ends the fight. The Lightkeeper's existing amends option still works.
- **"Taking a gift a second time" has no hook.** The dialogue never offers a taken gift, so there is nothing to catch until Track 11 adds a shared table; the act id `second_gift` is reserved and the chorus has its line.
- **"Raising a weapon in the undercroft" counts only when harm reaches a resident.** Swinging at the air in the undercroft is not caught; damage that reaches any Giving Light resident (or any resident standing in the Garden) is, even though it is refused.
- **Garden growth** is the block tag `hemomancy:court_growth/giving_light`: the four Sunburst blocks, moss block and carpet, farmland, glow lichen, azaleas, dripleaves, crops, flowers and leaves. Breaking it counts only inside the Lighthouse Garden's pieces.
- **One act is the same act repeated within 40 ticks**, read from the outstanding transgression's own act id and time, so no session map is needed.
- **Warden numbers:** 160 health; rooted 8 s, uprooted 5 s; a quarter damage while any bloom is fed; 0.75 heal per fed bloom per second, capped at 4; blooms wither 5 s after losing light; up to 12 blooms within 9 blocks. "Fed" is light from above: open sky, or a block giving light 10 or more up the column, stopped by a solid roof.
- **Champion numbers:** 220 health and armour 6; regrowth 2 / 3 / 4.5 per second by crown stage with no pause after a hit (the burst requirement comes from the rate); buds at 66% and 33%; yields at 10%; vents every 6 s, three at a time, one under the challenger.
- **The Champion exists only for a fight.** It takes the nearest Kindled elder's place (that elder is removed) and on any ending steps back onto the same mark as an ordinary Kindled elder; no Champion NPC stands in the Sanctum between challenges. The title moves at the moment it yields, before the head rises.
- **Bars show only to the player in the fight**, since both fights are personal.
- **Both followers save.** A Warden reloaded mid-fight resumes if its player is online and otherwise ends after its grace; a Champion whose fight had ended goes back to the elders on load.
- **No spawn eggs** for the followers; they only exist inside their fights. The review client and the tests call them up directly.
- **The challenge event is not in `CourtMemberDialogue.EVENTS`** (it is not hospitality) and has no row in the dialogue-route fingerprints yet; `CourtFollowerGameTests.theUsurpationRespectsItsGate` covers the option and the route's service.
- **Visuals** stay inside the custom suite: `BossTelegraphs` rings, lines and cones, HutosLib tendrils for roots, the light spear, the bloom feed and the crown grab, glow motes and ember sparks for the vents. The Warden's blooms reuse the Sunburst bloom pad model, so no new block art was drawn.
- **Blood profiles for the Court.** `alphaCheck`'s high-health sampling rule failed for every Court member (30 health, no profile; this predates Tier 2). Each `court_<role>` and both followers now ship an empty profile that requires the living syringe, like the other bosses and NPCs, and `restricted_entities.json` lists them.

### Verification

`test` (4807), `runCourtPresenceGameTestServer` (15), `runCombatTargetingGameTestServer` (22), registration, dialogue-route, Court NPC and Court life suites, and the full `alphaCheck` (1262 GameTests) pass. `courtPresenceReviewClient` captured all 42 shots, including `warden/` (11) and `champion/` (12); the Champion reads clearly, while the Warden sits at the edge of the undercroft frame and needs its camera moved in a later pass.

## Review (2026-10-30)

- **Fixed: reaching for Marimo's moss never woke the Warden in play.** `marimo_moss` and `marimo_heart` have strength -1, so survival mining never completes and `BlockEvent.BreakEvent` never fires; the GameTest passed only because `gameMode.destroyBlock` skips mining. `CourtTransgressions.onReach` now counts the first swing (`LeftClickBlock`, `START`) at an unbreakable Marimo block inside the Garden as the undercroft wrong. Petrified followers (strength 3) still count through the break. New GameTest `CourtFollowerGameTests.reachingForMarimosMossIsAWrong`.
- **Review framing:** `CourtPresenceClientReview` now moves the woken Warden onto the line between the camera and the heart, so the rooted/move shots frame it in the middle. Not yet re-shot.
- Rerun: compile (main, gameTest, test), `test`, `runCourtPresenceGameTestServer` 16/16, `runCombatTargetingGameTestServer` 22/22, dialogue-route 1, Court life 8, Court NPC 25, registration 47. Impact map regenerated.

