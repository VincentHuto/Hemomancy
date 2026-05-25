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

    gl_Position = ProjMat * ModelViewMat * vec4(p, 1.0);
    vertexDistance = fog_distance(p, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
    pulse = 0.55 + 0.45 * sin(HemoTime * 0.36 + BlockSeed * 17.0 + Progress * 3.14159);
}
