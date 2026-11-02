# Stained Priestess Tendril Form Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans (user preference: native, one final reviewer). Steps use checkbox (`- [ ]`) syntax.

**Goal:** Rebuild the Priestess's wings, head and body infestation as procedural writhing tendrils.

**Architecture:** The pure `PriestessTendrilRules` produces centre lines (in pixels, bone space) from pose weights,
time and motion. `TendrilTube` turns them into lit textured tubes, glow shells and webbing. `StainedPriestessTendrilLayer`
walks the model's bone chain and draws everything. The box rig keeps only the body. Textures come from
`stained_priestess.py`.

**Tech Stack:** NeoForge 1.21.1 `RenderLayer`, HutosLib `HLRenderTypeInit.TENDRIL_GLOW`, vanilla
`entityCutoutNoCull`/`entityTranslucent`/`eyes`, JUnit 5, the Python Pillow painter.

**Spec:** `docs/superpowers/specs/2026-10-16-stained-priestess-tendril-form-design.md`

## Global Constraints

- Model space is in pixels with +Y down, -Z front and +X her left. The renderer scale is 1.2.
- Spread wing reach is 4.5–5.5 blocks from her centre line.
- No copper/brass. Base tint is white; the textures carry the colour.
- Visual only: no AI, damage or drop changes.

## Review Focus

1. Pose blend jumps when the target flips every frame (walking on and off), giving visible popping. Test: `blendingIsRateLimited`.
2. Large wings culled at screen edges. The existing `getBoundingBoxForCulling` inflate(5.5) must still cover them. Test: `spreadSpanAboutFiveBlocks` bounds the reach.
3. NaN frames from zero-length tangents (coincident points). Test: `integrateNeverProducesNaN`.
4. A dead Priestess's tendrils must droop, not freeze mid-whip. This is covered by the `Motion.slack` droop term (in review).
5. Per-frame allocation churn in the layer. Reuse arrays where possible (in review).

---

### Task 1: PriestessTendrilRules + tests
Create `client/render/entity/boss/annetta/PriestessTendrilRules.java` and `src/test/.../PriestessTendrilRulesTest.java`.
- Pose tables: arm heading, finger heading range, length scale per pose.
- Weights: `stepWeights(float[] w, Pose target, float dt)` moves at most dt/6 per tick toward the target.
- `integrate(...)`: a Mnemonist-style curvature-wave integrator returning a `float[]` of centre points.
- Tests: `spreadSpanAboutFiveBlocks`, `shieldTipsInFront`, `foldedTipsBehind`, `rootsPinned`, `blendingIsRateLimited`,
  `integrateNeverProducesNaN`.

### Task 2: TendrilTube + textures
Create `TendrilTube` (parallel-transported frames, an 8-sided lit textured core, a glow shell and a webbing strip). Paint
`stained_priestess_tendril.png`, `stained_priestess_webbing.png` and `stained_priestess_eye.png` in `stained_priestess.py`.

### Task 3: Model trim + layer
Remove the wing, spike and head-tendril parts from the rig. Keep `thorn_core` as a dark knot core, plus `neck_stump`
and the tatters. Add public bone-chain helpers. Write `StainedPriestessTendrilLayer` (wings, webbing, drips, head knot
and eyes, arm wraps, chest spill, back burst and hem drag; motion hooks) and register it in `StainedPriestessRenderer`.
Update `AnnettaModelSourceTest` (parts gone, layer registered).

### Task 4: Review and polish
Run `runAnnettaReviewClient` for the folded, spread and shield poses, add a head close-up scene, and iterate until it
looks right. Update the docs. One final reviewer.
