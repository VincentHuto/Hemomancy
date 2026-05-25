#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float Progress;
uniform float BlockSeed;
uniform float FinalizeProgress;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float pulse;

out vec4 fragColor;

float mottleNoise(vec2 p) {
    vec2 q = p + vec2(BlockSeed * 3.7, Progress * 0.19);
    float a = sin(q.x * 21.0 + HemoTime * 0.13 + BlockSeed * 8.0);
    float b = sin(q.y * 24.0 - HemoTime * 0.11 + BlockSeed * 5.0);
    float c = sin(q.x * 9.0 + BlockSeed * 17.0) * sin(q.y * 11.0 - HemoTime * 0.08);
    return a * b * 0.42 + c * 0.58;
}

void main() {
    vec2 tileUv = fract(texCoord0);
    vec2 centered = tileUv - vec2(0.5);
    float rim = smoothstep(0.55, 0.18, length(centered));
    float bloodMottle = smoothstep(-0.08, 0.82, mottleNoise(texCoord0));
    float bloodGlow = clamp(0.28 + Progress * 0.56 + pulse * 0.18 + bloodMottle * 0.12, 0.0, 1.0);

    vec3 darkRed = vec3(0.28, 0.0, 0.0);
    vec3 hotRed = vec3(1.0, 0.035, 0.018);
    vec3 color = mix(darkRed, hotRed, bloodGlow) * ColorModulator.rgb;
    float finalizeFade = 1.0 - smoothstep(0.84, 1.0, FinalizeProgress);
    float alpha = clamp((0.20 + Progress * 0.28 + bloodMottle * 0.08 + rim * 0.08) * vertexColor.a * ColorModulator.a,
            0.0, 0.72);
    alpha *= finalizeFade;

    fragColor = linear_fog(vec4(color, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
