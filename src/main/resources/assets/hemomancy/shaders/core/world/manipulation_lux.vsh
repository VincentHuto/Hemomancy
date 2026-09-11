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
out vec2 flowUv;
flat out float flowShape;
flat out float flowSeed;
void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color;
    flowShape = floor(UV0.x / 2.0);
    flowSeed = floor(UV0.y / 2.0);
    flowUv = UV0 - vec2(flowShape, flowSeed) * 2.0;
}
