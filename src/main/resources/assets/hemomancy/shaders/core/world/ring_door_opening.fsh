#version 150

#moj_import <fog.glsl>

// The ring door opening to held Blood Projection (RingDoorRenderer). Drawn twice over the door: once on the slab's
// face outside the ring well and once on the rings' front inside it (vertex red picks the layer, 1 for the well).
// Everything is laid out in door pixels from the door's own corner, so it stays fixed to the door while the hit point
// (Origin) follows the aim; the spread grows from the hit point and covers the face at full Progress.
//
// Mode 0, a Silentium: the infusion's vein web over a thin wash of blood. Some way behind the front the blood turns
// into the monolith's black stone with red faults and red veins flowing through them, a hot seam where it turns; by
// full progress the whole face has turned.
// Mode 1, an Apotheos: the same vessel web drawn as pale mycelium, growing from a knot at the strike and thinning to
// its finest strands at the front. Behind them thin jagged cracks open through the stone, dark with a white-gold light inside that brightens
// toward the burst.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float Progress;
uniform float DoorSeed;
uniform float Mode;
uniform vec3 Origin;
uniform vec3 Anchor;
uniform vec3 DoorRight;
// x, y the well's centre in door pixels, z its radius, w the spread's full reach from the hit point.
uniform vec4 Well;

in float vertexDistance;
in vec4 vertexColor;
in vec3 surfacePos;

out vec4 fragColor;

#moj_import <hemomancy:noise/vein_hash3.glsl>
#moj_import <hemomancy:noise/vein_hash1.glsl>
#moj_import <hemomancy:noise/vein_noise.glsl>
#moj_import <hemomancy:noise/vein_fbm.glsl>
#moj_import <hemomancy:noise/vein_cellular.glsl>

float line(float gap, float width) {
    return 1.0 - smoothstep(width, width + 0.06, gap);
}

// A hairline: the same gap test with a soft edge in proportion to its width, so a thin crack stays thin.
float fineLine(float gap, float width) {
    return 1.0 - smoothstep(width, width * 1.8 + 0.004, gap);
}

// A thin branching strand: the contour of smooth noise, so it meanders and forks (the heart-node mat's strand).
float strand(vec3 p, float width) {
    return 1.0 - smoothstep(width * 0.7, width, abs(smoothNoise(p) - 0.5));
}

// The monolith's fault field, turned and drifting a little each octave as the monolith shards draw it.
float monolithFbm(vec2 p, float drift) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += smoothNoise(vec3(p, DoorSeed * 3.1 + float(i) * 1.7)) * amp;
        p = mat2(1.62, 1.15, -1.15, 1.62) * p + vec2(DoorSeed * 0.13, drift);
        amp *= 0.5;
    }
    return v;
}

// Lays a translucent layer over what is already there.
void layer(inout vec3 colour, inout float alpha, vec3 over, float overAlpha) {
    float combined = 1.0 - (1.0 - alpha) * (1.0 - overAlpha);
    colour = (colour * alpha * (1.0 - overAlpha) + over * overAlpha) / max(combined, 0.0001);
    alpha = combined;
}

// A camera-relative point as door pixels: across the door from its corner, and up from its foot.
vec2 doorPx(vec3 p) {
    vec3 r = p - Anchor;
    return vec2(dot(r, DoorRight), r.y) * 16.0;
}

void main() {
    vec2 px = doorPx(surfacePos);
    float inWell = step(length(px - Well.xy), Well.z);
    if (abs(vertexColor.r - inWell) > 0.5) discard;

    vec2 rel = px - doorPx(Origin);
    float d = length(rel);
    float p = clamp(Progress, 0.0, 1.0);
    float t = HemoTime;
    vec3 seed3 = vec3(DoorSeed * 731.0, DoorSeed * 419.0, DoorSeed * 257.0);
    vec3 body = vec3(px, 0.0);

    // The front creeps out from the strike with a ragged edge and reaches the far corner at full progress.
    float radius = 3.0 + pow(p, 0.9) * (Well.w + 12.0);
    float front = d + (fbm(body * 0.05 + seed3 * 0.01) - 0.5) * 16.0;
    float growth = 1.0 - smoothstep(radius - 3.0, radius, front);
    if (growth < 0.002) discard;
    float behind = max(radius - front, 0.0);
    float thin = clamp(d / max(radius, 1.0), 0.0, 1.0);

    vec3 rgb = vec3(0.0);
    float alpha = 0.0;
    if (Mode < 0.5) {
        // ---- the blood: the infusion's arteries and capillaries, a dark wall under each, a core that beats outward
        vec3 warp = vec3(fbm(body * 0.07 + seed3 + 3.1), fbm(body * 0.07 + seed3 + 7.7), fbm(body * 0.07 + seed3 + 11.3)) - 0.5;
        vec3 tissue = body * 0.09 + warp * 1.7 + seed3 * 0.1;
        vec2 vessels = cellular(tissue);
        vec2 capillaries = cellular(tissue * 2.6 + 11.0);
        float girth = fbm(tissue * 1.3 + 4.0);
        float thinning = mix(1.0, 0.5, thin);
        float arteryWidth = (0.014 + girth * 0.05) * thinning;
        float artery = line(vessels.y, arteryWidth);
        float capillary = line(capillaries.y, (0.006 + girth * 0.02) * thinning)
                * smoothstep(0.3, 0.62, fbm(tissue * 0.55 + 9.0)) * 0.6;
        float vein = clamp(artery + capillary, 0.0, 1.0) * growth;
        float wall = (1.0 - smoothstep(0.0, 0.14, vessels.y)) * growth * (1.0 - artery);
        float beat = 0.5 + 0.5 * sin(d * 0.35 - t * 0.9);
        float core = 1.0 - smoothstep(0.0, (arteryWidth + 0.06) * (0.42 + 0.12 * beat), vessels.y);
        vec3 vesselColour = mix(vec3(0.44, 0.0, 0.05), vec3(1.0, 0.24, 0.20), core) * (0.8 + 0.4 * fbm(tissue * 8.0));
        float glow = mix(0.85, 0.8 + 0.2 * beat, core);
        // The stone soaks between the veins, most near the strike.
        float wash = growth * (0.4 + 0.3 * (1.0 - thin)) * (0.75 + 0.25 * fbm(body * 0.2 + seed3));
        layer(rgb, alpha, vec3(0.36, 0.01, 0.05), wash);
        layer(rgb, alpha, vec3(0.10, 0.0, 0.02), wall * 0.55);
        layer(rgb, alpha, vesselColour * glow * 1.25, vein);
        // Where the blood goes in: a wet raw patch.
        float sore = d / (3.0 + p * 3.0) + (fbm(body * 0.3 + vec3(0.0, 0.0, t * 0.05)) - 0.5) * 0.6;
        float raw = 1.0 - smoothstep(0.6, 0.85, sore);
        layer(rgb, alpha, mix(vec3(0.16, 0.0, 0.02), vec3(0.48, 0.02, 0.05), fbm(body * 0.8 + seed3)), raw * 0.9);

        // ---- the turn: some way behind the front the blood becomes the monolith, and the whole face by the end
        float turnFront = behind + (fbm(body * 0.11 + seed3 * 0.02 + 5.0) - 0.5) * 10.0;
        float turn = smoothstep(14.0, 34.0, turnFront) * smoothstep(0.05, 0.25, p);
        turn = max(turn, smoothstep(0.9, 1.0, p) * growth);
        if (turn > 0.001) {
            vec2 q = px * 0.045;
            float web = monolithFbm(q + vec2(t * 0.004, 0.0), t * 0.002);
            float branch = monolithFbm(q * 2.35 - vec2(0.0, t * 0.003), -t * 0.002);
            float fault = abs(web - 0.53) + abs(branch - 0.48) * 0.48;
            float faultLine = smoothstep(0.096, 0.03, fault);
            float faultCore = smoothstep(0.046, 0.009, fault);
            // Red runs along the faults in trains, with dark gaps between.
            float along = dot(q, normalize(vec2(0.72, -0.41))) * 6.0 + monolithFbm(q * 0.8, 0.0) * 1.35;
            float train = fract(along * 0.42 - t * 0.06);
            float flowHead = smoothstep(0.035, 0.09, train) * (1.0 - smoothstep(0.145, 0.32, train));
            float flow = faultCore * (0.28 + flowHead + smoothstep(0.32, 0.145, train) * 0.34);
            float red = faultLine * 0.08 + faultCore * 0.12 + flow * 0.86;
            float depth = 0.004 + faultLine * 0.01 + faultCore * 0.012;
            vec3 black = vec3(depth * 0.38 + 0.012, depth * 0.02 + 0.004, depth * 0.02 + 0.006);
            vec3 stone = mix(black, vec3(0.62, 0.015, 0.01), clamp(red, 0.0, 0.92))
                    + vec3(0.82, 0.018, 0.01) * clamp(flow * 1.45, 0.0, 1.0);
            // A hot seam where the blood turns, gone once the whole face has turned.
            float seam = (1.0 - smoothstep(0.0, 0.22, abs(turn - 0.5))) * (1.0 - smoothstep(0.9, 1.0, p));
            vec3 monolith = stone + vec3(0.9, 0.12, 0.08) * seam * 0.6;
            rgb = mix(rgb, monolith, turn);
            alpha = mix(alpha, 0.95, turn);
        }
    } else {
        // ---- the hyphae: the Silentium's vessel web drawn as mycelium, the same warped cellular network, thinner and pale
        vec3 warp = vec3(fbm(body * 0.07 + seed3 + 3.1), fbm(body * 0.07 + seed3 + 7.7), fbm(body * 0.07 + seed3 + 11.3)) - 0.5;
        vec3 tissue = body * 0.09 + warp * 1.7 + seed3 * 0.1;
        vec2 strands = cellular(tissue);
        vec2 fibres = cellular(tissue * 2.6 + 11.0);
        float girth = fbm(tissue * 1.3 + 4.0);
        float density = 1.0 - thin;
        float strandWidth = (0.006 + girth * 0.022) * mix(1.0, 0.55, thin);
        float web = fineLine(strands.y, strandWidth);
        float fine = fineLine(fibres.y, (0.003 + girth * 0.01) * mix(1.0, 0.5, thin))
                * smoothstep(0.3, 0.62, fbm(tissue * 0.55 + 9.0)) * (0.35 + 0.65 * density);
        // At the front the web gives out into its finest strands, thinning to tips.
        float fray = 1.0 - smoothstep(0.0, 12.0, behind);
        float threads = clamp(max(web * (1.0 - fray * 0.6), fine * (1.0 - fray)), 0.0, 1.0) * growth;

        // ---- the cracks: thin jagged lines through the stone, some way behind the front, only some of them open
        vec2 jag = vec2(fbm(vec3(px * 0.06, DoorSeed + 61.0)), fbm(vec3(px * 0.06, DoorSeed + 67.0))) - 0.5;
        vec2 cells = cellular(vec3((px + jag * 14.0) * 0.04, DoorSeed * 5.0));
        float opening = smoothstep(10.0, 40.0, behind) * (0.25 + 0.75 * p);
        float shut = smoothstep(0.35, 0.55, smoothNoise(vec3(px * 0.05, DoorSeed + 51.0)));
        float crackWidth = 0.006 + 0.028 * opening;
        float crack = fineLine(cells.y, crackWidth) * opening * shut * growth;
        float crackCore = fineLine(cells.y, crackWidth * 0.4) * opening * shut * growth;
        // The threads gather along an open crack's lips, prising it apart.
        float lip = fineLine(cells.y, crackWidth * 3.0) * (1.0 - fineLine(cells.y, crackWidth)) * opening * shut * growth;
        float burst = smoothstep(0.82, 1.0, p);
        float flicker = 0.75 + 0.25 * sin(t * 0.7 + d * 0.2);

        vec3 bone = vec3(0.92, 0.89, 0.78);
        vec3 gold = vec3(0.99, 0.86, 0.48);
        layer(rgb, alpha, mix(bone, gold, 0.2 + 0.5 * girth), threads * 0.9);
        layer(rgb, alpha, vec3(0.04, 0.03, 0.02), crack * 0.9);
        layer(rgb, alpha, mix(gold, vec3(1.0, 0.98, 0.9), burst) * (0.7 + 1.3 * burst) * flicker,
                crackCore * (0.45 + 0.55 * burst));
        layer(rgb, alpha, bone, lip * 0.45);
        // The face takes the light just before it bursts.
        layer(rgb, alpha, vec3(1.0, 0.95, 0.75), burst * burst * 0.2 * growth);
        // The strike: a dense knot of hyphae where they came in.
        float knot = 1.0 - smoothstep(2.0 + p * 2.0, 4.0 + p * 3.0, d + (smoothNoise(vec3(px * 0.4, t * 0.03)) - 0.5) * 2.0);
        layer(rgb, alpha, mix(bone, gold, 0.4), knot * 0.95);
    }

    alpha *= vertexColor.a * ColorModulator.a;
    if (alpha < 0.01) discard;
    fragColor = linear_fog(vec4(rgb * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
