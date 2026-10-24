# Hemomancy Architecture Audit

Status: analysis only, taken 2026-10-21 at commit `3fae723d0` on `neo-1.21.1`.
No production source or resources were changed to produce it.

Companion documents:
- [ARCHITECTURE_MAP](ARCHITECTURE_MAP.md): how the systems fit together.
- [CANONICAL_STANDARDS_PROPOSAL](CANONICAL_STANDARDS_PROPOSAL.md): the recommended conventions.
- [CONSOLIDATION_ROADMAP](CONSOLIDATION_ROADMAP.md): phased plan.
- [QUICK_WINS](QUICK_WINS.md): low-risk first changes.
- [evidence/](evidence/): eleven subsystem reports with file:line citations. This
  document summarises them. When a number here and a number there disagree, the
  evidence report is the more detailed source.

Paths are relative to `src/main/java/com/vincenthuto/hemomancy/` unless they start
with `src/`, `docs/`, `tools/` or `build`.

---

## Evidence labels

| Label | Meaning |
|---|---|
| **V** (verified) | Read in code or counted with grep or a script. |
| **L** (likely) | Strong static evidence; not run in game. |
| **H** (hypothesis) | Inferred. Each one states what evidence would settle it. |
| **SC** (spot-checked) | The lead auditor re-checked this directly, on top of the subagent's own verification. |

How the audit was done:
1. A repository survey.
2. Eleven parallel read-only audits, one per subsystem: registration, networking,
   player state, gameplay logic, rendering, content class design, naming and dead
   code, tests/build/worldgen, cross-family gameplay mechanics, animation, and
   performance/lifecycle.
3. The lead auditor spot-checked the highest-impact claims. Those checks are marked **SC**.

---

## A. Executive summary

Hemomancy is large: about 4,900 main Java files and about 437,000 lines. It is
fully on the NeoForge 1.21.1 APIs:
- No real Forge-era API remains.
- There are no `LazyOptional` or `ICapabilityProvider` capabilities.
- There is no `SimpleChannel`.
- Every player attachment uses one serializer style.

The underlying platform migration is done. What remains is drift *above* the
platform: the same job is solved several ways, by different generations of code.

**The most important pattern** appears in almost every subsystem. A good shared
mechanism already exists, but only part of the codebase uses it. Most of the
recommended work is migrating callers onto existing code, not writing new code.

| Shared mechanism that already exists | Who uses it | Who bypasses it |
|---|---|---|
| `BloodManipulation.tryPerformAction` (cast gate) | All 91 manipulations | Nobody. This one is complete. |
| `BloodFlowLedger.applyDrain` (blood upkeep, Sanguine Reserve, diagnostics) | Passive income and some upkeep | About 98 direct `drain()` calls in 81 files: items, machines, blocks, rites, packets, scars, living weapons |
| `SchoolDamage` + `ManipulationCombatHelper.canHarm` (damage attribution, ally and PvP gate) | Manipulations, morphlings | Armour abilities, Still Arts, scars and about 25 raw `Monster.class` filters |
| `HemoCapabilityAccess` (attachment access) | 1,158 call sites | About 42 direct `getData` calls, plus about 466 raw `getPersistentData()` calls |
| `HemoCapabilityRegistrar` login/respawn/dimension sync trio | 2 attachments | 17 classes copy the same trio by hand |
| `TransientServerStateEvents` (session-state reset) | 19 managers | 21 classes with their own stop handlers; about 11 maps never cleared |
| `common/data/JsonResourceReloadListener` | 2 loaders | 6 loaders copy its boilerplate |
| `ManipulationVisualPacket` (data-driven VFX payload) | 23 call sites | 6–8 single-purpose particle payloads |
| `VeinBackgroundRenderer`, `ScreenDrawUtils` (screen chrome) | 6 screens | 16–20 screens with private copies |
| `RiteOfferingEscrow` and the brazier-rite family | Working crafts, station upgrades | Puppeteer trial and Succession consume offerings their own way |
| `VesperAnimClock` (boss animation replay) | Vesper | Five other boss sync conventions |
| HutosLib `SimpleInventoryBlockEntity` | Nobody | 12 hand-written container block entities |

**Major sources of complexity**, in order of how much they cost a developer:

1. **Tests that pin source text.** Between 383 and 471 test files read Java source as
   text and assert on it. The two counts use different definitions (see section H).
   Any split, rename or move breaks dozens of tests with no behaviour change. This is
   the main reason a refactor is expensive today. It has to be dealt with first.
2. **Two storage substrates for player state.** These are attachments and raw
   persistent NBT, with four key-naming styles, five hand-written death-clone
   handlers and live dual-writes.
3. **Unordered cross-family gameplay mechanics.** 48 damage listeners, 35
   damage-modification sites and four death-save stages run with no defined order.
   Blood paid by non-manipulation features skips the ledger.
4. **God classes.** These include `ClientEvents` (1,495 lines), `HemoCommand` (1,982),
   `DialogueEventHandler` (1,561), `HarbingerCardinalRiteEvents` (1,725),
   `ActiveCardinalRite` (1,413) and `BloodCraftingKeyPressPacket` (802, gameplay inside
   a packet). Several are also among the most text-pinned files in the test suite.
5. **Copy-paste helpers.**
   - About 60 private `smoothstep`, 72 `clamp`, 26 `lerp` and 13 `hash` definitions.
   - 20 `drawBorder` copies.
   - Four sync-helper names with seven different bodies.
   - 9 tube/tendril mesh builders.
   - 5 multiblock origin solvers.
6. **Dead code that is still wired up.** Examples:
   - 54 of 58 Vesper animation clips are unused, about 2,750 lines.
   - Five unregistered mobs total about 1,340 lines.
   - About 80 unreachable classes.
   - A dead world-bootstrap chain.
   - A per-frame render handler with an empty body.
   - 18 payloads that are registered but never sent.

**Most valuable consolidations**, all detailed in the roadmap:

1. Make the JUnit baseline green, and replace source-text tests with behaviour tests
   as each area is touched. This unblocks everything else.
2. One blood-payment path on top of `BloodFlowLedger`, with flags that keep today's
   behaviour exactly.
3. One target-validation and damage-attribution path, built on `ManipulationCombatHelper`
   and `SchoolDamage`.
4. One player-state sync registry, plus the `PendingDelivery` helper. These remove 17
   sync triplets and 6 copies of the pending-delivery state machine.
5. Network hygiene: register payloads in their real direction, set a real protocol
   version, add shared serverbound guards, and fold in the single-purpose VFX payloads.
6. Shared small helpers: one easing/maths class, one block-entity sync base, one
   ticker helper, and one screen-chrome toolkit.
7. Remove the verified dead code.

**Defects.** The audit found several dozen real defects. Some are exploits or
potential crashes. They are listed in section F and kept separate from
behaviour-preserving refactors, as the brief requires. Several of them need a
design decision, because fixing them changes gameplay.

---

## B. Baseline (verified 2026-10-21)

| Check | Result |
|---|---|
| `compileJava --offline` | **Pass** (7 s, up to date, with the `../HutosLib` composite build active). |
| `test --offline` | **Fail.** 3,908 tests, **20 failed**, 0 skipped, 51 s. See [evidence/08-baseline-test.log](evidence/08-baseline-test.log). |
| GameTests | 1,268 `@GameTest` methods across 33 suites. 999 run inside `alphaCheck`, and 269 are opt-in. **Not run in this audit.** |
| `alphaCheck` | **Not run.** It would fail at its `test` step on this HEAD. |
| Dedicated server / client start | **Not run.** |

None of the 20 failures is a logic regression:

| Group | Count | Tests |
|---|---|---|
| Generated-model marker missing: the model JSON was edited after its generator ran | 6 | `HematicArmatureModelSourceTest` ×2, `VialCentrifugeModelSourceTest` ×2, `MnemonicReliquaryModelSourceTest`, `ModelFirstTileRendererResourceTest` |
| Asset drift | 4 | `CardinalRitePlantedStaffModelTest`, `LivingSickleFeatureSourceTest`, `ClientModelTextureCoverageTest`, `VestmentOfTheFinalMoltResourceTest` |
| Data drift | 3 | `PhlegethonticBombardierResourceTest`, `BloodProfileMigrationTest`, `MorphlingLumenlaceRenameResourceTest` |
| Text encoding: mojibake in `en_us` and invalid UTF-8 in `docs/TESTING.md` | 3 | `EnglishLocalizationEncodingTest`, `GameTestHarnessSourceContractTest` ×2 |
| Source or doc text drift | 3 | `ArmorPreparationResourceTest`, `UtilityPolishResourceTest`, `LivingWeaponGraftRiteSourceTest` |
| A hard-coded legacy-test count (355 expected, 353 found) | 1 | `LegacyMainTestSuiteContractTest` |

`ClientModelTextureCoverageTest` may point to a real missing texture in game, for
`covenant_mantle` and the `silent_archon_*` items (**H**).

**Clean-clone hazards (V).**
- `/tools/` is gitignored with no tracked files, yet 6 tests read files from it.
- About half of the review-client launchers exist only in `build/*.init.gradle` or in
  untracked folders.
- CI (`.github/workflows/alpha-check.yml`) only runs on `master`/`main` and on PRs,
  not on pushes to `neo-1.21.1`.

---

## C. Competing implementation inventory

Each entry is identified by an ID (`R-1`, `N-3`, …) that the roadmap refers to. The
columns are:
- **Diff.** Migration difficulty: L, M or H.
- **Risk.** Behaviour or compatibility risk if done as described.

### C.1 Registration and initialisation ([evidence/01](evidence/01-registration.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Risk |
|---|---|---|---|---|---|
| R-1 | Event-bus subscription is written seven ways | 211 subscriber classes. Annotations use modid (159) or modid + `Dist.CLIENT` (44, spelled 5 ways), no modid (3), or `bus = MOD` (6), which NeoForge 21.1 ignores. The `Hemomancy` constructor adds method references, lambdas that wrap already-annotated classes, and `register(this)`. **V** | `@EventBusSubscriber(modid = Hemomancy.MOD_ID[, value = Dist.CLIENT])` everywhere, with no `bus=`. Constructor-level listeners only for mod lifecycle. | L | None |
| R-2 | Creative-tab routing is duplicated | `Hemomancy.acceptWipCreativeTabContents` and `isWipItem` list the same 42 items. `BlockInit.WIP_BLOCKS` repeats 17 blocks. `CreativeRoute.WIP` is computed but never used. `HemomancyCreativeTabsSourceTest:97` asserts the duplication. **V** | One ordered WIP list that drives both acceptance and exclusion, moved out of `Hemomancy` into a `CreativeTabContents` class beside `ItemInit`. | L | Creative tab order is pinned by `RegistryCreativeOrderSourceTest`. |
| R-3 | Three lookup styles for custom registries | `ManipulationInit.getByName` scans linearly and returns null (30 sites). 7 other sites use `registry.get`. `StillArtInit.getByName` scans linearly and returns `BLANK`. `ScarInit.getByName` uses the registry. **V** | Use the registry lookup, as `ScarInit` does. Keep the name strings, which may be persisted. | L | None |
| R-4 | Client registration uses deprecated or unsafe paths | All 50 BERs use vanilla `BlockEntityRenderers.register` in client setup, outside `enqueueWork`. Particle providers are registered on `Minecraft.getInstance().particleEngine`, not through the event API. `FluidType.initializeClient` is used 3 times and is deprecated for removal. **V/L** | `EntityRenderersEvent.RegisterRenderers#registerBlockEntityRenderer`; `RegisterParticleProvidersEvent` methods; `RegisterClientExtensionsEvent`. | L–M | Low |
| R-5 | Client registration is centralised in a god class, while some lives beside features | `ClientEvents` (1,495 lines) handles about 25 registration events plus runtime handlers. `ParticleInit`, `ShaderInit` and `RenderTypeInit` (client code) live in `common/init`. **V** | A `client/registry/` package with one class per registration concern. Runtime client handlers live by feature. | M | 62 test files read `ClientEvents` as text. |
| R-6 | Reload listeners are hand-rolled | There are 10 server listeners and none uses `Codec`. Only 2 use `JsonResourceReloadListener`. 6 hand-roll `listResources`, and 4 are registered by fully-qualified name in `Hemomancy:341-344`. `SuccessionProfessions` registers itself. **V** | Extend `JsonResourceReloadListener`, given a `Codec` parameter. Register every listener in one place. | M | JSON folder names must not change. |
| R-7 | Data components are written as inline builder chains | 14 hand-built chains containing 24 inline fully-qualified names. **V** | `DeferredRegister.DataComponents` with `registerComponentType`. | L | None; the IDs are unchanged. |

**Distinct for good reasons.** These should stay as they are:
- The 4 item registers and 10 block registers. Datagen keys off them, and they fix
  creative order.
- The skill and manipulation trees, which `tools/skill_tree_editor` rewrites.
- JEI's lazy listener.
- The 122 memory items that map one-to-one onto manipulations.

### C.2 Networking ([evidence/02](evidence/02-networking.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Risk |
|---|---|---|---|---|---|
| N-1 | `playBidirectional` is used as a Forge-era default | 33 bidirectional registrations, of which only `AntecedentEffectPacket` really goes both ways. 27 are client-to-server only and 5 are server-to-client only. Several carry `isClientSide` guards that exist only because of this. **V, SC** | Register each payload with its real single direction. | L | Low; the channel IDs are unchanged. |
| N-2 | Payload declaration style is mixed | 78 records vs 104 classes. 180 `StreamCodec.of` vs 2 `composite`. 9 codecs cast to `RegistryFriendlyByteBuf`. Forge-style `toBytes` methods and nested `Handler` classes remain in 10 files. **V** | A record with `TYPE`, then `STREAM_CODEC` (`composite` with `ByteBufCodecs` for up to 6 fields), then a static `handle`. `CastingAnimationPacket` is the template. Convert only files already being touched. | L each, M overall | Low if the byte layout is kept. |
| N-3 | Client handling lives in `common/network` | 8 files import `Minecraft` or `net.minecraft.client` directly. 3 handlers are lambdas inside `PacketHandler`. 66 of 105 client-bound handlers already delegate to `client/**`. **V** | `handle` forwards to a static method in `client/**`. | L | None |
| N-4 | Sending is split between a facade and direct `PacketDistributor` calls | `PacketHandler.sendToPlayer` 229, plus 28 direct `PacketDistributor.sendToPlayer` calls that skip its GameTest null-connection guard. Also 30 `sendToPlayersNear`, 18 tracking, 8 all-players. 15 per-effect send helpers. Dialogue routing lives inside `sendToPlayer`. **V** | `PacketHandler` as the only facade, with generic `sendNear` and `sendTracking`. Move dialogue routing to a `DialogueSender`. | M | None |
| N-5 | Serverbound validation is duplicated and sometimes missing | Cast eligibility is re-implemented in 7 places. The menu-guard idiom appears in 5 packets. NPC distance checks use three different thresholds. Several payloads lack validation (see section F). **V** | One `ServerboundGuards` utility: menu, `nearBlock`, `nearEntity`, `finite`, plus one shared cast-eligibility predicate that `BloodManipulation` owns. | M | None on the wire; rerun gameplay GameTests. |
| N-6 | Single-purpose VFX payloads | 4 payloads carry position plus colour and differ only by particle factory. 2 carry an entity ID plus a duration. `ManipulationVisualPacket` is already the data-driven model. **V** | `ParticleBurstPacket(kind, pos, colour)` and `EntityVisualPacket(kind, id, ticks)`. Keep the bespoke-geometry payloads. | L | Low |
| N-7 | The network version never changes | `event.registrar(Hemomancy.MOD_ID)` uses the mod ID as the protocol version. **V, SC** | A `PROTOCOL_VERSION` constant, bumped when a payload layout changes. | L | Mismatched builds start being refused at handshake, which is the intended effect. |
| N-8 | Gameplay lives in packet classes | `BloodCraftingKeyPressPacket` (802 lines) does rite activation, Blood Structure crafting and a private matcher. `PacketLedgerAction` (449) and `PlaceStructurePacket` (441) are similar. `BloodStructureCraftingHelper` lives in `network/` and is imported by `rite/` and `tile/`. **V** | A packet decodes, validates and calls one service method. The logic moves to `rite/`, `tile/` or `entity/`. | M | GameTests cover crafting. |

**Already right.** Leave these as they are:
- `SynchedEntityData` for mob state.
- The main-thread handling default. `enqueueWork` is harmless, and the 9 handlers
  without it are not bugs.
- The `ClairaudiographActionPacket` action shape, with container ID, version, enum and index.

### C.3 Player state, persistence and synchronisation ([evidence/03](evidence/03-state-capability.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Risk |
|---|---|---|---|---|---|
| S-1 | Two storage substrates for player state | 35 attachments versus about 466 `getPersistentData()` call sites in about 100 files. 81 of those files write outside `PERSISTED_NBT_TAG`, so the data is lost on death unless a clone handler copies it. Keys follow four naming families: `MOD_ID+":snake"`, `"hemomancy:snake"`, `"hemomancy.snake"`, and bare PascalCase. **V** | Durable, earned state goes in attachments (`copyOnDeath`). Raw persistent data is only for legacy reads. Session state goes in cleared static maps. Each move reads the old key once and then clears it. | H overall, L per system | Save-critical. Each migration needs a fixture test with old data. |
| S-2 | 17 hand-copied sync triplets | 15 capability `*Events` classes, plus `StationUpgradeEvents` and `AntecedentKnowledge`, each register the same login, respawn and dimension trio. Four of them also hook `EntityJoinLevel`, so they sync four times per login. The registrar already has the trio. **V** | One `PlayerStateSync` registry of `(attachment, packet factory)` pairs, driven from the existing registrar trio. | M | None; packet contents are unchanged. |
| S-3 | Polling sync instead of dirty flags | `KnownManipulationEvents` serialises and diffs a whole attachment every tick against a static map that is never cleared. `StationUpgradeEvents` resyncs every 20 ticks unconditionally. **V** | A dirty flag set on mutation, flushed once per tick. | M | None |
| S-4 | Access helper bypasses and ceremony | 42 direct `getData` calls in 22 files. 26 are `MUSCLE_MEMORY` and 2 are `DIALOGUE_KNOWLEDGE`, neither of which has a helper. 6 direct `getCapability` calls are in `HarbingerEquipmentContainer`. `Optional` wraps 18 player attachments that can never be empty, which produces about 57 `require*` and 56 `map(isActive).orElse(false)` chains. **V** | Return the concrete type for player attachments. Add the two missing accessors. Keep `Optional` only for entities, item stacks and block entities. | L | None |
| S-5 | Pending-delivery claims implement the same state machine eight times | First Draws, First Separation vials and tools, Mycophant Tendril, Vesper Memory, Communion Spine, station kits and Concentrated Blood. They use five storage choices and four trigger drivers. **V** | A `PendingDelivery` helper in which callers own their tag read and write lambdas, so no NBT key changes. One driver for tick, login and respawn. | L | None, if each caller's special rule stays a caller-side lambda: Spine's "already carried", Vesper's root migration. |
| S-6 | Competing authorities for the same fact | Archon choice: attachment plus a live root-key dual-write (`DialogueEventHandler:466`). Bloodline founding: attachment versus `BloodlineSavedData`, reconciled at every login. Chamber access: root keys versus advancements. Circus progress split four ways. The Unstained stage ladder is reimplemented in 5 places. Degree gates are inline in 36 files (57 literal comparisons). **V** | One authority per fact. `ProgressionGates` (degree, `isBloodActive`, Unstained stage) as the single gate facade over `RecipeDegreeGates`. | L for gates, M–H for authority moves | Gates: none. Authority moves are save-critical. |
| S-7 | Cooldowns live in four places | Attachments, root persistent longs (armour sets), static maps (`BloodManipulation.COOLDOWNS`, String-keyed `PASSIVE_COOLDOWNS`, `StillArt.UNIVERSAL_COOLDOWN_MAP`) and `SynapticLoadoutLockout`. **V** | A two-tier convention, decided per entry. **Persisted cooldowns** live in one attachment. **Session cooldowns** live in UUID-keyed maps registered with `TransientServerStateEvents`. | M | Moving between tiers changes whether a cooldown survives relog. Keep each entry's tier. |
| S-8 | Session-state cleanup is scattered | 459 static mutable collections in `common/`, 63 of them UUID-keyed. 22 files register their own `ServerStopped` handlers and 26 their own logout cleanup. About 11 maps are never cleared, including `ScaleGripEvents.PENDING_RETURNS`, which holds item stacks. **V** | Every session map registers once with `TransientServerStateEvents`, which also fans out logout removal. | L | None |
| S-9 | Migrations never retire | 11 login migrations. `LiberKnowledge` migrates 4 times per login, `FungalScarMigrationEvents` scans the inventory every tick, and a chunk mixin runs on every chunk load. Only `StationUpgradeProgress` records a migrated flag. **V** | A per-player `dataVersion` attachment, so each migration runs once. A removal date is recorded with each migration. | M | Save-critical. Keep the read paths for at least one release. |

**Fine as is.**
- `SavedData` is consistent: 20 subclasses use `Factory` plus `get`. The only gap is that
  overworld-only data accepts any `ServerLevel`.
- The six knowledge stores (Liber, Dialogue, Ichorian, Antecedent, Clinical, Whispers)
  cover different domains. Unify their access, not their storage.
- NBT key casing inside attachments is mixed. **Do not rename it**: that would be a save
  format change. New attachments use PascalCase.

### C.4 Cross-family gameplay mechanics ([evidence/09](evidence/09-shared-mechanics.md))

These families implement the same mechanics: manipulations, armour sets, morphlings,
scars, skills, mob effects, living weapons, rites, entity abilities, Unstained
mechanics, hazards, Still Arts and summons.

| ID | Mechanic | Evidence | Recommended canonical | Diff | Risk |
|---|---|---|---|---|---|
| G-1 | Damage modification order | 48 damage listeners and 35 modification sites in 22 files. Blood-drain-on-hit (`BloodVolumeEvents:275`) and vein strain (`VascularSystemEvents:37`) read the damage amount at NORMAL priority. Five other NORMAL handlers change that amount, two of them to zero. Silent Archon's death refusal tests lethality at NORMAL, before Endless Hour and Phoenix Debt at LOWEST. **V** | Explicit stages: modifiers early, consumers of the final amount late, all death-saves in one stage through `LastRiteHelper`. | M | **Behaviour change.** Blood lost per hit will differ. Needs approval. |
| G-2 | Target validation | 10 separate "valid target" implementations. About 25 sites filter on `Monster.class` alone, which also catches the caster's own `GroundPuppetEntity` (a `Zombie`). Two boss definitions disagree. **V** | `ManipulationCombatHelper.canHarm` / `allied` as the one predicate, with explicit opt-outs for deliberate ally or self effects. | M | Who gets hit can change. Migrate class by class with tests. |
| G-3 | Blood expenditure | About 60 raw drains outside the ledger in other families (armour abilities, primal morphlings, cerebral scar upkeep, 4 living-weapon copies, puppet threads), plus about 98 `drain()` calls overall (L-2). `drain()` empties the reserve on overstrain. **V** | One `BloodAccounting.pay(player, amount, Source{id, category, countsAsSpend, reserveProtected})` on top of `BloodFlowLedger`. Flags reproduce today's behaviour exactly. | M | Low with flags. High if the flags are "fixed" globally. |
| G-4 | Heal modifiers | 4 heal-reduction listeners in 3 files, one inside `HemorathEntity`. **V** | One heal-modifier table. | L | Low |
| G-5 | Attribute-modifier sync | The same "update modifier to amount" code is hand-written 9 times. Some modifiers are removed and re-added every tick or second (see P-1). **V** | One `AttributeModifiers.sync(entity, attr, id, amount, op)` helper that does nothing when the value is unchanged. | L | None |
| G-6 | Full-set detection | Three methods, and `hasFullSilentArchonSet` exists twice. **V** | One `ArmorSets.isFullSet(player, set)`. | L | None |
| G-7 | Flight and mobility | Four code paths write `abilities.mayfly` without coordination. **V** | A small flight arbiter that knows every source granting flight. | M | Flight edge cases. |

**Keep separate.** These differ for good reasons:
- The damage-absorption pools.
- Puppet targeting AI.
- Domain-specific target rules, such as rite spaces.
- White Humor as its own resource.
- One tick handler per feature family.

The existing shared pieces to build on are `SchoolDamage` with `MixinSchoolDamage`,
`SchoolCombatEvents.prepare`, `ManipulationCombatHelper`, `SchoolStates.canApply`,
`BloodFlowLedger.applyDrain`, `LastRiteHelper`, `TransientServerStateEvents` and
`RiteZoneSavedData`.

### C.5 Gameplay subsystems ([evidence/04](evidence/04-gameplay-logic.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Risk |
|---|---|---|---|---|---|
| L-1 | Manipulation behaviour is keyed by name strings in many places | Entity casts go through a `switch (getName())` over 34 names in `EntityManipulationEffects`. Eight places must agree when an ID changes. 48 inline drudge lambdas in `ManipulationInit`, 29 repeating one "nearest Monster" lookup. **V** | Registration-time hooks (`setEntityCast`, as with `setDrudgeAction`). A `DrudgeActions` helper class. Cast-sound profile kept in registration metadata. | M | Low |
| L-2 | Blood payments outside casting | Covered under G-3. Player volume sync also has two forms: 49 bare `BloodVolumeServerPacket` sends versus 73 `syncVolume` calls, and only the second refreshes diagnostics. **V** | `BloodAccounting.pay` ends in `syncVolume`. | M | Low |
| L-3 | Projected-blood endpoints | `BloodProjectionItem:144-217` is an 8-step `instanceof` chain. `IBloodReservoir.receiveBlood`, which honours `canReceiveBlood`, has one production caller. **V** | A `ProjectedBloodSink` interface on block entities, with a default built on `receiveBlood`. | M | Also closes defect F-12. |
| L-4 | Five multiblock origin solvers | `BloodCraftingKeyPressPacket:692-741` (exact solver), `BloodStructureCraftingHelper:105-126` (cube scan × 24 orientations), `HarbingerCardinalRiteEvents:859-875` (a third cube scan), `SuccessionRites:147-157`, `CardinalRiteStationMatcher`. `getMatchBounds` and `checkPathAlignment` are duplicated. **V** | A `PatternOrigins` utility for origin, offset, `findContaining` and bounds. Keep `CardinalRiteStationMatcher` and `WorkingCraftSubjectMatcher` as the semantic layers. | L–M | **The solvers can pick different winners on overlap.** Lock current results with GameTests first. |
| L-5 | Rite offering consumption | Four paths: `RiteOfferingEscrow` (Working, station), ordinary procession, Puppeteer trial, Succession. **V** | Keep escrow and procession distinct, as AGENTS.md requires. Move Puppeteer and Succession onto one `RiteOfferings.consumeCaptured` helper. | L | Low |
| L-6 | Rite kind dispatch | `HarbingerCardinalRiteEvents.onLevelTick` is a 280-line kind switch (124 commits). Kind checks are scattered: `isWorkingCraft()` 31 hits, `isPuppeteerTrial()` 15. `ActiveCardinalRite` has 50 fields and per-kind tag bags. Activation lives in a packet. **V** | A `RiteKind` strategy with `matches`, `tick`, `validate`, `complete` and `terminate`. The kind controllers already expose these statically. Activation moves to `rite/harbinger/CardinalRiteActivation`. | M–H | Tick ordering. Needs strong GameTest coverage first. |
| L-7 | Block entities have no base | 63 BEs. `getUpdatePacket` is identical in all 38 that override it. Four helper names (`sendUpdates`, `markDirtyAndSync`, `sync`, `setChangedAndSync`) carry 7 different bodies. 12 HutosLib `VanillaPacketDispatcher` calls are stacked on top of `sendBlockUpdated`. Nine BEs persist blood a second time on top of the attachment, four reading it back as float. **V** | A thin `HemoBlockEntity` (or a static `BlockEntitySync.markAndSync`). Use the attachment only for blood, with a one-release read fallback. | L–M | Low |
| L-8 | Recipe serializers | 8 use a hand-written JSON bridge, 4 of them without needing it. All 16 stream codecs are manual. 7 recipes extend `CustomRecipe` with a `matches` that returns a constant. **V** | `RecordCodecBuilder` where there are no patterns. `Recipe<RecipeInput>` instead of `CustomRecipe`. | L–M | Recipe JSON must parse identically. |
| L-9 | Scriptorium is the one station whose logic lives in a menu | Enchant and cure run in `EnzymaticScriptoriumMenu.clickMenuButton`. **V** | Move them into the block entity, as every other station does. | L | Low |

**Already consolidated.**
- **Cast gate.** `BloodManipulation.tryPerformAction` is the only gate. No manipulation
  pays its own cost.
- **Station upgrades.** All seven stations use the shared catalog, progress and rites.
  The tier adapters are intentional.
- **Brazier rites.** These are a clean shared family.

### C.6 Rendering and VFX ([evidence/05](evidence/05-rendering.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Visual risk |
|---|---|---|---|---|---|
| V-1 | Screen chrome is copied | 20 private `drawBorder` copies match `ScreenDrawUtils.drawBorder` except for colours. 16 screens carry their own `drawVeinTendril` and `renderVeinBackground`, while 6 use the shared batched `VeinBackgroundRenderer`. About 2k lines. **V** | `ScreenDrawUtils` and `VeinBackgroundRenderer`, with a palette parameter. | L | Low; palettes are kept. |
| V-2 | Six render-type holders with three caching strategies | Five per-school holders repeat a "static time + `begin()` + uniform" pattern. `HemoRenderTypes` builds some types per call, caches some in an LRU keyed on `gameTime` that misses every frame, and uses a mutable per-seed holder for others. Only the last is allocation-free. 37 factories build a new `RenderType` per call (see P-9). **V** | One caching pattern: the per-seed holder. One school render-type helper. | M | Low; batching improves. |
| V-3 | The material quad encoder exists four times | `LuxUmbraGeometry`, `RiteSurface`, `FerricDuctilisGeometry` and `ThermalGeometry` each write the UV encoding that the school shaders read. Ferric uses `floorMod(seed,128)`; the others use 127. **V** | One shared encoder. | M | **Ferric's constant must be checked visually** before choosing. |
| V-4 | Tube and tendril mesh builders | At least 9 exist: HutosLib `TendrilGeometry`, `TendrilTube`, `SeamTube`, `ArborMeshEmitter`, `IchorianSigilOrganicGeometry`, `FloorMesh`, `NaeglerophaeonModel`, `VisceralMesh`, `ManipulationVisualRenderer`. **V** | Share frame and ring generation only. Keep each family's profile, colour and motion. | H | **High.** Verify in the review clients. Do last. |
| V-5 | Shader noise is copied | 45 of 69 fragment shaders define their own `hash`, 41 `noise` and 32 `fbm`. **V** | A shared include for byte-identical copies only. Seeded variants stay. | M | Low for exact copies. |
| V-6 | Particle data triplets | Each particle has Data, Type and Factory. The colour-only data classes differ only by registered type, and HutosLib `ColorParticleData` already takes the type as a parameter. **V** | A parameterised colour particle data class. | M | Low |
| V-7 | HUD and world-render dispatch | 15 registered GUI layers plus about 9 raw `RenderGuiEvent` handlers. About 33 world renderers run through an ordered dispatcher (`ClientEvents:737`), and about 10 more subscribe themselves, unordered relative to it. **V** | All HUD drawing through `RegisterGuiLayersEvent`. All world effects through the ordered dispatcher. | M | Layer order, so check each visually. |
| V-8 | Client code in `common/` | `RenderTypeInit` and `ShaderInit` live in `common/init`. `RenderBloodLaser` lives in a capability package, and `FungalSkyBoxRenderer` in worldgen. 10 packets call `Minecraft.getInstance()`. 288 common files import some client class. 72 of those only import HutosLib `ParticleColor`, and 57 items build their renderers lazily; both are probably safe (**H**, prove with a `gameTestServer` run). **V** | Client code lives in `client/**`. Common code may only refer to client classes from inside a lambda passed to a client extension. | L–M | None |
| V-9 | Tiny entity renderers | 60 renderers are 30 lines or fewer. **V** | Optional: a shared simple-renderer factory. | L | None. **Optional.** |

**Already right.**
- All lightning goes through HutosLib `BoltRenderer`.
- `OutpouringBrushes` is the public bridge the visuals doc asks for.
- `FloorMesh` and the four rite boundary renderers are cohesive, deliberately distinct
  art. **Leave them alone.**

### C.7 Animation ([evidence/10](evidence/10-animation.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Timing risk |
|---|---|---|---|---|---|
| A-1 | Six boss animation sync conventions | Annetta replays a synced action tick on a client clock with 2-tick drift tolerance. The Priestess, Latent Annetta, Mycophant and the four Saints read the raw synced tick plus partial tick, which `VesperAnimClock`'s own comment says stalls and skips. Several bosses set a synced counter **every tick**. **V** | A shared replay clock promoted from `VesperAnimClock`, which has a unit test. Simple mobs keep synced state that starts vanilla `AnimationState`s, as Eruptus does. | M | Displayed timing may shift by up to 2 ticks. Moving from per-tick counters to a start timestamp is a separate, higher-risk job. |
| A-2 | Duplicated easing helpers | About 60 private `smoothstep` copies, 19 `lerp`, 13 `clamp01`, five crossfade implementations. No shared easing class exists in Hemomancy or HutosLib. **V** | One `HemoEasing` class seeded from `common/rite/CardinalRiteMath` (24 callers). **It must use the cubic form.** `Mth.smoothstep` in 1.21.1 is quintic, and swapping it in would change every curve. `lerp` argument order differs between copies (`(t,a,b)` versus `(a,b,t)`). | L | **High if done carelessly.** Each replacement must be byte-equivalent. |
| A-3 | Six bespoke player animations | Vial injection, microscope, torch breath, staff planting, staff morph and flail each have their own packet and client state, and repeat the same expiry logic. The generic `PlayerAnimationKind` channel has one value. **V** | One client player-animation registry that keeps the exact priority order of `PlayerAnimationClientState`. | M | Priority order. |
| A-4 | Partial-tick and clock sources | Partial ticks come from four sources. Item renderers animate on wall-clock time, block entities on game time. **L** | One `AnimationTime` helper, with game time plus partial tick by default. | M | Some animations would start to freeze under `/tick freeze`, so decide case by case. |
| A-5 | Procedural chains | Three unshared styles: per-segment sine waves, a curvature-integrated tube (Priestess wings) and endpoint-aligned segments. **V** | **Keep separate.** They are different art. | — | — |

**Casting system is the model.** It uses one payload, server game-time stamps, pure
playback maths and a single `MixinHumanoidModel` hook. It is the model for other
player animations.

### C.8 Content class design ([evidence/06](evidence/06-content-oop.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Risk |
|---|---|---|---|---|---|
| O-1 | No boss or encounter framework | Boss-bar wiring is copied into 11 bosses. The best existing example is the Mycophant: entity, a controller that owns the move timeline, and pure tested rules classes. `VesperMoveTimeline` is already shared with the Mycophant but is named after Vesper. **V** | A `BossBarBinding` composition helper. Rename `VesperMoveTimeline` to `BossMoveTimeline`. A `SaintTrialEntity` base for the three Saints. **Do not force** Vesper, Annetta and Mycophant under one base. | M | Low |
| O-2 | Copy-paste mob pairs and projectiles | Chitinite and Fervent Chitinite share 98% of their code; Chthonian and Chthonian Queen share 97%. Three tracking orbs share 84–94%. Five combat-weapon arrows repeat weapon storage. **V** | A shared base, or one class configured at registration. Registry IDs are unchanged. | L–M | Low |
| O-3 | Repeated content boilerplate | Protected-NPC damage gate in 14 NPCs. Synced owner UUID in 5 entities. Private `createTickerHelper` in 8 blocks, plus 8 unchecked ticker casts. Player-inventory slot loops in 19 menus, and 5 slotless view menus that are near-identical. `WarpChairShapeRules` duplicates `ModelShapes.rotate`. 25 effects carry redundant overrides. **V** | Composition helpers: `HemoTickers`, a `SyncedOwner` record modelled on `BoundSummonSync`, NPC default methods, and a `HemoContainerMenu` with `addPlayerInventory`. | L | None |
| O-4 | `DialogueEventHandler` is a god class | 1,561 lines. One method of about 440 lines with a 36-case switch, plus 50 handlers covering 10+ NPCs. **V** | A per-NPC dialogue-event routing table, each NPC's handlers in their own class. | M | 9+ text-pinning tests. Many progression gates live here. |
| O-5 | Two item data styles | The untyped `CUSTOM_DATA` component appears in 80 files (342 refs). The 14 typed `DataComponentInit` components appear in 41. **V** | Typed components for new data. Migrate an item only when it is already being touched. | M per item | **Save-critical.** Item data in existing worlds must be read through a migration. |
| O-6 | Client hooks inside item classes | 58 items implement a client interface to supply renderers. 15 `BlockItem` subclasses exist only for that. 12 armour items repeat the same model-selection chain. **V** | Register renderers in `RegisterClientExtensionsEvent`, keyed by item. | M | Low |
| O-7 | Hard-coded English | 4 effect names, Saint broadcasts and about 325 tooltip lines are literal strings. **V** | `Component.translatable` with en_us keys. | L each | None |

**Stay distinct.** These classes are separate for good reasons:
- Drudge vs Thrall, and Echo vs Intercession.
- Circus performers and simple monsters.
- Flower blocks.
- The capability `I*`/impl pairs.
- Puppet summons, which are the cleanest composition example in the repo.

### C.9 Naming, resources and data ([evidence/07](evidence/07-naming-deadcode.md), [evidence/08](evidence/08-tests-build-worldgen.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Risk |
|---|---|---|---|---|---|
| D-1 | Suffix vocabulary is overloaded | `*Rules` 323, `*Events` 112, `*Helper` 42, `*Handler` 29, `*Manager` 22, `*Service` 4. `*Service` and `*Manager` mean the same thing. `*Handler` mixes event subscribers with item handlers. `Packet*` vs `*Packet` is 71 vs 111. **V** | A defined vocabulary for **new** code (standards §9). **No mass rename.** | — | None |
| D-2 | ResourceLocation construction | `Hemomancy.rloc` 1,544 uses, plus 85 hand-built Hemomancy locations in 60 files. **V** | `Hemomancy.rloc`. | L | None; identical strings. |
| D-3 | Raw degree numbers | About 94 comparisons of the player's degree with a literal, plus 143 on local variables, though `EnumInitiatoryDegree` exists. **V** | The `ProgressionGates` facade (S-6). | L | None |
| D-4 | Tag files in pre-1.21 plural folders | Five tag files are in `tags/items` or `tags/fluids`, which 1.21 does not read. Examples are `data/minecraft/tags/items/{walls,small_flowers}.json` and `data/hutoslib/tags/items/knappers.json`. **V** | Singular folders. The tag IDs stay the same. | L | **Behaviour change**: the tags start working. Needs an in-game check. |
| D-5 | Datagen is mostly dead | 4 providers run. 8 classes in `gen/` are never registered. 46 generated files are shadowed by `src/main`, including all of `en_us.json`, of which 358 generated values are junk. About 57 generated files ship. **V** | Decide per provider whether to revive it or delete it (roadmap phase 9). | M | Low |
| D-6 | Five ways to author dialogue | 17 `*DialogueTrees.java` (5,143 lines), 17 procedural `*Dialogue.java`, 353 inquiry JSON files, lang prose, and untracked editor and Python round-trip tools. **V** | **Keep for now.** Document which kind of dialogue goes where. Consolidating it is a content-pipeline project, not an architecture fix. | — | — |
| D-7 | Jar packaging | The `.cache` datagen hashes ship because `build.gradle:26`'s exclude never matches. 203 `/old/` entries ship, and so does a 2.6 MB Blockbench reference PNG. The jar is 95.6 MB. **V** | Fix the exclude patterns. Move authoring archives out of `src/main/resources`. | L | None |
| D-8 | Docs drift | Five spot checks found stale claims. AGENTS.md says HutosLib owns dialogue (false) and that `HemoCapabilityAccess` is the only access (23 files bypass it). The reference says `HemoCommonConfig` is empty (it has 7 options) and that GeckoLib comes from a local jar (it now comes from Maven). The datagen providers are unregistered, not "commented out". **V** | Correct them in the same change as the related code. | L | None |

### C.10 Tests, build and worldgen ([evidence/08](evidence/08-tests-build-worldgen.md))

| ID | Problem | Evidence | Recommended canonical | Diff | Risk |
|---|---|---|---|---|---|
| T-1 | Source-text tests | 383 files (definition: assertions over `.java` text) contain 5,626 `assertContains`, 591 `assertNotContains`, 2,213 raw `.contains` and 55 regex checks. They reference about 1,200 `.java` paths. The most-pinned files are `ItemInit` (82), `ClientEvents` (62) and `PacketHandler` (28). **V** | Triage before refactoring each area. Keep tests that pin a real contract and rewrite them against behaviour or data. Delete tests that pin incidental text. | M–H | This is what lets the codebase change. |
| T-2 | No shared test helpers | 228 private `assertContains` copies, 225 private `read(` copies, 46 private `player(` GameTest factories. **V** | A `src/test/.../support` package: `SourceAssertions`, `ResourceReader`, `TestPlayers`. | L | None |
| T-3 | Legacy `main()` tests | 345 files have no `@Test` and run only through the reflective `LegacyMainTestAdapterTest`. **V** | Convert to JUnit 5 as they are touched. Remove the hard-coded count test. | L each | None |
| T-4 | Two GameTest registration paths | `build.gradle:403` puts the gameTest source set into the mod, so `@GameTestHolder` classes are scanned. `DevTestHooks` also registers 13 classes by hand, 11 of which already have holders. Its Javadoc contradicts the build. **V**; double registration is **H** (check the GameTest server log for duplicate batch names). | One path: annotation scanning. | L | None |
| T-5 | `/hemo test` scenario catalog | A third mini-framework that mostly duplicates GameTests. Journey replay is shared with GameTests and valuable. **L** | Keep journey replay. Retire the scenario catalog once GameTests cover it. | L | None |
| T-6 | Build sprawl | `build.gradle` is 599 lines with 25 copy-pasted run configs. Code reads about 60 `hemomancy.*Review*` system properties that no tracked build file sets. 7 of the 19 `libs/` jars are unreferenced (about 487 MB in total). 6 `implementation` dependencies have no imports. **V** | One review-client run factory driven by a table. Version-control `tools/` and the launchers. Prune unreferenced jars (keep dormant compat jars). | M | None for the mod. |
| W-1 | Six structure mechanisms | 13 jigsaw clones that copy the vanilla codec. A Dynasty castle composed in code from NBT. Vagrant Mind as a block-by-block `StructurePiece`. Village pool injection. An ancient-city mixin. Runtime `setBlock` arenas. **V** | One `HemoJigsawStructure` base for the 13 clones. **Keep** NBT for authored buildings and code for procedural geometry. | S–M | StructureType IDs and codec field names must stay byte-identical. |
| W-2 | Duplicated worldgen helpers | Identical replaceable-block setters: `SporeNexusTower:431` matches `TermiteMound:335`, and `DeepOceanVent:189` matches `ErythrocoralReef:160`. `FeatureLogic.IS_REPLACEABLE_AIR` exists with zero references. At least 7 hash/noise copies. **V** | Merge the identical setters. **Do not merge the noise copies**: their constants differ, and merging would change terrain on existing seeds. | L | None for the setters. |
| W-3 | Encounter managers live in `worldgen` | `ChamberOfWillManager` (785 lines), the Mycophant and Vesper managers, `ChamberVisitService`, `FungalGardenTravelHelper`, and two client classes. **V** | Move to `common/encounter/` (and `client/**`). Keep every saved key exactly, including `chamber_of_will_data`, the `hemomancy:chamber_visit_*`, `mycophant_*` and `vesper_*` player keys, and `hemomancy.circus_discovered` with its dot. | L | Save-critical keys, but these are package moves only. |

### C.11 Performance and lifecycle ([evidence/11](evidence/11-performance-lifecycle.md))

These are demonstrable from code paths. None was profiled.

| ID | Problem | Evidence | Fix | Diff |
|---|---|---|---|---|
| P-1 | Elytra attribute churn | `HarbingerEquipmentEntityEventHandler.playerTick:182-195` removes and re-adds a network-synced modifier every tick, so a wearer sends about 10 attribute packets per second to themselves and all trackers. **V** | The G-5 helper, which skips the update when the value is unchanged. | L |
| P-2 | Combat state is allocated on every entity | `SchoolCombatEvents.tick:266` calls `getData(SCHOOL_COMBAT)`, which creates the attachment on first touch. Every mob therefore allocates it and every saved mob writes a `school_combat` tag. **V** | `getExistingData` / `hasData` on the hot path. | L |
| P-3 | Unthrottled block scan | The Stillwater Condenser reads 1,736 blocks per tick per condenser. **V** | Throttle it, or cache it and invalidate on neighbour change. | L |
| P-4 | Undiffed periodic broadcasts | Founding Fane sends to every player in every level every 40 ticks, even with no fanes. Chamber `syncTo` sends 2 packets every 40 ticks. Rite sync goes to every player in the level every 10 ticks. **V** | Send only on change, as `RiteZoneSync` and `RiteSpaces` already do. | L–M |
| P-5 | Packet streams nobody needs | `BlackheartedEvents` (was `BodyIdiomEvents`) broadcasts a visual for every player every second, even with blood magic inactive. Sheolic Bastion and Black Veil re-apply effects every tick. `BloodBindingEffect` sends 4 particle packets per tick per bound mob. **V** | Gate on state; re-apply only near expiry. | L |
| P-6 | Per-tick item copying | `VialRackItem.inventoryTick` does 3 tag copies and 8 `ItemStack` decodes per rack per tick. `RecycledEnzymeItem` copies its tag every tick. **V** | Read the components without copying. | L |
| P-7 | Linear recipe lookup | `CardinalRiteRecipe.getRiteByLocation` streams all 91 recipes. It has 32 call sites, including one per active rite per tick. **V** | `RecipeManager.byKey`. | L |
| P-8 | Per-tick serialise-and-diff | See S-3. | Dirty flags. | M |
| P-9 | `RenderType` built per call | 37 factories, per object per frame. This also breaks batching. **H** (measure with Spark or JFR). | V-2. | M |
| P-10 | Client caches keyed by entity id never clear | Stale state can attach to a reused id in the next world, and some caches keep the old client level alive until the next world loads. **L** | Clear on `ClientPlayerNetworkEvent.LoggingOut`. | L |
| P-11 | Dead per-frame handler | `RenderBloodLaserEvent` loops over every player at every render-level stage, and its render body is empty. **V** | Delete it. | L |

**Already clean.**
- All 9 server reload listeners prepare off the main thread.
- Nearly all 54 damage handlers exit early on cheap checks.
- `RiteZoneSync` and `RiteSpaces` send only on change.

---

## D. Architectural hotspots

These files rank highest on size, churn, coupling and test-pinning together. Change
them only with a plan and characterisation tests.

| File | Lines | Why it is hot | Direction |
|---|---|---|---|
| `client/event/ClientEvents.java` | 1,495 | About 25 registration concerns and 31 runtime handlers. 62 tests read its text. | Split into `client/registry/*` and feature handlers (R-5). |
| `common/command/HemoCommand.java` | 1,982 | One literal tree of 18 branches plus 60 handlers. 9 text-pinning tests. | One class per subcommand under the same root. |
| `common/rite/harbinger/HarbingerCardinalRiteEvents.java` | 1,725 | A 280-line tick that dispatches on kind. 124 commits. | `RiteKind` strategy (L-6). |
| `common/entity/npc/dialogue/DialogueEventHandler.java` | 1,561 | A 440-line switch plus 50 handlers. | Per-NPC routing (O-4). |
| `common/rite/ActiveCardinalRite.java` | 1,413 | 50 fields, about 177 public methods, per-kind tag bags. | Per-kind state objects behind `RiteKind`. |
| `common/network/capa/harbinger/BloodCraftingKeyPressPacket.java` | 802 | Gameplay inside a packet. Private matcher. Sends an unused ItemStack. | Move logic out (N-8, L-4). |
| `client/render/HemoRenderTypes.java` | 1,625 | Three caching strategies. Text-pinned. | V-2. |
| `common/init/ManipulationInit.java` | 1,296 | 48 inline AI lambdas. | L-1. |
| `common/init/ItemInit.java` | 1,260 | 82 tests read its text. | Leave it as a registry. Triage the tests (T-1). |
| `client/screen/item/HarbingerAssignmentLedgerScreen.java` | 1,903 | Largest screen. | V-1 chrome extraction. |
| `client/render/world/QliphothBloomRenderer.java` | 1,847 | Large, but cohesive art. | Leave it. |
| `client/model/entity/boss/endgame/VesperTheEveningStarAnimations.java` | 3,003 | 54 of 58 clips unused. | Delete the dead clips (Quick Win). |
| `common/worldgen/ChamberOfWillManager.java` | 785 | Save-critical keys, misplaced package. | W-3. |
| `common/capability/HemoCapabilityAccess.java` | 435 | Hub with 1,158 callers. | S-4. Changes here touch everything, so keep them mechanical. |

---

## E. Dead code and legacy infrastructure

Removal candidates are classified as follows:
- **SAFE.** Demonstrably unused and removable with minimal risk.
- **REQUIRES VERIFICATION.** Appears obsolete, but may have runtime, data or
  integration dependencies.
- **MUST PRESERVE.** Used by active behaviour, persistence, external integration or
  intended future work.

"Unreferenced" means a whole-repository `git grep` for the class name across `src/**`,
resources and `build.gradle` found nothing outside the class itself. Each evidence
report lists the exact searches.

### SAFE

| Candidate | Evidence | Note |
|---|---|---|
| 54 unused clips in `VesperTheEveningStarAnimations` (~2,750 lines) | Only IDLE, DEFEAT, HIT and REMOVE_HOOD are referenced. `VesperCombatImplementationSourceTest:137-146` checks HIT only. **V** | Keep the 4 used clips. |
| `RenderBloodLaser` + `RenderBloodLaserEvent` | `renderLaser` has an empty body and the draw helpers have no callers. The handler is still subscribed. **V** | Removes a per-frame loop. |
| `TerraBiomeInit`, `WhiteHumorVolumeEvents`, `RecipeSacrificialBloodCrafting`, `TendencyWeaponCombatEvents` | Empty classes. **V** | |
| `HitGlowParticle`, `RenderChitiniteLayer`, `BloodArmorLayer`, `CellHandBakedModel`, `IRadialMenuItem`, `RadialTextLines`, `compat/jei/ScreenArea`, `GuideBookTooltipHelper`, `EnumRejectionEvent` | Unreferenced. **V** | Removed in consolidation step 3. |
| `Hemomancy.instance`, `Hemomancy.forcesLoaded` | Written, never read. **V** | The `forcesofreality` check is only a write. |
| `AttributeInit.canFly`, `ScarInit.getAllEntries`, `VillagerInit.HEMOPOTHECARY_PROCESSOR_LIST_KEY`, `BaseFeatureInit.setup()`, `EffectInit.setupPotionRecipes`, `EntityInit.commonSetup` (empty subscribed handlers) | No callers, or empty bodies. **V** | |
| `BloodManipulation.performDrudgeAction`, `IBloodVolume.drainFromSource` (inverted semantics, 0 external callers), `BloodCraftingKeyPressPacket.getMatchBounds` | 0 callers. **V** | |
| `HemoBookReloadListener` | Duplicates HutosLib's listener and is never registered. **V** | |
| 10 unused `RenderTypeInit` entries, `RadiantPortalRendertype.END_PORTAL`, `textWithRayShader`, shaders `DISTORTED_TEXTURE` and `SCAR_GLOW` (compiled on every reload, never used) | Unreferenced. **V** | Removing them shortens shader reload. |
| `ITEM_BLOOD_VOLUME` **attachment** (`HemoAttachmentTypes:176`) | Only the `ItemCapability` key is referenced. `ItemStackBloodVolume` stores data in `CUSTOM_DATA`. **V** | Keep the capability key. |
| `mixin/core/MixinScreenRenderState.java` (empty, tracked) | **V** | |
| Four `*.java~` editor backups (`CardinalFocusBlock`, `LivingSickleItem`, `StillwaterCondenserMenu`, `PlaceStructurePacket`) and the root `bash.exe.stackdump` | **SC**: the backups are untracked (gitignored), so this is local cleanup only. | One evidence report calls the backups tracked; `git ls-files` shows they are not. |
| Old fungus features, `TestRegion1/2` | Unreferenced. **V** | Removed in consolidation step 3. |

### REQUIRES VERIFICATION

| Candidate | Why it is uncertain | What would settle it |
|---|---|---|
| Five unregistered mobs with entity, model and renderer: Abyssal Siphon, Cruor Fiend, Frozen Clot, Synapse Hound, Void Drinker (~1,340 lines) | Their sounds are still registered, so they may be planned content. | Owner decision: shelve them to an archive branch, or keep them. |
| About 14 unregistered item classes, such as `LivingPistolItem` and `DSDItem`, plus 4 morphling items (756 lines) | `MorphlingMigrationRules` redirects the old morphling IDs; the classes themselves are unused. | Confirm that no save or serializer references the class names. |
| `LivingGraspItem` (unregistered), which leaves `TrackingBloodOrbEntity` with no spawner. `TrackingPestsEntity` and `TrackingSerpentEntity` are never constructed. | **Their entity types are registered and must stay registered** for saved worlds. Only the dead spawners can go. | — |
| 9 rule classes called only from tests (e.g. `CardinalRiteBrazierSignatureRules`, `VialCentrifugeStartupRules`, `EndgameBossCombatRules`, `MnemonicBlueprintRules`, `CardinalRiteStructureConsumptionRules`) | Either a call site was lost or the tests give false coverage. | Trace each against the feature it names. Wire it up or delete it with its test. |
| 18 registered payloads that are never sent (e.g. `UseQuickManipKeyPacket`, `UseContManipKeyPacket`, `PacketOpenSporeInv`, `AirBloodDrawPacket` with an empty handler) | Unregistering changes the payload set, which is harmless only when every client and server is upgraded together. | Remove them together with the N-7 protocol version bump. |
| `TeleportToVeinPacket` (unregistered, still sent from an unreachable `RadialChooseVeinScreen`) | The screen is never opened. | Delete both, with `TeleportToVeinPacketSourceTest`. |
| `IBloodRoutingTarget` (0 implementers) | `BloodRoutingHelper` checks for it, and `HEMOMANCY_REFERENCE.md` documents it as the routing hook for new blood consumers. Moved here from SAFE in step 3. | Owner decision: keep the hook, or remove it with its three `BloodRoutingHelper` branches and the reference text. |
| `FeatureLogic` | Unreferenced, but the W-2 plan merges the duplicate replaceable-block setters into it. Moved here from SAFE in step 3. | Resolve together with W-2 (step 13). |
| Textures left without a code reference by step 3: `textures/misc/laser.png`, `laser2.png`, `laser_glow.png`, `vine.png`, `textures/entity/chitinite/model_chitinite_warn.png` | Only the deleted render types and `RenderChitiniteLayer` used them. They may be wanted as art. | Owner decision: delete, or archive with the other `old/` textures. |
| `MobManipCaster` | `WillCombatRedesignSourceTest:42` asserts that it *exists*. | Update that test. |
| The worldgen bootstrap chain (`BiomeInit.bootstrapBiomes` with ~380 lines of builders, `PelagicBiomes.bootstrap`, `Configured/PlacedFeatureInit.bootstrap`, `HemoFeatureUtils`, `HemoPlacementUtils`), the empty `BIOME_REGISTER`, the unregistered `BIOME_MODIFIER_SERIALIZERS` | The JSON is authoritative and two tests already call it dead. `EscharianOvergrowthResourceTest:81` pins a line inside it. **Keep the `ResourceKey` constants.** | Update that test. |
| 8 unregistered datagen provider classes | They may be intended for a future datagen revival. | Owner decision (D-5). |
| `ResonantForgeTier.degree()` | No caller found. Its D3 disagrees with the catalog's D5/D7. | Grep once more after the station work. |
| 39 animation-state fields that are started or declared but never read | Some may be planned. | Per class. |
| Several NBT structure templates with no code or pool reference (`harbinger_outpost` root, `broken_unstained_church`, `qliphoth_bloom`, `qliphoth_fane`, `blood_tower_*`, `lowtide_ruin`) | Could be used by a runtime `/place` or an authoring flow. | Grep the commands and tools. Owner decision. |
| 7 unreferenced jars in `libs/` (Demonology, thaumaturge, two DistantHorizons, thebrokenscript, a GeckoLib jar, a HutosLib jar plus sources) | They may be wanted for local play-testing. | Owner decision. They are not shipped. |
| The 6 `implementation` dependencies with no imports (Moonlight, TOP, dummmmmmy, BG2, WorldEdit, Astral) | They may be there only for dev runtime. | Move them to `localRuntime` if they are only for the dev environment. |
| The `/hemo test` scenario catalog | Overlaps GameTests. | Once GameTests cover each scenario. |

### MUST PRESERVE

- **Every save-migration path.** This includes:
  - `StationUpgradeMigration` and the `ADVANCED_BREWING` / `RESONANT_FORGE` attachments.
  - `ScriptoriumLegacyMigration` and `MixinScriptoriumChunkMigration`.
  - `LegacyBloodStructureIds` and `MorphlingMigrationRules`.
  - The fungal-scar migration and the `InitiatoryDegree` pome-by-origin migration.
  - `VesperOrdealManager.memoryClaims`, which AGENTS.md requires.
  - `KnownManipulations` `avatarActive` and `MuscleMemoryState.legacyPrimedUntil`.

  These may only be retired through the `dataVersion` mechanism (S-9), with a recorded
  sunset date.
- **The `tome_of_the_unstained` save alias.**
- **Registered retired manipulations.** These are 14 classes, about 1,070 lines. Players
  can no longer use them, but their registry IDs back memory items and saves. Vesper
  uses `glacial_bastion` and `blood_eclipse_mantle` through `EntityManipulationEffects`.
- **All of `compat/mna/**` and `compat/curios/**`, plus their assets.** They are
  dormant, not dead. The orphaned `hemomancy.mna.mixins.json` and its empty
  `MixinHUDOverlayRenderer` stub belong to that dormant integration and should be
  fixed or removed together with it, not on their own.
- **`DevTestHooks`, the debug items** (`debug_showcase`, `structure_scanner`,
  `structure_spawner`) **and the Dynasty Castle generator.**
- **The legacy husbandry config key.**
- **The registered entity types of dead spawners**, such as Tracking Pests and
  Tracking Serpent.

---

## F. Observed defects (report only, not fixed)

These were found while auditing. **None of them is included in the
behaviour-preserving refactor phases.** Each needs its own approved fix.
- **Approval** column: **Fix**, a plain bug fix with no design question; or
  **Design**, which changes gameplay or balance and needs a decision.
- **Confidence** column: as in the evidence labels, plus **SC** when the lead auditor
  re-checked it.

### Security, crash and data-loss

| # | Defect | Location | Confidence | Approval |
|---|---|---|---|---|
| F-1 | **A NaN donation fills the bloodline pool.** `Math.min(NaN, vol)` is NaN and `NaN <= 0` is false. `drain(NaN)` then passes and sets the player's volume to NaN, and `contributeBlood(NaN)` fills the pool to its maximum. Requires a modified client. | `common/network/capa/harbinger/PacketLumpDonate.java:56` | V, SC | Fix |
| F-2 | **Client-only code reachable on a dedicated server.** `PacketEquipmentSync`, `PacketGourdScarSync`, `SyncTrackingAvatarPacket` and `PacketCurvedHornAnimation` are registered bidirectional but call client code. A client can send them to a server. | `PacketHandler.java:162,165,175,203` | V, SC (registration). Runtime effect is **H**. | Fix |
| F-3 | **Probable dedicated-server crash.** `WillEntity` calls a static method on `WillAbsorptionGlowParticleFactory`, which implements the client-only `ParticleProvider`. The class cannot load on a dedicated server, so this would fail when a Will absorption completes. | `common/entity/**/WillEntity.java:575` | V, SC (code). Crash is **L** (confirm with `runServer`). | Fix |
| F-4 | **ScaleGrip can lose items.** On death, protected items move into an unpersisted static map, even when the death is then cancelled by Chamber or Mycophant handlers at the same priority. The items are lost on server stop, and a later death overwrites the entry. | `common/item/shared/ScaleGripEvents.java` | V (path), L (consequence) | Fix |
| F-5 | **OrganEcho can delete the wrong item.** It clears a slot using a compartment-relative index, so a duplicate echo in the offhand wipes hotbar slot 0. | OrganEcho handling; see evidence/11 | V (path), L (consequence) | Fix |
| F-6 | **The ledger runs without its item.** `PacketLedgerAction`'s four actions (summon NPCs, recall to lodge, set recall point, disband) check bloodline leadership but not that the player holds the ledger. | `PacketLedgerAction.java:82-95` | V | Fix |
| F-7 | **Bloodline chat has no rate limit.** It bypasses vanilla chat: no signing, spam kick, mute handling or logging. | `PacketBloodlineMessage.java:42-77` | V | Design |
| F-8 | **NaN settings and reach extension.** `PacketUpdatePoolSettings` passes NaN through. `GroundBloodDrawPacket` uses a client-supplied, unclamped partial tick in `player.pick`. | `PacketUpdatePoolSettings:55-57`, `GroundBloodDrawPacket:51-56` | V (code), H (impact) | Fix |
| F-9 | **Structure paste checks only creative mode.** `PlaceStructurePacket` checks `isCreative()` only, with no permission or held-item check. | `PlaceStructurePacket.java:87` | V | Fix |
| F-10 | **Mismatched builds are not refused.** The network version is a constant, so mismatched client and server builds fail later, at decode time. | `PacketHandler.java:77` | V, SC | Fix (N-7) |

### Gameplay correctness

| # | Defect | Location | Confidence | Approval |
|---|---|---|---|---|
| F-11 | **Blood-structure crafts finish too fast.** `PendingBloodCraftManager.tick()` runs once per loaded level per tick, not once per server tick. With three dimensions loaded, crafts finish about 3x faster; the evidence report estimates up to 5x. `clear()` is never called, and pending crafts hold `ServerLevel` and `ServerPlayer` references across integrated-server sessions. | `HarbingerCardinalRiteEvents.java:183` | V, SC | Fix. Players will notice that crafts take their intended time. |
| F-12 | **Rite-locked stations still accept projected blood.** `BloodProjectionItem` calls `fillFromSource` without `canReceiveBlood()`, which contradicts `docs/STATION_UPGRADES.md`. | `BloodProjectionItem.java:207-216` | V (code), L (in game) | Fix |
| F-13 | **Scar upkeep bypasses Sanguine Reserve and the ledger.** It drains every tick with no sync, wipes the reserve when short, and contradicts the skill text. | `ScarDefinition.java:163-176` | V | **Design** (balance) |
| F-14 | **Passive scar effects are infinite effects applied once on bind.** Milk removes them until the next relog. | `ScarDefinition.java:102-104` | V | Design |
| F-15 | **Living weapons drain blood without checks.** The Blade, Axe, Crossbow and Tool drain in both branches. When short, this wipes the reserve, and none of them check `isActive()`. | living tool items | V | Design |
| F-16 | **Area damage hits everything, unattributed.** Silent Severance and a Still Art deal unattributed magic damage to every living entity nearby: pets, allies, and players unless they are in creative. There is no PvP check and no kill credit. Sheolic retaliation and the Saprovitta trail are also unattributed, so on-kill hooks do not fire. | `SilentArchonArmorAbilityHandler:132-137`, `StillArtInit:52-57,152-157` | V | Design |
| F-17 | **Curved Horn ignores the shared death-save cooldown.** Its death-save skips `LastRiteHelper`. | `HarbingerEquipmentEntityEventHandler:70-90` | V | Design |
| F-18 | **Damage-handler order is undefined, and reductions stack without a cap.** See G-1. | multiple | V | Design |
| F-19 | **Station kits ignore the delivery contract.** They can be delivered to dead players, count Creative discard as delivered, and skip the observational-visit check. This violates AGENTS.md. | `StationUpgradeProgress.java:103-110` | V | Fix |
| F-20 | **Ichorian knowledge does not resync on dimension change.** The client copy is probably lost after portal travel. | Ichorian sync events | V (path), L (effect) | Fix |
| F-21 | **The live Archon-choice dual-write.** See S-6. | `DialogueEventHandler.java:466` | V | Fix, as part of S-6 |
| F-22 | **The legacy Dictation Table probably still appears in the main creative tab.** Its exclusion check never matches. | `Hemomancy.java:259` | L | Fix |
| F-23 | **Tag files are silently ignored.** Venous walls are probably missing from the vanilla walls tag, and the knapper from HutosLib's knappers tag. | D-4 | V (path), L (effect) | Fix |
| F-24 | **Three structures and the mausoleum can never generate.** `crimson_lodge_annex`, `bog_body_ossuary_niche` and `sanguine_surveyor_bivouac` have live structure sets but no committed `.nbt`. The mausoleum biome tag is empty. | worldgen data | V | Design (finish or remove) |
| F-25 | **An MnA config is read but never registered.** `HemoMnAConfig.register()` is never called, yet `ManipulationCostLedger:66` reads it, inside a try/catch. | | V | Fix |
| F-26 | **The Chthonian Queen's entity type is built under the name `"chthonian"`.** | `EntityInit.java:674-677` | V | Fix (check data-fixer impact) |
| F-27 | **`HemolysisEffect` category disagrees with itself.** It is registered as neutral but reports itself as beneficial. | | V | Fix |

### Minor, cosmetic or performance-only

These are listed in the evidence reports:
- Avatar hit particles spawn at the player's eye.
- Edacious set sends an abilities packet every tick.
- Scar synergy re-adds a permanent modifier every second.
- Block blood is persisted twice with float truncation.
- The post-cast sync skips diagnostics.
- Silent Archon cancels the same damage twice.
- `WhiteHumorVolume.fillFromSource` can drain without filling; it has no callers yet.
- The Vesper mount blend may take the long way round (**H**).
- `BloodVolumeEvents.onDimensionChange:451` sends a "Welcome!" chat message on every dimension change, probably a debug leftover.
- The Scar Station sends three syncs per click.
- `BloodCraftingKeyPressPacket` sends an unused ItemStack on every keypress.

---

## G. Things that should stay distinct

Not everything that looks similar should be merged. These differences are deliberate
or carry real value:

- **Harbinger vs Unstained progression** stay separate attachments, with their
  documented reset rules.
- **Escrowed Working-craft offerings vs procession offerings.** AGENTS.md requires them.
- **Blood Structures take no offerings and stay floorless**, per the pinned roster.
- **Vesper, Annetta and the Mycophant** share helpers, not a base class.
- **Procedural chain styles** (A-5), **world noise constants** (W-2) and **seeded shader
  noise variants** (V-5) stay separate. Merging them changes output.
- **`FloorMesh`, the rite boundary renderers and `QliphothBloomRenderer`** are cohesive art.
- **The knowledge stores** cover different domains.
- **Puppet summons** are the cleanest composition pattern in the repo.
- **Dialogue authoring styles** (D-6) need a content-pipeline decision, not a refactor.
- **Bespoke VFX payloads with their own geometry**, such as `BloodFlowPacket`,
  `SpawnClawSlashPacket` and `CardinalRiteImpactPacket`.

---

## H. Verification notes and open questions

**Counts that differ between evidence reports.** Each definition is correct:
- **Source-text tests:** 383 files contain assertions over `.java` text (evidence/08).
  471 files call `Files.readString` on any source (evidence/01 and 07). Plan with 471.
- **Static per-player maps never cleared:** evidence/03 lists about 11 by name.
  evidence/06 counts 24 static player/UUID-keyed collections whose cleanup is
  unverified. Treat 24 as the upper bound.
- **Backup files:** one evidence report calls the `*.java~` files tracked.
  `git ls-files` shows they are untracked (**SC**).

**Spot-checked by the lead auditor (SC):**
- F-1: the code path.
- F-2: the bidirectional registrations at `PacketHandler.java:162,165,175,203`.
- F-3: the client `ParticleProvider` call from `WillEntity:575`.
- F-10: the constant network version at `PacketHandler.java:77`.
- F-11: the per-level `PendingBloodCraftManager.tick()` call.
- The backup-file tracking status.
- The baseline test numbers.

**Not yet verified, and what would settle each:**
- Dedicated-server classloading of the 288 common files that import client classes:
  a `runServer` or `gameTestServer` run.
- The F-3 crash: start a dedicated server and complete a Will absorption.
- Whether the 11 GameTest classes are registered twice: look for duplicate batch names
  in the GameTest server log.
- The F-22 and F-23 effects: one in-game check.
- The P-9 render-type cost: a Spark or JFR profile in a brazier-heavy scene.
- Whether the 9 test-only rule classes lost their production call sites: trace each.
