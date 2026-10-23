# Consolidation Progress Ledger

Every consolidation session reads this file first and updates it last, following
[CONSOLIDATION_SESSION_RULES](CONSOLIDATION_SESSION_RULES.md).

## Step map

| Step | Roadmap phase(s) | Status | Commit(s) | Date |
|---|---|---|---|---|
| 1 | Urgent defect fixes (F-1, F-2, F-3) | DONE | `025f2b18f` | 2026-10-21 |
| 2 | Phase 0: green baseline and test infrastructure | DONE | `c435034d4`, `ddb358304` | 2026-10-21 |
| 3 | Phase 1: verified dead-code removal and packaging | DONE | `ccc75e9a0` | 2026-10-21 |
| 4 | Phase 2: network hygiene | DONE | `d10e5e7f2` … `018394712` (10 commits) | 2026-10-21 |
| 5 | Phase 3: player-state plumbing | DONE | `dceced30a` … `7911eaa1f` (5 commits) | 2026-10-22 |
| 6 | Phase 4: shared small utilities | DONE | `48127a923` … `07d4a4183` (6 commits) | 2026-10-22 |
| 7 | Phase 5: blood accounting | DONE | `8338c98da` | 2026-10-22 |
| 8 | Phase 7: registration and client registration | DONE | `3e8b213d4`, `291731d6f` | 2026-10-22 |
| 9 | Phase 6: combat targeting and damage attribution | DONE | `3b841ba03`, `7f0c1968d`, `5fa01de44`, `c9af6c6c3` | 2026-10-22 |
| 10 | Phases 8 + 9: rendering plumbing and animation conventions | DONE | `a53982248`..`e2597dc41` (items 1-9), review fixes and ledger in the commit after `e2597dc41` | 2026-10-23 |
| 11 | Phase 10: god-class decomposition | DONE | `91bb77eba`, `b6c7bd846`, `866c0b990`, `3385bc323`, `ced99f334`, `0871dc5e6`, `8ba776eca`, `017d6f7dc`, `7028ada7d`, `44719064f`, item 5 in the commit after it | 2026-10-23 |
| 12 | Phase 11: persistence authority migrations | DONE | `b205211d0`, `b314e8204`, `227a7edee`, `b22b8283f`, `9185e427e`, `b575b0593`, `64dd755d0`, ledger in the commit after `64dd755d0` | 2026-10-23 |
| 13 | Phases 12 + 13: content, worldgen, data, tooling, standards | IN PROGRESS | see the step 13 entry | 2026-10-23 |

Statuses: `NOT STARTED`, `IN PROGRESS`, `DONE`, `DONE (uncommitted)`, `BLOCKED`.

## Baseline (2026-10-21, commit `3fae723d0`)

- **Compile:** pass.
- **`test`:** 3,908 tests, 20 failed. These 20 failures are the accepted baseline until
  step 2 clears them:
  - `HematicArmatureModelSourceTest` ×2
  - `VialCentrifugeModelSourceTest` ×2
  - `MnemonicReliquaryModelSourceTest`
  - `CardinalRitePlantedStaffModelTest`
  - `PhlegethonticBombardierResourceTest`
  - `BloodProfileMigrationTest`
  - `LivingSickleFeatureSourceTest`
  - `ClientModelTextureCoverageTest`
  - `EnglishLocalizationEncodingTest`
  - `GameTestHarnessSourceContractTest` ×2
  - `LegacyMainTestAdapterTest`, covering `ModelFirstTileRendererResourceTest`,
    `MorphlingLumenlaceRenameResourceTest`, `VestmentOfTheFinalMoltResourceTest`,
    `ArmorPreparationResourceTest`, `UtilityPolishResourceTest` and
    `LivingWeaponGraftRiteSourceTest`
  - `LegacyMainTestSuiteContractTest`
- **GameTests, `alphaCheck`, server and client start:** not run at baseline.
- **Full log:** [evidence/08-baseline-test.log](evidence/08-baseline-test.log).

## Baseline after step 2 (2026-10-21, commit `ddb358304`)

From step 3 onward the failure set must be empty.

- **`test`:** 3,926 tests, 0 failures, 0 skipped.
- **`alphaCheck`:** green. All 999 required GameTests passed, and so did `verifyGameTestResourceLog` and
  `verifyPackagedClientResources`.
- **Dedicated server (`runServer`, throwaway game directory):** reached `Done (6.262s)`, with 0 ERROR lines and no
  client-class or invalid-dist loading.
- **Client (`runClient`, throwaway game directory):** reached the title screen, with 0 ERROR lines.
- **Impact map:** [evidence/source-text-impact-map.csv](evidence/source-text-impact-map.csv). It lists, for each
  test, every file it reads (2,983 rows: 494 tests, 380 of them reading 967 Java files). Regenerate it with
  `python gradle/source-text-impact-map.py`.

## Decisions

The owner fills in the **Decision** column. A session may implement an item only when it
reads `APPROVED`. Any other value means the session leaves the item alone and lists it
under *Pending approvals*. IDs refer to [ARCHITECTURE_AUDIT](ARCHITECTURE_AUDIT.md).

| ID | Item | Step | Decision | Notes |
|---|---|---|---|---|
| F-1 | Reject non-finite or non-positive blood donations | 1 | APPROVED | Approved in the step 1 prompt; done in `025f2b18f` |
| F-2 | Register the 4 client-only bidirectional payloads as server-to-client only | 1 | APPROVED | Approved in the step 1 prompt; done in `025f2b18f` |
| F-3 | Remove the client-class call from `WillEntity` | 1 | APPROVED | Approved in the step 1 prompt; done in `025f2b18f` |
| M-1 | Hand-edited models vs their generators (Armature, Centrifuge, Reliquary, loom) | 2 | APPROVED | Owner chose "drop the marker checks" (2026-10-21). The models match their generators byte for byte; the failing tests only looked for a provenance string no generator writes. Done in `ddb358304` |
| M-2 | Five unregistered mobs (Abyssal Siphon, Cruor Fiend, Frozen Clot, Synapse Hound, Void Drinker) | 3 | PENDING | Delete / archive branch / keep |
| M-3 | Other "REQUIRES VERIFICATION" dead code (audit section E) | 3 | PENDING | Per item. Step 3 added three: `IBloodRoutingTarget`, `FeatureLogic` and five textures orphaned by the removals. `FeatureLogic` is in use again since step 6 (W-2), so it no longer needs a decision. Step 4 added two more orphaned textures: `textures/gui/vein_selection.png` and `vein_teleport.png` |
| N-6 / N-7 payload set | Fold the single-purpose VFX payloads (N-6) and delete the never-sent payloads (audit section E) under a new `PROTOCOL_VERSION` | 4 | APPROVED | Owner approved 2026-10-21. Payload byte layouts are frozen by the session rules, so this needed approval |
| F-6 | Ledger actions require the ledger item | 4 | APPROVED | Owner approved 2026-10-21 (step 4 prompt) |
| F-7 | Bloodline chat rate limit | 4 | APPROVED | Owner approved 2026-10-21: a simple per-player rate limit, with excess messages dropped. Vanilla chat integration (signing, mute, logging) stays out of scope |
| F-8 | Reject NaN pool settings; clamp client partial tick | 4 | APPROVED | Owner approved 2026-10-21 |
| F-9 | Structure paste requires operator permission and the held spawner | 4 | APPROVED | Owner approved 2026-10-21 |
| F-4 | ScaleGrip: respect cancelled deaths; persist pending returns | 5 | PENDING | Its map stays outside `TransientServerStateEvents` until then: clearing it would lose the parked items |
| F-5 | OrganEcho clears the correct slot | 5 | PENDING | |
| F-19 | Station kits follow the delivery contract (alive, not observational, no Creative discard) | 5 | PENDING | Once approved, `StationUpgradeProgress.deliver` becomes a `PendingDelivery` claim |
| F-20 | Resync Ichorian knowledge on dimension change | 5 | PENDING | One line once approved: add `DIMENSION` to the `ICHORIAN_KNOWLEDGE` row of `PlayerStateSync` |
| S-2b | Resend skill progress on respawn and dimension change, like every other synced attachment | 5 | PENDING | `PlayerStateSync.SKILLS` is login-only, as before step 5 |
| S-5b | Concentrated Blood replacement vial: count confirmed insertion and drop what does not fit (no more Creative discard) | 5 | PENDING | `ConcentratedBlood.replace` still uses add-or-drop; the same applies to the Qliphoth Bloom Spine reclaim and `CardinalRiteSavedData.deliverRecovery` |
| F-12 | Projected blood honours the rite lock | 7 | PENDING | Once approved, one line: `IBloodReservoir.takeProjectedBlood` returns 0 unless `canReceiveBlood()`. Flip `BloodAccountingGameTests#riteLockedStationStillTakesProjectedBloodUntilF12` with it |
| F-13 | Scar upkeep becomes reserve-protected and synced | 7 | PENDING | Balance. The sync half arrived in step 7, because every payment ends in `syncVolume`. What remains is one flag: `ScarDefinition.UPKEEP` `reserveProtected = true` |
| F-14 | Passive scar effects survive milk | 7 | PENDING | Design |
| F-15 | Living weapons check `isActive()` and stop wiping the reserve | 7 | PENDING | Balance. The sites are the `BLOOD_PRICE` payments in the Living Axe, Blade, Crossbow and Tool |
| F-22 | Hide the legacy Dictation Table from the main creative tab | 8 | PENDING | Still a no-op check in `CreativeTabInit.shouldShowItemInCreativeTab`, with a comment. Once approved, the fix is a block-side exclusion; the frozen creative-tab snapshot (`dictation_table` at line 633) must be updated in the same change |
| G-1 / F-18 | Explicit damage stages and one death-save stage | 9 | PENDING | Balance: blood lost per hit changes. Needs a damage-stage ordering test with it (final damage and blood drained per hit). The heal stages (standards §7) show the pattern |
| F-16 | Attribute AoE damage from armour, Still Arts, Sheolic and Saprovitta; respect allies and PvP | 9 | PENDING | Balance. Since step 9 the sites name their reach: Silent Severance `Reach.UNGATED_SURVIVAL`, Autoimmune Edge and Silver Rebuke `UNGATED`, the Saprovitta trail `MONSTER_FILTER_ANY_STATE`; Sheolic retribution acts on its attacker. Approving it moves them to `STRICT` and attributes the damage, and flips their `CombatTargetingMatrixGameTests` rows |
| F-17 | Curved Horn uses `LastRiteHelper` | 9 | PENDING | |
| G-2b | The caster's own puppets stop matching `Monster.class` filters | 9 | PENDING | One flag since step 9: `allies = false` in `Reach.MONSTER_FILTER` / `MONSTER_FILTER_ANY_STATE`, which also spares teammates' and impressed monsters. Drudges would then need their owner resolved (`DrudgeActions.monsters`). Flip the drudge, Prismatic, Lethean Mute and Glacier matrix rows |
| G-7b | A flight revoke checks the other flight sources | 9 | PENDING | Found in step 9. Today the Elytra effect landing drops Edacious flight until Edacious's next tick, and in water it keeps doing so. Once approved, `FlightArbiter.revoke` asks the other `Source`s; flip `FlightSourceGameTests#elytraLandingDropsEdaciousFlightUntilItsNextTick` |
| G-2c | One boss definition | 9 | PENDING | Found in step 9. `ManipulationReactiveEvents.isBoss` (with Warden, Elder Guardian and the bosses tag), `ConstrictorCordEntity.isBossTarget` (without them) and `ThreadRipperManip.protectedBody` (a package-name check) disagree |
| A-4 | Animations that would freeze under `/tick freeze` | 10 | PENDING | Per animation |
| V-3b | Ferric quad-encoder constant (128 vs 127) | 10 | PENDING | Visual check |
| A-1b | Replay clock for the bosses that read a raw synced tick | 10 | PENDING | Found in step 10. The Stained Priestess, Latent Annetta (model, renderer, infection layer), the Mycophant, Vesper's hood removal and the mount's absorption layer read a synced counter plus the partial tick. `BossReplayClock` would smooth packet bursts, but it can run up to 2 ticks ahead of a counter that pauses before it re-bases, so displayed timing changes. Once approved, give each a clock keyed on its action or state. The four Saints animate on `ageInTicks` only and need nothing. |
| V-3c | Outpouring surface marks round their alpha | 10 | PENDING | Found in step 10. The marks write the material encoding by hand, camera-relative with no pose, and round alpha × 255 where `MaterialQuads` truncates it. Once approved, write them through `MaterialQuads` (some vertices lose one alpha level). |
| V-6b | Mycophant spore colours given on the wrong scale | 10 | PENDING | Found in step 10. The Mycophant encounter's two spore bursts pass 0–1 floats (`0.48F, 0.72F, 0.34F`), which `ParticleColor` truncates to black. Once approved, give them 0–255 values (they turn green and amber). (The hit glow takes either scale: `ParticleGlow` divides by 255 only above 1.) |
| F-11 | Blood-structure crafts tick once per server tick | 11 | PENDING | Crafts take their intended time |
| S-9 | Per-player `dataVersion` and gated one-shot migrations | 12 | APPROVED | Save-critical. Owner approved 2026-10-23 (step 12 session); done in `b205211d0` (frozen id `b314e8204`) |
| F-21 | Archon choice: attachment is the only authority | 12 | APPROVED | Save-critical. Owner approved 2026-10-23 (step 12 session); done in `227a7edee`, recipe gate fix `64dd755d0` |
| S-6c | Circus progress moved into one attachment | 12 | APPROVED | Save-critical. Owner approved 2026-10-23 (step 12 session); done in `b22b8283f` |
| S-1c | Chamber access and `PendingWhispers` moved into attachments | 12 | APPROVED | Save-critical. Owner approved 2026-10-23 (step 12 session); done in `9185e427e` |
| S-7 | Two-tier cooldown convention | 12 | APPROVED | Keep each entry's tier. Owner approved 2026-10-23 (step 12 session); done in `b575b0593` |
| O-5 | Typed components for reworked items | 12 | PENDING | Save-critical. Not approved in the step 12 session: it applies only to items being reworked anyway, and none were |
| F-23 | Move tag files to singular folders (the tags start working) | 13 | APPROVED | Owner approved 2026-10-23 (step 13 session). The three live files moved; four files naming unregistered items stay ignored on purpose (`DataFolderNamingGuardrailTest`) |
| F-24 | Three template-less structures and the empty mausoleum tag | 13 | APPROVED (keep, document) | Owner chose 2026-10-23: keep the data for future templates and document them as not generating |
| F-25 | Register or remove the MnA config read | 13 | PENDING | Not approved in the step 13 session |
| F-26 | Chthonian Queen build name | 13 | APPROVED | Owner approved 2026-10-23. The build name only selects a data-fixer type, which no Hemomancy entity has |
| F-27 | `HemolysisEffect` category consistency | 13 | APPROVED | Owner approved 2026-10-23 (proposed: both NEUTRAL). Done as both BENEFICIAL instead: the Lethean Chalice strips non-beneficial effects, and Unstained purity needs Hemolysis, so NEUTRAL would have changed play |
| T-6b | Prune unreferenced `libs/` jars; move dev-only dependencies to `localRuntime` | 13 | APPROVED (move only) | Owner chose 2026-10-23: move import-free `implementation` jars to `localRuntime`; keep every jar on disk |
| D-5 | Datagen: revive or delete per provider | 13 | APPROVED (delete dead) | Owner chose 2026-10-23: keep the running providers, delete never-registered provider classes and generated files that `src/main` shadows |

## Guardrail tests

Each step lists the regression and guardrail tests it adds. Every later step must keep
them green.

| Test | Added in step | Protects |
|---|---|---|
| `common/capability/player/harbinger/bloodvolume/LumpDonationValidationTest` | 1 | F-1: NaN, infinite, zero and negative lump donations give nothing |
| `architecture/ClientBoundPayloadRegistrationTest` | 1 | F-2: the 4 client-handled payloads have exactly one registration, `playToClient` |
| `architecture/WillEntityClientIsolationTest` | 1 | F-3: `WillEntity` has no `net.minecraft.client.` or `com.vincenthuto.hemomancy.client.` reference |
| `architecture/RepositoryTextEncodingTest` | 2 | Every text file under `src/`, `docs/`, `wiki/` and `tools/model_export/` is strict UTF-8 |
| `architecture/PrivateTestHelperRatchetTest` | 2 | Private `read`/`assert*` helper copies in tests may only fall (ceiling 593). Lower the ceiling when you remove copies |
| `support/SourceAssertionsTest` | 2 | The shared helpers' `methodBody`, `section`, failures and line-ending handling |
| `testing/LegacyMainTestSuiteContractTest` | 2 | The adapter runs exactly the legacy `main()` tests found in source (set comparison, no hard-coded count) |
| `architecture/SourceTreeHygieneTest` | 3 | No empty `.java` files, no empty `@SubscribeEvent` handlers, and `hemomancy.mixins.json` lists exactly the `@Mixin` classes in `mixin/core` |
| `verifyPackagedClientResources` (part of `alphaCheck`) | 3 | The jar holds no `.cache/`, `/old/`, `.bbmodel` or `bbmodel/reference/` entries |
| `architecture/PayloadDirectionTest` | 4 | Every inline send matches its payload's registered direction, and only `AntecedentEffectPacket` is bidirectional |
| `architecture/PayloadProtocolVersionTest` | 4 | The registered payload set (with directions) matches `src/test/resources/network/payload-registry.txt` for the current `PROTOCOL_VERSION` |
| `architecture/NetworkClientIsolationTest` | 4 | `common/network` has no `net.minecraft.client` reference and no `Minecraft.getInstance()` |
| `architecture/SendFacadeTest` | 4 | Only `PacketHandler` and `DialogueSender` call `PacketDistributor` |
| `architecture/ServerboundGuardsCoverageTest` | 4 | Every serverbound payload uses `ServerboundGuards` or is listed with a reason; stale exemptions fail |
| `common/network/ServerboundGuardsTest`, gameTest `ServerboundGuardsGameTests` | 4 | `finite`, `partialTick`, `menu`, `withinReach` / `nearBlock` / `nearBlockEntity`, `nearEntity` |
| `common/network/particle/VisualPayloadCodecTest` | 4 | Every `ParticleBurstPacket` and `EntityVisualPacket` kind round-trips |
| gameTest `NetworkDefectGameTests` (batch `network_defects`) | 4 | F-6 ledger in hand, F-8 non-finite pool settings, F-9 structure-paste operator and spawner |
| `common/network/capa/harbinger/BloodlineChatRateLimitTest` | 4 | F-7 sliding window and per-sender separation |
| `architecture/AttachmentAccessTest` | 5 | Attachments are read only through `HemoCapabilityAccess` and a reasoned allow-list (stale entries fail); no `Player` accessor returns `Optional` |
| `architecture/SessionStateRegistryTest` | 5 | Every static UUID-keyed collection in `common/` is registered with `TransientServerStateEvents` or exempt with a reason; server stop handlers live only in the hub; logout handlers are the hub or a listed gameplay handler; Lantern Tick lights are released on logout |
| `architecture/SourceTreeHygieneTest#everyEventBusSubscriberSubscribesSomething` | 5 | No `@EventBusSubscriber` class without a `@SubscribeEvent` method (NeoForge refuses to load the mod) |
| gameTest `SessionStateGameTests` (batch `session_state`) | 5 | A real logout drops the chat window, fane preview and apply lockout |
| `architecture/PlayerStateSyncTest` | 5 | Only `HemoCapabilityRegistrar` resends `PlayerStateSync` entries on login, respawn and dimension change; no whole-attachment polling per tick |
| gameTest `OwnerSnapshotGameTests#everySyncedAttachmentReachesTheOwnerOnEachLifecycle` | 5 | Every table entry reaches the owner exactly once per lifecycle event it lists (one per equipment slot), and only then |
| `common/capability/PendingDeliveryTest` | 5 | The delivery rule: closed gates, carried items, full, partial and failed insertion, per-entry retry, Creative discard counted as nothing |
| `architecture/PendingDeliveryRegistryTest` | 5 | Only `PendingDelivery` counts insertions; each of the six claims is driven and keeps its saved NBT key |
| `architecture/EasingCopyGuardrailTest` | 6 | A-2: no private scalar `smoothstep` / `smooth` / `lerp` / `clamp01` / one-argument `clamp` or `ease` outside `HemoEasing` and a reasoned allow-list; also scans the `tools/model_export` templates |
| `common/util/HemoEasingTest` | 6 | Golden values for every `HemoEasing` function, including that `smoothstep` is the cubic, not the quintic |
| `architecture/SmallUtilityGuardrailTest` | 6 | No private copies of the attribute-modifier, armour-set, ticker, block-entity sync, pattern-origin or worldgen-setter helpers; no literal degree comparison outside `ProgressionGates`; immediate block-entity sends only before a menu opens |
| `common/util/ProgressionGatesTest` | 6 | `hasDegree` / `isDegree` at every degree boundary, the rewritten comparison forms, and every Unstained stage threshold |
| gameTest `AttributeSyncGameTests` (batch `attribute_sync`) | 6 | P-1: an unchanged held modifier, including a worn elytra or Fungal Elytra, leaves its attribute clean (no update packet) |
| gameTest `PatternOriginGameTests` (batch `pattern_origins`) | 6 | L-4: each origin solver keeps its pre-merge winner on overlapping matches |
| `common/capability/player/harbinger/bloodvolume/BloodPaymentRulesTest` | 7 | Every flag combination of a blood payment: covered, overstrain (one-off and upkeep), full-amount refusal, Sanguine Reserve on/off and protected/unprotected, dormant volumes, spend counting |
| `architecture/BloodAccountingGuardrailTest` | 7 | G-3 / L-2: a player volume is drained, subtracted or spend-counted only in `BloodAccounting`; every other `drain(` / `subtractBloodVolume(` / `addBloodSpend(` is allow-listed by file and count with its reason (stale entries fail); `BloodVolumeServerPacket` is built only in `syncVolume` and its own decoder; payments record and sync |
| gameTest `BloodAccountingGameTests` (batch `blood_accounting`) | 7 | Exact volume before and after for an item (Living Axe), machine (projection into a Vial Centrifuge), rite (brazier lighting, with spend) and armour (Bloodburst) payment, each ending in `syncVolume`; the pending F-12 rite-lock behaviour is pinned |
| gameTest `FrozenIdentifierGameTests` (batch `registration_validation`) | 8 | Every Hemomancy id in every loaded registry (1,991) and the exact order of both creative tabs (693 lines) match `src/gameTest/resources/frozen/`, captured from `de8e25ba1`. Payload ids stay with `PayloadProtocolVersionTest` |
| `architecture/RegistrationStyleGuardrailTest` | 8 | R-1 / R-2 / R-3 / R-4 / R-5 / R-7: the two subscriber forms only, no `bus=`; the mod constructor adds only lifecycle listeners; no `BlockEntityRenderers.register`, `particleEngine.register` or `initializeClient`; `ClientEvents` and the client inits stay out of `common/init`; registry lookups; typed data components; one WIP list and one tab builder |
| `architecture/ClientSessionCacheTest` | 8 | P-10: every static entity-id-keyed collection in `client/**` is cleared in `ClientSessionEvents.onClientPlayerLogout`, or exempt with a reason (stale entries fail) |
| `architecture/CommonClientIsolationTest` | 8 | V-8: nothing under `common/` names a `net.minecraft.client` or `hemomancy.client` class except inside an item's `hemomancy$getClientItemExtensions()` / `IClientItemExtensions` body; no wildcard client imports; every `ClientPayloadHandlers::dispatch` payload has exactly one client registration |
| gameTest `CombatTargetingMatrixGameTests` (suite `combat_targeting_validation`) | 9 | Who 11 representative effects reach (manipulation, Drudge, 2 armour, 2 Still Art, 2 morphling, 2 scar, living weapon) across hostile, pet, ally, own puppet, boss, PvP on/off; and that each `Reach` preset equals the filter it replaced over every body kind |
| gameTest `HealModifierGameTests` | 9 | Each heal modifier's scale, their stacking, and restraint purity reading the requested heal |
| gameTest `FlightSourceGameTests` | 9 | Each flight source's grant and revoke, including the pending G-7b Elytra/Edacious conflict |
| `architecture/CombatTargetingGuardrailTest` | 9 | G-2: every `Monster.class` gather passes `canHarm` in its arguments or is allow-listed (mob AI, ownerless zones; stale entries fail); `ManipulationCombatHelper` holds the only harm predicates; the area and look sites ask `canHarm`; Drudge work lives in `DrudgeActions` |
| `architecture/HealAndFlightGuardrailTest` | 9 | G-4: one heal-modifier stage and one HIGH reader; G-7: only `FlightArbiter` writes `mayfly` / `flying` |
| `architecture/ScreenChromeGuardrailTest` | 10 | V-1: no private border, slot-well or vein-background helper under `client/`; no copied two-pixel border or slot-well fill sequence outside `ScreenDrawUtils`; a seeded vein field outside `VeinBackgroundRenderer` only in the listed other-art files (stale entries fail) |
| `architecture/RenderTypeCachingGuardrailTest` | 10 | V-2 / P-9: no method builds a `RenderType` per call (a `RenderType`-returning method that builds one must take the holder's uniforms `Supplier`, or be a listed lazy singleton); no other method builds one; no static shader-clock field outside `SchoolRenderTypes` |
| `client/render/SeededRenderTypesTest` | 10 | V-2: the same seed and uniforms return the same type; changed uniforms move to the seed's spare and leave the queued type's uniforms alone; a seed never builds more than two types; seeds are kept apart; LRU eviction |
| `architecture/HudAndWorldDrawingGuardrailTest` | 10 | V-7: no `RenderGuiEvent` in main code (HUD drawing is a `GuiLayerRegistrar` layer); `WorldEffectDispatcher` is the only `RenderLevelStageEvent` subscriber, calls the self-staging effects first in their old order and keeps the Outpouring screen passes at high priority; the full-screen effects and the HUD markers keep their old order, and the markers are the last layer |
| `architecture/MaterialQuadGuardrailTest` | 10 | V-3: no `floorMod(…, 127/128)` outside `MaterialQuads`; the Lux, Thermal, Ferric and rite-surface geometries set no UVs of their own; Ferric's 128 stays the named `FERRIC_SEED_TILES` (V-3b); a class that both wraps a seed and sets UVs by hand is listed (the Outpouring marks, V-3c) |
| `client/render/world/MaterialQuadsEncodingTest` | 10 | V-3: a SHA-256 digest of every vertex's position, colour and UV bits from the Lux, Thermal, Ferric and rite-surface entry points over wrapping seeds, pinned from the pre-merge code (6,468 vertices) |
| `architecture/ShaderNoiseIncludeGuardrailTest` | 10 | V-5: no noise function (`hash`, `noise`, `fbm` names) whose text and callees match another shader's, whitespace aside, unless it reads its own shader's uniforms; every `include/noise` file is imported, reads no uniform and imports nothing |
| `architecture/BossAnimationClockGuardrailTest` | 10 | A-1: no `VesperAnimClock` / `VesperMoveTimeline` in main code or the model generators; no hand-written `tickCount - client…Start` replay clock; boss renderers that read a raw synced tick plus partial are listed (A-1b) |
| `common/entity/boss/BossReplayClockTest` | 10 | A-1: the three `VesperAnimClock` cases, tolerance and re-keying, the reset rule of `sample`, the keyed constructor, and a 200-run fuzz against the old Vesper clock and Annetta's start-tick clock |
| `architecture/PlayerAnimationGuardrailTest` | 10 | A-3: the staff-planting, warp-chair and casting third-person poses are called only from `PlayerAnimationRegistry`, and the registry only from `MixinHumanoidModel`; the ten first-person `RenderHandEvent` handlers keep their priorities (HIGHEST planting and staff morph, HIGH injection and microscope, NORMAL torch, fungal projection and axonal, LOW morphling hand, LOWEST casting and held weapon) |
| `client/player/PlayerAnimationRegistryTest` | 10 | A-3: the third-person pose order and which poses claim the frame |
| `architecture/AnimationTimeGuardrailTest` | 10 | A-4: no `getTimer().getGameTimeDeltaPartialTick(`, `getGameTime() + partial…` or `tickCount + partial…` in client or mixin code outside `AnimationTime`; the three common-side visual helpers and the 29 wall-clock client classes are listed |
| `architecture/ParticleDataGuardrailTest` | 10 | V-6: the particle data classes are `ColorParticleOptions` plus three listed ones with their payloads; every particle type is a `HemoParticleType`; no `createData` helper outside `HemoParticleData` |
| `common/particle/data/ColorParticleOptionsTest` | 10 | V-6: the colour particle's network form is the old serialized-colour UTF-8 string byte for byte, its command form is float fields `r`, `g`, `b`, and a decoded particle belongs to the type that decoded it |
| `client/screen/skilltree/harbinger/VeinBackgroundStyleTest` | 10 | V-1: the named vein styles (progress with its deep palette, crimson, machine, station) keep the values of the copies they replaced, including each style's vertex order |
| gameTest `RiteKindCharacterisationGameTests` (suite `rite_kind_validation`) | 11 | L-6: every rite kind (ceremony, countdown, Working craft, station upgrade, Puppeteer trial, Succession) driven through the real level tick: its `RiteKind`, start, completion, every ending (broken, recalled, leash, abandoned, collapsed, died, retired, refused), waiting for an absent caster, and a mid-rite save and load that must write the same NBT and finish the same way; the login snapshot carries the rite's stage |
| `common/rite/ActiveCardinalRiteNbtLayoutTest` | 11 | L-6: each kind's saved rite NBT pinned key for key (`src/test/resources/rite/nbt-layout/`), the pre-StateVersion countdown and the legacy `AlembicUpgrade` key |
| `architecture/RiteKindGuardrailTest` | 11 | L-6 / N-8: the shared tick, `CardinalRiteTermination`, `CardinalRiteCompletion` and `CardinalRiteStructure` test no kind (`isRite`, `isWorkingCraft`, `isPuppeteerTrial`, `SuccessionRites.is`); only `CardinalRiteActivation` builds an `ActiveCardinalRite`; the blood crafting key packet only delegates; no GameTest reflects on rite completion |
| `architecture/NetworkPackageGuardrailTest` | 11 | N-8: `common/network` holds payloads, `PacketHandler`, `ServerboundGuards` and three listed helpers (stale entries fail) |
| gameTest `DialogueRouteCharacterisationGameTests` (suite `dialogue_route_validation`) | 11 | O-4: every dialogue-event route (about 170 ids, prefix routes and their fall-throughs included) posted by a D0 player and a supplied D7 player to its speaker and with no speaker; the reward flag, chat, saved-data diff, drops and Hemomancy advancements must equal `src/gameTest/resources/dialogue/route-fingerprints.txt` (510 lines, recorded from the old handler) |
| `architecture/DialogueRoutingGuardrailTest` | 11 | O-4: `DialogueEventRouter` is the only `DialogueEvent` subscriber; every `<Npc>DialogueEvents` class has a `handle` and is in the router's table; no route class over 400 lines |
| `common/command/HemoCommandTreeTest` | 11 | `/hemo` syntax node by node (literals, argument names, types and bounds, executes, suggests, redirects, permission per node) against `src/test/resources/command/hemo-tree.txt` |
| gameTest `HemoCommandCharacterisationGameTests` (suite `hemo_command_validation`) | 11 | every executable `/hemo` path (133; `qliphoth tree` excluded as a world edit) run on a fresh player with arguments taken from the tree: return code, feedback, saved-data change and spawned entities against `src/gameTest/resources/command/hemo-command-fingerprints.txt` |
| gameTest `EntityCastCharacterisationGameTests` (suite `entity_cast_validation`) | 11 | L-1: each of the 34 entity casts (and one manipulation without) cast by a Husk at a Cow: went off, Will-castable, both mobs' health, effects, data keys and movement, spawned types, changed blocks, against `src/gameTest/resources/entitycast/entity-cast-fingerprints.txt`; over the live registry, exactly the 34 casts are attached, Wills may cast exactly the old 32, every Vesper CASTABLE entry has one |
| `architecture/EntityCastGuardrailTest` | 11 | L-1: `EntityManipulationEffects` has no name switch and names no manipulation; each `EntityCasts` method is attached once, to the manipulation it is named for; only the entry point reads `getEntityCast()` |
| gameTest `ScriptoriumOperationCharacterisationGameTests` (suite `scriptorium_operation_validation`) | 11 | L-9: every Scriptorium button driven through the real menu with fixed seeds (enzyme selection, the three modes and their gates, inscription in each row, targeted inscriptions across all eight tendencies with their curse rolls, refusals, denaturing and its gates, the rite lock, a stranger's click): accepted, item components, tank, lapis, enzymes, xp, practice and saved-data change against `src/gameTest/resources/scriptorium/operation-fingerprints.txt` |
| `architecture/StationMenuGuardrailTest` | 11 | L-9: no menu under `common/menu` drains blood, records station practice or enchants an item; the Scriptorium menu's button handler calls `station.inscribe` and `station.denature` |
| `architecture/PlayerDataMigrationGuardrailTest` | 12 | S-9 / F-21: each migration entry point is run only from the `PlayerDataMigrations` table; no tick handler runs the fungal scar sweep; only the migration, `/hemo qliphoth` and the constant name the Archon choice key, and only Armature gates (resolved by path) name it in data |
| `architecture/PersistentDataAllowListTest` | 12 | S-1: exact per-file `getPersistentData()` counts in `src/main/java` against `src/test/resources/architecture/persistent-data-allow-list.txt`, each with a reason; lower a count when you remove calls |
| `architecture/CooldownTierGuardrailTest` | 12 | S-7: no raw persistent-data long named as a cooldown; session cooldown maps keyed by player UUID |
| `common/capability/player/shared/migration/PlayerDataMigrationsTest` | 12 | S-9: migration versions strictly increase, ids are unique, every sunset is 2027-04-23 or later; the data version defaults to 0 and round-trips |
| gameTest `PlayerMigrationGameTests` (suite `player_migration_validation`) | 12 | One old-format NBT snapshot per migration under `src/gameTest/resources/migration/`, merged over a fresh save and loaded: the migrated state, the old keys gone, then a second login, a reload and a death clone run nothing, and two full reruns from version 0 agree. Also the Silent Archon armour gate through the path, the whisper merge, the once-only Chamber advancement backfill, and the cooldown tier across reload, death and End return |
| `architecture/HemoCommandStructureGuardrailTest` | 11 | `HemoCommand` is one root that only wires `<Name>Subcommand.build()`, each exactly once; subcommands register nothing themselves and stay under 300 lines; no other main class registers `/hemo` |

## Step entries

<!-- Append one entry per step, copying this template:

### Step N: <name>
- Status / date / commits:
- Test counts before → after:
- Metrics (classes and lines removed and added, call sites migrated, remaining legacy instances):
- Guardrails added:
- Approved items implemented:
- Pending approvals:
- Deviations from the roadmap:
- Not verified (needs in-game or visual check):
- Handoff notes for the next step:
-->

### Step 1: Urgent defect fixes
- **Status / date / commits:** DONE, 2026-10-21. Code in `025f2b18f`; this ledger update follows it in a second `consolidation(step-1):` commit.
- **Test counts before → after:**
  - `test`: 3,911 tests, 20 failed → 3,919 tests, 20 failed. The failure set is identical to the baseline list above.
  - The 8 new tests all pass. The two updated main-style tests (`CellHandRenderingResourceTest`, `WillBloodUtilitySourceTest`) pass through `LegacyMainTestAdapterTest`.
  - `runGameTestServer`: the dedicated server loads and runs all 999 GameTests. 3 fail. On the stashed pre-change tree the same run fails 4: the same 3 plus `carrioncannothealfromoverkill`, which is flaky. No new failure. See *Known issues* below.
- **Metrics:**
  - Main code: 25 lines added, 11 removed across 6 files. One method removed (`WillAbsorptionGlowParticleFactory.createPulseData`). Two methods added (`BloodVolumeTransferRules.donationAmount`, `HemoParticleData.willAbsorptionGlowPulse`).
  - Tests: 3 new files (113 lines). 2 source-text tests repointed (4 lines added, 3 removed).
  - Call sites migrated: 1 donation path, 4 registrations, 1 particle call. Remaining `createPulseData` callers: 0.
  - Remaining legacy: the 4 payload handlers still live in `common/network` and call `Minecraft` / `LayerEvents` directly. Moving them to `client/**` is Phase 2 item 3 (step 4).
- **Guardrails added:** `LumpDonationValidationTest`, `ClientBoundPayloadRegistrationTest`, `WillEntityClientIsolationTest` (see the table).
- **Approved items implemented:** F-1, F-2, F-3.
- **Pending approvals:** none new. Everything else in *Decisions* is untouched.
- **Deviations from the roadmap:**
  - F-2 changes only the registration direction. All four payloads are built and sent server-side through `PacketHandler.sendToPlayer`, so their handlers never run on a server and moving them was not needed. Step 4 moves them into `client/**` with the other client handlers.
  - The new guardrails read source text directly because the shared `SourceAssertions` helper arrives in step 2. Step 2 should switch them to it.
  - `alphaCheck` was not run as one task. It depends on `test` and `runGameTestServer`, which both fail on the baseline, so it cannot go green before step 2. Its three parts were checked one by one:
    - `test`: as above.
    - GameTests: as above. Neither log has a Hemomancy `/ERROR]` line, which is the resource-log rule.
    - `verifyPackagedClientResources`: fails on 18 paths under `textures/item/old/sanguine formation/` (space in the folder name, from `08eea4acd`). That predates this step and no resources were touched.
- **Not verified (needs in-game or visual check):**
  - The F-3 crash path itself: completing a Will absorption on a dedicated server. The server now loads with `WillEntity` free of client references, but no GameTest completes an absorption.
  - The Will absorption pulse visual. The particle data is byte-identical (same type, colour, `outwardPulse = true`), so no review client was run.
- **Known issues (pre-existing, unrelated to this step):**
  - GameTests failing on the baseline: `selectedloomrecipesurvivesstockandchargedreload`, `hemojellyfloatsatsurfaceandfeedsonsmallfish`, `blockitemexceptionskeeptheirowners`, and `carrioncannothealfromoverkill` (flaky).
  - The `verifyPackagedClientResources` path failure above.
- **Handoff notes for step 2:**
  - Step 2 baseline: 3,919 JUnit tests with the 20 known failures. Also record the 4 GameTest failures and the packaged-resource failure above, since `alphaCheck` must be green at its end.
  - The GameTest task is `runGameTestServer`. `AGENTS.md` and the step prompts say `gameTestServer`, which Gradle does not have.
  - When step 2 adds `support/SourceAssertions`, move the two new architecture tests onto it.

### Step 2: Phase 0, green baseline and test infrastructure
- **Status / date / commits:** DONE, 2026-10-21.
  - `c435034d4` tracks `tools/model_export/`.
  - `ddb358304` contains the fixes, helpers and guardrails.
  - A third `consolidation(step-2):` commit records this entry.
- **Test counts before → after:**
  - `test`: 3,919 tests, 20 failed → 3,926 tests, 0 failed, 0 skipped.
  - `runGameTestServer`: 999 run, 3 failed (plus 1 flaky) → all 999 required tests passed.
  - `alphaCheck`: red → green.
- **The 20 JUnit failures, by cause:**
  - **Generator markers (6, owner decision M-1):** removed only the assertions that looked for `tools/model_export/<gen>.py` inside model JSON, in `HematicArmatureModelSourceTest`, `VialCentrifugeModelSourceTest`, `MnemonicReliquaryModelSourceTest` and `ModelFirstTileRendererResourceTest`. Structural checks stay. No generator has ever written that string. Every generator reproduces its committed JSON byte for byte.
  - **Encoding (3):**
    - `en_us.json`: 240 values double-encoded in `08eea4acd` were repaired (`Â§`→`§` ×469, `Â·`→`·` ×9, `â†’`→`→`, and `Thelēma`). Keys, order and line endings are unchanged.
    - Stray cp1252 `×` bytes were fixed in `docs/TESTING.md` and `docs/validation/PRIORITY_FINDINGS_2026_10_02.md`.
  - **Missing asset (1):** restored the five `textures/item/{covenant_mantle,silent_archon_*}.png` deleted in `08eea4acd`. They are byte-identical to the `textures/item/old/` copies. Their models had fallen back to missingno for particles and sprite lookups.
  - **Stale tests (10).** Each was re-pinned to current, intentional code or data, with the cause traced to a commit:
    - bombardier loot and hemojelly aquatic membership: `08eea4acd`
    - Chromatophores display name: `08eea4acd`
    - Vestment rig: `31e640b83`
    - graft-rite particle signature: `3fae723d0`
    - Vesper sickle grip: `31e640b83`. The test now checks that the sickle still renders through the item's hand transform.
    - planted-staff fist: generated fittings append the fist. The test now locates the 27-element run instead of using fixed indices.
    - utility polish: barometer shapes, crucible spout and basin constants, ossuary bounds (now a JSON bounds check), and the candle flame (now in the lit model)
    - loom renderer hooks: `dcdf960e5`
    - rename scan: reworded the retired pre-Lumenlace strain name in four tracked docs, and the scan now treats `docs/OrchestratorReports/` as an archive, like `docs/done/`.
  - **Stale count (1):** `LegacyMainTestSuiteContractTest` now compares the set of source-discovered `main()` tests with the adapter's set.
- **GameTests fixed (all test-side):**
  - `selectedLoomRecipeSurvivesStockAndChargedReload`: compares thread spans before and after reload. Engram Weaving stores 3 spans per enzyme, and payment spends none.
  - Hemojelly, saved-capture and siphonophore pool tests: their mobs are persistent, so a player in a concurrent test can no longer far-despawn them.
  - `blockItemExceptionsKeepTheirOwners`: compares exact id sets (17/6/7). `085166b7d` had removed two custom block items.
  - The flaky `carrionCannotHealFromOverkill` (and its choir and stillness siblings): the combat fixture now loads every chunk in the rectangle between its corners, not just the two corner chunks.
- **Other Phase 0 items:**
  - **Shared test support:** `support/RepoPaths`, `SourceAssertions` and `ResourceReader`. Converted tests: the two step 1 guardrails, `VestmentOfTheFinalMoltResourceTest`, `ArmorPreparationResourceTest`, `LivingWeaponGraftRiteSourceTest`, `PhlegethonticBombardierResourceTest` (partly) and `LegacyMainTestSuiteContractTest`.
  - **`tools/`:** `.gitignore` now ignores `/tools/*` except `tools/model_export/`, which holds 59 text generators, about 1.3 MB, with no secrets or user paths. `PuppeteerCardinalOrdealResourceTest`'s skill-tree-editor check moved to its own test, which `assumeTrue`-skips when the editor is absent.
  - **CI:** `alpha-check.yml` also runs on pushes to `neo-1.21.1`.
  - **Packaging:** `build.gradle`'s exclude for the unused `sanguine formation` frames now points at `textures/item/old/`, where `08eea4acd` moved them.
- **Metrics:**
  - Production: 240 lang values repaired, 5 textures restored, 1 build exclude corrected. No Java changed.
  - Tests: `src/test` +507/−179 across 23 files. Support and architecture: +338/−12. GameTests: +49/−20.
  - Private helper copies: 593 remain, and the ratchet holds them at that ceiling.
  - New tooling: `gradle/source-text-impact-map.py` (140 lines) and the 2,983-row CSV.
- **Guardrails added:** `RepositoryTextEncodingTest`, `PrivateTestHelperRatchetTest`, `SourceAssertionsTest`, and the rewritten `LegacyMainTestSuiteContractTest` (see the table).
- **Approved items implemented:** M-1 (drop the marker checks).
- **Pending approvals:** none new.
- **Deviations from the roadmap:**
  - M-1 turned out not to be "regenerate vs accept hand edits". There were no hand edits, so no model changed, and no visual check was needed.
  - `/tools/`: the roadmap offered "track or skip". Model generators are tracked, and only the editor check skips.
  - The local-only `docs/MANIPULATION_BALANCE_PLAN.md` was reworded and `docs/done/.../orcadian-horseman/README.md` was re-encoded, so local runs match CI.
  - Dedicated-server and client starts used throwaway game directories under `build/` (from a scratch init script), so the shared `run/world` save was not touched.
- **Not verified (needs in-game or visual check):**
  - The restored armour inventory textures are visible only as break and item particles. Their in-hand rendering comes from the 3D armour BEWLR and is unchanged.
  - The `§` colour codes in the repaired dialogue lines were not checked on screen.
- **Known issues (pre-existing, not fixed here):**
  - `models/item/hematic_microscope_viewing_left` references a missing `hemomancy:item/hematic_microscope` texture. The client logs it as a WARN.
  - `tools/model_export/station_kit.py` was edited after its outputs were last generated. Re-running it would change the glass of the lantern, scar station, incubator and crucible: the panes lose their thin edge faces. Re-run it deliberately, with a visual check.
- **Handoff notes for step 3:**
  - The baseline is fully green. From now on any new failure blocks.
  - Use the impact map before moving or deleting code, and use the `support/` helpers in every test you touch. Lower `PrivateTestHelperRatchetTest.CEILING` whenever copies are removed.
  - Never write the retired pre-Lumenlace strain name in any doc, including this ledger: the rename scan reads all of `docs/`.
  - The dedicated-server start used the same pattern as the review clients. A scratch init script added `consolidationServer` and `consolidationClient` runs with game directories under `build/`.

### Step 3: Phase 1, verified dead-code removal and packaging
- **Status / date / commits:** DONE, 2026-10-21. The code is in `ccc75e9a0`; a second `consolidation(step-3):` commit records this entry and the audit-table moves.
- **Test counts before → after:**
  - `test`: 3,926 → 3,928, 0 failures. One test was deleted with the dead code it pinned; three hygiene tests were added.
  - `alphaCheck`: green, with all 999 required GameTests passing.
  - Client start (shader reload): reached the title screen with 0 ERROR lines and no shader failures.
  - Dedicated server start: reached `Done (5.149s)` with 0 ERROR lines.
- **Removed (all SAFE items in audit section E, each re-verified with `git grep` and an impact-map pass):**
  - 54 unused Vesper clips (2,741 lines).
  - `RenderBloodLaser` and `RenderBloodLaserEvent`. This removes the per-frame `RenderLevelStageEvent` loop over all players.
  - The 4 empty classes, plus 9 unreferenced classes from the list.
  - `HemoBookReloadListener`, `MixinScreenRenderState` (an empty file), `AbstractFungusFeature`, `BigMushgloomFeature`, `FungusFeatureConfig`, and `TestRegion1` and `TestRegion2`.
  - These members: `Hemomancy.instance` and `forcesLoaded`, `AttributeInit.canFly`, `ScarInit.getAllEntries`, `VillagerInit.HEMOPOTHECARY_PROCESSOR_LIST_KEY`, `BaseFeatureInit.setup`, the empty `EffectInit.setupPotionRecipes` and `EntityInit.commonSetup` handlers, `BloodManipulation.performDrudgeAction`, `IBloodVolume.drainFromSource` (and its 2 implementations), and `BloodCraftingKeyPressPacket.getMatchBounds`.
  - 11 unused `RenderTypeInit` entries. `LASER_MAIN_ADDITIVE` became dead once the laser was removed.
  - `RadiantPortalRendertype.END_PORTAL`, `textWithRayShader` and their ray shard and transparency state.
  - The `ray`, `distorted_texture` and `scar_glow` shaders and their 6 program files. `screen_generic.vsh` is kept because 3 other shaders use it.
  - The `item_blood_volume` attachment registration. Item stacks cannot hold attachments in 1.21.1, and the only write was removed in `b363f22d7`. The item capability key with the same id is unchanged.
- **Packaging:**
  - The `.cache` exclude now matches; it is relative to each resource root.
  - `assets/hemomancy/**/old/**` and `models/entity/bbmodel/reference/**` are excluded. This replaces three narrower `old/` excludes.
  - The jar went from 95,622,844 to 91,543,031 bytes (−4.08 MB). Entries went from 12,348 to 12,131, and classes from 4,920 to 4,899. The jar now holds 0 `/old/`, 0 `.cache` and 0 reference entries.
- **Local cleanup:**
  - Deleted the 4 untracked `*.java~` backups. Each matched a committed revision of its file.
  - Deleted the tracked `bash.exe.stackdump`.
- **Metrics:**
  - `src/main`: +0 / −3,951 lines across 22 deleted classes, 6 deleted shader files, and edits to 20 other files.
  - `src/test`: +82 / −25 (the new hygiene test, plus 5 tests updated or trimmed).
  - Remaining legacy instances in scope: 0.
- **Tests updated with the removals:**
  - `RegistryBackedScarRefactorSourceTest`: dropped the `getAllEntries()` pin.
  - `BloodVolumeTransferRulesTest` and `BloodRoutingRulesTest`: dropped the `drainFromSource` stub overrides.
  - `MemoryWeavingRedesignSourceTest`: dropped the `LOOM_EFFECT_CORE` pins, which gave false coverage.
  - `AvatarFirstPersonRendererTest`: deleted `avatarShellDoesNotWriteDepthOverHeldItems`, which pinned the never-called `firstPersonEnergySwirl`.
- **Guardrails added:** `SourceTreeHygieneTest`, plus the authoring-file check in `verifyPackagedClientResources` (see the table).
- **Approved items implemented:** none needed. Everything removed was SAFE.
- **Pending approvals (moved from SAFE to REQUIRES VERIFICATION, recorded under M-3 and in the audit):**
  - `IBloodRoutingTarget`: `HEMOMANCY_REFERENCE.md` documents it as the routing hook.
  - `FeatureLogic`: the target of the W-2 setter merge.
  - The orphaned textures `textures/misc/laser.png`, `laser2.png`, `laser_glow.png`, `vine.png` and `textures/entity/chitinite/model_chitinite_warn.png`.
- **Deviations from the roadmap:**
  - The roadmap says to move the `/old/` archives and the reference sheet out of `src/main/resources`. The audit evidence and the existing `build.gradle` convention ("Preserve authoring archives in source") instead keep them in source and exclude them from packaging, which is what this step does. Deleting or relocating them is left to the owner.
- **Not verified (needs an in-game check):** none beyond the client and server starts. Every removal was unreferenced, so it has no visible effect.
- **Known issues (pre-existing, not fixed):**
  - `BloodCraftingKeyPressPacket` imports `HarbingerCardinalRiteEvents` without using it.
  - `IWhiteHumorVolume.drainFromSource` has no callers. It is not on the SAFE list.
  - `docs/done/consolidation/capability-storage-index.md:40` calls `item_blood_volume` "serialized".
  - `SourceTreeHygieneTest` reads only `hemomancy.mixins.json`. If the dormant MnA mixins are re-enabled, extend it to `hemomancy.mna.mixins.json`.
- **Handoff notes for step 4 (Phase 2, network hygiene):**
  - The baseline is fully green: 3,928 JUnit tests and 999 GameTests.
  - `PacketHandler.java` is pinned by about 23 tests. Check `docs/architecture/evidence/source-text-impact-map.csv` before moving registrations.
  - The step 1 guardrail `ClientBoundPayloadRegistrationTest` expects exactly one `playToClient` registration for each of the 4 F-2 payloads. Moving their handlers into `client/**` (Phase 2 item 3) is compatible with it.
  - Use the scratch init-script pattern from step 2 for the server and client starts, so `run/world` stays untouched.

### Step 4: Phase 2, network hygiene
- **Status / date / commits:** DONE, 2026-10-21. Ten commits on `neo-1.21.1`:
  - `d10e5e7f2`: `PROTOCOL_VERSION`, and the 18 never-sent payloads deleted
  - `3f86520b8`: registrations in their real direction
  - `b8062c743`: the send facade
  - `95cb2e1ef`: `ServerboundGuards`
  - `599762d61`: the VFX fold
  - `36c4558a1`: F-6
  - `e62941c32`: F-8
  - `3c3241f9a`: F-9
  - `060bf8423`: F-7
  - `018394712`: review fixes and docs

  A further `consolidation(step-4):` commit records this entry.
- **Test counts before → after:**
  - `test`: 3,928 → 3,939, 0 failures.
  - GameTests: 999 → 1,005 required, all passing in the final `alphaCheck`. See *Known issues* for flakiness that predates this step.
  - Dedicated server start: `Done (5.4s)`, 0 ERROR lines. Client start: title screen reached, 0 ERROR lines.
- **What changed:**
  - **Protocol version:** `PacketHandler.PROTOCOL_VERSION` is now `"3"`. Before this step it was effectively the mod ID. Mismatched client and server builds are refused at the handshake.
  - **Payload set:** 181 → 157.
    - 18 never-sent payloads deleted, plus `TeleportToVeinPacket` and the unreachable `RadialChooseVeinScreen`.
    - 8 single-purpose VFX payloads folded into `ParticleBurstPacket` and `EntityVisualPacket`.
    - Owner-approved; see *Decisions*.
  - **Directions:** 15 client-only bidirectional payloads are now `playToServer`. `AntecedentEffectPacket` is the one documented bidirectional payload. The `isClientSide` wrappers that existed only because of the bidirectional registration are gone.
  - **Client code in `common/network`:**
    - The 3 `PacketHandler` lambdas became `handle` methods.
    - 6 handlers read the level from `ctx.player()` instead of `Minecraft.getInstance()`.
    - `common/network` no longer references vanilla client classes.
  - **Sending:**
    - All 68 direct `PacketDistributor` calls in 45 files go through the `PacketHandler` facade (`sendToPlayer`, `sendNear`, `sendTracking`, `sendToAll`, `sendToDimension`, `sendToServer`).
    - Dialogue routing moved to `DialogueSender`.
    - The per-effect helpers are thin wrappers over the facade.
  - **Validation:** `ServerboundGuards` holds `menu`, `withinReach` / `nearBlock` / `nearBlockEntity`, `nearEntity`, `heldItem`, `finite` and `partialTick`.
    - 13 payloads use it; every other serverbound payload has a stated reason in `ServerboundGuardsCoverageTest`.
    - The identical cast-eligibility core became `BloodManipulation.isEquippedCastReady`, used by the charge visual and the casting animation. The other cast checks differ on purpose (paralysis flavour, availability, retirement) and were left alone.
- **Metrics:**
  - `src/main`: +971 / −2,452 lines, 28 files deleted and 6 added.
  - `src/test`: +637 / −105. `src/gameTest`: +362 / −2.
  - Remaining legacy instances:
    - direct `PacketDistributor` use outside the facade: 0
    - client references in `common/network`: 0
    - undocumented bidirectional registrations: 0
- **Approved items implemented:** the payload-set changes (N-6 / N-7), F-6, F-7, F-8 and F-9.
  - **F-9 behaviour note:** the Structure Spawner now needs operator permission. In a single-player world created without cheats, the owner is not an operator, so the spawner does nothing there.
  - **F-7 limit:** 3 messages per sender per 100 server ticks; the excess is dropped with an action-bar note.
- **Pending approvals:** none new. Two more orphaned textures were added to M-3.
- **Deviations from the roadmap:**
  - Roadmap item 3 asks to move the client handling of the 8 inline files into `client/**`. The 6 live ones only looked an entity up on the client level, so they now use `ctx.player().level()` (an existing side-agnostic handler style) instead of a new client class. The other 2 files were dead and are deleted.
  - Roadmap item 4 says to replace the per-effect helpers with generic `sendNear` / `sendTracking`. Tests and many call sites use the helper names, so the helpers stay as one-line wrappers.
- **Visual check (VFX fold):**
  - `PayloadFoldVisualReview` (opt-in review harness in `src/gameTest`) captured all nine effects through their unchanged `PacketHandler` helpers, before and after the fold.
  - Screenshots: `build/payload-fold-review/before/`, `after/`, and the side-by-side sheets `compare-0.png`, `compare-1.png` and `compare-2.png`.
  - Match confirmed: Monolith shatter, shard pulse, pome pulse and the Dislocation darkening look the same.
  - Not visible in either capture: the avatar-hit, flask, claw and tool-break particles (too small at that range) and Crimson Fire (it needs a burning entity). For those, the evidence is the verbatim client code and `VisualPayloadCodecTest`.
  - The run configuration came from a scratch init script (`payloadFoldReviewClient`, game directory `build/payload-fold-review`, username `FoldReview`).
- **Not verified (needs in-game check):**
  - Multiplayer: an old client against a new server, to see the handshake refusal.
  - F-7's feel in real chat.
  - The small VFX listed above.
- **Known issues (pre-existing, not fixed here):**
  - **Flaky GameTests.** The pre-step-4 commit `2d96be3a5`, run three times, failed `circussuccessionrunnercompletesatdegreefour`, `ferricrampartletsfriendlyshotsthrough`, `magnetismcrusheswhatitpulledwhenthepillarexpires` and `bloodcloudkeepsitscastmastery` in run 1, passed all tests in run 2, and failed `loadedduplicatevespersleaveoneownedboss` in run 3. During this step `completeexitcleansownedfixtureandoutput` and `mandatoryjigsawwingsassemble` also failed intermittently, then passed on rerun. A later step should stabilise them; they need their own investigation.
  - **Two payloads now arrive separately.** `BloodInjectionEvents` used to send its two sync payloads as one bundle; they now go as two packets in the same order.
- **Handoff notes for step 5 (Phase 3, player-state plumbing):**
  - Add every new payload through `PacketHandler`. Bump `PROTOCOL_VERSION` and rewrite `payload-registry.txt` whenever the set, a direction or a byte layout changes.
  - Use `ServerboundGuards` for serverbound checks, or add an exemption with a reason.
  - Send only through the facade.
  - For world-dependent fixtures, `NetworkDefectGameTests.connectedPlayer` shows how to capture every outgoing packet without adding the player to the level. A live fixture player makes parallel GameTests flaky.

### Step 5: Phase 3, player-state plumbing
- **Status / date / commits:** DONE, 2026-10-22. Five commits on `neo-1.21.1`:
  - `dceced30a`: S-4 accessors
  - `0e2d0678c`: S-8 session hub
  - `379401b57`: S-2 / S-3 sync table and dirty flags
  - `369780d0a`: S-5 `PendingDelivery`
  - `7911eaa1f`: review fix (Lantern Tick lights)

  A further `consolidation(step-5):` commit records this entry.
- **Test counts before → after:**
  - `test`: 3,939 → 3,959, 0 failures.
  - `alphaCheck`: green, all 1,007 required GameTests (1,005 before; 2 new). See *Known issues* for the intermittent failures seen along the way.
  - Opt-in suites run because this step touches their code: clinical (131: 129 pass, the same 2 fail at `86d2d8157`), succession (the same 2 fail at `86d2d8157`), casting animation 9/9, Antecedent 14/14, advanced brewing 7/7.
  - Dedicated server (`runConsolidationServer`, throwaway game directory): `Done (5.093s)`, 0 ERROR lines, no client-class loading. Client (`runConsolidationClient`): title screen, 0 ERROR lines, no shader failures. The runs came from a scratch init script with game directories under `build/`.
- **What changed:**
  - **Accessors (S-4):**
    - `HemoCapabilityAccess` player overloads return the attachment's own class straight from `getData`, so callers no longer bypass them to reach `serializeNBT` or other class-only methods.
    - `Entity`, `BlockEntity` and `ItemStack` overloads keep `Optional`. The 18 `require*` methods are gone (831 calls renamed).
    - About 1,000 `Optional` chains in main and gameTest were collapsed. A javac-driven rewriter handled the mechanical ones; 75 were rewritten by hand (guards inverted, returns kept, method extraction where a lambda returned early mid-method).
    - New `muscleMemory()` and `dialogueKnowledge()`. Direct `getData` reads: 106 → 16, all on the allow-list (block reservoir owner, retired-attachment migration, chunk attachments, one migration fixture).
    - `HarbingerEquipmentContainer` uses `getScar(stack)`. Block reservoirs reach their attachment through `IBloodReservoir.super.getBloodCapability()`.
  - **Session hub (S-8):**
    - `TransientServerStateEvents` clears every session map on server start and stop.
    - `endPlayerSession(Player)` runs on logout in a fixed order: teardowns that cancel, restore or move something, then plain removals. Handlers that ran for any `Player` still do; `ServerPlayer`-only ones keep their guard.
    - `ServerStoppedEvent` files in `common/`: 22 → 1 (the hub). `PlayerLoggedOutEvent` files: 27 → 8 (the hub plus 7 gameplay handlers).
    - 62 UUID-keyed static collections in 43 classes are covered; 3 are exempt with reasons.
    - Newly cleared on start and stop: the flow ledger, Lantern Tick lights, fane previews, thurible yaw, discharge roots, pending blood crafts, avatar states, thrall picks and NPC markers.
  - **Sync table (S-2 / S-3):**
    - `PlayerStateSync` lists 22 entries over 20 attachments, each with its payload and the lifecycle events that resend it.
    - `HemoCapabilityRegistrar` sends the table at `LOWEST` priority, so each system's own lifecycle work (migrations, grants, clears) runs first and every entry goes out once, with its final state.
    - The sync halves of the 17 trios and the four `EntityJoinLevel` resyncs are gone. `EntityJoinLevelEvent` files: 7 → 4.
    - `KnownManipulations` and `StationUpgradeProgress` raise a dirty flag on real change, which the end-of-tick flush sends. This replaces the per-tick serialise-and-diff and the 20-tick station resync. The `KnownManipulations` mutation paths outside its setters (normalisation inside reads, retirement sanitising, the learn helper) now mark the flag too.
    - Direct `KnownManipulationServerPacket` sends: 25 → 2 (`syncPlayerEvent` and the decoder), so a change is no longer sent twice.
  - **Pending delivery (S-5):**
    - `PendingDelivery` owns the shared rule: living player, not observational, count what actually landed, keep the remainder. One driver retries on the 20-tick tick, login and respawn.
    - Six claims were migrated: First Draws vials, First Separation briefing vials and tools, Mycophant Tendril, Vesper Memory and Communion Spine. Each keeps its NBT through its own read and settle functions.
    - Special rules stay with the caller: the Spine's carried-Spine rule is its `held` predicate; Vesper's root-key migration stays in its read, still before the alive check; the tools keep unparseable entries.
    - The five hand-copied count-delta insertions and four per-system drivers are gone.
- **Metrics:**
  - `src/main`: +4,622 / −5,195 across 387 files. Classes added: `PlayerStateSync`, `PendingDelivery`. Deleted: `EngramEvents`.
  - `src/test`: +456 / −31. `src/gameTest`: +1,202 / −1,047 (mostly the accessor rewrite), plus the shared `CapturedPlayer` fixture.
  - Remaining legacy instances:
    - `Optional` player accessors: 0
    - `require*` calls: 0
    - unregistered UUID maps: 0
    - private server-stop handlers in `common/`: 0
    - lifecycle resends outside the registrar: 0
    - count-delta insertions outside `PendingDelivery`: 0
- **Guardrails added:** see the table (`AttachmentAccessTest`, `SessionStateRegistryTest`, the subscriber hygiene check, `SessionStateGameTests`, `PlayerStateSyncTest`, the owner-snapshot table GameTest, `PendingDeliveryTest`, `PendingDeliveryRegistryTest`).
- **Approved items implemented:** none. F-4, F-5, F-19 and F-20 are still `PENDING` and were left alone.
- **Pending approvals:** F-4, F-5, F-19, F-20, plus two new candidates found here, S-2b and S-5b (see *Decisions*).
- **Deviations from the roadmap:**
  - **Concentrated Blood has no item claim.**
    - Its `PENDING` flag is a rest state between injection and sleep, and its only hand-out (`replace`) is add-or-drop with nothing stored.
    - Making it a claim would add an NBT key and change the Creative-discard outcome, so it is S-5b instead.
    - Its `PlayerEvent.Clone` handler was removed: `ServerPlayer.restoreFrom` copies `PlayerPersisted` (the same tag) before Clone fires, so it was a no-op.
  - **Logout removal is not added everywhere.** It covers maps that were already dropped on logout, plus presentation and diagnostics state (fane preview, flow ledger, thurible yaw, Lantern Tick light). Cooldowns and timers that must outlast a relog, such as Still Art cooldowns and thurible resonance, stay until the server stops; adding logout removal would let a relog reset them.
  - **Station lessons keep a slow cadence.** Lesson eligibility moves with degree, blood activation and an advancement that change elsewhere, so the lessons keep their login and 20-tick backfill. Only the state packet moved to the dirty flag.
  - **A new facade route.** `PacketHandler.sendToPlayerAndTrackers` (owner plus trackers) replaces `sendTracking` in the four owner-and-tracker syncs. In game it reaches the same players; it also reaches an owner who is not yet tracked, which is what makes the table GameTest possible. `PayloadDirectionTest` knows the new name.
  - **Timing:** dirty state goes out at the end of the same tick, where the station resync took up to 20 ticks, and the known-manipulation duplicate send is gone. Pending claims also retry on respawn and login; the Spine and most claims used to wait for the next 20-tick tick. Vesper's login delivery no longer runs after its arena reconcile in the same handler; if it misses there, the 20-tick retry delivers it.
  - **The step was split into four reviewed sub-commits plus a review fix,** each green.
- **Final review:** one reviewer for the whole range plus a dedicated 5c pass.
  - **Confirmed (fixed in `7911eaa1f`):** clearing the Lantern Tick map on server stop could orphan a helmet light placed as a single-player world closed. Logout now removes it while the level is loaded. A guardrail locks this in.
  - **Low-risk, accepted:** a few setters mark `KnownManipulations` dirty by reference or unconditionally, which means at most one extra end-of-tick resend. First Separation tools now store unparseable entries after the remaining ones instead of in their old positions.
  - Everything else was checked clean: inlined returns, `orElse` defaults, `isEmpty` semantics, shadowing, precedence, frozen identifiers and client isolation.
- **Verified afterwards in a live multiplayer session (2026-10-22):** one dedicated server and two driven clients, using the opt-in `/hemoverify` command (`-Dhemomancy.mpVerify=true`) and the driver's `connect`, `peers` and `screen` actions, both in `src/gameTest`.
  - **Tracking:** player A was given a fungal scar, an equipped morphling, two active muscle memories and jar, charm and gourd equipment. Player B's client copy of A matched the server's copy exactly after each of these:
    - the initial change;
    - layer-visibility toggles;
    - A's respawn;
    - A's trip to the Nether and back, in both orders (B arriving first and A arriving first);
    - A's relog;
    - B's relog;
    - removing everything and putting it back.
  - Screenshots from B show the layers rendering. The morphling and active muscle memories leaving on death is the existing death rule (the morphling goes back to its jar).
  - **Chamber relog, case 1:** all six claims were recorded during a guided visit while the player had free space. They stayed owed through the visit, and the player relogged mid-visit. Everything arrived once at login in the Overworld, the player's own items were untouched, and later retries added nothing.
  - **Chamber relog, case 2:** the real inventory was full and only the visit copy had space. Nothing landed in the copy. After the relog the inventory and equipment came back intact and every claim was still owed. All of it arrived once on the first retry after real space was freed.
  - **Screens:** each of these opened with synced data and threw no errors:
    - bloodline pool, ledger, vascular status;
    - Harbinger and Unstained progress;
    - synaptic loadout, mnemonic reliquary, morphling jar, Puppeteer's spindle;
    - scrying diagnostics, tendency view, vascular view;
    - the blood volume and Unstained gauge overlays.
  - The manipulation and Still Art radials opened and drew their opening frames, then closed because the driver cannot hold a physical key.
  - The server and both client logs had 0 ERROR lines.
- **Known issues (pre-existing, not fixed here):**
  - **Intermittent GameTests in the combined run.** `mandatoryjigsawwingsassemble` failed in 4 of about 10 runs this session. It passes alone (`runRegistrationGameTestServer`) and failed at `0e2d0678c` without the later changes. Also seen once each: `harbingerrunneradvancesthroughhermitroadwithoutopeningscreens`, `obscuredmeleemobcanpursueandhitanearbyenemy` (at `86d2d8157`), and the step-4 cluster `circus*runnercompletesatdegreefour`, `ferricrampartletsfriendlyshotsthrough`, `magnetismcrusheswhatitpulledwhenthepillarexpires` and `bloodcloudkeepsitscastmastery`. Missing entities and wrong combat values suggest batch-mates sharing chunks; a later step should isolate them.
  - **Opt-in suites:** clinical fails `journeyacceptssilentchoiceafterrealprojection` and `journeyrestoresnumericfungalprojectionstate`; succession fails `legacyvialsarerejectedbeforeconsumption` and `manufactureconsumespatternandtwohundredfiftyblood`. All four fail identically at `86d2d8157`.
  - **`runGameTestServer` can report `BUILD SUCCESSFUL` when the server crashes during mod loading.** Seen once with an idle `@EventBusSubscriber`. Always read the `GAME TESTS COMPLETE` line.
  - **Line endings:** the working copy is LF while `core.autocrlf=true`. Checking out another commit rewrites changed files as CRLF, which breaks the raw multi-line source pins in several legacy `main()` tests. Avoid checkout round-trips, or convert the touched files back to LF.
- **Handoff notes for step 6 (Phase 4, shared small utilities):**
  - The baseline is fully green: 3,959 JUnit tests and 1,007 required GameTests.
  - **Player state:** read through `HemoCapabilityAccess` (no `Optional` for players). Add a new synced attachment as a `PlayerStateSync` entry, not a lifecycle handler. Register new session maps with `TransientServerStateEvents` (clear plus, if appropriate, logout). Deliver earned items with a `PendingDelivery.Claim`.
  - **Fixture:** `gametest/CapturedPlayer` is a shared connected player that captures payloads and stays out of the level. 58 older GameTest files still carry private copies of that fixture; folding them in is test cleanup for a later step.
  - **Rewriting tools:** the javac-driven rewriter used for 5a (`jchain.py`, `rewrite.py`) lived in the session scratchpad and was not committed; re-derive it if another mass `Optional` change is needed.

### Step 6: Phase 4, shared small utilities
- **Status / date / commits:** DONE, 2026-10-22. Six commits on `neo-1.21.1`, across two sessions:
  - `48127a923`: A-2 `HemoEasing`
  - `f200d6e1b`: P-1 / G-5 `AttributeModifiers`, G-6 `ArmorSets`, O-3 `HemoTickers`, L-7 `BlockEntitySync`
  - `a288c3fba`: S-6 / D-3 `ProgressionGates`
  - `73dda530e`: L-4 `PatternOrigins`
  - `576aab6c3`: W-2 `FeatureLogic` setters
  - `07d4a4183`: review fixes

  A further `consolidation(step-6):` commit records this entry.
- **Test counts before → after:**
  - `test`: 3,959 → 3,979, 0 failures.
  - `alphaCheck`: green, with all 1,011 required GameTests passing (1,007 before; 4 new).
  - Opt-in suites run because this step touches their code:
    - antecedent 14/14, advanced brewing 7/7, scriptorium 22/22, living weapons 10/10, Excoriated spawn 1/1.
    - Clinical 131 and succession 22 each fail the same 2 known tests as at `86d2d8157` (see step 5).
  - No dedicated-server or client start was run separately. The dedicated server loads in every GameTest run, and no resources, shaders or registrations changed.
- **What changed:**
  - **Easing (A-2):** `HemoEasing` holds the cubic smoothstep, `lerp(a, b, t)`, `clamp01`, `pulse`, crossfade and the cubic eases, in float and double forms.
    - Copies that genuinely differ are named variants: `smoothstepUnclamped` and the quintic `smootherstep`.
    - About 100 private copies in 81 classes are gone. `lerp(t, a, b)` callers had their arguments reordered.
    - The only value differences are negative zero against positive zero, as listed in `48127a923`.
  - **Attribute modifiers (P-1 / G-5):** `AttributeModifiers.sync` / `syncPermanent` do nothing while a modifier is unchanged.
    - It replaced ten remove-then-add copies: muscle memory, High-Strung, body refinement, Emberfang heat, avatar form, chitinite toughness, pelagic motion, Marrow Crown, brew poise and scar synergy (permanent), plus Excoriated clotting.
    - The Elytra flight modifier no longer comes off and goes back on every tick on both sides. It is held while the chestplate can glide or Fungal Elytra is active, which was its end-of-tick state before. This ends a flight-attribute update packet every tick for every player wearing an elytra.
    - Chitinite toughness also stops re-adding its modifier on every equipment change when the toughness cap clamps the bonus.
  - **Armour sets (G-6):** `ArmorSets.isFullSet` handles exact pieces, a list of helms, a slot map and a whole material. `ArmorSetHelper`, the material count in `ArmorSetBonusHandler`, `SimpleArmorSetAbility` and both Silent Archon copies use it. The public method names stay; the source tests pin them.
  - **Tickers (O-3):** `HemoTickers.create` replaced the 8 private `createTickerHelper` copies and the 10 raw ticker casts. The three suspended-crystal blocks had empty tickers and now have none.
  - **Block-entity sync (L-7):**
    - `BlockEntitySync.markAndSync` is the one save-and-resend body. 27 per-class `sendUpdates` / `sync` / `markDirtyAndSync` / `setChangedAndSync` bodies (seven variants) are gone, and about 180 call sites were rewritten.
    - The bodies differed only by `setBlocksDirty`, which does nothing on the server and duplicates `sendBlockUpdated` on the client.
    - `IBloodReservoir` and `IWhiteHumorTile` keep `sendUpdates` as a default that delegates, and `FillerBlockEntity` still forwards to its main reservoir.
    - Immediate `VanillaPacketDispatcher` sends stacked on a sync were dropped: Scar Station use and craft, the Somatic Loom and Effigy scar selection. The update now goes out with the chunk's block changes at the end of the same tick. The immediate sends right before a menu opens stay.
  - **Progression gates (S-6 / D-3):**
    - `ProgressionGates.hasDegree` / `isDegree` (for a player or a degree state), `isBloodActive` and `unstainedStage`.
    - The 64 literal degree comparisons in 39 files read through it. Degrees are integers, so every rewrite gives the same answer.
    - The Unstained stage ladder moved out of `HemoCapabilityAccess`, and `RecipeDegreeGates` reads it from `ProgressionGates`. Annetta's two blood-active helpers use `isBloodActive`.
  - **Pattern origins (L-4):** `PatternOrigins` holds `originForCell`, `offset`, `contains`, `bounds` and the two searches the solvers already used.
    - `findContaining` is the key press's search: orientation first, then pattern cell.
    - `scanAround` is the cube scan: origin first, from the low corner, with a predicate. Projection crafts and Working-craft subjects require the match to cover the hit; rite re-verification and consumption take any nearby match.
    - The two orders pick different placements when matches overlap, so both stay. `PatternOriginGameTests` recorded every solver's winner against the old code before the move, and still pass.
    - Migrated: the key-press packet (solver, origin arithmetic, inline bounds, duplicate alignment check), `BloodStructureCraftingHelper`, rite re-verification, `CardinalRiteStationMatcher`, Succession's floor re-match, `WorkingCraftSubjectMatcher`, the feed manager and `CardinalRiteFloorGeometry`.
  - **Worldgen (W-2):** `FeatureLogic` gained `placeIfReplaceable`, `placeIfSubmerged` and `isWaterSource`, shared by the Spore Nexus tower, termite mound, deep-ocean vent and Erythrocoral reef. The noise helpers are untouched.
- **Metrics:**
  - `src/main`: about +1,740 / −2,320 across about 265 files. 7 classes were added in `common/util`; no class was deleted.
  - `src/test`: +410 / −23 (2 guardrail and 2 unit test files). `src/gameTest`: +213 (2 files).
  - Remaining legacy instances:
    - private scalar easing copies: 0 outside the reasoned allow-list (2)
    - hand-written modifier writers: 2, allow-listed (Veinwing penalty, Scar equip)
    - private ticker helpers and raw ticker casts: 0
    - per-class sync helper bodies: 0
    - literal degree comparisons: 0
    - private origin solvers: 0
- **Guardrails added:** see the table (`EasingCopyGuardrailTest`, `HemoEasingTest`, `SmallUtilityGuardrailTest`, `ProgressionGatesTest`, `AttributeSyncGameTests`, `PatternOriginGameTests`).
- **Approved items implemented:** none needed; nothing in this step was marked APPROVAL.
- **Pending approvals:** none new. M-3's `FeatureLogic` entry is resolved: the W-2 merge put it back into use, so it is no longer dead code.
- **Deviations from the roadmap:**
  - **`PatternOrigins` keeps two search orders.** The roadmap expected one origin solver. The key press and the cube scans disagree on overlapping matches, so merging them would change which structure is crafted or consumed. Both are now one utility with named orders, and the winners are locked.
  - **Unstained "ladder copies":** only `getPlayerUnstainedLevel` was a stage ladder.
    - The other four sites check raw purity or clarity thresholds: the Stillwater Condenser, Zealot dialogue, Pale Intercession (clarity 75, which is not a stage boundary) and observance readiness.
    - Nothing enforces purity before clarity, so mapping them to stages would change their answer when clarity is unlocked below full purity. They stay.
  - **`isBloodActive`** replaced the private helper copies only. Inline `getBloodVolume(player).isActive()` reads, which usually hold the volume for other use, stay.
  - **`RecipeDegreeGates`** stays the recipe layer. It reads player levels through `ProgressionGates`, rather than `ProgressionGates` wrapping it.
  - **Named gate constants:** degree comparisons against constants or variables (`StationUpgradeProgress`, `UnlockPredicate`, `ScarBrazierRite` and others) were out of the literal scope. The guardrail only forbids literals.
  - **Saint Sarcophagus** keeps its server-guarded `sendBlockUpdated` calls. Two of them do not mark the block entity changed, so routing them through `markAndSync` would add a save.
  - **Unclamped ease variants** (`ClawSlashRenderer` and `SanguineMonolithShatterRenderer` `easeOutCubic`, `WorkingCraftDaemonPath` `easeInOut`, `PuppetStringsRules` `easeOut`) differ from `HemoEasing` and stay local.
- **Final review:** one reviewer read the whole range.
  - It found no bugs and no unintended behaviour change.
  - Fixed in `07d4a4183`:
    - The Mycophant and Vesper mount animation templates still used the removed helpers, so regenerating would have broken the build. Both generators now reproduce the committed models.
    - A duplicate import broke the Somatic Loom file's CR CR LF endings.
    - Three inline save-and-resend bodies and two inline setters were left over.
  - Accepted: a Scar Station opened while two players already use it no longer sends an update. Nothing changes on that path.
- **Not verified (needs in-game check):**
  - Elytra and Fungal Elytra gliding feel, in multiplayer. The attribute end state is unchanged and `AttributeSyncGameTests` covers the sync.
  - Station screens that lost a stacked immediate send: Scar Station craft and Effigy scar selection.
- **Known issues (pre-existing, not fixed here):**
  - `mandatoryjigsawwingsassemble` failed once in a combined run again, and passed alone (9/9 in `runRegistrationGameTestServer`).
  - `crossingdimensionsdeliberatelyunravelssummon` (40-tick haul timeout) failed once in a combined run and passed on the rerun and in four other runs.
  - Clinical and succession opt-in failures as in step 5.
- **Handoff notes for step 7 (Phase 5, blood accounting):**
  - The baseline is fully green: 3,979 JUnit tests and 1,011 required GameTests.
  - Use the `common/util` helpers (AGENTS.md lists them). `SmallUtilityGuardrailTest` fails on a new private copy or a literal degree comparison.
  - Blood-active checks have `ProgressionGates.isBloodActive(player)`.
  - Generated models: when a shared helper replaces code in a generated class, edit its `tools/model_export/*_anim.java` template and imports too, then re-run the generator and confirm it reproduces the class. `EasingCopyGuardrailTest` now scans those templates.
  - Line endings in the working copy are mixed (LF, CRLF and the loom's CR CR LF). Edit with tools that keep each file's own endings, and check `git diff` for whole-file rewrites.

### Step 7: Phase 5, blood accounting
- **Status / date / commits:** DONE, 2026-10-22. Implementation `8338c98da`. A further `consolidation(step-7):` commit records this entry.
- **Test counts before → after:**
  - `test`: 3,979 → 3,993, 0 failures.
  - `alphaCheck`: green, with all 1,016 required GameTests passing (1,011 before; 5 new).
  - Opt-in suites run because this step touches their code:
    - scriptorium 22/22, advanced brewing 7/7, living weapons 10/10.
    - Clinical 131 and succession 22 each fail the same 2 known tests as in steps 5 and 6.
    - The succession failure `manufactureconsumespatternandtwohundredfiftyblood` feeds blood through `BloodStructureFeedManager`. Its payment is clamped to the player's blood, so `pay` behaves exactly as the old `drain` did, and the assertion fails as it did at `86d2d8157`.
- **What changed:**
  - **`BloodAccounting` (G-3).** It lives in `capability/player/harbinger/bloodvolume`, beside `BloodFlowLedger`.
    - `pay(player, amount, Source)` is a one-off payment with `drain()`'s semantics. It has no active gate, and asking for more than the player holds empties the volume and is not satisfied. An amount of 0 or less owes nothing and is satisfied, as `drain(0)` was.
    - `payUpkeep(player, amount, Source, intervalTicks, requireFullAmount)` is the old `BloodFlowLedger.applyDrain`, moved verbatim. A dormant volume pays nothing, and `requireFullAmount` refuses a short payment.
    - The pure `BloodPaymentRules` holds the arithmetic.
    - `Source{id, label, category, countsAsSpend, reserveProtected}`:
      - `countsAsSpend` adds a satisfied payment to the Hemorath blood debt.
      - `reserveProtected` applies Sanguine Reserve's 15% floor while the technique is enabled.
      - `Source.of` labels a source from its id.
    - Every payment records a diagnostics row through `BloodFlowLedger.recordApplied`. When blood actually left the volume, it ends in `BloodVolumeEvents.syncVolume`.
  - **Migration.** 74 `pay` and 12 `payUpkeep` call sites, each holding a named `Source` constant (80 in all). Families covered:
    - armour abilities, the Silent Archon refusal and Covenant Mantle
    - blocks: Bloodwell, Covenant Throne, Vigil, construct birth, Bloodwood, Saint Sarcophagus, brazier lighting
    - items: brew dose, Scriptorium inscription, memory shard, Vein Spider, memory tools, thrall, Torch, Living Staff hot swap, succession self-sample
    - the Living Axe, Blade, Crossbow, Tool and Grasp
    - morphlings: primal ability, Dead Man's Purse, Lumenlace, upkeep, starvation
    - scar upkeep and the scar weapon bleed
    - High-Strung threads, Will redirect and banishment
    - manipulations: the cast and Crimson Tithe debt
    - block entities: loom burnt spans and ritual charge, Effigy motif, Morphling Cradle draw, Visceral Mirror, Blood Pylon, Blood Routing owner draw
    - packets: structure craft, formation, synaptic loadout, donation, skill unlock, ash trail
    - rites: Cardinal node cost, ordeal threat, boundary, legacy completion, Puppeteer trial, Scar Brazier rite, infusion, structure feed
    - Condenser, Earthen Vein and Fungal Gardens travel, dictation
    - hostile drains: wound (the damage drain), Mycophant claim and siphon, Desiccant, Lantern Tick, frayed thread, Stained Priestess, Velorum
    - the 12 former ledger drains, each keeping the reserve flag its category used to imply
  - **Flags reproduce each site's previous behaviour.**
    - Only the wound drain and the old ledger categories (morphling, armour, organ, tool, bloodline, muscle memory) are reserve-protected.
    - The 13 sites that called `addBloodSpend` count as spend.
    - `HemorathEntity.onPlayerBloodSpend` stays at its 2 call sites.
    - Guards before each payment, including the missing `isActive` checks, are unchanged.
  - **One sync form (L-2).**
    - The 52 bare `BloodVolumeServerPacket` sends are gone. Payment sites now sync through `pay`, and income and bloodline sites call `syncVolume`. That includes `BloodVolumeClientPacket`'s refresh reply.
    - `AfflictionDigestHelper` and `ConserveStateHelper` had dead `syncBlood` helpers, now removed. `RootedStateHelper`'s duplicated the sync `CirculationIncomeHelper.grant` already sends and is removed too.
  - **`ProjectedBloodSink` (L-3).**
    - `BloodProjectionItem`'s filler / Forge / Loom / Effigy / generic `instanceof` chain is now one `instanceof ProjectedBloodSink` dispatch.
    - `IBloodReservoir` extends the sink. Its default fills the reservoir's raw `BLOCK_BLOOD_VOLUME` attachment with `BloodVolumeTransferRules.transferableAmount`, exactly as the old generic path did.
    - The Resonant Forge (hammer repair), Somatic Loom (ritual charge only), Masons Effigy and `FillerBlockEntity` (forward to the main block) override it.
    - `IBloodVolume.fillFromSource` and its rule had no callers left and are deleted; `BloodVolumeTransferRulesTest` now covers `transferableAmount`.
  - `BloodFlowContribution.Category` gained `CAST`, `RITE`, `STATION` and `HAZARD` at the end. It is written with `writeEnum`, so the payload layout is unchanged.
- **Metrics:**
  - `src/main`: +422 / −489 in 98 tracked files, plus 3 new classes (`BloodAccounting`, `BloodPaymentRules`, `ProjectedBloodSink`; 168 lines). `BloodFlowLedger.applyDrain`, `DrainResult`, `reserveProtectedCategory`, `fillFromSource` (interface, 2 implementations, rule) and 3 `syncBlood` helpers are deleted.
  - Call sites migrated: 69 direct player drains, 2 indirect drains, 6 subtract/set bypasses, 12 ledger drains, 52 bare packet sends.
  - Remaining legacy instances: 0 player drains outside `BloodAccounting`. The guardrail's allow-list holds only station, reservoir, gourd and white-humor tanks. `compat/mna` is not compiled and keeps 4 raw drains.
  - Tests: 2 new unit and guardrail files, 1 new GameTest file; 7 source tests updated.
- **Guardrails added:** `BloodPaymentRulesTest`, `BloodAccountingGuardrailTest`, `BloodAccountingGameTests` (see the table).
- **Approved items implemented:** none. F-12, F-13, F-14 and F-15 are all PENDING, so none was built, and `HEMOMANCY_REFERENCE.md` / the wiki needed no balance updates for them.
- **Pending approvals:** F-12, F-13, F-14, F-15. Each is now a one- or two-line change; the *Decisions* notes say where.
- **Deviations from the roadmap:**
  - **`Source` has a label.** It is a fifth component beside the four the roadmap names. The diagnostics already showed a label per source (muscle memory uses the memory's display name); `Source.of` derives it from the id otherwise.
  - **Two entry points.** `pay` keeps `drain()`'s semantics; `payUpkeep` keeps `applyDrain`'s (dormant gate, interval, full-amount refusal). One method could not reproduce both without a behaviour change at about 20 sites that never checked `isActive`.
  - **`ProjectedBloodSink`'s default does not ask `canReceiveBlood()`.** The roadmap's "defaulting to `receiveBlood`" would have implemented F-12, which is pending. It uses `receiveBlood`'s transfer arithmetic on the raw attachment instead.
  - **The sync half of F-13 arrived.** Scar upkeep now ends in `syncVolume` like every payment. It already did each tick for the Living Torch and the ledger drains. Its reserve protection, the balance half, is untouched.
  - **Network and diagnostics differences:**
    - Payments sync the owner (blood, flow, max-blood and manipulation diagnostics) wherever blood moved. That includes about 15 sites that sent nothing before, such as scar upkeep, the Visceral Mirror, Lantern Tick bites, dictation and the boss drains.
    - Syncs that carried no change are gone: projection at air, a zero wound drain, and no-op block-entity resends when no blood moved.
    - One-off payments now show for their tick in Scrying Diagnostics. `HEMOMANCY_REFERENCE.md` §2.3a and `wiki/Blood-Systems.md` say so.
  - **Spend counting** adds the requested amount on a satisfied payment. Sanguine Spinning used to add `actual`, which differs only at the ulp level.
  - **`LinkedPlayerSource`** in `BloodRoutingRules` drains a player volume but has no callers. It stays, allow-listed with that reason, as a dead-code candidate for M-3.
- **Final review:** one reviewer compared every migrated site against `HEAD`.
  - It found no gameplay change, no frozen-identifier change, no client reach and no line-ending damage.
  - Fixed after the review:
    - The Living Axe GameTest missed the 50% Living Tool price `LivingAxeItem` inherits through `super.hurtEnemy`, so it now accepts 75 or 100 mL.
    - Three dead locals and two dead imports.
    - Muscle memory was missing from the reference's reserve list.
- **Not verified (needs in-game check):**
  - Scrying Diagnostics with the new one-off rows and categories.
  - Network cost of per-tick payment syncs (scar upkeep, Visceral Mirror, Living Torch) in multiplayer.
  - Projection into each station by hand: Forge hammer, Loom, Effigy, a filler-backed multiblock.
- **Known issues (pre-existing, not fixed here):**
  - Combined `alphaCheck` runs failed one flaky test each in two of the four runs: `loadedduplicatevespersleaveoneownedboss` (already listed in step 4) and `runnerlatchesfailureandretainsinspectionstate` (a journey fixture start). Both passed in the other runs; the third and fourth runs were fully green.
  - Clinical and succession opt-in failures as in step 5.
- **Handoff notes for step 8 (Phase 7, registration and client registration):**
  - The baseline is fully green: 3,993 JUnit tests and 1,016 required GameTests.
  - A new blood cost is `BloodAccounting.pay(player, amount, SOURCE)` with a `private static final BloodAccounting.Source` constant. `BloodAccountingGuardrailTest` fails on a raw player drain, `subtractBloodVolume`, `addBloodSpend` or bare `BloodVolumeServerPacket`.
  - A block entity that takes projected blood implements `ProjectedBloodSink`; an `IBloodReservoir` gets the default.
  - Approving F-12, F-13 or F-15 is now a flag or one-line change at the sites named in *Decisions*. F-14 (passive scar effects and milk) is untouched by this step.

### Step 8: Phase 7, registration and client registration
- **Status / date / commits:** DONE, 2026-10-22. Two commits on `neo-1.21.1`:
  - `3e8b213d4`: 8a, registration and client registration (items 1-8 of the prompt)
  - `291731d6f`: 8b, client code out of `common/` (the guardrail)

  A further `consolidation(step-8):` commit records this entry.
- **Test counts before → after:**
  - `test`: 3,993 → 4,004, 0 failures.
  - `alphaCheck`: green, with all 1,018 required GameTests passing (1,016 before; 2 new), `verifyGameTestResourceLog` and `verifyPackagedClientResources`.
  - `runRegistrationGameTestServer`: 11/11, including both frozen snapshots.
  - The `gameTestServer` run asked for is the dedicated-server GameTest run inside `alphaCheck` (`runGameTestServer`; the task has no other name). It loads every moved class on a dedicated server.
  - Client (`runConsolidationClient` from a scratch init script, game directory `build/consolidation-client`): loaded to the title screen (sound engine started) with 0 ERROR lines and no shader, particle-provider or renderer warnings. The only Hemomancy warnings are the old `SynchedEntityData.defineId` notices.
- **Frozen identifiers (new):** step 2 never added the snapshot the prompts refer to, so this step did. `FrozenIdentifierGameTests` lists every Hemomancy id in every loaded registry (static, NeoForge custom and datapack: 1,991 ids) and both creative tabs in display order (693 lines). The committed lists were generated by running that GameTest on `de8e25ba1` in a temporary worktree (`HEMOMANCY_FROZEN_SNAPSHOT_OUT`), and the changed tree matches them exactly. Registry ids and creative-tab order are therefore proven unchanged.
- **What changed (8a):**
  - **Subscribers (R-1):** 40 annotations normalised to `@EventBusSubscriber(modid = Hemomancy.MOD_ID[, value = Dist.CLIENT])`; the six `bus = MOD` arguments and the two modid-less forms are gone. The `Hemomancy` constructor keeps only `commonSetup`, the GameTest hook and the reload listeners.
    - The level tick (`ChamberOfWillManager.onLevelTick`) and server start (`EngramTextureCache.onServerAboutToStart`) became static subscribers at `HIGH`, so they still run before the other `NORMAL` handlers, as the constructor listeners did.
    - `LiberDiscoveryEvents.commonSetup` and `UnstainedProgressEvents.commonSetup` are called at the end of `Hemomancy.commonSetup` instead of from two wrapper lambdas, keeping their order. `MorphlingMutationRegistry.init` moved to client setup.
    - `PacketHandler.registerChannels`' misnamed `forgeBus` parameter is now `modBus`.
  - **Creative tabs (R-2):** `common/init/CreativeTabInit` holds both tabs and one ordered WIP list (42 items, 17 blocks) that fills the WIP tab and keeps the same entries out of the main tab. `BlockInit.WIP_BLOCKS` is gone; `itemDecision` asks `CreativeTabInit.isWipBlock`. The ids are compared by holder id, so the block check works while items are still registering. F-22 is not implemented.
  - **Registry lookups (R-3):** `ManipulationInit.getByName` and `StillArtInit.getByName` use `common/util/RegistryLookup.byName`, which uses `getOptional` so a defaulted registry never answers an unknown name with its default. All 123 manipulation and 9 Still Art names equal their registry paths, so every lookup returns the same object. A null name now gives null / `BLANK` instead of an NPE.
  - **Client registration (R-4):** 50 block-entity renderers via `registerBlockEntityRenderer`, 12 particle providers via `registerSpriteSet` (`client/registry/ParticleProviderRegistrar`), and the three fluids' textures and tints via `RegisterClientExtensionsEvent` (`FluidClientExtensions`), replacing `FluidType.initializeClient`. Each event method calls the same vanilla registry the old code did.
  - **`ClientEvents` split (R-5):** `client/registry/` now holds `KeyMappings`, `EntityRendererRegistrar`, `ModelRegistrar`, `MenuScreenRegistrar`, `GuiLayerRegistrar`, `ClientExtensionsRegistrar`, `ItemPropertiesRegistrar`, `ItemColorRegistrar`, `ItemDecorationRegistrar`, `DimensionEffectsRegistrar`, `RenderBufferRegistrar`, `RecipeBookRegistrar`, `ClientReloadListenerRegistrar`, `ClientSetupRegistrar` and `ParticleProviderRegistrar`. The runtime handlers live in `client/event/{ClientTickEvents, ClientInputEvents, ManipulationChargeInput, ArmatureCameraEvents, ItemTooltipEvents, ClientSessionEvents, ScreenOverlayEvents, PlayerRenderEvents}`. Members were copied verbatim by a brace-matching script.
    - The ordered world-effect dispatcher moved intact to `client/render/world/WorldEffectDispatcher`.
    - The per-tick fan-out stays one handler in its old order.
    - The unused `renderPlayerArm` was dropped.
  - **Client classes out of `common/init` (V-8):** `ShaderInit` moved to `client/render/shader`, `RenderTypeInit` to `client/render` and `FungalSkyBoxRenderer` to `client/render/world`. The 18 particle type and data classes moved from `client/particle/{type,data}` to `common/particle/{type,data}`, because the server builds them. `ParticleInit` keeps the register.
  - **Data components (R-7):** `DeferredRegister.DataComponents` with `registerComponentType`, with the same ids and codecs and `cacheEncoding` on the same four.
  - **Client caches (P-10):** 13 entity-id caches (Crimson Fire, Monolithic Dislocation, blood binding tendrils, torch breath loops, vial injection, microscope, staff morph, seed tendril pulses, dislocation shell layer, projectile trails, cinder breath, puppet strings, staff planting) gained `clear()`, which also drops their level reference, and are cleared on `ClientPlayerNetworkEvent.LoggingOut`.
- **What changed (8b, the guardrail):**
  - **Client-bound payloads:** the 68 handlers that reached client classes moved verbatim to `client/network/Clientbound{State,Effect,Screen}Handlers`. `PacketHandler` registers them as `ClientPayloadHandlers::dispatch`, a common table the client fills at `FMLClientSetupEvent`.
    - Field reads became accessor calls; the 17 class-based payloads gained accessors.
    - `AntecedentEffectPacket` keeps its server branch and dispatches its client branch.
    - The redundant `FMLEnvironment.dist` checks in the Liber and Unstained syncs were dropped, since that code is now client-only.
    - Byte layouts and `PROTOCOL_VERSION` are unchanged, and `payload-registry.txt` still matches.
  - **`common/util/ClientBridge`:** a common interface that the client implements (`client/ClientBridgeImpl`, installed first in client setup). Its defaults are neutral (null player, `false` Shift, the key itself as its translation, no-op screens). It replaces 35 direct client calls in 20 files: screen openers (Synaptic Loadout, Harbinger Progress via `ItemSanguineConduit`'s former reflection hack too, bloodline pool, ledger, scar binder, vascular status, Unstained progress, rite and structure hints, blood-structure guide page and renderer), the blueprint projection, Shift and `I18n` in tooltips, the local player and level, client registries for the item handlers, the rite-space lookup and the active-rite list.
  - **Shared types moved to common:** `ActiveRiteClientData`'s nested sync records (`RiteEntry`, `SocketMarker`, `SigilSegment`, `SanguineBlob`, `NodeKind` and their growth helpers) moved to `common/rite/harbinger/ActiveRiteData`, and `QliphothBloomClientData.BloomEntry` to `QliphothBloomData`. `EnumNodeShape`, `ManipulationTreeEntry`, `EntityParticleUtils` and `HemoClientItemExtensionsProvider` also moved to common; `HemoItemProperties` moved to `client/registry`.
  - **Render hooks:** `IRenderHarbingerEquipment` (one implementer) is gone; `RenderScarsLayer` draws the fungal scar itself. `ICellHand` is a marker; `ModelRegistrar.cellHandModel` picks the baked hand model.
  - The local skill-tree editor (`tools/skill_tree_editor/src/server/skillParser.ts`, not version-controlled) writes the new `EnumNodeShape` import path, so it keeps producing compiling branch files.
- **Metrics:**
  - `src/main`: +4,993 / −3,738 lines in 391 files: 36 added, 4 deleted (`ClientEvents`, `IRenderHarbingerEquipment`, `ItemInventoryClientAccess` and `ActiveRiteClientData`'s data half, counted as a modification), 25 renamed.
  - `src/test`: +630 / −217 in 96 files. `src/gameTest`: +2,813 / −26, mostly the two frozen lists (2,684 lines).
  - Call sites migrated: 40 annotations, 50 block-entity renderers, 12 particle providers, 3 fluid extensions, 33 `getByName` callers (now through the registry), 68 payload handlers, 35 bridge calls, and about 60 files repointed for moved classes.
  - Remaining legacy instances:
    - non-canonical subscribers: 0
    - `bus=`: 0
    - legacy client registration calls: 0
    - client references under `common/` outside item client extensions: 0 (57 item extension files, checked span by span)
    - `ClientEvents`: gone
- **Tests rewritten:**
  - The 57 tests that read `ClientEvents` now read the file that holds each pin. Nine needed two or three files, and `SharedMemoryInputSourceTest` follows `tick()`.
  - The 8 creative-tab pins read `CreativeTabInit`. `HemomancyCreativeTabsSourceTest` used to assert the duplication ("appears at least twice"); it now requires each WIP entry exactly once in the one list. The order itself is a behaviour test now: the frozen creative-tab snapshot.
  - 13 handler-body pins read `client/network`; 17 path pins follow moved files; 4 particle-provider pins read `ParticleProviderRegistrar`.
- **Guardrails added:** `FrozenIdentifierGameTests`, `RegistrationStyleGuardrailTest`, `ClientSessionCacheTest`, `CommonClientIsolationTest` (see the table).
- **Approved items implemented:** none needed; nothing in this step was marked APPROVAL.
- **Pending approvals:** F-22 (unchanged, see *Decisions*).
- **Deviations from the roadmap:**
  - **`ParticleInit` stays in `common/init`.** The prompt says to move it into `client/**`, but it holds the `PARTICLE_TYPES` register the server needs. Its provider half moved to `client/registry/ParticleProviderRegistrar`, as the audit recommends.
  - **Client isolation went past `common/init`.** The guardrail ("no client import anywhere under `common/`") required moving the client halves of 68 payloads and 20 other files. Phase 2 (step 4) had moved only the handlers that used `Minecraft.getInstance()`.
  - **The allow-list is structural, not a file list.** A `common/` file may name client classes only if it is a `HemoClientItemExtensionsProvider`, and only inside `hemomancy$getClientItemExtensions()` or an `IClientItemExtensions` body. The 57 such files are counted, not listed by name, so a new extension needs no list edit, and client code that leaks outside the extension body fails.
  - **Frozen-identifier snapshot added here,** because step 2 had not added one (see above).
  - **`HIGH` priority for the two former constructor listeners** keeps them ahead of the other Hemomancy `NORMAL` handlers. Against other mods' handlers for the same events, the order is now explicit rather than set by load order. No other handler depends on either.
- **Final review:** one reviewer read the whole diff from `de8e25ba1` and found no defects.
  - Confirmed clean: call order in the tick fan-out and the world-effect dispatcher, every registration line, verbatim handler bodies with correct accessors, `enqueueWork` placement, codecs, data component flags, the WIP order, F-22 untouched, no line-ending rewrites, no client reach from `common/`.
  - From its notes, the guardrail now also rejects wildcard client imports.
  - Accepted as is: the guardrail does not scan `com.vincenthuto.hutoslib.client` (`ClientBridge` names HutosLib's `BookBodyRenderer` in one signature) or `com.mojang.blaze3d`, which datagen uses. `SporiticThuribleItem.currentGameTime()` now answers 0 on a dedicated server instead of crashing; only client tooltip and item-bar paths call it.
- **Not verified (needs in-game check):**
  - Opening each bridged screen and each dispatched payload's effect in a world. The client start proves registration, not every handler. The handler bodies are verbatim, and the dispatch table is pinned by `CommonClientIsolationTest`.
  - Block-entity renderers and particles in a world (identical registry calls, now made at resource load instead of client setup).
  - Relogging into a second world to see the cleared entity-id caches.
- **Known issues (pre-existing, not fixed here):**
  - Static `ClientLevel` fields in about 17 client render classes (listed in the step 8 impact map, section 8) still hold the last level until the next world loads. They are not entity-id caches, so P-10 did not cover them.
  - Clinical and succession opt-in failures as in step 5 (not re-run; this step does not touch their code paths beyond moved client handlers).
- **Handoff notes for step 9 (Phase 6, combat targeting and damage attribution):**
  - The baseline is fully green: 4,004 JUnit tests and 1,018 required GameTests.
  - **Client calls from common code:** a new client-bound payload that needs client classes registers `ClientPayloadHandlers::dispatch` in `PacketHandler`, with its body as a `handle` overload in the matching `client/network/Clientbound*Handlers`, registered in that class's `register`. Any other client call from common code adds a method to `ClientBridge` (with a neutral default) and `ClientBridgeImpl`.
  - **Registration:** new client registration goes in the matching `client/registry/*` class, and new world-space effects go in `WorldEffectDispatcher`, in order. New creative-tab WIP entries go in `CreativeTabInit.WIP`.
  - **Frozen snapshot:** a registry id or creative-order change now fails `FrozenIdentifierGameTests`. Regenerate the lists only for an approved migration, with `HEMOMANCY_FROZEN_SNAPSHOT_OUT` set during a `runRegistrationGameTestServer` run.
  - **Baseline comparisons:** a temporary `git worktree` beside the repository works for this. Junction `libs/` into it, run one suite, then remove the junction with `rmdir` before `git worktree remove --force`. Removing the worktree first would follow the junction into the real `libs/`.

### Step 9: Phase 6, combat targeting and damage attribution
- **Status / date / commits:** DONE, 2026-10-22. Four commits on `neo-1.21.1`:
  - `3b841ba03`: 9a, the GameTest matrix, written and green before any targeting code moved
  - `7f0c1968d`: 9b, targeting through `ManipulationCombatHelper.canHarm`, plus `DrudgeActions`
  - `5fa01de44`: 9c, the heal-modifier table and the flight arbiter
  - `c9af6c6c3`: test isolation, review fixes and docs

  A further `consolidation(step-9):` commit records this entry.
- **Test counts before → after:**
  - `test`: 4,004 → 4,010, 0 failures.
  - `alphaCheck`: green, with all 1,036 required GameTests passing (1,018 before; 18 new in the new alpha suite `combat_targeting_validation`), `verifyGameTestResourceLog` and `verifyPackagedClientResources`.
  - `runCombatTargetingGameTestServer` (the focused launcher for the new suite): 18/18.
  - Opt-in suites were not re-run. The only opt-in code paths touched are the Still Art casts, which `casting_animation_validation` drives; their bodies change only in the target predicate, and the matrix covers both Still Art rows.
- **The matrix (`CombatTargetingMatrixGameTests`).** Each row's effect is struck at one target at a time, and the row records which targets it touched. The columns are: a hostile Husk, the caster's tamed wolf, a Husk on the caster's scoreboard team, the caster's own Gorebound Hulk, a Warden, and a survival player with PvP on and with PvP off. Rows and today's results:

  | Row | Reaches |
  |---|---|
  | `ManipulationCombatHelper.hurt` (manipulations) | hostile, boss, PvP-on player |
  | Drudge `blood_shot` (owned by the caster) | hostile, ally, own puppet, boss |
  | Silent Severance (armour) | all seven |
  | Prismatic flash (armour; the attacker is a separate cow) | hostile, ally, own puppet, boss |
  | Autoimmune Edge (Still Art) | all seven |
  | Lethean Mute (Still Art) | hostile, ally, own puppet, boss |
  | Irontooth primal damage (morphling) | hostile, boss |
  | `MorphlingItem.findLookTarget` (Bootlace, Emberfang) | all seven |
  | Glacier slow (scar) | hostile, ally, own puppet, boss |
  | Thorn retaliation (scar) | everything but the PvP-off player |
  | Living Sickle spin (living weapon) | hostile, boss, PvP-on player |

  - The Sickle's filter admits the caster's puppet. Its hit is a weapon-school hit, though, so `SchoolCombatEvents.prepare` cancels it.
  - `reachPresetsReproduceTheirLegacyFilters` checks each `Reach` preset against the exact pre-step-9 filter it replaced. It covers every body above plus a creative player, a spectator, a dying Husk, an armour stand and the caster, with PvP on and off. `STRICT` is compared with canHarm's old body.
- **What changed (9b, targeting):**
  - **`ManipulationCombatHelper` is the one harm predicate.**
    - `canHarm(Player, target)` is unchanged in meaning; it now delegates to `canHarm(caster, target, Reach.STRICT)`.
    - **`Reach`** is a record of five widenings: allies, any player, creative, spectators and dying bodies. Each preset reproduces a family of older filters flag for flag:
      - `MONSTER_FILTER`: a `Monster.class` gather with `isAlive`.
      - `MONSTER_FILTER_ANY_STATE`: a gather with no predicate, which also returns dying bodies.
      - `UNGATED`: everything living but the caster, whatever PvP says.
      - `UNGATED_SURVIVAL`: as `UNGATED`, sparing creative and spectating players.
    - A non-player caster (a Drudge, an effect holder) may only use a preset that checks neither allies nor PvP.
    - **`canHarmFor(source, target)`** is `ConductionManager.canHarm` and its `controller` lifted verbatim, for constructs, summons, projectiles and effect holders. It has its own name, so callers passing a `Player` keep the Blood Construct exclusion. 19 callers were repointed and `ConductionManager.canHarm` is gone.
  - **Migrated sites.** These now ask `canHarm` with the preset that keeps their verdicts:
    - 31 `Monster.class` gathers: 20 morphling, 4 Still Art, the Glacier scar, the Saprovitta trail, Phantasmal Masquerade's hostile check, Predatory Eyes, the Sporitic Thurible, the Pale Silver Bell and the Rite of the Closed Vein. (The 9b commit message says 38; 31 is the count.)
    - The Hemorrhagic Venom and Verminous Aura effects. Their `canHarmFor` check moved from the loop into the gather.
    - Silent Severance, Silver Rebuke, Autoimmune Edge, `findLookTarget`, the Prismatic flash, the Verdigris Censer mark and Crimson Wake.
  - **`DrudgeActions` (L-1).** The 48 Drudge lambdas moved verbatim out of `ManipulationInit`, one static method per manipulation, registered as `DrudgeActions::bloodShot` and so on. Their 29 Monster lookups are now two helpers, `monsters` and `nearestMonster`, routed through `canHarm(drudge, monster, MONSTER_FILTER_ANY_STATE)`. `activation_potential` keeps its distance filter. `ManipulationInit` lost 466 lines and 21 imports.
- **What changed (9c):**
  - **Heal-modifier table (G-4).** `HealModifier` lists necrosis (×0.75 unless Insatiable Hunger or Hemophagy applies), Insatiable Hunger (×0.25), Hemophagy (×0.25 unless Insatiable Hunger applies) and Impaired Recovery (×0.5).
    - `ManipulationStatusEvents.onLivingHeal` applies the table at `NORMAL`, including the Hunger Collapse burst. The listeners in `HemorathEntity`, which also lost its now-empty `@EventBusSubscriber`, and in `BloodInjectionEvents` are gone.
    - Unstained restraint purity read the *requested* heal before every reducer. That was only class-scan order; a probe GameTest showed it before any code moved. `PurityGainEvents.onLivingHeal` now runs at `HIGH`, so the same result is explicit. The heal stage table is in standards §7.
  - **Flight arbiter (G-7).** `common/util/FlightArbiter` is the only writer of `mayfly` / `flying`. Its `Source` enum (Edacious set, Silent Slipping, Elytra effect) holds each source's flying speed, whether it lifts the player into flight, and how it resets speed on revoke. Sheolic Bastion grounds through `ground`. Every grant, revoke, speed and sync is as before.
- **What changed (test fix and review):**
  - The heal and flight GameTests first used in-level fixture players inside multi-test batches. That made the combined run fail `mandatoryjigsawwingsassemble` in 5 of 6 runs; it is a test-placement interaction, since the code changes alone passed 2 of 2.
    - They now use detached `CapturedPlayer`s, one test per batch, on a 5×4×5 `small_room` template.
    - Measured in a clean worktree: 4 of 4 combined runs green, against 2 of 2 on `2f3502a38`.
  - Javadoc and standards wording followed the reviewer's notes.
- **Metrics:**
  - `src/main`: +970 / −644 in 49 files. 3 new classes: `DrudgeActions` (601 lines, 466 of them moved), `HealModifier` and `FlightArbiter`. Deleted: `ConductionManager.canHarm`/`controller`, 3 heal listeners, `HEMOPHAGY_HEAL_MULTIPLIER`, `SILENT_SLIPPING_FLIGHT_SPEED` (now in `FlightArbiter.Source`) and Hemorath's subscriber annotation.
  - Call sites migrated: 31 `Monster.class` gathers, 2 effect gathers, 7 area and look predicates, 19 `canHarmFor` callers, 48 Drudge lambdas (29 lookups), 4 heal listeners, 7 flight writers.
  - Tests: 2 JUnit guardrails and 3 GameTest classes added (18 GameTests); 2 source tests repointed (`WillResourceSourceTest` now reads `DrudgeActions`; `SilentArchonIncorporealitySourceTest` reads the arbiter call). The impact map was regenerated.
  - Remaining legacy instances:
    - 6 ungated `Monster.class` gathers, all allow-listed with reasons: Will AI, Pale Intercession AI, Phantasmal Echo AI and smoke ×3, Husk Effigy retargeting, the ownerless Pale Consecration zone.
    - Living-weapon sweeps (`LivingSickleItem`, `LivingTorchItem`) keep vanilla melee eligibility.
    - `EntityManipulationEffects.canAffect` serves only mob casters (Will, Vesper).
    - Hand-written `LivingEntity` reveal filters: Irontooth tremor and burrow sense, Witch's Ear, Pale Diagnosis, Bloodhound.
- **Guardrails added:** `CombatTargetingMatrixGameTests`, `HealModifierGameTests`, `FlightSourceGameTests`, `CombatTargetingGuardrailTest`, `HealAndFlightGuardrailTest` (see the table).
- **Approved items implemented:** none. G-1/F-18, F-16, F-17 and G-2b all read PENDING, so the matrix expectations, `HEMOMANCY_REFERENCE.md` and the wiki are unchanged.
- **Pending approvals:** G-1/F-18, F-16, F-17, G-2b, plus two found in this step:
  - G-7b: a flight revoke checks the other sources.
  - G-2c: one boss definition.

  The *Decisions* notes say where each change would go.
- **Deviations from the roadmap:**
  - **Presets instead of a bare `canHarm`.** Routing a `Monster.class` filter through plain `canHarm` would have stopped it hitting the caster's own puppets, teammates and impressed mobs (G-2b), and PvP-off players for the ungated area effects (F-16). Each site therefore names the `Reach` preset that keeps its verdicts, and approving the item moves it to `STRICT`.
  - **Kept local, with reasons:**
    - Puppet and summon AI (`BoundSummonBehavior`, the crossbar, hounds, echoes, Pale Intercession): standards §5 exception.
    - The Will/Vesper entity-cast rule `canAffect`.
    - Domain rules (Pallid Shadow, Husk Effigy, flail impact rules, absorption).
    - World hazards with no caster (Founding Fane, Viridis Salis trail, Morphling Cradle, Vigil, Pale Consecration).
    - The living-weapon sweeps: their vanilla `canAttack`/`isAlliedTo` rule has no exact `Reach` equivalent.
  - **No damage-stage ordering test.** G-1 is not approved. The heal stage table is documented and tested instead.
  - **A new alpha suite** with its own launcher, so the matrix can be run in seconds.
- **Final review:** one reviewer read the whole diff from `2f3502a38`.
  - It confirmed, among other things:
    - no behaviour change, frozen-identifier change or client reach
    - all 48 Drudge methods identical to the old lambdas, apart from the lookup helper
    - every site's preset correct, checked by hand
    - the heal and flight semantics, Hemorath left with no empty subscriber, and the docs accurate
  - Fixed after the review:
    - a stale `HemophagyEffect` javadoc
    - the `Reach` javadoc's decision mapping
    - an overstated standards sentence (the reveal filters are now listed as migration targets)
    - the heal GameTest name
    - the heal-listener regex now accepts `final`
  - Accepted: the guardrail checks that `canHarm` appears, not which preset. A swapped preset would pass, so presets are pinned by the matrix rows and the preset-equivalence test, not per site.
- **Not verified (needs in-game check):**
  - Drudge work on a live server.
  - Edacious, Silent Slipping and Elytra flight by hand, including the client-side Elytra speed reset.
  - A Hunger Collapse burst seen in a world.
- **Known issues (pre-existing, not fixed here):**
  - `mandatoryjigsawwingsassemble` builds its outpost from (48, 3, 48), outside its own template, and fails when parallel tests add live players nearby. `completeexitcleansownedfixtureandoutput` is the step 4 flake. Both passed in the final runs.
  - The step-9 matrix still uses in-level players, which it needs for area queries, so it keeps one test per batch.
- **Handoff notes for step 10 (Phases 8 + 9, rendering plumbing and animation conventions):**
  - The baseline is fully green: 4,010 JUnit tests and 1,036 required GameTests.
  - **Targeting:** a new effect that picks targets calls `ManipulationCombatHelper.canHarm(caster, target)` (or a `Reach` preset if it must keep an older verdict), never a bare `Monster.class` filter. A mob-AI gather goes in `CombatTargetingGuardrailTest`'s allow-list with a reason. New heal reductions are `HealModifier` entries; new flight sources are `FlightArbiter.Source` entries.
  - **GameTests:** use a detached `CapturedPlayer` unless the test needs the player in the level. Area tests that need one get their own batch.

### Step 10: Phases 8 + 9, rendering plumbing and animation conventions
- **Status / date / commits:** DONE, 2026-10-22 to 2026-10-23. Items are committed one by one with the prefix
  `consolidation(step-10):`.

  | Item | Status | Commit |
  |---|---|---|
  | 1. Screen chrome (V-1) | DONE | `a53982248` |
  | 2. Render-type caching (V-2), Spark profile | DONE | `1f642dccf` |
  | 3. Colour particle data (V-6) | DONE | `af24ecea6` |
  | 4. HUD layers and world dispatch (V-7) | DONE | `9690741ce` |
  | 5. Shared material quad encoder (V-3) | DONE | `e1245b2de` |
  | 6. Shader noise include (V-5) | DONE | `da80a7f2a` |
  | 7. `BossReplayClock` / `BossMoveTimeline` (A-1) | DONE (A-1b PENDING) | `9136d673d` |
  | 8. Player-animation registry (A-3) | DONE | `5d539f5fb` |
  | 9. `AnimationTime` (A-4) | DONE (A-4 PENDING) | `e2597dc41` |
  | Review fixes (items 6 and 9) | DONE | the commit after `e2597dc41` |
  | V-4 tube frames (optional, last) | NOT DONE (optional; left for a later step) | |
- **Test counts before → after:** `test` 4,010 → 4,017 (item 1) → 4,026 (item 2) → 4,031 (item 3) → 4,034 (item 4) → 4,038 (item 5) → 4,040 (item 6) → 4,048 (item 7) → 4,052 (item 8) → 4,055 (item 9 and the review fixes), 0 failures.
  `alphaCheck` after item 9: BUILD SUCCESSFUL, all 1,036 required GameTests passed. After the review fixes, one run
  failed two fish-AI GameTests (`schoolsusingfishnavigationandsurvivesunderice`, `strongerpreyneedsmultiplebitesbeforereward`,
  server-side timing, untouched by this step); the rerun passed all 1,036.
- **Impact map:** one read-only reviewer listed every chrome, render-type, particle, dispatch, encoder, shader-noise,
  boss-clock, player-animation and time-source site with the tests that pin them. It is not committed (it is a
  working list); the items below cite what they used from it.
- **Item 1, screen chrome (V-1):**
  - `ScreenDrawUtils` gained `drawRingBorder` (concentric single borders), `drawBevelBorder` (single and two-pixel),
    `drawCornerAccents` and `drawSlotWell`. Each draws the same fills in the same order as the copies it replaced.
  - `VeinBackgroundRenderer` takes a `Style`: a seeded `Field` (count, seed, speed/amplitude/length/thickness ranges,
    step stride, dot growth, angle phase), a `Palette` (base, glow-ring spacing and strength, tendril channels and
    scales) with an optional deep palette for the progress screen's fade, and `Speckles` (`Scattered`, re-rolled each
    frame; `Fixed`, rolled once; `Deepening`, the progress screen's). `render` keeps its own 0.016-per-call clock;
    `draw` takes a screen's clock. Named styles: `PROGRESS` (the default, unchanged), `CRIMSON`, `MACHINE`.
  - **Migrated (14 vein copies):** Bloodline Pool, Ledger, Tendency View, Vascular Status, Vascular View, Scrying
    Diagnostics, Visceral Mirror (its fog stays local, drawn after the veins), Mnemonic Reliquary, Harbinger Equipment,
    Mason's Effigy, Vial Centrifuge, Scar Station, Ghastly Alembic, Morphling Incubator. Each keeps its own clock
    (frame count, partial-tick, game time or wall time) and passes it in.
  - **Borders and slot wells:** 21 private two-pixel borders, 3 fungal bevels, 2 gauge ring borders, Scrying's frame,
    the Structure Spawner's two helpers, the Visceral Mirror's double and thin borders, the Harbinger Equipment
    inventory border, the Synaptic Loadout neural border body, the Scar action button and 11 slot wells now call
    `ScreenDrawUtils`. The Mason's Effigy frame (two-pixel lines, bright right edge) is its own art and is now named
    `drawEffigyFrame`.
  - **Left local as other art (listed in the guardrail):** the Puppeteer's Spindle thread veins, the Morphling Jar's
    green veins, the fungal hyphae (Spore Implant, Lantern, Crucible), the SSC's bare tendril field, the Synaptic
    Loadout's jagged neural arcs, the Pallid Retort's diamonds, and the monolith's world-space veins.
  - **Vertex order.** `ColoredRectBatch` can now emit each quad in `GuiGraphics.fill`'s vertex order. A quad split
    along the other diagonal can round a pixel one colour level differently on some GPUs, so each `Style` records the
    order its screens used: the 14 migrated screens filled each rect (`fillOrder = true`), the 7 screens already on the
    shared renderer were batched (`PROGRESS`, `false`).
  - **Review harness:** `src/gameTest/.../ScreenChromeReview` (run `screenChromeReviewClient`, `-PchromeLabel=<label>`,
    save `ScreenChromeReview`). It opens 30 screens (every migrated one plus the existing shared-renderer users) and
    draws each one's first frame by hand into a black target as the screen opens, with the client game time pinned to
    48,000, so the frame-counted veins and the blurred world behind them are identical from run to run. Before shots come
    from a worktree of `29a5576ae` with the same harness.
  - **Screenshots:** `build/screen-chrome-review/screenshots/chrome-beforeP{1,2}-<screen>.png` (baseline) and
    `chrome-afterP{1,2,3}-<screen>.png` (this item), 30 screens each, 1280×720 at GUI scale 2.
    - 25 of 30 are byte-identical before and after.
    - The other five differ only where the same code also differs between two runs: the progress screen's centre node,
      the Tendency wheel, the Vascular Status and Mason's Effigy player models (wall-clock animation), and Scrying's
      veins, which it draws twice per frame (its own `renderBackground` plus the container's) on a wall-clock delta.
      The unmigrated Scrying screen differed from its own baseline by 144 pixels (up to 20 levels); the migrated one by
      32 pixels (1 level).
- **Item 1 metrics:** `src/main` +447 / −2,105 in 29 files; no class added or removed. Call sites migrated: 14 vein
  backgrounds; 36 border calls (22 `drawBorder`, 6 `drawSimpleBorder`, 4 `drawBevelBorder`, 2 `drawRingBorder` with
  2 `drawCornerAccents`); 12 slot wells.
- **Item 1 tests:** 5 source tests repointed (Scrying budget and stride, Lantern border, Effigy veins, the progress
  palette strings, Synaptic border); the deep-palette pins became data assertions in `VeinBackgroundStyleTest`.
- **Item 2, render-type caching (V-2, P-9):**
  - **`SeededRenderTypes<K, U, T>`** (`client/render`) is the one caching pattern. A seed's types are built once by a
    factory that takes the seed and a `Supplier` of the uniforms. Asking with the same uniforms returns the same type, so
    its draws batch. Asking with new uniforms moves to the seed's second type and sets them there, so geometry already
    queued on the first keeps the uniforms it was written with. A buffer source ends a type's batch as soon as another
    type is requested, so a type is never handed back with new uniforms before its earlier geometry is drawn. Seeds are
    kept LRU (256 per family, 32 for the rite families, as before).
  - **Converted (37 factories):** the 28 per-call factories in `HemoRenderTypes` (monolith fragment and surfaces, Will
    shell, hermit dissolve, Vesper wound, structure warp and infusion veins, crucible basin, latent skin, infected and
    blood pools, the Lowtide parchment, lake and skyboxes, the seven Apotheos types, the Qliphoth black hole, the Silent
    Archon storm and fog, the loom orb shell); the three caches keyed on every uniform including game time (engram
    sigil, Cardinal floor membrane and mound, staff blood melt); the four existing mutable per-seed holders (rite fog,
    sky wound, daemon haze, staff haze); `RenderTypeInit.energySwirl`, `RadiantPortalRendertype.textWithWaterShader`,
    `CardinalFloorRenderTypes.withRite`, and the three Suspended crystal renderers' translucent wrappers (now one
    `TranslucentModelTypes`).
  - **How each kept its output:** every factory keeps its signature, render-type name, shards and buffer flags. Its old
    body moved to a `create*` method that binds the key's fields and, inside the uniforms lambda, the uniform record's
    fields to the original parameter names, so every `setUniform` line is unchanged. The key is the seed plus anything
    structural (a texture, the glow/core or depth flag, the floor name and write mask, the black hole's apex flags).
  - **Caller audit:** a caller that asked for one seed with three different uniform sets before drawing would lose the
    first set. Every factory called twice in one place differs by key (Vesper's mount and rider textures, the floor's
    membrane and mound, the Fane's core and glow) or ends its batch itself (the Qliphoth black holes).
  - **One school helper:** `SchoolRenderTypes` builds the school materials (Lux and Umbra, Thermal, Animus and Mortem,
    Ferric and Ductilis, the cast outpouring) and sets `HemoTime` and `ColorModulator` from a `Clock`. Each family keeps
    its own clock: callers `begin` them with different times (a needle's partial tick, the manipulation renderer's
    frame time), so one shared clock would let the last caller win.
  - **Spark profile:** see [evidence/12-step10-render-type-profile.md](evidence/12-step10-render-type-profile.md).
    Harness `RenderTypeProfileReview` (run `renderTypeProfileClient`), 81 Hematic Stakes and a hotbar of monolith items,
    30 s windows, two runs each side. Render-thread allocation under `RenderType.create` fell from about 27 MB per 30 s
    to none sampled, and under `HemoRenderTypes` from about 94 MB to 12–21 MB (the uniform records and boxed seeds).
    Spark CPU and frame times are unchanged within noise (mean frame 8.79–8.80 ms before, 8.81–8.87 ms after).
  - **Visual check:** before (worktree `29a5576ae`) and after runs of the review clients that draw the converted types:
    brazier (blood pool, steady flame), condenser (square pool), infusion (structure warp, infusion veins), rite floor
    (floor membrane and mound, rite fog, sky wound, daemon haze), loom (engram sigil, floor), outpouring (all school
    materials, cast veins, blood pools) and Vesper (`-PvesperReviewOnly=wound_carapace,wound_neck,live_wound_drain`).
    Every shot matches by eye; the scenes animate, so pixels differ only by animation phase, particles and dropped
    items. Shots: `build/step10-review/item2/<Task>/{before,after}/`, side-by-side sheets `build/step10-review/item2/
    sheet-*.png`. Loom (12 lines, stale `thaumaturge:*` attachments in its save) and the Vesper baseline (a connection
    reset at shutdown) log the same unrelated ERROR lines.
- **Item 2 metrics:** `src/main` +944 / −343 in 15 files (most of the added lines are the per-family uniform records
  and binds); 3 classes added (`SeededRenderTypes`, `SchoolRenderTypes`, `TranslucentModelTypes`); the
  `riteTypeCache` and the four hand-written holder classes are gone. Remaining per-call render types: 0 (the guardrail
  allows three lazy singletons and `SchoolRenderTypes.create`).
- **Item 2 tests:** `DaemonHazeSourceTest` (the haze key) and the Fane-heart legacy test (the depth flag now lives in
  `SchoolRenderTypes`) repointed.
- **Item 3, colour particle data (V-6):**
  - **`ColorParticleOptions`** (`common/particle/data`) replaces the seven colour-only data classes (blood cell,
    absorbed blood cell, blood avatar hit, blood claw, serpent, hermit edge glow, Sporitic spore). The registered type
    tells them apart. Its codecs are built per type: the network form is still `ParticleColor.serialize()` as one UTF-8
    string, and the command form is still float fields `r`, `g`, `b`.
  - **`HemoParticleType`** (`common/particle/type`) replaces the ten type classes. Its codecs come either from the
    type itself (colour particles) or from the data class's static codecs (hit glow, Will absorption glow, daemon
    diffuse glow, which keep their own data classes). Registry ids are unchanged.
  - **One way to build particle data:** the factories' eleven `createData` / `createFeedData` helpers are gone. Their
    19 callers and 13 direct constructions call `HemoParticleData`, which gained `willAbsorptionGlow` (the non-pulsing
    glow) and `abocipher`. Each facade method builds exactly what the helper did. Literal colours keep their values
    (`ParticleColor`'s float constructor truncates to the same ints).
  - **Found, not changed (V-6b, PENDING):** the Mycophant encounter's spore bursts pass 0–1 floats, which truncate to
    black. See *Decisions*.
  - **Visual check:** brazier and rite-floor review pairs (worktree `29a5576ae` against this item), which spawn blood
    cells, serpents and spores, match by eye. Shots: `build/step10-review/item3/<Task>/{before3,after3}/`, sheets
    `build/step10-review/item3/sheet3-*.png`.
- **Item 3 metrics:** `src/main` +213 / −754 in 50 files; 17 classes deleted (7 data, 10 type), 2 added. Remaining
  colour-only data classes: 0.
- **Item 3 tests:** 6 source tests repointed from factory helpers and deleted classes to `HemoParticleData` (Living
  Torch, Abocipher, cell hand, Fane heart, hermit farewell, blood projection feed spiral).
- **Item 4, HUD layers and world dispatch (V-7):**
  - **HUD:** the three `RenderGuiEvent.Pre` handlers (white humor, morphic nectar with the Circus edge, the Sanguine
    Omen world grade with the Axonal and Naeglerophaeon grades and the Fane post) are one `registerBelowAll` layer,
    `screen_effects`, under vanilla's camera overlays. The three `RenderGuiEvent.Post` handlers (the Earthen Vein
    destination tooltip, the Tithe / Endless Hour text, the Antecedent scar marker and Clairaudiograph captions) are
    one `registerAboveAll` layer, `hud_markers`, registered last. Layers stack 200 units toward the camera each, so
    the markers' layer translates back to depth 0, where `Post` drew; HUD text and fills drawn by earlier layers still
    cover them where they overlap, as before. Both events fired whether or not the HUD was hidden, and mod layers are
    not gated by `hideGui` either; the handlers that checked it still do.
  - **Order of the full-screen effects.** The three `Pre` handlers were methods of one class, so the bus ran them in
    the JVM's method order, which is not fixed: a listener probe in the consolidation client saw white humor, grade,
    nectar, while the review client's baseline shows nectar before the grade and white humor after it. The layer pins
    what the baseline draws: nectar and Circus, then the grade, then white humor. The `Post` handlers were in three
    classes and keep their probed class order.
  - **World:** the seven self-staging `RenderLevelStageEvent` subscribers (Axonal travel view, Earthen Vein markers,
    Mycophant glimpse, crossbar holder tracker, first-person Avatar, the Outpouring, the manipulation visuals) ran at
    normal priority straight before the dispatcher, in that order. The dispatcher now calls them first, in that order,
    and each keeps its own stage checks. The Outpouring screen passes ran at high priority, ahead of every other
    AFTER_LEVEL drawer including other mods'; the dispatcher's second handler, `renderScreenPasses`, keeps that
    priority. `AxonalTransductionClient.world` (a one-line forward) is gone.
  - **Left alone:** the `RenderGuiLayerEvent.Pre` suppressors (the Fungal projection's hotbar, the two radial menus'
    crosshair) are layer filters, not drawing.
  - **Review harness:** `src/gameTest/.../HudLayerReview` (run `hudLayerReviewClient`, `-PhudLabel=<label>`, save
    `HudLayerReview`). It draws the HUD over a fixed test pattern once per scene (plain, white humor, morphic nectar,
    Circus, Omen grade, scar marker, nectar + Circus + grade + scar, white humor + grade), with the game time, the
    frame timer's partial tick and the vignette pinned. Two baseline runs are byte-identical.
  - **Screenshots:** `build/step10-review/item4/HudLayerReview/{before4,after3}/`: all 8 scenes byte-identical. The
    world moves were checked with the axonal, Mycophant (`-PmycophantReviewOnly=glimpse`) and Outpouring review pairs
    (`build/step10-review/item4/<Task>/{before4,after4}/`, sheets `build/step10-review/item4/sheet-*.png`): they match
    by eye. The axonal shots catch the travelling camera at different points; a second baseline (`before5`) differs
    from the first in the same way.
- **Item 4 metrics:** `src/main` +74 / −54 in 12 files; `RenderGuiEvent` handlers 6 → 0; `RenderLevelStageEvent`
  subscribers 9 → 2 (both in `WorldEffectDispatcher`).
- **Item 4 tests:** `SanguineOmenResourceTest` repointed to the layer; the weaker dispatcher check in
  `RegistrationStyleGuardrailTest` moved into `HudAndWorldDrawingGuardrailTest`.
- **Item 5, shared material quad encoder (V-3):**
  - **`MaterialQuads`** (`client/render/world`) writes the encoded-UV vertex the school and rite shaders read: U is the
    shape's tile plus a coordinate along the quad, V the seed's tile plus one across it, colour RGB with alpha × 255
    truncated. `quad` emits the a, b, c, d corners (or b, c, d, a for the other diagonal); `seedTile` wraps a seed into
    `SEED_TILES` (127).
  - **Migrated:** `LuxUmbraGeometry`, `RiteSurface`, `ThermalGeometry` and `FerricDuctilisGeometry` keep their entry
    points and their own early-outs (Lux at alpha ≤ 0, the rite surfaces at ≤ 0.003); their private `quad` and
    `vertex` bodies call the encoder. Thermal passes white for its fixed colour. `VesperRecallGeometry` and the
    Outpouring marks take their seed tile from `seedTile`.
  - **Ferric stays at 128** (V-3b is PENDING): `FerricDuctilisGeometry.FERRIC_SEED_TILES = 128` feeds its tile into
    the shared encoder.
  - **Found, not changed (V-3c, PENDING):** the Outpouring surface marks write the encoding by hand with no pose and
    round their alpha. See *Decisions*.
  - **Proof of identical output:** `MaterialQuadsEncodingTest` drives every public and package entry point of the four
    geometries (cards, ribbons, ribbon paths, streams, flame ribbons, glass shards, clots, block faces, plates, spikes,
    rite cards, grounds, domes and quads) over eleven seeds, including 127, 128, negative and large ones, through a
    rotated pose. It hashes each vertex's float bits and colour bytes. The digest was taken from the baseline worktree
    before the merge and is unchanged after it (6,468 vertices).
  - **Visual check:** Outpouring (`before5` / `after5`, item 5 alone) and rite floor (`before5` / `after6`, items 5
    and 6) review pairs match by eye. One rite-floor `after5` run caught the night shot before the sky darkened; the
    rerun matches. Shots: `build/step10-review/item5/<Task>/`, sheets `build/step10-review/item5/sheet-*.png`.
- **Item 5 metrics:** `src/main` +69 / −34 in 7 files; 1 class added (`MaterialQuads`). Encoders: 4 → 1 (plus the
  listed Outpouring marks).
- **Item 5 tests:** none repointed.
- **Item 6, shader noise include (V-5):**
  - **Nine includes** under `assets/hemomancy/shaders/include/noise/`, imported as `#moj_import <hemomancy:noise/...>`
    (NeoForge resolves a namespaced import to that mod's `shaders/include`):
    - `value_hash` (the 127.1 / 311.7 lattice hash, 12 school materials);
    - `material_noise` (7 solid school materials: cruor, ductilis, ferric, flame, glass, molten, skin);
    - `flow_noise` (5 flowing ones: animus, infection, lux, mortem, umbra);
    - `material_fbm` (5);
    - `vein_hash1` (cast veins, cast umbra, infusion veins), `vein_hash3` (cast veins, infusion veins) and
      `vein_noise` (cast veins, cast umbra);
    - `haze_noise` (the daemon and staff hazes' hash, value noise and fractal noise);
    - `monolith_noise` (the monolith fragment and Will monolith's hash and value noise). Item 6 first moved their two
      fractal noises too; those read the importing shader's `ShardSeed`, `HemoTime` and `FBM_LAYER_DRIFT_RATE`, so the
      review put them back in each shader, after the import where they stood.
  - **What moved:** a function moved only with everything it calls, and only where that whole closure matched in each
    importer, whitespace aside. Each import sits where the copies stood, so declaration order is unchanged. The two
    school `noise` variants differ in one token (`hash(i+1.0)` against `hash(i+vec2(1,1))`) and stay two includes.
  - **Whitespace-only differences:** the include keeps the first copy's layout. The animus, infection and mortem
    copies were written on one line, and the lux and umbra copies spaced out; their tokens are identical.
  - **Left local:** seeded noise that reads its shader's own uniforms (`Seed`, `PoolSeed`, `CeilingSeed`, the floor and
    hole seeds); the 17 `noise(vec2 value)` copies that share text but call differently seeded hashes; one-offs.
  - **GPU check:** the rite-floor, Outpouring, infusion and render-type-profile clients launched with the includes. Every
    core shader compiles at resource load. The logs show no import or compile error, and the same two pre-existing
    `ShaderInstance` warnings as the baseline.
  - **Visual check:** Outpouring and rite floor (`before5` / `after6`), infusion (`before6` / `after6`) and the
    render-type-profile scene of stakes and monolith items (`profile-scene-shot{before,after}6`) match by eye; the
    held fragment differs only by its equip animation. Shots: `build/step10-review/item6/`.
- **Item 6 metrics:** shader sources +40 / −230 in 19 files, plus 9 include files (94 lines); duplicated unseeded
  noise closures: 0. `RenderTypeProfileReview` now takes a labelled scene screenshot before measuring.
- **Item 6 tests:** none repointed; no test pinned the moved noise text.
- **Item 7, boss replay clock and move timeline (A-1):**
  - **`BossReplayClock`** (`common/entity/boss`, so an entity can own one) is `VesperAnimClock` made public. `sample`
    keeps its exact behaviour; `sync` (re-base on a new key or drift beyond 2 ticks) and `at` (the replayed value) split
    it for an entity that syncs once per client tick and reads each frame. `new BossReplayClock(key)` starts already
    running from local tick 0 under that key.
  - **`BossMoveTimeline`** (`common/entity/boss`) is `VesperMoveTimeline` renamed; the Vesper mount and Mycophant
    controllers, rules, moves and models use it, and so do the `tools/model_export` generators that write those models.
  - **Migrated:** Vesper's Evening Star and mount anim states (now on the shared class). Annetta's hand-written action
    and scene clocks (`clientActionStart`, `clientSceneStart`) became two `BossReplayClock`s synced in `aiStep`: the
    action clock starts keyed on `ACTION_NONE` and the scene clock unstarted, matching her old first-tick behaviour.
  - **Not migrated (A-1b, PENDING):** the Priestess, Latent Annetta, the Mycophant, Vesper's hood removal and the
    mount's absorption layer read a raw synced tick plus the partial tick. Moving them onto the clock changes displayed
    timing when packets bunch or a counter pauses. See *Decisions*. The Saints have no synced-tick animation.
  - **Proof of identical timing:** `BossReplayClockTest` fuzzes 200 runs of jittery, resetting and re-keyed counters
    against copies of the old Vesper clock and of Annetta's start-tick clock, value for value.
  - **Visual check:** Annetta (`-PannettaReviewOnly=combo_motion,live_break`) and Vesper
    (`wound_carapace,wound_neck,live_wound_drain`) review pairs, `before7` / `after7`: poses and the combo frames match;
    the live scenes differ by AI and particles as between any two runs. The `after7` runs also carried item 8's
    uncommitted player-pose edits; no player is posed in those scenes. Shots: `build/step10-review/item7/`.
- **Item 7 metrics:** `src/main` 1 class added (`BossReplayClock`), 1 removed (`VesperAnimClock`), 1 renamed; replay
  clock implementations 2 → 1.
- **Item 7 tests:** `VesperAnimClockTest` became `BossReplayClockTest` (3 → 8 tests); three rules tests repointed to
  `BossMoveTimeline`.
- **Item 8, player-animation registry (A-3):**
  - **`PlayerAnimationRegistry`** (`client/player`) holds `THIRD_PERSON`, every third-person player pose in priority
    order. Staff planting, the part-offset reset, the casting pose and the warp chair always apply. Of the microscope,
    vial injection, torch breath, Unstained swing and living-weapon stroke, the first to pose the model claims the frame
    and the rest are skipped. That is the old chain: `MixinHumanoidModel`'s three TAIL calls (planting, then
    `PlayerAnimationClientState.applyThirdPersonPose`, then the warp chair) with the early returns inside the middle
    call. The mixin now makes one call. An Unstained weapon in the swinging hand still claims the frame between swings,
    as its early return did. The non-player checks are kept per pose.
  - **`PlayerAnimationClientState`** keeps the Living Torch breath state (`set`, `isActive`, `hand`, `elapsed`, `tick`,
    `isBreathing`); its pose code moved into the registry unchanged.
  - **Left with each animation:** packets (frozen layouts), client states, ticks (several run inside handlers that do
    other work in the same tick), first-person hand renderers and session clears (`ClientSessionCacheTest` wants each
    cache cleared by name in `ClientSessionEvents`). The first-person priorities, which also order these renderers
    against other mods, are pinned by the guardrail.
  - **Visual check:** Outpouring review pair (`before8` / `after8`), where the player is seen in third person casting,
    armoured and holding weapons: poses match; vein animation phase differs as between runs. Shots:
    `build/step10-review/item8/`.
- **Item 8 metrics:** `src/main` 1 class added; pose entry points called from the mixin 3 → 1.
- **Item 8 tests:** `WaistPivotRulesTest` (the reset now precedes the casting entry in the registry list) and
  `LivingTorchReworkSourceTest` (the mixin calls the registry) repointed.
- **Item 9, `AnimationTime` (A-4):**
  - **`AnimationTime`** (`client/render`): `partialTick()` (the frame timer with `runsNormally = false`, which reads 1
    under `/tick freeze`), `partialTickWhileFrozen()` (`true`), `gameTime(level, partialTick)` and `age(entity,
    partialTick)` with float and double overloads, `gameTime(level)` and `gameTimeAsDouble(level)` for game time at the
    frame's partial tick. Each method body is the expression it replaced, so the arithmetic type and evaluation order
    are unchanged.
  - **Migrated (190 sites in 138 files):** every client and mixin `X.getTimer().getGameTimeDeltaPartialTick(…)`,
    `X.getGameTime() + partial…` and `X.tickCount + partial…`. A sum was replaced only where nothing beside it binds
    tighter or regroups it (no cast, `*`, `/`, `%`, `+` or `-` on its left; no `*`, `/` or `%` on its right). The four
    `(float) level.getGameTime() + partialTicks` model sites take a float partial tick, so binary promotion already made
    them the same sum. `X.getGameTime() + (double) partialTick` became `gameTimeAsDouble`.
  - **Not changed (A-4 stays PENDING):** wall-clock animation (29 client classes: monolith and Qliphoth item
    renderers, the Liber tab views, hint screens, the Mnemonic Reliquary hold timer and others, pinned by the
    guardrail) and the per-frame `animTime += 0.016` counters. Moving any of them onto game time changes how they
    behave under `/tick freeze` and pause, which is the A-4 decision. Render events and HUD layers keep the partial tick
    they are handed. The three common-side helpers (`AwakenedIchorianSigilEntity`, `HumanitySpriteEntity`,
    `CovenantThroneBlockEntity`) cannot name a client class and are listed.
  - **Visual check:** the HUD layer review (`after9`) is byte-identical to the baseline in all 8 scenes, overlays whose
    time reads moved included. The screen chrome review matches item 1's result in 24 of 30 screens. The other six are
    the wall-clock screens, which vary between runs: the five item 1 named, plus Vascular View (4 pixels), whose vein
    clock reads `Util.getMillis` twice in its first frame. Item 9 did not touch that screen. Rite floor and Outpouring
    (`after9`) match their baselines by eye. Shots: `build/step10-review/item9/`.
- **Item 9 metrics:** `src/main` 138 files, 190 time reads moved to 1 helper; raw client time reads left 0.
- **Item 9 tests:** `BestiaryEntityPreviewSourceTest` repointed to `AnimationTime.partialTick()`.
- **Step review:** a fresh reviewer read `29a5576ae..e2597dc41` (code only) and found no behaviour change in any item.
  It checked item 9's replacements line by line, item 8's claim chain, item 7's clocks against both old ones, item 4's
  layer depth and `hideGui` handling, the encoder arithmetic, the include resolution and the particle wire form.
  Acted on:
  - `monolith_noise.glsl` read its importers' uniforms and a constant, which the guardrail missed because it only looked
    for the word `uniform`. The two seeded fractal noises went back into the shaders, and
    `ShaderNoiseIncludeGuardrailTest` now fails an include that names any uniform or constant its importers declare. The
    render-type-profile client relaunched with no shader error, and its scene matches the baseline.
  - Time reads the item 9 pattern missed: `CardinalFloorConsecrationRenderer` (`+ (double) partialTick`), the Earthen
    Vein markers, Priestess impact and Vesper burrow camera shakes and the NPC progression marker (the event's partial
    tick), and 14 models adding HutosLib's cached partial tick to an entity's age. Each now passes the same value through
    `AnimationTime`, and the guardrail pattern covers casts, event partial ticks and `HLClientUtils.getPartialTicks()`.
    Item 9 totals about 200 reads in 156 files.
  Noted, not changed:
  - Since item 2, back-to-back requests for one seed with equal uniforms share a batch, and a `sortOnUpload` type sorts
    that batch by distance. Before, every call made a new type and drew in submission order. No caller asks for one seed
    repeatedly with overlapping geometry, and the seven review clients matched.
  - Near-copies that are not token-identical stay local: the infusion veins' `smoothNoise` and `fbm` (`0.57` written
    for `.57`) and `latent_infection_skin`'s `hash` and `noise` (parameter `value`).
  - The new layers `screen_effects` and `hud_markers` can be cancelled through `RenderGuiLayerEvent.Pre`, as any layer
    can.
- **Metrics (whole step):** screen chrome copies 0 (14 vein backgrounds, 36 borders, 12 slot wells shared); per-call
  render types 0; colour-only particle data classes 7 → 0 and particle type classes 10 → 1; `RenderGuiEvent` handlers
  6 → 0; `RenderLevelStageEvent` subscribers 9 → 2 (both in the dispatcher); material encoders 4 → 1; shared noise
  closures moved into 9 includes; replay clocks 2 → 1; mixin pose calls 3 → 1; raw client time reads about 200 → 0.
- **Guardrails added:** `ScreenChromeGuardrailTest`, `RenderTypeCachingGuardrailTest`, `ParticleDataGuardrailTest`,
  `HudAndWorldDrawingGuardrailTest`, `MaterialQuadGuardrailTest`, `ShaderNoiseIncludeGuardrailTest`,
  `BossAnimationClockGuardrailTest`, `PlayerAnimationGuardrailTest`, `AnimationTimeGuardrailTest` (see the table).
- **Deviations from the prompt:**
  - Item 4: the three `RenderGuiEvent.Pre` handlers were methods of one class, so their run order was the JVM's method
    order. The layer pins the order the review client's baseline draws (nectar and Circus, grade, white humor), not
    the order a listener probe printed in another run.
  - Item 7: only Vesper and Annetta moved onto `BossReplayClock`. The other bosses read raw synced ticks, and moving them
    would change displayed timing, so they wait on A-1b. The Saints have no synced-tick animation.
  - Item 8: ticks, packets, session clears and first-person renderers stay with each animation; the registry owns the
    third-person order and the guardrail pins the first-person priorities.
  - V-4 (optional) not done.
- **Pending approvals:** A-4 and V-3b stay PENDING (items 9 and 5); V-6b was added by item 3, V-3c by item 5 and A-1b by item 7.
- **Handoff notes for step 11:**
  - The baseline worktree `../Hemomancy-step10-before` was removed (junction first).
  - **Deterministic review harnesses:** `HudLayerReview` (run `hudLayerReviewClient`, save `HudLayerReview`) and
    `ScreenChromeReview` draw by hand into a black target with game time, partial tick and vignette pinned, so before
    and after runs compare byte for byte. `RenderTypeProfileReview` now saves `profile-scene-<label>.png`. The live
    review clients (Outpouring, rite floor, Annetta, Vesper, axonal) vary between runs; compare them by eye or against
    a second baseline run.
  - A review client started while sources are being edited builds whatever is on disk at that moment; let a run finish
    before editing the code it checks.
  - **Review clients:** a fresh game directory without `options.txt` stops at the first-launch screen and the driver
    never logs `startup`; copy one in. The condenser review directory had none.

### Step 11: Phase 10, god-class decomposition
- **Status / date / commits:** DONE, 2026-10-23. One commit per item, prefix `consolidation(step-11):`.

  | Item | Status | Commit |
  |---|---|---|
  | Characterisation suite for item 1 (written and green before the extraction) | DONE | `91bb77eba` |
  | 1. `RiteKind` strategy, per-kind state, `CardinalRiteActivation`, Blood Structure crafting out of the packet | DONE | `b6c7bd846` |
  | F-11 (crafts tick once per server tick) | NOT DONE: PENDING in *Decisions* | |
  | Characterisation suite for item 2 | DONE | `866c0b990` |
  | 2. Per-NPC dialogue-event routing | DONE | `3385bc323` |
  | Characterisation suite for item 3 | DONE | `ced99f334`, `0871dc5e6` (a boolean argument read as an object hash in the snapshot) |
  | 3. `HemoCommand` split | DONE | `8ba776eca` |
  | Characterisation suite for item 4 | DONE | `017d6f7dc` |
  | 4. Registration-time `setEntityCast` | DONE | `7028ada7d` |
  | Characterisation suite for item 5 | DONE | `44719064f` |
  | 5. Scriptorium operations into its block entity | DONE | the commit after `44719064f` |
- **Test counts before → after:** `test` 4,055 → 4,064 (characterisation commit) → 4,067 (item 1) → 4,070 (item 2),
  0 failures; 4,073 after item 3; 4,075 after item 4; 4,077 after item 5. `alphaCheck` after item 1: BUILD SUCCESSFUL, all 1,068 required GameTests passed
  (1,036 before; 32 are the new `rite_kind_validation` suite). After item 2: all 1,069 (plus
  `dialogue_route_validation`). After item 3: all 1,070 (plus `hemo_command_validation`). After item 4: all 1,072
  (plus the two `entity_cast_validation` tests); one run before that failed the known flaky fish-AI timing test
  `strongerpreyneedsmultiplebitesbeforereward` (step 10 recorded it), and the rerun passed. After item 5: all
  1,073 (plus `scriptorium_operation_validation`); the opt-in `scriptorium_validation` suite (22) also passed.
- **Impact map:** one read-only reviewer listed the tick's branches, `ActiveCardinalRite`'s fields and keys, the
  packet's parts and every test pinning them (not committed; a working list).
- **Characterisation (before the extraction):**
  - New GameTest suite `rite_kind_validation` (alpha gate, focused launcher `runRiteKindGameTestServer`, about a
    minute). Rites run through the real `onLevelTick`, with a `CapturedPlayer` caster that is listed online and
    records chat. 30 tests at first, all green on the old code; item 1 added the kind assertions, the login-snapshot
    test and the recipe-gone retirement test (32).
  - `ActiveCardinalRiteNbtLayoutTest` recorded each kind's saved NBT from the old code.
  - `CapturedPlayer` gained chat capture and `online()` / `offline()`. `StationUpgradeActivationGameTests.prepare` /
    `practice` and `SuccessionGameTests.fixture` / `setup` / `recipe` became package-visible for reuse.
- **Item 1, `RiteKind` (L-6, N-8):**
  - `rite/harbinger/RiteKind` is an enum strategy, resolved from the rite's state and recipe in declaration order:
    `SUCCESSION`, `STATION_UPGRADE`, `WORKING_CRAFT`, `PUPPETEER_TRIAL`, `COUNTDOWN`, `CEREMONY`. Hooks: `admit`
    (tick, wait or break), `isWorking` (pause instead of binding, no collapse backlash, wait at an incomplete finish),
    `intact` (the craft's subject check), `capturesOfferingsAsItRuns`, `holdsOnlyItsStructure`, `holdsBeforeFinale`,
    `runOwnCourse` (Succession), `advance` (countdown or verify-and-run, final, with the old countdown test first so
    ordering is preserved by construction), `advanceCeremony` (the Puppeteer trial phase), `complete`,
    `returnEscrow` and `release` (termination).
  - `onLevelTick` keeps the shared course in its old order and asks the kind at each fork. `CardinalRiteTermination`
    dispatches to the kind instead of calling every controller. `enterFinale` asks `holdsBeforeFinale`.
  - Moved out of `HarbingerCardinalRiteEvents` (1,690 → 912 lines): completion to `CardinalRiteCompletion` (public
    `complete`, replacing 12 GameTest sites that reflected on the private `completeRite`); structure checks to
    `CardinalRiteStructure`; the client snapshot (entries, checklist, blobs, segments, sync) to `CardinalRiteSync`;
    `ritualFootprintRadius` to `CardinalRiteFootprintResolver.radius(rite, recipe)` (4 callers).
  - Per-kind state behind `ActiveCardinalRite` (1,413 → 1,368 lines): `PuppeteerTrialState` (the seven trial fields,
    flat keys), `WorkingCraftState` (`Craft`), `StationUpgradeState` (`StationUpgrade`, still reading
    `AlembicUpgrade`), `SuccessionState` (`Succession`). Typed accessors for the keys the rite itself reads; the
    escrow, snapshot and Succession keys stay in each state's `tag()` for `RiteOfferingEscrow`,
    `StationUpgradeRites`, `WorkingCraftRites` and `SuccessionRites`. NBT unchanged (`ActiveCardinalRiteNbtLayoutTest`).
  - `BloodCraftingKeyPressPacket` (700 → 63 lines) now picks once and calls `BloodStructureCrafting.tryCraft`
    (new, `rite/structure`) then `CardinalRiteActivation.tryStart` (new, `rite/harbinger`). Its byte layout is unchanged.
  - Out of `common/network`: `BloodStructureCraftingHelper`, `BloodCraftingPatternSearchRules`,
    `BloodCraftingPatternBlockRules` and `BloodStructureVisiblePositionRules` to `rite/structure`;
    `BloodlineChatRateLimit` to `capability/player/harbinger/bloodvolume` (tests moved with them).
  - Source-text tests: the pinned tick block, the retirement checks, the escalate-call count and the activation ring
    check were replaced by `rite_kind_validation` GameTests (cited in each test); `CardinalRiteSigilFeedbackSourceTest`,
    `CardinalRiteProgressionResourceTest`, `BloodStructureFloorlessSourceTest`, `BloodlineScreenRegressionSourceTest`,
    `BloodProjectionStructureCraftingResourceTest` and `PuppeteerIntegrationSourceTest` were retargeted to where the
    text now lives (moved verbatim; mostly negative guards).
- **Metrics:** item 1 is about 59 files, +2,500 / -1,900 lines, most of it moved. 10 new main classes, 2 guardrails.
  Call sites migrated: 12 reflective completions, about 30 activation callers, 4 footprint callers, about 60 tag-bag
  readers. Remaining legacy instances: 0 kind checks in the shared course; the `StationUpgradeRites.isRite` checks
  inside `ActiveCardinalRite` (`sealAltar`, `ringOpenerCount`, circuit arithmetic) are state logic and stay.
- **Guardrails added:** `RiteKindGuardrailTest`, `NetworkPackageGuardrailTest`, `ActiveCardinalRiteNbtLayoutTest`,
  the `rite_kind_validation` suite.
- **Approved items implemented:** none. F-11 is PENDING, so `PendingBloodCraftManager.tick()` still runs once per level
  per tick (its `clear()` has been wired since step 5, so the audit's "never cleared" is stale).
- **Pending approvals:** F-11.
- **Deviations from the roadmap:**
  - The roadmap names `matches`, `tick`, `validate`, `complete` and `terminate`. The hooks are finer (`admit`,
    `isWorking`, `advance`, `returnEscrow` / `release`) because the old tick forks at more points than five, and
    collapsing them would have changed the order.
  - The client snapshot moved too (`CardinalRiteSync`), so the shared course reads without view code.
  - Working crafts complete through `passesFinalGates` then `WorkingCraftRites.complete`. That equals the old branch
    because no Working craft is Unstained (all 20 `working/*.json` checked).
- **Final review:** one fresh reviewer read the diff and confirmed no behaviour change, no frozen identifier change
  and no client reach. Fixed from its minors: the manifested-puppet structure rule is the base rule for every kind
  again (exact under a datapack reload); a recipe-gone retirement GameTest that returns an escrowed staff; the
  reflection guardrail catches the split-line form; one copy of the Unstained level-gate bypass (both call
  `UnstainedAccessRules` directly); `CardinalRiteClientView` renamed `CardinalRiteSync`; `ARCHITECTURE_MAP.md`
  updated. Left as noted: Puppeteer and Unstained activation starting ungated is no longer pinned (the old check was
  source text); several former `test` checks now run only in `alphaCheck`.
- **Not verified:** no in-game client run (the change is server-side and behaviour-preserving; the characterisation
  suite drives every kind through the real tick).
- **Item 2, per-NPC dialogue routing (O-4):**
  - Characterisation first (`866c0b990`): a golden-master GameTest posts every id the handler routed, three ways
    (D0 to its speaker, supplied D7 to its speaker, D7 with no speaker), and fingerprints the reward flag, chat, the
    diff of the player's saved NBT (inventory, persistent data, attachments), drops and new Hemomancy advancements.
    UUIDs and bearing distances are normalised; three recordings were identical. 233 of the 510 fingerprints carry
    chat and 210 a saved-data change; the silent ones are mostly the no-speaker passes.
  - `DialogueEventHandler` (1,531 lines) is gone. `dialogue/route/DialogueEventRouter` is the only subscriber and
    tries an ordered table: the shared routes in their old order (station claims, `antecedent_`, `clinical_lesson_`,
    Annetta, `circus_school_`, Ringmaster), then one class per NPC: Mnemonist, Alchemist, Artificer, Vicar, Voyager,
    Vein Mason, Acolyte, Zealot, Guardian, Hermit, Sanguine Monolith, fungal whisper, Scout, plus recruitment (spoken
    by three NPCs) and `DialogueRewards` (shared give, drop, count and take). 2,031 lines in 19 classes, the largest
    301. Methods and cases moved verbatim (generated, then checked label by label by the reviewer); two `return;` in
    the Archon choices became `return true;` (the old handler stopped there).
  - The old `consumeInventory` folded into `DialogueRewards.consumeItemCount`; the only difference is an
    `Inventory.setChanged()` on the Woven Vessel turn-in (a dirty counter, not saved, no gameplay effect).
  - `hasClaimedConsecrationKit` moved to `ArtificerDialogueEvents`, `hasClaimedMonolithCornerstone` to
    `SanguineMonolithDialogueEvents` (4 callers). `CircusIntroductionRules` became public for the Mnemonist route.
  - 14 source-text tests read the handler; they now read `SourceAssertions.readDialogueRoutes()` (every route class,
    joined), so the same text is pinned wherever it now lives. `gradle/source-text-impact-map.py` maps that helper to
    each route class.
  - Final review: no behaviour change, no identifier change; fixed its findings: five cases that were space-indented
    in the old file had landed in the Mnemonist class and now sit with the Vicar and the Alchemist; the seal scan
    covers every route class again; a comment, an unused import.
- **Item 3, `/hemo` per subcommand:**
  - Characterisation first: `HemoCommandTreeTest` (JVM, the syntax snapshot) and the `hemo_command_validation`
    GameTest (every executable path, fingerprinted; three identical recordings). No branch had a behaviour test before.
  - `HemoCommand` (1,979 lines) is now the root alone (one `requires(hasPermission(2))`) wiring 16 classes in
    `common/command`: `BloodSubcommand`, `BloodlineSubcommand`, `DegreeSubcommand`, `MorphlingSubcommand`,
    `QliphothSubcommand`, `SkillsSubcommand`, `UnstainedSubcommand`, `OrgansSubcommand`, `TendencySubcommand`,
    `BloodMoonSubcommand`, `FaneSubcommand`, `ChamberSubcommand`, `WillSubcommand`, `SummonsSubcommand`,
    `ManipulationsSubcommand`, `SlotsSubcommand` (2,382 lines with headers; the largest 263). Each `build()` returns its
    branch verbatim; each class holds the handlers its branch reaches. The Will branch calls the tendency parsers in
    `TendencySubcommand`. The root's stale command index became a pointer to the classes.
  - Eight source-text tests now read their subcommand's file; `FanePreviewCommandSourceTest`'s exact-indentation
    check compares without whitespace.
  - Final review: the tree and all 89 members verified identical by machine; fixed its finding (`resetUnstained`
    had been placed with the degree branch because a local variable shares its name) and its guardrail suggestion
    (each subcommand wired exactly once). Noted, not changed: the subcommands keep the old wildcard imports.
- **Item 4, entity casts as a registration hook (L-1):**
  - Characterisation first (`017d6f7dc`): every entity cast fingerprinted through the shared entry point; three
    identical recordings.
  - `EntityCast` (functional interface) and `BloodManipulation.setEntityCast` / `getEntityCast` (transient, like the
    Drudge action). The 34 bodies moved verbatim to `EntityCasts` (670 lines, `crimsonSight` renamed `hematicBeacon`
    after the manipulation it serves); `ManipulationInit` attaches `.setEntityCast(EntityCasts::x)` beside each Drudge
    action. `EntityManipulationEffects` (731 → 29 lines) keeps the shared paralysis and school-damage rules and runs
    the hook; `isSupported` is "has a cast and is not retired", which equals the old 32-name set (the two retired
    ones among the 34 are `glacial_bastion` and `blood_eclipse_mantle`). Every caller uses the registry instance.
  - Source-text tests: `VesperManipulationCoverageTest`'s two parses of `SUPPORTED_CASTS`, the Will kit-id check and the
    gap-combat id check became the registry GameTest; `ClawSlashRendererSourceTest` and
    `LivingWeaponGraftUnlockSourceTest` read `EntityCasts.java`. Stale references fixed in `ARCHITECTURE_MAP.md`,
    `MANIPULATION_BALANCE_PLAN.md` and `wiki/School-Combat.md`.
  - Final review: no defects; added its guardrail suggestions (cast named for its manipulation; only the entry point
    runs a cast).
- **Item 5, Scriptorium operations in its block entity (L-9):**
  - Characterisation first (`44719064f`): `scriptorium_operation_validation` drives every button through the real
    menu with a player beside the station and fixed seeds (the enchantment seed through `onEnchantmentPerformed`, the
    player's random reseeded before the curse roll); three identical recordings.
  - `EnzymaticScriptoriumBlockEntity` gained `inscribe(actor, row, cost, offer, enzymeCosts, targeted)` and
    `denature(actor)`: the old button bodies, moved verbatim (the enzyme check reads the costs the menu passes, built
    from the same data slots `hasOfferEnzymes` read, right after the same `refresh`). The blood costs are one constant
    there (`inscriptionBlood(row)`, `DENATURE_BLOOD`), which the screen now reads instead of its own copy. The
    per-player offer preview (`refresh`) stays in the menu, which checks the actor, the rite lock and reach, rolls the
    offer and dispatches. The unused `consumeSelection` was deleted. `BloodAccountingGuardrailTest`'s allow-list entry
    moved with the two drains.
  - No source-text test pinned the moved code; the opt-in `EnzymaticScriptoriumGameTests` pass unchanged.
  - Final review: no defects (guard expression, effect order, `markAndSync` count, denature, menu guard, client
    reach and identifiers all checked). Applied its nits: the guardrail also rejects `drainBlood(` and
    `setEnchantments(`; import order. Left as noted: the golden never hits lapis short, xp short, enzymes short with a
    positive cost, an unenchantable input or Creative `instabuild` (that guard moved verbatim; scenarios added now
    would be recorded from the new code); `inscribe` trusts its caller's row (the menu passes 0-2).
- **Step summary:**
  - Metrics: five god classes decomposed: `HarbingerCardinalRiteEvents` 1,690 → 912 lines (`ActiveCardinalRite`
    1,413 → 1,368), `BloodCraftingKeyPressPacket` 700 → 63, `DialogueEventHandler` 1,531 → deleted (19 route classes),
    `HemoCommand` 1,979 → root plus 16 subcommands, `EntityManipulationEffects` 731 → 29, the Scriptorium menu's two
    operations into its block entity. Five characterisation suites, each recorded from the old code before its move.
  - Guardrails added: `RiteKindGuardrailTest`, `NetworkPackageGuardrailTest`, `ActiveCardinalRiteNbtLayoutTest`,
    `DialogueRoutingGuardrailTest`, `HemoCommandTreeTest`, `HemoCommandStructureGuardrailTest`,
    `EntityCastGuardrailTest`, `StationMenuGuardrailTest`, and the GameTest suites `rite_kind_validation`,
    `dialogue_route_validation`, `hemo_command_validation`, `entity_cast_validation`,
    `scriptorium_operation_validation` (all in `alphaCheck`).
  - Frozen identifiers: unchanged (the payload registry snapshot, the rite NBT layout test and the `/hemo` tree
    snapshot are green).
  - Approved items implemented: none. Pending approvals: F-11.
  - Not verified: no in-game client run for any item (all server-side or menu dispatch; each is characterised by
    GameTests through the real entry point).
- **Handoff notes for step 12 (persistence authority migrations):**
  - Every step-12 item (S-9, F-21, S-6c, S-1c, S-7, O-5) is PENDING in *Decisions*. The step implements only APPROVED
    items, so it needs the user's approvals before it can do anything but the guardrail.
  - Pitfalls kept from this step: inline Python heredocs passed through the shell tool lose their backslashes (write
    edit scripts to a file first); a JVM snapshot must not print object hashes (they differ between JVMs); check the
    test results before committing; golden-master GameTests need UUIDs and bearing words normalised, and a captured
    player needs its spawn invulnerability cleared.

### Step 12: Phase 11, persistence authority migrations
- **Status / date / commits:** DONE, 2026-10-23. Prefix `consolidation(step-12):`, one commit per item.

  | Item | Status | Commit |
  |---|---|---|
  | Approvals: S-9, F-21, S-6c, S-1c, S-7 APPROVED by the owner at the start of the session; O-5 left PENDING | DONE | recorded in `b205211d0` |
  | S-9 `PlayerDataVersion` + `PlayerDataMigrations`, existing login migrations gated, fungal scar tick sweep removed | DONE | `b205211d0`, frozen id `b314e8204` |
  | F-21 Archon path the only authority | DONE | `227a7edee`, review fix `64dd755d0` |
  | S-6c `CircusProgress` attachment | DONE | `b22b8283f` |
  | S-1c `ChamberAccess` and `PendingWhisperInbox` attachments | DONE | `9185e427e` |
  | S-7 two-tier cooldowns (`PlayerCooldowns`, UUID-keyed passive map) | DONE | `b575b0593` |
  | O-5 typed components for reworked items | NOT DONE: PENDING (no item is being reworked) | |
- **Test counts before → after:** `test` 4,077 → 4,083 (S-9) → 4,084 (F-21) → 4,084 (S-6c, S-1c) → 4,086 (S-7, review
  fix), 0 failures. `alphaCheck` green after F-21 (1,076 GameTests), S-6c (1,077), S-1c (1,080), S-7 and the review fix
  (1,081, of which 8 are `player_migration_validation`). One combined run flaked on
  `circussuccessionrunnercompletesatdegreefour` ("Expected one CircusRingmasterEntity, found 0") and passed on rerun.
  Opt-in suites: clinical 131/131 (the two long-standing failures `journeyacceptssilentchoiceafterrealprojection` and
  `journeyrestoresnumericfungalprojectionstate` now pass, see S-6c), scriptorium 22/22, antecedent 14/14.
- **The migration table** (`PlayerDataMigrations`, run once per save at `HIGHEST` login priority, before every other
  login handler and the `LOWEST` `PlayerStateSync` send; one log line per migrated player):

  | Version | Id | Reads (old location) | Writes | Sunset |
  |---|---|---|---|---|
  | 1 | `fungal_scar_items` | five retired fungal scar ids in the scar slot and inventory | the surviving scar | 2027-04-23 |
  | 2 | `station_upgrade_claims` | `advanced_brewing` / `resonant_forge` attachments, two Armature root flags (still behind its own `LegacyMigrated` flag) | `station_upgrades` | 2027-04-23 |
  | 3 | `muscle_memory_priming` | Muscle Memory's legacy `Primed` map | reserves | 2027-04-23 |
  | 4 | `liber_legacy_notes` | field-note items (now a no-op scan); Scar Practice backfill from the Vein Mason advancement | `liber_knowledge` | 2027-04-23 |
  | 5 | `archon_choice` | root `hemomancy:archon_choice_made` (removed) | the degree's Archon path, only if NONE | 2027-04-23 |
  | 6 | `circus_progress` | root `hemomancy.circus_*` durable keys (removed) | `circus_progress` | 2027-04-23 |
  | 7 | `chamber_access` | five root `hemomancy:chamber_visit_*` earned keys (removed), then the advancement backfill once | `chamber_access` | 2027-04-23 |
  | 8 | `pending_whispers` | root `hemomancy:pending_whispers` (removed) | `pending_whispers` | 2027-04-23 |
  | 9 | `persisted_cooldowns` | eight cooldown root longs (removed) | `cooldowns` | 2027-04-23 |

  Not gated, on purpose: the Vesper Memory root-key migration inside its `PendingDelivery` claim read (AGENTS
  contract); the bloodline founding and bloodline re-pointing reconciles (they compare against world data, which can
  change while the player is offline); the per-login eligibility backfills (degree, manipulations, Still Arts, staff,
  clinical recipes, station lessons); deserialize-time reads inside existing attachments (Liber book-id rename,
  `familyMemoryVersion`, bestiary normalising), which run before login; the Scriptorium chunk mixin (world-level, not
  per player). Muscle Memory still migrates legacy priming lazily on read; that map is empty once saved.
- **S-9:** `PlayerDataVersion` (`data_version`, copyOnDeath, key `Version`). The per-100-tick fungal scar inventory sweep
  and the Liber respawn/dimension re-runs are gone. `FungalScarMigrationEvents` was deleted (its sweep is
  `FungalScarMigrationRules.migrateCarried`); its source test was updated. The five retired scar ids are unregistered
  with no alias, so their stacks already load as empty: the migration is kept for the table but can never match.
- **F-21:** the Eighth Degree answer no longer writes the root key; the migration reads it once, applies it to an unset
  path only and removes it (a leftover key can no longer override or resurrect a path). `QliphothSubcommand`'s clear
  still removes the key. The final review found the four `blood_lust_to_silent_archon_*` Armature recipes gating on
  that key: `PersistentDataGate` now answers it from the path (`SILENT_ARCHON` → `"silent"`), so the recipe JSON and the
  serializer's stream are unchanged. **Behaviour change:** Silent Archons who chose after `21f80dbfb` (when the key
  stopped being written) could not pass that gate before; now they can.
- **S-6c:** discovery, acclimation, route and repair, challenges, milestones and the apprenticeship record moved into
  `CircusProgress` behind the unchanged facade methods. The live act timers (`circus_active_*`, `circus_challenge_*`)
  stay session root keys, and pavilion sites stay world data (the audit's "four stores" counted those; they are not
  durable player progress). The clone handler keeps its act cleanup and pavilion release. Reading the apprenticeship no
  longer creates an empty root compound; that side effect broke the journey-restore postcondition in the two clinical
  tests, which now pass. The hemo-command and dialogue-route goldens changed only where the empty compound moved.
- **S-1c:** `ChamberAccess` (five earned flags) and `PendingWhisperInbox` (entries keep `{id, tree, decision}`); both
  hand-written clone handlers deleted. Visit session keys stay root keys. Login and respawn no longer re-derive access
  from the advancements; both advancements are `minecraft:impossible` and granted only by `bindChair`/`attune`, so the
  once-only backfill differs only for an operator `/advancement grant` after migration.
- **S-7:** `PlayerCooldowns` (`cooldowns`) holds the persisted tier. Its copy handler returns null, so it survives relog
  and resets on death and End return exactly as the root longs did. `ManipulationReactiveEvents.PASSIVE_COOLDOWNS` is
  keyed by player UUID (same clears). Deviation: cooldowns that are fields of a system's own copyOnDeath attachment
  (Last Rite, Blackhearted, Mycophant retry, Muscle Memory, Will ambush) stay there; moving them would rewrite keys
  inside existing attachments, which the step forbids. Effect windows (`silent_slipping_until`, `sheolic_bastion_*`,
  `weave_*_until`) and the `ScriptoriumLastShot` same-tick dedup are not cooldowns and stay root keys.
- **Manual load of an existing development world:** `run/saves/New World (28)` (5.9 GB, last played 2026-10-23),
  copied to `build/step12-world-load/saves/New World (28)` and opened with quick play from a scratch init script
  (`runStep12WorldLoadClient`, runClient's classpath, username `Dev`, whose offline UUID matches the saved player), then
  closed through the window so the integrated server saved. Result: `Migrated Dev's saved data from version 0 to 9 (9
  migrations)`, no Hemomancy ERROR line, all dimensions saved. The saved player now has `data_version` 9 and no
  circus, chamber-access or cooldown root keys; `circus_progress` holds acclimation 622, discovered, 8 milestones and the
  8 apprenticeship entries, `chamber_access` holds attuned and chair bound, `cooldowns` holds the Masquerade ability and
  Phantasmal reaction cooldowns, all equal to the old values. Other attachments changed only by ticking (blood and
  vascular regeneration, `school_combat.observedAt`). The log's other ERROR lines are chunk attachments from a
  `thaumaturge` mod this world was once played with, plus shutdown `ClosedChannelException`s. A first attempt as
  `WorldLoad` showed the bloodline reconcile unlinking the bloodline because the UUID differed; that was the launch,
  not a migration. The original world was not opened.
  Two orphan root keys in that save, `hemomancy.circus_exposure_ticks` and `hemomancy.circus_safe_floor`, have no
  reader or writer in the code or its history; they were left alone (the old clone handler copied them).
- **Final review** (fresh Opus reviewer over `git diff a83d742f5..HEAD`): one confirmed defect, the Silent Archon
  recipe gate, fixed in `64dd755d0` with a GameTest and a guardrail scanning Java and JSON for the literal key. Also
  applied: the whisper merge into a non-empty inbox is now covered; the impact map is regenerated. Checked clean:
  remaining old-key readers, circus key coverage, value types, death and End-return semantics per store, client
  cooldown sync, login order, frozen identifiers, dead code, client isolation. Left as noted: the cooldown guardrail
  only catches a "cooldown" name in the same statement (the allow-list's exact counts are the stronger check); the
  migration guardrail lists known entry points; `HarbingerRepairGameTests`'s whisper test copies the inbox by
  deserializing; the journey snapshot can still restore the legacy Archon and Armature root keys from an old snapshot
  (harmless: nothing reads them now).
- **Metrics:** 60 files, +1,560 / −302 lines before the review fix (main +699 / −223, tests +852 / −72). Six classes
  added (`PlayerDataVersion`, `PlayerDataMigrations`, `CircusProgress`, `ChamberAccess`, `PendingWhisperInbox`,
  `PlayerCooldowns`), one deleted (`FungalScarMigrationEvents`). Three clone handlers stopped copying root keys (two
  deleted, the circus one keeps its cleanup); `ManipulationReactiveEvents`' Endless Hour clone handler is the last
  hand-written copy. `getPersistentData()` call sites in `src/main/java`: 468 in 100 files → 426 in 95 files, all
  listed with reasons in the allow-list. Root keys removed from saves by migration: 7 circus families, 5 Chamber access
  keys, the whisper list, 8 cooldown families and the Archon choice key.
- **Guardrails added:** `PlayerDataMigrationGuardrailTest`, `PersistentDataAllowListTest`, `CooldownTierGuardrailTest`,
  `PlayerDataMigrationsTest` and the `player_migration_validation` GameTest suite (in `alphaCheck`).
- **Frozen identifiers:** no key inside an existing attachment renamed; no payload, command or recipe JSON change. New
  attachment ids `data_version`, `circus_progress`, `chamber_access`, `pending_whispers` and `cooldowns` were added to
  `src/gameTest/resources/frozen/registry-ids.txt`.
- **Docs:** AGENTS.md (migration, allow-list and cooldown-tier conventions), standards §6. No player-facing mechanic
  changed apart from the Silent Archon recipe gate above, so `HEMOMANCY_REFERENCE.md` and the wiki were not changed.
- **Pending approvals:** O-5, plus every earlier PENDING item.
- **Handoff notes for step 13:**
  - Add a migration as the next row of `PlayerDataMigrations` with a sunset date, plus a fixture under
    `src/gameTest/resources/migration/`; lower `persistent-data-allow-list.txt` counts when you remove calls (the
    scratch script that does it refuses increases; a hand edit is fine).
  - All step-12 read paths sunset on 2027-04-23: after the first release past that date, delete migrations 1-9's read
    code (the table rows stay as unused version numbers or are deleted together).
  - L-7 (step 13) persists block blood only through the attachment and needs a read fallback; it is block-entity data,
    so it does not use the player table.
  - Pitfalls: an `@EventBusSubscriber` class left with no `@SubscribeEvent` method fails at startup; quick play
    (`--quickPlaySingleplayer`) plus `Process.CloseMainWindow()` is a clean way to load and save a dev world; launch as
    the save's username or per-login reconciles act on a different UUID.
