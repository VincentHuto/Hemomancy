#version 150
#moj_import <fog.glsl>

// Saint Seraphae's floor healing over (visual pass 2): healing that hurts. Shape 0 is a furrow laid along a strip, the
// floor opened like an incision: U is the distance along it in blocks / 32, V runs across the strip from one side to
// the other. The cut down its middle is a sunken channel looked into through its mouth (Hemorath's trench, carried
// over): raw walls of torn flesh, and white light welling up from its floor and standing in it, white-hot at the
// commit; swollen lips run either side and the pallor fades round them. Shape 1 is a patch of overgrowth: U and V are
// its ground position in blocks / 32 about the patch's centre, plus one; lumps of new tissue of every size swell up and
// crowd together, wet and lit from above, raw in the crevices. Shape 2 is a vessel lying on the floor: U along it in
// blocks / 32, V across it; a raised dark vein with knots of white light running outward in it. The vertex colour
// carries state, never a tint: red is how swollen the tissue stands, green the white light welling in it (from 0.9 the
// hot core), blue how raw and torn it is, and alpha its strength. Ivory, pale flesh, bruise, raw red and white light
// only: gold is a nerve firing and is never drawn here (it rides the Ductilis material).

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
const vec3 IVORY = vec3(0.880, 0.820, 0.740);
const vec3 FLESH = vec3(0.800, 0.580, 0.540);
const vec3 BRUISE = vec3(0.380, 0.220, 0.260);
const vec3 SHADOW = vec3(0.110, 0.050, 0.055);
const vec3 RAW = vec3(0.620, 0.090, 0.110);
const vec3 CLOT = vec3(0.220, 0.020, 0.040);
const vec3 LIGHT = vec3(1.000, 0.985, 0.955);
const vec3 HOT = vec3(1.000, 0.824, 0.769);
// The cut at full swell is this deep for each block of its width, and its walls lean in by this much a block of depth.
const float FURROW_DEPTH = 0.75;
const float WALL_LEAN = 0.3;
// The cut's mouth, as a share of the strip's half-width (the lips' outer foot, the footprint's edge, is 1 / HALO).
const float MOUTH = 0.46;
const float FOOT = 0.7407;

float sq(float x) {
    return x * x;
}

// ── overgrowth: soft-unioned domes of varied size at two scales ──────────────────────────────────────────────

float domes(vec2 p, float cover, float salt) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    float keep = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 o = vec2(float(x), float(y));
            vec2 key = cell + o + salt;
            vec2 centre = o + 0.12 + 0.76 * vec2(hash(key + seed * 0.31), hash(key + vec2(41.7, 3.9) + seed * 0.13));
            float size = hash(key + vec2(7.3, 19.1));
            float radius = 0.38 + 0.55 * size;
            float on = step(hash(key + vec2(3.1, 8.7) + seed * 0.07), cover);
            float d = length(f - centre) / radius;
            float bump = on * sqrt(max(0.0, 1.0 - d * d)) * (0.55 + 0.45 * size);
            keep = 1.0 - (1.0 - keep) * (1.0 - bump);
        }
    }
    return keep;
}

// How high the new tissue stands at p (blocks): big lumps, with small ones crowding the gaps and riding on them.
float lumps(vec2 p, float cover) {
    vec2 warp = vec2(noise(p * 0.9 + seed), noise(p * 0.9 + seed + 17.0)) - 0.5;
    vec2 q = p + warp * 0.5;
    float big = domes(q * 1.35, cover, 0.0);
    float small = domes(q * 3.1 + 5.3, min(1.0, cover * 1.2), 31.0);
    return clamp(big + small * 0.42 * (1.0 - big * 0.5), 0.0, 1.2);
}

// Wet new tissue lit from above: h how high it stands, n its normal (z up), spec its gloss.
vec3 flesh(float h, vec3 n, float light, float raw, float grain) {
    vec3 sun = normalize(vec3(-0.45, -0.35, 0.82));
    float diffuse = max(dot(n, sun), 0.0);
    vec3 base = mix(mix(BRUISE, RAW, 0.35 + 0.4 * raw), mix(FLESH, IVORY, 0.35), smoothstep(0.05, 0.75, h));
    base *= 0.92 + 0.16 * grain;
    vec3 colour = base * (0.28 + 0.85 * diffuse);
    float spec = pow(max(dot(reflect(-sun, n), vec3(0.0, 0.0, 1.0)), 0.0), 22.0);
    colour += vec3(1.0, 0.95, 0.92) * spec * (0.45 - 0.25 * raw);
    // the light under stretched new skin glows through its thinnest tops
    colour += LIGHT * light * 0.22 * smoothstep(0.55, 1.0, h);
    return colour;
}

// ── the furrow ───────────────────────────────────────────────────────────────────────────────────────────────

// The swollen lip either side of the cut: s runs 0 at the cut's edge to 1 at the footprint's edge.
vec4 lip(float along, float c, float s, float swell, float light, float raw) {
    float grain = noise(floor(vec2(along, c) * 16.0) + seed);
    float h = sin(3.14159 * pow(s, 0.8)) * min(1.0, swell);
    // the inner face falls into the cut in shadow; the crest catches the light; the outer face is in half light
    float slope = cos(3.14159 * pow(s, 0.8));
    vec3 n = normalize(vec3(0.0, -slope * 1.4 * min(1.0, swell) * sign(c), 1.0));
    vec3 colour = flesh(0.25 + 0.55 * h, n, light * 0.6, raw * (1.0 - s), grain);
    // the inner face falls away into the cut: dark, so the cut reads as a hole and not a painted band
    colour *= mix(0.35, 1.0, smoothstep(0.0, 0.35, s));
    // torn and raw where the skin parts at the cut
    colour = mix(colour, RAW * 0.75, raw * (1.0 - smoothstep(0.0, 0.3, s)) * 0.85);
    // a vessel bulging under the lip, pulsing with the light
    float run = 0.5 + 0.5 * sin(along * 1.7 - HemoTime * 0.2 + seed);
    float vein = exp(-sq((s - 0.55) / 0.09));
    colour = mix(colour, mix(BRUISE * 0.8, LIGHT * 0.8, light * run), vein * 0.55);
    return vec4(colour, 0.94);
}

// The pallor and bruising of the stretched floor beyond the lips, outside the footprint: k 0 at the foot to 1 out.
vec4 pallor(float along, float c, float k, float swell, float light, float raw) {
    float mottle = fbm(vec2(along * 1.3, c * 4.0) + seed);
    vec3 colour = mix(BRUISE, FLESH * 0.85, 0.3 + 0.35 * mottle) + LIGHT * light * 0.06;
    float alpha = (1.0 - smoothstep(0.0, 1.0, k)) * (0.28 + 0.22 * raw) * smoothstep(0.0, 0.3, swell) * (0.6 + 0.4 * mottle);
    return vec4(colour, alpha);
}

// A furrow, looked into through its mouth (Hemorath's trench, carried over). The strip's frame in view space comes from
// the screen derivatives (a block along, the strip's whole width across, up out of the floor); the view ray is traced
// in that frame against the two leaning walls, the floor of the cut and the light standing in it. Nothing is geometry
// under the floor, so the floor's own blocks never hide it.
vec4 furrow(float swell, float light, float raw) {
    float along = uv.x * SPAN;
    float v = uv.y;
    vec3 dpx = dFdx(viewPosition);
    vec3 dpy = dFdy(viewPosition);
    float dax = dFdx(along);
    float day = dFdy(along);
    float dvx = dFdx(v);
    float dvy = dFdy(v);
    float det = dax * dvy - dvx * day;
    float across = v * 2.0 - 1.0;
    float a = abs(across);
    vec3 glow = mix(LIGHT, HOT, smoothstep(0.9, 1.0, light));
    if (abs(det) < 1.0e-14) return vec4(mix(SHADOW, glow, light * 0.5), 0.9 * (1.0 - smoothstep(FOOT, 1.0, a)));
    vec3 ta = (dpx * dvy - dpy * dvx) / det;
    vec3 tc = (dpy * dax - dpx * day) / det;
    float width = length(tc);
    float run = length(ta);
    float c0 = across * width * 0.5;
    // the mouth wanders and its edge is ragged; it opens as the swelling rises and gapes at the flash
    float jag = noise(vec2(along * 2.1, seed + step(0.0, c0) * 11.0)) * 0.6 + noise(vec2(along * 6.3, seed + 3.0)) * 0.4;
    float open = smoothstep(0.08, 0.45, swell);
    float mouthFrac = (MOUTH - 0.07 * jag) * open;
    float mouth = mouthFrac * width * 0.5;
    if (a >= FOOT) return pallor(along, c0, (a - FOOT) / (1.0 - FOOT), swell, light, raw);
    if (a >= mouthFrac) return lip(along, c0, (a - mouthFrac) / max(FOOT - mouthFrac, 0.001), swell, light, raw);
    float depth = FURROW_DEPTH * mouth * 2.0 * (0.55 + 0.45 * min(1.0, swell));
    if (!(width > 0.1 && width < 8.0 && run > 0.25 && run < 4.0) || depth < 0.01) {
        return vec4(mix(SHADOW, glow, light * exp(-sq(across / 0.2))), 0.95);
    }
    vec3 ea = ta / run;
    vec3 ec = tc / width;
    vec3 up = normalize(cross(ea, ec));
    if (dot(up, viewPosition) > 0.0) up = -up;
    vec3 ray = normalize(viewPosition);
    float ra = dot(ray, ea);
    float rc = dot(ray, ec);
    float ry = min(dot(ray, up), -0.002);

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
    float y = -ry * th;
    float q = clamp(y / depth, 0.0, 1.0);
    float throb = 0.8 + 0.2 * sin(HemoTime * 0.3 + ah * 0.9 + seed);
    float level = depth * (0.2 + 0.35 * light);
    vec3 colour;
    if (tb <= tw) {
        // the floor of the cut: granulation, raw lumps packed tight, under the light
        float g = lumps(vec2(ah, ch) * 2.2, 0.9);
        colour = mix(CLOT, RAW * 0.8, g) * (0.5 + 0.5 * g);
    } else {
        // a wall of torn flesh: pale torn skin at the top, then raw muscle in fibres running along the cut, darkening
        // down, wet, lit from beneath by the light standing in the cut. 16 texels a block, on the floor's own grid.
        float wall = ch > 0.0 ? 1.0 : 0.0;
        float yb = floor(y * 16.0) / 16.0;
        float ab = floor(ah * 16.0) / 16.0;
        float fibre = noise(vec2(ab * 0.7 + seed + wall * 9.0, yb * 9.0));
        float tear = smoothstep(0.62, 0.8, noise(vec2(ab * 2.5, yb * 3.0 + wall * 5.0 + seed)));
        vec3 muscle = mix(RAW * 0.95, CLOT * 1.6, fibre * 0.7) * (0.75 + 0.5 * fibre);
        colour = mix(muscle, SHADOW, tear * 0.7);
        colour *= mix(1.0, 0.35, smoothstep(0.1, 0.95, q));
        float skin = 1.0 - smoothstep(0.0, 0.03 + 0.05 * noise(vec2(ab * 4.0, seed + wall)), y);
        colour = mix(colour, mix(IVORY, FLESH, 0.5) * 0.9, skin * 0.85);
        float wetShine = smoothstep(0.75, 0.95, noise(vec2(ab * 3.0 - HemoTime * 0.03, yb * 6.0 + wall)));
        colour += vec3(0.55, 0.32, 0.30) * wetShine * 0.3 * (1.0 - q);
        float above = max(depth - level - y, 0.0);
        colour *= 0.75;
        colour += glow * light * 0.6 * exp(-above / (0.14 * width)) * throb;
        // at the flash the whole cut glows from within, so it reads from a player's eye when the light itself is hidden
        colour += glow * light * light * 0.28 * (1.0 - 0.5 * q) * throb;
    }
    // the light standing in the cut: milky and lit from within, white-hot at the flash, a lit meniscus at the walls
    if (light > 0.02) {
        float ts = (depth - level) / -ry;
        if (ts < th) {
            float sa = along + ra * ts;
            float sc = c0 + rc * ts;
            float surface = max(mouth - WALL_LEAN * (depth - level), 0.02 * width);
            float marble = noise(vec2(sa * 1.7 - HemoTime * 0.05, sc * 3.0 + seed));
            vec3 pool = mix(mix(FLESH, LIGHT, 0.75), glow, 0.45 + 0.55 * smoothstep(0.5, 1.0, light)) * (0.85 + 0.2 * marble);
            float core = exp(-sq(sc / max(surface * 0.45, 0.01)));
            pool = mix(pool * 0.8, glow * 1.3, core * light * throb);
            pool += LIGHT * 0.35 * exp(-max(surface - abs(sc), 0.0) / (0.025 * width));
            pool *= 0.55 + 0.45 * light;
            float thick = (th - ts) * -ry;
            colour = mix(colour, pool, smoothstep(0.0, 0.04 * width, thick) * 0.96);
        }
    }
    return vec4(colour, 0.97);
}

void main() {
    float swell = vertexColor.r;
    float light = vertexColor.g;
    float raw = vertexColor.b;
    float strength = vertexColor.a * ColorModulator.a;
    float t = HemoTime;
    vec3 glow = mix(LIGHT, HOT, smoothstep(0.9, 1.0, light));
    vec3 colour = SHADOW;
    float alpha = 0.0;

    if (shape < 0.5) {
        vec4 cut = furrow(swell, light, raw);
        colour = cut.rgb;
        alpha = cut.a;
    } else if (shape < 1.5) {
        // Overgrowth: lumps of new tissue taking the floor, first where the field favours them, then everywhere.
        vec2 p = (uv - 1.0) * SPAN;
        float field = fbm(p * 0.32 + seed);
        float cover = clamp(swell * 1.15 - (1.0 - field) * 0.55, 0.0, 1.0);
        float e = 0.035;
        float h = lumps(p, cover);
        float hx = lumps(p + vec2(e, 0.0), cover);
        float hy = lumps(p + vec2(0.0, e), cover);
        vec3 n = normalize(vec3(-(hx - h) / e * 0.22, -(hy - h) / e * 0.22, 1.0));
        float grain = noise(floor(p * 16.0) + seed);
        float tissue = smoothstep(0.02, 0.16, h);
        vec3 skin = flesh(h, n, light, raw, grain);
        // between the lumps: raw and dark, lit from below by the light
        vec3 under = mix(CLOT, mix(BRUISE, RAW, raw), 0.4 + 0.4 * field) + glow * light * 0.35;
        colour = mix(under, skin, tissue);
        float stain = smoothstep(0.25, 0.7, field) * min(1.0, swell);
        alpha = max(tissue * 0.96, (0.2 * stain + 0.12 * light) * smoothstep(0.0, 0.3, swell) * (1.0 - tissue));
    } else {
        // A vessel lying on the floor: a raised dark vein, lit on its top, knots of light running outward in it.
        float along = uv.x * SPAN;
        float a = abs(uv.y * 2.0 - 1.0);
        float tube = sqrt(max(0.0, 1.0 - a * a));
        vec3 sheath = mix(CLOT * 1.3, mix(RAW, BRUISE, 0.35), tube) * (0.45 + 0.7 * tube);
        sheath += mix(FLESH, IVORY, 0.3) * pow(tube, 10.0) * 0.4;
        float flow = fract(along * 0.6 - t * 0.09 + seed * 0.13);
        float bead = exp(-sq((flow - 0.5) * 4.0));
        float core = exp(-sq(a / 0.35)) * light * (0.35 + 0.65 * bead);
        colour = mix(sheath, glow, clamp(core * 1.2, 0.0, 1.0));
        alpha = (1.0 - smoothstep(0.7, 1.0, a)) * 0.95;
    }

    alpha *= strength;
    if (alpha < 0.003) discard;
    fragColor = linear_fog(vec4(colour * ColorModulator.rgb, clamp(alpha, 0.0, 1.0)), vertexDistance, FogStart, FogEnd, FogColor);
}
