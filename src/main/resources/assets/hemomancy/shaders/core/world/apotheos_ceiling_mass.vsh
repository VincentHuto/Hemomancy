#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float HemoTime;
uniform float RotationSpeed;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out float ceilingAngleT;
out float ceilingRadialT;
out float ceilingBowlDepth;
out float organicShift;

void main() {
    float angle = UV0.x * 6.2831853;
    float radial = UV0.y;
    float time = HemoTime * RotationSpeed;
    float foldA = sin(angle * 3.0 + radial * 8.0 + time * 1.6);
    float foldB = sin(angle * 7.0 - radial * 5.0 - time * 1.1);
    float inwardWeight = pow(1.0 - radial, 1.35);
    organicShift = (foldA * 0.035 + foldB * 0.018) * (0.28 + inwardWeight * 0.72)
            - inwardWeight * 0.075;

    vec3 surfacePosition = Position;
    surfacePosition.y += organicShift;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    ceilingAngleT = UV0.x;
    ceilingRadialT = radial;
    ceilingBowlDepth = inwardWeight;
}
