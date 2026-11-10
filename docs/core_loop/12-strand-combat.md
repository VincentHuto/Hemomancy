# Track 12: Strand combat

Status: **review draft, 2026-11-09. Nothing approved, no product code written.** The lash half of this track should be
proved by the throwaway spike in [12a-lash-spike.md](12a-lash-spike.md) before the rest is designed in detail.

**Goal:** make held Projection a weapon the player swings. While a combat manipulation is selected, Projection puts out
a **strand** of blood, the manipulation decides the strand's **form**, and the damage comes from how the player moves
it. Give Absorption a matching job: it reads what Projection left on the target. After this track the two verbs fight
as well as work, and R is the button for bursts.

**Depends on:** Track 1 (Shapings, `VerbTargeting`, `VerbReach`, `tryPerformVerbPulse`). Track 4 for Temper per crack
and Track 6 for stains under a Plant are optional ties; the track works without them.

Line numbers and counts come from two read-only audits on 2026-11-09. Re-check before relying on one.

## What exists

- **Projection deals no damage.** `BloodProjectionItem.performProjection` (83 to 148) never calls `hurt(`. Its only
  entity branches are the recruit player (`EarlyInitiation`), Saints and their companions (`SaintChannelRouter`) and
  faltering Wills. Aimed at a mob or at open air it does nothing and spends nothing. That empty space is where a
  strand goes.
- **Projection's reach is a fixed 5.5** (`SanguineProjectionTargeting.PROJECTION_REACH`), block-only pick.
- **Projection is slow to carry.** Nothing cancels vanilla's use-item slowdown for it, so the player moves at 0.2
  speed while projecting. Absorption has its own handling (`ClientInputEvents.onBloodAbsorptionMovementInput`,
  `BloodAbsorptionItem.applyChannelMovementPenalty`).
- **Absorption already damages.** Bare: 1.0 per pulse, nearest living thing in a 5.0 box, no aim. Staff:
  `4 + 0.75 × draw + 0.25 × focus`. It reads eleven skills (cadence, dragging, mobile conduit, unbound, tolerance,
  mercy, shared, guarded, selective, sated, distributed).
- **Absorption's damage has no owner.** `BloodAbsorptionItem` line 258 uses `user.damageSources().generic()`: no
  attacker, so no kill credit, no aggro, no school, no cast impact.
- **The combat skills do not reach the verbs.** `crimson_mastery` (damage) and `sanguine_reach` (range) are read by R
  casts only. Projection's three skills (`crimson_projection`, `hematic_focus`, `vespers_refusal`) raise fill rate on
  the Staff and nothing else.
- **Searing Lash is a canned burst.** `SearingLashManip` (Flammeus Mediocritas, QUICK, 225 mL): one
  `aimedTarget(player, level, 12 × reach, 0.94)`, 4 damage × mastery, 120 ticks of Searing, 3 seconds of fire, and a
  16-tick `Form.SEARING_LASH` burst drawn by `SearingLashGeometry` from the packet's `from` and `to`. The player
  presses a key and watches a whip animation. Nothing about it is swung.
- **School damage is one pipe.** `ManipulationCombatHelper.hurt(manip, player, target, level, amount, duration,
  levels)` inside a `SchoolDamage.cast` scope checks `canHarm`, attributes the hit, applies the school's state once
  per root attack (`SchoolCombatEvents.commit`), and fires a `CAST_IMPACT` visual (`CastImpacts.landed`, throttled to
  one per target per 5 ticks). A strand hit that goes through it gets all of that for free.
- **School states exist for all eight schools:** Pressure (Animus), Necrosis (Mortem), Lodestone (Ferric), Searing
  (Flammeus), Rime (Congeatio), Disrupted (Ductilis), Illuminated (Lux), Obscured (Tenebris). Several manipulations
  already pay off a state (Vitric Combustion reads Searing; Umbral Rend reads Obscured).
- **A followed visual exists.** `ManipulationVisuals.attached(entity, form, ...)` makes a keyed cue that follows the
  entity each frame and is kept alive by a re-send (the cell hand does this every 10 ticks). The `_CHARGE` forms
  already recompute their end point every frame from the caster's look. `BloodBindingTendrilClientState` plus
  `BloodBindingTendrilRenderer` is the closest thing to a persistent strand: client state keyed by two entity ids,
  ticked, lerped by partial tick, drawn through `SanguineTendrilRibbonRenderer`.
- **Vanilla hurt timing.** A living entity refuses a repeat hit of the same size for 10 ticks after the last one. Any
  per-target hit rate faster than that is thrown away unless the code resets `invulnerableTime`.
- **No attack key is read during a channel.** The only `keyAttack` readers are the baghnakh auto-attack (polls
  `isDown()` in `ClientInputEvents`) and the Staff planting handler. No serverbound payload carries a secondary action
  during use.

## Target design

### 1. The strand

A strand is a held projection Shaping with a combat form. It is not a new item or a new key.

- **It opens when** the player holds Projection with a strand manipulation selected and the first tick's target is
  open air, a creature, or a plain block. Rites, Saints and Wills still claim first. A block that takes blood (a
  machine, an anchor, a brazier) is not claimed, so projecting into a station with Searing Lash selected still fills
  the station.
- **It holds until release.** Track 1 lets a Shaping claim tick by tick. A strand claims once, at `begin`, and keeps
  the channel until the button comes up or the blood runs out. Otherwise a lash would flicker off every time the aim
  swept across a basin. This is the one place the strand departs from Track 1's per-tick claim.
- **It lives in one session object,** `StrandSession`, held by Track 1's `ShapingSessions`: node positions for this
  tick and the last, per-target lockouts, and the form's own counters. Transient, cleared through
  `TransientServerStateEvents`.
- **Length** is `VerbReach.of(player, PROJECT)` scaled by the form: Lash +2 blocks, Jet ×2, Spray ×0.6, Plant and
  Brand ×1. Starting numbers. A lash of about 7.5 blocks at base outreaches a sword and stays well short of the R
  cast's 12.
- **It never stores blood.** A strand is blood in motion from the body. Release it and it is gone; what touched the
  ground is a stain (Track 6) or nothing.

### 2. The five forms

| Form | What the player does | What it rewards | Rule class |
| --- | --- | --- | --- |
| **Lash** | Sweeps the aim side to side | Stroke speed and clean reversals | `LashRules` |
| **Jet** | Holds the aim on one target | Steadiness; pressure builds, then pierces | `JetRules` |
| **Spray** | Stands close and fans it | Position; low damage, heavy school state | `SprayRules` |
| **Plant** | Projects into the ground under them | Timing; a delayed eruption sized by blood sunk | `PlantRules` |
| **Brand** | Holds it on a creature until it takes | Commitment; marks the target for Absorption | `BrandRules` |

One manipulation has one form. The form is data on the registration (`Strands.lash(spec)`), so a new strand
manipulation is a `ManipulationInit` line and a `StrandSpec`, not a class of logic.

### 3. Lash

- **The tip lags.** The server keeps a short chain of nodes from the hand outward. Each tick every node moves toward
  its place on the straight line from hand to full length along the look vector. Nodes near the hand follow almost at
  once; the tip follows slowly. A distance clamp keeps the chain from stretching. This is a first-order lag chain: no
  spring, so it cannot blow up, and it is the same arithmetic on server and client.
- **Blocks clip it.** A node that would pass through a block stops at the face. A lash around a corner is shorter.
- **Hits come from sweeps.** Each tick the outer half of the chain is tested as swept segments (where each node was,
  where it is) against nearby creatures. A creature is hit when a swept segment crosses its box.
- **Damage scales with tip speed** between a floor and a cap. Below the floor the lash is slack and does not hurt. At
  the cap more speed adds nothing, so a high mouse sensitivity buys nothing.
- **One hit per creature per 10 ticks,** tracked in the session. That matches vanilla's own hurt timing, so no hit is
  silently thrown away and nothing needs `invulnerableTime` reset.
- **A stroke tires.** A sweep keeps full damage for its first quarter turn. Past that the damage falls to the floor
  until the stroke reverses or the lash comes to rest. Back and forth is strong; spinning in place is weak. This is
  the answer to spin-to-win and it is one counter in `LashRules`.
- **The crack.** A sharp reversal at speed snaps the tip: one hit in a small radius at the tip for more damage, and
  the school's state at long duration. Ordinary sweeps do not refresh the state. The crack is where Searing, Rime or
  Obscured comes from.
- **Starting numbers,** all for the spike to tune: 3 damage at the floor, 6 at the cap, crack ×1.5, crack once per
  20 ticks, × `crimson_mastery`. Against one target that is under a same-tier sword. Across three it is over.

### 4. The snap (`CL-22`)

Left click while a strand is out forces the form's accent on demand: the lash cracks straight along the aim, the jet
spends its pressure in one pulse, the plant erupts early, the brand sets at once at a higher price.

- **Why:** a lash that only cracks on a mouse reversal asks for wrist work some players cannot or will not do. The
  snap gives the same payoff from a click. Reversal stays as the free version; the snap costs extra blood.
- **Why this is not the two-button scheme Track 1 rejected:** the left button is read only while a strand session is
  open. With no strand out it is attack and block breaking as always.
- **How:** from memory, vanilla drains attack clicks and skips block breaking while an item is in use, which leaves
  the button free. The spike must confirm that. The client polls `keyAttack.isDown()` for an edge (the baghnakh
  handler already polls it) and sends one empty `StrandSnapPacket`. The server checks an open session through
  `ServerboundGuards`, paces it with `ServerboundRateLimiter`, and calls `StrandService.snap(player)`.
- This is the track's only new payload.

### 5. Jet, Spray, Plant

- **Jet.** A straight strand to the first creature or block on the aim. While the aim stays on the same creature a
  pressure counter climbs over about two seconds; damage per pulse climbs with it. At full pressure the jet passes
  through to the next creature behind. Looking away bleeds the pressure off quickly. Pulses every 10 ticks. The
  client side is nearly built: the `_CHARGE` cues already re-aim a beam from the caster's look every frame.
- **Spray.** A short cone. Every 10 ticks, every harmable creature in the cone takes a small hit and the school's
  state at long duration. It is the form for setting up a payoff on a crowd. There is no shared cone gather today
  (each manipulation filters with its own dot product); add one to `ManipulationCombatHelper` and use it here.
- **Plant.** Aim at the ground and hold: blood sinks into the block and a telegraph ring grows with the amount sunk,
  up to a cap. Release, snap, or reach the cap and it erupts after a short beat, with the school's shape (a frost
  line, a bone bloom, iron spikes, a fire column). Damage and radius scale with the blood sunk. With Track 6, a Plant
  into a wet stain erupts along the stain. Living Land (Track 9) is the Animus Plant; build it on this session shape.
- All three ride `tryPerformVerbPulse`, pay per pulse, and hit through `ManipulationCombatHelper.hurt`.

### 6. Brand, and Absorption's half (`CL-26`)

- **Brand.** Hold the strand on one creature for about a second. It takes: the creature gets the school's state at
  long duration and a brand that names the caster. Track 2's Blood Binding conversion is already this shape; it
  becomes the Animus Brand instead of a one-off.
- **Absorption reads the state, not the manipulation.** One table, `AbsorptionRiders`, keyed by `SchoolState`. When
  the player draws from a creature that carries a state, the draw gains that school's rider:

  | State | Rider on the draw (starting ideas) |
  | --- | --- |
  | Pressure (Animus) | Pulls the target toward the caster; more blood back |
  | Searing (Flammeus) | Faster, harder draw; less blood back (it boils off) |
  | Rime (Congeatio) | Deepens the slow; holds the target still at full draw |
  | Lodestone (Ferric) | Strips armour while the draw holds |
  | Necrosis (Mortem) | A kill by draw spreads Necrosis to what stands near |
  | Disrupted (Ductilis) | The draw arcs to the nearest other marked creature |
  | Illuminated (Lux) | Draw reach up by half |
  | Obscured (Tenebris) | The target loses its target; the caster is Veiled |

- **A branded target** is valid for Absorption at twice the reach and is picked before anything nearer. So the loop
  is: brand at range, draw from safety, or close in with a lash while the state holds.
- **Why a state table and not a per-manipulation absorption hook:** the states already exist for all eight schools,
  every strand form applies them, and existing payoff manipulations keep working. Absorption specialises by what the
  player set up, which is the link between the two verbs this track is for. A manipulation can still carry its own
  absorption Shaping where Track 1 wants one.

### 7. Absorption repairs this needs

These are changes to plain Absorption, so they are owner decisions (`CL-24`), not silent fixes.

- **Give the damage an owner.** Replace `generic()` with a `hemomancy:blood_absorption` damage type that names the
  player. Mobs then turn on the one draining them, kills are credited, and school riders can route through
  `SchoolDamage`. Recommended: a kill by draw does not also pay the kill bounty, or draining pays twice.
- **Aim.** Bare absorption takes the nearest living thing in a box. Track 1's `VerbTargeting` already replaces that;
  Brand makes it matter.
- **`crimson_mastery`** scales strand and draw damage. `sanguine_reach` is already in Track 1's `VerbReach`.
- Four faults found in this audit (cadence skills against hurt timing, strain that never builds, and two payment
  slips in Will banishment and the Condenser) are logged in Track 0 under "The verb items". Fix them there before
  balancing anything here.

### 8. Cost

- **Blood per second while the strand is out,** through `tryPerformVerbPulse` and `BloodAccounting.payUpkeep`,
  `Source` `SHAPING`. Starting point for a Mediocritas lash: 60 mL per second, so a full body is about 80 seconds of
  lash. The R cast is 225 mL for one hit.
- **Extra blood per snap.** A reversal crack is free beyond the upkeep.
- **Temper per crack, eruption or brand** (Track 4). Dry is never a refusal: the accent is weaker and the state
  shorter.
- **Movement** (`CL-25`): a strand declares its own multiplier and the existing `MovementInputUpdateEvent` handler
  applies it. Starting points: Spray 1.0, Lash 0.8, Plant and Brand 0.6, Jet 0.5. Projection's 0.2 today is
  unusable for a weapon.

### 9. Authority and drawing

- **The server decides every hit** from its own record of the player's position and look. No client ever claims a
  hit. Targets pass `ManipulationCombatHelper.canHarm`.
- **No per-tick packets.** The server sends one keyed `ManipulationVisuals.attached` cue when the strand opens and
  refreshes it every 10 ticks, as the cell hand does. Each client runs the same rule class from the caster's synced
  look and draws its own copy. The caster's own client uses its exact look, so the strand feels immediate.
- **Accents are server events.** A crack, a pierce, an eruption and a set brand are each a `burst` at the server's
  point, plus the automatic `CAST_IMPACT` on every landed hit. A player learns to trust the impact, not the ribbon,
  which is what makes small disagreements between client and server copies harmless.
- **Geometry is the mod's own.** `SearingLashGeometry` drawn from a live point list; `ThermalGeometry.flameRibbon`,
  `LuxUmbraGeometry.ribbonPath`, `SanguineTendrilRibbonRenderer` and the tendril tube brushes per school. Bold, at the
  Riposte bar: a strand is a focal effect, not an overlay. Render types cached; no vanilla particles.
- **Cast impacts** choose their delivery from `CastPresentation`. Searing Lash is filed as a projectile there. Strand
  hits want a sweep delivery (new) or the existing tether one.
- **The arm.** Third person copies the vanilla bow pose today (`CellHandLayer`), and `PlayerAnimationRegistry` has no
  projection entry. A lash needs an arm that follows the stroke: one new entry in `THIRD_PERSON` at its own priority,
  and the first-person hand in `CellHandItemRenderer`. `CastingAnimationManager` must treat an open strand as a
  channel or its cue drops after a tick (Track 1 notes the same for all Shapings).

### 10. Which manipulations convert

First wave, one per form and five schools, so the forms can be judged against each other:

| Manipulation | School, rank | Form | Why this one |
| --- | --- | --- | --- |
| `searing_lash` | Flammeus Me | Lash | It is already a whip in name and art |
| `hematic_flare` | Lux Hu | Jet | A single aimed ray today |
| `blood_eclipse` | Tenebris Me | Spray | A 60° cone today |
| `glacial_grasp` | Congeatio Hu | Plant | A ground route today |
| `hemorrhage` | Mortem Hu | Brand | Applies `blood_loss` to one target today |

Second wave, once the forms are tuned:

| School | Candidates |
| --- | --- |
| Animus | Brand: the Blood Binding family (Track 2). Plant: Living Land (Track 9). Lash: new (`CL-23`) |
| Flammeus | Plant: `crimson_flame_conjuration` |
| Ductilis | Brand: `conductive_mark`. Spray: `synaptic_jolt` |
| Lux | Spray: `prismatic_reproof` |
| Mortem | Brand: `grave_debt`, `insatiable_hunger` |
| Congeatio | Jet: new, or leave `cruor_javelin` on R |
| Ferric | Spray: `needle_fan`. Jet: `needle_lance`. Plant: `ferric_spikes` |
| Tenebris | Lash: `gloam_laceration` |

**Stay on R:** thrown things (Blood Shot, Guided Shot, Mortar, Needle), bursts and payoffs (Aneurysm, Vitric
Combustion, Exsanguinate, Umbral Rend, Fruiting Contagion), every Perfectus, self buffs and auras, Hematic Riposte,
constructs, and the five existing CONTINUOUS channels. R is the burst button; a strand is the sustained weapon.

**A converted manipulation keeps its R cast** (`CL-21`). Pressing R still does the canned strike at its old price:
a quick flick. Holding Projection puts out the strand. Reasons: Vesper, Wills and the thermal echo casts go on using
the canned version with no change; `searingLashLaysALongSearing`, `BalancePassVisualSourceTest`,
`VesperManipulationCoverageTest` and the synergy chain (`ferric_spikes` into `searing_lash`) stay green; and a player
in a weapon form still has the flick. Whether the flick should retire can be decided after playing both.

### 11. Projection skills

A combat line beside the absorption skills in `LivingStaffSkillBranch`, read by the hand tool and the Staff alike, as
the absorption skills are. Working names:

| Skill | Effect |
| --- | --- |
| Long Strand (3) | Strand length |
| Taut Line (2) | Less tip lag on a lash; faster pressure on a jet |
| Whipcrack (3) | Accent damage and state duration |
| Free Hand (3) | Movement while a strand is out |
| Deep Brand (2) | Brand sets faster and lasts longer |
| Second Strand (1, late) | A strand from the off hand (`CL-27`; recommended deferred) |

The existing fill-rate skills keep their job. On the Staff in staff form the strand uses the Staff's reach; weapon
forms follow Track 8's rule (base reach, none for the crossbow).

### 12. Non-players

- Vesper, Wills and Drudges never run a strand. They use the manipulation's entity cast or Drudge action, which is
  the canned strike.
- Any new manipulation (a new Animus lash) needs a `VesperManipulationCoverage` row, as a variant or as excluded
  with a reason; the coverage test requires every registry id.
- A strand hit opens a `SchoolDamage.cast` scope, and any player cast scope stops an active Marionette channel. That
  is the existing rule and is probably right here too. Say it in the Liber.

## What retires

Nothing. This track adds a second way to use some manipulations and repairs Absorption's damage path. If `CL-21` is
later ruled the other way, the retired R casts move to `removed_assets/` then.

## What changes, file by file

| File | Change |
| --- | --- |
| New `common/manipulation/verb/strand/` | `StrandForm`, `StrandSpec`, `StrandShaping`, `StrandSession`, `Strands` (factories), `StrandHits`, `StrandService`; pure `LashRules`, `JetRules`, `SprayRules`, `PlantRules`, `BrandRules`. |
| `ManipulationInit` | `.setProjectionShaping(Strands.lash(...))` and so on for the first wave. |
| `ManipulationCombatHelper` | One cone gather and one swept-segment gather; both take `canHarm` in their arguments. |
| `BloodProjectionItem` | Nothing beyond Track 1's dispatcher call. |
| `BloodAbsorptionItem`, `BloodAbsorptionTargetRules` | Owned damage type, branded-target priority and reach, rider call. |
| New `AbsorptionRiders` (beside `SchoolStates`) | The state table. |
| New data `damage_type/blood_absorption.json`, tags | The owned damage type. |
| `SkillPointInit`, `LivingStaffSkillBranch`, `SkillPointHelper` | The projection combat line. |
| New `common/network/manips/StrandSnapPacket`; `PacketHandler`, `payload-registry.txt` | The snap; protocol bump. |
| `ClientInputEvents` | Movement multiplier for a strand; attack-key edge during a strand. |
| `ManipulationVisuals.Form` | Appended strand forms and accents. |
| New `client/data/StrandClientState`, `client/render/world/StrandRenderer` and per-school geometry | The drawn strand, called from `ManipulationVisualRenderer`. |
| `SearingLashGeometry` | Split into point building and `drawPath(points)`. |
| `CastPresentation`, `CastImpactRules` | A sweep delivery. |
| `PlayerAnimationRegistry`, `CellHandItemRenderer`, `CellHandLayer`, `CastingAnimationManager` | The arm. |
| `GuiLayerRegistrar` (Track 1's channel layer) | Pressure, sunk blood, brand progress, as one thin arc. |

## Tests

- **New, pure:** `LashRulesTest` (lag chain converges; length clamp; floor and cap; stroke tiring; reversal detection;
  a constant spin never exceeds the floor after a quarter turn), `JetRulesTest`, `SprayRulesTest`, `PlantRulesTest`,
  `BrandRulesTest`, `AbsorptionRidersTest`.
- **New, GameTest:** a `strand_validation` batch with a real player. A scripted sweep hits once per 10 ticks; a still
  lash deals nothing; a strand loses to a rite anchor, a Saint and a Will; a station still fills with a strand
  manipulation selected; a snap with no session is refused; a branded target is drawn from at range; a kill by draw
  is credited.
- **Guardrails:** `ShapingGuardrailTest` (no manipulation names in the dispatcher or in `strand/`),
  `CombatTargetingGuardrailTest` (the two new gathers), `NetworkPackageGuardrailTest` and
  `ServerboundGuardsCoverageTest` (the snap), `PayloadProtocolVersionTest`, `RenderTypeCachingGuardrailTest`,
  `VanillaParticleRatchetTest`, `PlayerAnimationGuardrailTest`, `EntityCastGuardrailTest`.
- **Must stay green untouched:** `searingLashLaysALongSearing`, `BalancePassVisualSourceTest`,
  `VesperManipulationCoverageTest`, the entity-cast fingerprints.
- **Rewrite:** `BloodAbsorptionItemSourceTest` and the absorption rules tests for the owned damage type.

## Hazards

- **Feel.** This is the whole risk. A lagging tip can read as a whip or as a broken laser. Motion damage can feel
  earned or feel like wrist strain. No document settles that; the spike does.
- **Spin-to-win.** Stroke tiring and the speed cap are the answer. If the spike shows players still spin, drop speed
  scaling and pay only on reversal.
- **Latency.** Hits are judged against server positions. At high ping the caster's ribbon will pass through something
  the server says it missed. A generous strand radius and server-only impacts soften it. Do not add client hit claims
  to fix it.
- **Observers.** Other clients rebuild the strand from the caster's synced look, which arrives at tracker rate. It
  will be smoother than exact. The spike does not test this; the track's first build must, with two clients.
- **Server cost.** A lash is a few dozen segment tests per tick per lashing player. Cheap, but measure it with ten.
- **PvP.** Every strand hit is a new targeting site. `G-2b` / `F-16` (moving sites to `Reach.STRICT`) should be
  ruled first; strands use the strict rule from the start.
- **Friendly fire by sweep.** A lash crosses allies, pets and villagers on its way. `canHarm` refuses them, but check
  that a refused creature does not eat the hit meant for the one behind it.
- **Bosses.** Saints and bosses with their own damage rules must see strand hits as ordinary school damage. Add one
  boss to the GameTest batch.
- **Absorption's new owner.** Mobs that never noticed a drain will now fight back. That changes how early Harbingers
  feed. It is right, and it is a change; teach it.
- **State spam.** `withApplication(0, 1)` is what a plain `hurt` passes. Confirm that it does not refresh a state
  before relying on "only the crack applies it".

## Open questions

| Id | Question | Recommendation |
| --- | --- | --- |
| CL-20 | Does held Projection become a weapon? This softens `CL-1` from "R stays for combat" to "R keeps bursts; manipulations about motion also get a strand". | Yes, if the lash spike is fun |
| CL-21 | Does a converted manipulation keep its R cast as a quick flick? | Keep it through the first wave; decide after play |
| CL-22 | Left click while a strand is out as a snap. | Yes; reversal stays as the free version |
| CL-23 | The first strand a Harbinger meets: a new weak Animus Humilis lash, or nothing until Searing Lash at Mediocritas? | A new Animus lash that teaches the motion |
| CL-24 | Plain Absorption's damage gets an owner (aggro, kill credit). Does a kill by draw also pay the kill bounty? | Owner yes; no bounty on a draw kill |
| CL-25 | Movement while a strand is out. | Spray 1.0, Lash 0.8, Plant and Brand 0.6, Jet 0.5; a skill lifts them |
| CL-26 | Absorption specialises by the school state on the target (one rider table), not by per-manipulation hooks. | Yes |
| CL-27 | A second strand from the off hand as a late skill. | Defer |

Also needed first: `CL-2` (the verb toggle), `CL-10` (Temper on combat), `G-2b` / `F-16`.

## Acceptance

- With a strand manipulation selected, holding Projection at open air puts out a strand, and how the player moves it
  decides what it does.
- A still lash does nothing; a good stroke beats a spin; a click cracks it.
- Every hit is decided by the server, attributed to the caster, carries its school, and shows an impact.
- Absorption draws differently from a creature the player has marked, and mobs know who is draining them.
- Rites, Saints, Wills and stations take a channel exactly as before, and every existing Searing Lash test passes
  untouched.
- The playthrough suite shows no new failures over the recorded baseline.
