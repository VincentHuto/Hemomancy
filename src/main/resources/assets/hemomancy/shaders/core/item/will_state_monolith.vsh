#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float HemoTime;
uniform float ShardSeed;
uniform float Burden;
uniform float Attuned;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out float burdenPulse;
out float attunedPulse;

const float BURDEN_PULSE_RATE = 2.2;
const float ATTUNED_PULSE_RATE = 1.1;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexDistance = fog_distance(Position, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
    burdenPulse = Burden * (0.65 + 0.35 * sin(HemoTime * BURDEN_PULSE_RATE + ShardSeed));
    attunedPulse = Attuned * (0.75 + 0.25 * sin(HemoTime * ATTUNED_PULSE_RATE + ShardSeed * 2.0));
}
