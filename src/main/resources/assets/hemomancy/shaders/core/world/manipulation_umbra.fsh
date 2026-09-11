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
    float value = 0.0, weight = 0.57;
    for (int i = 0; i < 4; i++) {
        value += noise(p) * weight;
        p = mat2(0.8, -0.6, 0.6, 0.8) * p * 2.04 + vec2(1.3, -0.7);
        weight *= 0.5;
    }
    return value;
}

void main() {
    vec2 uv = flowUv, p = uv * 2.0 - 1.0;
    float t = HemoTime * 0.023 + flowSeed * 1.71;
    vec2 warp = vec2(flow(p * 1.8 + t * 0.13), flow(p * 1.8 - t * 0.11 + 5.7)) - 0.5;
    float n = flow(p * 3.0 + warp * 1.2 + vec2(-t * 0.17, t * 0.23));
    float alpha;
    float highlight = 0.0;
    if (abs(flowShape - 2.0) < 0.5) {
        float r = length(p + warp * 0.09);
        // Rotating Cartesian noise is continuous across the angular seam.
        float spin = t * 0.22 + r * 3.2;
        vec2 current = mat2(cos(spin), -sin(spin), sin(spin), cos(spin)) * p;
        float liquid = flow(current * 5.0 + normalize(p + vec2(0.001)) * t * 0.08);
        alpha = (1.0 - smoothstep(0.58 + n * 0.14, 0.94, r)) * (0.76 + liquid * 0.23);
        highlight = smoothstep(0.60, 0.81, liquid) * smoothstep(0.13, 0.58, r);
    } else if (flowShape < 0.5 || abs(flowShape - 7.0) < 0.5) {
        float bend = (n - 0.5) * 0.27 * sin(uv.x * 3.14159);
        float d = abs(p.y - bend);
        float ink = exp(-d * d * 64.0) * 0.88;
        float smoke = exp(-d * d * 4.0) * smoothstep(0.27, 0.7, n) * 0.48;
        alpha = (ink + smoke) * smoothstep(0.0, 0.04, uv.x) * (1.0 - smoothstep(0.90, 1.0, uv.x));
        highlight = exp(-pow((d - 0.1) * 24.0, 2.0)) * n;
    } else {
        float lobes = flow((p + warp * 0.65) * 2.2 - t * 0.09);
        float density = smoothstep(0.30, 0.67, n * 0.62 + lobes * 0.38);
        float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
        alpha = density * smoothstep(0.02, 0.25, edge + (lobes - 0.5) * 0.09) * 0.64;
        highlight = smoothstep(0.5, 0.8, lobes) * 0.15;
    }
    alpha *= smoothstep(0.0, 0.035, min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y)));
    alpha *= vertexColor.a * ColorModulator.a;
    if (alpha < 0.002) discard;
    vec3 color = mix(vec3(0.008, 0.009, 0.014), vec3(0.095, 0.083, 0.11), highlight);
    color *= vertexColor.rgb * ColorModulator.rgb;
    fragColor = linear_fog(vec4(color, min(alpha, 0.94)), vertexDistance, FogStart, FogEnd, FogColor);
}
