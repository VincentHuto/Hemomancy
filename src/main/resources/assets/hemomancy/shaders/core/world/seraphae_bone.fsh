#version 150
#moj_import <fog.glsl>

// Saint Seraphae's bone and iron (visual pass 2): solid, faceted and lit by its own facets. Shape 0 is bone grown out
// of the floor or out of a struck body: U runs across the face in blocks, V up it from 0 at its foot to 1 at its tip.
// Shape 1 is her hematic iron (a chain link, a lock): U along the face in blocks. Shape 2 is a shard of bone thrown by a
// growth or a burst. The vertex colour is state, never a tint: red the white light running in the bone's grooves or
// caught in the iron (from 0.9 the hot core), green how raw and torn its root is, blue how wet. Alpha is the
// formation: bone climbs out of the floor as it grows (V above the front is still soft tissue) and dissolves back into
// light from the tip down as it withers. Ivory bone, pale flesh, raw red, white light and dark hematic iron only; no
// gilt, no copper.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
in float vertexDistance;
in vec4 vertexColor;
in vec2 uv;
in vec3 viewPosition;
flat in float shape;
flat in float seed;
out vec4 fragColor;

#moj_import <hemomancy:noise/value_hash.glsl>
#moj_import <hemomancy:noise/material_noise.glsl>
#moj_import <hemomancy:noise/material_fbm.glsl>

const vec3 BONE_DARK = vec3(0.500, 0.400, 0.320);
const vec3 BONE = vec3(0.900, 0.830, 0.700);
const vec3 BONE_EDGE = vec3(0.980, 0.960, 0.910);
const vec3 FLESH = vec3(0.880, 0.700, 0.660);
const vec3 RAW = vec3(0.640, 0.110, 0.130);
const vec3 CLOT = vec3(0.250, 0.025, 0.050);
const vec3 LIGHT = vec3(1.000, 0.985, 0.955);
const vec3 HOT = vec3(1.000, 0.824, 0.769);
const vec3 IRON = vec3(0.192, 0.220, 0.208);
const vec3 IRON_EDGE = vec3(0.412, 0.443, 0.388);
const vec3 IRON_SCALE = vec3(0.149, 0.165, 0.157);

void main() {
    float light = vertexColor.r;
    float raw = vertexColor.g;
    float wet = vertexColor.b;
    float formation = vertexColor.a;
    vec3 n = normalize(cross(dFdx(viewPosition), dFdy(viewPosition)));
    vec3 view = normalize(-viewPosition);
    // a key light from high and to one side of the view, a fill from the other, and a rim at the silhouette
    float key = max(0.0, dot(n, normalize(vec3(0.45, 0.80, 0.40))));
    float key2 = max(0.0, dot(-n, normalize(vec3(0.45, 0.80, 0.40))));
    float lambert = max(key, key2 * 0.85);
    float rim = pow(1.0 - abs(dot(n, view)), 3.0);
    // 16 grains a block across the face, 16 up a block-high spur
    vec2 g = vec2(uv.x * 16.0, uv.y * 16.0);
    float grain = noise(floor(g) + seed);
    vec3 glow = mix(LIGHT, HOT, smoothstep(0.9, 1.0, light));
    vec3 colour;

    if (shape > 1.5) {
        // a shard: bone lit by its facets, its broken edges glowing with the light it carried
        colour = mix(BONE_DARK, BONE, 0.3 + 0.6 * lambert) * (0.9 + 0.2 * grain) + BONE_EDGE * rim * 0.3;
        colour = mix(colour, glow, clamp(light * (0.35 + 0.5 * rim), 0.0, 0.9));
    } else if (shape > 0.5) {
        // hematic iron: dark, scaled, its struck edges catching the key; her light caught in it glows from inside
        colour = mix(IRON_SCALE, IRON, 0.35 + 0.65 * lambert) * (0.85 + 0.3 * grain);
        colour += IRON_EDGE * (rim * 0.5 + pow(key, 10.0) * 0.45);
        float caught = light * (0.3 + 0.7 * rim) * (0.75 + 0.25 * sin(HemoTime * 0.3 + uv.x * 6.0 + seed));
        colour = mix(colour, glow, clamp(caught, 0.0, 0.9));
    } else {
        float threshold = uv.y * 0.86 + noise(vec2(uv.x * 9.0, uv.y * 4.0) + seed) * 0.14;
        if (formation < 0.995 && threshold > formation) discard;
        // ivory bone, darker in the hollows of its facets, with the faint rings it grew in
        float band = fbm(vec2(uv.x * 3.0, uv.y * 6.0) + seed * 0.37);
        colour = mix(BONE_DARK, BONE, 0.25 + 0.75 * lambert) * (0.86 + 0.24 * grain);
        float growth = smoothstep(0.75, 1.0, sin(uv.y * 38.0 + band * 3.0) * 0.5 + 0.5);
        colour = mix(colour, BONE_DARK * 0.9, growth * 0.25);
        colour += BONE_EDGE * (rim * 0.35 + pow(key, 12.0) * 0.35);
        // white light running up its grooves toward the tip
        float groove = 1.0 - smoothstep(0.0, 0.12, abs(fract(uv.x * 8.0 + seed * 0.37) - 0.5));
        float run = 0.5 + 0.5 * sin(uv.y * 14.0 - HemoTime * 0.35 + seed);
        colour = mix(colour, glow, clamp(groove * light * (0.5 + 0.5 * run) + light * rim * 0.4, 0.0, 0.95));
        // the root: raw and wet where it tore out of the floor or the body
        float root = exp(-uv.y * 6.0) * raw;
        vec3 flesh = mix(RAW, CLOT, 0.4 + 0.4 * noise(g * 0.5 + seed));
        float shine = pow(max(0.0, dot(reflect(-view, n), normalize(vec3(0.2, 0.9, 0.3)))), 24.0);
        flesh += vec3(1.0, 0.75, 0.72) * shine * wet * 0.8;
        colour = mix(colour, flesh, clamp(root * 1.2, 0.0, 0.95));
        // the growing tip: still soft, pale pink and wet where it is coming out of the floor
        float front = (1.0 - smoothstep(0.0, 0.08, formation - threshold)) * (1.0 - step(0.995, formation));
        colour = mix(colour, FLESH * 1.05 + LIGHT * 0.25 * light, front * 0.85);
    }

    fragColor = linear_fog(vec4(colour * ColorModulator.rgb, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
}
