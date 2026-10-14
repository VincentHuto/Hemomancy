#version 150

#moj_import <fog.glsl>

// A consecrated Cardinal Rite floor: one living membrane spread over every floor block.
// texCoord0 is the world offset from the Focus centre in blocks, so the surface is continuous across block seams.

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

// The distance to the nearest line of a style's laid pattern, and that line's half-width, for its groove.
vec2 motifLine(vec2 p, float r, int style) {
    if (style == 0) return vec2(min(abs(p.x), abs(p.y)), 0.16);
    if (style == 1) return vec2(min(min(abs(r - 1.45), abs(r - 2.85)), abs(r - 4.25)), 0.05);
    if (style == 3) return vec2(min(abs(p.x), abs(p.y)), 0.09);
    return vec2(10.0, 0.0);
}

// Each style restates its floor's laid pattern in flesh: the iron cross, the dominion seal,
// the communion weave, the working sinew (its bone lamellae are drawn in main).
float motif(vec2 p, float r, int style) {
    if (style == 0) {
        return line(min(abs(p.x), abs(p.y)), 0.16);
    }
    if (style == 1) {
        float rings = line(abs(r - 1.45), 0.05) + line(abs(r - 2.85), 0.05) + line(abs(r - 4.25), 0.05);
        float angle = atan(p.y, p.x);
        float spokes = line(abs(sin(angle * 4.0)) * r, 0.05) * smoothstep(0.6, 1.2, r);
        return clamp(rings + spokes * 0.55, 0.0, 1.0);
    }
    if (style == 2) {
        float weave = sin(p.x * 3.14159) * sin(p.y * 3.14159);
        return smoothstep(0.55, 0.95, abs(weave)) * 0.6;
    }
    return line(min(abs(p.x), abs(p.y)), 0.09);
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
        return vec4(lit(color, capillaries + seam + dig + wet * 0.4), alpha);
    }

    // Follow the view ray down into the trench. The part of the ray running along the trench moves which stretch of
    // it is seen; the part running across it decides whether it reaches the floor or meets a wall.
    vec3 ray = normalize(view);
    float depth = (0.34 + 0.18 * swell) * mix(0.3, 1.0, taper);
    // The world stage fills the trench: the pooled blood rises toward the lip, and the view ray meets its surface
    // before it reaches the walls below. The walls narrow from the lip to the floor (0.45 of the lip), so the pool
    // widens as it rises. Strain darkens and clots it.
    float fill = clamp(RiteBlood.x, 0.0, 1.0);
    if (fill > 0.01) {
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
    float alpha = pad * (0.92 - 0.25 * smoothstep(edge * 0.6, edge, d)) + rootlets * (1.0 - pad) * 0.85;
    return vec4(lit(color, lip + rootlets + flare), alpha);
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
    float boundary = FloorHalf + 0.5 + clamp(bulge, -0.65, 0.9);
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
    float laid = motif(p, r, style) * smoothstep(0.2, -0.4, square - FloorHalf - 0.5);

    // Every vessel ends at the Focus: spokes converge on its rim and its basin holds a pool.
    float converge = line(abs(sin(angle * 3.0 + fbm(p * 0.8) * 2.4)) * r, 0.03 + 0.03 * girth)
            * smoothstep(2.8, 0.9, r) * (throat ? 1.0 : smoothstep(0.45, 0.75, square));
    float pool = throat ? 0.0 : 1.0 - smoothstep(0.38, 0.47, square);
    vec4 tributary = tributaries(p, r, angle);

    // Blood moves outward from the Focus on each heartbeat.
    float wave = 0.5 + 0.5 * sin(r * 2.2 - HemoTime * 0.21);
    float pulse = Beat * exp(-r * 0.18) + wave * 0.18;

    float veins = clamp(artery + capillary + laid * 0.85 + converge, 0.0, 1.0);
    vec3 color = BaseColor * (0.62 + grain * 0.75);
    if (style == 3) {
        // Bone: pale lamellae ringing the Focus like growth lines, with fibrous streaks between them.
        float fibre = fbm(vec2(tissue.x * 0.6, tissue.y * 4.0));
        color = mix(color, BaseColor * 1.45, smoothstep(0.45, 0.75, fibre) * 0.5);
        float lamella = line(abs(fract(r * 1.1 + fbm(tissue * 0.9) * 0.7) - 0.5), 0.05);
        color = mix(color, BaseColor * 1.7, lamella * 0.55);
    }
    color *= 1.0 - (1.0 - vessels.x) * 0.18;
    // The main vessels are sunk into the membrane: blood seen a little below the surface, shifting with the view.
    vec3 ray = normalize(viewOffset);
    vec2 sunk = p + ray.xz / max(-ray.y, 0.2) * 0.06;
    float bloodGrain = 0.8 + 0.4 * fbm(sunk * 8.0);
    color = mix(color, VeinColor * (0.8 + pulse * 0.6) * bloodGrain, veins);
    color += GlowColor * (artery * 0.12 + laid * 0.22 + converge * 0.3) * pulse;

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
    color = mix(color, BaseColor * 0.16 + VeinColor * 0.08 * Beat, pool * 0.95);
    float focusRim = throat ? 0.0 : exp(-sq((square - 0.5) / 0.09));
    color += GlowColor * focusRim * (0.25 + 0.5 * Beat + 0.8 * RiteSurge.w);
    color = mix(color, tributary.rgb * (0.85 + pulse * 0.3), tributary.w * 0.8);
    vec2 laidLine = motifLine(p, r, style);
    vec4 arteryGroove = groove(vessels.y, (0.008 + girth * 0.05) * thinning);
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
        float cuffGlow = exp(-sq((r - 0.14) / 0.06)) * (0.45 + 0.55 * Beat);
        float wet = pow(noise(p * 14.0 + vec2(HemoTime * 0.03, -HemoTime * 0.05)), 5.0) * down;
        color = mix(color, VeinColor * (0.7 + 0.5 * Beat), strands * 0.45);
        color += GlowColor * squeeze * (0.15 + 0.45 * down) * (0.6 + 0.4 * Beat);
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
    color += GlowColor * swell * (0.08 + 0.25 * veins) * (0.6 + 0.4 * Beat);
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    color = mix(color, vec3(luma) * 1.25 + BaseColor * 0.25 + vec3(0.1, 0.08, 0.08), RiteMood.z * 0.55);
    color *= 1.0 - 0.3 * ripple;
    color += GlowColor * 0.08 * ripple;
    float mottle = 0.55 + 0.45 * fbm(p * 1.4 + 31.0);
    vec3 bruised = vec3(0.13, 0.035, 0.11) * (0.6 + 0.7 * grain) + BaseColor * 0.12;
    color = mix(color, bruised, clamp(bruise * mottle * 0.85, 0.0, 0.85));
    color *= 1.0 - RiteSwell.y;
    color = lit(color, veins + pool + tributary.w + surge + swell * 0.5 + throatGlow);
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
