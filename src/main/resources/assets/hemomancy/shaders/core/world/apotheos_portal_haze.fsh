#version 150

uniform vec4 ColorModulator;
uniform float HemoTime;
uniform float HazeSeed;
uniform float OutwardSpeed;
uniform float HazeIntensity;
uniform float CenterVoidRadius;

in vec4 vertexColor;
in vec2 texCoord0;
in float radialDistance;
in float centerAperture;
in float outwardLift;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(41.17, 289.31)) + HazeSeed * 0.071) * 9437.271);
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
    mat2 turn = mat2(0.72, -0.69, 0.69, 0.72);
    for (int i = 0; i < 4; i++) {
        total += noise(value) * amplitude;
        value = turn * value * 2.15 + vec2(0.63, -1.21);
        amplitude *= 0.52;
    }
    return total;
}

float dustMoteField(vec2 value, float period) {
    vec2 cell = floor(value);
    vec2 wrappedCell = vec2(mod(cell.x, period), cell.y);
    vec2 local = fract(value);
    vec2 moteCenter = vec2(hash(wrappedCell + vec2(3.1, 17.7)),
            hash(wrappedCell + vec2(41.9, 5.3))) * 0.74 + 0.13;
    float moteRadius = 0.035 + hash(wrappedCell + vec2(11.8, 29.2)) * 0.030;
    float motePresence = smoothstep(0.42, 0.58, hash(wrappedCell + vec2(71.4, 9.6)));
    float individualDustMotes = 1.0 - smoothstep(moteRadius, moteRadius + 0.030, length(local - moteCenter));
    return individualDustMotes * motePresence;
}

void main() {
    float angle = texCoord0.x * 6.2831853;
    vec2 unitCircle = vec2(cos(angle), sin(angle));
    float slowerOutwardFlow = HemoTime * OutwardSpeed * 0.54 + HazeSeed * 0.019;

    float voidRimConcentration = smoothstep(CenterVoidRadius + 0.018, CenterVoidRadius + 0.060, radialDistance)
            * (1.0 - smoothstep(CenterVoidRadius + 0.18, CenterVoidRadius + 0.31, radialDistance));
    float portalEdgeDensityBoost = 1.0 - smoothstep(CenterVoidRadius + 0.055, CenterVoidRadius + 0.17,
            radialDistance);
    float visibleFieldEdgeTaper = 1.0 - smoothstep(CenterVoidRadius + 0.20, CenterVoidRadius + 0.34,
            radialDistance);
    float cyanMagentaMoteSuppression = 1.0 - smoothstep(CenterVoidRadius + 0.28, CenterVoidRadius + 0.39,
            radialDistance);
    float magentaCyanFalloff = cyanMagentaMoteSuppression;
    float firstFewRingMoteField = voidRimConcentration * visibleFieldEdgeTaper * magentaCyanFalloff;
    float portalRimEmitter = firstFewRingMoteField * (0.72 + portalEdgeDensityBoost * 0.48);
    float outwardPortalFlow = fract(radialDistance * 13.8 - slowerOutwardFlow * 1.10
            + fbm(unitCircle * 2.6 + radialDistance * 3.2) * 0.42);

    vec2 flowUv = vec2(unitCircle.x * 9.8 + radialDistance * 30.0 - slowerOutwardFlow * 1.35,
            unitCircle.y * 9.8 + radialDistance * 18.0 + slowerOutwardFlow * 0.38);
    vec2 dustUv = vec2(texCoord0.x * 192.0 + unitCircle.y * 2.4,
            radialDistance * 76.0 - slowerOutwardFlow * 2.05 + fbm(unitCircle * 2.0 + radialDistance) * 1.8);
    float numerousHazeFlecks = fbm(flowUv * 1.38)
            * (0.72 + fbm(flowUv * 0.47 + slowerOutwardFlow) * 0.28);
    float innerDenseDustMotes = dustMoteField(
            dustUv * vec2(1.32, 1.18) + vec2(47.0, slowerOutwardFlow * 0.25), 253.0);
    float individualDustMotes = max(max(dustMoteField(dustUv, 192.0),
            dustMoteField(dustUv * vec2(0.68, 1.23) + vec2(19.0, -slowerOutwardFlow * 0.55), 131.0) * 0.72),
            innerDenseDustMotes * portalEdgeDensityBoost);
    float smallerHazeFlecks = smoothstep(0.72, 0.96, numerousHazeFlecks)
            * (1.0 - smoothstep(0.0, 0.18, outwardPortalFlow))
            * smoothstep(0.0, 0.065, outwardPortalFlow);
    float softFleckHaze = smallerHazeFlecks * 0.22
            + smoothstep(0.64, 0.88, numerousHazeFlecks) * 0.24
            * (1.0 - smoothstep(0.0, 0.26, outwardPortalFlow))
            * smoothstep(0.0, 0.11, outwardPortalFlow);
    softFleckHaze *= visibleFieldEdgeTaper * magentaCyanFalloff;
    float voidEjectedDustMotes = individualDustMotes * firstFewRingMoteField
            * (0.62 + portalEdgeDensityBoost * 0.58)
            * smoothstep(CenterVoidRadius + 0.025, CenterVoidRadius + 0.11, radialDistance);
    float longLivedDustMotes = voidEjectedDustMotes
            * (1.0 - smoothstep(CenterVoidRadius + 0.24, CenterVoidRadius + 0.36, radialDistance));
    float pinkPurpleFlecks = (longLivedDustMotes + softFleckHaze * 0.26) * portalRimEmitter
            * (0.76 + outwardLift * 0.14 + smoothstep(0.40, 0.78, fbm(flowUv * 0.36 + slowerOutwardFlow)) * 0.10);
    float standaloneMoteVisibility = max(longLivedDustMotes, smallerHazeFlecks * 0.22) * portalRimEmitter;
    float moteOpacityFloor = standaloneMoteVisibility * (0.22 + longLivedDustMotes * 0.30);

    vec3 hotPink = vec3(1.0, 0.055, 0.70);
    vec3 deepPurple = vec3(0.54, 0.025, 1.0);
    vec3 paleViolet = vec3(1.0, 0.70, 1.0);
    vec3 brighterHazeCore = vec3(1.0, 0.86, 1.0);
    vec3 selfLitMoteEmission = vec3(1.0, 0.22, 0.92);
    vec3 hazeColor = mix(deepPurple, hotPink, smoothstep(0.16, 0.72, radialDistance));
    hazeColor = mix(hazeColor, paleViolet, softFleckHaze * 0.14);
    hazeColor += brighterHazeCore * longLivedDustMotes * 0.62;
    hazeColor += selfLitMoteEmission * standaloneMoteVisibility * 0.78;

    float alpha = (pinkPurpleFlecks + moteOpacityFloor) * HazeIntensity * centerAperture * vertexColor.a
            * ColorModulator.a;
    alpha *= 1.0 - smoothstep(0.82, 1.0, radialDistance);

    if (alpha < 0.004) {
        discard;
    }

    fragColor = vec4(hazeColor * ColorModulator.rgb, min(alpha, 0.78));
}
