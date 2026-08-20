#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float HemoTime;
uniform float HoleSeed;
uniform float LensStrength;
uniform float RingIntensity;
uniform float FinalHole;
uniform float TreeApex;
uniform vec2 ScreenSize;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(vec2 value) {
    return fract(sin(dot(value, vec2(127.1, 311.7)) + HoleSeed * 71.13) * 43758.5453);
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
        value = turn * value * 2.04 + vec2(1.31, -0.73);
        amplitude *= 0.5;
    }
    return total;
}

void main() {
    vec2 uv = texCoord0;
    vec2 centered = uv * 2.0 - 1.0;
    centered.x *= 1.03;
    float radius = length(centered);
    float angle = atan(centered.y, centered.x);
    float time = HemoTime + HoleSeed * 19.0;

    float safeRadius = max(radius, 0.035);
    float swirl = time * mix(1.7, 0.72, FinalHole) + LensStrength / safeRadius;
    float spiralNoise = fbm(vec2(angle * 0.74 + time * 0.27, safeRadius * 6.2 - time * 0.18));
    vec2 lensDirection = normalize(centered + vec2(0.0007, -0.0004));
    float lensFalloff = smoothstep(1.12, 0.08, radius);
    float lensAmount = LensStrength * lensFalloff / (safeRadius * 3.65 + 0.045);
    vec2 tangential = vec2(-lensDirection.y, lensDirection.x);
    vec2 lensOffset = lensDirection * lensAmount * mix(0.54, 0.82, FinalHole);
    vec2 shearOffset = tangential * (0.068 + FinalHole * 0.052)
            * sin(swirl + spiralNoise * 4.8) * lensFalloff;
    vec2 lensedUv = uv - lensOffset + shearOffset;
    float chromaticOffset = (0.010 + FinalHole * 0.012) * lensFalloff * smoothstep(0.88, 0.18, radius);
    vec3 warpedSky = vec3(
            texture(Sampler0, fract(lensedUv + lensDirection * chromaticOffset)).r,
            texture(Sampler0, fract(lensedUv)).g,
            texture(Sampler0, fract(lensedUv - lensDirection * chromaticOffset)).b
    );

    if (TreeApex > 0.5) {
        float x = centered.x;
        float y = centered.y;
        float absoluteX = abs(x);
        float shadowRadius = length(vec2(x, y * 0.96));
        float eventHorizon = smoothstep(0.318, 0.292, shadowRadius);

        // The direct disk and both gravitational images are one surface.  The
        // rear half rises over and under the shadow, then converges back into
        // the direct sheet at the left and right edges instead of becoming a
        // detached circular halo.
        float diskReach = smoothstep(1.04, 0.91, absoluteX);
        float diskCenter = -0.040 + x * 0.006;
        float diskThickness = mix(0.024, 0.060, smoothstep(0.28, 1.0, absoluteX));
        float directDisk = smoothstep(diskThickness, diskThickness * 0.18,
                abs(y - diskCenter)) * diskReach;
        float accretionDisk = directDisk;

        float lensDomain = clamp(absoluteX / 0.86, 0.0, 1.0);
        float lensArch = sqrt(max(0.0, 1.0 - lensDomain * lensDomain));
        float upperCenter = diskCenter + 0.565 * pow(lensArch, 0.72);
        float lowerCenter = diskCenter - 0.365 * pow(lensArch, 0.78);
        float imageThickness = 0.030 + 0.034 * lensArch;
        float imageReach = smoothstep(0.91, 0.84, absoluteX);
        float rearUpperImage = smoothstep(imageThickness, imageThickness * 0.20,
                abs(y - upperCenter)) * imageReach;
        float rearLowerImage = smoothstep(imageThickness * 0.82, imageThickness * 0.16,
                abs(y - lowerCenter)) * imageReach;
        float upperLens = rearUpperImage;
        float lowerLens = rearLowerImage;

        float upperDistance = abs(y - upperCenter) / max(imageThickness, 0.001);
        float radialBand = sin((lensArch * 72.0 - upperDistance * 11.0 - time * 2.4)
                + fbm(vec2(angle * 3.0, shadowRadius * 9.0 - time * 0.22)) * 8.0);
        float horizontalBand = sin((absoluteX * 84.0 - time * 3.1)
                + fbm(vec2(x * 7.0 - time * 0.18, y * 31.0)) * 7.0);
        float flowBands = 0.48 + 0.30 * radialBand + 0.22 * horizontalBand;
        float darkStriations = smoothstep(0.40, 0.66,
                fbm(vec2(angle * 6.0 - time * 0.36, shadowRadius * 18.0 + time * 0.15)));
        float doppler = mix(0.68, 1.24, smoothstep(-0.92, 0.72, x));

        vec2 sceneUv = gl_FragCoord.xy / ScreenSize;
        vec2 sceneDirection = normalize(centered + vec2(0.0004, -0.0003));
        float photonShell = smoothstep(0.88, 0.34, shadowRadius)
                * smoothstep(0.292, 0.338, shadowRadius);
        float coronaNoise = fbm(vec2(angle * 2.6 - time * 0.16,
                shadowRadius * 7.5 + time * 0.11));
        float primaryPulse = 0.5 + 0.5 * sin(time * 1.18);
        float secondaryPulse = 0.5 + 0.5 * sin(time * 2.37 + sin(time * 0.41) * 1.4);
        float alienPulse = 0.66 + primaryPulse * 0.25 + secondaryPulse * 0.09;
        float alienCorona = smoothstep(0.92, 0.34, shadowRadius)
                * smoothstep(0.304, 0.348, shadowRadius)
                * (0.42 + coronaNoise * 0.58);
        alienCorona *= alienPulse;
        float horizonRim = smoothstep(0.355, 0.324, shadowRadius)
                * smoothstep(0.304, 0.322, shadowRadius);
        horizonRim *= mix(0.76, 1.18, primaryPulse);
        float sceneLensFalloff = smoothstep(0.91, 0.30, shadowRadius);
        float inverseRadiusDeflection = LensStrength * photonShell
                * (0.006 + 0.014 / max(shadowRadius, 0.22));
        vec2 sceneTangent = vec2(-sceneDirection.y, sceneDirection.x);
        vec2 lensedSceneUv = sceneUv + sceneDirection * inverseRadiusDeflection
                + sceneTangent * sin(angle * 3.0 - time * 0.72)
                * inverseRadiusDeflection * 0.10 * sceneLensFalloff;
        vec3 lensedScene = texture(Sampler0, clamp(lensedSceneUv, vec2(0.001), vec2(0.999))).rgb;

        float diskFlow = max(directDisk, max(rearUpperImage, rearLowerImage));
        float redFlow = diskFlow;
        float horizonContact = (1.0 - smoothstep(0.303, 0.338, shadowRadius))
                * smoothstep(0.286, 0.302, shadowRadius)
                * (1.0 - smoothstep(0.045, 0.30, abs(y)));
        redFlow = max(redFlow, horizonContact);
        float hotFilaments = redFlow * smoothstep(0.46, 0.93, flowBands) * (1.0 - darkStriations * 0.82);
        vec3 bloodRed = vec3(0.42, 0.002, 0.006);
        vec3 arterialRed = vec3(1.0, 0.025, 0.008);
        vec3 hotRed = vec3(1.0, 0.16, 0.025);
        vec3 diskColor = mix(bloodRed, arterialRed, clamp(flowBands * doppler, 0.0, 1.0));
        diskColor = mix(diskColor, hotRed, hotFilaments * 0.72);
        vec3 alienTint = mix(vec3(0.34, 0.006, 0.090), vec3(0.18, 0.038, 0.70),
                0.45 + 0.30 * sin(angle * 2.0 + time * 0.31));
        vec3 coronaGlow = alienTint * alienCorona * (0.52 + RingIntensity * 0.42)
                + vec3(1.0, 0.025, 0.012) * horizonRim * 0.92;

        float shadow = eventHorizon;
        float outerLensing = photonShell * (1.0 - redFlow) * 0.48;
        vec3 color = lensedScene * (0.82 + photonShell * 0.18);
        color = mix(color, color * vec3(1.08, 0.82, 1.13) + alienTint * 0.24,
                alienCorona * 0.46);
        color = mix(color, diskColor, clamp(redFlow * (0.72 + hotFilaments), 0.0, 1.0));
        color *= 1.0 - shadow;
        coronaGlow *= 1.0 - eventHorizon;
        color += coronaGlow;

        float edgeFade = smoothstep(0.0, 0.11,
                min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y)));
        float alpha = edgeFade * vertexColor.a * ColorModulator.a;
        alpha *= clamp(eventHorizon * 1.24 + redFlow * (0.88 + hotFilaments * RingIntensity)
                + photonShell * 0.54 + alienCorona * 0.82 + horizonRim
                + outerLensing, 0.0, 1.0);
        if (alpha < 0.01) discard;
        fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha),
                vertexDistance, FogStart, FogEnd, FogColor);
        return;
    }

    float coreRadius = mix(0.162, 0.212, FinalHole);
    float eventHorizon = smoothstep(coreRadius + 0.035, coreRadius - 0.018, radius);
    float photonRing = smoothstep(coreRadius + 0.185, coreRadius + 0.038, radius)
            * smoothstep(coreRadius - 0.006, coreRadius + 0.038, radius);
    float outerRing = smoothstep(0.86, 0.30, radius) * smoothstep(coreRadius + 0.055, coreRadius + 0.26, radius);
    float spiral = smoothstep(0.22, 0.92,
            sin(angle * (4.4 + FinalHole * 1.8) - log(safeRadius) * 6.5 + time * 2.6 + spiralNoise * 4.2)
                    * 0.5 + 0.5);
    float tornMist = fbm(centered * vec2(4.2, 2.8) + vec2(time * 0.32, -time * 0.21));

    vec3 deepRed = vec3(0.54, 0.015, 0.018);
    vec3 arterial = vec3(0.96, 0.085, 0.035);
    vec3 bruisedBlue = vec3(0.11, 0.055, 0.24);
    vec3 ringColor = mix(deepRed, arterial, clamp(photonRing * 1.4 + spiral * 0.45, 0.0, 1.0));
    ringColor = mix(ringColor, bruisedBlue, clamp((1.0 - spiral) * outerRing * 0.36, 0.0, 1.0));
    float lensWindow = (1.0 - smoothstep(coreRadius + 0.30, 0.92, radius)) * smoothstep(coreRadius + 0.020, coreRadius + 0.120, radius);
    vec3 magnifiedSky = warpedSky * (0.78 + lensWindow * 0.70 + outerRing * 0.22);
    float ringBlend = clamp(photonRing * 1.08 + outerRing * spiral * 0.28 - lensWindow * 0.42, 0.0, 1.0);
    vec3 color = mix(magnifiedSky, ringColor, ringBlend);
    color *= 1.0 - eventHorizon;
    color += ringColor * photonRing * RingIntensity * (0.62 + tornMist * 0.48);

    float edgeFade = smoothstep(0.0, 0.13, min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y)));
    float residualFade = mix(1.0, 1.18, FinalHole);
    float alpha = edgeFade * vertexColor.a * ColorModulator.a;
    alpha *= clamp(eventHorizon * 1.18 + photonRing * 1.32 + lensWindow * 0.88
            + outerRing * (0.20 + spiral * 0.28), 0.0, residualFade);
    alpha *= 0.78 + FinalHole * 0.20;

    if (alpha < 0.01) {
        discard;
    }

    fragColor = linear_fog(vec4(color * ColorModulator.rgb, alpha), vertexDistance, FogStart, FogEnd, FogColor);
}
