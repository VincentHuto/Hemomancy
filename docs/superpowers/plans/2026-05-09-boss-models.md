# Boss Models Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Annetta and the currently invisible boss entities render with lore-readable first-pass models and textures.

**Architecture:** Add five client-only `HumanoidModel` subclasses with custom cuboid geometry, register them in `LayerEvents`, and update existing renderers to use those model layers and dedicated texture locations. Generate opaque placeholder PNGs so the renderer path is immediately visible in game.

**Tech Stack:** NeoForge 1.21.1 client model layers, Minecraft `HumanoidMobRenderer`, Java 21, generated PNG assets.

---

### Task 1: Add Client Model Classes

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/AnnettaKnowlesModel.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/StainedPriestessModel.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/annetta/LatentAnnettaInfectionModel.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/putriciel/PutricielModel.java`
- Create: `src/main/java/com/vincenthuto/hemomancy/client/model/entity/boss/velorum/VelorumModel.java`

- [ ] Add lore-specific cuboid geometry using `HumanoidModel.createMesh`.
- [ ] Override animation only where a simple pose improves readability, especially Annetta cowering.

### Task 2: Wire Renderers And Layers

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/LayerEvents.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/annetta/AnnettaKnowlesRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/annetta/StainedPriestessRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/annetta/LatentAnnettaInfectionRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/putriciel/PutricielRenderer.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/entity/boss/velorum/VelorumRenderer.java`

- [ ] Import and register all five model layer definitions in `LayerEvents`.
- [ ] Replace vanilla `ModelLayers.PLAYER` model construction with the dedicated model layer.
- [ ] Replace all `textures/entity/blank.png` boss references with boss-specific texture paths.

### Task 3: Add Visible Texture Assets

**Files:**
- Create: `src/main/resources/assets/hemomancy/textures/entity/boss/annetta/annetta_knowles.png`
- Create: `src/main/resources/assets/hemomancy/textures/entity/boss/annetta/stained_priestess.png`
- Create: `src/main/resources/assets/hemomancy/textures/entity/boss/annetta/latent_annetta_infection.png`
- Create: `src/main/resources/assets/hemomancy/textures/entity/boss/putriciel/putriciel.png`
- Create: `src/main/resources/assets/hemomancy/textures/entity/boss/velorum/velorum.png`

- [ ] Generate opaque 64x64 PNGs with the approved color palettes.
- [ ] Inspect the PNG alpha counts to confirm no asset is accidentally transparent.

### Task 4: Verify

- [ ] Run `./gradlew.bat build`.
- [ ] Report any compile failures or manual-testing gaps.
