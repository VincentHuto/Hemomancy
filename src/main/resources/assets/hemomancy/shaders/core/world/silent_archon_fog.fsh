#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float FogSeed;
uniform float FogLayer;
uniform float FogDensity;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + FogSeed * 19.17) * 43758.5453);
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
    mat2 turn = mat2(0.82, -0.57, 0.57, 0.82);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.06 + vec2(1.7, -0.4);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 uv = texCoord0;
    float time = HemoTime * 0.035;
    vec2 driftA = vec2(time * (0.32 + FogLayer * 0.08), -time * 0.18);
    vec2 driftB = vec2(-time * 0.21, time * (0.27 + FogLayer * 0.05));
    vec4 cloud = texture(Sampler0, uv + driftA);

    float volumeNoise = fbm(uv * vec2(3.7, 1.35) + driftB + FogSeed);
    float fineNoise = fbm(uv * vec2(9.2, 3.1) - driftA * 0.73 + FogLayer * 2.3);
    float roll = smoothstep(0.22, 0.88, volumeNoise * 0.78 + fineNoise * 0.32);
    float cloudLuma = dot(cloud.rgb, vec3(0.2126, 0.7152, 0.0722));
    float texturePresence = max(cloud.a, smoothstep(0.13, 0.58, cloudLuma));

    vec3 paleFog = vec3(0.52, 0.66, 0.68);
    vec3 deepFog = vec3(0.17, 0.30, 0.30);
    vec3 color = mix(deepFog, paleFog, clamp(texturePresence * 0.58 + roll * 0.45, 0.0, 1.0));
    color *= vertexColor.rgb * ColorModulator.rgb;

    float alpha = vertexColor.a * ColorModulator.a * FogDensity;
    alpha *= clamp(0.32 + texturePresence * 0.52 + roll * 0.46, 0.0, 1.0);
    alpha *= smoothstep(0.00, 0.055, min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y)));

    fragColor = linear_fog(vec4(color, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
