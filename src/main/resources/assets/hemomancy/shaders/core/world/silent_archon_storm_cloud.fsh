#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float CloudSeed;
uniform float CloudDensity;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(37.71, 149.23)) + CloudSeed * 59.1) * 43758.5453);
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
        value = turn * value * 2.05 + vec2(1.9, -0.7);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 uv = texCoord0;
    vec2 centered = uv * 2.0 - 1.0;
    float time = HemoTime * 0.042;

    vec2 driftA = vec2(time * 0.26, -time * 0.17);
    vec2 driftB = vec2(-time * 0.19, time * 0.12);
    float billow = fbm(uv * vec2(4.2, 2.7) + driftA + CloudSeed * 9.0);
    float breakup = fbm(uv * vec2(11.6, 6.8) + driftB + CloudSeed * 17.0);
    float undercurl = fbm((uv + vec2(0.0, 0.16)) * vec2(7.2, 4.8) - driftA * 0.73 + CloudSeed * 3.0);

    float edgeDistance = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float cardEdge = smoothstep(0.035, 0.24, edgeDistance);
    float domeMask = 1.0 - length(centered * vec2(0.92, 0.68));
    float stormMask = smoothstep(0.10, 0.56, domeMask);
    float tornEdge = smoothstep(0.34, 0.86, billow * 0.70 + breakup * 0.38 + undercurl * 0.20);
    float shelfShadow = smoothstep(-0.16, 0.38, 1.0 - length((centered + vec2(0.0, 0.24)) * vec2(0.94, 0.74)));

    vec3 darkCore = vec3(0.31, 0.37, 0.39);
    vec3 paleEdge = vec3(0.78, 0.84, 0.86);
    vec3 color = mix(darkCore, paleEdge, clamp(billow * 0.58 + shelfShadow * 0.42, 0.0, 1.0));
    color *= vertexColor.rgb * ColorModulator.rgb;

    float alpha = vertexColor.a * ColorModulator.a * CloudDensity;
    alpha *= cardEdge * stormMask * tornEdge * clamp(0.36 + billow * 0.42 + breakup * 0.28, 0.0, 1.0);
    alpha = clamp(alpha * 1.42, 0.0, 0.74);

    if (alpha < 0.012) {
        discard;
    }

    fragColor = vec4(color, alpha);
}
