# Circus audit 02: bosses, encounters and combat

Date: 2026-11-06. Scope: the Ringmaster finale (RAFTERS, CAROUSEL, DESCENT; Succession and Liberation), the
Impaled Procession, the four Succession acts, the performers and faculty as combatants, and the GameTests that
cover them, measured against the boss standard set by the Saint combat kit (`93070b745`) and the Brined Court
minibosses. Method: source reads only. Nothing was built, run or launched, so "verified" means verified in source,
not seen in game. Paths are under `src/main/java/com/vincenthuto/hemomancy/` unless they start with `src/` or `docs/`.
Items already tracked elsewhere are cited by id (CT-n, D-n, A-n, AN-n).

## Summary

The Circus finale has the shared presentation (a bar frame, an opening scene, two phase cards) and nothing else
from the current boss standard. There is no move table, no `BossMoveTimeline`, no `BossIntroWindow`, no telegraph,
no arena record, no challenger scaling, no `canHarmFor` targeting, no custom particle and no Circus sound. The
Ringmaster himself does very little: in RAFTERS he throws four knives in 160 ticks from an untouchable perch, in
CAROUSEL he stands still, and in DESCENT he is an 80-health mob on a vanilla `MeleeAttackGoal`. Every troupe attack
lands on the same tick as its cue. The four Succession acts are "stay within 24 blocks for N ticks", and downing
the troupe empties all of them of pressure. The school that teaches puppetry refuses damage from most puppets in
its own finale. Two stuck states exist (an empty troupe stalls Succession for good; a Ringmaster killed by anything
but `hurt()` on Succession leaves the site owned forever, CT-7's cousin).

The Brined Court, the parallel path, has two minibosses (Bloom Warden, Kindled Champion) built on the shared
plumbing: intro windows, replay clocks, move timelines, `BossTelegraphs` during wind-ups, `canHarmFor`, an arena
radius and leash, and a yield at 10%. The Circus has none (C-13, D-18). The gap is not polish; the Circus combat was
built as state plumbing in September and never got a combat pass.

Counts: **15 findings** (4 High, 7 Medium, 4 Low). **14 allow-listed vanilla particle references** across the six
Circus combat files, and **zero** Hemomancy particles, telegraphs or materials in them. **0** Circus sound events and
**0** music. **11** `CircusRingmasterGameTests` (state and flow) and **2** playthrough finales run under Resistance V
and Strength XXX; no test reads a combat number, and there is no Circus review client.

## Encounter map

| Stage | What happens | Telegraph | VFX and sound | Code path |
|---|---|---|---|---|
| Attention act (challenge 0, neutral) | A NoAI trained peacock spider stands 3.5 blocks from the ring centre; keep it in view (dot > 0.92, within 10 blocks) for 200 ticks | none needed | none | `common/entity/npc/circus/CircusPerformanceController.java:67-78`, `:129-136` |
| Route choice (bit 1) | Accept or reject the pact in dialogue; +50 | n/a | chat line | `CircusRingmasterEntity.java:252-285` |
| Act 2, troupe-and-doll pressure | Every finale performer except the Knife Thrower turns on the player, one raises 2-4 Enthralled Dolls; survive 200 ticks within 24 blocks | 20-tick `WARNING_TICKS` once at the start of the threat | vanilla particles per performer | `CircusPerformanceController.java:80-92`, `:95-127` |
| Act 3, knife/displacement | Only the Knife Thrower; survive 240 ticks | none | `CRIT`, trident sounds | same, `:86-87` |
| Act 4, carousel coordination | Whole troupe plus the carousel; survive 300 ticks | carousel 20-tick spin-up once | `CRIMSON_SPORE` round the horses | same; `CircusCarouselEntity.java:68-89` |
| Finale opening | `circus_ringmaster` scene (90 ticks, input locked to tick 52); bar after 60 | n/a | camera, card, shake at the "first knife" | `CircusRingmasterEntity.java:176-177`; `client/render/cinematic/scene/CircusRingmasterScene.java:107-152` |
| RAFTERS (160 ticks) | Ringmaster pinned to his perch, untouchable; a knife at the owner every 35 ticks (4 knives, 4 damage, slowness); a manifested Vampire Bat aggregate at tick 1, again at 100 if gone; the troupe turns on the owner | none; the throw animation starts on the release tick | `ENDER_DRAGON_GROWL` on the phase change | `CircusRingmasterEntity.java:170-180`, `:224-239`; `CircusFinaleRules.java:6`, `:14-16` |
| CAROUSEL | Ringmaster idle on the beam ("presenting"); carousel spins while any performer is ALERT and hits that performer's target for 4 on contact. Succession: down every finale performer. Liberation: sever the three captives (right-click or Thread Ripper), then hit each exposed anchor once | carousel 20-tick spin-up | `CRIMSON_SPORE`, `CHAIN_HIT`, `IRON_GOLEM_ATTACK`, `CHAIN_BREAK`, `ANVIL_DESTROY`, `EXPLOSION` | `CircusRingmasterEntity.java:181-185`; `CircusCarouselEntity.java:146-165`, `:199-221`, `:277-296` |
| DESCENT | Gravity on; Ringmaster drops off the beam, `MeleeAttackGoal` at the owner, 80 health, 12 armour, 7 attack; teleports back if more than 12 blocks from the carousel | none | vanilla swing | `CircusRingmasterEntity.java:128-131`, `:186-196` |
| Succession end | A hit whose raw amount reaches his health sets him to 1 and completes on the same tick: Ringmaster Pattern, Topper, troupe reset | none | `CRIMSON_SPORE` x80, `TOTEM_USE` | `CircusRingmasterEntity.java:315-319`, `:334-357` |
| Liberation end | He dies; Thread Ripper memory, Topper, six cobwebs round the carousel, surviving performers reset to full | none | same | `CircusRingmasterEntity.java:323-332`, `:343-346`, `:369-376` |
| Failure | Owner death: `resetOwned` on clone. Owner over 64 blocks from the carousel (or absent) for 200 ticks: site reset | n/a | none | `capability/player/harbinger/circus/CircusPlayerProgress.java:131-135`; `CircusCarouselEntity.java:137-143` |

## Compared with the current boss standard

"Saints" is the Saint kit (`common/entity/boss/saint/`); "Court" is the Bloom Warden and Kindled Champion
(`common/entity/boss/court/`), the closest parallel in size.

| Standard item | Saints / recent boss | Ringmaster finale | Gap |
|---|---|---|---|
| Synced replay clock, move timeline | Every move a `BossMoveTimeline` replayed through `BossReplayClock` over a synced move block (`SaintMove.java`, `docs/BOSSES.md`); Court `moveClock` (`BloomWardenEntity.java:81`) | No moves. `phaseTicks` is a server-only field (`CircusRingmasterEntity.java:78`, `:175`); the throw is a vanilla `AnimationState` started by an entity event (`:68`, `:110-111`) | Nothing to replay. No guardrail breach (no `tickCount - start`), only because there is no clock at all |
| Telegraphs | Wind-up floors 12 ticks (melee) and 20 (area), footprint drawn from the same geometry the server tests (`SaintMove.java:18`, `SaintMoves.java:98-110`); Court draws `BossTelegraphs` through every wind-up (`BloomWardenEntity.java:369-374`) | None. Damage on the cue tick for the Acrobat, Fire Eater, Stilt Walker and Ringmaster (B-2) | Total |
| Phase structure | `SaintPhaseTable`, cuts at recovery boundaries | Three phases by timer and condition (`CircusFinaleRules.java:11-23`); the boss acts only in RAFTERS | The boss has no fight of his own (B-3) |
| Intro | `BossIntroWindow`: passive, untouchable, bar hidden, saved (`SaintTrialEntity.java:84`; `KindledChampionEntity.java:75`) | Client scene only; knives and bats run during the input lock (B-6) | No server half |
| Arena bounds and reset | `SaintArena` restores on every exit, empty chamber 600 ticks resets (`SaintTrialEntity.java:417-418`); Champion `ARENA_RADIUS` 12, `LEASH` 32 (`KindledChampionRules.java:19-21`) | Owner radius 64 from the carousel for 200 ticks (`CircusCarouselEntity.java:138-140`); acts use 24 (`CircusPerformanceController.java:101`); no player tether | Loose and inconsistent (B-12) |
| Bar, frame, opening, cards | Frame, `saint_rise`, phase cards | Frame, opening, CAROUSEL and DESCENT cards; owner only; bar fixed at 1.0 for two phases; the Liberation DESCENT card never plays (B-7, B-14) | Partial |
| Music and sound | Vesper, Mycophant, Priestess music (`common/init/SoundInit.java:303`, `:319`, `:341`); `SaintSoundEvents` | Vanilla sounds only; no Circus sound event besides the doll ambient; no music (B-9) | Total |
| Death and defeat | `die()` refuses everything but `/kill` (`SaintTrialEntity.java:1052-1054`); one `endTrial`; Champion yields at 10% with a 60-tick beat (`KindledChampionRules.java:16-17`) | Nonlethal guard in `hurt()` only; `die()` unguarded on Succession; completion on the hit tick with no yield beat (B-4, B-13) | Stuck-state risk, no ending beat |
| Loot | `entities/<saint>` rolled once in `endTrial` | Topper and Pattern or memory given in code; no `entities/circus_ringmaster` table; `xpReward = 0` | Fine for a story reward; Liberation kill drops nothing |
| Multiplayer | Meter and health scale with challengers (`HemorathFightGameTests.java:459`) | One owner; everyone else's hits are refused silently, but troupe AoE and knives hit them (B-8) | Solo by construction |
| Targeting | `canHarmFor` on every move (`SaintMoves.java:53`, `BloomWardenEntity.java:391`) | Raw `LivingEntity` gathers (B-8) | Not on the shared rule |
| Despawn and recovery | Reloaded Saint restores its chamber on the first tick (`saint_kit_validation`) | Persistent entities; CT-7 open; empty-troupe stall (B-4) | Stuck states |
| Nonlethal handling | Champion yield, Saint `endTrial` | Performers DOWNED at 1 health with `CircusDownedPose`; the Ringmaster has no downed or yield pose | Ringmaster missing |
| Hit feedback | Openings, hit classes, refusals answered | `isAttackable()` false in RAFTERS and CAROUSEL; refused hits make no sign (`CircusRingmasterEntity.java:444`) | Silent |
| Animation | Generator rigs, Catmull-Rom keys per move, coil, arc, follow-through, settle | Hand-written `HumanoidModel`, hard pose cuts per phase, throw plays after the knife leaves, vanilla stride and swing (B-10) | Well below |
| VFX | Per-Saint telegraph passes and shaders at the Riposte bar | 14 vanilla particle references, no Hemomancy particle (B-9) | Total |
| Tests and review | Kit 8 + 19 to 20 per Saint fight; fight review clients | 11 state tests, 2 god-mode playthroughs, no review client (B-15) | No combat coverage |

## Findings

### B-1 · High · The Puppeteering school's finale refuses damage from most puppets

Evidence: the Ringmaster takes damage only when `source.getEntity()` is the owning `ServerPlayer`
(`common/entity/npc/circus/CircusRingmasterEntity.java:310-314`); finale performers refuse any source whose entity is
not the owner player (`CircusPerformerEntity.java:185-186`); the carousel the same (`CircusCarouselEntity.java:279`).
Puppet bodies hit as themselves: Gorebound Hulk `doHurtTarget` (`common/entity/summon/GoreboundHulkEntity.java:36`),
Veinwing Vulture `doHurtTarget` (`VeinwingVultureEntity.java:100`), both `mobAttack(this)`; the Marrow Juggler's
dagger names the Juggler as causer (`common/entity/projectile/CircusKnifeProjectileEntity.java:60`); the Mnemonist
Puppet uses `magic()` with no entity (`MnemonistPuppetEntity.java:162`). Only Cinder Bellows attributes to its owner
(`CinderBellowsEntity.java:72`) and would count.
What is wrong: the player who has just learned seven bodies from this troupe cannot use most of them against it, in
acts or finale. The Ringmaster Pattern, the Succession reward, is a Conductor that makes puppets better, but the
fight that grants it is won with a sword (the playthrough test wields a Netherite sword under Strength XXX,
`src/gameTest/.../playthrough/PlaythroughCrimsonTroupeGameTests.java:1150`, `:1701-1705`).
Fix: accept a source whose entity is a `BoundPuppeteerSummon` owned by the site owner (resolve through
`BoundSummonBehavior` / `SyncedOwner`), in all three `hurt()` overrides, through one helper in `CircusPavilionStateRules`
(`canActThrough(source, owner)`). Then design at least one finale beat that wants a puppet (B-3).

### B-2 · High · No attack in the Circus has a telegraph; damage lands on the cue tick

Evidence: the Acrobat teleports two blocks behind the target and hits on the same tick
(`CircusAcrobatEntity.java:84-95`, `:97-106`); the Fire Eater bursts flame and damages its 55-degree cone on the same
tick (`CircusFireEaterEntity.java:59-78`); the Stilt Walker's slam plays its sound and debris and hits everything in
3.5 blocks on the same tick (`CircusStiltWalkerEntity.java:78-96`); faculty swing every 30 game ticks with no cue
(`CircusFacultyEntity.java:25-28`); the Ringmaster fires the throw event and spawns the knife on the same tick
(`CircusRingmasterEntity.java:224-230`), and the model's wind-up occupies the first 30% of the 12-tick beat, so the arm
draws back after the knife has left (`client/model/entity/npc/CircusRingmasterModel.java:168-173`, `:219-228`). The
only warnings are the 20-tick `WARNING_TICKS` when a threat is first set (`CircusPerformerRules.java:4`) and the
carousel's one 20-tick spin-up (`CircusCarouselRules.java:5`, `:18-20`).
What is wrong: the Saint kit's floor is 12 ticks of wind-up for melee and 20 for area moves, with the footprint drawn
from the hit geometry (`common/entity/boss/saint/SaintMove.java:18`); the Court minibosses draw `BossTelegraphs`
through each wind-up (`common/entity/boss/court/BloomWardenEntity.java:369-374`). Circus damage cannot be read or
dodged, only out-healed.
Fix: give every performer attack and every Ringmaster move a `BossMoveTimeline` (wind-up, commit, active, recovery)
with the kit's floors, fire the throw event at wind-up start and the knife at commit, and draw the cone, ring and
landing point with `BossTelegraphs` (or a `BossFootprint` if the moves move onto the kit's generic half, decision 1).

### B-3 · High · The Ringmaster has no fight of his own, and none of it is the Circus's mental palette

Evidence: RAFTERS is 160 ticks (`CircusFinaleRules.java:6`) of a knife every 35 ticks at the owner, four in all, from
a perch he is pinned to and cannot be hit on (`CircusRingmasterEntity.java:170-180`, `:444`); CAROUSEL clears his
target and stops him (`:181-185`); DESCENT is `MeleeAttackGoal` (`:128-131`) with 80 health, 12 armour and 7 attack
(`:88-92`), no knives, no swarm calls, no moves. Nothing in the finale uses illusion, decoys, displacement, attention,
light or sound, which `docs/CIRCUS.md` "Rulings that bind" (C-02, C-09) make the Circus's identity.
What is wrong: the finale boss of a whole Harbinger path is a vanilla melee mob after two set pieces that other
entities run. A player on a two-block pillar is out of his reach for all of DESCENT (he has no ranged move and only
returns to the carousel at 12 blocks, `:192-194`). The Brined Court's Kindled Champion, a Tier 2 miniboss, has more
moves than this finale.
Fix: a Ringmaster move table on the shared plumbing, built around the Circus palette, for example: the Spotlight
(an attention cone that roots a player who looks away, the inverse of the attention act); Sleight (he and two
illusory doubles swap places through the troupe's own vault, only the real one bleeds); the Cue (a staff rap that
commands every standing performer to strike at once, the carousel accelerating on it); the Knife Board (a fan of
knives that outlines the player, a dash to break it); Strings Cut (in DESCENT he severs his own threads to the troupe
and each downed performer gives him one borrowed act, the Vesper roster idea at Circus scale). Give him a ranged
answer to pillaring and a reason to stand on the carousel hub in CAROUSEL, as the docs already claim he does.

### B-4 · High · Two stuck states: an empty troupe stalls Succession, and an unguarded `die()` leaves the site owned

Evidence: Succession leaves CAROUSEL only when `!troupe.isEmpty() && troupe.stream().allMatch(isDowned)`
(`CircusRingmasterEntity.java:197-198`). Liberation makes performers lethal (`CircusPerformerEntity.java:187`), killed
performers never come back, and a failed attempt releases the site (`CircusPavilionSavedData.java:94-117`). The one
route repair then flips Liberation to Succession while the outcome is still NEUTRAL (`CircusRingmasterEntity.java:270-278`,
`capability/player/harbinger/circus/CircusPlayerProgress.java:66-74`). Route state is per player but the troupe is
the pavilion's, so a second player's Liberation run empties the troupe for everyone. With every finale performer
dead, CAROUSEL never ends, and acts 2 to 4 have nobody in them (`CircusPerformanceController.java:80-92`), so they pass
by waiting. Separately, Succession's nonlethal guard lives only in `hurt()` (`:315-319`); `die()` completes only for
Liberation (`:323-332`), so on Succession any death that skips `hurt()` (an admin kill, a discard by another system)
leaves the site owned with no Ringmaster, the state CT-7 probes (`PlaythroughCrimsonTroupeGameTests.java:1512-1570`).
Fix: copy the Saint `die()` guard (`common/entity/boss/saint/SaintTrialEntity.java:1052-1054`) onto Succession; on any
real loss of the Ringmaster, settle or release the site with a line (CT-7's options). For the troupe, choose one
(decision 6): respawn the finale cast from the template when a site resets, refuse the repair once performers have
died, or count "no finale performer left" as downed and say so.

### B-5 · Medium · The Succession acts reduce to "down the troupe, then stand still"

Evidence: acts 2, 3 and 4 complete on a timer alone (200, 240 and 300 ticks) while the owner stays within 24 blocks
(`CircusPerformanceController.java:101`, `:113-120`). A downed performer stays downed for as long as its threat is
within 48 blocks (`CircusPerformerEntity.java:156-165`), and the carousel spins only while a performer is ALERT
(`CircusCarouselEntity.java:70-72`), so downing the four performers (26 to 40 health, `CircusAcrobatEntity.java:30`,
`CircusStiltWalkerEntity.java:28`) empties the act. The attention act is a stare at a NoAI spider
(`CircusPerformanceController.java:70`, `:135`). "Knife/displacement" arms the Knife Thrower only
(`:86-87`), who has no displacement; the only displacement in the troupe is the Acrobat's vault, which act 3 leaves out.
"Carousel coordination" asks for no coordination. The finale's CAROUSEL phase is the same "down the troupe" task again.
What is wrong: the five challenge bits that gate Succession test nothing the player does with puppetry or attention.
Fix: give each act an objective the timer does not cover: act 2, keep a doll or one of the player's own puppets alive
while the troupe hunts it (the Mummer practical already tests diversion, `CrimsonTroupePracticalGameTests.java:103`);
act 3, the Knife Board with swaps between marked spots; act 4, strike the carousel's three riders in the order the
Ringmaster calls; attention, keep the spider in view while the troupe performs distractions round it. Or replace acts
2 to 4 with the mini-boss ladder (decision 4).

### B-6 · Medium · The opening has no server intro window; knives and bats hit a player whose input is locked

Evidence: the scene locks input and hides the HUD until `OPEN = INTRO_TICKS - HANDBACK` = 60 - 8 = 52
(`client/render/cinematic/scene/CircusRingmasterScene.java:107`, `:127-129`; `client/render/cinematic/BossScene.java:20`).
The server spawns the bat aggregate on tick 1 and throws the first knife on tick 35
(`CircusRingmasterEntity.java:179-180`), and the troupe is turned on the owner from the first RAFTERS tick (`:167-169`).
Every other boss with an opening runs `BossIntroWindow` (Annetta, Naeglerophaeon, the Crowned Refusal, both Court
minibosses, Phlegethosoma, every Saint).
Fix: a `BossIntroWindow(INTRO_TICKS)` saved with the Ringmaster; hold knives, the swarm and `beginFinale` on the
troupe until it ends; start RAFTERS' 160 ticks after it.

### B-7 · Medium · Liberation can skip CAROUSEL, never shows its DESCENT card, and its designed tool is not in reach

Evidence: `severCaptive` and the carousel's `hurt` check owner and route but not phase
(`CircusCarouselEntity.java:199-205`, `:277-283`), so a Liberation player can sever and break during RAFTERS. The
carousel sets DESCENT itself when the third anchor breaks (`:290-291`); the Ringmaster's next tick reads DESCENT as the
current phase, so `next != site.phase()` is false and the DESCENT card and growl never play on Liberation
(`CircusRingmasterEntity.java:197-210`), and the Liberation branch of `CircusFinaleRules.nextPhase` (`:17-20`) is dead.
The archived design has Thread Ripper "mercifully kill and sever" each rider (`docs/archive/tracks/circus/04-creatures-encounters-and-rewards.md:55`),
but Thread Ripper is the Liberation reward and its only grant is `grantLiberationMemory` (`CircusRingmasterEntity.java:359-367`).
In practice a right-click on the invulnerable captive severs it (`common/entity/mob/monster/BloodDrunkPuppeteerEntity.java:134-136`),
and the playthrough test does exactly that (`PlaythroughCrimsonTroupeGameTests.java:1198`). An anchor breaks on one hit
of any player-sourced damage while the player looks at it (`CircusCarouselEntity.java:223-237`, `:284-288`).
Fix: gate severing and anchor hits on CAROUSEL; have the carousel report "destroyed" and let the Ringmaster's phase
step own the transition (one writer of the phase), so the card plays. Decide how a rider is severed (decision 5) and
give each anchor a short exposed window and a few hits rather than one.

### B-8 · Medium · Multiplayer: one owner, silent refusals for everyone else, and troupe AoE that hits bystanders

Evidence: only the site owner can damage the Ringmaster, the performers or the carousel (`CircusRingmasterEntity.java:313-314`,
`CircusPerformerEntity.java:185-186`, `CircusCarouselEntity.java:282-283`); the bar, scene and cards go to the owner only
(`CircusRingmasterEntity.java:118-120`, `:176`, `:203-207`). But the Fire Eater's cone hits any `LivingEntity` within 5
blocks that is not troupe (`CircusFireEaterEntity.java:69-77`), the Stilt Walker's slam any within 3.5
(`CircusStiltWalkerEntity.java:88-95`), and troupe and Ringmaster knives hit anything but troupe
(`CircusKnifeProjectileEntity.java:29-32`). None of these goes through `ManipulationCombatHelper.canHarmFor`, which the
Saints and the Court use (`common/entity/boss/saint/SaintMoves.java:53`, `BloomWardenEntity.java:391`). Saint fights
scale for two challengers (`src/gameTest/.../HemorathFightGameTests.java:459`).
What is wrong: a friend in the tent can be burned and knifed but cannot hit back, and gets no bar or explanation.
Fix: route every troupe hit through `canHarmFor(performer, victim)` and, during a finale or act, restrict it to the
owner (or the challenger set if co-op is allowed, decision 3); tell a refused non-owner once, as the ward tells a
refused block edit.

### B-9 · Medium · Every Circus combat visual is a vanilla particle, every sound is vanilla, and there is no music

Evidence: `src/test/resources/architecture/vanilla-particle-allow-list.txt:49-54` pins `CircusAcrobatEntity` 3
(`POOF`, `CLOUD`, `PORTAL`; `CircusAcrobatEntity.java:52`, `:78`, `:100`), `CircusCarouselEntity` 3 (`CRIMSON_SPORE` twice,
`EXPLOSION`; `:170`, `:215`, `:292`), `CircusFireEaterEntity` 3 (`FLAME`; `:41`, `:52`, `:65`), `CircusKnifeThrowerEntity` 1
(`CRIT`; `:75`), `CircusRingmasterEntity` 1 (`CRIMSON_SPORE` x80 at the ending; `:352`) and `CircusStiltWalkerEntity` 3
(block debris, a fair fallback). All but the stilts are "pending the vanilla particle sweep (A-33)". No Circus combat
file names `HemoParticleData`, `BossTelegraphs` or a school material; the one custom Circus combat visual is Thread
Ripper's `ManipulationVisuals.burst(THREAD)` (`common/manipulation/ductilis/ThreadRipperManip.java:66`). Sounds are
`ENDER_DRAGON_GROWL` for phase changes, `TOTEM_USE` for the ending, `ENDERMAN_TELEPORT`, `FIRECHARGE_USE`,
`TRIDENT_THROW`, `IRON_GOLEM_ATTACK`, `CHAIN_HIT`, `CHAIN_BREAK`, `ANVIL_DESTROY` (`CircusRingmasterEntity.java:202`, `:354`;
the performer and carousel files above). `SoundInit` has music for the Priestess, Vesper and the Mycophant
(`common/init/SoundInit.java:303`, `:319`, `:341`) and nothing for the Circus.
Fix: a Circus effect set from the existing toolkit: thread tendrils from the `PuppeteerThreadRenderer` look for the
Ringmaster's cues and the severing, glow motes and cells in the pavilion's crimson and bone for the vault and the
knives' trails, `ManipulationVisuals` Flammeus geometry for the fire breath, a collapse for the carousel. A calliope
music track, a whip-crack cue for the Ringmaster, a carousel organ that speeds with the spin. Read
`docs/MANIPULATION_VISUALS.md` first; attacks are bold (the 2026-11-06 ruling in `docs/BOSSES.md`).

### B-10 · Medium · The Ringmaster's model and animation are below the boss bar, and the docs describe poses he does not take

Evidence: the model is a hand-written `HumanoidModel` built in `createBodyLayer`, not a `tools/model_export/`
generator with the jointed rig (`client/model/entity/npc/CircusRingmasterModel.java:18`, `:32-140`); each phase sets a
fixed pose with no blend between them (`:151-167`, `:179-216`); DESCENT is the vanilla stride and swing (`:209-216`).
The template perches him at y 8, five blocks from the carousel at y 1 (`common/data/gen/CircusPavilionTemplate.java:124-129`),
and nothing moves him in CAROUSEL, so "presenting on the hub" (`docs/CIRCUS.md:8`, the model's own comment at `:192`)
is played on the beam. DESCENT turns gravity on and he falls seven blocks (`CircusRingmasterEntity.java:189-191`): the
descent is a drop, not a move. He has no downed or yield pose (only the performers use `CircusDownedPose`). A
destroyed carousel's horses simply stop rendering (`client/model/entity/npc/CircusCarouselModel.java:156`). AN-1
(2026-11-02) fixed the frozen perch; this is the next step.
Fix: move the Ringmaster to a generator (`tools/model_export/circus_ringmaster.py` + `_anim.java`, jointed rig, coat
tails and staff on springs), key each move on its own clock through `BossReplayClock`, author a descent (a swing down on
a rope or thread to the hub), a yield (kneel, topper off) and a carousel collapse.

### B-11 · Medium · Carousel rotation is integrated on both sides; horse arcs are not replayable

Evidence: client and server each add `rotationSpeed` to the yaw every tick (`CircusCarouselEntity.java:59-62`,
`:80-81`) from their own ramp (`CircusCarouselRules.java:26-30`), and the server's rotation reaches the client every
2 ticks (`common/init/EntityInit.java:287`), where the base `Entity` applies it at once, so the client's own increment
runs ahead and is snapped back. Strikes use the server yaw (`CircusCarouselEntity.java:151-163`); the horses and
riders are drawn at the client yaw (`CircusCarouselModel.java:151-157`, `CircusCarouselEntity.java:113-125`). The
error is small (a tick of spin, about 3 degrees at the active speed) but it shows as stutter, a reloaded carousel
restarts at idle speed (`:250-254`), and nothing marks the horses' hit volume (1.8 by 2.4 blocks, `:156-157`). Live
multiplayer sync of the arcs is listed as owed in `docs/CIRCUS.md:103`.
Fix: sync the spin as a start tick and an "active since" tick and compute yaw from a pure function of the synced
counter (the `BossReplayClock` pattern) on both sides; draw a ground arc under each horse while it can strike.

### B-12 · Low · Arena bounds are loose and inconsistent; retreating is free

Evidence: acts fail past 24 blocks from the ring centre (`CircusPerformanceController.java:101`); the finale resets only
after 200 ticks with the owner past 64 blocks from the carousel (`CircusCarouselEntity.java:138-140`), twice the
pavilion's 32-block half-width. Ringmaster health does not regenerate between, so walking out to eat and walking back
costs nothing. The troupe is gathered within 20 blocks of the origin (`CircusRingmasterEntity.java:220-222`), so a
performer lured past 20 (it follows its target up to 48 before giving up, `CircusPerformerRules.java:7`, `:26-29`) drops
out of the "all downed" check. The Ringmaster's 12-block return is a silent teleport (`CircusRingmasterEntity.java:192-194`).
Fix: one arena record for the pavilion ring (radius and height), shared by acts and finale, with a short grace and a
line; gather the troupe by finale membership, not by radius; give the return an effect.

### B-13 · Low · The endings have no beat, and Liberation's lethality has no lasting consequence

Evidence: Succession completes on the very hit whose raw amount reaches his health, before armour
(`CircusRingmasterEntity.java:315-319`), and he stays at 1 health for good; no yield, no line from him. Liberation's
surviving performers are reset to full health and go back to performing in the "ruin"
(`:349`), which is six cobwebs in air blocks (`:369-376`). The Champion yields over 60 ticks and passes its title on
screen (`KindledChampionRules.java:16-17`).
Fix: a yield beat for Succession (he kneels, offers the staff or the topper, a short Ringmaster line per
`docs/NPC_VOICES.md`); for Liberation, a collapse and a troupe that scatters, flees or is gone (decision 6).

### B-14 · Low · The bar says nothing for two of three phases

Evidence: the bar is pinned at 1.0 through RAFTERS and CAROUSEL (`CircusRingmasterEntity.java:178`, `:185`) and tracks
health only in DESCENT (`:189`). The frame's notches and phase reaction have nothing to react to before the descent.
Fix: show the CAROUSEL objective on the bar (troupe standing or anchors left) through the frame's phase channel.

### B-15 · Low · Tests cover state and flow, not combat; there is no Circus review client

Evidence: `src/gameTest/.../gametest/CircusRingmasterGameTests.java` has 11 tests: captives, captive sync, the perch,
acclimation-independent position, failed and expired acts, the attention act and route gate, ownership through reload,
saved-data round trip and the absent-owner grace (`:53-334`). The two playthrough finales run under Resistance V and
Strength XXX with a Netherite sword and attack every 12 ticks (`PlaythroughCrimsonTroupeGameTests.java:1113-1232`,
`:1701-1705`, `:1830-1849`); `finaleDeathResets` and `actDeathAndRelog` cover death. Nothing tests knife timing, the
intro lock, the bar's reveal, the cards, a second player, the empty-troupe stall, the Succession `die()` path,
Liberation's phase skip, a reload mid-RAFTERS or mid-DESCENT, or any damage number. The Saints have 8 kit tests, 19 to
20 per fight and a fight review client each; `docs/CIRCUS.md:55` lists no Circus review client.
Fix: a `circus_finale_validation` suite on the Saint pattern (each B-finding above gets a probe), and a
`circusFinaleReviewClient` row in `gradle/review-clients.gradle` that shoots each phase and move at 3-tick stills.

## Performers as combatants

They fight as generic mobs with one hard-coded attack each, on a cooldown, with no wind-up: Acrobat vault-and-hit
every 50 ticks (4), Fire Eater cone every 80 (4 and 3 s fire), Knife Thrower a knife every 70 and a three-knife fan
every fourth volley (4, or 3 each), Stilt Walker a 3.5-block slam every 70 (3), faculty (Strongman, Beast Tamer,
Threadkeeper, Understudy) one shared 4-damage swing every 30 game ticks (`CircusFacultyEntity.java:14-28`). Their
performance cycles (flips, juggling, the stilt spin, the fire breath) are good material, but none of it is used in a
fight: the act moves (`tickPerformance`) and the defence moves (`tickDefense`) are separate code. Downing is solid
(`CircusPerformerRules.shouldEnterDowned`, `CircusDownedPose`, an Enthralled Doll call on the first hit). The faculty
do not join the finale (`CircusFacultyEntity.java:20`), so three of the seven professors (Strongman, Beast Tamer,
Understudy) and the Threadkeeper never fight in any designed encounter.

## Mini-boss ladder (C-13)

What exists to build on: seven professors with distinct acts and bodies (Acrobat, Dagger Thrower, Stilt Walker,
Strongman, Beast Tamer, Fire Breather, Retired Understudy; D3 to D5, `docs/CIRCUS.md:20-28`), five 25x9x25 teaching
camps (air, blades, pageantry, embers, memory) that each hold faculty and a Threadkeeper
(`src/gameTest/.../CrimsonTroupeStructureGameTests.java:31-40`), the practicals that already test a real mechanic per
body (`CrimsonTroupePracticalGameTests.java:77-140`), nonlethal downing and its pose, and the Court's Kindled Champion
as the template for "a member steps out of their place to fight and yields alive"
(`common/entity/boss/court/KindledChampionTrial.java:40-60`, `KindledChampionRules.java:13-21`).

A sketch, one rung per camp, each with one Circus mental mechanic:

| Rung | Degree | Camp | Performer | Signature mechanic | Built from |
|---|---:|---|---|---|---|
| The Flyer | 3 | air | Acrobat | Afterimage decoys from each vault; only the real one is lit by the spotlight; hit the wrong one and it bursts into dolls | `CircusAcrobatEntity` vault, flip state |
| The Board | 3 | blades | Dagger Thrower | Knife Board: knives outline the player's position, then a swap teleports the player to a marked spot; stand still through the fan | `CircusKnifeThrowerEntity` volley, fan |
| The Parade | 4 | pageantry | Stilt Walker with the Mummer | Masks: the Mummer diverts aggro to a masked decoy; the player must keep their own puppet unmasked | Mummer practical (`:103`), stilt spin |
| The Lamp | 4 | embers | Fire Breather | Light and blinding: the breath lights the tent, shadows become safe lanes; a flash punishes looking at it (the inverse attention act) | Fire Eater breath, Cicada flash |
| The Double Act | 4 | pageantry or memory | Strongman and Beast Tamer | Commanded pair: the Tamer's cues send the hound, the Strongman guards; break the cue to stagger both | Hulk guard and Hound rupture practicals |
| The Echo | 5 | memory | Retired Understudy | Replays the player's own last attacks back at them (the Mnemonist rule) | `mnemonistMustReplayThePupilsOwnAttack` (`:130`) |

Then the Ringmaster (B-3) reuses one beat from each rung as his borrowed acts.

What the ladder needs, all of it existing infrastructure:
- A small shared controller for Circus acts on the generic half of the boss kit: `BossMoveTimeline` moves with the
  kit's 12/20-tick floors, `BossReplayClock` for the client, `BossFootprint` shapes, `BossBands` for picking,
  `BossIntroWindow`, `BossBarBinding` with one frame per act, a `BossScenes` card, `canHarmFor` targeting. Not
  `SaintTrialEntity`, whose tomb, ward and `endTrial` are Saint-specific.
- An arena record per camp (the 25x25 footprint), restore on exit, a leash and reset like the Champion's
  `ARENA_RADIUS` and `LEASH`.
- A yield at a threshold, with `CircusDownedPose` and a line; never lethal on Succession; lethal on Liberation only
  if decision 6 says so.
- A record the ladder writes and the Ringmaster reads: either the existing challenge bits (replacing acts 2 to 4,
  B-5) or a new set; puppets counted (B-1).
- A Circus effect and sound set (B-9), a review client row, and a validation suite per rung, as the Court and the
  Saints have.

## Decisions the owner must make

1. **Kit or own controller.** Should the Ringmaster and any Circus mini-bosses move onto the generic half of the boss
   kit (footprints, bands, timelines, intro window), and does that settle G-2c (one boss definition) for them? Proposed:
   yes, with a Circus controller modelled on the Court's, not `SaintTrialEntity`.
2. **Puppets in the fight (B-1).** Should a puppet's damage count as its owner's in the acts and the finale? Proposed:
   yes, and the Succession finale should want one.
3. **Solo or co-op (B-8).** Keep one owner per attempt (the September ruling) or allow a challenger set with scaling,
   as the Saints do? Either way the troupe should stop hitting bystanders.
4. **Build C-13 (D-18).** Build the mini-boss ladder now? If yes, do the rungs replace acts 2 to 4 as Succession's
   challenge bits, and does Liberation need them too? D-18's current answer is "strike until the Circus gets its own
   pass"; this audit is that pass.
5. **How a rider is severed (B-7).** Thread Ripper is only the Liberation reward, so the design's "Thread Ripper severs
   each rider" cannot happen on a first run. Grant Thread Ripper at the pact rejection, sever with held Blood Projection
   or a timed attention beat, or accept right-click and fix the docs.
6. **A killed troupe (B-4, B-13).** After a failed Liberation, do the dead performers come back, does the route repair
   close once any performer has died, and what happens to the survivors after a ruin?
7. **The Ringmaster's palette (B-3).** Confirm the moveset is built from the Circus's mental tools (attention,
   illusion, decoys, displacement, light and sound, commanding the troupe) rather than more knives, and whether he
   gets a music track.
8. **The Succession ending (B-13).** A yield beat like the Champion's (kneel, line, the staff offered), and does he keep
   his perch and trade afterwards?
