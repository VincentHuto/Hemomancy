#version 150

#moj_import <fog.glsl>

// Veins on the faces of a block being infused, drawn the way the cast outpouring draws them on a body
// (entity/cast_veins, itself the Cardinal floor membrane's vessel web lifted into three dimensions): a warped
// cellular web of arteries and capillaries, each with a dark wall and a hot core that beats outward. The web is fixed
// to the block; it fills in slowly outward from wherever the projection is aimed (VeinOrigin follows the aim), in
// block pixels, wrapping over the block's edges. In the last moments before the block breaks the veins darken; the
// melt shell takes it from there.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float Progress;
uniform float BlockSeed;
uniform float FinalizeProgress;
uniform float CrackShare;
uniform vec3 VeinOrigin;
uniform vec3 VeinNormal;
uniform vec3 VeinAnchor;

in float vertexDistance;
in vec4 vertexColor;
in vec3 surfacePos;

out vec4 fragColor;

vec3 hash3(vec3 p) {
    p = vec3(dot(p, vec3(127.1, 311.7, 74.7)), dot(p, vec3(269.5, 183.3, 246.1)), dot(p, vec3(113.5, 271.9, 124.6)));
    return fract(sin(p) * 43758.5453);
}

float hash1(vec3 p) {
    return fract(sin(dot(p, vec3(12.9898, 78.233, 37.719))) * 43758.5453);
}

float smoothNoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = mix(mix(hash1(i), hash1(i + vec3(1, 0, 0)), f.x), mix(hash1(i + vec3(0, 1, 0)), hash1(i + vec3(1, 1, 0)), f.x), f.y);
    float b = mix(mix(hash1(i + vec3(0, 0, 1)), hash1(i + vec3(1, 0, 1)), f.x), mix(hash1(i + vec3(0, 1, 1)), hash1(i + vec3(1, 1, 1)), f.x), f.y);
    return mix(a, b, f.z);
}

float fbm(vec3 p) {
    return smoothNoise(p) * 0.57 + smoothNoise(p * 2.03 + 7.1) * 0.28 + smoothNoise(p * 4.11 - 3.7) * 0.15;
}

// Distance to the nearest cell point and the gap to the second: thin gaps trace a branching vessel network.
vec2 cellular(vec3 p) {
    vec3 cell = floor(p);
    vec3 local = fract(p);
    float nearest = 8.0;
    float second = 8.0;
    for (int x = -1; x <= 1; x++) for (int y = -1; y <= 1; y++) for (int z = -1; z <= 1; z++) {
        vec3 o = vec3(x, y, z);
        float d = length(o + 0.15 + 0.7 * hash3(cell + o) - local);
        if (d < nearest) {
            second = nearest;
            nearest = d;
        } else if (d < second) {
            second = d;
        }
    }
    return vec2(nearest, second - nearest);
}

float line(float gap, float width) {
    return 1.0 - smoothstep(width, width + 0.06, gap);
}

// Lays a translucent layer over what is already there.
void layer(inout vec3 colour, inout float alpha, vec3 over, float overAlpha) {
    float combined = 1.0 - (1.0 - alpha) * (1.0 - overAlpha);
    colour = (colour * alpha * (1.0 - overAlpha) + over * overAlpha) / max(combined, 0.0001);
    alpha = combined;
}

void main() {
    // Block pixels out from the strike point: across the struck face, then down over its edges, so the web wraps
    // the block instead of tunnelling through it. The web's own shape is laid out from the block's corner, so it
    // stays put on the block while the strike point sweeps across it.
    vec3 n = normalize(VeinNormal);
    vec3 v = surfacePos - VeinOrigin;
    vec3 planar = v - n * dot(v, n);
    vec3 body = (surfacePos - VeinAnchor) * 16.0;
    float d = (length(planar) + abs(dot(v, n))) * 16.0;

    // The web creeps out slowly, with a ragged front; by full progress it has wrapped the whole block.
    vec3 offset3 = vec3(BlockSeed * 731.0, BlockSeed * 419.0, BlockSeed * 257.0);
    float radius = 2.0 + pow(clamp(Progress, 0.0, 1.0), 1.35) * 40.0;
    float front = d + (fbm(body * 0.35 + offset3 * 0.01) - 0.5) * 6.0;
    float growth = 1.0 - smoothstep(radius - 2.0, radius, front);

    // Arteries and capillaries as the cast outpouring draws them: a twice-warped web that swells and pinches along
    // its length and thins toward the edge of its reach, with a dark vessel wall under each line.
    vec3 warp = vec3(fbm(body * 0.07 + offset3 + 3.1), fbm(body * 0.07 + offset3 + 7.7), fbm(body * 0.07 + offset3 + 11.3)) - 0.5;
    vec3 fine = vec3(fbm(body * 0.23 + offset3.zxy), fbm(body * 0.23 + offset3.yzx + 5.0), fbm(body * 0.23 + offset3 + 9.0)) - 0.5;
    vec3 tissue = body * 0.11 + warp * 1.7 + fine * 0.45 + offset3 * 0.1;
    vec2 vessels = cellular(tissue);
    vec2 capillaries = cellular(tissue * 2.6 + 11.0);
    float girth = fbm(tissue * 1.3 + 4.0);

    // The strike is a sore: a lobed, swollen patch that throbs with the heartbeat and grows as the block takes
    // blood. Its shape is laid out from the strike point, so it keeps its shape as the aim drags it across the face.
    // Turned off the block's axes so the noise never lies flat on a lattice plane (that reads as blocky combs).
    vec3 local = mat3(0.80, 0.36, -0.48, -0.60, 0.48, -0.64, 0.0, 0.80, 0.60) * (v * 16.0) + vec3(0.37, 0.61, 0.83);
    float throb = 0.5 + 0.5 * sin(-HemoTime * 0.9);
    float soreRadius = (1.8 + clamp(Progress, 0.0, 1.0) * 2.6) * (1.0 + throb * 0.06);
    // Its edge writhes: the lobes churn through a slowly drifting, self-warping field, and ripples crawl round the
    // rim both ways.
    float writheTime = HemoTime * 0.05;
    vec3 churn = vec3(smoothNoise(local * 0.45 + vec3(writheTime, 0.0, 0.0)),
            smoothNoise(local * 0.45 + vec3(5.2, writheTime, 0.0)),
            smoothNoise(local * 0.45 + vec3(9.4, 0.0, writheTime))) - 0.5;
    vec3 writhing = local + churn * 3.0 + vec3(0.7, 0.5, 0.9) * writheTime * 2.0;
    float lobes = fbm(writhing * 0.32 + offset3 * 0.02) - 0.5;
    vec3 rimTangent = normalize(cross(n, abs(n.y) > 0.9 ? vec3(1.0, 0.0, 0.0) : vec3(0.0, 1.0, 0.0)));
    float rimAngle = atan(dot(planar, cross(n, rimTangent)), dot(planar, rimTangent));
    float crawl = sin(rimAngle * 5.0 + HemoTime * 0.16 + lobes * 4.0) * 0.06
            + sin(rimAngle * 3.0 - HemoTime * 0.11) * 0.05;
    float sore = d / soreRadius + lobes * 0.55 + crawl + (smoothNoise(writhing * 1.1 + 3.0) - 0.5) * 0.12;
    float raw = 1.0 - smoothstep(0.62, 0.8, sore);
    float rim = (1.0 - smoothstep(0.08, 0.2, abs(sore - 0.86))) * (1.0 - raw * 0.6);
    float inflamed = (1.0 - smoothstep(0.85, 2.1, sore)) * (1.0 - raw);
    float bruiseMottle = fbm(local * 0.55 + offset3 * 0.03);

    float thin = clamp(d / max(radius, 1.0), 0.0, 1.0);
    float thinning = mix(1.0, 0.45, thin);
    // Vessels running into the sore swell with the inflammation.
    float arteryWidth = (0.012 + girth * 0.055) * thinning * (1.0 + inflamed * 0.7);
    float artery = line(vessels.y, arteryWidth);
    float capillary = line(capillaries.y, (0.006 + girth * 0.02) * thinning)
            * smoothstep(0.3, 0.62, fbm(tissue * 0.55 + 9.0)) * 0.6 * step(d, radius * 0.75);
    float vein = clamp(artery + capillary, 0.0, 1.0) * growth;
    float wall = (1.0 - smoothstep(0.0, 0.14, vessels.y)) * growth * (1.0 - artery);

    // Blood pushes outward from the strike in beats; the core swells with each and streams outward.
    float beat = 0.5 + 0.5 * sin(d * 0.55 - HemoTime * 0.9);
    float bloodGrain = 0.8 + 0.4 * fbm(tissue * 8.0);
    float flow = 0.7 + 0.3 * smoothNoise(tissue * 5.0 - vec3(0.0, HemoTime * 0.18, 0.0) + d * 0.08);
    float core = (1.0 - smoothstep(0.0, (arteryWidth + 0.06) * (0.42 + 0.12 * beat), vessels.y)) * flow * (1.0 - thin * 0.5);
    core = clamp(max(core, capillary * 0.25), 0.0, 1.0);

    // Just before the block breaks the veins go dark, the blood in them stopping and clotting.
    float finalizing = step(0.0001, FinalizeProgress);
    float strain = clamp(FinalizeProgress / max(CrackShare, 0.001), 0.0, 1.0) * finalizing;
    float darken = smoothstep(0.25, 0.65, strain);
    vec3 outer = mix(vec3(0.44, 0.0, 0.05), vec3(0.07, 0.0, 0.01), darken);
    vec3 inner = mix(vec3(1.0, 0.24, 0.20), vec3(0.2, 0.0, 0.02), darken);
    vec3 vesselColour = mix(outer, inner, core) * bloodGrain;
    // Only the core carries the light; the wall stays dark so the line reads as a tube.
    float glow = mix(0.85, mix(0.8 + 0.2 * beat, 0.55, darken), core);

    // The sore's own colours: a purple-red bruise fading out, a swollen angry rim, and a wet raw middle with the
    // odd glistening speck.
    float settle = 1.0 - darken * 0.7;
    vec3 bruise = mix(vec3(0.36, 0.03, 0.12), vec3(0.55, 0.05, 0.07), bruiseMottle) * settle;
    vec3 swollen = mix(vec3(0.62, 0.05, 0.08), vec3(0.85, 0.16, 0.16), throb * 0.6 + bruiseMottle * 0.4) * settle;
    float rawMottle = fbm(local * 1.6 + offset3 * 0.05 + vec3(0.0, HemoTime * 0.02, 0.0));
    vec3 wound = mix(vec3(0.16, 0.0, 0.02), vec3(0.48, 0.02, 0.05), rawMottle) * (0.85 + 0.25 * throb) * settle;
    float gloss = smoothstep(0.72, 0.8, smoothNoise(local * 2.3 + vec3(HemoTime * 0.01)))
            * (1.0 - smoothstep(0.3, 0.62, sore)) * (1.0 - darken);
    wound = mix(wound, vec3(0.95, 0.42, 0.38), gloss * 0.6);

    // On the blocks around the infused one only the sore shows whole; the veins and bruising bleed a few pixels
    // over the border and fade.
    vec3 fromCentre = abs(surfacePos - (VeinAnchor + vec3(0.5)));
    float outside = length(max(fromCentre - vec3(0.5), vec3(0.0))) * 16.0;
    float spill = 1.0 - smoothstep(2.0, 7.0, outside);

    vec3 rgb = vec3(0.0);
    float alpha = 0.0;
    layer(rgb, alpha, vec3(0.14, 0.0, 0.03), wall * (0.28 + darken * 0.2) * spill);
    layer(rgb, alpha, bruise, inflamed * (0.25 + bruiseMottle * 0.3) * mix(spill, 1.0, 0.5));
    layer(rgb, alpha, vesselColour * glow, vein * 0.95 * (1.0 - thin * 0.35) * spill);
    layer(rgb, alpha, swollen, rim * 0.8);
    layer(rgb, alpha, wound, raw * 0.93);
    // The block breaks at the end of the strain; nothing of these veins outlives it.
    alpha *= (1.0 - smoothstep(0.96, 1.0, strain)) * vertexColor.a * ColorModulator.a;
    if (alpha < 0.01) discard;
    fragColor = linear_fog(vec4(rgb * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
