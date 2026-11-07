#version 150
#moj_import <fog.glsl>

// Saint Putriciel's floor answering them (visual pass 2): the chamber floor colonised by the rot and, in the cleansing
// interval, scorched black by the purifying fire. Four shapes:
//   0 MAT     a colonised patch of floor. U and V are its ground position in blocks / 32 about the patch's origin,
//             plus one. Red is how far the colony has taken this spot (written per vertex, so the colony runs out
//             from where it started), green how much rot has pooled into the floor, blue the sporulation.
//   1 TRENCH  a furrow the rot opens along a strip: U the distance along it in blocks / 32, V across it from one side
//             of the strip to the other. Red is how far it has opened, green how full of ooze, blue the sporulation
//             frilling its lips.
//   2 SCORCH  a patch the fire scorches, laid out as MAT. Red is how far the char has eaten, green the heat, blue the
//             ash.
//   3 FURROW  a burning trench, laid out as TRENCH. Red open, green heat, blue ash.
//   4 STAIN   MAT's wet stain round a pool of rot sludge (revision 2026-11-06), laid out as MAT: red the place out
//             from the pool's rim (0 under the pool, 1 at the stain's edge, 0.35 past the rim), green how wet, blue the
//             mould-green fringe where it meets the moss.
// Alpha is the strength. The vertex colour is state, never a tint. The rot is charcoal olive, olive, mould green,
// ochre, muted yellow and pale cream, with no red; the fire is soot, charcoal, ash grey, crimson and pink-white,
// never green or yellow. Patterns snap to the block texture's sixteen texels a block.

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

const float SPAN = 32.0;
// The trench's half-width over its strip's: PutricielGroundGeometry.TRENCH_HALO is 1.6.
const float WALL = 0.625;
const vec3 ROT_DARK = vec3(0.058, 0.062, 0.028);
const vec3 OLIVE = vec3(0.290, 0.300, 0.125);
const vec3 MOULD = vec3(0.330, 0.470, 0.215);
const vec3 OCHRE = vec3(0.560, 0.440, 0.180);
const vec3 YELLOW = vec3(0.690, 0.620, 0.320);
const vec3 CREAM = vec3(0.900, 0.840, 0.640);
const vec3 OOZE = vec3(0.070, 0.076, 0.032);
const vec3 SHEEN = vec3(0.640, 0.620, 0.430);
const vec3 SOOT = vec3(0.020, 0.015, 0.015);
const vec3 CHAR = vec3(0.072, 0.060, 0.056);
const vec3 ASH = vec3(0.420, 0.402, 0.388);
const vec3 EMBER = vec3(0.300, 0.012, 0.032);
const vec3 CRIMSON = vec3(0.840, 0.118, 0.180);
const vec3 HEAT = vec3(1.000, 0.660, 0.745);
const vec3 WHITE_HOT = vec3(1.000, 0.885, 0.905);
const vec3 STAIN_MOULD = vec3(0.376, 0.502, 0.251);

float sq(float x) {
    return x * x;
}

// A ground position snapped to the floor's texels.
vec2 texel(vec2 p) {
    return floor(p * 16.0) / 16.0 + 1.0 / 32.0;
}

// The colonies a patch is made of: x the distance to the nearest colony's heart, y to the second nearest, z a value
// for the nearest colony.
vec3 colonies(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    float a = 8.0;
    float b = 8.0;
    float id = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 key = cell + offset + seed * 0.29;
            vec2 heart = offset + vec2(hash(key), hash(key + vec2(31.7, 11.3)));
            float d = length(heart - f);
            if (d < a) {
                b = a;
                a = d;
                id = hash(key + vec2(5.1, 2.9));
            } else {
                b = min(b, d);
            }
        }
    }
    return vec3(a, b, id);
}

// The rot's colonies in one spot of floor: mould-green hearts, ochre and pale cream growth zones, dark crevices.
vec3 colonyColour(vec2 q, out float crevice) {
    vec3 c = colonies(q * 1.4 + seed * 0.7);
    crevice = 1.0 - smoothstep(0.03, 0.09, c.y - c.x);
    float zone = floor(fract(c.x * 4.5 + c.z * 2.0) * 3.0);
    vec3 colour = mix(MOULD * 0.8, OLIVE * 1.1, smoothstep(0.08, 0.28, c.x));
    colour = mix(colour, zone < 0.5 ? OCHRE : (zone < 1.5 ? YELLOW * 0.85 : OLIVE * 1.25), smoothstep(0.22, 0.34, c.x));
    colour = mix(colour, CREAM * 0.86, smoothstep(0.5, 0.62, c.x) * 0.75);
    colour = mix(colour, ROT_DARK, crevice * 0.85);
    return colour;
}

// Mould colonies seeded over the floor at one scale: x is the distance from the nearest colony's heart in its own
// radii (0 at the heart, 1 at its rim), y a value for that colony, zw the way out from its heart (for its dome's
// light). Only some cells hold a colony ({@code density}), and every colony has its own size, so the floor is taken in
// round fuzzy colonies of many sizes, not a grid.
vec4 moulds(vec2 p, float density, float salt) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    vec4 best = vec4(9.0, 0.0, 0.0, 0.0);
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 key = cell + offset + vec2(salt, seed * 0.37);
            if (hash(key + vec2(3.3, 7.7)) > density) continue;
            vec2 heart = offset + 0.2 + 0.6 * vec2(hash(key), hash(key + vec2(31.7, 11.3)));
            float radius = 0.3 + 0.38 * hash(key + vec2(5.1, 2.9));
            vec2 out_ = f - heart;
            float d = length(out_) / radius;
            if (d < best.x) best = vec4(d, hash(key + vec2(9.2, 1.4)), out_ / max(0.001, length(out_)));
        }
    }
    return best;
}

// Rot standing in the floor: glossy and near black, a slow shine running over it and scum floating on it.
vec3 ooze(vec2 p, float spore) {
    float t = HemoTime;
    float marble = noise(p * vec2(1.3, 2.1) + vec2(-t * 0.006, seed));
    vec3 colour = mix(OOZE, OLIVE * 0.45, marble * 0.6);
    float shine = smoothstep(0.62, 0.9, noise(p * vec2(2.0, 5.5) + vec2(t * 0.01, seed + 4.0)));
    colour += SHEEN * shine * 0.32;
    float scum = smoothstep(0.66, 0.78, fbm(p * 1.7 + vec2(t * 0.004, seed + 9.0)));
    colour = mix(colour, mix(CREAM * 0.62, CREAM, spore), scum * 0.8);
    return colour;
}

// A trench at full tear is this deep for each block of its mouth's width, and its walls lean in by this much for each
// block of depth (as Hemorath's trenches, HemorathFissureRules).
const float TRENCH_DEPTH = 0.8;
const float WALL_LEAN = 0.3;

// A trench looked into through its mouth (the trench idea, carried over from Hemorath's Iron Rake). The strip's frame in
// view space comes from the screen derivatives (a block along it, its whole width across, up out of the floor); the view
// ray is traced in that frame against the two leaning walls, the bottom and the ooze or coals standing on it. Nothing is
// real geometry under the floor, so the floor's blocks never hide it. c0 is the point's place across the channel and
// half the mouth's half-width, both as fractions of the strip's half-width. Alpha below zero: no frame here, paint flat.
vec4 traceTrench(float along, float c0f, float halfF, float open, float fill, float third, bool fire) {
    float v = uv.y;
    vec3 dpx = dFdx(viewPosition);
    vec3 dpy = dFdy(viewPosition);
    float dax = dFdx(along);
    float day = dFdy(along);
    float dvx = dFdx(v);
    float dvy = dFdy(v);
    float det = dax * dvy - dvx * day;
    if (abs(det) < 1.0e-14) return vec4(0.0, 0.0, 0.0, -1.0);
    vec3 ta = (dpx * dvy - dpy * dvx) / det;
    vec3 tc = (dpy * dax - dpx * day) / det;
    float width = length(tc);
    float run = length(ta);
    if (!(width > 0.05 && width < 10.0 && run > 0.25 && run < 4.0)) return vec4(0.0, 0.0, 0.0, -1.0);
    vec3 ea = ta / run;
    vec3 ec = tc / width;
    vec3 up = normalize(cross(ea, ec));
    if (dot(up, viewPosition) > 0.0) up = -up;
    vec3 ray = normalize(viewPosition);
    float ra = dot(ray, ea);
    float rc = dot(ray, ec);
    float ry = min(dot(ray, up), -0.002);

    float c0 = c0f * width * 0.5;
    float mouth = halfF * width * 0.5;
    float depth = TRENCH_DEPTH * mouth * 2.0 * smoothstep(0.0, 0.6, open);
    if (depth < 0.01) return vec4(0.0, 0.0, 0.0, -1.0);
    float far = 1.0e4;
    float dr = rc - WALL_LEAN * ry;
    float dl = rc + WALL_LEAN * ry;
    float tr = dr > 1.0e-4 ? (mouth - c0) / dr : far;
    float tl = dl < -1.0e-4 ? (-mouth - c0) / dl : far;
    float tw = min(tr, tl);
    float tb = depth / -ry;
    float th = min(tw, tb);
    float ah = along + ra * th;
    float ch = c0 + rc * th;
    float q = clamp(-ry * th / depth, 0.0, 1.0);
    float t = HemoTime;
    vec3 colour;
    if (tb <= tw) {
        if (fire) {
            // a bed of coals, their gaps glowing and throbbing, white-hot at the flash, ash on the cold ones
            float heat = fill;
            vec3 c = colonies(vec2(ah * 3.0, ch * 3.0) + seed);
            float gap = 1.0 - smoothstep(0.03, 0.12, c.y - c.x);
            float throb = 0.8 + 0.2 * sin(t * 0.35 + ah * 1.3 + c.z * 6.0);
            vec3 glow = mix(CRIMSON, HEAT, smoothstep(0.55, 0.9, heat));
            glow = mix(glow, WHITE_HOT, smoothstep(0.88, 1.0, heat) * 0.7);
            vec3 coal = mix(CHAR * 0.8, EMBER * (0.5 + heat), smoothstep(0.3, 0.6, heat) * c.z) * (0.7 + 0.5 * smoothstep(0.0, 0.4, c.x));
            colour = mix(coal, glow * throb, gap * heat);
            colour = mix(colour, ASH * 0.8, step(1.0 - 0.3 * third, hash(floor(vec2(ah, ch) * 16.0) + seed)) * (1.0 - gap));
        } else {
            // the bottom where no ooze stands: wet loam and roots
            float tone = noise(floor(vec2(ah, ch) * 16.0) * 0.25 + seed);
            colour = mix(ROT_DARK, OLIVE * 0.4, tone);
        }
    } else {
        // a wall, darker than the floor as a pit is: strata of loam and rot on the floor's sixteen texels, the cut edge
        // lit at the top, darkening down, the one wall turned to the light brighter than the other
        float wall = ch > 0.0 ? 1.0 : 0.0;
        float y = -ry * th;
        float yb = floor(y * 16.0) / 16.0;
        float ab = floor(ah * 16.0) / 16.0;
        float wobble = noise(vec2(ab * 0.9, seed + wall * 5.0)) * 0.7;
        float course = floor(yb * 5.0 + wobble);
        float strata = noise(vec2(ab * 1.7 + seed + course * 7.0, course * 3.1 + wall * 3.0));
        float lit = wall > 0.5 ? 1.0 : 0.62;
        if (fire) {
            colour = mix(SOOT * 1.6, CHAR * 1.5, strata) * lit;
            // ember seams along the courses, hotter toward the coals
            float seam = (1.0 - smoothstep(0.0, 0.12, fract(yb * 5.0 + wobble))) * smoothstep(0.55, 0.8, strata);
            colour = mix(colour, mix(EMBER, CRIMSON, fill), seam * fill * (0.3 + 0.7 * q));
            colour += CRIMSON * fill * q * q * 0.35;
        } else {
            vec3 band = mix(ROT_DARK * 1.6, OLIVE * 0.62, smoothstep(0.35, 0.65, strata));
            band = mix(band, OCHRE * 0.55, step(0.8, strata) * 0.8);
            colour = band * lit;
            // pale hyphae and root hairs hanging out of the cut, thickening as it fruits
            float hair = step(0.9 - 0.08 * third, hash(vec2(ab * 16.0, floor(yb * 8.0)) + seed + wall));
            colour = mix(colour, CREAM * 0.6, hair * (1.0 - q) * 0.8);
        }
        colour *= mix(1.0, 0.35, smoothstep(0.0, 0.85, q));
        float ledge = 1.0 - smoothstep(0.0, 0.12, fract(yb * 5.0 + wobble));
        colour *= 1.0 - 0.5 * ledge;
        float cutEdge = 1.0 - smoothstep(0.0, 0.03 + 0.03 * noise(vec2(ab * 4.0, seed + wall)), y);
        colour = mix(colour, fire ? ASH * 0.55 : mix(OCHRE, CREAM, 0.4) * 0.7, cutEdge * 0.75);
    }
    // the rot standing in the channel, rising from the bottom and never brimming: glossy, a cold sheen at a glancing
    // look, scum on it and an ochre meniscus where it meets the walls
    if (!fire) {
        float level = fill * 0.78 * depth;
        if (level > 0.004) {
            float ts = (depth - level) / -ry;
            if (ts < th) {
                float sa = along + ra * ts;
                float sc = c0 + rc * ts;
                float surface = max(mouth - WALL_LEAN * (depth - level), 0.02 * width);
                float thick = (th - ts) * -ry;
                vec3 pool = ooze(vec2(sa, sc * 2.0), third);
                pool += SHEEN * pow(1.0 + ry, 5.0) * 0.5;
                pool *= 0.75 + 0.25 * smoothstep(0.0, 0.35, (surface - abs(sc)) / max(surface, 0.001));
                pool = mix(pool, OCHRE * 0.85, exp(-max(surface - abs(sc), 0.0) / (0.025 * width)) * 0.8);
                colour = mix(colour, pool, smoothstep(0.0, 0.04 * width, thick) * 0.97);
            }
        }
    }
    return vec4(colour, 0.97);
}

void main() {
    float a1 = vertexColor.r;
    float a2 = vertexColor.g;
    float a3 = vertexColor.b;
    float strength = vertexColor.a * ColorModulator.a;
    float t = HemoTime;
    vec3 colour = ROT_DARK;
    float alpha = 0.0;

    if (shape < 0.5) {
        // ── MAT: the colony taking the floor ──
        float spread = a1, wet = a2, spore = a3;
        vec2 p = (uv - 1.0) * SPAN;
        vec2 q = texel(p);
        float lay = fbm(q * 0.55 + seed * 0.13);
        float reach = spread * 1.25 - 0.1;
        float cover = smoothstep(0.0, 0.14, reach - lay);
        float stain = smoothstep(-0.3, 0.0, reach - lay);
        // the damp the rot soaks into the floor first: dark, blotched, never a flat rug
        float damp = fbm(q * 1.1 + seed * 0.5 + 3.0);
        vec3 colony = mix(ROT_DARK * 1.1, OLIVE * 0.42, smoothstep(0.3, 0.8, damp));
        // round colonies of many sizes, their rims wobbling, more of them the further the rot has taken the floor
        vec2 warp = (vec2(fbm(q * 1.3 + seed), fbm(q * 1.3 + seed + 5.2)) - 0.5) * 0.5;
        float density = 0.35 + 0.5 * cover;
        // big colonies (up to a block and a half across) lie over the small satellites seeded between them
        vec4 big = moulds((q + warp) * 0.45, density * 0.85, 0.0);
        vec4 small = moulds((q + warp * 0.6) * 1.5 + 17.0, density * 0.55, 41.0);
        vec4 m = big.x < 1.0 ? big : small;
        float d = m.x + (noise(q * 9.0 + seed) - 0.5) * 0.14;
        float body = 1.0 - smoothstep(0.92, 1.04, d);
        // a dome, lit from the chamber's high side, darker on its far flank, a contact shadow round its foot
        float flank = smoothstep(0.25, 1.0, d);
        float shade = 0.82 + 0.42 * dot(m.zw, vec2(-0.6, -0.8)) * flank;
        float zone = d + (fbm(q * 3.0 + m.y * 9.0) - 0.5) * 0.26;
        vec3 heart = mix(MOULD * 0.5, OLIVE * 0.62, m.y);
        vec3 growth = mix(OCHRE * 0.72, YELLOW * 0.62, smoothstep(0.3, 0.8, noise(q * 2.4 + m.y * 7.0)));
        // every colony keeps its own proportions: some all green heart, some all ochre growth, some pale
        float inner = 0.18 + 0.4 * fract(m.y * 7.31);
        vec3 mould = mix(heart, growth, smoothstep(inner, inner + 0.16, zone));
        mould = mix(mould, CREAM * 0.55, step(0.82, m.y) * smoothstep(0.3, 0.6, zone) * 0.6);
        mould = mix(mould, OLIVE * 0.55, smoothstep(0.62, 0.72, zone) * (1.0 - smoothstep(0.72, 0.8, zone)) * 0.8);
        // the hyphal rim: pale cream fuzz, thin, broken by the texels
        float rim = smoothstep(0.74, 0.86, zone) * (1.0 - smoothstep(0.95, 1.04, d));
        mould = mix(mould, CREAM * (0.62 + 0.18 * spore), rim * step(0.35, hash(floor(p * 16.0) + seed + 2.0)));
        // sporulation: the hearts darken and speckle cream as the colony fruits
        float speck = step(1.0 - (0.03 + 0.16 * spore) * (1.0 - smoothstep(0.15, 0.4, d)), hash(floor(p * 16.0) + seed));
        mould = mix(mould, CREAM * 0.85, speck * 0.85);
        mould *= shade;
        float foot = smoothstep(0.98, 1.06, d) * (1.0 - smoothstep(1.06, 1.3, d));
        colony = mix(colony * (1.0 - 0.45 * foot), mould, body);
        // where the rot pools: low spots between the colonies fill with ooze first, an ochre meniscus round each
        float low = fbm(q * 0.8 + seed + 11.0) + body * 0.25;
        float level = wet * 0.72;
        float pooled = smoothstep(low - 0.03, low + 0.03, level) * step(0.02, wet);
        colony = mix(colony, ooze(q, spore), pooled);
        colony = mix(colony, OCHRE * 0.7, exp(-sq((level - low) / 0.025)) * step(0.02, wet) * 0.7);
        // the hyphal fringe where the colony is still taking hold
        float fringe = cover * (1.0 - cover) * 4.0 * smoothstep(0.35, 0.7, noise(q * 5.0 + seed));
        colour = mix(ROT_DARK * 1.2, colony, cover);
        colour = mix(colour, mix(OLIVE, CREAM, 0.45) * 0.7, fringe * 0.6);
        // the colonies stand thick; the damp between them lets a little of the floor through
        alpha = max(stain * 0.3, cover * mix(0.7 + 0.12 * damp, 0.95, max(body, pooled))) + fringe * 0.15;
    } else if (shape < 1.5 || (shape > 2.5 && shape < 3.5)) {
        // ── TRENCH and FURROW: the floor torn open along a strip ──
        bool fire = shape > 2.5;
        float open = a1;
        float along = uv.x * SPAN;
        float across = uv.y * 2.0 - 1.0;
        float wander = (noise(vec2(along * 0.6, seed + 3.0)) - 0.5) * 0.14;
        float x = abs(across + wander);
        float w = WALL * (0.82 + 0.3 * noise(vec2(along * 0.9, seed))) * (0.3 + 0.7 * open) * smoothstep(0.0, 0.3, open);
        float lip = 0.09 + 0.06 * noise(vec2(along * 3.0, seed + 7.0));
        float side = sign(across + wander);
        // traced for every fragment of the strip, so the screen derivatives are taken in uniform control flow
        vec4 traced = traceTrench(along, across + wander, w, open, a2, a3, fire);
        if (x < w && traced.a >= 0.0) {
            colour = traced.rgb;
            alpha = traced.a;
        } else if (x < w) {
            float depth = 1.0 - x / w;
            // the walls: strata in bands that follow the lips, the far wall catching the light, the near one in shadow
            float band = fract(depth * 3.6 + noise(vec2(along * 1.8, seed + 2.0)) * 0.45);
            float light = side > 0.0 ? 0.95 : 0.42;
            float deepening = 1.0 - 0.6 * smoothstep(0.0, 0.85, depth) * (0.4 + 0.6 * open);
            if (!fire) {
                vec3 wall = mix(ROT_DARK * 1.4, OLIVE * 0.75, step(0.55, band));
                wall = mix(wall, OCHRE * 0.7, step(0.86, band) * 0.8);
                wall *= light * deepening;
                // rot rising from the bottom, never brimming: the walls show above it
                float fill = a2, spore = a3;
                float level = 1.0 - fill * 0.78;
                float wet = smoothstep(level - 0.04, level + 0.04, depth) * step(0.02, fill);
                vec3 pool = ooze(vec2(along, across * 2.0), spore);
                pool += CREAM * 0.2 * spore * smoothstep(0.75, 1.0, depth);
                colour = mix(wall, pool, wet);
                colour = mix(colour, OCHRE * 0.9, exp(-sq((depth - level) / 0.035)) * step(0.02, fill) * 0.85);
            } else {
                float heat = a2, ash = a3;
                vec3 wall = mix(SOOT, CHAR * 1.3, step(0.55, band)) * (0.6 + 0.4 * light) * deepening;
                // ember seams in the walls, hotter toward the bottom
                float seam = step(0.9, band) * heat * smoothstep(0.2, 0.7, depth);
                wall = mix(wall, mix(EMBER, CRIMSON, heat), seam);
                // the bed: coals with glowing gaps between them, white-hot at the commit's flash
                vec3 c = colonies(vec2(along * 3.0, across * 2.4) + seed);
                float gap = 1.0 - smoothstep(0.03, 0.12, c.y - c.x);
                float throb = 0.8 + 0.2 * sin(t * 0.35 + along * 1.3 + c.z * 6.0);
                vec3 glow = mix(CRIMSON, HEAT, smoothstep(0.55, 0.9, heat));
                glow = mix(glow, WHITE_HOT, smoothstep(0.88, 1.0, heat) * 0.7);
                vec3 coal = mix(CHAR * 0.8, EMBER * (0.5 + heat), smoothstep(0.3, 0.6, heat) * c.z);
                vec3 bed = mix(coal, glow * throb, gap * heat);
                bed = mix(bed, ASH * 0.8, step(1.0 - 0.3 * ash, hash(floor(vec2(along, across) * 16.0) + seed)) * (1.0 - gap));
                colour = mix(wall, bed, smoothstep(0.45, 0.6, depth));
            }
            alpha = 0.96;
        } else if (x < w + lip) {
            // the torn lip, raised and lit along its top
            float l = (x - w) / lip;
            if (!fire) {
                // pale shelf frills scalloped along the lip, dark between them
                float lobe = fract(along * 4.0 + noise(vec2(along * 1.5, seed)) * 0.8);
                float frill = 1.0 - smoothstep(0.3, 0.45, length(vec2(lobe - 0.5, (l - 0.35) * 0.9)));
                vec3 turf = mix(OCHRE * 0.75, CREAM * 0.9, 1.0 - l) * (0.7 + 0.3 * (side > 0.0 ? 1.0 : 0.6));
                colour = mix(ROT_DARK * 1.5, turf, frill * 0.85 + 0.15);
                colour += CREAM * 0.25 * a3 * frill;
            } else {
                // blistered char, its crest gone to ash, embers caught in it
                float blister = noise(vec2(along * 6.0, l * 4.0) + seed);
                colour = mix(CHAR, ASH * 0.75, (1.0 - l) * smoothstep(0.35, 0.75, blister) * (0.4 + a3 * 0.6));
                colour = mix(colour, CRIMSON, step(0.82, blister) * a2 * 0.7);
            }
            alpha = 0.92;
        } else {
            // the stain the trench spreads into the floor round it
            float fall = 1.0 - smoothstep(w + lip, 1.0, x);
            vec2 q = texel(vec2(along, across * 1.5));
            float crevice;
            vec3 halo = fire ? mix(SOOT, CHAR, noise(q * 3.0 + seed)) : mix(ROT_DARK * 1.3, colonyColour(q, crevice), 0.5);
            float speck = step(1.0 - 0.12 * a3, hash(floor(vec2(along, across) * 16.0) + seed + 3.0));
            halo = mix(halo, fire ? ASH : CREAM, speck * 0.7);
            colour = halo;
            alpha = fall * (fire ? 0.6 : 0.55) * (0.5 + 0.5 * open);
        }
    } else if (shape > 3.5) {
        // ── STAIN: the dark wet ring a pool of sludge soaks into the floor, a mould-green fringe at its edge ──
        float beyond = a1, wet = a2, fringe = a3;
        vec2 p = (uv - 1.0) * SPAN;
        vec2 q = texel(p);
        float edge = beyond + (fbm(q * 1.4 + seed) - 0.5) * 0.4;
        float soak = 1.0 - smoothstep(0.5, 0.95, edge);
        vec3 damp = mix(OOZE * 1.2, OLIVE * 0.38, noise(q * 2.0 + seed) * 0.6);
        float ring = smoothstep(0.42, 0.72, edge) * (1.0 - smoothstep(0.8, 1.02, edge));
        // (polish: sparser, darker tufts, so the fringe frays into the moss instead of outlining the pool)
        float tuft = step(0.6, hash(floor(p * 16.0) + seed + 5.0)) * step(0.4, noise(q * 1.1 + seed + 9.0));
        colour = mix(damp, STAIN_MOULD * (0.55 + 0.25 * noise(q * 3.0 + seed)), ring * fringe * tuft * 0.8);
        alpha = (soak * 0.78 + ring * 0.4 * fringe * tuft) * wet;
    } else {
        // ── SCORCH: the fire eating the rot back to black floor ──
        float burn = a1, heat = a2, ash = a3;
        vec2 p = (uv - 1.0) * SPAN;
        vec2 q = texel(p);
        float lay = fbm(q * 0.6 + seed * 0.17 + 5.0);
        float reach = burn * 1.25 - 0.1;
        float burnt = smoothstep(0.0, 0.1, reach - lay);
        float stain = smoothstep(-0.25, 0.0, reach - lay);
        // the burning line where the char is still eating outward
        float line = exp(-sq((reach - lay) / 0.045)) * step(0.01, burn);
        // big plates of char, alligatored; their cracks glow only where the bed under them is still hot, so the heat
        // lies in a few burning seams and smouldering patches, not a web over the whole floor
        vec2 w = (vec2(fbm(q * 0.9 + seed), fbm(q * 0.9 + seed + 4.4)) - 0.5) * 0.6;
        vec3 c = colonies((q + w) * 0.75 + seed);
        float crack = 1.0 - smoothstep(0.015, 0.06, c.y - c.x);
        float hot = smoothstep(0.5, 0.66, fbm(q * 0.5 + seed + 8.0) + heat * 0.06);
        // each plate curls a little: lit along its high edge, sooty in its hollow
        float curl = smoothstep(0.0, 0.5, c.x);
        vec3 black = mix(SOOT, CHAR * 1.2, noise(q * 3.0 + seed) * 0.6 + c.z * 0.3) * (0.8 + 0.4 * curl);
        black = mix(black, ASH * 0.5, step(1.0 - 0.16 * ash, hash(floor(p * 16.0) + seed)) * 0.8);
        float throb = 0.78 + 0.22 * sin(t * 0.3 + c.z * 9.0);
        vec3 glow = mix(EMBER, CRIMSON, smoothstep(0.1, 0.5, heat));
        glow = mix(glow, HEAT, smoothstep(0.78, 0.96, heat) * hot);
        glow = mix(glow, WHITE_HOT, smoothstep(0.9, 1.0, heat) * 0.6 * hot);
        // the smoulder under the plates where it is hot, the seams brightest
        black = mix(black, EMBER * (0.6 + 0.6 * heat), hot * (1.0 - smoothstep(0.0, 0.2, c.y - c.x)) * smoothstep(0.1, 0.4, heat) * 0.7);
        // a cold seam is only a sooty line between the plates; a hot one glows
        black = mix(black, SOOT * 0.6, crack * (1.0 - hot) * 0.8);
        black = mix(black, glow * throb, crack * smoothstep(0.08, 0.3, heat) * hot);
        vec3 edge = mix(CRIMSON, HEAT, heat);
        colour = mix(SOOT * 1.4, black, burnt);
        colour = mix(colour, edge, line * (0.35 + 0.65 * heat));
        alpha = max(stain * 0.35, burnt * 0.92) + line * 0.5;
    }

    alpha *= strength;
    if (alpha < 0.003) discard;
    fragColor = linear_fog(vec4(colour * ColorModulator.rgb, clamp(alpha, 0.0, 1.0)), vertexDistance, FogStart, FogEnd, FogColor);
}
