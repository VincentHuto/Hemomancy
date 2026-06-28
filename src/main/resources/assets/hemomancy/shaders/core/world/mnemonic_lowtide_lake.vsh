#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float HemoTime;
uniform float LakeSeed;
uniform float WaveStrength;
uniform float NoiseScale;
uniform float EdgeFade;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out float waveLiftAmount;

float wave(vec2 uv, vec2 axis, float scale, float speed, float phase) {
    return sin(dot(uv, axis) * scale + HemoTime * speed + LakeSeed * phase);
}

void main() {
    vec2 uv = UV0;
    float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeDamping = smoothstep(0.0, EdgeFade, edge);
    float detailScale = max(0.1, NoiseScale * 0.125);
    float broad = wave(uv, normalize(vec2(1.0, 0.31)), 13.0 * detailScale, 0.052, 0.017);
    float cross = wave(uv, normalize(vec2(-0.42, 1.0)), 21.0 * detailScale, -0.073, 0.029);
    float small = wave(uv + broad * 0.018, normalize(vec2(0.74, -0.57)), 37.0 * detailScale, 0.111, 0.041);
    float waveLift = (broad * 0.58 + cross * 0.32 + small * 0.10) * WaveStrength * edgeDamping;

    vec3 surfacePosition = Position;
    surfacePosition.y += waveLift;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexDistance = fog_distance(surfacePosition, FogShape);
    vertexColor = Color;
    texCoord0 = uv;
    waveLiftAmount = waveLift;
}
