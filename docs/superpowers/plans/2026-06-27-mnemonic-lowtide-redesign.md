# Mnemonic Lowtide Visual Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rework `MnemonicLowtideChamberEffects` so the rendered theme reads as a dark cranial cavern with a near-black fluid lake, atmospheric horizon haze, legible Gothic ruin silhouettes, muted artery bridges, and a skull-vault ceiling that recedes overhead rather than looking like a boxy texture wall.

**Architecture:** All changes stay inside `MnemonicLowtideChamberEffects.java`, `mnemonic_lowtide_fluid.fsh`, and `ChamberSkyThemeRegistry.java` — the plumbing (shader pipeline, render types, progression logic, texture files) is unchanged. A new `renderMnemonicLowtideHorizonHaze` method is added to the effects class to provide the missing atmospheric horizon band. The source-level test in `MnemonicLowtideChamberThemeSourceTest` is updated first (TDD) before each registry or structural change.

**Tech Stack:** Java 17 / NeoForge 1.21.1, GLSL 150, Mojang Blaze3D render pipeline, existing `addLowtideBillboardQuad` / `addLowtide3DStroke` / `addLowtideBridgeDeckQuad` helpers (kept).

## Global Constraints

- Build command: `./gradlew.bat build` — must stay green after every task
- Source test: `MnemonicLowtideChamberThemeSourceTest` main runs via `./gradlew.bat test`
- Grid must stay at `int grid = 36` (tested)
- Shader must keep `uniform float HemoTime;`, `float rippleNormal`, `texture(Sampler0, fract(rippleUv))`, `ReflectionStrength` (tested)
- Bridge methods `renderMnemonicLowtideBridgeArchSegment`, `addLowtideBridgeDeckQuad`, `addLowtideBridgeRib` must remain (tested)
- `for (int face = 1; face < 6; face++)` in skull vault must remain (tested)
- `addLowtideBridgeRib` must NOT contain `-skyDistance * 0.58F` (tested)
- Fluid vertex color: `addLowtideFluidVertex(..., 12, 4, 6, 218)` — replacing `96, 55, 44`
- No changes to texture PNG files, shader JSON, shader vertex file structure, or progression/plumbing

## File Map

| File | Role |
|------|------|
| `src/test/java/.../MnemonicLowtideChamberThemeSourceTest.java` | Updated first per task before registry/structural changes |
| `src/main/java/.../MnemonicLowtideChamberEffects.java` | All render method changes + new haze pass |
| `src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_fluid.fsh` | Shader rewrite (shimmer, color, horizon fade) |
| `src/main/java/.../ChamberSkyThemeRegistry.java` | Theme config update (skybox color, nebula accent, layers) |

---

### Task 1: Update source test for registry changes

**Files:**
- Modify: `src/test/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberThemeSourceTest.java`

**Interfaces:**
- Produces: failing assertions for new skybox color, nebula accent, layer count, fog color, and haze method

- [ ] **Step 1: Update the four registry assertions and add two new ones**

In `MnemonicLowtideChamberThemeSourceTest.java`, replace these three `assertContains` calls (around lines 89–98):

```java
// OLD — replace all three:
assertContains("registry keeps lowtide base texture from being crushed by a dark tint", lowtideRegistry,
        ".skybox(0xFFFFD6A6, 0xFFE0A95F)");
assertContains("registry gives lowtide stronger red/gold nebula separation", lowtideRegistry,
        ".nebula(0x4A1710, 0x9E2F1D, 0xD39A32)");
// ...
assertContains("registry keeps lowtide biological layers sparse", registry,
        ".layers(1, 1, 1, 1)");
```

With:

```java
assertContains("registry tints lowtide skybox with dark cranial maroon", lowtideRegistry,
        ".skybox(0xFF5A1810, 0xFF380E08)");
assertContains("registry gives lowtide deep-crimson nebula accent (no gold)", lowtideRegistry,
        ".nebula(0x4A1710, 0x9E2F1D, 0x5C1208)");
assertContains("registry gives lowtide separated ivory red umber and gold shared tints", lowtideRegistry,
        ".tints(0xE7D4B0, 0xD15B3E, 0x9C7C66, 0xF2C86D)");
assertContains("registry suppresses blue-vein and neural shared layers for lowtide", registry,
        ".layers(1, 0, 1, 0)");
```

Also add these two assertions after the existing `assertContains("lowtide effects own red vein strands"` call:

```java
assertContains("lowtide effects own horizon haze band", effects,
        "renderMnemonicLowtideHorizonHaze(");
assertContains("lowtide fog override uses dark cranial maroon", effects,
        "new Vec3(0.22D, 0.09D, 0.07D)");
```

- [ ] **Step 2: Run test to confirm failures**

```
./gradlew.bat test
```

Expected: test output shows `AssertionError` for all five changed/new assertions. Everything else still passes.

---

### Task 2: Fix the fluid fragment shader

**Files:**
- Modify: `src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_fluid.fsh`

**Interfaces:**
- Consumes: existing `HemoTime`, `TideSeed`, `RippleStrength`, `ReflectionStrength` uniforms
- Produces: stripe-free fluid surface with near-black base and UV-distance horizon fade

- [ ] **Step 1: Rewrite the fragment shader**

Replace the entire file contents with:

```glsl
#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float TideSeed;
uniform float RippleStrength;
uniform float ReflectionStrength;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 worldPos;

out vec4 fragColor;

float rippleNormal(vec2 uv, float time) {
    float longWave  = sin((uv.x * 5.2  + uv.y * 1.7)  + time * 1.45 + TideSeed * 6.28318);
    float crossWave = sin((uv.y * 8.3  - uv.x * 2.6)  - time * 1.92 + TideSeed * 3.7);
    float fineWave  = sin((uv.x + uv.y) * 19.0         + time * 3.4);
    return longWave * 0.52 + crossWave * 0.34 + fineWave * 0.14;
}

// Organic shimmer using incommensurate frequencies — no repeating periodic bands
float organicShimmer(vec2 uv, float time) {
    float a = sin(uv.x *  7.13 + uv.y *  3.97 + time * 0.83) * 0.5 + 0.5;
    float b = sin(uv.x * 11.41 - uv.y *  5.23 - time * 0.61) * 0.5 + 0.5;
    float c = sin(uv.x *  2.71 + uv.y *  8.67 + time * 1.07) * 0.5 + 0.5;
    return a * b * c;
}

void main() {
    vec2 uv   = texCoord0;
    float time = HemoTime * 0.72;

    float waveA = rippleNormal(uv * 1.12        + vec2( time * 0.020, -time * 0.013), time);
    float waveB = rippleNormal(uv.yx * 0.91     + vec2(-time * 0.017,  time * 0.021), time + 1.73);
    vec2 rippleUv = uv + vec2(waveA * 0.018, waveB * 0.014) * RippleStrength;

    vec4 fluidTexture = texture(Sampler0, fract(rippleUv));
    float ripple = rippleNormal(rippleUv * 1.65, time);

    // Organic shimmer — multiply two independent FBM layers so bright spots are rare and uneven
    float shimmer = organicShimmer(rippleUv * 1.4, time * 0.40)
                  * organicShimmer(rippleUv.yx * 0.9, time * 0.30 + 1.2);
    shimmer = shimmer * shimmer * 2.2;

    // Horizon fade: raw UV centre is (1.9, 1.9) for uvScale=3.8.
    // edgeDist: 0 at centre, 1 at quad corners.
    vec2  centred = texCoord0 - vec2(1.9, 1.9);
    float edgeDist = max(abs(centred.x), abs(centred.y)) / 1.9;
    float horizonFade = 1.0 - smoothstep(0.62, 1.0, edgeDist);

    vec3 deepFluid  = vec3(0.008, 0.002, 0.004);
    vec3 bloodUmber = vec3(0.18,  0.042, 0.022);
    vec3 darkGlint  = vec3(0.45,  0.06,  0.03);

    vec3 textureColor = mix(deepFluid, fluidTexture.rgb * vec3(0.30, 0.08, 0.06), 0.55);
    vec3 color = mix(textureColor, bloodUmber, clamp(0.12 + ripple * 0.08, 0.0, 0.32));
    color += darkGlint * shimmer * ReflectionStrength * 0.10;
    color += vec3(0.40, 0.05, 0.02) * max(ripple, 0.0) * 0.038;

    float alpha = vertexColor.a * ColorModulator.a;
    alpha *= clamp(0.72 + shimmer * 0.08, 0.0, 0.88) * horizonFade;

    fragColor = linear_fog(vec4(color * vertexColor.rgb * ColorModulator.rgb, alpha),
            vertexDistance, FogStart, FogEnd, FogColor);
}
```

- [ ] **Step 2: Build and run test**

```
./gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`. The shader source test checks for `texture(Sampler0, fract(rippleUv))`, `float rippleNormal`, `ReflectionStrength`, and `uniform float HemoTime;` — all present in the new shader.

- [ ] **Step 3: Commit**

```
git add src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_fluid.fsh
git commit -m "fix(lowtide): replace fract-shimmer stripe artifacts with organic FBM shimmer, add horizon fade, darken fluid base"
```

---

### Task 3: Darken fluid surface geometry

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java`

**Interfaces:**
- Consumes: existing `addLowtideFluidVertex`, `addLowtideGroundQuad` helpers
- Produces: near-black vertex colors on fluid plane; dark maroon ground ring quads instead of orange bands

- [ ] **Step 1: Darken fluid vertex color**

In `renderMnemonicLowtideFluidSurface`, find this call (appears four times per cell, repeated in the loop):

```java
addLowtideFluidVertex(consumer, matrix, minX, y, minZ, minU, maxV, 96, 55, 44, 218);
addLowtideFluidVertex(consumer, matrix, minX, y, maxZ, minU, minV, 96, 55, 44, 218);
addLowtideFluidVertex(consumer, matrix, maxX, y, maxZ, maxU, minV, 96, 55, 44, 218);
addLowtideFluidVertex(consumer, matrix, maxX, y, minZ, maxU, maxV, 96, 55, 44, 218);
```

Replace all four (`replace_all`) — change `96, 55, 44` to `12, 4, 6`:

```java
addLowtideFluidVertex(consumer, matrix, minX, y, minZ, minU, maxV, 12, 4, 6, 218);
addLowtideFluidVertex(consumer, matrix, minX, y, maxZ, minU, minV, 12, 4, 6, 218);
addLowtideFluidVertex(consumer, matrix, maxX, y, maxZ, maxU, minV, 12, 4, 6, 218);
addLowtideFluidVertex(consumer, matrix, maxX, y, minZ, maxU, maxV, 12, 4, 6, 218);
```

- [ ] **Step 2: Darken ground ring quads**

In `renderMnemonicLowtideBlackFluid`, find the ring loop:

```java
addLowtideGroundQuad(buffer, matrix, -bandHalf, z - skyDistance * 0.012F, bandHalf,
        z + skyDistance * 0.012F, y - ring * 0.018F, 190, 111, 51, alpha);
```

Replace with:

```java
addLowtideGroundQuad(buffer, matrix, -bandHalf, z - skyDistance * 0.012F, bandHalf,
        z + skyDistance * 0.012F, y - ring * 0.018F, 38, 10, 8, alpha);
```

Then find the sigil loop:

```java
addLowtideGroundQuad(buffer, matrix, centerX - radius, centerZ - radius * 0.18F,
        centerX + radius, centerZ + radius * 0.18F, y - 0.05F, 220, 167, 91,
        28 + sigil % 3 * 10);
```

Replace with:

```java
addLowtideGroundQuad(buffer, matrix, centerX - radius, centerZ - radius * 0.18F,
        centerX + radius, centerZ + radius * 0.18F, y - 0.05F, 90, 55, 20,
        12 + sigil % 3 * 6);
```

- [ ] **Step 3: Build and test**

```
./gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```
git add src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java
git commit -m "fix(lowtide): darken fluid vertex color to near-black, replace orange ground ring quads with dark maroon"
```

---

### Task 4: Add horizon haze pass

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java`

**Interfaces:**
- Consumes: `addLowtideGroundQuad` helper; `skyDistance`, `time` parameters
- Produces: 5-layer atmospheric haze band at the fluid/wall seam; fog floor plane just above fluid; new method `renderMnemonicLowtideHorizonHaze` that `renderMnemonicLowtideDepthEffects` calls

- [ ] **Step 1: Add the haze method**

Insert the following method before `renderMnemonicLowtideMemoryFragments` in `MnemonicLowtideChamberEffects.java`:

```java
static void renderMnemonicLowtideHorizonHaze(PoseStack poseStack, Tesselator tesselator, float time,
        float skyDistance) {
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    Matrix4f matrix = poseStack.last().pose();
    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    float half = skyDistance * 1.55F;

    // 5 stacked haze quads rising from the fluid surface upward to hide the hard seam
    for (int layer = 0; layer < 5; layer++) {
        float t = layer / 4.0F;
        float yBottom = -skyDistance * Mth.lerp(t, 0.660F, 0.510F);
        float yTop    = -skyDistance * Mth.lerp(t, 0.630F, 0.480F);
        int r = (int) Mth.lerp(t, 55.0F, 18.0F);
        int g = (int) Mth.lerp(t, 14.0F,  4.0F);
        int b = (int) Mth.lerp(t, 10.0F,  3.0F);
        int alpha = (int) Mth.lerp(t, 22.0F, 8.0F);
        addLowtideGroundQuad(buffer, matrix, -half, yBottom, half, yTop, 0.0F, r, g, b, alpha);
    }

    // Fog floor: wide shallow quad just above the fluid plane to blur the transition
    float fogFloorY = -skyDistance * 0.630F;
    addLowtideGroundQuad(buffer, matrix, -half, -half, half, half, fogFloorY, 28, 7, 5, 18);

    // Gentle lateral haze on the inner side walls to soften the box corners
    for (int side = 0; side < 4; side++) {
        float angle = side * Mth.HALF_PI;
        float cosA  = Mth.cos(angle);
        float sinA  = Mth.sin(angle);
        float wallDist = skyDistance * 0.96F;
        float wallX0 = cosA * wallDist - sinA * half;
        float wallZ0 = sinA * wallDist + cosA * half;
        float wallX1 = cosA * wallDist + sinA * half;
        float wallZ1 = sinA * wallDist - cosA * half;
        float topY   = -skyDistance * 0.30F;
        float botY   = -skyDistance * 0.68F;
        buffer.addVertex(matrix, wallX0, botY, wallZ0).setColor(22, 5, 4, 20);
        buffer.addVertex(matrix, wallX0, topY, wallZ0).setColor(22, 5, 4, 0);
        buffer.addVertex(matrix, wallX1, topY, wallZ1).setColor(22, 5, 4, 0);
        buffer.addVertex(matrix, wallX1, botY, wallZ1).setColor(22, 5, 4, 20);
    }

    BufferUploader.drawWithShader(buffer.buildOrThrow());
}
```

- [ ] **Step 2: Wire the call into `renderMnemonicLowtideDepthEffects`**

Find in `renderMnemonicLowtideDepthEffects`:

```java
renderMnemonicLowtideBlackFluid(poseStack, tesselator, time, skyDistance, theme);
renderMnemonicLowtideNerveRoots(poseStack, tesselator, time, skyDistance);
```

Replace with:

```java
renderMnemonicLowtideBlackFluid(poseStack, tesselator, time, skyDistance, theme);
renderMnemonicLowtideHorizonHaze(poseStack, tesselator, time, skyDistance);
renderMnemonicLowtideNerveRoots(poseStack, tesselator, time, skyDistance);
```

- [ ] **Step 3: Build and run test**

```
./gradlew.bat test
```

Expected: the `"lowtide effects own horizon haze band"` assertion now passes. Build must be `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```
git add src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java
git commit -m "feat(lowtide): add horizon haze pass to eliminate hard fluid/wall seam"
```

---

### Task 5: Redesign skull vault (colors, alpha, curvature, top-face vascular)

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java`

**Interfaces:**
- Consumes: existing `addLowtideFaceStroke` helper; face-rotation loop unchanged
- Produces: dark maroon → pale bone color gradient per band; alpha range 6–36; 24-segment arching bands; secondary thin vascular strokes on face 1 (top)

- [ ] **Step 1: Replace the skull vault render method**

Replace the entire body of `renderMnemonicLowtideSkullVault` with:

```java
static void renderMnemonicLowtideSkullVault(PoseStack poseStack, Tesselator tesselator, float time,
        float skyDistance, ChamberSkyTheme theme) {
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

    for (int face = 1; face < 6; face++) {
        poseStack.pushPose();
        ChamberOfWillRenderHelpers.rotateSkyFace(poseStack, face);
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
        Random random = new Random(0x51A7B00DL + face * 19397L);
        float depth = -skyDistance * 0.948F;
        int bands = 6;

        // Primary bands: dark maroon at bandT=0, pale bone at bandT=1
        for (int band = 0; band < bands; band++) {
            float bandT = band / (float) Math.max(1, bands - 1);
            float centerZ = Mth.lerp(bandT, -0.72F, 0.70F) * skyDistance;
            float arc     = Mth.lerp(random.nextFloat(), 0.22F, 0.42F) * skyDistance;
            float phase   = face * 0.63F + band * 1.19F;
            float drift   = Mth.sin(time * 0.00042F + phase) * skyDistance * 0.018F;
            float startX  = -skyDistance * Mth.lerp(random.nextFloat(), 0.72F, 0.97F);
            float endX    =  skyDistance * Mth.lerp(random.nextFloat(), 0.72F, 0.97F);
            float prevX   = startX;
            float prevZ   = centerZ + drift + Mth.sin(phase) * skyDistance * 0.045F;

            // Gradient: horizon bands deep maroon, zenith bands pale bone
            int red   = (int) Mth.lerp(bandT, 75.0F,  195.0F);
            int green = (int) Mth.lerp(bandT, 18.0F,  172.0F);
            int blue  = (int) Mth.lerp(bandT, 12.0F,  140.0F);
            // Side faces get much lower alpha to recede; top face (face==1) gets full weight
            float faceMult = (face == 1) ? 1.0F : 0.38F;
            int alpha = (int) Mth.clamp(
                    (Mth.lerp(bandT, 28.0F, 9.0F) + random.nextFloat() * 8.0F) * faceMult,
                    6.0F, 36.0F);

            int segments = 24;
            for (int segment = 1; segment <= segments; segment++) {
                float t = segment / (float) segments;
                float x   = Mth.lerp(t, startX, endX);
                // Arc bows in Z (depth) direction — creates concave arch impression
                float bow     = Mth.sin(t * Mth.PI) * arc;
                float wrinkle = Mth.sin(t * Mth.TWO_PI * (1.4F + random.nextFloat() * 0.7F)
                        + phase + time * 0.0009F) * skyDistance * 0.018F;
                float z = centerZ + drift - bow + wrinkle;
                addLowtideFaceStroke(buffer, matrix, prevX, prevZ, x, z, depth,
                        skyDistance * Mth.lerp(bandT, 0.0048F, 0.0018F), red, green, blue, alpha);
                prevX = x;
                prevZ = z;
            }
        }

        // Top face only: thin secondary vascular network, dark crimson
        if (face == 1) {
            Random vascRandom = new Random(0xC0AGVALT + face * 7331L);
            for (int vessel = 0; vessel < 12; vessel++) {
                float vPhase = vessel * 0.52F;
                float vStartX = Mth.lerp(vascRandom.nextFloat(), -0.88F, 0.88F) * skyDistance;
                float vStartZ = Mth.lerp(vascRandom.nextFloat(), -0.68F, 0.68F) * skyDistance;
                float vLen    = Mth.lerp(vascRandom.nextFloat(), 0.18F, 0.46F) * skyDistance;
                float vAngle  = vascRandom.nextFloat() * Mth.TWO_PI;
                float prevX = vStartX;
                float prevZ = vStartZ;
                int vSegments = 4 + vascRandom.nextInt(4);
                for (int vs = 1; vs <= vSegments; vs++) {
                    float vt = vs / (float) vSegments;
                    float bend = Mth.sin(vt * Mth.PI + vPhase + time * 0.0007F) * skyDistance * 0.055F;
                    float nx = vStartX + Mth.cos(vAngle) * vLen * vt + Mth.cos(vAngle + Mth.HALF_PI) * bend;
                    float nz = vStartZ + Mth.sin(vAngle) * vLen * vt + Mth.sin(vAngle + Mth.HALF_PI) * bend;
                    int va = (int) Mth.clamp(28.0F * (1.0F - vt * 0.55F), 6.0F, 28.0F);
                    addLowtideFaceStroke(buffer, matrix, prevX, prevZ, nx, nz, depth - 0.005F,
                            skyDistance * 0.0015F, 90, 22, 16, va);
                    prevX = nx;
                    prevZ = nz;
                }
            }
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        poseStack.popPose();
    }
}
```

Note: the vascular random seed `0xC0AGVALT` is not valid hex — use `0xC0A6471EL` instead (pick any unique long seed).

- [ ] **Step 2: Build and test**

```
./gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`. Test assertion `"lowtide skull vault stays off the bottom-fluid face"` still matches `for (int face = 1; face < 6; face++)`.

- [ ] **Step 3: Commit**

```
git add src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java
git commit -m "fix(lowtide): skull vault color gradient maroon→bone, lower alpha, 24-seg arching bands, top-face vascular overlay"
```

---

### Task 6: Fix artery bridges (darken, thin, push back)

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java`

**Interfaces:**
- Consumes: `renderMnemonicLowtideBridgeArchSegment`, `lowtideBridgeSide`, `addLowtide3DStroke`, `addLowtideBridgeRib` (all unchanged)
- Produces: darker/thinner/more-distant bridges; max alpha 48

- [ ] **Step 1: Update bridge geometry and colors**

In `renderMnemonicLowtideArteryBridges`, replace the bridge setup block and segment loop. Find:

```java
for (int bridge = 0; bridge < 5; bridge++) {
    float angle = bridge / 5.0F * Mth.TWO_PI + 0.32F;
    float startDistance = skyDistance * (0.54F + bridge * 0.030F);
    float endDistance = skyDistance * (1.06F + bridge * 0.020F);
    float baseY = skyDistance * (0.070F + bridge * 0.032F);
    float bridgeHalfWidth = skyDistance * (0.0105F + bridge * 0.0008F);
    float bridgeDepth = skyDistance * (0.0140F + bridge * 0.0010F);
```

Replace with:

```java
for (int bridge = 0; bridge < 5; bridge++) {
    float angle = bridge / 5.0F * Mth.TWO_PI + 0.32F;
    float startDistance = skyDistance * (0.68F + bridge * 0.028F);
    float endDistance   = skyDistance * (1.30F + bridge * 0.018F);
    float baseY         = skyDistance * (0.070F + bridge * 0.032F);
    float bridgeHalfWidth = skyDistance * (0.0062F + bridge * 0.0005F);
    float bridgeDepth     = skyDistance * (0.0088F + bridge * 0.0006F);
```

Then find the alpha and color call inside the segment loop:

```java
int alpha = (int) Mth.clamp(Mth.lerp(t, 110.0F, 24.0F), 22.0F, 118.0F);
renderMnemonicLowtideBridgeArchSegment(buffer, matrix, previous, next, bridgeHalfWidth, bridgeDepth,
        182, 46, 28, alpha);
```

Replace with:

```java
int alpha = (int) Mth.clamp(Mth.lerp(t, 52.0F, 14.0F), 12.0F, 48.0F);
renderMnemonicLowtideBridgeArchSegment(buffer, matrix, previous, next, bridgeHalfWidth, bridgeDepth,
        88, 48, 28, alpha);
```

Then find the rail strand calls:

```java
addLowtide3DStroke(buffer, matrix, (float) leftStart.x, (float) leftStart.y, (float) leftStart.z,
        (float) leftEnd.x, (float) leftEnd.y, (float) leftEnd.z, skyDistance * 0.0018F,
        228, 160, 66, alpha / 2);
addLowtide3DStroke(buffer, matrix, (float) rightStart.x, (float) rightStart.y, (float) rightStart.z,
        (float) rightEnd.x, (float) rightEnd.y, (float) rightEnd.z, skyDistance * 0.0014F,
        130, 38, 28, alpha / 2);
```

Replace with:

```java
addLowtide3DStroke(buffer, matrix, (float) leftStart.x, (float) leftStart.y, (float) leftStart.z,
        (float) leftEnd.x, (float) leftEnd.y, (float) leftEnd.z, skyDistance * 0.0012F,
        155, 105, 38, alpha / 4);
addLowtide3DStroke(buffer, matrix, (float) rightStart.x, (float) rightStart.y, (float) rightStart.z,
        (float) rightEnd.x, (float) rightEnd.y, (float) rightEnd.z, skyDistance * 0.0009F,
        60, 18, 14, alpha / 4);
```

- [ ] **Step 2: Build and test**

```
./gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`. Test assertions `"lowtide artery bridges have actual width"`, `"lowtide artery bridges are placed in the sky band"` still match since `float bridgeHalfWidth = skyDistance *` and `float baseY = skyDistance *` are unchanged. `"lowtide artery bridge ribs should not drop supports into the lake"` still passes since rib method body is untouched.

- [ ] **Step 3: Commit**

```
git add src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java
git commit -m "fix(lowtide): bridges darkened to aged crimson, thinned 40%, pushed further back, max alpha 48"
```

---

### Task 7: Redesign outpost silhouettes (Gothic spire profiles)

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java`

**Interfaces:**
- Consumes: `addLowtideBillboardQuad`, `addLowtide3DStroke` helpers
- Produces: 5-quad Gothic spire profiles per outpost; visible dark umber silhouettes at greater distance

- [ ] **Step 1: Replace the outpost render method body**

Replace the entire body of `renderMnemonicLowtideOutposts` with:

```java
static void renderMnemonicLowtideOutposts(PoseStack poseStack, Tesselator tesselator, float time,
        float skyDistance) {
    RenderSystem.setShader(GameRenderer::getPositionColorShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    Matrix4f matrix = poseStack.last().pose();
    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION_COLOR);
    Random random = new Random(0x0F710571L);
    float baseY = -skyDistance * 0.58F;
    for (int outpost = 0; outpost < 13; outpost++) {
        float angle    = outpost / 13.0F * Mth.TWO_PI + Mth.lerp(random.nextFloat(), -0.14F, 0.14F);
        float distance = skyDistance * Mth.lerp(random.nextFloat(), 0.72F, 1.25F);
        Vec3 center    = new Vec3(Mth.cos(angle) * distance, baseY, Mth.sin(angle) * distance);
        Vec3 right     = new Vec3(-Mth.sin(angle), 0.0D, Mth.cos(angle)).normalize();
        Vec3 up        = new Vec3(0.0D, 1.0D, 0.0D);
        float scale    = skyDistance * Mth.lerp(random.nextFloat(), 0.022F, 0.058F);
        int alpha      = (int) Mth.clamp(Mth.lerp(distance / skyDistance, 68.0F, 28.0F), 24.0F, 72.0F);

        // Gothic spire: 5-layer stacked profile
        // 1. Wide base platform
        addLowtideBillboardQuad(buffer, matrix,
                center.add(up.scale(scale * 0.08F)), right, up,
                scale * 0.62F, scale * 0.08F,
                52, 34, 22, alpha);
        // 2. Main tower body
        addLowtideBillboardQuad(buffer, matrix,
                center.add(up.scale(scale * 0.62F)), right, up,
                scale * 0.36F, scale * 0.54F,
                46, 30, 20, alpha);
        // 3. Upper tower (narrower)
        addLowtideBillboardQuad(buffer, matrix,
                center.add(up.scale(scale * 1.35F)), right, up,
                scale * 0.22F, scale * 0.42F,
                52, 34, 22, alpha);
        // 4. Narrow spire shaft
        addLowtideBillboardQuad(buffer, matrix,
                center.add(up.scale(scale * 1.98F)), right, up,
                scale * 0.09F, scale * 0.52F,
                58, 38, 24, alpha);
        // 5. Spire tip — 3D stroke to a pointed top
        Vec3 shaftTop   = center.add(up.scale(scale * 2.50F));
        Vec3 spirePoint = center.add(up.scale(scale * 2.95F));
        addLowtide3DStroke(buffer, matrix,
                (float) shaftTop.x,   (float) shaftTop.y,   (float) shaftTop.z,
                (float) spirePoint.x, (float) spirePoint.y, (float) spirePoint.z,
                scale * 0.055F, 62, 42, 26, alpha / 2);

        // Small flanking turret on 2 out of 3 outposts
        if (outpost % 3 != 2) {
            Vec3 turretCenter = center.add(right.scale(scale * 0.58F));
            addLowtideBillboardQuad(buffer, matrix,
                    turretCenter.add(up.scale(scale * 0.78F)), right, up,
                    scale * 0.14F, scale * 0.72F,
                    42, 26, 18, alpha * 2 / 3);
            Vec3 turretTop   = turretCenter.add(up.scale(scale * 1.52F));
            Vec3 turretPoint = turretCenter.add(up.scale(scale * 1.88F));
            addLowtide3DStroke(buffer, matrix,
                    (float) turretTop.x,   (float) turretTop.y,   (float) turretTop.z,
                    (float) turretPoint.x, (float) turretPoint.y, (float) turretPoint.z,
                    scale * 0.040F, 55, 34, 20, alpha / 3);
        }
    }
    BufferUploader.drawWithShader(buffer.buildOrThrow());
}
```

- [ ] **Step 2: Build and test**

```
./gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`. Test assertion `"lowtide effects own outpost silhouettes"` still matches `renderMnemonicLowtideOutposts(`.

- [ ] **Step 3: Commit**

```
git add src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java
git commit -m "feat(lowtide): replace outpost rectangles with 5-quad Gothic spire profiles, pushed to greater distance"
```

---

### Task 8: Tighten nerve roots, memory fragments, and fog override

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java`

**Interfaces:**
- Consumes: existing `addLowtide3DStroke`, `addLowtideBillboardQuad`, `addLowtideDisc` helpers
- Produces: fewer/smaller nerve roots; fewer/smaller memory fragments; darker fog color

- [ ] **Step 1: Reduce nerve root count and cool color**

In `renderMnemonicLowtideNerveRoots`, find:

```java
for (int root = 0; root < 34; root++) {
```

Replace with:

```java
for (int root = 0; root < 28; root++) {
```

Find the color line:

```java
addLowtide3DStroke(buffer, matrix, prevX, prevY, prevZ, nextX, nextY, nextZ, width,
        225, 210, 174, alpha);
```

Replace with:

```java
addLowtide3DStroke(buffer, matrix, prevX, prevY, prevZ, nextX, nextY, nextZ, width,
        190, 168, 128, alpha);
```

- [ ] **Step 2: Reduce memory fragment count and size**

In `renderMnemonicLowtideMemoryFragments`, find:

```java
for (int fragment = 0; fragment < 26; fragment++) {
```

Replace with:

```java
for (int fragment = 0; fragment < 14; fragment++) {
```

Find the size line:

```java
float halfWidth = skyDistance * Mth.lerp(random.nextFloat(), 0.010F, 0.022F);
```

Replace with:

```java
float halfWidth = skyDistance * Mth.lerp(random.nextFloat(), 0.007F, 0.015F);
```

Find the color lines:

```java
int red = fragment % 5 == 0 ? 232 : 197;
int green = fragment % 5 == 0 ? 215 : 163;
int blue = fragment % 5 == 0 ? 167 : 111;
```

Replace with:

```java
int red   = fragment % 5 == 0 ? 210 : 182;
int green = fragment % 5 == 0 ? 188 : 160;
int blue  = fragment % 5 == 0 ? 140 : 115;
```

- [ ] **Step 3: Darken fog color override**

Find:

```java
return new Vec3(0.38D, 0.21D, 0.14D).scale(fogBrightness);
```

Replace with:

```java
return new Vec3(0.22D, 0.09D, 0.07D).scale(fogBrightness);
```

- [ ] **Step 4: Build and run test**

```
./gradlew.bat test
```

Expected: `BUILD SUCCESSFUL`, `"lowtide fog override uses dark cranial maroon"` assertion now passes (it checks for `new Vec3(0.22D, 0.09D, 0.07D)`).

- [ ] **Step 5: Commit**

```
git add src/main/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberEffects.java
git commit -m "fix(lowtide): reduce nerve roots to 28, fragments to 14, darken fog override to deep cranial maroon"
```

---

### Task 9: Update registry theme config + run full test

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/ChamberSkyThemeRegistry.java`
- Modify: `src/test/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberThemeSourceTest.java` (test already updated in Task 1 — verify still matches)

**Interfaces:**
- Consumes: `ChamberSkyTheme.builder` fluent API
- Produces: dark cranial maroon skybox tint, deep-crimson nebula accent, blue-vein and neural layers suppressed

- [ ] **Step 1: Update registry mnemonicLowtide builder**

In `ChamberSkyThemeRegistry.java`, find the mnemonicLowtide builder block:

```java
ChamberSkyTheme mnemonicLowtide = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_MNEMONIC_LOWTIDE)
        .textures(MNEMONIC_LOWTIDE_SKY, MNEMONIC_LOWTIDE_CLOUDS, MNEMONIC_LOWTIDE_CLOUDS, DEFAULT_NOISE)
        .skybox(0xFFFFD6A6, 0xFFE0A95F)
        .nebula(0x4A1710, 0x9E2F1D, 0xD39A32)
        .tints(0xE7D4B0, 0xD15B3E, 0x9C7C66, 0xF2C86D)
        .pulse(0.32F)
        .motion(0.54F)
        .layers(1, 1, 1, 1)
        .toggles(true, false, true, true)
        .build();
```

Replace with:

```java
ChamberSkyTheme mnemonicLowtide = ChamberSkyTheme.builder(ChamberOfWillManager.THEME_MNEMONIC_LOWTIDE)
        .textures(MNEMONIC_LOWTIDE_SKY, MNEMONIC_LOWTIDE_CLOUDS, MNEMONIC_LOWTIDE_CLOUDS, DEFAULT_NOISE)
        .skybox(0xFF5A1810, 0xFF380E08)
        .nebula(0x4A1710, 0x9E2F1D, 0x5C1208)
        .tints(0xE7D4B0, 0xD15B3E, 0x9C7C66, 0xF2C86D)
        .pulse(0.32F)
        .motion(0.54F)
        .layers(1, 0, 1, 0)
        .toggles(true, false, true, true)
        .build();
```

- [ ] **Step 2: Run full test suite**

```
./gradlew.bat test
```

Expected: `BUILD SUCCESSFUL`, all assertions pass. The four updated assertions from Task 1 now match:
- `".skybox(0xFF5A1810, 0xFF380E08)"` ✓
- `".nebula(0x4A1710, 0x9E2F1D, 0x5C1208)"` ✓
- `".layers(1, 0, 1, 0)"` ✓
- `"renderMnemonicLowtideHorizonHaze("` ✓ (from Task 4)
- `"new Vec3(0.22D, 0.09D, 0.07D)"` ✓ (from Task 8)

- [ ] **Step 3: Run full build**

```
./gradlew.bat build
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```
git add src/main/java/com/vincenthuto/hemomancy/client/render/world/ChamberSkyThemeRegistry.java
git add src/test/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideChamberThemeSourceTest.java
git commit -m "fix(lowtide): dark cranial maroon skybox tint, deep-crimson nebula accent, suppress blue-vein and neural shared layers"
```

---

## Self-Review

**Spec coverage check:**

| Spec section | Task |
|---|---|
| Fluid shader — FBM shimmer, darken, horizon fade | Task 2 |
| Fluid vertex color `(12,4,6)` | Task 3 |
| Ground ring quads darkened | Task 3 |
| Horizon haze pass (new method) | Task 4 |
| Skull vault — color gradient, alpha, curvature, top vascular | Task 5 |
| Bridges — darken, thin, push back, alpha 48 | Task 6 |
| Outpost Gothic spire profiles | Task 7 |
| Neural shared layers suppressed | Task 9 |
| Nerve roots reduced | Task 8 |
| Memory fragments reduced | Task 8 |
| Fog color override darkened | Task 8 |
| Registry skybox, nebula, layers | Task 9 |
| Tests updated | Task 1 + verified in Task 9 |

**Placeholder scan:** No TBDs, no "similar to Task N" references. All code blocks complete.

**Type consistency:** `addLowtideFaceStroke`, `addLowtideBillboardQuad`, `addLowtide3DStroke`, `addLowtideGroundQuad`, `renderMnemonicLowtideBridgeArchSegment`, `addLowtideBridgeDeckQuad`, `addLowtideBridgeRib` — names used consistently throughout.

**Vascular seed fix:** Task 5 notes that `0xC0AGVALT` is invalid hex — use `0xC0A6471EL` (explicit fix noted inline).
