#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float FunnelSeed;
uniform float RingRise;
uniform float RingSpeed;
uniform float MeatNoiseScale;
uniform float HighlightIntensity;
uniform float CenterVoidRadius;

in vec4 vertexColor;
in vec2 texCoord0;
in float funnelDepth;
in float centerAperture;
in float ringHeightBias;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + FunnelSeed * 0.137) * 43758.5453);
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
    mat2 turn = mat2(0.80, -0.60, 0.60, 0.80);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.08 + vec2(1.73, -0.91);
        amplitude *= 0.5;
    }
    return total;
}

vec3 portalEdgeRainbow(float portalRainbowProgress) {
    vec3 prismRed = vec3(1.0, 0.018, 0.010);
    vec3 prismMagenta = vec3(1.0, 0.020, 0.58);
    vec3 prismPurple = vec3(0.53, 0.050, 1.0);
    vec3 prismLilac = vec3(0.76, 0.46, 1.0);
    vec3 prismLightBlue = vec3(0.54, 0.76, 1.0);
    vec3 prismCyan = vec3(0.0, 0.94, 1.0);
    vec3 prismGreen = vec3(0.20, 1.0, 0.14);
    vec3 prismYellow = vec3(1.0, 0.94, 0.08);
    vec3 prismOrange = vec3(1.0, 0.38, 0.035);

    float p = clamp(portalRainbowProgress, 0.0, 1.0) * 9.0;
    if (p < 1.0) {
        return mix(prismRed, prismMagenta, p);
    }
    if (p < 2.0) {
        return mix(prismMagenta, prismPurple, p - 1.0);
    }
    if (p < 3.0) {
        return mix(prismPurple, prismLilac, p - 2.0);
    }
    if (p < 4.0) {
        return mix(prismLilac, prismLightBlue, p - 3.0);
    }
    if (p < 5.0) {
        return mix(prismLightBlue, prismCyan, p - 4.0);
    }
    if (p < 6.0) {
        return mix(prismCyan, prismGreen, p - 5.0);
    }
    if (p < 7.0) {
        return mix(prismGreen, prismYellow, p - 6.0);
    }
    if (p < 8.0) {
        return mix(prismYellow, prismOrange, p - 7.0);
    }
    return mix(prismOrange, prismRed, p - 8.0);
}

void main() {
    float radialDistance = texCoord0.y;
    float angle = texCoord0.x * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float time = HemoTime * RingSpeed + FunnelSeed * 0.011;
    float centerVoid = 1.0 - smoothstep(CenterVoidRadius, CenterVoidRadius + 0.040, radialDistance);

    vec2 polarFlow = vec2(unitCircle.x * 1.74 + sin(radialDistance * 8.0 + time) * 0.18,
            unitCircle.y * 1.74 + radialDistance * MeatNoiseScale - time * 0.72);
    float meatNoise = fbm(polarFlow);
    float fiberNoise = fbm(vec2(unitCircle.x * 3.15 + unitCircle.y * 0.72 - time * 0.31,
            unitCircle.y * 1.35 + radialDistance * MeatNoiseScale * 2.45 + meatNoise * 1.2));
    float veinNoise = fbm(vec2(unitCircle.x * 5.8 + radialDistance * 2.2,
            unitCircle.y * 5.8 + radialDistance * 19.0 - time * 1.05));

    float portalRainbowProgress = clamp((radialDistance - (CenterVoidRadius + 0.046)) / 0.42, 0.0, 1.0);
    float portalStartTightness = 1.0 - smoothstep(CenterVoidRadius + 0.055, CenterVoidRadius + 0.24,
            radialDistance);
    float outwardBandWidening = smoothstep(CenterVoidRadius + 0.22, 0.92, radialDistance);
    float rainbowColorPhase = fract(portalRainbowProgress * 9.0);
    float colorChangeStriations = (1.0 - smoothstep(0.035, 0.18,
            min(rainbowColorPhase, 1.0 - rainbowColorPhase)))
            * smoothstep(CenterVoidRadius + 0.050, CenterVoidRadius + 0.11, radialDistance)
            * (1.0 - smoothstep(CenterVoidRadius + 0.44, CenterVoidRadius + 0.56, radialDistance));
    float softColorChangeStriations = colorChangeStriations * (0.34 + smoothstep(0.18, 0.72,
            radialDistance) * 0.20);
    float ringWidthNoise = fbm(vec2(unitCircle.x * 2.8 + radialDistance * 5.6 - time * 0.11,
            unitCircle.y * 2.8 + radialDistance * 7.4 + time * 0.08)) - 0.5;
    float ringDensity = 34.0 + portalStartTightness * 9.0 + softColorChangeStriations * 3.5
            - outwardBandWidening * 3.0;
    float ringTravel = radialDistance * ringDensity - time * 1.34 + meatNoise * 0.58;
    float ringCell = abs(fract(ringTravel) - 0.5);
    float denseBandCore = clamp(0.118 + outwardBandWidening * 0.050 - portalStartTightness * 0.012
            - softColorChangeStriations * 0.006 + ringWidthNoise * 0.008, 0.086, 0.175);
    float denseBandFeather = clamp(0.280 + outwardBandWidening * 0.065 - portalStartTightness * 0.024
            - softColorChangeStriations * 0.010 + ringWidthNoise * 0.012, denseBandCore + 0.085, 0.365);
    float denseWideRing = 1.0 - smoothstep(denseBandCore, denseBandFeather, ringCell);
    float tightBandEdge = clamp(0.158 + outwardBandWidening * 0.028 - portalStartTightness * 0.014
            - softColorChangeStriations * 0.006, 0.112, 0.205);
    float expandingRing = max(denseWideRing, 1.0 - smoothstep(0.035, tightBandEdge, ringCell));
    float secondaryFrequency = 15.0 + portalStartTightness * 7.0 + softColorChangeStriations * 4.5
            - outwardBandWidening * 1.8;
    float secondaryBand = 1.0 - smoothstep(0.030, 0.106 + outwardBandWidening * 0.030,
            abs(fract(radialDistance * secondaryFrequency - time * 0.58 + fiberNoise * 0.42) - 0.5));
    float blackGap = smoothstep(0.315, 0.500, ringCell) * (1.0 - expandingRing * 0.72);
    float narrowGapDarkening = blackGap * (0.20 + funnelDepth * 0.065);
    float arterialHighlight = pow(expandingRing, 3.2)
            * smoothstep(0.70, 0.95, veinNoise + fiberNoise * 0.18)
            * HighlightIntensity;
    float whiteHotSheen = pow(denseWideRing, 5.0)
            * smoothstep(0.82, 1.02, veinNoise + fiberNoise * 0.26 + meatNoise * 0.10)
            * HighlightIntensity;
    float portalRainbowMask = smoothstep(CenterVoidRadius + 0.030, CenterVoidRadius + 0.070, radialDistance)
            * (1.0 - smoothstep(CenterVoidRadius + 0.42, CenterVoidRadius + 0.56, radialDistance));
    portalRainbowMask *= clamp(denseWideRing * 0.90 + secondaryBand * 0.22 + softColorChangeStriations * 0.08
            + arterialHighlight * 0.18, 0.0, 1.0);
    portalRainbowMask *= 0.72 + smoothstep(0.34, 0.84, meatNoise + fiberNoise * 0.20) * 0.28;
    vec3 portalRainbowColor = portalEdgeRainbow(portalRainbowProgress);
    float interRingSpace = smoothstep(0.18, 0.48, ringCell) * (1.0 - denseWideRing * 0.45);
    float fogFlow = fbm(vec2(unitCircle.x * 2.15 + time * 0.18 + fiberNoise * 0.36,
            unitCircle.y * 2.15 + radialDistance * 12.0 - time * 0.54 + meatNoise * 1.25));
    float fogVeil = fbm(vec2(unitCircle.x * 4.2 - time * 0.12,
            unitCircle.y * 4.2 + radialDistance * 22.0 + fogFlow * 1.45));
    float hazeBandMask = smoothstep(CenterVoidRadius + 0.055, CenterVoidRadius + 0.12, radialDistance)
            * (1.0 - smoothstep(0.86, 0.98, radialDistance));
    float driftingInterRingFog = smoothstep(0.24, 0.78, fogFlow + fogVeil * 0.32)
            * interRingSpace * hazeBandMask;
    float broadGapFill = smoothstep(0.070, 0.430, ringCell) * hazeBandMask
            * (0.18 + meatNoise * 0.12 + portalRainbowMask * 0.18);
    float interRingHaze = interRingSpace * hazeBandMask
            * (0.24 + meatNoise * 0.14 + secondaryBand * 0.08 + driftingInterRingFog * 0.30 + broadGapFill * 0.44)
            * (0.56 + portalRainbowMask * 0.38);

    vec3 blackBase = vec3(0.004, 0.003, 0.004);
    vec3 bruisedBlack = vec3(0.018, 0.009, 0.012);
    vec3 clottedRed = vec3(0.30, 0.016, 0.021);
    vec3 darkFlesh = vec3(0.18, 0.048, 0.055);
    vec3 arterialRed = vec3(0.86, 0.035, 0.028);
    vec3 paleSheen = vec3(1.0, 0.62, 0.55);
    vec3 whiteCore = vec3(1.0, 0.88, 0.78);
    vec3 hazeGlowColor = mix(clottedRed * 0.52 + darkFlesh * 0.28, portalRainbowColor * 0.42,
            portalRainbowMask * 0.82);
    vec3 driftingFogColor = mix(darkFlesh * 0.62 + paleSheen * 0.12, portalRainbowColor * 0.58,
            portalRainbowMask * 0.92);

    vec3 color = mix(blackBase, bruisedBlack, smoothstep(0.0, 1.0, meatNoise));
    color = mix(color, clottedRed, expandingRing * (0.68 + meatNoise * 0.18));
    color = mix(color, darkFlesh, secondaryBand * 0.34);
    color = mix(color, blackBase, narrowGapDarkening);
    color += arterialRed * arterialHighlight * 0.78;
    color += paleSheen * arterialHighlight * 0.22;
    color += whiteCore * whiteHotSheen * 0.32;
    color = mix(color, portalRainbowColor, portalRainbowMask * (0.68 + whiteHotSheen * 0.10));
    color += hazeGlowColor * broadGapFill * 0.46;
    color += hazeGlowColor * interRingHaze;
    color += driftingFogColor * driftingInterRingFog * (0.18 + whiteHotSheen * 0.07);
    color += clottedRed * ringHeightBias * 0.045;
    color *= 0.66 + radialDistance * 0.42 + RingRise * 0.035;
    color = mix(color, blackBase, centerVoid);

    float outerFade = 1.0 - smoothstep(0.91, 1.0, radialDistance);
    float innerFade = centerAperture;
    float meatOpacity = 0.76 + expandingRing * 0.18 + secondaryBand * 0.07 + arterialHighlight * 0.11
            + whiteHotSheen * 0.08 + interRingHaze * 0.14 + driftingInterRingFog * 0.08 + broadGapFill * 0.10;
    float alpha = meatOpacity * innerFade * outerFade * vertexColor.a * ColorModulator.a;
    alpha *= 0.74 + meatNoise * 0.14 + fiberNoise * 0.08;

    if (alpha < 0.01) {
        discard;
    }

    fragColor = vec4(color * ColorModulator.rgb, min(alpha, 0.94));
}
