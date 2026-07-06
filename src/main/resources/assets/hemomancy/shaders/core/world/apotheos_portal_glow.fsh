#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float GlowSeed;
uniform float GlowIntensity;
uniform float GlowRadius;
uniform float CenterVoidRadius;

in vec4 vertexColor;
in vec2 texCoord0;
in float radialDistance;
in float centerAperture;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(113.7, 271.9)) + GlowSeed * 0.083) * 19753.193);
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
    float amplitude = 0.54;
    mat2 turn = mat2(0.78, -0.63, 0.63, 0.78);
    for (int i = 0; i < 4; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.05 + vec2(1.17, -0.74);
        amplitude *= 0.50;
    }
    return total;
}

void main() {
    float angle = texCoord0.x * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float pitchBlackCenter = 1.0 - centerAperture;

    float rimStart = CenterVoidRadius + 0.030;
    float rimRise = smoothstep(rimStart, rimStart + 0.070, radialDistance);
    float extendedGreenLayerGlowReach = GlowRadius;
    float softOuterGlowFeather = 1.0 - smoothstep(CenterVoidRadius + extendedGreenLayerGlowReach * 0.42,
            CenterVoidRadius + extendedGreenLayerGlowReach, radialDistance);
    float oneThirdGlowFalloff = softOuterGlowFeather;
    float subtlePortalBackGlow = rimRise * oneThirdGlowFalloff;

    float time = HemoTime * 0.42 + GlowSeed * 0.017;
    float broadPulse = fbm(vec2(unitCircle.x * 1.9 + time * 0.18,
            unitCircle.y * 1.9 + radialDistance * 7.2 - time * 0.26));
    float ringBreath = 0.78 + sin(radialDistance * 36.0 - time * 3.2 + broadPulse * 0.9) * 0.10;
    float ringLuminanceBacklight = subtlePortalBackGlow * (0.82 + broadPulse * 0.55) * ringBreath;

    vec3 redGlow = vec3(1.0, 0.065, 0.075);
    vec3 magentaGlow = vec3(0.98, 0.085, 0.72);
    vec3 violetGlow = vec3(0.52, 0.060, 1.0);
    vec3 glowColor = mix(redGlow, magentaGlow, smoothstep(CenterVoidRadius + 0.06,
            CenterVoidRadius + GlowRadius * 0.62, radialDistance));
    glowColor = mix(glowColor, violetGlow, smoothstep(CenterVoidRadius + GlowRadius * 0.48,
            CenterVoidRadius + GlowRadius, radialDistance) * 0.35);

    float glowOpacityHeadroom = 0.74 + subtlePortalBackGlow * 0.26;
    float behindRingGlowSurvival = 1.0 + oneThirdGlowFalloff * 0.55 + broadPulse * 0.25;
    float alpha = ringLuminanceBacklight * GlowIntensity * glowOpacityHeadroom * behindRingGlowSurvival
            * centerAperture * vertexColor.a * ColorModulator.a;
    alpha *= 1.0 - pitchBlackCenter;
    alpha *= 0.68;

    if (alpha < 0.003) {
        discard;
    }

    fragColor = vec4(glowColor * ColorModulator.rgb, min(alpha, 0.58));
}
