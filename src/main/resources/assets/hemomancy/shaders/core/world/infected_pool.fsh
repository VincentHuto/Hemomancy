#version 150

#moj_import <fog.glsl>

// Infected blood spilled on a floor: the blood pool's living surface (convecting, marbled, glossy, simmering) with
// the infection running through it as branching, slightly raised veins of yellow-orange ichor and swollen clots that
// turn with the blood. Spilled, not held: there is no vessel, so the edge is a ragged splash with droplets flung
// past it. texCoord0 spans the drawn square, 0..1; vertex green is the simmer, vertex alpha the pool's opacity.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float PoolSeed;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 viewOffset;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + PoolSeed * 0.013) * 43758.5453);
}

vec2 hash2(vec2 value) {
    return vec2(hash(value), hash(value + vec2(17.3, 41.9)));
}

float noise(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    vec2 curve = local * local * (3.0 - 2.0 * local);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), curve.x),
            mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), curve.x), curve.y);
}

float fbm(vec2 value) {
    float total = 0.0;
    float amplitude = 0.5;
    mat2 turn = mat2(0.78, -0.63, 0.63, 0.78);
    for (int i = 0; i < 4; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.03 + vec2(1.71, -0.46);
        amplitude *= 0.5;
    }
    return total;
}

// The blood turns slowly, the centre swaying a little ahead of the edge.
vec2 convect(vec2 p) {
    float r = length(p);
    float turn = HemoTime * 0.0012 + 0.4 * sin(HemoTime * 0.005) * (1.0 - r);
    mat2 spin = mat2(cos(turn), -sin(turn), sin(turn), cos(turn));
    return spin * p * (1.0 - 0.05 * sin(HemoTime * 0.01 + r * 3.0));
}

// The splash outline: well under 1 (fully inside), near 1 at the ragged edge. Lobes vary with the angle, the edge
// frays with finer noise, and the whole outline breathes very slowly.
float outline(vec2 p) {
    float r = length(p);
    vec2 around = r > 1.0e-4 ? p / r : vec2(1.0, 0.0);
    float lobes = fbm(around * 1.7 + vec2(PoolSeed * 0.07, PoolSeed * 0.031));
    float tongues = fbm(around * 4.1 + vec2(5.2, PoolSeed * 0.043));
    float fray = fbm(p * 6.5 + vec2(PoolSeed * 0.05, 3.1));
    float reach = 0.76 + 0.6 * (lobes - 0.47) + 0.32 * (tongues - 0.47) + 0.14 * (fray - 0.47)
            + 0.012 * sin(HemoTime * 0.02 + lobes * 6.0);
    return r / min(reach, 0.95);
}

// Droplets flung past the edge: small round spots, more of them close to the splash.
float droplets(vec2 p) {
    vec2 grid = p * 7.0;
    vec2 cell = floor(grid);
    float spot = 0.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 id = cell + vec2(float(x), float(y));
            vec2 rnd = hash2(id + 3.7);
            if (rnd.x > 0.42) continue;
            vec2 centre = id + 0.2 + 0.6 * hash2(id + 9.2);
            float size = 0.08 + 0.18 * rnd.y;
            spot = max(spot, smoothstep(size, size * 0.55, length(grid - centre)));
        }
    }
    return spot;
}

// The infection in the blood: x is a vein (on a branching ridge), y the stain bleeding out round it, z a clot.
vec3 infection(vec2 p) {
    vec2 c = convect(p) * 2.3 + vec2(PoolSeed * 0.37, PoolSeed * 0.11);
    float drift = fbm(c + vec2(HemoTime * 0.002, -HemoTime * 0.0015));
    float branching = fbm(c * 1.8 + 7.3 + drift * 1.6);
    float ridge = 1.0 - abs(2.0 * branching - 1.0);
    float vein = smoothstep(0.965, 0.99, ridge);
    float stain = smoothstep(0.88, 0.965, ridge) * (1.0 - vein);
    float clot = smoothstep(0.73, 0.78, fbm(c * 0.85 + 21.0 + drift * 0.4));
    return vec3(vein, stain, clot);
}

// Simmering bubbles, as in the blood pool: domes that swell, hold and pop into a fading ring.
vec2 bubbles(vec2 p, float heat) {
    if (heat <= 0.0) {
        return vec2(0.0);
    }
    vec2 grid = p * 3.2;
    vec2 cell = floor(grid);
    float height = 0.0;
    float ring = 0.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 id = cell + vec2(float(x), float(y));
            vec2 rnd = hash2(id);
            if (hash(id + 5.3) > heat * (1.15 - 0.6 * length(id / 3.2))) continue;
            float period = 70.0 + rnd.x * 90.0;
            float age = fract(HemoTime / period + rnd.y);
            vec2 centre = id + 0.2 + 0.6 * rnd;
            float d = length(grid - centre);
            float size = 0.1 + 0.12 * hash(id + 9.1);
            if (age < 0.82) {
                float radius = size * smoothstep(0.0, 0.7, age);
                height = max(height, sqrt(max(radius * radius - d * d, 0.0)) * 2.2);
            } else {
                float spread = (age - 0.82) / 0.18;
                float edge = size * (1.0 + spread * 1.6);
                ring = max(ring, exp(-pow((d - edge) / 0.035, 2.0)) * (1.0 - spread));
            }
        }
    }
    return vec2(height, ring);
}

float relief(vec2 p, float heat) {
    vec2 c = convect(p);
    float swell = fbm(c * 2.2 + vec2(HemoTime * 0.004, 0.0)) * 0.35;
    vec2 b = bubbles(p, heat);
    vec3 inf = infection(p);
    // The infected veins and clots stand a little proud of the blood, so they catch the light.
    return swell + b.x * 0.5 + b.y * 0.06 + inf.x * 0.07 + inf.z * 0.09;
}

void main() {
    vec2 p = texCoord0 * 2.0 - 1.0;
    float shape = outline(p);
    float drop = shape > 1.0 && length(p) < 0.97 ? droplets(p) * smoothstep(1.45, 1.05, shape) : 0.0;
    if (shape > 1.0 && drop < 0.5) {
        discard;
    }
    float r = min(shape, 1.0);
    float heat = vertexColor.g;

    float e = 0.02;
    float h = relief(p, heat);
    vec3 normal = normalize(vec3((h - relief(p + vec2(e, 0.0), heat)) * 0.35, e,
            (h - relief(p + vec2(0.0, e), heat)) * 0.35));
    vec2 b = bubbles(p, heat);

    // Thick blood, marbled darker and brighter as it turns over.
    vec2 c = convect(p);
    float marble = fbm(c * 3.0 + 4.0);
    vec3 color = mix(vec3(0.16, 0.006, 0.018), vec3(0.40, 0.018, 0.04), smoothstep(0.3, 0.75, marble));
    color += vec3(0.55, 0.08, 0.05) * (b.x * 1.5 + b.y * 0.6) * heat;

    // The infection: ichor stains the blood round each vein; veins and clots are the ichor itself, pulsing slowly.
    vec3 inf = infection(p);
    float pulse = 0.82 + 0.18 * sin(HemoTime * 0.07 - length(p) * 5.0 + marble * 6.0);
    vec3 ichor = mix(vec3(0.5, 0.26, 0.05), vec3(0.95, 0.64, 0.2), smoothstep(0.35, 0.8, marble)) * pulse;
    color = mix(color, color * 0.6 + vec3(0.17, 0.06, 0.008), inf.y * 0.55);
    color = mix(color, ichor, max(inf.x * 0.92, inf.z * 0.8));

    // Glossy and wet: it mirrors the dim room at a glancing view and catches a highlight from above.
    vec3 ray = normalize(viewOffset);
    vec3 bounce = reflect(ray, normal);
    float facing = clamp(dot(-ray, normal), 0.0, 1.0);
    float fresnel = 0.04 + 0.96 * pow(1.0 - facing, 5.0);
    vec3 surroundings = mix(vec3(0.05, 0.012, 0.02), vec3(0.30, 0.20, 0.23), smoothstep(0.3, 0.95, bounce.y));
    color = mix(color, surroundings, fresnel * 0.55);
    float sky = pow(max(dot(bounce, normalize(vec3(0.3, 1.0, 0.25))), 0.0), 40.0);
    color += vec3(0.7, 0.5, 0.52) * sky * 0.4;

    // Thinner at the splash's edge: a touch darker where it soaks the floor, and fading out.
    color *= mix(1.0, 0.7, smoothstep(0.75, 1.0, r));
    float body = drop > 0.0 ? drop : smoothstep(1.0, 0.965, shape);
    float alpha = body * vertexColor.a * ColorModulator.a;
    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
