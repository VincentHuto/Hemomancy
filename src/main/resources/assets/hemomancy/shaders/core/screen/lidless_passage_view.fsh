#version 150

// The crossing view: the void over the whole screen, with the doorway windows cut out of it. Each pixel casts
// its ray from the camera's projection, turns it into passage space (+z toward the exit), and reads the void. A
// ray that hits the exit rectangle ahead (at Length - Progress) or the entry rectangle behind (at Progress) shows
// the scene instead: the camera really is in the door, so the world it sees in that direction is the right world.
// In seed mode (Mode 1) there is no exit window: a black hole sits on the axis ahead, growing as Hole.x, pulling
// the void and the eyes into itself, and the whole view warps by Hole.y; what was drawn into the focus mask (the
// Seed and its tendrils) is composited on top. A gateway crossing is Mode 1 too, with nothing in the hole.

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform sampler2D Sampler2;
uniform float Live;
uniform float EyeShape;
uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float Progress;
uniform float Length;
uniform float DoorSeed;
uniform vec3 CamRight;
uniform vec3 CamUp;
uniform vec3 CamForward;
uniform float FovTan;
uniform float Aspect;
uniform float EyeHeight;
uniform float Mode;
uniform vec2 Hole;
uniform float ChamberTurn;
uniform float Theme;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

#moj_import <hemomancy:noise/monolith_noise.glsl>
#moj_import <hemomancy:noise/lidless_void.glsl>
#moj_import <hemomancy:noise/vein_hash3.glsl>
#moj_import <hemomancy:noise/vein_hash1.glsl>
#moj_import <hemomancy:noise/vein_noise.glsl>
#moj_import <hemomancy:noise/vein_fbm.glsl>
#moj_import <hemomancy:noise/vein_cellular.glsl>
#moj_import <hemomancy:noise/lidless_theme.glsl>

// How much of a doorway rectangle at distance `away` along `sign` the ray sees: 1 inside, feathered at the edges.
float window(vec3 ray, float away, float sign, out float rim) {
    rim = 0.0;
    float along = ray.z * sign;
    if (along <= 0.001) return 0.0;
    float t = max(away, 0.05) / along;
    vec2 hit = ray.xy * t;
    float feather = 0.03 + away * 0.004;
    float inside = smoothstep(0.5, 0.5 - feather, abs(hit.x))
        * smoothstep(-EyeHeight, -EyeHeight + feather, hit.y)
        * smoothstep(2.0 - EyeHeight, 2.0 - EyeHeight - feather, hit.y);
    float edge = min(min(0.5 - abs(hit.x), hit.y + EyeHeight), 2.0 - EyeHeight - hit.y);
    rim = smoothstep(0.06, 0.0, edge) * inside;
    return inside;
}

void main() {
    vec2 uv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    vec2 ndc = uv * 2.0 - 1.0;
    float warp = Hole.y * Mode;
    // The pull on the eye: a pincushion that tightens with progress, and a slow wobble of the whole frame.
    float r2 = dot(ndc, ndc);
    vec2 warped = ndc * (1.0 + warp * 0.35 * r2);
    float wobble = 0.02 * warp * sin(HemoTime * 0.1 + DoorSeed);
    warped = vec2(warped.x * cos(wobble) - warped.y * sin(wobble), warped.x * sin(wobble) + warped.y * cos(wobble));
    vec3 rayCam = normalize(vec3(warped.x * Aspect * FovTan, warped.y * FovTan, 1.0));
    vec3 ray = normalize(CamRight * rayCam.x + CamUp * rayCam.y + CamForward * rayCam.z);
    float twist = Progress / max(Length, 1.0);

    // The black hole on the axis: rays near it are bent toward it before the void is read, so the void streams in.
    float holeRadius = Hole.x;
    // How far a ray is from the hole's axis: round for the Seed walk's black hole; for a gateway walk the eye's own
    // diamond (EyeShape 1), a little wider than tall like the panel, so the walk carries the window on.
    float fromAxis = mix(acos(clamp(ray.z, -1.0, 1.0)),
            abs(atan(ray.x, ray.z)) * 0.9 + abs(atan(ray.y, ray.z)), EyeShape);
    vec3 voidRay = ray;
    float horizon = 0.0;
    float ring = 0.0;
    float band = 0.0;
    if (Mode > 0.5 && holeRadius > 0.0) {
        float pull = clamp(holeRadius * holeRadius / max(fromAxis * fromAxis, 1e-4), 0.0, 1.0) * warp;
        voidRay = normalize(mix(ray, vec3(0.0, 0.0, 1.0), pull * 0.6 * smoothstep(3.0 * holeRadius, holeRadius, fromAxis)));
        horizon = smoothstep(holeRadius, holeRadius * 0.94, fromAxis);
        band = (fromAxis - holeRadius) / (holeRadius * 0.6);
        float theta = atan(ray.y, ray.x);
        // Accretion: a soft band of spiral arms turning around the horizon, brightest where it meets the dark.
        float arms = 0.6 + 0.4 * sin(theta * 3.0 + fromAxis * 40.0 - HemoTime * 0.06);
        float grain = 0.8 + 0.2 * lidlessNoise3(vec3(theta * 2.0, fromAxis * 12.0, HemoTime * 0.01));
        ring = exp(-band * band * 2.2) * arms * grain * step(holeRadius, fromAxis);
    }
    vec3 color = lidlessVoid(voidRay, HemoTime, DoorSeed, twist, 0.0);
    if (Mode > 0.5 && holeRadius > 0.0) {
        float thin = smoothstep(holeRadius, 2.0 * holeRadius, fromAxis);
        color = mix(lidlessVoid(voidRay, HemoTime, DoorSeed + 7.0, twist, 0.0) * 0.6, color, max(thin, 0.25));
        // Nearing the Chamber the passage turns toward its colours, by path (noise/lidless_theme), spreading out from
        // the hole. ChamberTurn is how far: it grows along the Seed walk, is whole on the gateway in and fades on the way out.
        float chamber = ChamberTurn;
        float spread = 1.0 - smoothstep(holeRadius * 1.2, holeRadius * 1.2 + 0.3 + 2.6 * chamber, fromAxis);
        float turned = chamber * max(spread, chamber * 0.8);
        color = mix(color, lidlessThemeSky(color, ray, Theme, HemoTime, DoorSeed), turned);
        // Added over dark void the band glows; over a pale sky adding would wash it out, so there it replaces.
        vec3 glowing = color + vec3(0.82, 0.05, 0.02) * ring * 0.9;
        vec3 banded = mix(color, lidlessThemeRing(Theme), clamp(ring * 1.05, 0.0, 1.0));
        color = mix(glowing, banded, chamber);
        // A halo outside the band, as the Chamber's light gathers around the hole.
        float halo = exp(-max(band - 0.5, 0.0) * max(band - 0.5, 0.0) * 0.5) * step(holeRadius, fromAxis);
        color += lidlessThemeHalo(Theme) * halo * (1.0 - clamp(ring, 0.0, 1.0)) * chamber * chamber * 0.14;
        // Inside the horizon: the Chamber's core colour, or on a walk in through a gateway the Chamber itself, drawn
        // into Sampler2 from the walk's camera and read by screen position, so the hole is a window coming closer.
        vec3 core = mix(vec3(0.0), lidlessThemeCore(Theme), chamber);
        if (Live > 0.5) core = texture(Sampler2, clamp(uv, 0.001, 0.999)).rgb;
        color = mix(color, core, horizon);
    }

    float rimAhead = 0.0;
    float rimBehind = 0.0;
    float ahead = Mode > 0.5 ? 0.0 : window(ray, Length - Progress, 1.0, rimAhead);
    float behind = window(ray, Progress, -1.0, rimBehind);
    float inside = max(ahead, behind);
    vec3 world = texture(Sampler0, clamp(uv, 0.001, 0.999)).rgb;
    color = mix(color, world, inside);
    color += vec3(0.62, 0.015, 0.010) * max(rimAhead, rimBehind) * 0.8;

    // The focus holds the Seed (opaque) and its glow (additive, thin alpha): either counts as presence.
    vec4 focus = texture(Sampler1, clamp(uv, 0.001, 0.999));
    float presence = clamp(max(focus.a, dot(focus.rgb, vec3(1.2))), 0.0, 1.0);
    color = mix(color, max(focus.rgb, color * (1.0 - presence)), presence * Mode);
    fragColor = vec4(color, 1.0) * vertexColor * ColorModulator;
}
