#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float HemoTime;
uniform float ShardSeed;
uniform float Burden;
uniform float Attuned;
uniform float VertexWarp;

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
const float VERTEX_MORPH_RATE = 1.45;
const float VERTEX_MORPH_STRENGTH = 0.014;

void main() {
    vec3 p = Position;
    vec2 fromCenter = p.xz;
    float centerDistance = length(fromCenter);

    if (centerDistance > 0.0001) {
        float morph = sin(
            p.y * 10.0
            + p.x * 15.0
            - p.z * 13.0
            + HemoTime * VERTEX_MORPH_RATE
            + ShardSeed * 6.28318
        );
        float strength = VERTEX_MORPH_STRENGTH * VertexWarp * (1.0 + Burden * 0.45 + Attuned * 0.25);
        float edgeWeight = smoothstep(0.0, 0.16, centerDistance);
        p.xz += normalize(fromCenter) * morph * strength * edgeWeight;
        p.y += sin((p.x - p.z) * 12.0 + HemoTime * 1.1 + ShardSeed * 4.2) * strength * 0.35 * edgeWeight;
    }

    gl_Position = ProjMat * ModelViewMat * vec4(p, 1.0);
    vertexDistance = fog_distance(p, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
    burdenPulse = Burden * (0.65 + 0.35 * sin(HemoTime * BURDEN_PULSE_RATE + ShardSeed));
    attunedPulse = Attuned * (0.75 + 0.25 * sin(HemoTime * ATTUNED_PULSE_RATE + ShardSeed * 2.0));
}
