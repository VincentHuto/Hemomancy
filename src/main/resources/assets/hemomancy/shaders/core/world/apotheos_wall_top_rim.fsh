#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float RimSeed;
uniform float RimPulseSpeed;
uniform float RimCoreIntensity;
uniform float RimGlowIntensity;

in vec4 vertexColor;
in float rimAngleT;
in float rimWidthT;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + RimSeed * 0.101) * 43758.5453);
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

void main() {
    float angle = rimAngleT * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float time = HemoTime * RimPulseSpeed + RimSeed * 0.017;
    float pulseNoise = noise(vec2(unitCircle.x * 5.8 + time * 0.28,
            unitCircle.y * 5.8 - time * 0.34));
    float seamSafeRimPhase = atan(unitCircle.y, unitCircle.x);
    float breathingRing = 0.82 + sin(seamSafeRimPhase * 8.0 - time * 5.2 + pulseNoise * 1.4) * 0.18;
    float centerDistance = abs(rimWidthT - 0.5) * 2.0;
    float separateRimCore = (1.0 - smoothstep(0.0, 0.48, centerDistance)) * RimCoreIntensity;
    float separateRimHalo = (1.0 - smoothstep(0.34, 1.0, centerDistance)) * RimGlowIntensity;
    float emberFlicker = smoothstep(0.42, 0.98, pulseNoise) * 0.32;

    vec3 rimGlowColor = max(vertexColor.rgb, vec3(0.001));
    vec3 rimHighlightColor = min(rimGlowColor * 1.38, vec3(1.0));
    vec3 rimHeatColor = mix(rimGlowColor, rimHighlightColor, separateRimCore * 0.36 + emberFlicker);
    vec3 color = rimHeatColor;
    color += rimHighlightColor * separateRimCore * 0.22;
    color *= 0.86 + breathingRing * 0.26;

    float alpha = (separateRimCore * 0.62 + separateRimHalo * 0.30 + emberFlicker * 0.18)
            * vertexColor.a * ColorModulator.a;
    alpha *= 0.72 + breathingRing * 0.28;

    if (alpha < 0.004) {
        discard;
    }

    fragColor = vec4(color * ColorModulator.rgb, min(alpha, 0.96));
}
