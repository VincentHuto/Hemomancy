#version 150

#moj_import <fog.glsl>

// Re-draws the Latent Infection's model just proud of its skin, carrying view-space position and normal so the
// fragment shader can find the silhouette.

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out vec3 viewNormal;
out vec3 viewPosition;

void main() {
    vec3 pos = Position + Normal * 0.004;
    vec4 view = ModelViewMat * vec4(pos, 1.0);
    gl_Position = ProjMat * view;
    vertexDistance = fog_distance(pos, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
    viewNormal = mat3(ModelViewMat) * Normal;
    viewPosition = view.xyz;
}
