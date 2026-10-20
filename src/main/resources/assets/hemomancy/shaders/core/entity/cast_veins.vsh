#version 150
#moj_import <fog.glsl>
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;
out float vertexDistance;
out vec2 skinUv;
flat out vec4 encoded;
flat out ivec2 overlayData;
flat out ivec2 lightData;
void main() {
    // Lift just above the hat and jacket layer so the veins sit on the outermost surface.
    vec3 pos=Position+Normal*.035;
    gl_Position=ProjMat*ModelViewMat*vec4(pos,1.0);
    vertexDistance=fog_distance(pos,FogShape);
    skinUv=UV0;
    encoded=Color;
    overlayData=UV1;
    lightData=UV2;
}
