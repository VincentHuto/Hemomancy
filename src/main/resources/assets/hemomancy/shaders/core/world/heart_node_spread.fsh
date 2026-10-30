#version 150

// A heart-node sanctum's mycelial spread over the ground inside its envelope (HeartNodeSpreadRenderer). A mat of
// hyphae: branching strands at two scales over a pale felted fill that is near-solid by the heart and thins to a lace
// of strands toward the lobes' edges, where the strands fray into frond tips that reach outward and stop on a ragged
// line. Bone-white and pale grey felt, deep crimson in the main strands, a faint glow in the veins only.
//
// Vertex colour (HeartNodeSpreadGeometry.pack): red the body strength (1 at the heart, 0 at the widest reach), green
// the depth inside the envelope over EDGE_SPAN blocks, blue the signed distance to the nearest thread over
// THREAD_SPAN (0.5 on it), alpha the orientation weight (floors full, walls near the floor, ceilings only tips).
// UV0.x is the pulse's run along that thread.
//
// Lobe0 is the heart and Lobe1..7 the synapses (live in raised order, then receding ones): xyz the camera-relative
// centre, w the growth front in blocks (1024 or more once grown, below 0 for an empty slot). LobeLife falls below 1
// while a withered lobe recedes; LobeArrive is how far the pulse runs from the heart to each lobe (below 0 for none).

uniform float HemoTime;
uniform vec4 Lobe0;
uniform vec4 Lobe1;
uniform vec4 Lobe2;
uniform vec4 Lobe3;
uniform vec4 Lobe4;
uniform vec4 Lobe5;
uniform vec4 Lobe6;
uniform vec4 Lobe7;
uniform vec4 LobeLife0;
uniform vec4 LobeLife1;
uniform vec4 LobeArrive0;
uniform vec4 LobeArrive1;
// The viewer's feet: strands within a couple of blocks stir away from them.
uniform vec3 PlayerOffset;
// x the frond edge, y the threads, z the pulse, w a stranger's view (dimmer, desaturated, no flare).
uniform vec4 SpreadFlags;
// x intensity 0..1, y the owner's seed tile, z ticks between heartbeats, w blocks the pulse runs a tick.
uniform vec4 SpreadLook;

in vec4 vertexColor;
in float pathLength;
in vec3 surfacePos;
in vec3 faceNormal;
in vec4 lightColor;

out vec4 fragColor;

#moj_import <hemomancy:noise/vein_hash1.glsl>
#moj_import <hemomancy:noise/vein_noise.glsl>
#moj_import <hemomancy:noise/value_hash.glsl>
#moj_import <hemomancy:noise/material_noise.glsl>
#moj_import <hemomancy:noise/material_fbm.glsl>

const float EDGE_SPAN = 3.0;
const float THREAD_SPAN = 2.0;
const float HEART_RADIUS = 12.0;
const float SYNAPSE_RADIUS = 8.0;
// The vertical band, with a block of slack: the renderer already found only faces inside the envelope.
const float BAND = 9.5;
const float GROWN = 1000.0;

vec4 lobeAt(int i) {
    if (i == 0) return Lobe0;
    if (i == 1) return Lobe1;
    if (i == 2) return Lobe2;
    if (i == 3) return Lobe3;
    if (i == 4) return Lobe4;
    if (i == 5) return Lobe5;
    if (i == 6) return Lobe6;
    return Lobe7;
}

float lifeAt(int i) {
    return i < 4 ? LobeLife0[i] : LobeLife1[i - 4];
}

float arriveAt(int i) {
    return i < 4 ? LobeArrive0[i] : LobeArrive1[i - 4];
}

// A thin branching strand: the contour of smooth noise, so it meanders and forks.
float strand(vec3 p, float width) {
    return 1.0 - smoothstep(width * 0.7, width, abs(smoothNoise(p) - 0.5));
}

// Short straight fibres lying along the angle a: strands stretched along one axis.
float fibres(vec2 p, float a, float salt) {
    vec2 r = vec2(cos(a) * p.x + sin(a) * p.y, -sin(a) * p.x + cos(a) * p.y);
    return strand(vec3(r.x * 1.6, r.y * 7.5, salt), 0.07);
}

// A soft bump a few ticks wide, where a heartbeat's wave is passing.
float beatBump(float phase, float centre, float width) {
    float x = (phase - centre) / width;
    return exp(-x * x);
}

void main() {
    vec3 n = normalize(faceNormal);
    float floorFace = step(0.5, n.y);
    float ceilingFace = step(0.5, -n.y);

    float strength = vertexColor.r;
    float depth = vertexColor.g * EDGE_SPAN;
    float toThread = (vertexColor.b * 2.0 - 1.0) * THREAD_SPAN;
    float weight = vertexColor.a;
    float intensity = SpreadLook.x;
    float seed = SpreadLook.y;
    float t = HemoTime;

    vec3 rel = surfacePos - Lobe0.xyz;
    float heartDist = length(rel.xz);

    // The plane the mat lies in: the ground on floors and ceilings, the wall itself on walls, stretched upward there
    // so the strands climb.
    vec2 plane = abs(n.y) > 0.5 ? rel.xz : (abs(n.x) > 0.5 ? vec2(rel.z, rel.y * 0.55) : vec2(rel.x, rel.y * 0.55));
    vec3 toFeet = surfacePos - PlayerOffset;
    float press = exp(-dot(toFeet, toFeet) / 2.5) * floorFace;
    vec2 away = toFeet.xz / max(length(toFeet.xz), 0.001);
    plane -= away * press * (0.2 + 0.07 * sin(t * 0.25));
    plane += vec2(seed * 7.31, seed * 3.17);
    // Snapped to the block's 16 pixels, so the mat has the texel density of the blocks it lies on.
    plane = (floor(plane * 16.0) + 0.5) / 16.0;
    // A slow warp, so nothing in the mat lines up with the block grid.
    vec2 warped = plane + (vec2(smoothNoise(vec3(plane * 0.35, seed)), smoothNoise(vec3(plane * 0.35, seed + 17.0)))
            - 0.5) * 0.9;

    // ---- growth: how far each lobe's front has crept, the deepest lobe for the fronds, and whether it is receding
    float grown = 0.0;
    float life = 0.0;
    float rim = 0.0;
    float deepest = -1000.0;
    vec2 outward = vec2(0.0, 1.0);
    float frontWobble = (smoothNoise(vec3(plane * 0.8, seed + 5.0)) - 0.5) * 1.4;
    for (int i = 0; i < 8; i++) {
        vec4 lobe = lobeAt(i);
        if (lobe.w < 0.0) continue;
        float radius = i == 0 ? HEART_RADIUS : SYNAPSE_RADIUS;
        vec3 d = surfacePos - lobe.xyz;
        float dist = length(d.xz);
        if (dist > radius + 1.0 || abs(d.y) > BAND) continue;
        float lobeLife = lifeAt(i);
        float reach = lobe.w - dist + frontWobble;
        grown = max(grown, smoothstep(-0.2, 0.6, reach) * lobeLife);
        life = max(life, lobeLife);
        if (lobe.w < GROWN && lobeLife >= 1.0) rim = max(rim, beatBump(reach, 0.35, 0.5));
        if (radius - dist > deepest) {
            deepest = radius - dist;
            outward = d.xz / max(dist, 0.001);
        }
    }
    if (grown <= 0.002) discard;

    // ---- the mat: felt whose cover rises toward the heart, over strands at two scales
    float heartCore = 1.0 - smoothstep(3.0, 5.0, heartDist);
    float density = clamp(strength * 0.8 + heartCore * 0.6, 0.0, 1.0);
    density = mix(density * 0.55, min(1.0, density * 1.2 + 0.1), intensity);
    float felt = fbm(warped * 1.6 + vec2(0.0, t * 0.0015));
    float threshold = mix(0.9, 0.18, density);
    float feltCover = smoothstep(threshold, threshold + 0.12, felt);
    float width = mix(0.03, 0.07, intensity) * (0.65 + 0.35 * density);
    float big = strand(vec3(warped * 0.5, seed + t * 0.0008), width);
    float fine = strand(vec3(warped * 1.8, seed + 11.0), width * 0.8) * (0.3 + 0.7 * density);
    // Hair: the finest hyphae, a fibrous nap over the felt that thins out with it.
    float hair = max(fibres(warped, 0.6 + seed, seed + 61.0), fibres(warped, 2.2 + seed, seed + 67.0))
            * (0.2 + 0.8 * density);
    feltCover = max(feltCover * (0.7 + 0.3 * hair), hair * 0.55 * density);

    // ---- the edge: the felt gives out and the strands thin into frond tips reaching outward, stopping on a ragged
    // line, so the boundary reads without ever being a circle
    vec2 across = vec2(-outward.y, outward.x);
    float along = dot(rel.xz, outward);
    float side = dot(rel.xz, across);
    float frond = 0.0;
    float edgeCut;
    if (SpreadFlags.x > 0.5) {
        float fray = 1.0 - smoothstep(0.3, 1.5, depth);
        float line = depth + (smoothNoise(vec3(side * 1.3, seed + 31.0, along * 0.2)) - 0.5) * 1.1;
        edgeCut = smoothstep(0.0, 0.25, line);
        frond = strand(vec3(side * 2.6, along * 0.45, seed + 23.0), 0.1) * fray;
        feltCover *= 1.0 - fray;
        big *= mix(1.0, 0.4, fray);
        fine *= 1.0 - fray;
        hair *= 1.0 - fray;
    } else {
        // No frond line: the mat simply thins away before the edge.
        edgeCut = smoothstep(0.0, 2.5, depth);
    }

    // ---- threads: a thicker crimson-cored hypha meandering along the ground from the heart toward each synapse
    float threadCore = 0.0;
    float threadHalo = 0.0;
    if (SpreadFlags.y > 0.5 && floorFace > 0.5 && abs(toThread) < THREAD_SPAN - 0.05) {
        float meander = (smoothNoise(vec3(pathLength * 0.45, seed + 41.0, 0.0)) - 0.5) * 0.55;
        float d = abs(toThread + meander);
        threadCore = 1.0 - smoothstep(0.07, 0.13, d);
        threadHalo = exp(-d * d * 18.0) * 0.4;
    }

    // ---- the pulse: a soft brightening ring rolls out from the heart through the veins, then down each thread to its
    // synapse, which flares
    float ring = 0.0;
    float run = 0.0;
    float flare = 0.0;
    if (SpreadFlags.z > 0.5) {
        float beat = SpreadLook.z;
        float speed = SpreadLook.w;
        ring = beatBump(mod(t - heartDist / speed, beat), 3.0, 2.2);
        if (threadCore + threadHalo > 0.0) run = beatBump(mod(t - pathLength / speed, beat), 3.0, 2.2);
        if (SpreadFlags.w < 0.5) {
            for (int i = 1; i < 8; i++) {
                float arrive = arriveAt(i);
                vec4 lobe = lobeAt(i);
                if (arrive < 0.0 || lobe.w < 0.0) continue;
                vec3 d = surfacePos - lobe.xyz;
                float near = exp(-dot(d, d) / 2.2);
                flare = max(flare, near * beatBump(mod(t - arrive / speed, beat), 4.5, 3.0));
            }
        }
    }

    // ---- orientation: floors carry the mat, walls a ragged band near the floor, low ceilings only hanging tips
    float ragged = smoothNoise(vec3(plane * 2.2, seed + 51.0)) - 0.5;
    float orient = floorFace > 0.5 ? 1.0 : smoothstep(0.05, 0.3, weight + ragged * 0.5);
    float veins = max(big, fine * 0.7);
    float cover = max(feltCover * 0.75, max(veins, frond * 0.85));
    if (ceilingFace > 0.5) cover = veins * 0.9;
    cover = max(cover, threadCore * 0.95);
    cover = max(cover, rim * 0.75);

    // ---- colour: lit felt and dark crimson strands, the fresh rim white, and the veins' glow added over the light
    vec3 lit = lightColor.rgb;
    vec3 feltColour = mix(vec3(0.6, 0.58, 0.56), vec3(0.88, 0.85, 0.8), fbm(warped * 4.3 + 2.0));
    feltColour = mix(feltColour, vec3(0.95, 0.93, 0.9), hair * 0.5);
    vec3 veinColour = vec3(0.42, 0.05, 0.07);
    vec3 glowColour = vec3(0.95, 0.16, 0.14);
    vec3 colour = feltColour * lit;
    colour = mix(colour, veinColour * lit, veins * 0.85);
    colour = mix(colour, vec3(0.92, 0.9, 0.86) * lit, frond * (1.0 - veins));
    colour = mix(colour, vec3(1.0, 0.98, 0.95) * max(lit, vec3(0.55)), rim * 0.85);
    colour = mix(colour, veinColour * lit, threadCore * 0.6);
    float veinGlow = big * (0.14 + 0.55 * ring) + fine * 0.25 * ring;
    colour += glowColour * (veinGlow * 0.5 + threadCore * (0.2 + 0.7 * run) + threadHalo * 0.2 * (0.3 + run)
            + flare * 0.9);

    float alpha = cover * grown * edgeCut * orient * mix(0.4, 1.0, intensity);
    float grey = dot(colour, vec3(0.3, 0.59, 0.11));
    // A receding lobe greys as it goes.
    colour = mix(colour, vec3(grey * 0.85), 1.0 - life);
    if (SpreadFlags.w > 0.5) {
        colour = mix(colour, vec3(grey), 0.7);
        alpha *= 0.5;
    }
    if (alpha < 0.02) discard;
    fragColor = vec4(colour, clamp(alpha, 0.0, 0.95));
}
