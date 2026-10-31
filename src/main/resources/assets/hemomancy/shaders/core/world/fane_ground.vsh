#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 faceUv;
out vec3 surfacePos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    faceUv = UV0;
    // Camera-relative, the space every offset uniform is given in.
    surfacePos = Position;
}
