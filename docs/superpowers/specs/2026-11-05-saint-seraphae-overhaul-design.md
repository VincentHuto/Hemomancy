# Saint Seraphae: the containment fight

Date: 2026-11-05. Status: design, written by the Seraphae designer of the saint-overhaul design run, for the user's
review before anything is built. It expands the Seraphae brief in
[the umbrella spec](2026-11-05-saint-overhauls-design.md) ("The four briefs") and is bound by that spec's shared kit,
rules, fight-shape targets, lanes and anti-goals. Nothing here changes code; the build run (umbrella "The workflow", run
2) builds it.

The user asked for a full mechanics cleanup, wings as branching nervous projections, white light in the vessels with
separate gold flashes along the nerves; her bar is containment integrity, not health; she is contained, never killed;
she is not a willing villain, and her reach is the old instinct to heal. Every decision this spec takes that no ruling
covers is listed in section 20.

Paths below are relative to `src/main/java/com/vincenthuto/hemomancy/` unless they start with `tools/`, `docs/` or
`src/`. A class marked **new** does not exist today.

## 0. Resolutions applied (2026-11-05)

The umbrella's "Resolutions that override the designs" settle these points; each is also marked inline where it lands.

- **Resolved 2026-11-05 (user answers 2, 3, 4, 5):** hits outside an opening add nothing; the radiance spread may change
  real chamber blocks through `SaintArena`, always restored; she speaks short nurse's lines (voice sheet for
  `docs/NPC_VOICES.md` is the final review stage's); she is rebuilt at about 3.2 blocks at render scale 2.0 with a bone
  halo, bone trims and hematic iron locks, so gold on her is only nerve light.
- **Resolved 2026-11-05 (A-2):** Healed Ground grows under the most wounded challenger and where she last healed, plus
  one more under the next most wounded per extra challenger (section 7).
- **Resolved 2026-11-05 (A-3):** Convulsion is no longer a jump ring: it is six gold nerve spokes (an annulus of six
  arcs) turning a sixth of a turn over the active span, with the gaps between them turning too; the answer is to step
  into a gap and walk with it. The wide repeat turns the other way.
- **Resolved 2026-11-05 (A-7):** the Laying On of Hands (and its Straining form) is the fight's only hold. Wing Grasp
  no longer lifts and holds: it yanks the caught player to 3 blocks in front of her and roots them 10 ticks.
- **Resolved 2026-11-05 (B-1):** bands are HUG under 3, NEAR under 6.5, MID under 12, FAR beyond (`BossBands.STANDARD`).
- **Resolved 2026-11-05 (B-2):** Wing Grasp is AREA, wind-up 20 (was 16). Every move's kind follows the AREA rule;
  the Laying On of Hands that follows a Wing Grasp keeps the area floor of 20 (was 18).
- **Resolved 2026-11-05 (B-3):** the Beholding deed's reach tells clamp to the area reveal floor: the footprint shows
  for the last 20 ticks of the wind-up, and the chain-end arc and the white arm fill stay hidden (section 10).
- **Resolved 2026-11-05 (B-4):** anchors take held Projection at 4 blood a tick or the item's own rate, whichever is
  lower, at the item's own reach (5.5); the Condensing feed counts blood, 1 integrity per 16 blood.
- **Resolved 2026-11-05 (B-5):** decay scales by 1 + 0.5 (n − 1) with n the kit's ratcheted `challengerCount()`.
- **Resolved 2026-11-05 (B-8):** every animation track blends from the last pose over 5 ticks; no track is absolute.
- **Resolved 2026-11-05 (B-9):** the Nerve Lash is the one jumpable wave and uses `Height.JUMPABLE` (0 to 0.9).
- **Resolved 2026-11-05 (B-10):** a reloaded ending resumes (`resumeOnLoad`).
- **Resolved 2026-11-05 (B-12):** her 1.2 by 3.2 hitbox is a `getDefaultDimensions` override in `SeraphaeEntity`;
  `EntityInit` is not edited.
- **Resolved 2026-11-05 (C-1, C-3, C-5):** the Eye's recovery is the **Lapse** (was the Gutter); the Last Reach is the
  **Straining Reach**; move constants are upper snake case (`LAYING_ON_OF_HANDS`), `key()` lower.
- **Resolved 2026-11-05 (D-5):** three named sources, one per drain: `seraphae_anchor_binding` (re-binding a slipped
  anchor), `seraphae_reserve_wake` (waking a reserve), `seraphae_condensing_feed` (feeding a held anchor in Condensing).
- **Resolved 2026-11-05 (D-8, E):** no polling workaround: the kit's `SaintChannelRouter` serves her. Edits outside her
  lane are compile-gate follow-ups (section 18).
- **Resolved 2026-11-05 (kit, part 3):** an ending cancels the active move on its first tick (the kit's
  `beginEnding`); she no longer finishes the move's active span first (section 11).
- **Resolved 2026-11-05 (B-6, worked fight length):** see section 4: about five minutes solo, unchanged.

## 1. The fight in one paragraph

Seraphae rises from her tomb on the dais, chained by four anchors, and glides out over the boss floor toward whoever is
most hurt, because she still wants to heal them. Her presence mends a wounded player and grows a whole one: excess
vitality swells, roots and finally bursts. Her most dangerous move is her kindest: the Laying On of Hands, a gliding
reach that takes the player in both hands, heals them to full and then keeps going. The player learns two colour rules
in the first minute: **white is what heals, and so what grows; gold is a nerve firing, and so the strike, now**. They
also learn that her reach is her chain. Stand just past where the chains end, let her reach, and the chains stop her
short and yank her back; that recoil, and every other opening, is the only time a blow tightens the binding. Integrity
rises from what the player does with their blood: struck openings, anchors re-bound with held Blood Projection when
she tears them loose, reserve anchors woken in the aisles, fragments of her light drawn in with held Blood Absorption
or led into an anchor. As integrity climbs her chains shorten and pull her back toward the dais, but the light she can
no longer reach with escapes as area moves, until the chamber's own healed stone glows from inside. Winning feels like
tending a patient who cannot stop: the anchors flare, braided light runs to her wrists and wing roots, her wings and
hands are drawn inward, and she sinks back into the tomb, still radiant and still reaching, and the room goes dark.

## 2. What stays and what goes

### Stays (reworked onto the kit)

| Piece | Path | What happens to it |
| --- | --- | --- |
| The entity and its tomb binding | `common/entity/boss/saint/seraphae/SeraphaeEntity.java` | Rewritten on `SaintTrialEntity`'s kit fields (controller, meter, presence, openings, arena, phases, deeds). Keeps the rise, `endTrial`, the enrage record, `rewardsGoToVictor`, `removeWhenFarAway(false)`, the integrity bar binding (`BossBarBinding` with `FRAME_SERAPHAE`). Health stops mattering at all (section 6). |
| Containment anchors | `common/entity/boss/saint/seraphae/ContainmentAnchorEntity.java`, `SeraphaeAnchorRules.java` | Kept as fight entities woken from the dormant `containment_anchor_block`s round the tomb. Gains a restraint slot, a held/slipped/binding state, projection feeding and the reserve kind. Its own block clear and restore (`wakeFrom`, `restoreHome`) moves to the kit's `SaintArena`, which becomes the only owner of the cells. The orphan check stays, simplified. `SeraphaeAnchorRules` keeps `placements`, `wakeable`, `inReach`, `ringSlot` and gains reserve selection. |
| Fragments | `common/entity/boss/saint/seraphae/SeraphaeFragmentEntity.java` | Kept as the entity her light breaks into. Loses its vanilla goals and its push; gains a seeking glide, the graft, the return timer and absorption binding. Keeps the `die` notify path and the `tickDeath` that skips vanilla's death poof. |
| Effects helper | `common/entity/boss/saint/seraphae/SeraphaeEffects.java` | Rewritten as the one place the server sends her `SERAPHAE_RELIQUARY` cues. Its `HemoParticleData.hitGlow(PALE)` goes (cursor and trace rule); accents become fine `HemoParticleData.glow` motes. |
| Visual rules | `common/entity/boss/saint/seraphae/SeraphaeVisualRules.java` | Keeps `burstOpacity` and the cue kinds; `featherWave` becomes `nerveWave`; the `Pose` record and the state-driven `pose(...)` go with the state machine. `MODEL_SCALE` becomes 2.0. |
| Growth rules | `common/entity/boss/saint/seraphae/SeraphaeGrowthRules.java` | Rewritten as the presence's pure rules (section 5). Today it is an 80-tick transfusion cycle with `REACH` 10 and "convulse on the second full-health pulse". |
| World geometry | `client/render/world/SeraphaeRadianceGeometry.java` | Kept and rewritten. Its `tether` braid (shell, three strands, core, travelling knots), `fragment` and `feather` drawings are kept as the chain, fragment and axon primitives; its gold `0xFFD563` ribbons move off the LUX batch onto `LuxUmbraBatch.Material.DUCTILIS`. |
| Bar frame | `client/screen/overlay/boss/frame/SeraphaeFrame.java` | Kept. The sagging chains already read integrity; fill and cores turn from gold to white (section 12). |
| Animation state | `client/model/entity/boss/seraphae/SeraphaeAnimState.java` | Kept as the per-entity replay holder; re-keyed from the old state timer to the kit's synced move id and move start tick. Keeps wrist and nerve-tip world points for tethers. |
| Renderer | `client/render/entity/boss/seraphae/SeraphaeRenderer.java` | Kept; scale 2.0; its layers swap (section 12). |
| Generator | `tools/model_export/seraphae.py`, `tools/model_export/seraphae_anim.java` | Rewritten rig and texture painter; `_anim.java` becomes the track library (section 13). |
| Chamber theme | `tools/worldgen/mausoleum/themes/seraphae.py` | Unchanged. Its `chamber_sockets` already stands the four tomb anchors and the two aisle-end anchors this fight uses. |
| Grown stone | `tools/model_export/seraphae_growth.py` and its blocks | Unchanged; the fight borrows `radiant_healed_quartz` (section 9). |
| Lang lines kept | `message.hemomancy.seraphae.excess_growth`, `.growth_warning` | Reused by the presence. |
| Review client | `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SeraphaeFightClientReview.java` | Same class and `-PseraphaeFightReview` flag; rewritten as a staged stop list (section 16). |

### Goes (deleted outright; the mod is not released, so no save migration)

| Piece | Path | Why |
| --- | --- | --- |
| The four-state timer cycle | `SeraphaeState.java` (STABLE 200, FRACTURING 100, DISPERSED 160, CONDENSING 80 ticks), `SeraphaeEntity.advanceState`, `enter*`, `tickState`, `DATA_STATE`, `DATA_STATE_TIMER` | It is a fixed timer, the anti-goal. Fracturing and condensing survive as a move and an opening that the fight's events trigger. |
| Dispersal | `setInvisible(true)` in `enterDispersed`, the renderer's DISPERSED skip | Eight seconds of an untargetable, invisible boss is dead air. |
| Vanilla goals | `SeraphaeEntity.registerGoals` (Float, WaterAvoidingRandomStroll, LookAtPlayer, RandomLookAround, HurtByTarget, NearestAttackableTarget); the fragment's goals | The kit's controller owns movement, facing and targets. |
| Secondary attacks | the `NERVE_LANCE` and `REGROWTH_KNOT` rows of `SaintAttackPattern`, `tickSecondaryAttacks`, the secondary posing in `seraphae_anim.java` `setupAnim` | The kit replaces `SaintSecondaryAttacks`; Nerve Lance becomes the Axon Lance and Regrowth Knot survives as a move. |
| Overrun | `onArenaOverrun` (4 damage to all, integrity to 5) | Becomes the Radiance Flood (section 7). |
| Colour-by-integrity bar | `updateBossBarColor` (green, yellow, red) | Its meaning is upside down for a bar that should fill; the frame paints the fill. |
| Bold chat for every state | `broadcastMessage` calls in the state machine, the bold `broadcast` override | Replaced by a few voice lines and once-per-fight teaching lines on the action bar (section 13 and 14). |
| Jingle | `SoundEvents.UI_TOAST_CHALLENGE_COMPLETE` on containment | A UI fanfare, out of tone. |
| Empty client tick | `SeraphaeEntity.tick` client branch | Does nothing. |
| One glow mask | `client/render/layer/mob/SeraphaeWingGlowLayer.java`, `textures/entity/boss/seraphae/seraphae_glow.png` | It draws one gold-to-white tint over every lit surface, mixing the two lights the lore keeps apart. Replaced by a white vessel layer and a gold nerve layer. |
| Hand-made anchor model | `client/model/entity/boss/seraphae/ContainmentAnchorModel.java`, `textures/entity/boss/seraphae/containment_anchor.png` | Boxes that do not match the generator-authored block. A woken anchor renders the block's own model. |
| Fragment model | `client/model/entity/boss/seraphae/SeraphaeFragmentModel.java`, `textures/entity/boss/seraphae/seraphae_fragment.png`, `models/entity/bbmodel/bosses/SeraphaeFragmentModel.bbmodel` | Not generator-authored; the geometry pass already draws the fragment. The renderer draws only geometry. |
| Old Seraphae chat keys | the eight `message.hemomancy.seraphae.*` state lines other than the two kept | Retired; removal is an integration-stage edit because `en_us.json` is add-only for Saint builders. |

Removed assets move to the untracked `removed_assets/` when the build happens, not now.

## 3. Phases

Phases go by the meter, not health, and **ratchet**: once integrity first crosses a threshold the phase is kept even if
integrity falls again (only the chain length and the moves' pressure follow the live value). A phase change never cuts
an active move; it queues to the recovery boundary (kit rule).

| Phase | Trigger | Name (bar caption, frame phase) | What it adds | Bar | Phase card |
| --- | --- | --- | --- | --- | --- |
| I | the rise ends (integrity starts at 30) | The Reaching (frame phase 0) | Presence, Laying On of Hands (always her first move), Nerve Lash and Counter-Lash, Axon Lance, Regrowth Knot, Transfusion Thread; the Recoil opening; reserve anchors can be woken | ivory fill, chains slack | none (the shared `SCENE_SAINT_RISE` plays) |
| II | integrity first reaches 40 | The Restraint (frame phase 1) | Chain Wrench (anchors slip; rebinding; the Seized opening), Fracture and fragments (the Condensing opening), Wing Grasp, Convulsion; Axon Lance gains its mirrored follow-up; radiance spread 24 blocks | ivory with a gold edge on phase change | **"The Chains Tighten"** (50 ticks, `BossScenes`, no camera) |
| III | integrity first reaches 75 | The Escaping Light (frame phase 2) | The Unclosing Eye (the Lapse opening; Resolved 2026-11-05, C-1), Healed Ground, the Straining Reach (Resolved 2026-11-05, C-3), the second Convulsion, the reach's stronger hold; radiance spread to 72 blocks | brilliant white | **"The Light Escapes"** (50 ticks) |
| End | integrity reaches 100 | (bar full) | The containment ending (section 11) | full | `SCENE_SERAPHAE_CONTAINED` (new, 140 ticks, letterbox and HUD hide, no camera, no input lock) |

Vanilla bar colour stays `BossBarColor.WHITE` for all phases; the painted frame carries the look. Phase names are lang
keys `boss.hemomancy.seraphae.phase.reaching`, `.restraint`, `.escaping_light`; card titles
`scene.hemomancy.seraphae_chains_tighten` and `scene.hemomancy.seraphae_light_escapes`.

## 4. The signature meter: containment integrity

**Owner:** the Saint (`SaintMeter`, Saint-owned, synced as her entity data). **The boss bar is this meter**, filled
from the left, not health (kit request K-5). Pure numbers live in **new** `SeraphaeIntegrityRules`.

| Rule | Number |
| --- | --- |
| Range, start | 0 to 100; starts at 30 on both a proper and a forced awakening (forced is harder through deeds, not longer) |
| Thresholds | 40 phase II, 75 phase III, 100 contained (checked before the tick's decay, as today), 0 the Radiance Flood |
| Passive decay | 0.010 per tick × (1 + 0.5 × (challengers − 1)), plus 0.008 per tick per living fragment (today's 0.01 and 0.008 kept) |
| Opening hits | Recoil and Seized: +2 per melee or projectile hit, +3 per school-cast hit. Condensing and Lapse: +3 per hit, +4 per school cast. A hit counts if its source traces to a player, whatever its damage. |
| School weighting | Congeatio, Tenebris and Mortem casts (the stilling schools: circulation, sensation, decay) ×1.5; Lux and Ductilis casts (her own) ×0.5; others ×1; results round to the nearest 0.5 |
| Per-opening cap | Recoil 4 hits, Seized 3, Condensing 5, Lapse 4, each +1 per challenger beyond the first; a hit past the cap still shows the wound closing but adds nothing |
| Condensing feed | held Blood Projection into any held anchor during Condensing: +1 per 16 blood fed (+0.25 a tick at 4 blood a tick), at most +10 per player per window, paid through `seraphae_condensing_feed` (Resolved 2026-11-05, B-4 and D-5) |
| Anchor work | a slipped binding anchor re-bound (240 blood through `seraphae_anchor_binding`): +8; a reserve anchor woken the first time (320 blood through `seraphae_reserve_wake`): +10; re-woken after she tears it (240 blood): +5 |
| Fragments | drawn in by held Blood Absorption +3; captured by a held anchor +5; struck apart +1.5; grafted onto a player −2; returned to her −4 |
| Losses | an anchor slips −6; any player's growth bursts −3 |
| Floor event | at 0 the Radiance Flood fires once and integrity resets to 10 |
| Outside openings | a hit adds nothing; the wound closes in white light on her body and, if the striker is within 4 blocks and at full health, they gain 1 growth (her light on their hands) |

A rough solo run at those numbers: phase I about 70 seconds (three Recoils and a reserve anchor), phase II about two
minutes (two Fracture cycles, two rebinds), phase III about 90 seconds (Gutters, a Last Reach recoil, Condensing),
about five minutes in all against 12 integrity a minute of decay. Four challengers decay at 30 a minute but bring more
hands to every cap.

**Worked solo number, Resolved 2026-11-05 (B-6, A-3, A-7).** Start 30. Phase I (30 to 40): about 70 seconds lose 14
to decay; three reach Recoils at a capped 4 hits of +2 (one with a cast, +3) give about 25, a reserve wake +10, one
slip -6: net about +15, so phase II opens at about 70 s. Phase II (40 to 75): about 125 s lose 25 to decay and about 8
to living fragments; two Fracture cycles (fragments +3 to +5 each, Condensing five hits of +3 and a +10 feed) give
about 2 x 30, two rebinds +16, two slips -12, a growth burst -3: net about +35 at about 195 s. Phase III (75 to 100):
about 95 s lose 19 to decay; two Lapses at four hits of +3 give 24, a Straining Reach Recoil 8 to 12, one Condensing
about 15: net about +25 at about 290 s. About five minutes, inside the four-to-six-minute target. A forced Apotheos
keeps the same integrity numbers; No Rest Given trims every Recoil to 28 ticks (still enough for four capped hits), so
the forced fight is harder (hidden reach tells, the Extraction, faster recoveries) without being longer.

**HUD meter (new `SeraphaeMeterStyle`, the kit's `SaintMeterStyle`).** Since the bar already shows integrity, her
meter under the frame shows what integrity is made of. Painted with `GuiCanvas` (`disc`, `segment`, `rect`) in the
frame's own colours (`SeraphaeFrame` `CHAIN 0xFF2E2628`, `CHAIN_LIP 0xFF5A4E50`, white Lux `0xFFFFF4E8`, nerve gold
`0xFFFFE3A8`), 182 by 16 px, centred under the bar:

- **The restraints row.** Four shackles at 22, 40, 60 and 78 per cent of the width in body order (left wing root, left
  wrist, right wrist, right wing root), joined by a hematic iron chain line of alternating flat and edge links like the
  frame's rails. A held shackle is an iron ring round a small white core (radius 2.2) that breathes with her presence
  pulse. A slipped shackle is a broken ring with a dark core, and the chain beside it sags and parts. A shackle being
  re-bound fills its core white clockwise as a ring sector with a single gold bead at the leading edge (the same gold
  spark the world draws at the growing chain). Two smaller reserve shackles stand at 8 and 92 per cent, drawn only as a
  faint outline until woken.
- **Openings.** While an opening is live a gold impulse runs along the chain line from the centre outward every 10
  ticks, and the links light white from both ends inward as its time runs out.
- **Your growth** (per player, kit request K-6). A thin vessel line, 120 px, under the restraints. It fills white from
  both ends toward a centre knot as growth rises; bone notches mark 3 (Swelling) and 6 (Knotting); at 10 the knot
  swells and flares white, then the line empties. Growth is Lux and never shows gold.

## 5. Presence: over-healing

Implemented as the kit's `SaintPresence` hook in **new** `SeraphaePresence`, rules in the rewritten
`SeraphaeGrowthRules`. Growth is per player, held in a UUID map on the Saint, cleared through the presence's
`TransientServerStateEvents` registration on logout and at the trial's end, and synced to its owner for the HUD.

| Rule | Value |
| --- | --- |
| Radius, line of sight | 9 blocks from her chest, block line of sight required |
| Cadence | every 20 ticks (Repose deed: 14) |
| A wounded player | is healed by up to 1 health per pulse (never past full), and sheds 1 growth. The heal is real: this is the old instinct. |
| A whole player | gains 1 growth per pulse, 2 if within 3.5 blocks |
| Growth stages | 0 to 2 nothing; 3 to 5 **Swelling** (Slowness I); 6 to 9 **Knotting** (Slowness II and jump strength 0 through `AttributeModifiers.sync`, a held modifier); 10 **Burst** |
| Burst | 6 damage (8 in phase III) of type `hemomancy:saint_seraphae`, Weakness 60 ticks, rooted 20 ticks; growth to 0; integrity −3 |
| Shedding | every 2 damage the player takes from any source but her presence sheds 1 growth (a wound for the light to fill); every 20 fed ticks of Blood Projection into an anchor sheds 1; each of their school casts that lands on her sheds 2; outside her radius growth falls by 1 every 40 ticks |
| Players only | `ManipulationCombatHelper.canHarmFor(seraphae, player)` filters; spectators and the dead are skipped |

**Ground read.** One thin white `RIPPLE` ring on the floor at the 9-block radius, alpha 0.10, rising to 0.16 for 6
ticks on each pulse, drawn by her geometry pass through `WorldEffectDispatcher`. On each pulse one fine
`HemoParticleData.glow` mote (white) travels along the floor from her to each player inside, never a thread. On the
player: growth draws as luminous sores in world geometry at their body points (Swelling: three to five small white
`FOCUS` cards on arms and torso at alpha 0.35; Knotting: plus short bone-white `SLASH` spurs at shoulders and knees and a
faint `RIPPLE` disc under the feet with three short root ribbons into the floor). A burst is a Riposte-style white ray
burst from the chest with a 6-tick gold `CRACKLE` along the limbs on the DUCTILIS material.

**Why this shape.** It makes the lore's line literal: she cannot let anything living remain as it is. It also gives
the two paths different presences without a rule for each (section 15): hallowed ground keeps an Apotheos wounded, so
her presence mostly heals them.

## 6. Openings

Her health is never the win and never changes: `hurt` from a player is turned into a "touch" (counted by the opening
rules or closed in white), her health stays at maximum, and lethal damage is refused (kit rule; she is never killed).
Every gain from striking her runs through these four openings (kit `SaintOpening`), each with a damage-taken multiplier
of 1 (health is not the bar).

| Opening | Entered by | Tell | Length | What counts | Feeds |
| --- | --- | --- | --- | --- | --- |
| **Recoil** | a Laying On of Hands or Last Reach that the chains stop short of its target (the target stood beyond the chain's end); a hold broken by the held player or by allies (20 ticks, cap 2) | the chains snap taut with a bright overshoot, she is jerked back half a block, her wings fling forward and open, her chest light opens; the restraint shackles on the HUD flash | 40 ticks (Last Reach 60; Repose deed 28 and 40) | melee, projectile and school-cast hits; cap 4 | integrity (+2, +3 per cast) |
| **Seized** | a slipped anchor re-bound, or an anchor held by projection at a Chain Wrench's commit | that anchor's chain snaps taut and drags her 2 blocks toward it; she kneels in the air with that arm or wing stretched back | 30 ticks | hits and casts; cap 3 | integrity (+2, +3 per cast) |
| **Condensing** | the last fragment of a Fracture resolves (bound, captured, struck, grafted or returned) | 10 ticks of gathering: every wisp and axon thread in the room converges on her, braided light runs from each held anchor to its restraint, wings fold, hands cross at the wrists over the chest | 60 ticks after the gather | hits and casts (cap 5) and held Blood Projection into any held anchor (+0.25 a fed tick, +10 a player) | integrity |
| **Lapse** (Resolved 2026-11-05, C-1; was the Gutter) | the Unclosing Eye's recovery | the eye of light in her chest lapses to a slit and flickers, she bows forward | 50 ticks | hits and casts; cap 4 | integrity (+3, +4 per cast) |

Outside an opening a hit is not wasted information: the wound visibly closes in white at the hit point (a `FOCUS` card
shrinking over 8 ticks), which teaches the rule the first time it happens. Inside an opening the hit point flashes and a
knot of white light travels from it along the nearest held chain to its anchor: the player sees their blow tighten the
binding.

Stagger rule: an opening entered while a move is active queues to that move's recovery boundary.

## 7. Moveset

Bands from her centre against her body yaw (Resolved 2026-11-05, B-1: the kit's one set): HUG under 3, NEAR 3 to
6.5, MID 6.5 to 12, FAR over 12. Sectors: FRONT up to 60 degrees, FLANK 60 to 120, REAR beyond. Damage is the base;
an enraged Saint multiplies it by her attack attribute over its base of 4 (the +30% enrage floor). All damage is the new
`hemomancy:saint_seraphae` type through `ManipulationCombatHelper.canHarmFor`. Every area move shows its footprint for
its whole wind-up; the gold commit flash starts 4 ticks before the commit tick as the last-chance cue. "Tracks" means
the aim follows the target until the commit tick, then locks. Move ids are the **new** `SeraphaeMove` enum, which
implements the kit's `SaintMove`; constants are upper snake case (`LAYING_ON_OF_HANDS`) and `key()` returns the lower
snake ids below (Resolved 2026-11-05, C-5). Kinds follow the AREA rule (Resolved 2026-11-05, B-2): every move here is
AREA except the two lashes, the Convulsion and the Fracture.

**L** below is her chain length, the leash (section 8).

| id | Name | Phases | Band / sector | Tell (animation beat; world geometry) | Footprint | Wind-up / active / recovery | Damage and effect | Counterplay | Follow-ups |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `laying_on_of_hands` | Laying On of Hands | I, II, III | NEAR, MID; FAR only if the target is within L + 3; FRONT (FLANK low) | Palms turn outward and rise, head lifts gently, wings draw back; the vessels of both arms fill white from shoulder to fingertip over the wind-up. Two white lines grow along the floor from her hands toward the target, and a faint white arc marks where her chains end, with a gold bead where the path crosses it. Gold flash runs down both arms at commit. | Path: a lane 1.6 wide along the locked aim, ending where her chain stops her plus 1.5 blocks of reach | 24 / 10 / 20; then the hold, up to 60 | She glides the lane in 10 ticks. A caught player is **held** (one at a time): each 10 ticks she heals 2 missing health; once they are whole, each 10 ticks adds 2 growth (3 in phase III), so a whole player bursts at about tick 50. Released rooted 30 ticks. | Stand just beyond the chain's end (she stops short: Recoil); sidestep the lane at the commit (no opening); be wounded (the hold spends its time healing you). Break a hold: hold Blood Absorption on her for 20 ticks, land a Congeatio or Tenebris cast on her, or two hits from allies (all three give a 20-tick Recoil). | On a hold's end: `nerve_lash` at the released player (70%, link 6). |
| `nerve_lash` | Nerve Lash | I, II, III | HUG, NEAR; FRONT, FLANK (REAR low) | The wing on the target's side drops low and draws back, its lower trunk stiffening; gold gathers at the wing root and runs the trunk at commit. | Cone sweep, 150 degrees, radius 6, on that side, the sweep crossing in 6 ticks; height band `Height.JUMPABLE`, 0 to 0.9 (Resolved 2026-11-05, B-9) | 14 / 6 / 16 | 7, Slowness II 20 ticks, 1 growth | Jump it (it is low); or leave the arc | `counter_lash` (60% if the target is still HUG or NEAR, link 6) |
| `counter_lash` | Counter-Lash | follow-up only | as above | The other wing rises high and arches over her head; gold runs its upper trunk at commit. | Cone sweep, 150 degrees, radius 6, the other side; standing band, and a crouching player is passed over (the kit tests feet height, so the crouch is read in the move's own hit) | 12 / 6 / 22 | 7, Slowness II 20 ticks | Crouch under it; low then high is the string to read | none |
| `axon_lance` | Axon Lance | I, II, III | MID, FAR (NEAR low); FRONT (FLANK low) | One wing's middle trunk swings forward and points, the tip fan opening; a white line draws along the floor and forks near its end; gold races out along the trunk at commit. | Line 16 long, half-width 0.6, forking at 10 blocks into three rays of 6, at 0 and plus or minus 20 degrees (composite footprint, K-3) | 22 / 8 / 18 | 6, Weakness 40 ticks, 1 growth | Step off the line before the fork, not into a ray | Phase II and on: a mirrored `axon_lance` from the other wing, aim locked at its start (50%, link 8) |
| `regrowth_knot` | Regrowth Knot | I, II, III | MID, FAR; any sector | Her hands cup together low in front of her; healed pads of pale tissue spread on the floor at the target. | Disc of radius 2.75 at the target; tracks for 16 ticks, then locks | 30 / 60 / 14 | Pulses at active ticks 0, 20 and 40: a wounded player is healed 1, a whole one takes 3 and 2 growth; each pulse roots 10 ticks | Leave the disc; or stand in it wounded (it heals) | none |
| `transfusion_thread` | Transfusion Thread | I, II, III | FAR (MID low); any sector; needs line of sight at commit | She lifts one hand toward the far player and a single white thread leaves her heart; gold beads travel it toward the target. | None on the floor; a held tether to the target's chest (K-3 `NONE`) | 20 / 80 / 16 | Each 10 ticks: 1 growth (or heals 1 if wounded) and a pull of 0.6 blocks toward her | Break line of sight for 4 ticks (the rank pillars and aisles), or get more than 22 blocks away | none |
| `wing_grasp` | Wing Grasp | II, III | NEAR, MID (HUG low); FLANK, REAR (FRONT low) | A wing reaches round behind or beside her, its tip fans opening like fingers over the target; a disc of white light marks where they will close; gold crackles at the tips at commit. | Disc of radius 2.5 at the target; tracks to commit | 20 / 8 / 22 (Resolved 2026-11-05, B-2: AREA) | 5; yanked to 3 blocks in front of her and rooted 10 ticks, no hold (Resolved 2026-11-05, A-7) | Keep moving through the wind-up; do not stand behind her at the anchors without a reason | `laying_on_of_hands` at the area floor of 20 (80%, link 4) |
| `convulsion` | Convulsion | II, III | HUG, NEAR; any sector | Her body locks rigid; every nerve on her flares gold at once in three stutters over the wind-up; her chains rattle. | Resolved 2026-11-05 (A-3): six gold nerve spokes, an annulus 1.5 to 7 of six 34-degree arcs turning 60 degrees clockwise over the 12 active ticks; standing band | 22 / 12 / 30 | 8, Slowness IV 20 ticks, a small camera twitch, 1 growth | Step into a gap and walk with it as it turns; or be outside 7 | Phase III: `convulsion_wide`, spokes 2 to 9 turning the other way, wind-up 20, active 14 (50%, link 10) |
| `chain_wrench` | Chain Wrench | II, III | any band; eligible when an anchor holds and a player is within 5 blocks of an anchor or she has strained at her chain's end 40 ticks | She seizes her own chain in both hands; gold travels the chain from her hands to the anchor through the wind-up; the anchor's crown flickers. | Line along that chain from her restraint to the anchor, half-width 1.0, plus a disc of radius 2 at the anchor's foot (composite, line between moving points, K-3) | 24 / 10 / 20 | 9 and knockback 0.8 to anyone on the line; the anchor **slips** at the active start (integrity −6, chain +3 blocks, that restraint free) | Get off the chain's line; or hold Blood Projection on that anchor at the commit tick, which holds it and **Seizes** her instead (30-tick opening) | none |
| `fracture` | Fracture | II, III | any; eligible only with no fragment alive, 500 ticks since the last Fracture and 200 since the last opening | Wings tear open wide, a vertical white crack opens down her chest, gold sparks run the cracks; light pours out at the active start. | Disc of radius 4 round her | 30 / 12 / 20 | 4 in the disc; releases fragments: 3 in phase II, 4 in phase III, +1 per challenger beyond the first, +1 Downfall deed, at most 8 | Back out of the disc; then bind the fragments (section 9) | none (Condensing follows when the fragments resolve) |
| `healed_ground` | Healed Ground | III | MID, FAR; any | Her hands open downward; pale healed pads spread on the floor under each player and between them; bone spurs show through. | Resolved 2026-11-05 (A-2): scattered discs, radius 2: under the most wounded challenger, where she last healed, and under the next most wounded per extra challenger | 28 / 10 / 14 | 6 and rooted 30 ticks to anyone on a disc at the active start; the pads stay 100 ticks and add 1 growth every 20 ticks to anyone standing on them | Step off the pads; stay off them afterwards | none |
| `unclosing_eye` | The Unclosing Eye | III | MID, FAR; FRONT (FLANK low) | Her head tips back and her chest light opens into an eye-shaped aperture, growing over the wind-up; the room's light leans toward her. | Cone sweep beam, 110 degrees over 40 ticks, angular half-width 3 degrees, range 24, occluded by blocks (raycast from her chest to the player's body centre, K-3) | 26 / 40 / 50 | Every 5 ticks in the beam: 3 and 1 growth | Outrun the sweep, or put a rank pillar between you and her chest | none (its recovery is the **Lapse**) |
| `straining_reach` | Straining Reach (Resolved 2026-11-05, C-3; was the Last Reach) | III | eligible when the target stands beyond her chain's end but within L + 8 | She wrenches one chain loose first (that anchor slips at commit), then the reach's tell, longer and plainer | Path: lane 1.6 wide, up to L + 5 + 1.5 | 28 / 14 / 24; hold up to 60 | As the Laying On of Hands with phase III growth | The torn anchor is the signal; sidestep the lane, or be beyond even L + 6.5; a whiff opens a 60-tick Recoil | On a hold's end: `nerve_lash` (70%, link 6) |
| `extraction` | The Extraction (Downfall deed) | II, III, forced with Downfall skipped | any | She pulls on every held chain at once; the light runs out of her along all of them into the anchors, then back. | Lines along every held chain, half-width 0.8, two passes: out at active 0 to 8, back at 10 to 18 | 30 / 20 / 26 | 6 per pass and 1 growth | Stand clear of all the chain lines (they all run to the dais) | none |

**The Radiance Flood** is not picked; it fires when integrity reaches 0. 40 ticks of tell (her arms and wings flung
wide, head back, the room whitening through a partly clear `LUX_SHELL` dome expanding from her toward the walls at alpha
0.18), then 6 damage and 4 growth to every challenger in the chamber, integrity to 10, radiance spread +24 blocks, 30
ticks of recovery. It fires once per fall to 0.

**Picking (new `SeraphaeSelectionRules`).** Weights by band, times the sector factor, times the phase factor; a move
with no weight in the player's band and sector is ineligible.

| Move | HUG | NEAR | MID | FAR | FRONT / FLANK / REAR | Phase factor I / II / III | Cooldown |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `laying_on_of_hands` | 0 | 30 | 40 | 25 (within L + 3) | 1 / 0.4 / 0 | 1 / 1 / 1.2 | 140 (III: 100) |
| `nerve_lash` | 35 | 35 | 0 | 0 | 1 / 1 / 0.3 | 1 / 1 / 1 | 60 |
| `axon_lance` | 0 | 10 | 30 | 40 | 1 / 0.4 / 0 | 1 / 1 / 0.8 | 80 |
| `regrowth_knot` | 0 | 10 | 25 | 25 | 1 / 1 / 1 | 1 / 0.8 / 0.6 | 160 |
| `transfusion_thread` | 0 | 0 | 10 | 45 | 1 / 1 / 1 | 1 / 1 / 1 | 200 |
| `wing_grasp` | 10 | 25 | 15 | 0 | 0.3 / 1.2 / 1.6 | 0 / 1 / 1 | 120 |
| `convulsion` | 40 | 25 | 0 | 0 | 1 / 1 / 1 | 0 / 1 / 1.2 | 100 |
| `chain_wrench` | 35 any band (60 if a player is projecting into an anchor) | | | | any | 0 / 1 / 1 | 180 |
| `fracture` | 30 any band when eligible | | | | any | 0 / 1 / 0.8 | 500 |
| `healed_ground` | 0 | 0 | 25 | 30 | 1 / 1 / 1 | 0 / 0 / 1 | 220 |
| `unclosing_eye` | 0 | 0 | 30 | 40 | 1 / 0.3 / 0 | 0 / 0 / 1 | 260 |
| `straining_reach` | 50 when eligible | | | | any | 0 / 0 / 1 | 400 |
| `extraction` | 25 any band (deed only) | | | | any | 0 / 1 / 1 | 500 |

Variety, on the Mycophant's model: never the same move twice in a row (a string counts as one); a move in the last three
picks is weighted 0.4, 0.6, 0.8 (most recent first); follow-ups ignore that and never go more than one link (strings
are two moves). **Kite pressure:** each 20 ticks the target spent in FAR in the last 200 adds 4 to the Transfusion
Thread (cap +40) and, in phase III, to the Last Reach. **Hug pressure:** each 20 ticks in HUG adds 4 to Convulsion (cap
+30). **Turtle pressure:** a target that has stayed beyond her chain's end and outside her presence for 200 ticks adds
20 to Regrowth Knot and Healed Ground. The decision delay between moves is 14 ticks in phase I, 10 in II, 8 in III
(Repose deed −4); no move runs on a timer.

**Target.** She means to heal: her target is the challenger with the largest missing-health fraction within 24 blocks
and line of sight, ties to the nearest (kit request K-11). Her first move after the rise is always the Laying On of
Hands at that target; if they stand beyond L + 8 she glides to her chain's end facing them first.

**Strings** (the umbrella asks for two): Nerve Lash into Counter-Lash (low then high); Wing Grasp into Laying On of
Hands (thrown into her hands); Laying On of Hands into Nerve Lash on the rooted player; Axon Lance into its mirror;
Convulsion into the wider Convulsion.

## 8. Locomotion: a chained glide

**New** `SeraphaeGlide`, the controller's per-Saint locomotion strategy; pure leash numbers in **new**
`SeraphaeLeashRules`.

- **She does not walk.** No gravity; she hovers 0.35 blocks over the floor beneath her (sampled each tick, eased at 0.2
  per tick, so she rides the dais steps smoothly). Glide speed up to 0.11 blocks a tick (the +15% enrage and +20% Repose
  floors apply), acceleration 0.01 a tick, 0.06 a tick within HUG. Vanilla collision stays on so she slides along
  pillars. No pathfinding: the boss floor is flat and open by the generator's pin.
- **Leash.** Centre C is the centroid of the four binding anchors' home cells (chamber-local (30.5, 13), on the dais;
  fallback: her spawn). Chain length L = 6 + 14 × (1 − integrity / 100) + 3 per slipped binding anchor − 2 per woken
  reserve anchor, never below 4. At the start L is 15.8 (she reaches x 14.7 down the centre line, so the boss floor's
  west third is out of her hands' reach); at 75 it is 9.5 (the floor's east edge and the steps); near 100 she is held
  on the dais.
- **Desired point.** The point on the line to her target at 5 blocks from them (NEAR, where her favoured moves live),
  clamped to the leash disc. A target beyond the leash: she glides to the leash edge facing them and **strains**
  (the strain pose; all held chains taut). With no challenger in the chamber she drifts back over the tomb and idles.
- **Reeling.** When L shrinks below her distance from C (integrity rose, a reserve woke), the chains reel her toward the
  leash edge at 0.15 blocks a tick and she picks no move until she is within a block of it (kit request K-11).
- **Facing.** Turn rate 6 degrees a tick roaming, 10 during a wind-up's tracking, 0 after the commit.
- **During a hold** she stops; during the reach's active span she travels the lane at up to 1.4 blocks a tick.

## 9. Arena

Resolved by the kit's `SaintArena` from the tomb and its facing at the rise. Chamber-local coordinates below follow
`tools/worldgen/mausoleum/pieces.py` (x from the ring door, `CHAMBER_ZC` 13; boss floor x 9 to 23, z 6 to 20; tomb main
block (30, 4, 13)).

**Set pieces that take part.**

| Piece | Where (from `pieces.CHAMBER_SOCKETS` and `themes/seraphae.py`) | Role |
| --- | --- | --- |
| Four binding anchors | the two `dais_flank` anchors at (28, 5, 7) and (28, 5, 19); the two `CONCH_ANCHORS` at (33, 4, 9) and (33, 4, 17); all inside `SeraphaeAnchorRules.HORIZONTAL_REACH` 8 of the tomb | Woken with her at the rise (as today). Each holds one restraint, assigned once by nearest: left and right wing roots, left and right wrists. Chain Wrench and Last Reach make them slip; held Blood Projection re-binds them: 60 fed ticks at 4 blood a tick (240), 4-tick grace, then the charge bleeds off at half speed, paid through `BloodAccounting.pay` with **new** `SeraphaeAnchorBindingRules.SOURCE` (`seraphae_anchor_binding`, counts as spend, not reserve-protected). Held anchors capture fragments within 3.5 (today's `CAPTURE_RADIUS`). |
| Two reserve anchors | the two `aisle_end` anchors at (26, 1, 3) and (26, 1, 23), behind the pillar ranks; whole anchors beyond reach 8 of the tomb | Dormant at the rise. Woken by held Blood Projection: 80 fed ticks at 4 a tick (320). A woken reserve throws a chain to her waist shackle: +10 integrity once, chain length −2, and it captures fragments. Within her reach until about integrity 67, so waking one early is a risk taken under her hands; Chain Wrench prefers reserves. |
| The broken anchor | the snapped anchor in the `apse_niche` behind the tomb's head | Untouched: the first anchor they built, broken. Lore only. |
| The rank pillars | `pieces.CHAMBER_PILLARS` at z 5 and 21 | Cover: they break the Transfusion Thread's and the Unclosing Eye's line of sight. |
| The grown stone | `healed_quartz_bricks` and `healed_calcite` cells the theme's `grow` pass put in the chamber (GROWTH level 4) | The radiance spread (below). |

**What the fight changes, all recorded by the arena.**

1. Anchor cells: a woken anchor's two block cells clear (both binding and reserve). The arena records them; the anchor
   entity no longer restores its own block.
2. **The radiance spread** (**new** `SeraphaeRadianceSpread`): at phase II the 24 healed stone cells nearest to her turn
   to `radiant_healed_quartz` (light 12; the block already exists for this look); at phase III 48 more (72); each Radiance
   Flood 24 more (at most 96). This is the lore's "the chamber floods with the light she emits", done with the theme's
   own block instead of a fog tint. The arena's scan by block predicate and its ordered restore are kit request K-8.

**Restored** in `endTrial`, in `remove`, on load of a Saint whose trial ended, and on server stop (kit rules): anchor
cells stand their dormant blocks again (unsnapped, as `ContainmentAnchorEntity.restoreHome` does today), and the
radiant cells turn back to their recorded originals in a wave from the walls inward over 20 ticks ("returns the room to
darkness"). Fight entities (anchors and fragments with her UUID as parent) are discarded with her; an orphaned anchor
whose Saint has been gone for three 100-tick checks with a player nearby discards itself (today's rule, kept).

**No chamber edit.** The theme already stands every anchor this fight needs, and the boss floor must stay clear, so
`tools/worldgen/mausoleum/themes/seraphae.py` is not changed or re-run. If review finds the floor too open for the
Thread and Eye counterplay, the fix is a Seraphae-theme follow-up (swollen low pillars in the aisles), not now.

**Fallback.** A Seraphae with no tomb (a command spawn, some tests) gets today's ring of four anchors at radius 10
(`SeraphaeAnchorRules.ringSlot`), no reserves, no radiance spread, and her spawn as the leash centre.

## 10. Deeds of a forced awakening

The stat floors stay (`SaintEnrageRules`: +30% attack, +15% speed; Downfall +50% health; Repose +20% speed; Beholding
Resistance). On her the health and Resistance floors change nothing, because her health is never the bar; her deeds
carry the forced fight. They live in **new** `SeraphaeDeeds`, read once from the skipped Chapel kinds the tomb recorded.
Her Chapels are Beholding, Downfall and Repose; an Apotheos who shatters the door at once skips all three.

| Skipped Chapel | What the Chapel was | The deed, in her language | Rule |
| --- | --- | --- | --- |
| **Beholding** | a memory watched correctly: the first patient she laid hands on | *Unwatched Hands.* You did not watch her hands, so you do not see them coming. | Resolved 2026-11-05 (B-3): the Laying On of Hands and the Straining Reach show their footprint only for the last 20 ticks of the wind-up (the kit's area reveal floor), and never show the white arm fill or the chain-end arc; the gold commit cue still shows. |
| **Downfall** | her fall to the power of the blood: the keepers who chained her and bled her | *The Extraction.* She fights with what was taken from her. | Adds the `extraction` move to phases II and III; each Fracture sheds one more fragment. |
| **Repose** | stillness under threat | *No Rest Given.* She was never allowed to rest, so she never pauses. | Recoveries −30% (never below 10 ticks; the opening-bearing recoveries keep their floors), Recoil 40 to 28 and the Straining Reach's 60 to 40, decision delay −4, presence cadence 20 to 14. |

The forced fight also starts with no reserve anchor lit and the same 30 integrity. Section 15 checks that an Apotheos
with all three deeds can finish.

## 11. The ending

Owned by **new** `SeraphaeEndingRules` (pure beats) and run from her tick; saved with her (`EndingTick`), so an unload
resumes it and a load of an ended Saint finishes it at once.

| Ticks | Beat |
| --- | --- |
| integrity reaches 100 | Resolved 2026-11-05 (kit part 3): the kit's `beginEnding` cancels the current move at once; she stops picking, cancels any recovery, releases a held player gently (set down, rooted cleared), recalls every fragment (each is drawn into the nearest held anchor with the capture tether). She becomes invulnerable. Every player's growth is cleared at tick 20 ("the light leaves you"). |
| 0 to 20 | The anchors flare one after another (binding anchors, then reserves); braided light runs from each to its shackle: the tether reveal of today's Condensing, at full strength, strands twisting faster. `SCENE_SERAPHAE_CONTAINED` starts for every challenger (letterbox, HUD hide). Line: "Still. It's still open." |
| 20 to 60 | The chains reel her back over the floor to hover above the tomb (speed set to arrive at tick 60). Wings draw inward (nerve trunks curling to her back), hands are drawn to her breast and cross at the wrists, head bows. |
| 60 to 90 | Over the tomb, her white vessel light retreats from fingertips and wing tips toward her heart; gold flashes stop one trunk at a time, the last one at her heart at tick 88. Line at 80: "I could have helped you." |
| 90 to 100 | She sinks into the tomb (the rise's sink, reversed). At tick 100 `endTrial(victor, null)` runs once: one loot roll (fragment-only when forced), the reward into the victor's inventory, `announceTrialEnd`, the sarcophagus restored dormant with its door record. |
| 100 to 120 | The arena restores: the radiant stone returns to healed stone from the walls inward; the anchor blocks stand again, unlit. |
| 120 to 140 | The scene's fade; she is discarded at 140. |

The victor is the player who landed the last integrity gain, else the nearest living challenger (today's
`endTrial(null, null)` nearest-player rule).

## 12. Visual pass

### Model (`tools/model_export/seraphae.py`)

- **Texel density.** Today `kit.SCALE = 2` at render scale 1.22 is about 26 px per block. New: render scale 2.0
  (`SeraphaeVisualRules.MODEL_SCALE`), `kit.SCALE = 2`: 16 px per block, the Mycophant's pairing. The rig's grid moves
  from quarter units to half units (one world pixel at scale 2), and the generator asserts it. Her body is authored
  25.5 units tall, about 3.2 blocks (halo top about 3.8); hitbox 1.2 by 3.2 as a `getDefaultDimensions` override in
  `SeraphaeEntity` (Resolved 2026-11-05, B-12; no `EntityInit` edit).
- **Body.** Keep the jointed rig the generator already uses (arm, forearm, hand; leg, shin), the gentle face, veil,
  hair, gown panels, robe hem. Hands gain a thumb part and a finger block each (`<side>_thumb`, `<side>_fingers`) so the
  reach can open and the hold can close. Legs hang slightly bent with the feet trailing (she hovers).
- **Wings as branching nervous projections.** Each wing is rebuilt as a nerve tree instead of 18 feathers with six axon
  stubs each (about 126 parts a side today). Per side: a root plexus `<side>_wing` at the shoulder blade carrying the
  wing-root iron; three trunks `<side>_trunk_upper`, `_middle`, `_lower`, each three jointed segments `_0`, `_1`, `_2`
  (upper 7, 6, 5 units; middle 8, 7, 6; lower 7, 6, 5), tapering from 1.0 to 0.5 units thick; two branches at each
  segment end (`_branch_a`, `_branch_b`, 3 to 4 units, 0.5 thick); a tip fan `<side>_trunk_<name>_tip` of four 2-unit
  dendrites that open and close like fingers. The right wing keeps the lore's distorted regrowth: its middle trunk is 3
  units shorter and its lower trunk carries an extra kink segment. Open, asymmetric, with negative space between the
  trunks; a wingspan of about 5.5 blocks at rest and reach to 6 blocks in a lash.
- **Restraints.** Iron at both wrists, both wing roots and the waist, each with an empty locator part
  (`<side>_wrist_shackle`, `<side>_wing_shackle`, `waist_shackle`) whose world position the renderer records for the
  chains, as `wrist()` does today. The hanging wrist chains (seven links) stay, shown only while their restraint is
  slipped; the waist chain stays.
- **Halo.** An open ring of ten bone segments with white light inlays (today's gilt segments go).
- **No gilt metal.** The gilt trims, the gilt halo and the brass-coloured lock faces go; gold on her is only ever nerve
  light. Trims become bone stitching; locks become hematic iron with a bone pin. The rust-orange pixel in the iron
  painter (`(100, 72, 39)`, which reads as copper) goes.

### Texture and palette

| Material | Base atlas `seraphae.png` | White vessel mask `seraphae_vessels.png` (new) | Gold nerve mask `seraphae_nerves.png` (new) |
| --- | --- | --- | --- |
| Skin, face | pale ivory flesh (226, 214, 204), vessel channels a faint pinkish white (238, 222, 218) | the vessel channels white (255, 252, 246); the eyes white | none |
| Nerve trunks, branches, tips | pale nerve sheath (222, 206, 198) with darker myelin bands every 3 px (196, 178, 172) | none | the one-pixel centre line of every trunk, branch and tip (255, 214, 120) |
| Arms | as skin | vessel channels | a nerve line down each arm's inner face |
| Spine | | | a nerve line from nape to waist under the bodice back |
| Vestments | linen (234, 230, 216), bodice (224, 219, 205), hem shadow (130, 120, 101); bone stitching (214, 204, 180) for trims | none | none |
| Hematic iron | (49, 56, 53), edges (105, 113, 99), dark scale (38, 42, 40) | none | none |
| Halo | bone (226, 216, 196) | inlays white | none |
| Hair | pale straw (201, 191, 153), unchanged | none | none |

White never appears in the nerve mask and gold never in the vessel mask; a resource test holds that (section 16).

### Layers

- **New `SeraphaeVesselLayer`** (`client/render/layer/mob/`): `RenderType.eyes(seraphae_vessels)` at full bright,
  white, alpha 0.55 breathing on a 120-tick cycle and rising 0.15 on each presence pulse. During a tell the model
  re-renders one region's subtree at the tell's strength (both arms for the reach, the chest for the Eye, the whole body
  in the Convulsion), through a `renderRegion` method the generator emits.
- **New `SeraphaeNerveLayer`**: the gold mask, drawn per nerve part with that part's own alpha from **new**
  `SeraphaeNerveFlashRules` (pure): a flash front travels a trunk from root to tip over 6 ticks with a 4-tick afterglow;
  idle, one seeded trunk fires every 70 to 110 ticks; at a commit, the moving part's trunk or arm fires; in the
  Convulsion, all at once in three stutters. No shader is added: each part is its own draw with its own colour.
- `SeraphaeWingGlowLayer` is deleted.

### World geometry per move and impact

All in `client/render/world/`, drawn in the shared school batch under `WorldEffectDispatcher`, built from
`LuxUmbraGeometry` primitives (`ribbonPath`, `card`, `ground`, `surface`; shapes `RIBBON`, `WISP`, `FOCUS`, `SLASH`,
`BEAM`, `WHITE_BEAM`, `RIPPLE`, `POOL`, `EYE`, `LUX_SHELL`). **White** is `LuxUmbraBatch.Material.LUX` with tint white,
the hot core `0xFFD2C4` and the clot `0x7A1020` (the Riposte's two accents). **Gold** is only ever
`LuxUmbraBatch.Material.DUCTILIS` with its own shapes (`LINE`, `GLOW`, `CRACKLE`, `SPARK`, as
`NerveCommandGeometry` uses them). The kit's `SaintTelegraphRenderer` draws each footprint's fill in LUX and its commit
edge in DUCTILIS; the classes below add the moves' own layers.

`SeraphaeRadianceGeometry` (kept, rewritten) draws her standing body accents, chains, presence, growth on players,
fragments and anchors. **New** `SeraphaeMoveGeometry` draws moves and impacts from the synced move block and her cues.

| Effect | Geometry (reused pieces in brackets) |
| --- | --- |
| Heart | one white `FOCUS` card at the chest, alpha by phase; nothing detached floats behind her (casting outpouring rule) |
| Chains | per held anchor, the existing `tether` braid from the anchor's crown to its shackle point (shell `BEAM` 0.15, three strands of 0.019 with one clot strand, core `RIBBON`, travelling `FOCUS` knots). Slack chains (well inside the leash) draw only the core ribbon with a sag; the braid shows as they near taut and is brightest when taut. A slipped chain is a short frayed length hanging from her shackle (the model's hanging links show) and a dark anchor crown. A re-binding chain grows from the anchor toward her at its progress, with a gold `SPARK` at the growing tip. |
| Anchor | rendered with the block's own generated model through the block renderer (no entity model), plus today's `anchor` accent (ground `RIPPLE`, crown `FOCUS`, three rising ribbons) |
| Presence ring | section 5 |
| Growth on players | section 5 |
| Laying On of Hands | wind-up: two white `RIBBON` lines on the floor from her palms toward the target, alpha up with the wind-up; the chain-end arc as a `RIPPLE` segment at alpha 0.2 with a gold `SPARK` bead; commit: a gold `LINE` flash runs the lane. Active: a trail ribbon behind each hand from its last poses. **Hold, on the Riposte anatomy:** a translucent shell ribbon (`BEAM`, alpha 0.25) from each palm into the held player's chest, three helical white sinew strands round it tapering 0.07, 0.05, 0.035 with a heartbeat pulse, `FOCUS` knots at palms and sternum, and every 10 ticks a gold `SPARK` impulse running up the held player's arms. |
| Recoil | each chain's alpha spikes and overshoots on a sine for 6 ticks; a white `RIPPLE` at each shackle |
| Nerve Lash, Counter-Lash | the trunk's swept path as a ribbon at the lash height; on the hit frame three parallel `SLASH` streaks (`slashOffsets()`); a gold `CRACKLE` along the trunk at commit |
| Axon Lance | a `WHITE_BEAM` core 0.06 wide along the line, the three forks as `RIBBON`s, gold `LINE` pulses running outward; a small dendrite burst at each ray's end (the existing `feather` axon) |
| Regrowth Knot | a `POOL` of healed pads at alpha 0.3 with a `RIPPLE` rim; each pulse a `RIPPLE` outward and a few short white `SLASH` spurs |
| Transfusion Thread | a single `RIBBON` core 0.018 with a `WISP` shell at 0.2; gold `SPARK` beads travelling toward the target; when the line of sight breaks, the thread frays (widths grow, alpha falls, strands drift up) |
| Wing Grasp | the disc as a `RIPPLE`; gold `CRACKLE` where the tips close; a white `RIPPLE` under the lifted player |
| Convulsion | a `RIPPLE` ring at the wave front with a thin gold `CRACKLE` ring on its leading edge (the line to jump) |
| Chain Wrench | gold `LINE` travelling the chain from her hands to the anchor through the wind-up; the whip as the tether drawn with a lateral sine across its line (amplitude 1.0); on a slip the tether flies loose and the crown `FOCUS` dies in a white flash |
| Fracture, fragments | a vertical white `SLASH` card down her chest, gold sparks in the cracks; fragments as the existing `fragment` nerve knot (its gold impulses moved to DUCTILIS) |
| Binding a fragment | absorption: the cell-hand stream already drawn for `CELL_HAND` plus fine white glow motes drawn into the hand; capture: the existing `CAPTURE` tether |
| Condensing | the existing converging axon orbit and the tether reveal |
| Healed Ground | `POOL` pads at 0.25 with `RIPPLE` rims; at the commit a cluster of white `SLASH` spurs with clot tips erupting from each disc |
| Unclosing Eye | an `EYE` card on her chest growing over the wind-up, a `LUX_SHELL` glow at the source, the beam as `WHITE_BEAM` to the raycast end, a faint `RIPPLE` scorch along the floor sweep; the Gutter as the `EYE` closing to a slit and flickering. White only. |
| Last Reach | the Chain Wrench slip, then the reach's geometry, longer |
| Extraction | every held chain fills: `FOCUS` knots race out along each, then back |
| Radiance Flood | the `LUX_SHELL` dome expanding to the walls at alpha 0.18 |
| Hit outside an opening | a white `FOCUS` card at the hit point shrinking over 8 ticks (the wound closing) |
| Hit inside an opening | a white flash at the hit point and a `FOCUS` knot travelling along the nearest held chain to its anchor |
| Ending | section 11's beats on the same chain, layer and heart pieces |

Particles are accents only: fine `HemoParticleData.glow` motes for traces, `bloodCell` sparingly at bursts; no
`hitGlow`, no vanilla `ParticleTypes` anywhere she is touched (`VanillaParticleRatchetTest`). Overlays stay subtle and
partly clear: no strand wider than the existing tether's 0.019, shells at most 0.28 alpha, and slack chains draw a
single ribbon, so four chains, two nerve trees and a presence ring never read as "spaghetti".

### HUD and frame

`SeraphaeMeterStyle` (section 4). `SeraphaeFrame`: `fillTop` and `fillBottom` change from gold (`0xFFFFE8A0`,
`0xFFC08A30`) to ivory white (`0xFFFFF8EE`, `0xFFD8CFC0`); the cores at each end lose their gold tone (`LUX` to
`0xFFF8F2`); on a phase change the cores flash and a single gold impulse runs the top rail. The chains' sag already
reads integrity correctly (looser when low).

## 13. Animation

Java keyframe tracks in `tools/model_export/seraphae_anim.java`, generated into `SeraphaeModel`, posed from the kit's
synced move id and move tick through `BossReplayClock` (`SeraphaeAnimState`), with Annetta-style Catmull-Rom keys
(K-12). Resolved 2026-11-05 (B-8): no track is absolute. Every frame resets the pose, lays down idle, then blends the
move track in from the last pose over 5 ticks (the outgoing track's final pose is held and cross-faded), so she never
snaps between moves. Beats are in move ticks.

| Track | Beats |
| --- | --- |
| `idle_hover` | 120-tick breath (body bob 0.5 px, chest rise); trunks sway on a delayed wave from root to tip (`nerveWave`); a seeded trunk fires gold every 70 to 110 ticks and twitches 2 degrees as it does; chains sway |
| `glide` | body leans 8 degrees into the motion, gown panels trail, wings drawn back 15 degrees, feet trail |
| `strain` | at the chain's end: body leans 20 degrees toward the target, arms pulled back by their chains, head forward, wings straining forward against their roots |
| `reeled` | body arched back, hands pulled toward their anchors, wings flattened |
| `laying_on_of_hands` | 0 to 16 palms turn out and rise to chest height, head lifts, wings draw back; 16 to 24 arms extend, fingers open; 20 gold down the arms; 24 to 34 the glide, arms forward and fingers spread; on a catch, hands close on the shoulders and chest, head tilts, wings fold round the held player (`hold`); on a whiff at the chain's end, `recoil` |
| `hold` | a slow rocking 40-tick cycle, head tilted toward the patient, wing tips curling in; gold fires down her arms every 10 ticks with the transfusion |
| `recoil` | 0 to 4 jerked back half a block, arms flung behind, wings forward and open (overshoot); 4 to 40 hanging open, chest forward, slowly gathering |
| `seized` | 0 to 6 dragged toward the anchor; then kneeling in the air, the held arm or wing stretched back to it |
| `nerve_lash` | 0 to 10 the near wing's lower trunk drops and draws back, segments lagging; 10 to 14 gold gathers at the root; 14 to 20 the whip, each segment 2 ticks behind the last; 20 to 36 the trunk recoils and settles |
| `counter_lash` | 0 to 12 the far wing rises and arches over her head; 12 to 18 the overhead whip; 18 to 40 settle |
| `axon_lance` | 0 to 18 middle trunk swings forward to point, tip fan opens; 18 to 22 gold races out; 22 to 30 the trunk locks rigid; 30 to 48 return |
| `regrowth_knot` | 0 to 30 hands cup low together, head bowed; 30 to 90 hands rise on each pulse at 30, 50 and 70; 90 to 104 settle |
| `transfusion_thread` | 0 to 20 one hand lifts toward the target; 20 to 100 the hand holds, fingers curling slowly as she draws; 100 to 116 lowers |
| `wing_grasp` | 0 to 16 a wing reaches round, tip fans open; 16 to 24 they close like fingers; on a catch the wing lifts the player and swings them round to set them down at 3 blocks in front |
| `convulsion` | 0 to 22 rigid lock with three gold stutters at 6, 13 and 20, body jerking 3 degrees each; 22 to 34 the seizure (every joint jittering 4 to 6 degrees on seeded frequencies); 34 to 64 sagging, head hanging, slowly recovering |
| `chain_wrench` | 0 to 10 hands find the chain; 10 to 24 both hands grip and draw back, body twisting; 24 to 34 the wrench, whole body throwing back; 34 to 54 settle |
| `fracture` | 0 to 20 wings tear open wide, head thrown back; 20 to 30 chest arches; 30 the burst; 30 to 42 open; 42 to 62 wings sag, head bows |
| `condensing` | 0 to 10 wings fold, hands cross at the wrists over the chest; 10 to 70 held, a slow tremor, head bowed |
| `healed_ground` | 0 to 28 hands open downward, palms to the floor, wings lifting; 28 the spurs; 28 to 52 hands rise back |
| `unclosing_eye` | 0 to 26 head tips back, chest opens forward, arms spread low; 26 to 66 the body turns slowly through the sweep; 66 to 116 the gutter bow |
| `straining_reach` | 0 to 12 she seizes one chain and tears it (the `chain_wrench` throw, faster); 12 to 28 the reach's wind-up, longer and lower; then as the Laying On of Hands |
| `extraction` | 0 to 30 she grips both wrist chains and pulls them to her chest, wings arching; 30 to 50 two pulses through the whole body; 50 to 76 settle |
| `radiance_flood` | 0 to 40 arms and wings flung wide, head back, every nerve firing; 40 the flood; 40 to 80 drooping |
| `phase_restraint` | the 50-tick card: she looks at her wrists for the first time as the chains flare and shorten |
| `phase_escaping_light` | the 50-tick card: her chest light opens briefly, head back, then she looks at the player |
| `rise` | `SaintRise`'s sink and surface kept; the wings unfurl from 30 to 60, nerve tips opening last |
| `ending` | section 11's beats: 0 to 20 every restraint pulled tight; 20 to 60 reeled back, wings curling inward, hands crossing at the wrists, head bowing; 60 to 90 still; 90 to 100 sinking |

## 14. Sound

No new audio files (umbrella non-goal). Mod events from `common/init/SoundInit`, vanilla events at chosen pitches.

| Beat | Sound |
| --- | --- |
| Ambient | `MAUSOLEUM_SERAPHAE_CHOIR` continues from the theme; her ambient `AMETHYST_BLOCK_CHIME` at pitch 0.7, sparse |
| Presence pulse | none on heal; on a growth gain `AMETHYST_BLOCK_RESONATE` at 1.6, quiet, to that player only |
| Growth burst | `AMETHYST_CLUSTER_BREAK` 0.5 and `BONE_BLOCK_BREAK` 0.7 |
| Laying On of Hands | wind-up `AMETHYST_BLOCK_RESONATE` rising 0.6 to 1.2; commit `ENTITY_STAINED_PRIESTESS_FLARE` 1.6; each hold pulse `RITE_AMBIENT_HEARTBEAT` at 1.4, quickening; release `ENTITY_VESPER_GRAB_RELEASE` at 1.5 |
| Recoil | `CHAIN_HIT` 0.6 and `RITE_ANCHOR_FILL` 0.8 |
| Nerve Lash, Counter-Lash | wind-up `AMETHYST_BLOCK_CHIME` 1.4; strike `PLAYER_ATTACK_SWEEP` 1.3 with `ENTITY_STAINED_PRIESTESS_FLARE` 1.8 |
| Axon Lance | wind-up `BEACON_POWER_SELECT` 1.6; fire `TRIDENT_THROW` 1.4 |
| Regrowth Knot | each pulse `AMETHYST_BLOCK_RESONATE` 0.8 |
| Transfusion Thread | `BEACON_AMBIENT` 1.8 while held; break `AMETHYST_BLOCK_BREAK` 1.5 |
| Wing Grasp | close `CHAIN_PLACE` 1.2 |
| Convulsion | three `SCULK_CLICKING` stutters at 1.8; the ring `BEACON_DEACTIVATE` 1.6 with `AMETHYST_CLUSTER_BREAK` 0.8 |
| Chain Wrench | grip `CHAIN_PLACE` 0.5; whip `CHAIN_BREAK` 0.6; slip `RITE_ANCHOR_BREAK` 1.0 |
| Anchor binding | each 10 fed ticks `MAUSOLEUM_SERAPHAE_ANCHOR_TICK`; `RITE_ANCHOR_FILL` rising 0.8 to 1.4 with progress; done `RITE_ANCHOR_REPAIR` 1.0 and `CHAIN_PLACE` 0.8 |
| Reserve anchor woken | `RITE_SIGIL_AWAKEN` 1.0 and `RESPAWN_ANCHOR_CHARGE` 1.3 (today's wake sound) |
| Fracture | `GLASS_BREAK` 0.6 (today's) and `AMETHYST_CLUSTER_BREAK` 0.6; fragments chime `AMETHYST_BLOCK_CHIME` 1.6 |
| Fragment bound or captured | `RITE_SEGMENT_SEAL` 1.4 |
| Condensing | `BEACON_DEACTIVATE` 0.8 (today's) |
| Unclosing Eye | wind-up `BEACON_ACTIVATE` 0.7; beam `BEACON_AMBIENT` 1.0; gutter `BEACON_DEACTIVATE` 0.6 |
| Healed Ground | spurs `BONE_BLOCK_PLACE` 0.6 |
| Radiance Flood | `BEACON_POWER_SELECT` 0.5 through the tell; flood `RITE_BACKLASH` 0.8 |
| Phase II, III cards | `RITE_ESCALATE` 0.9; `RITE_CULMINATION` 0.8 |
| Ending | four `RITE_ANCHOR_FILL` at 0.8, 0.9, 1.0, 1.1 as the anchors flare; `RITE_SEAL` 0.9 as the tomb closes; a last `BEACON_DEACTIVATE` 0.5 |
| Hurt sound | `AMETHYST_CLUSTER_BREAK` (today's) on opening hits only; a hit that closes plays `AMETHYST_BLOCK_HIT` 1.4 |

**Her voice.** She speaks a few lines as a nurse would: short, warm, present tense, at most eight words, never naming
her keepers, never cruel; the only question is "Where does it hurt?". Lang keys `message.hemomancy.seraphae.voice.*`,
spoken in chat to challengers in the chamber, not bold: rise "Hold still. Let me see."; reach wind-up, seeded, "Let me
see the wound.", "Don't pull away.", "Where does it hurt?"; a hold "There. It's closing."; a burst on a held player "It
keeps going. I can't stop it."; a Recoil "My hands. They've bound my hands."; phase II "Let me go to them."; phase III
"It's all coming out of me."; the flood "I can't hold it."; the ending's two lines (section 11). Each voice line at most
once every 200 ticks. **Teaching lines** (`message.hemomancy.seraphae.teach.*`, grey italic, action bar, once per fight
per player): the first Recoil "The chains stopped her. Strike while she is drawn back."; the first growth (the existing
`growth_warning`); the first hit that closes "The wound closes. Only an opening holds."; the first slip "An anchor let
go. Hold Blood Projection on it."; the first Fracture "Her light breaks loose. Draw it in, or lead it to an anchor.";
Condensing "She draws herself in. Strike, and feed the anchors."; the first hold "Her hands hold you. Draw your blood
back out of her."; the first Gutter "The eye gutters. Strike now." Death messages under `death.attack.hemomancy.saint_seraphae`
("%1$s was healed past bearing by Saint Seraphae", and the `.player` form).

## 15. Multiplayer and both paths

**One to four challengers** (players in the chamber box, counted by the arena). Her meter scales; per-player growth
does not. Decay rises by half per extra challenger; every opening's hit cap rises by one; Fracture sheds one more
fragment each; Healed Ground lays one disc per player. She holds one player at a time, and two hits from allies break a
hold (a 20-tick Recoil), so a held friend is a cue to strike. Chain Wrench is a co-op beat: one player holding
projection on the anchor she reaches for turns her wrench into a Seized opening for everyone. Her target is the most
hurt challenger, so the wounded draw her and the whole are the ones her presence grows; a group learns to rotate who
stands close. Reserve anchors are a natural split: one player wakes a reserve in the aisle while the others keep her on
the floor.

**Silentium** (usually a proper awakening, through the ring door with every node lit). No deeds; hallowed ground
tolerates them. They fight with everything the fight asks for: held Blood Absorption (fragments, breaking a hold), held
Blood Projection (anchors, Condensing), school casts (bonus gains, shedding growth; Congeatio, Tenebris and Mortem count
more), and movement (the chain's end, the low and high lashes, the rings).

**Apotheos** (usually forced, all three deeds, and burned by hallowed ground: 4 spore load a second, then 1
`hallowed_burn` a second). The twist is hers: the ground keeps an Apotheos wounded, and her presence heals a wounded
player up to 1 a second (1.4 with the Repose deed's cadence), so inside her 9 blocks the burn is roughly offset and
growth rarely builds, while outside it they burn. A forced Apotheos therefore fights close, under her hands, with hidden
reach tells (Beholding), the Extraction (Downfall) and shorter openings (Repose): harder, not longer. It stays winnable:
the integrity numbers are the same, every opening still exists, the hold heals a wounded Apotheos before it grows them,
and the burn sheds their growth. The GameTest in section 16 runs a forced all-deeds fight with a scripted Apotheos to
make sure the path can finish.

## 16. Tests

**JVM rules tests** (`src/test/java/com/vincenthuto/hemomancy/common/entity/boss/saint/seraphae/`, pure, no Minecraft
types, reading through `support/` helpers where they read files):

| Test | Holds |
| --- | --- |
| `SeraphaeIntegrityRulesTest` (new) | every gain and loss in section 4; school weighting; per-opening caps with challengers; decay scaling; ratchet phases at 40 and 75; overrun resets to 10 once per fall; win checked before decay |
| `SeraphaeLeashRulesTest` (new) | L by integrity, slips and reserves, the floor of 4; the reach lane's end point; reeling speed and the no-pick rule |
| `SeraphaeGrowthRulesTest` (rewritten) | heal first, then growth; HUG doubles; stages and burst; every shedding source; decay outside the radius; Repose cadence |
| `SeraphaeSelectionRulesTest` (new) | weights by band, sector and phase; ineligible moves; the first move after the rise; anti-repeat; strings one link deep; kite, hug and turtle pressure; Chain Wrench and Fracture and Last Reach eligibility; most-hurt targeting; no move eligible on a timer alone |
| `SeraphaeMoveTest` (new) | every move's timeline meets the floors (12 melee wind-up, 20 area); every area move's footprint shows from tick 0; ids unique; each phase adds a move; at least eight moves and two strings |
| `SeraphaeAnchorBindingRulesTest` (new) | fed ticks, grace, half-speed bleed, rebind and reserve completion, blood per tick, held-at-commit |
| `SeraphaeDeedsTest` (new) | each skipped Chapel's rules; all three together; recovery floors |
| `SeraphaeEndingRulesTest` (new) | beat order, `endTrial` at 100 once, resume from any saved tick |
| `SeraphaeNerveFlashRulesTest` (new) | flash front timing along segments; idle firing interval; Convulsion stutters |
| `SeraphaeVisualRulesTest`, `SeraphaeAnchorRulesTest` (updated) | `nerveWave`, `burstOpacity`; reserve selection beyond reach 8 |
| `client/model/entity/boss/seraphae/SeraphaeModelTest` (updated) | half-unit grid; `kit.SCALE × 16 / MODEL_SCALE == 16`; three trunks of three segments a side with branches and tips; the right wing's distortion; shackle locator parts exist and follow their limbs; hanging chains stay vertical; Blockbench UV and cube-count checks kept |
| `client/render/world/SeraphaeRadianceGeometryTest` (updated) | slack, taut, slipped and re-binding chains; strand widths and shell alphas under the subtlety budget |
| `SeraphaeTexturePaletteTest` (new) | the vessel mask is near-white only (saturation under 0.12), the nerve mask gold only (hue 35 to 50 degrees), no copper-hued iron texel in the base atlas, no gilt material left in the generator |

**Architecture guardrail** (`src/test/java/com/vincenthuto/hemomancy/architecture/SeraphaePresentationGuardrailTest.java`,
extended): no vanilla `ParticleTypes` and no `hitGlow` in any Seraphae file; gold colours only on the DUCTILIS material
in her geometry classes; her geometry hooked before the generic branch; animation read through `BossReplayClock` only.

**GameTests**, new `src/gameTest/java/com/vincenthuto/hemomancy/gametest/SeraphaeFightGameTests.java`,
`@GameTestHolder("seraphae_fight_validation")`, on a chamber fixture (the mausoleum chamber template or a built 37 by 27
copy) with real fake players:

1. `firstMoveIsTheReachAtTheMostHurt`: after the rise, her first move is the Laying On of Hands at the most hurt player.
2. `reachStoppedByTheChainOpensRecoil`: a player past the chain's end; the reach stops short; Recoil opens; hits add +2
   up to the cap; hits outside add nothing and the wound closes.
3. `holdHealsThenGrowsThenBursts`: a hurt player is healed to full, then grows, bursts at 10 (integrity −3); a held
   Blood Absorption of 20 ticks breaks the hold.
4. `chainWrenchSlipsUnlessProjected`: with and without a projecting player at the commit tick; the slip's −6 and chain
   +3; a rebind pays 240 blood through `BloodAccounting` with the `seraphae_anchor_binding` source and gives +8 and
   Seized.
5. `reserveAnchorWakesAndRestores`: 320 blood, +10, chain −2; the aisle anchor stands again after the trial.
6. `fractureFragmentsAndCondensing`: absorption +3, anchor capture +5, struck +1.5, graft −2, return −4; Condensing opens
   once the last resolves; projection feed during it.
7. `presenceHealsTheWoundedAndGrowsTheWhole`: heal 1 per pulse when hurt; growth when whole; shedding by damage.
8. `phasesRatchetAndCardsPlayOnce`: integrity crosses 40 and 75 and falls back; phases kept; each card scene sent once.
9. `overrunFloodsOnceAndResets`.
10. `containmentEndingRunsOnce`: at 100: the ending beats, `endTrial` once, the tomb dormant, anchors restored as
    blocks, radiant stone restored, one loot roll, fragment-only when forced.
11. `unloadMidFightAndMidEndingRestores`: the arena puts everything back; a reloaded ended Saint finishes.
12. `fourChallengersScale`: decay, caps, fragment count; one hold at a time; ally hits break a hold.
13. `forcedDeedsApply`: each skipped Chapel's rule; an Apotheos with all three deeds and hallowed burn can raise
    integrity to 100 with a scripted routine.
14. `emptyChamberResetsUnrewarded`: the kit leash; Seraphae's anchors and radiance restored.

**Review client** (`./gradlew.bat runBossPresentationReviewClient -PseraphaeFightReview=true`,
`SeraphaeFightClientReview` rewritten): a staged script in the mausoleum chamber with a real server player and a
spectator camera, a still at each named stop instead of every 16 ticks: the rise; idle and glide; strain at the chain's
end; each move at its wind-up midpoint, commit tick and active midpoint; the hold (heal, growth, burst); Recoil; Chain
Wrench and the slip; a rebind at 50% and done; a reserve wake; Fracture and fragments; a fragment drawn in and one
captured; Condensing; Healed Ground; the Unclosing Eye and the Gutter; Last Reach; the Radiance Flood; both phase cards;
the radiance spread at phase II and III; the ending at ticks 10, 40, 75, 95 and 130; the HUD meter in each restraint
state and at growth 0, 4, 8 and 10; a turntable of the new model in idle and in the wing-spread pose. Shots land in the
game dir with the stop name, and the report in `operator/seraphae-fight-review.json` lists any stop that failed to reach
its beat.

## 17. Build list

Inside Seraphae's lane as the umbrella defines it; Gradle only in the serialised stages after the writers.

1. **Pure rules.** In `common/entity/boss/saint/seraphae/`: new `SeraphaeMove` (enum on the kit's `SaintMove`, every
   timeline, footprint and hook declaration from section 7), `SeraphaeSelectionRules`, `SeraphaeLeashRules`,
   `SeraphaeIntegrityRules`, `SeraphaeAnchorBindingRules`, `SeraphaeDeeds`, `SeraphaeEndingRules`,
   `SeraphaeNerveFlashRules`, `SeraphaeRestraint` (enum: left wing, left wrist, right wrist, right wing, waist); rewrite
   `SeraphaeGrowthRules`; their JVM tests (section 16).
2. **The entity on the kit.** Rewrite `SeraphaeEntity`: kit controller with `SeraphaeGlide` (new), meter (integrity,
   bar from meter), `SeraphaePresence` (new), openings (`SeraphaeOpenings`, new), phase table (`SeraphaePhases`, new),
   arena use, deeds, the `hurt` touch rule, the Radiance Flood, the ending, save and load of all of it. Delete
   `SeraphaeState`, the state machine, `registerGoals`, `onArenaOverrun`, `updateBossBarColor`, the bold broadcast, the
   client tick. New `SeraphaeLines` holds the voice and teaching keys and their once-per rules.
3. **Anchors.** Rework `ContainmentAnchorEntity` (restraint slot, kind binding or reserve, held, slipped or binding
   state synced, projection feed through the kit's channel target, K-1, or the lane workaround below), its capture
   limited to held anchors; block cells handed to the arena; extend `SeraphaeAnchorRules` with reserve selection.
4. **Fragments.** Rework `SeraphaeFragmentEntity` (no goals, no push; seek glide, graft, 300-tick return, absorption
   binding progress).
5. **The hold.** New `SeraphaeHold` in her package (the kit's `BossHold`, K-2, when it lands; until then a server-side
   pin: zero motion and a set position every tick, held player's yaw toward her).
6. **Radiance spread.** New `SeraphaeRadianceSpread`, choosing cells and calling the arena's swap and restore (K-8).
7. **Effects.** Rewrite `SeraphaeEffects` (cues for every move, impact, chain and ending beat; motes only; no
   `hitGlow`); rewrite `SeraphaeVisualRules` (`MODEL_SCALE` 2.0, cue kinds, `nerveWave`, `burstOpacity`).
8. **Model and textures.** Rewrite `tools/model_export/seraphae.py` (half grid, nerve-tree wings, hands, shackle
   locators, bone halo, no gilt, three atlases) and `tools/model_export/seraphae_anim.java` (every track in section 13,
   `renderRegion`, nerve-part rendering, shackle points); run the generator for `SeraphaeModel.java`, `seraphae.png`,
   `seraphae_vessels.png`, `seraphae_nerves.png`, `SeraphaeModel.bbmodel`, `build/seraphae_preview.png`.
9. **Client.** Rewrite `SeraphaeAnimState` (move id and tick replay, shackle and nerve-tip points); `SeraphaeRenderer`
   (scale 2.0, new layers); new `SeraphaeVesselLayer` and `SeraphaeNerveLayer` in `client/render/layer/mob/`, delete
   `SeraphaeWingGlowLayer`; rewrite `ContainmentAnchorRenderer` to draw the block model and delete
   `ContainmentAnchorModel`; make `SeraphaeFragmentRenderer` draw nothing itself and delete `SeraphaeFragmentModel`.
10. **World geometry.** Rewrite `client/render/world/SeraphaeRadianceGeometry.java`; new
    `client/render/world/SeraphaeMoveGeometry.java`; gold moved to DUCTILIS.
11. **HUD.** New `SeraphaeMeterStyle` (kit `SaintMeterStyle`) in `client/screen/overlay/boss/frame/` beside her frame;
    recolour `SeraphaeFrame`.
12. **Shared add-only lines.** `en_us.json` keys under `message.hemomancy.seraphae.voice.*`, `.teach.*`,
    `boss.hemomancy.seraphae.phase.*`, `scene.hemomancy.seraphae_*`, `death.attack.hemomancy.saint_seraphae*`; damage
    type JSON `data/hemomancy/damage_type/saint_seraphae.json` and its tags; `BossPresentationIds` and
    `BossSceneCatalog`: `SCENE_SERAPHAE_CHAINS_TIGHTEN_CARD`, `SCENE_SERAPHAE_LIGHT_ESCAPES_CARD`,
    `SCENE_SERAPHAE_CONTAINED`.
13. **Tests.** `SeraphaeFightGameTests` (`seraphae_fight_validation`), the JVM tests, the extended
    `SeraphaePresentationGuardrailTest`, `SeraphaeTexturePaletteTest`; rewrite `SeraphaeFightClientReview`.
14. **Spec upkeep.** Record every deviation in this spec's "Kit follow-ups" (section 18).

**Lane workaround for K-1: not built (Resolved 2026-11-05, D-8).** The kit's `SaintChannelRouter` hands her every held
Blood Absorption and Projection aimed at the fight through `SeraphaeEntity.onChannel`; she pays only through
`SaintChannelRouter.pay` with her three named sources.

## 18. Kit requests

| # | Request | Reason |
| --- | --- | --- |
| K-1 | **`SaintChannelTarget`** (generic: `common/entity/boss/BossChannelTarget`): an entity interface with `takeAbsorption(player, progress)` and `takeProjection(player, rate, livingStaff)`, called by `BloodAbsorptionItem.onUseTick` and `BloodProjectionItem.performProjection` (and the Living Staff's modes) before their block and living-target paths, as `VesperBloodAbsorptionInteractions` is today. A Saint that is one also refuses the ordinary living-target drain. | Her anchors take held projection, her fragments and her hold take held absorption, and a player must not draw blood out of a Saint's body. Other Saints will want the same; Vesper could move onto it later. |
| K-2 | **`BossHold`** (`common/entity/boss/`): pin a player to a moving point for N ticks with the client input lock, extracted from `MycophantHoldKind` and `client/event/MycophantHoldClientEvents`, with break rules supplied by the caller | The Laying On of Hands, Last Reach and Wing Grasp hold players. The lane's server pin jitters on the client. |
| K-3 | `BossFootprint` additions: a **composite** (union) footprint; a **line between two moving points** re-read each tick; an **occluded** option (block raycast from origin to the body centre); a **NONE** footprint for target tethers | Axon Lance's fork, Chain Wrench and Extraction along chains, the Unclosing Eye behind pillars, the Transfusion Thread. |
| K-4 | `SaintPhaseTable`: a **ratchet** flag for meter phases | Her phases must not flip back when integrity dips. |
| K-5 | `SaintMeter`: a Saint can declare that its **bar shows the meter**, not health | Her bar is containment integrity (user ruling). |
| K-6 | `SaintMeterStyle.paint` receives the **Saint entity** (to read synced restraint states), and `SaintMeterPacket` can carry a **per-player secondary value** for a Saint whose main meter is its own | The restraints row and the player's own growth line. |
| K-7 | `SaintOpening`: per-hit gains keyed by **delivery** (melee or projectile against school cast) with **per-school multipliers**, per-opening **hit caps** that grow with challengers, and the hit's school supplied from the school-combat hook (`SchoolCombatEvents.commit` or `SchoolHitContext`) | Her integrity gains and the stilling schools. |
| K-8 | `SaintArena`: a scan by **block predicate** beyond the named set pieces, **batch swaps** with recorded originals, and an **ordered restore** over N ticks (outermost first) | The radiance spread and its "return to darkness". Anchor cells also move under the arena. |
| K-9 | `SaintPresence`: per-player state maps registered with `TransientServerStateEvents` by the kit, cleared at the trial's end; a **cadence override** for deeds | Growth; the Repose deed. |
| K-10 | One damage type per Saint, `hemomancy:saint_seraphae`, with death message keys | Section 7. |
| K-11 | `SaintCombatController`: a **target-choice hook** (hers: most hurt) and a locomotion **veto on picking** (while she is being reeled) | Section 8. |
| K-12 | Promote Annetta's private Catmull-Rom `curve` and `window` (`AnnettaKnowlesModel`) to `common/util/HemoEasing` | Four Saints' tracks would otherwise copy them, which `EasingCopyGuardrailTest` exists to stop. |

**Kit follow-ups for the integration stage** (shared files a Saint builder may not edit):
- ~~`EntityInit`: `seraphae` sized 0.9 by 2.4 becomes 1.2 by 3.2.~~ Resolved 2026-11-05 (B-12): her own
  `getDefaultDimensions` override.
- `client/event/LayerEvents`: drop the `SeraphaeFragmentModel` and `ContainmentAnchorModel` layer definitions.
- `SaintPowerGameTests`: retire `seraphaeBothSecondaryAttacksRespectWindupAndRecovery` and
  `seraphaeHealsThenOvergrowsWithoutChangingContainmentVictory` (both test the deleted cycle).
- `SaintPatternGeometry`: drop the `SERAPHAE_RELIQUARY` branch when the kit removes `SaintAttackPattern`.
- `en_us.json`: remove the eight retired `message.hemomancy.seraphae.*` state lines.
- `SeraphaeEffects` cue kinds that `ManipulationVisualRenderer` dispatches to `SeraphaeMoveGeometry`: one added hook line
  beside the existing `SeraphaeRadianceGeometry` hook.

## 19. Questions for the user

| Question | My default |
| --- | --- |
| At 16 px per block she grows to about 3.2 blocks tall (3.8 with the halo, wingspan about 5.5) at render scale 2.0. Is that size right, or should she stay near today's 2.4 blocks (a smaller rig, coarser detail)? | 3.2 blocks at render scale 2.0. **Resolved 2026-11-05: yes.** |
| Should she speak a handful of short nurse's lines (section 14), or stay silent with only the grey teaching lines? | She speaks. **Resolved 2026-11-05: she speaks.** |
| May the fight temporarily turn the chamber's healed stone into the glowing `radiant_healed_quartz` as she loses control, restored when she is contained? | Yes. **Resolved 2026-11-05: yes, through `SaintArena`.** |
| Gold only ever as nerve light: her gilt halo and trims become bone, her locks hematic iron. Agreed? | Yes. **Resolved 2026-11-05: yes.** |
| Hits outside an opening add nothing to integrity (and give a whole striker standing close a point of growth). Or should ordinary hits add a little? | Nothing outside openings. **Resolved 2026-11-05: nothing.** |

## 20. Calls made

- Phases by integrity at 40 and 75, ratcheted; starting integrity 30 on both awakenings (forced is harder through deeds
  and speed, not longer); every number in sections 4 to 8.
- Her health never matters and never changes; the bar is integrity; hits outside openings close in white and add
  nothing.
- The colour grammar: white heals and so grows; gold is a nerve firing and so the strike. White on the LUX material and
  the vessel mask; gold on the DUCTILIS material and the nerve mask; never mixed. Gold metal removed from her body.
- The chain as the leash, its length set by integrity, slips and reserves; the leash centre on the binding anchors'
  centroid.
- Her target is the most hurt challenger; her first move after the rise is always the reach.
- The hold heals before it grows; three ways to break it, two of them Hemomancy verbs (held absorption, a stilling
  cast).
- Four binding anchors hold four restraints (wing roots and wrists); the two aisle-end anchors become reserves that
  chain to her waist.
- Growth stages, burst damage and the four shedding sources; outside-radius decay.
- Fragments seek players and graft onto them; held absorption binds them; held anchors capture them.
- The stilling schools (Congeatio, Tenebris, Mortem) count more in openings and her own (Lux, Ductilis) count less.
- Fourteen moves, the strings, weights, cooldowns and pressure rules; the Radiance Flood replacing the overrun.
- The three deeds (Unwatched Hands, The Extraction, No Rest Given); health and Resistance floors left as no-ops on her.
- The radiance spread through `radiant_healed_quartz` and its restore wave; no chamber edit.
- The ending's 140 ticks, its scene and the victor rule.
- Render scale 2.0, the half-unit grid, the nerve-tree wing anatomy, the right wing's distortion, two emissive masks
  and two layers, per-part nerve flashes without a new shader.
- A woken anchor renders the block's own model; the fragment draws only geometry.
- Every sound pick; her voice and its lines; the teaching lines.
- The GameTest namespace `seraphae_fight_validation` and its cases; the review stops.
- The lane workaround for held absorption and projection until K-1.

## Build notes 2026-11-05

Built by the Seraphae builder of run 2, stage 2 (no Gradle in this stage: nothing here has been compiled or run; the
compile gate and the Seraphae verify stage prove it). The model generator was run and its outputs are in the tree.

### What is built

- **On the kit.** `SeraphaeEntity` is rewritten on `SaintTrialEntity`'s kit: `createKit()` returns her moves, the
  `SeraphaeGlide` locomotion, the integrity meter (`SaintMeter.Owner.SAINT`, an inner `IntegrityRule`: 0 to 100 from
  30, decay by `SeraphaeIntegrityRules.decay`, thresholds 0.40, 0.75 and 1.0), `SeraphaePresence`, the ratcheted
  `SeraphaePhases` table, `SaintDamageTypes.SERAPHAE`, the 0.5 challenger scaling and `SeraphaeEndingRules.SPEC`. She
  registers no goals, never calls `tickRise`, and uses none of `SaintSecondaryAttacks`, `SaintAttackPattern`,
  `setVisualState` or `sendFloorRing`. Her bar is `meter().fraction()`; `damageTakenFactor` is 0, so her health never
  changes. Her hitbox is a `getDefaultDimensions` override (1.2 by 3.2).
- **Pure rules (all JVM-tested):** `SeraphaeIntegrityRules`, `SeraphaeLeashRules`, `SeraphaeGrowthRules` (rewritten),
  `SeraphaeSelectionRules`, `SeraphaeAnchorBindingRules` (and the three named sources), `SeraphaeDeeds`,
  `SeraphaeEndingRules`, `SeraphaeNerveFlashRules`, `SeraphaeRestraintState`, `SeraphaeOpenings`, plus the
  `SeraphaeRestraint` enum. `SeraphaeAnchorRules` gains reserve selection.
- **Moves.** `SeraphaeMove` (17 constants: the 14 of section 7 with the resolutions, the Axon mirror and the wide
  Convulsion as follow-up-only constants, and the Radiance Flood as a Saint-started move). Timelines, footprints and
  telegraphs read only `SaintMoveInputs`: the reaches carry their lane length in quarter blocks (and a hasty bit for
  the thrown follow-up) in the variant, the lashes and the lance their wing, the wrench its anchor slot, the Eye its
  sweep direction. Every move's effect is `SeraphaeEntity.moveTick` / `moveHit`.
- **Openings, presence, hold, anchors, fragments.** All four openings; the hold through the kit's `BossHold` with the
  three break rules (20 ticks of held absorption on her, a Congeatio or Tenebris cast, two ally hits) and its 70%
  Nerve Lash on release; growth stored as the meter's per-player secondary with the stage flags; Knotting as a held
  jump-strength modifier; `ContainmentAnchorEntity` reworked (binding or reserve, held, slipped or binding, fed through
  the router, captures fragments when held, never edits a block); `SeraphaeFragmentEntity` reworked (no goals, a seeking
  glide, graft, 300-tick return, drawn in by held absorption, struck apart by a blow; no `die` override).
- **Channels.** `onChannel` answers held Projection into an anchor or a dormant reserve, and held Absorption on a
  fragment or on her body (counts toward breaking a hold; otherwise refused with a cue). Blood is taken only through
  `SaintChannelRouter.pay` with `seraphae_anchor_binding`, `seraphae_reserve_wake` or `seraphae_condensing_feed`, at
  `min(4, item rate)` a tick.
- **Arena.** Binding anchors wake through `arena.clearColumn` and `arena.own`; reserves wake the same way when fed;
  the radiance spread turns healed stone through `arena.set`; the ending's restore runs walls first over 20 ticks. A
  reload re-wakes her anchors in their saved states (slipped stays slipped), re-wakes woken reserves and re-spreads the
  radiance; a stale anchor or fragment that loads late discards itself (`SeraphaeEntity.knows`).
- **Ending.** 140 ticks from full integrity, scene `SCENE_SERAPHAE_CONTAINED` (new `SeraphaeContainedScene`), anchors
  flaring one by one, growth cleared at tick 20, reeled to over the tomb by tick 60, both lines, anchors and fragments
  let go at tick 100 just before the kit's `endTrial`, discarded at 140; the victor is the meter's last contributor.
- **Model and textures.** `tools/model_export/seraphae.py` rewritten (half-unit grid asserted, nerve-tree wings with
  the right wing's distortion, thumbs and finger blocks, shackle locator parts, bone halo and trims, iron locks with a
  bone pin, no gilt, no copper texel) and run: `SeraphaeModel.java` (160 joints, 279 cubes), `seraphae.png`,
  `seraphae_vessels.png`, `seraphae_nerves.png`, `SeraphaeModel.bbmodel`, `build/seraphae_preview.png`.
- **Animation.** `seraphae_anim.java` holds every track of section 13 as Catmull-Rom keys on the kit's move clock
  (`SaintMoveView.moveTime`), the openings, the glide states, the hold, the phase-card looks, the rise and the ending,
  blended from the last pose over 5 ticks through the new `SeraphaePose` channels and `SeraphaeAnimState.blend`; the
  hanging wrist chains show only while their iron is slipped.
- **Client.** `SeraphaeRenderer` (scale 2.0, sinks in the ending, records iron, hand, heart and tip points),
  `SeraphaeVesselLayer` (white, with the reach, Eye and Convulsion tells) and `SeraphaeNerveLayer` (gold per nerve part
  from the flash rules), `ContainmentAnchorRenderer` drawing the block model, `SeraphaeFragmentRenderer` drawing
  nothing; `SeraphaeRadianceGeometry` rewritten (heart, presence ring, chains slack to taut, frayed and re-binding
  chains, anchors, the hold's sinew, growth on players, fragments, every cue kind); new `SeraphaeMoveGeometry`;
  `SeraphaeTelegraphStyle` (Lux fill, Ductilis edge with the 4-tick pre-commit flare, healed pads);
  `SeraphaeMeterStyle` (restraints row, opening impulse, growth line, 16 rows); `SeraphaeFrame` recoloured with the
  phase flash.
- **Shared add-only lines.** 33 `en_us.json` keys (phase names, three cards, twelve voice lines, nine lessons, three
  blood sources); `BossPresentationIds` `SCENE_SERAPHAE_CHAINS_TIGHTEN_CARD`, `SCENE_SERAPHAE_LIGHT_ESCAPES_CARD`,
  `SCENE_SERAPHAE_CONTAINED`; their `BossSceneCatalog` registrations.
- **Tests.** JVM: `SeraphaeIntegrityRulesTest`, `SeraphaeLeashRulesTest`, `SeraphaeGrowthRulesTest`,
  `SeraphaeSelectionRulesTest`, `SeraphaeMoveTest`, `SeraphaeAnchorBindingRulesTest`, `SeraphaeDeedsTest`,
  `SeraphaeEndingRulesTest`, `SeraphaeNerveFlashRulesTest`, `SeraphaeRestraintStateTest`, `SeraphaeTexturePaletteTest`;
  rewritten `SeraphaeModelTest`, `SeraphaeRadianceGeometryTest`, `SeraphaeVisualRulesTest`; extended
  `SeraphaeAnchorRulesTest` and `SeraphaePresentationGuardrailTest`. GameTests: `SeraphaeFightGameTests`
  (`seraphae_fight_validation`, 8 cases on the empty template with the fallback arena) and its `empty.nbt`. Review:
  `SeraphaeFightClientReview` rewritten as a staged stop list (every move at wind-up, commit and active, the hold,
  Recoil, slip, both phases, Fracture, Condensing, the flood, the ending at 10, 40, 75, 95 and 130).
- **Removed** (copied first to `removed_assets/` keeping their paths): `SeraphaeState.java`,
  `SeraphaeWingGlowLayer.java`, `seraphae_glow.png`.

### Deviations from this design

- The Axon Lance's fork is three rays fanned 7 degrees from her feet that part about 10 blocks out (the kit's `Lane`
  cannot start a lane away from the origin).
- The Counter-Lash's "crouch under it" is read in the move's hit (a crouching target is passed over), because the
  kit's height bands test feet height.
- The Chain Wrench's disc at the anchor's foot is a one-point `Discs`, and its chain is a `Tether` from her body to
  anchor 0.
- The Extraction's two passes are its chains striking once per 10 ticks over its 20 active ticks.
- The hold's sinew strands are 0.019, 0.014 and 0.01 wide (the subtlety budget), not the 0.07 to 0.035 of section 12.
- Gold in the standing geometry (the re-binding chain's spark, the hold's impulses, the fragments' sparks) draws only
  when `SeraphaeRadianceGeometry` is handed the Ductilis consumer, which needs the follow-up hook below; until then her
  standing light is white only and the gold of her moves comes through her telegraph style.
- Healed Ground's extra discs for extra challengers sit under the next most wounded, not "between them and her".
- Turtle pressure counts a target past her chain's end and outside her presence; strain counts ticks in the strain
  glide state.
- The meter's per-player value is unused; growth rides the secondary (0 to 10).
- A forced move (the first reach, the flood, the hold's lash) skips the picker's cooldown and eligibility, by the kit's
  design.

### Not built

- No compile, no test run, no GameTest run, no review client run (stage rule).
- The Seraphae voice sheet for `docs/NPC_VOICES.md` (final review stage).
- GameTests for the reserve wake's 320 blood, the rebind's 240 through `BloodAccounting`, the forced all-deeds
  Apotheos routine and four challengers: not written (they need a blood-volume setup the verify stage should add).
- The mausoleum theme: unchanged and not re-run, as section 9 says.

### Compile-gate follow-ups

- `src/test/resources/architecture/saint-kit-legacy-allow-list.txt`: remove `seraphae/SeraphaeEntity.java`,
  `seraphae/ContainmentAnchorEntity.java` and `seraphae/SeraphaeFragmentEntity.java` (they no longer use a legacy
  piece, so the guardrail's stale check fails until they go).
- `SaintPowerRulesTest`: drop the `SeraphaeGrowthRules.healing`/`convulses` asserts (the presence heals up to 1 a pulse
  now and `convulses` is gone). `SaintPowerGameTests`: retire `seraphaeBothSecondaryAttacksRespectWindupAndRecovery`
  and `seraphaeHealsThenOvergrowsWithoutChangingContainmentVictory` (`onFragmentContained` is gone).
  `SaintEncounterGameTests`: retire the Seraphae state-cycle and save-key cases (`SeraphaeState`,
  `getSeraphaeState`, `getStateTimer`, `getContainmentIntegrity`, `onFragmentContained`).
- `SaintRiseTest.everySaintHoldsThroughItsRise`: drop Seraphae (a kit Saint rises through the kit's tick and must not
  call `tickRise`). Its `hurt` check still holds (`SeraphaeEntity.hurt` starts with the rise check).
- `ManipulationVisualRenderer`: pass the batch to her standing geometry,
  `SeraphaeRadianceGeometry.collect(world, poses, FLOWS, camera, right, up, time, partial)`, and to her cues
  (`SeraphaeRadianceGeometry.draw(packet, poses, FLOWS.vertices(LUX), FLOWS.vertices(DUCTILIS), ...)`); both overloads
  exist. `SeraphaePresentationGuardrailTest` already accepts either form.
- `client/event/LayerEvents`: drop the `SeraphaeFragmentModel` and `ContainmentAnchorModel` layer definitions, then
  delete those two models (copy to `removed_assets/`), `containment_anchor.png`, `seraphae_fragment.png` and
  `SeraphaeFragmentModel.bbmodel`, and remove `ContainmentAnchorEntity.isActive()`, which only the old model reads.
- `en_us.json`: remove the eight retired `message.hemomancy.seraphae.*` state lines.
- `gradle/game-tests.gradle`: add the `seraphae_fight_validation` suite row.
- `BossPresentationIds.SCENES`: add the three new Seraphae scene ids if that list should stay complete (no test reads
  it).
- `SaintPatternGeometry`: drop the `SERAPHAE_RELIQUARY` branch when `SaintAttackPattern` goes.

### Kit follow-ups

- A forced move (`SaintCombatController.start`) cannot carry a variant; her hold's Nerve Lash re-rolls its side from
  the target. A `start(move, cut, variant)` would let a Saint pass it.
- `SaintMoveContext` has no "first recovery tick" flag; she computes it from the timeline. A `recoveryStartedNow()`
  would match `committedNow()`.
- `SaintRoot` roots movement fully; Knotting's jump-only hold is her own held modifier. A `SaintRoot.jumpOnly` would
  share it.
- `SaintArena` has no "is owned" query for an unloaded UUID; she keeps her own sets of anchor and fragment ids.

### Compile gate 2026-11-06

The whole tree compiles (`compileJava compileTestJava compileGameTestJava`) and the JVM suite is green. Shared edits
made at the gate: the legacy Saint pieces are deleted (`SaintSecondaryAttacks`, `SaintAttackPattern`,
`SaintPatternGeometry` and their two tests, `registerTrialGoals`, `sendFloorRing`, the visual-state and secondary synced
fields, `reapplyEnrageEffects`), `createKit()` is abstract, the legacy allow-list is gone, `SaintPowerRulesTest` and
`SaintPowerGameTests` (every case drove a legacy piece; the `saint_power_validation` suite row and its template went
with them) are deleted, and the four fight suites have rows in `gradle/game-tests.gradle` (alpha). All deleted files
are under `removed_assets/` with MANIFEST lines. In this Saint:
- `ManipulationVisualRenderer` passes the school batch: `SeraphaeRadianceGeometry.collect(world, poses, FLOWS, ...)`
  and `draw(packet, poses, LUX, DUCTILIS, ...)`, so her gold sparks draw.
- `SeraphaeFragmentModel`, `ContainmentAnchorModel`, their bbmodels, `containment_anchor.png`, `seraphae_fragment.png`,
  their `LayerEvents` registrations, their `java_model_to_bbmodel.mjs` rows and `ContainmentAnchorEntity.isActive()`
  are deleted.
- `SeraphaeFightClientReview`'s `Stop` record component `wait` is renamed `waitTicks` (a record cannot declare
  `wait()`, which `Object` makes final).
- `SaintEncounterGameTests.seraphaeIsATrialSaintWithItsContainmentUnchanged` is retired (old states, integrity API and
  save keys); `seraphaeWakesTheChambersBlockAnchorsAndRestoresThem` stays and still compiles, but nobody has run it
  against the reworked anchors.
- Her three scene ids are in `BossPresentationIds.SCENES`.
- The eight retired `message.hemomancy.seraphae.*` state lines are removed from `en_us.json`.

## Verification 2026-11-05

Run on 2026-11-06 by the Seraphae verify stage. Nothing is committed. Logs are under `build/verify-seraphae/`; the
stills of the last review run are under `build/boss-presentation-review/screenshots/seraphae-fight-<nn>-<stop>-<tick>.png`
and its report is `build/boss-presentation-review/operator/seraphae-fight-review.json`.

**What ran.**

- JVM: `test --tests '*Seraphae*' '*Saint*' '*architecture*' '*BossFramework*' '*BossHold*' '*BossBar*'` passed 400
  tests with 0 failures (`jvm3.log`), after every change below. That covers all of her rules tests, `SeraphaeModelTest`,
  `SeraphaeTexturePaletteTest`, `SeraphaeRadianceGeometryTest` (the subtlety budget), `SeraphaePresentationGuardrailTest`
  and every architecture guardrail.
- GameTests: `runSeraphaeFightGameTestServer` passed 9 of 9 (`gt6-SeraphaeFight.log`), with the new hold case.
  `runSaintKitGameTestServer` passed 8 of 8 (with the new damage-factor case) and `runHemorathFightGameTestServer` 19 of
  19 after the two kit fixes. The combined `runGameTestServer` ran 1424 tests (`combined2.log`) with 9 failures, none of
  them Seraphae's: `SaintEncounterGameTests.seraphaeWakesTheChambersBlockAnchorsAndRestoresThem` now passes.
- Review client: `runBossPresentationReviewClient -PseraphaeFightReview=true`, five rounds on the rewritten harness. The
  last round reports `passed: true`: all 16 picked and follow-up moves reached their active span, the hold caught and
  grew the patient, all four openings opened (Recoil, Seized, Condensing, Lapse), a slipped anchor was re-bound and an
  aisle reserve woken by held Projection through the real router and blood accounting, four fragments were drawn in,
  led to an anchor and struck into the Condensing, both phases and cards played, the Radiance Flood ran, the ending ran
  endTrial once with one reward, the tomb closed and `arena_cells_changed` is 0 (every block in the chamber box is back
  as the template laid it, radiant stone and anchor cells included).
- Shaders: every round compiled and rendered on this GPU. She adds no shader; her materials are the Lux and Ductilis
  school batch. The client log has no shader, program or render errors and no exceptions. Its only shader lines are the
  two warnings that were already there (`rendertype_entity_translucent_emissive` has no `Sampler2`;
  `vesper_wound_draw` has no `FogColor`).

**Fixed in this stage.**

- Kit, `SaintTrialEntity.actuallyHurt`: the damage-taken factor never applied. NeoForge 21.1's `actuallyHurt` ignores its
  amount and spends the damage container's new damage, so `amount * factor` did nothing and her health fell under
  every blow (the fight suite's Recoil case caught it: 4 damage left 498.88 of 500, exactly 4 after armour 20). The
  container's damage is now scaled. This was dead for every Saint; `SaintKitGameTests.theDamageFactorReachesHealth`
  pins it. Noted in the umbrella's Kit API section.
- Kit, `BossHold.tick`: it looked the held player up in the server's player list, so a patient added to the level
  alone was released as LOST on the tick it was caught, and the reach never held anyone in a GameTest or the review.
  It now asks its own level; the same-level check was already there, so real players are held exactly as before.
- `SeraphaeFightGameTests.theReachHoldsTheWoundedHealsThenGrows` (design case 3, first half): her first reach holds a
  wounded player, heals them to full first, then grows them. The Recoil case's health assert now prints both values.
- `SaintEncounterGameTests.seraphaeWakesTheChambersBlockAnchorsAndRestoresThem`: its last assert pinned the old anchor
  rule (a cell filled while the anchor stood stays filled). The arena owns the cells now and the kit's restore contract
  puts back a position a player changed, so the case checks that all four anchors stand again.
- `SeraphaeRenderer`: a block-light floor of 12 (`getBlockLightLevel`, Hemorath's pattern). In the Reaching, before any
  radiant stone is lit, she read as dark brown sticks with a black face; she is the chamber's white light.
- `SeraphaeTelegraphStyle`: fill 0.16/0.06 to 0.22/0.09, edge 0.55/0.28 to 0.7/0.4. On her pale healed stone the
  white fill all but vanished; the Convulsion's spokes and the Wing Grasp disc could not be seen.
- `SeraphaeMoveGeometry`: floor lines (the reach's palm lines and commit flash, the chain-end arc, the spokes) are
  `FLOOR_LINE` 0.032 instead of 0.006 to 0.012 (a fifth of a texel, invisible at range); the Wing Grasp gains its
  design's `RIPPLE` disc through the wind-up; the Chain Wrench's gold line is the strand width; the Unclosing Eye's
  beam is 0.06 (the lance core's width) at alpha 0.85, so the one beam to outrun reads at range. Aerial strands stay
  inside the 0.019 budget and shells under 0.28.
- `SeraphaeRadianceGeometry.fragment`: a faint `LUX_SHELL` halo (alpha 0.2), a larger heart card, and gold nerve
  impulses that fire more often and a little larger. Fragments were specks on the radiant floor, and a player has to
  find them to draw them in.
- `SeraphaeFightClientReview` rewritten on the Hemorath harness: the real `mausoleum/seraphae/chamber` template and the
  encounter's own rise from the sarcophagus; a survival patient with a player-info entry (so the client draws it and
  the hold, thread and growth geometry tied to it) and a full blood volume; projection and absorption driven through
  `SaintChannelRouter`; cameras per scene (east is screen-left looking south, so each centres west of her); stills at
  wind-up, commit and active for every move; the HUD growth stops in Survival (growth skips Creative players); a block
  for block restore check taken once the sarcophagus has laid its fillers; Bright gamma, hidden hand, onboarding
  dismissal.

**Stills to look at** (last run, `build/boss-presentation-review/screenshots/`):

- The body: `seraphae-fight-00-rise-050.png`, `-01-idle-024.png`, `-04-laying_on_of_hands-025.png` (palm lines),
  `-10-hold-050.png` and `-10-hold-105.png` (the hold, then the burst line).
- Gold nerve light: `-05-nerve_lash-015.png`, `-19-convulsion-023.png` (every nerve flaring) and `-19-convulsion-028.png`
  (the spokes turning).
- Openings and anchors: `-11-recoil-040.png`, `-23-rebind-074.png` (Seized), `-24-reserve-086.png`,
  `-25-fragments-040.png`, `-31-lapse-010.png`.
- Phases and the room: `-16-phase_restraint-030.png`, `-26-phase_escaping_light-030.png`, `-28-healed_ground-029.png`,
  `-29-unclosing_eye-046.png`, `-32-radiance_flood-044.png`.
- HUD: `-12-hud_growth_0-010.png` to `-15-hud_growth_10-002.png` (restraints row and growth line).
- Ending: `-33-ending-010.png` to `-33-ending-130.png` and `-34-restored-036.png` (the room dark again).

**Still open.**

- The combined gate's 9 failures are outside her lane: five Velorum (`theLastTenthOnlyFalls...`,
  `preservationFreezesHurts...`, `killedSaintReturnsToItsTombUnrewarded`, `velorumEndingClosesTheTomb...`,
  `stillGuardHoldsTheFloors...`), Putriciel's `colonisationThresholdsApplyAndClearOnLeaving`, BloodProfile's
  `velorum_devotee`, the FrozenIdentifier `registry-ids.txt` snapshot (no `saint_*` damage types; only an approved
  migration may replace it) and a Puppeteer timing case that passed in the previous combined run. The damage-factor fix
  changed none of Velorum's five; his verifier should rerun them.
- Not covered by GameTests: the rebind's 240 and the reserve's 320 blood through `BloodAccounting` (the review drives
  both through the router and they complete, but no case asserts the sums), the hold's three break rules, the burst
  ending a hold, four challengers, the forced all-deeds Apotheos routine, the flood resetting to 10 and an unload
  mid-ending.
- The fragments are findable but still faint against lit radiant stone; the Radiance Flood's dome (alpha 0.18) and the
  Transfusion Thread are subtle by design and read only up close. The hold's sinew strands are hidden behind her arms
  from the side camera.
- The review report's `rebind_progress` reads 0.0 (it is overwritten on the tick before the rebind lands); `rebound`
  and `opening_seized` are the real checks.
- The voice sheet for `docs/NPC_VOICES.md` and the topic docs (BOSSES.md, SAINT_MAUSOLEUMS.md, the references, the wiki)
  are the final review stage's.

**For the user.**

- Her look in the chamber: pale ivory nerve-tree wings, bone halo, iron locks, white vessel light and gold only in the
  nerve flashes. Compare `-01-idle-024.png` with the generator preview `build/seraphae_preview.png`.
- The light floor of 12 makes her read as a light source even in the dark Reaching; say if she should sit darker there.
- Telegraph strength on her floor is higher than the other Saints' (fill 0.22, edge 0.7); her floor is the palest of the
  four. Check `-19-convulsion-028.png` and `-18-wing_grasp-024.png` for "readable but partly clear".
- The two kit fixes change every Saint: blows outside an opening now really are reduced by each Saint's factor.

## Visual pass 2 (2026-11-06), effects

Hemorath's pass 2 set the bar and the method: after the first build the user called the Saint effects "very plain...
like debug lines". Hers were the same flat kit tints, hairline gold edges and 0.019 strands, white on the palest floor
of the four (`seraphae-fight-04-laying_on_of_hands-025.png`, `-07-axon_lance-026.png`, `-19-convulsion-028.png`). The
"subtle, no spaghetti" ruling still holds for overlays on her body and on screens (her vessel and nerve layers, her
presence ring, the growth sores on players, the radiance braid). Her attacks, telegraphs, hazards, impacts, streams,
bursts and ending are now bold, layered, physical geometry. Nothing here is compiled or launched yet: this stage could
not run Gradle (see "Needs a launch"). Her animation was rebuilt at the same time by another stage and is not described
here.

Her language is **healing that hurts**. Where a move will land, the floor is cut open and heals over too fast: it
swells, roots, blisters and spurs bone. White light wells in the tissue and runs out along vessels. At the strike, gold
nerve-fire races across it. Light pours out of her in streams and sheets with body and is caught in her iron chains.
It is not a recolour of Hemorath's cracks and iron. His floor breaks and bleeds; hers swells shut. His iron punches up;
her bone grows and curls. His blood drains to him; her light pours out of her. Colours: ivory, pale flesh, bruise, raw
red, bone, white light and the dark hematic iron of her locks. Gold is only ever the Ductilis material, never in her
own shaders, and there is no gilt anywhere.

### The trench, carried over

The user's request was "do the trench idea for the rake lanes, then carry it over". Hemorath's polish notes left that
idea open: a lane should be a filled, sunken trench, not three parallel cracks. In her language that trench is a
**furrow**: the floor opened along the lane like an incision and swelling shut. It is one seam that fills the lane's
whole width (`SeraphaeOvergrowthRules.trench`, its half-width exactly the footprint's).

- **The cut.** The cut runs down the middle: shadowed walls going raw, white light welling from its floor, granulation
  blisters swelling up out of it as the move builds.
- **The lips.** Swollen lips run either side of the cut, shaded as raised ridges with a glossy crest and a vessel of
  light under it.
- **The pallor.** Bruise and pallor fade out beyond the lips. The lips' outer foot is the footprint's edge, so the drawn
  edge is the hit test's edge.
- **The vessels.** Vessels branch off the lips into the floor on alternating sides as the cut runs past them.

Every lane-shaped footprint she has is drawn this way: the Laying On of Hands and Straining Reach lane, each Axon Lance
ray, the Chain Wrench line, every Extraction line and the Unclosing Eye's starting bearing. A test pins one furrow per
lane, at the lane's full half-width (`aLaneIsOneFurrowFillingItsWholeWidth`).

### How it is drawn

- **Her own pass.** `SeraphaeTelegraphPass` (new, `client/render/world/`) draws every Seraphae footprint, her healed
  pads, her chains in iron, her openings, the hold, struck bodies and her ending. `ManipulationVisualRenderer` runs it in
  both of its passes with one add-only line:

  ```java
  SeraphaeTelegraphPass.collect(world, poses, FLOWS, camera, right, up, time, partial, solids);
  ```

  It reads the same synced move, `FootprintFrame`, effective timeline and reveal tick as the kit's
  `SaintTelegraphRenderer`, so every drawn edge is the hit test's edge and the Unwatched Hands deed still hides the
  reach (and its chain-end mark) until the kit's reveal floor. `SeraphaeTelegraphStyle` now claims every move and every
  pad and paints nothing, so the kit's flat tints never show for her. The kit itself (`TelegraphCanvas`,
  `SaintTelegraphPainter`, the renderer) is unchanged.
- **Her floor material.** `shaders/core/world/seraphae_tissue` (new) is registered as `ShaderInit.SERAPHAE_TISSUE`
  (add-only) and drawn through `SeraphaeRenderTypes.TISSUE`, built once: translucent, depth-tested, no depth write. It
  has three shapes:
  - **Shape 0, the furrow.** Described in "The trench, carried over" above.
  - **Shape 1, overgrowth.** A patch of blister-like domes of new tissue, each lit from above with a glossy crown. They
    take the floor where a large field favours them first and then everywhere as the swelling rises. The crevices
    between them are shadowed and raw, lit from below by the light.
  - **Shape 2, a vessel.** A vessel lying on the floor: a pale sheath shaded as a tube round a white core, with knots of
    light running outward along it.

  The vertex colour is state, not tint: red is swelling, green is light (from 0.9 the hot core `0xFFD2C4`), blue is
  rawness, alpha is strength. Against her pale healed stone it reads by shadow, raw red and gloss, not by white.
- **Her bone and iron material.** `shaders/core/world/seraphae_bone` (new) is registered as `ShaderInit.SERAPHAE_BONE`
  and drawn through `SeraphaeRenderTypes.BONE`: solid, depth-writing, drawn in the solid pass. It has three shapes:
  - **Shape 0, bone.** Ivory, lit by its own facets with a key, a fill and a fresnel rim. It has growth rings, 16-a-block
    grain, white light running up its grooves, and a raw, wet root where it tore out of the floor or a body. Its
    formation front is a soft pale-pink tip: bone climbs out of the floor as it grows and dissolves back into light from
    the tip down as it withers.
  - **Shape 1, hematic iron.** The (49, 56, 53) iron with (105, 113, 99) struck edges, her light glowing inside it.
  - **Shape 2, shards.** Bone shards.
- **Networks.** `SeraphaeOvergrowthRules` is pure and JVM-tested. Each footprint grows its own network in the
  footprint's local frame:
  - **trench**: see above.
  - **fan**: radial nerve fibres about every 13 degrees with side branches, and a furrow on the outer arc.
  - **disc**: vessel trees forking twice from near the centre, and a broken furrow rim.
  - **star**: five to seven forked vessels and a broken ring, for marks and pads.
  - **fingers**: five furrows curling in from a rim.
  - **spoke**: four nerve fibres per Convulsion bundle, with furrows on its inner and outer rims.
  - **ring**.

  Each seam has a start and a span within the run, so the network grows out from her through the build. Nerve fibres
  are never drawn in her tissue; `SeraphaeLightGeometry.nerves` lays them flat on the floor in gold on the Ductilis
  material, as a flowing band with a bright line whose impulse races outward, lit as far as a front.
- **The life of a telegraph** (`SeraphaeOvergrowthRules.look`), the same for every move:
  - **Emerge** (reveal to about 12% of it): the cut opens, faint.
  - **Build** (to the commit): the cut runs its whole length by 55% of the reveal. The swelling rises from 0.15 to 0.8
    and the light from 0.08 to 0.8 (squared). The stretched flesh goes raw over the last 55%, and the furrow widens
    from 0.45.
  - **Commit flash** (4 ticks): the light runs white-hot, the swelling surges to 1.3 and the furrow yawns 30% wider.
    The gold last-chance cue starts 4 ticks before the commit and races out along the move's nerves.
  - **Strike** (the active span): full, the light settling from 0.9 to 0.7.
  - **Linger** (16 ticks, never past the recovery): the light drains over the first half and the swelling subsides into
    a pale scar (0.35).
  - **Dissolve**: the whole fades over the linger's last 45%.
- **Bone with mass** (`SeraphaeBoneGeometry`). Spurs are real meshes:
  - **Shape.** Five or six fluted facets in four tapering, twisting stages that lean and curl further in their upper
    half, about 0.18 blocks across at a one-block scale.
  - **Clusters.** A tall central spur with shorter ones splaying and curling outward round a soft seven-sided blister.
  - **Grow.** A fast swelling growth to 1.1 of full over 4 ticks, settling to 1 over 4.
  - **Throb.** On her heartbeat (7% girth), dying over 20 ticks.
  - **Wither.** Over 12 ticks the bone shrinks and dissolves into light from the tip.
  - **Shards.** Tumbling octahedra of bone fly on arcs and settle over 16 ticks.
- **Her chains in iron.** Each held or re-binding chain is now drawn link by link in the solid pass, from the anchor's
  crown to its iron on her:
  - **Links.** Each link is 6 by 4 texels with a one-texel wire, flat and edge in turn, sagging exactly as the radiance
    braid sags.
  - **Recoil.** A Recoil snaps them straight.
  - **Chain Wrench.** The wrench throws its chain sideways in a wave that runs from her hands to the anchor, pinned at
    both ends and dying over 12 ticks (`whip`).
  - **Light caught in the iron.** Her light lights the iron from inside as it passes in the Extraction, through
    Condensing's gather, and white-hot as each anchor's braid flares in the ending.

  A slipped chain is not drawn: the model's own hanging links show. The radiance pass still draws the light braid over
  the iron.
- **Light with body** (`SeraphaeLightGeometry`, on the Lux material as Riposte and the outpouring draw):
  - **Streams.** Each has a soft shell with wisps, a pale sheath body (`SHEATH 0xEADBD2`) swelling with knots that
    travel toward the receiving end, a white core, one raw clot sinew twisting through it, and the knots' hearts lit.
  - **Lashes.** On the Riposte anatomy: a sheath stroke tapering from tail to head, a raw leading edge, a white core,
    and gold nerve-fire (line and crackle) with forked gold dendrites at the head. A lash frays upward, shedding motes.
  - **Reaches.** A long tongue of light behind each palm, with a raw thread under it and gold down its middle.
  - **Walls and curtains.** Standing sheets of light (the Lux shell) with a lit crest and wisps, and a gold edge where a
    move's height matters.
  - **Bursts.** The Riposte strike: a flash, a swelling shell and seven rays with body, with gold crackle for a nerve's
    strike.
  - **Cracks, beads, sparks and nerve lines.**
- **Trails follow the animation.** The pass reads the hook the animation stage exposed on `SeraphaeAnimState`:
  `handAt(left, age)` and `tipAt(trunk, age)`, world-space trails of both hands and the six trunk tips that
  `SeraphaeRenderer` feeds every frame (`motion.trail(...)`). Lashes are laid along the wing tip's own path and reaches
  along each hand's, so they follow the animation's swing whatever it does. A stand-in trail this stage wrote before the
  hook landed is kept in `removed_assets/`.
- **Impacts on struck bodies** (`SeraphaeImpactGeometry`). These are read on the client from the hurt flash every client
  has (hurt time and the damage event's source, which must be her), so no packet was added. There are three kinds of
  strike:
  - **Nerve-fire.** The lashes, the Convulsion, the lance, the wrench and the grasp burst in light with gold crackle,
    nerve-fire spirals up the body, and shards fly.
  - **Growth.** For the Knot and Healed Ground, four bone spurs drive out of the body along the blow, bite out over 1.5
    ticks and are drawn back in from 3 to 8. Light bursts round them.
  - **Light.** The Eye, the Fracture, the Extraction, the flood and the thread hit with the Riposte burst in white.

  The floor under grounded feet swells raw and lights. In first person the struck viewer sees the floor and the
  nerve-fire, never a burst in their eye. Timing: flash 0 to 3.5, burst 0 to 6, fray to 10.

### Each move and hazard

| Move or moment | Build (wind-up) | Commit and active | Linger |
| --- | --- | --- | --- |
| Laying On of Hands, Straining Reach | The lane opens as one furrow the lane's full width, from her toward the patient. It swells shut with pale tissue, light wells in it, and two palm vessels run down it while vessels branch off its lips. Where her chains will stop her, a swollen ridge crosses the lane round a knot of new tissue (a bone blister growing with the build) with a gold bead over it. The Unwatched Hands deed hides the ridge, the knot and the bead, and the reveal floor holds back the rest. In the last 4 ticks gold races down the furrow's nerve. | The furrow flashes white-hot and full. Through the 10-tick glide each hand leaves a tongue of light along its own path (shell, sheath body, raw thread, white core, gold down its middle, a hot knot at the palm). The hold and Recoil follow (below). | The hand trails fray over 8 ticks; the furrow drains and scars over 16. |
| Nerve Lash, Counter-Lash | The 150-degree cone swells with faint overgrowth. A fan of gold nerve fibres lies across it (dim through the build, flaring on the cue), and a furrow marks its outer arc at radius 6. | The live 40-degree band swells full under the sweep. A sheet of light stands on its leading edge: 0 to 0.9 high with a gold crest for the Nerve Lash (jump it), 1.25 to 2.4 with a gold foot for the Counter-Lash (duck under it). The lash rides the wing tip's own path (lower trunk for the lash, upper for the counter) with nerve-fire down its middle and dendrites at its head. Shards jump off the floor along the sweep. | The lash frays, lifting and shedding motes over 8 ticks; the cone scars. |
| Axon Lance, mirror | Each of the three rays opens as a 1.2-wide furrow with a nerve down its middle, dim, flaring gold on the cue. | Gold races out along each ray at the front's speed (2.5 blocks a tick, side rays 4 ticks later). A beam of light with body runs behind the front at knee height, flared wide at the fire. Gold dendrites fork at the front. Dendrite spurs of bone (three-spur clusters, 0.75 to 1.05 tall, leaning down the ray) punch up behind it every 1.4 blocks with a hot floor flash. Where a ray ends, a fan of five bone spurs bursts outward with big gold dendrites. | The beams fade 4 ticks past the active span; the spurs wither over 12; the rays scar. |
| Regrowth Knot | The 2.75 disc swells with overgrowth (0.35 to 0.9 through the build) and vessel trees run into it. At its heart a knot of five blisters grows and throbs. | On each pulse (active 0, 20, 40), a low ring of light rolls out to the rim, a hot flash shows at the heart, and seven small spur clusters burst up round the rim (rippling round) and wither 5 ticks later, with shards. | Scars. |
| Transfusion Thread | One stream of light with body grows from her heart to the far patient through the wind-up, sagging. | Knots of light run down it into them, gold impulses travel with them, and their chest glows on each 10-tick beat. | When the move ends (or line of sight breaks and the server skips to recovery) the thread frays from their end back toward her over 8 ticks, widening and fading. |
| Wing Grasp | The 2.5 disc swells; five furrows curl in from the rim like fingers closing, with nerves in them; five bone tine stubs show at the rim. | Five bone tines (1.8 tall) grow out of the rim and curl inward over the centre like a hand shutting, blisters at their feet. Gold crackles in a burst where their tips meet, and shards fall. | The tines wither from 4 ticks after the active span. |
| Convulsion, wide | Six nerve bundles: each a band of swollen floor with four gold fibres and furrowed rims, at their resting angles. The gaps stay clean floor. The fibres flare on her three stutters (6, 13, 20) and on the cue. | The bundles turn with the spokes (the wide one the other way), their fibres firing and flickering. A sheet of light 1.3 high stands on each bundle's outer rim and 0.8 on its inner, gold crested, turning with it. Shards jump off each bundle. | The light dims to 30% and the bundles scar where they stopped. |
| Chain Wrench | The line under the chain opens as a 2-block furrow; the anchor's foot swells in a disc with a root star. Gold runs the chain from her hands toward the anchor as a bold nerve line with a spark at its head. | A sheet of light 1.1 high stands along the line with a gold crest. The iron chain whips sideways in a wave from her hands to the anchor. A slipped anchor throws shards from its crown. | The sheet fades 4 ticks past the active span; the line scars. |
| Fracture | The 4-block disc swells and vessels of light run out under her, with a furrow ring at its edge. A crack of light opens down her chest (sheath lip, white core, gold crackle and a spark). | Light pours out of her: a burst with body, and a ring of light rolling out to the disc's edge (1.6 high, gold crest). Twelve shards of her are flung. Streams of light run from her heart into each new fragment over 10 ticks. | The crack closes over 6 ticks; the disc scars. |
| Healed Ground | Under each mark a pad of overgrowth swells with a root star, and five bone stubs show through (hidden with the tell). | Per pad, staggered 1.5 ticks: a six-spur cluster (1.25 to 1.45 tall) bursts out with four smaller clusters leaning outward 1.1 out, shards fly, a low ring of light rolls to the pad's edge and a hot flash lights it. | The spurs wither before the recovery ends and the standing pad takes over. |
| Healed pad (hazard) | | A pad of overgrowth with roots. It swells and lights on its 20-tick pulse, with a low ring of light rolling to its edge. Three short spur clusters stand in it, throbbing. | It withers back into the floor over its fade (from the tick it was expired). |
| Unclosing Eye | The aperture opens on her chest (eye card and shell). Eight streams of light are drawn in from round the room into it. A furrow scores the floor along the bearing the beam starts from, and faint overgrowth is seeded across the cone. | The beam: light with body (0.14 half-width, flared at the start) from her chest to wherever the blocks stop it, sweeping. The floor it has crossed swells raw behind it in a seared band. Light splashes up where it lands (a hot pool and a low ring), with shards. | The Lapse: the aperture gutters to a flickering slit, dimming out at the recovery's end. |
| Extraction | The line under every held chain opens as a furrow. | Light pours out of her along every chain into its anchor (active 0 to 8) and back (10 to 18) as streams with body. The iron of each chain lights from inside where the light passes. | Scars. |
| Radiance Flood | Overgrowth spreads out from under her across the floor (to 14 blocks) with vessel trees racing outward, and the pale dome of light grows from her. | A sheet of light 2.2 high rolls out to the walls (16 blocks) with a gold crest, and a burst of light shows at her heart. Ten bone clusters burst up in a ring round her, leaning out. | The clusters wither before the recovery ends. |
| Recoil, Seized | | A ring of light snaps out from her feet to 2.8 (1.4 high, gold crest). Shards fly from each of her irons, and the iron chains snap straight. | |
| Condensing | | Streams of light run in along every held chain to her over the 10-tick gather, and the iron glows. | |
| The hold | | Streams of light with body pour from both palms into the patient's chest. The floor under them swells with new tissue on the beat. On each 10-tick beat gold nerve lines run from their chest up their shoulders and down their arms. | |
| Ending | | Each anchor flares in turn (binding anchors first, 5 ticks apart) with a burst of light at its crown, and its iron chain lights white-hot. Through the reel (20 to 90) streams of light run in along every chain to her, dimming as her light retreats. From tick 80 the floor under her heals shut in shrinking overgrowth. From 86 a ring of light closes over the tomb round her, rising as she sinks, gold at its foot, gone at 100. | |

### Tests

- `SeraphaeOvergrowthRulesTest` (new, JVM) covers:
  - the telegraph's life from emerge to dissolve;
  - the linger never outlasting the recovery;
  - one furrow per lane at the lane's full half-width;
  - every network staying on its footprint over 40 seeds;
  - networks being stable per seed and bounded;
  - bone overgrowing, settling, throbbing and withering away;
  - clusters varying, with the centre tallest;
  - shards arcing and settling;
  - lash, reach, bite and beam timing;
  - waves rolling out and stopping;
  - links, sag and a whip pinned at both ends;
  - knots travelling.
- `SeraphaeEffectGeometryTest` (new, JVM) covers:
  - furrows, vessels and overgrowth keeping the tissue shader's tiles;
  - nerve fibres never reaching the tissue and lying flat in gold;
  - nothing drawing unrevealed, healed or before the cut runs;
  - bone hidden until it grows, standing about its scale, and gone once withered;
  - chains as exactly sixteen faces a link;
  - streams, lashes, reaches, sheets and bursts staying in budget and fraying out;
  - each move's strike kind.
- `SeraphaePresentationGuardrailTest` (extended): its no-vanilla-particles and gold-only-on-Ductilis checks now cover
  every new file, and a new case pins the pass running in both passes, the style painting no kit tints, her two shaders
  registered, and her render types built once.
- The network bounds and the rule numbers were checked by a scratch Python port before the Java tests were written. The
  tests themselves have not been run.

### Needs a launch (not done here)

- Compile the tree. No Gradle or Java build ran in this stage.
- Launch the review client (`runBossPresentationReviewClient -PseraphaeFightReview=true`) to prove `seraphae_tissue` and
  `seraphae_bone` compile on the GPU. Offline validators miss driver errors.
- Look at every move, the pads and the ending on her pale healed stone: the furrow's shadow and raw red against the
  stone, the gold fibres, and whether the overgrowth's blisters read as swelling rather than noise.
- Watch the frame cost of the iron chains: four to six chains of about 35 to 53 links, 16 quads a link, every frame in
  the solid pass. The Radiance Flood's 14-block overgrowth and the disc's vessel trees also need watching.
- Check the impacts on a real survival player (the review's patient is one).

### For the polish stage

- **The trail hook.** The pass reads `SeraphaeAnimState.handAt` and `tipAt`, the animation stage's hook, as Hemorath's
  slashes read `HemorathAnimState.clawAt`. If that API changes, only `SeraphaeTelegraphPass.reach` and `lashTrail`
  follow it.
- **Lash side and trunk.** The lash side follows the old geometry's mapping, and the pass draws the lash along that
  wing's trunk. Confirm against the rebuilt animation which wing whips and which trunk leads: variant 0 is the left wing
  (tips 0 to 2), the Nerve Lash's lower trunk is 2, the Counter-Lash's upper trunk is 0.
- **Tuning points.**
  - The furrow's cut and lip proportions in `seraphae_tissue.fsh`: cut 0.40, lip edge 0.74, `HALO` 1.35.
  - The overgrowth's coverage curve (`present`).
  - Spur girth (0.16 times the square root of the scale).
  - The chain's glow floor (0.12).
  - The lash width (0.03 to 0.19 times the scale).
- **Not done.**
  - Fragments, the presence ring and growth on players keep their pass-1 radiance look: they are overlays and
    targets, not attacks.
  - The thread's break is drawn from the move's end, not from line of sight, which the client does not know.
  - `docs/architecture/evidence/source-text-impact-map.csv` was not regenerated for the guardrail test's new reads.
    Run `python gradle/source-text-impact-map.py` once the parallel stages settle.

## Visual pass 2 (2026-11-06), animation

Hemorath's first build read "stiff and disjointed", and hers was built the same way. Her tracks were single curves added
to a flat pose and blended by a 5-tick crossfade. Her wing "reach" turned each trunk about its own long axis, so the
nerve trees barely moved. Nothing trailed anything, and her gown and legs passed through the floor when she sank. The
rebuild follows Hemorath's pass-2 machinery: coil, a fast arc through the commit, follow-through, a weighted settle,
springs on everything that hangs, inertial blends, and grounded contact. Nothing here is compiled or launched yet; this
stage could not run Gradle. Her effects were rebuilt at the same time by another stage (above). They read the limb
trails described here.

### What changed in the machinery

- **Layers.** A frame is built in this order:
  1. the idle of her phase and her irons;
  2. her glide;
  3. the playing track, as deltas over both;
  4. the blend;
  5. the springs;
  6. `pose()`.

  Every move's keys start and end at zero, so she rises out of the idle and settles back into it. The channels are
  `SeraphaePose` (89: 55 authored, 34 written by the springs).
- **The idle blends by phase.** The Reaching, the Restraint and the Escaping Light are spring-smoothed weights, so a
  phase change never steps her body. The breath rate changes with the phase, so its phase is carried in
  `SeraphaeAnimState`, not recomputed from her age. A slipped iron changes that limb's idle: the wrist hangs freer, the
  wing droops and swings out.
- **The glide is read off her real motion.** Her velocity is measured in her own frame (`SeraphaeHover.local`) and
  smoothed by a slightly under-damped spring:
  - She leans 8 degrees into a full glide and banks into a slide.
  - When she stops short she rocks past upright and settles.
  - Strain at the chain's end and the reel are spring weights from the synced glide state.
  - Her turn is read from her body yaw.

  No flag was added to the sync.
- **The inertial blend.** She reuses Hemorath's `HemorathPoseBlend` and `HemorathSpring`. They are general, so nothing
  was moved; she calls them. A changed track keeps the gap from the last shown pose and the velocity the body had, and
  that gap dies away along a cubic Hermite while the new track plays at full speed underneath. A new move, the same move
  again (its synced serial), an opening, the hold and the ending each count as a change. Blend lengths:
  - 5 ticks normally (B-8);
  - 4 into the hold (the hands are already out);
  - 8 into the ending;
  - 2.5 into a Recoil or a Seized, because the chains' snap is a jerk.
- **The clock.** Every move plays on the move view's `BossReplayClock` through `SeraphaeAnimState.authored`:
  - The wind-up keys stretch to the wind-up that runs (the thrown reach after a Wing Grasp winds up in 20, not 24).
  - The active span keeps its own clock.
  - A recovery cut by No Rest Given squeezes the settle keys, so she still lands before the next move.

  The hold has no synced counter. Its catch and its rocking run from the frame this client saw it begin, as Hemorath's
  patch eruption does.
- **Springs** (36 slots, stepped once a frame on the frame's own time, each trail clamped to 0.7 rad):
  - The head trails her chest's pitch, twist and roll.
  - Each forearm trails its arm, and each hand its forearm; they whip through when the arm stops.
  - Each wing trails her twist, her turn and her glide (it lags back as she sets off and swings forward when she stops),
    and droops as she rises.
  - Every trunk's outer segments, branches and tip fan trail its root, forward and up. A lash cracks at the tip, and a
    turn drags the nerve trees round after her.
  - The gown, veil, hair, halo, wrist chains and waist chain swing off her body and her motion and settle.
  - Her hanging legs trail like a pendulum.
  - A touch kicks a flinch that rings out.
- **The wings move as trees.** Each trunk's root is turned in its plexus's frame: a sweep about the vertical carries the
  trunk forward round her or back behind her, and a raise lifts it in the frontal plane. Fold closes all three down her
  back, spread fans them apart, and the dendrite fans open and close like fingers. The segments curl forward, sway on the
  delayed nerve wave, and twitch when an idle gold flash fires down that trunk (`SeraphaeNerveFlashRules.sinceIdle`, the
  same seed the nerve layer uses).
- **Her whole body turns.** `TURN` turns her about her own axis without touching the server's body yaw:
  - The Unclosing Eye turns her through its sweep at the beam's own rate, so the beam leaves her chest where she faces.
    The test is `theEyeTurnsHerThroughItsSweepAtTheBeamsOwnRate`.
  - The Convulsion turns her with its spokes (`theConvulsionTurnsHerWithItsSpokes`).
  - The lashes throw her whole body into the whip.
- **Grounded contact** (`SeraphaeHover`; the floor is 0.35 blocks under her feet):
  - **Palms.** A `PLANT` weight sinks her until her lower palm lies on the floor.
  - **Legs.** They fold only as far as it takes to keep the soles off the floor (`foldToClear`).
  - **Gown.** Each panel hangs mostly plumb (it follows 30% of her chest's tilt). It flares out exactly enough that its
    hem lies on the floor (`flare`, exact for any lean), and the front panels are pushed forward by her knees.
  - **Chains.** A hanging chain's links lie down on the floor and trail behind her instead of passing through it.
- **Limb trails for the effects.** `SeraphaeRenderer` feeds `SeraphaeAnimState.trail(origin, age)` every frame. The
  effects read the world position of a hand or a trunk's tip fan at any render age in about the last second through
  `handAt(left, age)` and `tipAt(trunk, age)` (`SeraphaeLimbTrail`, interpolated between frames). A reach or a lash drawn
  along them follows the hand or tip the animation drew. Tips are 0 to 2 for the left wing (upper, middle, lower) and 3
  to 5 for the right.
- **The rig is unchanged.** The generator was re-run only to splice the new `seraphae_anim.java`. It produced 279 cubes
  and 160 joints at 16 px a block. The atlases and the Blockbench project are byte-identical.

### Track beats

Times are on the authored clock; the commit is the end of the authored wind-up. "Left" is her left; variant 0 is the
left wing, as the server's side rule gives it.

| Track | Beats |
| --- | --- |
| Idle, the Reaching | A slow float (about a pixel) under a slow breath. Palms half-open and turned up before her, head tilted. Every 96 ticks one hand (the hands take turns) drifts out toward the hurt with the fingers opening and her head turning after it, then withdraws. |
| Idle, the Restraint | Drawn back by her chains, hands half-closed, head lowered, the wrists turning in their irons. Every 80 ticks one arm tugs forward and is stopped dead by its chain (the overshoot back), her head snapping to look at it. |
| Idle, the Escaping Light | Chest open and head back, arms lowered out from her sides with the palms turned out and the fingers splayed, wings raised and fanned, tips open, a constant fine tremble, quick deep breaths. |
| Irons | A slipped wrist hangs freer of the pull back. A slipped wing droops, swings out and curls. |
| Glide | She leans into the glide and banks into a slide. The wings draw back and lag as she sets off, the arms trail, the feet trail and the gown streams. Stopping short she rocks past upright and settles. |
| Strain | Leaning at the target, head forward, arms dragged back by their chains with the fingers reaching, wings straining forward against their roots, trembling. A heave every 24 ticks. |
| Reeled | Arched back, hands pulled toward their anchors, wings flattened back, legs swinging forward as she is drawn backward. |
| Turn | Her chest and head lead into it, banking. The wings and gown drag round after her. |
| Laying On of Hands (54) | 0-24: the coil. She draws herself up and back, rising a little, palms turning out and rising to her chest with the elbows bent, wrists back, wings drawn back and raised, head lifting, a tremble in the last ticks. 24-27: she throws herself down the lane, body pitched forward, arms driven straight out, fingers spread, wings swept hard back, feet trailing. 27-34: the glide. 34-37: where she stops she rocks on past the line, arms swinging up as the wings swing forward. 37-54: settles. A catch hands over to the hold. A whiff hands over to the Recoil. |
| Straining Reach (66) | 0-14: one hand finds the chain on the target's side, coils with the body turned to it, and wrenches it loose across her (10-11, a jolt). 12-28: the reach's coil, low and crouched in the air. 28: the lunge, deeper (body near level, legs trailing). 28-42: the glide. 42-66: as the Laying On of Hands. |
| Hold | 0-6: the catch. Hands close on the patient's shoulders and chest. She bends in and sinks to them with her head tilted, and her wings sweep forward and curl round them. Then a 40-tick rock. Every 10 ticks a squeeze runs through the hands, the wing tips and the trunks, with the transfusion's beat. It grows more urgent and trembling as the hold goes on. |
| Nerve Lash (36) | 0-14: she dips low and turns away from the strike. The wing's lower trunk drops and draws back with its segments arched behind it, and the other two follow. 14-20: her whole body turns through the whip (a third of a turn). The trunk cracks forward low across her front, its tip snapping on past the line and the fan bursting open. The other arm flings out to balance her. Her head stays on the target. 20-36: the trunk recoils and settles. |
| Counter-Lash (40) | 0-12: the other wing rises high and arches over her head toward her far side, its trunks bowed back, as she lifts and leans away. 12-18: it whips down and across in front of her, her body turning and bowing into it, the tips cracking open past the line. 18-40: it swings back and settles. |
| Axon Lance and mirror (48) | 0-6: the middle trunk coils back. 6-18: it swings forward to point, with the upper and lower trunks parting round it. 18-22: she holds the aim, trembling. 22-24: it jabs out straight and the tip fan bursts open; the recoil rocks her back. 24-34: locked rigid while the lance runs. 34-48: it folds home. |
| Regrowth Knot (104) | 0-30: she bows over the target with her hands cupped low together (palms up) and her wings half-cupping too. 30, 50 and 70: on each pulse the hands lift sharply, as if raising something out of the floor, her body and wings lifting with them, then sink back slowly. 90-104: she settles. |
| Transfusion Thread (116) | 0-6: the hand on the target's side dips. 6-20: it lifts toward the target as the other hand comes to her heart. 20: her chest lifts as the thread leaves it. 20-100: the hand tugs back every ten ticks with the pull, the fingers slowly curling closed. 100-116: it lowers. |
| Wing Grasp (50) | 0-6: the wing toward the target draws in and up as she leans away. 6-20: it reaches round toward the target (forward for a flank, far back for a rear target), its trunks fanned and arched back like spread fingers and the tips wide open, her body and that arm leaning after it, trembling at the end. 20-23: the trunks snap shut like a closing hand. 22-30: the wing yanks forward round her side as her body heaves back. 30-50: it opens and settles. |
| Convulsion (64) and wide (64) | 0-3: she locks rigid, fingers splayed, wings half-spread and stiff, head pressed back. At each gold stutter (6, 13, 20) she jerks sideways in alternate directions, her wings flicking. At the commit the seizure throws her open: chest arched, arms and wings flung out, every joint jittering. Through the active span she turns with the spokes, a sixth of a turn (the wide one the other way). After it she sags, head hanging, wings drooping, sinking, then slowly gathers herself and turns back. |
| Chain Wrench (54) | 0-10: she turns to the chain on the aim's side and reaches back and down for it with both hands. 10-24: she grips, takes up the slack leaning back on it, and sinks and braces as the strain builds into a tremble. 24-30: she heaves her whole weight forward and twists away, hauling it across her body, wings flung forward. 34-54: she staggers and settles. |
| Fracture (62) | 0-9: she curls in over her crossed hands with her wings folded and her head bowed. 10, 14 and 18: she tears herself open in three jerks, the hands pulling her chest apart, wings ripping wider at each and her head thrown back. 20-30: the chest arches and trembles. 30-32: the light bursts out of her, arching her hard, arms and wings flung wide and tips flaring, lifting her. 32-42: she hangs open, shaking. 42-62: the wings sag and her head bows. |
| Healed Ground (52) | 0-20: she sinks and bows, her hands opening downward with the wrists back, her wings lifting high and fanning. 20-26: she hangs low with her palms raised (the coil). 26-28: she drives both palms flat onto the floor, kneeling in the air (the plant sinks her until the lower palm is on the floor). Her wings snap down and forward, and a shock runs up her arms. 28-36: she stays pressed there while the spurs break through. 36-52: she rises back. |
| Unclosing Eye (116, its recovery the Lapse) | 0-8: she turns a little away. 8-26: she turns to the sweep's starting edge with her head tipping back and her arms spreading low, palms out, lifting. 26: the eye opens and her chest arches hard. 26-66: she turns her whole body through the 110-degree sweep at the beam's rate. 66-72: the Lapse. The light gutters, she flickers and collapses forward into a bow, wings drooping, sinking. 72-116: she turns home and straightens. |
| Extraction (76) | 0-12: she reaches down to the chains at her wrists and grips them. 12-30: she hauls them in to her chest with her elbows driven back and her wings arching back and up. 30-32: the light runs out of her along every chain and she is wrenched forward after it, arms dragged out. 40-42: it floods back in and slams her upright, fists to her sternum, wings flung up. 50-76: she settles. |
| Radiance Flood (72) | 0-10: she curls in, wings wrapping, hands crossed. 10-36: she opens: arms rising wide, wings unfurling and lifting, head tipping back, floating up, the tremble building. 36-40: a held breath. 40-42: the flood flings her wider still, arched and lifted on it. 42-72: she droops and sinks, arms falling, head bowed. |
| Recoil (40, or 28, 60 and 20, read off the synced length) | 0-4: the chains snap taut and jerk her back (the server moves her half a block). Her arms are flung behind, her wings thrown forward and open, her head snapped back, past open. 4 to the end: she hangs open with her chest forward and gathers herself back to rest over the opening's own length. |
| Seized (30) | 0-6: dragged to the anchor (the server moves her 2 blocks). She kneels in the air, bowed, trembling, with the held limb stretched back toward it: an arm (with her chest turned after it), a wing (swept back and arched, her body tilting to it), or her waist (arched, hips drawn back). |
| Condensing (70) | 0-10: her wings fold, her hands cross at the wrists over her chest and her head bows. 10: a contraction as the gather lands. 10-70: she holds, bowed, trembling, breathing in on a 20-tick cycle. |
| Lapse (50, when it plays on its own) | She collapses forward into a bow, wings drooping, flickering, then begins to lift. |
| Rise (60) | She comes up out of the tomb like her own effigy: wings folded tight, hands crossed on her breast, head bowed. 30-46: the wings unfurl root first, the upper trunk before the lower, as her arms open from her chest and her head lifts. 50: the wings overshoot open. 52: the tip fans open last. |
| Phase card, the Restraint (50) | 0-4: the chains shorten with a jolt that yanks her arms back. 6-14: she lifts her hands before her face. 14-38: she looks at her wrists for the first time, turning them over in their irons. 38-50: she lowers them. |
| Phase card, the Escaping Light (50) | 0-2: her chest is thrown open as the light breaks out of it. To 22: head back, arms and wings flung open, lifted, trembling. 22-30: she brings her head down and looks at them, one hand drifting out toward them. 42-50: she settles. |
| Ending (100 drawn) | 0-20: every iron is pulled tight in turn as its anchor flares (a jolt every five ticks: left wing, left wrist, right wrist, right wing), her body drawn taut and trembling. 20-60: the chains reel her back over the tomb, her legs swinging forward as she is drawn backward, while her wings curl inward, her hands cross at the wrists on her breast and her head bows. 60-90: still; the breath and the idle fade out. 80-92: with her last line her head lifts a little and one hand opens from the cross toward them, then falls back. 90-100: she sinks, her wings and veil lifting behind her as she goes down. |

### Tests

None of these have been run.

- **`SeraphaeModelTest`** (extended):
  - every move rises out of the idle and settles back to zero, for both variants and five aims;
  - every track stays finite and bounded;
  - every strike is at least 2.5 times faster through the commit than anywhere in its wind-up, and carries on past the
    commit;
  - the Eye turns her at the beam's own rate, and the Convulsion turns her with its spokes, both against the footprints'
    own angles;
  - the lashes wind away and then whip through on their own wing;
  - the hold closes her hands and wraps her wings, more urgently as it goes;
  - the Recoil flings her open and gathers back over each opening length;
  - her palms plant on the floor, and her soles and gown hems never pass through it at any sink.
- **`SeraphaeHoverTest`** (new): her own frame and bearing signs; the legs folding only as far as the floor asks; the
  flared hem landing on the floor at any lean; the recurring gestures.
- **`SeraphaeLimbTrailTest`** (new): interpolation between frames, the read lead, a frame drawn twice, a reload, the
  frame cap.
- **`SeraphaeAnimStateTest`** (new): the authored clock, the stretched thrown reach, and the settle under No Rest Given.
- **Checked by scratch scripts.** The strike speed ratios were checked numerically with a Python port of
  `HemoEasing.catmullRom`. The rig's sign conventions and the key poses were checked on rendered stills from a Python
  port of `pose()`: the reach coil and lunge, the hold, the lash wind-up and whip, the folded wings, the grasp open and
  shut, the plant, the Recoil, the flood, the lance, the Fracture, the rise and the ending.

### For the review

- **Run the review client.** Run `runBossPresentationReviewClient -PseraphaeFightReview=true` and watch her in motion,
  above all:
  - the reach's coil and lunge, and the stop past the line;
  - the lashes' whole-body turn and the tip's crack;
  - the trunks trailing a turn;
  - the hold's wrap;
  - the plant in Healed Ground;
  - the gown heaping on the floor.
- **Tune points.**
  - The spring gains in `secondary()` and the trail clamp (0.7).
  - The blend lengths (`BLEND_SNAP`, `BLEND_HOLD`).
  - `ROBE_FOLLOW` (0.3).
  - The fold (`FOLD_RAISE`, `FOLD_SWEEP`).
  - The idle gesture periods (96 and 80 ticks).
- **The effects' lash mapping.** The effects stage asked which trunk leads. The Nerve Lash leads with the lower trunk
  (tip 2 or 5). The Counter-Lash leads with the upper and middle trunks (tips 0 and 1, or 3 and 4), and the lower trunk
  follows at half strength. Variant 0 is the left wing.
- **Not done.**
  - The hit direction: a touch always flinches her backward.
  - No arm IK: the hold's hands are authored to meet a standing player 1.1 blocks in front of her, not solved to the
    patient.
  - The wrench's grip is authored toward the aim's side, not solved to the chain.

## Visual pass 2 (2026-11-06), polish

The two pass-2 stages and the compile gate left the tree green. Before any change, all Seraphae JVM tests, the
guardrails and `seraphae_fight_validation` (9 of 9) passed. Five full native runs of
`runBossPresentationReviewClient -PseraphaeFightReview=true` followed, each about 790 stills. Every run ended
`SERAPHAE_FIGHT_REVIEW_OK`: every move reached its active span, the ending was rewarded and the chamber was restored. No
run logged a shader, program or compile error, so `seraphae_tissue` (including its rewrite here) and `seraphae_bone`
compile on this GPU. After the last round the whole suite passed (5,454 JVM tests, no failures), and so did
`seraphae_fight_validation` (9 of 9).

### What round 1 showed

Measured against Hematic Riposte, the cast outpouring and Hemorath's accepted pass 2b:

- **The furrow.** The trench carry-over read as a flat, glossy pink strip with a white outline and a red stipple
  floating on the floor: a slug or a ribbon, not a cut. Nothing about it was sunken.
- **The overgrowth.** It was a regular grid of white discs on a dark grey wash. The lash cone looked polka-dotted and
  the Convulsion spokes looked like toadstool caps (red with white spots). Healed pads were honeycomb.
- **Vessels.** Pale pink forked twigs on the floor read as noodles and debug lines.
- **Light.** The Eye's beam, the lance beams and the threads were hairlines. The Lux material's `SLASH` shape is a
  radial lens and `RIBBON` is a filament with a faint skirt, so a long stream drew as a thin line whatever its width.
  Lashes were a pale lens and a gold hair, invisible on her pale stone. Sheets of light were flat glass panes.
- **Healed Ground.** The plant sank her 1.5 blocks. To bring a palm to the floor she pitched her torso nearly level and
  lay in the floor with her wings flat.
- **Fixture.** The first frames of every high scene were black because the camera moved on the same tick as the move.
  The Laying On of Hands' player-eye scene lost its lane, because a hold left over from the scene before drew the
  patient in. The Radiance Flood's patient stood between the camera and her.

### What was fixed

- **The furrow is sunken: Hemorath's trench, carried over** (`seraphae_tissue`, shape 0, rewritten). The vertex shader
  now passes the view position. The fragment shader rebuilds the strip's frame from screen derivatives and traces the
  view ray into a channel under the cut's mouth: two leaning walls (`WALL_LEAN` 0.3), a floor (`FURROW_DEPTH` 0.75 a
  block of width) and the light standing in it. It is drawn in four parts:
  - **Walls.** Torn flesh: a pale strip of skin at the top, then raw muscle in fibres running along the cut on a
    16-a-block grid, with dark tears and wet glints. They darken with depth and are lit from beneath by the light.
  - **The light.** It stands at 0.2 to 0.55 of the depth as the light rises, so the walls always show above it. It is
    milky and lit from within, white-hot down its middle at the flash, with a lit meniscus at the walls. At the flash
    the whole cut also glows from inside, so it still reads from a player's eye when the far wall hides the light.
  - **The floor of the cut.** Granulation lumps.
  - **Outside the mouth.** The swollen lips are shaded as ridges, flesh rather than white, with a dark inner face falling
    into the cut, a raw edge where the skin parts and a vessel bulging under the crest. Beyond them a faint pallor and
    bruise fade outside the footprint.

  The mouth is 0.46 of the strip and the lips' foot stays at 1/`HALO`, so the drawn edge is still the hit edge. Rim
  arcs, fingers and spoke rims are the same shape, so every furrow she has is cut into the floor.
- **Nothing lies across a pit.** The reach's two palm vessels and its middle nerve sat over the cut and floated once the
  cut was sunken. The reach and each Axon Lance ray now carry a gold nerve along each swollen lip instead
  (`SeraphaeTelegraphPass.lipNerves`, 0.8 of the half-width out).
- **Overgrowth is lumped flesh** (`seraphae_tissue`, shape 1). Domes of new tissue at two scales, each a different size,
  soft-unioned so they merge where they crowd and domain-warped so no grid shows. They are lit by a normal taken from
  the height field: diffuse light from above, a wet specular, bruise and raw red low down, pale flesh on the tops, and
  light glowing through the thinnest tops. Coverage follows the swell and an fbm field, so a patch takes the floor in
  clumps. The crevices are dark clot lit from below.
- **Vessels are raised veins** (shape 2): a dark red tube lit on its top, knots of white light running outward in it,
  soft at the edges. `VESSEL` went from 0.075 to 0.1, `VESSEL_GAP` from 1.3 to 1.7 (fewer branches along a lane), and
  `NERVE_FIBRE` from 0.06 to 0.08.
- **Light with body** (`SeraphaeLightGeometry`):
  - Streams fill their width with the Lux shell's flowing fill (`LUX_SHELL`) instead of the lens, at 0.75 to 1.3 times
    the half-width. The raw sinew is twice as wide and stronger.
  - The Eye's beam is 0.3 half-width (was 0.14) and the lance beams 0.16 (was 0.09).
  - Lashes have a filled body of raw nerve flesh (`SINEW 0xB04A58`) over a pale halo, with a clot edge along all but
    the tail. They are 0.05 to 0.32 wide (were 0.03 to 0.19), so the whip reads against her pale stone.
  - Reach tongues are wider (0.03 to 0.27) with a stronger raw thread.
  - Sheets of light have a warm shell, rising shafts of light every other segment (each beating on its own), a raw tear
    at the foot where the sheet leaves the floor, and a brighter crest.
- **Bone.** Spur girth is 0.21 times the root of the scale (was 0.16), and the lance's spurs are 1.0 to 1.4 tall (were
  0.75 to 1.05). The bone is warmer (`BONE` 0.90/0.83/0.70, `BONE_DARK` 0.50/0.40/0.32); it had read as grey quartz.
- **The Fracture's strike.** The burst out of her heart is 1.3 (was 0.7), and the streams into the fragments are 0.12
  (were 0.07).
- **Healed Ground's kneel** (`tools/model_export/seraphae_anim.java`, regenerated with
  `python tools/model_export/seraphae.py`):
  - The plant sinks her toward the floor until a palm touches it, but never more than `PLANT_REACH` (6 model units,
    three quarters of a block).
  - The lean peaks at 0.8 (was 1.15). The arms drive down (raise 0.25 to 0.35 at the commit, was 0.65 to 0.7). The
    lift is a third of what it was.

  She now kneels into the slam with her palms driven down, about one block lower in all. She no longer lies along the
  floor.
- **Review harness** (`SeraphaeFightClientReview`, `gradle/review-clients.gradle`):
  - Particles are set to All by default (`-PseraphaeReviewParticles=minimal|decreased|all`).
  - `-PseraphaeReviewOnly=a,b` keeps the rise and the named scenes.
  - Moves are shot every 3 ticks.
  - Each move waits 6 ticks after its camera moves before it is forced, and its stage is set again then.
  - Three-quarter `_side` scenes for the Laying On of Hands, both lashes, Wing Grasp, the Convulsion, the Fracture and
    Healed Ground. Player-eye `_low` scenes for the Laying On of Hands and the Axon Lance.
  - The hold, Recoil, fragments, Lapse, flood and ending are shot every 4 to 6 ticks.
  - The Chain Wrench and Extraction use the high west camera.
  - The flood's patient stands off the camera line.

### Tests

- `SeraphaeModelTest.thePalmsPlantOnTheFloorAndTheLegsAndGownNeverPassThroughIt` now pins the kneel. The palms are
  driven at least two units down from rest and never pass through the floor. The plant sinks her more than half of
  `PLANT_REACH` and at most one block. It replaces "the lower palm lies on the floor", which is what forced her flat.
- The other Seraphae JVM tests (`SeraphaeOvergrowthRulesTest`, `SeraphaeEffectGeometryTest`,
  `SeraphaePresentationGuardrailTest` and the model, hover, trail and anim-state tests) pass unchanged. The sheet budget
  in `lightWithBodyStaysBoundedAndFraysOut` (4,000 vertices for a 16-block ring) holds with the shafts every other
  segment.

### Evidence

- Rounds 1 to 5 are under `build/seraphae-pass2/roundN/`. Each has `stills/` and one sheet per scene with its frames in
  tick order (`NN-scene.png`). Round 1 is the effects and animation stages as built. In round 2, the Laying On of
  Hands' `_low` scene lost its lane (fixture). Round 3 was rerun once, because the first attempt mixed two builds.
- The final sheets, one per scene in order, are in `build/seraphae-pass2/sheets/` (44 scenes).
- `build/seraphae-pass2/compare-pass2-riposte.png` sets Seraphae's round 1 and her final moments against Hematic
  Riposte and Hemorath's accepted pass 2b.
- Logs: `build/seraphae-pass2/r1.log` to `r5.log`, `tests-r0.log` to `tests-r5.log` and `final-tests.log`.
- The pass-1 stills were moved to `build/boss-presentation-review/screenshots/seraphae-pass1-2026-11-06/`.

### Still open

- **Pale on pale.** Her light is white and her chamber is pale stone, so streams, sheets and the beam still have less
  contrast than Hemorath's blood on dark floor. The raw sinews, the clot edges and the warm shell carry them now, but a
  sheet of light still reads partly as glass from three-quarters.
- **The furrow from a low angle.** From a player's eye the far wall hides the standing light (correct for a cut), so
  the warning there is the glow, the dark lip faces and the raw walls. From the boss camera the light is plain. As with
  Hemorath's trenches, a body standing in a furrow stands on the floor plane over it.
- **Overgrowth reads as brain or intestine** at the Convulsion's and the lash's full swell. That suits healing that
  hurts, but the user has not seen it.
- **The vessel trees** are bolder veins now, but off the lance and the flood they are still many forked lines. If they
  read as noodles in play, the next step is fewer forks (lane trees at depth 0).
- **Wing Grasp** is small from both of its cameras: the fingers and tines close round the patient and are partly hidden
  by them. The **Chain Wrench**'s line is short, because the nearest anchor is close on the dais, and from the high
  camera it is small. Neither was tuned beyond framing.
- **The ending and the phase cards** run under the scene director's own camera, so her anchors' flares and the closing
  ring are small in frame. They were not changed.
- **Not checked:** motion at real time (only 3-tick strips), first person when struck, frame cost (the chains are
  drawn link by link every frame in the solid pass, and the trench trace costs more per pixel than the flat strip), and
  a multiplayer view.
- **Animation** was judged from the strips. The reach, the lashes' whole-body throw, the Eye's turn and the hold read as
  one body with weight. The Healed Ground kneel was fixed. The springs' trail and the inertial blends between moves have
  not been watched in motion.
