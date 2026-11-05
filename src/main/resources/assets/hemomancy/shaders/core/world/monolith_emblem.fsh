#version 150

#moj_import <fog.glsl>

// The Monolith's emblems: the vertex colour, fogged. All the drawing is in the geometry.

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = vertexColor * ColorModulator;
    if (color.a < 0.004) discard;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
