#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform float HemoTime;
uniform vec3 WoundCenter;
uniform float BodyYaw;
uniform float DrainProgress;
uniform float DrainFlash;
uniform float DrawSeed;

in float vertexDistance;
in vec2 texCoord0;
in vec3 cameraPosition;
in vec3 surfaceNormal;

out vec4 fragColor;

float hash3(vec3 p) {
    p = fract(p * 0.3183099 + vec3(0.11, 0.17, 0.13));
    p *= 17.0;
    return fract(p.x * p.y * p.z * (p.x + p.y + p.z));
}

float valueNoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = mix(mix(hash3(i), hash3(i + vec3(1, 0, 0)), f.x),
            mix(hash3(i + vec3(0, 1, 0)), hash3(i + vec3(1, 1, 0)), f.x), f.y);
    float b = mix(mix(hash3(i + vec3(0, 0, 1)), hash3(i + vec3(1, 0, 1)), f.x),
            mix(hash3(i + vec3(0, 1, 1)), hash3(i + vec3(1, 1, 1)), f.x), f.y);
    return mix(a, b, f.z);
}

void main() {
    if (texture(Sampler0, texCoord0).a < 0.1) discard;

    // Body-anchored coordinates around the wound, so the veins ride the mount instead of swimming with the camera.
    vec3 local = cameraPosition - WoundCenter;
    float c = cos(BodyYaw);
    float s = sin(BodyYaw);
    local = vec3(local.x * c + local.z * s, local.y, -local.x * s + local.z * c);
    float dist = length(local);

    // Branching vessels: thin bands where layered noise crosses its midline.
    float n = valueNoise(local * 1.15 + DrawSeed) * 0.65 + valueNoise(local * 2.7 - DrawSeed) * 0.35;
    float vessel = 1.0 - smoothstep(0.0, 0.055, abs(n - 0.5));

    // Heartbeats that run along the vessels toward the wound; faster and denser as he is drained.
    float speed = 0.07 + DrainProgress * 0.09;
    float beat = fract(dist * 0.55 + HemoTime * speed + n * 0.35);
    float surge = smoothstep(0.62, 0.96, beat) * (1.0 - smoothstep(0.96, 1.0, beat));

    // The silhouette glows so the whole body reads as "open" from any angle.
    vec3 viewDir = normalize(-cameraPosition);
    float rim = pow(1.0 - abs(dot(surfaceNormal, viewDir)), 2.2);
    float near = 1.0 - smoothstep(0.4, 3.2, dist);
    float throb = 0.5 + 0.5 * sin(HemoTime * (0.22 + DrainProgress * 0.3));

    // Flat cube faces take a uniform rim, so keep the fill low and let the beating vessels carry the read.
    float glow = rim * (0.22 + 0.14 * throb)
            + vessel * (0.30 + 0.70 * surge)
            + near * (0.16 + 0.24 * throb)
            + 0.05 + DrainFlash * 0.18;
    glow *= 0.85 + DrainProgress * 0.2;

    vec3 deep = vec3(0.55, 0.0, 0.02);
    vec3 hot = vec3(1.0, 0.16, 0.08);
    vec3 color = mix(deep, hot, clamp(vessel * surge + near * 0.5 + DrainFlash * 0.4, 0.0, 1.0));
    // Additive glow fades out into fog rather than tinting toward the fog colour.
    float fogFade = 1.0 - smoothstep(FogStart, FogEnd, vertexDistance);
    float alpha = clamp(glow, 0.0, 1.0) * ColorModulator.a * fogFade;
    fragColor = vec4(color * ColorModulator.rgb, alpha);
}
