#version 150
#moj_import <fog.glsl>
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec3 Normal;
out float vertexDistance;
out vec4 vertexColor;
out vec2 bodyUv;
void main() {
    vec3 pos=Position+Normal*.003;
    gl_Position=ProjMat*ModelViewMat*vec4(pos,1.0);
    vertexDistance=fog_distance(pos,FogShape);
    vertexColor=Color;
    bodyUv=UV0;
}
