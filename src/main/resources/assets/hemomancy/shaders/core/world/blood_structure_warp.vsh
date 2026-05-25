#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float HemoTime;
uniform float Progress;
uniform float BlockSeed;
uniform float WiggleAmp;
uniform vec3 WarpCenter;
uniform float FinalizeProgress;
uniform float MeltGroundY;
uniform float MeltHeight;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out float pulse;

float hash1(float n) {
    return fract(sin(n) * 43758.5453123);
}

vec3 coherentWarpDirection(vec3 p) {
    return normalize(p - WarpCenter + vec3(0.001));
}

vec3 wiggleOffset(vec3 p) {
    vec3 warpDirection = coherentWarpDirection(p);
    vec3 local = p - WarpCenter;
    float phase = HemoTime * (0.18 + Progress * 0.14) + BlockSeed * 23.0;
    float surfaceWave = sin(dot(local, vec3(9.1, 6.7, 11.3)) + phase * 6.28318);
    float snap = hash1(floor(phase * 6.0) + dot(local, vec3(17.0, 31.0, 47.0)));
    float surfaceLift = 0.026 + Progress * 0.016;
    float surfaceWiggle = (surfaceWave * 0.35 + (snap - 0.5) * 0.35) * WiggleAmp;
    return warpDirection * (surfaceLift + surfaceWiggle);
}

void main() {
    vec3 p = Position + wiggleOffset(Position);
    p += coherentWarpDirection(Position) * Progress * 0.012;
    float heightAboveGround = max(Position.y - MeltGroundY, 0.0);
    float heightRatio = clamp(heightAboveGround / max(MeltHeight, 0.001), 0.0, 1.0);
    float heightDelay = heightRatio * 0.46;
    float liquidMelt = smoothstep(0.12, 1.0, FinalizeProgress);
    float bottomOutMelt = smoothstep(0.14 + heightDelay, 0.72 + heightDelay * 0.42, FinalizeProgress);
    float bottomPuddleSpread = smoothstep(0.08, 0.72, FinalizeProgress);
    float puddleSpread = bottomPuddleSpread * mix(1.0 - heightRatio, 1.0, bottomOutMelt);
    vec2 fromCenter = Position.xz - WarpCenter.xz;
    vec2 outward = normalize(fromCenter + vec2(0.001));
    float ripple = sin(length(fromCenter) * 5.2 - HemoTime * 0.22 + BlockSeed * 9.0) * 0.008 * puddleSpread;
    float puddleThickness = 0.026 + ripple;
    float targetY = MeltGroundY + puddleThickness;
    float spread = (0.14 + heightAboveGround * 0.22) * puddleSpread * liquidMelt;
    p.xz += outward * spread;
    p.y = mix(p.y, targetY, bottomOutMelt);

    gl_Position = ProjMat * ModelViewMat * vec4(p, 1.0);
    vertexDistance = fog_distance(p, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
    pulse = 0.55 + 0.45 * sin(HemoTime * 0.36 + BlockSeed * 17.0 + Progress * 3.14159);
}
