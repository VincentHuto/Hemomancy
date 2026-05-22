#version 150

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
    vec2 uv = texCoord0;
    float aspect = 1.7778;
    vec2 centered = vec2((uv.x - 0.5) * aspect, uv.y - 0.5);
    float time = HemoTime * 0.055;

    float swellA = fbm(centered * 4.6 + vec2(time * 0.72, -time * 0.18));
    float swellB = fbm(centered * 8.2 + vec2(-time * 0.28, time * 0.58) + swellA * 0.85);
    float foam = smoothstep(0.54, 0.92, swellB + swellA * 0.24);
    float trough = smoothstep(0.25, 0.78, swellA);

    float red = 0.48 + trough * 0.38 + foam * 0.18;
    float green = 0.015 + foam * 0.12;
    float blue = 0.028 + foam * 0.04;
    vec3 color = vec3(red, green, blue);

    float vignette = smoothstep(0.78, 0.18, length(centered));
    float flash = 1.0 - smoothstep(0.0, 0.22, Progress);
    color += vec3(0.22, 0.03, 0.025) * flash;
    color *= 0.68 + 0.42 * vignette;

    float alpha = Intensity * (0.84 + foam * 0.18);
    fragColor = vec4(color, clamp(alpha, 0.0, 1.0)) * vertexColor * ColorModulator;
}
