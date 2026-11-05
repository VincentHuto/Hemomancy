#version 150

#moj_import <fog.glsl>

// The vertex stage of the Monolith's tendrils. The stone they climb is drawn by item/monolith_fragment, whose vertex
// stage breathes the stone in and out; the same morph, with the same clock and strength, is applied here so the
// strands ride the surface instead of hovering a hair above it. Keep the formula word for word with that shader.

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float StoneSeed;
uniform float MorphTime;
uniform float MorphStrength;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

const float VERTEX_MORPH_RATE = 1.45;

void main() {
    vec3 p = Position;
    vec2 fromCenter = p.xz;
    float centerDistance = length(fromCenter);

    if (centerDistance > 0.0001) {
        float morph = sin(
            p.y * 10.0
            + p.x * 15.0
            - p.z * 13.0
            + MorphTime * VERTEX_MORPH_RATE
            + StoneSeed * 6.28318
        );
        float edgeWeight = smoothstep(0.0, 0.16, centerDistance);
        p.xz += normalize(fromCenter) * morph * MorphStrength * edgeWeight;
        p.y += sin((p.x - p.z) * 12.0 + MorphTime * 1.1 + StoneSeed * 4.2) * MorphStrength * 0.35 * edgeWeight;
    }

    gl_Position = ProjMat * ModelViewMat * vec4(p, 1.0);
    vertexDistance = fog_distance(p, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
}
