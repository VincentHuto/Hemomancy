# Annetta Putriciel Velorum Telegraphs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Annetta's encounter family, Putriciel, and Velorum easier to read in combat through synced visual states, model poses, render pulses, and particle telegraphs.

**Architecture:** Add small `EntityDataAccessor<Integer>` visual-state/tick pairs to the relevant entity classes, with local helper methods to set and decay states. Existing attacks set windup/impact states and emit radius/tether/line particles. Existing vanilla models and renderers consume those states for poses and scaling.

**Tech Stack:** Java 21, Minecraft 1.21.1, NeoForge entity data synchronization, vanilla particles/sounds, existing `HumanoidModel` renderers.

---

### Task 1: Annetta Phase One And Support Telegraphs

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/AnnettaKnowlesEntity.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/AnnettaKnowlesModel.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/annetta/AnnettaKnowlesRenderer.java`

- [ ] **Step 1: Add synced visual state fields**

Add integer visual state and visual tick data to `AnnettaKnowlesEntity`, plus constants:

```java
public static final int VISUAL_NONE = 0;
public static final int VISUAL_SILVER_AURA = 1;
public static final int VISUAL_VIAL_THROW = 2;
public static final int VISUAL_KERATIN_SLASH = 3;
public static final int VISUAL_CURED_SUPPORT = 4;
```

- [ ] **Step 2: Tick and clear visual states**

Call `tickVisualState()` from server `aiStep()` before state-specific logic. The helper decrements `DATA_VISUAL_TICKS` and clears `DATA_VISUAL_STATE` when the timer expires.

- [ ] **Step 3: Set states in existing attacks**

Set `VISUAL_SILVER_AURA` when the silver aura fires, `VISUAL_VIAL_THROW` when a vial is thrown, `VISUAL_KERATIN_SLASH` when hair-and-nails slash fires, and `VISUAL_CURED_SUPPORT` during the support pulse.

- [ ] **Step 4: Add particle helpers**

Add a pale floor ring for silver aura and a short crit/sweep ring for keratin slash. Existing damage and effects remain unchanged.

- [ ] **Step 5: Pose and scale from visual states**

Update `AnnettaKnowlesModel` so cowering remains guarded, cured support opens the arms gently, silver aura raises arms, vial throw pulls one arm back, and keratin slash snaps both arms outward. Update renderer scale to subtly pulse during silver aura/support and shrink slightly while cowering.

### Task 2: Stained Priestess And Latent Infection Telegraphs

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/StainedPriestessEntity.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/annetta/LatentAnnettaInfectionEntity.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/StainedPriestessModel.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/LatentAnnettaInfectionModel.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/annetta/StainedPriestessRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/annetta/LatentAnnettaInfectionRenderer.java`

- [ ] **Step 1: Add visual state helpers to both entities**

Stained Priestess states: `VISUAL_LANCE`, `VISUAL_LUNGE`, `VISUAL_PRESSURE_BLOOM`.

Latent Infection states: `VISUAL_INFECTION_BLOOM`, `VISUAL_PRESSURE_SPIKE`.

- [ ] **Step 2: Set attack states and add visible lines/rings**

Set lance state before spawning the projectile and draw a red particle line to the target. Set lunge state before applying movement and leave a short trail. Set pressure bloom state and draw a red floor ring.

For Latent Infection, draw fungal bloom and soul-fire pressure rings before applying the existing effects.

- [ ] **Step 3: Pose and scale from states**

Stained Priestess model uses lance brace, lunge coil, and bloom-spread poses. Latent Infection model hunches/spreads during bloom and sharply contracts during pressure spike. Renderers pulse scale during these attack states.

### Task 3: Putriciel Telegraphs

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/putriciel/PutricielEntity.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/putriciel/PutricielModel.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/putriciel/PutricielRenderer.java`

- [ ] **Step 1: Add visual state/timer accessors**

Add `DATA_VISUAL_STATE`, `DATA_VISUAL_TICKS`, and `DATA_ABSOLUTION_TIMER`. Expose `getVisualState()`, `getVisualTicks()`, and `getAbsolutionWindowTimer()`.

- [ ] **Step 2: Sync absolution timer and rot nova state**

Update `aiStep()` to sync the open-window timer while absolved. Set `VISUAL_ROT_NOVA` when rot nova fires and `VISUAL_ABSOLUTION_HIT` after successful absolution hits.

- [ ] **Step 3: Add rot and absolution particles**

Draw a rot-fire floor ring for nova and add brighter particles when absolution is open. Keep the existing five-hit victory condition unchanged.

- [ ] **Step 4: Pose and scale Putriciel**

During absolution, the model slumps open and the renderer pulses brighter/larger. During rot nova, arms spread and the renderer gives a brief recoil pulse. Absolution count increases the intensity.

### Task 4: Velorum Telegraphs

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/entity/boss/saint/velorum/VelorumEntity.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/velorum/VelorumModel.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/velorum/VelorumRenderer.java`

- [ ] **Step 1: Add synced visual state/timer fields**

Add states for `VISUAL_FROST_NOVA`, `VISUAL_VEIL`, `VISUAL_SILENCE_DRAIN`, and continue using martyrdom as an exposed state.

- [ ] **Step 2: Add frost, veil, and silence helpers**

Draw a snowflake floor ring during frost nova, a dark ink cloud during veil, and soul-fire/dark tethers from blood-active players to Velorum during silence drain.

- [ ] **Step 3: Pose and scale from states**

Frost nova uses a still, braced posture; veil lowers the head and folds arms inward; silence drain raises arms and pulses; martyrdom stays rigid and bright. Renderer pulses scale subtly for each state.

### Task 5: Verification

**Files:**
- Build output only.

- [ ] **Step 1: Run compile/build verification**

Run:

```powershell
./gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`. Existing deprecation warnings are acceptable if no new compile errors appear.

- [ ] **Step 2: Summarize manual checks**

Report the exact fight paths to test in-game: Annetta Harbinger route, Stained Priestess phase, Annetta Unstained route, Putriciel absolution window, and Velorum low-HP silence drain.
