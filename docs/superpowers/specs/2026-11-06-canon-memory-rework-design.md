# Canon Memory rework: the four Saint gifts

Date: 2026-11-06. Answers OPEN_QUESTIONS D-1. Status: draft for the owner's review. Nothing here is built, and nothing
gets built until the owner approves it. Every number below is a starting value and my call unless a ruling is quoted.

## 1. Purpose and the ruling

The owner ruled on 2026-11-06 that the four Saint Canon Memories "do in fact need complete reworks": each Saint's gift
is a full rework of its Canon Memory, not a retune. Rulings recorded the same day that bind this design:

- Hemorath's epithet is **The Hollow Vessel**, not "The First Saint" (lang, Liber page, `LORE_REFERENCE.md` and
  `SAINT_MAUSOLEUMS.md` were updated on 2026-11-06).
- Saint memories do not count toward school tier coverage, so a gift's rank is free of the roster tables.
- Animus stays the larger default school.

Standing rulings: the tendency pairs (Hemorath Animus + Ferric, Seraphae Lux + Ductilis, Putriciel Mortem + Flammeus,
Velorum Congeatio + Tenebris); no God or Satan framing; no martyr framing; the Endless Hour is not literal time
manipulation; Putriciel is "they" and their fire keeps the Flammeus look; Velorum's ice is Frozen Cruor; focal effects
are bold at the Hematic Riposte bar, overlays on a body stay subtle; no vanilla particle clouds, no thin strand overlays.

### What exists today (from the code)

| Memory | Registration (`ManipulationInit`) | What it does now |
| --- | --- | --- |
| Crimson Tithe (`CrimsonTitheManip`) | SUMMA, QUICK, Animus / Ferric, HEART, cost 100, cooldown 100 | Deposits 500 into the shared borrowed-blood reserve (`BorrowedBloodReserve`) and starts a 600-tick deadline in `getPersistentData()`. At the deadline the unspent loan is reclaimed; spent blood is collected at twice its value through `BloodAccounting.pay` (`crimson_tithe_debt`), with 6 magic damage if any was spent. Visual: the generic `DEBT` / `TITHE_COLLECT` / `TITHE_RETURN` forms. |
| Unclosing Eye (`UnclosingEyeManip`) | SUMMA, QUICK, Lux / Ductilis, HEAD, cost 350, cooldown 120 | Applies `ILLUMINATED` to every living thing within 32 blocks and to the caster, re-applied every 10 ticks for 600 ticks by `ManipulationReactiveEvents.armUnclosingEye`, plus Night Vision. Anti-stealth with mutual exposure; no damage. Visual: one `ManipulationParticles.accent`. |
| Bloom of Rot (`BloomOfRotManip`) | SUMMA, QUICK, Mortem / Flammeus, BODY, cost 500, cooldown 80 | One burst: 3 damage (times Crimson Mastery) and 200 ticks of `NECROSIS` to everything harmable within 8 blocks; vanilla Poison on the caster for 100 ticks. Visual: the shared `BLOOM` burst. |
| Endless Hour (`EndlessHourManip`) | SUMMA, QUICK, Congeatio / Tenebris, BODY, cost 600, cooldown 200 | For 200 ticks the caster cannot drop below 1 health: damage past that is banked in `getPersistentData()` and repaid as one hit of the `endless_hour_debt` damage type at expiry or logout (waiving armour, effects, absorption and spawn grace through `ServerPlayerSpawnGraceAccessor`). Visual: the shared `HOUR` / `HOUR_BREAK` forms. |

All four are also wired outside their classes: entity casts and Drudge actions for the Eye, the Bloom and the Hour
(`EntityCasts`, `DrudgeActions`), Vesper's coverage rows (`VesperManipulationCoverage`), Loom echo casts
(`MortemEchoCasts`), cast styles (`CastPresentation`), tree positions (`ManipulationTreeInit`), reactive hooks
(`ManipulationReactiveEvents`) and the memory items, recipes and lang.

### What a full rework replaces

None of the four is a piece of its Saint. The Tithe is a loan from a reserve that Hemorath's fight never uses; the Eye
is a radius glow; the Bloom is a single radius burst with a vanilla debuff; the Hour is a death-deferral that reads as
held time. The rework replaces each class body, its cast type, cost, cooldown, numbers, state storage, visuals, sound,
lang, cast style and its non-player uses. Each gift becomes one of the Saint's own moves made the player's, carrying the
Saint's signature meter in small and paying the Saint's own flaw as its price.

## 2. Shared frame

### 2.1 Rank and degree gate

The ranks gate by degree in `ManipulationRankGates` (Humilis D0 to Perfectus D6); there is no D8 rank. Saints are D8
content, and the gifts should be too.

- **Rank:** PERFECTUS for all four. That gives the top ring of the Manipulations tab, Perfectus vein strain (3.0) and
  the hardest Engram weave (scars, valves, prisms and false fragments). Because canon memories do not count toward tier
  coverage, moving them off SUMMA costs nothing in the roster tables (but see open question 13 on Lux).
- **Degree floor:** a per-manipulation floor of 8, set in the registration (`.setRequiredDegree(8)`, new on
  `BloodManipulation`), read by a new `ManipulationRankGates.minDegreeFor(BloodManipulation)` that returns the larger
  of the rank gate and the floor. Every current caller of `minDegreeForRank` that holds a manipulation
  (`KnownManipulationGrantHelper.checkMemoryGrant`, `BloodMemoryItem` and `CrudeMemoryShardItem` tooltips, the
  Manipulations tab and its trace cache) moves to the new method. The comparison itself goes through
  `ProgressionGates` (no literal degree compare).
- Both D8 paths qualify equally. A gift does nothing different for an Apotheos or a Silentium; the paths differ in how
  they reach the Saint (a proper or a forced ending), not in the gift.

### 2.2 How the gift is earned

Three things, all required, matching the lore line that each gift is "rediscovered by weaving the Saint's Hallowed
Residuum with a Recollection recovered from the Gardens' Archive":

1. **The Recollection** (`recollection_<saint>`, from a cyst in the Archive biomes, taken by touch by an Apotheos or a
   possessing Silentium). This is the memory.
2. **The Hallowed Residuum** (`hallowed_residuum_<saint>`, or three fragments from a forced fight). This is the blood.
3. **The player's own ending of that Saint's trial.** New: `SaintTrialEntity.endTrial` records, for every challenger
   present at a rewarded ending, that they ended this Saint's trial (proper or forced). The record lives in an existing
   player progression attachment as four booleans (the build picks the attachment; no `getPersistentData()`). It is
   durable, survives death, and is never inferred for old saves (the mod is unreleased, so dev worlds simply lack it).

Why the third: today a peaceful vial draw gives a Consecrated Syringe and so Residuum without a fight, and both
catalysts are plain items another player can hand over. The gift should be a piece of a power the player has stood
against. The Loom checks the record when the weave starts, and the memory item checks it again when used (a woven
memory handed to someone else teaches them nothing until they too have ended that trial).

### 2.3 The answer to C-6 (recommended)

C-6: Recollections are tradeable, the Loom has no degree gate, and `mausoleum_treasure` still drops
`memory_crimson_tithe` directly; the ledger proposed `never_sold` and a D8 gate on the canon recipes.

| Gap | Recommendation |
| --- | --- |
| No degree gate at the Loom | `MemoryWeavingRecipe` gains an optional `required_degree` field (absent means 0); the four canon recipes set 8. `SomaticLoomBlockEntity` refuses the weave before any enzyme, span or blood is taken, with the reason shown on the Loom. |
| The learn step | The per-manipulation floor (2.1) gates `checkMemoryGrant`, so a memory obtained any other way still refuses below D8. |
| Tradeable Recollections | Add `recollection_*`, `hallowed_residuum_*`, the residuum fragments and the four `memory_<gift>` items to the NPC trade `never_sold` list, so no NPC buys or sells them. Player-to-player handing is not blocked: the trial record (2.2, item 3) makes a handed-over Recollection or memory useless to a player who has not ended that Saint's trial. |
| Direct drop | Remove the `memory_crimson_tithe` entry from `loot_table/chests/mausoleum_treasure.json` (an outer-hall barrel, D3 to D4) and spread its weight over the barrel's other entries (open question 16). |
| Proof | `CanonMemoryRecipeResourceTest` pins `required_degree: 8` and both catalysts; a GameTest refuses the weave and the grant at D7, without the record, and with the record but without the Recollection. |

### 2.4 Cost model

- Every payment is `BloodAccounting.pay` (one-off) or `payUpkeep` (per pulse) with a named `Source` constant per gift
  (listed in each section). None is reserve-protected; all count as spend.
- Scale: a D8 player holds about 5,000 to 9,000 blood (`MaxBloodLedger`). Each gift costs 450 to 700 to open, plus
  upkeep for the channelled two, so a full use is 8 to 15% of a deep pool. Cooldowns are long (300 to 900 ticks): these
  are centrepieces, not rotation filler.
- "Imprinted, not learned" stays: no Dynamic Use cost reduction (`HEMOMANCY_REFERENCE.md` §25.7.2). Crimson Mastery
  scales damage as for every other manipulation.
- Each gift carries a price taken from its Saint's flaw, on top of the blood:

| Gift | The Saint's flaw | The price in play |
| --- | --- | --- |
| Crimson Tithe | He can neither retain blood nor stop taking it | The vessel leaks the whole time, slows and softens the caster as it fills, and bursts on them if overfilled |
| Unclosing Eye | She cannot let anything living remain as it is | The beam grows allies as well as enemies, and the caster is laid bare (the Lapse, then a long exposure) |
| Bloom of Rot | Every cleansing fed him the rot | The caster is colonised by their own field, and if the fire never comes the rot reclaims them |
| Endless Hour | He forced stillness and became the still thing | The caster must stand planted, cannot run, and sets in their own cruor when the Hour closes |

### 2.5 Cast types

| Gift | Type | Why |
| --- | --- | --- |
| Crimson Tithe | CONTINUOUS (held), release on let-go | Appetite is a drink held as long as the caster dares |
| Unclosing Eye | CHARGED, then a 40-tick aimed beam | Her move is a wind-up and a sweep; charge buys range |
| Bloom of Rot | QUICK, with a second press (the Ignition) | Rot, then fire, then rot: two acts with a choice of when |
| Endless Hour | QUICK establish, held by standing planted, with upkeep | His power holds only while he does not move |

Four types, four different hands on the keys. The charge floor and server charge clamp apply to the Eye as to every
CHARGED cast; the Tithe's upkeep runs through the existing channel manager.

### 2.6 How a gift relates to its Saint's fight

Each gift is built from one or two of the Saint's moves (2026-11-05 designs) and one of its meters, shrunk to a player's
scale. Where the Saint's pure rules already compute the thing (Hemorath's leak curve, Seraphae's growth stages,
Putriciel's colonisation thresholds and Kindling, Velorum's stillness fill and drain), the gift's own rules class reuses
the shape with its own constants; it does not call into the boss package, so a fight retune never moves a gift. The
visuals reuse the Saint's own geometry classes and render types (`<Saint>RenderTypes`, the `<Saint>*Geometry` classes
in `client/render/world/`, all package-private in the same package as the new gift geometry), so the gift looks like the
Saint's power and no parallel shader art is written.

| Gift | Built from | Meter in small |
| --- | --- | --- |
| Crimson Tithe | Chained Debt and Tithe Reckoning (the chain), Marrow Siphon (the draw), Rib Burst (the release), iron patches | The absorbed vessel that leaks |
| Unclosing Eye | The Unclosing Eye beam and its Lapse; the Laying On of Hands' heal-then-grow | Growth (Swelling, Knotting, Burst) on what the beam touches |
| Bloom of Rot | Bloom of Rot (the field), the Ignition (the ring), the purging heat | Colonisation on bodies; the field's Kindling |
| Endless Hour | The Still Floor, the Endless Hour, preservation in cruor, the liquid dark, the Bow | Stillness on everything inside the Hour |

**Against its own Saint.** A Saint answers its own gift. Default (open question 11):

| Gift used in its Saint's fight | Answer |
| --- | --- |
| Crimson Tithe on Hemorath | The chain latches, but the blood runs the other way: each pulse feeds 40 into his meter and nothing into the vessel. A legitimate overload route, at the caster's upkeep. |
| Unclosing Eye on Seraphae | Being witnessed holds her: each pulse on her adds 1 integrity (cap 6 a cast, phase III only) and no growth. |
| Bloom of Rot in Putriciel's chamber | Rot phases: the field's Kindling feeds their Kindling one for one. Their interval: the field will not take (the fire is theirs). |
| Endless Hour near Velorum | It is his hour: the caster's planted guard is lost and their own stillness in his meter fills ×1.5 while it holds. A trap the fight teaches, not a tool. |

The other twelve pairings (a gift against another Saint) follow the kit's ordinary classification: a school cast,
admitted to an open window, with that Saint's damage-taken factor.

### 2.7 What stays and what changes

| Stays | Changes |
| --- | --- |
| Manipulation ids `crimson_tithe`, `unclosing_eye`, `bloom_of_rot`, `endless_hour` | Rank SUMMA to PERFECTUS, plus the D8 floor |
| Memory items `memory_<id>`, their names, models and the Loom recipes' ids | Recipe: `required_degree: 8`; enzymes 1 + 1 to 2 + 2; blood 100 to 300 (open question 4) |
| The names Crimson Tithe, Unclosing Eye, Bloom of Rot, Endless Hour | Every class body, cast type, cost, cooldown and number |
| The tendency pairs | Vein sections: the Eye moves HEAD to HEART (her eye is in her chest); the others stay |
| Both catalysts (Residuum, Recollection) | The trial record joins them (2.2) |
| "Imprinted": no Dynamic Use reduction | Lang: descriptions, tooltips and messages rewritten; old message keys deleted |
| `BorrowedBloodReserve` (other systems use it) | The Tithe no longer touches it |
| | Entity casts, Drudge actions and Vesper rows for the four (open question 5) |
| | The `endless_hour_debt` damage type, its settlement handlers and the clone transfer are deleted (frozen `registry-ids.txt` updated) |
| | Every `getPersistentData()` key the four use (`hemomancy:crimson_tithe_*`, `hemomancy:endless_hour_*`) and their allow-list lines |

Old saves: none to migrate (the mod is unreleased); stale persistent keys are simply never read again.

### 2.8 Shared engineering rules

- **State.** Every gift's live state (a vessel, a beam, a field, an Hour, and the per-target growth, colonisation or
  stillness it lays) is session state: UUID-keyed maps in one `CanonGiftState` holder under
  `common/manipulation/saint/`, cleared through `TransientServerStateEvents` on logout, death, dimension change and
  server stop. Nothing new is durable except the trial record. A target's gift-laid values die with the gift that laid
  them, and a target leaving the gift's reach decays them.
- **Targets** through `ManipulationCombatHelper.canHarm(player, target)` (the strict rule), which also carries PvP. Allies
  for the Eye are the complement among living, non-spectator players and owned or tamed bodies.
- **Bosses.** `ManipulationReactiveEvents.isBoss` bodies (the G-2c predicate) get the reduced rows named per gift; no
  gift roots, preserves or pulls a boss.
- **Blocks.** No gift places or breaks a block. Ash, seams, trenches and pools are client geometry over the real floor.
- **Payloads.** No new payload. Each gift drives its look through new `ManipulationVisuals.Form` values appended at the
  end of the enum (`TITHE_CHAIN`, `TITHE_VESSEL`, `TITHE_RELEASE`, `EYE_APERTURE`, `EYE_BEAM`, `EYE_GROWTH`,
  `BLOOM_FIELD`, `BLOOM_IGNITION`, `HOUR_FIELD`, `HOUR_PRESERVE`) carried by `attached` and `burst` with their numbers
  in radius, ticks and count. The old forms `DEBT`, `TITHE_COLLECT`, `TITHE_RETURN`, `HOUR`, `HOUR_BREAK` and `BLOOM`
  are removed once their last users go (Vesper's rows, the Loom echo casts); removing forms shifts ordinals, so that
  step bumps `PacketHandler.PROTOCOL_VERSION`.
- **HUD.** None. Each meter reads from the body and the world (the vessel in the caster's chest, the Kindling in the
  field's cracks, stillness as frost climbing a target). Open question 15.
- **Presentation.** A bespoke `CastStyle` and first- and third-person tracks per gift (`client/player/tracks/`); the
  Outpouring leaves the gift's vascular section; the gifts that wound their own body join `OutpouringRules.woundOnly`
  where noted.
- **Visual bar.** Each gift has its own `final` package-private geometry class with a named timeline and a pure rules
  class with no Minecraft types, hooked in `ManipulationVisualRenderer` before the `AnimusMortemGeometry` branch, drawn
  on the Saint's render types and the school materials (`SchoolRenderTypes`), vertices through `MaterialQuads`, seeds
  through `MaterialQuads.seedTile`, time from `AnimationTime`. White base tint and exactly two accents each. Every landed
  hit also gets the shared `CAST_IMPACT`; the gift's own impact layers over it. Particles are `HemoParticleData`
  accents only. Judge each from a player's eye, from third person and beside Riposte.

## 3. The four gifts

### 3.1 Crimson Tithe (Hemorath, the Hollow Vessel)

**Fantasy.** You chain what is near you and drink it, and like him you cannot hold what you take.

| | |
| --- | --- |
| Type, section | CONTINUOUS, HEART |
| Open | 150 (`crimson_tithe_open`) |
| Upkeep | 10 per 5-tick pulse, 40 a second (`crimson_tithe_draw`) |
| Longest hold | 160 ticks; then it releases itself |
| Reach | 10 blocks, block line of sight, up to 5 chained bodies |
| Cooldown | 400 ticks from the release |
| Vessel | 0 to 1,000, a gift value, not the caster's blood |

**Mechanics.**

1. *Latch.* On the first tick the caster's hand throws a chain at each harmable body in reach, nearest first, up to
   five. A latch is a DIRECT Animus hit for 3 and applies one level of `PRESSURE`. Every 20 ticks the channel tries to
   latch new bodies into free slots; a chain breaks when its body leaves 12 blocks or line of sight for 10 ticks.
2. *The draw.* Each 5-tick pulse, each chained body takes 1.5 (periodic, so it builds no state) and pours into the
   vessel: 25 from a blood-bearing body, 10 from a bloodless one. A blood-active player pays 25 blood instead of the
   health (their own `BloodAccounting.pay` with the source `crimson_tithe_taken`); an inactive player takes the health.
   A boss gives half and its chain never pulls. The caster heals 1 per 50 drawn, at most 10 a hold.
3. *The leak.* The vessel leaks every tick by `1000 × (0.0006 + 0.006 f²)`, where `f` is fill (Hemorath's curve at a
   player's scale): 0.6 a tick nearly empty, 6.6 a tick full. Leaked blood runs off the caster to the floor and is
   gone, but every 150 leaked in a hold sets one **iron** (up to 4): his patches, which grow on a long leaky drink.
4. *Body states.* Hollow (fill under 35%): nothing. Filling: nothing. **Brimming** (over 70%): the caster moves 20%
   slower and takes 20% more damage, as he is slow and soft when full.
5. *Release* (let go, out of blood, or the 160-tick limit): **the Vessel Empties.** Lanes of blood and iron burst out
   from the caster in a star: `6 + iron` lanes (up to 10), each `3 + 7f` long and 1.2 wide, the wedges between them safe.
   Each struck body takes `4 + 12f` once (DIRECT Animus with Ferric secondary, so `LODESTONE`), with a 0.6 knockback
   out along its lane, and Slowness I for 60 ticks while it stands in a flooded lane. Then the vessel is empty.
6. *Overfill.* Holding a full vessel for 20 ticks releases it at full and bursts on the caster: 4 damage and 5% of max
   blood (`crimson_tithe_burst`). He burst; so do you.

No blood comes back to the caster's pool: the vessel cannot be kept (open question 6 offers a refund instead).

**School layer.**

| State | Interaction |
| --- | --- |
| Pressure | The latch applies one level; the release consumes all levels on a struck body for +2 each, the gift's own payoff (one payoff per root attack). |
| Lodestone | The release applies it. A body already marked is pulled 1.5 times harder by its chain each pulse (0.05 a tick toward the caster). |
| Rime | A Ferric release on a body at maximum Rime shatters it (+4, the shared rule). |
| Searing | A Searing body gives 40% less to the vessel (the blood is cauterised, as Searing halves his leak). |
| Necrosis | No change to the draw; banked wounds release as usual. |
| Disrupted | A Disrupted caster's channel breaks and releases at once (the shared interrupt). |
| Obscured | A body Obscured from the caster cannot be latched past 4 blocks. |
| Illuminated | Reveals a concealed body so it can be latched. |

**Visuals.** Against the Riposte anatomy, back to front:

1. *Shell.* A broad translucent Animus ribbon along each chain's catenary (alpha about 0.28), with `WISP` cards trailing
   back toward the target, so the chain sits in a sheath of drawn blood.
2. *Sinew.* The chain itself: opaque hematic-iron links on the Ferric material, each about 0.12 by 0.2 blocks
   (`HemorathIronGeometry` links as on Tithe Reckoning), thick enough to read at 10 blocks. Along it, each pulse sends a
   bolus of blood back to the caster: a fluid stream with a travelling bulge, a dark underside and a highlight (the
   "streams are fluid" ruling), never a constant-width ribbon.
3. *Knots.* A `FOCUS` knot where each chain hooks the body (clot accent at the wound) and one at the caster's palm.
4. *Business end.* The vessel. Inside the caster's chest the blood mass climbs with fill (the inner-blood read of
   `HemorathReliquaryGeometry`, scaled to a player, on `CasterFrame` points); at Brimming the ribs' outline presses and
   short iron spurs push out of the forearm and shoulder, one per iron set. Drips fall from the ribs and hands at the
   leak rate and land as small spreading pools (`HemorathBloodGeometry`) that fade over 60 ticks.
5. *Motion and impact.* The release: Hemorath's Rib Burst made the caster's, as trenches (`HemorathFissureGeometry`,
   0.6 deep rather than his 1.0) that blood floods back into, iron spikes erupting near to far along each lane
   (`HemorathIronGeometry` SPIKE, 3 to 10 ticks), dark brick slabs heaved at the lips, and a Riposte-style ray burst from
   the chest. Timeline: emerge 0 to 3, spikes 3 to 10, flood and linger 10 to 30, the trenches close and dissolve 30
   to 50. Each struck body gets the cast impact plus `HemorathImpactGeometry`.

Colour: white base; accents the Riposte pair, hot core `0xFFD2C4` and clot `0x7A1020`; iron from the Ferric material,
no brass or copper. First person: chains start from `FirstPersonPalm` and drop their nearest 40%; the chest vessel is
not drawn, only the arm's veins and the forearm spurs. Body overlays (vessel, drips) stay at overlay strength; the
chains and the release are bold. Joins `OutpouringRules.woundOnly` (the blood is its own).

**Sound** (from Hemorath's table): chain throw `block.chain.break` 1.0/0.7; each pulse `entity.naeglerophaeon.drain`
0.8/0.7; Brimming `entity.generic.drink` 1.2/0.4; release `rite.escalate` then `entity.generic.explode` 1.0/0.8;
spikes `block.pointed_dripstone.place` 0.8/0.6 per lane; overfill `rite.backlash` 1.2/0.6.

**Code.** Rewrite `CrimsonTitheManip`; new `CrimsonTitheRules` (leak, iron, lanes, damage; pure); `CanonGiftState`
(vessel and chains); `ManipulationReactiveEvents` loses `tickDebt` and the `DEBT` refresh; new
`client/render/world/CrimsonTitheGeometry`; `CastPresentation` keeps `CRIMSON_TITHE` with a new track; Vesper's row and
`MortemEchoCasts.tithe` move to the new forms; lang.

**Risks.** Five chains at 10 blocks is a lot of geometry; cap the drawn links per chain at 24. A Brimming caster in PvP
is an easy target, which is the point, but needs a GameTest with two players. The draw on blood-active players must
never take blood from an Unstained or inactive player (health only). Hemorath answering his own gift is an overload
route that could shorten his fight; the GameTest should time a solo D8 fight with it.

### 3.2 Unclosing Eye (Seraphae)

**Fantasy.** You open her eye in your chest and sweep the room with light that heals the hurt and grows the whole,
friend or not, and while it is open you cannot hide either.

| | |
| --- | --- |
| Type, section | CHARGED, HEART (moved from HEAD) |
| Charge | 20 to 50 ticks (the shared 25% floor applies) |
| Cost | 450 on release (`unclosing_eye_open`) |
| Beam | 40 ticks, range `12 + 12 × charge` (12 to 24), half-width 4°, stopped by blocks |
| Cooldown | 500 ticks from the beam's end |
| Lapse | 50 ticks after the beam; then 200 ticks of the slit |

**Mechanics.**

1. *Charge.* The caster's chest opens over the charge; they move at sneak speed and cannot sprint.
2. *The beam.* On release a beam leaves the chest along the look. The caster sweeps it with their head, turning at most
   5° a tick (her 110° over 40 ticks is the model), still at sneak speed, no item use. Hits test a ray from the chest to
   each body's centre, so a pillar shelters.
3. *On harmable bodies,* each 5-tick pulse: `2 + 1.5 × charge` Lux damage and +1 **growth**. The first pulse on a body is
   a DIRECT hit and applies `ILLUMINATED` (the old anti-stealth survives: concealment is stripped along the sweep, and
   Illuminated breaks Obscured and Veiled by the shared rule). Growth stages, as hers: 3 **Swelling** (Slowness I), 6
   **Knotting** (rooted 20 ticks and a gold nerve flash that applies `DISRUPTED`), 8 **Burst** (8 Lux damage, DIRECT, and
   growth back to 2). Growth falls by 1 every 40 ticks once out of the beam. A boss takes the damage and growth but never
   roots; its Burst is 5.
4. *On allies,* each pulse: a wounded ally heals 1; a whole ally gains growth at half rate (1 every other pulse), and an
   ally's Burst deals 4 and never takes them below 1 health. Her instinct, and her danger, aimed by the player.
5. *The Lapse* (50 ticks). The aperture shrinks to a slit and the caster bows: no Lux casts, 20% more damage taken, and
   the caster is `ILLUMINATED` (visible through any concealment).
6. *The slit* (200 ticks after the Lapse). The eye does not close: anything the caster looks at directly within 32
   blocks (crosshair ray, occluded, checked every 10 ticks) is Illuminated for 60 ticks, and the caster stays
   Illuminated throughout. This keeps the old memory's purpose as a directed scout, at the old price of mutual exposure.

**School layer.**

| State | Interaction |
| --- | --- |
| Illuminated | Applied by the first pulse. The Burst consumes it for ×1.5 (the White Verdict rule), its one payoff. |
| Disrupted | Applied by the Knotting flash: the gold nerve firing interrupts the body's channel or use. |
| Obscured, Veiled | Stripped by the beam (Illuminated wins). |
| Searing | A Searing body sheds 1 growth per pulse instead of gaining (heat closes the light out). |
| Rime | A Rime body's Knotting root lasts 30 ticks. |
| Necrosis | A Necrotic ally is not healed by the beam (the rot refuses the light); their growth still builds. |
| Pressure, Lodestone | No change. |

**Visuals.**

1. *Shell.* At the source, a `LUX_SHELL` glow on the chest (overlay strength). Along the beam an outer translucent sheath
   (alpha about 0.28) that widens with range, with `WISP` cards trailing against the sweep so the beam has body.
2. *Sinew.* The beam's dense core ribbon on the Lux material (Seraphae's `WHITE_BEAM`), and with every pulse a gold
   nerve bead (`FOCUS` card, Ductilis gold) running out along it, so the cadence is visible.
3. *Knots.* The aperture: an `EYE`-shaped opening on the chest with swollen tissue lids (`SeraphaeTissueGeometry`) that
   part over the charge, a bright core inside; it closes to a flickering slit for the Lapse and the slit.
4. *Business end.* Where the beam ends, on a block or body, a white splash with radiating rays. On the floor under the
   sweep the ground is cut open in a shallow furrow (`seraphae_tissue` shape 0, 0.3 deep rather than her 0.75) with
   milky light standing in it, and it swells shut behind the sweep over 20 ticks ("cut open and swells shut too fast").
5. *Motion and impact.* Growth on a struck body follows her presence spec: luminous sores as `FOCUS` cards (overlay
   strength), Knotting adds bone spurs (`SeraphaeBoneGeometry`) at shoulders and knees, a Burst is a white ray burst from
   the chest with a gold crackle along the limbs (`SeraphaeImpactGeometry`). Timeline: open 0 to charge end; sweep 0 to
   40 with a 2-tick flare at each pulse; Lapse fold 0 to 8, flicker to 50.

Colour: white base; accents hot pale core `0xFFF4E8` and nerve gold `0xFFD27A`. Gold is only nerve light (her ruling).
First person: the beam starts below the crosshair; the sheath fades to nothing within 2 blocks of the camera so the view
is never walled off; the aperture is not drawn, only the light in the chest veins at the screen's lower edge.

**Sound** (from Seraphae's table): charge `block.beacon.activate` 0.7 pitch; beam `block.beacon.ambient` 1.0 for its
length; each Burst `block.amethyst_block.resonate` 1.0/1.4; Lapse `block.beacon.deactivate` 0.6.

**Code.** Rewrite `UnclosingEyeManip`; new `UnclosingEyeRules` (sweep rate, pulse, growth stages, ally rules; pure);
the beam tick in `CanonGiftState`; `ManipulationReactiveEvents.armUnclosingEye` and its 10-tick radius loop deleted;
new `UnclosingEyeGeometry`; the `HEAD` to `HEART` change; `EntityCasts::unclosingEye`, `DrudgeActions::unclosingEye`
and the Vesper row removed or replaced (open question 5); lang.

**Risks.** Growing allies can grief in multiplayer; the ally Burst floor at 1 health and the half rate are the guard,
and open question 7 offers heal-only. A head-aimed beam is a held aim channel: it must interact with the casting aim
layer correctly in both views. The beam's raycasts per pulse are cheap (at most a few dozen bodies in 24 blocks).

### 3.3 Bloom of Rot (Putriciel)

**Fantasy.** You lay the rot down around you and let it take everything standing in it, then burn it out with the
purifier's fire, and if you never light it, it takes you.

| | |
| --- | --- |
| Type, section | QUICK, with a second press; BODY |
| Cost | 450 (`bloom_of_rot_sow`); the Ignition is free |
| Field | grows from radius 3 to 8 over 40 ticks; lasts 240 ticks |
| Kindling | 0 to 100, a field value |
| Cooldown | 600 ticks from the field's end |
| Cast lock | 10 ticks kneeling (no movement) |

**Mechanics.**

1. *Sowing.* The caster kneels and lays the palm on the ground; a colony spreads out from it over floor cells (top
   faces within 2 blocks of the caster's feet height, not water, not lava) to radius 8.
2. *The rot,* every 20 ticks: each harmable body on the field takes 1.5 Mortem (the first pulse on a body is DIRECT and
   applies `NECROSIS`) and +1 **colonisation** (+2 if it stood still since the last pulse). Stages, from theirs at a
   smaller scale: 4 **Rooted** (Slowness I), 8 **Shelved** (Necrosis refreshed for as long as it stays at 8 or more), 12
   **Overgrown** (rooted 30 ticks, 5 Mortem, colonisation back to 6). A boss gains colonisation but is never rooted.
3. *The caster is not spared.* Standing on their own field they gain colonisation at half rate (1 per 40 ticks), with
   the same stages (their Overgrown roots 20 ticks for 4 damage). This replaces today's vanilla Poison.
4. *Kindling.* +1 per 10 ticks (the buried will stirring); +1 per 3 damage anything takes on the field; Flammeus damage
   or fire on the field counts ×2.5 (their rule). The cracks in the colony glow cream as it rises; there is no red
   before the fire.
5. *The Ignition.* The second press, once Kindling is 40 or more, or by itself at 100: fire runs out from the caster
   across the field in 12 ticks. Each body it crosses takes `2 + 0.75 × colonisation` Flammeus cleansing damage (DIRECT,
   applies `SEARING`) and its colonisation burns to 0; at 100 Kindling the damage is ×1.25. The caster's own
   colonisation burns off with it (self-cautery). The field chars to ash and ends 30 ticks later.
6. *The rot reclaims.* If the field ends unlit, the caster takes +4 colonisation and 160 ticks of Necrosis. It never
   lasts.

**School layer.**

| State | Interaction |
| --- | --- |
| Necrosis | Applied by the first pulse and held by Shelved. The Ignition releases a body's banked wounds early as its one payoff (the Funeral Bell rule), so a long rot makes a hard fire. |
| Searing | Applied by the Ignition. Flammeus hits from any source on the field feed Kindling ×2.5. |
| Rime | Searing into Rime removes a level instead of igniting (the shared rule); a Rime body gains colonisation at +1 (a still body roots). |
| Pressure | Pressure on a body adds +1 colonisation per pulse (the blood carries the spores). |
| Illuminated, Obscured, Lodestone, Disrupted | No change. |

**Visuals.**

1. *Shell.* At the edge of the growing field, a translucent mould sheet (`putriciel_ground`) with a pale cream rim
   (their Bloom tell), advancing over the 40 ticks.
2. *Sinew.* The colony itself, with mass rather than pattern (their known gap in a 12-block Bloom): `PutricielGrowthGeometry`
   colonies of mixed sizes, one shelf cluster 0.3 to 0.6 blocks tall in about one cell of six, ooze pools, and a few
   spore sacs pushing up near the caster's palm; olive, ochre, cream and mould green; no red.
3. *Knots.* Colonised bodies carry bracket growths at body points by stage (`PutricielColonisationGeometry`, overlay
   strength); the caster's own colonisation climbs from their feet the same way.
4. *Business end.* The Ignition: `PutricielFlameGeometry` tongues with full flame bodies (crimson body, pink-white heat,
   black soot tips, never green or yellow) running out as a ring from the caster, catching each colonised body as a
   column of fire, then the char eating the rot in curled plates (`PutricielGroundGeometry` char) to a packed-ash look
   that lingers 100 ticks. Client geometry only; no block changes.
5. *Motion and impact.* Kindling: heat shimmer above the field and cream cracks brightening with its value. Timeline:
   sow 0 to 10 (kneel, palm down, spores at the palm), spread 10 to 50, pulses every 20; Ignition catch 0 to 12, burn 12
   to 30, char 30 to 60, ash to 160. Each ignited body gets the cast impact and `PutricielImpactGeometry`.

Colour: the rot on Putriciel's materials (white base, the rot palette from the texture); the fire with Flammeus
accents only (crimson and pink-white). First person: the colony at the caster's own feet is drawn lower and thinner
within 1.5 blocks so the view of the floor stays clear.

**Sound** (from Putriciel's table): sowing `rite.ambient.drone` 1.0/0.5; each pulse `entity.mycophant.hurt_other`
1.0/0.6; a stage gained `block.fungus.place` 0.6/0.9 + 0.1 per stage; Kindling past 75
`mausoleum.putriciel.creak` 1.0/0.7; Ignition `mausoleum.putriciel.crackle` 1.4/0.8 then `item.firecharge.use` 1.2/0.6;
reclaim `block.rooted_dirt.place` 1.0/0.5.

**Code.** Rewrite `BloomOfRotManip` (the second press as a recast while the field lives, the cooldown starting at the
field's end); new `BloomOfRotRules` (spread, stages, Kindling, Ignition damage; pure); the field in `CanonGiftState`;
new `BloomOfRotGeometry` over the Putriciel render types; the floor-cell scan shared with the field's server test;
`EntityCasts::bloomOfRot`, `DrudgeActions::bloomOfRot`, Vesper's row and `MortemEchoCasts.bloomOfRot` replaced; lang;
the wiki's School Combat table (it lists the old secondary as Animus).

**Risks.** The recast on a QUICK manipulation is new to the cast pipeline; it needs a clear rule that the second press
is free, never triggers the cooldown twice, and works from the radial and the key. Several Blooms in multiplayer stack
colonisation; cap one live field per caster and gift-laid colonisation from any number of fields at 12. Frame cost:
cap the colony cells drawn at about 200 per field.

### 3.4 Endless Hour (Velorum)

**Fantasy.** You plant yourself and become the still thing: everything near you that stops moving is set in its own
frozen blood, and the dark at the edge makes stopping easy.

| | |
| --- | --- |
| Type, section | QUICK establish, held while planted; BODY |
| Cost | 600 (`endless_hour_plant`) |
| Upkeep | 15 per 20 ticks (`endless_hour_hold`) |
| The Hour | up to 200 ticks; radius 9 round the planted point |
| Cooldown | 900 ticks from the Hour's end |

This is not time manipulation (the lore rule): the world runs at its speed, nothing hangs in the air, and only bodies
slow, as circulation and sense do. Motes inside the Hour slow to a tenth and never stop (ruling D-3 of his fight).

**Mechanics.**

1. *Planting.* The caster plants their feet; the floor's seams fill with frozen cruor out to 9 blocks over 20 ticks. The
   Hour holds while the caster stays within 1.5 blocks of the planted point; stepping out ends it at once with no close.
   The caster cannot sprint but may swing, use items and cast.
2. *The still guard.* While planted the caster takes 40% less damage (his Still Guard at a player's scale).
3. *Stillness on bodies.* Each harmable body inside carries stillness 0 to 100. Fill a tick: +1.2 while moving under
   walking pace (0.13 blocks a tick over the last 10 ticks), +0.6 more while attacking or using an item, +0.4 per `RIME`
   level. Drain: −1.5 a tick while moving faster. A melee mob closing on a planted caster fills; a skeleton standing to
   shoot fills; a mob that keeps running does not. Frost and cruor climb the body as it fills.
4. *Preservation* at 100: the body is set in a shell of its own blood frozen to cruor. Paralysis (the shared effect,
   capped at 60 ticks for mobs and 20 for players), `RIME` to 3, 6 Congeatio damage (DIRECT), stillness back to 30. A
   boss's stillness stops at 90; when it would be preserved it takes the 6 and drops to 30.
5. *The dark.* Between 6 and 9 blocks the rim of the Hour is liquid dark. Bodies in it fill stillness ×1.4 and are
   `OBSCURED` while they stand in it (they lose reliable sight of the caster past 4 blocks). Open question 9.
6. *The close* (200 ticks, unbroken). Every body at 50 or more is preserved; the rest take 4 Congeatio. Then the caster
   sets: 20 ticks in their own cruor shell, Paralysis, taking 70% less damage. The still thing pays by being still.
7. *The Bow.* If the caster takes 30% of their max health inside one Hour, it breaks: no close, and 40 ticks in which
   the caster bows, takes 50% more damage and cannot cast. He bows beneath a wound; so do you.

The old death-deferral is gone entirely. Players who relied on it lose a survival tool; that is intended by the rework.

**School layer.**

| State | Interaction |
| --- | --- |
| Rime | Fills stillness (+0.4 a level); preservation sets Rime to 3, so a Ferric hit on a preserved body shatters it for +4 (the shared rule). This is the gift's designed combo. |
| Obscured | Applied in the dark rim. Umbral Rend consumes it for +4 as usual. |
| Searing | A Searing body drains 0.5 stillness a tick more (warmth); Searing on the caster ends their self-set early. |
| Illuminated | An Illuminated body is not Obscured by the rim (the shared rule). |
| Disrupted | A Disrupted body counts as standing (+1.2) for its 10 ticks. |
| Lodestone | A Lodestone pull or knockback on a body inside counts as travel (it drains stillness), so Ferric pulls and the Hour work against each other. |
| Pressure, Necrosis | No change. |

**Visuals.**

1. *Shell.* At the rim, pools of liquid dark with torn, smoky edges (`VelorumDarkGeometry`), lying on the floor between
   6 and 9 blocks, partly clear so the floor reads through.
2. *Sinew.* The floor's block seams filling with Frozen Cruor ring by ring from the caster's feet (`VelorumCruorGeometry`
   seams, dark oxblood with clots, pale frost on the lips; blue only as frost light), under a partly clear rime glaze
   (`VelorumRimeGeometry`).
3. *Knots.* On each body inside, cruor and frost climbing from the feet by its stillness (overlay strength); at
   preservation, the thick oxblood-glass shell of his fight (`VelorumIceGeometry`) and short cruor blades heaving up
   round it.
4. *Business end.* The close: every seam near a body at 50 or more erupts in leaning cruor blades (the Still Floor's
   slabs, fewer and bigger), then the caster's own set: their shell forms from the feet up over 6 ticks and cracks away
   at the end (`HOUR_PRESERVE`).
5. *Motion and impact.* The slowed motes (`HemoParticleData` frost and glow motes at a tenth speed). A broken Hour: the
   seams crack across from the caster's feet and the glaze shatters outward, the frost falling out of the light (his
   ending's read). Timeline: plant 0 to 20 (the seams spread), hold to 200, close 0 to 12, set 12 to 32.

Colour: white base; accents frost pale `0xE8F0F4` and cruor dark `0x4A0E14`. Red stays dark, never arterial. First
person: the rim and seams are floor geometry and never rise into the view; the caster's own shell at the set is drawn
only on the arms in first person.

**Sound** (from Velorum's tables): plant `block.conduit.deactivate` 0.5; seams welling `block.honey_block.slide`
0.4/0.4; then silence from the caster; a body preserved `entity.player.hurt_freeze` 0.7 then `block.glass.place` 0.5;
a shatter `block.glass.break` 0.9; the close `block.amethyst_block.chime` 0.6; the Bow `rite.subject_thud` 0.6.

**Code.** Rewrite `EndlessHourManip`; new `EndlessHourRules` (fill, drain, preservation, guard, close, Bow; pure);
the Hour and per-body stillness in `CanonGiftState`; delete `deferDamage`, `settleDebt`, `waiveMitigation`,
`transferDebt`, `isSettlement` and their calls in `ManipulationReactiveEvents`, the `endless_hour_debt` damage type and
`HemoDamageTypes.endlessHourDebt`, the `buffer` special case at line 272, and `ServerPlayerSpawnGraceAccessor` if nothing
else uses it; frozen `registry-ids.txt`; the persistent-data allow-list lines; new `EndlessHourGeometry` on the Velorum
render types; `DrudgeActions::endlessHour` and Vesper's row replaced; lang.

**Risks.** Paralysis on mobs must respect every mob kind's AI (Brain mobs included), and the 60-tick cap must hold even
with stacked preservations. Lethe Silver Ward, Still Waters and the Sheolic Bastion gained exemptions for the old
settlement; those exemptions go with it and their tests change. A planted caster in a narrow corridor makes almost
every mob preservable; the 900-tick cooldown and the close's self-set are the brakes, and the numbers need play.

## 4. Open questions for the owner

1. **Rank.** PERFECTUS with a per-manipulation D8 floor (default), or a new `CANON` rank at D8 (touches the tab rings,
   vein strain, Engram weave tiers and every switch on rank)?
2. **The trial record.** Must the player have ended that Saint's trial themselves to weave and learn its gift? Default
   yes; without it, the Recollection and Residuum alone suffice, and a peaceful draw plus a traded Recollection gives
   the gift without the fight.
3. **Forced endings.** Does an Apotheos who forced the door and won earn the record? Default yes: the paths are equal.
4. **Recipe weight.** Enzymes 1 + 1 to 2 + 2 and blood 100 to 300 (default), or leave the recipe as is and let
   PERFECTUS's harder weave carry it?
5. **Non-player casters.** Default: the four gifts are player-only. Remove the Eye, Bloom and Hour entity casts and
   Drudge actions (`DRUDGE_UNSUPPORTED`), and mark the four `excluded` in `VesperManipulationCoverage` ("a Saint's gift").
   Alternative: keep simple variants for Vesper.
6. **Tithe refund.** Default none: the caster heals a little and keeps no blood. Alternative: return a quarter of the
   vessel at release, capped at the blood paid that hold.
7. **The Eye on allies.** Default: it heals the hurt and grows the whole, with the ally Burst floored at 1 health.
   Alternative: heal-only on allies (safer in co-op, less of her).
8. **Bloom's price.** Default: the caster is colonised by their own field and reclaimed if it goes unlit. Alternative:
   the caster is spared and only the reclaim remains.
9. **The Hour's dark.** Velorum himself never applies `OBSCURED` (his revision ruling). For the player's gift the dark
   rim applies it (default), because it is the Tenebris school state and plays with Umbral Rend. Alternative: the rim
   only raises stillness.
10. **The Hour's self-set.** Default: 20 ticks of Paralysis in one's own cruor at the close, with 70% damage reduction.
    Keep, shorten, or drop?
11. **Saints answer their own gifts** (table in 2.6). Default yes, built after the gifts work in the open world.
12. **PvP.** Default: the gifts hit players wherever `canHarm` allows; the Tithe draws a blood-active player's blood.
13. **Lux at Summa.** The Unclosing Eye is Lux's only SUMMA entry. Under the ruling it never counted, so Lux already has
    no ordinary Summa memory; moving the Eye to PERFECTUS makes that visible in the tab. Default: flag it for the next
    balance pass, not this one.
14. **Tree placement.** `ManipulationTreeInit` draws White Verdict, Absolute Stillness, Carrion Communion and Fruiting
    Contagion as children of the canon memories (display edges only). A D8 node above D5 and D6 nodes reads wrong.
    Default: put the four gifts on their own outer nodes at the end of each school's branch and re-parent those four to
    their ordinary neighbours.
15. **HUD.** Default no meter; the vessel, Kindling and stillness read on the body and floor. Alternative: one small
    gift meter layer in `GuiLayerRegistrar`.
16. **`mausoleum_treasure`.** Default: drop the `memory_crimson_tithe` entry and spread its weight over the barrel's
    other entries. Alternative: replace it with a Residuum fragment of the complex's Saint.
17. **Player text.** Tooltips name the source as "Canon Memory of Hemorath, the Hollow Vessel" (and of Seraphae, of
    Putriciel, of Velorum). Messages are narration in the lore's register, a few each, written at build time and
    checked against `NPC_VOICES.md` only if a Saint speaks (default: none speaks).

## 5. Build order

One builder at a time; no parallel Gradle runs (parallel sessions share build directories).

1. **The gate and C-6** (small, testable alone): `setRequiredDegree`, `minDegreeFor`, callers moved; the Loom's
   `required_degree`; the trial record written in `endTrial` and read by the Loom and the grant; `never_sold`; the
   treasure entry. Tests: `CanonMemoryRecipeResourceTest`, a `canon_gift_validation` GameTest batch for the refusals.
2. **Shared plumbing:** `CanonGiftState` and its `TransientServerStateEvents` registration; the new forms; the removal
   of the old persistent keys and their allow-list lines; four `CastStyle` tracks as placeholders.
3. **Crimson Tithe** first: it reuses Hemorath's visuals, the only Saint fight the owner has played. Rules and JVM
   tests, the manipulation, GameTests (latch, draw, leak, Brimming, release, overfill, PvP with two players, inactive
   players pay health), then the geometry and a `canonGiftReviewClient` row in `gradle/review-clients.gradle` with one
   scene per beat.
4. **Bloom of Rot**, then **Endless Hour** (the largest deletion: the debt settlement and its damage type), then the
   **Unclosing Eye** (the held aim channel is the newest piece of casting work). Same steps each.
5. **Saints answering their gifts** (2.6), one GameTest per Saint in its fight batch.
6. **Remove the old forms** with the protocol bump, once Vesper's rows and the echo casts no longer use them.
7. **Docs** with the build, not before: `HEMOMANCY_REFERENCE.md` §8.2 and §25.7.2, `LORE_REFERENCE.md` §10 (the
   gift rows and §10.8), `SAINT_MAUSOLEUMS.md`, `MANIPULATIONS_AND_CASTING.md`, `wiki/School-Combat.md` and
   `wiki/Saints-and-Encounters.md`, `ARCHITECTURE_STANDARDS.md` if a guardrail row is added, and D-1 and C-6 moved to
   "Rulings that bind" in `OPEN_QUESTIONS.md` and the topic doc.

Each gift's visuals need a native client launch to prove its shaders and render types draw (offline validators miss
driver errors), and a look beside Riposte from a player's eye before it is called done.
