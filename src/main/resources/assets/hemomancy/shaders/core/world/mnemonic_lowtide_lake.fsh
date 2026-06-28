#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float LakeSeed;
uniform float NoiseScale;
uniform float GlossStrength;
uniform float EdgeFade;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float waveLiftAmount;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + LakeSeed * 0.019) * 43758.5453);
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
    mat2 turn = mat2(0.82, -0.57, 0.57, 0.82);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.04 + vec2(3.17, -1.41);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 uv = texCoord0;
    float time = HemoTime * 0.045;
    vec2 centered = uv - vec2(0.5);
    vec2 drift = vec2(time * 0.13, -time * 0.09);
    float low = fbm(centered * NoiseScale + drift);
    float high = fbm(centered * (NoiseScale * 2.35) + vec2(-time * 0.21, time * 0.17) + low * 0.85);
    vec2 warped = centered + vec2(
            sin((centered.y + low) * 8.0 + time * 2.4),
            cos((centered.x - high) * 7.0 - time * 2.1)
    ) * 0.035;

    float redStream = smoothstep(0.52, 0.88, fbm(warped * 9.5 + vec2(time * 0.31, -time * 0.22)));
    float blackPocket = smoothstep(0.30, 0.74, fbm(warped * 4.0 - vec2(time * 0.08, time * 0.11)));
    float parchmentHighlight = smoothstep(0.72, 0.96, fbm(warped * 17.0 + vec2(-time * 0.44, time * 0.37)));
    float glossLine = pow(max(0.0, high), 7.0) * GlossStrength;

    vec3 blackBase = vec3(0.006, 0.004, 0.004);
    vec3 red = vec3(0.48, 0.018, 0.012);
    vec3 deepRed = vec3(0.18, 0.006, 0.004);
    vec3 parchment = vec3(0.78, 0.62, 0.39);

    vec3 color = mix(blackBase, deepRed, blackPocket * 0.62);
    color = mix(color, red, redStream * 0.72);
    color += parchment * parchmentHighlight * 0.28;
    color += vec3(1.0, 0.84, 0.58) * glossLine * (0.10 + parchmentHighlight * 0.22);
    color += red * abs(waveLiftAmount) * 0.85;

    float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeFade = smoothstep(0.0, EdgeFade, edge);
    float alpha = (0.72 + redStream * 0.12 + parchmentHighlight * 0.08 + glossLine * 0.08) * edgeFade;
    alpha *= vertexColor.a * ColorModulator.a;

    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
