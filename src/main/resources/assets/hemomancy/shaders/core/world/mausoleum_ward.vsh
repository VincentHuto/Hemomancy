#version 150

// Shared by the ward on the stone (DefaultVertexFormat.BLOCK) and the ward burning into a body (NEW_ENTITY): both
// carry Position, Color and UV0 under the same names, and nothing else is read.

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
