#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float CeilingSeed;
uniform float AtmosphereNoiseScale;
uniform float RotationSpeed;
uniform float StormIntensity;
uniform float AtmosphereOpacity;

in vec4 vertexColor;
in vec2 texCoord0;
in float ceilingAngleT;
in float ceilingRadialT;
in float ceilingBowlDepth;
in float stormShift;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + CeilingSeed * 0.193) * 43758.5453);
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
    mat2 turn = mat2(0.74, -0.67, 0.67, 0.74);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.16 + vec2(1.31, -0.91);
        amplitude *= 0.5;
    }
    return total;
}

float circularDistance(float a, float b) {
    return abs(fract(a - b + 0.5) - 0.5) * 2.0;
}

vec3 rotateX(vec3 value, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return vec3(value.x, value.y * c - value.z * s, value.y * s + value.z * c);
}

vec3 rotateY(vec3 value, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return vec3(value.x * c + value.z * s, value.y, -value.x * s + value.z * c);
}

vec3 rotateZ(vec3 value, float angle) {
    float s = sin(angle);
    float c = cos(angle);
    return vec3(value.x * c - value.y * s, value.x * s + value.y * c, value.z);
}

float stormBloom(float angleT, float radialT, float index, float time) {
    float seed = hash(vec2(index * 19.19, CeilingSeed * 0.091));
    float orbAngle = fract(seed + time * (0.035 + index * 0.004));
    float orbRadius = 0.20 + hash(vec2(index * 7.37, 5.13)) * 0.68;
    float angleDistance = circularDistance(angleT, orbAngle);
    float radialDistance = abs(radialT - orbRadius);
    float field = sqrt(angleDistance * angleDistance * 3.1 + radialDistance * radialDistance);
    return 1.0 - smoothstep(0.028, 0.16 + seed * 0.035, field);
}

void main() {
    float angle = ceilingAngleT * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float time = HemoTime * RotationSpeed + CeilingSeed * 0.017;
    float hemisphereY = sqrt(max(0.0, 1.0 - ceilingRadialT * ceilingRadialT));
    vec3 spherePosition = normalize(vec3(unitCircle.x * ceilingRadialT, hemisphereY, unitCircle.y * ceilingRadialT));
    vec3 rotatedSpherePosition = rotateZ(
            rotateY(
                    rotateX(spherePosition, time * 1.16 + sin(time * 0.37) * 0.48),
                    -time * 0.82 + CeilingSeed * 0.003),
            time * 0.51 + cos(time * 0.43) * 0.36);

    vec2 stormFlow = vec2(rotatedSpherePosition.x * AtmosphereNoiseScale
            + rotatedSpherePosition.y * 4.9 + time * 1.8,
            rotatedSpherePosition.z * AtmosphereNoiseScale - rotatedSpherePosition.y * 6.3 - time * 1.35);
    float stormBase = fbm(stormFlow);
    float stormFiber = fbm(vec2(rotatedSpherePosition.z * 12.2 + stormBase * 3.1 + time * 0.72,
            rotatedSpherePosition.x * 8.6 - ceilingRadialT * 16.0 - time * 0.58));
    float violentStormBands = smoothstep(0.34, 0.78,
            stormBase + stormFiber * 0.46 + sin(ceilingAngleT * 44.0 + time * 2.9) * 0.13)
            * StormIntensity;
    float brokenCloudGaps = smoothstep(0.24, 0.70,
            fbm(stormFlow * 0.62 + vec2(time * 0.42, -time * 0.35)));
    float stormAlphaMask = clamp(violentStormBands * (0.38 + brokenCloudGaps * 0.62), 0.0, 1.0);
    stormAlphaMask *= smoothstep(0.08, 0.22, ceilingRadialT) * (1.0 - smoothstep(0.94, 1.0, ceilingRadialT));

    float yellowStormBloom = 0.0;
    float greenStormBloom = 0.0;
    for (int i = 0; i < 7; i++) {
        float bloom = stormBloom(ceilingAngleT, ceilingRadialT, float(i), time);
        yellowStormBloom += bloom * (i == 2 || i == 5 ? 0.45 : 1.0);
        greenStormBloom += bloom * (i == 2 || i == 5 ? 1.0 : 0.18);
    }
    yellowStormBloom = clamp(yellowStormBloom, 0.0, 1.0);
    greenStormBloom = clamp(greenStormBloom, 0.0, 1.0);

    vec3 smokeRed = vec3(0.46, 0.018, 0.045);
    vec3 stormPurple = vec3(0.20, 0.018, 0.34);
    vec3 emberOrange = vec3(0.95, 0.19, 0.030);
    vec3 color = mix(stormPurple, smokeRed, stormBase);
    color = mix(color, emberOrange, violentStormBands * 0.36);
    color += vec3(1.0, 0.78, 0.055) * yellowStormBloom * 0.62;
    color += vec3(0.52, 1.0, 0.16) * greenStormBloom * 0.48;
    color *= 0.60 + ceilingRadialT * 0.24 + ceilingBowlDepth * 0.16 + abs(stormShift) * 5.4;

    float alpha = AtmosphereOpacity * stormAlphaMask * (0.72 + yellowStormBloom * 0.18 + greenStormBloom * 0.16)
            * vertexColor.a * ColorModulator.a;
    if (alpha < 0.01) {
        discard;
    }

    fragColor = vec4(color * ColorModulator.rgb, min(alpha, 0.42));
}
