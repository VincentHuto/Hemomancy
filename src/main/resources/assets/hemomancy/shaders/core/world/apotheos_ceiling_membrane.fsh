#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float CeilSeed;
uniform float FiberScale;
uniform float TraceIntensity;
uniform float RedGlowIntensity;
uniform float MassDescent;
uniform float RimFadeStart;
uniform float RimFadeEnd;

in vec4 vertexColor;
in vec2 texCoord0;
in float ceilAngleT;
in float ceilRadialT;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + CeilSeed * 0.113) * 43758.5453);
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
    float amplitude = 0.55;
    mat2 turn = mat2(0.82, -0.57, 0.57, 0.82);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.04 + vec2(1.31, -0.87);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    float angle = ceilAngleT * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float radial = clamp(ceilRadialT, 0.0, 1.0);
    float time = HemoTime * 0.58 + CeilSeed * 0.015;

    // The descending overhead mass owns the low-radial center; the canopy membrane
    // spans outward and fades where it hands the top of the chamber back to the wall.
    float descendingMassMask = 1.0 - smoothstep(0.10, 0.30, radial);
    float rimHandoffFade = 1.0 - smoothstep(RimFadeStart, RimFadeEnd, radial);
    float centerReach = 1.0 - smoothstep(0.0, 0.62, radial);

    vec2 seamSafeFibers = unitCircle * FiberScale + vec2(radial * 2.4 - time * 0.045,
            radial * 6.0 + time * 0.070);
    float broadFiberNoise = fbm(seamSafeFibers);
    float threadNoise = fbm(vec2(unitCircle.x * FiberScale * 2.6 + radial * 4.2,
            unitCircle.y * FiberScale * 2.6 - radial * 8.4 + broadFiberNoise * 1.6 + time * 0.10));
    float hairlineNoise = fbm(vec2(unitCircle.x * FiberScale * 5.2 - time * 0.09,
            unitCircle.y * FiberScale * 5.2 + radial * 22.0 + threadNoise * 2.0));
    float radialStrands = 1.0 - smoothstep(0.12, 0.45,
            abs(fract(ceilAngleT * 22.0 + broadFiberNoise * 0.50) - 0.5));
    float crossFibers = 1.0 - smoothstep(0.020, 0.075,
            abs(fract(radial * 16.0 + hairlineNoise * 0.55) - 0.5));
    float scratchGate = smoothstep(0.62, 0.95, threadNoise + hairlineNoise * 0.33);
    float subtlePaleWebTrace = max(crossFibers, radialStrands * 0.30) * scratchGate * TraceIntensity
            * (1.0 - descendingMassMask * 0.40);

    vec3 deepTealBlack = vec3(0.010, 0.036, 0.048);
    vec3 bruisedBlueBlack = vec3(0.028, 0.072, 0.090);
    vec3 wetFiberTeal = vec3(0.050, 0.140, 0.150);
    vec3 readableTealMembrane = vec3(0.024, 0.082, 0.100);
    vec3 lowBloodRed = vec3(0.60, 0.045, 0.040);
    vec3 hotCoreRed = vec3(0.95, 0.160, 0.180);
    vec3 paleBlueTrace = vec3(0.68, 0.86, 0.96);
    vec3 palePinkTrace = vec3(1.0, 0.58, 0.72);

    float ceilFiberDepth = smoothstep(0.20, 0.85, broadFiberNoise + threadNoise * 0.42);
    float darkGaps = smoothstep(0.48, 0.92, hairlineNoise) * (1.0 - subtlePaleWebTrace * 0.50);
    float overheadReadabilityLift = 0.16 + centerReach * 0.10 + smoothstep(0.30, 0.90, threadNoise) * 0.08;
    float visiblePaleTraceBoost = 0.60 + centerReach * 0.14 + smoothstep(0.56, 0.94, hairlineNoise) * 0.18;

    // Red glow concentrates in the hanging mass, hottest at its descending tip.
    float pulse = 0.82 + 0.18 * sin(time * 1.9 + broadFiberNoise * 3.1);
    float massCoreGlow = descendingMassMask * RedGlowIntensity * MassDescent * pulse
            * (0.70 + broadFiberNoise * 0.40);

    vec3 color = mix(deepTealBlack, bruisedBlueBlack, ceilFiberDepth);
    color = mix(color, wetFiberTeal, radialStrands * 0.14 + threadNoise * 0.10);
    color = mix(color, readableTealMembrane, overheadReadabilityLift);
    color = mix(color, deepTealBlack * 0.60, darkGaps * 0.26);
    color += lowBloodRed * massCoreGlow * 1.10;
    color += hotCoreRed * pow(descendingMassMask, 2.2) * MassDescent * pulse
            * (0.50 + broadFiberNoise * 0.30);
    color += mix(paleBlueTrace, palePinkTrace, descendingMassMask * 0.40 + broadFiberNoise * 0.18)
            * subtlePaleWebTrace * visiblePaleTraceBoost;
    color *= 0.92 + centerReach * 0.16 + threadNoise * 0.16;

    float ceilOpacityHeadroom = 0.90 + massCoreGlow * 0.09 + subtlePaleWebTrace * 0.06;
    float alpha = (0.62 + ceilFiberDepth * 0.22 + massCoreGlow * 0.24 + subtlePaleWebTrace * 0.16)
            * rimHandoffFade * vertexColor.a * ColorModulator.a;
    alpha *= ceilOpacityHeadroom * (0.88 + broadFiberNoise * 0.16 + descendingMassMask * 0.12);

    if (alpha < 0.01) {
        discard;
    }

    fragColor = vec4(color * ColorModulator.rgb, min(alpha, 0.95));
}
