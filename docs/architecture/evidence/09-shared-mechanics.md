# 09 — Cross-cutting shared mechanics audit (Hemomancy, NeoForge 1.21.1)

Read-only audit. Paths are relative to `src/main/java/com/vincenthuto/hemomancy/common/`. **VERIFIED** means I read the code at the cited lines. **HYPOTHESIS** means the code shape suggests it but I have not traced the runtime (usually event-listener order or the content of a data tag). The four known cooldown stores are left out on purpose.

## 0. Families located

| Family | Main classes found |
|---|---|
| Manipulations | `manipulation/**` (BloodManipulation, ManipulationCombatHelper, ManipulationReactiveEvents, ManipulationStatusEvents, BodyIdiomEvents, HematicCommandManager, HematicRiposteEvents, EntityManipulationEffects), `init/ManipulationInit` |
| Armor | `event/ArmorSetBonusHandler`, `armor/ArmorSetHelper`, `armor/ability/{ArmorSetAbilityRegistry,SimpleArmorSetAbility,SilentArchon…,SheolicBastion…,PhanstmalBloodlust…,EdaciousBloodburst…}` |
| Morphlings | `item/harbinger/morphlings/*` (30 files incl. `MorphlingItem`, `MorphlingCombat`), `capability/player/harbinger/morphling/EquippedMorphlingEvents` |
| Scars | `init/ScarInit`, `item/harbinger/scar/ScarDefinition`, `capability/player/harbinger/scar/{ScarsContainer,ScarsEntityEventHandler,fungal/*}` |
| Skills / body | `SkillPointHelper`, `event/BodyRefinementSkillEvents`, `event/ToggleableSkillEvents`, `musclememory/*`, `bloodvolume/BloodVolumeEvents` |
| Mob effects | `effect/*` (BloodLoss, Hemolysis, Elytra, SilverWard, …), `item/harbinger/BloodInjectionEvents`, `brewing/AdvancedBrewEffects` |
| Living weapons and memories | `item/harbinger/tool/living/*` (Blade, Axe, Crossbow, Tool, Sickle, Torch, Spear, Staff), `item/harbinger/memory/*`, `memories/CrudeMemoryShardItem` |
| Unstained | `rite/unstained/*` (StillWaters, LetheCovenant, PaleConsecration; all extend `RiteZoneSavedData`), `event/UnstainedMilestoneHandler`, `capability/player/unstained/*` (PurityGainEvents, WhiteHumorVolume), `init/StillArtInit` |
| Summons and entities | `entity/summon/*` (BoundSummonBehavior, BloodThrallEntity, GroundPuppetEntity, FerricConstruct…), bosses under `entity/boss/**` |
| Hazards | `block/harbinger/PhlegethonticIchorBlock`, `PelagicColonyBlock`, `event/ViridSalisTrailHandler` |

## 1. Shared infrastructure that already exists (extend these; don't build new ones)

Most "shared mechanism" recommendations below just mean "use what's already here more widely":

- **`damage/SchoolDamage` + `mixin/core/MixinSchoolDamage` + `damage/SchoolCombatEvents.prepare`.** The mixin wraps every `hurt()` source made inside a `SchoolDamage.Scope` (`SchoolDamage.java:143-159`). At HIGH priority, `prepare` then cancels school hits the owning player may not harm (`SchoolCombatEvents.java:74-79`), applies `TendencyAffinityRules`, state payoffs and boss caps. Manipulations, morphlings (`MorphlingCombat.scope`, used in all 13 morphling items) and some living weapons already run through it. Armor, scars, Still Arts and most living-weapon melee code do not.
- **`manipulation/ManipulationCombatHelper`** (`allied`/`canHarm`/`isHostileTarget`/`hostileTargets`/`aimedTarget`, lines 21-97). This is the most complete ally, owner, impressed, PvP and creative check in the mod. It has 80 call sites, about 95% of them in manipulations.
- **`damage/SchoolStates.canApply`** (`SchoolStates.java:38-46`) runs the same `canHarm` gate on status application.
- **`BloodFlowLedger.applyDrain`** (`bloodvolume/BloodFlowLedger.java:99-138`) handles the dormant check, Sanguine-Reserve protection for ARMOR/MORPHLING/SCAR/TOOL/… categories (`:140-144`), records the drain for the flow UI and syncs. Siblings: `MaxBloodLedger`, `ManipulationCostLedger`.
- **`event/LastRiteHelper`** is the shared death-save gate (`LastRiteHelper.java:22-51`).
- **`event/TransientServerStateEvents`** is the server start/stop reset hub for 19 static stores (`:35-55`).
- **`entity/HemoEntityPredicates`** holds the blood-type predicates (NOBLOOD and others).
- **`rite/zone/RiteZoneSavedData`** is already the base for all three Unstained zones. This is a good example of consolidation that has already happened.

---

## 2. Damage calculation and modification

### 2.1 Handler pipeline (VERIFIED: registration, priority and effect)
All handlers are registered through `@EventBusSubscriber` annotation scanning (211 classes, 2 manual registrations). That means **order within one priority level is not defined by any contract.**

| Stage / priority | Handler (file:line) | Effect |
|---|---|---|
| Incoming HIGHEST | `item/harbinger/tool/living/LivingSpearCombatEvents.java:37` | records spear hit |
| Incoming HIGH | `damage/SchoolCombatEvents.java:63` | PvP/ally cancel, affinity ×, state bonus +, boss caps (Osseous Bloom 12, Exsanguinate) |
| Incoming NORMAL | `armor/ability/SilentArchonArmorAbilityHandler.java:198` | cancels physical damage and Monolithic Dislocation damage |
| Incoming NORMAL | `item/harbinger/BloodInjectionEvents.java:30-39` | cryoprotection cancels freeze; ender/explosion resistance × `RESISTANCE_MULTIPLIER` |
| Incoming NORMAL | `manipulation/ManipulationStatusEvents.java:60-73` | Iron Retort × `IRON_RETORT_DAMAGE_MULTIPLIER` |
| Incoming NORMAL | `manipulation/animus/HematicRiposteEvents.java:98` | parry cancel |
| Incoming NORMAL | `rite/unstained/LetheCovenantEvents.java:59` | cancels MAGIC damage in a covenant zone while Silver Ward is active |
| Incoming NORMAL | `event/ChamberVisitEvents:78`, `FungalProjectionEvents:61`, `HematicCommandManager:286`, `ductilis/AxonalTransductionManager:270`, `mission/vicar/EarlyInitiation:209` | context protections |
| Incoming LOW | `manipulation/ManipulationReactiveEvents.java:156-187` | Sanguine Ward pool absorption (static map) |
| Incoming LOWEST | `manipulation/BodyIdiomEvents.java:34-68` | Blackhearted wither metabolism, Iron Heart pool absorption |
| Incoming LOWEST | `entity/weave/WeaveDamageEvents.java:31` | health floor |
| Pre HIGH | `event/UnstainedMilestoneHandler.java:152-180` | Silver Ward ×(1−r) against `HEMOMANCY_MOB` attackers; +flat damage dealt to them at ABSOLVED purity |
| Pre HIGH | `musclememory/MuscleMemoryEvents.java:77-88` | Second Pulse −flat |
| Pre NORMAL | `bloodvolume/BloodVolumeEvents.java:275-302` | Iron Will ×, then **blood drain ∝ current damage** |
| Pre NORMAL | `vascular/VascularSystemEvents.java:37-64` | **vein strain ∝ current damage** |
| Pre NORMAL | `manip/KnownManipulationEvents.java:72-87` | Avatar ×(1−dr) |
| Pre NORMAL | `morphling/EquippedMorphlingEvents.java:149-169` | Winter Shroud / Emberfang adjust |
| Pre NORMAL | `event/ArmorSetBonusHandler.java:206-265` | Silent Archon death refusal, Venous Strider ×, Chitinite/lineage projectile ×, Barbed thorns, Prismatic flash |
| Pre NORMAL | `armor/ability/SheolicBastion…:90-110` | stance sets damage to 0; fire/fall/lava set to 0 |
| Pre NORMAL | `armor/ability/SilentArchon…:211-222` | repeats the Incoming negation |
| Pre NORMAL | `rite/unstained/StillWatersEvents.java:23-32` | MAGIC damage ×0.70 in zone |
| Pre NORMAL | `memory/HematicMemoryToolEvents.java:80-91` | husk effigy damage to owner set to 0 |
| Pre NORMAL | `scar/ScarsEntityEventHandler:103`, `armor/ability/PhanstmalBloodlust…:58`, `summon/MnemonistPuppetEvents:18` | hooks / recorders |
| Pre LOWEST | `musclememory/MuscleMemoryEvents.java:44-74` | Sanguine Fists +flat |
| Pre LOWEST | `manipulation/ManipulationReactiveEvents.java:220-262` | Endless Hour deferral, Penumbral ×, Phoenix Debt death-save |
| Post | 16 handlers (lifesteal, school commit, on-hit hooks) | no damage change |

That is 35 `setAmount`/`setNewDamage` sites in 22 files, and 12 independent damage-reduction multipliers that stack multiplicatively with no shared cap. `TriadAttributeCaps` does this budgeting for toughness only (`ArmorSetBonusHandler.java:426-429`).

### 2.2 Near-duplicates vs. legitimate differences
- **Legitimately different:** absorption pools (Sanguine Ward, Iron Hearts, Endless Hour debt) each need a specific stage. Keep them separate.
- **Duplicate:** Silent Archon negation is written twice, for Incoming and for Pre (`SilentArchon…:241-300`). The Pre copy only matters if Incoming was not cancelled, and the predicates are identical.
- **Order-dependent consumers (HYPOTHESIS: real if the registration order is unfavourable):** `BloodVolumeEvents` and `VascularSystemEvents` turn `getNewDamage()` into blood loss and vein strain at NORMAL. Five other NORMAL handlers change that same value: Avatar, Venous Strider/Chitinite, Still Waters, Sheolic stance (to 0) and husk effigy (to 0). So the blood a player loses for one hit depends on class-scan order. Example: a Sheolic-stance player could lose blood and vein health for a blow that was set to 0.
- **Death-check order (HYPOTHESIS):** Silent Archon refusal checks `getNewDamage() >= health` at NORMAL (`ArmorSetBonusHandler.java:311-313`). Endless Hour deferral and Phoenix Debt run later at LOWEST. A blow that Endless Hour would have deferred, or that a later NORMAL reduction would have made non-lethal, can still spend the refusal blood cost and the shared Last-Rite cooldown.

### 2.3 Recommendation
Don't merge the modifiers. **Pin the order.**
1. Move pure consumers (the drain part of `BloodVolumeEvents.onPlayerDamaged`, and `VascularSystemEvents.onPlayerDamaged`) to `LivingDamageEvent.Post`, or to Pre LOWEST. Iron Will is a modifier, so it must stay where it is, split from the drain. *Behaviour risk:* blood drained per hit drops for players who carry reductions. That is arguably the intended behaviour, but call it out as a balance change.
2. Move every **lethal-check death save** to one stage (Pre LOWEST, after Endless Hour), ordered through `LastRiteHelper`. Silent Archon refusal moves out of `ArmorSetBonusHandler.onPlayerHurt` NORMAL.
3. Delete the duplicated Pre negation in Silent Archon (Low).

Difficulty: Medium. Worthwhile: **yes**. This is the highest-value item in this report, because it is invisible balance drift.

---

## 3. Healing (VERIFIED)
- 29 `heal(` calls: items 7 (Living Blade/Axe `heal(dmg/2)` at `LivingBladeItem.java:68` and `LivingAxeItem.java:80`; Deadman's Purse; Gravecap; Scar tier heal `ScarDefinition.java:261`), manipulations 6, entities/bosses 6, capability 3, effects 3, armor 1 (Bloodlust lifesteal `ArmorSetBonusHandler.java:178-199`, the only one that banks overheal into `BorrowedBloodReserve`).
- 4 heal-*modifier* listeners in 3 places, all NORMAL:
  - Necrosis ×0.75 (`ManipulationStatusEvents.java:29-33`). Skipped if Insatiable or Hemophagy is present.
  - Insatiable Hunger (`:34-38`).
  - **Hemophagy, registered inside a boss class** (`entity/boss/saint/hemorath/HemorathEntity.java:414-418`). Skipped if Insatiable is present.
  - Impaired Recovery (`item/harbinger/BloodInjectionEvents.java:40-43`). Stacks with all of the above.
  - The mutual exclusions are written in two files. `PurityGainEvents.java:291/314` reads `getAmount() >= 2` at the same NORMAL priority, so whether a reduced heal earns purity depends on scan order (HYPOTHESIS).

**Recommendation:** move all heal-modifiers into one `LivingHealEvent` HIGH handler. `ManipulationStatusEvents.onLivingHeal` is the natural host: one table of (effect → multiplier, excludes). Move Purity to LOW. Keep the `heal()` call sites raw; a wrapper would add nothing. Low difficulty, near-zero risk. Worthwhile.

---

## 4. Blood expenditure and recovery

### 4.1 Spend paths (VERIFIED)
`BloodVolume.drain` sets volume to 0 and returns false when the cost exceeds the reserve (`BloodVolume.java:37-43`). Callers that drain without checking first therefore wipe the whole reserve.

| Path | Ledger record? | Hemorath `addBloodSpend`? | Borrowed-blood cover? | Sanguine-Reserve? |
|---|---|---|---|---|
| Manipulation cast `BloodManipulation.java:534-545` | no (own cost ledger) | yes | yes | n/a |
| Toggleable skills, Thurible, Muscle Memory, Covenant Mantle, morphling upkeep/starvation, organs, Blood Loss/Hemolysis, Blood Moon | yes (`applyDrain`) | some | Muscle Memory only | yes |
| Armor abilities `ArmorSetAbilityRegistry.java:137-147`, Silent Slipping `SilentArchon…:89-91`, Silent Archon refusal `ArmorSetBonusHandler.java:340` | **no** | **no** | no | **no**, though Category.ARMOR exists |
| Morphling primal `MorphlingItem.java:408-414`, Ink Mantle `LumenlaceMorphlingItem.java:143-146`, Deadman's transfusion `:94` | **no** | no | no | **no** (MORPHLING) |
| Cerebral scar upkeep `ScarDefinition.java:162-177` (every tick, 3 scars) | **no**, and **no sync** | no | no | **no** (SCAR); fungal scars *do* use the ledger |
| Living weapons Blade/Axe/Crossbow/Tool (`LivingBladeItem.java:69-80` etc., 4 copies) | no | no | no | no (TOOL) |
| Living Torch `LivingTorchItem.java:110-111`, staff form `LivingStaffWeaponFormHelper.java:222-223` | no | yes | no | no |
| Puppet thread upkeep `BoundSummonBehavior.java:520-526` | no | no | no | no |
| Rites (`HarbingerCardinalRiteEvents:348,1498`, `CardinalRiteOrdealEngine:598`, `ScarBrazierRite:283`) | no | Brazier only | no | (not a protected category, consistent) |

About 60 raw `drain`/`subtractBloodVolume` calls on player volumes sit outside the ledger.

### 4.2 Parallel resources
- `capability/player/unstained/white_humor/WhiteHumorVolume` is a field-for-field copy of `BloodVolume`; it even reuses `Bloodline`. Its `fillFromSource` still has the bug that `BloodVolumeTransferRules` fixed for blood (see Defects). There are no callers today.
- `BorrowedBloodReserve`, puppet thread, morphling feed bank and Emberfang heat are each legitimately separate.

### 4.3 Recommendation
Add one entry point next to `BloodFlowLedger.applyDrain`, for example `BloodFlowLedger.spend(ServerPlayer, volume, sourceId, label, Category, cost, SpendPolicy)`. It would wrap the "check ≥ cost → drain → addBloodSpend → Hemorath → sync" sequence that `BloodManipulation`, `TeleportToVeinPacket:101-102` and `EarthenVeinTravelManager:143-144` each write by hand. Then migrate consumers in this order:
1. Armor abilities (one site in `ArmorSetAbilityRegistry`).
2. Morphling primal (`MorphlingItem.tryBeginPrimalAbility`).
3. Scar upkeep.
4. The 4 living-weapon copies, via one `LivingToolItem` helper.

**Behaviour risk:** routing through `applyDrain` turns on Sanguine-Reserve protection and Hemorath debt for these families. That is a balance change, so add a `SpendPolicy` flag to preserve today's numbers until design signs off. Medium difficulty. Worthwhile: yes for ledger visibility and sync. Holding the reserve/debt policy constant is mandatory.

**Keep separate:** White Humor should stay its own resource for lore and gameplay. If it is ever wired up, give it the same transfer-rules fix rather than a merge.

---

## 5. Attribute modifiers (VERIFIED)
- There are 13 `addTransientModifier`, 2 `addPermanentModifier`, 18 `removeModifier` and 28 effect-level `addAttributeModifier` calls (23 of them in `EffectInit`, where vanilla handles add and remove).
- The "sync modifier to desired amount" helper is **hand-written 9 times**:

| File:line | Variant |
|---|---|
| `musclememory/MuscleMemoryWorldEvents.java:247` | compares amount (ε) and operation |
| `entity/summon/BoundSummonBehavior.java:542` | `!=` amount, add only if >0 |
| `event/BodyRefinementSkillEvents.java:73` | `!=` amount, add if ≠0 |
| `morphlings/EmberfangMorphlingItem.java:160` | ε amount, add if >0 |
| `manipulation/animus/AvatarManifestationManager.java:176` | always remove + re-add |
| `event/ArmorSetBonusHandler.java:410/433/451` | three inline copies (Chitinite, Pelagic, Marrow Crown) |
| `brewing/AdvancedBrewEffects.java:109` | presence only |
| `scar/ScarsEntityEventHandler.java:138-155` | **permanent**, remove + re-add every 20 ticks |

- Modifier ids: all unique as far as I can tell. Scar ids come from per-scar names (`ScarInit.java:41-85`), synergy ids are per-tendency, and Veinwing ids are per-entity UUID. No collisions found.
- Cleanup is symmetric for equip/unequip (`ScarDefinition.onUnequipped:107-125`, `MorphlingItem.clearMorphlingPassiveEffects:534`, `AvatarManifestationManager.dismiss`). Transient modifiers recompute after login and respawn through equipment-change and tick handlers.

**Recommendation:** add `AttributeModifierSync.ensure(AttributeInstance, id, amount, op)` (remove when 0, otherwise replace if different) and swap the 9 copies over. Low difficulty, low risk. The one subtle case is Avatar's unconditional re-add, which costs an extra dirty-sync but is otherwise equivalent. Worthwhile: modest. It also gives a single place to apply `TriadAttributeCaps` later.

---

## 6. Status-effect application
- There are 419 `addEffect(new MobEffectInstance…` sites: items 85, bosses 54, mobs 40, `event/` 30, `init/` 28, capability 18, Unstained rites 17, manipulations ≈50, armor 4.
- School states go through `SchoolStates.apply` (39 callers: manipulations plus 4 item sites). Morphlings use `MorphlingCombat.afflict`.
- Effect *modifiers* are spread around:
  - `BodyRefinementSkillEvents.onEffectAdded` rewrites durations.
  - `BloodInjectionEvents.effect` and `Paralysis.onApplicable` handle immunity.
  - `ArmorSetBonusHandler.java:149-157` (Unstained set strips blood_loss/hemolysis every N ticks), Phoenix Debt (`ManipulationReactiveEvents.java:243-245`), `LetheanChaliceItem:54` and Winter Shroud `cleanse` are four separate cleanse implementations with different filters.
- "Effect while equipped" markers: morphlings re-assert theirs every tick (`MorphlingItem.java:513-532`). Scars apply infinite `-1` effects **once, on bind** (`ScarDefinition.java:102-104`). So milk permanently strips a scar's Night Vision until the next rebind (see Defects).

**Recommendation:** keep effect application raw; one wrapper would hide too many design choices. Two narrow helpers would pay off:
- A hostile-effect gate: when the source is a player, always run `SchoolStates.canApply` or `ManipulationCombatHelper.canHarm` before `addEffect` (see §7).
- A per-tick "assert passive effect" helper, generalised from `MorphlingItem.applyPassiveEffect`, reused by scars.

Low–Medium difficulty. Worthwhile for scars.

---

## 7. AoE targeting and target validation
There are 356 `getEntitiesOfClass`/`getEntities(` calls. The independent "is this a valid target" implementations are:

| # | Implementation | Ally/owner | PvP rule | Creative | Used by |
|---|---|---|---|---|---|
| 1 | `ManipulationCombatHelper.canHarm/allied` `:21-39` | team, Ownable, BoundPuppeteer, impressed | server pvp + `canHarmPlayer` | yes | manipulations, SchoolCombatEvents, SchoolStates, spear |
| 2 | `ConductionManager.canHarm` `ductilis/ConductionManager.java:47-54` | delegates to #1 for players; excludes BloodConstruct | via #1 | yes | conduction |
| 3 | `EntityManipulationEffects.canAffect` `:664-668` | team only | **none** | **no** | entity-cast manipulations |
| 4 | `BoundSummonBehavior.canAttack` `:579-600` | owner, bound summons, circus | n/a | trial only | puppets |
| 5 | `MarionetteCrossbarItem.isFriendlyBoundSummon` `:571` | bound summons | — | — | crossbar |
| 6 | `LivingSickleItem.java:112-114`, `LivingTorchItem.java:136-139` | team | vanilla `canAttack` only | partial | living weapons |
| 7 | Morphlings, Still Arts, scar trail, Muscle Memory, Prismatic flash: `Monster.class` filter | **none** | n/a | n/a | ~25 sites |
| 8 | `SilentArchon…:132-134`, `StillArtInit.java:52-53,152-153` | **none** | **none** | partial | armor, Still Arts |
| 9 | `PallidShadowRules.canTarget`, `HuskEffigyRules.shouldRejectTarget`, `LivingFlailImpactRules.isValidTarget`, `BloodAbsorptionItem.isValidAbsorptionTarget` | domain rules | varies | | rites, memories, tools |
| 10 | `MorphlingItem.findLookTarget` `:436-447` | **none** | **none** | no | Bootlace, Emberfang |

Related near-duplicate classifiers:
- **Boss checks:** `ManipulationReactiveEvents.isBoss` (`:511-518`, 17 callers; includes Warden, Elder Guardian and the BOSSES tag) vs `ConstrictorCordEntity.isBossTarget` (`:64-68`, no Warden, no Elder Guardian, no tag).
- **"Blood mob" checks:** `EntityInit.HEMOMANCY_MOB` tag (Unstained, 9 uses) vs `ViridSalisTrailHandler.isBloodMob` (`:119-131`, a hard-coded list of 6 classes) vs `HemoEntityPredicates.NOBLOOD`.
- **"Blood damage" checks:** Silver Ward means "the attacker is HEMOMANCY_MOB" (`UnstainedMilestoneHandler.java:157-160`), while Still Waters and Lethe mean `DamageTypes.MAGIC` (`LetheCovenantEvents.java:62-66` comment: "magic (bleed) damage").

Because `GroundPuppetEntity extends Zombie` (`entity/summon/GroundPuppetEntity.java:17`), every family-7 `Monster.class` filter also selects the caster's own bound puppets. Morphling *damage* is still blocked for those puppets: the code runs in `MorphlingCombat.scope`, so `SchoolCombatEvents.prepare` cancels it through `allied`. Morphling `push`, raw `addEffect` and pest targeting are not blocked (`UrchinMorphlingItem.java:72-80`, `PestsMorphlingItem.java:169-176`). Scar, Still Art and armor paths have no school scope, so their damage is not blocked either.

**Recommendation:** extend `ManipulationCombatHelper` rather than writing a new helper:
- add `canHarm(LivingEntity owner, LivingEntity target)` by lifting #2's mob-owner branch;
- add `hostileMonsters(Player, AABB)`: `Monster` plus `canHarm`.

Then migrate #3, #6, #7, #8 and #10. Move `isBoss` to a shared place (`damage/` or `HemoEntityPredicates`) and make ConstrictorCord use it.

**Behaviour risk:**
- #7 stops hitting your own puppets. That is a fix.
- #8 gains PvP protection, which changes PvP-server balance; keep it behind the server pvp flag that #1 already reads.
- #3 gains PvP and creative checks, but only matters when the caster is a player.

Medium difficulty because of the number of sites, mechanical edits. **Worthwhile: yes.**

**Keep separate:** #4 (puppet AI focus/circus rules), #9 (domain rules) and the HEMOMANCY_MOB-vs-MAGIC split. Those are gameplay identities; document them and don't merge.

---

## 8. Movement and mobility
- There are 243 `setDeltaMovement`, 53 `push`, 14 `knockback` and 58 `hurtMarked=true` sites, mostly in bosses (105).
- Of the player-facing families, 16 files push other entities and never set `hurtMarked`: StillArtInit, BloodCloud, Vitric Combustion, Bootlace/Chitinite/Irontooth/Urchin morphlings, LivingFlail, UnstainedWarhammer and others. This matters only when the pushed entity is a player (HYPOTHESIS: the push won't sync to the client).
- Only `LivingAxeLodestoneFields.java:155` and `LivingSickleCombatRules.pullStrength` honour knockback resistance; other `setDeltaMovement` pushes ignore it.
- **Flight** is granted by four uncoordinated writers of `abilities.mayfly`:
  - `EdaciousBloodburst…:45-65`
  - Silent Slipping (`SilentArchon…:434-461`)
  - `effect/ElytraEffect.java:20-35`
  - HarbingerEquipment elytra modifier (`HarbingerEquipmentEntityEventHandler.java:43-66`)

  Any of them turning flight off drops the player even if another source is still active (HYPOTHESIS for the actual combinations).

**Recommendation:** add a tiny `ForcedMotion.push(target, vec, boolean respectKbResistance)` that sets `hurtMarked` for players. Keep the current "ignore resistance" behaviour by default. For flight, add a source-set arbiter on the player attachment (`FlightGrants.grant(player, id)` / `revoke`) so `mayfly` is false only when no source remains. Low (push) / Medium (flight). Worthwhile: flight yes, push only for player-targeting families.

---

## 9. Conditional passives (per-tick "if wearing / has / equipped")
- There are 58 `PlayerTickEvent` handlers. The combat-passive ones: 4 armor (`ArmorSetBonusHandler:128`, `Edacious:68`, `Sheolic:66`, `SilentArchon:186`), morphling, scars, muscle memory ×2, body refinement, toggleable skills, organs, vascular, blood volume, Avatar, BodyIdiom, Axonal, Channel, ManipulationReactive, BlackVeil, Thurible, Silthmere Glaive, staff form, brew, Unstained milestone, purity, candle aura.
- **Full-set detection is written three ways:**
  - material count `ArmorSetBonusHandler.java:85-100` (12 uses);
  - item identity `ArmorSetHelper` (11 methods, 23 uses);
  - slot map `SimpleArmorSetAbility.isAvailable:76-83`.
- `hasFullSilentArchonSet` exists twice (`ArmorSetHelper.java:82`, `SilentArchonArmorAbilityHandler.java:333`). A third Silent Archon test is `hasFullSet(SILENT_ARCHON)`, which is material-based and used by the death refusal at `:322`.

**Recommendation:** make `ArmorSetAbilityRegistry` / `SimpleArmorSetAbility` (the piece map) the only definition of a set and have `ArmorSetHelper` delegate to it. Delete the duplicate in SilentArchon. If the material-based count is intentional (variant pieces), keep it but name it `hasFullMaterialSet`. Low difficulty. Behaviour risk: only if some item shares a material without being in the piece map (HYPOTHESIS; I didn't enumerate armor materials). Worthwhile: yes, cheap.

**Keep separate:** one tick handler per family is fine. A central "passive dispatcher" would couple unrelated systems for no gain.

---

## 10. Resource upkeep
Periodic drains are split between `BloodFlowLedger.applyDrain` (morphling upkeep `EquippedMorphlingEvents.java:119`, organs, Thurible, toggleable skills, Covenant Mantle, Blood Moon, effects) and raw per-tick drains:
- scar upkeep `ScarDefinition.java:167-175`, every tick with no sync;
- Living Torch;
- puppet thread;
- rite caster boundary `HarbingerCardinalRiteEvents.java:348`;
- ordeal `CardinalRiteOrdealEngine.java:598`.

Avatar upkeep is a 20-tick pulse inside `AvatarManifestationManager.java:108-114`.

**Recommendation:** the same spend entry point as §4, used with an interval (it already takes `intervalTicks`). Scars first, because the SCAR category already exists and the fungal scars use it. Low–Medium. Worthwhile.

---

## 11. Expiry and cleanup of temporary state
- There are about 40 static `Map<UUID,…>`/`Set<UUID>` runtime stores in the combat families. Cleanup is written per family:
  - Logout handlers exist in 17 files.
  - `TransientServerStateEvents` resets 19 stores on server start/stop.
  - `ManipulationReactiveEvents`, `AvatarManifestationManager`, `HematicRiposteEvents`, `LivingAxeLodestoneFields` and `MortemStatusVisuals` each do their own stop/logout handling and are **not** in the hub.
  - Stores with no per-player logout clear (they grow until server stop): `HematicCommandManager` (5 maps, `:35-42`), `TemporaryIceManager`, `LignumMortisManip.SESSIONS`, `SporiticThuribleEvents.LAST_YAW`, `ConserveStateHelper`/`RootedStateHelper`, `BodyIdiomEvents.RUPTURING` (respawn only). `MuscleMemoryWorldEvents`' 4 maps are cleared indirectly through `MuscleMemoryEvents.onLogout:138` (verified).
- Timed states are stored four ways: game-time longs in `getPersistentData()` (armor, brew, Endless Hour), attachments (`PowerGuardrailState`), static maps with `until` fields, and MobEffects.

**Recommendation:** extend `TransientServerStateEvents` with a `PlayerLoggedOutEvent`/`Clone` fan-out. Each manager registers `clearPlayer(UUID)` once, and the ad-hoc logout handlers fold into it. Low difficulty, no behaviour change. Worthwhile for leak hygiene. Don't unify the timed-state storage itself.

---

## 12. Prioritised summary

| # | Mechanism | Extend | Consumers | Diff | Risk | Worth |
|---|---|---|---|---|---|---|
| A | Pin the damage-pipeline phase (consumers after modifiers; one death-save stage) | existing priorities + `LastRiteHelper` | BloodVolume, Vascular, Silent Archon refusal, Phoenix, Ink Mantle | Med | balance (blood per hit) | **High** |
| B | Unified target gate | `ManipulationCombatHelper` | morphling/StillArt/armor/scar/sickle/torch filters, `EntityManipulationEffects.canAffect`, `findLookTarget`, `isBoss` | Med | PvP behaviour (fix) | **High** |
| C | Spend entry point | `BloodFlowLedger` | armor abilities, primal morphlings, scar upkeep, living weapons, thread upkeep | Med | reserve/debt policy (keep it constant) | High |
| D | Heal-modifier table | `ManipulationStatusEvents.onLivingHeal` | Hemorath, BloodInjection, Purity order | Low | none | Med |
| E | `AttributeModifierSync` | new 15-line util | 9 copies | Low | none | Med |
| F | Set definition | `ArmorSetAbilityRegistry` | ArmorSetHelper, ArmorSetBonusHandler | Low | material vs item | Med |
| G | Player cleanup fan-out | `TransientServerStateEvents` | ~12 managers | Low | none | Med |
| H | Flight arbiter / ForcedMotion | new small util | Edacious, Slipping, Elytra, glide | Med | low | Med |
| — | Absorption pools, puppet targeting, domain target rules, White Humor resource, per-family tick handlers | **keep separate** | | | | |

---

## 13. Observed defects (report only; nothing fixed)

**VERIFIED**
1. `armor/ability/SilentArchonArmorAbilityHandler.java:132-137`: Silent Severance damages every living entity within the radius (pets, villagers, allies; players unless creative/spectator) with an unattributed `damageSources().magic()`. No PvP gate and no kill credit.
2. `init/StillArtInit.java:152-157`: a Still Art does 4 unattributed magic damage to *all* living entities within 5 blocks. `:52-57` pushes and slows every living entity in front of the player, with no ally/PvP check and no `hurtMarked`.
3. `armor/ability/SheolicBastionBloodlustArmorAbilityHandler.java:154`, `capability/player/harbinger/scar/ScarsEntityEventHandler.java:94`: retaliation and trail damage use unattributed `magic()`. Kills are not credited to the player, so morphling `onEquippedKill`, scar `onPlayerKill` and player-kill loot are skipped. That differs from manipulations, which are attributed.
4. `item/harbinger/scar/ScarDefinition.java:167-175`: scar blood upkeep runs every tick through raw `drain`. It never syncs to the client, never records to `BloodFlowLedger`, and when the reserve is short it zeroes the whole reserve (`BloodVolume.drain` semantics) without deactivating the scar.
5. `item/harbinger/scar/ScarDefinition.java:102-104`: passive scar effects are infinite instances applied only on bind/equip. A milk bucket removes them until the next rebind (relog or re-equip). Morphlings avoid this by re-asserting every tick.
6. Living Blade/Axe/Crossbow/Tool (`LivingBladeItem.java:69-80`, `LivingAxeItem.java:81-95`, `LivingCrossbowItem.java:134-137`, `LivingToolItem.java:114-117`): both branches call `drain(damageMod)`. In the insufficient branch that wipes the whole reserve before the weapon reverts or breaks. None of them check `volume.isActive()`.
7. `armor/ability/EdaciousBloodburstArmorAbilityHandler.java:52-54`: `onUpdateAbilities()` (an abilities packet) is sent **every tick** while the set is worn.
8. `capability/player/harbinger/scar/ScarsEntityEventHandler.java:146-153`: scar synergy removes and re-adds a *permanent* modifier every 20 ticks while active, which marks the attribute dirty and resyncs it each second.
9. `capability/player/unstained/white_humor/WhiteHumorVolume.java:61-64`: `fillFromSource` drains the source, then returns false without filling if the source's remainder is ≤ points, so the humor is destroyed. This is the bug `BloodVolumeTransferRules` fixed for blood. It has no callers yet.
10. `capability/player/harbinger/manip/KnownManipulationEvents.java:78-82`: Avatar hit-particles measure `e.getEntity().distanceToSqr(player)` where both are the same player, so `pick(0)` always spawns at the player's eye, not the hit point (cosmetic).
11. `entity/projectile/ConstrictorCordEntity.java:64-68` vs `manipulation/ManipulationReactiveEvents.java:511-518`: the two boss definitions diverge. Warden, Elder Guardian and BOSSES-tag entities are bosses for manipulations but not for Constrictor Cord.
12. `capability/player/harbinger/equipment/HarbingerEquipmentEntityEventHandler.java:70-90`: the Curved Horn death-save cancels `LivingDeathEvent` without going through `LastRiteHelper`, so it bypasses the shared Last-Rite cooldown that the other five death-saves respect.
13. `event/ArmorSetBonusHandler.java:217-227`, `capability/player/harbinger/manip/KnownManipulationEvents.java:84`, `bloodvolume/BloodVolumeEvents.java:285-287`, `rite/unstained/StillWatersEvents.java:30`, `event/UnstainedMilestoneHandler.java:163`: damage-reduction multipliers stack multiplicatively with no shared cap (Avatar × Iron Will × Chitinite × Venous Strider × Still Waters × Silver Ward × vanilla Resistance). This is a balance observation, not a crash.
14. `SilentArchonArmorAbilityHandler.java:211-222`: duplicate Pre negation of damage already cancelled at Incoming. Redundant.

**HYPOTHESIS (depends on listener order or data not enumerated)**
- H1. Blood drain and vein strain on hit (`BloodVolumeEvents:275`, `VascularSystemEvents:37`) can be computed before Sheolic stance, husk-effigy zeroing, Avatar, Chitinite or Still Waters reductions at the same NORMAL priority, so players lose blood for mitigated or zeroed hits.
- H2. Silent Archon death refusal (NORMAL) can fire on blows that Endless Hour (LOWEST) would defer, or that later NORMAL reductions make non-lethal, spending blood and the shared Last-Rite cooldown.
- H3. Morphling `push`/raw `addEffect` and pest targeting hit the caster's own bound puppets (`GroundPuppetEntity extends Zombie` → `Monster`).
- H4. Players pushed by the 16 non-`hurtMarked` sites see no client-side knockback.
- H5. Flight sources overwrite each other's `mayfly` (for example, Elytra effect expiry while the Edacious set is worn drops `flying` for one tick).
- H6. `BloodConstructEntity` / `FerricConstructEntity` are neither `OwnableEntity` nor handled in `ManipulationCombatHelper.allied`, so player AoE manipulations may damage the caster's own iron walls/pillars (only `ConductionManager` excludes them).
- H7. `PurityGainEvents.onLivingHeal` (`:314`) reads heal amount before or after the Necrosis/Hemophagy/Impaired reductions depending on order.
- H8. `ViridSalisTrailHandler.isBloodMob` (6 hard-coded classes) differs from the `hemomancy_mob` entity tag that Unstained uses elsewhere. I didn't diff the tag contents.
