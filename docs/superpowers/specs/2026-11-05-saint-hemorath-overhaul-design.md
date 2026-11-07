# Saint Hemorath: combat and visual overhaul

Date: 2026-11-05. Status: design, written in the design run of the Saint overhauls
(`2026-11-05-saint-overhauls-design.md`, "The workflow", run 1), updated to the umbrella's "Resolutions that override
the designs" on 2026-11-05 (each change is marked "Resolved 2026-11-05", and section 0 lists them). Build state is in
"Build notes 2026-11-05" at the end. The umbrella's shared kit,
rules, fight-shape targets, lanes and anti-goals bind this document; where this document needs more from the kit, it
says so under "Kit requests" and works around it inside Hemorath's lane.

The user's notes for this Saint (rulings of 2026-10-26, `SAINT_MAUSOLEUMS.md` "Rulings that bind"): a proximity blood
drain, a leaking absorbed meter (overload means out-feeding the leak), iron-spike patches on the bone. Lore also owes
"Hemorath's swelling drawing on the four wells" (`SAINT_MAUSOLEUMS.md` "Not built"; `LORE_REFERENCE.md` §10.8). Phase 2
keeps the card "The Vessel Empties". Wearing his body down stays as a second road to the ending.

Every decision that no ruling covers is mine and is listed in section 20.

## 0. Resolutions applied (2026-11-05)

The umbrella's "Resolutions that override the designs" win over this document. What they change for Hemorath:

- **User answers.** The first full meter in phase 1 empties him into phase 2 (tide, health tear, card); only a later
  fill bursts him. The Tithe is a per-fight ledger, the Last Thirst stops the leak, drinking heals him (all defaults).
  The fight may change real chamber blocks (the gutters), always through `SaintArena` and always restored.
- **A-2, Iron Thicket placement.** The spikes erupt where the challengers' blood fell in the last 5 seconds (where they
  were hit or where they paid blood), not under each challenger plus two. Section 7.
- **A-3.** `MARROW_RUPTURE` is Hemorath's one jump ring, at `Height.JUMPABLE` (0 to 0.9).
- **A-4, A-5.** The chain of `CHAINED_DEBT` / `TITHE_RECKONING` stays (it can be paid); `STAFF_FALL` stays his.
- **A-7, the Gorge.** No hold. `GORGE` becomes a pull: his open ribs draw the target across the floor toward him unless
  they sprint away; contact is one drained burst. Kit request 11 (the hold) is no longer used by Hemorath. Section 7.
- **B-1.** One set of bands for all four Saints: HUG under 3, NEAR under 6.5, MID under 12, FAR beyond.
- **B-2.** AREA is any footprint not attached to the body or reaching past 7 blocks; `CROWN_FALL` and the `GORGE` pull
  are AREA (wind-up at least 20), and `CHAINED_DEBT`'s 12-block throw makes it AREA (wind-up 18 to 20).
- **B-3.** Reveals clamp to 20 (area) and 12 (melee), under the Beholding deed too (as section 10 already said).
- **B-4.** The router passes the item's own reach and rate. Hemorath never takes more a tick than the held item moves;
  his held-channel numbers (20 a tick into openings and the Tithe, 30 a tick from a well, 25 a tick into a well) are
  capped by the item's rate, use the item's reach (5.5 for projection, the absorption item's own), and the overload
  counts projected blood at his multipliers (x1.5, x2) instead of draining faster.
- **B-5.** One scaling rule: max health and the meter's maximum scale by `1 + 0.5 × (n − 1)`, with `n` from the kit's
  `challengerCount()`. Health 600 solo (900, 1200, 1500); the meter 3000 solo (4500, 6000, 7500, unchanged).
- **B-6.** A worked proper-awakening fight length is in section 15.
- **B-8.** Every animation track blends from the last pose over 5 ticks (not 4).
- **B-9, B-10.** Jumpable waves use `Height.JUMPABLE`; a reloaded ending resumes (`resumeOnLoad`).
- **B-11.** Hemorath lands at exactly 16 px per block: one render scale of 2.0 (2 texels a unit), not 2.06.
- **B-12.** The hitbox is a `getDefaultDimensions` override in his own class.
- **C-4.** Death messages are `death.attack.hemomancy.saint_hemorath` (the kit ships them).
- **D-1, the Downfall deed.** It no longer floors the meter. He rises having drunk his wells (every well empties into
  him; the meter starts at 25%) and leaks at half rate for his first 1,200 ticks, so a forced Hemorath starts bloated and
  can still sink back to his Sunken rest. Section 10.
- **D-5.** Every drain from a player is a named `BloodAccounting.Source` constant in Hemorath's package, one per distinct
  drain (section 4, "Sources").
- **E.** Deleting `HematicCollapseGoal` and `EmptyPulseGoal`, removing lang keys and the shared tests' Hemorath cases,
  and any hook line in `ManipulationVisualRenderer` are the compile gate's. Hemorath records them under "Compile-gate
  follow-ups".

## 1. The fight in one paragraph

*Hemorath consumed.* The chamber is his circulation: his body drinks every challenger near him, the drink fills a
meter you can see on his body and on the HUD, and the meter leaks all the time, faster the fuller he gets. What leaks
runs down the floor gutters into four wells in the aisle alcoves, and he can call the wells back into himself. The
player learns in the first minute that hollow he is fast, hungry and iron-hard, and that full he is slow, strong and
soft: filling him is dangerous and it is the only way to hurt him properly. Both roads to the ending want him fed. The
overload road means giving him more than the leak can carry away: casting near him, holding Blood Projection into his
open ribs when he sags gorged, priming the wells with your own blood and letting him drink them. The wear-down road
means cutting him while he is swollen, and denying him the wells so he cannot heal. A first full meter does not burst
him; it empties him across the floor in a tide ("The Vessel Empties"), and the second fill is the one that bursts him.
Winning feels like a feast that went wrong for him: the ribs give way, the blood he took pours out of him into every
gutter in the room, the emptied skeleton folds into its vestments, and the blood runs home along the channel to the
tomb, which closes over him while the marrow starts, slowly, to fill again.

## 2. What stays and what goes

All paths are under `src/main/java/com/vincenthuto/hemomancy/` unless they start with `tools/`, `src/test` or
`src/gameTest`.

### Stays (rewritten onto the kit where noted)

| Piece | Path | What happens to it |
| --- | --- | --- |
| The entity class and its tomb, rise, reward and ending plumbing | `common/entity/boss/saint/hemorath/HemorathEntity.java`, base `common/entity/boss/saint/SaintTrialEntity.java` | Keeps `saintType()`, `rewardsGoToVictor()` (residuum into the victor's hands), `announceTrialEnd`, the save of a mid-ending state, the static `onPlayerBloodSpend(Player, double)` hook with its signature unchanged (so `common/manipulation/BloodManipulation.java` line 550 and `common/world/vein/EarthenVeinTravelManager.java` line 152 need no edit). Everything else in it is rewritten onto the kit's controller, meter, presence, openings, arena, phase table and deeds. |
| Attributes | `HemorathEntity.setAttributes` | Rebalanced (section 3): max health 220 to 600 (Resolved 2026-11-05: 520 in the first draft; raised so the wear-down road meets the four-to-six-minute target, section 15), armour 6 to 4 (his state multiplier carries the hardness now), knockback resistance 0.8 kept, follow range 48 kept. |
| Presentation | `common/entity/boss/presentation/BossPresentationIds.java` `FRAME_HEMORATH`, `SCENE_HEMORATH_EMPTIES_CARD`; `SCENE_SAINT_RISE` | Kept. The card now plays at the phase change whichever way it comes (section 3). |
| The bar frame | `client/screen/overlay/boss/frame/HemorathFrame.java` | Kept and extended: notches at 0.5 and 0.2, a third phase look for the Last Thirst (section 12). |
| Visual rules | `common/entity/boss/saint/hemorath/HemorathVisualRules.java` | Rewritten as the pure home of swell stages, rib spread, patch eruption and the burst kinds. `Pose` and the visual-state pose function go. |
| Server accents | `common/entity/boss/saint/hemorath/HemorathEffects.java` | Kept as the one place the server sends Hemorath bursts (`ManipulationVisuals.burst` with `Form.HEMORATH_RELIQUARY`) and its `HemoParticleData` accents. Already vanilla-free. |
| Animation state | `client/model/entity/boss/hemorath/HemorathAnimState.java` | Rewritten to read the kit's synced move block (move id, start tick through `BossReplayClock`, locked aim, phase, opening) plus Hemorath's own synced swell, patches and well levels. |
| The model | `tools/model_export/hemorath.py`, `tools/model_export/hemorath_anim.java`, generated `client/model/entity/boss/hemorath/HemorathModel.java`, `textures/entity/boss/hemorath/hemorath.png` and `hemorath_overload.png`, `models/entity/bbmodel/bosses/HemorathModel.bbmodel` | Kept as the pipeline; geometry, palette and every animation track are reworked (sections 12 and 13). The generated Java is never hand-edited. |
| The renderer | `client/render/entity/boss/hemorath/HemorathRenderer.java` | Kept. One render scale in both phases, 2.0 (Resolved 2026-11-05, B-11: exactly 16 px per block); the phase-2 scale of 2.12 goes (it broke texel density for no gain). Records the claw, core, staff head and halo attachments each frame as today. |
| World geometry | `client/render/world/HemorathReliquaryGeometry.java` | Kept and extended with the move, presence, well and arena geometry (section 12). Its debt tether, iron links, pulse ring, intake, debt hit and exsanguination bursts survive as building blocks. |
| Chamber theme | `tools/worldgen/mausoleum/themes/hemorath.py` (`chamber_sockets`, `dress_chamber`, `PIT`, `SPOKES`) | Kept; one small edit (section 9). |

### Goes (deleted outright; the mod is not released, so no save migration)

| Piece | Path | Why |
| --- | --- | --- |
| Hematic Collapse | `common/entity/boss/goal/HematicCollapseGoal.java` | A cadence goal (200/120 ticks). Its idea survives as the kit move `TITHE_RECKONING`, which reads a fight-local debt instead of the player's global blood debt. |
| Empty Pulse goal | `common/entity/boss/goal/EmptyPulseGoal.java` | A cadence goal (160 ticks) that hit every `LivingEntity` without `canHarmFor`. Survives as the kit move `EMPTY_PULSE`. |
| Vanilla goals | the `MeleeAttackGoal`, `WaterAvoidingRandomStrollGoal`, `LookAtPlayerGoal`, `RandomLookAroundGoal` and target goals in `HemorathEntity.registerGoals` | The kit controller owns locomotion, facing and targeting. `FloatGoal` stays only if the kit keeps it. |
| The secondary cycle | Hemorath's branch of `SaintSecondaryAttacks.select` and the `IRON_RAKE` / `MARROW_RUPTURE` rows of `SaintAttackPattern` | The kit replaces both; the two footprints become Hemorath kit moves with the same shapes. |
| The siphon timer | `common/entity/boss/saint/hemorath/HemorathSiphonRules.java`; `DATA_SIPHON_UNTIL`, `DATA_SIPHON_RELEASE`, `tickMarrowSiphon` | A fixed 100-tick pulse is a timer. The continuous drain becomes the kit presence; the big pulse becomes the picked move `MARROW_SIPHON`. |
| Old synced visual state | `DATA_COLLAPSE_CHARGING`, `DATA_VISUAL_TARGET`, `VISUAL_COLLAPSE_*`, `VISUAL_EMPTY_PULSE*`, `VISUAL_OVERLOAD` and `holdsVisualState` | The kit's move block replaces them; the ending is its own synced state. |
| Global blood debt use | `blood.getBloodDebt()` / `consumeDebt()` in the Collapse goal, `addDamage(amount * PHASE_2_BLOOD_SPEND_BONUS)` in `onPlayerBloodSpend`, `resetBloodDebt()` in `announceTrialEnd` | The debt that mixed health points with blood points goes; the Tithe ledger (section 4) is per fight and per player. The `IBloodVolume` debt field itself is outside this lane (kit request 9). |
| Melee self-heal | `doHurtTarget` healing 35% of attack damage | Healing now comes only from what he drinks (wells, the Gorge, the Last Thirst), so it is readable. |
| Wither sounds as his voice | `getAmbientSound`, `getHurtSound`, `getDeathSound`, the phase `WITHER_SPAWN` | Replaced by bone, iron and blood sounds (section 14). |
| The overload layer as is | `client/render/layer/mob/HemorathOverloadLayer.java` | Replaced by the new `HemorathVesselLayer` (new) that glows the vessels by meter, not by attack state. The class file is deleted; its mask texture is regenerated under the same path. |
| Old tests | `src/gameTest/.../gametest/HemorathGoalGameTests.java`; the Hemorath cases of `SaintPowerGameTests` and `SaintPowerRulesTest` | Replaced by `HemorathFightGameTests` and the JVM rules tests (section 16). The shared files' Hemorath cases are removed by the compile gate stage, not by the Saint builder. |

Removed assets (the old mask PNG, the old bbmodel) go to the untracked `removed_assets/` folder when the build happens.

## 3. Phases

Health phases decide the move set and the bar; meter states (below) decide his body, his speed and how much damage he
takes. The two layer: every phase passes through every meter state.

| Phase | Trigger | What it adds | Bar colour / name | Card |
| --- | --- | --- | --- | --- |
| 1. The Hollow | The rise ends | Presence, the leak, iron accrual, the wells. Moves: `RAKING_CLAW` (+`BACKHAND`), `STAFF_HEEL`, `STAFF_FALL`, `IRON_RAKE`, `EMPTY_PULSE`, `MARROW_SIPHON`, `CHAINED_DEBT`, `TITHE_RECKONING`, `WELLS_ANSWER`, and `RIB_BURST` when Brimming. Leak multiplier 1.0, intake multiplier 1.0, leak share to the wells 25%. | RED, "Saint Hemorath" (`entity.hemomancy.hemorath`) | None (the shared `SCENE_SAINT_RISE` opens the fight) |
| 2. The Vessel Empties | Health at or below 50%, **or** the meter reaching full in phase 1, whichever first. A first full meter does not end the fight: he empties (a forced `EXSANGUINATING_TIDE` that takes his meter to 30%), the burst tears 15% of his max health off, and phase 2 begins. | `MARROW_RUPTURE` (as `EMPTY_PULSE`'s follow-up and on its own), `SPIKED_HALO` (+`CROWN_FALL` below 35%), `IRON_THICKET`, `EXSANGUINATING_TIDE`, `TITHE_RECKONING` as `CHAINED_DEBT`'s follow-up. Wind-ups 15% shorter (never below the floors), decision delay 4 ticks shorter, leak ×1.3, intake ×1.35 (today's `OVERLOAD_PHASE_2_MULTIPLIER`), leak share to the wells 40%. A full meter now ends the fight. | PURPLE, "Saint Hemorath, The Vessel Empties" (`message.hemomancy.hemorath.saint_hemorath_the_vessel_empties`, kept) | "The Vessel Empties" (`SCENE_HEMORATH_EMPTIES_CARD`, 50 ticks, kept) |
| 2b. The Last Thirst | Phase 2, health at or below 20%, once per fight (saved flag), at the next recovery boundary | The desperation set piece (move `LAST_THIRST`, section 7): he roots in the pit, the leak stops, every well and gutter runs to him, his presence fills the chamber. It ends in overload, in his fall, or after 200 ticks in a forced vent, and phase 2 resumes. | RED, "Saint Hemorath, The Last Thirst" (new key `message.hemomancy.hemorath.saint_hemorath_the_last_thirst`); back to PURPLE and the phase-2 name when it ends | None; the bar's urns fill (section 12) |

Phase changes never cut an active move; they queue to the recovery boundary (kit rule). The phase-1 overload is the one
exception that cannot wait: a full meter cancels the active move at once, because "full" is the moment the body fails.

### Meter states

| State | Meter | Body | Locomotion | Damage taken | Decision delay | Moves it unlocks or weights |
| --- | --- | --- | --- | --- | --- | --- |
| Sunken | below 35% | the rest silhouette: frail, ribs closed, cords dried, the hollow chest open | hungry stalk, 0.20 blocks per tick | ×0.70 (iron-hard) | 14 ticks | Feeding moves ×1.5 (`MARROW_SIPHON`, `WELLS_ANSWER`, `CHAINED_DEBT`) |
| Filling | 35 to 70% | ribs part, the vessel lobes bud and fill inside the ribs, arm cords thicken | stalk, 0.15 | ×1.00 | 18 ticks | none |
| Brimming | 70% and above | ribs prised wide, the heart mass presses between them, the clavicles lift, every vessel glows | heavy sway, 0.10 | ×1.30 | 24 ticks | `RIB_BURST` unlocked; melee damage ×1.2; above 85% venting moves ×2 (`RIB_BURST`, `EXSANGUINATING_TIDE`) and feeding moves ×0.3 |

He knows he can burst: above 85% he tries to vent. The player's job at the top is to catch the vent's recovery and feed
him through it.

## 4. The signature meter: absorbed blood

**Owner.** The Saint (kit `SaintMeter`, synced as entity data). Value in blood points, the same unit as a player's
volume, so a player can read "he took 300 of mine".

**Maximum.** `M = 3000 × (1 + 0.5 × (n − 1))` for `n` challengers (1 to 4: 3000, 4500, 6000, 7500). `n` is the kit's
`challengerCount()`, counted inside the chamber at the rise's end and only rising afterwards (Resolved 2026-11-05, B-5).
`OVERLOAD_THRESHOLD` (3000) stays the solo value.

**The leak.** Every tick, with `f = value / M`:

`leak = M × (0.0004 + 0.0040 × f²) × phaseFactor × searing × gorged × fresh`

- `phaseFactor` 1.0 in phase 1, 1.3 in phase 2.
- `searing` 0.5 while Hemorath carries the Flammeus state `SchoolState.SEARING` (cauterised vessels), else 1.
- `gorged` 0 during the Gorged opening and the Last Thirst (he is digesting), else 1.
- `fresh` 0.5 for his first 1,200 ticks under the Downfall deed, else 1 (Resolved 2026-11-05, D-1).
- Never below 0 (Resolved 2026-11-05, D-1: the Downfall floor of 35% is gone).

Solo phase-1 values: 1.2 per tick at empty (24 a second), 2.7 at 35%, 4.2 at 50%, 7.1 at 70%, 9.9 at 85%, 13.2 at
full (264 a second). The curve is the fight: low down he holds what he takes; near the top only a surge gets past it.

**Where the leak goes.** 25% (40% in phase 2) of every leaked point is credited to the two wells nearest him, half
each; the rest is lost. The chamber was built to catch what he spills.

**Iron accrual.** The iron in the blood stays in the bone: every 1000 points leaked erupt one spike patch (section 7,
"Patches"), up to 9.

**What fills it** (all intake is ×1.35 in phase 2):

| Source | Rate | Notes |
| --- | --- | --- |
| Presence | up to 2.5 per tick per player (section 5) | ×1.25 from an Apotheos (spore-thick blood) |
| A cast within 24 blocks | the cast's effective cost | through the kept `onPlayerBloodSpend` hook; also adds to that player's Tithe debt |
| Earthen vein feeding within 24 blocks | the blood fed | same hook, already wired |
| Animus hits | +60 per level of `SchoolState.PRESSURE` applied to him | Animus is living flow: pressing blood into him fills him |
| `RAKING_CLAW` hit | 80 | drained from the struck player's volume |
| `MARROW_SIPHON` | 6 per tick per player in its field | |
| `GORGE` (deed) | 10 per tick | |
| `WELLS_ANSWER` | the drawn wells' whole content over 40 ticks | |
| Held Blood Projection in the Gorged opening | 20 a tick paid, 30 a tick into the meter (×1.5) | the item's reach (5.5) to his chest; never more a tick than the item moves (Resolved 2026-11-05, B-4) |
| Held Blood Projection in the Strained opening | 20 a tick paid, 40 a tick into the meter (×2) | the item's reach |
| Held Blood Projection during the Last Thirst | 20 paid, 30 in | the item's reach |
| Paying the Tithe | 20 a tick paid, 20 in | during `TITHE_RECKONING`'s wind-up, from any range along the chain |

Projection at him outside those windows does nothing ("his ribs are shut"); held Blood Absorption aimed at him is
refused with a cue (his blood answers him, not you) so it never falls through to the generic living drain.

**Overload.** `f ≥ 1`: in phase 1 the Vessel Empties (section 3); in phase 2 or the Last Thirst the ending (section 11).
The victor is the player whose blood pushed the meter over (the last contributor), or the nearest challenger if the
last point came from a well.

**The Tithe ledger** (new `HemorathTitheLedger`, per player, saved on the entity, cleared at the ending). Debt rises by
the cost of every cast or vein feed a challenger makes within 24 blocks (×1.5 under the Litany deed). It is spent by
`TITHE_RECKONING` (as damage) or paid by projection (as meter). Kit request 4 landed: each challenger's debt rides the
meter's per-player secondary value (`SaintMeter.secondary`, maximum 1000) in the `SaintMeterPacket`, so each player's own
HUD shows their links (Resolved 2026-11-05). Hemorath also syncs his current target's debt as one entity data int so
the chain drawn in the world has the right link count.

**Sources** (Resolved 2026-11-05, D-5). Every drain from a player names its own constant in `HemorathSources`:
`hemorath_presence` (the drain), `hemorath_claw` (`RAKING_CLAW`), `hemorath_siphon` (`MARROW_SIPHON`), `hemorath_tide`
(the standing tide), `hemorath_thirst` (the Last Thirst's running gutters), `hemorath_gorge` (the pull's burst),
`hemorath_vessel_offering` (held Projection into an opening or the Last Thirst), `hemorath_tithe_payment` (paying the
Tithe along the chain) and `hemorath_well_offering` (held Projection into a well), all `Category.HAZARD`, none counted
as spend, none reserve-protected.

**Thresholds.** Sunken below 35%, Filling to 70%, Brimming from 70%, the vent preference from 85%.

### The HUD meter

Drawn by the kit's `SaintMeterLayer` under the bar frame with a new `HemorathMeterStyle` (new), painted with the same
`GuiCanvas` primitives and palette as `HemorathFrame`:

- **The vial.** A 182 by 7 pixel channel, the bar fill's width, 3 pixels under the frame. Riveted hematic-iron rails
  above and below (`IRON_DARK`, `IRON`, `IRON_LIP` from the frame; rivets every 16 pixels as on the frame), a glass well
  inside drawn as the frame's `wellColor` with a 1-pixel pale glint along its top.
- **The blood.** Fills from the left to `f`, a two-tone vertical gradient (top `0xFFB0202A`, bottom `0xFF4A0A12`, the
  frame's phase-1 fill) with a 1-pixel meniscus line in the pale core (`0xFFD2C4` at 70% alpha) that wobbles by a pixel
  with his heartbeat.
- **The leak.** Drips fall from the vial's underside below the blood's leading third: 1 by 2 pixel drops that slide 6
  pixels and fade. One drip every 40 ticks at empty, every 6 at full: the leak rate is visible.
- **Thresholds.** Two iron studs on the lower rail at 35% and 70%; a cracked iron cap at the right end (the burst).
  Above 85% the cap's crack glows with the core colour.
- **Iron.** Nine small studs along the upper rail's left end; one darkens to rust-red (`0xFF70281C`) per erupted patch.
- **Openings.** Gorged and Strained flash a core-coloured line along the glass (2 pixels) that pulses for the window;
  the Last Thirst stops the drips and the blood visibly climbs.
- **Phase 2.** The rails tarnish violet exactly as the frame's (`TARNISH_*`), and the blood's bottom tone darkens to
  `0xFF2A0C3A`.
- **The target's debt** (if kit request 4 lands): a strip of dark iron links over the left of the lower rail, one link
  per 100 debt, up to 10.

## 5. Presence: the drain

Kit `SaintPresence`, owned by `HemorathPresenceRules` (new, pure).

| | |
| --- | --- |
| Radius | full drain within 4 blocks of his chest, falling linearly to nothing at 9; line of sight required (the pillar ranks are cover). During the Last Thirst: 2 per tick flat everywhere in the chamber with line of sight (radius 18). |
| Cadence | every 5 ticks (one `payUpkeep` per player per pulse, interval 5) |
| Effect, blood-active player | `BloodAccounting.payUpkeep(player, 12.5 × falloff, HEMORATH_PRESENCE, 5, false)` with the new `Source.of("hemorath_presence", Category.HAZARD, false, false)`; the paid amount (×1.25 for an Apotheos) goes into the meter. |
| Effect, inactive or empty player | 1 health of `hemomancy:saint_hemorath` damage every 20 ticks inside 4.5 blocks; each health point puts 25 into the meter |
| Exclusions | `ManipulationCombatHelper.canHarmFor(this, player)` (creative, spectator, dead, allies), and nobody during the rise, the ending or the Vessel Empties card |
| Ground read | A dark wet sheen on the floor under him out to 9 blocks, alpha 0.08 at the edge rising to 0.2 at the 4-block line, drawn with `OutpouringBrushes.ground` (shape `POOL`, Animus material); a thin ring at 4 blocks (`ribbonPath`, width 0.02, alpha 0.25). Off each drained player, fine blood motes (`HemoParticleData.bloodCell` at the small size, plus `glow` motes, never `hitGlow`) lift from the chest and drift in a gentle arc to his ribs, one per pulse per player. No tethers. |
| Sound | His heartbeat (`mausoleum.hemorath.heartbeat`) every 40 ticks at empty, quickening to every 20 at full; pitch 0.7. |

The presence is not a move and never stops for a move; it pauses only for the rise, the card and the ending.

## 6. Openings

Kit `SaintOpening`. The Saint decides when he is open; the player decides whether to take it. Multipliers stack with
the state multiplier and the patch reduction (section 7) into one damage-taken factor, applied in his `actuallyHurt`.

| Opening | Entered by | Tell | Length | What counts | Damage taken | Feeds |
| --- | --- | --- | --- | --- | --- | --- |
| **Gorged** | the recovery of a `MARROW_SIPHON` that took at least 10% of `M`, or of a `WELLS_ANSWER` that drew at least 12% of `M`; the fight's first `MARROW_SIPHON` always ends Gorged (teaching) | knees buckle, he sags onto the staff, ribs part, the heart mass glows pale core, a wet gulp | 50 ticks (replaces the move's recovery) | hits; held Blood Projection into his chest from within 6 blocks | ×1.25 | meter (30 per tick per projector), health; the leak stops for the window |
| **Strained** | the recovery of a `RIB_BURST`, or any Brimming move's recovery that ends at 85% or more | ribs lock wide with a crack, a high whine, the core glow flickers | 40 ticks | held Blood Projection within 6; any single hit of 8 or more cracks a rib (a 10-tick flinch) | ×1.0 | meter (40 per tick per projector) |
| **Planted** | `STAFF_FALL`'s recovery | the staff stuck in the floor, both hands on it, the chains swinging | 24 ticks | hits | ×1.15 | health |
| **Haloless** | `SPIKED_HALO` from its release to the end of its recovery | the bare iron crown of his skull; the halo visibly away | 56 ticks | hits | ×1.30 | health |
| **Clotted** | a Congeatio hit (`SchoolState.RIME` arriving on him) during `WELLS_ANSWER`'s wind-up or draw: the streams freeze and the draw stops | the streams stiffen to rime-dark, he jerks and staggers | 30 ticks | hits | ×1.20 | health; denies the wells |
| **Wrung** | `EXSANGUINATING_TIDE`'s commit (he has just emptied) | the ribs collapse inward, the vessels hang slack, he bows | 40 ticks | hits | ×1.40 | health |
| **Broken grasp** | a `GORGE` pull that reaches no one (Resolved 2026-11-05, A-7: there is no hold to break) | he reels back with empty arms | 30 ticks | hits | ×1.20 | health |
| **The Tithe** | `TITHE_RECKONING`'s wind-up | the iron chain from his claw to the target, the links ringing the target's waist | 36 ticks | held Blood Projection aimed at him from anywhere along the chain (10° cone, line of sight) | ×1.0 | meter (20 per tick); each point paid removes a point of debt; debt at zero on commit means no damage |

Every win runs through these windows: Gorged and Strained are the overload road's doors, Planted, Haloless, Clotted and
Wrung are the wear-down road's.

## 7. Moveset

The kit's `SaintMove` enum `HemorathMove` (new). Bands and sectors are the kit's (Resolved 2026-11-05, B-1: HUG under 3
blocks, NEAR under 6.5, MID under 12, FAR beyond; FRONT to 60° off his facing, FLANK to 120°, REAR beyond). A cell marked "half" gives half the
weight. Timelines are wind-up / active / recovery in ticks for phase 1; phase 2 takes 15% off wind-ups, never below 12
for melee and 20 for area moves. A move's kind follows resolution B-2 (AREA: not attached to the body or reaching past
7 blocks), so `CROWN_FALL`, `GORGE` and `CHAINED_DEBT` are AREA (Resolved 2026-11-05). Damage is Normal difficulty, of the damage type `hemomancy:saint_hemorath`, every
target filtered through `ManipulationCombatHelper.canHarmFor`. Aim: "track" follows the target until commit and then
locks (kit default); "lock" fixes at the move's start.

| Id | Name | Phase | Range band and sector (weight) | Tell: animation beat and world geometry | Footprint | Timeline | Damage and effect | Counterplay | Follow-ups |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| `RAKING_CLAW` | Raking Claw | 1, 2 | HUG, NEAR · FRONT 24, FLANK half | right shoulder lifts, claw fingers spread and curl back (0 to 14); three fine iron-scratch arcs appear on the floor in the cone from tick 2 | cone 100°, radius 4.5, height 0 to 2.5 | 14 / 5 / 16, track | 7, and drains 80 blood into the meter (2 more damage if the target has none). Brimming ×1.2. | step out of the cone or behind him | `BACKHAND` (HUG or NEAR, 55%, link 6) |
| `BACKHAND` | Backhand | follow-up | from `RAKING_CLAW` only | the claw swings back across the body without pausing; the scratch arcs mirror to the other side | cone 80°, radius 3.5, on the other side of the aim | 12 / 4 / 18, lock | 6, knockback 0.6 | read it off the first rake; its recovery is the punish | none |
| `STAFF_HEEL` | Staff Heel | 1, 2 | HUG, NEAR · REAR 16, FLANK half | he leans forward onto the claw and draws the staff's iron foot back past his hip (0 to 12); a short rust-crack wedge behind him | rear cone 120°, radius 3.5 | 12 / 4 / 16, lock | 6, knockback 0.8 | stop standing behind him | none |
| `STAFF_FALL` | Staff Fall | 1, 2 | NEAR, MID · FRONT 20 | both hands raise the staff overhead, the crown chains swing out and back (0 to 22); a narrow crack line runs out from his feet and two chain marks show either side of its end | line 7 by 1.4 from him along the aim, then two discs of radius 1.2 at the line's end ±1.5 to the sides, 4 ticks after the line | 22 / 6 / 24, track | line 11; chains 6 | sidestep the line, then do not drift into the chain marks | opens **Planted** |
| `IRON_RAKE` | Iron Rake | 1, 2 | MID, FAR · FRONT 20, FLANK half | he crouches and drags the claw across the floor with a bone scrape (0 to 28); the lanes show from tick 0 as cracked bone lines with rust seeping along them | 3 parallel lanes 14 long, half-width 0.5, 2 apart (4 lanes at 4 patches, 5 at 8) | 28 / 12 / 20, lock at commit | 6 and knockback per spike; spikes erupt along each lane from near to far through the active span | stand in the gaps | none |
| `EMPTY_PULSE` | Empty Pulse | 1, 2 | HUG · any 18; NEAR · any half | the ribs compress inward, the staff is planted, blood is drawn in from a ring at 6 blocks toward his chest (0 to 24) | disc radius 6 around him | 24 / 4 / 18 | 5, knockback 1.2 outward, Hemophagy for 140 ticks | leave the 6-block ring before the release | `MARROW_RUPTURE` (phase 2, always, link 8) |
| `MARROW_RUPTURE` | Marrow Rupture | 2 | MID · any half; and as the pulse's follow-up | the pulse's ring does not fade: it darkens and starts to roll outward | expanding ring wave from 0 to 10 over the active span, band 0.8, `Height.JUMPABLE` (0 to 0.9; Resolved 2026-11-05, A-3 and B-9: his one jump ring) | 20 / 24 / 24 | 6, knockback 0.45 | jump it (feet above 0.9) or be beyond 10 | none |
| `MARROW_SIPHON` | Marrow Siphon | 1, 2 | MID, FAR · FRONT 14, REAR 14, FLANK half; kite pressure adds 3 per 20 ticks spent MID or FAR in the last 200 ticks, up to +24 | staff planted, head tipped back, ribs open (0 to 24); a faint ring at 14 blocks and fine motes drawn inward along the floor | disc radius 14 around him, line of sight | 24 / 40 / 20 | each blood-active player in the field is pulled 0.05 blocks per tick toward him and drained 6 blood a tick into the meter; inactive players take 1 health every 10 ticks | break line of sight behind a pillar, leave the 14-block ring, or choke it: a single hit of 10 or more on him during the active span ends it | ends **Gorged** if it took 10% of `M` or more; the first one of the fight always does |
| `CHAINED_DEBT` | Chained Debt | 1, 2 | MID · FRONT 16; NEAR · FRONT half; kite pressure as the siphon | the reaching claw draws back and the blood-threaded chain uncoils from the fingers (0 to 18); a line of faint iron links marks the throw | line 12 by 0.8, the hook travelling it in 8 ticks | 20 / 8 / 14, track (Resolved 2026-11-05, B-2: AREA, so 20) | 3; a hooked player is reeled toward him at up to 0.4 blocks per tick until 2 blocks away (at most 20 ticks); velocity only, never a teleport | sidestep the throw | on a hook: phase 1 `RAKING_CLAW` (always, link 4); phase 2 `TITHE_RECKONING` (always, link 4) |
| `TITHE_RECKONING` | Tithe Reckoning | 1, 2 | any band within 24 · FRONT, FLANK; weight 4 + debt / 50, up to 30; ineligible below 150 debt; it targets the challenger with the most debt | claw raised high; an iron chain of one link per 100 debt hangs from the claw to the target and the same links ring the target's waist (0 to 36, tracking) | the target alone, line of sight at commit | 36 / 2 / 22, track | `min(16, 3 + debt / 80)`, debt cleared | pay it: hold Blood Projection at him along the chain (20 a tick off the debt and into the meter); or break line of sight at commit behind a pillar (the debt stays) | opens **The Tithe** during its wind-up |
| `WELLS_ANSWER` | Wells Answer | 1, 2 | any band and sector, only while he stands within the pit ring (5 blocks of its centre) and an aisle's pair of wells holds 150 or more; weight 6 + 20 × (1 − f) + pair content / 100 | he drives the staff into the pit's centre; the chosen pair's spoke gutters run wet toward the pit, front travelling from the wells over the wind-up, and streams lift off the two wells (0 to 30) | no damage footprint; the two wells and their spokes are the read | 30 / 40 / 30 | draws both wells' whole content into the meter over 40 ticks (content / 40 per tick each) and heals 1 health per 50 drawn | deny: hold Blood Absorption at a drawn well (you take 30 a tick of its flow first, and the blood is yours); or a Congeatio hit on him (Clotted). Prime: leave the wells full and let him drink | ends **Gorged** if it drew 12% of `M` or more |
| `SPIKED_HALO` | Spiked Halo | 2 | MID, FAR · FRONT 18, FLANK half | the claw reaches up and lifts the halo off his skull; its spikes lengthen with his patches (0 to 26); the out line and the return arc are drawn on the floor | path: out along the locked aim 10 blocks in 14 ticks, a 6-tick spin at its end (disc 1.5), back along an arc bowed 3 blocks to his staff side in 14 ticks; height 0.4 to 2.2 | 26 / 34 / 22, lock at commit | 8 + 1 per 3 patches per pass, Hemophagy 60 ticks; each pass hits a player once | sidestep the line, then watch the return arc | opens **Haloless**; `CROWN_FALL` below 35% health (50%, link 4) |
| `CROWN_FALL` | Crown Fall | 2, below 35% | follow-up of `SPIKED_HALO` only | he catches the returning halo overhead and brings it down at his feet; a ring of spikes shows round him as he catches | disc radius 3.5 around him | 20 / 4 / 26 (Resolved 2026-11-05, B-2: AREA, so 20; the catch fills the longer wind-up) | 12 | do not chase the halo in | none |
| `IRON_THICKET` | Iron Thicket | 2 | MID, FAR · any 14; NEAR · any half; needs 3 or more patches | he tears iron out of his own bones, the patches retracting one by one with a grind, and flings it overhead (0 to 24); a rust-crack disc marks each landing from the throw | scattered discs of radius 1.5 where the challengers' blood fell in the last 5 seconds (the places they were hit or paid blood, newest first, at least 2 blocks apart), two per patch spent, up to 6; with fewer marks than discs the rest go round the target's feet; he spends up to 3 patches (Resolved 2026-11-05, A-2) | 24 / 6 / 20, lock at the throw | 8 and Slowness II for 40 ticks; the spikes then stand for 160 ticks as hazards: 2 damage per 10 ticks and Slowness I to anyone inside | read the cracks and leave them; spending patches softens him (fewer patches, less reduction) | none |
| `EXSANGUINATING_TIDE` | Exsanguinating Tide | 2 (and forced at the phase-1 overload and at the end of the Last Thirst) | any, only at 55% or more; weight 10 + 30 × (f − 0.55) / 0.45; ×2 above 85% | both arms raised, ribs wide, the blood mass pressing out between them (0 to 30); a thin glowing line runs round the edge of the boss floor and its gutters start to fill | the boss floor (15 by 15, the arena's clear rectangle) from the floor to 0.6 blocks | 30 / 100 / 20 | he dumps 40% of his meter onto the floor. On the flood: 6 and Slowness I to anyone on the floor. While it stands: 15 blood (or 1 health) every 10 ticks from anyone still in it, into the tide, not into him. When it drains, half of what it holds runs to the four wells, a quarter each | get off the boss floor: the aisles, the processional, the dais steps | opens **Wrung** |
| `RIB_BURST` | Rib Burst | 1, 2, Brimming only | HUG, NEAR · any 20; MID · any half; ×2 above 85% | the ribs prise apart and eight floor veins glow out from him in a star, a rising whine (0 to 24) | 8 radial lanes 8 long, half-width 0.6, 45° apart (the wedges between are safe) | 24 / 10 / 24 | 9, Hemophagy 100, knockback; vents 12% of his meter | stand in a wedge, or be beyond 8 | ends **Strained** |
| `GORGE` | The Gorge | Downfall deed only, 1 and 2 | HUG, NEAR, MID · FRONT 18 | he drops low, both arms spread wide, ribs prised open, jaw-cords parting (0 to 20) | the pull: a cone of 50° and radius 9 in front of him; contact: a disc of 1.6 round his chest | 20 / 30 / 20 (Resolved 2026-11-05, A-7 and B-2: a pull, AREA) | anyone in the cone is drawn toward him at 0.12 blocks a tick (0.04 while sprinting away, so a sprint escapes); whoever reaches 1.6 blocks takes one drained burst: 6 damage and 150 blood into the meter (`hemorath_gorge`), he heals 1 per 40 drawn; no hold | sprint out of the cone, or leave it sideways | a pull that reaches no one opens **Broken grasp** |
| `LAST_THIRST` | The Last Thirst | 2b, once | forced at the trigger | tell 40 ticks: he drags himself to the pit at 0.25 blocks per tick (half damage taken while he goes), drives the staff into it, every well lifts a stream and every gutter in the chamber runs full in fronts from the wells and the processional | the whole chamber, line of sight | 40 / 200 / forced vent | leak stops; presence 2 a tick everywhere he can see; all four wells drain into him over the 200 ticks; heals 1 per 40 drawn from wells and presence; anyone standing in a running gutter loses 3 blood a tick to him. He may still pick `IRON_RAKE` and `SPIKED_HALO` while rooted (decision delay 30). Projection into him within 6 feeds 30 a tick per 20 paid. | choose the road: feed him to burst (the leak is gone, so this is the overload's best moment), or deny the wells and cut the last 20% before he heals out of it. If the 200 ticks run out, he vents (a forced `EXSANGUINATING_TIDE`) and phase 2 resumes | none |

**Strings.** Four: *Claw and Return* (`RAKING_CLAW` → `BACKHAND`), *Emptying* (`EMPTY_PULSE` → `MARROW_RUPTURE`, phase 2),
*Debt Called* (`CHAINED_DEBT` → `RAKING_CLAW` in phase 1, → `TITHE_RECKONING` in phase 2), *Crown Return*
(`SPIKED_HALO` → `CROWN_FALL` below 35%). Follow-ups ignore anti-repeat and never chain more than two deep (kit).

**Cooldowns** (ticks, from the move's end): `RAKING_CLAW` 0, `STAFF_HEEL` 40, `STAFF_FALL` 40, `IRON_RAKE` 80,
`EMPTY_PULSE` 60, `MARROW_RUPTURE` 80, `MARROW_SIPHON` 240, `CHAINED_DEBT` 120, `TITHE_RECKONING` 160, `WELLS_ANSWER`
300 (and 400 for the pair just drawn), `SPIKED_HALO` 200, `IRON_THICKET` 240, `EXSANGUINATING_TIDE` 500, `RIB_BURST` 160,
`GORGE` 200. Anti-repeat as the Mycophant: never the same move twice running, then ×0.4, ×0.6, ×0.8 for the last three
picks. Hug pressure (ticks spent in HUG over the last 200) adds 3 per 20 to `EMPTY_PULSE`, up to +18. None of these is a
cadence: every move is picked by the controller from where the player stands, what he holds and what he needs.

**Coverage.** Every HUG and NEAR cell has at least two answers in every phase (claw, backhand, heel, pulse, rib burst,
staff fall, chain); MID and FAR answer FRONT with four or more and FLANK and REAR with the siphon, the wells, the halo
and the thicket, plus his turn. The selection rules test (section 16) holds this.

**Patches.** Nine spike-patch sites on his bone, erupting in a fixed order: right forearm, right upper arm, left rib 2,
right rib 5, left forearm, left upper arm, right shin, left shin, skull. One erupts per 1000 points leaked. Each erupted
patch takes 4% off the damage he takes (9 patches: 36%), adds lanes to `IRON_RAKE` and damage to `SPIKED_HALO`, and
arms `IRON_THICKET`. Patches leave by `IRON_THICKET` (up to 3), by a Ferric hit (each level of `SchoolState.LODESTONE`
applied to him tears the newest patch out: the player commands his iron, as he once did), at the phase-1 overload (all
of them burst out) and at the ending. So a long, leaky fight makes him spikier and harder, and the player has a Ferric
answer to it.

**School answers on him** (read by polling `SchoolStates.has`/`levels` on himself each tick; no kit hook needed):
Flammeus `SEARING` halves the leak (cauterises); Congeatio `RIME` during a well draw freezes it (Clotted); Animus
`PRESSURE` pushes blood into him (+60 per level); Ferric `LODESTONE` strips a patch. The other four schools only hurt.

## 8. Locomotion: the stalk

The kit's per-Saint strategy, `HemorathStalk` (new).

- **Gait.** A staff-planted three-beat stalk: the staff plants, the left foot steps, the right foot steps. The cycle is
  24 ticks Sunken (0.20 blocks a tick), 30 Filling (0.15), 38 Brimming (0.10, with a side-to-side sway of the hips); 15%
  faster in phase 2. A hungry hollow thing comes at you; a gorged one lumbers.
- **Where he wants to be.** Sunken: HUG or NEAR, where his presence takes most. Filling: NEAR or MID. Brimming: MID, or
  the pit if a vent or a draw is wanted. When `WELLS_ANSWER` scores highest among the moves he cannot yet start (he is
  hungry below 35%, or a pair holds 1200 or more), he walks to the pit ring and the move becomes eligible there.
- **The lurch.** Sunken only: when the target has stayed out of HUG for 40 ticks and he is not in a move, he lurches 2.5
  blocks toward them in 6 ticks (a gap-closer, not an attack, and never twice inside 80 ticks).
- **Facing.** Turn rate 8° a tick Sunken, 6° Filling, 4° Brimming; 4° while a wind-up tracks; none after commit. With no
  answer for the target's cell he turns and steps toward them.
- **Pathing.** Ground navigation driven by the controller (the vanilla navigator, not goals). The pillar ranks block
  him as they block sight. He keeps to the boss floor and the aisles and never climbs the dais except at the rise and the
  ending; the kit leash holds him inside the chamber.
- **Targeting.** He wants the fullest vessel: among challengers he can see within 24 blocks, the score is `10 × (blood
  fraction) − distance`, re-scored at each pick. `TITHE_RECKONING` overrides this with the most indebted challenger.
- **Size.** His hitbox grows with the swell, through an override of `getDefaultDimensions` in his own class: 0.9 wide
  Sunken to 1.4 Brimming, 3.0 tall (the rendered figure is about 3.1 blocks; today's 0.8 by 2.2 box is too short). The
  culling box is inflated by 11 blocks while the halo is out (override of `getBoundingBoxForCulling`).

## 9. Arena

All positions below are chamber-local, as `tools/worldgen/mausoleum/pieces.py` defines them: x runs from the ring door
(the processional x 1 to 8, the hall x 9 to 25, the dais steps x 24 to 26, the apse to x 35), z is symmetric about the
centre line `CHAMBER_ZC` = 13, the boss floor is x 9 to 23, z 6 to 20 (`CHAMBER_CLEAR`), the pillar ranks stand at x 9,
13, 17, 21, 25 on z 5 and 21 (`CHAMBER_RANK` 8), the aisles run on z 3 and 23 (`CHAMBER_AISLE` 10). The kit's
`SaintArena` resolves all of this from the tomb's position and facing at the rise.

### The set pieces that take part

| Piece | Where (from `themes/hemorath.py`) | Part in the fight |
| --- | --- | --- |
| The four wells (`blood_basin`) | the aisle alcoves at x 15 and 19, z 1 (the left pair seen from the door) and z 25 (the right pair) | The shared pool. Each holds up to 1000 (fight value, kept on the entity, not in the block entity), starting at 300. Gains the leak share. Drunk by `WELLS_ANSWER` and the Last Thirst. Held Blood Absorption aimed at a well within 5.5 blocks takes 30 a tick from it into the player (up to their room); held Blood Projection into a well puts 25 a tick of the player's blood into it (new `Source.of("hemorath_well_offering", Category.HAZARD, false, false)`). The `BloodBasinBlockEntity` and its old puzzle are not touched; the kit's channel router reaches the wells (kit request 1). |
| The pit ring (`blood_gutter`, `PIT` (17, 5)) | a dry ring of radius 5 centred at (17, 0, 13), a block east of the boss floor's middle | His drinking place: `WELLS_ANSWER` and the Last Thirst need him inside it. Its gutters show his meter: fill 0 below 35%, 1 to 70%, 2 to 90%, 3 above. |
| The spokes (`blood_gutter`, `SPOKES` (15, 19)) | from the pit ring out under the arcades to the wells | Each spoke shows its well's content: fill 0 below 200, 1 below 500, 2 below 800, 3 above. During a draw a wet front runs along it from the well to the pit. |
| The west channel and the processional gutter | from the ring door down the processional into the pit, authored full | Untouched until the Last Thirst and the ending, when they run from the pit outward. |
| The east channel | from the pit to the dais steps, authored dry | The ending's road home: the blood runs along it to the tomb. |
| The dais flank basins (`venous_basin`, fill 3) | either side of the tomb's foot at (28, 5, 13 ± 6) | Drained by the Last Thirst in steps (3 to 0 over its 200 ticks); refilled by the ending. |
| The pillar ranks | z 5 and 21 | Line-of-sight cover against `MARROW_SIPHON`, `TITHE_RECKONING` and the presence. |
| The aisles, the processional and the dais steps | off the boss floor | Dry ground under `EXSANGUINATING_TIDE`. |

### What the fight changes, and how it is put back

The only blocks the fight changes are the `fill` property of the chamber's `blood_gutter` cells and of the two dais
flank `venous_basin`s. Every change goes through the arena, which records each cell's original state once at the rise
and restores it in `endTrial`, in `remove`, and on loading a Hemorath whose trial already ended (kit). Writes are
rate-limited (one change per cell per 10 ticks) and travelling fronts are scheduled cell by cell through the arena
(kit request 7), so a flood costs at most one block update per cell. The well contents, the tide, the standing iron and
every hazard are entity state and world geometry, with nothing to restore. A trial reset by the kit's empty-chamber
leash restores the same way.

The fight must still run where the scan finds no wells or gutters (GameTest pads, a damaged chamber): with no wells,
`WELLS_ANSWER` is never eligible and the leak share is lost; with no pit, the strategy treats a 5-block circle round his
rise point as the pit; the tide uses the arena's boss-floor bounds or, failing those, a 7.5-block square on his rise
point.

### The one generator edit

In `tools/worldgen/mausoleum/themes/hemorath.py`, `dress_chamber`: the pit's centre cell (17, 0, 13) becomes a
`blood_gutter` sump at fill 0, open on all four sides, so the place where his staff plants reads as a drain from the
door. Re-run the generator for Hemorath only (`python tools/worldgen/mausoleum_complexes.py`, Hemorath's pieces), and
re-check `MausoleumComplexResourceTest` and the four-seed assembly GameTests. Nothing else in the chamber moves.

## 10. Deeds

On top of the kit's stat floor (+30% attack, +15% speed, and per skipped Chapel: Downfall +50% health, Beholding
Resistance, Litany Regeneration), each of Hemorath's three Chapels lends a forced awakening a deed in his own language.
`HemorathDeeds` (new) reads the skipped kinds from `SaintTrialEntity`'s record.

| Chapel skipped | Deed | What it does |
| --- | --- | --- |
| **Beholding** (the memory of his students sealing the vessel, which must be watched correctly) | *What was not watched* | His tells are hidden: an area footprint shows only for the last 20 ticks before its commit instead of from the move's start (never less than 20 for area moves or 12 for melee), and `RAKING_CLAW` gains a held variant (50%) that freezes for 8 ticks at tick 10 of its wind-up before striking (wind-up 22). The watch his students kept was refused, so the player has to watch him instead. |
| **Litany** (the order of offerings, hidden in an inscription) | *The unsung measure* | The basins were sung to a measure that regulated what he could drink; unsung, they run free. Each well refills from the marrow by 2 a tick up to its 1000, `WELLS_ANSWER` draws all four wells at once, and Tithe debt accrues at ×1.5 (the words owed were never said). |
| **Downfall** (the Draining Pit: his fall to Hemomancy, the gorging) | *The gorging* | He fights with his fall. He rises having drunk his wells: every well empties into him and the meter starts at 25%; for his first 1,200 ticks he leaks at half rate, so he starts bloated and can still sink back to his Sunken rest; and he gains `GORGE` (Resolved 2026-11-05, D-1: no floor). |

A Silentium who woke him properly fights none of these. A forced Apotheos usually fights all three.

## 11. The ending

Either road: the meter reaching full in phase 2 or the Last Thirst, or his health reaching its last point (his
`actuallyHurt` holds health at 1 and starts the ending, so `die` never runs and the ending always passes through
`endTrial` once). New `HemorathEnding` (new) on the kit's `SaintEnding.Spec(125, 130, false, resumeOnLoad = true, IMMEDIATE, 0)`,
so a reload mid-ending carries on (Resolved 2026-11-05, B-10). He is untouchable and has no AI from the first tick; every move, hazard, standing spike and tide retires;
the presence stops.

| Ticks | Beat |
| --- | --- |
| 0 to 6 | Hit-stop. He locks rigid mid-pose; a dry crack (section 14). The HUD vial's cap shatters. |
| 6 to 30 | The burst. The ribs fly wide, the heart mass ruptures: the existing `EXSANGUINATE` burst (radius 3.5) at his chest plus eight short jets (the `RIB_BURST` jet geometry, once). The halo drops and rings off the floor. All nine patches burst out of the bone as rust flakes. |
| 30 to 80 | Exsanguination. Blood pours from his ribs and fingertips into the floor; through the arena, every gutter in the chamber floods in fronts running outward from where he stands to the pit and on; the vessel stages shrink back to dried cords; he sinks. |
| 80 to 110 | The fold. The emptied skeleton folds into its ruined vestments: knees, then the chest, then the bowed head into the robes, which settle in a heap (today's overload slump, lengthened and keyed). |
| 110 to 125 | The road home. A blood stream (`BloodStreams.stream`) runs from the heap to the nearest gutter, along the pit and the east channel to the dais steps and up into the sarcophagus' bowl; the heap thins into the stream and is gone. |
| 125 | `endTrial(victor, cause)`: the one reward roll into the victor's hands (a fragment if forced), `announceTrialEnd`, `SaintSarcophagusBlock.restoreDormant` closes the tomb, the door record is kept. The arena restores every gutter and both flank basins here. A client-only wet trace over the floor fades for 100 ticks so the restore does not snap. Then the body is discarded. |
| after | A last line to everyone in the fight (new key `message.hemomancy.hemorath.the_marrow_labours_on`: "Inside the closed tomb, the marrow labours on."), the heartbeat once more at pitch 0.5, then the chamber's own ambience. |

`announceTrialEnd` keeps its line to the victor and stops calling `resetBloodDebt` (the Tithe ledger clears with the
entity).

## 12. Visual pass

The bar is the Riposte standard (`docs/MANIPULATION_VISUALS.md`): layered shell, sinew, knots, business end and motion
for the focal moments (the Rib Burst jets, the well streams, the burst at the ending), white base tint with exactly the
two accents already in `HemorathReliquaryGeometry` (`CORE 0xFFD2C4`, `CLOT 0x7A1020`), geometry first and particles as
accents that survive Minimal particles. Overlays stay subtle and partly clear: no thick strands, no noise-contour lines.
No copper, brass, bronze or silver anywhere on him.

### Model and generator (`tools/model_export/hemorath.py`)

Kept: the hunched skeleton, the open rib cage with no torso cube, the bowed faceless skull under its cords, the long
articulated hands, the double halo, the gripped chained staff, the ragged ivory and crimson vestments, `kit.SCALE = 2`
at render scale 2.0 (2 texels per unit × 16 ÷ 2.0 = 16 px per block; Resolved 2026-11-05, B-11: 2.06 gave 15.5), the quarter-unit grid and its asserts.

New and changed parts:

1. **Spike patches.** The nine `iron_growth_*` parts become `spike_patch_0` to `spike_patch_8` on the same parents, in
   the eruption order of section 7. Each is a scab plate (1.5 by 1.25 by 0.5, `iron`) and three to five spikes, each
   spike three stacked cubes tapering 0.75, 0.5, 0.25 square in section with lengths 1.25, 1.0, 0.75, the last cube in
   `iron_tip`. The anim scales each patch from 0 (hidden) to 1, with a 1.15 overshoot on eruption; the bone under each
   site carries a rust-stain decal in the base atlas so the iron reads as under the bone before it breaks out.
2. **The vessel, in three nested stages inside the ribcage** (children of `chest`, so they move with it):
   `vessel_cords` (always shown: six cords 0.5 thick from the neck down the spine to the hips, in `dried_vessel`),
   `vessel_lobes` (four lobes, 1.5 by 2 by 1.25, two each side between the ribs, in `vessel`) and `vessel_mass` (a heart
   mass of three cubes about 2.5 by 3 by 2, an aorta 0.75 thick up into the neck, two lower sacs, in `vessel`). Each stage
   is authored at its true size and only ever scaled between 0.8 and 1.15 once shown, so texel density stays within the
   ruling; a stage buds from 0 to 0.8 over a 5% band of the meter while the stage before it is at its largest, so it
   emerges from inside. Cords show from 0, lobes from 25%, the mass from 55%. The cords compress to 0.6 thick at rest
   (they are a single texel wide either way).
3. **Arm vessels.** `left_arm_vessel` and `right_arm_vessel` (two lobes each along the humerus, `vessel`), shown from the
   Filling state.
4. **The halo** keeps its 16 outer and 12 inner segments and gains eight `halo_spike_*` on the outer ring, scaled by
   patch count. It stays a child of `chest`; during `SPIKED_HALO` the anim moves it along the move's path in model
   space from the same pure rules the server's footprint uses, so the drawn halo is where the hit is.
5. **The claw.** The right hand's fingertips gain `iron_tip` hooks (one 0.25 cube each), and a hidden `claw_chain` (three
   links from the existing `chain` helper) that shows during `CHAINED_DEBT` and `TITHE_RECKONING` as the root of the
   world-geometry chain.
6. **The face cords** split into `face_cords_left` and `face_cords_right` so the card and the Gorge can part them over
   the void; the face stays faceless.
7. **Robes.** Unchanged shapes; the lower 3 texels of the blood-cloth strips and the train take a wet gloss.

The preview sheet gains Sunken, Filling and Brimming stills, patches at 0, 4 and 9, and the halo away.

### Texture and palette (`paint` in `hemorath.py`)

| Material | Base | Detail |
| --- | --- | --- |
| `bone` | (224, 211, 185), kept | edge (170, 151, 124) and pits (123, 102, 78) kept; rust-stain decal (98, 46, 30) at 50% in a 3 by 3 texel patch at each spike site |
| `iron` | hematic iron (40, 31, 33) | corrosion toward dried-blood rust (112, 40, 28), never the old orange (151, 65, 27) that read as copper; edge +14; rare pits (86, 30, 22) |
| `iron_tip` (new) | wet blood (139, 18, 24) | one pale highlight texel (205, 120, 110) per tip; glows faintly in the mask while the patch is fresh (first 100 ticks) |
| `vessel` (new) | (104, 12, 20) | top-edge highlight (178, 40, 44), clot specks (52, 6, 10); in the glow mask, one-texel core lines every fourth row |
| `dried_vessel` (new) | (78, 28, 24) | darker every third texel; no glow |
| `blood`, `void`, `ivory_cloth`, `blood_cloth` | kept | wet gloss on the cloth hems |

Both atlases are regenerated: `hemorath.png` and the glow mask, still at `textures/entity/boss/hemorath/hemorath_overload.png`
(the path stays so the resource tests keep their pins; only its contents change).

### Layers and shaders

- `HemorathVesselLayer` (new, `client/render/layer/mob/`), replacing `HemorathOverloadLayer`: the glow mask through a
  static `RenderType.eyes` constant (built once), alpha `0.15 + 0.6 × f`, plus the opening pulses (Gorged and Strained
  lift it to 0.9), with a heartbeat `pow(max(0, sin(t × rate)), 4)` whose rate climbs from 0.12 at empty to 0.30 at full.
  Only vessel and fresh spike-tip texels are in the mask, so he glows from within and only when fed.
- No new shader. Every world effect draws on the existing materials: Animus (`LuxUmbraBatch.Material.ANIMUS`) for blood,
  Cruor for the darker clot shells, the Ferric solid `FerricDuctilisRenderTypes.IRON` for iron, through the toolkit
  primitives `LuxUmbraGeometry.ribbonPath`, `card`, `RIBBON`, `WISP`, `FOCUS`, `SLASH`, `POOL`, `OutpouringBrushes.ground`,
  `BloodStreams.stream` and `droplet`, `FerricDuctilisGeometry.spike`, the `link` and `debtLinks` helpers already in
  `HemorathReliquaryGeometry`, and `MaterialQuads.seedTile` for seeds.

### World geometry per move and impact

In `HemorathReliquaryGeometry` (kept, package-private in `client/render/world/`), split if it grows past about 600 lines
into `HemorathMoveGeometry` and `HemorathArenaGeometry` (both new, same package). The kit's `SaintTelegraphRenderer`
draws each footprint; the Hemorath style hooks (kit request 8) give the looks below.

| Move or moment | Telegraph | Active and impact | Toolkit pieces |
| --- | --- | --- | --- |
| Presence | wet sheen and 4-block ring (section 5) | motes off drained players | `OutpouringBrushes.ground` (`POOL`), `ribbonPath`, `HemoParticleData.bloodCell` / `glow` |
| The leak | none | drops from the rib undersides and fingertips; small floor stains that fade over 80 ticks where they land | `BloodStreams.droplet`, `ground` (`POOL`) |
| `RAKING_CLAW`, `BACKHAND` | three thin scratch arcs in the cone | claw streaks on the swing; on a hit the existing `MELEE` slash burst and a short intake line of motes to his chest | `slashOffsets`, `SLASH`, existing `MELEE` and `INTAKE` kinds |
| `STAFF_HEEL` | short rust-crack wedge behind | a dust-free iron scuff ring at the foot | `ribbonPath` (`SLASH`), `link` |
| `STAFF_FALL` | narrow crack line and two chain marks | iron links fall along the chains; a crack splits along the line on impact | `link`, `SLASH` ribbons |
| `IRON_RAKE` | cracked bone lanes with rust seep (the old `SaintPatternGeometry` lane look, moved here) | spikes erupt near to far in iron, each a five-spike cluster that sinks back over 20 ticks | `FerricDuctilisGeometry.spike` in `IRON` |
| `EMPTY_PULSE` | inward ring at 6 and six rising strands (the existing pulse body) | the existing `PULSE_RELEASE` ring | kept geometry |
| `MARROW_RUPTURE` | the pulse ring darkening | a travelling ring with a thin pale crest at 0.9 high (the jump height made visible) | `ribbonPath` ring, `CORE` crest |
| `MARROW_SIPHON` | faint ring at 14; motes drawn along the floor | one thin ribbon (width 0.02, alpha 0.3) from each drained player's chest to his ribs, plus motes; never a tether rope | `ribbonPath`, motes |
| `CHAINED_DEBT` | faint iron links along the throw line | a real chain of iron links from the claw to the hook, with a thin blood thread through the links | `debtLinks`, `link`, `ribbonPath` (width 0.02) |
| `TITHE_RECKONING` | the chain to the target, its link count the debt; the same links circling the target's waist | the existing `DEBT_HIT` burst on the target; when paid, the links unthread one by one toward him | `debtLinks`, `DEBT_HIT` |
| `WELLS_ANSWER` | wet fronts along the spokes (blocks) and streams lifting off the wells | one blood stream per drawn well: up out of the well, along the spoke and up into his chest, width 0.06 narrowing to 0.03, with the Riposte layering (shell `WISP` ribbon at 0.28 alpha, two sinew strands, `FOCUS` knots at the well and the chest) | `BloodStreams.stream`, `ribbonPath`, `card` |
| `SPIKED_HALO` | the out line and the bowed return arc on the floor | the model halo in flight, a short motion trail and `SLASH` streaks on each pass | `ribbonPath` trail, `SLASH` |
| `CROWN_FALL` | a ring of spike stubs round him | iron spikes in a ring as the halo lands | `spike` in `IRON` |
| `IRON_THICKET` | rust-crack discs at each landing | clusters of standing spikes for 160 ticks, rusting dark and sinking over their last 40 | `spike`, `ground` (`SLASH`) |
| `EXSANGUINATING_TIDE` | a thin glowing line round the boss floor's edge | a flat blood surface over the boss floor (1-block `POOL` cards in a grid, alpha 0.55, slow ripple), a meniscus ribbon on its edge; when it drains, streams run into the gutters | `ground` (`POOL`), `ribbonPath`, `BloodStreams` |
| `RIB_BURST` | eight thin vein lines glowing out from him | eight jets in the Riposte layering: shell, three sinew strands, a `FOCUS` knot at the rib, a `SLASH` tip with the clot accent | `ribbonPath`, `card`, `SLASH` |
| `GORGE` | none beyond the pose | the held player ringed by the arm vessels; motes into his chest | motes, `INTAKE` |
| `LAST_THIRST` | every gutter running full (blocks), the four streams | the streams and the flank basins draining; a faint ring at 18 | as `WELLS_ANSWER`, `ribbonPath` |
| Patch eruption | none | a small iron bloom round the site (three short spikes that sink in 10 ticks) and rust flakes | `spike` in `IRON`, `bloodCell` in rust |
| Phase-1 overload | the vial's cap glows | the existing `PHASE_BREAK` iron-link burst, then the tide | kept geometry |
| The ending | section 11 | the existing `EXSANGUINATE` burst, the jets, the streams home | kept and stream geometry |

### HUD meter art and the bar frame

The meter is in section 4. `HemorathFrame` changes: notches at 0.5 and 0.2 (`notches()` returns both); a third phase
read from the new Last Thirst name key (`phaseOf`): the frame's two empty urns fill with blood rising from their base
(a `disc` of the phase-1 fill colours climbing with the Last Thirst's progress, the core meniscus on top), and its rails
go back to iron; the tarnish stays the phase-2 look. `BossBarFramePhaseSaintTest` gains the third phase.

## 13. Animation

All Java in `tools/model_export/hemorath_anim.java`, regenerated into `HemorathModel`. Each move has its own track keyed
on the kit's move id and replayed move tick (`BossReplayClock.sample` per frame from the synced start tick), written
with Annetta-style Catmull-Rom keys (`curve(t, t0, v0, t1, v1, ...)` copied from `annetta_knowles_anim.java`, with
`ramp` and `window`). Every frame starts with `resetPose`; the staff and the shoulder chains keep their counter-rotation
from today's code. Move-to-move changes blend over 5 ticks from the last pose (as the Mycophant's `blendFromLast`; Resolved 2026-11-05,
B-8).
Swell (`f` smoothed client-side over 10 ticks), patches and the opening are additive layers over every track.

| Track | Length (ticks) | Beats |
| --- | --- | --- |
| `idle_sunken` | loop 80 | slow shallow breath (chest x ±0.02), head bowed, claw fingers twitch every 40, cords sway |
| `idle_filling` | loop 64 | deeper breath, ribs breathe open and shut ±0.05, staff hand tightens |
| `idle_brimming` | loop 48 | laboured breath, ribs held wide, hips sway ±0.04, clavicles raised, a shudder every 24 |
| `stalk` | 24 / 30 / 38 by state | 0 staff plants, 0 to 8 left step (left leg forward 0.35), 8 to 16 right step, the claw dragging; the chest pitches forward on each step; Brimming adds a hip sway |
| `lurch` | 6 | the torso throws forward, claw reaching, legs trail |
| `turn` | 10 | shoulders lead, staff pivots, feet follow |
| `raking_claw` | 35 | 0 shoulder lifts; 6 fingers spread; 14 commit, the swing (fast, x −1.6 to +0.9 on the arm); 19 follow-through; 19 to 35 recover |
| `backhand` | 34 | 0 to 12 the claw swings back across (z −0.8 to +0.6), body counter-twists; 12 strike; 16 to 34 recover, off balance |
| `staff_heel` | 32 | 0 lean onto the claw; 4 to 12 staff drawn back; 12 drive; 16 to 32 recover |
| `staff_fall` | 52 | 0 to 18 staff raised in both hands, chains swing out; 22 commit, the fall; 24 impact (head down, knees bend); 28 to 52 planted, leaning on it, chains swinging |
| `iron_rake` | 60 | 0 to 10 crouch; 10 to 28 claw drags across the floor in an arc, right forearm low; 28 to 40 the drag completes as the spikes run; 40 to 60 rise |
| `empty_pulse` | 46 | 0 to 18 ribs compress (rib z −0.15), chest folds; 24 release, ribs fling open; 28 to 46 recover |
| `marrow_rupture` | 68 | from the pulse's last pose; 0 to 20 stays folded, head lifts; 20 to 44 ribs pump twice; 44 to 68 recover |
| `marrow_siphon` | 84 | 0 to 12 staff planted; 12 to 24 head back, ribs open; 24 to 64 held draw (chest rises with intake); 64 to 84 lower; or into `gorged` |
| `chained_debt` | 40 | 0 to 18 claw draws back, chain uncoils; 18 throw (arm extends fully, fingers open); 18 to 26 the reel (arm hauls back); 26 to 40 recover |
| `tithe_reckoning` | 60 | 0 to 36 claw raised high, tracking, fingers clench a link at a time (one clench per 100 debt); 36 strike (fist closes, arm drops); 38 to 60 recover |
| `wells_answer` | 100 | 0 to 12 staff driven into the pit; 12 to 30 both arms spread toward the wells; 30 to 70 the draw (head back, chest swells with each well's stream); 70 to 100 lower, or into `gorged` |
| `spiked_halo` | 82 | 0 to 14 claw reaches up; 14 to 26 lifts the halo off; 26 throw; 26 to 60 bare-headed, tracking it; 60 catch; 60 to 82 recover |
| `crown_fall` | 42 | 0 to 12 halo caught overhead in both hands; 12 slam down; 16 to 42 crouched recovery |
| `iron_thicket` | 50 | 0 to 18 the claw tears at the other arm's patches, then the ribs, one beat per patch; 18 to 24 the fling overhead; 24 to 50 recover, holding the torn arm |
| `exsanguinating_tide` | 150 | 0 to 30 arms raised, ribs wide, the mass pressing out; 30 release, arms fall; 30 to 70 `wrung` sag; 70 to 150 he stands slowly as the tide stands |
| `rib_burst` | 58 | 0 to 24 ribs prise apart (spread to 0.45 rad), shoulders back; 24 vent; 34 to 58 into `strained` |
| `gorge` | 70 | 0 to 20 crouch, arms wide, ribs prised open, face cords part; 20 to 50 the pull (arms beckoning in, chest heaving; a snap closed on contact); 50 to 70 straighten (Resolved 2026-11-05, A-7: no hold) |
| `last_thirst` | 240 | 0 to 40 the drag to the pit; 40 staff driven; 40 to 240 rooted draw, arms spread to all four wells, swell rising, shudders every 20; ends into the vent |
| Openings | 50 / 40 / 24 / 56 / 30 / 40 / 30 | `gorged`: knees bend 0.3, sags onto the staff, ribs part, head lolls; `strained`: ribs locked wide, shoulders shaking at 0.5 Hz; `planted`: both hands on the staff; `haloless`: head bare and raised; `clotted`: a jerk at 0 then a stagger; `wrung`: ribs folded in, bowed; `broken grasp`: reels back two steps |
| `patch_erupt` (additive) | 8 | the site's patch scales 0 to 1.15 to 1, the limb jolts 0.1 rad |
| `flinch` (additive) | 8 | a hit on him: head and chest jolt away from the hit, 0.08 rad |
| `vessel_empties` (phase card) | 50 | 0 to 10 rigid; 10 to 30 ribs fling wide and the mass empties (swell eased to 0.3); 30 to 50 bows, then straightens to phase 2's stance, face cords parted for 20 ticks |
| `ending` | 125 | the beats of section 11: rigid 0 to 6, burst 6 to 30, sink 30 to 80, fold 80 to 110, fade 110 to 125 |
| Rise | as `SaintRise` | kept; the shared rise drives the sink; the first 20 ticks after it play `idle_sunken` with the head lifting toward the nearest challenger |

## 14. Sound

No new audio. Mod events by id from `SoundInit`; vanilla events by id. Pitches below.

| Beat | Sound | Volume, pitch |
| --- | --- | --- |
| Heartbeat (presence) | `mausoleum.hemorath.heartbeat` | 1.0, 0.7; interval 40 ticks at empty to 20 at full |
| Leak drips | `mausoleum.hemorath.drip` | 0.4, 0.9 to 1.2 random, one per HUD drip |
| Ambient | `minecraft:entity.skeleton.ambient` | 0.8, 0.5 (replaces wither ambient) |
| Hurt | `minecraft:entity.skeleton.hurt` | 1.0, 0.55; Brimming adds `minecraft:entity.generic.drink` at 0.5, 0.6 |
| Claw wind-up / swing / hit | `minecraft:block.bone_block.hit` / `minecraft:entity.player.attack.sweep` / `item.blood_vial.inject` | 0.9, 0.6 / 1.0, 0.6 / 1.0, 0.6 |
| Staff heel and fall | `minecraft:block.chain.place` at the lift, `minecraft:block.anvil.land` at impact | 1.0, 0.6 / 1.0, 0.5 |
| Iron Rake drag and spikes | `minecraft:block.grindstone.use` for the drag, `minecraft:block.pointed_dripstone.place` per spike lane | 1.0, 0.5 / 0.8, 0.6 |
| Empty Pulse charge / release | `minecraft:entity.warden.sonic_charge` / `minecraft:entity.warden.sonic_boom` | 1.5, 0.8 / 1.6, 0.7 (lower than today's 1.0) |
| Marrow Rupture roll | `minecraft:block.bubble_column.whirlpool_inside` | 1.0, 0.6 |
| Marrow Siphon draw | `entity.naeglerophaeon.drain` | 1.2, 0.6, every 20 ticks of the draw |
| Chain throw and reel | `minecraft:block.chain.break` / `minecraft:block.chain.step` | 1.0, 0.7 / 0.8, 0.5 |
| Tithe wind-up / strike / paid | `minecraft:block.chain.hit` per link clench / `minecraft:block.iron_door.close` / `rite.offering_drawn` | 0.8, 0.6 / 1.0, 0.5 / 1.0, 0.8 |
| Wells Answer plant / draw | `rite.staff_impact` / `rite.offering_drawn` | 1.0, 0.7 / 1.0, 0.6 |
| Spiked Halo throw / return / catch | `minecraft:item.trident.throw` / `minecraft:item.trident.return` / `minecraft:block.anvil.place` | 1.0, 0.5 / 1.0, 0.6 / 1.0, 0.5 |
| Iron Thicket tear / landing | `minecraft:block.iron_trapdoor.open` per patch / `minecraft:block.pointed_dripstone.land` | 1.0, 0.5 / 1.0, 0.6 |
| Tide rise / standing / drain | `minecraft:item.bucket.empty` / `minecraft:ambient.underwater.loop` (short) / `minecraft:block.bubble_column.upwards_inside` | 2.0, 0.5 / 0.5, 0.6 / 1.0, 0.6 |
| Rib Burst whine / vent | `rite.escalate` / `minecraft:entity.generic.explode` | 1.0, 0.7 / 1.0, 0.8 |
| Gorge catch / hold | `entity.vesper.grab_bite` / `minecraft:entity.generic.drink` | 1.0, 0.6 / 0.8, 0.5 |
| Gorged gulp | `minecraft:entity.generic.drink` | 1.2, 0.4 |
| Strained crack | `minecraft:block.bone_block.break` | 1.2, 0.5 |
| Patch erupt / patch torn | `minecraft:block.pointed_dripstone.place` / `minecraft:block.chain.break` | 0.8, 0.7 / 1.0, 0.6 |
| Threshold crossings (35%, 70%) | `rite.anchor_fill` | 0.8, 0.6 / 0.8, 0.8 |
| Phase-1 overload | `rite.backlash` then the card | 2.0, 0.6 |
| Last Thirst | `rite.ambient.heartbeat` for its length, every gutter `mausoleum.hemorath.drip` in bursts | 1.2, 0.6 |
| Ending: crack / burst / pour / fold / tomb closes | `minecraft:block.bone_block.break` / `rite.backlash` / `minecraft:item.bucket.empty` / `rite.collapse` / `rite.seal` | 1.5, 0.4 / 2.0, 0.5 / 1.5, 0.5 / 1.2, 0.6 / 1.0, 0.7 |

## 15. Multiplayer and both paths

**One to four players.** `M` scales by `1 + 0.5 × (n − 1)` and the leak scales with it, so the meter feels the same;
presence drains every challenger in range, so more players fill him faster. Max health scales by the same
`1 + 0.5 × (n − 1)` (600, 900, 1200, 1500; Resolved 2026-11-05, B-5: one coefficient), through the kit's
`healthPerExtraChallenger` and `challengerCount()`, raised (never lowered) when a challenger joins. Wells stay at
1000 each. Every projector in an opening pays and feeds separately. Debt is per player; the Reckoning picks the most
indebted. He targets the fullest visible vessel, so a full-blood player draws him and a drained one gets a moment. The
overload victor is the last contributor. Shared hazards (the tide, the thicket, the rupture) hit each player once per
pulse.

**Silentium (usually proper).** No deeds; the fight as written. Tolerated on hallowed ground. Their tools are the
whole player kit: casts (feed and debt), held Projection into the openings and the wells, held Absorption from the
wells, school answers (Flammeus cauterise, Congeatio clot, Animus press, Ferric strip), and movement round the pillars.

**Apotheos (usually forced).** All three deeds as a rule, the stat floor, and the hallowed ground burning them
(4 spore load a second, then 1 `hallowed_burn` a second). The fight is designed so the Apotheos can finish faster on the
overload road: their drained blood feeds him ×1.25, the Downfall deed starts him at 50% and floors him at 35%, and the
Regeneration deed works against the wear-down road. The wells are their sustain: absorbed well blood pays for Sanguine
Mending against the burn. A forced fight should be harder (hidden tells, four-well draws, the Gorge) and not longer: on
the overload road its target length is three to four minutes. Morphling abilities hit him as ordinary damage.

**Worked fight length, proper awakening, solo prepared D8** (Resolved 2026-11-05, B-6; the verify stage tunes against
it). The rise (80 ticks) is not counted.

- *Overload road.* Passive presence (about 1.5 a tick averaged over NEAR) against the leak settles him near 30%: he
  holds what he takes only low down. The first `MARROW_SIPHON` comes in the first 40 to 60 seconds (kite pressure) and
  always ends Gorged; one Gorged window of 50 ticks fed at 20 a tick puts in 1,500 with the leak stopped, so he
  reaches Brimming at about 80%. Above 85% he vents (`RIB_BURST`, 12% out) and its Strained window of 40 ticks takes
  him the rest of the way (1,600 in): the first full meter at about 2 to 2.5 minutes, then 50 ticks of card and the
  forced tide. Phase 2 restarts at 30% with intake x1.35 and leak x1.3: one more Gorged or Strained window and the
  wells primed (about 2,100 needed) take another 1.5 to 2 minutes. Total about 4 to 4.5 minutes.
- *Wear-down road.* 600 health plus about 90 healed back (two pair draws at 1 per 50, the Last Thirst at 1 per 40).
  Melee uptime of about 40% at about 12 raw damage a second, through Sunken x0.7 and a few patches, gives about 3.6
  effective damage a second: 690 / 3.6, about 190 seconds of fighting, plus about 50 seconds when he cannot be hit or
  must not be stood near (the card, the tide on the floor, the Last Thirst's drag). Total about 4 minutes.
- *Mixed* (the usual fight: cut him while he is swollen, feed him through his openings): about 4 to 5 minutes.
- *Forced, overload road.* The Downfall start at 25% with half leak for the first minute skips most of the first climb;
  the hidden tells and the Gorge make it harder. About 3 to 3.5 minutes.

## 16. Tests

### JVM rules tests (`src/test/java/com/vincenthuto/hemomancy/common/entity/boss/saint/hemorath/`, all new unless noted)

- `HemorathMeterRulesTest`: the leak curve at 0, 0.35, 0.5, 0.7, 0.85 and 1; scaling with 1 to 4 challengers; phase
  factor; `SEARING` halving; Gorged and Last Thirst pause; the Downfall floor; the leak share to the two nearest wells;
  iron accrual every 1000 and the cap of 9; the overload threshold by phase.
- `HemorathPresenceRulesTest`: falloff 4 to 9; cadence 5; the Apotheos factor; inactive players to health at 25 per
  point; the Last Thirst's flat 2 and radius 18; nothing during the rise, card or ending.
- `HemorathWellRulesTest`: absorb 30 and project 25 a tick, capped by content, capacity and player room; the draw over
  40 ticks; the heal of 1 per 50; denial taking the flow first; the Litany refill of 2 a tick and the four-well draw;
  the tide's half returning a quarter to each well.
- `HemorathSelectionRulesTest`: every HUG and NEAR cell has two or more answers per phase; the state multipliers
  (feeding ×1.5 Sunken, venting ×2 and feeding ×0.3 above 85%); wells only in the pit; the Tithe only from 150 debt;
  kite and hug pressure; anti-repeat; follow-ups at most two deep; a simulated fight of a few thousand picks with a
  scripted player in every cell where no move takes more than a third of picks.
- `HemorathMoveTimelineTest`: every wind-up at least 12 (melee) or 20 (area) in both phases and under the Beholding
  deed; recoveries and openings as tabled; phase 2's 15% cut and its floors.
- `HemorathFootprintTest`: hits and misses for each move (lane gaps by patch count, the rupture's 0.9 jump height, the
  halo's out line and bowed return, the rib burst's wedges, the tide's boss-floor rectangle and its 0.6 height).
- `HemorathTitheRulesTest`: accrual from spends within 24, the Litany ×1.5, payment 20 a tick, the damage formula and
  its cap of 16.
- `HemorathDamageRulesTest`: the stacked damage-taken factor (state × patches × opening, with Resistance on top).
- `HemorathPhaseRulesTest`: phase 2 from 50% health or from the first full meter (tide forced, 15% burst, meter to 30%);
  the Last Thirst once at 20% and its three exits; the ending from either road.
- `HemorathEndingRulesTest`: the beat ticks and the single `endTrial` at 125.
- `HemorathVisualRulesTest` (kept file, rewritten): swell stages show at 0, 25 and 55%, scales stay within 0.8 to 1.15
  once shown, patch eruption overshoot, rib spread by state.
- `HemorathModelTest` (kept, updated): the hollow chest test exempts the `vessel*` parts and checks the rest pose hides
  the lobes and the mass; the staff stays upright in every track; every new part exists.
- `HemorathReliquaryGeometryTest` (kept, updated) and `HemorathPresentationGuardrailTest` (kept, rewritten): the
  geometry hooks sit before the shared Animus/Mortem branch; no `ParticleTypes` in any Hemorath file; the two deleted
  goals are gone; Hemorath never reads `getBloodDebt` or calls `consumeDebt`/`addDamage`; no `RenderType` built per frame
  in the new layer.

### GameTests: `HemorathFightGameTests` (new, `src/gameTest/java/com/vincenthuto/hemomancy/gametest/`), batch `hemorath_fight`

Replaces `HemorathGoalGameTests` and the Hemorath cases of `SaintPowerGameTests`. Each case with a real mock player:

1. `presenceDrainsActiveBloodIntoTheMeter` (and the paid amount matches the ledger row `hemorath_presence`).
2. `presenceHurtsAnEmptyPlayer`.
3. `aPillarBlocksThePresenceAndTheSiphon`.
4. `theMeterLeaksFasterWhenFuller`.
5. `castingNearHimFeedsTheMeterAndTheTithe` (through `onPlayerBloodSpend`).
6. `paidTitheDealsNoDamage` and `unpaidTitheDealsItsDebt`.
7. `firstFullMeterEmptiesIntoPhaseTwoWithoutEnding` (card, tide, 15% burst, meter at 30%, no reward).
8. `fullMeterInPhaseTwoEndsTheTrialOnceWithTheReward` (victor is the last feeder).
9. `wearingHimDownEndsTheTrialOnce` (health never reaches 0, `endTrial` once).
10. `wellsAnswerDrawsAPairAndHeals`; `absorbingADrawnWellDeniesHim`; `projectingPrimesAWell`; `aRimeHitClotsTheDraw`.
11. `gorgedOpeningFeedsHeldProjection` and `projectionOutsideAnOpeningDoesNothing`.
12. `ironAccruesFromTheLeakAndALodestoneHitStripsAPatch`; `ironThicketSpendsPatches`.
13. `theTideHitsTheBossFloorButNotTheAisles`.
14. `lastThirstRunsOnceAndStopsTheLeak`.
15. `arenaRestoresGuttersAfterTheEnding`, `...AfterRemoval`, `...AfterReloadOfAnEndedSaint`, `...AfterTheLeashReset`.
16. `eachDeedApplies` (Beholding late footprints, Litany four-well draw and refill, Downfall start, floor and Gorge).
17. `twoChallengersScaleTheMeterAndHealth`.
18. `theGorgeBreaksOnACast`.
19. `noArenaStillFights` (an empty pad: no wells, a virtual pit, the tide on its fallback square).

Fixtures that need the real chamber use the mausoleum test template (`/hemo test mausoleum hemorath` pieces) as the
assembly GameTests do.

### Review client: `HemorathFightClientReview` (kept file, rewritten), `-PhemorathFightReview` on `bossPresentationReviewClient`

Stops (one still each unless noted; posed against a dummy where a target is needed, a real player for openings):
rise and the first siphon (teaching, three stills: wind-up, draw, Gorged); idle in Sunken, Filling and Brimming;
patches at 0, 4 and 9; each move at its tell, its commit and its active peak (`RAKING_CLAW`, `BACKHAND`, `STAFF_HEEL`,
`STAFF_FALL`, `IRON_RAKE`, `EMPTY_PULSE`, `MARROW_RUPTURE`, `MARROW_SIPHON`, `CHAINED_DEBT`, `TITHE_RECKONING` unpaid and
paid, `WELLS_ANSWER` primed and denied, `SPIKED_HALO` out and back, `CROWN_FALL`, `IRON_THICKET`, `EXSANGUINATING_TIDE`,
`RIB_BURST`, `GORGE`); each opening; the phase-1 overload card; the Last Thirst from the door; every ending beat; the HUD
meter at 0, 35, 70 and 95%, with drips, in phase 2 tarnish and during the Last Thirst; the bar frame's three phases.
The report keeps `passed`, and adds one flag per move that reached its active span, as today's `iron_rake` and
`marrow_rupture` flags do. Minimal particles must still show every telegraph.

## 17. Build list

Inside Hemorath's lane as the umbrella defines it. The kit is built and frozen before this starts. No Gradle in this
stage; compiling and tests happen in the compile gate and verify stages.

1. **Rules first** (pure, no Minecraft types) in `common/entity/boss/saint/hemorath/`: `HemorathMeterRules` (leak,
   scaling, thresholds, iron accrual), `HemorathPresenceRules`, `HemorathWellRules`, `HemorathTitheRules`,
   `HemorathIronRules` (patch order and effects), `HemorathDamageRules`, `HemorathPhaseRules`, `HemorathEndingRules`,
   `HemorathHaloPath` (the halo's position by move tick, shared by the footprint and the model), and the rewritten
   `HemorathVisualRules`. Delete `HemorathSiphonRules`. Write their JVM tests beside them.
2. **The moves.** `HemorathMove` (enum implementing the kit's `SaintMove`: timelines, footprints, aims, bands and sectors
   with weights, cooldowns, phases, follow-ups, openings) and `HemorathMoves` (the `onTick`/`onHit` hooks:
   damage through the kit's `hemomancy:saint_hemorath` type and `canHarmFor`, drains through `BloodAccounting` with the
   named sources, pulls by velocity). The selection extras (state multipliers, kite and hug pressure, debt weighting,
   the pit condition) as the move's weight hook or kit request 3.
3. **The state.** `HemorathTitheLedger`, `HemorathWells` (four well contents, the leak share, absorb/project/draw),
   `HemorathPatches` (count and order), saved in the entity's NBT; the openings as kit `SaintOpening`s; the deeds in
   `HemorathDeeds`; the stalk in `HemorathStalk`; the arena hooks in `HemorathArena` (set-piece scan of `blood_basin`,
   `venous_basin` and `blood_gutter`, the fill tiers, the fronts, the fallbacks).
4. **The entity.** Rewrite `HemorathEntity` onto the kit: attributes, the meter, presence, controller and phase table
   wiring, the hitbox and culling overrides, `actuallyHurt` with the damage factor, the health floor and the ending
   start, the school-state polling, the synced extras (swell, patches, the four well levels as one packed int, the
   target's debt, the ending tick), `onPlayerBloodSpend` routed to the meter and the ledger, save and load. Add
   `HemorathEnding`. Keep `rewardsGoToVictor` and `announceTrialEnd`.
5. **Delete** `common/entity/boss/goal/HematicCollapseGoal.java` and `EmptyPulseGoal.java` (Hemorath-only files the
   umbrella names as replaced) and Hemorath's use of `SaintSecondaryAttacks`.
6. **Lang** (add-only, `en_us.json`, under `message.hemomancy.hemorath.*` and `boss.hemomancy.hemorath.*`): the Last Thirst
   bar name, the teaching lines (first drain "Your blood answers him.", first Brimming "His ribs part. He cannot hold
   it.", first Gorged "He sags, gorged. Feed him, or cut him.", first Tithe "He reaches for what you owe."), the
   ending line, the source labels `blood_source.hemomancy.hemorath_presence` and `hemorath_well_offering`, the death
   messages for `saint_hemorath` if the kit leaves them to the Saint. Remove the keys only the deleted goals used once
   nothing reads them.
7. **Generator.** `tools/model_export/hemorath.py`: patches, vessel stages, arm vessels, halo spikes, claw hooks and
   chain, split face cords, palette, glow mask, preview stills. Then `tools/model_export/hemorath_anim.java`: every
   track of section 13. Run `python tools/model_export/hemorath.py --preview`; check the stills and the texel density.
8. **Client state and layer.** Rewrite `HemorathAnimState`; add `HemorathVesselLayer` and delete
   `HemorathOverloadLayer`; update `HemorathRenderer` (one scale, the layer, the halo attachment).
9. **World geometry.** Extend `HemorathReliquaryGeometry` (or split into `HemorathMoveGeometry` and
   `HemorathArenaGeometry`) with section 12's table, and the Hemorath telegraph styles for the kit's
   `SaintTelegraphRenderer`. Move the rake-lane look out of `SaintPatternGeometry` only if the kit has not already.
10. **HUD and frame.** `HemorathMeterStyle` beside the kit's `SaintMeterStyle`; `HemorathFrame` notches and phase 3.
11. **Chamber.** The pit sump in `tools/worldgen/mausoleum/themes/hemorath.py`; re-run the generator for Hemorath only.
12. **Tests.** The JVM tests of section 16; `HemorathFightGameTests`; delete `HemorathGoalGameTests`; rewrite
    `HemorathFightClientReview` with the new stops. Leave the shared test files' Hemorath cases for the compile gate.
13. **Spec follow-ups.** Record anything the kit could not give under "Kit follow-ups" at the end of this file.
## 18. Kit requests

Beyond the ten parts, or changes to them, with the reason. Each names the in-lane workaround if the kit says no.

1. **A channel router for held Blood Absorption and Blood Projection** (`SaintChannelRouter`, new in
   `common/entity/boss/saint/`), called from `BloodAbsorptionItem.onUseTick` (after the Vesper hook),
   `BloodProjectionItem.performProjection` (before the block dispatch) and the Living Staff's equivalents. It asks the
   Saint in range whether it takes the channel at the Saint's body (with a reach the Saint gives: 6 for Hemorath's
   openings, the chain line for the Tithe) or at an arena set piece (the wells). It returns the blood moved. The umbrella
   says openings count held Projection, so the kit needs this; those item files are outside every Saint lane. No
   workaround: without it the overload road loses its main door.
2. **More footprint shapes**: `RADIAL` (n spokes from the origin with a half-width and length, for `RIB_BURST`),
   `REGION` (the arena's boss-floor rectangle with a height band, for the tide), and a union of footprints (the Staff
   Fall's line plus chain discs). Workaround: compose lines in Hemorath's own hit code and draw them himself.
3. **A weight hook on `SaintMove`**: `weight(context)` with the meter fraction, the target's debt, pressure counts and an
   `eligible(context)` predicate (in the pit, a pair with 150, three patches). Hemorath's choice depends on what he
   needs, not only where the player is. Workaround: Hemorath zeroes cooldown-free weights by forcing cooldowns.
4. **A per-player secondary meter beside the Saint meter**, through the same `SaintMeterPacket`: the Tithe debt per
   challenger, drawn on each player's own HUD meter. Workaround: entity data with the current target's debt only.
5. **A dynamic presence**: radius and amount as functions of the Saint's state (the Last Thirst's 18 blocks flat), and a
   per-player multiplier (the Apotheos ×1.25). Workaround: a second presence object swapped in for the Last Thirst.
6. **A dynamic leak and a floor on `SaintMeter`**: `leak(context)` per tick rather than a constant, and a floor (the
   Downfall deed). Workaround: Hemorath subtracts his own leak and holds a floor himself.
7. **Arena block edits with a schedule**: `arena.set(pos, state)` that records the original once, plus
   `arena.schedule(pos, state, delayTicks)` for travelling fronts, rate-limited per cell, restored by the arena. Plus a
   set-piece scan by block id that returns `blood_basin`, `venous_basin` and every `blood_gutter`. Workaround: a
   Hemorath-side scheduler that still writes through `arena.set`.
8. **Telegraph style hooks**: the kit's `SaintTelegraphRenderer` calls a per-Saint style by move id, so Hemorath's lanes
   draw as bone cracks with rust seep and his rib-burst spokes as vein lines, in his own geometry class. Workaround:
   Hemorath draws his own telegraphs from the synced move block and asks the kit renderer to skip his moves.
9. **The old blood debt.** Hemorath stops reading `IBloodVolume` blood debt. `BloodAccounting.Source.countsAsSpend`
   still adds to it ("adds a satisfied payment to the Hemorath blood debt") and nothing else reads it. The integration
   stage may retire the field and the flag or leave them; the Saint builder does not touch them.
10. **A phase trigger by event**: `SaintPhaseTable.advance(reason)` so the first full meter in phase 1 can start phase 2
    with its card; and the rule that this one change cancels the active move instead of queueing. Workaround: Hemorath
    lowers his own health to 50% at the overload, which would read wrongly on the bar.
11. **A generic boss hold** lifted from the Mycophant's hold kinds (client-pinned, slack rules), for `GORGE`.
    Workaround: a velocity anchor with 1 block of slack inside Hemorath's package; the Gorge is a deed-only move, so the
    jitter risk is small.
12. **A max-health scale per challenger** alongside the meter's. The umbrella scales only Saint meters; a four-player
    wear-down at 520 health would be over in a minute. Workaround: Hemorath applies a permanent attribute modifier
    `hemorath_challengers` himself.

## 19. Questions for the user

Only those whose answer changes what gets built, each with the default I would take.

1. **The first full meter.** Should filling him in phase 1 empty him into phase 2 (a tide, 15% of his health torn off,
   the card), so only the second fill bursts him? Default: yes. The alternative is today's rule, where any full meter
   ends the fight, which lets an overload player skip phase 2 entirely.
2. **The Tithe.** Keep blood debt as a move (he collects what you spent near him as damage, unless you pay it with held
   Projection along his chain)? Default: yes, as a per-fight ledger instead of the player-wide debt.
3. **The Last Thirst stops his leak.** That makes his last 20% the easiest moment to burst him, on purpose: a race
   between feeding him and cutting him. Default: yes. The alternative is a leak that keeps running, which makes the set
   piece a pure damage race.
4. **Feeding heals him.** Drinking the wells, the Gorge and the Last Thirst heal him (1 per 50, 1 per 40), so denying the
   wells is the wear-down road's job. Default: yes. Without it, the wells only matter to the overload road.

## 20. Calls made

Decisions no ruling covers, all mine:

- The chamber as his circulation: 25% (40% in phase 2) of the leak runs to the two nearest wells; the spokes show the
  wells' levels and the pit ring shows his meter as gutter fills.
- Meter states that change his body, speed and damage taken: Sunken ×0.7 and fast, Filling ×1.0, Brimming ×1.3 and slow,
  so both roads want him fed.
- The leak formula `M × (0.0004 + 0.004 f²)` per tick, ×1.3 in phase 2, halved by Searing, paused while Gorged and in the
  Last Thirst.
- The meter maximum `3000 × (1 + 0.5 (n − 1))` and health `520 × (1 + 0.35 (n − 1))`; armour 6 to 4; health 220 to 520.
- Presence 2.5 a tick at 4 blocks falling to nothing at 9, every 5 ticks, line of sight, ×1.25 for an Apotheos; 1 health
  per 20 ticks for an empty player, 25 meter per health point.
- Every intake number in section 4, including projection ×1.5 in Gorged and ×2 in Strained, the Tithe at ×1, Animus +60
  per Pressure level.
- Iron accrual of one patch per 1000 leaked, nine sites in a fixed order, 4% damage reduction each; Ferric Lodestone
  strips a patch; Flammeus Searing halves the leak; Congeatio Rime clots a draw; Animus Pressure feeds.
- The wells: 1000 capacity, 300 at the start, fight values kept on the entity rather than in `BloodBasinBlockEntity`;
  absorb 30 and project 25 a tick; drawn in aisle pairs.
- Phase 2 from 50% health or the first full meter, with a forced tide, 15% health burst and a meter left at 30%.
- The Last Thirst at 20%, once, 200 ticks, with the leak stopped and a forced vent if it times out.
- The whole moveset: eighteen moves (follow-ups, the deed's Gorge and the Last Thirst included) with their names, numbers, footprints, cooldowns and weights; the four strings;
  the eight openings and their lengths and multipliers.
- The first siphon of the fight always ends Gorged, to teach the overload door in the first minute.
- He targets the fullest visible vessel; the Reckoning targets the most indebted.
- The stalk: gait cycles 24, 30, 38 ticks; the Sunken lurch; turn rates 8°, 6°, 4°.
- The hitbox growing with the swell (0.9 to 1.4 wide, 3.0 tall) through an override in his own class, not `EntityInit`.
- One render scale (2.06) in both phases; the vessel in three nested stages scaled only between 0.8 and 1.15 once shown.
- The iron palette shifted from orange rust to dried-blood rust so nothing on him reads as copper.
- The halo leaves his head for `SPIKED_HALO`, drawn by the model from the same path rules as the hit.
- The pit sump as the one chamber edit.
- The deeds' names and effects: *What was not watched*, *The unsung measure*, *The gorging*.
- The ending's beats and its 125-tick length, his health held at 1 so every ending is one `endTrial`, and the road home
  along the east channel.
- The bar frame's 0.2 notch and the urns filling in the Last Thirst; the HUD vial's look.
- Teaching lines as narration (he does not speak), four of them, once each per fight.
- Every sound and pitch in section 14.
- Deleting both goals outright, the melee self-heal, and the phase-2 scale change.

## Kit follow-ups

Filled in by the Saint builder during run 2 (2026-11-05). None blocks the build; each has its in-lane workaround.

1. **One move at a time during the Last Thirst.** The design lets him pick the Iron Rake and the Spiked Halo while he is
   rooted. The controller runs one move, so the Last Thirst is a 40-tick forced move (the drag) followed by a 200-tick
   rooted state on the entity (`thirstTicks`), during which the picker allows only those two moves at decision delay
   30. A kit "state with a move set" hook would make this a phase-like table entry instead.
2. **A Path that dwells.** `SPIKED_HALO`'s 6-tick spin at the far end has no footprint shape (a `Path` walker moves at
   one speed). The halo flies the whole out-and-back path at one speed (`HemorathHaloPath`); the spin is only drawn.
3. **A hazard's end hook.** The tide's held blood returns to the wells when the tide ends; the hazard entity has no
   end callback, so the Saint counts the tide's ticks itself (`tideTicks`).
4. **A per-part hit report from `SaintMoves.struck`.** `STAFF_FALL`'s line (11) and chain marks (6) deal different
   damage; `onHit` re-tests the line geometry to tell them apart.
5. **Hold slack is unused.** The Gorge became a pull (resolution A-7), so Hemorath does not use `BossHold`.

## Compile-gate follow-ups

Edits outside Hemorath's lane that the tree needs, for the compile gate (resolution E):

1. **Delete** `common/entity/boss/goal/HematicCollapseGoal.java` and `common/entity/boss/goal/EmptyPulseGoal.java`
   (copy both to `removed_assets/` first). They name removed `HemorathEntity` members (`VISUAL_*`,
   `setCollapseCharging`, `setVisualTarget`) and removed `HemorathVisualRules` constants (`PULSE_RADIUS`, `PULSE_WINDUP`,
   `GOAL_TICK_INTERVAL`, `PULSE_IMPACT`, `COLLAPSE_WINDUP`, `COLLAPSE_IMPACT`); the tree does not compile until they go.
2. **Remove** `hemorath/HemorathEntity.java` from `src/test/resources/architecture/saint-kit-legacy-allow-list.txt`:
   the file no longer uses a legacy piece, so `SaintCombatKitGuardrailTest.saintsUseTheKitNotTheLegacyPieces` reports it
   stale until it leaves.
3. **Shared tests:** `SaintPowerRulesTest` reads the deleted `HemorathSiphonRules` (remove its Hemorath case);
   `SaintPowerGameTests` builds a Hemorath that ticks the legacy secondary cycle and expects a full meter to end the trial
   (remove the Hemorath cases); `SaintEncounterGameTests` feeds `OVERLOAD_THRESHOLD` and expects the trial to end, which
   now empties him into phase 2 (feed twice, or set phase 2 first); `SaintPatternGeometryTest` loses its Hemorath rows
   with `SaintAttackPattern`; `SaintRiseTest.everySaintHoldsThroughItsRise` reads a `public void aiStep()` with
   `tickRise()` in `HemorathEntity`, which a kit Saint no longer has (the kit runs the rise): accept a kit Saint.
4. **`SaintKitGameTests`** uses `EntityInit.hemorath` as its legacy Saint (`theRouterPassesForALegacySaint...`,
   `aSaintBodyRefusesTheOrdinaryDrain`) and `SaintKitClientReview` stands a legacy Hemorath on its stage; Hemorath is on
   the kit now, so those need a bare legacy `SaintTrialEntity` subclass or another Saint while one is still legacy.
5. **`BossBarFramePhaseSaintTest`**: add the third Hemorath phase,
   `HemorathFrame.phaseOf(translatable("message.hemomancy.hemorath.saint_hemorath_the_last_thirst")) == 3`.
6. **`gradle/game-tests.gradle`**: add the suite row `'hemorath_fight': [alpha: true, run: 'hemorathFightGameTestServer',
   directory: 'build/hemorath-fight-gametest']` (namespace `hemorath_fight`, template
   `src/gameTest/resources/data/hemorath_fight/structure/empty.nbt`; the chamber cases run in batch `hemorath_chamber`).
7. **Lang keys nothing reads any more** (remove once the goals are gone): `blood_source.hemomancy.hemorath_marrow_siphon`,
   `message.hemomancy.hematic_collapse_goal.hemorath_draws_in_your`,
   `message.hemomancy.hemorath.hemorath_bursts_exsanguinated_by_the_blood`,
   `message.hemomancy.hemorath.hemorath_empties_further`, `message.hemomancy.hemorath.hemorath_s_hollow_frame_strains_with`.
8. **The mausoleum:** only `structure/mausoleum/hemorath/chamber.nbt` changed (the pit sump); re-check
   `MausoleumComplexResourceTest` and the four-seed assembly GameTests.
9. **`docs/architecture/evidence/source-text-impact-map.csv`**: regenerate (`HemorathPresentationGuardrailTest` now reads
   every Hemorath file, `HemorathModelTest` and the new JVM tests read none outside their package).
10. **The old blood debt** (kit request 9): Hemorath no longer reads or resets `IBloodVolume` blood debt;
    `BloodAccounting.Source.countsAsSpend` still adds to it and nothing reads it. The integration stage may retire it.
11. `ManipulationVisualRenderer` needs no new hook line: `HemorathReliquaryGeometry` kept `handles`, `collect` and
    `draw` with their signatures.

## Build notes 2026-11-05

Built in run 2 (Saint builder, no Gradle: nothing here has been compiled; the compile gate compiles it). Every number is
one named constant in a pure rule class.

**Built (server, `common/entity/boss/saint/hemorath/`).**

- Pure rules, JVM-tested: `HemorathMeterRules` (maximum by challenger count, the leak `M (0.0004 + 0.004 f²)` with phase,
  Searing, digesting and the Downfall's fresh half rate, well share, phase-2 intake, the three meter states with their
  speed, gait, turn rate, decision delay and damage taken), `HemorathIronRules` (patch order, accrual, reduction, rake
  lanes, halo damage, thicket spend), `HemorathDamageRules`, `HemorathPresenceRules`, `HemorathWellRules`,
  `HemorathTitheRules`, `HemorathPhaseRules`, `HemorathEndingRules` (with `SPEC`, resuming on load), `HemorathHaloPath`,
  `HemorathPickRules` (every move's band and sector weights plus the need factors, pressure bonuses and eligibility),
  and the rewritten `HemorathVisualRules` (stages, rib spread, patch overshoot, glow, heartbeat, width, authored time).
- State: `HemorathTitheLedger`, `HemorathWells`, `HemorathPatches`, `HemorathOpenings` (the eight specs and the
  projection conversions), `HemorathSources` (nine named sources, resolution D-5).
- Kit parts: `HemorathMove` (18 moves: timelines with the phase-2 cut and floors, footprints, telegraph specs with the
  Beholding reveal, variants carrying patches, the held feint, the forced tide and the drawn wells), `HemorathMoves`
  (every `onTick`/`onHit`, the variant roll, the recovery openings), `HemorathStalk`, `HemorathMeter`,
  `HemorathPresence`, `HemorathArena` (wells, flank basins and every gutter sorted into its run; fills and fronts
  through `SaintArena`), `HemorathEnding`, `HemorathEffects` (now `glow` motes, never `hitGlow`).
- `HemorathEntity` rewritten onto the kit: `createKit()`, attributes (600 health, armour 4), the hooks (damage factor,
  hit taken, floor reached, arena resolved, phase changed, opening changed, ending tick, channel, hazards), the
  phase-1 overload that empties him (immediate phase advance, 15% tear, patches burst, forced tide that waits out the
  card and dumps to 30%), the Last Thirst, the tide's return to the wells, the school answers (Searing, Rime clot,
  Pressure feed, Lodestone strip), the gutters showing his circulation, the hitbox override, culling while the halo flies,
  the Tithe debt on the meter's per-player secondary, save and load. `onPlayerBloodSpend(Player, double)` keeps its
  signature. Off `registerGoals`, `SaintSecondaryAttacks`, `SaintAttackPattern`, `setVisualState` and `sendFloorRing`.
  `HemorathSiphonRules` deleted (copied to `removed_assets/`).
- Lang (add-only): the Last Thirst bar name, four teaching lines, the two channel cues, the closing line, nine source
  labels. Hemorath does not speak; his lines are narration.

**Built (client and assets).**

- `tools/model_export/hemorath.py`: nine `spike_patch_*` (scab plate and three to five tapering spikes with
  `iron_tip`), the three vessel stages, arm vessels, eight halo spikes, claw hooks, the hidden `claw_chain`, split face
  cords, the hematic-iron palette (no orange), rust stains under the sites, a glow mask of only vessel core lines and
  spike tips, wet hems; preview stills for the three states, patches 0/4/9 and the halo away. Generated: 675 cubes, 273
  joints, atlas 256 by 512. Render scale 2.0 (16 px a block).
- `tools/model_export/hemorath_anim.java`: every track of section 13 as Catmull-Rom channels (all 18 moves, seven
  openings, idle by state with the stalk, the card, the Last Thirst, the ending), laid on the rig by `pose` with the
  swell, patches, heartbeat and flinch additive, the 5-tick crossfade in `HemorathAnimState`, the staff counter-rotation
  now including the hips.
- `HemorathAnimState` (track, authored time, smoothed swell, patch beat on the client clock, blend, attachments),
  `HemorathVesselLayer` (replaces `HemorathOverloadLayer`, deleted), `HemorathRenderer` (one scale).
- World geometry: `HemorathReliquaryGeometry` (body blood, every burst kind including the rib jets, crack, spike rings,
  well streams and the road home), `HemorathMoveGeometry` (presence sheen and ring, leak drops, the chain and Tithe
  links, the well and Last Thirst streams, the flying halo in iron), `HemorathTelegraphStyle` (claw scratches, rake cracks
  with erupting iron, the siphon's ring and inward arcs, the tide's edge line, crown spike stubs, the standing thicket
  and the tide surface as hazards; the rest in the default painter in his families).
- `HemorathMeterStyle` (the riveted vial, meniscus, drips by leak rate, threshold studs, cracked cap, rusting patch
  studs, the opening pulse, the challenger's own Tithe links) and `HemorathFrame` (notches at 0.5 and 0.2, the Last
  Thirst as phase 3 with the urns filling).
- Chamber: the pit sump in `tools/worldgen/mausoleum/themes/hemorath.py`; Hemorath alone rebuilt through
  `build.write_saint('hemorath')` (layout check clean); only `chamber.nbt` changed in content.
- Tests: JVM `HemorathMeterRulesTest`, `HemorathPresenceRulesTest`, `HemorathWellRulesTest`, `HemorathSelectionRulesTest`
  (coverage, need factors, pressure, the Thirst set, strings, a 4,000-pick simulation), `HemorathMoveTimelineTest`,
  `HemorathFootprintTest`, `HemorathTitheRulesTest`, `HemorathPhaseRulesTest` (with the damage stacking),
  `HemorathEndingRulesTest`, `HemorathIronRulesTest` (with the halo path), rewritten `HemorathVisualRulesTest`,
  `HemorathModelTest`, `HemorathReliquaryGeometryTest`, `HemorathPresentationGuardrailTest`. GameTests
  `HemorathFightGameTests` (namespace `hemorath_fight`; 19 cases); `HemorathGoalGameTests` deleted.
  `HemorathFightClientReview` rewritten for `-PhemorathFightReview` (idle by state, patches, every move, openings, the
  HUD at four fills, the overload card, the Last Thirst, the ending; one flag per move reaching its active span).

**Not built, or changed from the design.**

- The Last Thirst's rooted picks run as a state after a 40-tick forced move (kit follow-up 1); the halo's end spin is
  drawn, not a footprint (kit follow-up 2); the flying halo is drawn by the world geometry in iron while the model's
  halo hides, rather than the model halo moving along the path.
- The Iron Rake has 3 lanes, then 5 from four patches, then 5 wider lanes from eight (the lane footprint is always
  odd), not 3, 4, 5.
- The stalk's blocks-a-tick speeds are asked of the vanilla move control as multiples of a 0.25 base speed; the verify
  stage should measure and tune them.
- Not covered by GameTests yet: the Gorge's pull, the Clotted draw, denial by absorbing a drawn well, the Litany's
  four-well draw and the Beholding feint in a live fight (each is covered by JVM rules tests).
- No native launch: the model, the layer, the geometry, the telegraphs and the HUD need the verify stage's review
  client, and the GameTests need the suite row (compile-gate follow-up 6).

### Compile gate 2026-11-06

The whole tree compiles (`compileJava compileTestJava compileGameTestJava`) and the JVM suite is green. Shared edits
made at the gate: the legacy Saint pieces are deleted (`SaintSecondaryAttacks`, `SaintAttackPattern`,
`SaintPatternGeometry` and their two tests, `registerTrialGoals`, `sendFloorRing`, the visual-state and secondary synced
fields, `reapplyEnrageEffects`), `createKit()` is abstract, the legacy allow-list is gone, `SaintPowerRulesTest` and
`SaintPowerGameTests` (every case drove a legacy piece; the `saint_power_validation` suite row and its template went
with them) are deleted, and the four fight suites have rows in `gradle/game-tests.gradle` (alpha). All deleted files
are under `removed_assets/` with MANIFEST lines. In this Saint:
- `common/entity/boss/goal/HematicCollapseGoal.java` and `EmptyPulseGoal.java` deleted.
- `SaintEncounterGameTests.hemorathOverloadRewardsTheVictorAndClosesTheTomb` now brings phase 2 by health (45%), then
  feeds a full meter, runs the ending and checks the reward and the closed tomb; the blood-debt assertion is gone.
- `SaintKitGameTests`'s two legacy cases no longer use Hemorath: the router case uses the test kit Saint, the drain case
  Velorum.
- `BossBarFramePhaseSaintTest` checks the Last Thirst name reads as phase 3.
- The bar is built with the literal `"entity.hemomancy.hemorath"` (BossPresentationBindingTest reads the literal);
  `NAME_KEY` stays for the phase table.
- `HemorathWells.drain()` is renamed `takeAll()`: `BloodAccountingGuardrailTest` reads `wells.drain(` as a player drain.
- `HemorathFrame.notches()` lists 0.2 then 0.5 (`BossBarFrameLayoutTest` wants them ascending).
- `HemorathFootprintTest.theHaloFliesOutStraightAndBowsHomeToHisLeft` samples its return-arc hit half a block behind the
  live front's head; at the head it sat on the floating-point edge and failed.
- Lang keys removed: `blood_source.hemomancy.hemorath_marrow_siphon`,
  `message.hemomancy.hematic_collapse_goal.hemorath_draws_in_your`, `message.hemomancy.hemorath.hemorath_bursts_*`,
  `hemorath_empties_further`, `hemorath_s_hollow_frame_strains_with`.
- Not done: the IBloodVolume blood debt is not retired (kit request 9, optional); the chamber's four-seed assembly
  GameTests were not run here (the JVM `MausoleumComplexResourceTest` passes against the pit sump).

## Verification 2026-11-05

Run on 2026-11-06 by the verify stage. Nothing is committed. Logs are under `build/verify-hemorath/` and stills under
`build/boss-presentation-review/screenshots/`.

**What ran.**

- JVM: `test --tests '*Hemorath*' --tests '*architecture*'` passed 278 tests with 0 failures. That covers the eleven
  Hemorath rules tests, `HemorathModelTest`, `HemorathReliquaryGeometryTest`, `HemorathPresentationGuardrailTest` and
  every architecture guardrail. An earlier `--tests '*Saint*' '*BossFramework*' '*BossBar*' '*Mausoleum*'` run was also
  green, including `MausoleumComplexResourceTest` against the pit-sump chamber.
- GameTests: `runHemorathFightGameTestServer` passed 19 of 19 (`round7.log`). `runSaintKitGameTestServer` passed 7 of 7.
  The combined `runGameTestServer` ran 1422 tests (`combined2.log`) with 10 failures, none of them Hemorath's (listed
  under "Still open"). `SaintEncounterGameTests.hemorathOverloadRewardsTheVictorAndClosesTheTomb` passes.
- Review client: `runBossPresentationReviewClient -PhemorathFightReview=true`, five rounds. The last round
  (`review5.log`) reports `passed: true`: every move reached its active span, the vessel emptied into phase 2, the
  Last Thirst ran, the ending rewarded once, `arena_cells_changed: 0` and the tomb closed.
- Shaders: every round compiled and rendered on this GPU. The client log has no shader, program or render errors and no
  exceptions. Its only shader lines are two warnings that were already there (`rendertype_entity_translucent_emissive`
  has no `Sampler2`; `vesper_wound_draw` has no `FogColor`).

**Fixed in this stage.**

- `HemorathFightGameTests` (five failures, all from the fixtures):
  - Mock players are never ticked, so they kept their 60-tick spawn invulnerability, and the presence, the Tithe and
    the tide could not hurt them. The fixture now clears it.
  - The well-priming player was created after the arena's once-a-tick challenger read, so the router passed. The
    player is now created before the first tick.
  - The fixture's own clear swept away the ring gutter before the second Saint. It is now laid again.
  - The chamber cases stood him below the chamber box. The leash then held him up while his fall built, and he took
    fall damage that undid the heal. The chamber fixture now lays the floor course and stands him on it.
- `SaintEncounterGameTests.hemorathOverload...`: the victor is the last feeder, looked up by id, and the test's player
  was never added to the level. It now is.
- Model (`tools/model_export/hemorath.py`, regenerated with `--preview`): the Brimming heart mass was 2.5 by 3 by 2
  units and could not be seen in game. Brimming and Sunken looked the same in the stills and in the generator preview.
  The mass is now 4 by 4.5 by 3 and presses on the front ribs, and the lobes are 2 by 2.5 by 1.5. The cube count and
  the atlas size did not change.
- `HemorathVisualRules.RIB_SPREAD` went from 0.3 to 0.55 and `RIB_BURST_SPREAD` from 0.45 to 0.8, so the ribs visibly
  part at Brimming.
- `HemorathRenderer` gives his bone a block-light floor of 8 (`getBlockLightLevel`, the same pattern as the moth and
  cicada renderers). His chamber's boss floor is lit only from the walls and the vault, and without the floor he read
  as a black outline in his own fight.
- `HemorathTelegraphStyle` tide surface: the pool cards now overlap (radius 0.85) and are jittered off the block grid,
  at alpha 0.3. Before, they showed a lattice of discs with dark diamond gaps.
- `HemorathFightClientReview` now runs in the real chamber, and it has new stops:
  - The chamber template is placed and its tomb bound. The rise uses the encounter's own spawn.
  - New stops: a gutters view at a brimming meter, the Tithe paid mid-wind-up, all eight openings, and the ending
    followed by a block-for-block check of the chamber against the template.
  - Each move gets a camera that suits it: melee from close, area moves from above the boss floor, the Wells Answer
    from across the pit.
  - The review hides the hand, uses Bright gamma (as `MausoleumClientReview` does), dismisses the onboarding screen,
    and captures at tick 3 of every 6 so a scene's first still is taken after the camera has moved.

**Stills (final round).** All in `build/boss-presentation-review/screenshots/`:
`hemorath-fight-00-rise-069.png`, `01-idle_sunken-021`, `03-idle_brimming-021`, `04-gutters_brimming-033`,
`07-patches_9-009`, `11-staff_fall-027`, `12-iron_rake-039`, `14-marrow_rupture-033`, `16-chained_debt-045`,
`18-wells_answer-093`, `19-spiked_halo-039`, `21-iron_thicket-033`, `23-rib_burst-033`, `26-open_gorged-021`,
`37-hud_95-015`, `38-vessel_empties-105`, `39-last_thirst-117`, `40-ending-099` and `41-restored-015` (each name has
the `hemorath-fight-` prefix). Earlier rounds are in the `hemorath-verify-round1` to `round4` subfolders. Round 1
showed the chamber lit by leftover sky light; round 2 shows its true darkness at Moody gamma.

**What the stills show against the rules.**

- He is frail at rest, with a pale bone frame, rust-red hematic-iron staff, halo and chains, and no copper, brass or
  orange. He reads at 16 px a block next to the chamber blocks.
- The telegraphs are the mod's own geometry, with no vanilla particle clouds: soft red rupture disc, six translucent
  rib-burst wedges, rake lanes with pale iron rising, standing thicket spikes, the thrown chain, the flying halo, and
  the tide sheet. Each shows before its commit and stays partly clear.
- The HUD vial fills, and the bar frame shows its three phase names and colours.

**Still open.**

- The combined gate's other ten failures belong to other lanes:
  - Velorum: `theLastTenthOnlyFalls...`, `preservationFreezesHurts...`, `stillGuardHoldsTheFloors...`,
    `velorumEndingClosesTheTomb...` and `killedSaintReturnsToItsTombUnrewarded` (which uses Velorum).
  - Putriciel: `colonisationThresholds...`.
  - Seraphae: `aReachTheChainsStopShort...` and `seraphaeWakesTheChambersBlockAnchors...`.
  - `BloodProfileGameTests.highHealthRestrictions...` lacks `velorum_devotee`.
  - `FrozenIdentifierGameTests` needs the kit's five new `saint_*` damage types added to `frozen/registry-ids.txt`.
    The file says only an approved migration may replace it, so it was left alone.
- The mock quarry is not drawn on the client (it has no player-info entry), so his moves aim at an empty spot in the
  stills. Holds and damage on a real survival player are covered by the GameTests, not by the review.
- Two moves read weakly in the stills: the Wells Answer streams and the Last Thirst's gutters running to him. Both are
  faint motes at this distance and in this light.
- The "Vessel Empties" card is allowed to play in its scene, but no still caught it.
- The Gorge is forced but excluded from the active-span check, as before.
- The stalk speeds were not measured against the design's blocks a tick.
- Not covered by GameTests yet: the Gorge pull, the Clotted draw, denying a drawn well, the Litany four-well draw and
  the Beholding feint.

**For the user to look at.**

- The Brimming body after the larger heart mass and wider ribs (`03-idle_brimming-021`): it is fuller, but still a
  modest change from Sunken.
- The light floor of 8 on his bone: does it read as his own faint glow, or does the chamber want more light instead?
- The tide surface's swirl texture (`38-vessel_empties-105`): it is faint and continuous now, but check it against
  the "spaghetti" ruling.
- Whether the Wells Answer and the Last Thirst need stronger streams.

## Visual pass 2 (2026-11-06), effects

The user played the fight on 2026-11-06 and found the effects plain next to the rest of the mod: flat ground tints
with hairline edges, thin grey spikes, line streams. The cause was the kit's `TelegraphCanvas` budget (fills at 22%
alpha, edges 0.012 wide), which carried the "subtle, no spaghetti" ruling over from body and screen overlays onto
attacks. That ruling still holds for overlays on his body; his attacks, telegraphs, impacts and focal moves are now
bold, layered, physical geometry. Nothing here is compiled or launched yet (this stage could not run Gradle); see
"Needs a launch" at the end. His animation was rebuilt at the same time by another stage and is not described here.

### How it is drawn

- **His own pass.** `HemorathTelegraphPass` (new, `client/render/world/`) draws every Hemorath footprint and hazard.
  `HemorathReliquaryGeometry.collect` runs it first in both of the manipulation visuals' passes. It reads the same
  synced move, `FootprintFrame`, timeline and reveal tick as the kit's `SaintTelegraphRenderer`, so every drawn edge
  is still the hit test's edge. `HemorathTelegraphStyle` now claims every Hemorath move and both hazards and paints
  nothing. The kit's flat tints never show for him, and the kit itself (`TelegraphCanvas`, `SaintTelegraphPainter`,
  the renderer) is unchanged for the other three Saints. The pass-1 style is kept in `removed_assets/`.
- **The floor's own material.** A new shader, `shaders/core/world/hemorath_fissure` (`ShaderInit.HEMORATH_FISSURE`,
  one additive registration), and one render type built once (`HemorathRenderTypes.FISSURE`, translucent, no depth
  write). Shape 0 is a crack laid along a strip. It draws a ragged gap that wanders in width, its walls running from
  scorched lip to dried crimson, a lit seam at the bottom (white-hot above heat 0.85), blood welling up from the seam
  as a glossy near-black pool with a shine streak and a bright meniscus, a pale bone-dust lip with rust seeping
  through it, and scorched stone round the gap with rust blooms and branching hairlines. Shape 1 is a crazed patch of
  floor: voronoi cracks about a block across with finer hairlines, stained dark with rust blooms, the lines lit by
  the same heat and holding the same blood. The vertex colour carries state, not tint: red is heat, green is blood,
  blue is rust, alpha is strength. Palette: dried crimson, black-red clot, rust (112, 40, 28) and pale bone dust only.
- **Crack networks** (`HemorathFissureRules`, pure, JVM-tested). Each shape breaks into its own network in the
  footprint's local frame. A lane is a main fault, its two edges, side branches and an end cap. A cone is its sides,
  its outer arc, interior radials and broken mid arcs. A disc is radials, a ring and broken arcs. A ring is a crack
  circle with short cracks across it. A star is five to seven radials and a broken ring. The rim is the boss floor's
  inset edge with cracks running in. A path follows the halo's line. The gouges are the claw's three furrows. Each
  crack has a start and a span within the run, so the network grows out from his feet through the wind-up.
- **The life of a telegraph** (`HemorathFissureRules.look`), the same for every move:
  - **Build** (reveal to commit): the cracks run their whole length by 60% of the reveal. The seam heat climbs from
    0.12 to 0.7 (squared), blood wells to 0.75 from 20% of the reveal, rust seeps in over the first 60%, and the gap
    opens from half width.
  - **Flash** (4 ticks): the seam runs white-hot, the gap yawns 35% wider and the blood surges full.
  - **Burst** (the active span): full blood, the heat settling to 0.6.
  - **Linger** (14 ticks into the recovery): the seams cool to dark embers, the blood drains over the first 70%, the
    gap seals from 35% and the whole fades over the last 40%.
- **Iron with mass** (`HemorathIronGeometry`). Spikes are real meshes on the Ferric plate material, tinted toward
  his hematic iron (`IRON 0xCC9EA6`, so the plate reads dark red-grey with dried-blood rust, never steel, copper or
  brass). Each spike has five or six fluted facets in three tapering stages that twist, a lean, and a blood-dark
  point (`TIP`). Its foot is buried under the floor, its U runs across each face in blocks (the plate's grain at
  about 16 a block) and its V runs up the spike. Clusters (`HemorathFissureRules.cluster`) are a tall central spike
  with shorter ones splaying outward, each with its own girth, lean and facets, round a low seven-sided scab.
  - **Punch:** a 3-tick rise past full height (1.14), settling to 1 over 3 ticks, while the plate's wet blood front
    climbs it.
  - **Hold:** a shiver of 0.035 blocks dying over 7 ticks.
  - **Sink:** 10 ticks back into the floor while the iron dissolves into blood from the point down (the formation
    falls to half).
  - **Debris:** chips of iron and floor (small tumbling octahedra) fly on arcs and settle over 14 ticks, and puffs of
    rust and dried blood rise and spread over 16.
- **Blood with body** (`HemorathBloodGeometry`, on the Animus material as the outpouring and Riposte draw). Streams
  have a soft shell with wisps trailing back, a glossy dark body (`BODY 0xB04A52`) that swells with boluses
  travelling toward him, a lit rim along the top in `CORE`, two sinews twisting through it (one `CLOT`), knots at the
  boluses' hearts, the source and the running head, and drops shedding off the high part of the arc.
  - **Drains:** beading drops that leave a pulsing wound, gather speed and run along a sagging thread, each with its
    own tapering trail.
  - **Crowns:** spurts leaping and arcing over a swirl on the ground.
  - **Spatter:** drops on short arcs flung along a blow.
  - **The rupture wall:** a standing sheet of blood with a pale crest and spray.
- **Impacts on a struck body** (`HemorathImpactGeometry`). These are read on the client from the hurt flash every
  client already has (the body's `hurtTime` and the damage event's source, which must be him), so no packet was
  added and every viewer sees the same hit.
  - **Iron hits** (Rake, Thicket, Crown Fall, Halo, Staff Fall, Staff Heel, Chained Debt, Tithe): four splinters
    drive through the body out of the far side and are torn back out (bite 0 to 1.5 ticks, out 3 to 8). Chips fly,
    a pale flash and a clot shell show at the chest, blood spurts along the blow, and the floor splashes at the feet.
  - **Blood hits:** the Riposte strike's seven rays out of the body along the blow, a clot-dark cross-slash, the
    flash and shell, spray and the splash.
  - **Timing:** flash 0 to 3.5, burst 0 to 6, fray to 10.

### Each move and hazard

| Move or moment | Build (wind-up) | Commit and active | Linger |
| --- | --- | --- | --- |
| Raking Claw, Backhand | The cone crazes (strength 0.4) and cracks along its sides, arc, radials and mid arcs (0.65). From tick 2 three claw gouges are scored across it, faint and narrow (heat 0.15, no blood). | The gouges rip open over 3 ticks in the swing's direction (the claw sweeps from his right, the backhand from his left), full of blood and white-hot at the flash. Chips (4 a gouge) and spatter fly along the swing from each gouge's middle, staggered 0.7 ticks. The `MELEE` slash burst and the intake drain show on a hit. | The cone and gouges drain and seal over 14 ticks. |
| Staff Heel | A rust-heavy crack wedge behind him (rust ×1.4, blood ×0.6, crazing 0.5). | At the commit, chips (7) and a puff of rust dust burst from his heel, a block behind him. | Drains and seals. |
| Staff Fall | A fault runs out along the 7-block line with its edges, branches and cap. The line crazes (0.5). Two starbursts mark the chain discs on their own clock, 4 ticks behind. | The line splits. A row of iron splinters (two-spike clusters about 0.55 tall) juts along it, running out from his feet at 0.35 ticks a block; they sink from tick 8. Chips fly from every other splinter and blood spatters to the sides. Four ticks later each chain mark bursts with chips (6) and a crown of blood fading over 14 ticks. The `STAFF_CRACK` burst at the line's end is now a crown and five spatters. | Drains and seals; the splinters are gone by tick 18. |
| Iron Rake | Each lane is a cracked bone fault with its edges and branches. Rust seeps from it (crazing 0.4), brightening and filling with blood toward the commit. | Behind each lane's front a cluster of five spikes (0.95 to 1.4 tall) punches out every 1.5 blocks, jittered across the lane. Each cluster throws chips (4) and rust dust, and blood wells round its foot. | The spikes hold 6 ticks past the active span, then crack and sink over 10 while the lanes seal. |
| Empty Pulse | The 6-block disc crazes (0.4) and cracks out to its rim. Twelve beads of blood are drawn inward along the radials toward him, faster as the commit nears. | At the release, chips and spatter are thrown out off the rim (10 points, 2 ticks after the commit). The `PULSE_RELEASE` burst is now a bold band with a dark clot wake (0.12 and 0.32 wide). | Drains and seals. |
| Marrow Rupture | A crack ring at 1.6 round him, crazed inside, and a faint preview ring at its 10-block reach (strength 0.35). | The wave rolls out over the active span: a cracking ring band at the front (heat 0.85, full blood, crazed 1.4 blocks behind), a standing sheet of blood exactly 0.9 high (`Height.JUMPABLE`, the jump made visible) with a pale crest and spray, and chips kicked up all along the front. | The final ring at 10 drains and seals. |
| Marrow Siphon | A glowing crack circle at 14 with cracks across it. The floor just inside is crazed (0.35). Sixteen beads are drawn in along the floor. | Through the active span every living body within 14 bleeds a beading drain from its chest to his, easing in over 6 ticks and out over the last 6. The ring and beads stay. | Drains and seals. |
| Chained Debt | A narrow groove scored along the 12-block throw (strength 0.7). | The chain: iron links in his hematic tint from the claw to the hook, with a thin blood thread. | Seals. |
| Tithe Reckoning | A crack star under the debtor (1.1 radius) while the links are counted out. | The `DEBT_HIT` burst; the links ring the target's waist. | Seals. |
| Wells Answer | The drawn wells' surfaces break into crowns, and the streams lift off them. | One stream with body per drawn well, from the well up into his chest, arching 1.4 high. Its half-width is 0.12 to 0.175, fuller for a fuller well, with boluses running at 0.32 blocks a tick. Each `WELL_STREAM` pulse is now a crown at the well rather than a second stream. | The streams fall with the move. |
| Spiked Halo | The out line and the bowed return arc are scored shallow into the floor (half width, heat ×0.6, no blood). | The halo flies as twelve hematic links with eight real spikes on its rim, 0.32 long plus 0.035 a patch. It trails a widening blood trail with a pale core, a smeared ring flung off its spinning teeth, and drops. Behind its head the scored path splits and bleeds as it passes. | Seals. |
| Crown Fall | The 3.5-block disc crazes and cracks. A ring of fourteen iron stubs pokes up through it, growing from 0.25 to full stub height as he catches the halo. | Fourteen three-spike clusters (1.25 to 1.6 tall) punch out round the ring, leaning outward and rippling round from his front over 3 ticks. Each throws chips and rust dust. | They hold, then crack and sink over 10 ticks, gone 4 ticks before the recovery ends, while the disc seals. The old `CROWN_SPIKES` burst draws nothing now. |
| Iron Thicket (move) | A starburst of rust cracks at each landing (rust full, crazing 0.55), welling blood. In the last 12 ticks of the wind-up, torn iron (a 0.2 hematic chunk and a blood-dark shard) flies on a 3-block arc from his chest to each landing, landing at the commit. | The hazard takes over. | |
| Iron Thicket (hazard) | | At each landing, two ticks apart in landing order: a six-spike cluster 1.35 tall and three smaller four-spike clusters leaning outward, staggered 0.7 ticks. Chips (7), rust dust, crazing, star cracks, and a pool of blood at the foot. The seams cool over 40 ticks. | The iron rusts toward `RUSTED 0x9C6A64` over its last 60 ticks (blood thinning in the cracks), then cracks and sinks over the last 16. The cracks seal with it. |
| Exsanguinating Tide (move) | The boss floor's rim cracks glow, the whole floor crazes and sweats (strength 0.35 times the build), and up to fourteen blood beads well and swell across it. | The telegraph fades over the 4-tick flash as the flood takes over. | |
| Exsanguinating Tide (hazard) | | One sheet of glossy blood: the brazier pool's shader (`HemoRenderTypes.squareBloodPool`), square, filling the boss floor to its edges and drawn with the camera-relative world axes its reflections need. It rises to 0.5 deep over 8 ticks, with ripples running out (jet 70) and a slow simmer (heat 40). A roll of blood sweeps out from his feet to the corners over 10 ticks (a sheet 0.55 high, shrinking) and a lit meniscus runs along the edges. | Over its last 20 ticks and the 10-tick fade the surface sinks and thins, and four streams with body run off the floor's edge to the wells. |
| Rib Burst | Eight floor veins glow out from him in a star (lane faults 0.75 of the spoke's half-width). The wedges between stay clean stone, the safe read. | Behind each vein's front, blood geysers up every 1.3 blocks as crowns 0.9 high fading over 12 ticks, with chips. The `RIB_JETS` burst (the Riposte-layered jets) is unchanged. | Seals. |
| The Gorge | None before the pose. | While it pulls, twelve smears of floor dust and blood are dragged in toward him across the cone. | |
| The Last Thirst | A crack circle at 18 round him, beating with his heart (heat 0.45 to 0.8). | The streams lift from all four wells into his chest, each with a crown. | |
| Patch eruption | | Three small two-spike blooms (0.32 tall) punch out at the site, staggered, and sink within ten ticks, with chips and a puff of rust dust. | |
| Phase-1 overload and the ending (`PHASE_BREAK`, `EXSANGUINATE`) | | A white-hot heart (flash over 5 ticks) and a dark clot shell. Twelve jets with body (shell, glossy body swelling along its arc, lit rim, clot sinew, tip knot) are flung on falling arcs, growing over 6 ticks for the overload and 12 for the ending. Drops rain off every other tip from tick 4. Hematic iron links are thrown. The ending also spreads a pool on the floor beneath him. The road home is a stream with body. | The burst's own fade. |

### Tests

- JVM: `HemorathFissureRulesTest` (new) covers:
  - the telegraph's life from build to seal;
  - every network staying on its footprint over 40 seeds;
  - networks being stable per seed and bounded;
  - the punch overshooting, holding, sinking and dissolving;
  - clusters varying, with the centre spike tallest;
  - chips arcing and settling, and boluses spacing.

  `HemorathEffectGeometryTest` (new) covers:
  - cracks and crazing decoding as their shader shapes even along the 18-block ring;
  - nothing drawing unrevealed, sealed or before a crack runs;
  - iron hidden until it forms, standing about its scale, and gone once sunk;
  - streams, drains, crowns, spatter and the rupture wall staying in budget.

  `HemorathReliquaryGeometryTest`'s stream budget rose from 800 to 1400 vertices (the streams have body now), and
  its stream test was renamed to `chainsAndStreamsStayBounded`.
- The pure rules were also checked numerically by a scratch Python port before the Java tests were written. The
  tests themselves have not been run.

### Needs a launch (not done here)

- Compile the tree. No Gradle or Java build ran in this stage.
- Launch the review client (`runBossPresentationReviewClient -PhemorathFightReview=true`) to prove
  `hemorath_fissure` compiles on the GPU, and to look at every move and both hazards. Offline validators miss driver
  errors.
- Check the tide surface's reflections under the pushed model-view, and the ferric plate's tint in the dark chamber.
- Check the impacts on a real survival player. The review's mock quarry is not drawn and may not receive damage
  events.
- Watch the frame cost of the Iron Rake at five lanes and the Thicket at six landings. Each spike is 15 to 18 quads
  and each cluster carries a 14-quad scab.
- Two calls to check: the claw sweeps from his right and the backhand from his left (match them to the animation's
  swing), and the impacts read the struck body's last damage source, which the client sets from the damage event.

## Visual pass 2 (2026-11-06), animation

The user's verdict on the first build: "the animations are okay but he feels rather stiff and disjointed". The rebuild
measures against Annetta's rebuild (anticipation, a committed strike with the whole body behind it, follow-through)
and the Mycophant (weight and settle on a large body). Nothing here is compiled or launched yet; the stills need the
review client.

### What changed in the machinery

- **Layers, not one track.** A frame is the stance, then the stalk, then the playing track as deltas over both, then
  the blend, then the springs, then `pose()`. Every move's keys start and end at zero (Marrow Rupture and Crown Fall
  start from the pose the move before them leaves), so a move rises out of the stance and settles back into it.
- **The stance blends with the meter.** The three idles are no longer `if` bands: Sunken, Filling and Brimming are
  smooth weights over the smoothed swell, so the sag and the swell never step. The breath and the weight sway run on
  phases carried in `HemorathAnimState` (their rates change with the swell, so they are not recomputed from the age).
- **Inertial blend** (`HemorathPoseBlend`, JVM-tested). A changed track (a new move, the same move again by its synced
  serial, an opening, the card, the ending) keeps the gap from the last shown pose and the velocity the body had, and
  that offset dies away along a cubic Hermite over 4 ticks hollow to 7 full. The new track plays at full speed under it,
  so a strike keeps its snap and a body moving when the move changed carries on a moment before it turns.
- **Springs** (`HemorathSpring`, JVM-tested; 29 slots in `HemorathAnimState`, stepped once a frame on the frame's own
  time). Each writes how far a part trails what carries it: the skull trails the chest's pitch, twist and lean; the
  chest dips on through a sudden crouch; the right forearm, wrist and fingers trail the arm and whip through when it
  stops; the shoulder, waist, crown and halo chains swing off the chest, the stride, the staff and his turn, their tips
  trailing their roots (each link bends a little more than the one above, the pendant riding the last); the robe
  strips, shrouds and train trail his stride and settle forward when he stops, the train drags round after a turn; the
  ribs ring after a fling; the face cords swing off the skull; the heart mass wobbles on a jolt; a hit kicks a recoil
  that rings out. Hollow the springs are tight and quick; full they are slow and loose, so the heavy body sways after
  itself. Each trail is clamped to 0.7 rad.
- **Measured motion.** The lurch (ground speed over 0.27 blocks a tick) and the turn (body yaw rate) are read off his
  real movement and smoothed, so they need no synced flag.
- **The legs plant.** The knees fold for a crouch and a slump and the hips sink until the lower sole meets the floor
  (`HemorathGait.fold` and `reach`); a lifted foot does not hold him up. The robe strips flare out as the hips sink so
  the cloth heaps instead of passing through the floor; the shrouds counter most of the chest's bow.
- **The staff is a pole he leans on and swings.** Planted, its iron foot stays where the gait or the move puts it and
  the grip slides up to 7 units along the shaft, so the staff tilts as he leans and as he walks past it. Lifted, it
  holds the angle the move gives it (`STAFF_X`, `STAFF_Z`), so the heel jab and the fall really swing it. The crown
  chains hang plumb off it whatever it does.
- **The halo rides the claw.** Lifted, the ring's lower edge sits in the claw's grip; at Crown Fall it is driven into
  the floor ahead of him; at the ending it drops behind him and comes to lie flat.
- **The stalk** (`HemorathGait`, JVM-tested) is keyed to distance walked, not time: a cycle of 1.9 blocks hollow to
  1.5 full, left foot in the first third, right foot in the second, the staff swinging ahead in the last and planting
  as the cycle turns. A planted foot and the planted staff sweep back at exactly the body's speed, so nothing skates.
  At the design speeds this gives a cycle of about 9.5 ticks hollow and 15 full (the 24/30/38 of section 8 would need
  strides longer than his legs).
- **Clocks.** Every track still runs on its replayed clock; the Last Thirst's counter now replays through its own
  `BossReplayClock` instead of adding the partial tick. The Spiked Halo stays on its own track through Haloless so the
  catch shows.
- **Rig** (`tools/model_export/hemorath.py`, regenerated: 711 cubes, 289 joints, atlas unchanged at 256 by 512, 16 px a
  block). The chain pendants are their own child parts (`*_pendant`) so a bent chain carries them, and two short
  chains (four links and a pendant) hang off the halo's lower ring.

### Track beats

Times are on the authored clock; the commit is the end of the authored wind-up.

| Track | Beats |
| --- | --- |
| Stance, hollow | skulking hunch on bent knees, skull raised to the quarry and snapping between held looks every 26 ticks, claw held up with the fingers creeping and a sharp twitch every 40, quick shallow breaths, the staff planted ahead and leant on |
| Stance, filling | deeper breaths, the ribs working, the staff hand tightening every 64 |
| Stance, full | upright on bowed legs, ribs held wide, clavicles lifted, slow laboured breaths, the hips rolling his weight side to side over the staff, the skull lolling with it, a shudder every 26, the claw hanging heavy |
| Stalk | left foot, right foot, then the staff swings ahead and plants; each landing pitches the chest and drops the head a little and jars the staff hand; the claw drags behind; full, the hips roll over each step and the shoulders counter |
| Lurch | torso thrown forward, skull up, claw flung ahead and splayed, the legs trailing, the staff left behind |
| Turn | the shoulders and the skull lead, the hips lag, the staff swings out, the feet shuffle round when he turns in place |
| Raking Claw (35) | 0-13 the shoulder rises and draws back, fingers splay then hook, he coils on bent knees (the feint freezes at 10 and trembles); 13-16.5 the claw tears down and across, the right foot stamps in; 16.5-19 the arm whips past the line; 19-35 off balance, hauled upright |
| Backhand (34) | 0-12 the claw folds back high across the chest as the shoulders wind the other way, held; 12-14.5 the back of the hand flings out to his right; 14.5-17 past it, fingers flung open; 15-26 the left foot stumbles out to catch him |
| Staff Heel (32) | 0-12 leans onto the braced claw, lifts the staff and draws its foot up and back with a look over his shoulder; 12-13.5 rams it back; 13.5-15.5 past the line; 19-25 it swings down and plants |
| Staff Fall (52) | 0-20 both hands take the staff overhead, the crown tipping behind him as he rears; 22-24 hauled over and down, the crown strikes the floor ahead, the left foot steps in, he drops after it with a judder; 30-40 levers it upright and plants it ahead (Planted takes over at 28) |
| Iron Rake (60) | 0-9 drops into a deep crouch, claw slammed to the floor out right, staff braced wide; 9-24 drags it across, grinding; 24-27 fingers dig in; 27-30 rips it up and over, rearing; 30-40 conducts the eruption in small beats; 40-60 rises |
| Empty Pulse (46) | 0-4 lifts and rams the staff; 4-23 folds over it, claw to the breastbone, ribs crushing in, a shudder building; 23-27.5 everything flings open, ribs, arms, the skull thrown back, past open; 28-46 gasping as it rings out |
| Marrow Rupture (68) | 0-20 bowed, a fist clenched, the skull lifting; 20-40 two pumps of the ribs, each a heave of the shoulders and a bob of the knees; 40-68 comes up |
| Marrow Siphon (84) | 0-9 lifts and rams the staff; 9-25 skull back, ribs open, the claw raised open; 25-64 the chest heaves as it fills, a swallow every ten ticks, the claw beckoning; 64-84 hunches over what he took (Gorged may take over) |
| Chained Debt (42) | 0-19 the claw draws back and up, elbow cocked, the chain uncoiling through the fingers, a held beat; 19-23 thrown out straight with a lunge, fingers flung open; 23-28 hauled back hard, weight thrown onto the left foot; 28-42 recovers |
| Tithe Reckoning (60) | 0-10 the claw goes up high; 10-34 held there tracking, trembling harder, the fingers closing one at a time (once per hundred debt); 34-36 draws back a hair; 36-38.5 the fist yanks down and the body follows, past the line; 41-60 settles |
| Wells Answer (100) | 0-5 lifts the staff; 5-8 drives it into the pit with his whole weight; 12-28 rises and opens his arms to the wells; 30-70 skull back, the chest swelling at each stream's arrival, the arms drawing in as he pulls; 70-100 lowers and hunches (Gorged may take over) |
| Spiked Halo (82) | 0-12 the claw reaches up behind the skull; 12-16 lifts the halo off; 16-23.5 draws it back to his right, shoulders coiled; 23.5-26 slings it across his body and lets go; 26-56 bare-headed, following it out and round its return to his staff side; 56-60 raises the open claw; 60 the catch, a jolt through the knees; 63-74 lifts it overhead and settles it back on his skull |
| Crown Fall (50) | 0-19.5 the halo overhead in both hands, the staff lifted with them, rising onto his toes and rearing further; 19.5-21.5 brought down to his feet, the halo driven into the floor ahead; 21.5-30 folded hard over it, a judder; 30-50 comes up as the halo rises back to his skull |
| Iron Thicket (50) | 2-17 three tearing beats (the left forearm twice, then the ribs), the body flinching from each; 17-22.5 the claw cocks low behind him; 22.5-25 flings the iron straight up, letting go at 24, arching after it; 27-50 recovers hunched round the torn arm |
| Exsanguinating Tide (150) | 0-29 arms raised wide, the staff lifted with the left, ribs prised open, arching back with a shudder building and a hitch at 29; 29.5-34 everything drops, ribs cave, he folds deep and bounces; 34-90 wrung, hanging, breathing shallow, the staff planted to hold him; 86-92 and 104-112 two attempts to stand that slip back; 112-150 he stands |
| Rib Burst (58) | 0-20 the ribs prise apart in three jerks, shoulders drawn back, a shudder building; 20-24 a locked breath; 24-27 the vent flings them wide and throws him back on his heels, past open; 27-34 held trembling (Strained takes over) |
| The Gorge (70) | 0-17 drops low, arms flung wide, ribs prised, the face cords parting, leaning in; 20-46 the arms beckon in a seven-tick rhythm, the claw curling, the chest heaving, his weight hauled back; 47-49 the arms snap shut; 49-70 straightens |
| The Last Thirst (240) | 0-34 drags himself low over his knees, the claw hauling at the floor in ten-tick pulls, the staff a crutch; 34-38 raises the staff; 38-41 drives it into the pit; 42-60 rises and spreads his arms; 60-240 turns slowly between the four wells as the swell climbs, a shudder every twenty |
| Gorged (50) | a wet gulp, the knees buckle (overshooting at 7), he sags sideways onto the staff, skull lolling, the claw hanging slack; sated heaving breaths; a second gulp at 24 |
| Strained (40) | ribs locked wide with a crack, shoulders back, shaking at half a beat a second |
| Planted (24) | out of the blow into both hands on the staff stuck ahead of him, bowed over it, heaving |
| Haloless | the bare skull raised and searching, the claw half raised (the Spiked Halo's own track shows the window in a fight; this plays only on its own) |
| Clotted (30) | a jerk as the streams freeze, then a stiff two-step stagger back, rime-stiff trembling fading |
| Wrung (40) | the ribs fold in, bowed over his knees, the arms slack, the skull rolling |
| Broken Grasp (30) | the arms snap shut on nothing and he reels back two steps, arms flailing for balance |
| The Vessel Empties (50) | 0-10 locked rigid, trembling; 10-13 the ribs and arms fling wide, the skull thrown back, the cords parting; 13-30 it drains; 30-40 bows deep over his knees; 40-50 straightens into phase two's stance |
| Ending (125) | 0-6 rigid hit-stop; 6-12 the burst flings ribs and arms wide, the skull back, the halo dropping behind him to lie flat; 12-80 he empties and sinks, arms slack, swaying; 80-92 the knees go; 92-102 the chest; 100-110 the skull into the robes, which heap; the staff leans away |
| Patch eruption | the newest patch overshoots to 1.15 and the bone it breaks from jolts 0.12 rad |
| Flinch | a spring kicked by the hit: the chest and skull recoil and ring out |

### Tests

- `HemorathSpringTest`: settling, overshoot below critical damping and none at it, a lag that trails then swings past,
  stability over a long frame, a kick that rings out.
- `HemorathPoseBlendTest`: no jump at a change, the new track alone by the blend's end, the carried velocity, the flat
  landing.
- `HemorathGaitTest`: no skating for either foot or the staff, the beat order, shorter steps when full, the fold and
  the sole under the hip, the landing jolt.
- `HemorathModelTest` (rewritten checks): the staff is one rigid pole whose planted foot stays where it is put and
  whose lifted angle is the move's; the rest pose stands the staff plumb on the floor; a crouch sinks the hips with the
  soles on the floor and a swinging foot does not hold him up; the chains hang plumb when he bows and off a swung
  staff; the lifted halo rides the claw; every move rises out of the stance and settles back to zero; every strike is
  at least 2.5 times faster than its wind-up and carries on past the commit; the stance has no step anywhere along the
  meter.

### For the review

- Run `runBossPresentationReviewClient -PhemorathFightReview=true` and look at the move stills and, above all, in
  motion: the strikes' snap and follow-through, the stalk's three beats, the staff's lean and the chains.
- Tune points: the spring gains and frequencies in `secondary()`, the trail clamp (0.7), the blend lengths
  (`BLEND_SUNKEN`, `BLEND_BRIMMING`), the stride lengths (`CYCLE_SUNKEN`, `CYCLE_BRIMMING`), the grip slide (7 units).
- The halo chains vanish with the model halo while it flies (the world geometry draws the flying halo without them).
- Not done: a hit's direction (the flinch always recoils backward), and arm IK for the second hand on the staff (the
  right hand is authored to meet it at Staff Fall and Planted, not solved).

## Visual pass 2 (2026-11-06), polish

The two pass-2 stages compiled first time. `compileJava`, `compileTestJava` and `compileGameTestJava` were clean, all 5,340 JVM tests passed, and `hemorath_fight` (19) and `saint_kit_validation` (8) passed. Both shaders (`hemorath_fissure`, then the new `hemorath_iron`) compiled on this GPU. Five native runs of `runBossPresentationReviewClient -PhemorathFightReview=true` logged no shader, program or `Not building` error, and every run ended `HEMORATH_FIGHT_REVIEW_OK` with the ending rewarded and the chamber restored. The ordering of the FISSURE and blood-pool batches in AFTER_LEVEL held.

### What round 1 showed

Measured against Hematic Riposte and the cast outpouring:

- **Iron.** Near-black, needle-thin cones on the Ferric plate. A vertex tint can only darken that steel shader, so it could never read as hematic iron.
- **Telegraphs.** Webs of equal-weight thin lines. The crazed fill was too dark to show.
- **Strikes.** Nothing in the air at a commit.
- **Wells Answer.** Never seen: the camera faced the drawn wells' far side.
- **Siphon.** The review player's own drain began at the camera, so it read as two blobs.

### What was fixed

- **His own iron material.** `shaders/core/world/hemorath_iron` (json, vsh, fsh) and `HemorathRenderTypes.IRON` (solid, depth-writing, built once), registered in `ShaderInit`. `ManipulationVisualRenderer` hands it to `HemorathReliquaryGeometry.collect` and the reliquary bursts in the solid pass. The shader draws oxblood iron lit by its own facets: a key and fill light, a warm fresnel rim on the silhouette, 16-a-block grain and rust blooming lower down. Wet blood runs down from the point with drips and a moving shine. The foot is white-hot where the iron tore out and cools over `HemorathIronGeometry.COOL` (7 ticks). A blood lip marks the formation front, and floor chips use a separate pale bone-stone shape. The vertex colour is state (heat, wet, rust), not a tint: `HemorathIronGeometry.state` / `withHeat`.
- **Mass.** Cluster girth went from 0.11 to 0.19 times the square root of the scale (about four times the cross-section), and scabs are 2.0 times the girth. New scales: rake 1.3 to 1.9 blocks, Crown Fall 1.8 to 2.4 with four spikes a cluster, Thicket 2.1 with satellites at 1.2, Staff Fall splinters 0.9 to 1.2 with three spikes. Halo teeth are now 0.12 radius and 0.5 + 0.05 a patch long.
- **Rupture glow.** `HemorathIronGeometry.rupture` puts a hot floor bloom under every punch, cooling with the iron's heat. A first version added a flash card above it; it rendered as a blurry red balloon and was removed.
- **Claw strikes.** `HemorathAnimState` keeps a world-space trail of his claw hand, fed by `HemorathRenderer` every frame (`attachments(..., origin, age)` and `clawAt(age)`). `HemorathTelegraphPass.slashes` lays three Riposte-recipe rakes along it: a SLASH base tapering tail to head, a clot-dark leading half and a pale core line. The rakes fan across the swing as the viewer sees it. The slash runs from two ticks before the commit, then holds, widens, drifts up and frays over 7 ticks, dropping blood (`HemorathFissureRules.slashHead/slashFade/slashFray`). Because the trail is the animation's own hand path, the sweep direction can no longer disagree with the animation; the effects stage's left-or-right assumption is gone.
- **Slam shocks.** `HemorathTelegraphPass.shock` throws a low ring of blood out across the floor with a pale crest, a crown of spurts and a hot ground flash (`HemorathFissureRules.shockRadius`, 7 ticks). It plays at the heel, at the staff's landing, on the pulse's release and on Crown Fall.
- **Telegraph legibility.** The changes in `hemorath_fissure.fsh`:
  - The gap is dark clot with a narrow seam and a hair of white-hot core.
  - Blood rises to 78% of the gap and no further, so the dark walls show above it and the seam glows up through it.
  - The lips are dulled to rust-stained dust, because pale lips read as rail-track outlines.
  - The crazed fill soaks dark crimson in blotches, smoulders with heat and flashes with the seams at the commit.

  In `HemorathFissureRules` the crack widths rose (fault 0.14, edge 0.09, inner 0.075, branch 0.05). `HemorathFissureGeometry` now fades branches to half strength, so faults and edges carry each shape.
- **Iron colour.** Darker oxblood mid-tone, less rust in fresh iron (0.14) and a softer edge highlight. Round 2 had read as terracotta.
- **Blood with body.** Stream widths: move streams 0.14 to 0.24, reliquary 0.11 to 0.18. The body is drawn on the SLASH sprite with a clot core. The bolus knots are smaller and fainter, so a stream no longer reads as beads on a string.
- **Pulse beads.** Now tapering smears with no pin heads.
- **Siphon drain in first person.** The local player's own drain starts 1.6 blocks in front of them, not in their eye.
- **Review harness** (`HemorathFightClientReview`, `gradle/review-clients.gradle`):
  - Moves are shot every 3 ticks.
  - `-PhemorathReviewOnly=a,b` keeps the rise and the named scenes.
  - `-PhemorathReviewParticles` takes minimal, decreased or all; the default is now all, as a player sees it.
  - A three-quarter `_side` scene each for Raking Claw, Backhand, Staff Heel, Staff Fall and Crown Fall.
  - The Wells Answer camera moved, and the scene logs its drawn-well mask.
- **Riposte reference.** `OutpouringClientReview` gained `riposte` scenes that arm Hematic Riposte on the reviewer (`-PoutpouringReviewOnly=riposte`), capturing `outpouring-NN-riposte-sweep|strike|dissolve.png`.

### Evidence

- Rounds 1 to 5 are in `build/hemorath-pilot/roundN/`. Each holds `stills/`, one contact sheet per scene with its frames in tick order (`NN-scene.png`) and key-frame grids round each commit (`keys/key-<move>.png`).
- The final round is `build/hemorath-pilot/round5/`, with animation strips for the three-quarter strikes (`anim-<move>_side.png`).
- `build/hemorath-pilot/compare-riposte-outpouring.png` puts his best frames next to Riposte and the outpouring.
- The pass-1 stills are kept in `build/boss-presentation-review/screenshots/hemorath-pass1-2026-11-06/`.

### Still open

- **Lanes and spokes.** The Iron Rake lanes and Rib Burst spokes are bolder and layered now, but from the boss camera they still read as glossy red stripes more than broken floor. A lane may want a filled, sunken trench instead of three parallel cracks. (Taken up in pass 2b below.)
- **Empty Pulse and Marrow Siphon** depend on framing: the pulse's shock and the siphon's ring at 14 fall mostly outside the review cameras. Neither has been judged at play distance with a moving camera.
- **Riposte comparison.** The Riposte stills at sweep and strike are small at this distance. The reference shows its recipe more than its full presence.
- **Claw slashes.** Seen only in this fixture: a fixed quarry, a creative viewer, and 3-tick frames. Their exact fan and width want a live look in motion, including first person when the player is the target.
- **Frame cost** has not been profiled. Five rake lanes, a six-landing Thicket and the solid-pass iron all ran smoothly in the review client, but nothing was measured.
- **Animation.** Judged from frame strips only. Claw, Staff Fall and Crown Fall read as one body with weight: coil, a fast arc through the commit, follow-through, settle. The stalk's gait, the springs' trail amplitude and the halo catch at 60 have not been watched in real-time motion.
- **Not reviewed this pass.** The Last Thirst's streams have more body now, but at the wide camera they still read thin. Tithe Reckoning, Chained Debt and the Gorge were left as the effects stage drew them.

## Visual pass 2b (2026-11-06), trenches

The user's ask after pass 2: from the boss camera the Iron Rake's lanes and the Rib Burst's spokes read as glossy red
stripes, not broken floor. Each lane and each spoke is now a trench: the floor splits and drops into a real sunken
channel. Nothing here is compiled or launched yet; this stage could not run Gradle. The shader passed
`glslangValidator` offline only.

### How it is drawn

- **The mouth is the hit strip.** `HemorathFissureGeometry.trench` lays the trench's mouth on the floor over exactly
  the strip the server tests. For a rake lane that is `0` to `length` along and `halfWidth` either side of the lane's
  line. For a spoke it is `inner` to `length`. It is cut into half-block quads, and each quad end carries the trench's
  state at that point. The fissure shader's new shape 2 draws it. Red is the seam's heat, green the blood's height as
  a share of the depth, blue how far the floor has dropped (in place of rust), and alpha the strength.
- **A channel under the floor that the floor cannot hide.** The channel is not geometry below the floor, because the
  chamber's own floor blocks would hide it. The shader looks into it through the mouth:
  - It rebuilds the strip's frame in view space from screen derivatives: a block along, the whole width across, and
    up out of the floor. The vertex shader now passes the view position.
  - It traces the view ray against two walls that lean in by 0.3 a block of depth, the bottom (0.75 deep for each
    block of width at full drop, `TRENCH_DEPTH`, kept equal in the rules and the shader), and the blood's surface.
  - What the ray meets is shaded:
    - **Walls:** the cut floor at their top catches the light, then banded strata darken down into dried crimson,
      torn by fractures and lit from beneath by the seam. These are the dark inner tones of the fissure walls.
    - **Bottom:** dark broken rubble, split along its length by a wandering seam that runs white-hot above heat 0.85.
    - **Blood:** glossy, with the seam glowing up through it, a lit meniscus at the walls, and a brighter tongue where
      it runs thin.
  - The mouth is ragged. On either side a broken lip of crazed, crumbling floor is left inside the strip. A dark crack
    runs along the strip's own edge and glows when the seam is hot. Before the floor drops, the whole strip reads as
    split, crazed floor, which is the telegraph.
- **Slabs and rubble** (`HemorathFissureRules.slabs`, `slabCorners`, `slabLift`; `HemorathIronGeometry.slab`). Each
  lip breaks into slabs of floor:
  - Size: 0.4 to 0.82 blocks long and 0.38 to 0.74 of the half-width out, scaled to the trench's width, with gaps
    between them and about one in seven missing.
  - Heave: each slab is hinged on its outer edge and its trench-side edge rises, tilted 18 to 39 degrees with one end
    higher. Each starts on its own lag as the trench drops.
  - Material: solid floor stone in the iron material's chip shape at 16 grains a block. The broken edge facing the
    trench is wet low down.
  - Rubble: one piece a slab, on the floor beyond it or on the lip just inside the edge.
  - Clear of the footprint: a slab is pushed outward until no corner comes nearer the line than the strip's edge, so
    it frames the footprint and never covers it.
  - At rest a slab lies under the floor and draws nothing.
- **Iron out of the channel.** The rake's clusters punch up behind the front as before, now from the channel's middle
  (jitter 0.1 of the half-width). Their scab, about 0.55 in radius, plugs the channel across its width. The pass-2 blood
  pool card at each foot is gone, because the trench holds the blood now.

### The life of a trench (`HemorathFissureRules.trench`)

| Span | What happens |
| --- | --- |
| Build | The split runs out from his feet over the first 45% of the reveal. Each point opens as it is reached and drops from 0.3 toward full depth through the wind-up. Blood floods in from the far end and runs back toward his feet from 20% to 95% of the reveal, its tongue rising over 1.5 blocks, to 0.6 of the depth. The seam warms from 0.12 to 0.62. The slabs heave with the drop (0.8 of it). |
| Commit (4 ticks) | The seam runs white-hot along the whole channel, the blood surges toward 0.78 of the depth, and the slabs jolt a quarter past full. |
| Active | The seam settles to 0.6 but flares again (+0.3, fading over 1.5 blocks) just behind the hit front as it runs and the iron punches out. |
| Close (up to 20 ticks, never past the recovery) | The seam cools. The blood drains back out the far end, the near end first, over the first 55%. The channel closes and the slabs settle back under the floor from 35% (eased). The strip fades over the last 25%. |

Both ends shoal up over 0.9 blocks, so the channel closes to a lens at each end; the broken lip still covers the strip
there. The blood never brims, so the dark walls always show above it.

### Rib Burst

The same trench, scaled to the spoke's width (half-width 0.6), from `inner` to `length`. Its front is the spokes' reach
once they run. Near him the eight spokes crowd together, so slabs start only where they cannot reach into the next
spoke's strip: `(half + half * (1 + SLAB_REACH) + 0.05) * cos(gap) / sin(gap)`, about two blocks out for eight spokes.
The geysers and chips behind each front are unchanged and now rise out of the channel.

### Tests

- `HemorathFissureRulesTest`:
  - `aTrenchSplitsFloodsFromTheFarEndFlaresThenDrainsAndCloses`: nothing before the reveal; the split runs out from
    his feet; the flood comes from the far end; the drop deepens; the channel is flooded to his feet by the commit; the
    flash and jolt; the ends shoal up; the flare behind the front; the blood never brims; the near end drains first;
    the channel closes by `trenchLastTick`.
  - `slabsFrameTheTrenchNeverCoverItAndHideAtRest`, over 40 seeds and three widths: every corner stays outside the
    strip and within `SLAB_REACH`, slabs are hidden at rest, they wait for their lag, and they are heaved, not thrown.
- `HemorathEffectGeometryTest.aTrenchMouthLiesExactlyOnItsStripAndItsSlabsHideAtRest`: the mouth's vertices lie
  exactly on the strip's edges and ends and decode as shape 2; a closed trench draws nothing; slabs at rest draw
  nothing; heaved slabs stay in budget and are floor stone.
- The rules and the slab bounds were also checked by a scratch Python port over 400 seeds. The worst reach was 1.84
  times the half-width, under `SLAB_REACH` (0.9 past the edge). The Java tests have not been run.

### Needs a launch

- Compile, then run the review client to prove shape 2 compiles on the GPU.
- Look from the boss camera and from a player's eye at a lane: the channel's depth read, the walls' tone, the flood
  running back toward him, the seam's flash, the iron plugs, the slabs, and the drain and close.
- Things to watch:
  - **Derivative frame:** the frame comes from screen derivatives, so a lane seen nearly edge-on falls back to flat
    clot. Watch for sparkle at grazing angles.
  - **Overshoot at the far end:** the channel's depth is taken at the pixel where the ray enters the mouth, not where
    it lands. A ray entering near the far end at a low angle can look a little past the end. The taper keeps that
    shallow.
  - **Sunken bodies:** a body standing in a trench shows its feet on the floor plane over an apparent pit.
  - **Frame cost:** five lanes each carry about 35 slabs.
- Tune points:
  - `TRENCH_DEPTH` (rules and shader together) and `WALL_LEAN`.
  - The flood's timing and `FLOOD_EDGE`.
  - The slab sizes and tilts in `slabs`, and `SLAB_HEAVE`.
  - The lip width (0.05 to 0.13 of the width) in the shader.

## Visual pass 2b (2026-11-06), polish

The trench stage compiled first time. Before any change, the Hemorath JVM tests and `hemorath_fight` (19 of 19)
passed. Five native runs of `runBossPresentationReviewClient -PhemorathFightReview=true
-PhemorathReviewOnly=iron_rake,rib_burst` all ended `HEMORATH_FIGHT_REVIEW_OK`. None logged a shader, program or
compile error, so `hemorath_fissure` shape 2 and the new `hemorath_iron` slab shapes compile on this GPU. The
view-space assumption held: the channel renders sunken, not inverted or flat, and no sparkle showed at the review
angles.

### What round 1 showed

- **Slabs.** Pale pink-grey rectangles in two neat rows along every lane. They filled the floor between lanes, so they
  read as paving tiles laid over the floor rather than floor heaved up. They were the brightest thing in the frame:
  the iron shader's chip shape is pale bone dust, and slabs used it.
- **The channel.** It did read as sunken, but the blood stood at 0.78 of the depth and was lit bright red by the seam
  through it. From the boss camera each lane was still mostly a glossy red stripe with a thin dark wall above it.
- **Walls.** Too few pixels to judge from above. From a player's eye (round 2) they were brighter than the floor, a
  smooth grey-pink half-pipe with regular vertical dark stripes (the "tear" noise ran full height). That reads as a
  raised trough, not a pit.

### What was fixed

- **Slabs are the chamber's own floor** (`hemorath_iron.fsh`, `HemorathIronGeometry`). There are two new iron shapes:
  - `SLAB` (2) is the top: the floor's dark brick in half-block bricks and quarter-block courses, lit by its facet.
    Blue runs 0 at the outer edge to 1 at the broken inner edge, which the shader lights as a thin pale cut lip, with
    blood spatter on the inner half.
  - `BROKEN` (3) is every broken face: rough dark stone with crimson strata and fractures, wet low down.

  The states are `SLAB_OUTER`, `SLAB_INNER`, `BROKEN_LOW` and `BROKEN_HIGH` (`SLAB_TOP` is gone). Noise is snapped
  to 16 a block. Chips and rubble keep the pale `CHIP` shape. A heaved slab now matches the floor round it, so the read
  is the floor tearing up along the lip.
- **Slab layout** (`HemorathFissureRules.slabs`):
  - tilt 0.4 to 0.75 rad (was 0.32 to 0.68);
  - width 0.3 to 0.74 of the half-width (was 0.38 to 0.74);
  - one in five missing (was one in seven).

  The rows are less regular and the floor shows between them. Every bound in `slabsFrameTheTrenchNeverCoverItAndHideAtRest`
  still holds.
- **The channel is deeper and the blood lower.**
  - `TRENCH_DEPTH` is 1.0 a block of width (was 0.75), in the rules and the shader together.
  - `WALL_LEAN` is 0.35 (was 0.3), so the far wall faces the camera more.
  - `FLOOD_LEVEL` and `SURGE_LEVEL` are 0.45 and 0.62 (were 0.6 and 0.78), so the wall always shows above the blood.
- **Walls darker than the floor, as a pit is** (`hemorath_fissure.fsh`, the wall branch of `trench`):
  - The top is a thin, ragged lit cut edge.
  - Below it, broken courses of stone sit on a 16-a-block grid, each with a dark ledge line, and darken toward the
    bottom.
  - Voronoi fractures replace the full-height stripes, and dark pockets are broken out one course tall.
  - The wall is lit red only just above the blood (falloff 0.1 of the width), plus the seam's heat low down.

  A first version lit the whole wall red at the commit, which brought the stripe back, so it was tightened.
- **Blood with weight.**
  - Dark oxblood to crimson, marbled, with a glancing sheen and drifting glints.
  - The seam is a buried glowing line (0.07 of the width), white-hot only at the flash.
  - The meniscus is a crisp bright line at the walls; it carries the telegraph's red edge from the boss camera.
- **Review harness** (`HemorathFightClientReview`): new `iron_rake_low` and `rib_burst_low` scenes. They shoot every
  3 ticks from a player's eye 1.62 above the boss floor, three-quarters on (`LOW`). `-PhemorathReviewOnly=iron_rake,rib_burst`
  picks them up.
- **Test change:** `HemorathEffectGeometryTest` now expects the slabs' shapes to be `CHIP`, `SLAB` and `BROKEN`.

### Evidence

- Rounds 1 to 5 are under `build/hemorath-pass2/roundN/`. Each has `stills/` plus one sheet per scene in tick order;
  round 1 has no `_low` scenes.
- The final sheets, each a move's frames in order every 3 ticks:
  - `build/hemorath-pass2/iron_rake.png`
  - `build/hemorath-pass2/rib_burst.png`
  - `build/hemorath-pass2/iron_rake_low.png`
  - `build/hemorath-pass2/rib_burst_low.png`
- `build/hemorath-pass2/compare-pass2-riposte.png` sets the final trenches, from the boss camera and from a player's
  eye, against Hematic Riposte, pass 2's lanes and pass 2b as first written.
- Logs: `build/hemorath-pass2-r1.log` to `-r5.log`, `build/hemorath-pass2-final-test.log`,
  `build/hemorath-pass2-final-gametest.log`.

### Still open

- **From the boss camera at the commit**, a flooded lane is still a band of red. It now has a lit edge, a wall of
  stone above the blood and slab rims, so it reads as a flooded cut rather than paint, but it is the same colour
  family as pass 2's stripes. A darker blood would lose the telegraph's legibility across the floor. I kept legibility.
- **Far lanes from a player's eye** show their walls and slabs but not their blood: the near wall hides it, which is
  correct for a pit. The warning there comes from the slabs and the dark channel.
- **The crowns' swirl** in the Rib Burst lies at floor height over a channel whose blood is below it. It reads as
  ripples on the surface from above, but strictly it floats. It cannot sink: any card under the floor plane is hidden
  by the floor blocks, which is why the channel is traced in the shader.
- **Walls at a low angle** still shade a little like a rounded trough near the far end, where the taper shoals.
- **Not checked:** a body standing in a trench (its feet sit on the floor plane over the pit), the frame cost of five
  lanes with their slabs, motion at real time, and first person from inside a lane.
- **Fixture:** the first frame of each `_low` scene can be black or miss the boss while the review player teleports
  and the chunk redraws.
