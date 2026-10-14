#version 150

// The sky wound over a deepening Cardinal Rite: a slow vortex of cloud that darkens and grows veins as the rite
// deepens, glows at its core, and at culmination opens into a red eye. texCoord0 runs -1..1 across the disc.

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float WoundSeed;
// x: how far the wound has grown, y: how far its eye has opened, z: a flash, w: strain.
uniform vec4 WoundShape;
uniform vec3 BaseColor;
uniform vec3 VeinColor;
uniform vec3 GlowColor;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + WoundSeed * 0.013) * 43758.5453);
}

float noise(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    vec2 u = local * local * (3.0 - 2.0 * local);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), u.x),
            mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 value) {
    float total = 0.0;
    float amplitude = 0.5;
    mat2 turn = mat2(0.8, -0.6, 0.6, 0.8);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.02 + 1.7;
        amplitude *= 0.5;
    }
    return total;
}

// Distance to the nearest cell point, and to the edge between the two nearest.
vec2 cellular(vec2 value) {
    vec2 cell = floor(value);
    vec2 local = fract(value);
    float nearest = 8.0;
    float second = 8.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 shift = vec2(float(x), float(y));
            vec2 point = shift + 0.15 + 0.7 * vec2(hash(cell + shift), hash(cell + shift + vec2(17.3, 41.9)));
            float gap = length(point - local);
            if (gap < nearest) {
                second = nearest;
                nearest = gap;
            } else if (gap < second) {
                second = gap;
            }
        }
    }
    return vec2(nearest, second - nearest);
}

float line(float d, float width) {
    return 1.0 - smoothstep(width, width + 0.04, d);
}

void main() {
    vec2 p = texCoord0;
    float r = length(p);
    if (r > 1.0) {
        discard;
    }
    float grow = clamp(WoundShape.x, 0.0, 1.0);
    float open = clamp(WoundShape.y, 0.0, 1.0);
    float flash = clamp(WoundShape.z, 0.0, 1.0);
    float strain = clamp(WoundShape.w, 0.0, 1.0);

    // The cloud turns about the centre, faster near it.
    float angle = atan(p.y, p.x);
    float twist = HemoTime * 0.004 + (1.0 - r) * (2.5 + 2.0 * grow);
    vec2 q = vec2(cos(angle + twist), sin(angle + twist)) * r;
    float cloud = fbm(q * 3.0 + WoundSeed);
    float bands = fbm(vec2(angle * 1.5 + twist * 2.0, r * 6.0 - HemoTime * 0.01) + 9.0);
    float density = smoothstep(1.0, 0.55, r) * (0.55 + 0.45 * cloud);

    // Veins thread the cloud as the wound grows.
    vec2 warp = vec2(fbm(q * 2.0 + 3.0), fbm(q * 2.0 + 7.0)) - 0.5;
    float veins = line(cellular(q * 4.0 + warp * 1.5).y, 0.015 + 0.02 * grow) * smoothstep(0.0, 0.6, grow);

    // The core glows; at culmination it opens into an eye with a burning rim. Strain makes the glow stutter.
    float core = exp(-r * r * (18.0 - 10.0 * grow));
    float eyeRadius = 0.08 + 0.32 * open;
    float eye = smoothstep(eyeRadius + 0.04, eyeRadius - 0.04, r) * open;
    float rimDistance = (r - eyeRadius) / 0.05;
    float rim = exp(-rimDistance * rimDistance) * open;
    float stutter = 1.0 - strain * 0.6 * step(0.5, fract(HemoTime * 0.37 + hash(vec2(floor(HemoTime * 0.25), 3.0))));

    vec3 color = mix(BaseColor * 0.35, BaseColor * 0.7 + VeinColor * 0.15, bands) * (0.6 + 0.4 * cloud);
    color = mix(color, VeinColor * 0.9, veins * 0.8);
    color += GlowColor * core * (0.25 + 0.75 * grow) * stutter;
    color = mix(color, VeinColor * 0.25, eye);
    color += GlowColor * rim * (0.8 + 0.4 * sin(HemoTime * 0.2));
    color += GlowColor * flash * (0.4 + 0.6 * exp(-r * 3.0));

    float alpha = density * (0.55 + 0.35 * grow) + veins * 0.4 + core * 0.5 + rim + eye * 0.9 + flash * 0.3;
    alpha = clamp(alpha, 0.0, 1.0) * smoothstep(1.0, 0.85, r) * vertexColor.a;
    // The wound writes depth so clouds sort behind it; empty sky around it must not.
    if (alpha <= 0.004) {
        discard;
    }
    fragColor = vec4(color * ColorModulator.rgb, alpha * ColorModulator.a);
}
