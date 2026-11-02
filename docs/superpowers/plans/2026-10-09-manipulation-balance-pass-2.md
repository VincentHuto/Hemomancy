# Manipulation Balance Pass 2 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix every finding of the 2026-10-09 second-pass manipulation audit, in the agreed order: balance-affecting bugs, systemic economy and scaling, Animus re-rank, Ductilis moves, school gap fills, then docs and tooltips.

**Architecture:** All changes stay in the existing manipulation framework: `ManipulationInit` metadata, the per-school `*Manip` classes, the shared reactive/state layers (`ManipulationReactiveEvents`, `common/damage/SchoolCombatEvents`, `SchoolStates`), the cast pipeline (`BloodManipulation.tryPerformAction`, `ManipulationCostLedger`, `ManipulationCastingRules`) and family/tree registries. No new subsystems.

**Tech Stack:** NeoForge 21.1.x for Minecraft 1.21.1, Java 21, Gradle (`./gradlew.bat`), NeoForge GameTests in `src/gameTest/java/com/vincenthuto/hemomancy/gametest/`, JUnit in `src/test/java`.

**Spec:** The audit is recorded in this conversation and summarised in `docs/MANIPULATION_BALANCE_PLAN.md` (pass 1, §1–§10). Task 7 appends pass 2 as §11 of that document.

**Plan-shape note:** This plan fixes *decisions and values* precisely but does not pre-write every code body. The ~60 findings touch ~50 classes whose current code the implementer must read anyway; each step names the exact file, the exact behaviour, the exact numbers and the test that pins it.

## Global Constraints

- NeoForge imports only (`net.neoforged.*`); `DeferredHolder`; `Hemomancy.rloc("snake_case")`. No `net.minecraftforge.*`.
- Player state is read/written only through `HemoCapabilityAccess`.
- Do **not** commit. Leave all work in the working tree (owner preference).
- Do **not** rename or remove any registered manipulation ID. Display-name changes go through lang only.
- Server-authoritative: targeting, validation and payment stay on the server. Any "fails before payment" check goes in `canPerformAction`, which `BloodManipulation.tryPerformAction` runs before draining blood.
- Damage that is *manually computed* by a manipulation multiplies by `SkillPointHelper.getCrimsonMasteryMultiplier(player)` exactly once.
- Use the existing boss predicate (Task 2 broadens it) for every boss cap; never add a second boss list.
- Visuals: reuse the existing `ManipulationVisuals` forms; no new vanilla particle clouds (see `docs/MANIPULATION_VISUALS.md`).
- Lang and docs are edited **only in Task 7**. Implementers of Tasks 1–6 list every player-facing change in their report under "Text to update" instead of editing `en_us.json` or `docs/`.
- New GameTests go in new classes named `ManipulationBalancePass2<Area>GameTests` so tasks never edit the same test file. Reuse helpers/templates from `ManipulationBalancePassGameTests`.
- A baseline of pre-existing failures is in `build/balance2-baseline.log`: 8 JVM failures (PhlegethonticBombardierResource, BloodProfileMigration, ClientModelTextureCoverage, EnglishLocalizationEncoding, 2× GameTestHarnessSourceContract, MorphlingLumenlaceRename, ArmorPreparationResource) and 10 GameTest failures (2 centrifuge, 5 Circus/puppet summon, sampling restriction, pyrosome spawner, Hemojelly). A task is green when it adds no new JVM or GameTest failures relative to that baseline. Full gate: `./gradlew.bat compileJava compileGameTestJava test runGameTestServer --continue --console=plain` (~2 min).

## Review Focus

1. **Old saves with learned entries whose tendency/rank changes** (Tasks 4, 6): `ManipulationRetirementRules.sanitizeKnownManipulations` → `adoptRegisteredMetadata` must refresh tendency, rank, cost; equipped slots and family mastery must survive. Test: an NBT copy of the old `sanguine_marionette` (Animus) adopts Ductilis after sanitize, and stays equipped.
2. **Mob-cast and Drudge paths** reusing player manipulations (`MobManipCaster`, `WillManipulationCaster`, `EntityManipulationEffects`, Drudge actions) must not NPE on new `canPerformAction` checks that assume a `Player`. Test: a Will/mob cast of Blood Aneurysm and Hemorrhage still works after Task 2.
3. **Charge floor vs fixed-charge spells** (Task 3): spells that already require full charge (Lance, Mortar, Ironhearted, Thread Ripper) must be unaffected; QUICK/CONTINUOUS/PASSIVE spells must never hit the charge floor. Test: QUICK Blood Shot casts with chargeTicks 0.
4. **PvP targets** for every hostility filter added in Task 2: hostile players (canHarm true) must remain valid targets for Halo/Aneurysm; tamed pets/villagers must not. Test in Task 2.
5. **Searing removal side effects** (Task 1): Vitric Combustion's Searing bonus must still read remaining duration while Searing is active. Existing `SchoolCombatGameTests` Vitric test must remain green; new test asserts damage stops after expiry.

---

### Task 1: State and reactive-event bugs

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java` (Searing tick ~245-255; Bell consume ~85-100)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationReactiveEvents.java` (Ward refresh ~103; Drift ambush ~175-225; Phoenix readiness ~249)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/tenebris/GloamLacerationManip.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/manipulation/saint/EndlessHourManip.java` (settlement ~136)
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java` (Endless Hour Drudge action ~1272)
- Modify: `FuneralBellManip`, `CarrionCommunionManip` (affliction sets)
- Test: Create `src/gameTest/java/com/vincenthuto/hemomancy/gametest/ManipulationBalancePass2StateGameTests.java`

- [ ] **1.1 Searing expires.** In the `tick` handler, clear `SEARING` when `now >= heat.expires` *or* the target no longer has the Searing effect, before dealing tick damage. Test: apply Searing for 80t to a zombie in a water-free template, wait 140t, assert health stopped dropping after ~t90 and `SchoolStates.has(target, SEARING)` is false.
- [ ] **1.2 Penumbral Drift melee ambush.** In `onAttack` (AttackEntityEvent), if `selected(player,"penumbral_drift") && isChanneling(uuid)`, record `PENUMBRAL_AMBUSH.put(uuid, gameTime)` *before* `ManipulationChannelManager.stop`. In `onFinalDamage`, the ambush applies when `isChanneling(uuid)` **or** `PENUMBRAL_AMBUSH.remove(uuid) == gameTime`. Clear stale entries on player logout. Test: channel Drift, simulate a melee `player.attack(target)`, assert damage ≈ 1.5× unchanneled baseline and target Obscured.
- [ ] **1.3 Gloam ambush and Blood Loss.** Ambush = `player.isInvisible() || SchoolStates.has(player, VEILED) || ManipulationChannelManager.isChanneling(uuid)` (Drift) `|| dark`. Apply Blood Loss only if the hurt succeeded and `ManipulationCombatHelper.canHarm(player, target)`. Test: Veiled caster in full light deals 6 base (×CM 1.0) instead of 3.5; a tamed wolf in the cone receives no Blood Loss.
- [ ] **1.4 Sanguine Ward tap-cycle.** When a ward ends (expiry or release), store `(remainingPool, endGameTime)` per player. A refresh within 100 ticks of `endGameTime` resumes at the stored remaining pool instead of 6. Test: start ward, absorb 4 damage, let it lapse, recast at +30t → pool is 2 (+1 pulse = 3), not 6; recast at +120t → fresh 6.
- [ ] **1.5 Endless Hour settlement respects death protection.** Replace `setHealth`/`die` repayment with a single `player.hurt(<bypass-armor, bypass-effects damage source attributed to the hour>, debt)` so totems and Phoenix Debt trigger normally. Change the Drudge action to Absorption I for 200t (`MobEffects.ABSORPTION, 200, 0`) with description "Grants the Drudge a brief absorption ward". Test: player holding a Totem of Undying with lethal debt survives at expiry and the totem is consumed.
- [ ] **1.6 Phoenix Debt readiness.** The ready visual and the trigger both require `volume.getBloodVolume() >= 2000` (read the live registered cost, not a literal; Task 6 changes the cost). Test: with 500 mL, lethal damage kills normally and no ready visual is queued.
- [ ] **1.7 Funeral Bell partial charge keeps unpaid debt.** On consumption, subtract only `stored × charge` from the Necrosis entry; leave `stored × (1−charge)` to release at expiry. Test: 6 stored debt, half-charge Bell → bonus 3, entry retains 3.
- [ ] **1.8 Insatiable Hunger is an affliction.** Add `EffectInit.insatiable_hunger` to Funeral Bell's +2 affliction set (spent on detonation like Wither/Poison/Blood Loss) and to Carrion Communion's afflicted-target predicate. Test: Bell vs a target with only Insatiable Hunger deals 9 base at full charge.
- [ ] **1.9 Run** `./gradlew.bat compileJava compileGameTestJava test runGameTestServer --continue --console=plain`, compare failures with baseline, fix regressions.

### Task 2: Targeting, payment and boss bugs

**Files:**
- Modify: `ManipulationReactiveEvents.java` (`isBoss` ~436; Sovereign Instinct ~358; Coronation recast)
- Modify: `common/manipulation/ManipulationCombatHelper.java` (new helpers below)
- Modify: `mortem/ExsanguinateManip.java`, `mortem/HemorrhageManip.java`, `mortem/GraveDebtManip.java`, `mortem/InsatiableHungerManip.java`, `animus/BloodAneurysmManip.java`, `animus/VitalEffusionManip.java`, `congeatio/RimeboundSentenceManip.java`, `congeatio/OsseousBloomManip.java`, `congeatio/GlacialRampartManip.java`, `congeatio/GlacialGraspManip.java`, `animus/BloodShotManip.java`, `ferric/BloodNeedleManip.java`, `entity/projectile/BloodShotEntity.java`, `BloodNeedleEntity.java`, `ferric/LodestoneCollapseManip.java`, `lux/HematicBeaconManip.java`, `flammeus/ScaldingUpdraftManip.java`, `flammeus/FurnaceVeinsManip.java`, `animus/CrimsonFlameConjurationManip.java`, `mortem/LignumMortisManip.java`, `animus/BloodBindingManip.java` (root sync), `ferric/FerricConstructEntity` (spikes attribution), `animus/CrimsonCoronationManip.java`, `tenebris/BlackVeilCovenantManip.java`, `HematicCommandManager.java`
- Modify: `ManipulationInit.java` (Activation Potential Drudge action ~342)
- Test: Create `ManipulationBalancePass2TargetingGameTests.java`

- [ ] **2.1 One boss predicate.** `ManipulationReactiveEvents.isBoss(LivingEntity)` returns true for the current set **plus** `entity.getType().is(net.neoforged.neoforge.common.Tags.EntityTypes.BOSSES)`, `Warden`, `ElderGuardian`. Test: Warden and Elder Guardian are bosses; a zombie is not.
- [ ] **2.2 Exsanguinate.** `BLOOD_RESTORE` 600 → 250. Damage vs `isBoss` targets capped at `12 × CM`. Test: kill at ≤30% HP refunds exactly 250 (minus cost 300 → net −50).
- [ ] **2.3 Osseous Bloom.** Use the broadened `isBoss` cap (12). Move the "no target in radius" check to `canPerformAction`. Test: Warden takes ≤12; empty radius costs 0 mL.
- [ ] **2.4 Validate before payment** in `canPerformAction` (message + `return false`, same style as Exsanguinate): Hemorrhage, Grave Debt, Insatiable Hunger, Blood Aneurysm, Vital Effusion (no bonemealable block in area), Rimebound Sentence (no aimed target), Glacial Rampart (no surface). Each must also work when the caster is not a `Player` (mob/Will paths skip the check or use the same targeting). Test: each spell cast with no target leaves blood volume and cooldown unchanged.
- [ ] **2.5 Hostile-only auto-targeting.** Add `ManipulationCombatHelper.isHostileTarget(Player caster, LivingEntity e)`: `canHarm(caster,e) && (e instanceof Enemy || (e instanceof Mob m && m.getTarget() == caster) || e instanceof Player)`. Use it for Sanguine Halo's auto-fire and Blood Aneurysm's target pick. Test: Halo with a cow and a zombie nearby fires only at the zombie; a tamed wolf is never picked.
- [ ] **2.6 Manipulation projectiles ignore weapon enchantments.** Player-cast Blood Shots and Needles pass `null` as the vanilla `firedFromWeapon`; keep any tendency/weapon context the entity needs in its own field (`combatWeaponItem`) without exposing it via `getWeaponItem()`. Test: Blood Shot from a player holding a Sharpness V sword deals the same damage as from an empty hand.
- [ ] **2.7 Ferric Spikes are Ferric hits.** Capture the cast's `SchoolHitContext` on `FerricConstructEntity` spawn (same pattern as `SchoolDamage.captureSpawn`) and hurt through `SchoolDamage.attributed(...)` as DIRECT, base 3.5 × CM captured at cast. Test: spike contact applies Lodestone.
- [ ] **2.8 Lodestone Collapse line of sight.** Skip targets with no clear `ClipContext` (COLLIDER, NONE) from the aim point to the target's eye. Test: a zombie behind a stone wall from the aim point is not pulled or damaged.
- [ ] **2.9 Glacial Grasp rain.** Include `isInWaterOrRain()` in `wet`. Test: unit test of the wet predicate or GameTest with `level.setWeatherParameters` rain.
- [ ] **2.10 Hematic Beacon** illuminates any `canHarm` LivingEntity in the zone, not only `Mob`.
- [ ] **2.11 Updraft needs footing.** `canPerformAction` fails unless `onGround() || isInWater() || onClimbable()`. Test: airborne cast refused without payment.
- [ ] **2.12 Build permission.** Add `ManipulationCombatHelper.mayAlter(Player p, BlockPos pos)`: `p.mayBuild() && p.level().mayInteract(p, pos)`. Gate block placement/removal in Crimson Flame Conjuration, Furnace Veins (melting), Glacial Rampart, Glacial Grasp water freezing, Lignum harvesting and Sanguine Magnetism pillar placement if it places blocks. Test: adventure-mode player cannot place crimson flames.
- [ ] **2.13 Binding holds players.** When the bound target is a `ServerPlayer`, set `hurtMarked = true` after zeroing velocity each tick so the client receives it.
- [ ] **2.14 Sovereign Instinct respects command limits.** Redirect only mobs that pass `HematicCommandManager.canCommand` (80 max HP, not boss); bosses keep the Pressure +1 response. Test: a Ravager (100 HP) is not redirected.
- [ ] **2.15 No refresh while active.** Crimson Coronation and Black Veil Covenant `canPerformAction` refuse while their effect/zone is still active for the caster.
- [ ] **2.16 Activation Potential Drudge action** becomes a shock: every `Monster` within 5 of the Drudge gets Disrupted (`SchoolStates.apply(drudge, m, DISRUPTED, 10)`) and 3 magic damage; description "Shocks hostiles near the Drudge".
- [ ] **2.17 Run** the full gate as in 1.9.

### Task 3a: Cast pipeline — charge floor, rewards, Crimson Mastery coverage

**Files:**
- Modify: `common/manipulation/BloodManipulation.java` (`tryPerformAction` ~452-510), `ManipulationCastingRules.java`
- Modify: `common/network/.../UseManipKeyPacket.java` (charge ticks ~44-72)
- Modify: `KnownManipulationEvents.java` (~201-209 skill points), `BloodTendencyEvents.java` (cast alignment)
- Modify: `common/damage/SchoolCombatEvents.java` (~89-91 Aneurysm Pressure bonus)
- Modify (CM coverage): `ductilis/SynapticStormManip`, `lux/WhiteVerdictManip`, `mortem/FuneralBellManip`, `animus/CrimsonCoronationManip` (via `ManipulationReactiveEvents` lance base), `congeatio/RimeboundSentenceManip`, `ferric/IronChoirManip` (+ intercept damage), `mortem/CarrionCommunionManip`, `flammeus/FurnaceVeinsManip`, `flammeus/ScaldingUpdraftManip`, `ductilis/DeadlyGazeManip` (slam), `ManipulationReactiveEvents` (Phoenix eruption 8, Iron Retort 4), `mortem/GraveDebt` burst (`SchoolHitHelper`), `animus/BloodCloudManip` / `CloudEntityBlood` (capture CM at spawn), `tenebris/EclipseWellManip` if it deals damage after Task 5
- Test: Create `ManipulationBalancePass2CastGameTests.java`; JUnit for `ManipulationCastingRules`

- [ ] **3a.1 Charge floor.** Add `ManipulationCastingRules.MIN_CHARGE_FRACTION = 0.25`. For `EnumManipulationType.CHARGED`, a cast whose charge fraction is below 0.25 is refused before payment with "Not enough charge". QUICK/CONTINUOUS/PASSIVE ignore it. Spells that already require full charge are unaffected. JUnit: `chargedCost(…, 0.1)` → refused; `0.25` → 25% cost.
- [ ] **3a.2 Rewards scale with charge.** For CHARGED casts, alignment gain, family mastery uses and skill-point payout are multiplied by the charge fraction (mastery uses: award 1 use only if fraction ≥ 0.5). Test: a 0.3-charge Synaptic Storm grants 0.3× alignment and no mastery use.
- [ ] **3a.3 Server clamps charge.** The server records the game time when the manipulation key-down for the selected slot arrives and clamps the client-reported charge ticks to `elapsed server ticks + 2`. If no start was recorded, treat charge as 0 for CHARGED spells.
- [ ] **3a.4 Aneurysm Pressure bonus scales with charge** (`2 × stacks × hit.charge()`), like Bell and Lodestone.
- [ ] **3a.5 Crimson Mastery everywhere damage is computed by hand.** Multiply once by CM in: Synaptic Storm, White Verdict, Funeral Bell (base and affliction terms; Necrosis debt stays unscaled), Crimson Coronation lance base, Rimebound Sentence, Iron Choir intercept damage, Carrion Communion pulse, Furnace Veins tick, Scalding Updraft burst, Deadly Gaze slam, Phoenix eruption, Iron Retort thorns, Grave Debt burst, Blood Cloud tick and bolt (captured at spawn). State DoTs (Searing 1/s, Blood Loss, Necrosis release) stay flat by design. Test: at CM level 3 (×1.45) Synaptic Storm's per-target damage is 1.45× the level-0 value.
- [ ] **3a.6 Run** the full gate.

### Task 3b: Economy, affinity and profile systems

**Files:**
- Modify: `SkillPointHelper.java` (`getSanguineSurgeRegen` ~155; Last Wind), `BloodVolumeEvents.java` (~120-124)
- Modify: `TendencyAffinityRules.java` (~21-30)
- Modify: `SynapticLoadoutActionPacket.java` (~78-96)
- Modify: `VascularSystemEvents.java` (~137-157), `VascularSystem.java`, `UseManipKeyPacket.java` (~213 dead-section gate), `ManipulationCostLedger.java`
- Modify: `BloodTendencyEvents.java` (~71-131), `HemoServerConfig.java` (~182-184)
- Modify: loot tables under `src/main/resources/data/hemomancy/loot_table/` (Harbinger outpost, Sainted Mausoleum treasure, Blood Temple chest)
- Test: Create `ManipulationBalancePass2EconomyGameTests.java`; JUnit for pure rules

- [ ] **3b.1 Sanguine Surge** = `level × 0.25` mL/tick (5 / 10 / 15 mL/s). **Last Wind** = `level × 0.5` mL/tick below 10% (max 30 mL/s). Both stay cap-exempt. JUnit on the pure formulas.
- [ ] **3b.2 Secondary never dilutes.** `TendencyAffinityRules` returns `max(primaryMultiplier, 0.75 × primary + 0.25 × secondary)`. JUnit: primary 2.0 / secondary 1.0 → 2.0; primary 1.0 / secondary 2.0 → 1.25.
- [ ] **3b.3 Preset swap lockout.** Applying a Synaptic loadout preset starts a 200-tick lockout on further preset applies (message with seconds left) and puts every newly equipped manipulation on a 40-tick cooldown. Test: second apply within 200t is refused.
- [ ] **3b.4 Vein strain matters.** Strain per cast by rank: Humilis 1, Mediocritas 1, Summa 2, Magister 2, Perfectus 3. A CLOTTED section adds +25% cost (new `ManipulationCostLedger` term "Clotted vein"). A DEAD section refuses manipulations routed through it (same check muscle memories use), message names the section. Test: a DEAD HEAD section refuses Blood Shot (HEAD) without payment; a CLOTTED HEAD section charges 125 mL for it.
- [ ] **3b.5 No Animus fallback.** Kills of blooded mobs with no blood profile give **no** tendency shift (remove the Animus default). Test: killing an unprofiled blooded test mob leaves all tendencies unchanged.
- [ ] **3b.6 Separate cast-shift config.** Add `tendencyShiftOnCastEnabled` (default true); cast alignment uses it instead of `tendencyShiftOnKillEnabled`.
- [ ] **3b.7 Loot breadth.** Add full memories to chest loot: `memory_prismatic_reproof` and `memory_lumen_suture` (Lux), `memory_searing_lash` (Flammeus), `memory_cruor_javelin` (Congeatio) to the Harbinger outpost table (weight 2 each), and `memory_white_verdict`-tier is **not** added. Verify item IDs exist; follow existing entry format. Test: existing loot-table JSON/resource tests stay green.
- [ ] **3b.8 Run** the full gate.

### Task 4: Animus re-rank and command moves

**Files:**
- Modify: `ManipulationInit.java`, `common/init/ManipulationTreeInit.java`, `common/manipulation/family/ManipulationFamilyRegistry.java`
- Modify: `animus/BloodRushManip.java` / `BloodRushEffect` / `EffectInit` (amp), `animus/BloodCloudManip.java`, `CloudEntityBlood.java`, `KnownManipulationEvents.java` (~67-81 avatar DR), `animus/BloodBindingManip.java`, `HematicCommandManager.java`, `animus/SanguineMarionetteManip.java`
- Move: `animus/CrimsonFlameConjurationManip.java` → `flammeus/`; `animus/SanguineMarionetteManip.java` → `ductilis/` (update imports; `SanguineMarionetteRules` stays)
- Modify: memory-weaving recipes in `src/main/resources/data/hemomancy/recipe/memory_weaving/` for moved entries (enzyme of the new primary)
- Test: Create `ManipulationBalancePass2RosterGameTests.java`

- [ ] **4.1 Re-rank.** MEDIOCRITAS: `hematic_mortar`, `sanguine_halo`, `blood_lattice`, `summon_avatar`, `summon_avatar_arms`. MAGISTER: `sanguine_tempest`, `summon_avatar_complete`. Update tree placement so nodes sit in their new rank band. Family forms keep their mastery thresholds. Test: each entry's `getRank()`; a D1 player with Shot mastery L3 can learn Mortar; D3 cannot learn Tempest.
- [ ] **4.2 Blood Rush.** Effect amplifier 1 → 0, duration 250t → 160t, cooldown 60t → 160t. Healing stays 0.5 HP/s. Test: effect amplifier 0 and duration 160.
- [ ] **4.3 Blood Cloud family.** Cloud damage hits are DIRECT on first contact per target per cloud (so Pressure applies once), PERIODIC afterwards. Damage uses CM captured at spawn (from 3a.5). Expansive radius 3 → 4. Tempest stays static (Task 7 fixes its text). Test: a zombie in a basic cloud gains Pressure.
- [ ] **4.4 Avatar damage reduction** skips damage tagged `DamageTypeTags.BYPASSES_INVULNERABILITY` and `DamageTypes.STARVE`. Test: void damage is not reduced.
- [ ] **4.5 Chain Binding** duration 120t → 80t (its lang already says "shorter"). Lattice unchanged.
- [ ] **4.6 Sanguine Marionette → Ductilis.** Primary DUCTILIS, secondary ANIMUS. Remove it from the Blood Binding family and add it to the Hematic Rebuke family as a mastery-L2 form (Impressment stays L3). Marionette on a Blood-Bound target releases the binding instead of refusing. Existing learners keep it known (no un-learning). Old-save test per Review Focus 1.
- [ ] **4.7 Sovereign Instinct → Ductilis.** Primary DUCTILIS, secondary ANIMUS, rank MAGISTER; move its tree node under Hematic Rebuke/Impressment. Animus's Magister slots are now Tempest and Avatar Complete.
- [ ] **4.8 Secondaries.** `sanguine_tempest` secondary MORTEM → DUCTILIS. `crimson_tithe` secondary LUX → ANIMUS.
- [ ] **4.9 File moves.** Crimson Flame Conjuration class to `flammeus/`; Marionette class to `ductilis/`.
- [ ] **4.10 Weaving recipes** for Marionette and Sovereign Instinct draw the Ductilis (Neurotic) enzyme in place of Vivacious; Tempest's recipe swaps its Mortem enzyme for Ductilis.
- [ ] **4.11 Run** the full gate.

### Task 5: School gap fills and per-school tuning

**Files:** `ManipulationInit.java` plus the named classes below.
**Test:** Create `ManipulationBalancePass2SchoolGameTests.java`.

Ferric
- [ ] **5.1 Sanguine Magnetism crush.** When the pillar expires, every target it pulled within 3 blocks takes `(4 + 2 if Lodestone) × CM` as a DIRECT Ferric hit. Summa now has an attack.
- [ ] **5.2 Iron Choir volley.** Each pulse also fires one needle (`BloodNeedleEntity`, base 0.4 → ~3 × CM) at the nearest hostile (`isHostileTarget`) within 10 blocks. Intercepts unchanged.
- [ ] **5.3 Ferric Rampart.** Projectiles owned by the caster or the caster's allies pass through; others are destroyed. Duration 6s → 10s. Cost 450 → 350 (constant in `FerricBarrierManip`). Spikes keep 450.
- [ ] **5.4 Glacial Rampart** duration 25–30s → 12s.

Flammeus
- [ ] **5.5 Updraft forms.** `suspended_updraft` and `expulsive_updraft` → MEDIOCRITAS. Expulsive: vertical launch 1.1 → 0.6, burst `3 × CM` in radius 4 plus Searing 60t and outward knockback.
- [ ] **5.6 Pyretic Forge** → HUMILIS, cost 350 → 150.
- [ ] **5.7 Cauterizing Rebuke.** Cost 275 → 150; HP cost per purged ailment 2 → 1; after purging, enemies within 4 take `3 × CM` fire damage and Searing 60t (DIRECT Flammeus).
- [ ] **5.8 Lumen Suture.** Absorption II 20s → Absorption I 10s (heal 2 and cleanse unchanged).
- [ ] **5.9 Phoenix Debt** cost 2000 → 1200.
- [ ] **5.10 Sanguine Ignition** sets targets on fire for 2s (use the unused `FIRE_SECONDS`, set it to 2).

Congeatio
- [ ] **5.11 Rimebound Sentence** damage `2 + 6c` → `4 + 8c` (× CM from 3a).
- [ ] **5.12 Absolute Stillness** pulses deal `1.5 × CM` freeze damage to targets already at 3 Rime.
- [ ] **5.13 Water identity.** Cruor Javelin adds +1 Rime (3 total) against wet targets (`isInWaterOrRain`).

Lux
- [ ] **5.14 White Verdict** `2 + 8c` → `3 + 9c` (× CM from 3a).
- [ ] **5.15 Radiant Wake** pulse deals `1 × CM` Lux damage to Illuminated enemies within its radius (DIRECT Lux, EXPLOIT role so it does not re-apply).

Mortem
- [ ] **5.16 Insatiable Hunger** → HUMILIS; deals `2 × CM` magic damage on cast (DIRECT Mortem, so it seeds Necrosis).
- [ ] **5.17 Bloom of Rot** deals `3 × CM` to each affected target.
- [ ] **5.18 Worked Lignum** → MEDIOCRITAS (frees a Mortem Humilis slot for combat).

Tenebris
- [ ] **5.19 Black Veil Covenant** cooldown 220t → 900t.
- [ ] **5.20 Eclipse Well.** Each 20t pulse deals `1.5 × CM` to Obscured targets inside. At full charge, when the well collapses, every non-boss enemy within 2 blocks of the centre is randomly teleported up to 8 blocks (chorus-fruit style, `randomTeleport`, End flavour).

Ductilis
- [ ] **5.21 Hemolymphal Pulse** cost 400 → 175, cooldown 300t → 200t, affects only `canHarm` targets.
- [ ] **5.22 Living Circuit** cost 180/s → 100/s; each pulse arcs `2 × CM` to the nearest hostile within 4 of each linked recipient (REACTION, no state).
- [ ] **5.23 Thread Ripper general use.** Also works on any hostile summoned/owned mob: `Vex`, or any `OwnableEntity` whose owner is not the caster or an ally → unravel (discard with the existing unravel visual). Bosses excluded.
- [ ] **5.24 Run** the full gate.

### Task 6: (folded) — none

Task numbering keeps 7 for docs so later references stay stable.

### Task 7: Lang, reference, wiki and plan doc

**Files:** `src/main/resources/assets/hemomancy/lang/en_us.json`, `docs/HEMOMANCY_REFERENCE.md` (§8.2, §9, Drudge table ~2400-2440, skill text), `docs/MANIPULATION_BALANCE_PLAN.md` (§11 + corrections), `wiki/*.md` that mention changed spells, dialogue/Mnemonist lines (e.g. `memory_gloam_laceration.line2`).

- [ ] **7.1** Apply every "Text to update" item reported by Tasks 1–5.
- [ ] **7.2** Fix audit drift: Void Shroud (Veiled, not invisibility), Hematic Flare (single target), Sanguine Ignition, Cauterizing Rebuke, Lumen Suture (cleanses Necrosis/Obscured, heals), Bloom of Rot (Necrosis + damage), Umbral Reversal/Eclipse Well ("obscures", not "blinds"), Black Veil Covenant, Penumbral Drift, Absolute Stillness (boss text, Rime), Iron Choir (3 intercepts + volley), Cryogenic Pulse, Osseous Bloom display name → "Crystalline Bloom" (ID unchanged) and %-HP text, Blood Cloud ("withers" removed), Sanguine Tempest (static storm with lightning), Summon Avatar (no entity), Chain Binding, Synaptic Jolt, Hematic Beacon (no Regeneration), White Verdict, Gloam Laceration desc (add missing key), Dynamic Use ("cost discount"), Crimson Mastery tooltip, Sanguine Surge/Last Wind values, Blood Rush.
- [ ] **7.3** Restore the overwritten Drudge table rows in the reference.
- [ ] **7.4** Fix class Javadocs/comments that still describe old effects (`BloodEclipseManip`, `CryogenicPulseManip`, `OsseousBloomManip`).
- [ ] **7.5** Append §11 "Pass 2" to `MANIPULATION_BALANCE_PLAN.md` with the new roster table and correct pass-1 claims (Coronation CM, Drift ambush, Animus healing).
- [ ] **7.6** Validate JSON parses; run `./gradlew.bat test verifyPackagedClientResources --console=plain`.

### Task 8: Final verification

- [ ] **8.1** `./gradlew.bat build test runGameTestServer --continue --console=plain > build/balance2-final.log`; compare with baseline; no new failures.
- [ ] **8.2** Whole-change review against this plan (fresh reviewer).
