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
uniform float FillLine;
uniform float InfusionTiming;
uniform float CrackShare;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float pulse;
in float fillHeight;

out vec4 fragColor;

float mottleNoise(vec2 p) {
    vec2 q = p + vec2(BlockSeed * 3.7, Progress * 0.19);
    float a = sin(q.x * 21.0 + HemoTime * 0.13 + BlockSeed * 8.0);
    float b = sin(q.y * 24.0 - HemoTime * 0.11 + BlockSeed * 5.0);
    float c = sin(q.x * 9.0 + BlockSeed * 17.0) * sin(q.y * 11.0 - HemoTime * 0.08);
    return a * b * 0.42 + c * 0.58;
}

void main() {
    // A Working floor climbs the structure up to a ragged, wet line; nothing shows above it.
    float ragged = 0.05 * sin(texCoord0.x * 13.0 + HemoTime * 0.21 + BlockSeed * 5.0)
            + 0.03 * sin(texCoord0.x * 31.0 - HemoTime * 0.13);
    float edge = FillLine + ragged;
    if (fillHeight > edge) discard;
    float meniscus = 1.0 - smoothstep(0.0, 0.14, edge - fillHeight);
    vec2 tileUv = fract(texCoord0);
    vec2 centered = tileUv - vec2(0.5);
    float rim = smoothstep(0.55, 0.18, length(centered));
    float bloodMottle = smoothstep(-0.08, 0.82, mottleNoise(texCoord0));
    float bloodGlow = clamp(0.28 + Progress * 0.56 + pulse * 0.18 + bloodMottle * 0.12, 0.0, 1.0);

    vec3 darkRed = vec3(0.28, 0.0, 0.0);
    vec3 hotRed = vec3(1.0, 0.035, 0.018);
    vec3 color = mix(darkRed, hotRed, bloodGlow);
    float useVertexTint = 1.0 - step(0.999, min(vertexColor.r, min(vertexColor.g, vertexColor.b)));
    vec3 tintedColor = vertexColor.rgb * mix(0.52, 1.08, bloodGlow);
    color = mix(color, tintedColor, useVertexTint) * ColorModulator.rgb;
    color = mix(color, vec3(1.0, 0.82, 0.77), meniscus * 0.55);
    float finalizeFade = 1.0 - smoothstep(0.84, 1.0, FinalizeProgress);
    float alpha = clamp((0.20 + Progress * 0.28 + bloodMottle * 0.08 + rim * 0.08) * vertexColor.a * ColorModulator.a,
            0.0, 0.72);
    // An infusion's shell stays away while its veins spread over the block, swallows the darkened block just
    // before it breaks, then melts after it.
    float swallow = smoothstep(CrackShare * 0.82, CrackShare, FinalizeProgress);
    float infusionMelt = clamp((FinalizeProgress - CrackShare) / max(1.0 - CrackShare, 0.001), 0.0, 1.0);
    float infusionFade = swallow * (1.0 - smoothstep(0.84, 1.0, infusionMelt));
    alpha *= mix(finalizeFade, infusionFade, InfusionTiming);
    alpha = max(alpha, meniscus * 0.62 * vertexColor.a * ColorModulator.a);

    fragColor = linear_fog(vec4(color, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
