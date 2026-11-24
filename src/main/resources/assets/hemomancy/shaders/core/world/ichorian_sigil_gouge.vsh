#version 150

#moj_import <fog.glsl>

// One Ichorian Sigil link's ground strip (CardinalRiteBoundaryRenderer.drawSigilGouges). Every stage value rides the
// vertex: UV1 is the cut-in and the fill, UV2 the glow and the vein reach, each times 1000; Normal x is the link's
// seed, y how far down the link (-1..1), z the angle of the across direction (left to right) over pi. The
// vein reach has 8000 added if the link's first node is a loose end of the pattern and 16000 if its last is.

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
out vec4 vertexColor;
out vec2 stripUv;
out vec4 stage;
out vec3 viewOffset;
out float linkSeed;
out float linkAlong;
out vec2 acrossDir;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color;
    stripUv = UV0;
    stage = vec4(vec2(UV1), vec2(UV2)) / 1000.0;
    // Camera-relative, so the fragment shader can follow the view ray down into the cut.
    viewOffset = Position;
    linkSeed = Normal.x;
    linkAlong = Normal.y * 0.5 + 0.5;
    float angle = Normal.z * 3.14159265;
    acrossDir = vec2(cos(angle), sin(angle));
}
