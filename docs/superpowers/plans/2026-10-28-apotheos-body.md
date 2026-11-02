# Apotheos Body (Section 1) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** An Apotheos body runs on spore load instead of food. It burns spores or sinks into fungal ground to heal, and the morphic tonic is its only remedy inside the Gardens.

**Architecture:**
- Pure rules (`SporeLoadRules`, `ApotheosBodyRules`) hold every number and decision, so JVM tests pin them.
- One attachment (`SporeLoad`) holds the state. It is synced through the `PlayerStateSync` table with a dirty flag.
- Server behaviour lives in `ApotheosBodyEvents` (tick and use-item) and `SporeLoadService` (burn). The client adds a key, a HUD layer and a food-layer cancel.

**Tech Stack:** NeoForge 1.21.1, Java 21, JUnit 5.

**Spec:** `docs/superpowers/specs/2026-10-28-apotheos-body-design.md`

## Global Constraints
- Player text is lang keys only. Every `"<prefix>.hemomancy.*"` literal must exist in `en_us.json`.
- Attachments are read through `HemoCapabilityAccess`. Sync goes through the `PlayerStateSync` table, never a lifecycle resend.
- Subscribers use `@EventBusSubscriber(modid = Hemomancy.MOD_ID)` (or with `value = Dist.CLIENT`).
- New payloads bump `PacketHandler.PROTOCOL_VERSION` to `"18"` and update `payload-registry.txt` (`version 18`, sorted entries). The serverbound key packet gets a reason entry in `ServerboundGuardsCoverageTest`.
- No `net.minecraft.client` under `common/`. No vanilla particles in new effects. No new `getPersistentData()`.
- No commits.

## Review Focus
1. A non-Apotheos player (D7, Silentium, Unstained) keeps eating, drinking, natural regen and the vanilla food bar. Covered by Task 1 rule tests with `apotheos=false`.
2. An Apotheos in a Chamber visit (food restored by `ChamberVisitService`) is re-pinned and not starved. Covered by the Task 2 pin rule test.
3. A burn at full health, below 25 load, or on cooldown spends nothing. Covered by Task 1 `burn` table.
4. The tonic is never refused, even for an Apotheos in the Gardens. Covered by Task 1 `refusesUse` cases with `isTonic=true`.
5. Spore load resets on death (no `copyOnDeath`). Covered by a Task 2 source check in `SporeLoadAttachmentTest`.

---

### Task 1: Rules

**Files:**
- Create: `common/capability/player/harbinger/spore/SporeLoadRules.java`, `common/capability/player/harbinger/spore/ApotheosBodyRules.java`
- Test: `src/test/java/.../common/capability/player/harbinger/spore/SporeLoadRulesTest.java`, `ApotheosBodyRulesTest.java`

**Interfaces (Produces):**
- `SporeLoadRules`:
  - Constants: `MAX = 100f`, `BURN_COST = 25f`, `BURN_HEAL = 6f`, `BURN_COOLDOWN_TICKS = 100`, `SINK_DELAY_TICKS = 40`, `SINK_HEAL = 2f`, `SINK_INTERVAL = 20`, `STILL_SPEED_SQR = 0.0004`.
  - `float gainPerSecond(boolean inDimension, boolean inOverworldBiome)`: 2.0 / 0.5 / 0.
  - `float clamp(float)`.
  - `BurnResult burn(float load, float health, float maxHealth, boolean cooldownReady)`: an enum `OK, TOO_FEW_SPORES, COOLING, WHOLE`. It checks in the order WHOLE, COOLING, TOO_FEW_SPORES.
  - `boolean isSinking(boolean crouching, boolean onGround, double horizontalSpeedSqr, boolean fungalGroundBelow)`.
  - `float sinkHeal(int stillTicks)`: `SINK_HEAL` when `stillTicks >= 40 && (stillTicks - 40) % 20 == 0`, else 0.
- `ApotheosBodyRules`:
  - `HUNGER_PIN = 17`.
  - `boolean refusesUse(boolean apotheos, boolean inGardens, boolean isFood, boolean isPotionOrMilk, boolean isTonic)`: tonic → false; not apotheos → false; food → true; potion or milk → `inGardens`.
  - `boolean symbioticSplit(boolean symbioticSkill, boolean apotheos)` = `symbioticSkill && !apotheos`.

- [ ] Step 1: Write tests with these exact cases:
  - gain: (true,false)=2, (false,true)=0.5, (false,false)=0.
  - clamp: -3→0, 140→100.
  - burn: (50,20,20,true)=WHOLE; (50,10,20,false)=COOLING; (20,10,20,true)=TOO_FEW_SPORES; (25,10,20,true)=OK.
  - isSinking: (true,true,0.0001,true) true; flip each argument to false (or speed 0.01) → false.
  - sinkHeal: 39→0, 40→2, 41→0, 60→2.
  - refusesUse: tonic with apotheos in Gardens → false; non-apotheos food → false; apotheos food outside → true; apotheos potion outside → false; apotheos potion in Gardens → true.
  - symbioticSplit: (true,true)=false, (true,false)=true.
- [ ] Step 2: Run `./gradlew.bat test --tests "*SporeLoadRules*" --tests "*ApotheosBodyRules*"`. Expected: compile FAIL.
- [ ] Step 3: Implement.
- [ ] Step 4: Run again. Expected: PASS.

### Task 2: Attachment, sync, server events

**Files:**
- Create: `common/capability/player/harbinger/spore/SporeLoad.java` (fields `load`; `transient int stillTicks`; `transient boolean syncDirty`; `INBTSerializable`), `common/network/capa/harbinger/PacketSyncSporeLoad.java`, `common/world/FungalGardensPlaces.java` (`static boolean isInGardens(Level, BlockPos)`, `static boolean inDimension(Level)`, `static boolean inOverworldBiome(Level, BlockPos)`), `common/event/ApotheosBodyEvents.java`
- Modify: `HemoAttachmentTypes` (`SPORE_LOAD`, no `copyOnDeath`), `HemoCapabilityAccess.getSporeLoad(Player)`, `PlayerStateSync` (`SPORE_LOAD` entry with dirty predicate), `PacketHandler` (playToClient, version 18), `payload-registry.txt`, `EquippedMorphlingEvents` (`ApotheosBodyRules.symbioticSplit`), `en_us.json` (`message.hemomancy.apotheos_body.refuses_it`)
- Test: `SporeLoadAttachmentTest` (add / clamp / dirty / NBT round trip; source check: the `SPORE_LOAD` registration has no `copyOnDeath`), `PayloadProtocolVersionTest`

**Interfaces (Produces):**
- `SporeLoad`:
  - `float load()`.
  - `void add(float)`: clamps and marks dirty when the value changes.
  - `boolean spend(float)`: false if load is short.
  - `int stillTicks()`, `void setStillTicks(int)`.
  - `boolean isSyncDirty()`, `void sync(ServerPlayer, boolean force)`.
- `ApotheosBodyEvents`:
  - `PlayerTickEvent.Post`, server side, Apotheos only. Every tick it pins hunger. Every 20 ticks it adds `gainPerSecond`. Every tick it updates `stillTicks` from `isSinking` and heals by `sinkHeal`.
  - `LivingEntityUseItemEvent.Start`: cancels and shows the message when `refusesUse`.

- [ ] Step 1: Write `SporeLoadAttachmentTest` and bump the registry header to 18. Run. Expected: FAIL.
- [ ] Step 2: Implement.
- [ ] Step 3: Run `./gradlew.bat test --tests "*SporeLoad*" --tests "*PayloadProtocolVersion*" --tests "*PlayerStateSync*" --tests "*AttachmentAccess*" --tests "*RegistrationStyle*"`. Expected: PASS.

### Task 3: Burn key, service, HUD

**Files:**
- Create: `common/capability/player/harbinger/spore/SporeLoadService.java` (`static void burn(ServerPlayer)`), `common/network/capa/harbinger/BurnSporeLoadKeyPacket.java`, `client/screen/overlay/SporeLoadOverlay.java`, `client/event/ApotheosHudEvents.java` (cancels `FOOD_LEVEL` for a local Apotheos)
- Modify: `PlayerCooldowns` (`SPORE_BURN = "spore_burn"`), `KeyMappings` (`burnSpores`, `GLFW_KEY_U`), `ClientInputEvents`, `GuiLayerRegistrar` (`registerAbove(VanillaGuiLayers.FOOD_LEVEL, rloc("spore_load"), …)`), `PacketHandler` (playToServer), `payload-registry.txt`, `ServerboundGuardsCoverageTest` reason, `en_us.json` (key desc, three burn-refusal lines)
- Test: `SporeLoadServiceSourceTest` is not needed (the rules are covered). Run the network guardrails.

- [ ] Step 1: Implement. The service checks Apotheos, maps `BurnResult` to a message, and on OK calls `spend` + `heal` + cooldown + glow particles from `HemoParticleData`.
- [ ] Step 2: Run `./gradlew.bat test --tests "*ServerboundGuards*" --tests "*PayloadProtocolVersion*" --tests "*NetworkPackage*" --tests "*HardCodedEnglish*" --tests "*HudAndWorldDrawing*" --tests "*CommonClientIsolation*"`. Expected: PASS.

### Task 4: Morphic tonic

**Files:**
- Create: `common/item/harbinger/MorphicTonicItem.java`, `assets/hemomancy/models/item/morphic_tonic.json`, `assets/hemomancy/textures/item/morphic_tonic.png` (recolour of `lethean_dew.png`), `data/hemomancy/recipe/morphic_nectar/bloody_jug_to_morphic_tonic.json`
- Modify: `ItemInit` (`morphic_tonic`), `CreativeTabInit`, `en_us.json` (name + tooltip)
- Test: `MorphicTonicRecipeResourceTest` (the recipe JSON has input `hemomancy:bloody_jug`, result `hemomancy:morphic_tonic`, `transform_time` 100; the item model exists)

- [ ] Step 1: Write the test. Run. Expected: FAIL.
- [ ] Step 2: Implement.
- [ ] Step 3: Run. Expected: PASS.

### Task 5: Docs and verification
- [ ] Update `HEMOMANCY_REFERENCE.md` §5 and `wiki/Harbinger-Path.md` Degree 8. Regenerate the impact map.
- [ ] `./gradlew.bat test compileGameTestJava`: all PASS. `./gradlew.bat build`: SUCCESS. `runGameTestServer`: only the known red tests (centrifuge, brood_tumor snapshot, scriptorium clue ids), plus any golden this change moves, which must be explained.
