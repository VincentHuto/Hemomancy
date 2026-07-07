#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float HemoTime;
uniform float RotationSpeed;
uniform float StormIntensity;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out float ceilingAngleT;
out float ceilingRadialT;
out float ceilingBowlDepth;
out float stormShift;

void main() {
    float angle = UV0.x * 6.2831853;
    float radial = UV0.y;
    float time = HemoTime * RotationSpeed;
    float inwardWeight = pow(1.0 - radial, 1.10);
    float stormWrithe = sin(angle * 9.0 + radial * 17.0 + time * 3.2)
            + sin(angle * 4.0 - radial * 23.0 - time * 2.1) * 0.58
            + sin(angle * 15.0 + time * 1.4) * 0.22;
    stormShift = stormWrithe * 0.030 * StormIntensity * (0.28 + radial * 0.92);

    vec3 surfacePosition = Position;
    surfacePosition.y += stormShift;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    ceilingAngleT = UV0.x;
    ceilingRadialT = radial;
    ceilingBowlDepth = inwardWeight;
}
