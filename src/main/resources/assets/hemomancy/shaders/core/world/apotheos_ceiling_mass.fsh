#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float CeilingSeed;
uniform float MassNoiseScale;
uniform float RotationSpeed;
uniform float YellowGlowIntensity;
uniform float GreenOrbIntensity;

in vec4 vertexColor;
in vec2 texCoord0;
in float ceilingAngleT;
in float ceilingRadialT;
in float ceilingBowlDepth;
in float organicShift;

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
    mat2 turn = mat2(0.78, -0.63, 0.63, 0.78);
    for (int i = 0; i < 5; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.06 + vec2(1.67, -0.73);
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

float orbPulse(float angleT, float radialT, float index, float time) {
    float seed = hash(vec2(index * 17.31, CeilingSeed * 0.071));
    float orbAngle = fract(seed + time * (0.020 + index * 0.002));
    float orbRadius = 0.26 + hash(vec2(index * 5.77, 3.41)) * 0.55;
    float angleDistance = circularDistance(angleT, orbAngle);
    float radialDistance = abs(radialT - orbRadius);
    float distanceField = sqrt(angleDistance * angleDistance * 3.6 + radialDistance * radialDistance);
    return 1.0 - smoothstep(0.020, 0.105 + seed * 0.030, distanceField);
}

void main() {
    float angle = ceilingAngleT * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float time = HemoTime * RotationSpeed + CeilingSeed * 0.013;
    float hemisphereY = sqrt(max(0.0, 1.0 - ceilingRadialT * ceilingRadialT));
    vec3 spherePosition = normalize(vec3(unitCircle.x * ceilingRadialT, hemisphereY, unitCircle.y * ceilingRadialT));
    vec3 rotatedSpherePosition = rotateZ(
            rotateY(
                    rotateX(spherePosition, time * 0.73 + sin(time * 0.31) * 0.34),
                    time * 0.49 + CeilingSeed * 0.002),
            time * 0.27 + cos(time * 0.41) * 0.22);

    vec2 sphericalMassFlow = vec2(rotatedSpherePosition.x * MassNoiseScale
            + rotatedSpherePosition.y * 2.2 + ceilingBowlDepth * 1.4,
            rotatedSpherePosition.z * MassNoiseScale + rotatedSpherePosition.y * 5.6 - time * 0.92);
    float primaryNoise = fbm(sphericalMassFlow);
    float fiberNoise = fbm(vec2(rotatedSpherePosition.x * 5.8 + rotatedSpherePosition.z * 2.1
            + primaryNoise * 1.5 - time * 0.28,
            rotatedSpherePosition.y * 7.6 + ceilingRadialT * 11.0 + time * 0.18));
    float veinNoise = fbm(vec2(rotatedSpherePosition.z * 8.4 + rotatedSpherePosition.y * 3.7
            + ceilingRadialT * 5.5,
            rotatedSpherePosition.x * 8.4 - ceilingRadialT * 12.0 + primaryNoise * 1.9));

    float meatFold = smoothstep(0.24, 0.88, primaryNoise + fiberNoise * 0.24);
    float purpleBruise = smoothstep(0.32, 0.78, fiberNoise + ceilingBowlDepth * 0.18);
    float redPulse = smoothstep(0.40, 0.86, veinNoise + sin(time + ceilingRadialT * 13.0) * 0.09);
    vec3 clottedRed = vec3(0.34, 0.012, 0.026);
    vec3 wetRed = vec3(0.72, 0.025, 0.050);
    vec3 deepPurple = vec3(0.21, 0.020, 0.34);
    vec3 voidBlack = vec3(0.004, 0.003, 0.006);
    vec3 redPurpleOrganicMass = mix(clottedRed, deepPurple, purpleBruise);
    redPurpleOrganicMass = mix(redPurpleOrganicMass, wetRed, redPulse * 0.46);
    redPurpleOrganicMass = mix(voidBlack, redPurpleOrganicMass, meatFold);

    float tendrilPhase = abs(fract(ceilingAngleT * 24.0 + fiberNoise * 0.58 + time * 0.10) - 0.5);
    float tendrilRadialBreak = smoothstep(0.12, 0.92, ceilingRadialT)
            * (1.0 - smoothstep(0.92, 1.0, ceilingRadialT));
    float blackWhiteTendrilTrace = (1.0 - smoothstep(0.020, 0.070, tendrilPhase)) * tendrilRadialBreak;
    float whiteTrace = pow(blackWhiteTendrilTrace, 4.0) * smoothstep(0.55, 0.96, veinNoise);

    float yellowBiolume = smoothstep(0.72, 1.05, primaryNoise + redPulse * 0.35 + ceilingBowlDepth * 0.22)
            * YellowGlowIntensity;
    float orbField = 0.0;
    for (int i = 0; i < 7; i++) {
        orbField += orbPulse(ceilingAngleT, ceilingRadialT, float(i), time);
    }
    orbField = clamp(orbField, 0.0, 1.0);
    float greenOrbGlow = orbField * GreenOrbIntensity
            * (0.74 + sin(time * 2.7 + ceilingAngleT * 17.0) * 0.14);
    float edgeFade = 1.0 - smoothstep(0.985, 1.0, ceilingRadialT);

    vec3 color = redPurpleOrganicMass;
    color = mix(color, voidBlack, blackWhiteTendrilTrace * 0.58);
    color += vec3(0.95, 0.92, 0.88) * whiteTrace * 0.55;
    color += vec3(1.0, 0.74, 0.055) * yellowBiolume * 0.28;
    color += vec3(0.72, 1.0, 0.16) * greenOrbGlow * 0.38;
    color *= 0.74 + ceilingRadialT * 0.22 + ceilingBowlDepth * 0.12 + organicShift * 0.32;

    float opacityCore = 0.62 + meatFold * 0.20 + redPulse * 0.10 + yellowBiolume * 0.06
            + greenOrbGlow * 0.16;
    float centerFeather = smoothstep(0.0, 0.08, ceilingRadialT);
    float alpha = opacityCore * centerFeather * edgeFade * vertexColor.a * ColorModulator.a;

    if (alpha < 0.01) {
        discard;
    }

    fragColor = vec4(color * ColorModulator.rgb, min(alpha, 0.95));
}
