#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 HazeShape;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec3 localPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // The box's own coordinates ride in UV (x, z) and red (height), so the volume never depends on the view.
    localPos = vec3(UV0.x, mix(-HazeShape.w, HazeShape.w, Color.r), UV0.y);
}
