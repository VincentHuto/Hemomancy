#version 150

// The heart-node spread (HeartNodeSpreadRenderer), in DefaultVertexFormat.BLOCK: Color carries the corner's packed
// values (see heart_node_spread.fsh), UV0.x the pulse's run along the face's nearest thread, UV2 the world light of
// the open cell over the face, and Normal the face's side.

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform sampler2D Sampler2;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

out vec4 vertexColor;
out float pathLength;
out vec3 surfacePos;
out vec3 faceNormal;
out vec4 lightColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    pathLength = UV0.x;
    // Camera-relative, the space every lobe and the viewer's feet are given in.
    surfacePos = Position;
    faceNormal = Normal;
    lightColor = texelFetch(Sampler2, UV2 / 16, 0);
}
