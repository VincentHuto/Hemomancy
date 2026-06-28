#version 150

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float HemoTime;
uniform float LakeSeed;
uniform float WaveStrength;
uniform float WaveDetailScale;
uniform float EdgeFade;

in vec3 Position;
in vec4 Color;
in vec2 UV0;

out vec4 vertexColor;
out vec2 texCoord0;
out float waveLiftAmount;
out float rippleHighlight;

float wave(vec2 uv, vec2 axis, float scale, float speed, float phase) {
    return sin(dot(uv, axis) * scale + HemoTime * speed + LakeSeed * phase);
}

void main() {
    vec2 uv = UV0;
    float edge = min(min(uv.x, 1.0 - uv.x), min(uv.y, 1.0 - uv.y));
    float edgeDamping = smoothstep(0.0, EdgeFade, edge);
    float waveDetailScale = max(0.1, WaveDetailScale);
    float broad = wave(uv, normalize(vec2(1.0, 0.31)), 13.0 * waveDetailScale, 0.052, 0.017);
    float cross = wave(uv, normalize(vec2(-0.42, 1.0)), 21.0 * waveDetailScale, -0.073, 0.029);
    float small = wave(uv + broad * 0.018, normalize(vec2(0.74, -0.57)), 37.0 * waveDetailScale, 0.111, 0.041);
    float fineRipples = wave(uv + cross * 0.010, normalize(vec2(0.92, 0.38)), 63.0 * waveDetailScale, 0.167, 0.053);
    float crossingRipples = wave(uv - broad * 0.012, normalize(vec2(-0.28, 0.96)), 78.0 * waveDetailScale, -0.191, 0.071);
    float waveLift = (broad * 0.43 + cross * 0.25 + small * 0.14
            + fineRipples * 0.12 + crossingRipples * 0.075) * WaveStrength * edgeDamping;

    vec3 surfacePosition = Position;
    surfacePosition.y += waveLift;

    gl_Position = ProjMat * ModelViewMat * vec4(surfacePosition, 1.0);
    vertexColor = Color;
    texCoord0 = uv;
    waveLiftAmount = waveLift;
    rippleHighlight = abs(fineRipples - crossingRipples) * edgeDamping;
}
