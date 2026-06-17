#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float HermitDissolveProgress;
uniform float HermitDissolveSeed;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 localPosition;

out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32 + HermitDissolveSeed);
    return fract(p.x * p.y);
}

float spotGrowthForProgress(float progress) {
    return mix(0.24, 1.12, smoothstep(0.04, 0.86, clamp(progress, 0.0, 1.0)));
}

float spotCell(vec2 p, float progress) {
    vec2 cell = floor(p);
    vec2 f = fract(p) - vec2(0.5);
    float best = 0.0;
    float growth = spotGrowthForProgress(progress);

    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 neighbor = vec2(float(x), float(y));
            vec2 id = cell + neighbor;
            vec2 center = vec2(
                hash21(id + vec2(7.13, 2.91)),
                hash21(id + vec2(3.77, 9.41))
            ) - vec2(0.5);
            center *= 0.56;
            float radius = (0.11 + hash21(id + vec2(11.7, 5.2)) * 0.24) * growth;
            vec2 delta = f - neighbor - center;
            float angle = atan(delta.y, delta.x);
            float angularWobble = sin(angle * 5.0 + hash21(id + vec2(13.7, 4.9)) * 6.28318)
                    + sin(angle * 11.0 + hash21(id + vec2(1.7, 14.9)) * 6.28318) * 0.55;
            float tornNoise = hash21(floor((delta + id) * 19.0) + id * 1.37) - 0.5;
            float d = length(delta) * (1.0 + angularWobble * 0.075) + tornNoise * 0.018;
            float softness = mix(0.022, 0.070, growth);
            best = max(best, smoothstep(radius + softness * 0.55, radius - softness, d));
        }
    }

    return best;
}

float holeField(vec2 uv, vec3 p, float progress) {
    vec2 bodyDrift = vec2(
        sin(HemoTime * 0.017 + HermitDissolveSeed * 6.1),
        cos(HemoTime * 0.013 + HermitDissolveSeed * 4.7)
    ) * 0.12;
    vec2 bodyUv = uv + p.xz * 0.018 + vec2(p.y * 0.006, -p.y * 0.004) + bodyDrift;
    float fine = spotCell(bodyUv * 17.0 + HermitDissolveSeed * 9.0, progress);
    float broad = spotCell(bodyUv * 8.5 + vec2(2.4, -1.7) + HermitDissolveSeed * 5.0, progress);
    float torn = spotCell((uv.yx + p.xy * 0.012) * 12.0 + vec2(-3.1, 1.8), progress);
    return max(max(fine, broad * 0.92), torn * 0.72);
}

float cutForProgress(float progress) {
    return mix(1.08, 0.14, smoothstep(0.08, 0.94, clamp(progress, 0.0, 1.0)));
}

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.01) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;

    float progress = clamp(HermitDissolveProgress, 0.0, 1.0);
    float field = holeField(texCoord0, localPosition, progress);
    float cut = cutForProgress(progress);
    float bodyVisible = 1.0 - step(cut, field);
    if (bodyVisible < 0.5) {
        discard;
    }

    float edgeBand = mix(0.070, 0.145, smoothstep(0.18, 0.82, progress));
    float edge = 1.0 - smoothstep(0.014, edgeBand, abs(field - cut));
    float finalFade = 1.0 - smoothstep(0.90, 1.0, progress);

    vec3 dissolveColor = color.rgb;
    float scorch = edge * bodyVisible * smoothstep(0.22, 0.86, progress);
    dissolveColor *= mix(vec3(1.0), vec3(0.64, 0.45, 0.42), scorch * 0.28);
    dissolveColor += vec3(0.92, 0.025, 0.014) * edge * bodyVisible * (0.24 + progress * 0.58);

    float alpha = color.a * finalFade * bodyVisible;
    if (alpha < 0.015) {
        discard;
    }

    fragColor = linear_fog(vec4(dissolveColor, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
