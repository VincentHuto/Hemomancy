#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float HemoTime;
uniform float OrbSeed;
uniform vec3 OrbCenter;
uniform float OrbRadius;
uniform float WritheStrength;
uniform float ThreadScale;
uniform float GlowLayer;

in vec3 Position;
in vec4 Color;

out float vertexDistance;
out vec4 vertexColor;
out vec3 orbNormal;
out float threadFlow;
out float surfaceLift;

float wave(vec3 p, vec3 axis, float rate, float phase) {
    return sin(dot(p, axis) * ThreadScale + HemoTime * rate + OrbSeed * phase);
}

void main() {
    vec3 dir = normalize(Position - OrbCenter + vec3(0.0001));
    float bandA = wave(dir, normalize(vec3(1.0, 0.35, 0.62)), 0.083, 1.7);
    float bandB = wave(dir, normalize(vec3(-0.54, 1.0, 0.27)), -0.071, 2.3);
    float bandC = wave(dir, normalize(vec3(0.22, -0.46, 1.0)), 0.119, 3.1);
    float knot = sin((dir.x * dir.z + dir.y * 0.45) * ThreadScale * 2.1 + HemoTime * 0.17 + OrbSeed * 5.0);
    float writhe = (bandA * 0.42 + bandB * 0.30 + bandC * 0.20 + knot * 0.18) * WritheStrength;
    float pulse = 0.62 + 0.38 * sin(HemoTime * 0.21 + OrbSeed * 1.9);
    float glowPush = GlowLayer * OrbRadius * 0.32;
    vec3 p = Position + dir * (glowPush + OrbRadius * writhe * pulse);

    gl_Position = ProjMat * ModelViewMat * vec4(p, 1.0);
    vertexDistance = fog_distance(p, FogShape);
    vertexColor = Color;
    orbNormal = dir;
    threadFlow = bandA * 0.48 + bandB * 0.34 + knot * 0.18;
    surfaceLift = writhe;
}
