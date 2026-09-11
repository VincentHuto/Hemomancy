#version 150
#moj_import <fog.glsl>
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
in vec3 Position;
in vec4 Color;
in vec2 UV0;
out float vertexDistance;
out vec4 vertexColor;
out vec2 uv;
out vec3 viewPosition;
flat out float shape;
flat out float seed;
void main() {
    vec4 view = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * view;
    viewPosition = view.xyz;
    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color;
    shape = floor(UV0.x / 2.0);
    seed = floor(UV0.y / 2.0);
    uv = UV0 - vec2(shape, seed) * 2.0;
}
