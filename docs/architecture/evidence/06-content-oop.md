# 06 — Content class design audit (entities, blocks, items, menus, effects, summons)

Read-only audit of `src/main/java/com/vincenthuto/hemomancy/` (paths below are relative to it unless they start with `src/`). Counts come from a declaration parser and greps run over 2,182 `common/` files on 2026-10-21. **VERIFIED** means I read the code or the grep is exact. **HYPOTHESIS** means it is inferred and not run.

Scope sizes (files / lines): `common/entity` 370 / 57,008 · `common/block` 143 / 20,594 · `common/item` 310 / 27,307 · `common/menu` 47 / 4,739 · `common/effect` 46 / 2,146 · `common/armor` 9 / 1,513 · `common/summon` 13 / 937.

---

## 1. Inheritance trees (VERIFIED, top-level classes)

### Entities (207 entity classes; the other 163 files in `common/entity` are helpers, rules, records or goals)
```
Monster 33 ─ every boss (Vesper×2, Annetta, Priestess, Latent Annetta, Mycophant, Naeglerophaeon, 4 Saints, Seraphae fragment) + 19 mobs
PathfinderMob 32 ─┬ ProfessionalHarbingerEntity (lives in common/succession!) → 5 Harbinger NPCs
                  ├ CircusPerformerEntity → 5 performers
                  ├ BloodConstructEntity → CloudEntityBlood, EntityWretchedWill, FerricConstructEntity → EntityIronPillar/Spike/Wall
                  └ 7 standalone NPCs, Drudge, BloodThrall, PaleIntercession, PhantasmalEcho, 13 creatures
Entity 13 (utility/anchor/seat entities) · PartEntity 2 · ItemEntity 2
ThrowableProjectile 11 · AbstractArrow 8 (→ BloodNeedle → CircusKnifeProjectile) · ThrowableItemProjectile 3 · AbstractHurtingProjectile 1
WaterAnimal 5 (→ PelagicAnimal → 6 small pelagic mobs) · AbstractSchoolingFish 2 · AbstractFish 1
Zombie → GroundPuppetEntity → 5 puppets · Skeleton/Wolf/Vex → 1 puppet each · Spider 3 · Animal→Leech→Bloodlicker · Bat 1
```
Shared entity interfaces: `ProgressionDialogueNpc` (12 implementers), `SyncedBoundSummon`/`BoundPuppeteerSummon` (9), `CombatWeaponCarrierProjectile` (6), `OwnableEntity` (5 hand-rolled), `TendencyDamageCarrier` (2), `IBloodConstruct` (1), `BroodMother` (1).

### Blocks (110 `*Block` classes repo-wide; 104 in `common/block`)
`Block` 49 · `BaseEntityBlock` 30 · `FlowerBlock` 8 · `AshTrailBlock` 4 · `WaterloggableBlock` 2 · `DictationTableBlock`/`PelagicColonyBlock`/`SnowyDirtBlock` 2 each · 1 each of vanilla `DoorBlock`, `WallBlock`, `IronBarsBlock`, `PumpkinBlock`, `LiquidBlock`, `BaseFireBlock`, `PointedDripstoneBlock` and others. There are 59 `EntityBlock`s: 30 extend `BaseEntityBlock` and 29 are `Block implements EntityBlock`. 48 classes implement `SimpleWaterloggedBlock`. `BlockInit` already uses data-driven construction for 40 plain `new Block(...)` registrations plus vanilla types (FlowerPot ×7, IronBars ×5, Stair, Slab and others).

### Items (285 top-level classes in `common/item`)
`Item` 91 · `BlockItem` 18 · `ArmorItem` 14 · `MorphlingItem` 12 · `LivingToolItem` 9 · `ItemFungalScar` 8 · `HematicMemoryToolItem` 7 · `ItemGuideBook` 5 · `AbstractSpecialBloodLustArmorItem` 3 · `StackBackedItemHandler` 3 · smaller bases (`ItemScar`, `BloodGourdItem`, `LivingItem`, `DiggerItem`, `SwordItem`, `ArrowItem`) 2 each · 93 non-item helpers.

### Menus / effects / armor / summon
- **Menus:** all 25 menus extend `AbstractContainerMenu` directly. There is no project menu base. There are 10 `Slot`, 5 `SlotItemHandler` and 5 `MenuProvider` classes.
- **Effects:** 38 classes extend `MobEffect` directly. `ManipulationStatusEffect` is the one parameterised class, registered 7+ times in `common/init/EffectInit.java:296-319`.
- **`common/armor` and `common/summon`:** no inheritance. Both hold static rules, handlers and registries.

---

## 2. Competing patterns and recommendations

| # | Pattern (evidence) | Active count | Recommendation | Simplification | Difficulty | Behaviour / registry risk |
|---|---|---|---|---|---|---|
| P1 | **Copy-paste mob pairs.** `mob/arthropod/ChitiniteEntity` vs `FerventChitiniteEntity` share 153/156 code lines; only the class name and the 3 sound getters differ. `ChthonianEntity` vs `ChthonianQueenEntity` share 177/182; they differ by sounds and `hurtAndBreak(5 vs 8)` (lines 180/184). Both pairs have identical attributes. | 2 pairs, ~420 duplicate lines | Make each variant a thin subclass of the base, overriding sounds plus one `int shieldDamage()` hook. Keep both `EntityType`s and classes, because renderers, models and spawn-placement refs are typed on them. **Careful:** `sporePuff`/`RollupGoal` use `instanceof ChitiniteEntity`. Once Fervent extends Chitinite, that check covers both types, a small semantic change. `sporePuff` is dead code anyway (§8). | −350 lines | Low | No registry change. One `instanceof` semantic to preserve. |
| P2 | **Tracking projectile trio.** `projectile/TrackingPestsEntity` vs `TrackingBloodOrbEntity` share 146/154 lines (94%). Pests vs `TrackingSerpentEntity` share 142/169 (84%). They differ in particle colour, speed and an on-hit effect. | 3 (plus `DirectedBloodOrbEntity`, which has the same static `TYPE`) | One `AbstractTrackingOrbEntity` with a small record of tuning values (speed, turn rate, particle factory, on-hit hook). This is a justified shared base: same parent, same synced fields (`EVIL`, `TARGET`) and the same steering code. **But see §6:** 2 of the 3 are only constructed by dead items. Decide reachability first. | −300 lines | Low | Keep the `tracking_*` ids; saved worlds may contain them. |
| P3 | **Hand-rolled synced owner UUID.** `DATA_OWNER_UUID` + get/set + `"Owner"` NBT appear in `npc/DrudgeEntity.java:57,179-184,507,520`, `summon/BloodThrallEntity.java:52,129-134,224,235`, `summon/PaleIntercessionEntity.java:41,286-300`, `summon/PhantasmalEchoEntity.java:34,306-356`, `mob/monster/EnthralledDollEntity.java:39,88,202-207`. | 5 | Use a composition record like the existing `summon/BoundSummonSync` (`SyncedOwner.define(Cls)`, `defineDefaults`, `save`/`load`). Do **not** create a base class: the parents differ (PathfinderMob vs Monster), and Drudge/Thrall share only 21% of their code. | ~−60 lines, one NBT key spelling | Low | Keep the `"Owner"` tag name. |
| P4 | **Protected NPC boilerplate.** The same `hurt()` gate (GENERIC_KILL / creative / successor) appears in 14 NPC files, e.g. `npc/harbinger/HarbingerArtificerEntity.java:48`, `HarbingerCicatrixAnchoriteEntity.java:48`, `HarbingerMnemonistEntity.java:55`, `HarbingerVoyagerEntity.java:48`, `npc/unstained/UnstainedGuardianEntity.java:57`. Plus `removeWhenFarAway=false`, an idle `AnimationState` tick, and `mobInteract → PacketHandler.sendToPlayer(new OpenDialoguePacket(progressionDialogue(sp)))` (17 files). Pairs share 64–87% (Wayfarer/Voyager 87%, Artificer/Anchorite 87%). | 14 | Add a default `openProgressionDialogue(Player, Hand)` to `ProgressionDialogueNpc`, plus a static `ProtectedNpcDamage.allows(source, entity)` helper. Keep the classes separate: each owns its dialogue tree, renderer and model, and Hermit/Vicar/Scout have real exceptions to the damage gate. | ~−250 lines | Low | None if exceptions stay explicit. |
| P5 | **Boss bars.** 11 bosses each declare `new ServerBossEvent(...)`, `startSeenByPlayer`/`stopSeenByPlayer` (21 overrides) and `setProgress(health/max)`: `boss/annetta/AnnettaKnowlesEntity.java:99,483`, `boss/endgame/MycophantEntity.java:74,244`, `NaeglerophaeonEntity.java:59,227`, `VesperTheCrownedRefusalEntity.java:127,221`, `boss/saint/putriciel/PutricielEntity.java:94,183-208`, and others. | 11 | Use a composition helper `BossBarBinding` (owner, colour, overlay → `track/untrack/tickProgress/setVisible`). Avoid a universal boss base class: Vesper, Annetta and Mycophant have very different state machines. | ~−100 lines | Low | None. |
| P6 | **The three Saints are a real family.** Putriciel/Velorum share 121 of ~235 code lines. All three Saints (`hemorath`, `putriciel`, `velorum`) copy `DATA_VISUAL_STATE`/`DATA_VISUAL_TICKS`/`tickVisualState`, the boss bar, `broadcastMessage` with `Component.literal`, the "drop nothing outside trial" override and sound stubs. | 3 (4 with Seraphae) | A `SaintTrialEntity extends Monster` abstract base is **justified** here: same lore role, same spawner (`HarbingerSaintEncounterHooks.spawnSaintBoss`, called from `SaintSarcophagusBlock.java:690`), same trial lifecycle. Keep Seraphae separate unless it fits naturally. | ~−200 lines | Med | No ids. Verify attributes and visual ids stay per class. |
| P7 | **Boss move timeline is shared but misnamed.** `boss/endgame/VesperMoveTimeline` (record windup/active/recovery) is already used by `MycophantControllerRules.java:15-19` and `MycophantController.java:129-186`. `EndgameBossActions` is mostly Vesper-only (16 of 22 methods take a Vesper type), with 5 generic helpers (`acquireTarget`, `findGround`, `finishWithExplosion`...). | 2 bosses | Rename to `BossMoveTimeline` and split the generic helpers into `BossActions`. **The best existing example is the Mycophant:** entity (712 lines) + `MycophantController` (301) owning timeline and interrupts + pure `*Rules` classes with tests. Annetta (EncounterState enum + `AnnettaEncounterRules`) is second best. Vesper follows the same idea but is fragmented across ~45 files. Saints and Naeglerophaeon hand-roll tick counters. | naming/discoverability | Low | None. |
| P8 | **`DialogueEventHandler` is a god method.** `npc/dialogue/DialogueEventHandler.java:83` `onDialogueOption` runs from about line 83 to 525: 6 prefix `startsWith` chains, a 36-label string `switch` (line 140) and 50 static handlers for 10+ NPCs. It is the most-churned file in scope (40 commits since June). | 1 file, 1,561 lines | Route by event-id prefix through a `Map<String, DialogueEventAction>` registry, one class per NPC (`VicarDialogueEvents`, `ArtificerDialogueEvents`, …). The existing `StationUpgradeDialogue.parseClaim`, `AntecedentDialogue.handle` and `CircusSchoolDialogue.handle` already follow this pattern. There is no class hierarchy to fix: the dialogue tree classes are static factories, which is fine. | splits 1,561 → ~10 files of 100–200 | Med | Event-id strings must match exactly. Keep the order of the prefix checks (`artificer_` validation precedes the switch). |
| P9 | **Ticker wiring.** There are 8 private copies of vanilla `createTickerHelper` in `Block implements EntityBlock` classes: `block/harbinger/crafting/MorphlingIncubatorBlock.java:134`, `MycelialCrucibleBlock.java:222`, `MycelialLanternBlock.java:181`, `PuppeteersSpindleBlock.java:45`, `SomaticLoomBlock.java:69`, `VialCentrifugeBlock.java:56`, `functional/MnemonicReliquaryBlock.java:105`, `VisceralMirrorBlock.java:71`. Another 8 use raw `type == X.get() ? Foo::tick : null` without the type-safe helper (`decoration/BlockSerpentineIdol.java:70`, `functional/ClairaudiographBlock.java:39`, `EarthenVeinBlock.java:87`, `rite/SuspendedBloodCrystalBlock.java:74`, `rite/SuspendedVivianiteBlock.java:74`, `unstained/SuspendedCleansedBloodCrystalBlock.java:73`, both Phlebotomist blocks). | 47 `getTicker` overrides | Add one static `HemoTickers.of(actual, expected, ticker)` (and `serverOnly(...)`) in `block/shared`. | −8 helpers, uniform side-gating | Low | None. |
| P10 | **`BaseEntityBlock` vs `Block implements EntityBlock`.** 30 / 29 split. `BaseEntityBlock` forces `codec()`: there are 66 overrides, 42 of them `return CODEC`. It also defaults to `RenderShape.INVISIBLE`. | 59 | Do not mass-migrate: render shape and codec behaviour differ. Pick `Block implements EntityBlock` + `HemoTickers` for new blocks and document it. | n/a | — | Changing the base can change render shape. |
| P11 | **Suspended crystal triplet.** `rite/SuspendedBloodCrystalBlock` vs `SuspendedVivianiteBlock` share 53/55 lines and vs `unstained/SuspendedCleansedBloodCrystalBlock` 52/55. The only differences are the BE type/factory and shape. | 3 | One `SuspendedCrystalBlock(props, Supplier<BlockEntityType>, VoxelShape)`, registered three times. The BEs differ, so they stay separate. | −110 lines | Low | Keep the 3 registry ids. `BlockItem` subclasses may need retyping. |
| P12 | **Shape rotation.** `functional/WarpChairShapeRules.java:49 rotate()` is mathematically identical to `shared/ModelShapes.java:43 rotate()` (I checked all 4 facings by hand). `rite/BrazierMount.java:131` and `functional/HematicStakeBlock.java:58` have their own array-based rotators. 11 blocks keep `SHAPE_N/E/S/W` tables. `ModelShapes` has 18 users, so it is the winner. | 3 extra rotators | Point them at `ModelShapes`. `HorizontalFacingRotationHelper` intentionally keeps a "reversed" convention for legacy multiblocks; leave that alone. | small | Low | Shape regressions are visible; spot-check. |
| P13 | **Waterlogging is mostly converged.** `shared/WaterloggedBlockSupport` has 44 users, the `WaterloggableBlock` base has 2 subclasses, and 5 files still inline `Fluids.WATER.getSource(false)`. | 48 | Finish the migration of the remaining 5; leave the base class. | trivial | Low | None. |
| P14 | **Item data storage.** Legacy `DataComponents.CUSTOM_DATA` appears in 80 files with 342 references (53 item files, 196 `copyTag()`, 112 `CustomData.of`). Typed `DataComponentInit` has 14 components with 105 references in 41 files. `getOrCreateTag` = 0 (migration done). Hotspots: `item/harbinger/morphlings/MorphlingItem.java` (30 refs), `tool/MarionetteCrossbarItem.java` (11), `tool/SporiticThuribleItem.java` (10), `morphlings/EmberfangMorphlingItem.java` (10), `tool/living/*` (14 files). 6 private `getCustomData/getData` helpers are re-declared in separate items. | 80 vs 41 files | Move state that is synced or rendered each frame (morphling state, living-tool modes, crossbar threads) to typed records. Keep `CUSTOM_DATA` for one-off flags. | perf plus type safety | Med–High | **Save migration:** needs a reader for the old `CUSTOM_DATA` keys. |
| P15 | **BEWLR/armour-model forwarding subclasses.** 58 common items implement the **client-package** interface `client/item/HemoClientItemExtensionsProvider` and build renderers inline. 15 are `BlockItem` subclasses that only do that (e.g. `item/harbinger/tile/functional/EarthenVeinBlockItem.java`, `tile/crafting/VialCentrifugeBlockItem.java`). 12 armour items repeat an item-identity `if` chain in `getHumanoidArmorModel` (`item/shared/armor/BarbedArmorItem.java`; Barbed/Chitinite/Prismatic share 82–85%). | 58 / 15 / 12 | Keep a client-side map from item holder to extension factory in `ClientEvents.registerClientItemExtensions` (`client/event/ClientEvents.java:1315`). Then the 15 BlockItems become plain `BlockItem`, and armour items become one `ModeledArmorItem(material, slot, tooltipKey)` with the model chosen by `EquipmentSlot`. | −15 classes, ~−400 lines, removes the common→client edge | Med | Registry ids unchanged. Only item **classes** change; check `instanceof` uses first. |
| P16 | **Effect boilerplate.** 25 effects override the same 4 methods. At least 13 `getDisplayName()` overrides return exactly `effect.hemomancy.<id>`, which vanilla already produces. 4 return `Component.literal` (BloodLoss, BloodRush, Hemolysis, MarkedByCanon). 25 `isBeneficial()` overrides restate the category. 11 effects are constructor-only. | 25 | Delete the redundant overrides. Marker effects can be `new MobEffect(cat, colour)` via a small public subclass, following `ManipulationStatusEffect`. | ~−300 lines | Low | `HemolysisEffect` is NEUTRAL but returns `isBeneficial()=true` (§8). Keep that value deliberately or fix it. |
| P17 | **Menu slot/quick-move boilerplate.** 19 menus hand-write the 3×9+9 player-inventory loops; only 3 have a private `addPlayerSlots`. 25 `quickMoveStack` bodies total 837 lines, all distinct, but the skeleton (copy, range move, `setChanged`, `onTake`) repeats. 5 slotless "view" menus (`TendencyViewMenu`/`VascularViewMenu` 95% identical, `ScryingDiagnostics`, `StructureSpawner`, `MnemonicReliquary`) have identical `quickMoveStack` returning EMPTY. 5 `MenuProvider` classes could be `SimpleMenuProvider` and return `Component.literal("Tendency View")`. | 25 | A thin `HemoContainerMenu extends AbstractContainerMenu` with `addPlayerInventory(inv,x,y)` and `quickMoveTo(ranges…)` is justified: the parent is the same and the code is pure boilerplate. Replace the providers with `SimpleMenuProvider` + translatable titles. | ~−350 lines, −5 classes | Low–Med | Slot index order must stay identical, or shift-click breaks. |

**Should stay distinct (VERIFIED):**
- Drudge vs BloodThrall (21% shared).
- PhantasmalEcho vs PaleIntercession (19%).
- Circus performers (19%; they already share `CircusPerformerEntity`).
- Vesper, Annetta and Mycophant controllers.
- Simple monsters (Cruor/FrozenClot 72%, Thirster/LumpOfThought 80%). That overlap is vanilla goals/attributes/sounds idiom, so merging buys nothing.
- FlowerBlock subclasses, which each have different soil rules.
- Puppet summons, which already compose `SyncedBoundSummon` + `BoundSummonSync` + `BoundSummonBehavior` (628 lines) across Zombie/Skeleton/Wolf/Vex parents. This is the cleanest content composition pattern in the repo; reuse it for P3.

Items that duplicate manipulation logic: not established. Blood projectiles are spawned from both items (`BloodBoltItem`, `LivingPistolItem`) and manipulations through the entity's own factories (e.g. `BloodShotEntity.fromManipulation`). I found no copied manipulation bodies (HYPOTHESIS: not exhaustively checked).

---

## 3. Boilerplate quantification (VERIFIED greps over `common/entity`)

| Override / idiom | Files | Notes |
|---|---|---|
| `defineSynchedData` | 94 | 251 `SynchedEntityData.defineId` calls in 83 files |
| `addAdditionalSaveData` / `readAdditionalSaveData` | 87 / 88 | |
| `registerGoals` | 88 | Custom goals: 30 classes. All bespoke except `RollupGoal`, copied in 4 arthropods |
| Attribute builders | 91 `setAttributes`, 3 `attributes`, 1 `createAttributes`, 1 `definitionAttributes` | Naming is inconsistent but harmless |
| `getAmbient/Hurt/DeathSound` | 54 / 53 / 53 | 101 return `SoundInit.X.get()`, 39 return `SoundEvents.X`. Idiomatic: **not worth abstracting** |
| `removeWhenFarAway` | 24 | |
| Boss bar trio | 11 files, 21 `startSeenByPlayer`/`stopSeenByPlayer` | P5 |
| Synced-int enum state via `byId(entityData.get(...))` | 15 | Acceptable; a custom `EntityDataSerializer` is not worth it |
| `idleAnimationState.startIfStopped` | 27 | |
| NPC protected-hurt gate | 14 | P4 |
| Owner UUID trio | 5 × ~15 lines | P3 |
| `CombatWeaponCarrierProjectile` field + `"CombatWeapon"` NBT + private `copyCombatWeapon` | 5 copies (`BloodBoltEntity.java:319`, `BloodBulletEntity.java:144`, `BloodNeedleEntity.java:235`, `BloodShotEntity.java:358`, `DarkArrowEntity.java:118`) | All `AbstractArrow`. A shared abstract `CombatWeaponArrow` base is justified. Note vanilla `AbstractArrow.getWeaponItem()` already stores the firing weapon. The duplicate field exists because manipulation-shed shots carry a tendency source without a vanilla firing weapon, so keep the separate field. |

Sampled 23 entity pairs (code-line similarity, ignoring imports and comments): 98, 97, 96*, 94, 87, 87, 84, 80, 78, 76, 72, 68, 64, 53, 50, 44, 41, 39, 30, 23, 22, 19, 12 %. (*96% is the block pair in P11.)

---

## 4. Single-implementation interfaces and forwarding wrappers (VERIFIED)

| Interface | Implementers | Referenced in | Verdict |
|---|---|---|---|
| `capability/.../routing/IBloodRoutingTarget` | **0** | `BloodRoutingHelper.java:87,180,204` (instanceof) | Dead extension point. All three branches are unreachable. Delete it or implement it. |
| `entity/summon/IBloodConstruct` | 1 (`BloodConstructEntity`, plus its 5 subclasses) | 5 files, instanceof only | Marker. Could be `instanceof BloodConstructEntity`. Low value. |
| `entity/mob/animal/BroodMother` | 1 (`MycophantEntity`) | `FunglingEntity.java:198` | Keep. It decouples a `mob` package class from `boss`. |
| `manipulation/EntityCastableManipulation` | 1 (`BloodManipulation`) | `WillManipulationCaster.java:17` | The instanceof is always true for `BloodManipulation`. Inline it. |
| `capability/block/vein/IEarthenVeinLoc` | 1 | none besides its implementer | Collapse. |
| `tile/IWhiteHumorTile` | 1 (`PallidRetortBlockEntity`) | `HemoCapabilityAccess.java:362` | Keep (capability lookup seam). |
| `capability/.../equipment/IRenderHarbingerEquipment` | 1 (`ItemFungalScar`) | `client/render/layer/player/RenderScarsLayer.java:39` | Keep (render seam). |
| `entity/summon/BoundPuppeteerSummon` | 1 (`SyncedBoundSummon`, which has 9 implementers) | 36 files | Fine. The split is historical; you could fold it into `SyncedBoundSummon`. |
| `armor/ability/ArmorSetAbility` | 1 (`SimpleArmorSetAbility`) | registry + radial screen | Fine as an API type. |
| `item/harbinger/morphlings/IMorphling` | 1 (`MorphlingItem`) | 11 files | Fine (instanceof seam), but could be the class. |
| Capability `I*` pairs (`IInitiatoryDegree`, `IKnownManipulations`, `IScars`, `IBloodTendency`, `IUnstainedProgress`, `IVascularSystem`, `IVisceralOrgans`, `IKnownSummons`, `IKnownStillArts`, `IEquippedMorphling`, `IWhiteHumorVolume`, `ILivingStaffProgress`, `IScarItem`, `IHarbingerEquipmentItemHandler`) | 1 each | 4–35 files | **Keep.** They are the `HemoCapabilityKeys`/`HemoCapabilityAccess` contract (AGENTS.md); collapsing them is churn with no payoff. |
| `CommandedCreeper`, `PelagicBiomeSourceAccess`, `PelagicRandomStateAccess` | 1 mixin each | | Required mixin duck types. Keep. |

**Forwarding / near-empty classes:**
- 15 `*BlockItem` classes that only supply a BEWLR (P15).
- 7 ctor-only items: `MonolithImbuedClothItem`, `TinctureSanguineFistsItem`, `Edacious/Phantasmal/SheolicBloodLustArmorItem`, `BloodHonedScar`, `CurvedHornItem`.
- 6 `ItemFungalScar` subclasses that only append one `Component.literal` tooltip line (e.g. `item/harbinger/scar/fungal/TalaromycesMinusItem.java`). Only 2 are used by `instanceof` (`AntiphonomycesResonansItem` in `CrawlingChoirHandler.java:33`, `SaprovittaVestigiumItem` in `ScarsEntityEventHandler.java:85`), and those checks could be `stack.is(ItemInit.x)`.
- `LivingSyringeItemHandler` and `ScarBinderItemHandler` (ctor-only `StackBackedItemHandler`).
- `SmoulderingAshTrailBlock` (no members).
- The 5 slotless view menus and their providers (P17).

---

## 5. Naming counts (VERIFIED, repo-wide `src/main/java`)

| Convention | Count |
|---|---|
| `*BlockEntity` / `*Tile` / `*BE` | 64 / 1 (`IWhiteHumorTile`) / 0. The `common/tile/` **package** name is the only leftover "tile". |
| `*Menu` / `*Container` / `*MenuProvider` | 26 / 3 / 5. The 3 Containers (`HarbingerEquipmentContainer`, `ScarsContainer`, `SpecimenContainer`) are capability stores, not menus. That is OK. |
| `*Entity` suffix / `Entity*` prefix | 209 / 13. Real outliers are `EntityFlyingCharm`, `EntityMorphlingPolypItem`, `EntityQliphothSeedItem`, `EntityIronPillar/Spike/Wall`, `EntityWretchedWill`, plus the infix `CloudEntityBlood`. |
| `*Item` suffix / `Item*` prefix | 187 / 19. 10 real items use the prefix: `ItemScar`, `ItemFungalScar`, `ItemImmatureFungalScar`, `ItemScarBinder`, `ItemScarPattern`, `ItemMorphlingJar`, `ItemMorphlingPolyp`, `ItemMindSpike`, `ItemSanguineConduit`, `ItemSelfReflectionMirror`. |
| `*Block` suffix / `Block*` prefix | 111 / 7. Real outliers: `BlockHumaneIdol`, `BlockSerpentineIdol`. |
| `Hemo*` / `Blood*` prefixes | 62 / 197 |
| `I*` interfaces / `Enum*` prefix | 35 / 20 |
| Other | `PhanstmalBloodlustArmorAbilityHandler` (typo of "Phantasmal"); `AbhorentThoughtEntity` (single "r"; renaming the class is safe if the registry id is untouched). |

Renaming is low value and only touches classes; registry ids live in `*Init` strings. Do it opportunistically.

---

## 6. Dead or orphaned classes

Method: for each class in scope, I searched for its simple name across all of `src/main/java`, `src/test` and resources (json/toml/mcmeta), excluding `@EventBusSubscriber`/`@Mixin` classes. For each hit I then checked `*Init` registrations and resource ids.

| Class | Lines | Confidence | Evidence |
|---|---|---|---|
| `item/harbinger/morphlings/ChitiniteMorphlingItem`, `PestsMorphlingItem`, `TickMorphlingItem`, `UrchinMorphlingItem` | 756 total | **Verified** | Never instantiated. The ids are redirected in `MorphlingMigrationRules.java:27-30,52-58` (`morphling_pests → morphling_gravecap`, etc.). There is no `ItemInit` registration. |
| `entity/projectile/TrackingPestsEntity` | 210 | **Verified orphan** (type still registered) | Only constructed by the dead `PestsMorphlingItem`. Keep `EntityInit.tracking_pests` for saves; the class could be collapsed per P2. |
| `item/harbinger/tool/living/LivingGraspItem` | 151 | **Verified** unregistered | No registration. It is the only constructor of `TrackingBloodOrbEntity`, which is therefore an orphan too. |
| `entity/projectile/TrackingSerpentEntity` | 228 | **Verified orphan** | No `new TrackingSerpentEntity(` anywhere. Reachable only via the EntityType factory (`/summon`). |
| `item/shared/GuideBookTooltipHelper` | 66 | **Verified** | Zero references; not a subscriber. |
| `item/harbinger/bloodline/BloodlinePoolMonitorItem`, `memory/WitnessOrganItem`, `tool/BloodTendencyGaugeItem`, `tool/DSDItem`, `shared/FieldNotesItem`, `shared/FieldNotesBookItem`, `unstained/VerdigrisCenserItem`, `itemhandler/CharmItemHandler` | ~700 | **Likely** | No Java reference outside their own file. Lang/model JSON for `blood_tendency_gauge`, `bloodline_pool_monitor`, `field_notes` and `verdigris_censer` still exist, so these look like removed registrations that left classes and assets behind. Confirm against git history before deleting. |
| `entity/boss/saint/EnumRejectionEvent` | 19 | **Verified** | Zero references. |
| `capability/.../routing/IBloodRoutingTarget` | 23 | **Verified** (0 implementers) | §4 |
| `sporePuff(...)` method in `ChitiniteEntity`, `FerventChitiniteEntity`, `ChthonianEntity`, `ChthonianQueenEntity`, `FunglingEntity` | ~5 × 20 | **Verified** | Never called. |
| `TendencyViewMenu` private ctor `(MenuType, int, Inventory, ItemStack)` | | **Verified** | Unused. |
| `EndgameBossCombatRules`, `MnemonicBlueprintRules` | | Not dead | Referenced only from `src/test`. They are tested pure rules; keep them, but check whether production code ever calls them (HYPOTHESIS: test-only logic). |
| Stray backups `CardinalFocusBlock.java~`, `LivingSickleItem.java~`, `StillwaterCondenserMenu.java~`, `PlaceStructurePacket.java~` | | **Verified** | Untracked and gitignored (`.gitignore:76`). Local clutter only. |

The Pale Lady is not an entity. She exists only in dialogue and discovery (`OurLadyWhisper*`, `MemoDefinitions`). Erythromycelium Eruptus and Abhorrent Thought are ordinary `Monster` mobs without boss bars.

---

## 7. Hotspot files (size × churn since 2026-06)

1. `entity/npc/dialogue/DialogueEventHandler.java`: 1,561 lines, 40 commits (P8).
2. `entity/boss/endgame/VesperPhaseTwoCombat.java` (1,333) and `VesperTheCrownedRefusalEntity.java` (1,281).
3. `entity/boss/annetta/AnnettaKnowlesEntity.java` (1,244), `NaeglerophaeonEntity.java` (1,126), `StainedPriestessEntity.java` (864).
4. `entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java` (911, 21 commits) and `HarbingerVicarDialogueTrees.java` (736). These are data in code; fine as static factories.
5. `entity/mob/monster/will/WillEntity.java` (784), `npc/DrudgeEntity.java` (678), `summon/BloodThrallEntity.java` (542).
6. `item/harbinger/tool/MarionetteCrossbarItem.java` (637), `morphlings/MorphlingItem.java` (573, 30 `CUSTOM_DATA` refs), `tool/living/LivingCrossbowItem.java` (563), `tool/DebugShowcaseItem.java` (539; a debug item in the content tree).
7. `block/harbinger/puzzle/SaintSarcophagusBlock.java` (695), `crafting/ArmatureGeometry.java` (726) and `AlembicGeometry.java` (672), `unstained/ViridSalisTrailBlock.java` (544), `rite/AshTrailBlock.java` (527).

---

## 8. Observed defects and risks (report only; nothing changed)

| # | Finding | Status |
|---|---|---|
| D1 | `common/entity/mob/monster/will/WillEntity.java:575` calls `WillAbsorptionGlowParticleFactory.createPulseData(...)` on the **server** (`ServerLevel.sendParticles`). That class `implements net.minecraft.client.particle.ParticleProvider` (`client/particle/factory/WillAbsorptionGlowParticleFactory.java:13`). Loading it on a dedicated server needs a client-only interface. | Code VERIFIED. **Crash on a dedicated server when a Will's absorption completes is a HYPOTHESIS (high confidence), not run.** Fix: move `createPulseData` into the data class or a common helper. |
| D2 | `common/block/harbinger/functional/DendriticDistributorBlock.java:112` and `SanguineConduitBlock.java:120,133` call client `Screen.openScreen` directly from common block code (guarded by `isClientSide`). `EnzymaticScriptoriumBlock.java:112`, `ScarletMummerEntity.java:133`, `BloodBindingEffect` and 22 common files import `client.particle.data.*`. Those are server-safe particle-option classes that live in the wrong package. In total 161 common files import `client.*`; 69 of them are in `common/item`, mostly through `HemoClientItemExtensionsProvider` (57). | VERIFIED structure. Runtime-safe only through lazy loading. Recommend moving `client/particle/data` and `type` to `common/particle`, and routing screen opens through a client proxy or packet. |
| D3 | `common/init/EntityInit.java:674-677`: `chthonian_queen` is built with type name `rloc("chthonian")`, a copy-paste error. The `build()` string feeds the datafixer type name. | VERIFIED. Low impact. Changing the string does not change the registry id. |
| D4 | `sporePuff` (5 copies, e.g. `ChthonianEntity.java:223-230`) casts every nearby `Entity` to `LivingEntity` (ClassCastException on items or arrows) and applies Poison amplifier 200. It is dead today (§6); it would be a crash if revived. | VERIFIED. |
| D5 | `HemolysisEffect` is registered `MobEffectCategory.NEUTRAL` (`EffectInit.java:99`) but overrides `isBeneficial()` → `true` (`HemolysisEffect.java:63`). | VERIFIED inconsistency. Intent unknown. |
| D6 | Hard-coded English: 4 effect `getDisplayName()` overrides return `Component.literal`; Saint `broadcastMessage` uses literals (`PutricielEntity.java:345`, and 15 `Component.literal` across 9 boss files); 6 fungal-scar tooltips; `TendencyViewMenuProvider` and similar titles; 325 `Component.literal` uses in 87 `common/item` files. | VERIFIED counts. Some are debug items. |
| D7 | Public non-final statics: `TYPE = EntityInit.x.get()` in 4 projectiles (`TrackingPestsEntity.java:31`, `TrackingBloodOrbEntity.java:33`, `TrackingSerpentEntity.java:33`, `DirectedBloodOrbEntity.java:23`). It works only because the class initialises after registration. Also: `AntiphonomycesResonansItem.IS_ECHO_CAST` (a reentrancy flag written by `CrawlingChoirHandler.java:38-42`); `HemoEntityPredicates.PREDICATES/BLOODTYPES` (mutable lists); 9 mutable `TAG_*` string "constants" in items; 7 `RenderProp* INSTANCE` fields; review-only hooks `AnnettaKnowlesEntity.reviewHoldClock` and `MycophantEntity.reviewHoldPose` in production entities. In total there are 44 non-final static fields in content packages. | VERIFIED. Make these `final` or move them. Keep the review hooks if the review clients need them, but isolate them. |
| D8 | 24 `static final` mutable maps or lists in content classes keyed by `Player`/`UUID`/`ServerPlayer` (e.g. `item/shared/ScaleGripEvents.java:26 PENDING_RETURNS`, `item/harbinger/tool/living/LivingAxeLodestoneFields.java:43-44`, `effect/ChummedWatersAreaManager.java:22`, `effect/LanternTickLightManager.java:26`, `item/harbinger/MicroscopeExamination.java:27`). In single-player these survive world switches unless they are cleared on server stop. `ScaleGripEvents.PENDING_RETURNS` holds `ItemStack`s in memory only. | Existence VERIFIED. Leak or item loss on restart is a HYPOTHESIS; each needs a lifecycle check. |
| D9 | `BloodManipulation.java:89` reaches `compat.mna.spell.ManipComboHelper` by string reflection, although `compat/mna/**` is excluded from the build (AGENTS.md). | VERIFIED. Inert but dead. |
| D10 | Capability layer depends on content: `common.capability` imports `common.item` (28 files) and `common.entity` (7). There are package cycles item↔capability, entity↔manipulation and block↔tile. | VERIFIED import graph. Typical for MC mods; only worth fixing where a capability imports a concrete item class for `instanceof`. |
| D11 | `ProfessionalHarbingerEntity`, the base of 5 NPCs in `entity/npc/harbinger`, lives in `common/succession/`. It adds `MeleeAttackGoal` in its constructor, outside `registerGoals`, to every professional, even non-successors. | VERIFIED placement. Behavioural impact unknown (HYPOTHESIS: harmless because of `hurt()` gating and no target). |

---

### Priority order (behaviour-neutral first)
1. Delete or quarantine the verified dead items and helpers (§6) and fix D1.
2. Introduce the P9 `HemoTickers`, P5 `BossBarBinding`, P3 `SyncedOwner` and P4 NPC helpers (all Low difficulty).
3. Collapse the P1, P2 and P11 duplicate pairs/triplets behind thin subclasses that keep their ids.
4. Split `DialogueEventHandler` (P8).
5. Add the `HemoContainerMenu` base (P17) and move BEWLR wiring to the client (P15).
6. Plan the typed-component migration (P14) with save-compat readers.
