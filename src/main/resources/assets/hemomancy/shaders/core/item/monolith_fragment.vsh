#version 150

#moj_import <fog.glsl>

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform int FogShape;
uniform float HemoTime;
uniform float ShardSeed;
uniform float Burden;
uniform float Attuned;
uniform float Snap;
uniform float GuiClamp;

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out float burdenPulse;
out float attunedPulse;

const float SNAP_RATE_BASE = 0.85;
const float SNAP_RATE_BURDEN = 0.55;
const float FOLD_PHASE_RATE = 0.18;
const float TREMOR_RATE = 5.4;
const float BURDEN_PULSE_RATE = 2.2;
const float ATTUNED_PULSE_RATE = 1.1;

float hash1(float n) {
    return fract(sin(n) * 43758.5453123);
}

float smooth01(float value) {
    return value * value * (3.0 - 2.0 * value);
}

float phaseNoise(float phase, float offset) {
    float basePhase = floor(phase);
    float blend = smooth01(fract(phase));
    return mix(hash1(basePhase + offset), hash1(basePhase + 1.0 + offset), blend);
}

vec3 foldVector(vec3 p, float snapPhase) {
    vec3 axis = normalize(vec3(
        phaseNoise(snapPhase, ShardSeed * 31.17 + 1.0) - 0.5,
        phaseNoise(snapPhase, ShardSeed * 37.23 + 2.0) - 0.5,
        phaseNoise(snapPhase, ShardSeed * 41.29 + 3.0) - 0.5
    ));
    float fold = sin(dot(p, axis) * 17.0 + snapPhase * 0.29 + HemoTime * FOLD_PHASE_RATE + ShardSeed) * (0.018 + Burden * 0.028);
    return p + axis * fold;
}

void main() {
    float snapPhase = HemoTime * (SNAP_RATE_BASE + Burden * SNAP_RATE_BURDEN) + ShardSeed * 13.0;
    float snapNoise = phaseNoise(snapPhase, ShardSeed * 19.0) - 0.5;
    vec3 p = Position;

    if (GuiClamp < 0.5) {
        p = foldVector(p, snapPhase);
        p.x += p.y * snapNoise * (0.22 + Burden * 0.32);
        p.z += p.x * (phaseNoise(snapPhase, 91.0) - 0.5) * (0.14 + Burden * 0.20);
        p *= 1.0 + (phaseNoise(snapPhase, 7.0) - 0.5) * Snap * 0.18;
        p += normalize(p + vec3(0.001)) * sin(HemoTime * TREMOR_RATE + ShardSeed * 3.1) * (0.006 + Burden * 0.012);
    }

    gl_Position = ProjMat * ModelViewMat * vec4(p, 1.0);
    vertexDistance = fog_distance(p, FogShape);
    vertexColor = Color;
    texCoord0 = UV0;
    burdenPulse = Burden * (0.65 + 0.35 * sin(HemoTime * BURDEN_PULSE_RATE + ShardSeed));
    attunedPulse = Attuned * (0.75 + 0.25 * sin(HemoTime * ATTUNED_PULSE_RATE + ShardSeed * 2.0));
}
