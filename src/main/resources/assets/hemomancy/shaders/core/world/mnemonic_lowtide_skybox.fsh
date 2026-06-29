#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float FaceSeed;
uniform float CoverageBias;
uniform float TunnelScale;
uniform float BubbleScale;
uniform float TendrilIntensity;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + FaceSeed * 13.71) * 43758.5453);
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
    float amplitude = 0.56;
    mat2 turn = mat2(0.80, -0.60, 0.60, 0.80);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.02 + vec2(2.17, -1.31);
        amplitude *= 0.5;
    }
    return total;
}

float ringMask(vec2 value, vec2 center, float radius, float width) {
    float edgeDistance = abs(length(value - center) - radius);
    return 1.0 - smoothstep(width, width * 2.8, edgeDistance);
}

float innerGlass(vec2 value, vec2 center, float radius) {
    float distanceToCenter = length(value - center);
    float inside = 1.0 - smoothstep(radius * 0.74, radius, distanceToCenter);
    float rimFalloff = smoothstep(radius * 0.20, radius * 0.82, distanceToCenter);
    return inside * rimFalloff;
}

float tendrilLane(vec2 value, float lane, float phase, float width) {
    float curl = sin(value.x * 3.1 + phase) * 0.070
            + sin(value.x * 7.3 - phase * 0.61) * 0.026
            + sin((value.x + value.y) * 4.7 + phase * 0.37) * 0.018;
    float distanceToLane = abs(value.y - lane - curl);
    return 1.0 - smoothstep(width, width * 2.9, distanceToLane);
}

void main() {
    vec2 uv = texCoord0;
    vec2 centered = uv * 2.0 - 1.0;
    centered.x *= 1.22;
    float time = HemoTime + FaceSeed * 0.017;

    float broadNoise = fbm(centered * 1.55 + vec2(time * 0.15, -time * 0.11));
    float fineNoise = fbm(centered * 5.8 + vec2(-time * 0.24, time * 0.18) + broadNoise * 0.44);
    vec2 warped = centered + vec2(
            sin(centered.y * 4.6 + broadNoise * 3.2 + time * 0.62),
            cos(centered.x * 4.1 - fineNoise * 2.7 - time * 0.54)
    ) * 0.064;
    float faceEdge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeBreakup = fbm(centered * 3.7 + warped * 0.9 + vec2(FaceSeed * 0.041, time * 0.13));
    float edgeCleanup = smoothstep(0.030, 0.150 + edgeBreakup * 0.050, faceEdge);
    float foregroundEdgeFill = mix(0.58, 1.0, edgeCleanup);
    float structureEdgeFade = smoothstep(0.018, 0.125 + edgeBreakup * 0.040, faceEdge);
    float tendrilEdgeFade = smoothstep(0.045, 0.190 + edgeBreakup * 0.055, faceEdge);

    float tunnelMask = 0.0;
    float tunnelGlass = 0.0;
    for (int i = 0; i < 4; i++) {
        float fi = float(i);
        float seedA = FaceSeed * 0.029 + fi * 2.17;
        vec2 center = vec2(
                cos(seedA) * 0.62 + (fi - 1.5) * 0.30,
                sin(seedA * 1.37) * 0.34 + (hash(vec2(fi, FaceSeed)) - 0.5) * 0.46
        );
        float radius = (0.52 + hash(vec2(FaceSeed, fi * 11.0)) * 0.42) * TunnelScale;
        float width = 0.030 + hash(vec2(fi * 7.0, FaceSeed)) * 0.026;
        float ring = ringMask(warped, center, radius, width);
        float breakup = smoothstep(0.18, 0.86,
                fbm(warped * (2.0 + fi * 0.35) + vec2(time * 0.11 + fi, -time * 0.07)));
        tunnelMask = max(tunnelMask, ring * (0.58 + breakup * 0.42));
        tunnelGlass = max(tunnelGlass, innerGlass(warped, center, radius) * 0.18 * (0.65 + breakup * 0.35));
    }

    float bubbleMembrane = 0.0;
    float bubbleGlass = 0.0;
    for (int i = 0; i < 10; i++) {
        float fi = float(i);
        vec2 bubbleCenter = vec2(
                hash(vec2(fi * 3.17, FaceSeed * 0.71)),
                hash(vec2(FaceSeed * 0.43, fi * 5.91))
        ) * 2.35 - 1.175;
        bubbleCenter.x *= 1.18;
        bubbleCenter += vec2(
                sin(time * 0.10 + fi * 1.73),
                cos(time * 0.08 + fi * 1.41)
        ) * 0.035;
        float radius = (0.080 + hash(vec2(fi * 13.0, FaceSeed * 2.0)) * 0.205) * BubbleScale;
        float distanceToBubble = length(warped - bubbleCenter);
        float membrane = 1.0 - smoothstep(0.014, 0.046, abs(distanceToBubble - radius));
        float glass = (1.0 - smoothstep(radius * 0.70, radius, distanceToBubble))
                * smoothstep(radius * 0.12, radius * 0.78, distanceToBubble);
        float sparkle = smoothstep(0.66, 0.96,
                fbm(warped * 9.0 + vec2(fi * 0.37 + time * 0.18, -fi * 0.21)));
        bubbleMembrane = max(bubbleMembrane, membrane * (0.62 + sparkle * 0.38));
        bubbleGlass = max(bubbleGlass, glass * (0.08 + sparkle * 0.04));
    }

    float redTendril = tunnelMask * 0.58;
    float parchmentTendril = pow(tunnelMask, 1.75) * 0.48;
    for (int i = 0; i < 7; i++) {
        float fi = float(i);
        float lane = mix(-0.92, 0.92, (fi + 0.5) / 7.0)
                + (hash(vec2(FaceSeed, fi)) - 0.5) * 0.15;
        float phase = FaceSeed * 0.081 + fi * 1.44 + time * (0.18 + fi * 0.014);
        float broadTendril = tendrilLane(warped, lane, phase, 0.010 + hash(vec2(fi, 4.0)) * 0.012);
        float hairline = tendrilLane(warped, lane + 0.026, phase + 1.9, 0.0045);
        float torn = smoothstep(0.20, 0.88,
                fbm(warped * (3.1 + fi * 0.18) + vec2(phase * 0.07, -phase * 0.05)));
        redTendril = max(redTendril, broadTendril * torn * TendrilIntensity);
        parchmentTendril = max(parchmentTendril, hairline * (0.45 + torn * 0.55) * TendrilIntensity);
    }

    tunnelMask *= structureEdgeFade;
    tunnelGlass *= structureEdgeFade;
    bubbleMembrane *= structureEdgeFade;
    bubbleGlass *= structureEdgeFade;
    redTendril *= tendrilEdgeFade;
    parchmentTendril *= tendrilEdgeFade;

    float membraneSheen = smoothstep(0.67, 0.95, fineNoise + bubbleMembrane * 0.18 + tunnelMask * 0.12);
    float lineVeinFlecks = smoothstep(0.56, 0.91,
            fbm(warped * 14.0 + vec2(time * 0.22 + FaceSeed * 0.01, -time * 0.17)));
    lineVeinFlecks *= clamp(tunnelMask * 0.72 + bubbleMembrane * 0.50 + parchmentTendril * 0.36, 0.0, 1.0);
    float membraneBreakupPoints = smoothstep(0.72, 0.97,
            fbm(warped * 21.0 + vec2(-time * 0.31, time * 0.27 + FaceSeed * 0.02)));
    membraneBreakupPoints *= clamp(tunnelMask * 0.62 + bubbleMembrane * 0.74 + redTendril * 0.34, 0.0, 1.0);
    redTendril = clamp(redTendril + lineVeinFlecks * 0.20, 0.0, 1.0);
    parchmentTendril = clamp(parchmentTendril + lineVeinFlecks * 0.16 + membraneBreakupPoints * 0.12, 0.0, 1.0);
    bubbleMembrane = clamp(bubbleMembrane + membraneBreakupPoints * 0.10, 0.0, 1.0);
    float structure = clamp(tunnelMask * 0.68 + bubbleMembrane * 0.42
            + redTendril * 0.34 + parchmentTendril * 0.52, 0.0, 1.0);
    float negativeSpace = 1.0 - structure;
    float foregroundLift = clamp(0.30 + structure * 0.76 + lineVeinFlecks * 0.30 + membraneBreakupPoints * 0.20,
            0.0, 1.25);

    vec3 darkOxblood = vec3(0.030, 0.010, 0.010);
    vec3 bruisedRed = vec3(0.30, 0.034, 0.026);
    vec3 arterialRed = vec3(0.70, 0.048, 0.030);
    vec3 parchment = vec3(0.88, 0.56, 0.27);
    vec3 paleGlass = vec3(0.78, 0.58, 0.48);
    vec3 color = darkOxblood * (0.72 + negativeSpace * 0.18);
    color = mix(color, bruisedRed, tunnelGlass * 0.54);
    color += paleGlass * (bubbleGlass * 0.54 + bubbleMembrane * 0.075 + membraneBreakupPoints * 0.085);
    color += arterialRed * redTendril * 0.44 * foregroundLift;
    color += parchment * parchmentTendril * 0.54 * foregroundLift;
    color += vec3(0.020, 0.004, 0.003) * lineVeinFlecks * 0.16;
    color += vec3(1.0, 0.74, 0.48) * membraneSheen * parchmentTendril * 0.24;
    color += vec3(0.62, 0.20, 0.15) * tunnelMask * broadNoise * 0.13;
    color = mix(vec3(0.090, 0.020, 0.014), color, foregroundEdgeFill);

    float coverage = clamp(CoverageBias * foregroundEdgeFill, 0.0, 1.15);
    float alpha = (tunnelMask * 0.46 + bubbleMembrane * 0.28 + bubbleGlass * 0.14
            + redTendril * 0.28 + parchmentTendril * 0.34 + membraneSheen * 0.055
            + lineVeinFlecks * 0.10 + membraneBreakupPoints * 0.11) * coverage;
    alpha *= vertexColor.a * ColorModulator.a;
    alpha = clamp(alpha * 0.82, 0.0, 0.76);

    if (alpha < 0.010) {
        discard;
    }

    fragColor = vec4(color * ColorModulator.rgb, alpha);
}
