#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

out float vertexDistance;
out vec4 vertexColor;
out vec3 surfacePos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color;
    // Camera-relative point on the block face; the veins are laid out from VeinOrigin in the same space.
    surfacePos = Position;
}
