# 04. Cast economy, Temper and manipulation balance numbers

Review pass 2026-11-13. Nothing here is built. Source: `docs/Open Questions.md`.

## Status on 2026-11-14

| Task | State |
| --- | --- |
| T1 | Built (P03, B04): LP-6, LP-7, LP-9 (NQ-04-9 A: the carried debt is collected every second, blocks a new Tithe, survives death). B04 also drove `DrudgeActions.graveDebt` in the Grave Debt GameTest and removed the duplicate effect add. |
| T2 | Built (P03, B04): CB-6, CB-8, CB-9, CB-10, and a death during Endless Hour restarts its cooldown (B04). |
| T3 | Built (P03, B04): LP-13, CQ-16, CB-19, CB-25, LP-14 (NQ-04-8 bands, channels included), CQ-9 with Sated Siphon to a worn gourd (NQ-04-10 B). |
| T4 | Built (P03, B04): CB-2, CB-1 (2 mL/s; LP-10 A). |
| T5 | Built (B04, B05): T5a (Sovereign Instinct 200, Ironhearted 600, Void Shroud 300, Radiant Wake and Living Circuit 30), T5b (the NQ-04-5 channel prices, Thread Ripper 300) and T5c (NQ-04-6 B: the sheet `04-T5c-price-sheet.csv` written from the code for the 41 damaging R casts; twelve cut in `ManipulationInit`, the rest at or above target, the updrafts left out). |
| T6 | Built (B04): `BossBindingRules`, `BossBindings`, the gate in `BloodBindingManip.applyBinding`, the boss hold in `BloodBindingSculpting.pulse`, two lang lines, JVM and GameTests. |
| T7 | Built (P03, B04): the tank half of CQ-8, the vein at 12 mL per extra ore (B04), Hewing by hardness with a 36 mL floor (B04). |
| T8 | Built (B04): the `blood_moon` line only for D1 or higher in the Overworld. The drain is unchanged (CB-11). |
| T9 | Built (P03). The carried dome stays as built (NQ-04-12 A). |
| T10 | Built (P03, B04): the RV-28, CB-27 and CQ-8 records are in `MANIPULATIONS_AND_CASTING.md`. The stale strand lines in `docs/archive/tracks/core_loop/` stay, per CQ-14. |
| PT-25 (no task) | Built (B04): 90% of what was paid (NQ-04-15 A), the probe asserts it, and the playthrough doc uses PT-25. |
| CQ-17 (no task) | Built (B04) to NQ-04-11 B. |

Open questions and remaining work for this chunk are tracked in `docs/OPEN_QUESTIONS.md`. The New questions and Still
to do tables below are the 2026-11-13 snapshot.

## Summary

These rulings close the blood loops (discount floor, refunds, Tithe debt, free vein, ring farming), make Temper bite
(slower scar refill, burn by price, best-fit catalysts), cut the regeneration skills and reprice the channels and the
high-rank casts. The largest piece is one registry repricing pass (CB-4, LP-11, CB-5, CB-7, CB-22, CB-23, LP-12). The
second largest is the new boss binding limit (LP-16), which is a design with open choices.

Paths below that start with `common/` or `config/` sit under `src/main/java/com/vincenthuto/hemomancy/`.

What checking the code turned up:

- All 115 R cast costs in `ManipulationInit` still match `manipulations.csv`. Only its Shaping columns are stale.
- The 0.4x floor does not close LP-7 alone: Exsanguinate then costs 120 mL and still returns 250. Build both rules.
- Only the three lashes are strands now. CB-27 has nothing registered to act on. LP-10 would reprice lash numbers set
  on 2026-11-12, so it is on the conflict sheet.
- PT-25 cannot be built as written: the Staff and cell hands have no wear. The flat 900 mL refund against a
  discounted conjure price is free blood at any discount above 10%.
- CQ-17 as written (`canHarm`) would refuse every wild and player-built vanilla golem.
- The Blood Moon has no region (one flag for the server). Its drain is 25 mL/s, not 75, and only below D1.
- At scar tier 3 the ruled refill (0.3/s) still outpaces a standard scarred Shaping burn (0.25/s).
- A Drudge's Grave Debt pays its owner 250 mL per death today, for a cast the owner never paid for.

Counts: 38 rows. BUILD 27, KEEP 6, DONE 0, MOOT 1, CONFLICT 1, UNCLEAR 2, OWNER 1, PARKED 0.

## Rulings reviewed

| Id | Ruling as understood | Verdict | Checked against | Task |
| --- | --- | --- | --- | --- |
| LP-6 | Stacked cast discounts never go below 0.4x. | BUILD | `common/capability/player/harbinger/manip/ManipulationCostRules.java` `snapshot`: multiplies every line, no floor. Lines come from `ManipulationCostLedger.collect`. Shaping pulses, lash taps and snaps use the same ledger total, so one floor covers all. | T1 |
| LP-7 | Exsanguinate and Grave Debt refund 60% of what was paid. Grave Debt only on the caster's kill. | BUILD | `common/manipulation/mortem/ExsanguinateManip.java` `BLOOD_RESTORE` 250. `common/manipulation/ManipulationStatusRules.java` `GRAVE_DEBT_DEATH_REFUND` 250, paid by `SchoolHitHelper.tryRefundGraveDebt` from `ManipulationStatusEvents.onLivingDeath` on any death. Grave Debt lost its held form on 2026-11-12. The R cast refund is still there. | T1 |
| LP-9 | Unpaid Tithe debt is carried forward (first option). | BUILD | `common/manipulation/saint/CrimsonTitheManip.java` `tickDebt`: pays `min(due, volume)` and drops the rest. State is three root keys in player persistent data. How the carried debt is collected is open (NQ-04-9). | T1 |
| LP-10 | Price strands toward 2x a cast's value per mL. | CONFLICT | `common/manipulation/verb/Shapings.java`: only `bloodLash`, `searingLashStrand`, `gloamLacerationLash` call `Strands.lash`. Their tap and charge numbers (`LashProfile`) were set on 2026-11-12 and are the owner's to tune in the tuning client. By those numbers a tap still beats its R cast about 16 to 19 times per mL. | - |
| LP-11 | Shapings may undercut their channels. Reprice the channels with CB-4. | BUILD | Shapings unchanged: `LignumMortisShaping.BLOOD_PER_PULSE` 8 (16 mL/s), `FurnaceVeinsShaping.BLOOD_PER_PULSE` 6 (12 mL/s). Channels in `ManipulationInit`: lignum_mortis 100, canopy_mortis 125, worked_lignum 175, furnace_veins 150. CB-4 gives no number for these four (NQ-04-5). | T5 |
| LP-12 | Each rank buys about 25% more damage per mL, mostly by cutting high-rank costs. | BUILD | `manipulations.csv` has 41 damaging R casts (Humilis 14, Mediocritas 11, Summa 9, Magister 2, Perfectus 5). All 115 registry costs match the file. How to count area casts is open (NQ-04-6). | T5 |
| LP-13 | Scar floor refills at 0.1 x tier per second. | BUILD | `common/capability/player/harbinger/temperament/TemperRules.java` `FLOOR_REFILL_PER_SECOND_PER_TIER` 0.25. Tier 3 still never runs a 1/s Shaping dry at 0.1 (NQ-04-7). | T3 |
| LP-14 | Shapings and strands burn 0.5 to 2 Temper per second by price band. R casts burn 2 to 8. | BUILD | `TemperRules.PULSE_COST` 0.5 per 10 ticks for every Shaping. `TemperRules.castCost` returns 1 to 5 by rank. `TemperCharge.forPulse` takes no price. The lash tap burns no Temper (`StrandService` pays `TAP` with no charge). Band edges are open (NQ-04-8). | T3 |
| LP-16 | New design. A boss takes a fixed number of successful bindings, set by its max health (1,000 HP gives 10). Each attempt can also fail by a chance that grows with max health and with how healthy the boss is. Failed attempts do not count. | BUILD | `common/manipulation/animus/BloodBindingManip.java` `applyBinding` force-adds the hold on a boss. `BloodBindingShaping.pulse` renews it every 10 ticks. Lattice is 22 mL a pulse (44 mL/s) on 8 bodies. No counter, no fail roll today. `common/entity/boss/BossHold.java` is a boss holding a player and is not this. Blocked on NQ-04-1 to NQ-04-4. | T6 |
| CQ-9 | A full body still draws Temper from a creature. The blood is lost. | BUILD | `common/item/harbinger/tool/living/BloodAbsorptionChannelRules.java` `canDrainLivingTarget`: refuses a full body unless Sated Siphon. Called from `BloodAbsorptionItem` and `LivingStaffItem.absorbWithStaff`. This makes Sated Siphon do nothing (NQ-04-10). | T3 |
| CQ-16 | Bone block gives 9 Mortem Temper. No work-scale multiplier. | BUILD | `common/manipulation/mortem/CarrionRules.java` `Remains.BONE_BLOCK` 24. `temper()` multiplies skulls and bone blocks by the work scale. Flesh 2, bone 3, skull 10 and 4 mL a pulse are as the sheet says. | T3 |
| CQ-8 | The Talaromyces vein pays 6 to 12 mL per extra ore. Staff rings count only blood spent or drawn from creatures. | BUILD | `common/capability/player/harbinger/scar/fungal/VeinMinerHelper.java` `tryVeinMine`: no payment, up to `OreVeins.MAX_BLOCKS` 64. `LivingStaffItem.recordUtilityBloodHandled` is called for tank absorption and for projection into tanks. | T7 |
| CB-1 | Base regeneration 2 mL/s, if strand prices stay. | BUILD | `config/HemoServerConfig.java` `bloodRegenRate` default 1.0 per 20 ticks. The condition holds while LP-10 is unanswered. | T4 |
| CB-2 | Surge 0.05 a level (1 to 3 mL/s). Candle 5 mL/s. Rib gourd halved. | BUILD | `SkillPointHelper.sanguineSurgeRegenPerTick` 0.25 per level. `common/effect/MnemonicCandleRules.java` `BLOOD_REGEN_PER_TICK` 1.5. `common/item/harbinger/tool/EnumBloodGourdTiers.java` `RIB` generation 0.25 per tick. | T4 |
| CB-4 | Avatar 10 to 45. Choir 40 to 60. Stillness 60 to 80. Communion 40 to 60. Wake and Circuit 30. | BUILD | `ManipulationInit`: avatar stages 75, 125, 175, 250, 400. iron_choir 250, absolute_stillness 300, carrion_communion 220, radiant_wake 120, living_circuit 100. All as the sheet says. Points inside the ranges need picking (NQ-04-5). | T5 |
| CB-5 | Thread Ripper costs 250 to 350 mL. | BUILD | `ManipulationInit` thread_ripper: 35, CHARGED, Summa. | T5 |
| CB-6 | Charged casts need 50% charge (first option). | BUILD | `common/manipulation/ManipulationCastingRules.java` `MIN_CHARGE_FRACTION` 0.25. | T2 |
| CB-7 | Sovereign Instinct costs 200 mL when it fires (first option). | BUILD | `ManipulationInit` sovereign_instinct 450, paid by `ManipulationReactiveEvents.trigger` through `tryPerformPassiveTrigger`. | T5 |
| CB-8 | Osseous Bloom caps at 20 on anything over 100 max HP. | BUILD | `common/manipulation/congeatio/OsseousBloomManip.java`: `HP_FRACTION` 0.25, `BOSS_DAMAGE_CAP` 12 on bosses only. | T2 |
| CB-9 | Hematic Beacon heals twice as much per pulse. | BUILD | `ManipulationReactiveEvents.pulseHematicBeacon`: `ally.heal(0.4F)` once a second. | T2 |
| CB-10 | Endless Hour has a 1,200-tick cooldown after it ends. | BUILD | `ManipulationInit` endless_hour `setCooldownTicks(200)`. `EndlessHourManip.DURATION_TICKS` 200. | T2 |
| CB-11 | The Blood Moon discount goes only to Harbingers in the moon's region (first option). Confirm the drain. | BUILD | `ManipulationCostLedger.collect` adds 0.75 whenever `BloodMoonEvents.isBloodMoonActive` is true. That is one flag on the Overworld's saved data, true in every dimension, with no region. Drain: `BLOOD_MOON_DRAIN_AMOUNT` 50 per 40 ticks (25 mL/s), only on players below D1, no sky check. "The moon's region" needs a meaning (NQ-04-13). | T8 |
| PT-25 | The conjuration dispel refund scales with wear. | UNCLEAR | `common/manipulation/ferric/ConjurationManip.java` `dispelHeld`: refund is 90% of `IDispellable.getBaseCost()` (1,000). `ItemInit` registers the Staff and both cell hands with no durability, so there is no wear. The probe `PlaythroughNeophyteGameTests.dispelRefundIsSyncedAndRecorded` only logs the amount. It does not fail. NQ-04-15. | - |
| CB-19 | Spend the smallest catalyst that covers the shortfall. | BUILD | `common/capability/player/harbinger/temperament/TemperCatalysts.java` `find`: off hand, then first inventory slot. | T3 |
| CB-20 | Hematic Hewing costs 2 mL per hardness point. | BUILD | `common/manipulation/ferric/StaffMiningRules.java` `HEW_BLOOD_PER_PULSE` 36 flat per cut (2, 3 or 9 blocks). `LivingDigRules.ML_PER_HARDNESS` 2, cap 40. A pure hardness price makes a stone tunnel cheaper than now (NQ-04-14). | T7 |
| CB-22 | Ironhearted costs 150 mL per absorb HP (first option). | BUILD | `ManipulationInit` ironhearted 300. `IronheartedRules.IRON_HEART_HEALTH_PER_CAST` 4, duration 12,000 ticks. | T5 |
| CB-23 | Void Shroud costs the same as 60 mL/s. | BUILD | `ManipulationInit` void_shroud 100, cooldown 20. `VoidShroudManip.DURATION_TICKS` 100. | T5 |
| CB-25 | A Staff weapon-form swap burns 1 Temper. | BUILD | `common/item/harbinger/tool/living/LivingStaffWeaponFormHelper.java` pays `HOT_SWAP` only. `StaffWeaponFormManip.chargesTemper` is false. | T3 |
| CB-27 | 10-tick spray snap wait. First plant pulse priced as a full plant. | MOOT | `Shapings.java` registers no spray or plant. `Strands.spray` and `Strands.plant` are called only from `StrandGameTests` and `FirstConversionsGameTests`. The kit code still snaps a spray with no wait (`StrandService.snap`). | - |
| CB-29 | Accept that combat Absorption does not pay for combat. | KEEP | Nothing in `BloodAbsorptionItem` or `LivingStaffFocusRules` changed after 2026-11-10. Lashes still cost 15, 40 and 60 mL/s. | - |
| CL-9 | The pool is called Temper. | KEEP | `TemperRules`, `BloodTemper`. | - |
| CL-10 | One Temper rule for R casts and Shapings, never refused. | KEEP | `TemperRules.Kind`: `SHAPING(1.5, 0.75)`, `CAST(1.25, 0.9)`. | - |
| CL-11 | Death keeps half the Temper. | KEEP | `TemperRules.KEPT_ON_DEATH` 0.5, applied in `TemperEvents.onPlayerClone`. | - |
| RV-24 | Keep balance passes 1 and 2 as built. | KEEP | `EndlessHourManip.deferDamage`, `ManipulationFamilyRegistry` (Marionette under hematic_rebuke), `LivingCircuitManip`, `ManipulationCastingRules.clampReportedCharge`. None touched after 2026-11-11. CB-6 and CB-10 change numbers beside these rules, not the rules. | - |
| RV-28 | Keep round one prices and tune them with sections A and C. | KEEP | Per-second prices are unchanged. The "strands 15 to 60" range now covers three lashes. The eight rebuilt held manipulations kept 35 to 60 mL/s and are Shapings now. `LivingDigRules.ML_PER_HARDNESS` 2. `WorldAnswerRules.MOON_BY_PHASE` unchanged. I did not open the Formation key constant. | - |
| CQ-17 | Sanguine Mending picks targets by `canHarm` and takes the mod's constructs. | UNCLEAR | `common/manipulation/ferric/SanguineMendingShaping.java` `mendable`: tag `mendable_constructs` (iron and snow golem), not targeting the caster. `ManipulationCombatHelper.allied` needs an owner or a team, which vanilla golems lack. NQ-04-11. | - |
| CQ-41 | The field of dark hides only its caster. | BUILD | `common/world/ground/DarkFields.java` `covers(body)` hides any body inside any field. The hung field still exists (`EclipseSnuffShaping` feeds it, up to 2,400 ticks, radius 3 to 8). The carried dome from 2026-11-12 uses the same `covers` (NQ-04-12). | T9 |
| PR-16 | Play Temper with LP-13 and LP-14 in mind. | OWNER | Needs T3 built first. | - |

## Plan

### T1. Close the cost loops: floor, refunds, Tithe debt (size M)

- Rulings: LP-6, LP-7, LP-9
- Change:

  | File | Symbol | Now | Ruled |
  | --- | --- | --- | --- |
  | `common/capability/player/harbinger/manip/ManipulationCostRules.java` | `snapshot` | product of all lines, no floor | the product of the discount lines is held at 0.4 or above. Surcharges (dry Temper, purity, pome corruption, clotted vein) still multiply on top |
  | `common/manipulation/mortem/ExsanguinateManip.java` | `BLOOD_RESTORE` | 250 mL flat | 60% of the blood this cast paid |
  | `common/manipulation/ManipulationStatusRules.java` | `GRAVE_DEBT_DEATH_REFUND` | 250 mL flat, any death | 60% of what the caster paid, only when the caster has the kill |
  | `common/manipulation/saint/CrimsonTitheManip.java` | `tickDebt` | pays `min(due, volume)`, rest forgiven | the unpaid rest stays owed |

  How LP-6 and LP-7 combine. The floor alone leaves the loop open (120 mL paid, 250 back). With both, the refund is
  always 60% of the paid price, so a kill always costs 40% of it. At the floor that is 48 mL for Exsanguinate and 52 mL
  for Grave Debt.

  The floor: show it as its own ledger line (a `discount_floor` contribution that lifts the total back to 0.4) so the
  cost tooltip explains it and the diagnostics payload keeps its layout. A blocked line (purity 0) must stay blocked.

  The refunds: carry the paid amount into the cast (the cast scope `BloodManipulation.tryPerformAction` opens before
  `getAction`). Exsanguinate reads it. Grave Debt stores it on the target beside the owner id in
  `SchoolHitHelper.markGraveDebt`. A Drudge or entity cast paid nothing, so it refunds nothing. The kill test reads the
  death's kill credit. Pay refunds through `BloodFlowLedger.applyDirectIncome`, not `volume.fill`.

  The Tithe: keep a debt amount after the window closes, take from the pool as blood comes in, and refuse a new Tithe
  while debt is owed. Plan to NQ-04-9's recommendation unless the owner rules otherwise.
- Files: the four above, plus `common/capability/player/harbinger/manip/ManipulationCostLedger.java`,
  `common/manipulation/SchoolHitHelper.java`, `common/manipulation/ManipulationStatusEvents.java`,
  `common/manipulation/BloodManipulation.java`, `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: `ManipulationCostRulesTest` (floor, floor with a surcharge, blocked line), `ManipulationStatusRulesTest`
  (60%), `ManipulationBalanceGameTests` (the Tithe test already drives `tickDebt`), a GameTest for Grave Debt paying
  only on the caster's kill and nothing for a Drudge's cast. `PersistentDataAllowListTest` counts stay the same if no
  new `getPersistentData()` call is added to `CrimsonTitheManip` or `SchoolHitHelper`.
- Docs: `docs/MANIPULATIONS_AND_CASTING.md` Rulings that bind, `docs/HEMOMANCY_REFERENCE.md` cast cost section,
  `docs/OPEN_QUESTIONS.md` roll-up, `wiki/Blood-Systems.md` if it names the refunds
- Depends on: nothing

### T2. Single-cast fixes (size S)

- Rulings: CB-6, CB-8, CB-9, CB-10
- Change:

  | File | Symbol | Now | Ruled |
  | --- | --- | --- | --- |
  | `common/manipulation/ManipulationCastingRules.java` | `MIN_CHARGE_FRACTION` | 0.25 | 0.5 |
  | `common/manipulation/congeatio/OsseousBloomManip.java` | damage cap | 12 on bosses only | also 20 on any target over 100 max HP. Bosses keep 12 |
  | `common/manipulation/ManipulationReactiveEvents.java` | `pulseHematicBeacon` heal | 0.4 a pulse | 0.8 a pulse |
  | `common/init/ManipulationInit.java`, `common/manipulation/saint/EndlessHourManip.java` | endless_hour cooldown | 200 ticks from the cast, equal to the hour | 1,200 ticks counted from the hour's end |

  Endless Hour: start the cooldown when the hour ends. Setting 1,400 at cast would let cooldown reductions eat into
  the gap.
- Files: the five above
- Tests: `ManipulationCastingRulesTest`, `ManipulationChargeVisualGameTests` and `ManipulationBalancePass2CastGameTests`
  where they use a 25% charge, a GameTest for Osseous Bloom on a 200 HP non-boss
- Docs: `docs/MANIPULATIONS_AND_CASTING.md` (the "25% charge floor" line and Rulings that bind),
  `docs/HEMOMANCY_REFERENCE.md`
- Depends on: nothing

### T3. Temper pass (size M)

- Rulings: LP-13, LP-14, CQ-9, CQ-16, CB-19, CB-25
- Change:

  | File | Symbol | Now | Ruled |
  | --- | --- | --- | --- |
  | `common/capability/player/harbinger/temperament/TemperRules.java` | `FLOOR_REFILL_PER_SECOND_PER_TIER` | 0.25 | 0.1 |
  | same | `castCost(rank)` | 1, 2, 3, 4, 5 | 2 to 8. Proposed 2, 3.5, 5, 6.5, 8 |
  | same | `PULSE_COST` / `pulseCost` | 0.5 per 10 ticks for every Shaping (1 per second) | 0.5 to 2 per second by price band (NQ-04-8) |
  | `common/capability/player/harbinger/temperament/TemperCharge.java` | `forPulse` | no price input | takes the Shaping's blood per second |
  | `common/item/harbinger/tool/living/BloodAbsorptionChannelRules.java` | `canDrainLivingTarget` | active blood and (not full or Sated Siphon) | active blood. Blood over the cap is lost |
  | `common/manipulation/mortem/CarrionRules.java` | `Remains.BONE_BLOCK` | 24 | 9 |
  | same | `temper(remains, workScale)` | skulls and bone blocks times the work scale | never above the remain's worth. A dry pulse still gives less |
  | `common/capability/player/harbinger/temperament/TemperCatalysts.java` | `find` | off hand, then first slot | the smallest worth that covers the shortfall. Off hand breaks a tie |
  | `common/item/harbinger/tool/living/LivingStaffWeaponFormHelper.java` | swap payment | 250 mL, no Temper | plus 1 Temper in the form's primary school |

  CB-25 detail (my call): the swap is never refused. A dry swap pays the cast dry rate (1.25x blood).
  Channels (continuous R casts) burn 1 per second today. Put them on the same price bands once T5 sets their prices.
- Files: the six above, plus `common/manipulation/BloodManipulation.java` (passes the price to `forPulse`),
  `common/item/harbinger/tool/living/BloodAbsorptionItem.java`, `common/item/harbinger/tool/living/LivingStaffItem.java`
- Tests: `TemperRulesTest`, `CarrionRulesTest`, `BloodAbsorptionChannelRulesTest`, `TemperCatalystDataTest` or a new
  best-fit test, `LivingStaffWeaponFormRulesTest`, `TemperValidationGameTests`
- Docs: `docs/archive/tracks/core_loop/04-temper-and-catalysts.md`, `docs/MANIPULATIONS_AND_CASTING.md` Rulings that bind,
  `docs/HEMOMANCY_REFERENCE.md` Temper section, `docs/OPEN_QUESTIONS.md` (CQ-9, CQ-16 move to rulings)
- Depends on: NQ-04-7, NQ-04-8 and NQ-04-10 for the last numbers. Everything else can be built now.

### T4. Body regeneration (size S)

- Rulings: CB-1, CB-2
- Change:

  | File | Symbol | Now | Ruled |
  | --- | --- | --- | --- |
  | `config/HemoServerConfig.java` | `bloodRegenRate` default | 1.0 per 20 ticks | 2.0 |
  | `common/capability/player/shared/skill/SkillPointHelper.java` | `sanguineSurgeRegenPerTick` | 0.25 per level (5, 10, 15 mL/s) | 0.05 per level (1, 2, 3 mL/s) |
  | `common/effect/MnemonicCandleRules.java` | `BLOOD_REGEN_PER_TICK` | 1.5 (30 mL/s) | 0.25 (5 mL/s) |
  | `common/item/harbinger/tool/EnumBloodGourdTiers.java` | `RIB` passive generation | 0.25 per tick (5 mL/s) | 0.125 per tick (2.5 mL/s) |

  A dev world keeps the old config value until its server config file is edited or deleted.
- Files: the four above, `src/main/resources/assets/hemomancy/lang/en_us.json` if the skill or gourd text states a rate
- Tests: `MnemonicCandleRulesTest`, `BloodGourdTierStatsTest`, `BloodRegenRulesTest` (unchanged, it tests the
  multiplier), a small JVM test for the Surge rate
- Docs: `docs/HEMOMANCY_REFERENCE.md` blood section, `wiki/Blood-Systems.md`,
  `docs/archive/tracks/core_loop/10-daily-loop-and-world-answer.md`
- Depends on: LP-10's answer only in that CB-1 was accepted "if strand prices stay"

### T5. Registry repricing pass (size L)

- Rulings: CB-4, LP-11, CB-5, CB-7, CB-22, CB-23, LP-12
- Change: one edit of `ManipulationInit`, made from one approved price sheet, so no number moves twice.

  | Entry | Now | Ruled |
  | --- | --- | --- |
  | summon_avatar, _arms, _armor, _legs, _complete | 75, 125, 175, 250, 400 mL/s | 10 to 45. Proposed 10, 18, 26, 35, 45 |
  | iron_choir | 250 mL/s | 40 to 60. Proposed 50 |
  | absolute_stillness | 300 mL/s | 60 to 80. Proposed 70 |
  | carrion_communion | 220 mL/s | 40 to 60. Proposed 50 |
  | radiant_wake | 120 mL/s | 30 |
  | living_circuit | 100 mL/s | 30 |
  | lignum_mortis, canopy_mortis, worked_lignum | 100, 125, 175 mL/s | no number given. Proposed 30, 40, 50 |
  | furnace_veins | 150 mL/s | no number given. Proposed 40 |
  | thread_ripper | 35 mL | 250 to 350. Proposed 300 |
  | sovereign_instinct | 450 mL | 200 |
  | ironhearted | 300 mL for +4 HP | 600 mL for +4 HP (150 per HP) |
  | void_shroud | 100 mL per 100 ticks | 300 mL per 100 ticks (60 mL/s) |
  | 41 damaging R casts | flat near 0.02 HP/mL | target by rank: 0.020, 0.025, 0.031, 0.039, 0.049 HP/mL |

  LP-12 scope. 41 rows of `manipulations.csv` deal damage on R. The worst against the target are Synaptic Storm (900 mL,
  8 HP), Lodestone Collapse (900, 10), Eclipse Well (900, 15), White Verdict (800, 12), Funeral Bell (1,000, 17),
  Rimebound Sentence (750, 12), Blood Aneurysm (400, 8), Umbral Rend, Fruiting Contagion, Bloom of Rot and Sanguine
  Magnetism. Casts already above the target (Sanguine Halo, the clouds, Sanguine Tempest, Blood Needle Lance,
  Hemorrhage) are left alone unless the owner says otherwise. The updrafts are priced for movement and stay out.

  Staging:
  1. T1 and T2 first. The floor and the charge minimum change what every cost means.
  2. Regenerate `manipulations.csv` from the code (its Shaping and strand columns are stale after 2026-11-12).
  3. Write the proposed cost column for the 41 casts and the channel rows above. The owner approves the sheet.
  4. Apply the sheet to `ManipulationInit` in one edit.
  5. Only then answer LP-10. Lash value is measured against the R cast, and step 4 moves the R cast.
- Files: `src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java`,
  `docs/audits/2026-11-11-visual-balance/manipulations.csv`, `docs/audits/2026-11-11-visual-balance/tuning-sheet.csv`
- Tests: every source-text test that quotes a registry line with its cost (`BloodBindingManipulationSourceTest` does
  this for Blood Binding, so check the others under `src/test/java/com/vincenthuto/hemomancy/common/manipulation/`),
  `ManipulationBalanceGameTests`, `ManipulationBalancePassGameTests`, `ManipulationBalancePass2EconomyGameTests`,
  `ManipulationBalancePass2SchoolAGameTests`, `ManipulationBalancePass2SchoolBGameTests`, `AvatarManifestationRulesTest`
- Docs: `docs/MANIPULATIONS_AND_CASTING.md`, `docs/HEMOMANCY_REFERENCE.md` section 8 tables, `docs/CORE_LOOP.md`
  price lines, `docs/OPEN_QUESTIONS.md`
- Depends on: T1, T2, NQ-04-5, NQ-04-6

### T6. Boss binding limit (size M)

- Rulings: LP-16
- Change: a new rule for bosses, applied at the one place both the R cast and the Shaping bind a boss
  (`BloodBindingManip.applyBinding`). All four forms go through it, Lattice included.

  What the design needs:
  - A pure rules class beside `BloodBindingManip`: the total a boss allows from its max health, and the fail chance
    from its max health and current health. The owner's example gives 1 binding per 100 max HP.
  - A counter of successful bindings on the boss itself, saved with the entity, so a relog does not refill it. That is
    a new entity marker (a line in `persistent-data-allow-list.txt`) or an entity attachment.
  - One counter per boss, shared by every player. The owner wrote "10 TOTAL bindings".
  - A definition of one binding for the held Shaping. Today a pulse renews the hold every 10 ticks. Counting each
    pulse would spend ten bindings in five seconds. Not counting renewals would let one binding last for ever.
  - A wait after a failed attempt. Without one the Shaping re-rolls twice a second and the fail chance only delays.
  - Two action-bar lines (the binding slipped, this body will not be bound again) as `en_us.json` keys.
  - The half-length boss hold (`BOSS_DURATION_TICKS`) and the boss's half tail stay.

  | File | Symbol | Now | Ruled |
  | --- | --- | --- | --- |
  | `common/manipulation/animus/BloodBindingManip.java` | `applyBinding` on a boss | always `forceAddEffect` | refused once the boss has taken its total. Otherwise a fail roll, then the hold, then count one |
  | `common/manipulation/animus/BloodBindingShaping.java` | `pulse` on a boss | renews every 10 ticks with no end | one binding per hold, per NQ-04-1 |
  | new rules class | total | none | example: max HP / 100 |
  | new rules class | fail chance | none | grows with max HP and with current health. Numbers per NQ-04-3 |
- Files: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodBindingManip.java`,
  `src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodBindingShaping.java`,
  `src/test/resources/architecture/persistent-data-allow-list.txt`,
  `src/main/resources/assets/hemomancy/lang/en_us.json`
- Tests: a JVM test for the new rules, GameTests in `FirstConversionsGameTests` (a boss takes its total and then
  refuses, a failed roll does not count, Lattice counts each boss once), `CombatTargetingGuardrailTest` stays green
- Docs: `docs/BOSSES.md`, `docs/MANIPULATIONS_AND_CASTING.md` Rulings that bind, `docs/HEMOMANCY_REFERENCE.md`
  section 8.0.1
- Depends on: NQ-04-1, NQ-04-2, NQ-04-3, NQ-04-4. The one-boss-definition question G-2c also touches it
  (`ManipulationReactiveEvents.isBoss` decides who is a boss here).

### T7. Mining and Staff rings (size M)

- Rulings: CQ-8, CB-20
- Change:

  | File | Symbol | Now | Ruled |
  | --- | --- | --- | --- |
  | `common/capability/player/harbinger/scar/fungal/VeinMinerHelper.java` | `tryVeinMine` | free after the first ore, up to 64 | 6 to 12 mL per extra ore. Proposed 12, the Lode Draw price. The vein stops where the blood stops |
  | `common/item/harbinger/tool/living/LivingStaffItem.java` | `recordUtilityBloodHandled` call sites | tank absorption and tank projection count toward rings | only Shaping blood, creature draws and digging count |
  | `common/manipulation/ferric/StaffMiningRules.java` | `HEW_BLOOD_PER_PULSE` | 36 mL per cut | 2 mL per hardness point for each block cut (`LivingDigRules.bloodPerBlock`), per NQ-04-14 |

  The vein pays through `BloodAccounting.pay` with a new named `Source`. The session key that feeds the rings also
  decides whether a short press was a tap ("moved no blood"). Split the two so tank transfers still count as blood
  moved for the tap rule but not for rings.
- Files: the three above, `common/manipulation/ferric/HewingShaping.java`, `common/manipulation/ferric/OreVeins.java`
- Tests: `StaffMiningRulesTest`, `LivingStaffGrowthRulesTest`, `BloodAccountingGuardrailTest` stays green, GameTests
  for a paid vein that stops when blood runs out and for a tank round trip adding nothing to the rings
- Docs: `docs/archive/tracks/core_loop/08-living-staff-and-mining.md`, `docs/HEMOMANCY_REFERENCE.md`, `docs/OPEN_QUESTIONS.md` (CQ-8)
- Depends on: NQ-04-14 for the Hewing floor

### T8. Blood Moon discount (size S)

- Rulings: CB-11
- Change:

  | File | Symbol | Now | Ruled |
  | --- | --- | --- | --- |
  | `common/capability/player/harbinger/manip/ManipulationCostLedger.java` | `blood_moon` line | 0.75 for every caster in every dimension while the moon is up | 0.75 only for a D1 or higher caster in the moon's region. Planned as the Overworld, per NQ-04-13 |

  The drain, confirmed by reading: 50 mL every 40 ticks (25 mL/s), only on players below D1, and only if their blood
  is active. There is no sky check. The sheet's "75 mL/s from exposed players" is wrong. No change planned.
- Files: the one above. `common/event/worldevent/BloodMoonEvents.java` only if the drain changes
- Tests: a GameTest for the ledger line in and out of the Overworld and at D0
- Docs: `docs/HEMOMANCY_REFERENCE.md` world answer section, `docs/archive/tracks/core_loop/10-daily-loop-and-world-answer.md`,
  the audit's section 3.11 figure
- Depends on: NQ-04-13

### T9. Field of dark hides only its caster (size S)

- Rulings: CQ-41
- Change:

  | File | Symbol | Now | Ruled |
  | --- | --- | --- | --- |
  | `common/world/ground/DarkFields.java` | `covers(body)` over `FIELDS` | any body inside any hung field is veiled | only the field's owner is veiled by their own hung field |

  Mobs inside still get Obscured. The carried dome (`CARRIED`, built 2026-11-12) is left as it is until NQ-04-12.
- Files: the one above, `common/damage/SchoolAwareness.java` only if the call changes shape
- Tests: `DarkFieldRulesTest`, a GameTest that a second player in the field is still seen
- Docs: `docs/MANIPULATIONS_AND_CASTING.md`, `docs/HEMOMANCY_REFERENCE.md`, `docs/OPEN_QUESTIONS.md` (CQ-41)
- Depends on: nothing

### T10. Record the keeps and refresh the audit files (size S)

- Rulings: CB-29, CL-9, CL-10, CL-11, RV-24, RV-28, CB-27
- Change: no code. Move the six keeps to "Rulings that bind". Mark CB-27 as moot with the reason. Fix the stale lines:
  the strand rows of the audit, "strands 15 to 60" where it still lists jets, sprays, plants and brands.
- Files: `docs/MANIPULATIONS_AND_CASTING.md`, `docs/archive/tracks/core_loop/04-temper-and-catalysts.md`,
  `docs/archive/tracks/core_loop/12-strand-combat.md`, `docs/OPEN_QUESTIONS.md`
- Tests: none
- Docs: as listed
- Depends on: nothing

## Conflicts with recent work

| Id | Owner's answer | What changed after 2026-11-11 | Question for the owner |
| --- | --- | --- | --- |
| LP-10 | Price strands toward 2x a cast's value per mL. | Fourteen of the seventeen strands are gone. The three lashes got a new damage model on 2026-11-12 (tap, charged crack, sweeps) with numbers in `LashProfile` that you tune in the tuning client. Taps cost 8, 24 and 12 mL for about 2.7, 7 and 4.6 HP. That is still 16 to 19 times an R cast per mL. Reaching 2x would mean taps near 70 to 175 mL, or far cheaper R casts. | Do the lash prices you set stand, or should the lashes be repriced toward 2x after the R casts are repriced (T5)? Do the eight rebuilt held manipulations (35 to 60 mL/s) get the same test? |

## New questions

| New id | From | Question | Options | Recommendation |
| --- | --- | --- | --- | --- |
| NQ-04-1 | LP-16 | What is one binding on a boss for the held Shaping, which renews every 10 ticks? | A: one binding is one hold. It lasts the boss length (60 ticks, Lingering 120, Chain 40, Lattice 80) and then drops, whatever the player holds. B: every pulse is a binding. C: one binding lasts while blood flows. | A. B spends the whole total in seconds. C makes one binding endless. |
| NQ-04-2 | LP-16 | How is the total set and when does it come back? | Total: A: max HP / 100, at least 1 (Wither 3, Warden 5, 1,000 HP boss 10). B: a table per boss. Reset: A: never, for the life of that entity. B: when the boss resets its fight. Scope: one counter per boss, or one per player. | Max HP / 100 with a floor of 1. One counter per boss, shared. Never reset on the same entity. I did not check whether a Saint that returns to its tomb is the same entity. |
| NQ-04-3 | LP-16 | What is the fail chance, and what does a fail cost? | Formula: A: max HP / 2000, held between 10% and 60%, then times (0.5 + 0.5 x health fraction). A 1,000 HP boss at full health fails half the time. B: max HP only. Cost: blood paid or refunded. Wait after a fail: none, or about 3 seconds. | A. Blood is paid. The R cast's cooldown starts. The Shaping cannot retry that boss for 60 ticks. |
| NQ-04-4 | LP-16 | What else counts as a hold on a boss? | The constricted slow and the Animus brand the Shaping lays with each hold. The Blood Lash charged crack, which lays Slowness VII for up to 40 ticks on anything (built 2026-11-12, no boss check). | Slow and brand land only with a successful binding. Leave the lash root alone until you have played it. |
| NQ-04-5 | CB-4, LP-11, CB-5, CQ-8 | Several rulings are ranges, and LP-11 names four channels without a price. Which points? | Proposed: Avatar 10, 18, 26, 35, 45. Choir 50. Stillness 70. Communion 50. Thread Ripper 300. Vein 12 mL per extra ore. Lignum, Canopy, Worked Lignum 30, 40, 50. Furnace Veins 40. | The proposed set. It keeps each channel at two to three times its own Shaping. |
| NQ-04-6 | LP-12 | Is damage per mL measured on one target for every cast? | A: one target always. Synaptic Storm would fall from 900 mL to about 165. B: bolts on one target, area casts on three. Storm lands near 500, Collapse near 600. | B. A prices area casts as if the crowd were not there. |
| NQ-04-7 | LP-13 | At tier 3 the scar refills 0.3/s and a standard Shaping burns 0.25/s after the scar's cut. That school still never runs dry. Accept? | A: accept. Dear Shapings (1.5 to 2 per second under LP-14) do run dry. B: flat 0.1/s at every tier. C: cap the scar's burn cut at 50%. | A, then judge in PR-16. |
| NQ-04-8 | LP-14 | Where do the price bands sit, and does the lash tap burn Temper? | Bands by base mL/s: up to 10 burns 0.5/s, up to 25 burns 1, up to 45 burns 1.5, above burns 2. R casts 2, 3.5, 5, 6.5, 8 by rank. Tap: no burn (as built 2026-11-12), or 0.5 per tap. | The proposed bands. Leave the tap free for now. |
| NQ-04-9 | LP-9 | How is carried Tithe debt collected, and does death clear it? | A: taken from the pool every second until paid, no new Tithe meanwhile, survives death. B: same, cleared on death. C: added to the next Tithe's bill. | A. Dying should not be the cheap way out of a Saint's debt. |
| NQ-04-10 | CQ-9 | Once any full body can draw, Sated Siphon does nothing. What becomes of it? | A: cut the skill. B: Sated Siphon sends the lost blood to a worn gourd. C: without it a full body draws Temper at half rate. | B. It keeps the skill worth its 375 points. |
| NQ-04-11 | CQ-17 | `canHarm` treats only owned or teamed creatures as allies. Wild and player-built vanilla golems are neither, so the ruling as written stops them being mended. Which rule? And which mod entities join the tag? | A: allies only. B: allies, plus an unowned golem that is not targeting any player. Entities: Drudge, Ferric Rampart wall, iron pillar, or a wider list. | B, with Drudge, `ferric_rampart` and `iron_pillar`. |
| NQ-04-12 | CQ-41 | Does "caster only" also apply to the carried dome of 2026-11-12? It hides everyone inside, costs 60 mL/s and moves with the caster. | A: leave the dome as built. B: caster only. C: caster and allies. | A. The dome already pays an upkeep, which was one of the three fixes the question offered. |
| NQ-04-13 | CB-11 | The Blood Moon is one flag for the whole server. What is "the moon's region"? And is the real drain right (25 mL/s, players below D1 only)? | Region: A: the Overworld. B: the Overworld under open sky. C: make moons regional. Drain: keep, or also drain Harbingers under the sky. | A, and keep the drain. C is a new feature. |
| NQ-04-14 | CB-20 | A pure hardness price makes a two-block stone tunnel cost 6 mL where it costs 36 now. Keep a floor? | A: pure 2 mL per hardness point per block. B: the larger of 36 mL and the hardness sum. | B. The file's own rule is that Hewing costs more per block than the pickaxe. |
| NQ-04-15 | PT-25 | The Staff and cell hands have no wear. The refund is a flat 900 mL while the conjure is paid after discounts, so a 25% discount makes 150 mL per conjure and dispel. What should the refund follow? | A: 90% of what was paid. B: falls with time held. C: give the conjured items durability again. | A. It closes the loop with the same rule as LP-7. |

## Still to do

| Id | What is left | Who | Waiting on |
| --- | --- | --- | --- |
| LP-10 | Decide whether the lash prices stand. | Owner | T5 applied, then a play session |
| LP-16 | Answer the four design questions, then build. | Owner | NQ-04-1 to NQ-04-4 |
| LP-12 | Approve the price sheet for the 41 damaging casts. | Owner | T5 step 3 |
| PT-25 | Pick what the refund follows. | Owner | NQ-04-15 |
| CQ-17 | Pick the heal rule and the construct list. | Owner | NQ-04-11 |
| PR-16 | Play Temper: dry penalty, first casts, notice pace, scar refill. | Owner | T3 built |
| CB-1 | Look again at 2 mL/s if lash prices change. | Later | LP-10 |
| CB-27 | The jet, spray, plant and brand kits are used only by tests. Keep or delete them is another chunk's call. If a kit returns to a memory, apply the snap wait and the plant price first. | Later | The strand kit ruling |
| RV-28 | Correct the docs that still list seventeen strands. | Build | T10 |
| PT-25 | `docs/PLAYTHROUGH_TESTS.md` calls this D-25. `docs/OPEN_QUESTIONS.md` uses D-25 for the Chamber gateway. One of them needs a new id. | Later | Docs pass |
| LP-7 | Drudge and entity casts of Grave Debt stop refunding once T1 lands. Say so in the Drudge action text if it promises blood. | Build | T1 |
