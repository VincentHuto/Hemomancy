#version 150

#moj_import <fog.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float HemoTime;
uniform vec3 WoundCenter;
uniform float DrainProgress;

out float vertexDistance;
out vec2 texCoord0;
out vec3 cameraPosition;
out vec3 surfaceNormal;

void main() {
    // A thin swollen skin over the body, breathing with the wound's pulse.
    float breath = 0.5 + 0.5 * sin(HemoTime * (0.2 + DrainProgress * 0.25));
    vec3 n = normalize(Normal);
    vec3 p = Position + n * (0.018 + breath * 0.012);
    gl_Position = ProjMat * ModelViewMat * vec4(p, 1.0);
    vertexDistance = fog_distance(p, FogShape);
    texCoord0 = UV0;
    cameraPosition = p;
    surfaceNormal = n;
}
