# Half-baked refactors and dead Java code (audit, 2026-11-02)

## Summary

Read-only audit of `src/main/java` (3,980 compiled files, plus the 37 excluded `compat/mna` and `compat/curios` files),
`src/test`, `src/gameTest`, resources and `tools/`. No build or test was run. There are **34 findings: 1 High, 11 Medium,
22 Low.** The size of the dead code:

- **49 top-level classes (4,315 lines) that nothing reaches.** About 38 of them are already waiting on decisions
  M-2 and M-3. The new ones are 678 lines of 1.19-era math copies that shadow JOML, a duplicate enum, and five classes
  the step-3 audit marked SAFE that never reached its table.
- **84 dead private methods (about 1,805 lines) in 31 files.** Three files hold 1,522 of those lines.
- **About 280 methods and constants nothing references**, plus about 300 that only tests or tools name.
- **5 registered entity types that nothing spawns, 2 effects that nothing applies, and 1 feature type that no data
  uses.**
- **A save-compatibility layer for a mod that has never shipped:** 11 player migrations (none past its sunset), about
  a dozen alias and legacy read paths, and 15 retired manipulations with 20 retired memory items still registered.
- **The excluded `compat/mna` package no longer compiles against the MnA jar in `libs/`:** 22 of its 62 MnA imports
  are missing from it.

The High finding is the git index. It holds two staged files whose public class names do not match their file
names. A plain `git commit` would commit them and break a clean compile.

**Method.** I tokenized every text file under `src/`, `tools/`, `gradle/`, `docs/` and `wiki/` and walked a reference
graph from the class roots: `@Mod`, `@EventBusSubscriber`, `@Mixin`, `@JeiPlugin`, `@GameTestHolder`, and fully
qualified names in resources and mixin configs. Comments and strings were stripped, and an edge from another package
counted only with a matching import. I then checked each finding by hand with grep and by reading the code. Pending
decisions are cited by their ledger id (`docs/architecture/CONSOLIDATION_PROGRESS.md` *Decisions*) or their
`docs/OPEN_QUESTIONS.md` id, and are not argued again.

Paths are relative to `src/main/java/com/vincenthuto/hemomancy/` unless they start with `src/`, `docs/`, `tools/` or
another repository-root folder.

---

## 1. Dead classes and members

### RF-1 · Low · Dead 1.19-era math copies shadow the JOML class names
- **Claim:** `client/render/tile/Quaternion.java` (261 lines), `Vector3f.java` (252) and `Vector4f.java` (165) are
  copies of the old Mojang math classes. Nothing imports them. Every `Vector3f`/`Quaternion` user imports
  `org.joml`.
- **Evidence:** no `import com.vincenthuto.hemomancy.client.render.tile.(Vector3f|Vector4f|Quaternion)` exists in
  `src/` or `tools/`. The other files in the package (`RadiantPortalRendertype`, `RetexturedBakedModel`,
  `SectionedBakedModel`) don't use the names. The step-3 audit missed them because the name-based scan matched the
  JOML users. An IDE auto-import can pick these copies by mistake.
- **Confidence:** Verified. **Action:** delete. **Effort:** S.

### RF-2 · Low · Duplicate `EnumCorpusState`
- **Claim:** `common/entity/boss/saint/EnumCorpusState.java` is an unused byte-for-byte copy (only its Javadoc
  differs) of `common/tile/harbinger/puzzle/EnumCorpusState.java`, which every user imports.
- **Evidence:** the importers `common/block/harbinger/puzzle/SaintSarcophagusBlock.java:21`,
  `src/gameTest/.../SaintEncounterGameTests.java:21` and `SarcophagusClientReview.java:6` all use the `puzzle`
  copy. Nothing in `common/entity/boss/saint/` names the type.
- **Confidence:** Verified. **Action:** delete the `boss/saint` copy. **Effort:** S.

### RF-3 · Low · Audit-SAFE items that never reached the audit table or a decision
- **Claim:** `docs/architecture/evidence/07-naming-deadcode.md:312,315` lists these as SAFE. The audit's SAFE table
  (`docs/architecture/ARCHITECTURE_AUDIT.md:421`) carried only nine names, so step 3 never removed the rest and no
  decision row covers them:
  - `common/item/itemhandler/CharmItemHandler.java` (179 lines).
  - `common/capability/player/harbinger/bloodvolume/routing/BloodlineSource.java`, `EquippedGourdSource.java` and
    `LinkedPlayerSource.java` (109 lines). These are three of the four `IBloodSourceContract` implementations; only
    `ThrallCourierSource` is used (`common/entity/summon/BloodThrallEntity.java:520`).
  - `src/main/resources/data/forge/tags/fluids/test_fluid.json`. It has since been pinned as an exemption
    (`src/test/.../architecture/DataFolderNamingGuardrailTest.java:27`).
  - `client/screen/tile/crafting/scar/ScreenScarPattern.java` (143 lines). Its only referrer,
    `ScreenScarBinderViewer`, is gone, so this item no longer needs a "verify" step.
- **Confidence:** Verified. **Action:** delete, and drop the exemption. **Effort:** S.

### RF-4 · Medium · The M-2 and M-3 dead classes are still present, and the save reason no longer applies
- **Claim:** these are tracked by the pending M-2 and M-3 decisions. Current state:
  - **M-2 (about 1,070 lines):** the five unregistered mobs `AbyssalSiphon`, `CruorFiend`, `FrozenClot`,
    `SynapseHound` and `VoidDrinker`, each with an entity, model and renderer under
    `common/entity/mob/monster/`, `client/model/entity/mob/monster/` and `client/render/entity/mob/monster/`.
    Their sounds are still registered (`common/init/SoundInit.java:268-279` and following) and they have 26 lang keys.
  - **M-3 items (661 lines):** `LivingGraspItem`, `DSDItem`, `BloodTendencyGaugeItem`, `BloodlinePoolMonitorItem`,
    `WitnessOrganItem`, `VerdigrisCenserItem`, `FieldNotesItem` and `FieldNotesBookItem`. `ItemInit` carries
    tombstone comments for two of them ("Blood Tendency Gauge -- now part of sanguine conduit", "Bloodline Pool
    Monitor - Now part of the bloodwell").
  - **M-3 test-only classes (737 lines):** `EndgameBossCombatRules`, `MnemonicBlueprintRules`,
    `CardinalRiteStructureConsumptionRules`, `VialCentrifugeStartupRules`, `RecipeMapQuery`,
    `VesperEveningStarMotion`, `VesperWeaponAnimationRules`, and `ManipulationTraceLayerCache` with
    `TendencyTraceStyle`.
  - **M-3 shims and stubs (59 lines):** `VirtualFieldNotesButton`, `InventoryVirtualFieldNotesEvents`,
    `BlankChamberThemeEffects`, `MobManipCaster` and `SpentTempleInitiation`. The last is a constant `false`
    (`common/mission/hermit/SpentTempleInitiation.java:7`).
- **Evidence:** the reachability walk. Each class has zero main references, and the test-only classes have one test
  each. The audit's grounds for keeping them ("may be planned content", "saves") are void under the not-live ruling
  (the memory note *Mod Not Live*; `docs/archive/tracks/brined_court/01-strain-table-and-replacement.md`,
  "Decided 2026-10-29: no migration").
- **Confidence:** Verified. **Action:** decide M-2/M-3, then delete with their tests. **Effort:** M.

### RF-5 · Medium · Five registered entity types that nothing spawns
- **Claim:** these are registered, get renderers and appear in the frozen registry ids, but no code or data creates
  them:
  - `dark_arrow` (`common/init/EntityInit.java:1016`; `DarkArrowEntity`, 108 lines).
  - `wretched_will` (`EntityInit.java:1010`, attributes at `:1216`; `EntityWretchedWill`, `WretchedWillModel`,
    `WretchedWillRenderer`, 180 lines).
  - `tracking_snake` (`:913`) and `tracking_pests` (`:932`). The audit already said their classes are never
    constructed.
  - `tracking_blood_orb` (`:891`). Its only spawner is the dead `LivingGraspItem`
    (`common/item/harbinger/tool/living/LivingGraspItem.java:94,113,127`).
  - Renderers are registered at `client/registry/EntityRendererRegistrar.java:81-83,102,245`.
  - `TrackingBloodOrbEntity.TYPE = EntityInit.tracking_blood_orb.get()`
    (`common/entity/projectile/TrackingBloodOrbEntity.java:15`) is a static-init `.get()` that would throw if the
    class loaded before registration.
- **Evidence:** I checked every `EntityInit` field for references outside its own class and the renderer registrar,
  for `new <Class>(` calls, and for its id in data JSON. These five are the only ones with none of the three. The
  audit's "keep the types registered for saved worlds" (`ARCHITECTURE_AUDIT.md:438,478`) no longer holds.
- **Confidence:** Verified. **Action:** decide (with M-3), then delete, updating
  `src/gameTest/resources/frozen/registry-ids.txt`. **Effort:** S.

### RF-6 · Low · A registered feature type that no configured feature uses
- **Claim:** the `small_fungus` feature (`common/init/BaseFeatureInit.java:44`), with
  `common/worldgen/feature/SmallInfectedMushroomFeature.java` and `.../config/SmallInfectedMushroomConfig.java`
  (55 lines), is named by no `worldgen/configured_feature` JSON. The live small fungus,
  `hemomancy:small_infected_fungus` (`src/test/.../FungalGardensFeatureOrderTest.java:22`), uses another type.
- **Confidence:** Verified. **Action:** delete. **Effort:** S.

### RF-7 · Medium · 84 dead private methods (about 1,805 lines) in 31 files
- **Claim:** these private methods are never called, directly or through other private methods. The largest cases:
  - `client/render/world/QliphothBloomRenderer.java`: **880 of its 1,849 lines.** The whole procedural tree path is
    dead: `drawTrunk:768`, `computeRootBulge:870`, `drawTrunkOrbs:903`, `drawRoots:1044`, `drawSingleRoot:1122`,
    `drawBranches:1232`, `drawCanopy:1472`, `drawApexOrb:1573`, `tiltAndRotateX/Y/Z:1688-1705`,
    `trunkHeightFrac/rootLengthFrac/branchLengthFrac:156-174`. The public `renderRings:225` has no caller. The class
    Javadoc (`:30-38`) still describes the tree.
  - `client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java`: 413 lines.
    `renderLowtideRetiredPanelRuinCluster:1030` and its foundation, tower, dome, arch, spire and reflection helpers
    (`:1118-1330`) are dead. The public `lowtideRuinTint` has no caller. The live path is
    `renderLowtideRuinedStructures:654`.
  - `client/screen/overlay/BloodVolumeOverlay.java`: 230 lines. The old HUD ornaments are dead:
    `renderDegreeOrnament:445`, `renderCrownVessel:496`, `renderPomeCrown:591` and `renderApotheosHalo:623`, plus
    `drawSidePlate`, `drawBand`, `drawCrownArc`, `renderEllipse`, `drawDiamond`, `drawLine`, `blendColor` and
    `multiplyColor`.
  - `common/recipe/serializer/CardinalRiteRecipeSerializer.java:41-125`: the old block-pattern JSON helpers
    `blockFromJson`, `blockPredFromHash`, `generateBlockPatternFromArray`, `keyEntriesFromJson`,
    `displayKeyMapFromKeyEntries` and `patternFromJson`. `BloodStructureRecipeSerializer.java:37`
    (`blockFromJson`) is the same.
  - Smaller cases: `client/model/entity/boss/endgame/VesperTheEveningStarModel.java:521` (`applyDefeatPose`, 30
    lines) and `client/screen/tile/functional/SporeImplantScreen.java:295` (`renderFungalSlotGlow`, 28 lines).
  - Copy-paste dead helpers: `hasClarityUnlocked`/`isPurifying` in `common/entity/npc/harbinger/HarbingerArtificerEntity.java:90,95`
    and `HarbingerCicatrixAnchoriteEntity.java:90,95`; `hasBloodline` in `HarbingerAlchemistEntity.java:202`,
    `HarbingerMnemonistEntity.java:140` and `HarbingerVicarEntity.java:112`; `isEquipped` in
    `common/item/harbinger/armor/AbstractSpecialBloodLustArmorItem.java:52` and `BloodLustArmorItem.java:66`.
- **Evidence:** I built a per-file call graph from the non-private methods and field initialisers, excluding
  private constructors. The three largest files above were checked by hand.
- **Confidence:** Verified. **Action:** delete. The compiler proves it, and no visual check is needed. **Effort:** S.

### RF-8 · Low · About 280 non-private methods and constants that nothing references
- **Claim:** these are public, protected or package methods and constants whose name appears nowhere else in main
  code, tests, data or tools. Appendix A lists them all. Notable groups:
  - Six `CardinalRiteRecipe` setters.
  - `BloodManipulation.setAlignLevel`, `setRank`, `setType`, `setXpCost` and `getXpCost`.
  - Six `Bloodline` setters and getters.
  - `FoundingFaneSavedData`: `canPlaceBloodwell`, `getAllFanes`, `getFaneCenter` and `removeHeart`.
  - Four `ScarDefinition` passive getters and setters.
  - Four `SomaticLoomBlockEntity` getters.
  - `HemoCapabilityAccess.getScarItem`.
  - `common/worldgen/feature/FeatureLogic.java`: 8 of its 11 members are unused. Only `placeIfReplaceable`,
    `placeIfSubmerged` and `isWaterSource` have callers, so the M-3 note that it is "in use again" covers about a
    quarter of the class.
  - `BlockInit.getAllBlockEntries` (`common/init/BlockInit.java:1321`) serves only the excluded `MnAPluginBlockInit`.
- **Confidence:** Suspected for the list as a whole: it is a token scan, and same-named members can hide entries but
  not create false ones. The spot checks above are Verified. **Action:** delete in a sweep. **Effort:** M.

---

## 2. Git working tree

### RF-9 · High · Two staged files would break a clean compile on the next commit
- **Claim:** `git status` shows `AD` for `common/item/harbinger/morphlings/MorphlingCamouflageRules.java` and
  `MorphlingColdResilienceRules.java`. The index blobs (`b5a67559`, `d09865a8`) were never in any commit, and they
  declare `public final class LumenlaceCamouflageRules` and `public final class WinterShroudResilienceRules`. A
  plain `git commit` (without `-a`) would commit them, and javac rejects a public class in a file of another name.
- **Evidence:** `git show :<path>` shows the class names. `git log --all` on the old names ends at `62f5b38da`.
  Brined Court Track 1 kept the helpers under neutral names, then Track 2 removed them
  (`docs/archive/tracks/brined_court/01-strain-table-and-replacement.md:114`, `02-strain-powers.md:96`).
  `src/`, `tools/` and `gradle/` contain neither the new names nor the old ones. `docs-and-test-drift.md` in this
  audit set notes the same index state.
- **Confidence:** Verified. **Action:** drop the index entries with `git restore --staged` on both paths. There is
  no working-tree change, and nothing needs restoring. **Effort:** S.

---

## 3. Parallel implementations and state nobody reads

### RF-10 · Medium · Rule helpers are tested, but gameplay runs its own copy
- **Claim:** several `*Rules` methods have unit tests while the live code inlines a different implementation, so
  the tests cover numbers the game never uses:
  - `common/damage/SchoolStateRules.java:26` `healingMultiplier` (0.75 / 0.25) is test-only. The live heal cut is
    `common/effect/HealModifier.java:17-22` (`NECROSIS`, `INSATIABLE_HUNGER`, `HEMOPHAGY`).
  - `SchoolStateRules.java:18` `rimeSlow` (0.15 per level) is test-only. The live slow is the attribute modifier on
    `rime` (`common/init/EffectInit.java:267-270`).
  - `common/capability/player/harbinger/possession/PossessionRules.java:56` `mayBegin` is test-only. The live gate is
    inlined in `PossessionService.refusal` (`PossessionService.java:112-118`).
  - `common/item/harbinger/morphlings/MorphlingHeatRules.java:19-40`: `benefit`, `exhaustionMultiplier(int)` and
    `incomingDamageMultiplier` are Emberfang leftovers. Only `environmentLevel` and `HOT` are used
    (`MorphlingHomeConditions.java:140-142`).
  - Constant stubs that only tests call: `VesperFlammeusBreathRules.java:23` returns `0.0D`,
    `SanguineHoundRules.java:19` returns `0`, `VasculariumCharmRules.java:11` returns `false`, and
    `ChamberVisitRules.java:35` (`canUseArbor`) returns `true`.
  - Other test-only rule methods: `PuppeteerSummonRules.canUnlockAtDegree`, `QliphothPomeRules.shouldResetProgressOnPrune`,
    `LivingFlailRules.slownessAmplifier`, `IronheartedRules.addIronHeartHealth`, `TendencyAffinityRules.adjustManipulationDamage`,
    `MorphlingHungerRules.capMaturityByHusbandry` (see RF-26), `VesperMountAttackRules.carapaceDamageMultiplier`/`mayStartAneurysm`,
    `TriadAttributeCaps.clampMoveSpeed`, the nine `MorphicNectarMutationRules.primal*` methods, and ten
    `HarbingerAdvancementGranter.isArtificer*Ready` readers (`common/event/HarbingerAdvancementGranter.java:419-503`).
    Appendix B lists all of them.
- **Confidence:** Verified for the first four bullets; Suspected for the rest of Appendix B (no inline twin was
  looked for). **Action:** merge each twin (make gameplay call the rule) or delete the rule and its test. **Effort:** M.

### RF-11 · Low · Two item-inquiry paths; the old one survives only for tests
- **Claim:** the held-item inquiry trees are uncalled:
  - `GuardianDialogueTrees.forItem` (`common/entity/npc/dialogue/GuardianDialogueTrees.java:50`).
  - `itemInquiry` in Alchemist (`HarbingerAlchemistDialogueTrees.java:863`), Mnemonist (`:418`), Vicar (`:659`),
    VotaryWayfarer (`:47`), Voyager (`:46`) and Zealot (`:292`).
  - Only `SanguineMonolithDialogueTrees.itemInquiry` is live (`common/block/harbinger/functional/SanguineMonolithBlock.java:223`).

  These are the only callers of the `(degree, purity)` overloads (`DialogueItemInquiryNodes.java:47-50,152-155`,
  `inquiry/ItemInquiryRegistry.java:25`, `ItemInquiryCondition.java:42`, `ItemInquiryEntry.java:16`). The
  overloads go through `ItemInquiryContext.legacy` (`inquiry/ItemInquiryContext.java:20-22`), which guesses
  `activeBlood = degree > 0` and `apotheos = degree >= 8`, so a D8 Silentium resolves as an Apotheos. Separately, the
  15-caller `withInventoryItemInquiries(tree, player, speakerKey, degree, purity)` (`DialogueItemInquiryNodes.java:21-28`)
  ignores its `degree` and `purity` and reads `ItemInquiryContext.from(player)` instead.
- **Confidence:** Verified. **Action:** delete the dead trees, the `legacy` factory and the overloads; drop the
  vestigial parameters. **Effort:** S.

### RF-12 · Medium · The field-notes removal is half done
- **Claim:** the physical field-notes item is unregistered (M-3), but the code around it is still live:
  - `MemoHelper.migrateLegacyFieldNotesStack` is a constant `return false` (`common/capability/player/shared/knowledge/discovery/MemoHelper.java:355-357`).
    Even so, `migrateLegacyFieldNotes` (`:212`) walks the whole inventory on every memo capture and dictation
    (`:63,84,95`) and runs as migration 4 (`capability/player/shared/migration/PlayerDataMigrations.java:50`).
  - `refillFieldNotes`, `getInkPath`, `isInk`, `getRemainingMemos` and `getMemoCount` serve only the dead
    `FieldNotesItem`.
  - `dictateToLiber` (with an unused `notes` parameter), `canDictateInto`, `getLegacyKnownMemos` and
    `getLegacyUnlockedLiberEntries` have no callers.
  - The deprecated `DictationTableBlockEntity.getLiber`/`setLiber`/`removeLiber` (`common/tile/inscription/DictationTableBlockEntity.java:12-14`)
    have no callers. F-22 covers only the Dictation Table's creative-tab visibility.
- **Confidence:** Verified. **Action:** finish the removal with M-3: delete the items, the helpers and the no-op scan,
  and drop the field-notes half of migration 4. **Effort:** M.

### RF-13 · Low · `BloodBoltEntity` carries vanilla tipped-arrow code that never runs, and its save keys don't match
- **Claim:**
  - `setPotionEffect` acts only on `TIPPED_ARROW` or `ARROW` (`common/entity/projectile/BloodBoltEntity.java:333-354`),
    but its only caller passes the blood-bolt ammo stack (`common/item/harbinger/tool/living/BloodBoltItem.java:19-20`).
    So the potion, custom-effect, colour and `ENTITY_EFFECT` particle code never activates.
  - The save writes `"CustomPotionEffects"` (`:121`) but the load reads `"custom_potion_effects"` (`:299`).
  - The load reads `"Potion"` (`:294`), which nothing writes.
- **Confidence:** Verified. **Action:** delete the potion path. **Effort:** S.

### RF-14 · Low · Flags and fields written but never read
- **Claim:**
  - **Covered by D-7:** `mycophant_heir` (`common/encounter/MycophantEncounterManager.java:70,244`).
  - **Also never read:**
    - `WorkingCraftRites` writes `"Side"` (`common/rite/harbinger/WorkingCraftRites.java:72`).
    - `RecallBarbEntity` writes `"RecallBarbTransient"` (`common/entity/projectile/RecallBarbEntity.java:125`); the
      load ignores it.
    - `TemporaryCoagulumEntity` saves `"Expiry"`, but its load sets `expires = 0`
      (`common/entity/boss/phlegethosoma/TemporaryCoagulumEntity.java:34-35`).
    - `DesiccantModel.telsonStingSwell` is computed and never used (`client/model/entity/mob/arthropod/DesiccantModel.java:39,171,176`).
    - `MycophantModel.thoraxBaseY/Z` are captured and never used (`client/model/entity/boss/endgame/MycophantModel.java:2459-2474`).
    - `MasonsEffigyScreen.prepareButton` is stored and never read (`:63,78`).
  - **Started but never read by a model (the M-3 animation-state family, now three):**
    `PutricielEntity.absolvedAnimationState` (`common/entity/boss/saint/putriciel/PutricielEntity.java:104`),
    `VelorumEntity.martyrdomAnimationState` (`.../velorum/VelorumEntity.java:105,219`) and
    `ChalybeateSnailEntity.crawlAnimationState` (`common/entity/mob/aquatic/ChalybeateSnailEntity.java:50`).
  - No synced `EntityDataAccessor` is written without being read (0 found).
- **Confidence:** Verified. **Action:** delete the writes and fields. **Effort:** S.

### RF-15 · Low · The "legacy" construct lifecycle survives for one dead entity and one live one
- **Claim:** `BloodConstructEntity` defaults `usesLegacyConstructLifecycle()` and `usesLegacyConstructParticles()`
  to `true` (`common/entity/summon/BloodConstructEntity.java:143-145`). Its tick sprays vanilla `SQUID_INK`,
  `REDSTONE` and `ASH` particles and kills the construct at tick 120 (`:111-140`).
  - `CloudEntityBlood` turns the particles off (`common/entity/projectile/CloudEntityBlood.java:141`).
  - `FerricConstructEntity` turns the lifecycle off for player constructs (`common/entity/summon/FerricConstructEntity.java:78`).
  - The only subclass that keeps both is the dead `EntityWretchedWill` (RF-5).
- **Confidence:** Verified. **Action:** finish: turn the 120-tick expiry into a named lifetime for the users that
  want it, and delete the legacy particles with RF-5. **Effort:** S.

### RF-16 · Low · The Muscle Memory migration also runs on every read
- **Claim:** `MuscleMemoryState.getPrimed` and `isPrimed` call `migrateLegacyPriming` (`common/capability/player/harbinger/musclememory/MuscleMemoryState.java:118,125`)
  on top of migration 3 (`PlayerDataMigrations.java:47-48`). That is a second path for one migration, which the
  standards forbid ("never run a migration from its own login, respawn or tick handler").
  `isPrimed` and `learnAndPrime` (`:32`) have no callers.
- **Confidence:** Verified. **Action:** delete with RF-23. **Effort:** S.

---

## 4. TODOs, stubs and swallowed failures

### RF-17 · Low · Placeholder art and stale "for now" markers
- **Claim:** placeholders and stale markers still in the code:
  - **`TODO: Replace with actual … geometry`** in `client/model/armor/OpenCurvedHornModel.java:25` and
    `OpenBloodGourdModel.java:24`. Each "open" model is its closed model minus the stopper box.
  - **"Same model or texture … for now"** in `client/model/tile/SuspendedCleansedBloodCrystalModel.java:24`,
    `client/render/item/tile/unstained/SuspendedCleansedBloodCrystalItemRenderer.java:21` and
    `client/render/tile/unstained/SuspendedCleansedBloodCrystalRenderer.java:24`.
  - **A placeholder appendage "until Track 3"** in `client/model/entity/summon/MorphlingPolypLayerModel.java:24`.
  - **Stale comments:** "Temporary boss testing catalysts" (`common/init/ItemInit.java:400`; the `saint_relic_*`
    items are now used by data) and the two tombstones in RF-4.
  - **A lossy encode in `BloodStructureRecipeSerializer.encode`:** it skips `pattern` and `key` "for now"
    (`common/recipe/serializer/BloodStructureRecipeSerializer.java:182`). This is part of the ledger's "seven
    bridged recipes not yet `RecordCodecBuilder` codecs".
  - **Only three `TODO` comments remain in main code** (plus one in dormant compat), and there is no `FIXME`,
    `XXX` or `HACK`.
- **Confidence:** Verified. **Action:** finish the art, or delete the markers. **Effort:** S per item.

### RF-18 · Medium · Two empty catches hide always-failing MnA calls in core code
- **Claim:**
  - `ManipulationCostLedger` calls `HemoMnAConfig.ARCANE_RESONANCE_BLOOD_REDUCTION.get()` and swallows
    `Exception` (`common/capability/player/harbinger/manip/ManipulationCostLedger.java:65-71`). `HemoMnAConfig.register()`
    (`config/HemoMnAConfig.java:39`) is never called, so the value is null whenever the branch runs. That needs MnA
    loaded plus the `arcane_resonance` effect, which only the excluded compat applies. The pending item for this is F-25.
  - `BloodManipulation.invokeMnAComboHelper` reflectively loads `compat.mna.spell.ManipComboHelper` and swallows
    `Exception` (`common/manipulation/BloodManipulation.java:93,343-350`, called at `:626`). The class is excluded
    from compilation, so the call always fails when MnA is loaded.
  - The other 33 empty catches are deliberate parse or registry fallbacks (see *Not a problem*).
- **Confidence:** Verified. **Action:** delete with RF-20 (this supersedes F-25). **Effort:** S.

### RF-19 · Low · Deprecated members nothing calls
- **Claim:** `RitesTabView.minDegree` (`client/screen/skilltree/shared/RitesTabView.java:66`) and the four-argument
  `measureInfoPanelHeight` overload (`:1010-1016`) have no callers, and neither do the three `DictationTableBlockEntity`
  wrappers (RF-12). The other `@Deprecated` tags are vanilla overrides or M-3 shims.
- **Confidence:** Verified. **Action:** delete. **Effort:** S.

---

## 5. Dormant pieces

### RF-20 · Medium · `compat/mna` has rotted: it would not compile against today's jar and would fail guardrails if re-enabled
- **Claim:** 22 of the 62 distinct `com.mna.*` imports in `compat/mna/**` do not exist in
  `libs/mna-3.1.11-neoforge.1.21.1.0.jar`. Examples: `com.mna.capabilities.playerdata.magic.PlayerMagicProvider`,
  `...progression.PlayerProgressionProvider`, `com.mna.items.ItemInit`, `com.mna.api.particles.ParticleInit`,
  `com.mna.network.ServerMessageDispatcher` and `com.mna.items.armor.DyeableMageArmor`. The code also breaks current
  rules:
  - **Forge-era `getCapability(...).orElse(null)` calls:** `compat/mna/item/BloodShotOcculusItem.java:35`,
    `compat/mna/spell/BloodTitheHandler.java:49,96` and `ComponentBloodToMana.java:77`.
  - **Direct player-volume `drain(...)`, which `BloodAccountingGuardrailTest` forbids:** `compat/mna/ritual/RitualEffectBloodMote.java:52`,
    `BloodTitheHandler.java:112` and `ComponentBloodToMana.java:97`.
  - **`Component.literal` English:** for example `RitualEffectBloodMote.java:79-82` and
    `SanguineTransfusionManip.java:46,60`.

  The package still costs the project:
  - 37 files and 3,367 lines in `compat/mna` and `compat/curios`.
  - `config/HemoMnAConfig.java` (141 lines, never registered).
  - The empty stub `mixin/core/MixinHUDOverlayRenderer.java`, which has no `@Mixin`, and
    `src/main/resources/hemomancy.mna.mixins.json`, which names it and ships in the jar unlisted.
  - 69 MnA resource files (`assets/hemomancy/mna_guide/`, `textures/block/mna/`, `textures/item/mna/` and others).
  - The MnA-only effects `arcane_resonance` and `sanguine_clarity` (`common/init/EffectInit.java:230,233`).
  - The two swallowed calls in RF-18, `BlockInit.getAllBlockEntries`, and the `data/mna` and `data/curios` tags
    (RF-29).
  - Dormant-compat exemptions in 12 guardrail tests, among them `HardCodedEnglishGuardrailTest`,
    `EnglishLiteralAllowListTest`, `BloodAccountingGuardrailTest`, `CombatTargetingGuardrailTest` and
    `RegistrationStyleGuardrailTest`.
  - GeckoLib, Curios and the MnA jar stay `implementation` dependencies only for this code (`build.gradle:226-230,244`).
    Nothing outside `compat/mna` imports `software.bernie`.
- **Evidence:** I compared the import list against the jar's class list (`unzip -l`). Every Hemomancy symbol the
  package names still exists; the rot is on the MnA side.
- **Confidence:** Verified. **Action:** decide. My recommendation is to delete it outright: the mod is not live, so
  there is no save concern, and an archive branch keeps the history. With it go the config, stub mixin, mixin JSON,
  assets, MnA-only effects, guardrail exemptions and the three dependencies. A future integration should be written
  fresh against the 1.21.1 MnA API. **Effort:** M.

### RF-21 · Low · `compat/curios` is a single stub file
- **Claim:** `compat/curios/CuriosPlugin.java` registers eight generic Curios slots by IMC and holds a TODO for the
  grimoire renderer (`:23`). The two Curios API classes it uses still exist in `curios-neoforge-9.5.1+1.21.1`, so it
  would probably compile. Nothing in core uses Curios, and the `data/curios` tags name the unregistered
  `harbinger_grimore`.
- **Confidence:** Verified. **Action:** delete with RF-20. **Effort:** S.

### RF-22 · Low · The dormant Dynasty Castle generator ships in the release jar
- **Claim:** `common/data/gen/DynastyCastlePieces.java` (514 lines) and `DynastyCastlePieceProvider.java` (143) are
  deliberately not registered (`common/data/gen/DataGeneration.java:25-28`). The 19 dynasty-castle files under
  `src/main/resources/data/hemomancy/structure/dynasty_castle/` are now hand-editable. Every other structure
  generator is a Python tool under `tools/worldgen/`. Three tests pin the Java generator (`DynastyCastlePiecesTest`,
  `DynastyCastleLayoutsTest`, `DynastyCastleWorldgenSourceTest`), and two rows of
  `english-literal-allow-list.txt` exist only for it.
- **Confidence:** Verified for the placement. Whether the generator still reproduces the hand-edited NBT is
  Suspected; I did not run it. **Action:** decide: delete it, or move it out of `src/main` (to `tools/` or the
  gameTest source set) so it stops shipping. **Effort:** S.

---

## 6. Player data migrations and other save compatibility

### RF-23 · Medium · No migration is past its sunset, but all 11 protect only dev worlds
- **Claim:**
  - **Sunsets:** entries 1-9 sunset on 2027-04-23 and entries 10-11 on 2027-05-01
    (`common/capability/player/shared/migration/PlayerDataMigrations.java:41-64`). Today is 2026-11-02, so none can
    go on sunset grounds.
  - **Releases:** none of the features these entries cover was ever released (the not-live ruling). Entries 10
    `artificer_claim_keys` and 11 `mnemonist_starter_claim` were added on 2026-11-01, after the 2026-10-29 "no
    migration" ruling. The same change chose *not* to migrate the met-Hermit flag "(the mod is not live)"
    (`docs/NPC_DIALOGUE_AND_TRADE.md:65`), so the policy is applied inconsistently.
  - **Id collision:** entries 10 and 11 cite "(F-4)", which collides with ledger F-4 (ScaleGrip).
  - **What the table carries:** 8 snapshot fixtures under `src/gameTest/resources/migration/`,
    `PlayerMigrationGameTests`, `PlayerDataMigrationGuardrailTest` and 9 allow-list rows "read once by the …
    migration" in `src/test/resources/architecture/persistent-data-allow-list.txt`.
- **Confidence:** Verified. **Action:** decide. If the owner confirms the dev worlds have logged in once (version 11)
  or accepts losing that state, delete the entries, their legacy read paths, the fixtures and the allow-list rows.
  Keep `PlayerDataMigrations` and `PlayerDataVersion` as empty infrastructure for the first post-release change.
  **Effort:** M.

### RF-24 · Medium · Save-compatibility paths outside the migration table
- **Claim:** each of these exists only for saves that predate a rework:
  - **Old station progress:** `StationUpgradeMigration`, plus the `ADVANCED_BREWING` and `RESONANT_FORGE`
    attachments and their `AdvancedBrewingProgress` and `ResonantForgeProgress` classes, which nothing else reads
    (`common/capability/HemoAttachmentTypes.java:61-68`).
  - **Old Scriptorium ids:** `ScriptoriumLegacyMigration`'s block and item aliases for `eightfold_scriptorium` and
    `monolithic_scriptorium` (`common/enchanting/ScriptoriumLegacyMigration.java:17-21`), and
    `mixin/core/MixinScriptoriumChunkMigration`.
  - `LegacyBloodStructureIds`.
  - **The `tome_of_the_unstained` alias item** (`common/init/ItemInit.java:1068-1073`).
  - **The `hemomancy:renderer` dimension-effects alias** (`client/registry/DimensionEffectsRegistrar.java:23-24`).
    No dimension type names it.
  - `FungalScarMigrationRules`.
  - **Old rite-phase names:** `CardinalRitePhase.byName` maps `ALEMBIC_PROJECTION` and `SCRIPTORIAL_INSCRIPTION`
    (`common/rite/CardinalRitePhase.java:22-33`).
  - **Old fields:** `KnownManipulations` `avatarActive` (`common/capability/player/harbinger/manip/KnownManipulations.java:484-570`),
    `MuscleMemoryState.legacyPrimedUntil` (RF-16), `InitiatoryDegree`'s pome `legacyOrigin` path
    (`common/capability/player/harbinger/degree/InitiatoryDegree.java:86-110`, `common/item/harbinger/QliphothPomeItem.java:221-402`),
    `VialCentrifugeBlockEntity` `legacyRecovery` (`common/tile/harbinger/crafting/VialCentrifugeBlockEntity.java:78-79,424,638`)
    and `GhastlyAlembicBlockEntity` `legacyStage` (`:114,174-186`).
  - **Old saved ids:** the `iron_wall` entity id kept "for legacy saves" (`common/init/EntityInit.java:942`).
  - **The L-7 block-blood fallback read,** listed in the ledger's closing section.

  The audit filed most of these as MUST PRESERVE (`docs/architecture/ARCHITECTURE_AUDIT.md:457-466`); the not-live
  ruling removes that reason.
- **Confidence:** Verified. **Action:** decide, together with RF-23 and RF-25, then delete. The pinned NBT layouts in
  `src/test/resources/rite/nbt-layout/` (`ActiveCardinalRiteNbtLayoutTest`: the pre-StateVersion countdown and the
  legacy `AlembicUpgrade` key) and the frozen registry ids change with them. **Effort:** L.

### RF-25 · Medium · Retired manipulations are filtered, not removed
- **Claim:** 15 retired manipulation ids and 20 retired memory-item ids are still registered
  (`common/capability/player/harbinger/manip/ManipulationRetirementRules.java:11-47`). 51 call sites filter them,
  among them `common/manipulation/BloodManipulation.java:142,505`. Without the save-protection reason, that is a
  parallel "retired" state kept alive across the registry, items, recipes and loadouts. Two of the ids
  (`glacial_bastion`, `blood_eclipse_mantle`) are still cast by Vesper through `EntityManipulationEffects` (audit
  §E), and `crude_memory_blood_lamp` and `crude_memory_crimson_harvest` still have data references.
- **Confidence:** Verified for the counts. The full data fallout is Suspected (that belongs to the materials audit).
  **Action:** decide: delete the retired entries outright, and give Vesper's two either a live or a Vesper-only home.
  **Effort:** L.

---

## 7. Config and registry entries

### RF-26 · Low · Two config values nothing reads
- **Claim:** `HemoServerConfig.MORPHLING_HUSBANDRY_STAGE_QUOTA` ("Legacy … Retained for compatibility",
  `config/HemoServerConfig.java:75,348-350`) has no reader. Its only would-be consumer,
  `MorphlingHungerRules.capMaturityByHusbandry`, is test-only. `HemoClientConfig.RENDER_MORPHLING_FEEDING_ANIMATION`
  ("Legacy compatibility setting", `config/HemoClientConfig.java:14,84-86`) also has no reader. All other server,
  common and client values and getters are read. The MnA config is F-25 and RF-20.
- **Confidence:** Verified. **Action:** delete both. A dev config file just drops the key. **Effort:** S.

### RF-27 · Medium · Registered effects that nothing applies; one is a documented, unbuilt feature
- **Claim:**
  - **`marked_by_canon`** (`common/init/EffectInit.java:245-247`) is documented as the sarcophagus-rejection mark
    that "lowers extraction odds" (`docs/HEMOMANCY_REFERENCE.md:2451`; `common/effect/MarkedByCanonEffect.java:11-17`),
    but nothing applies it. The rejection path only counts the attempt and wakes the Saint
    (`common/block/harbinger/puzzle/SaintSarcophagusBlock.java:524-532`), and `getExtractionAttempts` has no reader
    (`common/tile/harbinger/puzzle/SaintSarcophagusBlockEntity.java:148`).
  - **`chummed_waters`** (`EffectInit.java:300`) has an empty effect class (`common/effect/ChummedWatersEffect.java`).
    Blood Chum now works through `ChummedWatersAreaManager`.
  - Overlap: `half-baked-systems-and-clutter.md` (SY-7) also cuts these two and the nine pre-Court Morphling
    passive effects and their potions; `blocks-and-materials-utility.md:323` lists them too. The effect tag
    constants `EntityInit.WILLS` and `CIRCUS_PERFORMERS` (`common/init/EntityInit.java:79,81`) are unused in code;
    their tag files are data.
  - Items and blocks with no Java reference outside their init class number 206. Every one has a resource
    reference, so they are handed to the materials auditor.
- **Confidence:** Verified. **Action:** decide whether to finish `marked_by_canon` or delete it (and fix the
  reference doc); delete `chummed_waters`. **Effort:** S to delete, M to finish.

---

## 8. Tests and allow-lists

### RF-28 · Low · Tests whose only subject is dead code (false coverage)
- **Claim:** these tests pass while testing nothing the game runs. The M-3 group:
  - `VesperWeaponAnimationRulesTest`, `VesperEveningStarMotionTest`, `EndgameBossCombatRulesTest`,
    `MnemonicBlueprintRulesTest`, `CardinalRiteStructureConsumptionRulesTest`, `VialCentrifugeStartupRulesTest`,
    `RecipeMapNavigationTest`, `ManipulationSoftParentConnectionSourceTest` and `TendencyTraceStyleTest`.
  - `WillCombatRedesignSourceTest` and `WillResourceSourceTest`, through `MobManipCaster`.
  - `VirtualFieldNotesSourceTest`.
  - `ChamberThemeEffectsSourceTest` and `ApotheosFloorFunnelSourceTest`, through `BlankChamberThemeEffects`.

  The rule tests behind RF-10 and Appendix B behave the same way.
- **Confidence:** Verified. **Action:** delete them with their subjects. **Effort:** S.

### RF-29 · Low · Guardrail exemptions that go away with the dead code
- **Claim:**
  - **`HardCodedEnglishGuardrailTest.java:42`** skips `FieldNotesItem`.
  - **`DataFolderNamingGuardrailTest.java:25-28`** exempts four dead data files:
    `data/curios/tags/items/belt.json` and `spell.json` (unregistered `harbinger_grimore`),
    `data/forge/tags/fluids/test_fluid.json` and `data/mna/tags/items/dusts/purified_vinteum.json`.
  - **`BloodAccountingGuardrailTest.java:47-48`** justifies the `BloodRoutingRules` drain by the dead
    `LinkedPlayerSource`. The drain itself sits behind the test-only `transferFromGourdThenPlayer`.
  - **Allow-list rows for dead or dormant code:** two `english-literal-allow-list.txt` rows (Dynasty Castle) and nine
    `persistent-data-allow-list.txt` migration rows (RF-23).
  - **Dormant-compat skips** in 12 guardrail tests (RF-20).
  - **Ratchets unchanged since step 2:** `PrivateTestHelperRatchetTest` still holds `CEILING = 593`
    (`src/test/.../architecture/PrivateTestHelperRatchetTest.java:21`). 319 legacy `main()` tests remain (the
    ledger counted 335 at close). These are known ratchets, noted only for status.
- **Confidence:** Verified. **Action:** delete each exemption along with its subject. **Effort:** S.

---

## 9. Names left behind by renames

### RF-30 · Low · Velorum still uses internal `MARTYR` names (C-5)
- **Claim:** the names `MARTYRDOM`, `MARTYR` and `martyr` remain in `common/entity/boss/saint/velorum/VelorumVisualRules.java:9,11,15-16,31`
  and in `VelorumEntity.java:94,105,163,177,219,345` (`isMartyrdom`, `martyrdomAnimationState`,
  `getMartyrdomTicks`). They also appear in `client/model/entity/boss/velorum/VelorumModel.java`,
  `VelorumAnimState.java`, `client/render/world/VelorumRimeGeometry.java` and
  `client/render/layer/mob/VelorumFrostGlowLayer.java`, in the "The Frozen Martyr" comment at
  `common/init/EntityInit.java:419`, and in the `client/screen/overlay/boss/frame/VelorumFrame.java:11` Javadoc.
  The live code already says "still guard" (`STILL_GUARD_DURATION`, `DATA_STILL_GUARD_TICKS`).
  `VelorumVisualRules.legacy(...)` (`:34`) is the live phase builder, not a legacy one
  (`client/model/entity/boss/velorum/VelorumAnimState.java:13`, `VelorumModel.java:1254`).
- **Confidence:** Verified. **Action:** rename `martyr` to `stillGuard` and `legacy` to `phase`, and update
  `tools/model_export/velorum_anim.java`. **Effort:** S.

### RF-31 · Low · Hemorath's lang keys carry the retired "Hollow Vessel" title (D-17)
- **Claim:** the boss bar uses `entity.hemomancy.hollow_vessel` (`common/entity/boss/saint/hemorath/HemorathEntity.java:98`).
  The messages `the_hollow_vessel_empties_further` (`:321`), `the_hollow_vessel_stirs_blood_will` (`:349`) and
  `the_hollow_vessel_draws_in_your` (`common/entity/boss/goal/HematicCollapseGoal.java:78`) also keep it, and so does
  the authoring file `HollowVesselModel.bbmodel`. The English values already read "Hemorath".
- **Confidence:** Verified. **Action:** rename the keys. **Effort:** S.

### RF-32 · Low · `CardinalRitePhase.LEGACY` is the live countdown phase
- **Claim:** the enum's Javadoc says `LEGACY` exists "so rites saved by releases predating interactive ceremonies can
  finish" (`common/rite/CardinalRitePhase.java:4-8`). In fact it is the phase of every countdown rite, including the
  live Unstained rites (`common/rite/harbinger/RiteKind.java:181-228`). The same misnomer runs through
  `BloodAccounting.Source` `legacy_rite_completion` (`common/rite/harbinger/CardinalRiteCompletion.java:51,122`),
  which players see as a blood-ledger row, and through `ActiveRiteData.isLegacy` (`:250`), which the boundary
  renderer branches on (`client/render/world/CardinalRiteBoundaryRenderer.java:280-312`).
- **Confidence:** Verified. **Action:** rename to `COUNTDOWN`. The phase is saved by name, so the NBT layout fixtures
  change; the mod is not live, so no remap is needed. **Effort:** S.

### RF-33 · Low · Registry ids that still carry retired names
- **Claim:** the mod is not live, so a rename needs no alias. Each id below differs from the name players see:
  - `spawn_egg_eruptor` (`common/init/ItemInit.java:1129`) is the egg for `erythromycelium_eruptus`.
  - `puppeteering_thread` (`ItemInit.java:306`; 39 data files) displays as "Enthralling Filament". The lore says it
    was formerly named Puppeteering Thread (`docs/LORE_REFERENCE.md:1431`).
  - `iron_wall` is Ferric Rampart (RF-24).
  - `covenant_waybill` displays as "Harbinger Lodestone", `calcified_blood_spine` as "Barbed Splinter", `spawn_mark`
    as "Downfall Seal", and `warp_chair` with `warp_chair_filler` as "Somnolent Seat".
  - The `fungal_elytra` effect displays as "Noctifly Agaric", and the entities `humanity_sprite`, `frayed_thread`
    and `circus_carousel` as "Daemon", "Intruding Memory" and "The Impaled Procession".
  - The Muscle Memory classes and ids (47 files, and `muscle_memory:<id>` loadout keys) display as "Thelemic Memory".
    `half-baked-systems-and-clutter.md` calls this one system under two names.
- **Confidence:** Verified. Whether each id is meant to stay is a design call. **Action:** decide which to rename;
  each rename touches lang, models, data and the frozen ids. **Effort:** S each; L for Muscle Memory.

### RF-34 · Low · `harbinger_grimore` is misspelled (dormant code only)
- **Claim:** `compat/mna/item/MnAPluginItemInit.java:33` registers `harbinger_grimore`; the lang key and the
  `data/curios` tags use the same spelling.
- **Confidence:** Verified. **Action:** goes with RF-20. **Effort:** none on its own.

---

## Not a problem

- **No duplicate renderer registrations.** No entity or block-entity type is registered twice across
  `client/registry/*`.
- **Only one duplicated class name in main code** (RF-2).
- **No synced state written without being read.** Every `EntityDataAccessor` with a `set` also has a `get`.
- **The allow-lists are consistent.** Every file listed in `persistent-data-allow-list.txt` and
  `english-literal-allow-list.txt` exists, and every `getPersistentData()` count matches the source exactly. No file
  with a call is missing from the list. The English counts are enforced exactly by `EnglishLiteralAllowListTest`.
- **No disabled tests.** There is no `@Disabled` or `@Ignore`. The one `assumeTrue`
  (`src/test/.../common/recipe/PuppeteerCardinalOrdealResourceTest.java:71`) skips deliberately when the local
  skill-tree editor is absent.
- **No stale test paths.** All 39 missing repository paths named in tests are absence pins that assert a deleted
  file stays deleted (`assertFileDoesNotExist`, `assertMissing`, `assertNoPath`, `stale` lists). None is a read.
- **The other 33 empty catches are deliberate:** parse fallbacks (`NumberFormatException`, `IllegalArgumentException`
  on enum or UUID reads in saved data), per-block preview rendering guards, the debug showcase, and the
  `DevTestHooks` `ClassNotFoundException` that is expected in release jars.
- **`@SuppressWarnings` hides nothing.** It covers only `unchecked`, `rawtypes` and `deprecation`, plus three
  `ConstantConditions` on mixin `(Object) this` casts.
- **The remaining `@Deprecated` tags are fine.** Those on `ViridSalisTrailBlock` and `CrimsonFlameBlock` are vanilla
  overrides; `tome_of_the_unstained` is covered by RF-24.
- **`common/encounter`, `common/world`, `common/world/fold` and `common/testing`** have no dead classes.
  `DevTestHooks` is reflective by design. A few uncalled members are in Appendix A.
- **The two progress-screen tab controllers are not a stale duplicate.** The Harbinger screen uses
  `RitesAndCraftsTabController` (`client/screen/skilltree/harbinger/HarbingerProgressScreen.java:104`). The Unstained
  screen still uses `RitesTabController` and `CraftingTabController` (`client/screen/skilltree/unstained/UnstainedProgressScreen.java:71-72`)
  because the Unstained rite system is deferred (D-11).
- **Retired Morphling strain names do not survive in code.** The only hits, `cryptobiosis` and `ink_mantle`, are
  carried-forward Last Rite ids of the house strains.
- **No backup sources.** There are no `*.java~`, `.orig` or `.rej` files under `src/` or `tools/`. The `- Copy`
  textures and `old/` archives are packaging-excluded assets; they belong to the materials audit.
- **The live JEI compat** (`compat/jei`, `@JeiPlugin`) is fully reachable.
- **Dormant MnA code names only current Hemomancy symbols.** Every Hemomancy class and init field it uses still
  exists; the rot is in its MnA dependencies (RF-20).

---

## Appendix A: Members whose name appears nowhere else

These are methods and constants whose name appears nowhere outside their own declaration in `src/main/java`, tests,
gameTests, data or tools. Private methods are included; RF-7 covers their transitive closure. Paths are relative to
`src/main/java/com/vincenthuto/hemomancy/`. The RF-1 vector classes are left out.

- `client/ClientLiberScreenHooks.java`: `markEntriesUnreadAndRefresh`
- `client/data/EarthenVeinTravelClientState.java`: `closeSelection`
- `client/model/AnimationHelper.java`: `applyStatic`
- `client/model/entity/boss/endgame/VesperTheEveningStarModel.java`: `applyDefeatPose`
- `client/model/morphling/MorphlingModelAttachment.java`: `hideAttachedPartAt`
- `client/render/entity/misc/ArborMeshEmitter.java`: `fruitEllipsoid`
- `client/render/item/ModelBackedArmorItemRenderer.java`: `applyManualFirstPersonTransforms`
- `client/render/world/FungalSkyBoxRenderer.java`: `oscillatingValueWithHeartbeat`
- `client/render/world/QliphothBloomRenderer.java`: `branchLengthFrac`, `drawApexOrb`, `drawBranches`, `drawCanopy`, `drawRoots`, `drawTrunk`, `drawTrunkOrbs`, `renderRings`, `rootLengthFrac`, `trunkHeightFrac`
- `client/render/world/chamberofwill/ChamberOfWillRenderHelpers.java`: `renderCorticalFolds`
- `client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java`: `lowtideRuinTint`, `renderLowtideRetiredPanelRuinCluster`
- `client/render/world/chamberofwill/VesperFightFloorRenderer.java`: `flashFromDistantLightning`
- `client/render/world/rite/CardinalRiteVisualRules.java`: `noduleAngle`, `noduleSink`, `DAEMON_HALF_WIDTH`
- `client/render/world/rite/RiteSurface.java`: `colourDisc`
- `client/screen/overlay/BloodVolumeOverlay.java`: `renderApotheosHalo`, `renderCrownVessel`, `renderDegreeOrnament`, `renderPomeCrown`
- `client/screen/overlay/StillArtCooldownOverlay.java`: `getCooldownRemaining`
- `client/screen/radial/GenericRadialMenu.java`: `addInner`, `cycleNext`, `cyclePrevious`, `iterateVisible`
- `client/screen/skilltree/harbinger/BestiaryEntityPreview.java`: `hasPreviewPath`, `previewGuiDepth`
- `client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java`: `iconCrafting`, `positionCrafting`, `positionRites`
- `client/screen/skilltree/shared/MaterialsTabView.java`: `groupByBucket`
- `client/screen/skilltree/shared/SkillsTabController.java`: `getDeepFade`, `DEGREE_LABEL_GAP_Y`
- `client/screen/skilltree/util/PanZoomState.java`: `copyTo`
- `client/screen/skilltree/util/UnlockPredicate.java`: `hasItemInInventory`
- `client/screen/tile/crafting/scar/ScarButton.java`: `getI`, `getJ`, `setI`, `setJ`
- `client/screen/tile/functional/SporeImplantScreen.java`: `renderFungalSlotGlow`
- `client/screen/util/AbocipherText.java`: `veilIfUnread`
- `common/block/harbinger/CrimsonFlameBlock.java`: `setFireInfo`
- `common/block/harbinger/plant/GourdBlock.java`: `getAttachedStem`, `getStem`
- `common/block/shared/HorizontalFacingRotationHelper.java`: `rotateSouthOffsetsReversed`
- `common/capability/HemoCapabilityAccess.java`: `getScarItem`
- `common/capability/block/vein/VeinLocation.java`: `setRandomName`
- `common/capability/player/harbinger/bloodvolume/Bloodline.java`: `getLeader`, `getNpcMemberTypes`, `getTotalMemberCount`, `setBloodlineUUID`, `setLeaderUUID`, `setPlayerUUIDS`
- `common/capability/player/harbinger/degree/EnumInitiatoryDegree.java`: `getNumber`
- `common/capability/player/harbinger/equipment/HarbingerEquipmentType.java`: `getValidSlots`
- `common/capability/player/harbinger/manip/IKnownManipulations.java`: `isAvatarActive`
- `common/capability/player/harbinger/manip/ManipulationCostSnapshot.java`: `discountPercent`, `surchargePercent`
- `common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java`: `placeTemporaryWeb`
- `common/capability/player/harbinger/musclememory/MuscleMemoryActivationService.java`: `tryTriggerSecondPulse`
- `common/capability/player/harbinger/musclememory/MuscleMemoryState.java`: `isPrimed`, `learnAndPrime`
- `common/capability/player/harbinger/scar/fungal/VeinMinerHelper.java`: `actionResultFromType`
- `common/capability/player/shared/knowledge/discovery/MemoHelper.java`: `canDictateInto`, `dictateToLiber`, `getLegacyKnownMemos`, `getLegacyUnlockedLiberEntries`
- `common/capability/player/shared/skill/SkillPoint.java`: `setParent`
- `common/capability/player/shared/skill/SkillProgress.java`: `getCompletedMilestones`
- `common/capability/player/shared/trade/NpcTradeLedger.java`: `rapportView`
- `common/data/book/BloodStructurePageTemplate.java`: `setStructureloc`
- `common/encounter/ChamberOfWillManager.java`: `enterRefuge`, `orderedSkyThemes`, `ROOM_HEIGHT`
- `common/encounter/ChamberVisitService.java`: `isDream`
- `common/entity/animation/AnimationCycleManager.java`: `clearAllAnimations`, `isForceFinishEnabled`, `setForceFinish`
- `common/entity/boss/court/BloomWardenEntity.java`: `huntedId`
- `common/entity/boss/court/KindledChampionEntity.java`: `challengerId`
- `common/entity/boss/endgame/EndgameBossCombatRules.java`: `regenerationPerTick`, `shouldTrigger`
- `common/entity/boss/endgame/MycophantEntity.java`: `isSplitComplete`
- `common/entity/boss/endgame/NaeglerophaeonEntity.java`: `isCharging`, `moveVector`
- `common/entity/boss/endgame/VesperBorrowedAttacks.java`: `isBorrowed`
- `common/entity/boss/endgame/VesperPhaseTransitionRules.java`: `jumpArc`
- `common/entity/boss/saint/EnumSaintType.java`: `isOpposed`
- `common/entity/mob/animal/FunglingEntity.java`: `sporePuff`
- `common/entity/mob/aquatic/ChalybeateSnailEntity.java`: `getHarvestCooldownTicks`
- `common/entity/mob/arthropod/PhlegethonticBombardier.java`: `getChemicalCharge`
- `common/entity/mob/monster/BloodDrunkPuppeteerEntity.java`: `getPuppets`
- `common/entity/mob/monster/EnthralledDollEntity.java`: `getPuppeteer`
- `common/entity/mob/monster/will/WillBloodUtilityInteractions.java`: `findNearestFalteringWill`
- `common/entity/mob/monster/will/WillEntity.java`: `getAbsorptionStage`
- `common/entity/npc/DrudgeEntity.java`: `getChargeRatio`
- `common/entity/npc/circus/CircusPerformerEntity.java`: `dialogueTree`
- `common/entity/npc/dialogue/DialogueOptionPresentation.java`: `loreLine`
- `common/entity/npc/dialogue/DialogueTopic.java`: `withUnread`
- `common/entity/npc/dialogue/inquiry/ItemInquiryCondition.java`: `isUnconstrained`
- `common/entity/npc/dialogue/inquiry/ItemInquiryRegistry.java`: `allEntries`
- `common/entity/projectile/BloodCloudCarrierEntity.java`: `setDirectionMotion`
- `common/entity/projectile/LivingFlailHeadProjectileEntity.java`: `getOriginalHand`
- `common/entity/summon/BloodThrallEntity.java`: `getThrallState`
- `common/entity/summon/puppeteer/PuppeteerSummonBodies.java`: `hasBody`
- `common/entity/summon/puppeteer/PuppeteerSummonDefinitions.java`: `unlockedByDegree`
- `common/entity/utility/ArmatureRestraintEntity.java`: `getArmaturePos`
- `common/entity/utility/CovenantThroneSeatEntity.java`: `getThronePos`
- `common/event/worldevent/FoundingFaneSavedData.java`: `canPlaceBloodwell`, `getAllFanes`, `getFaneCenter`, `removeHeart`
- `common/init/ManipulationTreeEntry.java`: `getRequirementParentNames`
- `common/init/ManipulationTreeInit.java`: `getEntry`, `TREE_OFFSET_X`, `TREE_TOP_Y`
- `common/item/harbinger/SanguineSalveItem.java`: `getHealAmount`
- `common/item/harbinger/armor/BloodLustArmorItem.java`: `setMaskType`
- `common/item/harbinger/memory/HematicMemoryToolItem.java`: `canUseHematicToolSilently`
- `common/item/harbinger/morphlings/MorphlingAllies.java`: `nearestHostile`
- `common/item/harbinger/morphlings/MorphlingCombat.java`: `afflict`
- `common/item/harbinger/morphlings/MorphlingItem.java`: `clearWildBound`, `isPrimalRecovering`
- `common/item/harbinger/scar/ItemScarBinder.java`: `filterItem`
- `common/item/harbinger/scar/ItemScarPattern.java`: `getPrimaryRecipe`
- `common/item/harbinger/scar/ScarDefinition.java`: `getPassiveEffects`, `getPassiveModifiers`, `setAssignedTendency`, `setDeepenAmount`
- `common/item/harbinger/tool/EnumBloodGourdTiers.java`: `getEnchantability`, `getTierLevel`
- `common/item/harbinger/tool/MarionetteCrossbarItem.java`: `findFirstCrossbar`, `findFirstItem`
- `common/item/harbinger/tool/living/LivingGraspItem.java`: `summomCorruptNote`, `summonTentacleAid`
- `common/item/harbinger/tool/living/LivingStaffFocusRules.java`: `absorptionPulseIntervalTicks`
- `common/item/harbinger/tool/living/VialRackItem.java`: `hasFilledVials`
- `common/item/unstained/tool/UnstainedWarhammerItem.java`: `getPurityDamageBonus`
- `common/manipulation/BloodManipulation.java`: `getDisplayNameKey`, `getXpCost`, `setAlignLevel`, `setRank`, `setType`, `setXpCost`
- `common/manipulation/HemomancyTendrilEffects.java`: `boneConfig`, `iceConfig`
- `common/manipulation/ManipLevel.java`: `getXpToNextLevel`
- `common/manipulation/ManipulationCastContext.java`: `casterAsPlayer`
- `common/manipulation/animus/SummonThrallManip.java`: `hasPendingThrall`
- `common/manipulation/ductilis/Discharge.java`: `hasHit`
- `common/manipulation/ferric/StaffWeaponFormManip.java`: `getFormItem`
- `common/menu/MorphlingJarMenu.java`: `getFillRatio`
- `common/menu/PuppeteersSpindleMenu.java`: `getScaledCrossbarThreadWidth`, `getScaledThreadBufferWidth`
- `common/mission/hermit/SpentTempleInitiation.java`: `acceptBlessing`
- `common/network/capa/harbinger/OpenSSCScreenPacket.java`: `getDrudges`, `getSscBlood`, `getSscMaxBlood`
- `common/network/capa/harbinger/visceral/VisceralMirrorUpdatePacket.java`: `getPhaseOrdinal`
- `common/recipe/BloodStructureRecipe.java`: `setHeldItem`, `setHitBlock`
- `common/recipe/CardinalRiteRecipe.java`: `getPreviewPattern`, `setBreakBlocksOnCreation`, `setRankup`, `setRiteDescription`, `setRiteName`, `setRiteType`
- `common/recipe/CardinalRiteType.java`: `bySize`
- `common/recipe/MemoryWeavingRecipe.java`: `getCraftTimeTicks`, `isTendencyRequired`
- `common/recipe/ScarRecipe.java`: `setScarType`
- `common/rite/ActiveCardinalRite.java`: `getCarriedIchorTicks`, `getOfferingDwellTicks`, `isAbbreviated`, `tickOfferingDwell`
- `common/rite/CardinalRiteCeremonyDefinition.java`: `anchorBloodCostMl`
- `common/rite/harbinger/CardinalRiteCancellationHandler.java`: `canStart`
- `common/rite/harbinger/SanguineDominionSavedData.java`: `addDominion`
- `common/rite/structure/BloodStructureCraftingHelper.java`: `getMatchPositions`, `projectionHintMessage`
- `common/tile/harbinger/crafting/HematicArmatureBlockEntity.java`: `getRestrainedPlayer`
- `common/tile/harbinger/crafting/PuppeteersSpindleBlockEntity.java`: `hasSlottedCrossbar`
- `common/tile/harbinger/crafting/ScarStationBlockEntity.java`: `clearScarList`, `getPlayersUsing`, `onOpenOrClose`
- `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java`: `getCraftingPhase`, `getRecipePath`, `getStoredEnzymes`, `getTendCapability`
- `common/tile/harbinger/crafting/VialCentrifugeBlockEntity.java`: `findEmptyOutputSlot`, `isCentrifugeEmpty`
- `common/tile/harbinger/functional/CovenantThroneBlockEntity.java`: `getSeatedPlayer`
- `common/tile/harbinger/functional/EarthenVeinBlockEntity.java`: `isHasTicked`
- `common/tile/harbinger/functional/MasonsEffigyBlockEntity.java`: `getEffigyState`
- `common/tile/harbinger/functional/MnemonicReliquaryBlockEntity.java`: `getOpenCount`
- `common/tile/harbinger/functional/VisceralMirrorBlockEntity.java`: `getRitualProgress`, `getSelectedOrganIndex`, `getTargetOrgan`, `SELECTION_TIMEOUT_TICKS`
- `common/tile/harbinger/puzzle/BloodBasinBlockEntity.java`: `getCapacity`, `getFillAmount`
- `common/tile/harbinger/puzzle/SaintSarcophagusBlockEntity.java`: `getExtractionAttempts`
- `common/tile/inscription/DictationTableBlockEntity.java`: `getLiber`, `removeLiber`, `setLiber`
- `common/tile/unstained/crafting/PallidRetortBlockEntity.java`: `getWhiteHumorCapability`
- `common/world/fold/FoldedHallwayManager.java`: `isSameActiveFold`
- `common/worldgen/biome/FungalGardensOverworldRegion.java`: `replacementTargets`
- `common/worldgen/biome/FungalSurfaceBiomeRules.java`: `fungalSurfaceBiomes`
- `common/worldgen/feature/FeatureLogic.java`: `canRootGrowIn`, `hasEmptyHorizontalNeighbor`, `isAreaClear`, `isBlockNotOk`, `isBlockOk`
- `common/worldgen/feature/escharian/EscharianOvergrowthLayout.java`: `spanX`, `spanZ`
- `common/worldgen/structure/StructurePlacementChecks.java`: `isSuitableCircusPavilionSite`
- `common/worldgen/village/VillageEvents.java`: `buyItem`, `TAILOR_SHOP_PROCESSOR_LIST_KEY`
- `client/render/item/tile/harbinger/functional/MasonsEffigyItemRenderer.java`: `GUI_PITCH`, `GUI_YAW`
- `client/render/world/CardinalRiteBoundaryRenderer.java`: `SOCKET_GLOW_WIDTH`, `SOCKET_SURFACE_WAVE`, `SOCKET_UNDULATION_SCALE`
- `client/render/world/UmbralTearGeometry.java`: `DEPART_TICKS`
- `client/render/world/chamberofwill/ApotheosChamberEffects.java`: `APOTHEOS_CEILING_OUTWARD_TENDRIL_SEGMENTS`
- `client/render/world/chamberofwill/ChamberSkyThemeRegistry.java`: `DEFAULT_CLOUDS`, `DEFAULT_SKY`
- `client/screen/skilltree/util/ConcentricTreeGeometry.java`: `CONTENT_PADDING`
- `common/block/harbinger/plant/HyphaeBlock.java`: `AABB_OFFSET`
- `common/block/unstained/functional/PaleSilverBellsBlock.java`: `SILVER_BELLS_PURITY_BOOST`
- `common/capability/player/shared/knowledge/discovery/MemoDefinitions.java`: `ANNETTA_INSECT_OBSERVATION`, `ANNETTA_INSECT_OBSERVATION_IMMACULATUS`, `PALE_LADY_NOTES`
- `common/capability/player/unstained/PurityGainEvents.java`: `EMPTY_BLOOD_INTERVAL`
- `common/effect/LanternTickRules.java`: `LIGHT_CLEANUP_RADIUS`
- `common/entity/boss/endgame/EndgameBossActions.java`: `MYCOPHANT_ATTACK_RANGE`
- `common/entity/boss/phlegethosoma/PhlegethosomaCombatRules.java`: `CLOT_LIFETIME`, `SUCTION_RANGE`
- `common/entity/mob/aquatic/BarbedUrchinEntity.java`: `STATE_FULL`, `STATE_MID`
- `common/entity/mob/aquatic/MnemonicWhaleTuning.java`: `MODEL_REAR_BODY_WIDTH_UNITS`
- `common/entity/mob/monster/will/WillBendRules.java`: `REDIRECT_BLOOD_COST`
- `common/event/ConsecrationHandler.java`: `CONSECRATION_PURITY_THRESHOLD`
- `common/event/SilentArchonArmorRules.java`: `APOTHEOS_DEGREE`, `SILENT_ARCHON_DEGREE`
- `common/init/BaseFeatureInit.java`: `MORPHIC_POOL`, `RENDERING_POOL`
- `common/init/CarverInit.java`: `DRY_FUNGAL_CAVE`
- `common/init/EffectInit.java`: `MORPHLING_PASSIVES`
- `common/init/EntityInit.java`: `CIRCUS_PERFORMERS`, `WILLS`
- `common/init/LootModifierInit.java`: `ADD_ITEM`
- `common/init/SoundInit.java`: `ANTECEDENT_QUIETING`, `ANTECEDENT_SEVERED`, `ANTECEDENT_SURVEY`
- `common/item/harbinger/tile/functional/SpecimenJarData.java`: `TAG_ROTATION`
- `common/item/harbinger/tool/DSDItem.java`: `DSD_RANGE`
- `common/item/harbinger/tool/living/LivingTorchBreathRules.java`: `MAX_USE_TICKS`
- `common/item/unstained/tool/SilthmereGlaiveItem.java`: `REACH_BONUS`
- `common/manipulation/animus/HematicRiposteRules.java`: `COST`
- `common/manipulation/flammeus/VitricCombustionManip.java`: `COMBUSTION_DUST`
- `common/manipulation/mortem/LignumMortisManip.java`: `INNER_BLOOD`, `OUTER_BLOOD`
- `common/manipulation/tenebris/BloodEclipseManip.java`: `BLINDNESS_AMP`, `WEAKNESS_AMP`, `WEAKNESS_TICKS`
- `common/menu/tile/functional/MasonsEffigyMenu.java`: `PLAYER_INV_SLOTS`
- `common/rite/harbinger/CardinalRiteFinaleTiming.java`: `DAEMON_TRAVEL_BLOCKS_PER_TICK`
- `common/rite/harbinger/CardinalRiteHumanityGeometry.java`: `EMISSION_INTERVAL_TICKS`

## Appendix B: Members only tests, gameTests or tools name

These are never named in `src/main/java`. A test, gameTest, tool script or a token of the same name in a resource
does name them (resource hits can be coincidental). Suspected: each needs the RF-10 check (is there an inline twin?)
before it is deleted.

- `client/data/MycophantIntroClientState.java`: `inputBound`
- `client/model/entity/boss/endgame/VesperWeaponAnimationRules.java`: `actionBlend`, `broodTramplePitch`, `contactMotion`, `cycloneSpin`, `flailArmMotion`, `flailFollowMotion`, `stanceBlend`, `stingerMotion`, `swingArc`
- `client/model/entity/mob/arthropod/LanternTickModel.java`: `addAngledLeg`
- `client/player/FirstPersonHandRules.java`: `insideCrosshairZone`, `onScreen`
- `client/player/WarpChairPlayerPose.java`: `seatedLegs`, `trancePresentation`
- `client/render/entity/misc/ArborRenderBudget.java`: `crownBlades`, `leafBladeVertices`
- `client/render/world/CardinalRiteBoundaryGeometry.java`: `socketDistortion`, `socketIntensity`, `socketStainFeatherAlpha`, `socketStainInnerAlpha`, `socketStainSurfaceOffset`, `veinRootRadius`
- `client/render/world/VisceralGeometry.java`: `wellPoint`
- `client/render/world/chamberofwill/SilentArchonChamberEffects.java`: `renderSilentArchonForegroundCloudWisps`, `renderSilentArchonVolumetricFog`
- `client/render/world/chamberofwill/VesperFightArenaLayout.java`: `tileAt`
- `client/render/world/floor/CreepCells.java`: `maxCells`
- `client/render/world/rite/CardinalRiteVisualRules.java`: `noduleRadius`
- `client/screen/dialogue/DialogueScreen.java`: `expandTradeCard`
- `client/screen/skilltree/harbinger/HarbingerProgressScreen.java`: `bottomRightTabLabelKeys`, `topTabLabelKeys`
- `client/screen/skilltree/shared/HarbingerRecipeMapDefinitions.java`: `craftingLinks`, `craftingPaths`, `puppetryPaths`, `riteLinks`, `ritePaths`
- `client/screen/skilltree/unstained/UnstainedProgressLayout.java`: `focusNode`
- `client/screen/skilltree/util/ConcentricTreeGeometry.java`: `pointOnDegreeRing`
- `client/screen/skilltree/util/HarbingerChromeGeometry.java`: `frameSegments`
- `client/screen/skilltree/util/ProgressTabRegistry.java`: `isInstantiated`
- `client/screen/skilltree/util/ScreenDrawUtils.java`: `drawCircleTrace`, `drawFineCubicTrace`
- `client/sound/SpectrogramAnalysis.java`: `playedFrequency`
- `common/armor/ArmorSetHelper.java`: `hasFullD7Set`, `hasFullForkSet`
- `common/block/harbinger/EscharianOvergrowthBlock.java`: `cachedShapeCount`
- `common/block/harbinger/functional/ConsecratedBloodwellBlock.java`: `isInOwnFane`
- `common/brewing/BoundBrewRules.java`: `bindDoses`
- `common/capability/HemoCapabilityAccess.java`: `hasPendingWhispers`
- `common/capability/player/harbinger/bloodvolume/BloodFlowContribution.java`: `circulation`
- `common/capability/player/harbinger/bloodvolume/routing/BloodRoutingRules.java`: `ancestralSovereigntyMultiplier`, `servitorCommandRange`, `transferFromGourdThenPlayer`
- `common/capability/player/harbinger/circus/CircusProgressRules.java`: `passivePoints`
- `common/capability/player/harbinger/court/CourtLife.java`: `hasLesson`, `isChampion`, `revokeChampion`
- `common/capability/player/harbinger/musclememory/MuscleMemoryActivationService.java`: `tryTrigger`
- `common/capability/player/harbinger/musclememory/MuscleMemoryState.java`: `copyForDeath`
- `common/capability/player/harbinger/possession/PossessionRules.java`: `mayBegin`, `OFFERING_EFFECTS`
- `common/capability/player/harbinger/possession/PossessionSceneRules.java`: `sceneAt`
- `common/capability/player/harbinger/rite/IchorianKnowledge.java`: `discoveryProgress`
- `common/capability/player/shared/migration/PlayerDataMigrations.java`: `migrations`, `LATEST`
- `common/capability/player/shared/trade/NpcTradeLedger.java`: `usesView`
- `common/capability/player/unstained/UnstainedAccessRules.java`: `mayGainBloodProgress`
- `common/capability/player/unstained/UnstainedPathGuidance.java`: `estimatedClarityMinutes`, `estimatedPurityMinutes`
- `common/damage/SchoolStateRules.java`: `healingMultiplier`, `rimeSlow`
- `common/enchanting/ResonantForgeRules.java`: `isBlankCylinder`
- `common/encounter/ChamberVisitRules.java`: `canBuild`, `canUseArbor`
- `common/entity/boss/court/BloomWardenEntity.java`: `bloomCount`
- `common/entity/boss/court/KindledChampionEntity.java`: `pendingVents`
- `common/entity/boss/endgame/MycophantIntroRules.java`: `beatAt`
- `common/entity/boss/endgame/MycophantMotionRules.java`: `pairPlanted`
- `common/entity/boss/endgame/MycophantSplitRules.java`: `halves`
- `common/entity/boss/endgame/MycophantVisualRules.java`: `claimWrapProgress`
- `common/entity/boss/endgame/NaeglerophaeonEntity.java`: `captureStart`, `isOverloading`
- `common/entity/boss/endgame/VesperCombatRules.java`: `primaryAttackTick`, `secondaryAttackTick`
- `common/entity/boss/endgame/VesperEveningStarAct.java`: `locksWeaponToStance`
- `common/entity/boss/endgame/VesperFlammeusBreathRules.java`: `bloodCostPerTick`
- `common/entity/boss/endgame/VesperMountAttackRules.java`: `carapaceDamageMultiplier`, `mayStartAneurysm`
- `common/entity/boss/endgame/VesperPhaseTransitionRules.java`: `cocoonVisibility`, `collapseProgress`
- `common/entity/boss/endgame/VesperPuppetRoster.java`: `onEncounterReset`
- `common/entity/boss/endgame/VesperRageCombatRules.java`: `isRageThreshold`
- `common/entity/boss/endgame/VesperSickleRules.java`: `committedSideHolds`, `ALL_MOVES`
- `common/entity/boss/endgame/VesperTheEveningStarEntity.java`: `beginAwakening`
- `common/entity/boss/saint/SaintTrialEntity.java`: `sendFloorRing`
- `common/entity/mob/animal/HematicBurrowerRules.java`: `escapeDropWeight`, `BREAKS_BLOCKS_ON_ESCAPE`
- `common/entity/mob/animal/ScarletSerpentSlitherRules.java`: `segmentPropagatedYaw`
- `common/entity/mob/arthropod/PhlegethonticBombardier.java`: `setTerritoryAnchor`
- `common/entity/mob/monster/ExcoriatedEntity.java`: `isSentinel`
- `common/entity/mob/monster/will/WillAbsorptionRules.java`: `graceTicks`, `rageTicks`
- `common/entity/npc/circus/CircusPavilionSavedData.java`: `hasSite`, `removeSite`
- `common/entity/npc/circus/CircusPavilionStateRules.java`: `resetOutcome`
- `common/entity/npc/court/CourtMemberEntity.java`: `stir`
- `common/entity/npc/dialogue/DialoguePresentation.java`: `tradeCards`
- `common/entity/npc/dialogue/DialogueRapportLore.java`: `teaserKey`
- `common/entity/npc/dialogue/FungalWhisperDialogueTrees.java`: `fungalSpineEmerged`
- `common/entity/npc/dialogue/GuardianDialogueTrees.java`: `forItem`
- `common/entity/npc/dialogue/HarbingerAlchemistDialogueTrees.java`: `withCurrentVesselLesson`
- `common/entity/summon/MorphlingPolypLayerRules.java`: `maskFor`
- `common/entity/summon/SanguineHoundRules.java`: `dissolutionBloodRefund`
- `common/entity/summon/puppeteer/PuppeteerSummonRules.java`: `canUnlockAtDegree`
- `common/entity/utility/arbor/ArborFruitGeometry.java`: `familyProfiles`
- `common/event/HarbingerAdvancementGranter.java`: `hasRecordedEnzyme`, `isArtificerAssumedLimbFittingReady`, `isArtificerAssumedLimbLessonReady`, `isArtificerCrimsonVestmentFittingReady`, `isArtificerCrimsonVestmentLessonReady`, `isArtificerThreeAnswersFittingReady`, `isArtificerThreeAnswersLessonReady`, `isArtificerWeightOfTheFrameFittingReady`, `isArtificerWornVowFittingReady`, `isArtificerWornVowLessonReady`, `isHematicIronBlockCrafted`, `isLiberSanguinumCrafted`, `isMnemonistWovenVesselFinished`, `isVeinMasonContinuationReady`, `isVesselFilled`
- `common/event/LastRiteHelper.java`: `getArmedSource`
- `common/event/PendingBloodCraftManager.java`: `cancelWithin`
- `common/event/TriadAttributeCaps.java`: `clampMoveSpeed`
- `common/event/worldevent/FaneFootprint.java`: `containsStake`, `stakeBudget`, `withHeart`
- `common/init/BiomeInit.java`: `bootstrapBiomes`
- `common/init/BlockInit.java`: `getAllBlockEntries`
- `common/init/ManipulationTreeEntry.java`: `getSoftParentNames`
- `common/item/MorphicNectarMutationRules.java`: `primalOverlayFrameCount`, `primalOverlayFrameSize`, `primalOverlaySpriteCount`, `primalProceduralCoreFillAlpha`, `primalProceduralLayerCount`, `primalProceduralUsesManagedGuiBuffer`, `primalProceduralUsesVanillaGuiQuadWinding`, `primalProceduralUsesVanillaGuiRenderType`, `primalTendrilSegments`
- `common/item/harbinger/QliphothPomeRules.java`: `shouldResetProgressOnPrune`
- `common/item/harbinger/bloodline/VasculariumCharmRules.java`: `shouldDropEquippedSlot`
- `common/item/harbinger/morphlings/MorphlingBalanceBands.java`: `reactiveCooldownMax`, `reactiveCooldownMin`, `DEATH_SAVE_COST_MAX`, `DEATH_SAVE_COST_MIN`, `PRIMAL_COOLDOWN_MAX`, `PRIMAL_COOLDOWN_MIN`, `PRIMAL_COST_MAX`, `PRIMAL_COST_MIN`
- `common/item/harbinger/morphlings/MorphlingHeatRules.java`: `incomingDamageMultiplier`
- `common/item/harbinger/morphlings/MorphlingHungerRules.java`: `capMaturityByHusbandry`
- `common/item/harbinger/morphlings/MorphlingItem.java`: `findLookTarget`
- `common/item/harbinger/morphlings/MorphlingStrain.java`: `folkNameKey`
- `common/item/harbinger/morphlings/MorphlingStrains.java`: `byPolypFamily`
- `common/item/harbinger/morphlings/PrimalMorphlingRules.java`: `primalBonusMultiplier`
- `common/item/harbinger/tool/MarionetteCrossbarItem.java`: `findCrossbar`
- `common/item/harbinger/tool/living/LivingAxeLodestoneRules.java`: `shouldPulse`
- `common/item/harbinger/tool/living/LivingFlailRules.java`: `orbitSpeedScale`, `slownessAmplifier`
- `common/item/harbinger/tool/living/LivingSickleCombatRules.java`: `sweepDamage`
- `common/item/harbinger/tool/living/LivingStaffFocusRules.java`: `bareAbsorptionPerSecond`, `staffAbsorptionPerSecond`
- `common/item/harbinger/tool/living/LivingStaffItem.java`: `isLivingStaffUtilityUse`
- `common/item/harbinger/tool/living/LivingTorchBreathRules.java`: `bloodAfterPayment`, `stopReason`, `withActiveBlood`, `withAlive`, `withBlockingCardinalRite`, `withConnected`, `withSameDimension`, `withSameHeldStack`, `withStaffFormRestored`, `withUsing`
- `common/item/shared/MnemonicBlueprintRules.java`: `shouldShowCue`
- `common/manipulation/BloodManipulation.java`: `setSection`, `setTend`
- `common/manipulation/HematicCommandManager.java`: `isRebuked`
- `common/manipulation/ManipulationReactiveEvents.java`: `sanguineWardPool`
- `common/manipulation/TendencyAffinityRules.java`: `adjustManipulationDamage`
- `common/manipulation/animation/CastPresentation.java`: `hasExplicit`
- `common/manipulation/animus/HematicRiposteEvents.java`: `isParrying`
- `common/manipulation/ferric/CellHandRules.java`: `usePulse`
- `common/manipulation/ferric/IronheartedRules.java`: `addIronHeartHealth`
- `common/mission/alchemist/PallidVesselProgress.java`: `withReport`
- `common/mission/shared/HarbingerChapterMilestone.java`: `isRankUnlocked`
- `common/mission/vicar/FirstBloodcraftLedgerProgress.java`: `completedProofs`
- `common/network/PlaceStructurePacket.java`: `structureType`
- `common/network/capa/harbinger/manips/SynapticLoadoutActionPacket.java`: `applyLoadout`
- `common/particle/EntityParticleUtils.java`: `genRandomColor`
- `common/recipe/ArmatureUpgradeRules.java`: `bowlSlotForLocalHit`
- `common/recipe/MemoryWeavingRecipe.java`: `getEnzymeRequirements`, `getRequiredTendencyCount`
- `common/recipe/serializer/PatternKeyEntry.java`: `fromJsonLiteral`
- `common/rite/ActiveCardinalRite.java`: `absorbCurrentOffering`, `getAwakenedSigils`, `getBloodspentUntil`, `getNpcReservesMl`
- `common/rite/CardinalRiteBrazierSignatureRules.java`: `exactMatch`
- `common/rite/CardinalRiteStructureConsumptionRules.java`: `shouldConsume`
- `common/rite/WorkingCraftBed.java`: `smallestFitting`
- `common/rite/harbinger/CardinalRiteAnchorVisualRules.java`: `boundaryVisual`
- `common/rite/harbinger/CardinalRiteCancellationGeometry.java`: `riteOpacity`
- `common/rite/harbinger/CardinalRiteFinaleTiming.java`: `offeringParticleStrength`
- `common/rite/harbinger/CardinalRiteHumanityGeometry.java`: `orientPoint`, `particleLifetime`, `particleStyle`, `scaledCloud`
- `common/rite/harbinger/CardinalRiteNpcStationRules.java`: `assignedNpcAllies`, `faneReturnPosition`
- `common/rite/harbinger/HarbingerCardinalRiteEvents.java`: `canPlaceQliphothBloom`
- `common/rite/harbinger/SanguineDominionSavedData.java`: `getDominions`
- `common/rite/harbinger/SanguineFervorSavedData.java`: `isInFervorRange`
- `common/rite/sigil/CardinalRiteSigilRules.java`: `isActionableNode`
- `common/rite/structure/BloodCraftingPatternSearchRules.java`: `compareDimensionsBySearchCost`
- `common/rite/unstained/LetheCovenantSavedData.java`: `isInDomain`
- `common/rite/zone/RiteZoneList.java`: `ownerCovers`
- `common/tile/IBloodReservoir.java`: `receiveBlood`
- `common/tile/harbinger/crafting/HematicArmatureBlockEntity.java`: `hasRestrainedPlayer`
- `common/tile/harbinger/crafting/ScarStationBlockEntity.java`: `tryLoadPatternFromSlot`
- `common/tile/harbinger/crafting/SomaticLoomBlockEntity.java`: `engramPreview`, `getCurRecipe`
- `common/tile/harbinger/crafting/VialCentrifugeBlockEntity.java`: `getResultFromVial`, `SPIN_TOTAL_TIME`
- `common/tile/harbinger/crafting/engram/EngramRecollections.java`: `hasWoven`
- `common/tile/harbinger/crafting/engram/EngramSigilGeometry.java`: `edgeIndex`, `starLines`
- `common/tile/harbinger/functional/CardinalFocusBlockEntity.java`: `getTempleHermit`
- `common/tile/harbinger/functional/MasonsEffigyBlockEntity.java`: `getPendingBlood`, `tryReceiveProjectedBlood`
- `common/tile/harbinger/functional/MortalDisplayBlockEntity.java`: `isClaimedBy`
- `common/tile/harbinger/puzzle/ChapelFocusBlockEntity.java`: `setMarks`
- `common/world/HeartNodeFootprint.java`: `overlapsAnother`
- `common/world/fold/FoldActivationBounds.java`: `entrySideForCrossing`
- `common/world/fold/FoldTransform.java`: `clampWorldPosition`, `exitSide`, `oppositeExitCamera`
- `common/worldgen/biome/ErythrocoralReefRegion.java`: `beachBiomePaths`, `middleBiomePaths`, `middleBiomeVariantPaths`, `oceanBiomePaths`, `peakBiomePaths`, `peakBiomeVariantPaths`, `plateauBiomePaths`, `plateauBiomeVariantPaths`, `shatteredBiomePaths`, `slopeBiomePaths`, `slopeBiomeVariantPaths`, `DEFERRED_PLACEHOLDER_PATH`, `ERYTHROCORAL_REEF_PATH`
- `common/worldgen/biome/FungalGardensOverworldRegion.java`: `replacementTargetPaths`
- `common/worldgen/biome/FungalSurfaceBiomeRules.java`: `surfaceRuleOrderNames`, `usesFungalSurfaceName`
- `common/worldgen/feature/ErythrocoralReefTuning.java`: `maxClusterRadius`, `PLACED_FEATURE_COUNT`
- `common/worldgen/structure/HemoJigsawStructure.java`: `assembleOnSurface`
- `common/worldgen/structure/court/CourtSeatLayout.java`: `outpostCentre`, `GARDEN_DEPTH`, `SANCTUM_CHIMNEY_ZONE`
- `common/worldgen/terrain/phlegethontic/PhlegethonticBasinLayout.java`: `largestBasin`
- `client/model/entity/mob/arthropod/DesiccantModel.java`: `TELSON_STING_COLOR`
- `client/render/world/FuneralBellGeometry.java`: `TOLL_TICKS`
- `client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java`: `LOWTIDE_RUIN_FAR_LIGHT`
- `client/screen/skilltree/shared/SkillsTabController.java`: `DEGREE_LABEL_TOP_Y`, `DEGREE_LABEL_X`
- `common/block/harbinger/crafting/SomaticLoomGeometry.java`: `GANGLION_RADIUS`, `PIRNS`
- `common/encounter/MycophantNurseryLayout.java`: `PLAY_RADIUS`
- `common/entity/boss/endgame/MycophantBroodRules.java`: `NURSE_TICKS`
- `common/entity/boss/saint/SaintEnrageRules.java`: `FRAGMENTS_PER_RESIDUUM`
- `common/entity/mob/aquatic/BrinedVotaryBehaviorRules.java`: `LAND_SPEED`
- `common/entity/mob/aquatic/MnemonicWhaleTuning.java`: `MODEL_BODY_SEGMENTS`, `MODEL_HEAD_WIDTH_UNITS`, `MODEL_MID_BODY_WIDTH_UNITS`, `MODEL_TOTAL_LENGTH_UNITS`
- `common/entity/mob/arthropod/VenomRibCentipedeRules.java`: `MAX_GROUP_SIZE`, `MIN_GROUP_SIZE`, `NATURAL_SPAWN_WEIGHT`
- `common/entity/mob/arthropod/VenomRibCentipedeSlitherRules.java`: `LEG_PAIRS_PER_SEGMENT`
- `common/entity/npc/trade/NpcTradeRequirements.java`: `LOCK_KEYS`
- `common/entity/npc/trade/NpcTradeService.java`: `REASON_KEYS`
- `common/entity/utility/arbor/ArborSkeleton.java`: `TRUNK_SURFACE_RADIUS`
- `common/event/BloodStructureFeedRules.java`: `BLOOD_TILE_TRANSFER_RATE`
- `common/event/BorrowedBloodRules.java`: `DEFAULT_CAP`
- `common/event/CirculationIncomeRules.java`: `DEFAULT_BANDWIDTH_PER_CAPACITY_POINT`, `DEFAULT_BANDWIDTH_PER_DEGREE`, `DEFAULT_BASE_BANDWIDTH`
- `common/event/LastRiteRules.java`: `DEFAULT_SHARED_COOLDOWN_TICKS`
- `common/init/BaseFeatureInit.java`: `SMALL_FUNGUS`
- `common/manipulation/ManipulationStatusRules.java`: `SANGUINE_MAGNETISM_PIN_RADIUS`, `SANGUINE_MAGNETISM_RADIUS`
- `common/worldgen/structure/court/CourtSeatCandidates.java`: `OUTPOSTS`
