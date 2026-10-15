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
out vec2 texCoord0;
out vec3 viewOffset;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
    // Camera-relative and world-aligned: the view ray, so sunken channels shift with the eye.
    viewOffset = Position;
}
