#version 150
#moj_import <fog.glsl>

// Saint Hemorath's floor answering him: the bone and venous stone of his chamber cracking along a move's footprint.
// Shape 0 is one crack laid along a strip: U is the distance along it in blocks / 32, V runs across it from one side
// of the strip to the other. Shape 1 is a crazed patch of floor: U and V are its ground position in blocks / 32 about
// the patch's centre, plus one. The vertex colour carries the crack's state, never a tint: red is the heat of the lit
// seam (above 0.85 it runs white-hot, the commit's flash), green how far blood has welled up into the gap, blue the
// rust seeping from its lips, and alpha its strength. Dried crimson, black-red clot, rust and pale bone dust only.
// Shape 2 is a trench (visual pass 2b): its mouth lies on the floor exactly over a lane's hit strip, U the distance along
// it in blocks / 32 and V 0 to 1 from one edge to the other, and the channel is looked into under it. There blue is how
// far the floor has dropped (0 flat to 1 full depth) instead of rust, and green how high blood stands as a share of the
// depth.

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
const vec3 SCORCH = vec3(0.040, 0.007, 0.009);
const vec3 CLOT = vec3(0.090, 0.006, 0.012);
const vec3 CRIMSON = vec3(0.330, 0.030, 0.036);
const vec3 HOT = vec3(0.960, 0.110, 0.070);
const vec3 CORE = vec3(1.000, 0.820, 0.760);
const vec3 WET = vec3(0.300, 0.012, 0.030);
const vec3 RUST = vec3(0.440, 0.160, 0.110);
const vec3 BONE = vec3(0.620, 0.550, 0.450);
// A trench at full drop is this deep for each block of its width (HemorathFissureRules.TRENCH_DEPTH), and its walls
// lean in by this much for each block of depth.
const float TRENCH_DEPTH = 1.0;
const float WALL_LEAN = 0.35;

float sq(float x) {
    return x * x;
}

// The gap between the nearest and second-nearest cell centres: small along the cell walls, the crazing's hairlines.
float crazing(vec2 p) {
    vec2 cell = floor(p);
    vec2 f = fract(p);
    float a = 8.0;
    float b = 8.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 id = cell + offset;
            vec2 centre = offset + vec2(hash(id + seed * 0.37), hash(id + vec2(19.3, 7.1) + seed * 0.11));
            float d = length(centre - f);
            if (d < a) {
                b = a;
                a = d;
            } else {
                b = min(b, d);
            }
        }
    }
    return b - a;
}

// Blood standing in a gap: glossy and near black, lit from the seam beneath, a streak of shine running with the flow.
vec3 standing(float along, float across, float heat) {
    float t = HemoTime;
    float marble = noise(vec2(along * 2.1 - t * 0.04, across * 3.0 + seed));
    vec3 colour = mix(CLOT * 1.4, WET * 1.15, marble);
    colour += HOT * 0.14 * heat;
    float streak = exp(-sq((across - 0.3) * 9.0)) * smoothstep(0.35, 0.8, noise(vec2(along * 3.0 - t * 0.12, seed + 4.0)));
    colour += vec3(0.85, 0.42, 0.40) * streak * 0.5;
    return colour;
}

// The floor of a trench's footprint outside its channel: crazed into shards, crumbling dark toward the channel, and
// split along the footprint's own edge by a ragged crack that runs hot with the seam. k runs from 0 at the channel's
// broken edge to 1 at the footprint's edge, so before the floor drops the whole strip reads as split, crazed floor.
vec4 brokenLip(float along, float across, float k, float open, float heat) {
    vec2 p = vec2(along, across) * 2.6;
    float lines = 1.0 - smoothstep(0.02, 0.07, crazing(p + seed * 0.5));
    float grain = noise(floor(vec2(along, across) * 16.0) + seed);
    vec3 tone = mix(SCORCH * 2.4, BONE * 0.3, 0.3 + 0.4 * grain);
    tone = mix(tone, CLOT * 1.2, (1.0 - smoothstep(0.0, 0.5, k)) * open);
    vec3 colour = mix(tone, mix(CLOT, HOT * 0.8, heat * 0.5), lines);
    float rim = smoothstep(0.86, 0.97, k);
    colour = mix(colour, mix(CLOT * 0.8, HOT * 0.9, heat * heat * 0.6), rim);
    return vec4(colour, 0.8 + 0.17 * max(lines, rim));
}

// Where the seam runs along a trench's bottom, wandering about its middle.
float seamLine(float along, float floorHalf) {
    return (noise(vec2(along * 0.9, seed + 6.0)) - 0.5) * floorHalf * 0.9;
}

// A trench, looked into through its mouth. The strip's frame in view space comes from the screen derivatives (a block
// along it, its whole width across, and up out of the floor); the view ray is traced in that frame against the two
// leaning walls, the bottom and the blood standing on it; and what it meets is shaded: the cut floor at the top of the
// walls darkening down into dried crimson, broken rubble on the bottom split by the seam, and glossy blood lit from the
// seam beneath. Nothing here is real geometry under the floor, so the floor's own blocks never hide it.
vec4 trench(float heat, float blood, float drop) {
    float along = uv.x * SPAN;
    float v = uv.y;
    vec3 dpx = dFdx(viewPosition);
    vec3 dpy = dFdy(viewPosition);
    float dax = dFdx(along);
    float day = dFdy(along);
    float dvx = dFdx(v);
    float dvy = dFdy(v);
    float det = dax * dvy - dvx * day;
    if (abs(det) < 1.0e-14) return vec4(CLOT, 0.85);
    vec3 ta = (dpx * dvy - dpy * dvx) / det;
    vec3 tc = (dpy * dax - dpx * day) / det;
    float width = length(tc);
    float run = length(ta);
    if (!(width > 0.1 && width < 8.0 && run > 0.25 && run < 4.0)) return vec4(CLOT, 0.85);
    vec3 ea = ta / run;
    vec3 ec = tc / width;
    vec3 up = normalize(cross(ea, ec));
    if (dot(up, viewPosition) > 0.0) up = -up;
    vec3 ray = normalize(viewPosition);
    float ra = dot(ray, ea);
    float rc = dot(ray, ec);
    float ry = min(dot(ray, up), -0.002);

    // the channel's mouth: ragged, a broken lip left on either side, opening as the floor drops
    float c0 = (v - 0.5) * width;
    float side = c0 < 0.0 ? 0.0 : 1.0;
    float jag = noise(vec2(along * 2.3, seed + side * 11.0)) * 0.6 + noise(vec2(along * 7.1, seed + 3.0 + side * 5.0)) * 0.4;
    float open = smoothstep(0.0, 0.45, drop);
    float mouth = (width * 0.5 - width * (0.05 + 0.08 * jag)) * open;
    float depth = TRENCH_DEPTH * width * drop;
    if (abs(c0) >= mouth || depth < 0.01) {
        float k = clamp((abs(c0) - mouth) / max(width * 0.5 - mouth, 0.001), 0.0, 1.0);
        return brokenLip(along, c0, k, open, heat);
    }

    // the walls lean in as they go down: c = +-(mouth + WALL_LEAN * y) with y negative under the floor
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
    float floorHalf = max(mouth - WALL_LEAN * depth, 0.02 * width);
    float throb = 0.82 + 0.18 * sin(HemoTime * 0.25 + ah * 0.8 + seed);
    float whiteHot = smoothstep(0.85, 1.0, heat);
    vec3 colour;
    if (tb <= tw) {
        // the bottom: broken rubble, dark, split down its length by the seam
        vec2 rb = vec2(ah, ch) * 3.4;
        float gaps = 1.0 - smoothstep(0.0, 0.09, crazing(rb + seed * 0.3 + 2.0));
        float tone = noise(rb * 0.8 + seed);
        colour = mix(CLOT * 1.5, SCORCH * 2.6 + RUST * 0.08, tone) * (1.0 - 0.6 * gaps);
        float seamDistance = abs(ch - seamLine(ah, floorHalf));
        float seamBand = 1.0 - smoothstep(0.015 * width, 0.07 * width, seamDistance);
        float hair = 1.0 - smoothstep(0.0, 0.02 * width, seamDistance);
        colour += HOT * 0.3 * heat * exp(-seamDistance / (0.12 * width));
        colour = mix(colour, mix(HOT, CORE, whiteHot * hair), seamBand * heat * throb);
    } else {
        // a wall, darker than the floor as a pit is: the floor's own brick cut off at the top with a thin lit edge, then
        // broken courses of stone darkening down, split by fractures, and lit red from beneath by the blood and the seam.
        // 16 texels a block down the wall and along it, so the strata sit on the floor's own grid.
        float wall = ch > 0.0 ? 1.0 : 0.0;
        float y = -ry * th;
        float yb = floor(y * 16.0) / 16.0;
        float ab = floor(ah * 16.0) / 16.0;
        float wobble = noise(vec2(ab * 0.9, seed + wall * 5.0)) * 0.7;
        float course = floor(yb * 4.0 + wobble);
        float strata = noise(vec2(ab * 1.7 + seed + course * 7.0, course * 3.1 + wall * 3.0));
        float fracture = 1.0 - smoothstep(0.0, 0.08, crazing(vec2(ab * 2.2, yb * 3.0) + seed * 0.4 + wall * 7.0));
        vec3 stone = mix(vec3(0.120, 0.098, 0.090), vec3(0.150, 0.080, 0.070), smoothstep(0.2, 0.8, q));
        colour = stone * (0.7 + 0.6 * strata) * (1.0 - 0.6 * fracture);
        // the pit darkens down, and each course's ledge throws a dark line
        colour *= mix(1.0, 0.35, smoothstep(0.0, 0.85, q));
        float ledge = 1.0 - smoothstep(0.0, 0.1, fract(yb * 4.0 + wobble));
        colour *= 1.0 - 0.65 * ledge;
        // the cut edge at the top catches the light, ragged
        float cutEdge = 1.0 - smoothstep(0.0, 0.025 + 0.035 * noise(vec2(ab * 4.0, seed + wall)), y);
        colour = mix(colour, vec3(0.30, 0.25, 0.22), cutEdge * 0.7);
        // chunks broken out of the wall's face: dark pockets, one course tall, here and there
        float pocket = step(0.74, noise(vec2(floor(ab * 3.0), course + seed * 0.3 + wall * 11.0)));
        colour *= 1.0 - 0.55 * pocket * smoothstep(0.1, 0.3, q);
        // red light off the blood and the seam below
        float pool = blood * depth;
        float above = max(depth - pool - y, 0.0);
        colour += (WET * 0.9 + HOT * 0.12 * heat) * exp(-above / (0.1 * width)) * step(0.004, pool);
        colour += HOT * heat * heat * q * q * q * 0.35 * throb;
    }

    // the blood standing in the channel, glossy, with the seam glowing up through it, a lit meniscus where it meets
    // the walls, and a brighter leading tongue where it runs thin
    float level = blood * depth;
    if (level > 0.004) {
        float ts = (depth - level) / -ry;
        if (ts < th) {
            float sa = along + ra * ts;
            float sc = c0 + rc * ts;
            float surface = max(mouth - WALL_LEAN * (depth - level), 0.02 * width);
            float thick = (th - ts) * -ry;
            // dark oxblood, the walls' shadow on it, a cold sheen at a glancing look and the seam only as a buried line
            float marble = noise(vec2(sa * 2.1 - HemoTime * 0.04, sc * 3.0 + seed));
            vec3 pool = mix(WET * 0.8, CRIMSON * 0.85, marble);
            float sheen = pow(1.0 + ry, 5.0);
            float glint = smoothstep(0.55, 0.85, noise(vec2(sa * 1.4 - HemoTime * 0.09, sc * 6.0 + seed + 4.0)));
            pool += vec3(0.62, 0.30, 0.30) * sheen * (0.35 + 0.65 * glint);
            pool += vec3(0.45, 0.16, 0.15) * glint * 0.18;
            float seamDistance = abs(sc - seamLine(sa, floorHalf));
            pool += mix(HOT, CORE, whiteHot * 0.6) * heat * throb * exp(-seamDistance / (0.07 * width))
                    * exp(-level / max(0.45 * depth, 0.01));
            pool += HOT * 0.12 * heat;
            pool *= 0.7 + 0.3 * smoothstep(0.0, 0.35, (surface - abs(sc)) / max(surface, 0.001));
            pool += vec3(0.75, 0.12, 0.10) * exp(-max(surface - abs(sc), 0.0) / (0.02 * width));
            pool += vec3(0.32, 0.04, 0.05) * (1.0 - smoothstep(0.0, 0.2, blood));
            colour = mix(colour, pool, smoothstep(0.0, 0.05 * width, thick) * 0.95);
        }
    }
    return vec4(colour, 0.97);
}

void main() {
    float heat = vertexColor.r;
    float blood = vertexColor.g;
    float rust = vertexColor.b;
    float strength = vertexColor.a * ColorModulator.a;
    float t = HemoTime;
    vec3 colour = SCORCH;
    float alpha = 0.0;

    if (shape < 0.5) {
        float along = uv.x * SPAN;
        float across = uv.y * 2.0 - 1.0;
        float a = abs(across);
        // The gap wanders in width along its length and its lips are ragged; it never closes to nothing mid-crack.
        float open = 0.30 * (0.55 + 0.9 * noise(vec2(along * 1.3, seed))) + (noise(vec2(along * 7.0, seed + 5.0)) - 0.5) * 0.12;
        open = max(open, 0.06);
        float lip = 0.09 + 0.06 * noise(vec2(along * 4.0, seed + 9.0));
        float q = a / open;
        if (q < 1.0) {
            float depth = 1.0 - q;
            vec3 wall = mix(SCORCH, CRIMSON, smoothstep(0.12, 0.6, depth));
            wall *= 0.7 + 0.6 * noise(vec2(along * 9.0, depth * 5.0 + seed));
            // a narrow seam down the middle, white-hot at the commit, a hair of core light inside it
            float seam = smoothstep(0.7, 1.0, depth);
            float hair = smoothstep(0.9, 1.0, depth);
            float throb = 0.82 + 0.18 * sin(t * 0.25 + along * 0.8 + seed);
            vec3 glow = mix(HOT, CORE, smoothstep(0.85, 1.0, heat) * hair);
            colour = mix(wall * 0.6, glow, seam * heat * throb);
            colour += HOT * 0.12 * heat * depth * depth;
            // Blood rises toward the lips but never brims: the dark walls show above it, the seam glows up through it,
            // and where it meets the wall it climbs in a lit meniscus.
            float level = 1.0 - blood * 0.78;
            float wet = smoothstep(level - 0.05, level + 0.05, depth) * step(0.02, blood);
            vec3 pool = standing(along, across, heat);
            pool += mix(HOT, CORE, smoothstep(0.85, 1.0, heat)) * seam * heat * throb * 0.55;
            pool += vec3(0.55, 0.04, 0.05) * exp(-sq((depth - level) / 0.05)) * step(0.02, blood);
            colour = mix(colour, pool, wet);
            alpha = 0.95;
        } else if (a < open + lip) {
            // The broken lip: pale bone dust catching the light, stained by the rust that seeps out of the gap.
            float grain = 0.5 + 0.5 * noise(vec2(along * 13.0, a * 20.0 + seed));
            vec3 dust = BONE * (0.22 + 0.2 * grain);
            float seep = rust * smoothstep(0.2, 0.6, noise(vec2(along * 2.3, seed + 2.0)));
            colour = mix(dust, RUST * 0.7, seep);
            colour += HOT * 0.12 * heat;
            alpha = 0.72;
        } else {
            // The stone round the gap: scorched dark, rust blooming out of it, and hairlines branching off.
            float fall = 1.0 - smoothstep(open + lip, 1.0, a);
            float bloom = rust * smoothstep(0.45, 0.75, fbm(vec2(along * 1.4, a * 5.0) + seed));
            colour = mix(SCORCH, RUST * 0.75, bloom);
            float hair = 1.0 - smoothstep(0.0, 0.05, crazing(vec2(along * 2.4, across * 3.2) + seed * 0.5));
            colour = mix(colour, mix(CLOT, HOT * 0.7, heat * 0.6), hair);
            alpha = fall * (0.5 + 0.25 * bloom) + hair * fall * 0.7;
        }
    } else if (shape < 1.5) {
        vec2 p = (uv - 1.0) * SPAN;
        float gap = crazing(p * 1.1 + seed);
        float fine = crazing(p * 3.1 + seed + 7.0);
        float line = 1.0 - smoothstep(0.015, 0.06, gap);
        float hair = (1.0 - smoothstep(0.01, 0.035, fine)) * 0.6;
        float stain = fbm(p * 0.6 + seed);
        float bloom = rust * smoothstep(0.5, 0.75, fbm(p * 1.3 + seed + 3.0));
        // the floor soaks dark crimson as blood comes up through it, blotched, smouldering where the heat is
        float soak = blood * smoothstep(0.25, 0.75, stain + 0.25 * blood);
        colour = mix(SCORCH * 1.6, mix(CLOT * 1.8, CRIMSON * 0.75, stain), soak);
        colour = mix(colour, RUST * 0.75, bloom);
        // the footprint smoulders under the soak as it heats, and flashes with the seams at the commit
        colour += HOT * (0.10 + 0.35 * smoothstep(0.8, 1.0, heat)) * heat * smoothstep(0.35, 0.8, stain);
        float throb = 0.82 + 0.18 * sin(t * 0.25 + p.x * 0.5 + p.y * 0.3 + seed);
        vec3 lit = mix(CLOT, mix(HOT, CORE, smoothstep(0.85, 1.0, heat) * 0.6), heat * 0.85 * throb);
        lit = mix(lit, standing(p.x, fract(p.y) * 2.0 - 1.0, heat), blood * line * 0.6);
        float l = max(line, hair * (0.35 + 0.65 * heat));
        colour = mix(colour, lit, l);
        alpha = min(0.95, (0.34 + 0.24 * stain) * (0.55 + 0.45 * max(blood, rust)) + l * 0.8 + bloom * 0.25);
    } else {
        // a trench: blue is the drop here, not rust
        vec4 dug = trench(heat, blood, rust);
        colour = dug.rgb;
        alpha = dug.a;
    }

    alpha *= strength;
    if (alpha < 0.003) discard;
    fragColor = linear_fog(vec4(colour * ColorModulator.rgb, clamp(alpha, 0.0, 1.0)), vertexDistance, FogStart, FogEnd, FogColor);
}
