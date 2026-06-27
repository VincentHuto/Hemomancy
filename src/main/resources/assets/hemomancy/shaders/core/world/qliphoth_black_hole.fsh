#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float HoleSeed;
uniform float LensStrength;
uniform float RingIntensity;
uniform float FinalHole;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + HoleSeed * 71.13) * 43758.5453);
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
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.04 + vec2(1.31, -0.73);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 uv = texCoord0;
    vec2 centered = uv * 2.0 - 1.0;
    centered.x *= 1.03;
    float radius = length(centered);
    float angle = atan(centered.y, centered.x);
    float time = HemoTime + HoleSeed * 19.0;

    float safeRadius = max(radius, 0.035);
    float swirl = time * mix(1.7, 0.72, FinalHole) + LensStrength / safeRadius;
    float spiralNoise = fbm(vec2(angle * 0.74 + time * 0.27, safeRadius * 6.2 - time * 0.18));
    vec2 lensDirection = normalize(centered + vec2(0.0007, -0.0004));
    float lensFalloff = smoothstep(1.12, 0.08, radius);
    float lensAmount = LensStrength * lensFalloff / (safeRadius * 3.65 + 0.045);
    vec2 tangential = vec2(-lensDirection.y, lensDirection.x);
    vec2 lensOffset = lensDirection * lensAmount * mix(0.54, 0.82, FinalHole);
    vec2 shearOffset = tangential * (0.068 + FinalHole * 0.052)
            * sin(swirl + spiralNoise * 4.8) * lensFalloff;
    vec2 lensedUv = uv - lensOffset + shearOffset;
    float chromaticOffset = (0.010 + FinalHole * 0.012) * lensFalloff * smoothstep(0.88, 0.18, radius);
    vec3 warpedSky = vec3(
            texture(Sampler0, fract(lensedUv + lensDirection * chromaticOffset)).r,
            texture(Sampler0, fract(lensedUv)).g,
            texture(Sampler0, fract(lensedUv - lensDirection * chromaticOffset)).b
    );

    float coreRadius = mix(0.162, 0.212, FinalHole);
    float eventHorizon = smoothstep(coreRadius + 0.035, coreRadius - 0.018, radius);
    float photonRing = smoothstep(coreRadius + 0.185, coreRadius + 0.038, radius)
            * smoothstep(coreRadius - 0.006, coreRadius + 0.038, radius);
    float outerRing = smoothstep(0.86, 0.30, radius) * smoothstep(coreRadius + 0.055, coreRadius + 0.26, radius);
    float spiral = smoothstep(0.22, 0.92,
            sin(angle * (4.4 + FinalHole * 1.8) - log(safeRadius) * 6.5 + time * 2.6 + spiralNoise * 4.2)
                    * 0.5 + 0.5);
    float tornMist = fbm(centered * vec2(4.2, 2.8) + vec2(time * 0.32, -time * 0.21));

    vec3 deepRed = vec3(0.54, 0.015, 0.018);
    vec3 arterial = vec3(0.96, 0.085, 0.035);
    vec3 bruisedBlue = vec3(0.11, 0.055, 0.24);
    vec3 ringColor = mix(deepRed, arterial, clamp(photonRing * 1.4 + spiral * 0.45, 0.0, 1.0));
    ringColor = mix(ringColor, bruisedBlue, clamp((1.0 - spiral) * outerRing * 0.36, 0.0, 1.0));
    float lensWindow = (1.0 - smoothstep(coreRadius + 0.30, 0.92, radius)) * smoothstep(coreRadius + 0.020, coreRadius + 0.120, radius);
    vec3 magnifiedSky = warpedSky * (0.78 + lensWindow * 0.70 + outerRing * 0.22);
    float ringBlend = clamp(photonRing * 1.08 + outerRing * spiral * 0.28 - lensWindow * 0.42, 0.0, 1.0);
    vec3 color = mix(magnifiedSky, ringColor, ringBlend);
    color *= 1.0 - eventHorizon;
    color += ringColor * photonRing * RingIntensity * (0.62 + tornMist * 0.48);

    float edgeFade = smoothstep(0.0, 0.13, min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y)));
    float residualFade = mix(1.0, 1.18, FinalHole);
    float alpha = edgeFade * vertexColor.a * ColorModulator.a;
    alpha *= clamp(eventHorizon * 1.18 + photonRing * 1.32 + lensWindow * 0.88
            + outerRing * (0.20 + spiral * 0.28), 0.0, residualFade);
    alpha *= 0.78 + FinalHole * 0.20;

    if (alpha < 0.01) {
        discard;
    }

    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
