# Consolidation Session Prompts

Fire these one at a time, in order, each in a new session. Wait for the previous
session to finish and commit before starting the next one; sessions share build
folders.

Each prompt relies on two files:
- [CONSOLIDATION_SESSION_RULES](CONSOLIDATION_SESSION_RULES.md): the protocol every
  session follows (preflight, execution, postflight).
- [CONSOLIDATION_PROGRESS](CONSOLIDATION_PROGRESS.md): the ledger. Fill in its
  *Decisions* table before the step that needs them. Anything not marked `APPROVED` is
  left alone and reported back to you.

If a step does not fit in one session, it stops at a green commit and marks itself
`IN PROGRESS`. Re-fire the same prompt to continue.

---

## Step 1: Urgent defect fixes

```
Consolidation step 1 of 13: urgent defect fixes. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly, with one exception: the baseline still has 20 known JUnit failures, listed in docs/architecture/CONSOLIDATION_PROGRESS.md. Preflight passes if the failure set is exactly those 20.

I approve F-1, F-2 and F-3 from docs/architecture/ARCHITECTURE_AUDIT.md. Mark them APPROVED in the ledger's Decisions table.
- F-1: PacketLumpDonate must reject non-finite or non-positive amounts before draining.
- F-2: Register PacketEquipmentSync, PacketGourdScarSync, SyncTrackingAvatarPacket and PacketCurvedHornAnimation with playToClient only. Move their client handling into client/** if that is needed to do it cleanly.
- F-3: WillEntity must not reference WillAbsorptionGlowParticleFactory or any other client class. Create the particle data from a common-side factory.

Do nothing else from the roadmap. Add regression tests:
- a unit test of the donation validation (NaN, infinity, 0, negative)
- an architecture test that those four payloads are client-bound
- an architecture test that WillEntity imports no net.minecraft.client or hemomancy.client class

Run ./gradlew.bat test (same 20 failures, no new ones). Then run ./gradlew.bat gameTestServer to prove the dedicated server loads. Update the ledger, then commit with the prefix consolidation(step-1):.
```

## Step 2: Green baseline and test infrastructure (roadmap Phase 0)

```
Consolidation step 2 of 13: roadmap Phase 0, green baseline and test infrastructure. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Preflight accepts the 20 known baseline failures, and step 1 must be DONE.

Do the following:
1. Make every one of the 20 baseline failures pass.
   - The 6 generator-marker failures depend on decision M-1. If M-1 is not APPROVED for a model, do not regenerate it. Mark only its specific tests @Disabled("pending decision M-1") and list them in the ledger.
   - If ClientModelTextureCoverageTest reveals a real missing texture, record it as a defect. Do not invent art.
2. Add src/test/java/com/vincenthuto/hemomancy/support/ with SourceAssertions, ResourceReader and RepoPaths. Convert only the tests you touch.
3. Tests that need the gitignored tools/ folder must skip with a clear message when it is absent.
4. Add neo-1.21.1 to the push branches in .github/workflows/alpha-check.yml.
5. Add a frozen-identifier snapshot: a committed list of every Hemomancy registry ID, payload type ID and attachment ID, plus a test (GameTest or data test) that fails when any of them changes. This guards every later step.
6. Generate the source-text test impact map: test file to the production files it pins. Save it as docs/architecture/evidence/source-text-impact-map.csv.

Postflight:
- ./gradlew.bat test must be fully green, except tests explicitly disabled under M-1.
- Run ./gradlew.bat alphaCheck and record the baseline result.

Register the snapshot test as a guardrail in the ledger, then commit with the prefix consolidation(step-2):.
```

## Step 3: Verified dead-code removal and packaging (Phase 1)

```
Consolidation step 3 of 13: roadmap Phase 1, verified dead-code removal and packaging. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 2 must be DONE and the build green.

Scope:
- Remove the SAFE list in docs/architecture/ARCHITECTURE_AUDIT.md section E. Use the impact-map CSV to find the tests that pin each removed item, and update them in the same step.
- Remove items under "REQUIRES VERIFICATION" only if decision M-2 or M-3 in the ledger marks them APPROVED for deletion. Otherwise list them as pending.
- Never touch the MUST PRESERVE list.

Before deleting the ITEM_BLOOD_VOLUME attachment, prove that no saved data can contain it. If you cannot prove it, keep it and record why.

Packaging:
- Fix the build.gradle excludes so datagen .cache files, /old/ archives and the Blockbench reference PNG do not ship.
- Move the authoring archives out of src/main/resources rather than hiding them.
- Add a guardrail test that the packaged jar contains no .cache or /old/ entries.

The frozen-identifier snapshot must stay unchanged. Run test and alphaCheck, and do one client start to confirm shaders still reload. Record the jar size before and after, update the ledger, and commit with the prefix consolidation(step-3):.
```

## Step 4: Network hygiene (Phase 2)

```
Consolidation step 4 of 13: roadmap Phase 2, network hygiene. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 3 must be DONE.

Implement the following, behaviour preserving:
1. A PROTOCOL_VERSION constant in PacketHandler.
2. Every payload registered in its real direction. Remove the isClientSide guards that only existed because of bidirectional registration. AntecedentEffectPacket stays as the one documented bidirectional exception.
3. All client handling in common/network moved into client/**, including the 3 lambdas inside PacketHandler.
4. PacketHandler.sendNear and sendTracking facade methods. Migrate the direct PacketDistributor.sendToPlayer calls, and move dialogue routing into a DialogueSender.
5. A ServerboundGuards utility (menu, nearBlock, nearEntity, finite, and a shared cast-eligibility predicate owned by BloodManipulation). Migrate the duplicated guards, keeping their current thresholds.
6. Fold the single-purpose particle payloads into ParticleBurstPacket and EntityVisualPacket.
7. Delete the 18 never-sent payloads, and TeleportToVeinPacket with its unreachable screen.

Implement F-6, F-7, F-8 and F-9 only if they are APPROVED in the ledger, each as its own commit.

Update the payload type IDs in the frozen-identifier snapshot deliberately, and explain the change in the ledger. Bump PROTOCOL_VERSION in the same commit.

Guardrails:
- no net.minecraft.client or hemomancy.client import under common/network
- playBidirectional only on an allow-list
- unit tests for ServerboundGuards

Run test, alphaCheck and gameTestServer. Update the ledger and commit with the prefix consolidation(step-4):.
```

## Step 5: Player-state plumbing (Phase 3)

```
Consolidation step 5 of 13: roadmap Phase 3, player-state plumbing. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 4 must be DONE.

Behaviour preserving, with no NBT key or packet-content changes:
1. HemoCapabilityAccess accessors:
   - Add muscleMemory() and dialogueKnowledge().
   - Player-attachment accessors return concrete types; collapse the Optional ceremony.
   - Replace the 6 direct getCapability calls in HarbingerEquipmentContainer.
2. TransientServerStateEvents becomes the single hub for every static session or per-player map, with logout removal. Delete the private stop and logout handlers it replaces.
3. One PlayerStateSync table of (attachment, packet factory) pairs, driven by the existing HemoCapabilityRegistrar login/respawn/dimension trio.
   - Mutations set a dirty flag that is flushed once per tick.
   - This replaces the 17 sync triplets, the KnownManipulationEvents per-tick diff and the StationUpgradeEvents 20-tick resync.
4. A PendingDelivery helper with caller-owned tag lambdas, plus one tick/login/respawn driver.
   - Migrate First Draws, First Separation vials and tools, Mycophant Tendril, Vesper Memory, Communion Spine and Concentrated Blood.
   - Keep each claim's special rule as a caller lambda.
   - Respect every AGENTS.md documentation-contract paragraph.

Implement F-4, F-5, F-19 and F-20 only if they are APPROVED in the ledger, each as its own commit.

Guardrails:
- no getData(HemoAttachmentTypes.*) outside the access class and an allow-list
- every static UUID-keyed map in common/ is registered with TransientServerStateEvents
- PendingDelivery unit tests, including partial insertion and Creative discard
- a GameTest proving each synced attachment reaches the client after login, respawn and dimension change

Update the AGENTS.md delivery paragraphs to name PendingDelivery. Run test and alphaCheck. Update the ledger and commit with the prefix consolidation(step-5):.
```

## Step 6: Shared small utilities (Phase 4)

```
Consolidation step 6 of 13: roadmap Phase 4, shared small utilities. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 5 must be DONE.

Create these in common/util, then migrate every call site and delete the copies:

HemoEasing
- Seed it from CardinalRiteMath: cubic smoothstep, lerp(a, b, t), clamp01, pulse, crossfade.
- Replace a private copy only when it is byte-equivalent. Watch lerp argument order.
- Never substitute Mth.smoothstep, which is quintic.
- Copies that genuinely differ become named variants.

Other utilities
- AttributeModifiers.sync: a no-op when the value is unchanged. This fixes the Elytra packet churn.
- ArmorSets.isFullSet.
- PatternOrigins. First, add GameTests that lock today's winner for overlapping multiblock matches. Then migrate the packet matcher, BloodStructureCraftingHelper, rite re-verification and SuccessionRites.
- HemoTickers and BlockEntitySync.markAndSync. These replace the private ticker helpers, the unchecked casts and the four sync-helper names, and drop stacked VanillaPacketDispatcher calls.
- ProgressionGates: hasDegree, isBloodActive, unstainedStage, wrapping RecipeDegreeGates. Migrate the literal degree comparisons and the Unstained ladder copies.

Also merge the identical worldgen replaceable-block setters into FeatureLogic. Do not touch the noise helpers.

Guardrails:
- golden-value tests for every HemoEasing function
- no private smoothstep, lerp or clamp01 outside an allow-list
- no literal getDegreeNumber() comparison outside ProgressionGates
- a GameTest asserting no attribute packet is sent while an unchanged modifier is held

Run test and alphaCheck. Update the ledger and commit with the prefix consolidation(step-6):.
```

## Step 7: Blood accounting (Phase 5)

```
Consolidation step 7 of 13: roadmap Phase 5, blood accounting. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 6 must be DONE.

1. Create BloodAccounting.pay(player, amount, Source{id, category, countsAsSpend, reserveProtected}).
   - It sits on top of BloodFlowLedger.
   - It reproduces today's semantics exactly, including drain() emptying the reserve on overstrain.
   - It ends in BloodVolumeEvents.syncVolume.
2. Migrate every player blood payment: items, tiles, blocks, rites, packets, armour abilities, morphlings, living weapons, scar upkeep, puppet threads.
   - Each call site passes flags that reproduce its current behaviour, so there is no balance change.
3. Replace the bare BloodVolumeServerPacket sends with syncVolume.
4. Add a ProjectedBloodSink interface on block entities, defaulting to IBloodReservoir.receiveBlood, to replace the instanceof chain in BloodProjectionItem.

Implement F-12, F-13, F-14 and F-15 only if they are APPROVED in the ledger, each as its own commit. Update docs/HEMOMANCY_REFERENCE.md and the wiki for any that are approved.

Guardrails:
- drain( on a player blood volume only inside BloodAccounting and a documented allow-list
- unit tests for every flag combination
- GameTests asserting the exact volume before and after for a representative item, machine, rite and armour payment

Run test and alphaCheck. Update the ledger and commit with the prefix consolidation(step-7):.
```

## Step 8: Registration and client registration (Phase 7)

```
Consolidation step 8 of 13: roadmap Phase 7, registration and client registration. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 7 must be DONE.

1. Subscriber style: @EventBusSubscriber(modid = Hemomancy.MOD_ID[, value = Dist.CLIENT]) everywhere, with no bus= argument.
2. Creative tab: one ordered WIP list that drives both acceptance and exclusion, moved out of Hemomancy.java. Implement F-22 only if APPROVED.
3. ManipulationInit and StillArtInit look entries up through the registry; keep the name strings.
4. Event-API registration for block-entity renderers, particle providers and client extensions (replacing FluidType.initializeClient).
5. Split ClientEvents into client/registry/* plus feature handlers, keeping the ordered world-effect dispatcher intact.
6. Move ParticleInit, ShaderInit, RenderTypeInit and FungalSkyBoxRenderer into client/**.
7. Convert the data components to DeferredRegister.DataComponents.
8. Clear client entity-id caches on ClientPlayerNetworkEvent.LoggingOut.

Use the impact map: about 62 tests pin ClientEvents. Rewrite them against behaviour where possible. The frozen-identifier snapshot and creative-tab order must stay unchanged.

Guardrails:
- no net.minecraft.client or hemomancy.client import anywhere under common/, apart from a documented allow-list of client-extension lambdas
- no bus= argument

Run test and alphaCheck. Do one client start and one gameTestServer run. Update the ledger and commit with the prefix consolidation(step-8):.
```

## Step 9: Combat targeting and damage attribution (Phase 6)

```
Consolidation step 9 of 13: roadmap Phase 6, combat targeting and damage attribution. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 8 must be DONE.

First, write a GameTest matrix that pins today's behaviour. For representative manipulations, armour abilities, Still Arts, morphlings and scars, record who is hit:
- hostile mob, tamed pet, ally, own puppet, boss
- another player, with PvP on and with PvP off

Behaviour preserving:
- Route the target-validation implementations and Monster.class filters through ManipulationCombatHelper.canHarm or allied. Deliberate ally or self effects get explicit opt-outs. Work class by class; the matrix must not change.
- Add a heal-modifier table, a flight arbiter, and a DrudgeActions helper. The drudge lambdas move verbatim.

Each of these is its own commit, implemented only if APPROVED in the ledger:
- G-1 / F-18: explicit damage stages and one death-save stage
- F-16: AoE damage attribution and ally/PvP respect
- F-17: Curved Horn through LastRiteHelper
- G-2b: puppets excluded from the caster's filters

For an approved item, update the matrix expectations in that same commit, plus docs/HEMOMANCY_REFERENCE.md and the wiki.

Guardrails:
- the matrix itself
- no getEntitiesOfClass(... Monster.class ...) in a damage path without canHarm
- if G-1 is approved, a damage-stage ordering test

Run test and alphaCheck. Update the ledger and commit with the prefix consolidation(step-9):.
```

## Step 10: Rendering plumbing and animation conventions (Phases 8 + 9)

```
Consolidation step 10 of 13: roadmap Phases 8 and 9, rendering plumbing and animation conventions. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 9 must be DONE.

This step will likely span several sessions. Work in this order, committing after each item and marking the ledger IN PROGRESS between sessions.

Before each visual change, capture before screenshots in the matching review client. Capture after screenshots when the change is done and record both paths in the ledger. Pixel output and animation timing must match, unless an item is APPROVED in the ledger.

Rendering:
1. Screen chrome through ScreenDrawUtils and VeinBackgroundRenderer, with palette parameters.
2. One render-type caching pattern (the per-seed holder) and one school render-type helper. Profile with Spark before and after.
3. A parameterised colour particle data class.
4. All HUD drawing through RegisterGuiLayersEvent, and all world effects through the ordered dispatcher.
5. A shared material quad encoder. The Ferric constant follows decision V-3b.
6. Byte-identical shader noise moved into a shared include.

Animation:
7. Promote VesperAnimClock to BossReplayClock, rename VesperMoveTimeline to BossMoveTimeline, and migrate the other bosses.
8. One client player-animation registry that keeps the exact priority order.
9. One AnimationTime helper. Freeze-behaviour changes follow decision A-4.

Leave FloorMesh, the rite boundary renderers, QliphothBloomRenderer, each tube family's profile and the procedural chain styles alone. Shared tube frame generation (V-4) is optional and goes last.

Guardrails:
- no private drawBorder or vein-background copies
- the same RenderType instance is returned for the same seed
- unit tests for BossReplayClock

Run test and alphaCheck. Update the ledger and commit each item with the prefix consolidation(step-10):.
```

## Step 11: God-class decomposition (Phase 10)

```
Consolidation step 11 of 13: roadmap Phase 10, god-class decomposition. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 10 must be DONE.

This step spans several sessions: one sub-PR per item below, each green and committed, with the ledger marked IN PROGRESS in between. Before each extraction, write characterisation GameTests:
- every rite kind's start, completion and termination
- save and load across a restart in the middle of a rite
- every dialogue-event route
- every /hemo command branch

Items, in this order:
1. A RiteKind strategy extracted from HarbingerCardinalRiteEvents.onLevelTick.
   - Per-kind state objects behind ActiveCardinalRite, keeping the exact NBT layout.
   - Rite activation moved out of BloodCraftingKeyPressPacket into rite/harbinger/CardinalRiteActivation, and Blood Structure crafting out of the packet as well.
   - F-11, crafts ticking once per server tick, only if APPROVED, as its own commit.
2. DialogueEventHandler replaced by a per-NPC routing table, with one handler class per NPC.
3. HemoCommand split into one class per subcommand under the same root, with identical command syntax.
4. A registration-time setEntityCast hook replacing the EntityManipulationEffects name switch.
5. Scriptorium operations moved from EnzymaticScriptoriumMenu into its block entity.

The frozen-identifier snapshot must stay unchanged. Rewrite pinned source-text tests as behaviour tests.

Guardrails:
- the characterisation suites
- no gameplay classes under common/network, apart from payloads, PacketHandler and ServerboundGuards

Run test and alphaCheck after every item. Update the ledger and commit with the prefix consolidation(step-11):.
```

## Step 12: Persistence authority migrations (Phase 11)

```
Consolidation step 12 of 13: roadmap Phase 11, persistence authority migrations. This step is save-critical. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 11 must be DONE.

Implement only the items marked APPROVED in the ledger (S-9, F-21, S-6c, S-1c, S-7, O-5), one commit each.

Every migration must:
- Read the old key or location exactly once.
- Write the new home, clear the old key and bump the per-player dataVersion.
- Keep its read path for at least one release, and record a sunset date in the ledger.
- Come with a fixture test that loads an old-format NBT snapshot and asserts the migrated state, plus a test that running it twice is a no-op.

S-9 (dataVersion) goes first if it is approved, since the others depend on it. Gate the existing login migrations and the per-tick fungal-scar scan on it.

Never rename keys inside existing attachments. Leave unapproved stores exactly as they are.

Guardrails:
- the fixture tests
- no new getPersistentData() call outside a documented allow-list

Run test and alphaCheck. Also run one manual load of an existing development world: record its path and the result in the ledger. Update the ledger and commit with the prefix consolidation(step-12):.
```

## Step 13: Content, worldgen, data, tooling and standards (Phases 12 + 13)

```
Consolidation step 13 of 13: roadmap Phases 12 and 13, content, worldgen, data, tooling and standards. Follow docs/architecture/CONSOLIDATION_SESSION_RULES.md exactly. Step 12 must be DONE.

This may span sessions; commit each item separately.

Content and data:
- O-1 BossBarBinding and SaintTrialEntity.
- O-2 collapse the copy-paste mob pairs and tracking projectiles.
- O-3 SyncedOwner, NPC default methods and HemoContainerMenu.
- O-6 item renderers through client extensions.
- O-7 hard-coded English made translatable.
- W-1 HemoJigsawStructure for the 13 clones. StructureType IDs and codec field names stay byte-identical.
- W-3 encounter managers moved to common/encounter, keeping every saved key.
- R-6 codec-based JsonResourceReloadListener plus a generic sync payload.
- L-7 HemoBlockEntity, with blood persisted only through the attachment and a read fallback.
- L-8 recipe serializers.
- D-5 per-provider datagen decisions.

F-23, F-24, F-25, F-26, F-27 and T-6b are implemented only if APPROVED.

Tooling:
- One table-driven review-client run factory in build.gradle.
- Commit the review launchers.
- Convert the legacy main() tests you touch.
- One GameTest registration path.

Standards:
- Write docs/architecture/ARCHITECTURE_STANDARDS.md from the approved and implemented parts of CANONICAL_STANDARDS_PROPOSAL.md.
- Point AGENTS.md and docs/agents/hemomancy-mod-agent.md at it.
- Fix the doc drift listed under D-8.
- Make sure every guardrail from steps 1 to 12 is listed in the standards.

Final check: run test, alphaCheck, gameTestServer and a client start. Then write a closing section in the ledger comparing the before and after metrics against the original audit, and listing what remains. Commit with the prefix consolidation(step-13):.
```
