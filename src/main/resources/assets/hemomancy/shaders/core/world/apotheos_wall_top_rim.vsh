#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out float rimAngleT;
out float rimWidthT;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    rimAngleT = UV0.x;
    rimWidthT = UV0.y;
}
