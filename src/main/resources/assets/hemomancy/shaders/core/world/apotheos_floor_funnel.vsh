#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float HemoTime;
uniform float RingRise;
uniform float CenterVoidRadius;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out float funnelDepth;
out float centerAperture;
out float ringHeightBias;

void main() {
    float radial = UV0.y;
    float ringPhase = radial * 31.0 - HemoTime * 2.85;
    float bandLift = pow(max(0.0, sin(ringPhase) * 0.5 + 0.5), 4.0) * RingRise * radial;
    float outerRise = pow(radial, 1.85) * RingRise * 0.88;
    float funnelDrop = pow(1.0 - radial, 1.72) * RingRise * 0.58;

    vec3 surfacePosition = Position;
    surfacePosition.y += outerRise + bandLift - funnelDrop;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    funnelDepth = clamp(1.0 - radial, 0.0, 1.0);
    centerAperture = smoothstep(CenterVoidRadius, CenterVoidRadius + 0.045, radial);
    ringHeightBias = clamp(outerRise + bandLift, 0.0, 2.0);
}
