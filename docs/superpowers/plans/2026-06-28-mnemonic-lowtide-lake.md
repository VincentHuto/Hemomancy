# Mnemonic Lowtide Lake Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first Mnemonic Lowtide visual pass: a nearby but unreachable glossy black/beige/red subfloor lake with shader vertex waves and fragment noise.

**Architecture:** Lowtide gets its own `MnemonicLowtideChamberEffects` strategy registered through `ChamberSkyThemeRegistry`. The sky strategy owns the theme identity, while a public static world-stage render entry draws the depth-tested tessellated lake during `AFTER_TRANSLUCENT_BLOCKS` so the Chamber floor occludes it. Custom shader plumbing follows the existing `ShaderInit` + `HemoRenderTypes` pattern.

**Tech Stack:** Java 21, NeoForge 1.21.1 render events, Minecraft `RenderType`/`MultiBufferSource`, GLSL 150 core shaders, existing main-method source guard tests.

---

## File Structure

- Modify `src/test/java/com/vincenthuto/hemomancy/client/render/world/ChamberThemeEffectsSourceTest.java`: update moved Chamber renderer paths and assert Lowtide uses its own effects strategy.
- Create `src/test/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideLakeSourceTest.java`: guard shader registration, render type state, world-stage hook, renderer constants, shader source behavior, and docs.
- Modify `src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java`: add and register `MNEMONIC_LOWTIDE_LAKE`.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java`: add `mnemonicLowtideLake(...)`.
- Create `src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.json`: shader definition and uniforms.
- Create `src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.vsh`: vertex wave displacement.
- Create `src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.fsh`: fbm liquid texture, red/beige/black material, highlights.
- Create `src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java`: render-only tessellated lake mesh.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java`: register the new Lowtide strategy.
- Modify `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`: call the Lowtide lake renderer from `AFTER_TRANSLUCENT_BLOCKS`.
- Modify `docs/HEMOMANCY_REFERENCE.md`: replace the blank Lowtide note with the implemented subfloor tide description.

### Task 1: Red Source Guards For Lowtide Ownership

**Files:**
- Modify: `src/test/java/com/vincenthuto/hemomancy/client/render/world/ChamberThemeEffectsSourceTest.java`
- Create: `src/test/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideLakeSourceTest.java`

- [ ] **Step 1: Update the existing Chamber effects guard to the moved package and Lowtide strategy**

In `ChamberThemeEffectsSourceTest.java`, replace the path constants at the top with:

```java
private static final Path PACKAGE = Path.of(
        "src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill");
private static final Path RENDERER = PACKAGE.resolve("ChamberOfWillEffects.java");
private static final Path HELPERS = PACKAGE.resolve("ChamberOfWillRenderHelpers.java");
private static final Path REGISTRY = PACKAGE.resolve("ChamberSkyThemeRegistry.java");
private static final Path QLIPHOTH_EFFECTS = PACKAGE.resolve("QliphothCommunionChamberEffects.java");
private static final Path SILENT_ARCHON_EFFECTS = PACKAGE.resolve("SilentArchonChamberEffects.java");
private static final Path LOWTIDE_EFFECTS = PACKAGE.resolve("MnemonicLowtideChamberEffects.java");
private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");
```

After reading `silentArchonEffects`, add:

```java
String lowtideEffects = Files.readString(LOWTIDE_EFFECTS).replace("\r\n", "\n");
```

Add the Lowtide file assertion near the other theme file assertions:

```java
assertFileExists("mnemonic lowtide effects exists", "MnemonicLowtideChamberEffects.java");
```

Replace the old Lowtide registry assertion:

```java
assertContains("mnemonic lowtide uses blank theme effects", registry,
        "new BlankChamberThemeEffects(mnemonicLowtide)");
```

with these assertions:

```java
assertContains("mnemonic lowtide effects are registered", registry,
        "new MnemonicLowtideChamberEffects(mnemonicLowtide)");
assertNotContains("mnemonic lowtide no longer uses blank theme effects", registry,
        "new BlankChamberThemeEffects(mnemonicLowtide)");
assertContains("lowtide effects own lake render entry", lowtideEffects,
        "public static void renderLake(RenderLevelStageEvent event)");
assertContains("lowtide effects use lowtide lake render type", lowtideEffects,
        "HemoRenderTypes.mnemonicLowtideLake(");
assertContains("lowtide effects place lake below floor", lowtideEffects,
        "ChamberOfWillManager.FLOOR_Y - LAKE_DEPTH");
```

- [ ] **Step 2: Create the Lowtide lake source guard**

Create `MnemonicLowtideLakeSourceTest.java` with:

```java
package com.vincenthuto.hemomancy.client.render.world;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class MnemonicLowtideLakeSourceTest {
    private static final Path LOWTIDE_EFFECTS = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java");
    private static final Path REGISTRY = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java");
    private static final Path CLIENT_EVENTS = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java");
    private static final Path SHADER_INIT = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java");
    private static final Path RENDER_TYPES = Path.of(
            "src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java");
    private static final Path SHADER_JSON = Path.of(
            "src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.json");
    private static final Path SHADER_VERTEX = Path.of(
            "src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.vsh");
    private static final Path SHADER_FRAGMENT = Path.of(
            "src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.fsh");
    private static final Path REFERENCE = Path.of("docs/HEMOMANCY_REFERENCE.md");

    private MnemonicLowtideLakeSourceTest() {
    }

    public static void main(String[] args) throws IOException {
        assertFileExists("lowtide effects class", LOWTIDE_EFFECTS);
        assertFileExists("lowtide shader json", SHADER_JSON);
        assertFileExists("lowtide vertex shader", SHADER_VERTEX);
        assertFileExists("lowtide fragment shader", SHADER_FRAGMENT);

        String lowtideEffects = read(LOWTIDE_EFFECTS);
        String registry = read(REGISTRY);
        String clientEvents = read(CLIENT_EVENTS);
        String shaderInit = read(SHADER_INIT);
        String renderTypes = read(RENDER_TYPES);
        String shaderJson = read(SHADER_JSON);
        String vertexShader = read(SHADER_VERTEX);
        String fragmentShader = read(SHADER_FRAGMENT);
        String reference = read(REFERENCE);

        assertContains("registry registers dedicated lowtide effects", registry,
                "new MnemonicLowtideChamberEffects(mnemonicLowtide)");
        assertContains("client event imports lowtide effects", clientEvents,
                "import com.vincenthuto.hemomancy.client.render.world.chamberofwill.MnemonicLowtideChamberEffects;");
        assertContains("client event renders lake after translucent blocks", clientEvents,
                "MnemonicLowtideChamberEffects.renderLake(event);");

        assertContains("shader init declares lowtide lake shader", shaderInit,
                "MNEMONIC_LOWTIDE_LAKE");
        assertContains("shader init uses world lowtide shader path", shaderInit,
                "Hemomancy.rloc(\"world/mnemonic_lowtide_lake\")");
        assertContains("shader init registers lowtide lake shader", shaderInit,
                "registerShader(event, MNEMONIC_LOWTIDE_LAKE.createInstance(provider));");

        assertContains("render type method exists", renderTypes,
                "public static RenderType mnemonicLowtideLake(");
        assertContains("render type uses lowtide shader shard", renderTypes,
                "ShaderInit.MNEMONIC_LOWTIDE_LAKE.getShard()");
        assertContains("render type keeps depth test enabled", renderTypes,
                "RenderType.LEQUAL_DEPTH_TEST");
        assertContains("render type uses color-only write mask", renderTypes,
                "RenderType.COLOR_WRITE");

        assertContains("renderer only runs in chamber dimension", lowtideEffects,
                "level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)");
        assertContains("renderer only runs for lowtide theme", lowtideEffects,
                "ChamberOfWillManager.THEME_MNEMONIC_LOWTIDE.equals(ChamberSkyThemeRegistry.activeTheme().id())");
        assertContains("renderer uses synced chamber radius", lowtideEffects,
                "ChamberOfWillClientData.radius()");
        assertContains("renderer snaps to chamber spacing", lowtideEffects,
                "ChamberOfWillManager.CHAMBER_SPACING");
        assertContains("renderer uses requested lake depth", lowtideEffects,
                "private static final float LAKE_DEPTH = 1.25F;");

        assertContains("shader json points to vertex program", shaderJson,
                "\"vertex\": \"hemomancy:world/mnemonic_lowtide_lake\"");
        assertContains("shader json exposes wave strength", shaderJson,
                "\"name\": \"WaveStrength\"");
        assertContains("shader json exposes gloss strength", shaderJson,
                "\"name\": \"GlossStrength\"");

        assertContains("vertex shader displaces y", vertexShader,
                "surfacePosition.y += waveLift;");
        assertContains("vertex shader damps edges", vertexShader,
                "smoothstep(0.0, EdgeFade, edge)");
        assertContains("fragment shader uses fbm", fragmentShader,
                "float fbm(vec2 value)");
        assertContains("fragment shader names parchment highlights", fragmentShader,
                "parchmentHighlight");
        assertContains("fragment shader uses glossy highlight", fragmentShader,
                "GlossStrength");

        assertContains("reference documents lowtide subfloor tide", reference,
                "Mnemonic Lowtide");
        assertContains("reference documents unreachable nearby lake", reference,
                "nearby but unreachable");
    }

    private static String read(Path path) throws IOException {
        return Files.readString(path).replace("\r\n", "\n");
    }

    private static void assertFileExists(String label, Path path) {
        if (!Files.exists(path)) {
            throw new AssertionError(label + ": missing " + path);
        }
    }

    private static void assertContains(String label, String text, String expected) {
        if (!text.contains(expected)) {
            throw new AssertionError(label + ": missing " + expected);
        }
    }
}
```

- [ ] **Step 3: Compile and run the new guards to verify red**

Run:

```powershell
./gradlew.bat compileTestJava
java -cp build/classes/java/test com.vincenthuto.hemomancy.client.render.world.ChamberThemeEffectsSourceTest
java -cp build/classes/java/test com.vincenthuto.hemomancy.client.render.world.MnemonicLowtideLakeSourceTest
```

Expected: `compileTestJava` passes. The two Java guard runs fail because `MnemonicLowtideChamberEffects.java` and shader resources do not exist yet, and the registry still uses `BlankChamberThemeEffects`.

- [ ] **Step 4: Commit the red guards**

Use path-limited staging because this worktree currently contains unrelated chamber renderer moves:

```powershell
git add -- src/test/java/com/vincenthuto/hemomancy/client/render/world/ChamberThemeEffectsSourceTest.java src/test/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideLakeSourceTest.java
git commit -m "test: add mnemonic lowtide lake guards"
```

### Task 2: Shader Registration And Render Type

**Files:**
- Modify: `src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java`
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.json`
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.vsh`
- Create: `src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.fsh`

- [ ] **Step 1: Add the shader holder**

In `ShaderInit.java`, add this field after `MYCELIAL_CRUCIBLE_BASIN`:

```java
public static ShaderHolder MNEMONIC_LOWTIDE_LAKE = new ShaderHolder(
        Hemomancy.rloc("world/mnemonic_lowtide_lake"),
        DefaultVertexFormat.POSITION_TEX_COLOR, "HemoTime", "LakeSeed", "WaveStrength", "NoiseScale",
        "GlossStrength", "EdgeFade");
```

In the shader registration method, add this line immediately after `MYCELIAL_CRUCIBLE_BASIN` registration:

```java
registerShader(event, MNEMONIC_LOWTIDE_LAKE.createInstance(provider));
```

- [ ] **Step 2: Add the Lowtide lake render type**

In `HemoRenderTypes.java`, add this method after `mycelialCrucibleBasin(...)`:

```java
public static RenderType mnemonicLowtideLake(float gameTime, float lakeSeed, float waveStrength,
        float noiseScale, float glossStrength, float edgeFade) {
    RenderStateShard.TexturingStateShard uniforms = new RenderStateShard.TexturingStateShard(
            "mnemonic_lowtide_lake_uniforms",
            () -> {
                ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_LAKE.getInstance().get();
                setUniform(shader, "HemoTime", gameTime);
                setUniform(shader, "LakeSeed", lakeSeed);
                setUniform(shader, "WaveStrength", waveStrength);
                setUniform(shader, "NoiseScale", noiseScale);
                setUniform(shader, "GlossStrength", glossStrength);
                setUniform(shader, "EdgeFade", edgeFade);
            },
            () -> {
                ShaderInstance shader = ShaderInit.MNEMONIC_LOWTIDE_LAKE.getInstance().get();
                if (shader instanceof ExtendedShaderInstance extended) {
                    extended.setUniformDefaults();
                }
            });
    return RenderType.create("mnemonic_lowtide_lake",
            DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 16384, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(ShaderInit.MNEMONIC_LOWTIDE_LAKE.getShard())
                    .setTexturingState(uniforms)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderType.COLOR_WRITE)
                    .setCullState(RenderType.NO_CULL)
                    .setLightmapState(RenderType.NO_LIGHTMAP)
                    .createCompositeState(false));
}
```

- [ ] **Step 3: Create the shader JSON**

Create `mnemonic_lowtide_lake.json` with:

```json
{
  "blend": {
    "func": "add",
    "srcrgb": "srcalpha",
    "dstrgb": "1-srcalpha"
  },
  "vertex": "hemomancy:world/mnemonic_lowtide_lake",
  "fragment": "hemomancy:world/mnemonic_lowtide_lake",
  "attributes": [
    "Position",
    "Color",
    "UV0"
  ],
  "samplers": [],
  "uniforms": [
    { "name": "ModelViewMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
    { "name": "ProjMat", "type": "matrix4x4", "count": 16, "values": [ 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0 ] },
    { "name": "ColorModulator", "type": "float", "count": 4, "values": [ 1.0, 1.0, 1.0, 1.0 ] },
    { "name": "FogStart", "type": "float", "count": 1, "values": [ 0.0 ] },
    { "name": "FogEnd", "type": "float", "count": 1, "values": [ 1.0 ] },
    { "name": "FogColor", "type": "float", "count": 4, "values": [ 0.0, 0.0, 0.0, 0.0 ] },
    { "name": "FogShape", "type": "int", "count": 1, "values": [ 0 ] },
    { "name": "HemoTime", "type": "float", "count": 1, "values": [ 0.0 ] },
    { "name": "LakeSeed", "type": "float", "count": 1, "values": [ 0.0 ] },
    { "name": "WaveStrength", "type": "float", "count": 1, "values": [ 0.18 ] },
    { "name": "NoiseScale", "type": "float", "count": 1, "values": [ 8.0 ] },
    { "name": "GlossStrength", "type": "float", "count": 1, "values": [ 0.85 ] },
    { "name": "EdgeFade", "type": "float", "count": 1, "values": [ 0.16 ] }
  ]
}
```

- [ ] **Step 4: Create the vertex shader**

Create `mnemonic_lowtide_lake.vsh` with:

```glsl
#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float HemoTime;
uniform float LakeSeed;
uniform float WaveStrength;
uniform float NoiseScale;
uniform float EdgeFade;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out float waveLiftAmount;

float wave(vec2 uv, vec2 axis, float scale, float speed, float phase) {
    return sin(dot(uv, axis) * scale + HemoTime * speed + LakeSeed * phase);
}

void main() {
    vec2 uv = UV0;
    float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeDamping = smoothstep(0.0, EdgeFade, edge);
    float broad = wave(uv, normalize(vec2(1.0, 0.31)), 13.0, 0.052, 0.017);
    float cross = wave(uv, normalize(vec2(-0.42, 1.0)), 21.0, -0.073, 0.029);
    float small = wave(uv + broad * 0.018, normalize(vec2(0.74, -0.57)), 37.0, 0.111, 0.041);
    float waveLift = (broad * 0.58 + cross * 0.32 + small * 0.10) * WaveStrength * edgeDamping;

    vec3 surfacePosition = Position;
    surfacePosition.y += waveLift;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexDistance = fog_distance(surfacePosition, FogShape);
    vertexColor = Color;
    texCoord0 = uv;
    waveLiftAmount = waveLift;
}
```

- [ ] **Step 5: Create the fragment shader**

Create `mnemonic_lowtide_lake.fsh` with:

```glsl
#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float LakeSeed;
uniform float NoiseScale;
uniform float GlossStrength;
uniform float EdgeFade;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float waveLiftAmount;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + LakeSeed * 0.019) * 43758.5453);
}

float noise(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    vec2 curve = local * local * (3.0 - 2.0 * local);
    float a = hash(cell);
    float b = hash(cell + vec2(1.0, 0.0));
    float c = hash(cell + vec2(0.0, 1.0));
    float d = hash(cell + vec2(1.0, 1.0));
    return mix(mix(a, b, curve.x), mix(c, d, curve.x), curve.y);
}

float fbm(vec2 value) {
    float total = 0.0;
    float amplitude = 0.5;
    mat2 turn = mat2(0.82, -0.57, 0.57, 0.82);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.04 + vec2(3.17, -1.41);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 uv = texCoord0;
    float time = HemoTime * 0.045;
    vec2 centered = uv - vec2(0.5);
    vec2 drift = vec2(time * 0.13, -time * 0.09);
    float low = fbm(centered * NoiseScale + drift);
    float high = fbm(centered * (NoiseScale * 2.35) + vec2(-time * 0.21, time * 0.17) + low * 0.85);
    vec2 warped = centered + vec2(
            sin((centered.y + low) * 8.0 + time * 2.4),
            cos((centered.x - high) * 7.0 - time * 2.1)
    ) * 0.035;

    float redStream = smoothstep(0.52, 0.88, fbm(warped * 9.5 + vec2(time * 0.31, -time * 0.22)));
    float blackPocket = smoothstep(0.30, 0.74, fbm(warped * 4.0 - vec2(time * 0.08, time * 0.11)));
    float parchmentHighlight = smoothstep(0.72, 0.96, fbm(warped * 17.0 + vec2(-time * 0.44, time * 0.37)));
    float glossLine = pow(max(0.0, high), 7.0) * GlossStrength;

    vec3 blackBase = vec3(0.006, 0.004, 0.004);
    vec3 red = vec3(0.48, 0.018, 0.012);
    vec3 deepRed = vec3(0.18, 0.006, 0.004);
    vec3 parchment = vec3(0.78, 0.62, 0.39);

    vec3 color = mix(blackBase, deepRed, blackPocket * 0.62);
    color = mix(color, red, redStream * 0.72);
    color += parchment * parchmentHighlight * 0.28;
    color += vec3(1.0, 0.84, 0.58) * glossLine * (0.10 + parchmentHighlight * 0.22);
    color += red * abs(waveLiftAmount) * 0.85;

    float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeFade = smoothstep(0.0, EdgeFade, edge);
    float alpha = (0.72 + redStream * 0.12 + parchmentHighlight * 0.08 + glossLine * 0.08) * edgeFade;
    alpha *= vertexColor.a * ColorModulator.a;

    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
```

- [ ] **Step 6: Run focused guards**

Run:

```powershell
./gradlew.bat compileJava compileTestJava
java -cp build/classes/java/test com.vincenthuto.hemomancy.client.render.world.MnemonicLowtideLakeSourceTest
```

Expected: compilation may still fail if `ClientEvents` imports the renderer before Task 3 exists. If compilation passes, the source guard still fails on missing `MnemonicLowtideChamberEffects`, missing event hook, missing registry swap, and docs.

- [ ] **Step 7: Commit shader plumbing**

```powershell
git add -- src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.json src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.vsh src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.fsh
git commit -m "feat: add mnemonic lowtide lake shader"
```

### Task 3: Lowtide Renderer And World-Stage Hook

**Files:**
- Create: `src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java`
- Modify: `src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java`

- [ ] **Step 1: Create the dedicated Lowtide effects class**

Create `MnemonicLowtideChamberEffects.java` with:

```java
package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.data.ChamberOfWillClientData;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class MnemonicLowtideChamberEffects extends AbstractChamberThemeEffects {
    private static final float LAKE_DEPTH = 1.25F;
    private static final float LAKE_MARGIN = 12.0F;
    private static final int LAKE_SUBDIVISIONS = 56;
    private static final float WAVE_STRENGTH = 0.18F;
    private static final float NOISE_SCALE = 8.0F;
    private static final float GLOSS_STRENGTH = 0.85F;
    private static final float EDGE_FADE = 0.16F;

    MnemonicLowtideChamberEffects(ChamberSkyTheme theme) {
        super(theme);
    }

    public static void renderLake(RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
            return;
        }
        if (!ChamberOfWillManager.THEME_MNEMONIC_LOWTIDE.equals(ChamberSkyThemeRegistry.activeTheme().id())) {
            return;
        }

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        float time = level.getGameTime() + partialTick;
        Vec3 camera = event.getCamera().getPosition();
        int chamberZ = Mth.floor(camera.z / ChamberOfWillManager.CHAMBER_SPACING)
                * ChamberOfWillManager.CHAMBER_SPACING;
        float lakeSeed = Math.floorMod(chamberZ, 10000);
        float centerX = 0.5F;
        float centerY = ChamberOfWillManager.FLOOR_Y - LAKE_DEPTH;
        float centerZ = chamberZ + 0.5F;
        float halfSpan = ChamberOfWillClientData.radius() + LAKE_MARGIN;

        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        RenderType renderType = HemoRenderTypes.mnemonicLowtideLake(time, lakeSeed, WAVE_STRENGTH, NOISE_SCALE,
                GLOSS_STRENGTH, EDGE_FADE);
        VertexConsumer consumer = buffer.getBuffer(renderType);
        PoseStack.Pose pose = event.getPoseStack().last();
        emitLakeGrid(consumer, pose.pose(), camera, centerX, centerY, centerZ, halfSpan);
        buffer.endBatch(renderType);
    }

    private static void emitLakeGrid(VertexConsumer consumer, Matrix4f matrix, Vec3 camera, float centerX,
            float centerY, float centerZ, float halfSpan) {
        float step = (halfSpan * 2.0F) / LAKE_SUBDIVISIONS;
        for (int xIndex = 0; xIndex < LAKE_SUBDIVISIONS; xIndex++) {
            float x0 = centerX - halfSpan + step * xIndex;
            float x1 = x0 + step;
            float u0 = (float) xIndex / LAKE_SUBDIVISIONS;
            float u1 = (float) (xIndex + 1) / LAKE_SUBDIVISIONS;
            for (int zIndex = 0; zIndex < LAKE_SUBDIVISIONS; zIndex++) {
                float z0 = centerZ - halfSpan + step * zIndex;
                float z1 = z0 + step;
                float v0 = (float) zIndex / LAKE_SUBDIVISIONS;
                float v1 = (float) (zIndex + 1) / LAKE_SUBDIVISIONS;

                emitLakeVertex(consumer, matrix, camera, x0, centerY, z1, u0, v1);
                emitLakeVertex(consumer, matrix, camera, x1, centerY, z1, u1, v1);
                emitLakeVertex(consumer, matrix, camera, x1, centerY, z0, u1, v0);
                emitLakeVertex(consumer, matrix, camera, x0, centerY, z0, u0, v0);
            }
        }
    }

    private static void emitLakeVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 camera, float x, float y,
            float z, float u, float v) {
        consumer.addVertex(matrix, (float) (x - camera.x), (float) (y - camera.y), (float) (z - camera.z))
                .setUv(u, v)
                .setColor(255, 255, 255, 230);
    }
}
```

- [ ] **Step 2: Register the dedicated Lowtide effects strategy**

In `ChamberSkyThemeRegistry.java`, replace:

```java
register(mnemonicLowtide, new BlankChamberThemeEffects(mnemonicLowtide));
```

with:

```java
register(mnemonicLowtide, new MnemonicLowtideChamberEffects(mnemonicLowtide));
```

- [ ] **Step 3: Hook the lake into the depth-aware world stage**

In `ClientEvents.java`, add the explicit import near the existing Chamber import:

```java
import com.vincenthuto.hemomancy.client.render.world.chamberofwill.MnemonicLowtideChamberEffects;
```

Inside the `AFTER_TRANSLUCENT_BLOCKS` block in `renderLevelLastEvent`, call the lake renderer before other overlays:

```java
if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
    MnemonicLowtideChamberEffects.renderLake(event);
    float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
    CardinalRiteBoundaryRenderer.render(event.getPoseStack(), partialTick);
```

- [ ] **Step 4: Run focused guards**

Run:

```powershell
./gradlew.bat compileJava compileTestJava
java -cp build/classes/java/test com.vincenthuto.hemomancy.client.render.world.ChamberThemeEffectsSourceTest
java -cp build/classes/java/test com.vincenthuto.hemomancy.client.render.world.MnemonicLowtideLakeSourceTest
```

Expected: compilation passes. `ChamberThemeEffectsSourceTest` passes. `MnemonicLowtideLakeSourceTest` still fails only on the `docs/HEMOMANCY_REFERENCE.md` Lowtide wording.

- [ ] **Step 5: Commit the renderer and hook**

```powershell
git add -- src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java
git commit -m "feat: render mnemonic lowtide lake"
```

### Task 4: Reference Documentation

**Files:**
- Modify: `docs/HEMOMANCY_REFERENCE.md`

- [ ] **Step 1: Update the Lowtide documentation**

Replace the paragraph that currently says Lowtide is a blank reserved theme:

```markdown
The Mnemonic Lowtide progression slot currently exists as a reserved transitional Chamber theme between the default refuge state and later Archon/Qliphoth branches. Its previous bespoke visual pass has been intentionally removed so the theme can be rebuilt from a clean blank-skybox baseline.
```

with:

```markdown
The Mnemonic Lowtide progression slot is the first transitional disturbance between the default refuge state and later Archon/Qliphoth branches. It keeps the sky restrained, but renders a nearby but unreachable glossy black, red, and parchment-beige subfloor tide below the blood-wood refuge. The lake is renderer-only: it adds no fluid, collision, hazard, or movement rule, and the Chamber floor occludes it naturally so it reads as blood-memory pressing up beneath the player's private room rather than a fallable ocean.
```

- [ ] **Step 2: Run the focused source guards again**

```powershell
./gradlew.bat compileTestJava
java -cp build/classes/java/test com.vincenthuto.hemomancy.client.render.world.ChamberThemeEffectsSourceTest
java -cp build/classes/java/test com.vincenthuto.hemomancy.client.render.world.MnemonicLowtideLakeSourceTest
```

Expected: both source guards pass.

- [ ] **Step 3: Commit the documentation update**

```powershell
git add -- docs/HEMOMANCY_REFERENCE.md
git commit -m "docs: describe mnemonic lowtide lake"
```

### Task 5: Full Verification And Visual Check

**Files:**
- Verify all files from Tasks 1-4.

- [ ] **Step 1: Run the full test task**

```powershell
./gradlew.bat test
```

Expected: Gradle exits `BUILD SUCCESSFUL`.

- [ ] **Step 2: Run Java compilation**

```powershell
./gradlew.bat compileJava
```

Expected: Gradle exits `BUILD SUCCESSFUL`.

- [ ] **Step 3: Run the client**

```powershell
./gradlew.bat runClient
```

Expected: the dev client starts without shader load errors.

- [ ] **Step 4: Visual validation in-game**

In the client, enter the Chamber of Will and run:

```text
/hemo chamber theme set mnemonic_lowtide
```

Check these points:

- The lake appears below the refuge floor, close enough to feel present.
- The blood-wood floor/platform occludes the lake, so it feels unreachable.
- The surface has visible vertex wave motion, not only scrolling texture.
- The fragment material reads glossy black with red streams and beige/parchment highlights.
- There are no hard exposed mesh edges at the current Chamber radius.
- There is no collision, fluid behavior, falling hazard, or rescue-rule change.

- [ ] **Step 5: Final status check**

```powershell
git status --short
```

Expected: only unrelated pre-existing worktree changes remain. If files from this plan are dirty after verification tuning, commit those path-limited changes:

```powershell
git add -- src/test/java/com/vincenthuto/hemomancy/client/render/world/ChamberThemeEffectsSourceTest.java src/test/java/com/vincenthuto/hemomancy/client/render/world/MnemonicLowtideLakeSourceTest.java src/main/java/com/vincenthuto/hemomancy/common/init/ShaderInit.java src/main/java/com/vincenthuto/hemomancy/client/render/HemoRenderTypes.java src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/MnemonicLowtideChamberEffects.java src/main/java/com/vincenthuto/hemomancy/client/render/world/chamberofwill/ChamberSkyThemeRegistry.java src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.json src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.vsh src/main/resources/assets/hemomancy/shaders/core/world/mnemonic_lowtide_lake.fsh docs/HEMOMANCY_REFERENCE.md
git commit -m "chore: tune mnemonic lowtide lake"
```

## Self-Review Notes

- Spec coverage: tasks cover dedicated Lowtide strategy, depth-aware world-stage rendering, tessellated renderer-only mesh, shader registration, vertex displacement, fragment noise, source guards, docs, and run-client visual validation.
- Scope check: this plan only builds the first lake feature. It excludes parchment props, towers, full sky/weather overhaul, gameplay fluids, collision, hazards, and progression changes.
- Type consistency: the render type uses `mnemonicLowtideLake(...)`, shader holder uses `MNEMONIC_LOWTIDE_LAKE`, shader uniforms are `HemoTime`, `LakeSeed`, `WaveStrength`, `NoiseScale`, `GlossStrength`, and `EdgeFade`, and the renderer calls the same names.
