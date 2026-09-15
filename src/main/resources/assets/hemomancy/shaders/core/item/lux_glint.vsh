#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform mat4 TextureMat;
in vec3 Position;
in vec4 Color;
in vec2 UV0;
out vec2 glintUv;
out float chargeAlpha;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    glintUv = (TextureMat * vec4(UV0, 0.0, 1.0)).xy;
    chargeAlpha = Color.a;
}
