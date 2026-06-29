#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float FaceSeed;
uniform float CoverageBias;
uniform float NoduleScale;
uniform float VeinIntensity;
uniform float BaseIntensity;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + FaceSeed * 9.31) * 43758.5453);
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
        value = turn * value * 2.01 + vec2(2.17, -1.31);
        amplitude *= 0.5;
    }
    return total;
}

vec2 cellularPoint(vec2 cell) {
    return vec2(hash(cell + vec2(3.17, 11.43)), hash(cell + vec2(19.71, 5.29)));
}

vec3 cellularNoduleField(vec2 value) {
    vec2 cellBase = floor(value);
    vec2 local = fract(value);
    float nearestDistance = 10.0;
    float secondNearestDistance = 10.0;
    float nearestSeed = 0.0;
    for (int y = -1; y <= 1; y++) {
        for (int x = -1; x <= 1; x++) {
            vec2 offset = vec2(float(x), float(y));
            vec2 cell = cellBase + offset;
            vec2 point = offset + cellularPoint(cell);
            point += (vec2(hash(cell + vec2(41.0, 7.0)), hash(cell + vec2(13.0, 29.0))) - 0.5) * 0.18;
            float distanceToPoint = length(local - point);
            if (distanceToPoint < nearestDistance) {
                secondNearestDistance = nearestDistance;
                nearestDistance = distanceToPoint;
                nearestSeed = hash(cell + vec2(5.0, 23.0));
            } else if (distanceToPoint < secondNearestDistance) {
                secondNearestDistance = distanceToPoint;
            }
        }
    }
    return vec3(nearestDistance, secondNearestDistance, nearestSeed);
}

void main() {
    vec2 uv = texCoord0;
    vec2 centered = uv * 2.0 - 1.0;
    centered.x *= 1.18;
    float time = HemoTime + FaceSeed * 0.011;

    float broadNoise = fbm(centered * 1.35 + vec2(time * 0.10, -time * 0.08));
    float fineNoise = fbm(centered * 5.2 + vec2(-time * 0.17, time * 0.14) + broadNoise * 0.38);
    vec2 warped = centered + vec2(
            sin(centered.y * 3.9 + broadNoise * 3.6 + time * 0.38),
            cos(centered.x * 3.4 - fineNoise * 2.8 - time * 0.34)
    ) * 0.078;

    float faceEdge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeBreakup = fbm(centered * 3.4 + warped * 0.8 + vec2(FaceSeed * 0.037, time * 0.10));
    float edgeCleanup = smoothstep(0.030, 0.160 + edgeBreakup * 0.050, faceEdge);
    float cornerRedFill = mix(0.52, 1.0, edgeCleanup);

    vec2 noduleSpace = warped * (2.12 * max(NoduleScale, 0.05)) + vec2(FaceSeed * 0.013, -FaceSeed * 0.017);
    vec3 cellular = cellularNoduleField(noduleSpace);
    float nearestDistance = cellular.x;
    float cellularBoundary = cellular.y - cellular.x;
    float noduleSeed = cellular.z;

    float noduleBreakup = fbm(noduleSpace * 2.35 + vec2(time * 0.08 + noduleSeed, -time * 0.06));
    float noduleBulge = 1.0 - smoothstep(0.34, 0.82, nearestDistance + (noduleBreakup - 0.5) * 0.082);
    noduleBulge = pow(clamp(noduleBulge, 0.0, 1.0), 1.20);

    float veinBreakup = fbm(noduleSpace * 5.8 + vec2(-time * 0.16, time * 0.13));
    float blackVeinWeb = 1.0 - smoothstep(0.038, 0.145, cellularBoundary + (veinBreakup - 0.5) * 0.044);
    float betweenNodules = 1.0 - smoothstep(0.22, 0.66, noduleBulge);
    float curledVeins = 1.0 - smoothstep(0.030, 0.090,
            abs(sin(noduleSpace.x * 5.4 + broadNoise * 3.2 + time * 0.28)
            + sin(noduleSpace.y * 6.2 - fineNoise * 2.6 - time * 0.21)) * 0.5);
    float capillaryVeins = 1.0 - smoothstep(0.018, 0.064,
            abs(sin(noduleSpace.x * 11.6 + fineNoise * 4.1 - time * 0.18)
            + sin((noduleSpace.x + noduleSpace.y) * 8.8 + broadNoise * 3.4 + time * 0.16)) * 0.5);
    capillaryVeins *= smoothstep(0.16, 0.72, noduleBulge) * (0.55 + veinBreakup * 0.45);
    blackVeinWeb = clamp(max(max(blackVeinWeb, curledVeins * betweenNodules * 0.54),
            capillaryVeins * 0.46) * VeinIntensity, 0.0, 1.0);

    float wetHighlight = smoothstep(0.42, 0.90, noduleBulge)
            * smoothstep(0.54, 0.92, fbm(noduleSpace * 4.1 + vec2(time * 0.12, noduleSeed * 2.4)));
    float ambientRedWash = clamp(0.28 + broadNoise * 0.42 + fineNoise * 0.18, 0.0, 1.0);
    float redPressure = clamp(noduleBulge * 0.66 + ambientRedWash * 0.42 + fineNoise * 0.10, 0.0, 1.0);

    vec3 deepBlack = vec3(0.024, 0.006, 0.005);
    vec3 oxblood = vec3(0.20, 0.026, 0.018);
    vec3 noduleRed = vec3(0.50, 0.050, 0.032);
    vec3 soreRed = vec3(0.74, 0.086, 0.048);
    vec3 veinBlack = vec3(0.002, 0.001, 0.001);

    vec3 color = mix(deepBlack, oxblood, 0.60 + ambientRedWash * 0.28);
    color = mix(color, noduleRed, redPressure * 0.62 * BaseIntensity);
    color += soreRed * wetHighlight * 0.18 * BaseIntensity;
    color = mix(color, veinBlack, clamp(blackVeinWeb * 0.78, 0.0, 0.88));
    color += vec3(0.30, 0.043, 0.030) * (1.0 - blackVeinWeb) * ambientRedWash * 0.13;
    color = mix(vec3(0.070, 0.010, 0.008), color, cornerRedFill);

    float basePresence = clamp(0.26 + noduleBulge * 0.38 + blackVeinWeb * 0.18 + ambientRedWash * 0.16, 0.0, 1.0);
    float alpha = basePresence * CoverageBias * BaseIntensity * cornerRedFill;
    alpha *= vertexColor.a * ColorModulator.a;
    alpha = clamp(alpha * 0.70, 0.0, 0.60);

    if (alpha < 0.008) {
        discard;
    }

    fragColor = vec4(color * ColorModulator.rgb, alpha);
}
