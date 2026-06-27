#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float TideSeed;
uniform float RippleStrength;
uniform float ReflectionStrength;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec3 worldPos;

out vec4 fragColor;

float rippleNormal(vec2 uv, float time) {
    float longWave  = sin((uv.x * 5.2  + uv.y * 1.7)  + time * 1.45 + TideSeed * 6.28318);
    float crossWave = sin((uv.y * 8.3  - uv.x * 2.6)  - time * 1.92 + TideSeed * 3.7);
    float fineWave  = sin((uv.x + uv.y) * 19.0         + time * 3.4);
    return longWave * 0.52 + crossWave * 0.34 + fineWave * 0.14;
}

// Organic shimmer using incommensurate frequencies - no repeating periodic bands
float organicShimmer(vec2 uv, float time) {
    float a = sin(uv.x *  7.13 + uv.y *  3.97 + time * 0.83) * 0.5 + 0.5;
    float b = sin(uv.x * 11.41 - uv.y *  5.23 - time * 0.61) * 0.5 + 0.5;
    float c = sin(uv.x *  2.71 + uv.y *  8.67 + time * 1.07) * 0.5 + 0.5;
    return a * b * c;
}

void main() {
    vec2 uv    = texCoord0;
    float time = HemoTime * 0.72;

    float waveA = rippleNormal(uv * 1.12     + vec2( time * 0.020, -time * 0.013), time);
    float waveB = rippleNormal(uv.yx * 0.91  + vec2(-time * 0.017,  time * 0.021), time + 1.73);
    vec2 rippleUv = uv + vec2(waveA * 0.018, waveB * 0.014) * RippleStrength;

    vec4 fluidTexture = texture(Sampler0, fract(rippleUv));
    float ripple = rippleNormal(rippleUv * 1.65, time);

    // Multiply two independent FBM layers so bright spots are rare and spatially uneven
    float shimmer = organicShimmer(rippleUv * 1.4, time * 0.40)
                  * organicShimmer(rippleUv.yx * 0.9, time * 0.30 + 1.2);
    shimmer = shimmer * shimmer * 2.2;

    // Horizon fade: raw UV centre is (1.9, 1.9) for uvScale=3.8.
    // edgeDist: 0 at centre, 1 at quad corners. Alpha drops to 0 at edges to hide hard rim.
    vec2  centred = texCoord0 - vec2(1.9, 1.9);
    float edgeDist = max(abs(centred.x), abs(centred.y)) / 1.9;
    float horizonFade = 1.0 - smoothstep(0.62, 1.0, edgeDist);

    vec3 deepFluid  = vec3(0.008, 0.002, 0.004);
    vec3 bloodUmber = vec3(0.18,  0.042, 0.022);
    vec3 darkGlint  = vec3(0.45,  0.06,  0.03);

    vec3 textureColor = mix(deepFluid, fluidTexture.rgb * vec3(0.30, 0.08, 0.06), 0.55);
    vec3 color = mix(textureColor, bloodUmber, clamp(0.12 + ripple * 0.08, 0.0, 0.32));
    color += darkGlint * shimmer * ReflectionStrength * 0.10;
    color += vec3(0.40, 0.05, 0.02) * max(ripple, 0.0) * 0.038;

    float alpha = vertexColor.a * ColorModulator.a;
    alpha *= clamp(0.72 + shimmer * 0.08, 0.0, 0.88) * horizonFade;

    fragColor = linear_fog(vec4(color * vertexColor.rgb * ColorModulator.rgb, alpha),
            vertexDistance, FogStart, FogEnd, FogColor);
}
