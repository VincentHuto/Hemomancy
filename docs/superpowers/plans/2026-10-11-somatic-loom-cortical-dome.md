# Somatic Loom Cortical Dome Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (inline). Steps use checkbox (`- [ ]`) syntax.

**Goal:** While a Cortical (stage 2) loom weaves, a dome of the weaver's own Chamber of Will sky rises around it, and the echo stands inside. Within the dome's radius the world beyond is replaced by that Chamber sky. Whatever is inside the dome stays real (the loom, the floor, the weaver, the threads, the echo). This holds from inside and outside.

**Architecture:** This is a stencil portal, drawn at `RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS`: all opaque terrain, including the loom's baked body, is in the depth buffer, and entities, block entities and translucent effects have not yet drawn.
1. The dome's **back faces** (cull front) are depth-tested against the scene and write stencil = 1 with no colour or depth. This marks the pixels where the view ray reaches the far dome wall without hitting anything inside the dome. The rule works the same from inside and outside the dome.
2. The weaver's theme draws inside that stencil through `ChamberSkyThemeRegistry.effectsById(theme).renderSky(context)`, the same camera-centred skybox the Chamber dimension draws. Depth uses `ALWAYS`, so the far sky can never lose to the world it replaces.
3. The back faces are drawn again, depth only, with depth func `ALWAYS` inside the stencil. The covered pixels then carry the dome wall's depth, so entities and translucent blocks beyond the dome stay hidden, while those inside draw normally over the sky.
4. A faint theme-coloured membrane shell and a ground ring mark the boundary. Stencil and GL state are restored.

**Spike result:** themes render camera-centred (boxes at `skyDistance` 128 under a rotation-only model-view). So they cannot be bent into a bounded sphere directly, but they can be windowed. `NonEuclideanHallwayRenderer` already proves the main render target's stencil (`enableStencil()`) on this stack.

**Tech Stack:** Java 21, NeoForge 1.21.1 client rendering (`RenderSystem`, GL11 stencil, `VertexBuffer`), JUnit 5, GameTests, native capture.

**Spec:** `docs/superpowers/specs/2026-10-11-somatic-loom-weave-design.md` (Cortical perk, and the "Cortical dome" spike note).

**Depends on:** Plan 2 (stage 2 looms).

## Global Constraints

- **Client-only rendering.** The server only records which theme the weaver owns; it never changes the Chamber or the player.
- **Theme source:** `ChamberOfWillManager.get(server).getChamberState(weaver).skyTheme()`. The theme is captured when the weave starts and when an orphaned weave is adopted, then saved and synced through the loom's update tag.
  - Unknown or missing theme ids fall back to `ChamberSkyThemeRegistry.DEFAULT`.
  - The client must not use `ChamberOfWillClientData.skyTheme()`: it holds only the local player's theme and is cleared on level unload.
- **Gating:** only stage 2 looms, only while weaving or in the climax. The dome rises over 40 weave ticks and holds while paused (dimmed, not removed). During the climax it collapses into the fell with the echo's sink. At most the two nearest domes within 64 blocks render.
- **No vanilla particles.** No new packets: the update tag carries the theme.
- **State hygiene:** always clear and disable the stencil, restore the colour mask, depth mask, depth func and cull, reset the shader colour and the blend function. A throw inside a theme must not leave stencil enabled, so wrap the work in `try/finally`.
- Do not commit.

## Review Focus

1. From outside, the far side of the dome shows Chamber sky. The loom, floor and echo inside stay visible, and so do the terrain and mobs in front of the dome.
2. From inside, the sky surrounds the weaver and the ground inside stays. Nothing from beyond the dome draws over the sky afterwards.
3. Two Cortical looms weaving side by side each get their own weaver's theme.
4. GL state stays clean after the dome renders. Other stencil users (the folded hallway) and later passes are unaffected.
5. A save or reload mid-weave keeps the theme, with no flash of the default.

---

### Task 1: Capture and sync the weaver's theme

**Files:** `SomaticLoomBlockEntity`. It gains a field `@Nullable ResourceLocation weaverTheme` (tag `weaverTheme`).
- The theme is set in `beginWeave` and `adoptNearestWeaver` through `ChamberOfWillManager`.
- It is cleared in `clearActiveRitual`.
- It is exposed as `getWeaverTheme()`.

**Test:** `SomaticLoomWeavingGameTests.aCorticalWeaveCarriesTheWeaversChamberTheme`.
- A fresh weaver gives `will_default`.
- After `setSkyThemeOverride(weaver, qliphoth_communion)` a second weave gives `qliphoth_communion`.
- The value round-trips through save and load.

### Task 2: Dome rules

**Files:** `SomaticLoomEffectRules`.
- `DOME_RADIUS = 7.0f`.
- `domeRadius(int stage, boolean weaving, float weaveTicks, boolean climax, float climaxProgress)`. It is 0 below stage 2 or when not weaving. It eases from 0 to the full radius over 40 ticks, holds, then in the climax shrinks with the echo's sink (progress 0.7 to 1.0).
- `domeAlpha(paused)`.

**Tests:** in `SomaticLoomEffectRulesTest`.

### Task 3: The portal renderer

**Files:**
- Create `client/render/world/chamberofwill/SomaticLoomDomeRenderer.java`, in the package, for access to `ChamberThemeEffects`.
  - `track(SomaticLoomBlockEntity)` is called from the loom's `clientTick`. It keeps a weak list and prunes removed, non-weaving and non-stage-2 looms.
  - `render(RenderLevelStageEvent)` handles `AFTER_CUTOUT_BLOCKS`. It takes the nearest two tracked looms within 64 blocks and renders each as in the Architecture section.
  - The UV sphere mesh is cached in a `VertexBuffer`, 48 × 32, built once from the unit sphere with outward normals.
  - The model-view for the sky is the event's model-view matrix, with no camera translation (the sky is camera-centred).
  - The dome mesh pose is `translate(center - cameraPos)` and `scale(r)`.
  - `ChamberThemeRenderContext` is built with:
    - the event's level, ticks, partial tick, model-view, camera and projection;
    - `foggy = false` and a no-op fog;
    - a fresh `PoseStack` multiplied by the model-view, and `Tesselator.getInstance()`;
    - the theme and `time = (gameTime + partial) * motionMultiplier`;
    - `skyDistance` 128;
    - `membranePulse` computed as in `ChamberOfWillEffects`;
    - `qliphothPomesConsumed` 0.
- Membrane: a translucent position-colour shell at radius `r`, alpha 0.12 with a fresnel-like rim (brighter near the silhouette), in the theme's `nebulaSecondary` and `capillaryTint`. Plus a ground ring disc band at the floor (y = loom y) in the theme's `bloodTint`.
- Modify `ClientEvents.renderLevelLastEvent` to call `SomaticLoomDomeRenderer.render(event)` at `AFTER_CUTOUT_BLOCKS`.
- Modify `SomaticLoomBlockEntity.clientTick` to call `track` (client only, through a `DistExecutor`-free static hook kept on the client class).

**Tests:**
- A source contract in `ModelFirstTileRendererResourceTest` covers:
  - the stencil enable/disable pair;
  - the `finally` restore;
  - the `AFTER_CUTOUT_BLOCKS` stage;
  - `effectsById(`;
  - the `track(` hook.
- A native capture covers the default and Qliphoth themes, inside and outside, by day and night, with a weaving Cortical loom.

### Task 4: Docs and capture review

- `docs/STATION_UPGRADES.md`: the Somatic Loom stages, Cortical bullet.
- `docs/HEMOMANCY_REFERENCE.md`: the loom row and §25.7 rendering notes.
- `wiki/Harbinger-Path.md`: loom tiers.
- The validation note.
