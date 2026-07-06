#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float HemoTime;
uniform float OutwardSpeed;
uniform float CenterVoidRadius;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out float radialDistance;
out float centerAperture;
out float outwardLift;

void main() {
    float radial = UV0.y;
    float liftPulse = pow(max(0.0, sin(radial * 24.0 - HemoTime * OutwardSpeed * 2.4) * 0.5 + 0.5), 3.0);

    vec3 surfacePosition = Position;
    surfacePosition.y += liftPulse * radial * 0.24 + smoothstep(0.12, 0.72, radial) * 0.035;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    radialDistance = radial;
    centerAperture = smoothstep(CenterVoidRadius, CenterVoidRadius + 0.045, radial);
    outwardLift = liftPulse;
}
