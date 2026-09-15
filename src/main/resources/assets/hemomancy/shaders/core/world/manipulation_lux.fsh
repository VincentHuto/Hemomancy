#version 150
#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
in float vertexDistance;
in vec4 vertexColor;
in vec2 flowUv;
flat in float flowShape;
flat in float flowSeed;
out vec4 fragColor;

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }
float noise(vec2 p) {
    vec2 i = floor(p), f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1, 0)), f.x),
               mix(hash(i + vec2(0, 1)), hash(i + vec2(1, 1)), f.x), f.y);
}
float flow(vec2 p) {
    return noise(p) * 0.62 + noise(p * 2.03 + 3.7) * 0.26 + noise(p * 4.1 - 1.3) * 0.12;
}

void main() {
    vec2 uv = flowUv, p = uv * 2.0 - 1.0;
    float t = HemoTime * 0.045 + flowSeed * 1.71;
    float n = flow(p * 2.4 + vec2(-t * 0.4, t * 0.13));
    float alpha = 0.0;
    if (flowShape < 0.5 || abs(flowShape - 5.0) < 0.5 || abs(flowShape - 8.0) < 0.5) {
        // A bright filament inside a much wider, transparent stream of light.
        float bend = (n - 0.5) * 0.22 * sin(uv.x * 3.14159);
        float d = abs(p.y - bend);
        float coreWidth = abs(flowShape - 8.0) < 0.5 ? 96.0 : flowShape > 4.5 ? 150.0 : 72.0;
        float core = exp(-d * d * coreWidth);
        float skirt = exp(-d * d * 7.0) * (0.14 + n * 0.24);
        float currents = 0.72 + 0.28 * flow(vec2(uv.x * 13.0 - t * 2.3, p.y * 3.0));
        alpha = (core * 0.96 + skirt) * currents;
        alpha *= smoothstep(0.0, 0.025, uv.x) * (1.0 - smoothstep(0.97, 1.0, uv.x));
    } else if (abs(flowShape - 9.0) < 0.5) {
        // Continuous spherical coordinates: no per-quad edge mask on the light shell.
        float longitude = uv.x * 6.283185;
        vec2 shellFlow = vec2(cos(longitude), sin(longitude)) * sin(uv.y * 3.14159) * 3.0;
        float currents = flow(shellFlow + vec2(uv.y * 2.0, -t));
        alpha = .35 + currents * .65;
    } else if (abs(flowShape - 1.0) < 0.5) {
        float curl = (flow(p * vec2(1.4, 2.1) + vec2(t * 0.17, -t * 0.55)) - 0.5) * 0.7;
        float d = abs(p.x + curl);
        float current = flow(vec2(p.x * 4.0 + curl, p.y * 3.0 - t * 0.8));
        alpha = exp(-d * d * 12.0) * smoothstep(0.26, 0.74, current) * 0.62;
        alpha += exp(-pow((d - 0.13 - current * 0.12) * 22.0, 2.0)) * current * 0.24;
        alpha *= smoothstep(0.0, 0.18, uv.y) * (1.0 - smoothstep(0.66, 1.0, uv.y));
    } else if (abs(flowShape - 3.0) < 0.5) {
        float arc = 0.55 * sin(clamp(uv.x, 0.0, 1.0) * 3.14159);
        float d = abs(abs(p.y) - arc - (n - 0.5) * 0.035);
        alpha = exp(-d * d * 1400.0) * (0.7 + n * 0.3) + exp(-d * d * 95.0) * 0.16;
        alpha *= smoothstep(0.0, 0.1, uv.x) * (1.0 - smoothstep(0.9, 1.0, uv.x));
    } else if (abs(flowShape - 6.0) < 0.5) {
        float r = length(p);
        alpha = exp(-pow((r - 0.74 - (n - 0.5) * 0.04) * 36.0, 2.0)) * 0.48;
    } else {
        vec2 warp = p + (vec2(n, flow(p * 2.0 - t * 0.19 + 7.0)) - 0.5) * 0.3;
        float d = dot(warp, warp);
        float core = exp(-d * (abs(flowShape - 4.0) < 0.5 ? 17.0 : 6.0));
        alpha = core * (0.52 + n * 0.45) + exp(-d * 3.3) * 0.11;
        alpha *= 1.0 - smoothstep(0.56, 1.0, length(p));
    }
    if (abs(flowShape - 9.0) >= 0.5)
        alpha *= smoothstep(0.0, 0.045, min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y)));
    alpha *= vertexColor.a * ColorModulator.a;
    if (alpha < 0.002) discard;
    vec3 color = vertexColor.rgb * ColorModulator.rgb;
    fragColor = linear_fog(vec4(color, min(alpha, 0.98)), vertexDistance, FogStart, FogEnd, FogColor);
}
