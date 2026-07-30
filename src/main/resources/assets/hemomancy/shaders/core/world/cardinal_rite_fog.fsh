#version 150

#moj_import <fog.glsl>

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float FogSeed;

in float vertexDistance;
in vec4 vertexColor;
in vec2 fogUv;

out vec4 fragColor;

float hash(vec2 value) {
    float puffSeed = vertexColor.r * 97.31 + FogSeed * 23.71;
    return fract(sin(dot(value, vec2(127.1, 311.7)) + puffSeed) * 43758.5453);
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
    for (int octave = 0; octave < 5; octave++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.03 + vec2(1.9, -0.7);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 uv = fogUv;
    vec2 centered = uv * 2.0 - 1.0;
    float time = HemoTime * 0.006;
    float phase = vertexColor.r * 12.73 + FogSeed * 0.17;
    vec2 driftA = vec2(time * 0.18, -time * 0.11);
    vec2 driftB = vec2(-time * 0.13, time * 0.16);

    vec2 warp = vec2(
            fbm(centered * 1.28 + driftA + phase),
            fbm(centered * 1.28 - driftB + phase + vec2(5.17, -3.41))) - 0.5;
    vec2 billowed = centered + warp * 0.38;
    float broad = fbm(billowed * 1.62 + driftA * 0.72 + phase * 0.31);
    float curls = fbm(billowed * 4.85 - driftB + phase * 0.73);
    float erosion = fbm(billowed * 8.70 + driftA.yx * 1.37 + phase * 1.11);

    // Break up the billboard boundary with noise instead of drawing a circular disc.
    float edgeDistance = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeNoise = fbm(centered * 2.15 + driftB * 0.54 + phase * 0.83);
    float edgeFeather = 0.17 + edgeNoise * 0.11;
    float warpedEdge = edgeDistance + (edgeNoise - 0.5) * 0.075;
    float cardEdge = smoothstep(0.0, edgeFeather, warpedEdge);

    // Cloud coverage is driven by overlapping noise fields, not a radius test.
    // This produces lobes, torn pockets, and thin wisps across the whole sprite.
    float cloudField = broad * 0.56 + curls * 0.29 + erosion * 0.15;
    float lobeField = fbm(billowed * 2.35 - driftA * 0.45 + phase * 0.57);
    float cloudCoverage = cloudField + (lobeField - 0.5) * 0.24;
    float bodyMask = smoothstep(0.34, 0.62, cloudCoverage);
    float tornBillow = smoothstep(0.24, 0.76,
            broad * 0.54 + curls * 0.29 + erosion * 0.17);
    float density = bodyMask * cardEdge * mix(0.38, 1.0, tornBillow);
    density *= clamp(0.44 + cloudField * 0.52, 0.0, 1.0);

    // A soft vertical value gradient gives the billboard a top-lit, billowing volume.
    float topLight = smoothstep(-0.72, 0.68, centered.y);
    float volumeCurls = fbm(billowed * 3.25 + driftA.yx * 0.80 + phase * 1.23);
    density *= mix(0.74, 1.08, topLight);
    density *= 0.86 + volumeCurls * 0.20;

    float redCurrent = smoothstep(0.52, 0.84,
            fbm(billowed * 3.35 + driftB * 0.61 + phase * 1.47));
    float crimsonWeight = vertexColor.g;
    float crimsonMix = clamp(crimsonWeight * (0.17 + redCurrent * 0.92), 0.0, 0.94);
    vec3 blackCore = vec3(0.007, 0.0015, 0.0025);
    vec3 sootEdge = vec3(0.035, 0.004, 0.006);
    vec3 bloodFog = vec3(0.34, 0.006, 0.014);
    vec3 color = mix(blackCore, sootEdge, clamp(broad * 0.58 + curls * 0.18, 0.0, 1.0));
    color = mix(color, bloodFog, crimsonMix);
    color *= mix(0.72, 1.08, topLight);
    color *= vertexColor.b * ColorModulator.rgb;

    float alpha = vertexColor.a * ColorModulator.a * density;
    alpha = min(alpha * 1.24, 0.34);
    if (alpha < 0.003) {
        discard;
    }

    fragColor = linear_fog(vec4(color, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
