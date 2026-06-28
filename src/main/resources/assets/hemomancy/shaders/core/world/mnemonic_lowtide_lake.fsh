#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float LakeSeed;
uniform float NoiseScale;
uniform float GlossStrength;
uniform float EdgeFade;

in vec4 vertexColor;
in vec2 texCoord0;
in float waveLiftAmount;
in float rippleHighlight;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + LakeSeed * 0.019) * 43758.5453);
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
    float amplitude = 0.5;
    mat2 turn = mat2(0.82, -0.57, 0.57, 0.82);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.04 + vec2(3.17, -1.41);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 uv = texCoord0;
    float time = HemoTime * 0.10;
    vec2 centered = uv - vec2(0.5);
    vec2 drift = vec2(time * 0.13, -time * 0.09);
    float low = fbm(centered * NoiseScale + drift);
    float high = fbm(centered * (NoiseScale * 2.35) + vec2(-time * 0.21, time * 0.17) + low * 0.85);
    vec2 warped = centered + vec2(
            sin((centered.y + low) * 8.0 + time * 2.4),
            cos((centered.x - high) * 7.0 - time * 2.1)
    ) * 0.035;

    float redStream = smoothstep(0.48, 0.82, fbm(warped * 9.5 + vec2(time * 0.31, -time * 0.22)));
    float blackPocket = smoothstep(0.30, 0.74, fbm(warped * 4.0 - vec2(time * 0.08, time * 0.11)));
    float greyPinkMarble = smoothstep(0.28, 0.72, fbm(warped * 6.2 + vec2(-time * 0.16, time * 0.12)));
    float microBreakup = fbm(warped * 31.0 + vec2(time * 0.52, -time * 0.47) + high * 0.36);
    float parchmentNoise = fbm(warped * 13.0 + vec2(-time * 0.34, time * 0.29));
    float parchmentFine = fbm(warped * 24.0 + vec2(time * 0.27, -time * 0.33) + microBreakup * 0.42);
    float parchmentHighlight = smoothstep(0.68, 0.94, parchmentNoise);
    float beigeStreak = pow(smoothstep(0.64, 0.98,
            parchmentNoise + parchmentFine * 0.24 + high * 0.12 + rippleHighlight * 0.18), 5.0);
    float parchmentVein = beigeStreak * smoothstep(0.42, 0.84, microBreakup + redStream * 0.18);
    float brightRedVein = pow(smoothstep(0.52, 0.91, redStream + microBreakup * 0.18 + rippleHighlight * 0.16), 2.4);
    float glintStreak = pow(smoothstep(0.58, 0.95, high + rippleHighlight * 0.45 + microBreakup * 0.16), 4.0)
            * GlossStrength;
    float glossLine = pow(max(0.0, high), 6.0) * GlossStrength;

    vec3 blackBase = vec3(0.012, 0.011, 0.012);
    vec3 oxblood = vec3(0.56, 0.045, 0.048);
    vec3 brightRed = vec3(0.92, 0.045, 0.032);
    vec3 deepOxblood = vec3(0.18, 0.014, 0.016);
    vec3 greyPink = vec3(0.24, 0.125, 0.148);
    vec3 parchment = vec3(0.62, 0.46, 0.28);
    vec3 warmBeigeVein = vec3(1.0, 0.68, 0.32);

    vec3 color = mix(blackBase, deepOxblood, blackPocket * 0.66);
    color = mix(color, greyPink, greyPinkMarble * 0.24);
    color = mix(color, oxblood, redStream * 0.48);
    color = mix(color, brightRed, brightRedVein * 0.18);
    color = mix(color, blackBase, smoothstep(0.18, 0.58, microBreakup) * 0.16);
    color += parchment * parchmentHighlight * 0.08;
    color += warmBeigeVein * parchmentVein * (0.24 + glintStreak * 0.22);
    color += vec3(0.90, 0.65, 0.58) * glossLine * (0.08 + parchmentVein * 0.16);
    color += vec3(0.76, 0.48, 0.50) * rippleHighlight * 0.18;
    color += vec3(0.94, 0.78, 0.54) * glintStreak * 0.18;
    color += greyPink * abs(waveLiftAmount) * 0.28;
    float luma = dot(color, vec3(0.299, 0.587, 0.114));
    float saturationBoost = 1.18;
    float contrastLift = 1.12;
    color = mix(vec3(luma), color, saturationBoost);
    color = (color - vec3(0.16)) * contrastLift + vec3(0.13);

    float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float horizonFade = smoothstep(0.0, EdgeFade * 1.85, edge);
    float edgeFade = smoothstep(0.0, EdgeFade, edge) * horizonFade;
    float alpha = (0.80 + redStream * 0.08 + parchmentVein * 0.14 + glossLine * 0.11
            + rippleHighlight * 0.06 + glintStreak * 0.08) * edgeFade;
    alpha *= vertexColor.a * ColorModulator.a;

    fragColor = vec4(color * ColorModulator.rgb, min(alpha, 0.95));
}
