# Hemomancy Consolidation Roadmap

Status: **proposal, awaiting approval**, 2026-10-21. No phase has started.

Related documents:
- Finding IDs (`S-5`, `F-1`, …) refer to [ARCHITECTURE_AUDIT](ARCHITECTURE_AUDIT.md).
- The target conventions are in [CANONICAL_STANDARDS_PROPOSAL](CANONICAL_STANDARDS_PROPOSAL.md).
- Low-risk items that can be done inside or before any phase are in [QUICK_WINS](QUICK_WINS.md).

## Ground rules for every phase

1. **Green before, green after.** The phase starts with `./gradlew.bat test` green, and
   ends with `test` and `alphaCheck` green.
2. **One subsystem, finished.** A phase migrates **every** call site in its scope and
   deletes the superseded code. It does not leave a new helper next to the old copies.
   - A temporary adapter is allowed only with a removal step inside the same phase.
3. **Behaviour preserving by default.** Anything that would change gameplay, balance,
   visuals or timing is pulled out into a separate change marked **APPROVAL**.
4. **Persisted identifiers are frozen.** These may not change during cleanup:
   - registry IDs and resource paths
   - NBT keys and codec field names
   - payload byte layouts and structure codecs

   A change to any of them is a migration with its own fixture test and approval.
5. **Tests move with code.** Source-text tests that pin the moved text are updated or
   replaced **in the same commit**. Prefer turning them into behaviour or data tests.
6. **Measure.** Each phase records the following in its PR description:
   - files and lines removed and added
   - helpers and classes deleted
   - call sites migrated
   - test counts before and after
   - remaining legacy instances

   These are diagnostics, not targets.
7. **Commit discipline.** Each phase is one branch and one reviewable PR. Large phases
   are split into numbered sub-PRs, each green on its own.

Effort scale:
- **S**: up to 1 focused day.
- **M**: 2–4 days.
- **L**: 1–2 weeks.
- **XL**: more than 2 weeks.

These are rough single-developer-with-agent estimates.

---

## Priority overview

| Phase | Name | Value | Risk | Effort | Depends on |
|---|---|---|---|---|---|
| **0** | Green baseline and test infrastructure | Unblocks everything | Low | M | — |
| **1** | Verified dead-code removal and packaging | High clarity, low risk | Low | S–M | 0 |
| **2** | Network hygiene | High: security and clarity | Low–Med | M | 0 |
| **3** | Player-state plumbing | High: removes 3 families of duplication | Low–Med | M | 0 |
| 4 | Shared small utilities | Medium–High | Low (with byte-equivalence tests) | M | 0 |
| 5 | Blood accounting | High | Med | M–L | 3, 4 |
| 6 | Combat targeting and damage attribution | High | Med–High | L | 4, 5 |
| 7 | Registration and client registration | Medium | Low–Med | M | 0 (test triage) |
| 8 | Rendering plumbing | Medium | Med (visual) | L | 7 |
| 9 | Animation conventions | Medium | Med (timing) | M–L | 4 |
| 10 | God-class decomposition | High for future work | Med–High | XL | 0, 2, 5 |
| 11 | Persistence authority migrations | High | **High (saves)** | L | 3 |
| 12 | Content, worldgen and data consolidation | Medium | Low–Med | L | 7 |
| 13 | Build, tooling and enforcement | Medium | Low | M | 0 |

**Recommended first three phases: 0, 1 and 2.**

- **Phase 0** is mandatory. Today the suite has 20 failures, so no refactor can be
  judged.
- **Phase 1** removes noise everyone else would otherwise have to read around. It is the
  safest work and validates the "tests move with code" process.
- **Phase 2** has the best value-to-risk ratio. It closes a group of security and crash
  problems, is mechanical, and touches a well-bounded package.
- **Phase 3** is the strongest candidate to come next, and it can run in parallel with
  Phase 2 once Phase 0 is done.

---

## Phase 0: Green baseline and test infrastructure

**Problem.** The JUnit baseline fails 20 tests (audit section B). Hundreds of tests pin
source text (T-1), every test carries its own copy of the assertion helpers (T-2), and
a fresh clone cannot run every test (`/tools/` is gitignored).

**Scope**

1. **Make the 20 failures pass.** They fall into four groups:

   | Group | Count | Action |
   |---|---|---|
   | Generator markers missing | 6 | Regenerate the models from their generators, or update the tests if the hand edits are intended. **Owner decision per model.** |
   | Asset or data drift | 7 | Fix the data or the test as appropriate. `ClientModelTextureCoverageTest` may reveal a real missing texture for `covenant_mantle` and `silent_archon_*`. |
   | Encoding | 3 | Fix the mojibake in `en_us.json`. Re-save `docs/TESTING.md` as UTF-8. |
   | Stale count | 1 | Delete the hard-coded count in `LegacyMainTestSuiteContractTest` and assert discovery instead. |

   The remaining 3 are source or doc text drift. Update each test to the current text,
   or replace it with a behaviour check.

2. **Shared test support.** Add `src/test/java/com/vincenthuto/hemomancy/support/` with
   `SourceAssertions`, `ResourceReader` and `RepoPaths`. Do not mass-convert existing
   tests in this phase. Convert only the tests touched here.
3. **Tests that need `/tools/`.**
   - Version-control the parts of `tools/` that tests read.
   - Or make those 6 tests skip with a clear message when the folder is absent.
4. **Run CI on the working branch.** Add `neo-1.21.1` to the push branches in
   `.github/workflows/alpha-check.yml`.
5. **Record a baseline run** for comparison:
   - `alphaCheck`
   - one `runServer` start (dedicated-server classloading)
   - one `runClient` start
6. **Inventory the source-text tests.** Generate a CSV listing, for each test file, the
   production files it pins. This becomes the impact map every later phase consults.

**Canonical:** standards §11.
**Files:** `src/test/**`, `src/main/resources/assets/hemomancy/lang/en_us.json`, `docs/TESTING.md`, `.github/workflows/alpha-check.yml`, the affected model JSONs.
**Expected simplification:** an honest, green baseline, and one impact map instead of guesswork.
**Risk:** low. Model regeneration could change shipped models, so each one needs a visual check.
**Compatibility:** none.
**Prerequisites:** none.
**Tests:** this phase *is* the tests.
**Effort:** M.

**Done when**
- `test` is green.
- `alphaCheck` is green, or its remaining failures are recorded as known with an owner.
- A dedicated-server start is recorded.
- The impact map is committed under `docs/architecture/evidence/`.

---

## Phase 1: Verified dead-code removal and packaging

**Problem.** About 80 unreachable classes, about 2,750 lines of unused Vesper clips,
dead registrations, a per-frame dead render handler, and jar bloat (audit section E
"SAFE", D-7, P-11).

**Scope.** Only the **SAFE** list in audit section E:
- the Vesper clips
- `RenderBloodLaser*`
- empty classes and handlers
- unreferenced particles, layers and models
- `Hemomancy.instance` / `forcesLoaded`
- uncalled methods
- `HemoBookReloadListener`
- unused render types and shaders
- the dead `ITEM_BLOOD_VOLUME` attachment
- the empty mixin file

Packaging fixes:
- the `.cache` exclude
- `/old/` archives and the 2.6 MB reference image moved out of `src/main/resources`

Plus local cleanup of the untracked `*.java~` files and `bash.exe.stackdump`.

**Not in scope.** Everything in "REQUIRES VERIFICATION". Each of those becomes an owner
decision.

The `ITEM_BLOOD_VOLUME` attachment deserves care. It is registered, so removing the
registration needs proof that no player save ever wrote it. Item attachments are not
serialised for item stacks, so the risk is very low, but confirm this before deleting.

**Expected simplification:** roughly 5,000 lines and 25–30 classes removed, and one per-frame handler gone.
**Risk:** low.
**Compatibility:** none for registry entries kept. Shader removals need a resource-reload check.
**Prerequisites:** Phase 0.
**Tests:** update the source-text tests that assert on removed text; the Phase 0 impact map lists them. Run `alphaCheck` and one client start (shader reload).
**Effort:** S–M.

**Done when**
- Every SAFE item is gone, or moved to "REQUIRES VERIFICATION" with a reason.
- The jar size before and after is recorded.
- The build is green.

---

## Phase 2: Network hygiene

**Problem.** N-1, N-2 (partial), N-3, N-4, N-5, N-6, N-7. It also makes the network
defects easy to fix: F-1, F-2, F-6, F-8, F-9, F-10.

**Scope, behaviour preserving**

1. Add `PROTOCOL_VERSION` to `PacketHandler`.
2. Register each payload in its real direction, and remove the `isClientSide` guards
   that existed only because of bidirectional registration. Keep
   `AntecedentEffectPacket` as the one documented bidirectional exception.
3. Move the client handling in the 8 `common/network` files, and the 3 lambdas inside
   `PacketHandler`, into `client/**` statics.
4. Add the `PacketHandler.sendNear` and `sendTracking` facade. Migrate the 28 direct
   `PacketDistributor.sendToPlayer` calls. Move dialogue routing into `DialogueSender`.
5. Add `ServerboundGuards`: `menu`, `nearBlock`, `nearEntity`, `finite`, and a shared
   cast-eligibility predicate exposed by `BloodManipulation`. Migrate the duplicated
   guards to it, keeping their thresholds.
6. Fold the 6 to 8 single-purpose particle payloads into `ParticleBurstPacket` and
   `EntityVisualPacket`.
7. Unregister and delete the 18 never-sent payloads and the unregistered
   `TeleportToVeinPacket` with its unreachable screen. This is safe because the
   protocol version bumps at the same time.

**APPROVAL items.** These are separate commits in the same PR, for review:

| Defect | Fix |
|---|---|
| F-1 | Reject non-finite or non-positive donations. |
| F-8 | Reject NaN pool settings. Clamp the client partial tick to [0, 1], or use 1.0 on the server. |
| F-6 | Require the ledger to be held or owned for ledger actions. |
| F-9 | Require operator permission and the held Structure Spawner for structure paste. |
| F-7 | Bloodline chat rate limiting. **Needs a design decision.** |

**Canonical:** standards §2.
**Files:** `common/network/**` (184 files), `PacketHandler.java`, matching `client/**` handler classes, about 85 send call sites.
**Expected simplification:**
- About 25 payload classes removed.
- One send facade and one validation utility.
- 32 registration directions corrected.
- No client imports left in `common/network`.

**Risk:** low to medium. Re-registering a payload's direction can surface a forgotten
sender. GameTests cover most gameplay flows.
**Compatibility:** client and server must update together. The protocol bump makes a
mismatch a clean refusal.
**Prerequisites:** Phase 0.
**Tests**
- Add a guardrail test: no `net.minecraft.client` import under `common/network`.
- Add a test asserting every payload's registration direction matches its senders.
- Add unit tests for `ServerboundGuards`.
- Update the 22 to 28 tests that read `PacketHandler.java` as text.
- Run gameplay GameTests and a dedicated-server start.

**Effort:** M.

**Done when**
- No `playBidirectional` registrations remain except the documented one.
- No client imports remain in `common/network`.
- Every serverbound payload uses `ServerboundGuards`, or documents why not.
- The approved defect fixes are merged.

---

## Phase 3: Player-state plumbing

**Problem.** S-2, S-3, S-4, S-5, S-8. There are 17 hand-copied sync triplets, per-tick
serialise-and-diff, missing accessors, eight copies of the pending-delivery state
machine, and session maps that are never cleared.

**Scope, behaviour preserving**

1. **Accessors (S-4).**
   - Add `HemoCapabilityAccess.muscleMemory(Player)` and `dialogueKnowledge(Player)`.
   - Player-attachment accessors return the concrete type. Collapse the `Optional`
     ceremony at about 110 sites; this is mechanical.
   - Replace the 6 direct `getCapability` calls in `HarbingerEquipmentContainer`.
2. **Session state (S-8).**
   - Every static per-player or session map registers with `TransientServerStateEvents`.
   - Add a logout fan-out there.
   - Delete the 21 private stop handlers and the 26 private logout handlers.
3. **Sync registry (S-2, S-3).**
   - One `PlayerStateSync` table of `(attachment, packet factory)` pairs, driven by the
     existing `HemoCapabilityRegistrar` login, respawn and dimension trio.
   - Mutators mark the attachment dirty. A single end-of-tick flush replaces the
     `KnownManipulationEvents` per-tick diff and the `StationUpgradeEvents` 20-tick resync.
   - Delete the 17 triplet classes' sync handlers and the redundant `EntityJoinLevel`
     resyncs.
4. **`PendingDelivery` (S-5).**
   - A helper for "living, not observational, count confirmed insertion, keep the
     remainder", with caller-owned tag read and write lambdas.
   - One driver for tick, login and respawn.
   - Migrate the seven claims: First Draws, First Separation vials and tools, Mycophant
     Tendril, Vesper Memory, Communion Spine, Concentrated Blood.
   - Spine's "already carried" check and Vesper's root-key migration stay as
     caller-side lambdas. **NBT keys are unchanged.**

**APPROVAL items:**
- **F-19.** Migrate station kits onto `PendingDelivery` as well. That adds the living
  and observational checks and stops counting Creative discard as delivered, which
  changes behaviour to match AGENTS.md.
- **F-20.** Resync Ichorian knowledge on dimension change.
- **F-4.** ScaleGrip: check whether the death was cancelled, and persist pending returns.

**Canonical:** standards §6 and §7.
**Files:** `common/capability/**`, `common/mission/alchemist/**`, `common/worldgen/{Mycophant,Vesper}*Manager`, `common/rite/harbinger/QliphothBloomEvents`, `common/station/**`, `common/event/TransientServerStateEvents`, about 45 classes holding static maps.
**Expected simplification:**
- 17 sync handler sets become 1 table.
- 8 delivery implementations become 1 helper.
- About 47 private lifecycle handlers become 1 hub.
- Per-tick NBT serialisation is gone.

**Risk:** low to medium. Pending deliveries are progression-critical.
**Compatibility:** no NBT changes. Packet contents are unchanged.
**Prerequisites:** Phase 0.
**Tests**
- The existing loaded-fixture GameTests for each delivery: full inventory, death, Creative, observational visit, and duplicates.
- A new unit test for `PendingDelivery` covering partial insertion.
- A GameTest asserting each synced attachment reaches the client after login, respawn and dimension change.
- A leak test: a map entry is gone after logout.

**Effort:** M.

**Done when**
- No `*Events` class hand-registers the sync trio.
- Every pending claim goes through `PendingDelivery`.
- Every session map is listed in `TransientServerStateEvents`.
- The AGENTS.md documentation contract paragraphs still hold. Update them to name
  `PendingDelivery`.

---

## Phase 4: Shared small utilities

**Problem.** A-2, G-5, G-6, L-4, L-7 (sync helper), O-3, S-6 (gates), D-3, P-1, W-2
(identical setters).

**Scope**

1. **`common/util/HemoEasing`.**
   - Cubic smoothstep, `lerp(a, b, t)`, `clamp01`, `pulse`, crossfade.
   - Seeded from `CardinalRiteMath`, which then delegates to it or is folded into it.
   - Replace the private copies **only where they are byte-equivalent**. Watch the
     `lerp` argument order. A copy that differs, for example a quintic variant, stays
     as a named variant in `HemoEasing`.
2. **`AttributeModifiers.sync`.** It does nothing when the value is unchanged. Migrate
   the 9 hand-written copies. **This also fixes the P-1 Elytra packet storm without any
   gameplay change.**
3. **`ArmorSets.isFullSet`** replaces the three detection methods.
4. **`PatternOrigins`.** `originForCell`, `offset`, `findContaining` and bounds.
   Migrate the packet matcher, the Blood Structure helper, rite re-verification and
   Succession.
   - **First:** add GameTests that lock today's winner on overlapping matches, using
     `HematicStakeBloodStructureGameTests` and `EnzymaticScriptoriumGameTests` as a base.
5. **`HemoTickers.create`** and **`BlockEntitySync.markAndSync`.** Replace the 8 private
   ticker helpers, the 8 unchecked casts, and the four sync-helper names with seven
   bodies. Drop the stacked `VanillaPacketDispatcher` calls (the F-adjacent sync storms).
6. **`ProgressionGates`.** `hasDegree(player, n)`, `isBloodActive(player)` and
   `unstainedStage(player)`, wrapping `RecipeDegreeGates`. Migrate the 57 literal degree
   comparisons and the 5 Unstained ladder copies.
7. Merge the identical worldgen replaceable-block setters into `FeatureLogic`. **Do not
   touch the noise helpers.**

**Canonical:** standards §4, §5 and §9.
**Expected simplification:**
- About 100 private helper copies removed.
- 5 origin solvers become 1 utility plus 2 semantic matchers.
- One gate API instead of about 40 inline checks.

**Risk:** low, provided every replacement is byte-equivalent.
**Compatibility:** none.
**Prerequisites:** Phase 0.
**Tests**
- Golden-value unit tests for every easing function.
- GameTests for multiblock overlaps.
- Unit tests for `ProgressionGates` at each degree boundary.
- A packet-count assertion for P-1 in a GameTest.

**Effort:** M.

**Done when**
- No private `smoothstep`, `lerp` or `clamp01` remains outside `HemoEasing`, except documented variants.
- No private ticker helper remains.
- No literal degree comparison remains outside `ProgressionGates`.

---

## Phase 5: Blood accounting

**Problem.** G-3, L-2, L-3. About 98 direct `drain()` calls bypass `BloodFlowLedger`.
Volume sync has two forms. Projected blood goes through an `instanceof` chain.

**Scope, behaviour preserving**

1. **`BloodAccounting.pay(player, amount, Source{id, category, countsAsSpend, reserveProtected})`.**
   - It wraps today's semantics exactly, including `drain()`'s empty-on-overstrain.
   - It records to the ledger and ends with `BloodVolumeEvents.syncVolume`.
2. **Migrate every payment site** in items, tiles, blocks, rites, packets, armour
   abilities, morphlings and living weapons.
   - **The flags passed reproduce today's behaviour.** For example, scar upkeep passes
     `reserveProtected = false`, because that is what it does today.
3. Replace the 49 bare `BloodVolumeServerPacket` sends with `syncVolume`.
4. **`ProjectedBloodSink`.** An interface on block entities, defaulting to
   `IBloodReservoir.receiveBlood`. It replaces the `instanceof` chain in
   `BloodProjectionItem`.

**APPROVAL items.** These are balance changes:

| Defect | Change |
|---|---|
| F-13 | Scar upkeep becomes reserve-protected and synced. |
| F-15 | Living weapons check `isActive()` and stop wiping the reserve. |
| F-12 | Projection honours the rite lock. The `ProjectedBloodSink` default fixes it, so it is listed here explicitly. |

**Canonical:** standards §5.
**Expected simplification:**
- About 80 three-to-five-line payment blocks become one call each.
- One sync form.
- An 8-step `instanceof` chain becomes 1 interface.
- The diagnostics panel shows every spend.

**Risk:** medium. Blood is the core economy.
**Compatibility:** none. No NBT changes.
**Prerequisites:** Phases 3 and 4.
**Tests**
- Unit tests of `BloodAccounting` for each flag combination: overstrain, reserve and spend.
- GameTests for a representative item, machine, rite and armour payment, asserting the
  exact volume before and after.
- A rite-lock projection test.

**Effort:** M–L.

**Done when**
- `drain(` on a player volume appears only inside `BloodAccounting` and the documented
  effect payloads.
- Every payment site carries an explicit `Source`.

---

## Phase 6: Combat targeting and damage attribution

**Problem.** G-1, G-2, G-4, G-7, and the targeting gaps in L-1 and the manipulation
subclasses (audit C.5).

**Scope, behaviour preserving first**

1. Route the 10 target-validation implementations and the about 25 `Monster.class`
   filters through `ManipulationCombatHelper`.
   - Deliberate ally or self effects get an explicit opt-out.
   - Go **class by class**, recording any behaviour difference found.
2. **Heal-modifier table (G-4).**
3. **Flight arbiter (G-7).**
4. **`DrudgeActions` helper** for the 29 repeated "nearest Monster" lambdas, moved
   verbatim.

**APPROVAL items.** These change gameplay:

| Item | Change |
|---|---|
| G-1 / F-18 | Explicit damage stages and a single death-save stage. Blood lost per hit changes. |
| F-16 | Attribute the AoE damage from armour, Still Arts, Sheolic and Saprovitta, and respect allies and PvP. |
| F-17 | Curved Horn uses `LastRiteHelper`. |
| — | Puppets stop being caught by the caster's own `Monster.class` filters. |

**Canonical:** standards §5 and §7.
**Risk:** medium to high. Who gets hit is player-visible.
**Prerequisites:** Phases 4 and 5.
**Tests**
- A GameTest matrix: caster vs hostile, pet, ally, other player (PvP on and off), own
  puppet, and boss.
- Damage-stage ordering tests asserting the final damage and the blood drained per hit.

**Effort:** L.

**Done when**
- No raw `getEntitiesOfClass(... Monster.class ...)` remains in a damage path without
  `canHarm`.
- The damage-stage table is documented in the standards.

---

## Phase 7: Registration and client registration

**Problem.** R-1 to R-5, R-7, V-8, and the client half of P-10.

**Scope**
1. Normalise the `@EventBusSubscriber` style and remove `bus=`.
2. Move the creative-tab lists out of `Hemomancy.java` into one ordered WIP list.
3. Switch the custom registries to registry lookup.
4. Use event-API registration for BERs, particle providers and client extensions.
5. Split `ClientEvents` into `client/registry/*` plus feature handlers.
6. Move `ParticleInit`, `ShaderInit`, `RenderTypeInit`, `RenderBloodLaser` (if Phase 1
   kept it) and `FungalSkyBoxRenderer` into `client/**`.
7. Convert the data components to `DeferredRegister.DataComponents`.
8. Clear the client caches on logout.

**Risk:** low to medium. `ClientEvents` is pinned by 62 tests.
**Compatibility:** none. Registry IDs are unchanged.
**Prerequisites:** Phase 0, with the impact map.
**Tests**
- Registry snapshot test: the set of registered IDs is unchanged.
- Creative-tab order test.
- Client start.
- A dedicated-server start proving no client class loads.

**Effort:** M.

**Done when**
- `ClientEvents` is gone or is a thin index.
- No client class remains under `common/`.
- One subscriber style is used everywhere.

---

## Phase 8: Rendering plumbing

**Problem.** V-1 to V-7. P-9.

**Scope, in this order**
1. **Screen chrome (V-1).**
2. **Render-type caching (V-2).** One per-seed holder pattern and one school helper.
   Measure P-9 before and after with Spark.
3. **Particle data (V-6).**
4. **HUD and world dispatch (V-7).**
5. **Shared quad encoder (V-3).** Settle the Ferric `128` vs `127` difference visually
   first.
6. **Exact-copy shader noise into an include (V-5).**
7. **Optional, last:** shared frame and ring generation for tube builders (V-4). Each
   family keeps its own profile.

**Risk:** medium (visual).
**Compatibility:** none.
**Prerequisites:** Phase 7.
**Tests**
- A review-client screenshot comparison for each affected effect, attached to the PR.
- GUI layer order verified in game.
- About 125 tests read `client/render` text; update them with the moves.

**Effort:** L.

**Done when**
- Each step's duplicate copies are gone.
- Screenshots match.

---

## Phase 9: Animation conventions

**Problem.** A-1, A-3, A-4.

**Scope**
1. Promote `VesperAnimClock` to a shared `BossReplayClock`.
2. Rename `VesperMoveTimeline` to `BossMoveTimeline`.
3. Migrate Annetta, the Priestess, Latent Annetta, the Mycophant and the Saints.
4. Add one client player-animation registry that keeps today's priority order, and fold
   in the six bespoke player animations.
5. Add one `AnimationTime` helper.

**APPROVAL item:** any animation that would start freezing under `/tick freeze`
(A-4), decided case by case.

**Out of scope:** replacing per-tick synced counters with start timestamps. That is a
separate, higher-risk follow-up.

**Risk:** medium. Timing may shift by up to 2 ticks.
**Prerequisites:** Phase 4 (`HemoEasing`).
**Tests**
- Unit tests for the clock.
- Each boss reviewed in its review client: `runAnnetta*`, `loomReviewClient` and the
  Vesper and Mycophant clients.

**Effort:** M–L.

---

## Phase 10: God-class decomposition

**Problem.** L-1, L-6, N-8, O-4, and the `HemoCommand` hotspot.

**Scope.** Each item below is its own sub-PR.
1. **`RiteKind` strategy.** Extract the strategy from `HarbingerCardinalRiteEvents.onLevelTick`.
   - Move per-kind state out of `ActiveCardinalRite` tag bags into per-kind objects.
     **Keep the NBT layout.**
   - Move rite activation out of `BloodCraftingKeyPressPacket` into
     `rite/harbinger/CardinalRiteActivation`.
   - Move Blood Structure crafting out of the packet.
   - Fix F-11 here, under **APPROVAL**: `PendingBloodCraftManager` should tick once per
     server tick. Players will notice crafts taking their intended time.
2. **`DialogueEventHandler`.** Replace it with a per-NPC dialogue-event routing table.
   Each NPC's handlers live in their own class.
3. **`HemoCommand`.** Split it into one class per subcommand under the same root.
   Command syntax is unchanged.
4. **Manipulation entity casts (L-1).** A registration-time `setEntityCast` hook
   replaces the `EntityManipulationEffects` name switch.
5. **Scriptorium (L-9).** Move its operations into its block entity.

**Risk:** medium to high. These are the most-touched gameplay files.
**Prerequisites:** Phases 0, 2 and 5.
**Tests**
- Characterisation GameTests before each extraction: every rite kind start to complete
  and terminate, every dialogue event route, every command branch.
- Rite save and load across a restart mid-rite.

**Effort:** XL, spread across sub-PRs.

---

## Phase 11: Persistence authority migrations (save-critical)

**Problem.** S-1, S-6 (authorities), S-7, S-9, O-5.

**Scope.** Each item is **APPROVAL** and has its own fixture test.

1. **`dataVersion`.** A per-player attachment, so each migration runs once. Gate the 11
   existing login migrations on it, and stop the per-tick fungal-scar scan.
2. **Archon choice.** Make the attachment the only authority, and remove the live
   root-key dual-write (F-21).
3. **Circus progress.** Move its four stores into one `CircusProgress` attachment.
4. **Root-persistent stores with hand-written clone handlers.** Move Chamber access and
   `PendingWhispers` into attachments, and delete the clone handlers.
5. **Cooldowns.** Apply the two-tier convention, keeping each cooldown's current tier.
6. **Item `CUSTOM_DATA`.** Move to typed components only for items being reworked
   anyway, with a component-migration reader.

**Risk:** high. This is saved data.
**Compatibility.** Every migration has the following:
- a read-old-key path kept for at least one release
- a recorded sunset date
- an NBT fixture test with an old-format snapshot

**Prerequisites:** Phase 3.
**Effort:** L.

---

## Phase 12: Content, worldgen and data consolidation

**Scope**
- **O-1:** `BossBarBinding`, plus a `SaintTrialEntity` base.
- **O-2:** collapse copy-paste mob pairs and the tracking projectiles.
- **O-3:** `SyncedOwner`, NPC default methods, `HemoContainerMenu`.
- **O-6:** move item renderers into client extensions.
- **O-7:** make the hard-coded English translatable.
- **W-1:** `HemoJigsawStructure` for the 13 clones.
- **W-3:** move encounter managers to `common/encounter`. Keys are kept.
- **R-6:** a codec-based `JsonResourceReloadListener` plus a generic sync payload.
- **L-7:** `HemoBlockEntity`, and blood persisted only through the attachment, with a
  read fallback.
- **L-8:** recipe serializers.
- **D-5:** a datagen decision, per provider: revive or delete.

**APPROVAL items:**
- **F-23 / D-4:** move the tag files, which makes the tags start working.
- **F-24:** finish or remove the three template-less structures and the empty
  mausoleum tag.
- **F-26:** the Chthonian Queen build name.

**Effort:** L.

---

## Phase 13: Build, tooling and enforcement

**Scope**
- **T-6:**
  - One table-driven review-client run factory.
  - Commit the review launchers.
  - Prune or move the unreferenced `libs/` jars (owner decision).
  - Move dev-only `implementation` dependencies to `localRuntime`.
- **T-3:** convert the legacy `main()` tests as they are touched.
- **T-4:** one GameTest registration path.
- **T-5:** retire the `/hemo test` scenario catalog once GameTests cover it.
- **Write `docs/architecture/ARCHITECTURE_STANDARDS.md`.** Promote the approved parts of
  the proposal into it, and point `AGENTS.md` and `docs/agents/hemomancy-mod-agent.md`
  at it. Correct the D-8 doc drift.
- **Lightweight guardrail tests.** Each catches a pattern that previous phases removed:

  | Guardrail | Catches |
  |---|---|
  | No `net.minecraft.client` import under `common/` | Client code leaking into common |
  | No `playBidirectional` outside an allow-list | Mis-registered payload directions |
  | No `getPersistentData()` in new files | New code bypassing attachments |
  | No private `smoothstep` / `lerp` / `clamp01` in `client/` | Easing copies creeping back |
  | No `ResourceLocation.fromNamespaceAndPath("hemomancy"` | Hand-built resource locations |
  | No `bus = ` in `@EventBusSubscriber` | The ignored `bus=` argument |
  | Every static UUID-keyed map is registered with `TransientServerStateEvents` | Session state that is never cleared |

**Effort:** M.

---

## Decisions that need the owner

| Decision | Phase | Options |
|---|---|---|
| Hand-edited models vs their generators | 0 | Regenerate, or accept the edits and update the tests |
| Five unregistered mobs | 1 | Archive branch, or keep for later content |
| Network defect fixes F-1, F-6, F-8, F-9; bloodline chat F-7 | 2 | Approve as listed / adjust |
| Station kit delivery contract F-19, ScaleGrip F-4, Ichorian resync F-20 | 3 | Approve as listed |
| Scar upkeep F-13, living weapons F-15 | 5 | Balance decision |
| Damage stages G-1, AoE attribution F-16, Curved Horn F-17 | 6 | Balance decision |
| Animation freeze behaviour A-4 | 9 | Per animation |
| Blood-structure craft speed F-11 | 10 | Approve fix, which makes crafts take their intended time |
| Each save migration | 11 | Approve per migration |
| Tag folders F-23, missing structures F-24 | 12 | Fix, finish or remove |
| Unreferenced jars, dev-only dependencies | 13 | Prune or keep |

## Success measures per phase

These are recorded in each PR, as diagnostics rather than targets:
- Distinct implementations of the phase's concern, before and after (e.g. sync mechanisms 17 to 1).
- Classes and lines removed and added.
- Call sites migrated, and remaining legacy instances (should be 0 unless documented).
- Test count, and pass/fail before and after.
- Source-text assertions removed or replaced with behaviour tests.
- For performance items: the Spark measurement or packet count before and after.
