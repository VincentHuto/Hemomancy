#version 150

#moj_import <fog.glsl>

// A consecrated Cardinal Rite floor: one living membrane spread over every floor block.
// texCoord0 is the world offset from the Focus centre in blocks, so the surface is continuous across block seams.

// The Abocipher font atlas: 26 glyphs of 24 px in a row, lowercase above uppercase, for the Dominion seal's runes.
uniform sampler2D Sampler0;
// The engram runes, a to z in 24 px cells with clear gutters (tools/assets/build_engram_atlas.py), for the Communion inscriptions.
uniform sampler2D Sampler1;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float FloorSeed;
uniform float FloorStyle;
uniform float FloorHalf;
uniform float Spread;
uniform float Front;
uniform float Beat;
uniform float FloorLight;
uniform vec2 FocusOffset;
uniform vec3 BaseColor;
uniform vec3 VeinColor;
uniform vec3 GlowColor;
// The rite held on the floor (FloorRiteResponse); all zero when none is.
// RiteRings: the staff's shock (radius, strength) and a grounding rite's ring (radius, strength).
uniform vec4 RiteRings;
// RiteSurge: a filled anchor's pulse running out (angle, head radius, strength); w the Focus mouth's flare.
uniform vec4 RiteSurge;
// RiteDraw: a drawn offering's pulse running in (angle, head radius, strength); w a wave flinch's ripple radius.
uniform vec4 RiteDraw;
// RiteMood: vigor of a live rite, bruising, a wave's blanch, a flush of relief or completion.
uniform vec4 RiteMood;
// RiteSwell: culmination's swell, a cancellation's dimming, the flinch ripple's strength.
uniform vec4 RiteSwell;
// Up to four damaged anchors bruise the membrane toward themselves: their angles and how badly.
uniform vec4 WoundAngles;
uniform vec4 WoundLevels;
// The world stage (RiteWorldState.bloodUniform): x how full the brazier trench is, y how far it spills over its lip,
// z the creep's radius from the Focus centre in blocks, w the creep's strength. All zero with no rite.
uniform vec4 RiteBlood;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 viewOffset;
in vec2 gashSide;
in float gashLean;
in vec3 soilTint;

out vec4 fragColor;

// Squares without pow(): pow(x, y) is undefined for x < 0 in GLSL.
float sq(float x) {
    return x * x;
}

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + FloorSeed * 0.017) * 43758.5453);
}

vec2 hash2(vec2 value) {
    return vec2(hash(value), hash(value + vec2(17.3, 41.9)));
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
    mat2 turn = mat2(0.80, -0.60, 0.60, 0.80);
    for (int i = 0; i < 4; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.02 + vec2(1.7, -0.4);
        amplitude *= 0.5;
    }
    return total;
}

// Distance to the nearest cell wall of a jittered grid: thin values trace a branching vessel network.
vec2 cellular(vec2 p) {
    vec2 cell = floor(p);
    vec2 local = fract(p);
    float nearest = 8.0;
    float second = 8.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 point = offset + 0.15 + 0.7 * hash2(cell + offset);
            float distanceToPoint = length(point - local);
            if (distanceToPoint < nearest) {
                second = nearest;
                nearest = distanceToPoint;
            } else if (distanceToPoint < second) {
                second = distanceToPoint;
            }
        }
    }
    return vec2(nearest, second - nearest);
}

float line(float distanceToLine, float width) {
    return 1.0 - smoothstep(width, width + 0.06, distanceToLine);
}

float hash3(vec3 value) {
    return fract(sin(dot(value, vec3(127.1, 311.7, 74.7)) + FloorSeed * 0.023) * 43758.5453);
}

float valueNoise3(vec3 value) {
    vec3 cell = floor(value);
    vec3 local = fract(value);
    vec3 curve = local * local * (3.0 - 2.0 * local);
    float a = mix(hash3(cell), hash3(cell + vec3(1.0, 0.0, 0.0)), curve.x);
    float b = mix(hash3(cell + vec3(0.0, 1.0, 0.0)), hash3(cell + vec3(1.0, 1.0, 0.0)), curve.x);
    float c = mix(hash3(cell + vec3(0.0, 0.0, 1.0)), hash3(cell + vec3(1.0, 0.0, 1.0)), curve.x);
    float d = mix(hash3(cell + vec3(0.0, 1.0, 1.0)), hash3(cell + vec3(1.0, 1.0, 1.0)), curve.x);
    return mix(mix(a, b, curve.y), mix(c, d, curve.y), curve.z);
}

// The Apotheos chamber's prism (apotheos_floor_funnel): its portal rim's spectrum, red through violet and cyan
// round to orange and back to red.
vec3 apotheosPrism(float t) {
    vec3 stops[10] = vec3[](vec3(1.0, 0.018, 0.010), vec3(1.0, 0.020, 0.58), vec3(0.53, 0.050, 1.0),
            vec3(0.76, 0.46, 1.0), vec3(0.54, 0.76, 1.0), vec3(0.0, 0.94, 1.0), vec3(0.20, 1.0, 0.14),
            vec3(1.0, 0.94, 0.08), vec3(1.0, 0.38, 0.035), vec3(1.0, 0.018, 0.010));
    float s = clamp(t, 0.0, 1.0) * 9.0;
    int i = int(min(floor(s), 8.0));
    return mix(stops[i], stops[i + 1], s - float(i));
}

// Pink and violet motes thrown out of the Focus mouth, like the chamber portal's haze. Cells run round the Focus in
// a whole number of slices, so the field never seams where the angle wraps.
float apotheosMotes(float r, float angle) {
    float slices = 30.0;
    vec2 polar = vec2((angle / 6.2831853 + 0.5) * slices, r * 2.4 - HemoTime * 0.022);
    vec2 cell = floor(polar);
    cell.x = mod(cell.x, slices);
    vec2 local = fract(polar);
    vec2 centre = vec2(hash(cell + 3.1), hash(cell + 7.7)) * 0.6 + 0.2;
    float present = step(0.72, hash(cell + 11.3));
    vec2 d = (local - centre) * vec2(6.2831853 * max(r, 0.2) / slices, 1.0 / 2.4);
    return present * exp(-dot(d, d) / 0.006);
}

// The chamber ceiling's green orbs, a handful drifting slowly round the Focus at their own radii.
float apotheosOrbs(vec2 p) {
    float field = 0.0;
    for (int i = 0; i < 5; i++) {
        float index = float(i);
        float seed = hash(vec2(index * 17.31, 3.7));
        float turn = (mod(index, 2.0) < 0.5 ? 1.0 : -1.0) * (0.0035 + 0.0012 * index);
        float a = seed * 6.2831853 + HemoTime * turn;
        float radius = 1.3 + hash(vec2(index * 5.77, 3.41)) * max(FloorHalf - 1.6, 0.4);
        vec2 d = p - vec2(cos(a), sin(a)) * radius;
        field += exp(-dot(d, d) / 0.035) * (0.75 + 0.25 * sin(HemoTime * 0.09 + index * 2.1));
    }
    return clamp(field, 0.0, 1.0);
}

// The Apotheos floor's tendrils, like those reaching from the chamber ceiling's hivemind: pale spines, a few of them
// gold, that leave the Focus mouth almost straight, bend a little as they go and thin to a point, with small hooked
// barbs along them. Returns how much of a tendril covers p; tint is its colour there, glow how much light it gives
// off, and shadow the contact shadow the tendrils cast on the membrane beside them.
float apotheosTendrils(vec2 p, float r, float angle, out vec3 tint, out float glow, out float shadow) {
    float cover = 0.0;
    tint = vec3(0.0);
    glow = 0.0;
    shadow = 0.0;
    for (int i = 0; i < 18; i++) {
        float index = float(i);
        float seed = hash(vec2(index * 7.13, 1.9));
        float reach = 1.8 + hash(vec2(index * 3.7, 8.2)) * max(FloorHalf - 0.4, 0.5);
        if (r > reach + 0.2) continue;
        float t = clamp(r / reach, 0.0, 1.0);
        float bend = (seed - 0.5) * 1.1 * t * t + sin(r * 1.7 + seed * 6.2831853 + HemoTime * 0.015) * 0.05 * r;
        float a = (index + seed * 0.5) / 18.0 * 6.2831853 + bend / max(r, 0.6);
        float sideOffset = (mod(angle - a + 3.14159265, 6.2831853) - 3.14159265) * r;
        float d = abs(sideOffset);
        float w = mix(0.085, 0.01, t);
        float body = (1.0 - smoothstep(w, w + 0.025, d)) * smoothstep(0.5, 0.75, r)
                * (1.0 - smoothstep(reach, reach + 0.15, r));
        // Every so often a barb leaves the spine to one side and hooks back outward.
        float barbCell = r * 1.1 + seed * 5.0;
        float barbSide = mod(floor(barbCell), 2.0) < 0.5 ? 1.0 : -1.0;
        float reachOut = sideOffset * barbSide - w;
        float hook = 0.04 + 6.0 * reachOut * reachOut;
        float barb = (1.0 - smoothstep(0.008, 0.02, abs(fract(barbCell) / 1.1 - hook))) * step(0.0, reachOut)
                * (1.0 - smoothstep(0.07, 0.1, reachOut)) * step(0.5, hash(vec2(floor(barbCell), index)))
                * smoothstep(0.9, 1.3, r) * step(r, reach);
        float here = max(body, barb * 0.9);
        if (here > cover) {
            cover = here;
            float roundness = body > barb ? sqrt(max(1.0 - sq(d / w), 0.0)) : 0.6;
            bool gold = hash(vec2(index * 11.1, 5.5)) > 0.72;
            tint = gold ? mix(vec3(0.62, 0.46, 0.06), vec3(1.0, 0.88, 0.3), roundness)
                    : mix(vec3(0.42, 0.44, 0.48), vec3(0.97, 0.97, 0.95), roundness);
            glow = gold ? 0.6 + 0.3 * roundness : 0.15 + 0.2 * roundness;
        }
        shadow = max(shadow, (1.0 - smoothstep(w, w + 0.09, d)) * smoothstep(0.5, 0.75, r) * step(r, reach + 0.1));
    }
    return cover;
}

// A cut through the ground as seen on a trench wall. site is the wall's fixed position on the ground, v how far down
// the wall the point is in blocks, and t that depth as a fraction of the trench's: a band of the ground block with
// grain, stones and hanging roots, a thin glossy membrane, then layered muscle with fibres running along the cut, severed vessels oozing
// short drips, and a vein winding along the wall that pulses with the heart.
vec3 trenchWall(vec2 site, float v, float t, vec3 ground) {
    vec3 at = vec3(site.x, v, site.y);
    // Topsoil, with a crumbling lower edge.
    float soilDepth = 0.04 + 0.03 * noise(site * 3.0 + 7.0) + 0.012 * noise(site * 11.0);
    float soil = smoothstep(soilDepth + 0.012, soilDepth - 0.012, v);
    float grain = valueNoise3(at * 26.0);
    // The cut runs through whatever block the trench lies in: its average colour, broken up by grain and stones.
    vec3 rock = dot(ground, ground) < 1.0e-4 ? vec3(0.5, 0.36, 0.24) : ground;
    vec3 earth = rock * 0.6 * (0.65 + 0.6 * grain);
    float pebble = smoothstep(0.74, 0.8, valueNoise3(at * 11.0 + 3.0));
    earth = mix(earth, rock * (0.85 + 0.3 * grain), pebble * 0.6);
    earth *= 1.0 - 0.35 * smoothstep(0.55, 0.75, valueNoise3(at * 17.0 + 9.0));
    // Roots hang from the soil a little way into the flesh, wavering as they go.
    vec2 rootSite = site + vec2(sin(v * 38.0 + site.y * 5.0), cos(v * 31.0 + site.x * 5.0)) * 0.012;
    float rootLine = line(abs(fract(noise(rootSite * 9.0) * 3.0) - 0.5), 0.018)
            * smoothstep(0.55, 0.7, noise(site * 2.5 + 30.0));
    float rootLength = soilDepth + 0.03 + 0.06 * noise(site * 5.0 + 2.0);
    float roots = rootLine * smoothstep(rootLength, rootLength - 0.05, v) * step(soilDepth - 0.01, v);
    // A thin glossy membrane where the ground gives way to flesh, in the floor's own colours.
    float fatTop = soilDepth + 0.004;
    float fatBottom = fatTop + 0.008 + 0.01 * noise(site * 4.0 + 13.0);
    float fat = smoothstep(fatTop - 0.004, fatTop + 0.004, v) * smoothstep(fatBottom + 0.006, fatBottom - 0.006, v);
    float globules = valueNoise3(at * 34.0);
    vec3 fatColor = mix(VeinColor * 0.9, VeinColor * 1.1 + GlowColor * 0.25, smoothstep(0.35, 0.7, globules));
    // Muscle: fibres running along the cut, bundled into layers that bulge and pinch.
    float bundle = valueNoise3(vec3(site.x * 5.0, v * 9.0, site.y * 5.0) + 21.0);
    float fibres = valueNoise3(vec3(site.x * 7.0, v * 70.0 + bundle * 6.0, site.y * 7.0));
    float sheath = line(abs(fract(v * 9.0 + bundle * 1.4) - 0.5), 0.04);
    vec3 muscle = mix(VeinColor * 0.62, VeinColor * 0.95 + BaseColor * 0.15, fibres);
    muscle = mix(muscle, BaseColor * 0.35 + VeinColor * 0.25, sheath * 0.55);
    // Severed vessels: dark open ends with wet rims, each oozing a short drip down the wall.
    float vesselField = valueNoise3(at * 9.0 + 41.0);
    float vesselEnd = smoothstep(0.8, 0.84, vesselField);
    float vesselRim = smoothstep(0.76, 0.8, vesselField) * (1.0 - vesselEnd);
    float drip = smoothstep(0.8, 0.86, valueNoise3(vec3(site.x * 9.0, v * 2.2, site.y * 9.0) + 41.0))
            * smoothstep(0.7, 0.5, noise(site * 60.0)) * step(soilDepth + 0.05, v);
    // A vein winding along the wall, swelling with the heartbeat.
    float veinDepth = soilDepth + 0.1 + 0.08 * noise(site * 2.4 + 5.0);
    float vein = line(abs(v - veinDepth), 0.008 + 0.006 * Beat);
    vec3 color = muscle;
    color = mix(color, fatColor, fat);
    color = mix(color, VeinColor * 0.3, drip * 0.7);
    color = mix(color, VeinColor * 0.22, vesselEnd);
    color = mix(color, VeinColor * 1.15, vesselRim * 0.8);
    color = mix(color, VeinColor * (0.9 + 0.5 * Beat) + GlowColor * 0.12 * Beat, vein * (1.0 - fat));
    color = mix(color, earth, soil);
    color = mix(color, vec3(0.16, 0.11, 0.07), roots * (1.0 - soil) * 0.85);
    // Wet glints on the flesh.
    float glint = smoothstep(0.86, 0.95, valueNoise3(at * 22.0 + 77.0)) * (1.0 - soil);
    color += GlowColor * glint * 0.18;
    // Blood from the trench floor has soaked a ragged way up the lower wall.
    float soak = smoothstep(0.55, 0.92, t + (valueNoise3(at * 7.0 + 19.0) - 0.5) * 0.3);
    color = mix(color, VeinColor * (0.36 + 0.12 * valueNoise3(at * 15.0)), soak * 0.75);
    return color * mix(1.0, 0.36, t);
}

// A vessel sunk into the membrane, from its distance d to the centre line and its half-width w: x is how far inside
// it is, y its shaded walls, z a wet highlight down its middle, w a dark lip on the membrane around it.
vec4 groove(float d, float w) {
    float inside = 1.0 - smoothstep(w, w + 0.02, d);
    float wall = smoothstep(w * 0.35, w, d) * inside;
    float highlight = exp(-sq(d / max(w * 0.28, 0.004))) * inside;
    float lip = smoothstep(w + 0.07, w + 0.01, d) * (1.0 - inside);
    return vec4(inside, wall, highlight, lip);
}

// The distance to the Qliphoth tree's hyphae paths: the trunk, three cross-paths and two outer pillars.
float qliphothPaths(vec2 p) {
    float trunk = abs(p.x);
    float rows = min(min(abs(p.y), abs(p.y - 3.0)), abs(p.y + 3.0)) + max(abs(p.x) - 3.0, 0.0);
    float pillars = abs(abs(p.x) - 3.0) + max(abs(p.y) - 3.0, 0.0);
    return min(trunk, min(rows, pillars));
}

// --- The Qliphoth floor: black monolith glass over a starred void, with a black hole at the Focus. ---
// Its event horizon sits just inside the Focus pad's opening, so the pad and the floor draw one continuous hole.
const float QLIPHOTH_HORIZON = 0.40;
const vec3 QLIPHOTH_BLOOD = vec3(0.42, 0.002, 0.006);
const vec3 QLIPHOTH_ARTERIAL = vec3(1.0, 0.025, 0.008);
const vec3 QLIPHOTH_HOT = vec3(1.0, 0.16, 0.025);

// The Chamber sky's alien corona: bruised wine turning toward indigo around the hole.
vec3 qliphothCoronaTint(float angle, float t) {
    return mix(vec3(0.34, 0.006, 0.090), vec3(0.18, 0.038, 0.70), 0.45 + 0.30 * sin(angle * 2.0 + t * 0.31));
}

// A sparse layer of stars: one candidate per cell, lit when its hash clears the density, each twinkling on its own.
float qliphothStars(vec2 v, float density, float t) {
    vec2 cell = floor(v);
    vec2 local = fract(v);
    float h = hash(cell + 23.7);
    vec2 site = 0.2 + 0.6 * hash2(cell + 5.1);
    float d = length(local - site);
    float twinkle = 0.55 + 0.45 * sin(t * (1.3 + h * 2.4) + h * 40.0);
    return step(1.0 - density, h) * exp(-d * d / 0.0035) * twinkle;
}

// The void seen through the glass: two layers of stars and a slow crimson-violet nebula, fixed to the floor (no
// parallax, so nothing slides as the viewer moves) and lensed toward the Focus so the sky bends into the hole. Returns its colour, and in w how much of it glows on its own.
vec4 qliphothVoid(vec2 p, float r, float t) {
    vec2 dir = p / max(r, 0.001);
    // Gravitational lensing: the deep sky is pulled toward the hole and wound round it.
    float pull = 1.1 / (r + 0.35);
    float twist = 1.4 / (r + 0.6) - t * 0.05;
    mat2 turn = mat2(cos(twist), -sin(twist), sin(twist), cos(twist));
    vec2 lensed = turn * (p - dir * pull * 0.55);
    vec2 nearSky = lensed;
    vec2 farSky = lensed * 0.8 + 7.0;
    float stars = qliphothStars(nearSky * 3.2, 0.12, t) + qliphothStars(farSky * 5.5 + 13.0, 0.18, t) * 0.6;
    float nebula = fbm(farSky * 0.32 + vec2(t * 0.02, -t * 0.015));
    float dust = smoothstep(0.45, 0.85, nebula);
    vec3 cloud = mix(vec3(0.14, 0.004, 0.02), vec3(0.08, 0.02, 0.22), smoothstep(0.35, 0.75, fbm(farSky * 0.6 + 4.0)));
    vec3 color = cloud * dust * 0.9 + vec3(1.0, 0.86, 0.92) * stars;
    return vec4(color, stars + dust * 0.35);
}

// The monolith's fractal fault web, drifting as the stone's memory moves.
float qliphothFault(vec2 q, float t) {
    float web = fbm(q + vec2(t * 0.11, FloorSeed));
    float branch = fbm(q * 2.35 - vec2(FloorSeed, t * 0.08));
    return abs(web - 0.53) + abs(branch - 0.48) * 0.48;
}

// The monolith's red memory running in its faults: trains of blood flowing in toward the hole.
// x is the fault's faint body, y its bright core, z the travelling flow.
vec3 qliphothFaults(vec2 p, float r, float t) {
    vec2 q = p * 0.42;
    float distance = qliphothFault(q, t);
    float body = smoothstep(0.096, 0.030, distance);
    float core = smoothstep(0.040, 0.009, distance);
    float lane = fbm(q * 0.82 + FloorSeed * 1.7);
    float train = fract((r + lane * 1.35) * 0.42 + t * 0.9 + FloorSeed * 0.37);
    float head = smoothstep(0.035, 0.09, train) * (1.0 - smoothstep(0.145, 0.32, train));
    float trail = smoothstep(0.32, 0.145, train) * 0.34;
    float gap = 1.0 - smoothstep(0.42, 0.86, train) * (1.0 - smoothstep(0.9, 0.99, train));
    return vec3(body, core, core * (0.28 + head + trail) * gap);
}

// The black hole: a lightless horizon, a hot photon ring, a spiral accretion disk running into it with one side
// brighter as it turns toward the viewer, and the alien corona breathing round it.
// Returns its colour and, in w, how much of the surface it covers (the horizon is opaque black).
vec4 qliphothHole(vec2 p, float r, float angle, float t, float beat) {
    float safeR = max(r, 0.05);
    float horizon = smoothstep(QLIPHOTH_HORIZON + 0.05, QLIPHOTH_HORIZON - 0.03, r);
    float primary = 0.5 + 0.5 * sin(t * 1.18);
    float secondary = 0.5 + 0.5 * sin(t * 2.37 + sin(t * 0.41) * 1.4);
    float ring = exp(-sq((r - QLIPHOTH_HORIZON - 0.07) / 0.045)) * mix(0.8, 1.25, primary) * (0.85 + 0.4 * beat);
    float spiralNoise = fbm(vec2(angle * 0.74 + t * 0.27, safeR * 2.2 - t * 0.18));
    float spiral = smoothstep(0.2, 0.92,
            sin(angle * 3.0 - log(safeR) * 6.5 + t * 2.6 + spiralNoise * 4.2) * 0.5 + 0.5);
    float disk = smoothstep(3.3, 1.0, r) * smoothstep(QLIPHOTH_HORIZON + 0.02, QLIPHOTH_HORIZON + 0.35, r);
    float bands = 0.48 + 0.3 * sin(r * 22.0 - t * 3.1 + spiralNoise * 7.0) + 0.22 * spiral;
    float striations = smoothstep(0.40, 0.66, fbm(vec2(angle * 6.0 - t * 0.36, safeR * 6.0 + t * 0.15)));
    float doppler = mix(0.6, 1.3, 0.5 + 0.5 * sin(angle - 0.6));
    float hot = disk * smoothstep(0.46, 0.93, bands) * (1.0 - striations * 0.82);
    vec3 diskColor = mix(QLIPHOTH_BLOOD, QLIPHOTH_ARTERIAL, clamp(bands * doppler, 0.0, 1.0));
    diskColor = mix(diskColor, QLIPHOTH_HOT, hot * 0.72);
    float corona = smoothstep(4.2, 0.9, r) * smoothstep(QLIPHOTH_HORIZON + 0.05, QLIPHOTH_HORIZON + 0.5, r)
            * (0.42 + 0.58 * fbm(vec2(angle * 2.6 - t * 0.16, safeR * 2.5 + t * 0.11)))
            * (0.66 + primary * 0.25 + secondary * 0.09);
    float diskCover = clamp(disk * (0.35 + spiral * 0.55) * doppler, 0.0, 1.0);
    vec3 color = qliphothCoronaTint(angle, t) * corona * 0.95;
    color = mix(color, diskColor, diskCover);
    color += QLIPHOTH_ARTERIAL * ring * 1.1 + QLIPHOTH_HOT * ring * ring * 0.4;
    color *= 1.0 - horizon;
    return vec4(color, clamp(horizon + diskCover + ring + corona * 0.4, 0.0, 1.0));
}

// One of the nine ripened husks as a small singularity of its own: a black pit, a beating red ring and a violet halo.
// Returns its colour and its cover in w.
vec4 qliphothHuskWell(vec2 p, float t, float beat) {
    vec2 husks[9] = vec2[](vec2(0.0, -4.0), vec2(-3.0, -3.0), vec2(3.0, -3.0), vec2(-3.0, 0.0), vec2(3.0, 0.0),
            vec2(-3.0, 3.0), vec2(3.0, 3.0), vec2(0.0, -2.0), vec2(0.0, 2.0));
    vec3 color = vec3(0.0);
    float cover = 0.0;
    float pit = 0.0;
    for (int i = 0; i < 9; i++) {
        vec2 d = p - husks[i];
        float r = length(d);
        float swell = 0.85 + 0.25 * beat + 0.08 * sin(t * 1.7 + float(i) * 1.9);
        float core = smoothstep(0.15 * swell, 0.10 * swell, r);
        float ring = exp(-sq((r - 0.19 * swell) / 0.035));
        float halo = exp(-sq(r / 0.55)) * (1.0 - core);
        float a = atan(d.y, d.x);
        float swirl = smoothstep(0.55, 0.95, sin(a * 2.0 - log(max(r, 0.03)) * 5.0 + t * 3.0 + float(i)) * 0.5 + 0.5)
                * smoothstep(0.5, 0.2, r) * (1.0 - core);
        color += QLIPHOTH_ARTERIAL * ring * (0.9 + 0.6 * beat) + QLIPHOTH_BLOOD * swirl * 0.8
                + qliphothCoronaTint(a, t) * halo * 0.45;
        cover = max(cover, max(core, max(ring, max(halo * 0.5, swirl * 0.6))));
        pit = max(pit, core);
    }
    color *= 1.0 - pit;
    return vec4(color, clamp(cover, 0.0, 1.0));
}

// The Ancestral floor is a pedigree fan: each generation ring outward splits every ancestor's cell in two, four
// parents at the Focus, then eight, sixteen, thirty-two. Returns the distance to the nearest ring or divider in x,
// the generation (0 nearest, -1 outside the fan) in y, and the cell index within that ring in z.
const float ANCESTRAL_INNER = 1.0;
const float ANCESTRAL_STEP = 0.75;

vec3 ancestralFan(vec2 p, float r) {
    float band = (r - ANCESTRAL_INNER) / ANCESTRAL_STEP;
    float ringDist = abs(fract(band + 0.5) - 0.5) * ANCESTRAL_STEP;
    if (band < -0.5 || band > 4.5) return vec3(10.0, -1.0, 0.0);
    if (band < 0.0 || band >= 4.0) return vec3(ringDist, -1.0, 0.0);
    float generation = floor(band);
    float cells = 4.0 * exp2(generation);
    // The first parents sit between the axes, so the eldest lines run out along the diagonals.
    float turn = fract(atan(p.y, p.x) / 6.2831853 + 0.125);
    float divider = (0.5 - abs(fract(turn * cells) - 0.5)) * 6.2831853 * r / cells;
    return vec3(min(ringDist, divider), generation, floor(turn * cells));
}

// One amber bead for every ancestor, set in the middle of its cell and smaller the further back it is remembered.
float ancestralBead(vec2 p, float r, vec3 fan) {
    if (fan.y < 0.0) return 0.0;
    float cells = 4.0 * exp2(fan.y);
    float a = ((fan.z + 0.5) / cells - 0.125) * 6.2831853;
    float mid = ANCESTRAL_INNER + (fan.y + 0.5) * ANCESTRAL_STEP;
    float size = 0.13 - fan.y * 0.022;
    return 1.0 - smoothstep(size * 0.55, size, length(p - vec2(cos(a), sin(a)) * mid));
}

// A crisp stroke for the finer laid marks, which line()'s soft edge would blur together.
float stroke(float d, float w) {
    return 1.0 - smoothstep(w, w + 0.025, d);
}

// The Threshold floor's doorways: square frames nested round the Focus, each broken open where the cross runs through
// it. Returns the distance to the nearest frame, or 10 inside the first frame and in the cross's open doorways.
float thresholdFrameDistance(vec2 p, float square) {
    if (square < 1.0 || min(abs(p.x), abs(p.y)) < 0.45) return 10.0;
    return abs(fract(square / 1.5 + 0.5) - 0.5) * 1.5;
}

// The Dominion seal's rune band: between its first two rings, turning with the seal, it spells "BY BLOOD THIS GROUND
// IS HELD" in Abocipher, read upright from the Focus, with a dot between words. Letters index the font atlas, a to z.
float dominionGlyphs(float r, float angle) {
    const float radius = 2.15;
    const float size = 0.4;
    float ly = r - radius;
    if (abs(ly) > size * 0.5) return 0.0;
    int phrase[29] = int[](1, 24, -1, 1, 11, 14, 14, 3, -1, 19, 7, 8, 18, -1, 6, 17, 14, 20, 13, 3, -1, 8, 18, -1,
            7, 4, 11, 3, -1);
    const float count = 29.0;
    float s = ((angle + HemoTime * 0.0012) / 6.2831853 + 0.5) * count;
    int letter = phrase[int(mod(floor(s), count))];
    float lx = (fract(s) - 0.5) * 6.2831853 * radius / count;
    if (letter < 0) {
        return stroke(length(vec2(lx, ly)), 0.03);
    }
    vec2 cell = vec2(0.5 + lx / size, 0.5 - ly / size);
    if (cell.x < 0.0 || cell.x > 1.0) return 0.0;
    // The glyphs are red over a grey drop shadow: only the red counts.
    vec4 glyph = texture(Sampler0, vec2((float(letter) + cell.x) / 26.0, cell.y * 0.5));
    return glyph.a * smoothstep(0.08, 0.3, glyph.r - glyph.g);
}

// The Dominion seal: three rings, each a double line graduated with ticks and slowly turning, the middle one against the
// others. Returns the rings and ticks in x and the glyph band in y.
vec2 dominionSeal(float r, float angle) {
    float rings = 0.0;
    for (int i = 0; i < 3; i++) {
        float radius = 1.45 + float(i) * 1.4;
        float a = angle + (i == 1 ? -1.0 : 1.0) * HemoTime * 0.0015 / (1.0 + float(i));
        float lines = stroke(abs(abs(r - radius) - 0.1), 0.022);
        float count = 12.0 * float(i + 1);
        float tick = stroke(abs(fract((a / 6.2831853 + 0.5) * count) - 0.5) * 6.2831853 * radius / count, 0.018)
                * step(abs(r - radius), 0.1);
        rings = max(rings, max(lines, tick));
    }
    return vec2(rings, dominionGlyphs(r, angle));
}

// The Dominion floor's roots: eight trunks leave the seal's centre and fork twice as they reach out to claim the
// ground. Returns the distance to the nearest root and its half-width there, thinning toward the floor's edge.
vec2 dominionRoots(float r, float angle, vec2 tissue) {
    float a = angle + (fbm(tissue * 0.45 + 3.0) - 0.5) * 1.2 / max(r, 0.8);
    float fork = smoothstep(1.3, 2.9, r) * 0.2;
    float twig = smoothstep(2.9, 4.5, r) * 0.09;
    float nearest = 10.0;
    for (int i = 0; i < 4; i++) {
        float split = (i < 2 ? -fork : fork) + (i == 0 || i == 2 ? -twig : twig);
        nearest = min(nearest, abs(sin(4.0 * (a - split))) * r * 0.25);
    }
    return vec2(nearest, mix(0.05, 0.012, smoothstep(0.6, FloorHalf + 0.5, r)));
}

// The Working floor's conduits: blood channels engraved into its iron plates, routed at right angles like a
// machine's wiring. Each cell of a half-block grid joins its centre to whichever edges its neighbours share, so the
// channels run unbroken from cell to cell, branching and dead-ending. Returns the distance to the nearest channel in
// blocks in x, and in y the distance to a terminal pad where a channel ends or a junction where three or more meet.
vec2 workingConduits(vec2 p) {
    const float scale = 2.0;
    vec2 q = p * scale;
    vec2 cell = floor(q);
    vec2 l = fract(q) - 0.5;
    vec4 joins = step(0.48, vec4(hash(cell + vec2(0.5, 0.0) + 71.0), hash(cell + vec2(-0.5, 0.0) + 71.0),
            hash(cell + vec2(0.0, 0.5) + 71.0), hash(cell + vec2(0.0, -0.5) + 71.0)));
    float nearest = 10.0;
    if (joins.x > 0.5) nearest = min(nearest, length(vec2(l.x - clamp(l.x, 0.0, 0.5), l.y)));
    if (joins.y > 0.5) nearest = min(nearest, length(vec2(l.x - clamp(l.x, -0.5, 0.0), l.y)));
    if (joins.z > 0.5) nearest = min(nearest, length(vec2(l.x, l.y - clamp(l.y, 0.0, 0.5))));
    if (joins.w > 0.5) nearest = min(nearest, length(vec2(l.x, l.y - clamp(l.y, -0.5, 0.0))));
    float links = dot(joins, vec4(1.0));
    float node = links == 1.0 || links >= 3.0 ? length(l) : 10.0;
    return vec2(nearest, node) / scale;
}

// One engram rune from the atlas (24 px cells, each a 16 px rune inside a 4 px clear gutter, filtered smoothly). g is
// the position across the rune, 0 to 1, y down. Returns the rune's smoothed body in x and a soft glow round it in y.
vec2 engramRune(int letter, vec2 g) {
    vec2 base = vec2(float(letter) * 24.0 + 4.0, 4.0);
    vec2 atlas = vec2(624.0, 24.0);
    vec2 at = clamp(g, -0.22, 1.22) * 16.0;
    // The runes are drawn at about three quarters opacity: their edge is where that falls by half.
    float body = smoothstep(0.22, 0.5, texture(Sampler1, (base + at) / atlas).a);
    float glow = 0.0;
    for (int i = 0; i < 8; i++) {
        float a = float(i) * 0.785398;
        glow += texture(Sampler1, (base + at + vec2(cos(a), sin(a)) * 2.2) / atlas).a;
    }
    return vec2(body, glow / 6.0);
}

// The Communion floor's inscriptions: short engram words that write themselves into the membrane rune by rune, glow,
// then fade, each in its own place and time. The floor is split into loose, staggered two-block cells; every cell has
// its own slow cycle, and each cycle it may hold one word at a fresh spot and tilt, or lie bare. Returns the runes'
// body in x, their glow in y, and in z 1 for a white word, 0 for a gold one.
vec3 communionEngrams(vec2 p) {
    const float span = 2.0;
    const float size = 0.32;
    const float pitch = 0.35;
    // WHAT ONE BLED ALL KEPT SHARE BLOOD KIN HELD WE, a to z as 0 to 25.
    int letters[37] = int[](22, 7, 0, 19, 14, 13, 4, 1, 11, 4, 3, 0, 11, 11, 10, 4, 15, 19, 18, 7, 0, 17, 4,
            1, 11, 14, 14, 3, 10, 8, 13, 7, 4, 11, 3, 22, 4);
    int starts[10] = int[](0, 4, 7, 11, 14, 18, 23, 28, 31, 35);
    int lengths[10] = int[](4, 3, 4, 3, 4, 5, 5, 3, 4, 2);
    vec2 q = p / span;
    q.x += mod(floor(q.y), 2.0) * 0.5;
    vec2 cell = floor(q);
    vec2 local = (fract(q) - 0.5) * span;
    float seed = hash(cell + 51.3);
    float t = HemoTime * (0.0012 + 0.0008 * seed) + seed * 7.0;
    float phase = fract(t);
    // The cycle count is folded small before it is hashed: in an old world it runs into the thousands, and the GPU's
    // sin() loses all precision on arguments that large, which would leave every cell bare.
    float cycle = floor(t);
    vec2 key = cell + vec2(mod(cycle, 61.0) * 1.37, mod(cycle, 53.0) * 2.91);
    if (hash(key + 2.0) < 0.2) return vec3(0.0);
    int word = int(min(hash(key + 5.0) * 10.0, 9.0));
    int count = lengths[word];
    // Longer words keep nearer the cell's middle and lie flatter, so none runs past its cell. While shown, a word
    // drifts a little way off in its own direction, as if carried on what moves beneath the membrane.
    float drift = phase * 0.06 * float(6 - count);
    float driftAngle = hash(key + 23.0) * 6.2831853;
    vec2 centre = (hash2(key + 8.0) - 0.5) * (0.7 - 0.12 * float(count)) + vec2(cos(driftAngle), sin(driftAngle)) * drift;
    float turn = (hash(key + 11.0) - 0.5) * (1.9 - 0.3 * float(count)) + sin(HemoTime * 0.01 + seed * 9.0) * 0.06;
    vec2 w = mat2(cos(turn), -sin(turn), sin(turn), cos(turn)) * (local - centre);
    // The word lies on a gentle curve with a slow ripple running along it, never a ruled line.
    float bend = (hash(key + 29.0) - 0.5) * 0.9;
    w.y -= bend * w.x * w.x + sin(w.x * 4.0 - HemoTime * 0.03 + seed * 5.0) * 0.035;
    // As it fades the word loosens: its runes float apart, the short words most, so the long ones stay in their cell.
    float loosen = smoothstep(0.55, 0.78, phase);
    float spacing = pitch * (1.0 + 0.12 * float(5 - count) * loosen);
    float reach = float(count) * spacing * 0.5;
    if (abs(w.y) > size * 1.1 || abs(w.x) > reach + 0.1) return vec3(0.0);
    float slot = clamp(floor((w.x + reach) / spacing), 0.0, float(count - 1));
    // Each rune keeps its own lean, size and slow bob, and rises a little as the word lets go.
    float runeSeed = hash(key + vec2(slot * 3.1, 37.0));
    float runeTurn = (runeSeed - 0.5) * 0.45 + sin(HemoTime * 0.02 + runeSeed * 12.0) * 0.08;
    float runeSize = size * (0.85 + 0.3 * hash(key + vec2(slot * 5.3, 41.0)));
    vec2 rl = vec2(w.x + reach - (slot + 0.5) * spacing,
            w.y + sin(HemoTime * 0.025 + slot * 1.7 + seed * 4.0) * 0.03 + loosen * (runeSeed - 0.5) * 0.12);
    rl = mat2(cos(runeTurn), -sin(runeTurn), sin(runeTurn), cos(runeTurn)) * rl;
    vec2 rune = engramRune(letters[starts[word] + int(slot)], vec2(0.5) + rl / runeSize);
    // Written in left to right, a rune at a time, flaring as each arrives; then the word fades, its bodies going
    // before their glow so each rune dissolves into light.
    float written = smoothstep(0.02 + slot * 0.03, 0.07 + slot * 0.03, phase);
    float flare = 1.0 + 0.8 * exp(-max(phase - 0.07 - slot * 0.03, 0.0) * 40.0) * written;
    float fading = 1.0 - smoothstep(0.62 + runeSeed * 0.06, 0.8 + runeSeed * 0.06, phase);
    float shown = written * fading * flare;
    rune *= vec2(shown * fading, shown);
    return vec3(rune, hash(key + 17.0) < 0.5 ? 0.0 : 1.0);
}

// The distance to the nearest line of a style's laid pattern, and that line's half-width, for its groove.
vec2 motifLine(vec2 p, float r, int style) {
    float square = max(abs(p.x), abs(p.y));
    if (style == 0) {
        float frame = thresholdFrameDistance(p, square);
        float arms = min(abs(p.x), abs(p.y));
        return frame < arms ? vec2(frame, 0.035) : vec2(arms, 0.16);
    }
    if (style == 1) return vec2(min(min(abs(r - 1.45), abs(r - 2.85)), abs(r - 4.25)), 0.12);
    if (style == 3) return vec2(min(abs(p.x), abs(p.y)), 0.05);
    // The Qliphoth paths are wandering cracks drawn in main, with no laid groove.
    if (style == 5) return vec2(10.0, 0.0);
    if (style == 6) return vec2(ancestralFan(p, r).x, 0.045);
    return vec2(10.0, 0.0);
}

// Each style restates its floor's laid pattern in flesh: the iron cross and its doorways, the dominion seal,
// the communion weave, the working floor's bus channels (its plates and conduits are drawn in main).
float motif(vec2 p, float r, int style) {
    if (style == 0) {
        float square = max(abs(p.x), abs(p.y));
        return clamp(line(min(abs(p.x), abs(p.y)), 0.16) + stroke(thresholdFrameDistance(p, square), 0.035) * 0.8,
                0.0, 1.0);
    }
    if (style == 1) {
        vec2 seal = dominionSeal(r, atan(p.y, p.x));
        return clamp(seal.x * 0.8 + seal.y * 0.9, 0.0, 1.0);
    }
    if (style == 2) {
        float weave = sin(p.x * 3.14159) * sin(p.y * 3.14159);
        return smoothstep(0.55, 0.95, abs(weave)) * 0.6;
    }
    float square = max(abs(p.x), abs(p.y));
    if (style == 4) {
        // The hush lays no pattern of its own: its bands and tendrils are drawn in main.
        return 0.0;
    }
    if (style == 5) {
        // Its paths are cracks and its husks wells, both drawn in main.
        return 0.0;
    }
    if (style == 6) {
        // The lineage: the near dead drawn boldly by the Focus, the far generations fainter as the fan widens.
        vec3 fan = ancestralFan(p, r);
        float fade = fan.y < 0.0 ? 0.55 : 1.0 - fan.y * 0.15;
        return clamp((line(fan.x, 0.04) * 0.85 + ancestralBead(p, r, fan) * 0.6) * fade, 0.0, 1.0);
    }
    return line(min(abs(p.x), abs(p.y)), 0.05);
}

// The world light at the Focus dims the membrane, but its vessels keep a faint glow in the dark.
vec3 lit(vec3 color, float glowing) {
    float light = 0.28 + 0.72 * FloorLight;
    return color * mix(light, max(light, 0.62), clamp(glowing, 0.0, 1.0));
}

// Four kinds of vessel leave the Focus mouth as tributaries, strand i at angle i * PI/4 and opposite it:
// artery, vein, nerve and lymph, each in its own colour.
vec3 strandColor(int strand) {
    if (strand == 0) return vec3(0.60, 0.10, 0.12);
    if (strand == 1) return vec3(0.13, 0.17, 0.42);
    if (strand == 2) return vec3(0.52, 0.48, 0.38);
    return vec3(0.21, 0.31, 0.11);
}

float strandWidth(int strand) {
    if (strand == 0) return 0.045;
    if (strand == 1) return 0.028;
    if (strand == 2) return 0.018;
    return 0.032;
}

// Returns the strongest tributary at p in w and its colour in rgb.
vec2 tributaryGroove = vec2(10.0, 0.0);

vec4 tributaries(vec2 p, float r, float angle) {
    vec4 best = vec4(0.0);
    if (r < 0.55 || r > 3.2) {
        return best;
    }
    for (int strand = 0; strand < 4; strand++) {
        for (int side = 0; side < 2; side++) {
            float start = float(strand) * 0.785398 + float(side) * 3.14159265;
            float seed = float(strand * 2 + side) * 3.7;
            float meander = ((fbm(vec2(r * 0.9 + seed, 1.3)) - 0.5) * 1.5
                    + (noise(vec2(r * 3.1 + seed, 4.2)) - 0.5) * 0.35) / max(r, 0.6);
            float offset = mod(angle - start - meander + 3.14159265, 6.2831853) - 3.14159265;
            float width = strandWidth(strand) * (1.0 - smoothstep(0.75, 2.4, r));
            if (strand == 3) {
                // Lymph keeps its beads as it runs out.
                width *= 0.7 + 0.6 * smoothstep(0.3, 0.8, noise(vec2(r * 6.0, seed)));
            } else if (strand == 2) {
                // The nerve wavers.
                offset += sin(r * 19.0 + seed) * 0.02 / max(r, 0.6);
            }
            float strength = line(abs(offset) * r, width) * smoothstep(0.58, 0.74, r) * (1.0 - smoothstep(1.4, 2.6, r));
            if (strength > best.w) {
                best = vec4(strandColor(strand), strength);
                tributaryGroove = vec2(abs(offset) * r, width);
            }
        }
    }
    return best;
}

float angleGap(float a, float b) {
    return abs(mod(a - b + 3.14159265, 6.2831853) - 3.14159265);
}

float ringBand(float r, float radius, float width) {
    return exp(-sq((r - radius) / width));
}

// A rite pulse running along the floor in one direction: lane is (angle, head radius, strength). dir 1 runs out
// from the Focus with its trail behind it toward the Focus; dir -1 runs in, trailing back toward the trench.
float lanePulse(float r, float angle, vec4 lane, float dir) {
    if (lane.z <= 0.001) {
        return 0.0;
    }
    float across = angleGap(angle, lane.x) * max(r, 0.6);
    float beam = exp(-sq(across / (0.32 + 0.07 * r)));
    float behind = (lane.y - r) * dir;
    float trail = smoothstep(-0.1, 0.6, behind) * exp(-max(behind, 0.0) * 0.35);
    return lane.z * beam * (ringBand(r, lane.y, 0.55) + 0.45 * trail);
}

float wound(float r, float angle, float woundAngle, float level) {
    if (level <= 0.01) {
        return 0.0;
    }
    float across = angleGap(angle, woundAngle) * max(r, 1.0);
    return level * exp(-sq(across / 1.4)) * smoothstep(0.5, 2.5, r);
}

// How bruised the membrane is here: the rite's instability everywhere, and more toward each damaged anchor.
float bruising(float r, float angle) {
    float local = max(max(wound(r, angle, WoundAngles.x, WoundLevels.x), wound(r, angle, WoundAngles.y, WoundLevels.y)),
            max(wound(r, angle, WoundAngles.z, WoundLevels.z), wound(r, angle, WoundAngles.w, WoundLevels.w)));
    return clamp(RiteMood.y + local, 0.0, 1.0);
}

// Half the width of the brazier gash's strip, matching FloorMesh.GASH_HALF.
const float GASH_HALF = 0.8;

// The brazier ring: one trench carved into the ground around the floor through every socket. sAcross is the signed
// distance from its centre line in blocks; swell rises toward each socket, where it opens wider and deeper for the
// brazier. The trench is not geometry: each pixel of the opening follows the view ray down into a V-shaped cut,
// so its far wall and the blood pooled at its bottom shift with the camera like a real hole in the ground.
// The trench's half-width where it passes centre-line point c. It is sampled on the centre line, never at the pixel
// being shaded, so every view ray meets the same walls and their pattern stays fixed as the camera moves.
float trenchEdge(vec2 c, float swell, float taper) {
    return (0.24 + 0.16 * swell + (fbm(c * 1.3 + 11.0) - 0.5) * 0.12 + (noise(c * 4.5 + 3.0) - 0.5) * 0.05) * taper;
}

// How far down (0 at the lip, 1 at the floor) a ray entering at sAcross meets the wall on the given side, for a
// trench of half-width edge whose floor is 45% as wide.
float wallHit(float sAcross, float sink, float edge, float sideSign) {
    return clamp((sideSign * edge - sAcross) / (sink + sideSign * edge * 0.55 + 1.0e-4), 0.0, 1.0);
}

vec4 gash(vec2 p, float sAcross, float swell, vec3 view, vec2 side, float taper, float dig, vec3 ground) {
    float across = abs(sAcross);
    // Where the dig front is cutting, a bright seam runs down the trench's line even before it has opened.
    float seam = dig * exp(-sq(across / (0.05 + 0.15 * taper)));
    // Toward a break in the ground the cut pinches shut, and its crust and capillaries fade with it.
    float closing = smoothstep(0.0, 0.35, taper);
    vec2 centre = p - side * sAcross;
    float edge = trenchEdge(centre, swell, taper);
    float opening = smoothstep(edge + 0.012, edge - 0.012, across);
    // Outside the cut: a crust of torn ground at the lip, a shadow where the ground falls away, and capillaries.
    float crust = smoothstep(edge + 0.07, edge + 0.01, across) * (1.0 - opening) * closing;
    float shadow = smoothstep(edge + 0.2, edge, across) * (1.0 - opening) * closing;
    vec2 creep = cellular(p * 3.2 + 17.0);
    float capillaries = line(creep.y, 0.014) * smoothstep(edge + 0.28, edge + 0.06, across) * step(edge, across)
            * closing;
    // Late in a rite the brimming trench spills over its lip in runnels that soak into the torn ground.
    float spill = clamp(RiteBlood.y, 0.0, 1.0) * closing;
    float wet = 0.0;
    if (spill > 0.001) {
        float runnel = line(cellular(p * 1.7 + 41.0).y, 0.02 + 0.03 * spill)
                * smoothstep(edge + 0.08 + 0.32 * spill, edge, across) * step(edge, across);
        float soak = smoothstep(edge + 0.02 + 0.12 * spill, edge, across) * step(edge, across) * spill;
        wet = max(runnel * spill, soak);
    }
    if (opening + shadow + capillaries + seam + wet <= 0.003) {
        discard;
    }
    if (opening < 0.5) {
        vec3 color = mix(vec3(0.0), BaseColor * 0.3 + VeinColor * 0.12, crust);
        color = mix(color, VeinColor * 0.6, capillaries * (1.0 - crust));
        // The dig front throws its glow onto the torn lip it is cutting.
        color += GlowColor * (seam * 1.4 + dig * crust * 0.9);
        float alpha = max(max(max(crust * 0.9, shadow * 0.45), capillaries * 0.8), seam);
        color = mix(color, VeinColor * (0.5 + 0.2 * Beat) + GlowColor * 0.05, wet);
        alpha = max(alpha, wet * 0.95);
        if (int(FloorStyle + 0.5) == 4) {
            // The Apotheos trench's light spills over its lip as an orange halo.
            float halo = exp(-max(across - edge, 0.0) / 0.12) * (1.0 - opening) * closing;
            return vec4(lit(color, capillaries + seam) + vec3(1.0, 0.5, 0.06) * halo * 0.8, max(alpha, halo * 0.75));
        }
        return vec4(lit(color, capillaries + seam + dig + wet * 0.4), alpha);
    }

    // Follow the view ray down into the trench. The part of the ray running along the trench moves which stretch of
    // it is seen; the part running across it decides whether it reaches the floor or meets a wall.
    vec3 ray = normalize(view);
    bool apotheos = int(FloorStyle + 0.5) == 4;
    // The Apotheos trench is cut a little deeper, down to its channel of light.
    float depth = (0.34 + 0.18 * swell) * mix(0.3, 1.0, taper) * (apotheos ? 1.1 : 1.0);
    // The world stage fills the trench: the pooled blood rises toward the lip, and the view ray meets its surface
    // before it reaches the walls below. The walls narrow from the lip to the floor (0.45 of the lip), so the pool
    // widens as it rises. Strain darkens and clots it.
    float fill = clamp(RiteBlood.x, 0.0, 1.0);
    if (fill > 0.01 && !apotheos) {
        float surfaceDepth = depth * (1.0 - 0.92 * fill);
        vec2 toSurface = ray.xz / max(-ray.y, 0.12) * surfaceDepth;
        float acrossPool = sAcross + dot(side, toSurface);
        float poolHalf = edge * (0.45 + 0.55 * (1.0 - surfaceDepth / max(depth, 0.001)));
        if (abs(acrossPool) < poolHalf) {
            vec2 at = p + toSurface;
            float sheen = pow(noise(at * 4.0 + vec2(HemoTime * 0.02, -HemoTime * 0.013)), 3.0);
            float ripple = 0.5 + 0.5 * sin(dot(at, side) * 9.0 - HemoTime * 0.25);
            vec3 pool = VeinColor * (0.42 + 0.18 * fbm(at * 5.0)) + GlowColor * (sheen * 0.3 + ripple * Beat * 0.06);
            pool = mix(pool, VeinColor * 0.25 + BaseColor * 0.2, clamp(RiteMood.y, 0.0, 1.0) * 0.6);
            pool += GlowColor * smoothstep(poolHalf * 0.8, poolHalf, abs(acrossPool)) * fill * 0.15;
            return vec4(lit(pool, 0.4 + 0.3 * fill), 1.0);
        }
    }
    vec2 drop = ray.xz / max(-ray.y, 0.12) * depth;
    float sink = dot(side, drop);
    // The trench is treated as locally straight, so the ray may only look a short way along it; at grazing angles a
    // longer reach would carry the walls round a bend they do not follow and crease them.
    vec2 dropAlong = drop - side * sink;
    float alongLength = length(dropAlong);
    if (alongLength > 0.6) {
        dropAlong *= 0.6 / alongLength;
    }
    drop = side * sink + dropAlong;
    float sFloor = sAcross + sink;
    float floorHalf = trenchEdge(centre + dropAlong, swell, taper) * 0.45;
    // The trench floor: blood pooled in shadow, and at each socket a lumpy, glistening clot.
    vec2 bottom = p + drop;
    float sheen = pow(noise(bottom * 5.0 + vec2(HemoTime * 0.015, -HemoTime * 0.01)), 3.0);
    vec3 blood = VeinColor * (0.32 + 0.2 * fbm(bottom * 6.0)) + GlowColor * sheen * 0.16;
    float clot = swell * smoothstep(0.32, 0.6, fbm(bottom * 7.0 + 5.0));
    blood = mix(blood, VeinColor * (0.5 + 0.25 * Beat) + GlowColor * 0.05, clot * 0.8);
    float hug = smoothstep(floorHalf * 1.3, floorHalf * 0.3, abs(sFloor));
    blood *= 0.45 + 0.35 * hug;
    // A sloping wall of raw tissue. Its width is refined at the stretch of trench the ray actually reaches.
    float sideSign = sFloor > 0.0 ? 1.0 : -1.0;
    float t = wallHit(sAcross, sink, edge, sideSign);
    vec2 wallCentre = centre + dropAlong * t;
    t = wallHit(sAcross, sink, trenchEdge(wallCentre, swell, taper), sideSign);
    wallCentre = centre + dropAlong * t;
    // Each wall is patterned by where it lies along the trench and how deep, so the pattern is fixed in place.
    vec2 site = wallCentre + side * sideSign * 0.37;
    vec3 wall = trenchWall(site, t * depth, t, ground);
    // The ground overhangs the wall a little: a shadow tucked under the lip.
    wall *= 0.7 + 0.3 * smoothstep(0.0, 0.06, t);
    // The pooled blood's edge is ragged and soft, lapping a little way up the walls rather than ending in a line.
    float shore = floorHalf * (1.0 + (noise(centre * 5.0 + 13.0) - 0.5) * 0.6 + (noise(centre * 17.0) - 0.5) * 0.2);
    float pooled = smoothstep(shore * 1.35, shore * 0.75, abs(sFloor));
    if (apotheos) {
        // The chamber's ring of light runs along the trench floor: molten orange turning gold toward its middle,
        // white-hot down its centre, slowly churning. The walls above it are scorched dark, cracked with embers, lit
        // from below by the channel and catching a bright rim at the lip. All of it lights itself.
        float churn = fbm(bottom * 2.2 + vec2(HemoTime * 0.02, -HemoTime * 0.015));
        float x = abs(sFloor) / max(shore, 0.001);
        vec3 molten = mix(vec3(0.85, 0.3, 0.03), vec3(1.0, 0.72, 0.12), smoothstep(1.2, 0.2, x) * (0.7 + 0.3 * churn));
        molten += vec3(1.0, 0.96, 0.88) * exp(-sq(x / 0.4)) * (0.5 + 0.25 * Beat + 0.25 * churn);
        molten *= 0.85 + 0.3 * churn;
        float ember = line(cellular(site * 3.0 + 3.0).y, 0.012) * smoothstep(0.15, 0.6, t)
                * smoothstep(0.55, 0.75, valueNoise3(vec3(site * 2.0, 7.0)));
        vec3 scorched = mix(vec3(0.12, 0.05, 0.035), vec3(0.85, 0.32, 0.04), pow(t, 1.6));
        // Strata in the cut glow where the channel's heat has reached them.
        float strata = line(abs(fract(t * depth * 4.0 + valueNoise3(vec3(site * 3.0, 1.0)) * 0.8) - 0.5), 0.03);
        scorched += vec3(1.0, 0.5, 0.08) * strata * (0.06 + 0.22 * t);
        scorched *= 0.75 + 0.5 * valueNoise3(vec3(site.x, t * depth, site.y) * 14.0);
        scorched += vec3(1.0, 0.45, 0.05) * ember * (0.3 + 0.6 * t) * (0.7 + 0.3 * Beat);
        scorched += vec3(1.0, 0.55, 0.1) * (1.0 - smoothstep(0.0, 0.07, t)) * 0.7;
        vec3 channel = mix(scorched, molten, pooled);
        channel *= (1.0 + RiteSwell.x * 0.2) * (1.0 - RiteSwell.y);
        return vec4(channel, 1.0);
    }
    vec3 color = mix(wall, blood, pooled);
    // Freshly cut flesh glows behind the dig front, brightest where it is still cutting.
    color += GlowColor * dig * (0.55 + 0.9 * seam);
    return vec4(lit(color, 0.35 + dig), 1.0);
}

// Half the width of the Focus pad's quad (CardinalFloorConsecrationRenderer.PAD_HALF), and how much larger than a
// brazier socket's pad it is drawn: large enough that its clot opening sits just inside the basin.
const float PAD_HALF = 1.45;
const float PAD_SCALE = 2.45;

// The Focus mouth as a fleshy pad, in the proportions of a brazier socket's pad: a ragged rim, rootlets creeping over
// the floor, and a wet lip around a round clot opening. The opening shows the basin's dark clot, and the medium in it.
vec4 focusPad(vec2 p, vec2 lp) {
    vec2 q = lp / PAD_SCALE;
    float d = length(q);
    float a = atan(q.y, q.x);
    float seed = FloorSeed * 0.37;
    // A ragged, lopsided opening rather than a turned hole. Its widest bulge (0.1725 * PAD_SCALE, about 0.42 blocks)
    // stays inside the Focus basin's walls (0.4375), so the bare iron rim never shows past the pad.
    float clotEdge = 0.1485 + (noise(vec2(a * 3.0, seed + 2.0)) - 0.5) * 0.034
            + (noise(vec2(a * 7.0 + seed, 5.0)) - 0.5) * 0.014;
    if (d < clotEdge) {
        discard;
    }
    float edge = 0.34 + (fbm(vec2(a * 1.6 + seed, seed)) - 0.5) * 0.18 + (noise(vec2(a * 5.0 + seed, 3.0)) - 0.5) * 0.06;
    float pad = smoothstep(edge + 0.02, edge - 0.03, d);
    float rootlets = line(abs(sin(a * 2.5 + seed + (fbm(q * 3.0 + seed) - 0.5) * 3.0)) * d,
            0.01 + 0.02 * smoothstep(0.58, 0.3, d)) * smoothstep(0.58, 0.36, d) * step(edge - 0.03, d);
    float body = max(pad, rootlets);
    if (body <= 0.003) {
        discard;
    }
    vec2 tissue = p * 1.7 + seed;
    vec3 color = mix(BaseColor, VeinColor * 0.55, 0.35) * (0.7 + 0.6 * fbm(tissue * 2.0));
    color = mix(color, VeinColor * 0.8, line(cellular(tissue * 1.6).y, 0.02) * 0.6);
    // A wet lip rolls over into the opening, darkening where it folds down. Its swelling and its shine come and go
    // round the mouth in patches, with a slow pulse creeping round it, so it never reads as a lit ring.
    float lipWidth = 0.02 + 0.025 * noise(vec2(a * 4.0 + seed, 11.0));
    float lip = exp(-sq((d - clotEdge - lipWidth) / lipWidth));
    float wet = smoothstep(0.25, 0.8, fbm(vec2(a * 2.2 + seed, HemoTime * 0.004 + seed)));
    float creep = 0.5 + 0.5 * sin(a * 2.0 - HemoTime * 0.03 + seed);
    color *= 0.55 + 0.45 * smoothstep(clotEdge, clotEdge + 0.06, d);
    color += VeinColor * lip * wet * (0.18 + 0.32 * Beat * creep);
    color = mix(color, vec3(0.16, 0.006, 0.018), lip * (1.0 - wet) * 0.5);
    // The rite reaches the Focus mouth: the staff's shock bursts out of it, an offering's pulse flares its lip, and it
    // swells, bruises or dims with the rest of the membrane.
    float padShock = RiteRings.y * ringBand(length(lp), RiteRings.x, 0.6);
    float flare = RiteSurge.w + padShock * 0.6;
    color += GlowColor * (lip * wet * 1.1 + 0.15) * flare;
    color *= (1.0 + RiteSwell.x * 0.2 + RiteMood.x * 0.1) * (1.0 - RiteSwell.y);
    color = mix(color, vec3(0.13, 0.035, 0.11), RiteMood.y * 0.4);
    // The pad thins and darkens toward its ragged rim, so it sits in the floor.
    color *= 0.72 + 0.28 * smoothstep(edge, edge * 0.5, d);
    color = mix(color, VeinColor * 0.6, rootlets * (1.0 - pad));
    float glowing = lip + rootlets + flare;
    if (int(FloorStyle + 0.5) == 4) {
        // The Apotheos mouth is the chamber's void: pitch black within, the prism banding its rim as it opens.
        float rim = clamp((d - clotEdge) / 0.16, 0.0, 1.0);
        float bands = 0.6 + 0.4 * sin(d * 70.0 - HemoTime * 0.12);
        color = mix(color, vec3(0.004, 0.003, 0.004), (1.0 - smoothstep(0.0, 0.05, d - clotEdge)) * 0.85);
        color += apotheosPrism(rim) * (1.0 - smoothstep(0.55, 1.0, rim)) * smoothstep(0.0, 0.08, rim) * bands
                * pad * (0.55 + 0.25 * Beat + 0.5 * flare);
        glowing += 1.0 - rim;
    }
    if (int(FloorStyle + 0.5) == 5) {
        // The Qliphoth mouth is the black hole's horizon and the inner edge of its accretion disk.
        vec4 hole = qliphothHole(p, length(p), atan(p.y, p.x), HemoTime * 0.05, Beat);
        color = hole.rgb * (1.0 + RiteSwell.x * 0.2) * (1.0 - RiteSwell.y) + GlowColor * flare * 0.4;
        glowing += hole.a;
    }
    float alpha = pad * (0.92 - 0.25 * smoothstep(edge * 0.6, edge, d)) + rootlets * (1.0 - pad) * 0.85;
    return vec4(lit(color, glowing), alpha);
}

// Half the width of an anchor sore's quad (AnchorSores.HALF), and how deep its pit is in blocks.
const float SORE_HALF = 0.78;
const float SORE_DEPTH = 0.5;

// A rite's anchor as a sore in the ground. lp is the position across its quad in blocks, fill how full of blood it
// is, and broken whether instability has torn it. Outside its ragged mouth the floor slumps into it, the floor's own
// vessels swelling and flushing toward it on the heartbeat (hungrier while it is empty). Inside, each pixel
// follows the view ray down into a bowl: raw walls of flesh, a dark hungry bottom, and blood rising to the brim as
// the anchor is fed, so the pit and its pool shift with the camera like a real hole in the ground.
vec4 sore(vec2 p, vec2 lp, float fill, float broken, vec3 view) {
    float d = length(lp);
    float a = atan(lp.y, lp.x);
    // One seed per sore, from its centre, so its rim never seams where the quad crosses a block edge.
    float seed = hash(floor(p - lp + vec2(0.5))) * 40.0;
    float mouth = 0.34 + (noise(vec2(a * 1.9 + seed, seed)) - 0.5) * 0.1 + (noise(vec2(a * 6.0, seed + 3.0)) - 0.5) * 0.035;
    float halo = 0.73 + (noise(vec2(a * 1.5 + seed, 21.0)) - 0.5) * 0.06;
    if (d > halo) {
        discard;
    }
    float hunger = 1.0 - fill;
    float beat = 0.4 + 0.6 * Beat;
    vec3 inflamed = broken > 0.5 ? vec3(0.30, 0.06, 0.20) : VeinColor * 1.2 + GlowColor * 0.12;
    if (d >= mouth) {
        // No raised lip laid over the floor: the membrane itself slumps into the mouth. Its own vessels (the same
        // field the floor draws) swell and flush as they near the sore and a few channels run down into it, while
        // the tint and the slope's shading fade out across the halo so the floor shows through.
        float t = clamp((d - mouth) / max(halo - mouth, 0.01), 0.0, 1.0);
        float near = 1.0 - smoothstep(0.0, 1.0, t);
        float slump = smoothstep(0.45, 0.0, t);
        float wetLip = exp(-sq((d - mouth - 0.012) / 0.02));
        vec2 warp = vec2(fbm(p * 0.6 + vec2(3.1, 7.7)), fbm(p * 0.6 + vec2(9.4, 1.3))) - 0.5;
        vec2 floorTissue = p + warp * 1.5 + vec2(sin(HemoTime * 0.011), cos(HemoTime * 0.009)) * 0.08;
        float girth = fbm(floorTissue * 1.3 + 4.0);
        float swollen = line(cellular(floorTissue * 0.95).y, (0.008 + girth * 0.05) * (1.0 + 0.6 * near)) * 0.8;
        float channels = line(abs(sin(a * 5.0 + seed + (fbm(lp * 3.0 + seed) - 0.5) * 2.0)) * d, 0.004 + 0.008 * near)
                * smoothstep(0.85, 0.15, t) * 0.7;
        float vessel = clamp(max(swollen, channels) * near, 0.0, 1.0);
        // The flesh round the mouth darkens toward the blood rather than paling: clotted at the lip, inflamed beyond.
        vec3 clot = vec3(0.16, 0.006, 0.018);
        vec3 tint = broken > 0.5 ? inflamed * 0.4 : mix(VeinColor * 0.4, clot, slump);
        vec3 color = mix(tint, mix(VeinColor * 0.8, vec3(0.40, 0.018, 0.04), slump) * (0.9 + 0.4 * beat * near), vessel);
        // The slope into the pit falls into shadow; the wet edge catches the heartbeat.
        color *= 1.0 - 0.6 * slump;
        color += vec3(0.40, 0.03, 0.05) * wetLip * beat * (0.1 + 0.3 * hunger);
        color += inflamed * vessel * beat * 0.12 * hunger;
        if (broken > 0.5) {
            float tear = line(abs(sin(a * 5.0 + seed) * d - 0.02), 0.012) * slump;
            color = mix(color, vec3(0.04, 0.0, 0.03), tear);
        }
        float alpha = clamp(near * near * 0.35 + slump * 0.6 + vessel * 0.6 + wetLip * 0.4, 0.0, 1.0)
                * (1.0 - smoothstep(0.8, 1.0, t));
        if (alpha <= 0.004) {
            discard;
        }
        return vec4(lit(color, vessel * beat + wetLip * 0.6), alpha);
    }

    // Inside the mouth: follow the view ray down into a round bowl, y = -depth * (1 - (r / mouth)^2), and stop at
    // the blood's surface or the wall. Both are solved exactly, so the wall has no stepped terraces.
    vec3 ray = normalize(view);
    ray.y = min(ray.y, -0.08);
    ray = normalize(ray);
    float surface = -SORE_DEPTH * (hunger * 0.92) - 0.012;
    float k = SORE_DEPTH / (mouth * mouth);
    float qa = k * dot(ray.xz, ray.xz);
    float qb = 2.0 * k * dot(lp, ray.xz) - ray.y;
    float qc = k * dot(lp, lp) - SORE_DEPTH;
    float wallHit = qa > 1.0e-6 ? (-qb + sqrt(max(qb * qb - 4.0 * qa * qc, 0.0))) / (2.0 * qa) : -qc / qb;
    float poolHit = surface / ray.y;
    bool pooled = fill > 0.004 && poolHit < wallHit;
    vec3 at = vec3(lp.x, 0.0, lp.y) + ray * (pooled ? poolHit : wallHit);
    vec3 color;
    float glowing;
    if (pooled) {
        // Pooled blood, painted as the Iron Brazier's and the Bloodwell's pools are: thick and dark, marbled as it
        // slowly turns, fresh brighter blood welling up at the centre where the jet rises, rings running out from
        // it, and a glossy surface that mirrors the dark wall at a glancing view. The floor's palette never tints it.
        vec2 q = at.xz / mouth;
        float r = length(q);
        float turn = HemoTime * 0.0015 + 0.5 * sin(HemoTime * 0.006) * (1.0 - min(r, 1.0));
        vec2 c = mat2(cos(turn), -sin(turn), sin(turn), cos(turn)) * q;
        float marble = fbm(c * 3.0 + seed);
        color = mix(vec3(0.16, 0.006, 0.018), vec3(0.40, 0.018, 0.04), smoothstep(0.3, 0.75, marble));
        float full = smoothstep(0.92, 1.0, fill);
        color += vec3(0.30, 0.012, 0.02) * full * exp(-r * r * 4.0) * (0.7 + 0.3 * Beat);
        float rings = sin(r * 26.0 - HemoTime * 0.32) * smoothstep(1.0, 0.15, r) * smoothstep(0.0, 0.12, r);
        // Glancing views mirror the dark wall; a faint sheen catches the rings.
        float glance = pow(1.0 - clamp(-ray.y, 0.0, 1.0), 5.0);
        color = mix(color, vec3(0.05, 0.012, 0.02), glance * 0.6);
        color += vec3(0.22, 0.03, 0.04) * max(rings, 0.0) * full * 0.25;
        float sheen = pow(noise(c * 6.0 + vec2(HemoTime * 0.01, -HemoTime * 0.008)), 4.0);
        color += vec3(0.30, 0.10, 0.11) * sheen * 0.35;
        // A dark meniscus where the blood meets the wall.
        color *= 0.55 + 0.45 * smoothstep(1.0, 0.75, r);
        if (broken > 0.5) {
            color = mix(color, vec3(0.10, 0.02, 0.08), 0.6);
        }
        glowing = 0.35 + full * 0.25;
    } else {
        // Raw walls: muscle fibres running down into the dark, wetter and darker with depth; at the bottom of an
        // empty sore a faint glow throbs, hungry for blood.
        float depth = clamp(-at.y / SORE_DEPTH, 0.0, 1.0);
        float around = atan(at.z, at.x);
        float fibres = valueNoise3(vec3(cos(around) * 3.0 + seed, at.y * 26.0, sin(around) * 3.0));
        float bands = line(abs(fract(at.y * 14.0 + fibres * 1.3) - 0.5), 0.05);
        color = mix(vec3(0.36, 0.035, 0.05), vec3(0.18, 0.02, 0.035) + BaseColor * 0.12, fibres);
        color = mix(color, BaseColor * 0.3, bands * 0.4);
        // The pit falls away into darkness, deepest under the overhanging lip.
        color *= mix(0.85, 0.05, pow(depth, 0.7));
        color *= 0.45 + 0.55 * smoothstep(0.0, 0.1, -at.y);
        color += vec3(0.45, 0.04, 0.06) * hunger * smoothstep(0.7, 1.0, depth) * (0.05 + 0.18 * Beat);
        if (broken > 0.5) {
            color = mix(color, vec3(0.13, 0.03, 0.11) * (0.5 + fibres), 0.65);
        }
        glowing = 0.25 + hunger * depth * Beat;
    }
    color *= 1.0 - RiteSwell.y;
    return vec4(lit(color, glowing), 1.0);
}

// The ground beyond the trench, lit only while a grounding rite's ring runs out through it: the pulse races along
// roots in the earth, brightest at its band, with a fading trail behind.
vec4 groundRing(vec2 p) {
    float r = length(p);
    float band = ringBand(r, RiteRings.z, 0.8);
    float behind = RiteRings.z - r;
    float trail = smoothstep(-0.2, 0.8, behind) * exp(-max(behind, 0.0) * 0.6) * 0.5;
    float strength = RiteRings.w * max(band, trail);
    vec2 warp = vec2(fbm(p * 0.5 + 3.0), fbm(p * 0.5 + 8.0)) - 0.5;
    float vessel = line(cellular(p * 0.8 + warp * 1.6 + 23.0).y, 0.02 + 0.04 * fbm(p * 1.3));
    float fine = line(cellular(p * 2.2 + 5.0).y, 0.01) * 0.5;
    float body = max(vessel, fine);
    float alpha = strength * (0.12 + 0.88 * body);
    if (alpha <= 0.004) {
        discard;
    }
    vec3 color = mix(VeinColor * 0.9, GlowColor, band * body);
    return vec4(lit(color, 1.0), clamp(alpha, 0.0, 1.0));
}

// The world stage's creep (RiteBlood.z, .w): veined membrane spread over the ground beyond the floor, out to
// RiteBlood.z blocks from the Focus behind a ragged front. A step's shock (RiteRings.xy) runs out across it, and
// strain (RiteMood.y) bruises it toward purple.
vec4 creepGround(vec2 p) {
    float r = length(p);
    float frontNoise = (fbm(p * 0.7 + 19.0) - 0.5) * 2.2 + (noise(p * 2.3 + 4.0) - 0.5) * 0.5;
    float covered = smoothstep(RiteBlood.z + frontNoise + 0.1, RiteBlood.z + frontNoise - 1.4, r)
            * clamp(RiteBlood.w, 0.0, 1.0);
    if (covered <= 0.003) {
        return vec4(0.0);
    }
    vec2 warp = vec2(fbm(p * 0.5 + 3.0), fbm(p * 0.5 + 8.0)) - 0.5;
    float vessel = line(cellular(p * 0.8 + warp * 1.6 + 23.0).y, 0.02 + 0.04 * fbm(p * 1.3));
    float fine = line(cellular(p * 2.2 + 5.0).y, 0.012) * 0.6;
    float body = max(vessel, fine);
    float film = 0.22 + 0.25 * fbm(p * 1.4 + 2.0);
    float pulse = Beat * exp(-max(r - FloorHalf, 0.0) * 0.15);
    float shock = RiteRings.y * ringBand(r, RiteRings.x, 0.9);
    vec3 color = mix(BaseColor * 0.7, VeinColor * (0.8 + 0.4 * pulse), body);
    color = mix(color, color * vec3(0.55, 0.45, 0.75), clamp(RiteMood.y, 0.0, 1.0));
    color += GlowColor * shock * body;
    float alpha = covered * (film * (1.0 - body) + body);
    return vec4(lit(color, body * (0.4 + 0.6 * pulse) + shock), clamp(alpha, 0.0, 1.0));
}

void main() {
    vec2 p = texCoord0;
    if (vertexColor.g > 0.07 && vertexColor.g < 0.12) {
        // Green 23 marks an anchor sore; its alpha packs the fill, above one half when the anchor is broken.
        // ("packed" is a reserved word in GLSL 1.50, and NVIDIA's compiler rejects it as a name.)
        float soreAlpha = vertexColor.a;
        float broken = soreAlpha > 0.5 ? 1.0 : 0.0;
        float fill = clamp(broken > 0.5 ? (soreAlpha - 0.502) / 0.498 : soreAlpha / 0.498, 0.0, 1.0);
        vec4 pit = sore(p, (vec2(vertexColor.r, vertexColor.b) * 2.0 - 1.0) * SORE_HALF, fill, broken, viewOffset);
        fragColor = linear_fog(vec4(pit.rgb * ColorModulator.rgb, pit.a * ColorModulator.a),
                vertexDistance, FogStart, FogEnd, FogColor);
        return;
    }
    if (vertexColor.g > 0.12 && vertexColor.g < 0.35) {
        vec4 mouth = focusPad(p, (vec2(vertexColor.r, vertexColor.b) * 2.0 - 1.0) * PAD_HALF);
        fragColor = linear_fog(vec4(mouth.rgb * ColorModulator.rgb, mouth.a * vertexColor.a * ColorModulator.a),
                vertexDistance, FogStart, FogEnd, FogColor);
        return;
    }
    if (vertexColor.g < 0.06) {
        // The vertex shader unpacked the trench's sideways direction and how far it is from pinching shut, at a
        // break in the ground or where the dig front started; its ground position comes from the camera-relative one.
        float sideLength = length(gashSide);
        vec2 side = sideLength < 1.0e-4 ? vec2(1.0, 0.0) : gashSide / sideLength;
        float taper = clamp((gashLean - 0.05) / 0.95, 0.0, 1.0);
        // The trench's vertex alpha carries the dig front's glow, not transparency.
        float dig = clamp((1.0 - vertexColor.a) / 0.745, 0.0, 1.0);
        vec4 tear = gash(viewOffset.xz - FocusOffset, (vertexColor.r * 2.0 - 1.0) * GASH_HALF, vertexColor.b,
                viewOffset, side, taper, dig, soilTint);
        tear.rgb *= (1.0 - RiteSwell.y) * (1.0 + RiteSwell.x * 0.15);
        fragColor = linear_fog(vec4(tear.rgb * ColorModulator.rgb, tear.a * ColorModulator.a),
                vertexDistance, FogStart, FogEnd, FogColor);
        return;
    }
    if (vertexColor.g > 0.35 && vertexColor.g < 0.58) {
        // Green 128 marks the ground beyond the trench; its blue carries the trench distance, so it stays outside.
        if (vertexColor.b > 0.5) {
            discard;
        }
        // Red 0 marks the world stage's creep; red 255 the grounding ring's passing pulse.
        vec4 ring = vertexColor.r < 0.5 ? creepGround(p) : groundRing(p);
        if (ring.a <= 0.004) {
            discard;
        }
        fragColor = linear_fog(vec4(ring.rgb * ColorModulator.rgb, ring.a * vertexColor.a * ColorModulator.a),
                vertexDistance, FogStart, FogEnd, FogColor);
        return;
    }
    float r = length(p);
    float square = max(abs(p.x), abs(p.y));
    float angle = atan(p.y, p.x);
    // A brazier's seat mound is the floor membrane itself heaped up; its red channel carries its slope shading, and
    // elsewhere red marks how far a drape hangs.
    bool seat = vertexColor.g > 0.58 && vertexColor.g < 0.72;
    // Blue 0 on a seat marks the Focus throat gripping a planted staff: flesh, not the basin's clot.
    bool throat = seat && vertexColor.b < 0.5;
    // Inside the brazier trench, flat blue carries the signed distance inside its centre line: the membrane stops
    // exactly there, and the laid floor's wandering edge no longer applies.
    bool toTrench = !seat && vertexColor.b > 0.38 && vertexColor.b < 0.62;
    if (toTrench && vertexColor.b < 0.5) {
        discard;
    }
    float shade = seat ? vertexColor.r : 1.0;
    float hang = seat ? 0.0 : 1.0 - vertexColor.r;
    int style = int(FloorStyle + 0.5);
    // The Apotheos floor keeps the hush: its heart beats at under half strength.
    float beatLevel = Beat * (style == 4 ? 0.4 : 1.0);
    // Drapes hanging over a ledge end in a ragged hem, with the odd longer drip.
    if (hang > 0.001) {
        float hem = 0.3 + 0.55 * fbm(p * 2.4 + 7.0) + 0.35 * smoothstep(0.72, 0.95, noise(p * 6.0 + 2.0));
        // A drape end with nothing to carry on into curls its hem up to nothing (green 0.78 there, 1 elsewhere).
        hem *= smoothstep(0.0, 1.0, clamp((vertexColor.g - 0.78) / 0.22, 0.0, 1.0));
        if (hang > hem) {
            discard;
        }
    }

    // The settled edge wanders off the laid square: it creeps onto the ground in lobes and bites back in places.
    float bulge = (fbm(p * 0.75 + FloorSeed * 1.7) - 0.5) * 1.7 + (noise(p * 3.3 + 5.0) - 0.5) * 0.35;
    // The Qliphoth floor's roots creep further onto the ground around it.
    float boundary = FloorHalf + 0.5 + clamp(bulge * (style == 5 ? 1.4 : 1.0), -0.65, style == 5 ? 1.6 : 0.9);
    float outside = toTrench || seat ? -1.0 : square - boundary;
    float inside = smoothstep(0.035, -0.035, outside);

    // While forming, the membrane creeps outward with a ragged front.
    float frontNoise = (fbm(p * 0.9 + FloorSeed) - 0.5) * 1.6 + (noise(p * 2.7 + 3.0) - 0.5) * 0.45;
    float edge = Spread + frontNoise;
    float covered = smoothstep(edge + 0.04, edge - 0.10, r) * inside;
    if (covered <= 0.003) {
        discard;
    }

    vec2 warp = vec2(fbm(p * 0.6 + vec2(3.1, 7.7)), fbm(p * 0.6 + vec2(9.4, 1.3))) - 0.5;
    vec2 tissue = p + warp * 1.5 + vec2(sin(HemoTime * 0.011), cos(HemoTime * 0.009)) * 0.08;
    // A wave's flinch: a contraction runs in from the edge, puckering the tissue toward the Focus as it passes.
    float ripple = RiteSwell.z * ringBand(r, RiteDraw.w, 0.5);
    tissue -= p / max(r, 0.001) * ripple * 0.12;
    // Instability bruises the membrane and thins its vessels.
    float bruise = bruising(r, angle);
    float thinning = 1.0 - 0.55 * bruise;

    float grain = fbm(tissue * 1.8);
    vec2 vessels = cellular(tissue * 0.95);
    vec2 capillaries = cellular(tissue * 2.6 + 11.0);
    // Vessels swell and pinch along their length, and capillaries thin out in patches.
    float girth = fbm(tissue * 1.3 + 4.0);
    float artery = line(vessels.y, (0.008 + girth * 0.05) * thinning);
    float capillary = line(capillaries.y, (0.006 + girth * 0.02) * thinning) * smoothstep(0.3, 0.62, fbm(tissue * 0.55 + 9.0)) * 0.6;
    // The main vessels' distance and half-width, for their groove.
    vec2 arteryLine = vec2(vessels.y, (0.008 + girth * 0.05) * thinning);
    if (style == 0) {
        // Threshold: no vessel web, but an iron-dark scab crazed into plates, with blood showing in every crack.
        vessels = cellular(tissue * 1.2 + 31.0);
        arteryLine = vec2(vessels.y, (0.012 + girth * 0.03) * thinning);
        artery = line(arteryLine.x, arteryLine.y);
        capillary = line(cellular(tissue * 3.6 + 7.0).y, 0.004) * smoothstep(0.45, 0.7, fbm(tissue * 0.8 + 2.0)) * 0.35;
    } else if (style == 1) {
        // Dominion: roots run out from the seal and fork as they claim the ground; only faint seams cross the clot.
        arteryLine = dominionRoots(r, angle, tissue);
        arteryLine.y *= thinning;
        artery = line(arteryLine.x, arteryLine.y);
        capillary *= 0.35;
    } else if (style == 3) {
        // Working: no web, but conduits engraved into the iron, all leading on to the four bus channels.
        // The iron has grown rather than been cut: plates and conduits both sit on the membrane's slow warp, and each
        // conduit swells and pinches along its run like a vessel.
        vec2 grown = p + warp * 0.32;
        vec2 conduits = workingConduits(grown);
        arteryLine = vec2(conduits.x, (0.012 + girth * 0.03) * thinning);
        artery = max(stroke(conduits.x, arteryLine.y), stroke(abs(conduits.y - 0.05), 0.012) * 0.9);
        capillary = 0.0;
    }
    float laid = motif(p, r, style) * smoothstep(0.2, -0.4, square - FloorHalf - 0.5);
    vec3 tendrilTint = vec3(0.0);
    float tendrilGlow = 0.0;
    float tendrilShadow = 0.0;
    if (style == 4) {
        // The Apotheos floor carries tendrils rather than a vessel web.
        artery = apotheosTendrils(p, r, angle, tendrilTint, tendrilGlow, tendrilShadow);
        capillary = 0.0;
    }

    // Every vessel ends at the Focus: spokes converge on its rim and its basin holds a pool.
    float converge = line(abs(sin(angle * 3.0 + fbm(p * 0.8) * 2.4)) * r, 0.03 + 0.03 * girth)
            * smoothstep(2.8, 0.9, r) * (throat ? 1.0 : smoothstep(0.45, 0.75, square))
            * (style == 4 || ((style == 0 || style == 1 || style == 3) && !throat) ? 0.0 : 1.0);
    float pool = throat ? 0.0 : 1.0 - smoothstep(0.38, 0.47, square);
    vec4 tributary = tributaries(p, r, angle);

    // Blood moves outward from the Focus on each heartbeat; on the Threshold floor it closes in through the doorways.
    float wave = 0.5 + 0.5 * (style == 0 ? sin(square * 2.6 + HemoTime * 0.21) : sin(r * 2.2 - HemoTime * 0.21));
    float pulse = beatLevel * exp(-r * 0.18) + wave * 0.18;

    float veins = clamp(artery + capillary + laid * 0.85 + converge, 0.0, 1.0);
    vec3 color = BaseColor * (0.62 + grain * 0.75);
    if (style == 3) {
        // Lacquered hematic iron laid in block-wide plates: each brushed along one grain, alternating like a
        // parquet, its seams dark and a rivet set in from every corner.
        // The plates have grown like scutes: warped, their edges swollen and rounded down into soft seams, and their
        // rivets knotted nodules that sit a little off the corners.
        vec2 grown = p + warp * 0.32;
        vec2 plate = floor(grown + 0.5);
        vec2 local = fract(grown + 0.5);
        bool across = mod(plate.x + plate.y, 2.0) < 0.5;
        float brushed = noise(across ? vec2(grown.x * 1.5, grown.y * 26.0) : vec2(grown.x * 26.0, grown.y * 1.5) + 9.0);
        float seamWobble = (noise(grown * 7.0 + 3.0) - 0.5) * 0.05;
        vec2 toEdge = min(local, 1.0 - local) + seamWobble;
        float edgeDistance = min(toEdge.x, toEdge.y);
        float swell = smoothstep(0.0, 0.22, edgeDistance);
        color = BaseColor * (0.75 + 0.3 * brushed + 0.35 * grain) * (0.55 + 0.45 * sqrt(swell));
        color = mix(color, VeinColor * 0.22, (1.0 - smoothstep(0.0, 0.05, edgeDistance)) * 0.6);
        vec2 nodule = toEdge - 0.13 - (hash2(plate) - 0.5) * 0.06;
        float rivet = smoothstep(0.05, 0.0, length(nodule));
        color = mix(color, BaseColor * 2.0 + VeinColor * 0.15, rivet * 0.75);
    }
    // Light a floor gives off itself (the Apotheos funnel, the Threshold's cracks, the Dominion's claim, the Working's
    // conduits), added after
    // the world light so it still shows in the dark.
    vec3 ownLight = vec3(0.0);
    vec3 ray = normalize(viewOffset);
    if (style == 4) {
        // The Apotheos chamber's funnel laid flat on the floor. The ground is the chamber wall's membrane: a dull
        // slate gradient, deep by the Focus and paler toward the edge, ribbed in strands that fan out from the mouth.
        float meat = fbm(tissue * 0.9 + vec2(0.0, -HemoTime * 0.004));
        float fibre = fbm(tissue * 2.2 - p / max(r, 0.001) * HemoTime * 0.012);
        float hairline = fbm(tissue * 4.6 + fibre * 2.0);
        float fiberDepth = smoothstep(0.2, 0.86, meat + fibre * 0.42);
        vec3 slate = mix(vec3(0.035, 0.05, 0.068), vec3(0.13, 0.16, 0.2), smoothstep(0.6, FloorHalf + 0.6, r));
        color = slate * (0.78 + fiberDepth * 0.36 + grain * 0.18);
        // Ribs: raised strands fanning from the mouth, lit along one flank and shaded along the other.
        float ribPhase = fract((angle / 6.2831853 + 0.5) * 26.0 + (fbm(tissue * 0.45 + 5.0) - 0.5) * 1.4) - 0.5;
        float rib = (1.0 - smoothstep(0.06, 0.42, abs(ribPhase))) * smoothstep(0.7, 1.5, r);
        color *= 0.7 + 0.4 * rib;
        color += vec3(0.06, 0.08, 0.11) * smoothstep(0.0, 0.25, ribPhase) * rib * 0.5;
        color *= 1.0 - 0.3 * smoothstep(0.42, 0.5, abs(ribPhase)) * smoothstep(0.7, 1.5, r);
        // Fine cross fibres and the wall's pale blue and pink scratch traces.
        float crossFibre = 1.0 - smoothstep(0.02, 0.08, abs(fract(r * 4.5 + hairline * 0.6) - 0.5));
        float scratch = crossFibre * smoothstep(0.62, 0.95, fibre + hairline * 0.34) * smoothstep(0.8, 1.4, r);
        color = mix(color, color * 0.72, smoothstep(0.5, 0.9, hairline) * 0.3);
        ownLight += mix(vec3(0.68, 0.86, 0.96), vec3(1.0, 0.58, 0.72), smoothstep(0.3, 0.8, meat))
                * scratch * 0.12;

        // The bands rise out of the Focus, buckling with the membrane rather than running as clean circles.
        float ringR = r + (fbm(tissue * 0.7 + 17.0) - 0.5) * 0.7 + (noise(tissue * 2.4) - 0.5) * 0.12;
        float ringCell = abs(fract(ringR * 1.5 - HemoTime * 0.03 + meat * 0.5) - 0.5);
        float band = 1.0 - smoothstep(0.14, 0.36, ringCell);
        float tight = 1.0 - smoothstep(0.03, 0.13, ringCell);
        float gap = smoothstep(0.32, 0.50, ringCell) * (1.0 - band * 0.7);
        float funnel = smoothstep(0.55, 0.9, r) * smoothstep(boundary + 0.2, boundary - 1.4, square);
        color = mix(color, slate * 0.35, gap * 0.4 * funnel);

        float veinGleam = fbm(tissue * 3.1 + vec2(HemoTime * 0.006, -HemoTime * 0.01));
        float arterial = sq(band) * band * smoothstep(0.55, 0.9, veinGleam + fibre * 0.18) * funnel;
        float sheen = sq(sq(band)) * band * smoothstep(0.72, 0.98, veinGleam + fibre * 0.26 + meat * 0.1) * funnel;
        ownLight += vec3(1.0, 0.12, 0.30) * arterial * 0.7 + vec3(1.0, 0.92, 0.86) * sheen * 0.5;

        // The prism: a spectrum from red at the mouth out through violet, cyan and gold, riding the bands; past
        // its reach the bands keep a magenta glow out to the floor's edge.
        float prismReach = max(FloorHalf * 0.75, 2.0);
        float prismMask = smoothstep(0.5, 0.75, r) * (1.0 - smoothstep(prismReach * 0.75, prismReach, r))
                * clamp(band + tight * 0.4, 0.0, 1.0) * (0.6 + 0.4 * smoothstep(0.3, 0.8, meat + fibre * 0.3));
        vec3 prism = apotheosPrism(clamp((ringR - 0.55) / (prismReach - 0.55), 0.0, 1.0));
        color = mix(color, prism * 0.4, prismMask * 0.4);
        ownLight += prism * prismMask * (0.48 + 0.3 * beatLevel);
        float outerBands = band * funnel * smoothstep(prismReach * 0.75, prismReach, r);
        ownLight += GlowColor * outerBands * (0.07 + 0.08 * beatLevel);

        // Violet fog between the bands, warming to pink toward the floor's edge.
        float fog = smoothstep(0.3, 0.8, fbm(tissue * 1.2 - p / max(r, 0.001) * HemoTime * 0.018) + fibre * 0.3)
                * gap * funnel;
        vec3 fogColor = mix(vec3(0.54, 0.025, 1.0), vec3(1.0, 0.055, 0.70), smoothstep(0.8, FloorHalf, r));
        ownLight += fogColor * fog * 0.2 * (1.0 - smoothstep(prismReach * 0.6, prismReach, r) * 0.75);

        // Motes thrown out of the mouth, thinning as they travel.
        float motes = apotheosMotes(r, angle) * smoothstep(0.55, 0.8, r) * (1.0 - smoothstep(2.6, FloorHalf + 0.5, r));
        ownLight += mix(vec3(1.0, 0.22, 0.92), vec3(1.0, 0.9, 1.0), motes) * motes * 1.25;

        // The tendrils give off their own pale light, the gold ones most, brightest on the beat.
        ownLight += tendrilTint * artery * tendrilGlow * (0.45 + 0.35 * pulse);

        // The ceiling's black-and-white tendril traces, and its green orbs drifting over the membrane.
        float tendrilPhase = abs(fract((angle / 6.2831853 + 0.5) * 12.0 + fibre * 0.6 + HemoTime * 0.0012) - 0.5);
        float trace = (1.0 - smoothstep(0.015, 0.05, tendrilPhase)) * smoothstep(0.9, 1.6, r) * funnel;
        color = mix(color, vec3(0.01, 0.012, 0.018), trace * 0.3);
        ownLight += vec3(0.95, 0.92, 0.88) * sq(trace) * smoothstep(0.6, 0.95, veinGleam) * 0.25;
        float orbs = apotheosOrbs(p);
        ownLight += vec3(0.72, 1.0, 0.16) * orbs * 0.5;
    }
    if (style == 0) {
        // The scab's plates: dull iron crust flecked with rust and bright grit, raised off the cracks between them.
        float rust = smoothstep(0.55, 0.85, fbm(tissue * 3.3 + 5.0));
        float fleck = smoothstep(0.8, 0.92, noise(tissue * 13.0 + 2.0));
        color = BaseColor * (0.5 + grain * 0.55);
        color = mix(color, vec3(0.2, 0.07, 0.03), rust * 0.5);
        color += vec3(0.2, 0.17, 0.17) * fleck * 0.35;
        color *= 0.5 + 0.5 * smoothstep(0.02, 0.2, vessels.y);
        // The cracks run hot, and each pulse lights the doorway frames as it closes in on the Focus.
        float inward = sq(sq(wave));
        float frames = stroke(thresholdFrameDistance(p, square), 0.035);
        ownLight += GlowColor * sq(artery) * (0.05 + 0.2 * inward + 0.12 * beatLevel);
        ownLight += GlowColor * frames * inward * 0.45;
    }
    if (style == 1) {
        // A thick clot: black lumps under a wet skin that mirrors the sky, shifting as the viewer moves.
        float lumps = smoothstep(0.3, 0.0, cellular(tissue * 1.7 + 13.0).x);
        color = BaseColor * (0.7 + grain * 0.6) * (0.8 + 0.4 * lumps);
        vec2 mirror = tissue * 0.7 + ray.xz / max(-ray.y, 0.15) * 0.6;
        float sheen = sq(smoothstep(0.58, 0.9, fbm(mirror * 1.4 + vec2(HemoTime * 0.002, 0.0))));
        color += vec3(0.5, 0.14, 0.17) * sheen * (0.2 + 0.15 * lumps);
        // A claim sweeps round the seal like a hand round a dial, lighting the rings, glyphs and roots it passes and
        // leaving them to fade behind it; the glyphs also stir on each beat.
        vec2 seal = dominionSeal(r, angle);
        float behind = mod(HemoTime * 0.02 - angle, 6.2831853);
        float sweep = exp(-behind * 1.8) * smoothstep(0.0, 0.05, behind) * smoothstep(0.5, 1.0, r);
        ownLight += GlowColor * (seal.x * 0.5 + seal.y + artery * 0.4) * sweep * 0.5;
        ownLight += GlowColor * seal.y * beatLevel * 0.12;
    }
    if (style == 3) {
        // The lacquer catches the light along each plate's grain, and beads of blood run along the conduits into
        // the bus channels and on to the Focus, glowing at the pads they pass.
        vec2 plate = floor(p + warp * 0.32 + 0.5);
        bool across = mod(plate.x + plate.y, 2.0) < 0.5;
        vec2 mirror = p + warp * 0.32 + ray.xz / max(-ray.y, 0.15) * 0.5;
        float sheen = sq(smoothstep(0.55, 0.95, noise(across ? vec2(mirror.x * 0.8, mirror.y * 6.0)
                : vec2(mirror.x * 6.0, mirror.y * 0.8))));
        color += vec3(0.32, 0.26, 0.38) * sheen * 0.25;
        float bead = pow(0.5 + 0.5 * sin((abs(p.x) + abs(p.y)) * 5.0 + HemoTime * 0.16), 10.0);
        ownLight += GlowColor * (artery + laid * 0.6) * (0.04 + 0.4 * bead + 0.1 * beatLevel);
    }
    if (style == 2) {
        // Engram words glowing up through the weave, kept off the Focus and inside the laid floor: some in a deep
        // reddish orange haloed in faint yellow, some in white.
        vec3 engram = communionEngrams(p + warp * 0.25);
        float keep = smoothstep(0.9, 1.3, r) * (1.0 - smoothstep(FloorHalf + 0.1, FloorHalf + 0.4, square));
        vec3 ink = mix(vec3(0.86, 0.22, 0.04), vec3(0.96, 0.95, 1.0), engram.z);
        vec3 core = mix(vec3(1.0, 0.36, 0.08), vec3(1.0), engram.z);
        vec3 halo = mix(vec3(1.0, 0.82, 0.3) * 0.55, vec3(0.96, 0.95, 1.0), engram.z);
        color = mix(color, ink * 0.6, clamp(engram.x, 0.0, 1.0) * keep * 0.6);
        ownLight += (core * engram.x * 0.75 + halo * engram.y * 0.4) * keep * (0.85 + 0.25 * beatLevel);
    }
    if (style == 6) {
        // Each ancestor's bead breathes in its own slow time, and bright flecks of remembered blood run outward
        // along the lineage lines from child to parent.
        vec3 fan = ancestralFan(p, r);
        float bead = ancestralBead(p, r, fan);
        float recall = 0.5 + 0.5 * sin(HemoTime * 0.04 + hash(vec2(fan.y, fan.z)) * 6.2831853);
        color += GlowColor * bead * (0.18 + 0.5 * sq(recall));
        float lines = line(fan.x, 0.035) * step(0.0, fan.y);
        float fleck = pow(0.5 + 0.5 * sin(r * 4.0 - HemoTime * 0.12 + fan.y * 1.7), 16.0);
        color += GlowColor * lines * fleck * 0.5;
    }
    if (style != 4) color *= 1.0 - (1.0 - vessels.x) * 0.18;
    // The main vessels are sunk into the membrane: blood seen a little below the surface, shifting with the view.
    vec2 sunk = p + ray.xz / max(-ray.y, 0.2) * 0.06;
    float bloodGrain = 0.8 + 0.4 * fbm(sunk * 8.0);
    color = mix(color, VeinColor * (0.8 + pulse * 0.6) * bloodGrain, veins);
    if (style == 4) {
        // The tendrils lie on the membrane rather than in it, shadowing it beside them.
        color *= 1.0 - 0.45 * tendrilShadow * (1.0 - artery);
        color = mix(color, tendrilTint, artery);
    }
    color += GlowColor * (artery * 0.12 + laid * 0.22 + converge * 0.3) * pulse;
    // The Qliphoth floor is not flesh: black monolith glass over the void, its fractal faults running red into the
    // black hole at the Focus, the tree's paths as glowing fissures and its nine husks as small singularities.
    float qliphothGlow = 0.0;
    if (style == 5) {
        float t = HemoTime * 0.05;
        vec4 deep = qliphothVoid(p, r, t);
        vec3 glass = BaseColor * (0.6 + grain * 0.5) + deep.rgb * 0.85;
        vec3 faults = qliphothFaults(p, r, t);
        glass = mix(glass, QLIPHOTH_BLOOD * 0.6, faults.x * 0.35);
        glass = mix(glass, QLIPHOTH_ARTERIAL * 0.7, faults.y * 0.3);
        glass += QLIPHOTH_ARTERIAL * faults.z * 1.2 + QLIPHOTH_HOT * faults.z * faults.z * 0.3;
        // The membrane's vessels survive only as hairline fractures in the glass.
        glass += VeinColor * (artery * 0.35 + capillary * 0.25) * (0.5 + pulse * 0.5);
        // The tree's paths are cracks in the glass, not drawn lines: they wander, and the blood in them runs in
        // separate beads toward the hole.
        vec2 crackWarp = vec2(noise(p * 2.7 + 3.0), noise(p * 2.7 + 9.0)) - 0.5;
        float path = line(qliphothPaths(p + crackWarp * 0.16), 0.025);
        float pathFlow = pow(0.5 + 0.5 * sin((abs(p.x) + abs(p.y)) * 2.4 + t * 4.0), 10.0)
                * (0.5 + 0.5 * noise(p * 1.3 + t * 0.4));
        glass = mix(glass, QLIPHOTH_BLOOD * 0.5, path * 0.45);
        glass += QLIPHOTH_ARTERIAL * path * (0.12 + pathFlow * 1.1) * (0.7 + 0.3 * beatLevel);
        vec4 wells = qliphothHuskWell(p, t, beatLevel);
        glass = mix(glass, wells.rgb, wells.a);
        vec4 hole = qliphothHole(p, r, angle, t, beatLevel);
        color = mix(glass, hole.rgb, hole.a);
        qliphothGlow = deep.w + faults.z * 1.5 + path * 0.6 + wells.a + hole.a;
    }

    // The rite runs through the floor's vessels. The staff's shock and a grounding ring burst outward with the
    // vessels flaring in their wake, an anchor's pulse runs out toward it, an offering's runs in to the Focus, and a
    // live rite keeps them all brighter.
    float shock = RiteRings.y * ringBand(r, RiteRings.x, 0.45);
    float wake = RiteRings.y * smoothstep(RiteRings.x + 0.2, RiteRings.x - 0.6, r) * exp(-max(RiteRings.x - r, 0.0) * 0.3);
    float grounding = RiteRings.w * ringBand(r, RiteRings.z, 0.7);
    float lanes = lanePulse(r, angle, RiteSurge, 1.0) + lanePulse(r, angle, RiteDraw, -1.0);
    float surge = clamp(lanes + shock + wake * 0.5 + grounding, 0.0, 1.5);
    float vesselGain = RiteMood.x * 0.35 + RiteMood.w * 0.5 + RiteSwell.x * 0.45 + surge * 1.1;
    color += VeinColor * veins * bloodGrain * vesselGain * 0.5;
    color += GlowColor * (veins * 0.7 + 0.25) * surge * 0.6;

    // Under the Focus pad the basin holds a dark clot rather than a bright pool.
    color = mix(color, BaseColor * 0.16 + VeinColor * 0.08 * beatLevel, pool * 0.95);
    float focusRim = throat ? 0.0 : exp(-sq((square - 0.5) / 0.09));
    // The Qliphoth hole has its own photon ring, so it keeps no square rim.
    color += GlowColor * focusRim * (0.25 + 0.5 * beatLevel + 0.8 * RiteSurge.w) * (style == 5 || style == 4 ? 0.0 : 1.0);
    // The Apotheos floor's tendrils take the place of the four coloured strands.
    color = mix(color, tributary.rgb * (0.85 + pulse * 0.3), tributary.w * 0.8 * (style == 4 ? 0.0 : 1.0));
    vec2 laidLine = motifLine(p, r, style);
    vec4 arteryGroove = style == 4 ? vec4(0.0) : groove(arteryLine.x, arteryLine.y);
    vec4 laidGroove = groove(laidLine.x, laidLine.y);
    vec4 tributaryCut = tributaryGroove.y > 0.0 ? groove(tributaryGroove.x, tributaryGroove.y) : vec4(0.0);
    vec4 grooveMix = max(max(arteryGroove, laidGroove), tributaryCut);
    color *= 1.0 - 0.45 * grooveMix.y;
    color *= 1.0 - 0.3 * grooveMix.w;
    color += GlowColor * 0.3 * grooveMix.z * (0.6 + 0.4 * pulse);
    // A soft contact shadow ringing the Focus pad.
    color *= 1.0 - (throat ? 0.0 : 0.5) * exp(-sq((r - 0.66) / 0.16));

    // The throat swallowing a planted staff: rings of contraction glow as they run down toward the staff, the flesh
    // streams inward in twisting strands, and a hungry glow builds where the cuff grips, swelling on each beat.
    float throatGlow = 0.0;
    if (throat) {
        float down = smoothstep(0.55, 0.12, r);
        float squeeze = pow(0.5 + 0.5 * sin(r * 30.0 + HemoTime * 0.22 + fbm(p * 5.0) * 2.0), 6.0);
        float strands = line(abs(sin(angle * 3.5 + r * 16.0 + HemoTime * 0.05 + fbm(p * 7.0) * 1.2)) * r,
                0.008 + 0.012 * down);
        float cuffGlow = exp(-sq((r - 0.14) / 0.06)) * (0.45 + 0.55 * beatLevel);
        float wet = pow(noise(p * 14.0 + vec2(HemoTime * 0.03, -HemoTime * 0.05)), 5.0) * down;
        color = mix(color, VeinColor * (0.7 + 0.5 * beatLevel), strands * 0.45);
        color += GlowColor * squeeze * (0.15 + 0.45 * down) * (0.6 + 0.4 * beatLevel);
        color += vec3(0.75, 0.06, 0.08) * cuffGlow * 0.7;
        color += vec3(0.9, 0.55, 0.55) * wet * 0.5;
        throatGlow = squeeze * down + cuffGlow + strands * 0.3;
    }

    // A dried scab lines the wandering edge, and the membrane thins as it spills past the laid floor.
    float scab = smoothstep(-0.32, -0.04, outside);
    color = mix(color, BaseColor * 0.28, scab * 0.75);
    float spill = toTrench || seat ? 0.0 : smoothstep(-0.3, 0.6, square - FloorHalf - 0.5);

    float frontDistance = (r - edge) / 0.35;
    color += GlowColor * Front * exp(-frontDistance * frontDistance) * 1.4;

    if (seat) {
        color *= shade;
    }

    // Moods of the rite over the whole membrane. A flush of relief or completion warms it; culmination swells and
    // brightens it on the beat; a wave's arrival blanches it and its contraction creases it; instability bruises it
    // toward purple-black; a cancellation dims it.
    float flush = RiteMood.w;
    color += VeinColor * flush * 0.18 * (1.0 - veins) + GlowColor * flush * veins * 0.2;
    float swell = RiteSwell.x;
    color *= 1.0 + swell * 0.22;
    color += GlowColor * swell * (0.08 + 0.25 * veins) * (0.6 + 0.4 * beatLevel);
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(color, vec3(luma) * 1.25 + BaseColor * 0.25 + vec3(0.1, 0.08, 0.08), RiteMood.z * 0.55);
    color *= 1.0 - 0.3 * ripple;
    color += GlowColor * 0.08 * ripple;
    float mottle = 0.55 + 0.45 * fbm(p * 1.4 + 31.0);
    vec3 bruised = vec3(0.13, 0.035, 0.11) * (0.6 + 0.7 * grain) + BaseColor * 0.12;
    color = mix(color, bruised, clamp(bruise * mottle * 0.85, 0.0, 0.85));
    color *= 1.0 - RiteSwell.y;
    color = lit(color, veins + pool + tributary.w + surge + swell * 0.5 + throatGlow + qliphothGlow);
    color += ownLight * (1.0 - veins * 0.5) * (1.0 - pool) * (1.0 - scab * 0.8) * (1.0 - bruise * 0.6)
            * (1.0 + swell * 0.4 + flush * 0.3) * (1.0 - RiteSwell.y);
    // Hanging membrane sits in the ledge's shade and thins toward its hem.
    color *= 1.0 - hang * 0.35;
    float alpha = (0.93 + veins * 0.07) * covered * (1.0 - spill * 0.12) * (1.0 - hang * 0.15);
    if (seat) {
        // The seat mound is solid flesh over the brazier's foot; it fades in only at its rim, through vertex alpha.
        alpha = covered;
    }
    alpha *= vertexColor.a * ColorModulator.a;
    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
