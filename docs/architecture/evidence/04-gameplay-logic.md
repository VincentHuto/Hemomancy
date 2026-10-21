# Hemomancy audit 04: gameplay-logic subsystem architecture

Scope: manipulations, blood accounting, rites and multiblocks, tiles, recipes, stations, smaller gameplay packages, and duplicated utilities. This was a read-only pass. No repository file was changed. Paths are relative to `src/main/java/com/vincenthuto/hemomancy/` unless they start with `src/`, `docs/` or `HutosLib/`. Labels: **VERIFIED** means I read the code or grepped it exhaustively. **LIKELY** means I read the code path but did not run it. **HYPOTHESIS** means the claim is inferred.

---

## 1. Subsystem map (VERIFIED counts)

| Package | Files | Lines | Notes |
|---|---|---|---|
| `common/rite/` | 142 | 17,453 | Harbinger Cardinal Rites, the Unstained legacy path, sigils, floors, zones, brazier rites |
| `common/manipulation/` | 179 | 16,494 | 91 classes `extends BloodManipulation`; no per-tendency base class exists |
| `common/tile/` | 92 | 16,325 | 47 extend `BlockEntity` directly, 12 extend `BaseContainerBlockEntity`; no project base class |
| `common/recipe/` | 39 | 5,535 | 18 serializers; 7 recipes extend vanilla `CustomRecipe` |
| `common/mission/` | 44 | 3,502 | Ad-hoc static assignment classes; no shared interface |
| `common/circus/`, `succession/`, `damage/`, `summon/`, `enchanting/`, `brewing/` | 21/18/10/13/13/12 | 1,316/1,170/1,182/937/787/636 | |
| `common/vein/` | 7 | 698 | Earthen Vein *travel*, not blood tanks. Tank logic is `tile/IBloodReservoir` plus the `BLOCK_BLOOD_VOLUME` attachment |
| `common/station/` | 10 | 507 | Shared upgrade catalog, progress and rules |

Registry facts: `init/ManipulationInit.java` (1,296 lines) registers **123** manipulations. 121 use an inline positional constructor followed by `.setCooldownTicks` (108), `.setSecondaryTend` (90) and `.setDrudgeAction` (119). Two of them (`SanguineMarionetteManip`, `AxonalTransductionManip`) carry their metadata in their own constructors. Drudge actions: 71 are `DRUDGE_UNSUPPORTED` and **48 are inline lambdas** in the init file. 29 of those lambdas repeat the same "nearest `Monster` in AABB" lookup. A script check confirmed that all 121 registry IDs equal their constructor name strings.

---

## 2. Competing patterns

### 2.1 Manipulation cost and cooldown authority: centralized (VERIFIED)
`manipulation/BloodManipulation.java:446-582` (`tryPerformAction`) is the single gate. In order it checks paralysis, known form, retirement, cooldown, dead vascular section, purity multiplier, pome permission, cost ledger (`ManipulationCostLedger.collect`), charge, alignment, borrowed-blood cover, `drain` and `addBloodSpend` (539-541), then sync, the action, sound and particles, use credit and cooldown (`startCooldown` at 314).
- **No manipulation charges its own cost outside the base.** A grep for `drain/fill/addBloodVolume` across `manipulation/` finds only effect payloads: `saint/CrimsonTitheManip.java:102` (a tithe debt), `mortem/ExsanguinateManip.java:108` (a heal-back) and retired `mortem/VitalReservoirManip.java:70`. It also finds `SchoolHitHelper.java:165` (the Grave Debt death refund). Reactive and passive triggers (`ManipulationReactiveEvents.java:470`) go through `tryPerformPassiveTrigger`.
- Cooldown bypasses: only `stillarts/StillArt.java:102` has its own (already covered by the earlier cooldown audit).
- **Duplicate pre-gates (VERIFIED):** `network/capa/harbinger/manips/UseManipKeyPacket.java:70,88,115-135,215-219` re-checks paralysis (`isParalyzed`, while the base uses `blocksActions`), retirement and `isActive` before calling `performAction`.
  - **Recommendation:** keep the base as the authority. The packet should only resolve input (charge clock, channel start/stop) and delegate gating.
  - Difficulty: Low. Gameplay risk: Low, though messages may differ.
- **Recommendation for metadata:** keep `ManipulationInit` as the single source. Do not move cost/rank/tendency into the classes; the two outliers should follow the init style.
  - Move the 48 drudge lambdas into a `DrudgeActions` helper with a shared `nearestHostile(drudge, radius)` lookup. Expected reduction is about 150 lines in the init file.
  - Difficulty: Low. Gameplay risk: none, if the lambdas move verbatim.

### 2.2 Per-caster parallel implementations keyed by manipulation-name strings (VERIFIED)
A manipulation can have up to three implementations, plus several side tables keyed by name strings:
1. Player: `getAction` in the class (91 overrides).
2. Entity: `manipulation/EntityManipulationEffects.java:66-106`, a `switch (manipulation.getName())` over 34 names. It is used by `WillManipulationCaster` and directly by `entity/boss/endgame/VesperPhaseTwoCombat.java:1091`.
3. Drudge: the inline lambdas in `ManipulationInit`.

Name-keyed side tables in common code are `ManipulationCastSounds.profileFor`, `ManipulationVisuals`, `family/ManipulationFamilyRegistry`, `animation/CastPresentation`, `ManipulationTreeInit`, `capability/.../ManipulationRetirementRules` and `VesperManipulationCoverage`. That makes 8 places that must agree when an ID changes.
- **Recommendation:** add an optional `EntityCastBehavior` hook on `BloodManipulation`, set at registration like `setDrudgeAction`. `EntityManipulationEffects.cast` would then dispatch to it instead of a string switch. Move cast-sound profiles into registration metadata.
  - Keep player and entity bodies **distinct**: entity casts deliberately skip blood, cooldown and progression, and Vesper uses retired forms.
  - Expected result: one place per manipulation for metadata, and about 100 lines of switch/profile code removed.
  - Difficulty: Med. Gameplay risk: Low if the bodies move unchanged.

### 2.3 Duplicated blocks inside manipulation classes (VERIFIED counts across the 91 subclasses)
| Idiom | Classes |
|---|---|
| `instanceof ServerLevel` guard | 66 |
| `playSound(null, …)` (89 calls) on top of the base `ManipulationCastSounds.play` | 60 |
| Shared visuals (`ManipulationVisuals/Particles/TendrilEffects/CastImpacts`) | 60 |
| `getEntitiesOfClass` target gather | 34 (37 use `ManipulationCombatHelper.hostileTargets/aimedTarget/canHarm`) |
| Gather **without** `canHarm` in the file | 9 (`BloodShotManip`, `AbsoluteStillnessManip`, `LivingCircuitManip`, `IronChoirManip`, `CauterizingRebukeManip`, `LumenSutureManip`, `RadiantWakeManip`, `UnclosingEyeManip`, `BlackVeilCovenantManip`). HYPOTHESIS: several are ally or self effects on purpose. |
| Raytrace (`pick`, `ClipContext`, `ProjectileUtil`) | 13 |
| Eye plus look-vector math | 18 |
| `addFreshEntity` projectile spawn | 7 |
| Vanilla `ParticleTypes` | 0 |

Classes average about 81 lines, and the shared helpers are already in place. **Recommendation:** route the remaining raw target gathers through `ManipulationCombatHelper` where a review confirms hostile intent. Do not force a "projectile manipulation" base class: only 7 classes spawn entities, and their behaviors differ.
- Difficulty: Low. Gameplay risk: Med (a targeting filter change alters who gets hit), so do this per class with tests.

### 2.4 Blood accounting: three mutation idioms (VERIFIED)
`IBloodVolume` (`capability/player/harbinger/bloodvolume/IBloodVolume.java`) exposes:
- `drain`/`fill`, which clamp and return false at the limit. **`drain` sets the volume to 0 when it overstrains** (`BloodVolume.java:37-45`).
- `addBloodVolume`/`subtractBloodVolume`, which are unclamped.
- `setBloodVolume`, `drainFromSource`/`fillFromSource`, and `addBloodSpend`/`addDamage`, which feed `bloodDebt` and are consumed only by `entity/boss/goal/HematicCollapseGoal.java:126`.

Call sites in `common/` (VERIFIED greps):
- `drain(` on a blood volume: about 98 hits in 81 files, across items 27, tiles 24, capability 13, entities 12, network 10, rites 8, blocks 8, manipulations 4.
- `subtractBloodVolume`: 15. `setBloodVolume`: 48 (mostly load/sync). `addBloodVolume`: 11.
- `BloodFlowLedger.apply*`/`transferFromVessel`: 25 calls in 19 files. `CirculationIncomeHelper.grant`: 2 (it is a documented compatibility adapter over the ledger).
- `addBloodSpend` is called at only **15** spend sites (`BloodManipulation:540`, `ScarBrazierRite:284`, `BrazierBlock:335`, `SanguineCondenserProjection:56`, `EarthenVeinTravelManager:143`, `MasonsEffigyBlockEntity:184` and others). Most other paid costs (rites, station operations, living tools, morphlings, menus) do not count toward debt.
- Player sync uses two forms: `new BloodVolumeServerPacket` directly (49 calls in 41 files, including `BloodManipulation:545`) and `BloodVolumeEvents.syncVolume` (73 calls), which also pushes flow, max-blood and manipulation diagnostics.

So `BloodFlowLedger` is the authority only for **passive income and upkeep**. Active payments bypass it, and so does some upkeep (see defect 6.2). The ledger's `applyDrain` (`BloodFlowLedger.java:99-140`) is also where the Sanguine Reserve skill protects categories ARMOR, MORPHLING, SCAR, TOOL, ORGAN, BLOODLINE and MUSCLE_MEMORY (line 141).

Paid-cost paths:
- Manipulations: the base class.
- Rites: `CardinalRiteInteractionHandler.spendBlood:448-460` (ichor, then personal, then allies); `HarbingerCardinalRiteEvents:1496` (LEGACY only, which today means Unstained); `PuppeteerTrialRiteController:152`; `ScarBrazierRite:283`; `CardinalRiteOrdealEngine:598`; boundary drain at `HarbingerCardinalRiteEvents:348`.
- Machines: `GhastlyAlembicBlockEntity:451,530,590,596,1017,1031`, `HematicArmatureBlockEntity:179`, `ResonantForgeBlockEntity:187`, `MycelialCrucible:189,277`, `MycelialLantern:118`, `MorphlingIncubator:143`, and the Scriptorium **menu** (`menu/tile/crafting/EnzymaticScriptoriumMenu.java:314,351`).
- Blocks: `CovenantThroneBlock:392`, `SaintSarcophagusBlock:428`, `SemiSentientConstructBlock:213`, `ConsecratedBloodwellBlock:251`, `SanguineVigilBlock:213`, `BloodwoodGrowthHandler:60`, `BrazierBlock:334`.
- Items: living tools (`LivingAxe/Blade/Crossbow/Tool/Grasp/Torch/StaffWeaponFormHelper`), `MorphlingItem:414`, `BloodVialItem:125`, `CrudeMemoryShardItem:141`, `HematicMemoryToolItem:45`.
- Packets: `BloodCraftingKeyPressPacket:166`, `BloodFormationKeyPressPacket:53`, `SynapticLoadoutActionPacket:138`, `TeleportToVeinPacket:100`, `PacketUnlockSkill:143`, `GroundBloodDrawPacket:96,137`.

**Recommendation.** Add one `BloodPayment` facade, for example `BloodAccounting.pay(player, amount, Source{id, category, countsAsSpend, reserveProtected})`. It would do the check, `drain`, optional `addBloodSpend`, ledger record and `syncVolume` in one call. Callers would pass flags matching today's behavior, so nothing changes in gameplay; the diagnostics panel would gain the missing entries.
- Expected result: about 80 three-to-five-line pre-check/drain/sync blocks collapse to one call each.
- Difficulty: Med (many files, mechanical). Gameplay risk: Low with flags; **High** if anyone "fixes" `countsAsSpend` or reserve protection globally, because that changes Hemorath's debt and the reserve skill.

### 2.5 Projected-blood endpoints: an `instanceof` chain (VERIFIED)
`item/harbinger/tool/living/BloodProjectionItem.java:144-217` tries these in order:
1. `BloodStructureFeedManager.feedStructure`
2. `SanguineCondenserProjection.tryProject`
3. `BloodInfusionManager.feedBlock`
4. Filler-to-controller resolve
5. `ResonantForgeBlockEntity.receiveHammerRepairBlood`
6. `SomaticLoomBlockEntity.tryChargeRitualBlood`
7. `MasonsEffigyBlockEntity.receiveProjectedBlood`
8. A generic `tileVolume.fillFromSource`

Each endpoint pays differently. The Loom uses `subtractBloodVolume` (`SomaticLoomBlockEntity.java:580`), the Forge path uses `drain`, and the generic path uses `fillFromSource`. Meanwhile `IBloodReservoir.receiveBlood`/`drainBlood` (`tile/IBloodReservoir.java`), which honor `canReceiveBlood`, have **1** production caller (`SanguineCondenserBlockEntity:152`).
- **Recommendation:** add a `ProjectedBloodSink` interface (`acceptProjected(player, max, livingStaff) → double`) that the BEs implement. The item then dispatches once, with a default implementation over `IBloodReservoir.receiveBlood`. This also closes defect 6.1.
- Difficulty: Med. Gameplay risk: Low.

### 2.6 Multiblock / structure matchers: five origin solvers (VERIFIED)
| Mechanism | Matcher | Notes |
|---|---|---|
| Blood Structures (10 floorless JSONs remain) | `BloodCraftingKeyPressPacket.java:692-741` (private exact origin solver `patternOriginForHit`) **and** `BloodStructureCraftingHelper.java:105-126` (brute-force cube scan × 24 orientations) | Both are live. The packet uses its own copy; `BloodStructureFeedManager` and `WorkingCraftRites:268` use the helper's |
| Cardinal Rite floors and upper structure | `rite/CardinalRiteStationMatcher.java:147-173,232-247` (`originForCell`, `offset`) | The cleanest of the five |
| Working-craft subject | `rite/harbinger/WorkingCraftSubjectMatcher.java:32-90` (`cellOffset`) | Bed-constrained, which is right for its purpose |
| Rite re-verification | `HarbingerCardinalRiteEvents.java:859-875` (`findPatternNearCenter`, a third copy of the cube scan) | |
| Succession | `succession/SuccessionRites.java:147-157` | Inlines the `originForCell` arithmetic verbatim |
| Founding Fane | `block/harbinger/functional/HematicStakeBlock.java:197` (`findFormation`, bar + crystal geometry), special-cased in the packet at `:56,635-690` | A different shape problem; keep it separate |
| Engram loom | none; `SomaticLoomBlockEntity` is a single-block station with its own ritual state machine | Keep it separate: it is a puzzle, not a multiblock |

- **Recommendation:** add one `PatternOrigins` utility with `originForCell(cell, forwards, up, x, y, z)`, `offset`, `findContaining(pattern, level, hit)` (the packet's exact solver) and `bounds`/`positions`. Point the packet, the helper, `SuccessionRites` and rite re-verification at it.
  - Keep `CardinalRiteStationMatcher` and `WorkingCraftSubjectMatcher` as the semantic layers on top.
  - Expected reduction is about 150 lines. Two cube-scan copies go away (also a performance win for the performance agent to quantify).
  - Difficulty: Low-Med. Gameplay risk: Med. The exact solver and the cube scan can disagree on which match wins when two overlap, so lock behavior with the existing `HematicStakeBloodStructureGameTests` and `EnzymaticScriptoriumGameTests` before swapping.

### 2.7 Rite offering consumption: four paths (VERIFIED)
1. `rite/harbinger/RiteOfferingEscrow` is used by `StationUpgradeRites:86-173` and `WorkingCraftRites:69-232` (escrow, then consume or return through the recovery queue). This is the contract in `docs/STATION_UPGRADES.md`.
2. The ordinary ceremony procession, `CardinalRiteOrdealEngine.java:126`, uses `consumeForRite(false)` plus `releaseShown` with no escrow ledger.
3. `PuppeteerTrialRiteController.java:136-147` consumes directly.
4. `SuccessionRites.java:188-197` stores its own `Offerings` list and calls `consumeOffering`.

The Iron-Brazier channel rites (`rite/brazier/BrazierRites` plus `BrazierRite<P>`, with Memory, Scar and LivingWeaponGraft implementations) are already unified. That subsystem is a good model.
- **Recommendation:** keep paths 1 and 2 distinct. AGENTS.md makes only Working rites escrowed; ordinary rites consume as the procession draws. Move paths 3 and 4 onto a small `RiteOfferings.consumeCaptured(level, itinerary)` helper shared with path 2 (it does the match-then-consume check that `SuccessionRites.matchesOffering` already does).
  - Difficulty: Low. Gameplay risk: Low.

### 2.8 The rite tick and kind dispatch (VERIFIED)
`HarbingerCardinalRiteEvents.onLevelTick` (lines 179-457, about 280 lines, in a file with 124 commits) also ticks the Blood Structure managers (`PendingBloodCraftManager`, `BloodStructureFeedManager`, `BloodInfusionManager` at 183-185). It then branches by kind: Succession, StationUpgrade, WorkingCraft, Puppeteer, Ordeal and LEGACY.

Kind checks are spread across the code base: `isWorkingCraft()` 31 hits in 15 files, `isPuppeteerTrial()` 15 in 6, `CardinalRitePhase.LEGACY` 16 in 5, `StationUpgradeRites.isRite` 9 in 4.

`ActiveCardinalRite` (1,413 lines, 50 fields, about 177 public methods) carries per-kind `CompoundTag` bags (`craft()`, `upgrade()`, `succession()`) alongside typed puppeteer fields.

Rite activation, about 370 lines (`tryStartCardinalRite`, `BloodCraftingKeyPressPacket.java:259-631`), lives in a network packet.
- **Recommendation:**
  - Introduce a `RiteKind` strategy (`matches(rite)`, `tick`, `validate`, `complete`, `terminate`). `StationUpgradeRites`, `WorkingCraftRites`, `SuccessionRites` and `PuppeteerTrialRiteController` already expose static `isRite/tick/complete`, so the change is mechanical.
  - Move `tryStartCardinalRite` to `rite/harbinger/CardinalRiteActivation`.
  - Move the Blood Structure manager ticks to their own `LevelTickEvent` subscriber.
  - Keep LEGACY: the only non-ceremony recipes are the 17 Unstained rites (resources grep: 74 of 91 `cardinal_rite` JSONs have `ceremony`), and the Unstained rework is deferred by design.
  - Difficulty: Med-High. Gameplay risk: Med (tick ordering).

### 2.9 Tiles (VERIFIED)
- There is no project base BE. The sync boilerplate is repeated:
  - `getUpdatePacket` 38/38 identical
  - `getUpdateTag` in 37 files
  - `onDataPacket` in 20
  - `sendBlockUpdated` 40, and `IBloodReservoir.sendUpdates()` called 97 times
  - HutosLib `VanillaPacketDispatcher.dispatch` 12 times
- HutosLib already ships `HutosLib/.../common/block/entity/SimpleInventoryBlockEntity` (a `BaseContainerBlockEntity` with sync), which has **0** uses.
- The blood tank lives in the `BLOCK_BLOOD_VOLUME` attachment (`capability/HemoAttachmentTypes.java:163-165`, `AttachmentType.serializable`, so it is auto-persisted). Even so, 9 BEs also persist `TAG_BLOOD_LEVEL` by hand, and 7 define a private `resolveVolume()` wrapper.
- Inventories: 14 files use `NonNullList` plus `ContainerHelper`; one uses `ItemStackHandler`.
- **Recommendation:** add a thin `HemoBlockEntity` (update packet/tag, `onDataPacket`, `sync()`) and drop manual blood-level persistence where the attachment already saves it. Removing it needs a save-compat read fallback for one release.
  - Expected reduction: about 600 lines.
  - Difficulty: Low (sync) to Med (blood migration). Gameplay risk: Low, as long as the load fallback stays.

### 2.10 Recipes (VERIFIED)
- Codecs. Six serializers use `RecordCodecBuilder`. Eight use a hand-written JSON bridge (`MapCodec` → `RecipeCodecJson.toJsonObject` → `fromJsonObject`). Of the eight, only CardinalRite, BloodStructure and Scar need pattern arrays. **Incubator, FungalScar, ArmatureUpgrade and MemoryWeaving use the bridge without patterns.** All 16 stream codecs are manual `StreamCodec.of`; none use `composite`.
- Seven classes extend vanilla `CustomRecipe` with vestigial `matches(CraftingInput)` that return a constant (`true` in BloodStructure:164, CardinalRite:360, MemoryWeaving:210 and Scar:86; `false` in Incubator:88 and FungalScar:82). Real matching happens in bespoke methods. Five others implement `Recipe<RecipeInput>`.
- `RecipeDegreeGates` (6 callers) and `LegacyBloodStructureIds` (the remap of 20 moved structures plus the Bloodwell → Fane remap) are focused and fine.
- **Recommendation:** use RCB for the four pattern-free bridge serializers, and use `Recipe<RecipeInput>` instead of `CustomRecipe`.
  - Difficulty: Low-Med. Gameplay risk: Low (keep the JSON shape; datapack round-trip tests exist).

### 2.11 Stations (VERIFIED: consolidated)
All seven `UpgradeStation` entries (`station/UpgradeStation.java`: Alembic, Centrifuge, Forge, Armature, Scriptorium, Loom, Condenser) implement `UpgradeableStation`, and so do their BEs. `StationUpgradeCatalog` lists 14 tiers. Rites go through `StationUpgradeRites` and `RiteOfferingEscrow`. The Armature's mission hook goes through `onUpgraded` (`HematicArmatureBlockEntity:461`).

Station-specific leftovers, all intentional adapters:
- `brewing/AlembicTier`, `enchanting/ResonantForgeTier` and `recipe/ArmatureUpgradeRules.ArmatureTier` map the shared `stage` int to behavior.
- `legacyStage` migration fields remain in Alembic (114, 818), Armature (86, 648) and Forge (53, 441).
- `StationUpgradeMigration` folds the old `ADVANCED_BREWING`/`RESONANT_FORGE` attachments.
- `ResonantForgeTier.degree()` (3/5/7) has no caller I could find. LIKELY dead, and its 3 does not match the catalog's D5/D7.

Station *operation* logic is split. The Scriptorium enchant and cure run inside `EnzymaticScriptoriumMenu.clickMenuButton` (395-line menu, blood drains at 314 and 351), while other stations run in their BE. Recommendation: move it into the BE. Difficulty: Low. Gameplay risk: Low.

### 2.12 Transient server-state cleanup (VERIFIED)
- `event/TransientServerStateEvents.java:40-60` clears 19 managers.
- 21 other classes register their own `ServerStopped`/`Stopping` handlers (for example `ductilis/AxonalTransductionManager`, `ConductionManager`, `EngramEvents`, `EarthenVeinTravelManager`).
- `BloodManipulation.clearSessionState` (93-98) fans out to three more.

Recommendation: register everything in the central list. Difficulty: Low. Risk: none.

### 2.13 Small packages
- `damage/`: `SchoolDamage.cast` is a try-with-resources context opened by both the player path (`BloodManipulation:546`) and the entity path (`EntityManipulationEffects:69`). The design is coherent (cross-family damage is out of scope for this audit).
- `summon/`: puppeteer rules plus the crossbar; the trial is wired as a Cardinal Rite kind. Fine.
- `brewing/` and `enchanting/`: the Alembic and Forge rule layers. Fine.
- `loot/`: one 60-line modifier.
- `mission/`: 44 classes. Each assignment is a static class with its own `*Progress` record, and there is no shared lifecycle interface (pending deliveries were covered by the earlier audit).
- `succession/`: a separate rite kind (see 2.6 and 2.7).
- `circus/`: self-contained controllers plus saved data.

---

## 3. Duplicated utilities (VERIFIED)
| Utility | Copies | Canonical candidate |
|---|---|---|
| Entity raytrace | `manipulation/ductilis/DeadlyGazeManip.java:27` is a fork of `HutosLib/.../math/MathUtils.java:68` with geometry clip and visibility added; `HematicCommandManip:30` and `ThreadRipperManip:69` call it **through a concrete manipulation class** | Move it to `ManipulationCombatHelper.rayTraceEntities` |
| Cone look-target | `ManipulationCombatHelper.aimedTarget:86` (with `canHarm` and visibility) and `item/harbinger/morphlings/MorphlingItem.java:436 findLookTarget` (neither check) | Keep both semantics; host them in one helper with flags |
| Look-at block | `block/harbinger/BlockBloodInteractions.java:94-118` plus 34 `new ClipContext` sites in 26 files | `SanguineProjectionTargeting.pick` already exists; prefer it |
| Pattern origin, match bounds and positions | See 2.6. `getMatchBounds` appears twice (packet:748, helper:172) and `checkPathAlignment` twice (packet:776, helper:196), with identical bodies | `PatternOrigins` |
| `lerp` | 19 private copies (13 in `common/`) with **inconsistent argument order**: `(amount,start,end)` in `CardinalRiteMath:35`, `HarbingerCardinalRiteEvents:844`, `CardinalRiteOrdealEngine:354` and `CardinalRiteHumanityGeometry:239`; `(start,end,t)` in the sigil classes and `FoldActivationBounds:113` | `Mth.lerp` (836 existing uses) or `CardinalRiteMath`. Watch the argument order |
| `smoothstep` | 16 private copies; `CardinalRiteMath:24,30` is public | `CardinalRiteMath` |
| `clamp(` | 35 private defs | `Mth.clamp` / `Math.clamp` |
| give-or-drop | `DialogueEventHandler:1240,1529`, `HematicArmatureBlockEntity:477`, `CardinalRiteCompletionEffects:347`, `LivingArsenalInventoryGuard:189`, `QliphothPomeInventoryEvents:156`; 43 raw `getInventory().add(` calls | Defer to the earlier pending-delivery audit |
| BE blood accessor | 7 private `resolveVolume()` copies plus `IBloodReservoir.getBloodCapability` | `IBloodReservoir.getBloodCapability()` |
| Inventory BE | 12 `BaseContainerBlockEntity` subclasses | HutosLib `SimpleInventoryBlockEntity` (unused) |
| UUID list/map NBT | `ActiveCardinalRite.java:1235-1410` (6 helpers, the only copy) | Fine as is |

---

## 4. Dead or abandoned implementations
| Item | Evidence | Confidence |
|---|---|---|
| `recipe/RecipeSacrificialBloodCrafting.java`, an empty class | 0 references in main, test or gameTest | VERIFIED |
| `manipulation/MobManipCaster.java` | 0 main references. `WillCombatRedesignSourceTest:42` asserts that it *exists*, and `WillResourceSourceTest:68` asserts that Will no longer uses it, so deletion must update that test | VERIFIED |
| `BloodManipulation.performDrudgeAction` (701) | 0 callers; `DrudgeEntity` calls `getDrudgeAction()` directly | VERIFIED |
| `IBloodVolume.drainFromSource` | 0 external callers. Its semantics are inverted: it drains `this` and fills `src` | VERIFIED |
| `BloodCraftingKeyPressPacket.getMatchBounds` (748, private) | No call inside the file | VERIFIED |
| `CardinalRiteStructureConsumptionRules` | 0 main references; only its unit test | VERIFIED |
| Player-side bodies of 14 retired manipulation classes (about 1,070 lines: `BloodLampManip`, `CrimsonHarvestManip`, `HemosynthesisManip`, `VitalReservoirManip`, `SanguineExcavationManip`, `FerricResonanceManip`, `GlacialBastionManip`, `BloodEclipseMantleManip`, `CrimsonSightManip`, `GlacialCirculationManip`, `FerricTransmutationManip`, `VigilOfGlassManip`, `HematicBallastManip`, `SummonThrallManip`) plus base-class `venous_travel` | Still registered (`ManipulationRetirementRules.java:11-26`); `tryPerformAction` refuses them. **Keep the registry IDs** for save and memory-item compatibility. The `getAction` bodies are unreachable for players | VERIFIED unreachable for players |
| `EntityManipulationEffects` arms `glacial_bastion` and `blood_eclipse_mantle` (95, 104) | Dead through `castFromEntity` (retired guard at `BloodManipulation:136`), but **live** through Vesper's direct `EntityManipulationEffects.cast` (`VesperManipulationCoverage:102,117`). Keep them | VERIFIED live |
| `ResonantForgeTier.degree()` | No forge-related `.degree()` call found | LIKELY |
| `CirculationIncomeHelper` | Documented as a compatibility adapter; 2 callers (`RootedStateHelper:72`, `ArmorSetBonusHandler:367`) | Live, could be inlined |

---

## 5. Hotspot files (size × churn from `git log --follow`)
| File | Lines | Commits | Why it is hot |
|---|---|---|---|
| `rite/harbinger/HarbingerCardinalRiteEvents.java` | 1,725 | 124 | A 280-line tick, kind dispatch, completion and client snapshot all in one file |
| `manipulation/BloodManipulation.java` | 716 | 67 | The single cast gate; a 136-line `tryPerformAction` |
| `network/capa/harbinger/BloodCraftingKeyPressPacket.java` | 802 | 66 | Rite activation, Blood Structure crafting and a private matcher, inside a packet |
| `init/ManipulationInit.java` | 1,296 | 45 | 48 inline AI lambdas |
| `tile/harbinger/crafting/GhastlyAlembicBlockEntity.java` | 1,038 | 37 | |
| `recipe/serializer/CardinalRiteRecipeSerializer.java` | 704 | 35 | |
| `tile/harbinger/crafting/SomaticLoomBlockEntity.java` | 1,412 | 33 | |
| `tile/harbinger/crafting/VialCentrifugeBlockEntity.java` | 821 | 33 | |
| `rite/ActiveCardinalRite.java` | 1,413 | 18 | 50 fields and per-kind tag bags |
| `rite/harbinger/CardinalRiteCompletionEffects.java` | 1,138 | 1 | Large but already a path-keyed registry; low risk |

---

## 6. Observed defects (report only, not fixed)
1. **LIKELY: rite-locked stations accept projected blood.** `BloodProjectionItem.java:207-216` calls `tileVolume.fillFromSource(...)` without checking `IBloodReservoir.canReceiveBlood()`. Alembic, Armature, Centrifuge, Scriptorium, Forge and Loom (lines 203, 435, 805, 88, 109, 1230) return `!riteLocked` there. `docs/STATION_UPGRADES.md` says rite locking stops blood transfer. The GameTests (`CentrifugeUpgradeGameTests:320`, `StationUpgradeGameTests:298`) only exercise `receiveBlood`, which the projection never calls. The absorption side does check `canProvideBlood` (`BlockBloodInteractions:123`). This is not checked in game.
2. **VERIFIED: scar blood upkeep bypasses the ledger and Sanguine Reserve.** `item/harbinger/scar/ScarDefinition.java:163-176` calls `v.drain(effectiveUpkeep)` directly for three scars (`ScarInit.java:67,108,158`, at 0.3–0.5 per tick). The skill text promises protection "from passive and automatic spending" (en_us `skill_sanguine_reserve.desc`), and `BloodFlowLedger.applyDrain` protects the SCAR category. There is no sync and no diagnostics entry, and `drain` empties the volume to 0 when it is short. Fixing this changes the balance experienced in play: get a design decision first.
3. **VERIFIED: block blood level persisted twice, with a precision mismatch.** `BLOCK_BLOOD_VOLUME` auto-serializes a double, and 9 BEs also write `TAG_BLOOD_LEVEL` with `putDouble`. Four of them read it back with `getFloat`: `GhastlyAlembicBlockEntity:856,935,946`, `MorphlingIncubatorBlockEntity:501,566`, `MycelialCrucibleBlockEntity:524,574` and `MycelialLanternBlockEntity:342,376`. The result is float truncation on load and in client sync. Low impact.
4. **VERIFIED: stale Javadoc.** `tile/harbinger/rite/IronBrazierBlockEntity.java:180` cites `BloodStructureCraftingHelper.consumeMatchedOfferings`, which does not exist. AGENTS.md says Blood Structures take no offerings.
5. **VERIFIED, by design: `drain()` empties on overstrain.** `BloodVolume.java:37-45` and `ItemStackBloodVolume.java:50-56` set the volume to 0 and return false. Almost every caller pre-checks first. The few that do not are deliberate punishments (for example the rite boundary at `HarbingerCardinalRiteEvents:348`). Treat this as an API hazard rather than a bug, and make the facade in 2.4 explicit about it.
6. **VERIFIED: the post-cast sync skips diagnostics.** `BloodManipulation.java:545` sends a bare `BloodVolumeServerPacket` rather than `BloodVolumeEvents.syncVolume`, so the flow/max/manipulation diagnostics are not refreshed after a cast (40 other sites do the same).
7. **HYPOTHESIS: possible friendly fire or missed allies.** Nine manipulations gather entities without a `canHarm` filter (list in 2.3). Each needs a design read before anyone calls it a bug.

---

### Where to start
1. `PatternOrigins` (2.6) and the dead-code removals (section 4) are the lowest-risk changes.
2. Next comes the `BloodAccounting.pay` facade with behavior-preserving flags (2.4), together with the projected-sink interface (2.5), which also closes defect 1.
3. Then the `HemoBlockEntity` sync base (2.9).
4. Last, the `RiteKind` strategy extraction (2.8), which needs the most test coverage.
