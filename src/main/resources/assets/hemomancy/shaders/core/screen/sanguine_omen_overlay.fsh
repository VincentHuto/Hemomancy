#version 150

uniform sampler2D Sampler0;
uniform float HemoTime;
uniform float Progress;
uniform float Intensity;
uniform float Seed;
uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + Seed * 0.013) * 43758.5453);
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
    mat2 rotate = mat2(0.80, -0.60, 0.60, 0.80);
    for (int i = 0; i < 5; i++) {
        total += amplitude * noise(value);
        value = rotate * value * 2.02 + vec2(1.7, -0.4);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 baseUv = vec2(texCoord0.x, 1.0 - texCoord0.y);
    float aspect = 1.7778;
    vec2 centered = vec2((baseUv.x - 0.5) * aspect, baseUv.y - 0.5);
    float time = HemoTime * 0.055;

    float swellA = fbm(centered * 4.6 + vec2(time * 0.72, -time * 0.18));
    float swellB = fbm(centered * 8.2 + vec2(-time * 0.28, time * 0.58) + swellA * 0.85);
    vec2 current = vec2(
        sin((baseUv.y + swellA * 0.45) * 28.0 + time * 7.0),
        cos((baseUv.x + swellB * 0.35) * 23.0 - time * 5.5)
    ) * 0.0045 * Intensity;
    vec2 sampleUv = clamp(baseUv + current, vec2(0.001), vec2(0.999));
    vec4 source = texture(Sampler0, sampleUv);

    float luminance = dot(source.rgb, vec3(0.299, 0.587, 0.114));
    float maxChannel = max(source.r, max(source.g, source.b));
    float minChannel = min(source.r, min(source.g, source.b));
    float chroma = maxChannel - minChannel;

    float warmCloudExclusion = smoothstep(0.06, 0.24, source.r - source.b)
        * smoothstep(0.02, 0.18, source.g - source.b);
    float blueWaterMask = smoothstep(0.04, 0.24, source.b - source.r * 0.94);
    float greenWaterMask = smoothstep(0.05, 0.24, source.g - source.r * 0.96)
        * smoothstep(-0.02, 0.16, source.b - source.r * 0.72);
    float waterMask = max(blueWaterMask, greenWaterMask) * smoothstep(0.08, 0.55, luminance);
    float cloudMask = smoothstep(0.62, 0.88, luminance) * (1.0 - smoothstep(0.04, 0.16, chroma))
        * (1.0 - warmCloudExclusion);
    float blackMask = clamp(max(waterMask, cloudMask), 0.0, 1.0);

    float waveContrast = 0.76 + swellA * 0.22 + swellB * 0.14;
    float flash = 1.0 - smoothstep(0.0, 0.20, Progress);
    float warmTerrainMask = smoothstep(0.10, 0.42, luminance)
        * smoothstep(0.04, 0.24, source.r - source.b)
        * smoothstep(0.00, 0.18, source.g - source.b)
        * (1.0 - waterMask);
    float shadowLift = 0.10 * warmTerrainMask * (1.0 - blackMask);
    float redValue = clamp(luminance * waveContrast + flash * 0.16 + shadowLift, 0.0, 1.0);
    vec3 redScale = vec3(redValue, 0.0, 0.0);
    redScale = mix(redScale, vec3(0.0), blackMask);

    float gradeStrength = smoothstep(0.02, 0.45, Intensity);
    vec3 finalColor = mix(source.rgb, redScale, gradeStrength);
    finalColor = mix(finalColor, vec3(0.0), blackMask * gradeStrength);

    fragColor = vec4(finalColor, source.a) * vertexColor * ColorModulator;
}
