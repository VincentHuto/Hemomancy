#version 150
#moj_import <fog.glsl>

// Saint Velorum's floor: blood arrested as dark cruor under pale frost, and the liquid dark that thins sight (revision
// 2026-11-06). The vertex colour is state, never a tint; alpha is strength. Shapes:
//   0 seam: a crack with dark blood in it under a frost lip; red frost, green cruor set in it, blue glint. U along / 32.
//   1 rime, 2 cruor sheet, 4 trench bed, 9 gloom: ground patches; U and V the ground position / 32, plus one.
//   3 trench wall: U along the wall / 32, V from the lip (0) to the bottom (1).
//   5 fold, 6 pool, 7 shade, 8 light, 10 trail: cards, U and V from 0 to 1.
//   11 liquid dark: a ground patch whose alpha is its coverage, torn at every edge; red is strength.
//   12 smoke: a torn tatter of dark smoke, a card; green is how far it has torn away, red its frost-light rim.
//   13 seam blood: a ground patch over the floor's own block seams; red how far blood has welled into them, green how far
//      it has set into cruor under a frost lip, blue the cruor's glow (the Eclipse's red grid).
//   14 stain: spilled blood on the floor, a card; red how deep it has soaked, green beading up, blue a frozen ring.
//   15 cut: a gloam cut, a crescent of liquid dark (V runs from its torn trailing edge to its leading edge); blue glint.
//   16 ridge: a frozen vein along a strip, a raised cruor ridge with a frost crust; red frost, blue its running bead.
// Cruor is red; blue is only frost light. Telegraph shapes take the fog at 0.35 strength; the dark takes it whole.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float CruorGlow;
in float vertexDistance;
in vec4 vertexColor;
in vec2 uv;
flat in float shape;
flat in float seed;
out vec4 fragColor;

#moj_import <hemomancy:noise/value_hash.glsl>
#moj_import <hemomancy:noise/material_noise.glsl>
#moj_import <hemomancy:noise/material_fbm.glsl>

const float SPAN = 32.0;
const vec3 NIGHT = vec3(0.014, 0.010, 0.022);
const vec3 SHADOW = vec3(0.036, 0.026, 0.050);
const vec3 CRUOR_DEEP = vec3(0.102, 0.020, 0.031);
const vec3 CRUOR = vec3(0.227, 0.055, 0.078);
const vec3 CRUOR_LIT = vec3(0.416, 0.110, 0.149);
const vec3 CLOT = vec3(0.071, 0.012, 0.039);
const vec3 HOAR = vec3(0.863, 0.910, 0.957);
const vec3 GLOW = vec3(0.541, 0.141, 0.188);
const vec3 LIQUID = vec3(0.027, 0.016, 0.047);
const vec3 SHEEN = vec3(0.102, 0.063, 0.188);
const vec3 TENEBRIS = vec3(0.078, 0.039, 0.118);
// frost light, the only blue
const vec3 ICE = vec3(0.604, 0.800, 0.941);
const vec3 CORE = vec3(0.910, 0.957, 1.000);

float sq(float x) {
    return x * x;
}

// Nearest-cell walls: x the gap between the nearest and second-nearest centres (small along a wall), y fixed per cell.
vec2 plates(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    float a = 8.0;
    float b = 8.0;
    float id = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 o = vec2(float(x), float(y));
            vec2 c = cell + o;
            vec2 centre = o + vec2(hash(c + seed * 0.31), hash(c + vec2(41.7, 13.3) + seed * 0.17));
            float d = length(centre - f);
            if (d < a) {
                b = a;
                a = d;
                id = hash(c + vec2(7.1, 3.9) + seed * 0.07);
            } else {
                b = min(b, d);
            }
        }
    }
    return vec2(b - a, id);
}

// Hoarfrost growing out from a lip: two sets of slanted needles, each its own length, reaching `reach` blocks.
float feathers(float along, float dist, float reach) {
    if (dist < 0.0 || reach <= 0.001) return 0.0;
    float s1 = along * 8.0 - dist * 6.4;
    float len1 = reach * (0.35 + 0.65 * hash(vec2(floor(s1), seed + 3.0)));
    float line1 = 1.0 - smoothstep(0.12, 0.2, abs(fract(s1) - 0.5));
    float s2 = along * 8.0 + dist * 6.4 + 0.5;
    float len2 = reach * 0.7 * (0.35 + 0.65 * hash(vec2(floor(s2), seed + 5.0)));
    float line2 = 1.0 - smoothstep(0.12, 0.2, abs(fract(s2) - 0.5));
    return max(line1 * (1.0 - smoothstep(len1 * 0.75, len1, dist)), line2 * (1.0 - smoothstep(len2 * 0.75, len2, dist)) * 0.8);
}

// Frozen cruor seen into: black-red with clots held deep in it, darker fracture planes, a glossy skin with a slow
// frost-light sheen sliding over it (slowly: things slow under him, they never stop) and its own dull glow.
vec3 cruor(vec2 p) {
    float depth = fbm(p * 0.6 + seed * 0.13);
    vec3 c = mix(CRUOR_DEEP, CRUOR, 0.3 + 0.7 * depth);
    float plane = 1.0 - smoothstep(0.0, 0.03, plates(p * 1.2 + 5.0).x);
    c = mix(c, CRUOR_LIT, plane * 0.4);
    float clot = smoothstep(0.62, 0.78, noise(p * 2.3 + seed * 0.7));
    c = mix(c, CLOT, clot * 0.8);
    float band = p.x * 0.55 + p.y * 0.35 - HemoTime * 0.012 + seed * 0.1;
    c += ICE * pow(0.5 + 0.5 * sin(band * 2.4), 18.0) * 0.22;
    return c + GLOW * CruorGlow * (0.4 + 0.6 * depth) * 0.6;
}

// Rime crusting cruor as it greys: plates of frost where the frost passes them, white along the walls between.
float crusted(vec2 p, float frost) {
    vec2 cell = plates(floor(p * 16.0) / 16.0 * 3.2);
    float cover = smoothstep(cell.y * 0.8, cell.y * 0.8 + 0.12, frost);
    return cover * (0.3 + 0.7 * (1.0 - smoothstep(0.0, 0.08, cell.x)));
}

// The liquid dark's surface: flat, glossy near-black with a slow violet-black sheen and a slow ripple.
vec3 liquid(vec2 p) {
    float ripple = sin(p.x * 1.1 + p.y * 0.7 + fbm(p * 0.4 + seed) * 3.0 - HemoTime * 0.02);
    float sheen = pow(0.5 + 0.5 * ripple, 6.0) * (0.5 + 0.5 * fbm(p * 0.25 - HemoTime * 0.003));
    return mix(LIQUID, SHEEN, sheen * 0.8);
}

void main() {
    float frost = vertexColor.r;
    float ice = vertexColor.g;
    float glint = vertexColor.b;
    float strength = vertexColor.a * ColorModulator.a;
    float t = HemoTime;
    vec3 colour = NIGHT;
    float alpha = 0.0;
    float fog = 0.35;

    if (shape < 0.5) {
        // A seam: a dark gap with old blood glossy in it, a frost lip, hoarfrost feathering out from both lips; as the
        // cruor sets the blood darkens and the gap narrows; the glint is frost light along its lip at a commit.
        float along = floor(uv.x * SPAN * 16.0) / 16.0;
        float a = abs(uv.y * 2.0 - 1.0);
        float gap = max(0.05, (0.16 + 0.14 * noise(vec2(along * 1.4, seed))) * (1.0 - 0.5 * ice));
        float lip = 0.07;
        if (a < gap) {
            float depth = 1.0 - a / gap;
            float bead = 0.5 + 0.5 * sin(along * 9.0 + seed);
            colour = mix(CRUOR, CRUOR_DEEP, depth);
            colour += CRUOR_LIT * pow(bead, 8.0) * (1.0 - ice) * 0.6 + ICE * glint * pow(depth, 5.0) * 0.25;
            colour = mix(colour, cruor(vec2(along, depth * 2.0)), ice * 0.7);
            alpha = 0.96;
        } else if (a < gap + lip) {
            colour = HOAR * (0.6 + 0.4 * noise(vec2(along * 9.0, seed + 2.0))) * (0.55 + 0.45 * frost);
            colour = mix(colour, CORE, glint * 0.4);
            alpha = 0.85 * (0.55 + 0.45 * frost);
        } else {
            float room = max(0.001, 1.0 - gap - lip);
            float beyond = a - gap - lip;
            float reach = frost * (0.55 + 0.45 * noise(vec2(along * 0.9, seed + 7.0))) * room;
            float barbs = feathers(along, beyond * 0.34, reach * 0.34);
            float haze = (1.0 - smoothstep(0.0, room, beyond)) * frost;
            colour = mix(mix(SHADOW, CRUOR_DEEP, haze * 0.6), HOAR, barbs);
            colour = mix(colour, CORE, glint * glint * barbs * 0.5);
            alpha = max(barbs * 0.85, haze * 0.22);
        }
    } else if (abs(shape - 1.0) < 0.5) {
        // Rime over arrested blood: a dark red glaze plate by plate as the frost passes each plate's own threshold, the
        // stone showing through it, the frost gathered pale along the walls between plates (a crazed lattice, never a
        // sheet of snow); hairline frost creeps ahead and cruor films into the gaps as the ice rises.
        vec2 p = floor((uv - 1.0) * SPAN * 16.0) / 16.0 + 1.0 / 32.0;
        vec2 cell = plates(p * 3.2);
        float cover = smoothstep(cell.y * 0.8, cell.y * 0.8 + 0.12, frost);
        // polish round 2: at eye height the white walls between plates broke into texel noise, salt on a red cloth;
        // the walls are thinner and greyer so the blood under the glaze leads
        float edge = 1.0 - smoothstep(0.0, 0.05, cell.x);
        float hair = 1.0 - smoothstep(0.0, 0.05, plates(p * 7.0 + 11.0).x);
        float facet = hash(vec2(cell.y * 91.0, seed));
        vec3 plate = mix(CRUOR_DEEP, CRUOR * 1.1, 0.3 + 0.5 * facet);
        float twinkle = step(0.985, hash(floor(p * 16.0) + floor(t * 0.04 + facet * 7.0)));
        colour = mix(SHADOW, plate, cover);
        colour = mix(colour, HOAR * 0.8, edge * cover * 0.55);
        colour = mix(colour, CORE, twinkle * cover * 0.3);
        float creep = smoothstep(0.02, 0.35, frost) * (1.0 - cover);
        colour = mix(colour, HOAR * 0.85, hair * creep);
        float film = ice * (1.0 - cover * 0.8) * smoothstep(0.3, 0.6, fbm(p * 1.3 + seed));
        colour = mix(colour, cruor(p), film * (1.0 - edge * cover));
        colour = mix(colour, CORE, glint * glint * (edge * cover + hair * creep) * 0.6);
        float stain = smoothstep(0.0, 0.25, frost) * (1.0 - cover) * 0.22;
        alpha = max(max(cover * (0.38 + 0.14 * facet + 0.45 * edge), hair * creep * 0.55), max(film * 0.75, stain));
    } else if (abs(shape - 2.0) < 0.5) {
        // A sheet of frozen cruor, crusting over with rime as the frost rises.
        vec2 p = (uv - 1.0) * SPAN;
        colour = mix(cruor(p), HOAR * 0.85, crusted(p, frost) * 0.85);
        colour = mix(colour, CORE, glint * 0.25);
        alpha = 0.92 * max(ice, crusted(p, frost));
    } else if (abs(shape - 3.0) < 0.5) {
        // A trench wall seen down into: strata of frozen blood darkening with depth, a frost crust over the lip with
        // icicles of cruor hanging from it, the blood's dull glow at its foot.
        float along = floor(uv.x * SPAN * 16.0) / 16.0;
        float v = uv.y;
        float strata = noise(vec2(along * 0.7, v * 5.0 + seed));
        colour = mix(CRUOR * 0.9, CRUOR_DEEP * 0.6, smoothstep(0.0, 1.0, v));
        colour += CRUOR_LIT * 0.35 * (1.0 - smoothstep(0.0, 0.35, abs(fract(v * 2.5 + strata * 0.8) - 0.5))) * (1.0 - v);
        colour = mix(colour, CLOT, smoothstep(0.7, 0.9, noise(vec2(along * 5.0, seed + floor(v * 4.0)))) * 0.7);
        float drip = (0.14 + 0.3 * noise(vec2(floor(along * 8.0), seed + 1.0))) * (0.3 + 0.7 * frost);
        colour = mix(colour, HOAR * 0.9, 1.0 - smoothstep(drip - 0.04, drip + 0.02, v));
        colour += GLOW * smoothstep(0.55, 1.0, v) * (0.15 + 0.6 * CruorGlow) + CORE * glint * smoothstep(0.6, 1.0, v) * 0.2;
        alpha = 0.97;
    } else if (abs(shape - 4.0) < 0.5) {
        // A trench's bed: empty, shadow with the blood's dull glow coming up through cracks; old blood wells up through
        // it in pools as the ice rises until it is a glossy sheet of cruor; rime crusts it as it greys.
        vec2 p = (uv - 1.0) * SPAN;
        vec3 empty = mix(NIGHT, SHADOW, fbm(p * 1.3 + seed) * 0.8);
        float crack = 1.0 - smoothstep(0.0, 0.05, plates(floor(p * 16.0) / 16.0 * 1.1 + 3.0).x);
        empty += mix(GLOW, CRUOR_LIT, glint) * crack * (0.25 + 0.5 * glint);
        float n = fbm(p * 1.4 + seed + 9.0);
        float fill = smoothstep(n * 0.9, n * 0.9 + 0.1, ice);
        colour = mix(empty, cruor(p), fill);
        colour = mix(colour, HOAR * 0.85, crusted(p, frost) * 0.8);
        colour = mix(colour, CORE, glint * fill * 0.2);
        alpha = 0.97;
    } else if (abs(shape - 5.0) < 0.5) {
        // A fold of liquid dark: a pleated curtain rising, smoking and tearing at its top, a cold hairline down its crests.
        float x = uv.x * 2.0 - 1.0;
        float y = uv.y;
        float drift = t * 0.01;
        float pleat = 0.5 + 0.5 * cos(x * 6.2832 + (noise(vec2(y * 2.0 - drift, seed)) - 0.5) * 3.0);
        float smoke = fbm(vec2(x * 1.8, y * 2.6 - drift * 1.5) + seed);
        float top = 1.0 - smoothstep(0.5, 1.0, y + (smoke - 0.5) * 0.4);
        float sides = 1.0 - smoothstep(0.6, 1.0, abs(x) + (smoke - 0.5) * 0.3);
        colour = mix(LIQUID, TENEBRIS * 1.4, pleat * 0.7);
        colour += ICE * 0.7 * smoothstep(0.88, 1.0, pleat) * (1.0 - 0.6 * y) * frost;
        colour += CORE * 0.3 * top * (1.0 - smoothstep(0.0, 0.18, abs(y + (smoke - 0.5) * 0.4 - 0.62))) * frost;
        alpha = top * sides * smoothstep(0.0, 0.06, y) * (0.7 + 0.3 * pleat) * smoothstep(0.2, 0.5, smoke + 0.25);
        fog = 1.0;
    } else if (abs(shape - 6.0) < 0.5) {
        // A pool of liquid dark on a card, its edge torn and slowly shifting, a frost rim where the cold meets it.
        vec2 q = uv * 2.0 - 1.0;
        float r = length(q);
        float ragged = r + (fbm(q * 2.6 + seed + t * 0.004) - 0.5) * 0.3;
        float dark = 1.0 - smoothstep(0.55, 0.95, ragged);
        float rim = exp(-sq((ragged - 0.9) * 12.0)) * frost;
        colour = mix(liquid(q * 4.0), ICE * 0.6, rim);
        alpha = max(dark * 0.9, rim * 0.5) * (1.0 - smoothstep(0.98, 1.0, r));
        fog = 1.0;
    } else if (abs(shape - 7.0) < 0.5) {
        // A shade of liquid dark: hooded, robed, smoke tearing off hood and hem, two pale eye points.
        float x = uv.x * 2.0 - 1.0;
        float y = uv.y;
        x -= sin(t * 0.07 + seed) * 0.03 * y;
        float wisp = (noise(vec2(x * 7.0, y * 3.0 - t * 0.03 + seed)) - 0.5) * 0.14;
        float hood = length(vec2(x / 0.30, (y - 0.84) / 0.15));
        float hw = mix(0.46, 0.27, smoothstep(0.0, 0.76, y));
        float robe = (1.0 - smoothstep(hw - 0.05, hw + 0.02, abs(x) + wisp)) * (1.0 - smoothstep(0.76, 0.8, y));
        float body = max(robe, 1.0 - smoothstep(0.88, 1.0, hood + wisp));
        float hem = smoothstep(0.0, 0.2, y + (noise(vec2(x * 9.0, t * 0.04 + seed)) - 0.5) * 0.2);
        float edge = body * (1.0 - smoothstep(0.0, 0.05, max(hw - abs(x), (1.0 - hood) * 0.3)));
        vec2 e = vec2(abs(x) - 0.085, y - 0.83);
        float eyes = exp(-dot(e, e) * 3000.0);
        colour = mix(liquid(vec2(x, y) * 3.0), ICE * 0.6, edge * frost);
        colour = mix(colour, CORE, eyes * (0.6 + 0.4 * glint));
        alpha = max(body * hem * 0.9, eyes);
        fog = 1.0;
    } else if (abs(shape - 8.0) < 0.5) {
        // The lantern's cold light on the floor: streaked by its cage into slow rays, sharp cold edges, a slow caustic.
        float r = uv.x;
        float ang = uv.y;
        float fall = 1.0 - smoothstep(0.1, 1.0, r);
        float sides = smoothstep(0.0, 0.04, ang) * (1.0 - smoothstep(0.96, 1.0, ang));
        float caustic = 0.7 + 0.3 * noise(vec2(r * 14.0 - t * 0.02, ang * 9.0 + seed));
        float bars = 0.55 + 0.45 * smoothstep(0.25, 0.75, noise(vec2(floor(ang * 64.0) * 0.37, seed + floor(t * 0.01))));
        float rim = exp(-sq(min(ang, 1.0 - ang) / 0.035)) * (1.0 - smoothstep(0.3, 1.0, r));
        float motes = step(0.985, hash(vec2(floor(r * 80.0), floor(ang * 30.0)) + floor(t * 0.03) + seed));
        colour = mix(mix(ICE * 0.75, CORE, 0.3 + 0.7 * glint), CORE, rim * 0.6);
        alpha = (0.62 * fall * caustic * bars + rim * 0.55 + motes * 0.35 * fall) * sides;
    } else if (abs(shape - 9.0) < 0.5) {
        // Gloom: slow smoke over the floor; its depth is carried in the vertex alpha.
        vec2 p = (uv - 1.0) * SPAN;
        float smoke = fbm(p * 0.5 + vec2(t * 0.006, -t * 0.004) + seed);
        colour = mix(LIQUID, TENEBRIS, smoke);
        alpha = 0.92 * (0.7 + 0.3 * smoke);
        fog = 1.0;
    } else if (abs(shape - 10.0) < 0.5) {
        // A trail cut through the air by his lantern or hand: a pale frost blade with a cold core, dragging liquid dark
        // at its edges, thin at the tail; as it fades it frays apart.
        float u = uv.x;
        float a = abs(uv.y * 2.0 - 1.0);
        float streak = noise(vec2(u * 16.0 - t * 0.01, a * 3.0 + seed));
        float body = 1.0 - smoothstep(0.6, 1.0, a + (streak - 0.5) * 0.2);
        colour = mix(HOAR * (0.8 + 0.2 * streak), LIQUID, smoothstep(0.35, 0.9, a) * ice);
        colour = mix(colour, CORE, exp(-sq(a / 0.16)) * (0.5 + 0.5 * glint));
        float frayed = smoothstep(0.85 - strength, 1.1 - strength, fbm(vec2(u * 5.0, uv.y * 3.0) + seed));
        alpha = body * smoothstep(0.0, 0.25, u) * (0.65 + 0.35 * streak) * frayed;
    } else if (abs(shape - 11.0) < 0.5) {
        // Liquid dark spread on the floor. The alpha is its coverage: where it falls off (the pool's rim, the lantern's
        // light pushing it back) the edge is torn into broad smoky tatters that lift off it; never a hard disc, never a
        // contour line. Red is its strength.
        vec2 p = (uv - 1.0) * SPAN;
        float cover = vertexColor.a;
        float n = fbm(p * 0.85 + vec2(t * 0.004, -t * 0.003) + seed);
        float e = cover + (n - 0.5) * 0.6;
        float surface = smoothstep(0.5, 0.58, e);
        float tatter = smoothstep(0.3, 0.5, e) * (1.0 - surface) * smoothstep(0.45, 0.7, fbm(p * 2.2 - t * 0.01 + seed));
        colour = mix(TENEBRIS, liquid(p), surface);
        alpha = surface * 0.9 + tatter * 0.55;
        strength = frost * ColorModulator.a;
        fog = 1.0;
    } else if (abs(shape - 12.0) < 0.5) {
        // A torn tatter of dark smoke lifting off the dark: broad at its foot, fraying upward and tearing away.
        float x = uv.x * 2.0 - 1.0;
        float y = uv.y;
        float s = fbm(vec2(x * 2.2, y * 2.0 - t * 0.012) + seed);
        float body = (1.0 - smoothstep(0.35, 0.95, abs(x) + y * 0.35 + (s - 0.5) * 0.5)) * smoothstep(0.0, 0.12, y);
        float torn = smoothstep(ice * 0.9 + y * 0.45, ice * 0.9 + y * 0.45 + 0.2, s);
        colour = mix(LIQUID, TENEBRIS * 1.3, s) + ICE * 0.35 * frost * smoothstep(0.55, 0.75, y + s * 0.3) * body;
        alpha = body * torn * 0.85 * (1.0 - smoothstep(0.7, 1.0, y));
        fog = 1.0;
    } else if (abs(shape - 13.0) < 0.5) {
        // The floor's own block seams, where the congregation's old blood lies: it wells up glossy and black-red along
        // them, then sets into cruor under a frost lip; in the Eclipse every seam glows dull red, a grid across the floor.
        vec2 p = floor((uv - 1.0) * SPAN * 16.0) / 16.0 + 1.0 / 32.0;
        vec2 f = fract(p);
        vec2 d2 = min(f, 1.0 - f);
        float d = min(d2.x, d2.y);
        float along = d2.x < d2.y ? p.y : p.x;
        float well = clamp(frost * (1.0 + (noise(vec2(along * 0.8, seed)) - 0.5) * 0.6), 0.0, 1.0);
        // polish round 1: the Eclipse's grid read as a neon lattice. The seam is now a hairline (a pixel and a bit) whose
        // dull cruor glow runs unevenly along it, clotted dark in places, with a faint under-light on the stone beside it
        float run = noise(vec2(along * 0.55, seed + (d2.x < d2.y ? 3.0 : 9.0)));
        float gap = mix(0.04 + 0.03 * well, 0.022 + 0.02 * run, glint);
        if (d < gap) {
            float bead = pow(0.5 + 0.5 * sin(along * 8.0 + hash(floor(p) + seed) * 6.0), 6.0);
            colour = mix(CRUOR_DEEP, CRUOR, well) + CRUOR_LIT * bead * (1.0 - ice) * 0.7;
            colour = mix(colour, cruor(p), ice * 0.6);
            float clot = smoothstep(0.62, 0.8, noise(vec2(along * 2.6, seed + 5.0)));
            float lit = glint * (0.3 + 0.7 * smoothstep(0.15, 0.85, run)) * (1.0 - 0.75 * clot);
            colour = mix(colour, mix(CRUOR_LIT, GLOW, 0.5) * (0.75 + 0.35 * bead), lit);
            alpha = max(well * (1.0 - glint), glint * (0.75 + 0.2 * run)) * 0.95;
        } else if (glint > 0.01) {
            // the glow lying on the stone beside the seam: low, soft, falling off within a few pixels
            float under = exp(-(d - gap) / 0.06) * (0.3 + 0.7 * run);
            colour = mix(CRUOR_DEEP, GLOW, 0.45);
            alpha = glint * 0.3 * under;
        } else if (d < gap + 0.07) {
            // the frost lip on a welled seam: patchy along it, never a white grid
            float spotty = smoothstep(0.35, 0.65, noise(vec2(along * 1.3, seed + 4.0)));
            colour = HOAR * 0.85;
            alpha = ice * well * 0.45 * spotty * (0.6 + 0.4 * noise(p * 9.0 + seed));
        }
    } else if (abs(shape - 14.0) < 0.5) {
        // Spilled blood soaked into the floor where a challenger stood still or bled: glossy and dark, spreading and
        // deepening slowly; it beads up before the Splinter Crown finds it, and a frozen ring locks round it.
        vec2 q = uv * 2.0 - 1.0;
        float r = length(q) + (fbm(q * 2.2 + seed) - 0.5) * 0.35;
        float reach = 0.35 + 0.5 * frost;
        float mask = 1.0 - smoothstep(reach - 0.1, reach, r);
        float gloss = pow(0.5 + 0.5 * sin(q.x * 3.0 + q.y * 2.0 + fbm(q * 3.0 + seed) * 4.0 - t * 0.01), 10.0);
        colour = mix(CRUOR, CRUOR_DEEP, frost) + CRUOR_LIT * gloss * 0.4;
        vec2 b = fract(q * 5.0 + seed) - 0.5;
        float beads = (1.0 - smoothstep(0.12, 0.3, length(b))) * step(0.55, hash(floor(q * 5.0 + seed))) * ice;
        colour = mix(colour, CRUOR_LIT * 1.3, beads * 0.6) + HOAR * beads * 0.15;
        float ring = exp(-sq((r - reach - 0.08) * 22.0)) * glint;
        colour = mix(colour, HOAR, ring);
        alpha = max(mask * (0.55 + 0.4 * frost), ring * 0.8);
    } else if (abs(shape - 15.0) < 0.5) {
        // A gloam cut: a crescent of liquid dark flying point-forward, its trailing edge torn and smoking, a cold
        // hairline along its leading edge.
        float x = uv.x * 2.0 - 1.0;
        float lead = 0.62 + 0.3 * (1.0 - x * x);
        float s = uv.y - lead;
        float n = fbm(vec2(x * 3.0, uv.y * 2.0 - t * 0.02) + seed);
        float body = smoothstep(-0.55 + (n - 0.5) * 0.4, -0.25, s) * (1.0 - smoothstep(-0.02, 0.0, s));
        float sides = 1.0 - smoothstep(0.8, 1.0, abs(x) + (n - 0.5) * 0.2);
        float hair = exp(-sq(s / 0.02)) * sides;
        colour = mix(TENEBRIS, liquid(vec2(x, uv.y) * 3.0), smoothstep(-0.3, -0.05, s));
        // polish round 4: a violet-black sheen rising toward the leading edge, so the crescent has a face on a dark floor
        colour += SHEEN * 0.9 * smoothstep(-0.35, -0.03, s) * (0.6 + 0.4 * n);
        colour = mix(colour, CORE, hair * (0.6 + 0.4 * glint));
        alpha = max(body * sides * 0.92, hair * 0.9);
    } else {
        // A frozen vein: a raised ridge of cruor along the floor's seams, lit on its crest, crusted with frost, a bright
        // bead running at its front.
        float along = floor(uv.x * SPAN * 16.0) / 16.0;
        float a = abs(uv.y * 2.0 - 1.0);
        float crest = 1.0 - a;
        colour = mix(CRUOR_DEEP, CRUOR_LIT, crest * crest) + GLOW * CruorGlow * 0.6;
        colour = mix(colour, CLOT, smoothstep(0.7, 0.85, noise(vec2(along * 3.0, seed))) * 0.7);
        float crust = smoothstep(0.55, 0.8, crest + (noise(vec2(along * 12.0, a * 4.0 + seed)) - 0.5) * 0.5) * frost;
        colour = mix(colour, HOAR, crust * 0.85);
        colour = mix(colour, CORE, glint * crest * 0.6);
        alpha = (1.0 - smoothstep(0.75, 1.0, a + (noise(vec2(along * 4.0, seed)) - 0.5) * 0.2)) * 0.97;
    }

    alpha = clamp(alpha * strength, 0.0, 0.97);
    if (alpha < 0.003) discard;
    vec4 lit = vec4(colour * ColorModulator.rgb, alpha);
    fragColor = mix(lit, linear_fog(lit, vertexDistance, FogStart, FogEnd, FogColor), fog);
}
