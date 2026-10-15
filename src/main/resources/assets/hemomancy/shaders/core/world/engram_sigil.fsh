#version 150

#moj_import <fog.glsl>

// The engram a Somatic Loom unfurls around itself while it weaves: an {8/3} star of empty vessels sunk into a skin of
// living flesh, threaded with tendency-coloured ichor as the weaver draws each fragment home. texCoord0 is the point in
// the engram's own frame, in blocks from the loom (the loom's front toward -y), so the star stays continuous across
// blocks, steps and drapes. One program, three layers chosen by SigilLayer:
//   0  the skin and its empty star (vertex red: 1 on the ground, falling down a drape)
//   1  a lane of ichor (UV0 x along the lane in blocks, y across it -1..1; colour the tendency; alpha > 0.75 home)
//   2  a node's cap (UV0 across it -1..1; colour its tint; alpha carries which cap, in sixteenths)

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float SigilSeed;
uniform float SigilLayer;
uniform float SigilRadius;
// How far the skin has spread out from the loom (0 to 1), and at the climax how far it has drawn back into it.
uniform float Unfurl;
uniform float Withdraw;
uniform float Beat;
// Fungal pressure, 0 to 1 of its limit: rot creeping in from the rim.
uniform float Pressure;
// Keyed faces, and the face Animus is keyed to.
uniform float Keyed;
uniform float Rotation;
// Quarter turns of the loom from north, to carry the world-aligned view ray into the engram's frame.
uniform float Turns;
uniform float Dim;
uniform float FloorLight;
// Where the weaver's staff points, in the engram's frame, and whether it points anywhere (z).
uniform vec3 AimPoint;
// A weave broken before its climax: how far its engram has come apart (0 whole, 1 gone).
uniform float Dissolve;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 viewOffset;

out vec4 fragColor;

const float TAU = 6.2831853;
const float NOTCH_RADIUS = 0.54120;
const float INNER_RADIUS = 0.41421;
// The Vessel's rim, where the spokes enter the loom.
const float RIM_RADIUS = 0.24;

float sq(float x) {
    return x * x;
}

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + SigilSeed * 0.017) * 43758.5453);
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
            vec2 shift = vec2(float(x), float(y));
            vec2 point = shift + 0.15 + 0.7 * hash2(cell + shift);
            float toPoint = length(point - local);
            if (toPoint < nearest) {
                second = nearest;
                nearest = toPoint;
            } else if (toPoint < second) {
                second = toPoint;
            }
        }
    }
    return vec2(nearest, second - nearest);
}

float line(float toLine, float width) {
    return 1.0 - smoothstep(width, width + 0.05, toLine);
}

// A vessel sunk into the skin, from its distance d to the centre line and its half-width w: x how far inside it is,
// y its shaded walls, z a wet highlight down its middle, w a dark lip on the skin around it.
vec4 groove(float d, float w) {
    float inside = 1.0 - smoothstep(w, w + 0.02, d);
    float wall = smoothstep(w * 0.35, w, d) * inside;
    float highlight = exp(-sq(d / max(w * 0.28, 0.004))) * inside;
    float lip = smoothstep(w + 0.08, w + 0.01, d) * (1.0 - inside);
    return vec4(inside, wall, highlight, lip);
}

// A tip of the star, k clockwise from the loom's front, in tip-radius units.
vec2 tip(float k) {
    float a = k * TAU / 8.0;
    return vec2(sin(a), -cos(a));
}

float segment(vec2 p, vec2 a, vec2 b) {
    vec2 ab = b - a;
    float t = clamp(dot(p - a, ab) / dot(ab, ab), 0.0, 1.0);
    return length(p - a - ab * t);
}

// Bearing clockwise from the loom's front, 0 to TAU.
float bearing(vec2 p) {
    float a = atan(p.x, -p.y);
    return a < 0.0 ? a + TAU : a;
}

// The eight tendencies in wheel order, as the vessels carry them.
vec3 tendencyColor(int t) {
    if (t == 0) return vec3(0.86, 0.06, 0.08);
    if (t == 1) return vec3(1.00, 0.40, 0.06);
    if (t == 2) return vec3(1.00, 0.86, 0.16);
    if (t == 3) return vec3(1.00, 0.97, 0.86);
    if (t == 4) return vec3(0.22, 0.62, 0.16);
    if (t == 5) return vec3(0.18, 0.52, 1.00);
    if (t == 6) return vec3(0.58, 0.60, 0.64);
    return vec3(0.52, 0.12, 0.86);
}

// World light dims the skin, but what glows keeps a faint light in the dark.
vec3 lit(vec3 color, float glowing) {
    float light = 0.32 + 0.68 * FloorLight;
    return color * mix(light, max(light, 0.95), clamp(glowing, 0.0, 1.0));
}

// The view ray, turned from world axes into the engram's frame.
vec3 viewRay() {
    vec3 ray = normalize(viewOffset);
    vec2 flat2 = ray.xz;
    for (int i = 0; i < 4; i++) {
        if (float(i) >= Turns) break;
        flat2 = vec2(flat2.y, -flat2.x);
    }
    return vec3(flat2.x, ray.y, flat2.y);
}

vec4 skin() {
    vec2 q = texCoord0;
    float rb = length(q);
    vec2 p = q / SigilRadius;
    float r = length(p);
    float hang = 1.0 - vertexColor.r;

    // The skin creeps out from the loom with a ragged living hem, and at the climax draws back into it.
    float frontNoise = (fbm(q * 0.9 + SigilSeed) - 0.5) * 1.4 + (noise(q * 2.7 + 3.0) - 0.5) * 0.4;
    float reach = (1.14 * SigilRadius + 0.7) * Unfurl * (1.0 - Withdraw);
    float edge = reach + frontNoise;
    float covered = smoothstep(edge + 0.05, edge - 0.3, rb);
    if (covered <= 0.003) discard;
    if (hang > 0.001) {
        float hem = 0.28 + 0.5 * fbm(q * 2.4 + 7.0) + 0.3 * smoothstep(0.72, 0.95, noise(q * 6.0 + 2.0));
        if (hang > hem) discard;
    }

    vec2 warp = vec2(fbm(q * 0.6 + vec2(3.1, 7.7)), fbm(q * 0.6 + vec2(9.4, 1.3))) - 0.5;
    vec2 tissue = q + warp * 1.4 + vec2(sin(HemoTime * 0.011), cos(HemoTime * 0.009)) * 0.07;
    float grain = fbm(tissue * 1.8);
    vec2 vessels = cellular(tissue * 1.1);
    vec2 capillaries = cellular(tissue * 3.0 + 11.0);
    float girth = fbm(tissue * 1.3 + 4.0);
    float artery = line(vessels.y, 0.006 + girth * 0.035);
    float capillary = line(capillaries.y, 0.005 + girth * 0.015) * smoothstep(0.3, 0.62, fbm(tissue * 0.55 + 9.0)) * 0.5;

    // Translucent dark flesh, bruised toward violet, its own fine vessels breathing with the loom.
    vec3 color = vec3(0.12, 0.022, 0.05) * (0.6 + grain * 0.8);
    float veins = clamp(artery + capillary, 0.0, 1.0);
    color = mix(color, vec3(0.42, 0.05, 0.09) * (0.75 + 0.45 * Beat), veins * 0.75);
    color *= 1.0 - (1.0 - vessels.x) * 0.16;

    // The engram itself: empty vessels sunk along the star's eight lines, its rim and its spokes.
    float starDistance = 1.0e3;
    for (int k = 0; k < 8; k++) {
        starDistance = min(starDistance, segment(p, tip(float(k)), tip(float(k + 3))));
    }
    float spokeDistance = 1.0e3;
    for (int k = 0; k < 8; k++) {
        vec2 t = tip(float(k));
        spokeDistance = min(spokeDistance, segment(p, t * INNER_RADIUS, t * RIM_RADIUS));
    }
    float channelDistance = min(min(starDistance, abs(r - 1.0)), spokeDistance) * SigilRadius;
    bool onSpoke = spokeDistance <= min(starDistance, abs(r - 1.0));
    // Seen from above, the channel's floor lies a little deeper than its lip.
    vec3 ray = viewRay();
    vec2 sunk = p + ray.xz / max(-ray.y, 0.25) * 0.012;
    vec4 channel = groove(channelDistance, 0.085);
    // Memory runs in toward the loom along the empty channels: pale pulses, dormant, waiting for ichor.
    float inward = pow(0.5 + 0.5 * sin(length(sunk) * SigilRadius * 2.2 + HemoTime * 0.16 + bearing(p) * 2.0), 6.0);
    // The faces each spoke enters: on a keyed engram the spoke itself carries its tendency's colour out from the loom.
    float spokeFace = mod(floor(bearing(p) / (TAU / 8.0) + 0.5), 8.0);
    vec3 memoryLight = vec3(0.78, 0.36, 0.56);
    if (Keyed > 0.5 && onSpoke) {
        int t = int(mod(spokeFace - Rotation + 8.0, 8.0) + 0.5);
        memoryLight = tendencyColor(t);
    }
    vec3 dormant = vec3(0.03, 0.005, 0.014) + memoryLight * (0.10 + 0.55 * inward) * (0.55 + 0.45 * Beat)
            * (onSpoke && Keyed > 0.5 ? 1.6 : 1.0);
    color = mix(color, dormant, channel.x);
    color *= 1.0 - 0.45 * channel.y;
    color *= 1.0 - 0.38 * channel.w;
    color += vec3(0.85, 0.55, 0.62) * channel.z * 0.14;
    // A faint bioluminescent seam along each channel's lip.
    float seam = exp(-sq((channelDistance - 0.1) / 0.03));
    color += memoryLight * seam * (0.12 + 0.1 * Beat);

    // Twenty-four sockets where the lines cross and end, each a pit with a pale cartilage rim.
    float socketDistance = 1.0e3;
    float innerDistance = 1.0e3;
    float innerFace = 0.0;
    for (int k = 0; k < 8; k++) {
        vec2 t = tip(float(k));
        socketDistance = min(socketDistance, length(q - t * SigilRadius));
        socketDistance = min(socketDistance, length(q - tip(float(k) + 0.5) * NOTCH_RADIUS * SigilRadius));
        float toInner = length(q - t * INNER_RADIUS * SigilRadius);
        if (toInner < innerDistance) {
            innerDistance = toInner;
            innerFace = float(k);
        }
    }
    socketDistance = min(socketDistance, innerDistance);
    float socket = 1.0 - smoothstep(0.2, 0.27, socketDistance);
    float rim = exp(-sq((socketDistance - 0.29) / 0.045));
    // Each socket is a pit with a hungry ember at its bottom, waiting for a thread.
    float ember = exp(-sq(socketDistance / 0.11));
    color = mix(color, vec3(0.025, 0.003, 0.01), socket * 0.85);
    color += vec3(0.75, 0.10, 0.12) * ember * (0.25 + 0.35 * Beat);
    color += vec3(0.62, 0.26, 0.28) * rim * (0.28 + 0.2 * Beat);
    // On a keyed engram each inner socket opens onto its face of the loom: its rim burns in that face's tendency,
    // and the Animus face's rim carries a second, pale ring the wheel is read from.
    float faceRing = 0.0;
    if (Keyed > 0.5) {
        int t = int(mod(innerFace - Rotation + 8.0, 8.0) + 0.5);
        faceRing = exp(-sq((innerDistance - 0.3) / 0.05)) + 0.6 * exp(-sq((innerDistance - 0.39) / 0.035));
        color = mix(color, tendencyColor(t) * (0.8 + 0.4 * Beat), clamp(faceRing, 0.0, 1.0) * 0.9);
        if (abs(innerFace - Rotation) < 0.5) {
            float pale = exp(-sq((innerDistance - 0.5) / 0.035));
            color += vec3(1.0, 0.93, 0.85) * pale * (0.75 + 0.25 * Beat);
            faceRing += pale;
        }
    }

    // The Vessel's rim around the loom's foot: eight faces, keyed to the wheel on a keyed engram, with the Animus face
    // marked so the order reads from one landmark.
    float rimBand = exp(-sq((r - RIM_RADIUS) * SigilRadius / 0.16));
    float a = bearing(p);
    float face = floor(a / (TAU / 8.0) + 0.5);
    float faceIndex = mod(face, 8.0);
    float gap = abs(a - face * TAU / 8.0) * r * SigilRadius;
    float onFace = 1.0 - smoothstep(0.32, 0.42, gap);
    vec3 faceColor = vec3(0.55, 0.42, 0.40);
    float marked = 0.0;
    if (Keyed > 0.5) {
        int t = int(mod(faceIndex - Rotation + 8.0, 8.0) + 0.5);
        faceColor = tendencyColor(t);
        marked = abs(faceIndex - Rotation) < 0.5 ? 1.0 : 0.0;
    }
    color = mix(color, faceColor * (0.5 + 0.35 * Beat + 0.6 * marked), rimBand * onFace * 0.85);
    // Fungal pressure: grey-green rot creeping in from the rim, sprouting pale spore blooms.
    float rot = 0.0;
    if (Pressure > 0.001) {
        float rotFront = SigilRadius * (1.25 - Pressure * 0.7) + (fbm(q * 0.8 + 5.0) - 0.5) * 1.3;
        rot = smoothstep(rotFront - 0.2, rotFront + 0.7, rb);
        vec3 necrotic = vec3(0.09, 0.10, 0.05) * (0.5 + fbm(tissue * 3.0));
        color = mix(color, necrotic, rot * 0.8);
        // Pale spore blooms, small and sparse, in the rot's oldest flesh.
        float spores = smoothstep(0.8, 0.86, noise(tissue * 5.5 + 1.7)) * smoothstep(0.4, 1.0, rot);
        color = mix(color, vec3(0.34, 0.38, 0.22), spores * 0.45);
    }

    // The spreading hem glows as it advances.
    float frontDistance = (rb - edge) / 0.45;
    color += vec3(0.95, 0.24, 0.30) * exp(-sq(frontDistance)) * (1.0 - Unfurl * 0.85) * 1.1;
    // Where the staff points, the skin quickens.
    if (AimPoint.z > 0.5) {
        float aimed = exp(-sq(length(q - AimPoint.xy) / 0.5));
        color += vec3(0.95, 0.60, 0.50) * aimed * (0.10 + 0.25 * channel.x);
    }
    color *= 1.0 - Dim * 0.5;
    color = lit(color, veins * 0.4 + channel.x + seam + rim * 0.6 + ember + rimBand * onFace + faceRing);
    color *= 1.0 - hang * 0.35;
    float alpha = (0.84 + 0.1 * veins + 0.06 * channel.x) * covered * (1.0 - hang * 0.15);
    return vec4(color, alpha);
}

vec4 lane() {
    float along = texCoord0.x;
    float across = texCoord0.y;
    float edgeOff = abs(across);
    if (edgeOff >= 1.0) discard;
    bool home = vertexColor.a > 0.75;
    vec3 ichor = vertexColor.rgb;
    float core = exp(-across * across * 5.0);
    // Peristalsis: boluses of ichor squeezed along toward the loom, brisker once the lane is home.
    float flow = along * 2.4 - HemoTime * (home ? 0.32 : 0.2);
    float bolus = pow(0.5 + 0.5 * sin(flow), 5.0);
    float grainy = 0.82 + 0.35 * noise(vec2(along * 5.0 - HemoTime * 0.05, across * 1.7));
    vec3 color = ichor * (0.3 + 0.95 * core) * grainy;
    color += mix(ichor, vec3(1.0), 0.55) * bolus * core * (home ? 0.8 : 0.5);
    color += mix(ichor, vec3(1.0), 0.7) * exp(-across * across * 30.0) * 0.35;
    // A wet sheen down one flank, and the channel's dark lip at both.
    color += vec3(1.0, 0.92, 0.92) * exp(-sq((across + 0.35) / 0.16)) * 0.16;
    color *= 0.4 + 0.6 * smoothstep(1.0, 0.5, edgeOff);
    color *= home ? 1.0 + 0.4 * Beat : 0.9;
    color *= 1.0 - Dim * 0.4;
    float alpha = smoothstep(1.0, 0.72, edgeOff) * 0.94;
    return vec4(lit(color, 1.0), alpha);
}

vec4 cap() {
    vec2 lp = texCoord0;
    float d = length(lp);
    if (d >= 1.0) discard;
    int kind = int(vertexColor.a * 16.0);
    vec3 tint = vertexColor.rgb;
    float a = atan(lp.y, lp.x);
    float wobble = sin(HemoTime * 0.07 + a * 3.0) * 0.03;
    vec3 color = vec3(0.0);
    float alpha = 0.0;
    float glow = 1.0;
    if (kind <= 2 || kind == 10 || kind == 11) {
        // A recalled fragment: a pool of its ichor welling in the socket. A false one beats between the loom's beats.
        float beat = kind == 2 ? 1.0 - Beat : Beat;
        vec3 ichor = kind == 11 ? vec3(0.78, 0.76, 0.80) + 0.12 * vec3(sin(HemoTime * 0.05), sin(HemoTime * 0.05 + 2.0), sin(HemoTime * 0.05 + 4.0)) : tint;
        float pool = 1.0 - smoothstep(0.42 + wobble, 0.5 + wobble, d);
        float swirl = fbm(vec2(a * 1.6 + HemoTime * 0.02, d * 4.0 - HemoTime * 0.05) + SigilSeed);
        float rimRing = exp(-sq((d - 0.56) / 0.07));
        float halo = exp(-sq(d / 0.75)) * (1.0 - pool);
        float drained = kind == 10 ? 0.45 : 1.0;
        color = ichor * (0.45 + 0.7 * swirl) * pool * drained;
        color += mix(ichor, vec3(1.0), 0.55) * exp(-sq(d / 0.22)) * (0.35 + 0.55 * beat) * pool * drained;
        color += vec3(0.62, 0.30, 0.32) * rimRing * (0.5 + 0.3 * beat);
        color += ichor * halo * (0.25 + 0.35 * beat) * drained;
        alpha = max(max(pool * 0.95, rimRing * 0.8), halo * 0.55);
        if (kind == 1) {
            // The lane in the staff's hold: a slow ring of bright beads circling the socket.
            float beads = pow(0.5 + 0.5 * sin(a * 6.0 - HemoTime * 0.25), 10.0) * exp(-sq((d - 0.78) / 0.07));
            color += mix(ichor, vec3(1.0), 0.6) * beads * 1.2;
            alpha = max(alpha, beads);
        }
    } else if (kind == 3) {
        // A husk: a false fragment the bloodline already knows, dry, grey and split.
        float pod = 1.0 - smoothstep(0.48, 0.55, d);
        float cracks = 1.0 - smoothstep(0.0, 0.05, abs(sin(a * 5.0 + fbm(lp * 3.0) * 3.0)) * d);
        color = vec3(0.32, 0.30, 0.30) * (0.6 + 0.5 * fbm(lp * 6.0)) * pod;
        color = mix(color, vec3(0.06, 0.05, 0.05), cracks * pod * 0.8);
        alpha = pod * 0.85;
        glow = 0.2;
    } else if (kind == 4) {
        // A scar: dead clot crusted over the socket, cracked to the rim, a sickly bruise around it.
        float clot = 1.0 - smoothstep(0.58 + wobble, 0.66 + wobble, d);
        float crust = fbm(lp * 7.0 + SigilSeed);
        float cracks = 1.0 - smoothstep(0.0, 0.06, abs(sin(a * 7.0 + crust * 4.0)) * d);
        float bruise = exp(-sq((d - 0.72) / 0.14));
        color = vec3(0.05, 0.012, 0.02) * (0.5 + crust) * clot;
        color = mix(color, vec3(0.16, 0.14, 0.10), cracks * clot * 0.55);
        color += vec3(0.20, 0.05, 0.16) * bruise * 0.5;
        alpha = max(clot, bruise * 0.45);
        glow = 0.1;
    } else if (kind == 5) {
        // A valve: a sphincter of its tendency's flesh that opens only for its own ichor.
        float folds = 0.5 + 0.5 * sin(a * 9.0 + d * 10.0 - HemoTime * 0.03);
        float ring = smoothstep(0.18, 0.3, d) * (1.0 - smoothstep(0.78, 0.86, d));
        float mouth = 1.0 - smoothstep(0.12, 0.2, d);
        color = tint * (0.35 + 0.55 * folds) * ring;
        color += vec3(0.02, 0.0, 0.01) * mouth;
        color += tint * exp(-sq((d - 0.82) / 0.06)) * (0.5 + 0.4 * Beat);
        alpha = max(ring * 0.95, mouth * 0.9);
    } else if (kind == 6) {
        // A prism: a faceted iris that dyes a grey thread passing through it.
        float facet = floor((a + 3.14159) / (TAU / 6.0));
        float shade = 0.55 + 0.45 * hash(vec2(facet, SigilSeed));
        float body = 1.0 - smoothstep(0.62, 0.7, d);
        float edges = 1.0 - smoothstep(0.0, 0.03, abs(fract((a + 3.14159) / (TAU / 6.0)) - 0.5) * d * 2.0);
        float sparkle = pow(0.5 + 0.5 * sin(HemoTime * 0.2 + facet * 1.7), 12.0);
        color = mix(tint, vec3(1.0), 0.25 * shade) * shade * body;
        color += vec3(1.0) * edges * body * 0.35;
        color += mix(tint, vec3(1.0), 0.6) * sparkle * body * 0.5;
        color += tint * exp(-sq(d / 0.85)) * 0.3;
        alpha = max(body * 0.92, exp(-sq(d / 0.85)) * 0.3);
    } else if (kind == 7 || kind == 8) {
        // The staff's mark: a ring of turning ticks, pale when the step would be taken, red when it would not.
        float ring = exp(-sq((d - 0.86) / 0.05));
        float ticks = step(0.5, fract(a / TAU * 12.0 + HemoTime * (kind == 7 ? 0.01 : -0.03)));
        vec3 markColor = kind == 7 ? vec3(1.0, 0.86, 0.70) : vec3(1.0, 0.12, 0.10);
        color = markColor * ring * (0.6 + 0.4 * ticks);
        alpha = ring * (0.55 + 0.45 * ticks);
    } else if (kind == 9) {
        // A lane's advancing head: a hot swollen bead of its ichor.
        float bead = exp(-sq(d / 0.35));
        color = mix(tint, vec3(1.0), 0.45) * bead * (1.0 + 0.4 * Beat) + tint * exp(-sq(d / 0.8)) * 0.4;
        alpha = max(bead, exp(-sq(d / 0.8)) * 0.5);
    }
    if (alpha <= 0.004) discard;
    color *= 1.0 - Dim * 0.4;
    return vec4(lit(color, glow), clamp(alpha, 0.0, 1.0));
}

// What is left of a fragment as its engram comes apart: below the threshold it is gone, just above it the flesh chars
// dark and a burning lip of ichor-ember eats inward. `n` is where this fragment falls in the order of decay.
vec4 dissolve(vec4 c, float n) {
    if (Dissolve <= 0.0) return c;
    float gone = n - (Dissolve * 1.25 - 0.12);
    if (gone <= 0.0) discard;
    float lip = exp(-sq(gone / 0.018));
    float scorch = 1.0 - smoothstep(0.0, 0.16, gone);
    // Charred black-red flesh just ahead of the edge, and a thin seam of hot ichor along it.
    c.rgb = mix(c.rgb, vec3(0.05, 0.008, 0.012), scorch * 0.85);
    c.rgb = mix(c.rgb, mix(vec3(0.62, 0.05, 0.05), vec3(1.0, 0.5, 0.28), lip * lip), lip * 0.95);
    c.a = max(c.a, lip * 0.9 * clamp(c.a * 3.0, 0.0, 1.0));
    return c;
}

void main() {
    vec4 result;
    if (SigilLayer < 0.5) {
        result = skin();
        // Flesh rots from its rim toward the loom, in ragged patches.
        float rim = clamp(length(texCoord0) / (1.14 * SigilRadius + 0.7), 0.0, 1.0);
        result = dissolve(result, 0.6 * fbm(texCoord0 * 1.1 + 5.0) + 0.42 * (1.0 - rim));
    } else if (SigilLayer < 1.5) {
        result = lane();
        // Threads char through along their length and go before the flesh holding them.
        result = dissolve(result, 0.62 * fbm(vec2(texCoord0.x * 1.6, texCoord0.y * 0.7) + 9.0));
    } else {
        result = cap();
        result = dissolve(result, 0.55 * fbm(texCoord0 * 2.2 + 3.0) + 0.08);
    }
    fragColor = linear_fog(vec4(result.rgb * ColorModulator.rgb, result.a * ColorModulator.a), vertexDistance, FogStart,
            FogEnd, FogColor);
}
