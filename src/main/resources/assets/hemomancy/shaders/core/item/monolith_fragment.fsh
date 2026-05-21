#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float ShardSeed;
uniform float Burden;
uniform float Attuned;
uniform float FractalScale;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float burdenPulse;
in float attunedPulse;

out vec4 fragColor;

const float FBM_LAYER_DRIFT_RATE = 0.035;
const float FRACTAL_WEB_DRIFT_RATE = 0.11;
const float FRACTAL_BRANCH_DRIFT_RATE = 0.08;
const float VEIN_FLOW_RATE = 1.35;
const float VEIN_PULSE_RATE = 2.4;
const float VEIN_RIPPLE_SCALE = 0.42;
const float VEIN_ADVECT_RATE = 0.32;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 5; i++) {
        v += noise(p) * amp;
        p = mat2(1.62, 1.15, -1.15, 1.62) * p + vec2(ShardSeed * 0.13, HemoTime * FBM_LAYER_DRIFT_RATE);
        amp *= 0.5;
    }
    return v;
}

float staticFbm(vec2 p) {
    float v = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 4; i++) {
        v += noise(p) * amp;
        p = mat2(1.62, 1.15, -1.15, 1.62) * p + vec2(ShardSeed * 0.19, 0.37);
        amp *= 0.5;
    }
    return v;
}

float faultDistance(vec2 uv) {
    vec2 p = (uv - 0.5) * FractalScale;
    float web = fbm(p + vec2(HemoTime * FRACTAL_WEB_DRIFT_RATE, ShardSeed));
    float branch = fbm(p * 2.35 - vec2(ShardSeed, HemoTime * FRACTAL_BRANCH_DRIFT_RATE));
    return abs(web - 0.53) + abs(branch - 0.48) * 0.48;
}

vec2 flowOffset(vec2 p) {
    float pathA = staticFbm(p * 0.46 + vec2(ShardSeed * 2.1, HemoTime * 0.17));
    float pathB = staticFbm(p * 0.54 + vec2(-HemoTime * 0.13, ShardSeed * 2.8));
    vec2 curl = vec2(pathA - 0.5, pathB - 0.5) * (0.86 + Burden * 0.22);
    vec2 current = normalize(vec2(0.72, -0.41)) * HemoTime * VEIN_ADVECT_RATE;
    return current + curl;
}

float movingFaultDistance(vec2 uv) {
    vec2 p = (uv - 0.5) * FractalScale;
    vec2 moved = p + flowOffset(p);
    float web = staticFbm(moved + vec2(ShardSeed * 0.7, 1.3));
    float branch = staticFbm(moved * 2.35 - vec2(ShardSeed * 1.4, 2.1));
    return abs(web - 0.51) + abs(branch - 0.46) * 0.52;
}

float fractalFault(vec2 uv) {
    float distance = faultDistance(uv);
    return smoothstep(0.096 + Burden * 0.024, 0.030, distance);
}

float veinCore(float distance) {
    return smoothstep(0.046 + Burden * 0.008, 0.009, distance);
}

float veinFlow(vec2 uv, float movingCore) {
    vec2 p = (uv - 0.5) * FractalScale;
    float lane = staticFbm(p * 0.82 + vec2(ShardSeed * 1.7, -ShardSeed * 0.9));
    float along = dot(p, normalize(vec2(0.72, -0.41))) + lane * 1.35;
    float train = fract(along * VEIN_RIPPLE_SCALE - HemoTime * VEIN_FLOW_RATE + ShardSeed * 0.37);
    float flowHead = smoothstep(0.035, 0.090, train) * (1.0 - smoothstep(0.145, 0.320, train));
    float flowTrail = smoothstep(0.320, 0.145, train) * 0.34;
    float flowDarkGap = 1.0 - smoothstep(0.420, 0.860, train) * (1.0 - smoothstep(0.900, 0.990, train));
    float pulse = 0.58 + 0.42 * sin(HemoTime * VEIN_PULSE_RATE + ShardSeed * 2.6) * 0.5 + 0.21;
    return movingCore * (0.28 + flowHead + flowTrail) * flowDarkGap * pulse;
}

float triangularEdge(vec2 uv) {
    float left = uv.x;
    float right = 1.0 - uv.x;
    float base = uv.y;
    return smoothstep(0.035, 0.0, min(min(left, right), base));
}

void main() {
    vec2 uv = texCoord0;
    float distance = faultDistance(uv);
    float movingDistance = movingFaultDistance(uv);
    float fault = smoothstep(0.096 + Burden * 0.024, 0.030, distance);
    float core = veinCore(distance);
    float movingCore = smoothstep(0.052 + Burden * 0.010, 0.010, movingDistance);
    float flow = veinFlow(uv, movingCore);
    float edge = triangularEdge(uv);
    float redMemory = fault * (0.07 + burdenPulse * 0.08 + attunedPulse * 0.04)
        + core * (0.10 + burdenPulse * 0.08 + attunedPulse * 0.05)
        + movingCore * 0.18
        + flow * (0.86 + burdenPulse * 0.18 + attunedPulse * 0.16);
    float blackDepth = 0.004 + fault * 0.010 + core * 0.012 + edge * 0.01;

    vec3 black = vec3(blackDepth * 0.38, blackDepth * 0.02, blackDepth * 0.02);
    vec3 red = vec3(0.62 + Burden * 0.30, 0.015 + Attuned * 0.02, 0.010);
    vec3 color = mix(black, red, clamp(redMemory, 0.0, 0.92));
    float veinGlow = clamp(flow * (1.45 + Burden * 0.35 + Attuned * 0.20), 0.0, 1.0);
    color += vec3(0.82 + Burden * 0.20, 0.018, 0.010) * veinGlow;
    color += vec3(0.18, 0.0, 0.0) * edge * (0.35 + Burden * 0.45);
    float alpha = clamp(0.92 + fault * 0.08 + veinGlow * 0.09 + edge * 0.05, 0.0, 1.0);

    vec4 shaded = vec4(color * ColorModulator.rgb, alpha * vertexColor.a * ColorModulator.a);
    fragColor = linear_fog(shaded, vertexDistance, FogStart, FogEnd, FogColor);
}
