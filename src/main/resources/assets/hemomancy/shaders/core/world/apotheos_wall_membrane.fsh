#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float WallSeed;
uniform float FiberScale;
uniform float TraceIntensity;
uniform float RedGlowIntensity;
uniform float CeilingFadeStart;
uniform float CeilingFadeEnd;

in vec4 vertexColor;
in vec2 texCoord0;
in float wallAngleT;
in float wallHeightT;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + WallSeed * 0.113) * 43758.5453);
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
    float angle = wallAngleT * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float height = clamp(wallHeightT, 0.0, 1.0);
    float time = HemoTime * 0.62 + WallSeed * 0.017;
    float ceilingHandoffFade = 1.0 - smoothstep(CeilingFadeStart, CeilingFadeEnd, height);
    float wallFloorBlendFeather = smoothstep(0.0, 0.13, height);
    float lowWallMask = 1.0 - smoothstep(0.0, 0.46, height);

    vec2 seamSafeFibers = unitCircle * FiberScale + vec2(height * 1.25 - time * 0.045,
            height * 5.6 + time * 0.070);
    float broadFiberNoise = fbm(seamSafeFibers);
    float threadNoise = fbm(vec2(unitCircle.x * FiberScale * 2.7 + height * 4.4,
            unitCircle.y * FiberScale * 2.7 - height * 9.0 + broadFiberNoise * 1.65 + time * 0.11));
    float hairlineNoise = fbm(vec2(unitCircle.x * FiberScale * 5.4 + height * 10.0 - time * 0.10,
            unitCircle.y * FiberScale * 5.4 + height * 24.0 + threadNoise * 2.1));
    float verticalStrands = 1.0 - smoothstep(0.12, 0.45,
            abs(fract(height * 19.0 + broadFiberNoise * 0.58 + unitCircle.x * 0.34) - 0.5));
    float crossFibers = 1.0 - smoothstep(0.018, 0.072,
            abs(fract(wallAngleT * 18.0 + height * 6.0 + hairlineNoise * 0.55) - 0.5));
    float scratchGate = smoothstep(0.64, 0.96, threadNoise + hairlineNoise * 0.34);
    float subtlePaleWebTrace = max(crossFibers, verticalStrands * 0.28) * scratchGate * TraceIntensity
            * (1.0 - smoothstep(0.64, 0.86, height));
    vec3 deepTealBlack = vec3(0.012, 0.040, 0.052);
    vec3 bruisedBlueBlack = vec3(0.030, 0.078, 0.094);
    vec3 wetFiberTeal = vec3(0.055, 0.150, 0.158);
    vec3 readableTealMembrane = vec3(0.026, 0.086, 0.104);
    vec3 lowBloodRed = vec3(0.58, 0.040, 0.038);
    vec3 paleBlueTrace = vec3(0.68, 0.86, 0.96);
    vec3 palePinkTrace = vec3(1.0, 0.58, 0.72);

    float portalFloorGlowBlend = (1.0 - smoothstep(0.12, 0.58, height))
            * (0.55 + wallFloorBlendFeather * 0.45);
    float redLowWallGlow = lowWallMask * portalFloorGlowBlend * RedGlowIntensity
            * (0.82 + broadFiberNoise * 0.34) * (1.0 - smoothstep(0.64, 0.94, height));
    float fiberDepth = smoothstep(0.20, 0.86, broadFiberNoise + threadNoise * 0.42);
    float darkGaps = smoothstep(0.48, 0.92, hairlineNoise) * (1.0 - subtlePaleWebTrace * 0.55);
    float wallReadabilityLift = 0.20 + lowWallMask * 0.10 + smoothstep(0.30, 0.92, threadNoise) * 0.08;
    float visiblePaleTraceBoost = 0.58 + lowWallMask * 0.14 + smoothstep(0.56, 0.94, hairlineNoise) * 0.18;

    vec3 color = mix(deepTealBlack, bruisedBlueBlack, fiberDepth);
    color = mix(color, wetFiberTeal, verticalStrands * 0.16 + threadNoise * 0.10);
    color = mix(color, readableTealMembrane, wallReadabilityLift);
    color = mix(color, deepTealBlack * 0.60, darkGaps * 0.28);
    color += lowBloodRed * redLowWallGlow * 1.18;
    color += mix(paleBlueTrace, palePinkTrace, lowWallMask * 0.34 + broadFiberNoise * 0.18)
            * subtlePaleWebTrace * visiblePaleTraceBoost;
    color *= 0.94 + lowWallMask * 0.18 + threadNoise * 0.18;

    float wallOpacityHeadroom = 0.90 + redLowWallGlow * 0.09 + subtlePaleWebTrace * 0.06
            + smoothstep(0.12, 0.52, 1.0 - height) * 0.04;
    float alpha = (0.66 + fiberDepth * 0.20 + redLowWallGlow * 0.24 + subtlePaleWebTrace * 0.16)
            * wallFloorBlendFeather * ceilingHandoffFade * vertexColor.a * ColorModulator.a;
    alpha *= wallOpacityHeadroom * (0.86 + broadFiberNoise * 0.18 + verticalStrands * 0.08);

    if (alpha < 0.01) {
        discard;
    }

    fragColor = vec4(color * ColorModulator.rgb, min(alpha, 0.92));
}
