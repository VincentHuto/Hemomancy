#version 150

#moj_import <fog.glsl>

// A pool of thick, dark blood standing in a vessel: the Iron Brazier's bowl, the Consecrated Bloodwell's basin.
// texCoord0 spans the vessel, 0..1 across its inner diameter. The vertex colour says what is working the blood:
// red is a jet falling into its centre (ripples rings out from it), green is heat simmering it (bubbles), and blue
// is a fire burning over it (a deep glow beneath and the flame mirrored on its surface). Vertex alpha is the
// pool's opacity, so a shallow pool lets its floor show through.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float PoolSeed;
// 0 is a vessel's round pool; above 0 the blood lies loose on the ground with a lobed, slowly quivering edge.
uniform float Ragged;
// 1 fills a square vessel's mouth (a cauldron): the rim, its shadow and meniscus follow the walls instead of a circle.
uniform float Square;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 viewOffset;

out vec4 fragColor;

// The brazier pool's radius in blocks, and the flame's height above its surface (BrazierFlames).
const float POOL_RADIUS = 0.3375;
const float FLAME_HEIGHT = 0.28;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + PoolSeed * 0.013) * 43758.5453);
}

vec2 hash2(vec2 value) {
    return vec2(hash(value), hash(value + vec2(17.3, 41.9)));
}

float noise(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    vec2 curve = local * local * (3.0 - 2.0 * local);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), curve.x),
            mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), curve.x), curve.y);
}

float fbm(vec2 value) {
    float total = 0.0;
    float amplitude = 0.5;
    mat2 turn = mat2(0.78, -0.63, 0.63, 0.78);
    for (int i = 0; i < 4; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.03 + vec2(1.71, -0.46);
        amplitude *= 0.5;
    }
    return total;
}

// The blood turns slowly in its vessel, the centre swaying a little ahead of the rim.
vec2 convect(vec2 p) {
    float r = length(p);
    // The whole pool turns together; the centre only leads the rim by a bounded sway, so the shear never winds up.
    float turn = HemoTime * 0.0015 + 0.5 * sin(HemoTime * 0.006) * (1.0 - r);
    mat2 spin = mat2(cos(turn), -sin(turn), sin(turn), cos(turn));
    return spin * p * (1.0 - 0.06 * sin(HemoTime * 0.01 + r * 3.0));
}

// Simmering bubbles: each cell swells one up, holds it, then pops it into a fading ring. Returns the surface
// height they raise in x and how much of a popped ring is here in y.
vec2 bubbles(vec2 p, float heat) {
    if (heat <= 0.0) {
        return vec2(0.0);
    }
    vec2 grid = p * 3.2;
    vec2 cell = floor(grid);
    float height = 0.0;
    float ring = 0.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 id = cell + vec2(float(x), float(y));
            vec2 rnd = hash2(id);
            // Fewer bubbles toward the cooler rim, and more of them the harder the fire burns.
            if (hash(id + 5.3) > heat * (1.15 - 0.6 * length(id / 3.2))) continue;
            float period = 70.0 + rnd.x * 90.0;
            float age = fract(HemoTime / period + rnd.y);
            vec2 centre = id + 0.2 + 0.6 * rnd;
            float d = length(grid - centre);
            float size = 0.14 + 0.16 * hash(id + 9.1);
            if (age < 0.82) {
                float radius = size * smoothstep(0.0, 0.7, age);
                height = max(height, sqrt(max(radius * radius - d * d, 0.0)) * 2.2);
            } else {
                float spread = (age - 0.82) / 0.18;
                float edge = size * (1.0 + spread * 1.6);
                ring = max(ring, exp(-pow((d - edge) / 0.035, 2.0)) * (1.0 - spread));
            }
        }
    }
    return vec2(height, ring);
}

// Rings running out from where a jet falls into the centre, fading as they spread.
float ripples(float r, float jet) {
    return jet * sin(r * 26.0 - HemoTime * 0.32) * smoothstep(1.0, 0.15, r) * smoothstep(0.0, 0.12, r);
}

float relief(vec2 p, float heat, float jet) {
    vec2 c = convect(p);
    float swell = fbm(c * 2.2 + vec2(HemoTime * 0.004, 0.0)) * 0.35;
    vec2 b = bubbles(p, heat);
    return swell + b.x * 0.5 + b.y * 0.06 + ripples(length(p), jet) * 0.045;
}

void main() {
    vec2 p = texCoord0 * 2.0 - 1.0;
    vec2 heading = p / max(length(p), 0.0001);
    float lobes = fbm(heading * 1.7 + PoolSeed * 0.37 + HemoTime * 0.004) - 0.5;
    float rim = 1.0 - Ragged * (0.32 + 0.55 * lobes + 0.08 * sin(atan(p.y, p.x) * 5.0 + PoolSeed));
    float r = mix(length(p) / max(rim, 0.35), max(abs(p.x), abs(p.y)), Square);
    if (r > 1.0) {
        discard;
    }
    float jet = vertexColor.r;
    float heat = vertexColor.g;
    float fire = vertexColor.b;

    float e = 0.02;
    float h = relief(p, heat, jet);
    vec3 normal = normalize(vec3((h - relief(p + vec2(e, 0.0), heat, jet)) * 0.35, e,
            (h - relief(p + vec2(0.0, e), heat, jet)) * 0.35));
    vec2 b = bubbles(p, heat);

    // Thick blood, marbled darker and brighter as it turns over.
    vec2 c = convect(p);
    float marble = fbm(c * 3.0 + 4.0);
    vec3 color = mix(vec3(0.16, 0.006, 0.018), vec3(0.40, 0.018, 0.04), smoothstep(0.3, 0.75, marble));
    // A fire overhead heats the middle of the pool: a slow, deep glow, not a flicker.
    float under = exp(-r * r * 2.6) * (0.75 + 0.25 * fbm(c * 1.5 + HemoTime * 0.006));
    color += vec3(0.62, 0.07, 0.035) * under * (0.35 + 0.45 * heat) * fire;
    // A jet keeps fresh, brighter blood welling up where it falls.
    color += vec3(0.30, 0.012, 0.02) * jet * exp(-r * r * 4.0);
    // Bubble domes catch the glow; popped rings leave a brief bright ripple.
    color += vec3(0.55, 0.08, 0.05) * (b.x * 1.5 + b.y * 0.6) * heat;

    // Glossy surface: it mirrors the vessel's dark wall and the sky above at a glancing view, and any flame over it.
    vec3 ray = normalize(viewOffset);
    vec3 bounce = reflect(ray, normal);
    float facing = clamp(dot(-ray, normal), 0.0, 1.0);
    float fresnel = 0.04 + 0.96 * pow(1.0 - facing, 5.0);
    vec3 surroundings = mix(vec3(0.05, 0.012, 0.02), vec3(0.30, 0.20, 0.23),
            smoothstep(0.3, 0.95, bounce.y) * (1.0 - fire));
    color = mix(color, surroundings, fresnel * 0.6);
    vec3 surface = vec3(p.x, 0.0, p.y) * POOL_RADIUS;
    vec3 toFlame = normalize(vec3(0.0, FLAME_HEIGHT, 0.0) - surface);
    float flame = pow(max(dot(bounce, toFlame), 0.0), 18.0);
    color += vec3(1.0, 0.36, 0.28) * flame * (0.55 + 0.35 * heat) * fire;
    float sky = pow(max(dot(bounce, normalize(vec3(0.3, 1.0, 0.25))), 0.0), 40.0);
    color += vec3(0.7, 0.5, 0.52) * sky * (0.25 + 0.2 * (1.0 - fire));

    // The blood darkens into the vessel's shadow and climbs its wall in a thin, bright meniscus.
    color *= mix(1.0, 0.55, smoothstep(0.6, 0.96, r));
    color += vec3(0.38, 0.02, 0.04) * exp(-pow((r - 0.965) / 0.025, 2.0));

    float alpha = smoothstep(1.0, 0.985, r) * vertexColor.a * ColorModulator.a;
    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
