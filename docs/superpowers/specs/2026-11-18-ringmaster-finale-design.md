# The Ringmaster's finale, rebuilt in his Chamber (design, 2026-11-18)

## Intent

The Ringmaster's finale is the fight the whole Crimson Troupe builds toward, and it has fallen behind the seven puppet
ordeals: short wind-ups, thin footprints, no weight, no true self, and it is the one fight still staged in the real
world, where villagers, mobs, other players and terrain all interfere. The owner wants the fight to show that his Will
has grown so strong his mind has begun to merge with reality: the fight should *look* like it happens in the real big
top while the Chamber of Will bleeds in at the edges, with theatre and the professors' true selves performing in it.

The owner's decision (2026-11-18) is to run the whole fight in his Chamber of Will, on a copy of the pavilion, and only
make it look like a hybrid: the player is moved there seamlessly and does not know they have crossed until the tent
begins to come apart.

## Goals

- A seamless, hidden crossing from the real pavilion into a copy of it in his Chamber, under his opening scene.
- The merge as the fight's visible progress: the tent's edges dissolve into his own Chamber sky until the ring stands
  as an island in his mind.
- Two phases (the owner's phase list, below), then a kneel where the player chooses the ending by their own hand.
- Every move to the ordeal standard the owner approved: bold floor telegraphs, a slow gather, a fast blow, a hit-stop,
  a heavy recovery, impact sound, custom particles, a camera jolt on big hits.
- The professors' true selves from their ordeals perform in the dark; he calls one at a time for a signature move.
- The outcome lands on the real site when the player is returned (ruin and freed captives, or tenancy and the Pattern).

## Non-goals

- No change to the seven puppet ordeals beyond what the echoes reuse.
- No new routes: the two endings are the existing Liberation and Succession outcomes.
- No multiplayer finale: one owner fights, as today.

## Anti-goals (a failure even if it works)

- A visible "teleport" moment: a cut, a loading screen, a pop of the tent or the cast. The crossing must not show.
- Echoes crowding the ring: the professors mostly perform; only one at a time acts on his cue.
- Fakes that are a coin toss: reading the real Ringmaster among his copies must be a skill with a learnable tell.
- Anything that moves through molasses (the owner's word for the old fight and the first ordeal footage).
- Vanilla particles, brass/copper on blood-side things, hard-coded English, a second open-world fallback.

## Constraints

- AGENTS.md rules bind: `Hemomancy.rloc`, en_us keys for all text (lines in his voice, `docs/NPC_VOICES.md`),
  `HemoParticleData` (no vanilla `ParticleTypes`), `PlayerBlockEdits` for any player-caused block edit,
  `TransientServerStateEvents` for session maps, the boss kit (`BossMoveTimeline`, `BossReplayClock`,
  `BossGroundHazardEntity`), the one world-effect dispatcher, `HemoEasing`, the guardrail tests.
- The mod is not released: player data needs no migration; old route state may be dropped.
- He stays a boss (`hemomancy:bosses`): every boss cap and immunity applies.
- One Gradle process at a time; review clients film the fight at the real 20 ticks a second.

## Approaches considered

1. **The finale as the Ringmaster's own ordeal (chosen).** Reuse the ordeal machinery (`OrdealDirector`: a private
   Chamber stage, the saved return point, home on logout, the caught killing blow, outcomes settled on return), with
   the pavilion template as his stage, a new finale controller for his two phases and the kneel, the husk walk-in's
   seamless crossing, the professors' existing true-self entities as echoes running their existing ordeal moves, and
   the Somatic Loom's stencil dome (shrinking) for the merge. Chosen: most of the risky parts are already proven, and
   the echoes come almost free.
2. **A standalone Chamber encounter like Vesper's.** Its own manager. Rejected: it duplicates the stage, return, catch
   and logout handling, and the echoes would need the ordeal pieces anyway.
3. **The merge in the real world.** The owner's first idea; dropped by the owner because the overworld's NPCs and other
   interference are too hard to account for.

For the merge itself: physically breaking the copied tent down block by block was considered and rejected as the main
effect (slow to stage, cannot snap back at the end, fights the stage's cleanup); a few torn pieces as the dome passes
are allowed as accents.

## The design (owner's decisions, then proposals marked as such)

### Starting the finale and the crossing

- The finale still begins from his dialogue at the real pavilion (the existing `circus_begin_finale` event and its
  gates: Degree 4, the school report, the attention act), but **the route choice before it is removed** (owner,
  2026-11-18): no Succession/Liberation option, no warning-and-confirm, no route repair. The conversation leads to the
  finale; the ending is chosen at the kneel. The finale opens after the attention act, as Liberation's does today.
- **Absorption still has Succession's requirements** (owner, 2026-11-18, option b): 1000 acclimation, the three
  Succession acts (challenge bits 2 to 4, with their banked spectacles) and not Unstained. A player who lacks them can
  still finish the fight and kill him, but absorbing him is refused at the kneel: his blood does not rise toward them and
  he tells them they are not fit to hold the whip. The act chain stays as the way to earn it; the Unstained keep only
  the kill (D-31 holds). Absorption also needs active blood, as it always has.
- The crossing is hidden and seamless (owner). The player is moved to the copy at the matching position and facing
  under his opening scene, borrowing the husk walk-in's pieces (`ChamberProxyView`, `ChamberVeil`,
  `ChamberTransitionScreen`) so no cut shows. Proposal: the real-site cast (Ringmaster, troupe, carousel) holds still
  while the player is inside, as a professor's body does during an ordeal, and the real site is marked owned as today
  (`CircusPavilionSavedData`).
- The stage is a copy of `crimson_troupe_pavilion.nbt` (81 x 23 x 65) placed on the ordeal grid in his Chamber, with
  his own Chamber sky theme (a new `ChamberSkyTheme`: strings, stage lights, the dark beyond a big top). The copy's cast
  are copies spawned on the stage (his true self, the finale troupe as needed, the carousel, the captives).

### The merge

- A stencil dome after `SomaticLoomDomeRenderer`: inside it everything is real; wherever a view ray would reach past
  its wall, his Chamber sky is drawn instead. The dome starts just inside the tent walls and shrinks with the fight
  (proposal: from the walls at the opening, through the galleries and seating during phase 1, to the ring curb plus a
  margin by phase 2), so the canvas, galleries and seating dissolve and the ring is left as an island. A faint
  membrane marks the edge. Accents: a few torn canvas pieces fall away as the edge passes. At the end the tent snaps
  back to real as the player is returned.
- In the dissolved dark (owner): theatre (blazing stage lights, sweeping spotlights, a phantom audience filling where
  the seats were) and the professors' true selves performing their acts.

### Phase 1: the rafters (health 100% to 50%), the arena against you (owner)

He stays up in the rafters, mostly passive, using the arena as his weapon.

- **How he is hurt up there (owner, the builder's proposal accepted):** he is hittable at range while on a perch, and
  after each fake-copy volley the real Ringmaster drops briefly to a low perch where melee can reach him, so a melee
  player has openings and reading the fakes pays off.
- **Rafter teleports with fakes (owner):** he blinks between rafters and leaves fake copies of himself; every copy
  throws a ghostly version of the same attack; only his is real. Proposal: one learnable tell on the fakes (no shadow,
  or his thread not reaching them, or a faint flicker), and a hit bursts a fake. The pavilion has no rafter positions
  today (his RAFTERS "beam" is just his perch), so the copy needs authored rafter perches (proposal: on the king and
  quarter poles, the rigging towers and the gallery rails).
- **Theatre as attacks (owner):** living string lights that snake out to entangle the player; spotlights that burn and
  blind; hordes of ghostly audience members dashing into and out of the ring as distractions; add mobs.
- **The carousel comes alive (owner):** the Impaled Procession spins up and fires its horses out in three directions;
  spins rapidly then sends them sprinting outward in a spiral; rises on its central pole and slams down in shockwaves.
- The merge advances through the phase.

### Phase 2: at about 50% health he comes down (owner)

- He descends from the rafters and reveals his **true self** (a new true-self model in the ordeal pattern: jointed,
  real hands, visceral, still recognisably him; the look is the builder's design, the owner reviews it).
- He swings around the carousel (using its pole as a pivot) and strikes with his staff; the arena attacks continue.
- **The professors' echoes (owner, "mixed"):** only the professors whose ordeals this player has passed appear (owner,
  2026-11-18), so the player's own history fills his Chamber; their true selves mostly perform out in the dark; on his cue he calls
  one at a time for a signature move into the ring. Proposal: the echoes are the professors' actual true-self
  entities, and a called echo runs one of that professor's existing ordeal interventions (their `OrdealChoreography`)
  aimed at the player on his ring.
- **The bat swarms (owner):** vampire-bat swarms hang upside down in the rafters, become active in phase 2, swoop down
  in small groups, steal the player's blood and carry it to him. The blood charges him; while charged he can use new
  attacks only that state allows, which burn the charge down. Proposal: a swarm killed before it reaches him denies
  the charge; the blood is paid through `BloodAccounting` with a named source. A player without active blood takes the
  bites as plain damage instead (the builder's call).

### The kneel and the ending (owner)

- The finishing blow does not kill him: he kneels, weakened. The player chooses by their action:
  - **Kill him with damage:** the circus falls and the captives are freed (today's Liberation: the ruin overlay, the
    troupe leaves, Thread Ripper granted, he dies).
  - **Use Blood Absorption on him** (like the end of the Vesper fight: an absorption progress on him; Vesper's
    `canBeAbsorbedBy` / `absorbWithBlood` pattern): the player takes his place as the new ringmaster and the circus
    goes on (today's Succession: the tenancy banner with the player's name, the Ringmaster Pattern granted).
- The two options must be apparent (owner). Proposal: he says one line naming both ways out (his voice sheet); the
  world shows both (the captives' threads on the Procession glow taut for the kill; his blood visibly offered, rising
  toward the player, for absorption); a short on-screen prompt the first time, naming the two actions.
- After Succession he is **diminished but stays** (owner, the builder's pick accepted): emptied of his Will, the
  retired old ringmaster, still the trade speaker and the conductor teacher, with changed lines.
- The outcome is applied to the real site as the player is returned (the tent snaps back to real first).

### Failure

Proposal, as the ordeals: a killing blow on the player is caught, they are thrown out of his Chamber with nothing
dropped, and the finale fails; the site is released and the finale can be begun again from his dialogue.

### The copies and the real site (the builder's calls)

- The captives on the copy are scenery on the horses: cutting them during the fight and the anchors go away; the real
  captives are freed only by the kill ending.
- While the owner is inside, the real Ringmaster and troupe are frozen and untouchable (as a true self: no AI,
  invulnerable); another player who speaks to him gets a line that he is elsewhere. The outcome is applied when the owner
  returns, loading the site if it is not loaded; an unload of the real site meanwhile releases nothing.

## What becomes of the old finale

- The real-world finale phases (RAFTERS, CAROUSEL, DESCENT in `CircusFinaleRules` and the Ringmaster's tick), the
  troupe-or-anchors CAROUSEL objective and the anchor mechanic are replaced. (Finding: `CircusCarouselEncounterRules.
  breakAnchor` is never called outside tests, so Liberation's DESCENT trigger is unreachable today.)
- The route enum loses its pre-fight choices; its readers (greeting lines, `CircusSchoolDialogue.aftermathKey`,
  `CircusPerformanceController` gating, `severCaptive`, the site's route) follow the outcome instead.
- His existing moves (knife, staff blow, Spotlight, Sleight, Cue, Knife Board, Strings Cut) are raw material: the
  builder keeps, reworks or drops each to fit the phases above.

## Left to the builder

All numbers (damage, wind-ups, cooldowns, merge radii, charge size); his true-self and Chamber sky looks; which add
mobs; the empowered-only attacks; which intervention each echo brings; the rafter perch positions; the exact tell on the
fakes; staging and camera for the crossing and the kneel; lines in his voice. Each is marked as the builder's call in
`docs/CIRCUS.md` for the owner's review.
