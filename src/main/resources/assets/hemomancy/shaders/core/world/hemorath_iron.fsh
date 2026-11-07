#version 150
#moj_import <fog.glsl>

// Saint Hemorath's iron: bone-grown hematic iron torn up out of his chamber floor. Solid, faceted and lit by its own
// facets (the chamber is dark and the material carries its own light). Shape 0 is a spike or scab face: U runs across
// the face in blocks, V up it from 0 at the foot to 1 at the point. Shape 1 is a chip of floor stone. Shapes 2 and 3 are
// a floor slab heaved along a trench's lip: its brick top and its broken faces (see below). The vertex colour
// is state, never a tint: red the heat of the rupture at the foot, green how far wet blood runs down from the point,
// blue the rust. Alpha is the formation: the iron climbs out of the floor's blood as it rises (V above the formation
// front is not yet iron) and dissolves back from the point down as it sinks. Oxblood iron, rust, dried crimson,
// black-red clot and pale bone only.

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

const vec3 IRON_DARK = vec3(0.105, 0.040, 0.038);
const vec3 IRON_MID = vec3(0.270, 0.100, 0.088);
const vec3 IRON_EDGE = vec3(0.560, 0.300, 0.260);
const vec3 RUST = vec3(0.500, 0.205, 0.090);
const vec3 RUST_DEEP = vec3(0.250, 0.075, 0.035);
const vec3 BLOOD = vec3(0.300, 0.010, 0.028);
const vec3 BLOOD_SHINE = vec3(0.920, 0.420, 0.380);
const vec3 CLOT = vec3(0.075, 0.006, 0.012);
const vec3 HOT = vec3(1.000, 0.260, 0.100);
const vec3 CORE = vec3(1.000, 0.830, 0.700);
const vec3 BONE = vec3(0.640, 0.570, 0.470);

void main() {
    float heat = vertexColor.r;
    float wet = vertexColor.g;
    float rust = vertexColor.b;
    float formation = vertexColor.a;
    vec3 n = normalize(cross(dFdx(viewPosition), dFdy(viewPosition)));
    vec3 view = normalize(-viewPosition);
    // a key light from high and to one side of the view, a cool fill from the other, and a rim at the silhouette
    float key = max(0.0, dot(n, normalize(vec3(0.45, 0.80, 0.40))));
    float key2 = max(0.0, dot(-n, normalize(vec3(0.45, 0.80, 0.40))));
    float lambert = max(key, key2 * 0.85);
    float facing = abs(dot(n, view));
    float rim = pow(1.0 - facing, 3.0);
    // 16 grains a block across the face, 16 up a block-high spike
    vec2 g = vec2(uv.x * 16.0, uv.y * 16.0);
    float grain = noise(floor(g) + seed);

    if (shape > 1.5) {
        // a slab of the chamber's own floor heaved up along a trench's lip (visual pass 2b). Shape 2 is its top: the
        // floor's dark brick, half-block bricks in quarter-block courses, U along the slab and V out from its outer
        // edge, both in blocks; blue runs 0 at the outer edge to 1 at the broken inner edge, which catches the light as
        // a pale cut lip. Shape 3 is a broken face: rough dark stone with crimson strata, wet low down where the
        // trench's blood welled against it (green), V up the face in blocks.
        vec3 colour;
        if (shape < 2.5) {
            float edge = vertexColor.b;
            float course = floor(uv.y * 4.0);
            float brickU = uv.x * 2.0 + 0.5 * mod(course, 2.0);
            vec2 brick = vec2(floor(brickU), course);
            float seamU = min(fract(brickU), 1.0 - fract(brickU));
            float seamV = min(fract(uv.y * 4.0), 1.0 - fract(uv.y * 4.0));
            float mortar = max(1.0 - smoothstep(0.0, 0.06, seamU), 1.0 - smoothstep(0.0, 0.12, seamV));
            float shade = noise(brick + seed * 0.7);
            vec3 stone = vec3(0.150, 0.128, 0.118) * (0.8 + 0.4 * shade) * (0.85 + 0.3 * grain);
            stone = mix(stone, vec3(0.055, 0.042, 0.040), mortar * 0.8);
            colour = stone * (0.45 + 0.65 * lambert);
            // the cut lip: crumbled pale where the floor broke, a thin rim of fresh stone catching the light
            float lip = smoothstep(0.78, 0.97, edge + (noise(vec2(floor(g.x), seed)) - 0.5) * 0.12);
            colour = mix(colour, vec3(0.36, 0.30, 0.27) * (0.6 + 0.6 * lambert), lip);
            colour += IRON_EDGE * rim * 0.12;
            // spatter flung up out of the trench on the inner half
            float spatter = smoothstep(0.62, 0.8, noise(floor(g) * 0.35 + seed + 5.0)) * smoothstep(0.3, 0.8, edge);
            colour = mix(colour, BLOOD * 1.2, spatter * 0.5);
        } else {
            float wetLow = vertexColor.g;
            vec2 px = floor(g) / 16.0;
            float strata = noise(vec2(px.x * 5.0 + seed, px.y * 22.0));
            float fracture = smoothstep(0.72, 0.9, noise(vec2(px.x * 11.0, seed + 3.0)));
            vec3 broken = mix(vec3(0.085, 0.060, 0.055), vec3(0.20, 0.075, 0.065), strata);
            broken *= (0.85 + 0.3 * grain) * (1.0 - 0.5 * fracture);
            colour = broken * (0.4 + 0.75 * lambert) + IRON_EDGE * rim * 0.2;
            float coat = wetLow * smoothstep(0.35, 0.65, noise(floor(g) * 0.5 + seed + 9.0) + 0.4 * wetLow);
            vec3 blood = mix(CLOT, BLOOD, 0.4 + 0.6 * lambert);
            float shine = pow(max(0.0, dot(reflect(-view, n), normalize(vec3(0.2, 0.9, 0.3)))), 24.0);
            colour = mix(colour, blood + BLOOD_SHINE * shine * 0.6, coat);
        }
        fragColor = linear_fog(vec4(colour * ColorModulator.rgb, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
        return;
    }

    if (shape > 0.5) {
        // a chip of floor stone: pale bone dust over dark stone, lit by its facets
        vec3 stone = mix(vec3(0.16, 0.12, 0.11), BONE, 0.35 + 0.4 * grain);
        vec3 colour = stone * (0.35 + 0.75 * lambert) + IRON_EDGE * rim * 0.25;
        colour = mix(colour, BLOOD * 1.4, wet * smoothstep(0.4, 0.7, noise(g * 0.5 + seed)));
        fragColor = linear_fog(vec4(colour * ColorModulator.rgb, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
        return;
    }

    float threshold = uv.y * 0.86 + noise(vec2(uv.x * 9.0, uv.y * 4.0) + seed) * 0.14;
    if (formation < 0.995 && threshold > formation) discard;

    // the metal: oxblood iron, darker in the hollows of the facets, lit on the faces that catch the key
    float band = fbm(vec2(uv.x * 3.0, uv.y * 5.0) + seed * 0.37);
    vec3 metal = mix(IRON_DARK, IRON_MID, 0.25 + 0.75 * lambert);
    metal *= 0.82 + 0.3 * grain;
    // rust blooms out of the iron in patches, deeper in the lower half where blood dried on it
    float bloom = smoothstep(0.42, 0.72, band + 0.15 * (1.0 - uv.y)) * (0.35 + 0.65 * rust);
    vec3 rusted = mix(RUST_DEEP, RUST, grain * 0.7 + 0.3 * lambert);
    metal = mix(metal, rusted * (0.55 + 0.6 * lambert), bloom);
    // the struck edges catch the light: a warm rim at the silhouette and on facets turned to the key
    metal += IRON_EDGE * (rim * 0.45 + pow(key, 12.0) * 0.4);

    // wet blood running down from the point: a front with drips hanging off it, glossy, with a moving shine
    float drip = noise(vec2(floor(uv.x * 16.0) + seed, 3.0));
    float wetLine = 1.0 - wet * (0.55 + 0.45 * drip);
    float coat = smoothstep(wetLine - 0.04, wetLine + 0.04, uv.y) * step(0.02, wet);
    vec3 blood = mix(CLOT, BLOOD, 0.4 + 0.6 * lambert);
    float shine = pow(max(0.0, dot(reflect(-view, n), normalize(vec3(0.2, 0.9, 0.3)))), 24.0);
    blood += BLOOD_SHINE * (shine * 0.9 + rim * 0.25);
    vec3 colour = mix(metal, blood, coat);

    // the rupture at the foot: the iron still white-hot where it tore out of the floor, cooling up the spike
    float foot = exp(-uv.y * 7.0) * heat;
    float flicker = 0.85 + 0.15 * sin(HemoTime * 0.6 + seed * 1.7 + uv.x * 9.0);
    vec3 glow = mix(HOT, CORE, smoothstep(0.75, 1.0, heat) * exp(-uv.y * 18.0));
    colour = mix(colour, glow, clamp(foot * flicker, 0.0, 0.9));

    // the formation front: a lip of fresh blood where the iron is still coming out of the floor's
    float front = (1.0 - smoothstep(0.0, 0.07, formation - threshold)) * (1.0 - step(0.995, formation));
    colour = mix(colour, BLOOD * 1.3 + BLOOD_SHINE * 0.25, front * 0.85);

    fragColor = linear_fog(vec4(colour * ColorModulator.rgb, 1.0), vertexDistance, FogStart, FogEnd, FogColor);
}
