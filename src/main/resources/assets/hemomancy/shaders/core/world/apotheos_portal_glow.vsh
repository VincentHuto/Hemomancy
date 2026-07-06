#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float CenterVoidRadius;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out float radialDistance;
out float centerAperture;

void main() {
    float radial = UV0.y;

    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    texCoord0 = UV0;
    radialDistance = radial;
    centerAperture = smoothstep(CenterVoidRadius, CenterVoidRadius + 0.045, radial);
}
