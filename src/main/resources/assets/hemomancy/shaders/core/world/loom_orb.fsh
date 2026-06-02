#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float OrbSeed;
uniform float ThreadScale;
uniform float GlowLayer;

in float vertexDistance;
in vec4 vertexColor;
in vec3 orbNormal;
in float threadFlow;
in float surfaceLift;

out vec4 fragColor;

float lineBand(float value, float threadWidth) {
    float distanceToLine = abs(fract(value) - 0.5);
    return smoothstep(threadWidth + 0.035, threadWidth, distanceToLine);
}

void main() {
    vec3 n = normalize(orbNormal + vec3(0.0001));
    float longitude = atan(n.z, n.x);
    float latitude = asin(clamp(n.y, -1.0, 1.0));
    float drift = HemoTime * 0.010 + OrbSeed * 0.017;
    float wrapA = longitude * 0.82 + latitude * 1.96 + threadFlow * 0.22 + drift;
    float wrapB = longitude * -1.35 + latitude * 1.28 - threadFlow * 0.18 - drift * 0.72;
    float wrapC = longitude * 1.90 - latitude * 0.54 + surfaceLift * 0.34 + OrbSeed * 0.031;
    float threadWidth = 0.030 + GlowLayer * 0.014;
    float threadBand = max(lineBand(wrapA, threadWidth),
            max(lineBand(wrapB, threadWidth * 0.82) * 0.82,
                    lineBand(wrapC, threadWidth * 0.68) * 0.56));
    float rim = pow(1.0 - clamp(abs(n.z) * 0.18 + abs(n.y) * 0.34, 0.0, 0.92), 1.6);

    vec3 tendency = vertexColor.rgb * ColorModulator.rgb;
    vec3 darkThread = tendency * 0.18;
    vec3 litThread = min(tendency * (1.08 + surfaceLift * 1.4), vec3(1.0));
    vec3 color = mix(litThread, darkThread, clamp(threadBand * 0.86, 0.0, 1.0));
    color += tendency * rim * (GlowLayer > 0.5 ? 0.28 : 0.08);

    float coreAlpha = vertexColor.a * (0.78 + threadBand * 0.08);
    float glowAlpha = vertexColor.a * (0.18 + rim * 0.14 + threadBand * 0.08);
    float alpha = mix(coreAlpha, glowAlpha, GlowLayer) * ColorModulator.a;

    fragColor = linear_fog(vec4(color, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
