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

void main() {
    vec3 n = normalize(orbNormal + vec3(0.0001));
    float longitude = atan(n.z, n.x);
    float latitude = asin(clamp(n.y, -1.0, 1.0));
    float wrapA = sin(longitude * 5.0 + latitude * 9.0 + HemoTime * 0.045 + OrbSeed);
    float wrapB = sin(longitude * -7.0 + latitude * 6.0 - HemoTime * 0.061 + OrbSeed * 2.4);
    float threadBand = smoothstep(0.58, 0.96, abs(wrapA * 0.62 + wrapB * 0.38 + threadFlow * 0.55));
    float knotShadow = smoothstep(0.70, 0.98,
            abs(sin((n.x * n.z + n.y * 0.33) * ThreadScale * 1.8 + OrbSeed * 4.7)));
    float rim = pow(1.0 - clamp(abs(n.z) * 0.18 + abs(n.y) * 0.34, 0.0, 0.92), 1.6);

    vec3 tendency = vertexColor.rgb * ColorModulator.rgb;
    vec3 darkThread = tendency * 0.18;
    vec3 litThread = min(tendency * (1.08 + surfaceLift * 1.4), vec3(1.0));
    vec3 color = mix(litThread, darkThread, clamp(threadBand * 0.72 + knotShadow * 0.34, 0.0, 1.0));
    color += tendency * rim * (GlowLayer > 0.5 ? 0.28 : 0.08);

    float coreAlpha = vertexColor.a * (0.78 + threadBand * 0.08);
    float glowAlpha = vertexColor.a * (0.18 + rim * 0.14 + threadBand * 0.08);
    float alpha = mix(coreAlpha, glowAlpha, GlowLayer) * ColorModulator.a;

    fragColor = linear_fog(vec4(color, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
