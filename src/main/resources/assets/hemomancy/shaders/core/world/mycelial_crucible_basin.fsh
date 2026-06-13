#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float BasinSeed;
uniform float SwirlIntensity;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + BasinSeed * 0.013) * 43758.5453);
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
    mat2 turn = mat2(0.78, -0.63, 0.63, 0.78);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.03 + vec2(1.71, -0.46);
        amplitude *= 0.5;
    }
    return total;
}

float ribbon(float value, float width) {
    float distanceToLine = abs(fract(value) - 0.5);
    return smoothstep(width + 0.055, width, distanceToLine);
}

float vortex(vec2 p, vec2 center, float hand, float curlScale, float phase) {
    vec2 d = p - center;
    float radius = length(d);
    float angle = atan(d.y, d.x);
    return angle * hand + radius * curlScale + phase;
}

void main() {
    vec2 uv = texCoord0;
    vec2 p = uv - vec2(0.5);
    p.x *= 1.12;
    float time = HemoTime * 0.042;

    float lowNoise = fbm(p * 3.4 + vec2(time * 0.26, -time * 0.19));
    float highNoise = fbm(p * 8.0 + lowNoise * 1.6 + vec2(-time * 0.31, time * 0.24));
    vec2 warp = vec2(
            sin((p.y + lowNoise) * 7.0 + time * 2.4),
            cos((p.x - highNoise) * 6.2 - time * 2.0)
    ) * 0.075 * SwirlIntensity;
    vec2 q = p + warp;

    float redCurlA = vortex(q, vec2(-0.28, -0.18), 2.8, 13.5, time * 3.4);
    float redCurlB = vortex(q, vec2(0.16, 0.12), -2.2, 16.0, -time * 2.9 + 0.7);
    float greenCurlA = vortex(q, vec2(0.30, -0.24), -2.6, 14.0, -time * 2.6 + 1.1);
    float greenCurlB = vortex(q, vec2(-0.12, 0.25), 2.1, 11.5, time * 2.2);

    float redStream = max(
            ribbon(redCurlA / 6.28318 + highNoise * 0.42, 0.085),
            ribbon((q.y + sin(q.x * 5.4 + time * 2.6) * 0.18 + lowNoise * 0.46) * 2.2, 0.072)
    );
    redStream = max(redStream, ribbon(redCurlB / 6.28318 - lowNoise * 0.34, 0.060) * 0.76);

    float greenStream = max(
            ribbon(greenCurlA / 6.28318 - highNoise * 0.38, 0.078),
            ribbon((q.x + cos(q.y * 5.0 - time * 2.1) * 0.16 - highNoise * 0.32) * 2.0, 0.066)
    );
    greenStream = max(greenStream, ribbon(greenCurlB / 6.28318 + lowNoise * 0.31, 0.055) * 0.68);

    float centerBlend = smoothstep(0.52, 0.10, length(p));
    float darkPocket = smoothstep(0.48, 0.16, abs(redStream - greenStream)) * (1.0 - max(redStream, greenStream) * 0.55);
    float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeFade = smoothstep(0.0, 0.075, edge);
    float merge = redStream * greenStream;

    vec3 blackBase = vec3(0.004, 0.0, 0.003);
    vec3 red = vec3(1.0, 0.015, 0.0) * (0.74 + centerBlend * 0.55);
    vec3 green = vec3(0.02, 0.72, 0.045) * (0.64 + (1.0 - centerBlend) * 0.44);
    vec3 mergeColor = vec3(0.95, 0.18, 0.035);
    vec3 color = blackBase;
    color = mix(color, red, clamp(redStream * 0.92, 0.0, 1.0));
    color = mix(color, green, clamp(greenStream * 0.76, 0.0, 1.0));
    color = mix(color, mergeColor, clamp(merge * 0.58, 0.0, 1.0));
    color *= 1.0 - darkPocket * 0.34;
    color += red * redStream * 0.22 + green * greenStream * 0.17;

    float streamPresence = max(redStream, greenStream);
    float alpha = (0.50 + streamPresence * 0.30 + centerBlend * 0.08 + merge * 0.08) * edgeFade;
    alpha *= vertexColor.a * ColorModulator.a;

    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
